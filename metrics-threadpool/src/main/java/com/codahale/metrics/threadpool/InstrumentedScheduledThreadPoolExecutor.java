package com.codahale.metrics.threadpool;

import static com.codahale.metrics.MetricRegistry.name;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import com.codahale.metrics.Timer;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;
import com.codahale.metrics.threadpool.decorated.DecoratedScheduledThreadPoolExecutor;

/**
 * An instrumented {@link ScheduledThreadPoolExecutor} that provides the following {@link Metrics}:
 * <ol>
 * <li>Gauge representing total active threads</li>
 * <li>Gauge representing percent of idle threads</li>
 * <li>Gauge representing total jobs pending execution</li>
 * <li>Throughput & 90p,99p etc response time of the job</li>
 * </ol>
 * 
 * @author softmentor
 */
public class InstrumentedScheduledThreadPoolExecutor extends DecoratedScheduledThreadPoolExecutor {
  private MetricRegistry registry = null;
  private String metricName = "threads";
  private Timer tasksExecTimer = null;
  private Timer workerRunTimer = null;
  private Counter tasksSubmittedCounter = null;
  private static final String DEFAULT_METRIC_NAME = "threads";

  private static final String COREPOOL_SIZE = "corepool-size";
  private static final String CURRENT_POOL_SIZE = "current-pool-size";
  private static final String ACTIVE_THREADS = "active-threads";
  private static final String PERCENT_IDLE = "percent-idle";
  private static final String PENDING_JOBS = "pending-jobs";
  private static final String TASKS_EXECUTED = "tasks-executed";
  private static final String WORKER_TIME = "worker-time";
  private static final String MAX_POOLSIZE_EVERHIT = "max-poolSize-everhit";
  private static final String TASKS_SUBMITTED = "tasks-submitted";

  public InstrumentedScheduledThreadPoolExecutor(final int corePoolSize) {
    super(corePoolSize);
    instrument(null, this.metricName);
  }

  public InstrumentedScheduledThreadPoolExecutor(final int corePoolSize, MetricRegistry registry) {
    super(corePoolSize);
    instrument(registry, this.metricName);
  }

  public InstrumentedScheduledThreadPoolExecutor(final int corePoolSize, MetricRegistry registry,
      final String metricName) {
    super(corePoolSize);
    instrument(registry, metricName);
  }

  public InstrumentedScheduledThreadPoolExecutor(final int corePoolSize,
      final ThreadFactory threadFactory, MetricRegistry registry) {
    super(corePoolSize, threadFactory);
    instrument(registry, this.metricName);
  }

  public InstrumentedScheduledThreadPoolExecutor(final int corePoolSize,
      final ThreadFactory threadFactory, final MetricRegistry registry, final String metricName) {
    super(corePoolSize, threadFactory);
    instrument(registry, metricName);
  }

  /**
   * @param metricsRegistry
   * @param customMetricName
   */
  private void instrument(MetricRegistry metricsRegistry, final String metricName) {

    final BlockingQueue<Runnable> currentQue = super.getQueue();
    if (null == metricsRegistry) {
      metricsRegistry = SharedMetricRegistries.getOrCreate("default");
    }
    // Assign values to instance scope
    registry = metricsRegistry;
    String prefix;

    if (null == metricName) {
      prefix = DEFAULT_METRIC_NAME;
    } else {
      prefix = metricName.trim().length() > 0 ? metricName : DEFAULT_METRIC_NAME;
    }

    prefix = name(metricName);
    // Start capturing all Gauge metrics
    // Add metric to track threads being used
    registry.register(name(prefix, COREPOOL_SIZE), new Gauge<Integer>() {
      @Override
      public Integer getValue() {
        return getCorePoolSize();
      }
    });
    registry.register(name(prefix, CURRENT_POOL_SIZE), new Gauge<Integer>() {
      @Override
      public Integer getValue() {
        return getPoolSize();
      }
    });
    registry.register(name(prefix, ACTIVE_THREADS), new Gauge<Integer>() {
      @Override
      public Integer getValue() {
        return getActiveCount();
      }
    });
    registry.register(name(prefix, PERCENT_IDLE), new Gauge<Integer>() {
      @Override
      public Integer getValue() {
        return ((getCorePoolSize() - getActiveCount()) * 100) / getCorePoolSize();
      }
    });
    registry.register(name(prefix, PENDING_JOBS), new Gauge<Integer>() {
      @Override
      public Integer getValue() {
        return currentQue.size();
      }
    });
    registry.register(name(prefix, MAX_POOLSIZE_EVERHIT), new Gauge<Integer>() {
      @Override
      public Integer getValue() {
        return getLargestPoolSize();
      }
    });



    // Add metric counter to start counting the number of tasks submitted
    tasksSubmittedCounter = registry.counter(name(prefix, TASKS_SUBMITTED));

    // Add the timer metric to capture task execution time
    tasksExecTimer = buildTimer(registry, TASKS_EXECUTED);
    // Add the timer metric to capture runnable execution time
    workerRunTimer = buildTimer(registry, WORKER_TIME);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.codahale.metrics.threadpool.decorated.DecoratedScheduledThreadPoolExecutor
   * #execute(java.lang.Runnable)
   */
  @Override
  public void execute(Runnable command) {
    Timer.Context ctx = null;
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

  private Timer buildTimer(MetricRegistry registry, String metricName) {
    Timer metricTimer = null;
    if (null == registry.getTimers().get(metricName)) {
      registry.register(metricName, new Timer());
    }
    metricTimer = registry.getTimers().get(metricName);
    return metricTimer;
  }

}
