package com.invengo.rfidpad.base;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 静态读定时器
 */
public class StaticReadTimer {

	public static final int INIT = 0;
	public static final int RUNNING = 1;
	public static final int CANCELLED = 2;

	private int mStatus;
	private long mDelay;
	private TimerTask mTimerTask;
	private Timer mTimer;

	public StaticReadTimer(TimerTask task, long delay){
		this.mTimerTask = task;
		this.mDelay = delay;
		this.mStatus = INIT;
		this.mTimer = null;
	}

	public void start(){
		mTimer = new Timer();
		mTimer.schedule(this.mTimerTask, this.mDelay);
		this.mStatus = RUNNING;
	}

	public void start(long period){
		mTimer = new Timer();
		mTimer.schedule(this.mTimerTask, this.mDelay, period);
		this.mStatus = RUNNING;
	}

	public void cancel(){
		if(null != this.mTimer){
			mTimer.cancel();
		}
		if(this.mStatus == RUNNING){
			this.mStatus = CANCELLED;
		}
	}

	public int getStatus(){
		return this.mStatus;
	}

}
