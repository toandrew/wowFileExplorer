package com.mars.miuifilemanager.adapter;

import java.util.Collection;
import java.util.HashMap;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

import com.mars.miuifilemanager.ui.FileViewInteractionHub;
import com.mars.miuifilemanager.utils.FileConstants;
import com.mars.miuifilemanager.utils.FileIconHelper;
import com.mars.miuifilemanager.utils.Util;

import com.mars.miuifilemanager.R;

public class FileListCursorAdapter extends CursorAdapter {
	private static final String TAG = "FileListCursorAdapter";
	
	private HashMap mFileNameList;
	
	private final LayoutInflater mFactory;
	
	private FileViewInteractionHub mFileViewInteractionHub;
	
	private FileIconHelper mFileIcon;
	
	public FileListCursorAdapter(Context context, Cursor cursor, FileViewInteractionHub fileviewinteractionhub, FileIconHelper fileiconhelper) {
		super(context, cursor, false);
		
		mFileNameList = new HashMap();
		
		mFactory = LayoutInflater.from(context);
		
		mFileViewInteractionHub = fileviewinteractionhub;
		
		mFileIcon = fileiconhelper;
	}
	
	private FileInfo getFileInfo(Cursor cursor) {
		if (cursor == null || cursor.getCount() == 0) {
			return null;
		}
		
		return Util.getFileInfo(cursor.getString(cursor.getColumnIndex(FileConstants.CategoryList.DATA)));
	}
	
	public void changeCursor(Cursor cursor) {
		mFileNameList.clear();
		
		super.changeCursor(cursor);
	}
	
	@Override
	public void bindView(View arg0, Context arg1, Cursor arg2) {
		// TODO Auto-generated method stub
		
		FileListItem item = (FileListItem)arg0;
		int pos = arg2.getPosition();
		FileInfo fileInfo = getFileItem(pos);
		if (fileInfo == null) {
			fileInfo = new FileInfo();
			fileInfo.dbId = arg2.getLong(arg2.getColumnIndex(FileConstants.CategoryList._ID));
			fileInfo.filePath = arg2.getString(arg2.getColumnIndex(FileConstants.CategoryList.DATA));
			fileInfo.fileName =  Util.getNameFromFilepath(fileInfo.filePath);
			fileInfo.fileSize = arg2.getLong(arg2.getColumnIndex(FileConstants.CategoryList.SIZE));
			fileInfo.ModifiedDate = arg2.getLong(arg2.getColumnIndex(FileConstants.CategoryList.DATE_MODIFIED));
		}
		
		item.bind(fileInfo, mFileViewInteractionHub, mFileIcon);
	}

	@Override
	public View newView(Context arg0, Cursor arg1, ViewGroup arg2) {
		// TODO Auto-generated method stub
		
		return mFactory.inflate(R.layout.file_browse_item, arg2, false); // 0x7f030002
	}
	
	
	public Collection getAllFiles() {
		Collection collection = null;
		
		if (mFileNameList.size() == getCount()) {
			collection = mFileNameList.values();
		} else {
			Cursor cursor = getCursor();
			if (cursor.moveToFirst()) {
				do {
					if (!mFileNameList.containsKey(cursor.getPosition())) {
						FileInfo fileInfo = getFileInfo(cursor);
						if (fileInfo != null && (!fileInfo.IsDir && fileInfo.fileSize > 0)) {
							mFileNameList.put(cursor.getPosition(), fileInfo);
						}
					}
				} while (cursor.moveToNext());
			}
			
			collection = mFileNameList.values();
		}
		
		return collection;
	}
	
	public FileInfo getFileItem(int i) {
		FileInfo fileInfo;
		if (mFileNameList.containsKey(i)) {
			fileInfo = (FileInfo)mFileNameList.get(i);
		} else {
			Cursor cursor = (Cursor)getItem(i);
			fileInfo = getFileInfo(cursor);
			if (fileInfo != null) {
				fileInfo.dbId = cursor.getLong(0);
				mFileNameList.put(i, fileInfo);
			}
		}
		
		return fileInfo;
	}
}
