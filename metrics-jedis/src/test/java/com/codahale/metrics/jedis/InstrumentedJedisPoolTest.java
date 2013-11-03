package com.codahale.metrics.jedis;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.BeforeClass;
import org.junit.Test;

import redis.clients.jedis.Protocol;
import redis.clients.jedis.Tuple;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import com.codahale.metrics.Timer;
import com.codahale.metrics.threadpool.InstrumentedThreadPoolExecutor;

/**
 * @author softmentor
 * 
 */
public class InstrumentedJedisPoolTest {

  private static InstrumentedJedisPool JEDIS_POOL;
  private static IJedisProxy iJedisProxy = mock(IJedisProxy.class);;
  private static final String host = "localhost";
  private static final int port = Protocol.DEFAULT_PORT;
  private int maxRedisWrites = 100;
  // thread pool setting
  private static InstrumentedThreadPoolExecutor threadPool;
  private static int poolSize = 2;
  private static int maxPoolSize = 4;
  private static int keepAlive = 10;
  private static int maxJobs = 10;

  private static MetricRegistry registry;
  private Gauge<Integer> gaugeCorePoolSize = null;
  private Gauge<Integer> gaugeCurrentPoolSize = null;
  private Gauge<Integer> gaugeActiveCount = null;
  private Gauge<Integer> gaugePercentIdle = null;
  private Gauge<Integer> gaugePendingJobs = null;
  private Timer tasksExecTimer = null;
  private Timer workerRunTimer = null;
  

  @BeforeClass
  public static void setupPool() {
    System.out.println("BeforeClass");

    registry = SharedMetricRegistries.getOrCreate("default");
    if (null == registry) {
      System.out.println("registry is null");
    } else {
      System.out.println("All registries" + registry.getNames());
    }
    InstrumentedJedisPool thePool = new InstrumentedJedisPool(host, port, registry);
    JEDIS_POOL = thePool;

    JedisProxy jedisProxy = new JedisProxy(JEDIS_POOL);
    
    // RejectedExecutionHandler implementation
    RejectedExecutionHandlerImpl rejectionHandler = new RejectedExecutionHandlerImpl();
    // Get the ThreadFactory implementation to use
    ThreadFactory threadFactory = Executors.defaultThreadFactory();
    // Create the MetricsRegistry
    registry = new MetricRegistry();
    // creating the ThreadPoolExecutor
    threadPool =
        new InstrumentedThreadPoolExecutor(poolSize, maxPoolSize, keepAlive, TimeUnit.SECONDS,
            new ArrayBlockingQueue<Runnable>(2), threadFactory, rejectionHandler, registry, "test");

    // ConsoleReporter.enable(20, TimeUnit.MILLISECONDS);

  }

  @SuppressWarnings("unchecked")
  @Test
  public void testJedisPoolMetrics() throws Exception {
    System.out.println("Starting test, list registries" + registry.getNames());
    gaugeActiveCount =
        (Gauge<Integer>) registry.getGauges().get(InstrumentedThreadPoolExecutor.ACTIVE_THREADS);
    gaugeCorePoolSize =
        (Gauge<Integer>) registry.getGauges().get(InstrumentedThreadPoolExecutor.COREPOOL_SIZE);
    gaugeCurrentPoolSize =
        (Gauge<Integer>) registry.getGauges().get(InstrumentedThreadPoolExecutor.CURRENT_POOL_SIZE);
    gaugePercentIdle =
        (Gauge<Integer>) registry.getGauges().get(InstrumentedThreadPoolExecutor.PERCENT_IDLE);
    gaugePendingJobs =
        (Gauge<Integer>) registry.getGauges().get(InstrumentedThreadPoolExecutor.PENDING_JOBS);
    tasksExecTimer =
        (Timer) registry.getTimers().get(InstrumentedThreadPoolExecutor.TASKS_EXECUTED);
    workerRunTimer = (Timer) registry.getTimers().get(InstrumentedThreadPoolExecutor.WORKER_TIME);

    // ====================================================
    // start the monitoring thread
    int monitorThreadDelay = 1; // in sec
    MonitorThread monitor = new MonitorThread(this.threadPool, monitorThreadDelay);
    Thread monitorThread = new Thread(monitor);
    monitorThread.start();

    // submit work to the thread pool
    for (int i = 0; i < maxJobs; i++) {
      Runnable r = new WorkerThread("cmd" + i);
      this.threadPool.submit(r);
      System.out.println("Submitted runnable Id=" + r.hashCode());
    }

    Thread.sleep(5000);
    // shut down the pool
    this.threadPool.shutdown();
    // shut down the monitor thread
    while (!this.threadPool.isTerminated()) {
      System.out.println("Waiting for Workers to finish the task");
      // wait and see for one sec
      Thread.sleep(1000);
    }
    System.out.println("Finished all threads");
    monitor.shutdown();

  }

