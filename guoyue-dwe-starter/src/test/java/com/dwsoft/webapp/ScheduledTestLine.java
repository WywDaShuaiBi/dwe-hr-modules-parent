package com.dwsoft.webapp;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

public class ScheduledTestLine {
	
	private long sleepTime = 2000L;
	private boolean isSyncCall;
	CountDownLatch cdl = null;
	Timer _timer = new Timer("flow");
	
	public ScheduledTestLine(boolean isSyncCall) {
		this.isSyncCall = isSyncCall;
	}
	
	public void schedule(Runnable task) {
		if(isSyncCall) {
			runAtOnce0(task);
		} else {
			schedule0(task);
		}
	}
	
	private void runAtOnce0(Runnable task) {
		task.run();
	}
	
	private void schedule0(Runnable task) {
		_timer.schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					task.run();
				}finally {
					cdl.countDown();
				}
				
			}
		}, nextDelay());
	}
	
	private int _step = 0;
	
	private long nextDelay() {
		_step++;
		return _step * sleepTime;
	}
	
	public void setSleepTime(long sleepTime) {
		this.sleepTime = sleepTime;
	}

	public void await() throws InterruptedException {
		if(_step > 0) {
			cdl = new CountDownLatch(_step);
			cdl.await();
		}
	}
}
