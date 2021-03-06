package com.yammer.metrics.threadpool.tests;

import static org.fest.assertions.api.Assertions.assertThat;

import java.io.File;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import com.codahale.metrics.Timer;
import com.codahale.metrics.threadpool.InstrumentedThreadPoolExecutor;

/**
 * @author softmentor
 * 
 */
public class InstrumentedThreadPoolExecutorTest {
  private InstrumentedThreadPoolExecutor threadPool;
  private int poolSize = 2;
  private int maxPoolSize = 4;
  private int keepAlive = 10;
  private int maxJobs = 10;
  private MetricRegistry registry;
  private Gauge<Integer> gaugeCorePoolSize = null;
  private Gauge<Integer> gaugeCurrentPoolSize = null;
  private Gauge<Integer> gaugeActiveCount = null;
  private Gauge<Integer> gaugePercentIdle = null;
  private Gauge<Integer> gaugePendingJobs = null;
  private Timer tasksExecTimer = null;
  private Timer workerRunTimer = null;
  private File metricsOutputDirectory = new File("./target/");

  @Before
  public void setUp() throws Exception {
    // RejectedExecutionHandler implementation
    RejectedExecutionHandlerImpl rejectionHandler = new RejectedExecutionHandlerImpl();
    // Get the ThreadFactory implementation to use
    ThreadFactory threadFactory = Executors.defaultThreadFactory();
    // Create the MetricsRegistry
    registry = SharedMetricRegistries.getOrCreate("default");
    // creating the ThreadPoolExecutor
    this.threadPool =
        new InstrumentedThreadPoolExecutor(poolSize, maxPoolSize, keepAlive, TimeUnit.SECONDS,
            new ArrayBlockingQueue<Runnable>(2), threadFactory, rejectionHandler, registry, "test");
    if (!metricsOutputDirectory.exists()) {
      metricsOutputDirectory.mkdirs();
    }
    // CsvReporter.enable(metricsOutputDirectory,100,
    // TimeUnit.MILLISECONDS);
    // ConsoleReporter.enable(100, TimeUnit.MILLISECONDS);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testThreadPoolGauges() throws Exception {

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

    Thread.sleep(10000);
    // shut down the pool
    this.threadPool.shutdown();
    // shut down the monitor thread
    while (!this.threadPool.isTerminated()) {
      System.out.println("Waiting for Workers to finish the task");
    }
    System.out.println("Finished all threads");
    monitor.shutdown();

  }

  @After
  public void tearDown() {
    this.threadPool.shutdownNow();

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
        // 1sec sleep
        Thread.sleep(1000);
      } catch (InterruptedException e) {
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
  public class RejectedExecutionHandlerImpl implements RejectedExecutionHandler {

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
      System.out.println("ActiveCount" + executor.getActiveCount());
      System.out.println("Id=" + r.hashCode() + r.toString() + " is rejected");
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
                    "[monitor] [%d/%d] Active: %d, Completed: %d, Task: %d, isShutdown: %s, isTerminated: %s",
                    this.executor.getPoolSize(), this.executor.getCorePoolSize(),
                    this.executor.getActiveCount(), this.executor.getCompletedTaskCount(),
                    this.executor.getTaskCount(), this.executor.isShutdown(),
                    this.executor.isTerminated()));
        System.out
            .println(String
                .format(
                    "[metric-gauge] [%d/%d] Active: %d, PercentageIdle: %d, PendingTasks: %d, isShutdown: %s, isTerminated: %s",
                    gaugeCurrentPoolSize.getValue(), gaugeCorePoolSize.getValue(),
                    gaugeActiveCount.getValue(), gaugePercentIdle.getValue(),
                    gaugePendingJobs.getValue(), this.executor.isShutdown(),
                    this.executor.isTerminated()));
        assertThat(this.executor.getPoolSize()).isEqualTo(gaugeCurrentPoolSize.getValue());
        assertThat(this.executor.getCorePoolSize()).isEqualTo(gaugeCorePoolSize.getValue());
        assertThat(this.executor.getActiveCount()).isEqualTo(gaugeActiveCount.getValue());
        System.out.println(String.format(
            "[metric-submittasktimer] Min: %s, Max: %s, Mean: %s, Count: %d", tasksExecTimer
                .getSnapshot().getMin(), tasksExecTimer.getSnapshot().getMax(), tasksExecTimer
                .getSnapshot().getMean(), tasksExecTimer.getCount()));
        System.out.println(String.format(
            "[metric-workertimer] Min: %s, Max: %s, Mean: %s, Count: %d m1: %s", workerRunTimer
                .getSnapshot().getMin(), workerRunTimer.getSnapshot().getMax(), workerRunTimer
                .getSnapshot().getMean(), workerRunTimer.getCount(), workerRunTimer.getMeanRate()));

        try {
          Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }

    }
  }
}
