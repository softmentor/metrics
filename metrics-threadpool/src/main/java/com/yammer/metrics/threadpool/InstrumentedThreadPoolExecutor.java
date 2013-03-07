package com.yammer.metrics.threadpool;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.annotation.Metered;
import com.yammer.metrics.annotation.Timed;
import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.core.TimerContext;

/**
 * Instrumented instance of {@link ThreadPoolExecutor} with a set of gauges and
 * timers:
 * <p/>
 * <table>
 * <tr>
 * <td>{@code active-threads}</td>
 * <td>The number of active threads.</td>
 * </tr>
 * <tr>
 * <td>{@code percent-idle}</td>
 * <td>The percentage of idle threads.</td>
 * </tr>
 * <tr>
 * <td>{@code pending-jobs}</td>
 * <td>The approximate number of pending jobs. Refer
 * {@link ThreadPoolExecutor#getTaskCount()} and
 * {@link ThreadPoolExecutor#getCompletedTaskCount()}</td>
 * </tr>
 * <tr>
 * <td>{@code tasks-executed}</td>
 * <td>Timer metric which aggregates task execution time. It also provides
 * throughput statistics via {@link Meter}.</td>
 * </tr>
 * <tr>
 * <td>{@code tasks-submitted}</td>
 * <td>The cummulative total number of tasks submitted till now.</td>
 * </tr>
 * </table>
 * <br>
 * <ol>
 * <i>References and Inspirations:</i> <br>
 * <li>1) https://github.com/wotifgroup/dropwizard/blob/
 * 2b08b2e925a11586dc7a29d5be831430a9fdd835
 * /dropwizard-core/src/main/java/com/yammer
 * /dropwizard/executor/InstrumentedScheduledThreadPoolExecutor.java
 * <li>2) http://docs
 * .oracle.com/javase/6/docs/api/java/util/concurrent/ThreadPoolExecutor.html
 * </ol>
 * 
 * 
 * @author softmentor
 */
public class InstrumentedThreadPoolExecutor extends ThreadPoolExecutor {

	private MetricsRegistry registry = null;
	private String metricScope;
	private Timer tasksExecTimer = null;
	private Timer workerRunTimer = null;
	private Counter tasksSubmittedCounter = null;
	private static final String DEFAULT_METRIC_SCOPE = "default";

	private static final String COREPOOL_SIZE = "corepool-size";
	private static final String CURRENT_POOL_SIZE = "current-pool-size";
	private static final String ACTIVE_THREADS = "active-threads";
	private static final String PERCENT_IDLE = "percent-idle";
	private static final String PENDING_JOBS = "pending-jobs";
	private static final String TASKS_EXECUTED = "tasks-executed";
	private static final String WORKER_TIME = "worker-time";
	private static final String MAX_POOLSIZE_EVERHIT = "max-poolSize-everhit";
	private static final String TASKS_SUBMITTED = "tasks-submitted";

	public MetricName corePoolSize = new MetricName(this.getClass(),
			COREPOOL_SIZE);
	public MetricName currentPoolSize = new MetricName(this.getClass(),
			CURRENT_POOL_SIZE);
	public MetricName activeThreads = new MetricName(this.getClass(),
			ACTIVE_THREADS);
	public MetricName percentIdle = new MetricName(this.getClass(),
			PERCENT_IDLE);
	public MetricName pendingJobs = new MetricName(this.getClass(),
			PENDING_JOBS);
	public MetricName tasksExecuted = new MetricName(this.getClass(),
			TASKS_EXECUTED);
	public MetricName workerTimeMetric = new MetricName(this.getClass(),
			WORKER_TIME);
	private MetricName maxPoolSizeEverHit = new MetricName(this.getClass(),
			MAX_POOLSIZE_EVERHIT);;
	public MetricName tasksSubmitted = new MetricName(this.getClass(),
			TASKS_SUBMITTED);

