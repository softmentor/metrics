package com.yammer.metrics.threadpool.decorated;

import java.util.concurrent.Callable;

import com.yammer.metrics.threadpool.InstrumentedCallable;

/**
 * @author softmentor
 *
 */
public abstract class DecoratedCallable<V> extends InstrumentedCallable<V> {
    /**
	 * @param originalCallable
	 */
	public DecoratedCallable(Callable<V> originalCallable) {
		super(originalCallable);
		// TODO Auto-generated constructor stub
	}

	/**
     * Release resources
     */
    protected void forceReleaseResources(){};
}
