package com.mars.miuifilemanager.utils;

public class Settings {
	private static Settings mInstance;
	
	private boolean mShowDotAndHiddenFiles;
	
	private Settings() {
		
	}
	
	public static Settings instance() {
		if (mInstance == null) {
			mInstance = new Settings();
		}
		
		return mInstance;
	}
	
	public boolean getShowDotAndHiddenFiles() {
		return mShowDotAndHiddenFiles;
	}
	
	public void setShowDotAndHiddenFiles(boolean flag) {
		mShowDotAndHiddenFiles = flag;
	}
}
