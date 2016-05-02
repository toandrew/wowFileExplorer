package com.mars.miuifilemanager.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.os.Process;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.mars.miuifilemanager.ui.FileCategoryActivity;
import com.mars.miuifilemanager.ui.FileViewActivity;
import com.mars.miuifilemanager.utils.ActivitiesManager;
import com.mars.miuifilemanager.utils.FavoriteDatabaseHelper;
import com.mars.miuifilemanager.utils.FileConstants;
import com.mars.miuifilemanager.utils.FileIconHelper;
import com.mars.miuifilemanager.utils.IntentBuilder;
import com.mars.miuifilemanager.utils.Util;

import com.mars.miuifilemanager.R;

public class FavoriteList implements FavoriteDatabaseHelper.FavoriteDatabaseListener{
	private static final String TAG = "FavoriteList";
	
	private ListView mListView;
	
	private ArrayList mFavoriteList;
	
	private ArrayAdapter mFavoriteListAdapter;
	
	private FavoriteDatabaseHelper.FavoriteDatabaseListener mListener;

	private FavoriteDatabaseHelper mFavoriteDatabase;
	
	private OnMenuItemClickListener mMenuItemClick;
	
	private OnCreateContextMenuListener mListViewContextMenuListener;

	private Context mContext;
		
	private static final int MENU_ID_UNFAV = 1;
	
	private FileCategoryActivity mActivity;
	
	public FavoriteList(Context context, ListView listview, FavoriteDatabaseHelper.FavoriteDatabaseListener favoritedatabaselistener, FileIconHelper fileiconhelper) {
		
		mActivity = (FileCategoryActivity)context;
		
		mContext = context;
		
		mFavoriteList = new ArrayList();
		
		mListViewContextMenuListener = new OnCreateContextMenuListener(){
			public void onCreateContextMenu(ContextMenu menu, View v,
					ContextMenuInfo menuInfo) {
				// TODO Auto-generated method stub
				
				AdapterContextMenuInfo info = (AdapterContextMenuInfo)menuInfo;
				MenuItem menuItem = menu.add(0, MENU_ID_UNFAV, 0, R.string.operation_unfavorite);
				menuItem.setOnMenuItemClickListener(mMenuItemClick);
			}
		};
		
		mMenuItemClick = new OnMenuItemClickListener(){
			public boolean onMenuItemClick(MenuItem item) {
				// TODO Auto-generated method stub
				
				AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
				int pos = -1;
				if (info != null) {
					pos = info.position;
				} 
				
				if (pos == -1) {
					return false;
				}
				
				deleteFavorite(pos);
				
				return false;
			}
		};
		
		mFavoriteDatabase = new FavoriteDatabaseHelper(context, this);
		
		mFavoriteListAdapter = new FavoriteListAdapter(context, R.layout.favorite_item, mFavoriteList, fileiconhelper);//0x7f030001
		
		setupFavoriteListView(listview);
		
		mListener = favoritedatabaselistener;
	}
	
	@Override
	public void onFavoriteDatabaseChanged() {
		// TODO Auto-generated method stub
		
		update();
	}
	
	public ArrayAdapter getArrayAdapter() {
		return mFavoriteListAdapter;
	}
	
	public long getCount() {
		return (long)mFavoriteList.size();
	}
	
	public void initList() {
		mFavoriteList.clear();

		Thread thread = new Thread(new Runnable(){
			public void run() {
				// TODO Auto-generated method stub
				
				Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
				
				if (mFavoriteDatabase.isFirstCreate()) {
					for (Iterator<FavoriteItem> iterator = Util.getDefaultFavorites().iterator(); iterator.hasNext();) {
						FavoriteItem favoriteitem = (FavoriteItem)iterator.next();
						mFavoriteDatabase.insert(favoriteitem.title, favoriteitem.location);
					}
				}
				
				update();
			}
			
		});
		
		thread.start();
	}
	
