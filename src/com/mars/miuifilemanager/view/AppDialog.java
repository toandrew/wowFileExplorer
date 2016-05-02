package com.mars.miuifilemanager.view;

import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.mars.miuifilemanager.adapter.AppItem;
import com.mars.miuifilemanager.R;

public class AppDialog extends AlertDialog {
	private static final String TAG = "AppDialog";

	private static final int MSG_CLOSE_DIALOG = 100;

	private View mView;

	private Context mContext;

	AppItem mItem;

	public AppDialog(Context context, AppItem item) {
		super(context);

		mContext = context;
		mItem = item;
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_CLOSE_DIALOG:
			try {
				AppDialog.this.dismiss();
			} catch(Exception e){
				
			}
			default:
			}
		}
	};

	protected void onCreate(Bundle bundle) {
		mView = getLayoutInflater().inflate(R.layout.app_dialog, null);// 0x7f030006

		setIcon(mItem.getPackageInfo().applicationInfo.loadIcon(mContext
				.getPackageManager()));

		setTitle(mItem.getAppName());

		View infoView = mView.findViewById(R.id.app_info);
		infoView.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// TODO Auto-generated method stub

				getAppInfo(mItem);
				
				mHandler.sendEmptyMessage(MSG_CLOSE_DIALOG);
			}

		});

		View runView = mView.findViewById(R.id.app_run);
		runView.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// TODO Auto-generated method stub

				runApp(mItem);
				
				mHandler.sendEmptyMessage(MSG_CLOSE_DIALOG);
			}

		});

		View unsView = mView.findViewById(R.id.app_uninstall);
		unsView.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// TODO Auto-generated method stub

				uninstallApp(mItem);
				
				mHandler.sendEmptyMessage(MSG_CLOSE_DIALOG);
			}

		});

		setView(mView);

		super.onCreate(bundle);
	}

	private void getAppInfo(AppItem item) {

		// newer
		Intent intent = new Intent(
				"android.settings.APPLICATION_DETAILS_SETTINGS");
		intent.setData(Uri.fromParts("package",
				item.getPackageInfo().packageName, null));
		intent.setClassName("com.android.settings",
				"com.android.settings.applications.InstalledAppDetails");
		List<ResolveInfo> list = mContext.getPackageManager().queryIntentActivities(intent,
				0);
		if (list.size() > 0) {
			Log.v(TAG, "1 getApp info [" + intent.toString() + "]");
			mContext.startActivity(intent);
			return;
		}
		
		// older
		Intent sintent = new Intent(Intent.ACTION_VIEW);
		sintent.setClassName("com.android.settings",
				"com.android.settings.InstalledAppDetails");
		sintent.putExtra("com.android.settings.ApplicationPkgName",
				item.getPackageInfo().packageName);
		sintent.putExtra("pkg", item.getPackageInfo().packageName);
		list = mContext.getPackageManager().queryIntentActivities(sintent,
				0);
		if (list.size() > 0) {
			Log.v(TAG, "1 getApp info [" + sintent.toString() + "]");
			mContext.startActivity(sintent);
			return;
		}
		
		Toast.makeText(mContext, mContext.getResources().getString(R.string.get_app_info_failed), Toast.LENGTH_SHORT).show();
	}

	private void runApp(AppItem item) {
		try {
			Intent intent = mContext.getPackageManager().getLaunchIntentForPackage(item.getPackageInfo().packageName);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mContext.startActivity(intent);
		} catch(Exception e) {
			Toast.makeText(mContext, mContext.getResources().getString(R.string.run_app_failed), Toast.LENGTH_SHORT).show();
		}
	}

	private void uninstallApp(AppItem item) {
		try {
			String args[] = new String[2];
			args[0] = "package:";
			args[1] = item.getPackageInfo().packageName;
			
			Intent intent = new Intent(Intent.ACTION_DELETE, Uri.parse(String.format("%s%s", args[0], args[1])));
			mContext.startActivity(intent);
		} catch (Exception e) {
			Toast.makeText(mContext, mContext.getResources().getString(R.string.uninstall_app_failed), Toast.LENGTH_SHORT).show();
		}
	}
}
