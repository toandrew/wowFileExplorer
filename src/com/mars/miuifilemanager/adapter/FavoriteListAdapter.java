package com.mars.miuifilemanager.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.mars.miuifilemanager.utils.FileIconHelper;
import com.mars.miuifilemanager.utils.Util;
import com.mars.miuifilemanager.R;

public class FavoriteListAdapter extends ArrayAdapter<FavoriteItem>{
	private LayoutInflater mInflater;
	
	private FileIconHelper mFileIcon;
	
	private Context mContext;
	
	public FavoriteListAdapter(Context context, int i, List list, FileIconHelper fileiconhelper) {
		super(context, i, list);
		
		mInflater = LayoutInflater.from(context);
		mFileIcon = fileiconhelper;
		mContext = context;
	}

	public View getView(int pos, View convertView, ViewGroup viewgroup) {
		if (convertView == null) {
			convertView =  mInflater.inflate(R.layout.favorite_item, viewgroup, false);//0x7f030001
		}
		
		FavoriteItem item = (FavoriteItem)getItem(pos);
		FileInfo fileInfo = item.fileInfo;
		if (item.title != null) {
			Util.setText(convertView, R.id.file_name, item.title);//0x7f080006
		} else {
			Util.setText(convertView, R.id.file_name, fileInfo.fileName);
		}
		
		String dateStr = Util.formatDateString(mContext, fileInfo.ModifiedDate);
		Util.setText(convertView, R.id.modified_time, dateStr); //0x7f080007
		
		if (fileInfo.IsDir) {
			Util.setText(convertView, R.id.file_size, ""); // 0x7f080008
		} else {
			Util.setText(convertView, R.id.file_size, Util.convertStorage(fileInfo.fileSize));
		}
		
		ImageView fileImage = (ImageView)convertView.findViewById(R.id.file_image); // 0x7f080004
		ImageView fileImageFrame = (ImageView)convertView.findViewById(R.id.file_image_frame); //0x7f080003
		fileImage.setTag(pos);
		if (fileInfo.IsDir) {
			fileImageFrame.setVisibility(View.GONE);
			fileImage.setImageResource(R.drawable.folder_fav);//0x7f020031
		} else {
			mFileIcon.setIcon(fileInfo, fileImage, fileImageFrame);
		}
		return convertView;
	}
}