	public void show(boolean flag) {
		if (flag) {
			mListView.setVisibility(View.VISIBLE);
		} else {
			mListView.setVisibility(View.GONE);
		}
	}
	
	public void update() {
		mFavoriteList.clear();
		Cursor cursor = mFavoriteDatabase.query();
		if (cursor != null) {
			while (cursor.moveToNext()) {
				FavoriteItem favoriteitem = new FavoriteItem(cursor.getLong(cursor.getColumnIndex(FileConstants.FavoriteList._ID)), cursor.getString(cursor.getColumnIndex(FileConstants.FavoriteList.TITLE)), cursor.getString(cursor.getColumnIndex(FileConstants.FavoriteList.LOCATION)));
				FileInfo fileinfo = Util.getFileInfo(favoriteitem.location);
				favoriteitem.fileInfo = fileinfo;
				mFavoriteList.add(favoriteitem);
			}
			
			cursor.close();
		}
		
		// remove not exsits
		for( int i = mFavoriteList.size() - 1; i >= 0; i--) { 
			String path = ((FavoriteItem)mFavoriteList.get(i)).location;
			if (!(new File(path)).exists()) {
				FavoriteItem item = (FavoriteItem)mFavoriteList.get(i);
				mFavoriteDatabase.delete(item.id, false);
				mFavoriteList.remove(i);
			}
		}
		
		try {
			if (mActivity != null && !mActivity.isFinishing()) {
				Handler handler = mActivity.getHandler();
				if (handler != null) {
					handler.sendEmptyMessage(FileCategoryActivity.MSG_FAV_UPDATED);
				}
			}
		} catch(Exception e) {
			Log.e(TAG,"Failed to update application list![" + e.toString() + "]");
		}

		mListener.onFavoriteDatabaseChanged();
	}
	
	private void deleteFavorite(int i) {
		FavoriteItem item = (FavoriteItem)mFavoriteList.get(i);
		
		mFavoriteDatabase.delete(item.id,false);
		
		mFavoriteList.remove(item);
		
		mFavoriteListAdapter.notifyDataSetChanged();
		
		mListener.onFavoriteDatabaseChanged();
	}
	
	private void setupFavoriteListView(ListView listview) {
		mListView = listview;
		
		mListView.setAdapter(mFavoriteListAdapter);
		
		mListView.setLongClickable(true);
		
		mListView.setOnCreateContextMenuListener(mListViewContextMenuListener);
		mListView.setOnItemClickListener(new OnItemClickListener(){
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				Log.v(TAG,"setOnItemClickListener!!!");
				onFavoriteListItemClick(arg0, arg1, arg2, arg3);
			}
		});
	}
	
	public void onFavoriteListItemClick(AdapterView adapterview, View view, int i, long l) {
		FavoriteItem item = (FavoriteItem)mFavoriteList.get(i);
		if (item.fileInfo.IsDir) {
			final FileViewActivity activity = (FileViewActivity)ActivitiesManager.getInstance().getActivity("FileView");
			if (activity == null) {
				Util.saveFavoriteDirPath(item.location, new FileViewActivity.OnBackPressedListener(){
					public boolean OnBack() {
						// TODO Auto-generated method stub
						Util.showTab(0);
						return true;
					}
				});
				Util.showTab(1);
				return;
			}
			
			activity.setPath(item.location, new FileViewActivity.OnBackPressedListener(){
				public boolean OnBack() {
					// TODO Auto-generated method stub
					
					activity.resetCurrentPath();
					activity.refresh();
					Util.showTab(0);
					return true;
				}
			});
			
			Util.showTab(1);

			return;
		}
		
		// view file
		try {
			IntentBuilder.viewFile(mContext, item.fileInfo.filePath);
		} catch (ActivityNotFoundException e) {
			Log.e(TAG, "failed to view file:" + e.toString());
		}
	}
}
