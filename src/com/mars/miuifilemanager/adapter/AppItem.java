package com.mars.miuifilemanager.adapter;

import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;

public class AppItem {
	public String mName;
	private PackageInfo mInfo;
    private Drawable mIcon;
    
	public AppItem(String name, PackageInfo info) {
		mName = name;
		mInfo = info;
		mIcon = null;
	}
	
	public String getAppName() {
		return mName;
	}
	
	public PackageInfo getPackageInfo() {
		return mInfo;
	}
	
	public void setIcon(Drawable icon) {
		mIcon = icon;
	}
	
	public Drawable getIcon() {
		return mIcon;
	}
}