  @Test
  public void testSomething() {
    // System.out.println("Testing Something ===============");
  }

  /**
   * @author softmentor
   * 
   */
  public class WorkerThread implements Runnable {

    private String command;

    public WorkerThread(String s) {
      this.command = s;
    }

    @Override
    public void run() {
      System.out.println(Thread.currentThread().getName() + " Run started for Command Id= "
          + hashCode());
      processCommand();
      // System.out.println(Thread.currentThread().getName() + " End.");
    }

    private void processCommand() {
      try {
        String testKey = "testkey-" + Thread.currentThread().getName() + "-" + hashCode();
        //mock actual redis call
        when(iJedisProxy.zadd(testKey, System.currentTimeMillis(), "")).thenReturn(new Long("1"));
        for (int i = 0; i <= maxRedisWrites; i++) {
          String testValue = "testvalue-" + i;
          iJedisProxy.zadd(testKey, System.currentTimeMillis(), testValue);
          // Thread.sleep(100);
        }
        //mock the actual jedis call
        when(iJedisProxy.zcard(testKey)).thenReturn(new Long("10"));
        assertTrue(new Long("10").equals(iJedisProxy.zcard(testKey)));
        
        when(iJedisProxy.zrangeByScoreWithScores(testKey, 0, -1)).thenReturn(new HashSet<Tuple>());
        System.out.println("key=" + testKey + "  Value="
            + iJedisProxy.zrangeByScoreWithScores(testKey, 0, -1));
        /*
         * } catch (InterruptedException e) { e.printStackTrace();
         */
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    @Override
    public String toString() {
      return this.command;
    }
  }

  /**
   * @author softmentor
   * 
   */
  public static class RejectedExecutionHandlerImpl implements RejectedExecutionHandler {

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
      System.out.println("[rejection-handler] Thread ActiveCount" + executor.getActiveCount()
          + " Id=" + r.hashCode() + r.toString() + " is rejected");
    }

  }

  /**
   * @author softmentor
   * 
   */
  public class MonitorThread implements Runnable {
    private ThreadPoolExecutor executor;

    private int seconds;

    private boolean run = true;

    public MonitorThread(ThreadPoolExecutor executor, int delay) {
      this.executor = executor;
      this.seconds = delay;
    }

    public void shutdown() {
      this.run = false;
    }

    @Override
    public void run() {
      while (run) {
        System.out
            .println(String
                .format(
                    "[sysout-monitor-executor-stats] [%d/%d] Active: %d, Completed: %d, Task: %d, isShutdown: %s, isTerminated: %s",
                    this.executor.getPoolSize(), this.executor.getCorePoolSize(),
                    this.executor.getActiveCount(), this.executor.getCompletedTaskCount(),
                    this.executor.getTaskCount(), this.executor.isShutdown(),
                    this.executor.isTerminated()));
        if (null != gaugeCurrentPoolSize){
        System.out
            .println(String
                .format(
                    "[sysout-monitor-metric-gauge] [%d/%d] Active: %d, PercentageIdle: %d, PendingTasks: %d, isShutdown: %s, isTerminated: %s",
                    gaugeCurrentPoolSize.getValue(), gaugeCorePoolSize.getValue(),
                    gaugeActiveCount.getValue(), gaugePercentIdle.getValue(),
                    gaugePendingJobs.getValue(), this.executor.isShutdown(),
                    this.executor.isTerminated()));
        assertThat(this.executor.getPoolSize()).isEqualTo(gaugeCurrentPoolSize.getValue());
        assertThat(this.executor.getCorePoolSize()).isEqualTo(gaugeCorePoolSize.getValue());
        assertThat(this.executor.getActiveCount()).isEqualTo(gaugeActiveCount.getValue());
        System.out.println(String.format(
            "[sysout-monitor-metric-submittasktimer] Min: %s, Max: %s, Mean: %s, Count: %d",
            tasksExecTimer.getSnapshot().getMin(), tasksExecTimer.getSnapshot().getMax(),
            tasksExecTimer.getSnapshot().getMean(), tasksExecTimer.getCount()));
        System.out.println(String.format(
            "[sysout-monitor-metric-workertimer] Min: %s, Max: %s, Mean: %s, Count: %d m1: %s",
            workerRunTimer.getSnapshot().getMin(), workerRunTimer.getSnapshot().getMax(),
            workerRunTimer.getSnapshot().getMean(), workerRunTimer.getCount(),
            workerRunTimer.getMeanRate()));
        } else {
          System.out.println("gaugeCurrentPoolSize is null");
        }

        try {
          Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }

    }
  }
}
