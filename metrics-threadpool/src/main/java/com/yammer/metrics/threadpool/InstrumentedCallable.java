package com.yammer.metrics.threadpool;

import java.util.concurrent.Callable;

import com.yammer.metrics.core.Timer;


/**
 * @author softmentor
 *
 */
public class InstrumentedCallable<T> implements Callable<T> {
	private final Callable<T> underLyingCallable;
	private Timer workerRunTimer;

	/**
	 * 
	 */
	public InstrumentedCallable(Callable<T> originalCallable) {
		this.underLyingCallable = originalCallable;
		this.workerRunTimer = null;
	}
	/* (non-Javadoc)
	 * @see java.util.concurrent.Callable#call()
	 */
	@Override
	public T call() throws Exception {
		return workerRunTimer.time(underLyingCallable);
	}
	/**
	 * @return the workerRunTimer
	 */
	public Timer getWorkerRunTimer() {
		return workerRunTimer;
	}
	/**
	 * @param workerRunTimer the workerRunTimer to set
	 */
	public void setWorkerRunTimer(Timer workerRunTimer) {
		this.workerRunTimer = workerRunTimer;
	}
	
	

}
