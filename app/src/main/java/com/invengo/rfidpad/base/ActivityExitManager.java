package com.invengo.rfidpad.base;

import java.util.LinkedList;
import java.util.List;

import android.app.Activity;

public class ActivityExitManager {

	private static ActivityExitManager mExitManager = new ActivityExitManager();
	private List<Activity> mActivityContainer = new LinkedList<Activity>();
	
	private ActivityExitManager(){
		//
	}
	
	public static ActivityExitManager getInstance(){
		return mExitManager;
	}
	
	public void addActivity(Activity activity){
		mActivityContainer.add(activity);
	}
	
	public void removeActivity(Activity activity){
		mActivityContainer.remove(activity);
	}
	
	public void exitApp(){
		for(Activity activity : mActivityContainer){
			activity.finish();
		}
		System.exit(0);
	}
}
