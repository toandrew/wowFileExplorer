package com.mars.miuifilemanager.adapter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Process;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.mars.miuifilemanager.ui.FileCategoryActivity;
import com.mars.miuifilemanager.utils.FileIconHelper;
import com.mars.miuifilemanager.view.AppDialog;
import com.mars.miuifilemanager.R;

public class AppList implements Runnable {
	private static final String TAG = "AppList";

	private ListView mListView;

	private ArrayList<AppItem> mAppList;

	private ArrayAdapter mAppListAdapter;

	private Context mContext;

	private static final int MENU_ID_UNFAV = 1;

	private FileCategoryActivity mActivity;

	private boolean mIsScanning = false;

	public AppList(Context context, ListView listview,
			FileIconHelper fileiconhelper) {
		mContext = context;

		mActivity = (FileCategoryActivity) context;

		mAppList = new ArrayList<AppItem>();

		mAppListAdapter = new AppListAdapter(context, R.layout.app_item,
				mAppList, fileiconhelper);

		setupAppListView(listview);
	}

	public ArrayAdapter getArrayAdapter() {
		return mAppListAdapter;
	}

	public long getCount() {
		return (long) mAppList.size();
	}

	public void initList() {
		update();
	}

	public void show(boolean flag) {
		if (flag) {
			mListView.setVisibility(View.VISIBLE);
		} else {
			mListView.setVisibility(View.GONE);
		}
	}

	public void update() {
		mAppList.clear();

		// setup app list here
		Thread thread = new Thread(this);
		thread.start();
	}

	private void setupAppListView(ListView listview) {
		mListView = listview;

		mListView.setAdapter(mAppListAdapter);

		mListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				Log.v(TAG, "setOnItemClickListener!!!");

				onAppListItemClick(arg0, arg1, arg2, arg3);
			}
		});
	}

	public void onAppListItemClick(AdapterView adapterview, View view, int i,
			long l) {
		AppItem item = (AppItem) mAppList.get(i);
		(new AppDialog(mContext, item)).show();
	}

	private void getInstalledApp() {
		Log.v(TAG, "Begin get Apps!");
		List<PackageInfo> packages = mContext.getPackageManager()
				.getInstalledPackages(0);
		for (Iterator<PackageInfo> iterator = packages.iterator(); iterator
				.hasNext();) {
			PackageInfo packageInfo = (PackageInfo) iterator.next();

			// non system applications
			if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
				String appName = packageInfo.applicationInfo.loadLabel(
						mContext.getPackageManager()).toString();
				AppItem item = new AppItem(appName, packageInfo);
				
				Drawable d = packageInfo.applicationInfo.loadIcon(mContext.getPackageManager());
				item.setIcon(d);
				
				mAppList.add(item);
			}
		}
		Log.v(TAG, "End get Apps![" + packages.size() + "][" + mAppList.size()
				+ "]");
	}

	public void run() {
		// TODO Auto-generated method stub

		mIsScanning = true;

		Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

		getInstalledApp();

		try {
			if (mActivity != null && !mActivity.isFinishing()) {
				Handler handler = mActivity.getHandler();
				if (handler != null) {
					handler.sendEmptyMessage(FileCategoryActivity.MSG_APP_UPDATED);
				}
			}
		} catch (Exception e) {
			Log.e(TAG, "Failed to update application list![" + e.toString()
					+ "]");
		}

		mIsScanning = false;
	}

	public boolean isScanning() {
		return mIsScanning;
	}
}
