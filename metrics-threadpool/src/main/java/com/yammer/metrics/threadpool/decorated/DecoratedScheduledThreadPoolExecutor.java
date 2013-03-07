package com.yammer.metrics.threadpool.decorated;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

/**
 * <p>
 * This is decorated instance over {@link ScheduledThreadPoolExecutor}. It
 * provides additional capabilities like releasing resources.
 * <p>
 * However, there is a known issue of using this implementation for jdk 1.6,
 * hence this would be recommended implementation once the bug is fixed in jdk.
 * <p>
 * The mechanism of overriding the
 * {@link DecoratedScheduledThreadPoolExecutor#decorateTask(java.util.concurrent.Callable, RunnableScheduledFuture)}
 * has a known bug in jdk 1.6.x.
 * <p>
 * Please refer this link for more information:
 * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6560953
 * 
 * @author softmentor
 * 
 */
public class DecoratedScheduledThreadPoolExecutor extends
		ScheduledThreadPoolExecutor {

	/**
	 * @param corePoolSize
	 * @param handler
	 */
	public DecoratedScheduledThreadPoolExecutor(int corePoolSize,
			RejectedExecutionHandler handler) {
		super(corePoolSize, handler);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param corePoolSize
	 * @param threadFactory
	 * @param handler
	 */
	public DecoratedScheduledThreadPoolExecutor(int corePoolSize,
			ThreadFactory threadFactory, RejectedExecutionHandler handler) {
		super(corePoolSize, threadFactory, handler);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param corePoolSize
	 * @param threadFactory
	 */
	public DecoratedScheduledThreadPoolExecutor(int corePoolSize,
			ThreadFactory threadFactory) {
		super(corePoolSize, threadFactory);
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.util.concurrent.ScheduledThreadPoolExecutor#execute(java.lang.Runnable
	 * )
	 */
	@Override
	public void execute(Runnable command) {
		// TODO Auto-generated method stub
		super.execute(command);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.concurrent.ScheduledThreadPoolExecutor#shutdown()
	 */
	@Override
	public void shutdown() {
		// TODO Auto-generated method stub
		super.shutdown();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.concurrent.ScheduledThreadPoolExecutor#shutdownNow()
	 */
	@Override
	public List<Runnable> shutdownNow() {
		// TODO Auto-generated method stub
		return super.shutdownNow();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.util.concurrent.ThreadPoolExecutor#beforeExecute(java.lang.Thread,
	 * java.lang.Runnable)
	 */
	@Override
	protected void beforeExecute(Thread t, Runnable r) {
		// TODO Auto-generated method stub
		super.beforeExecute(t, r);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.util.concurrent.ThreadPoolExecutor#afterExecute(java.lang.Runnable,
	 * java.lang.Throwable)
	 */
	@Override
	protected void afterExecute(Runnable r, Throwable t) {
		// TODO Auto-generated method stub
		super.afterExecute(r, t);
	}

	// All Constructors...
	public DecoratedScheduledThreadPoolExecutor(int poolSize) {
		super(poolSize);
	}

	@Override
	protected <V> RunnableScheduledFuture<V> decorateTask(
			Runnable originalRunnable,
			RunnableScheduledFuture<V> underlyingFutureTask) {
		RunnableScheduledFuture<V> decoratedFutureTask;
		try {
			// Trying to decorate the originalRunnable with the
			// DecoratedRunnable
			decoratedFutureTask = new DecoratedRunnableScheduledFuture<V>(
					(DecoratedRunnable) originalRunnable, underlyingFutureTask);
		} catch (ClassCastException e) {
			// If the runnable is not an instance of DecoratedRunnable use the
			// default
			// decorator
			decoratedFutureTask = super.decorateTask(originalRunnable,
					underlyingFutureTask);
		}
		return decoratedFutureTask;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.util.concurrent.ScheduledThreadPoolExecutor#decorateTask(java.util
	 * .concurrent.Callable, java.util.concurrent.RunnableScheduledFuture)
	 */
	@Override
	protected <V> RunnableScheduledFuture<V> decorateTask(
			Callable<V> originalCallable,
			RunnableScheduledFuture<V> underlyingFutureTask) {
		RunnableScheduledFuture<V> decoratedFutureTask;
		try {
			// Trying to decorate the originalRunnable with the
			// DecoratedCallable
			decoratedFutureTask = new DecoratedCallableScheduledFuture<V>(
					(DecoratedCallable<V>) originalCallable,
					underlyingFutureTask);
		} catch (ClassCastException e) {
			// If the runnable is not an instance of DecoratedCallable use the
			// default
			// decorator
			decoratedFutureTask = super.decorateTask(originalCallable,
					underlyingFutureTask);
		}
		return decoratedFutureTask;
		// return super.decorateTask(callable, task);
	}
}