	/**
	 * Instrumented instance of {@link ThreadPoolExecutor} with a set of gauges
	 * and timers:
	 * <p/>
	 * <table>
	 * <tr>
	 * <td>{@code active-threads}</td>
	 * <td>The number of active threads.</td>
	 * </tr>
	 * <tr>
	 * <td>{@code percent-idle}</td>
	 * <td>The percentage of idle threads.</td>
	 * </tr>
	 * <tr>
	 * <td>{@code pending-jobs}</td>
	 * <td>The approximate number of pending jobs. Refer
	 * {@link ThreadPoolExecutor#getTaskCount()} and
	 * {@link ThreadPoolExecutor#getCompletedTaskCount()}</td>
	 * </tr>
	 * <tr>
	 * <td>{@code tasks-executed}</td>
	 * <td>Timer metric which aggregates task execution time. It also provides
	 * throughput statistics via {@link Meter}.</td>
	 * </tr>
	 * </table>
	 * 
	 * @param threads
	 * @param metricName
	 */
	public InstrumentedThreadPoolExecutor(final int threads,
			final String metricName) {
		this(threads, threads, 0L, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>(), Executors
						.defaultThreadFactory(), metricName);
	}

	/**
	 * @param corePoolSize
	 * @param maximumPoolSize
	 * @param keepAliveTime
	 * @param unit
	 * @param threadFactory
	 * @param metricName
	 */
	public InstrumentedThreadPoolExecutor(final int corePoolSize,
			final int maximumPoolSize, final long keepAliveTime,
			final TimeUnit unit, final ThreadFactory threadFactory,
			final String metricName) {
		this(corePoolSize, maximumPoolSize, keepAliveTime, unit,
				new LinkedBlockingQueue<Runnable>(), threadFactory, metricName);
	}

