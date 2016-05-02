package com.mars.miuifilemanager.ui;

import java.util.Collection;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.mars.miuifilemanager.adapter.FileInfo;
import com.mars.miuifilemanager.utils.FileIconHelper;
import com.mars.miuifilemanager.utils.FileSortHelper;

public interface IFileInteractionListener {
	public abstract void addSingleFile(FileInfo fileinfo);
	
	public abstract Collection getAllFiles();
	
	public abstract Context getContext();
	
	public abstract String getDisplayPath(String s);
	
	public abstract FileIconHelper getFileIconHelper();
	
	public abstract FileInfo getItem(int i);
	
	public abstract int getItemCount();
	
	public abstract String getRealPath(String s);
	
	public abstract View getViewById(int i);
	
	public abstract void onDataChanged();
	
	public abstract boolean onNavigation(String s);
	
	public abstract boolean onOperation(int i);
	
	public abstract void onPick(FileInfo fileinfo);
	
	public abstract boolean onRefreshFileList(String s, FileSortHelper filesorthelper);
	
	public abstract void runOnUiThread(Runnable runnable);
	
	public abstract boolean shouldHideMenu(int itemId);
	
	public abstract void sortCurrentList(FileSortHelper filesorthelper);
	
	public abstract void startActivity(Intent intent);
	
	public abstract void onDelete(FileInfo fileInfo);
}
