package com.codahale.metrics.threadpool;

import static com.codahale.metrics.MetricRegistry.name;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import com.codahale.metrics.Timer;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;



/**
 * Instrumented instance of {@link ThreadPoolExecutor} with a set of gauges and timers:
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
 * <td>The approximate number of pending jobs. Refer {@link ThreadPoolExecutor#getTaskCount()} and
 * {@link ThreadPoolExecutor#getCompletedTaskCount()}</td>
 * </tr>
 * <tr>
 * <td>{@code tasks-executed}</td>
 * <td>Timer metric which aggregates task execution time. It also provides throughput statistics via
 * {@link Meter}.</td>
 * </tr>
 * <tr>
 * <td>{@code tasks-submitted}</td>
 * <td>The cummulative total number of tasks submitted till now.</td>
 * </tr>
 * </table>
 * <br>
 * <ol>
 * <i>References and Inspirations:</i> <br>
 * <li>1) https://github.com/wotifgroup/dropwizard/blob/ 2b08b2e925a11586dc7a29d5be831430a9fdd835
 * /dropwizard-core/src/main/java/com/yammer
 * /dropwizard/executor/InstrumentedScheduledThreadPoolExecutor.java
 * <li>2) http://docs .oracle.com/javase/6/docs/api/java/util/concurrent/ThreadPoolExecutor.html
 * </ol>
 * 
 * 
 * @author softmentor
 */
public class InstrumentedThreadPoolExecutor extends ThreadPoolExecutor {

  private MetricRegistry registry = null;
  private Timer tasksExecTimer = null;
  private Timer workerRunTimer = null;
  private Counter tasksSubmittedCounter = null;
  private static final String DEFAULT_METRIC_NAME = "threads";

  public static final String COREPOOL_SIZE = "corepool-size";
  public static final String CURRENT_POOL_SIZE = "current-pool-size";
  public static final String ACTIVE_THREADS = "active-threads";
  public static final String PERCENT_IDLE = "percent-idle";
  public static final String PENDING_JOBS = "pending-jobs";
  public static final String TASKS_EXECUTED = "tasks-executed";
  public static final String WORKER_TIME = "worker-time";
  public static final String MAX_POOLSIZE_EVERHIT = "max-poolSize-everhit";
  public static final String TASKS_SUBMITTED = "tasks-submitted";

  /**
   * Instrumented instance of {@link ThreadPoolExecutor} with a set of gauges and timers:
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
   * <td>The approximate number of pending jobs. Refer {@link ThreadPoolExecutor#getTaskCount()} and
   * {@link ThreadPoolExecutor#getCompletedTaskCount()}</td>
   * </tr>
   * <tr>
   * <td>{@code tasks-executed}</td>
   * <td>Timer metric which aggregates task execution time. It also provides throughput statistics
   * via {@link Meter}.</td>
   * </tr>
   * </table>
   * 
   * @param threads
   * @param metricName
   */
  public InstrumentedThreadPoolExecutor(final int threads, MetricRegistry registry,
      final String metricName) {
    this(threads, threads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(),
        Executors.defaultThreadFactory(), registry, metricName);
  }

  /**
   * @param corePoolSize
   * @param maximumPoolSize
   * @param keepAliveTime
   * @param unit
   * @param threadFactory
   * @param metricName
   */
  public InstrumentedThreadPoolExecutor(final int corePoolSize, final int maximumPoolSize,
      final long keepAliveTime, final TimeUnit unit, final ThreadFactory threadFactory,
      MetricRegistry registry, final String metricName) {
    this(corePoolSize, maximumPoolSize, keepAliveTime, unit, new LinkedBlockingQueue<Runnable>(),
        threadFactory, registry, metricName);
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
  public InstrumentedThreadPoolExecutor(final int corePoolSize, final int maximumPoolSize,
      final long keepAliveTime, final TimeUnit unit, final BlockingQueue<Runnable> workQueue,
      final ThreadFactory threadFactory, MetricRegistry registry, final String metricName) {
    super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
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
  public InstrumentedThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime,
      TimeUnit unit, BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler,
      MetricRegistry registry, String metricName) {
    super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
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
  public InstrumentedThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime,
      TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory,
      RejectedExecutionHandler handler, MetricRegistry registry, String metricName) {
    super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    instrument(registry, metricName);
  }

  /**
   * @param corePoolSize
   * @param maximumPoolSize
   * @param keepAliveTime
   * @param unit
   * @param workQueue
   */
  public InstrumentedThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime,
      TimeUnit unit, BlockingQueue<Runnable> workQueue, MetricRegistry registry, String metricName) {
    super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
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
   * @see java.util.concurrent.ThreadPoolExecutor#execute(java.lang.Runnable)
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
