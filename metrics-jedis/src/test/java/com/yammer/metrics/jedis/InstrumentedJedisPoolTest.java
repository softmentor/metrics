package com.yammer.metrics.jedis;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import redis.clients.jedis.Protocol;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.threadpool.InstrumentedThreadPoolExecutor;

/**
 * @author softmentor
 * 
 */
public class InstrumentedJedisPoolTest {

	private static InstrumentedJedisPool JEDIS_POOL;
	private IJedisProxy jedisProxy;
	private static final String host = "localhost";
	private static final int port = Protocol.DEFAULT_PORT;
	private int maxRedisWrites = 100;
	// thread pool setting
	private InstrumentedThreadPoolExecutor threadPool;
	private int poolSize = 2;
	private int maxPoolSize = 4;
	private int keepAlive = 10;
	private int maxJobs = 10;

	private MetricsRegistry registry;
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
	}

	@Before
	public void setup() {
		System.out.println("Before");
		InstrumentedJedisPool thePool = new InstrumentedJedisPool(host, port);
		JEDIS_POOL = thePool;

		this.jedisProxy = JedisProxy.newInstance(JEDIS_POOL);
		// RejectedExecutionHandler implementation
		RejectedExecutionHandlerImpl rejectionHandler = new RejectedExecutionHandlerImpl();
		// Get the ThreadFactory implementation to use
		ThreadFactory threadFactory = Executors.defaultThreadFactory();
		// Create the MetricsRegistry
		registry = Metrics.defaultRegistry();
		// creating the ThreadPoolExecutor
		this.threadPool = new InstrumentedThreadPoolExecutor(poolSize,
				maxPoolSize, keepAlive, TimeUnit.SECONDS,
				new ArrayBlockingQueue<Runnable>(2), threadFactory,
				rejectionHandler, "test");

		// ConsoleReporter.enable(20, TimeUnit.MILLISECONDS);
	}

	@Test
	public void testJedisPoolMetrics() throws Exception {

		gaugeActiveCount = (Gauge<Integer>) registry.getAllMetrics().get(
				this.threadPool.activeThreads);
		gaugeCorePoolSize = (Gauge<Integer>) registry.getAllMetrics().get(
				this.threadPool.corePoolSize);
		gaugeCurrentPoolSize = (Gauge<Integer>) registry.getAllMetrics().get(
				this.threadPool.currentPoolSize);
		gaugePercentIdle = (Gauge<Integer>) registry.getAllMetrics().get(
				this.threadPool.percentIdle);
		gaugePendingJobs = (Gauge<Integer>) registry.getAllMetrics().get(
				this.threadPool.pendingJobs);
		tasksExecTimer = (Timer) registry.getAllMetrics().get(
				this.threadPool.tasksExecuted);
		workerRunTimer = (Timer) registry.getAllMetrics().get(
				this.threadPool.workerTimeMetric);

		// ====================================================
		// start the monitoring thread
		int monitorThreadDelay = 1; // in sec
		MonitorThread monitor = new MonitorThread(this.threadPool,
				monitorThreadDelay);
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

	@Test
	public void testSomething() {
		System.out.println("Testing Something ===============");
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
			System.out.println(Thread.currentThread().getName()
					+ " Run started for Command Id= " + hashCode());
			processCommand();
			// System.out.println(Thread.currentThread().getName() + " End.");
		}

		private void processCommand() {
			try {
				String testKey = "testkey-" + Thread.currentThread().getName()
						+ "-" + hashCode();
				for (int i = 0; i <= maxRedisWrites; i++) {
					String testValue = "testvalue-" + i;
					jedisProxy.zadd(testKey, System.currentTimeMillis(),
							testValue);
					// Thread.sleep(2);
				}
				assertTrue(new Long("10").equals(jedisProxy.zcard(testKey)));
				System.out.println("key=" + testKey + "  Value="
						+ jedisProxy.zrangeByScoreWithScores(testKey, 0, -1));
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
	public class RejectedExecutionHandlerImpl implements
			RejectedExecutionHandler {

		@Override
		public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
			System.out.println("ActiveCount" + executor.getActiveCount());
			System.out.println("Id=" + r.hashCode() + r.toString()
					+ " is rejected");
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
								.format("[monitor] [%d/%d] Active: %d, Completed: %d, Task: %d, isShutdown: %s, isTerminated: %s",
										this.executor.getPoolSize(),
										this.executor.getCorePoolSize(),
										this.executor.getActiveCount(),
										this.executor.getCompletedTaskCount(),
										this.executor.getTaskCount(),
										this.executor.isShutdown(),
										this.executor.isTerminated()));
				System.out
						.println(String
								.format("[metric-gauge] [%d/%d] Active: %d, PercentageIdle: %d, PendingTasks: %d, isShutdown: %s, isTerminated: %s",
										gaugeCurrentPoolSize.getValue(),
										gaugeCorePoolSize.getValue(),
										gaugeActiveCount.getValue(),
										gaugePercentIdle.getValue(),
										gaugePendingJobs.getValue(),
										this.executor.isShutdown(),
										this.executor.isTerminated()));
				assertThat(this.executor.getPoolSize(),
						Matchers.is(gaugeCurrentPoolSize.getValue()));
				assertThat(this.executor.getCorePoolSize(),
						Matchers.is(gaugeCorePoolSize.getValue()));
				assertThat(this.executor.getActiveCount(),
						Matchers.is(gaugeActiveCount.getValue()));
				System.out
						.println(String
								.format("[metric-submittasktimer] Min: %s, Max: %s, Mean: %s, Count: %d",
										tasksExecTimer.getMin(),
										tasksExecTimer.getMax(),
										tasksExecTimer.getMean(),
										tasksExecTimer.getCount()));
				System.out
						.println(String
								.format("[metric-workertimer] Min: %s, Max: %s, Mean: %s, Count: %d m1: %s",
										workerRunTimer.getMin(),
										workerRunTimer.getMax(),
										workerRunTimer.getMean(),
										workerRunTimer.getCount(),
										workerRunTimer.getMeanRate()));

				try {
					Thread.sleep(seconds * 1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		}
	}
}
