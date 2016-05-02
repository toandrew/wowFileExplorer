package com.mars.miuifilemanager.utils;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class FavoriteDatabaseHelper {
	private static final String TAG = "FavoriteDatabaseHelper";
	
	private static FavoriteDatabaseHelper mInstance;
	
	private boolean mFirstCreate = true;
	
	private FavoriteDatabaseListener mListener;
	
	private Context mContext;
	
	public static interface FavoriteDatabaseListener {
		public abstract void onFavoriteDatabaseChanged();
	}

	public FavoriteDatabaseHelper(Context context, FavoriteDatabaseListener listener) {
		mContext = context;
		
		mInstance = this;
		
		mListener = listener;
	}
	
	private ContentValues createValues(String title, String location) {
		ContentValues val = new ContentValues();
		val.put(FileConstants.FavoriteList.TITLE, title);
		val.put(FileConstants.FavoriteList.LOCATION, location);
		
		return val;
	}
	
	public static FavoriteDatabaseHelper getInstance() {
		return mInstance;
	}
	
	public void delete(long id, boolean notify) {
		mContext.getContentResolver().delete(FileConstants.FavoriteList.CONTENT_URI, FileConstants.FavoriteList._ID + "=" + id, null);
		
		if (notify) {
			mListener.onFavoriteDatabaseChanged();
		}
	}
	
	public void delete(String location) {
		mContext.getContentResolver().delete(FileConstants.FavoriteList.CONTENT_URI, "location" + "=" + "'" + location + "'", null);
		
		mListener.onFavoriteDatabaseChanged();
	}
	
    private static final String[] PROJECTION = new String[] {
    	FileConstants.FavoriteList._ID, // 0
    	FileConstants.FavoriteList.TITLE, // 1
    	FileConstants.FavoriteList.LOCATION // 2
    };
    
	public boolean isFavorite(String location) {
		Cursor cursor = mContext.getContentResolver().query(FileConstants.FavoriteList.CONTENT_URI, PROJECTION, "location=?", new String[]{location}, null);
		if (cursor == null) {
			return false;
		}
		
		boolean ret = false;
		if (cursor.getCount() > 0) {
			ret = true;
		}
		
		cursor.close();
		
		return ret;
	}
	
	public boolean isFirstCreate() {
		return mFirstCreate;
	}
	
	public boolean insert(String title, String location) {
		boolean ret = true;
		
		if (isFavorite(location)) {
			ret = false;
		} else {
			ContentValues cv = createValues(title, location);	
			mContext.getContentResolver().insert(FileConstants.FavoriteList.CONTENT_URI, cv);
			
			mListener.onFavoriteDatabaseChanged();
		}
		
		return ret;
	}
	
	public Cursor query() {
		return mContext.getContentResolver().query(FileConstants.FavoriteList.CONTENT_URI, null, null, null, null);
	}
}