	/**
	 * @param corePoolSize
	 * @param maximumPoolSize
	 * @param keepAliveTime
	 * @param unit
	 * @param workQueue
	 * @param threadFactory
	 * @param metricName
	 */
	public InstrumentedThreadPoolExecutor(final int corePoolSize,
			final int maximumPoolSize, final long keepAliveTime,
			final TimeUnit unit, final BlockingQueue<Runnable> workQueue,
			final ThreadFactory threadFactory, final String metricName) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
				threadFactory);
		instrument(registry, metricName);
	}

	/**
	 * @param corePoolSize
	 * @param maximumPoolSize
	 * @param keepAliveTime
	 * @param unit
	 * @param workQueue
	 * @param handler
	 */
	public InstrumentedThreadPoolExecutor(int corePoolSize,
			int maximumPoolSize, long keepAliveTime, TimeUnit unit,
			BlockingQueue<Runnable> workQueue,
			RejectedExecutionHandler handler, String metricName) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
				handler);
		instrument(registry, metricName);
	}

	/**
	 * @param corePoolSize
	 * @param maximumPoolSize
	 * @param keepAliveTime
	 * @param unit
	 * @param workQueue
	 * @param threadFactory
	 * @param handler
	 */
	public InstrumentedThreadPoolExecutor(int corePoolSize,
			int maximumPoolSize, long keepAliveTime, TimeUnit unit,
			BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory,
			RejectedExecutionHandler handler, String metricName) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
				threadFactory, handler);
		instrument(registry, metricName);
	}

	/**
	 * @param corePoolSize
	 * @param maximumPoolSize
	 * @param keepAliveTime
	 * @param unit
	 * @param workQueue
	 */
	public InstrumentedThreadPoolExecutor(int corePoolSize,
			int maximumPoolSize, long keepAliveTime, TimeUnit unit,
			BlockingQueue<Runnable> workQueue, String metricName) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
		instrument(registry, metricName);
	}

	/**
	 * @param metricsRegistry
	 * @param customMetricName
	 */
	private void instrument(MetricsRegistry metricsRegistry,
			final String customMetricName) {

		final BlockingQueue<Runnable> currentQue = super.getQueue();
		if (null == metricsRegistry) {
			metricsRegistry = Metrics.defaultRegistry();
		}
		// Assign values to instance scope
		registry = metricsRegistry;
		metricScope = customMetricName.trim().length() > 0 ? customMetricName
				: DEFAULT_METRIC_SCOPE;
		corePoolSize = new MetricName(this.getClass(), COREPOOL_SIZE,
				metricScope);
		currentPoolSize = new MetricName(this.getClass(), CURRENT_POOL_SIZE,
				metricScope);
		activeThreads = new MetricName(this.getClass(), ACTIVE_THREADS,
				metricScope);
		percentIdle = new MetricName(this.getClass(), PERCENT_IDLE, metricScope);
		pendingJobs = new MetricName(this.getClass(), PENDING_JOBS, metricScope);
		tasksExecuted = new MetricName(this.getClass(), TASKS_EXECUTED,
				metricScope);
		workerTimeMetric = new MetricName(this.getClass(), WORKER_TIME,
				metricScope);
		maxPoolSizeEverHit = new MetricName(this.getClass(),
				MAX_POOLSIZE_EVERHIT, metricScope);
		tasksSubmitted = new MetricName(this.getClass(), TASKS_SUBMITTED,
				metricScope);

		// Start capturing all Gauge metrics

		// Add metric to track threads being used
		registry.newGauge(corePoolSize, new Gauge<Integer>() {
			@Override
			public Integer getValue() {
				return getCorePoolSize();
			}
		});
		// Add metric to track threads being used
		registry.newGauge(currentPoolSize, new Gauge<Integer>() {
			@Override
			public Integer getValue() {
				return getPoolSize();
			}
		});

		// Add metric to track threads being used
		registry.newGauge(activeThreads, new Gauge<Integer>() {
			@Override
			public Integer getValue() {
				return getActiveCount();
			}
		});

		// Add metric to track percent of idle threads in this pool
		registry.newGauge(percentIdle, new Gauge<Integer>() {
			@Override
			public Integer getValue() {
				return ((getCorePoolSize() - getActiveCount()) * 100)
						/ getCorePoolSize();
			}
		});

		// Add metric to track number of pending jobs in this pool
		registry.newGauge(pendingJobs, new Gauge<Integer>() {
			@Override
			public Integer getValue() {
				return currentQue.size();
			}
		});
		// Add metric to largest number of threads that have ever
		// simultaneously been in the pool
		registry.newGauge(maxPoolSizeEverHit, new Gauge<Integer>() {
			@Override
			public Integer getValue() {
				return getLargestPoolSize();
			}
		});

		// Add metric counter to start counting the number of tasks submitted
		tasksSubmittedCounter = registry.newCounter(tasksSubmitted);

		// Add the timer metric to capture task execution time
		tasksExecTimer = registry.newTimer(tasksExecuted,
				TimeUnit.MILLISECONDS, TimeUnit.SECONDS);

		// Add the timer metric to capture runnable execution time
		workerRunTimer = registry.newTimer(workerTimeMetric, TimeUnit.MILLISECONDS,
				TimeUnit.SECONDS);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.concurrent.ThreadPoolExecutor#execute(java.lang.Runnable)
	 */
	@Override
	public void execute(Runnable command) {
		TimerContext ctx = null;
		try {
			if (null != tasksExecTimer) {
				ctx = tasksExecTimer.time();
			}
			super.execute(command);
		} finally {
			if (null != ctx) {
				// update the tasksExecTimer and return elapsed time
				ctx.stop();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.util.concurrent.AbstractExecutorService#submit(java.lang.Runnable)
	 */
	@Timed
	@Metered
	@Override
	public Future<?> submit(Runnable task) {
		tasksSubmittedCounter.inc();
		InstrumentedRunnable instrumentedRunnable = new InstrumentedRunnable(task);
		instrumentedRunnable.setWorkerRunTimer(workerRunTimer);
		return super.submit(instrumentedRunnable);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.util.concurrent.AbstractExecutorService#submit(java.lang.Runnable,
	 * java.lang.Object)
	 */
	@Timed
	@Metered
	@Override
	public <T> Future<T> submit(Runnable task, T result) {
		tasksSubmittedCounter.inc();
		InstrumentedRunnable instrumentedRunnable = new InstrumentedRunnable(task);
		instrumentedRunnable.setWorkerRunTimer(workerRunTimer);
		return super.submit(instrumentedRunnable, result);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.util.concurrent.AbstractExecutorService#submit(java.util.concurrent
	 * .Callable)
	 */
	@Timed
	@Metered
	@Override
	public <T> Future<T> submit(Callable<T> task) {
		tasksSubmittedCounter.inc();
		InstrumentedCallable<T> instrumentedCallable = new InstrumentedCallable<T>(task);
		instrumentedCallable.setWorkerRunTimer(workerRunTimer);
		return super.submit(instrumentedCallable);
	}

}
