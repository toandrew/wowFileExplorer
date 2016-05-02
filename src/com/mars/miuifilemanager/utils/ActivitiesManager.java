package com.mars.miuifilemanager.utils;

import java.util.HashMap;

import android.app.Activity;

public class ActivitiesManager {
	private static ActivitiesManager mInstance;
	private HashMap<String, Activity> mActivities;
	
	private ActivitiesManager() {
		mActivities = new HashMap<String, Activity>();;
	}
	
	public static synchronized ActivitiesManager getInstance() {
		if (mInstance == null)
			mInstance = new ActivitiesManager();
		return mInstance;
	}
	
	public Activity getActivity(String s) {
		return (Activity)mActivities.get(s);
	}
	
	public void registerActivity(String s, Activity activity) {
		mActivities.put(s, activity);
	}
	
	public void unRegisterActivity(String s) {
		mActivities.remove(s);
	}
	
	public void clearAll() {
		mActivities.clear();
	}
}
