package com.mars.miuifilemanager.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.mars.miuifilemanager.ui.FileViewInteractionHub;
import com.mars.miuifilemanager.utils.FileIconHelper;

import com.mars.miuifilemanager.R;

public class FileListAdapter extends ArrayAdapter {
	private LayoutInflater mInflater;
	
	private FileViewInteractionHub mFileViewInteractionHub;
	
	private FileIconHelper mFileIcon;
	
	public FileListAdapter(Context context, int i, List list, FileViewInteractionHub fileviewinteractionhub, FileIconHelper fileiconhelper) {
		super(context, i, list);
		
		mInflater = LayoutInflater.from(context);
		
		mFileViewInteractionHub = fileviewinteractionhub;
		
		mFileIcon = fileiconhelper;
	}
	
	public View getView(int i, View convertView, ViewGroup viewgroup) {

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.file_browse_item, viewgroup, false); //0x7f030002
		} 
		
		View view = convertView;
		
		FileListItem item = (FileListItem)view;
		
		FileInfo fileInfo = mFileViewInteractionHub.getItem(i);

		item.bind(fileInfo, mFileViewInteractionHub, mFileIcon);
		
		return convertView;
	}
}
