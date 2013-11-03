package com.yammer.metrics.threadpool;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.annotation.Metered;
import com.yammer.metrics.annotation.Timed;
import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.core.TimerContext;
import com.yammer.metrics.threadpool.decorated.DecoratedScheduledThreadPoolExecutor;

/**
 * An instrumented {@link ScheduledThreadPoolExecutor} that provides the
 * following {@link Metrics}:
 * <ol>
 * <li>Gauge representing total active threads</li>
 * <li>Gauge representing percent of idle threads</li>
 * <li>Gauge representing total jobs pending execution</li>
 * <li>Throughput & 90p,99p etc response time of the job</li>
 * </ol>
 * 
 * @author softmentor
 */
public class InstrumentedScheduledThreadPoolExecutor extends
		DecoratedScheduledThreadPoolExecutor {
	private MetricsRegistry registry = null;
	private String metricName = "threads";
	private Timer tasksExecTimer = null;
	private Timer workerRunTimer = null;
	private Counter tasksSubmittedCounter = null;
	private static final String DEFAULT_METRIC_NAME = "default";

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

	public InstrumentedScheduledThreadPoolExecutor(final int corePoolSize) {
		super(corePoolSize);
		instrument(Metrics.defaultRegistry(), metricName);
	}

	public InstrumentedScheduledThreadPoolExecutor(final int corePoolSize,
			final String metricName) {
		super(corePoolSize);
		instrument(Metrics.defaultRegistry(), metricName);
	}

	public InstrumentedScheduledThreadPoolExecutor(final int corePoolSize,
			final ThreadFactory threadFactory, final String metricName) {
		super(corePoolSize, threadFactory);
		instrument(Metrics.defaultRegistry(), metricName);
	}

	public InstrumentedScheduledThreadPoolExecutor(final int corePoolSize,
			final ThreadFactory threadFactory, final String metricName,
			final MetricsRegistry registry) {
		super(corePoolSize, threadFactory);
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
		metricName = customMetricName.trim().length() > 0 ? customMetricName
				: DEFAULT_METRIC_NAME;
		corePoolSize = new MetricName(this.getClass(), COREPOOL_SIZE,
				metricName);
		currentPoolSize = new MetricName(this.getClass(), CURRENT_POOL_SIZE,
				metricName);
		activeThreads = new MetricName(this.getClass(), ACTIVE_THREADS,
				metricName);
		percentIdle = new MetricName(this.getClass(), PERCENT_IDLE, metricName);
		pendingJobs = new MetricName(this.getClass(), PENDING_JOBS, metricName);
		tasksExecuted = new MetricName(this.getClass(), TASKS_EXECUTED,
				metricName);
		workerTimeMetric = new MetricName(this.getClass(), WORKER_TIME,
				metricName);
		maxPoolSizeEverHit = new MetricName(this.getClass(),
				MAX_POOLSIZE_EVERHIT, metricName);
		tasksSubmitted = new MetricName(this.getClass(), TASKS_SUBMITTED,
				metricName);

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
				TimeUnit.MICROSECONDS, TimeUnit.SECONDS);
		// Add the timer metric to capture runnable execution time
		workerRunTimer = registry.newTimer(workerTimeMetric,
				TimeUnit.MILLISECONDS, TimeUnit.SECONDS);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.yammer.metrics.threadpool.decorated.DecoratedScheduledThreadPoolExecutor
	 * #execute(java.lang.Runnable)
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
		InstrumentedRunnable instrumentedRunnable = new InstrumentedRunnable(
				task);
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
		InstrumentedRunnable instrumentedRunnable = new InstrumentedRunnable(
				task);
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
		InstrumentedCallable<T> instrumentedCallable = new InstrumentedCallable<T>(
				task);
		instrumentedCallable.setWorkerRunTimer(workerRunTimer);
		return super.submit(instrumentedCallable);
	}

}
