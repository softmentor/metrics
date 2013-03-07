package com.yammer.metrics.threadpool.decorated;

import com.yammer.metrics.threadpool.InstrumentedRunnable;

/**
 * @author softmentor
 *
 */
public abstract class DecoratedRunnable extends InstrumentedRunnable {
    /**
	 * @param originalRunnable
	 */
	public DecoratedRunnable(Runnable originalRunnable) {
		super(originalRunnable);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Default is empty implementation
     * Release resources
     */
    protected void forceReleaseResources(){};
}
