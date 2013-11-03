package com.codahale.metrics.threadpool.decorated;

import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author softmentor
 * 
 */
public class DecoratedCallableScheduledFuture<V> implements RunnableScheduledFuture<V> {

	/**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((decoratedCallable == null) ? 0 : decoratedCallable.hashCode());
    result =
        prime * result + ((underlyingFutureTask == null) ? 0 : underlyingFutureTask.hashCode());
    return result;
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    DecoratedCallableScheduledFuture other = (DecoratedCallableScheduledFuture) obj;
    if (decoratedCallable == null) {
      if (other.decoratedCallable != null) return false;
    } else if (!decoratedCallable.equals(other.decoratedCallable)) return false;
    if (underlyingFutureTask == null) {
      if (other.underlyingFutureTask != null) return false;
    } else if (!underlyingFutureTask.equals(other.underlyingFutureTask)) return false;
    return true;
  }

  private DecoratedCallable<V> decoratedCallable;
	private RunnableScheduledFuture<V> underlyingFutureTask;

	public DecoratedCallableScheduledFuture(DecoratedCallable<V> originalCallable,
			RunnableScheduledFuture<V> underlyingFutureTask) {
		this.decoratedCallable = originalCallable;
		this.underlyingFutureTask = underlyingFutureTask;
	}

	@Override
	public void run() {
		// Delegate the run method to the original future task
		this.underlyingFutureTask.run();
	}

	@Override
	public boolean cancel(final boolean pMayInterruptIfRunning) {
		// Cancel the thread as usual
		boolean isCanceled = this.underlyingFutureTask.cancel(pMayInterruptIfRunning);

		// Release resources
		if (pMayInterruptIfRunning) {
			decoratedCallable.forceReleaseResources();
		}

		return isCanceled;
	}

	// Delegate the other methods of the interface to given task

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.concurrent.Future#isCancelled()
	 */
	@Override
	public boolean isCancelled() {
		this.underlyingFutureTask.isCancelled();
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.concurrent.Future#isDone()
	 */
	@Override
	public boolean isDone() {
		this.underlyingFutureTask.isDone();
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.concurrent.Future#get()
	 */
	@Override
	public V get() throws InterruptedException, ExecutionException {

		return this.underlyingFutureTask.get();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.concurrent.Future#get(long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public V get(long timeout, TimeUnit unit) throws InterruptedException,
			ExecutionException, TimeoutException {
		return this.underlyingFutureTask.get(timeout, unit);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.concurrent.Delayed#getDelay(java.util.concurrent.TimeUnit)
	 */
	@Override
	public long getDelay(TimeUnit unit) {
		// TODO Auto-generated method stub
		return this.underlyingFutureTask.getDelay(unit);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Delayed o) {
		return this.underlyingFutureTask.compareTo(o);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.concurrent.RunnableScheduledFuture#isPeriodic()
	 */
	@Override
	public boolean isPeriodic() {
		return this.underlyingFutureTask.isPeriodic();
	}

}
