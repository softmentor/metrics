package com.yammer.metrics.threadpool;

import com.yammer.metrics.core.Timer;
import com.yammer.metrics.core.TimerContext;

/**
 * @author softmentor
 * 
 */
public class InstrumentedRunnable implements Runnable {

	private final Runnable underLyingRunnable;
	private Timer workerRunTimer;

	/**
	 * 
	 */
	public InstrumentedRunnable(Runnable originalRunnable) {
		// TODO Auto-generated constructor stub
		this.underLyingRunnable = originalRunnable;
		this.workerRunTimer = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		TimerContext ctx = null;
		try {
			if (null != getWorkerRunTimer()) {
				ctx = getWorkerRunTimer().time();
				this.underLyingRunnable.run();
			} else {
				this.underLyingRunnable.run();
			}
		} finally {
			if (null != ctx) {
				// update the tasksExecTimer and return elapsed time
				long duration = ctx.stop();
				System.out.println(Thread.currentThread().getName() + "-"+ underLyingRunnable.hashCode() +"-Runnable exection with duration (millisecond) = "
						+ duration/1000000);
			}
		}

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
