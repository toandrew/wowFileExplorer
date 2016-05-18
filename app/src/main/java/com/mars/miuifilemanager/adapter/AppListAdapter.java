package com.mars.miuifilemanager.adapter;

import java.util.List;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mars.miuifilemanager.utils.FileIconHelper;
import com.mars.miuifilemanager.utils.Util;
import com.mars.miuifilemanager.R;

public class AppListAdapter extends ArrayAdapter<AppItem>{
	private LayoutInflater mInflater;
	
	private FileIconHelper mFileIcon;
	
	private Context mContext;
	
	public AppListAdapter(Context context, int i, List list, FileIconHelper fileiconhelper) {
		super(context, i, list);
		
		mInflater = LayoutInflater.from(context);
		mFileIcon = fileiconhelper;
		mContext = context;
	}
	
	public View getView(int pos, View convertView, ViewGroup viewgroup) {
		if (convertView == null) {
			convertView =  mInflater.inflate(R.layout.app_item, viewgroup, false);
		}
		
		AppItem item = (AppItem)getItem(pos);
		Util.setText(convertView, R.id.app_name, item.getAppName());
		
		PackageInfo info = item.getPackageInfo();
		
		ImageView fileImage = (ImageView)convertView.findViewById(R.id.app_image);
		ImageView fileImageFrame = (ImageView)convertView.findViewById(R.id.app_image_frame);
		fileImageFrame.setVisibility(View.GONE);
		
		Drawable d = item.getIcon();
		if (d != null) {
			fileImage.setImageDrawable(d);
		} else {
			fileImage.setImageResource(R.drawable.file_icon_default);
		}
		
		TextView ver = (TextView)convertView.findViewById(R.id.app_ver);
		ver.setText(mContext.getResources().getString(R.string.app_verion) + info.versionName);
		
		return convertView;
	}
}
