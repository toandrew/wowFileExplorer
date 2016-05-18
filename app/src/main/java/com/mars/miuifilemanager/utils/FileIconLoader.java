package com.mars.miuifilemanager.utils;

import java.lang.ref.SoftReference;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import android.content.ContentProviderClient;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;

import com.mars.miuifilemanager.utils.FileCategoryHelper.FileCategory;
import com.mars.miuifilemanager.R;

public class FileIconLoader implements Handler.Callback{
	private static final String TAG = "FileIconLoader";
	
	private final ConcurrentHashMap mPendingRequests;
	
	private final Handler mMainThreadHandler;
	
	private final Context mContext;
	
	private IconLoadFinishListener mIconLoadListener;
	
	private ContentProviderClient mMediaProvider;

	private static final ConcurrentHashMap mImageCache = new ConcurrentHashMap();
	
	private boolean mPaused;
	
	private boolean mLoadingRequested;
	
	private LoaderThread mLoaderThread;
	
	public FileIconLoader(Context context, IconLoadFinishListener iconloadfinishlistener) {
		mPendingRequests = new ConcurrentHashMap();
		
		mMainThreadHandler = new Handler(this);
		
		mContext = context;
		
		mIconLoadListener = iconloadfinishlistener;
		
		mMediaProvider = mContext.getContentResolver().acquireContentProviderClient("media");
	}
	
	private class LoaderThread extends HandlerThread implements Handler.Callback {
		private Handler mLoaderThreadHandler;
		
		private Bitmap getImageThumbnail(long id) {
			return MediaStore.Images.Thumbnails.getThumbnail(mContext.getContentResolver(), id, 3, null);
		}
		
		private Bitmap getVideoThumbnail(long id) {
			return MediaStore.Video.Thumbnails.getThumbnail(mContext.getContentResolver(), id, 3, null);
		}
		
		public boolean handleMessage(Message msg) {
			
			FileId fileId = null;
			ImageHolder imgHolder = null;
			
			for (Iterator iterator = mPendingRequests.values().iterator();iterator.hasNext();) {
				fileId = (FileId)iterator.next();
				imgHolder =  (ImageHolder)FileIconLoader.mImageCache.get(fileId.mPath);
				if (imgHolder != null && imgHolder.state == 0) {
					break;
				}
			}
			
			if (imgHolder == null) {
				return true;
			}
			
			imgHolder.state = 1;
			
			switch(fileId.mCategory) {
			case Picture:
			case Video:
				
				boolean isVideo = false;
				if (fileId.mCategory == FileCategory.Video) {
					isVideo = true;
				}
				
				if (fileId.mId <= 0) {
					fileId.mId = getDbId(fileId.mPath, isVideo);
				}
				
				if (fileId.mId <= 0) {
					Log.e(TAG, "Fail to get dababase id for:" + fileId.mPath);	
				}
				
				Bitmap bitmap = null;
				if (fileId.mId > 0) {
					if (isVideo) {
						bitmap = getVideoThumbnail(fileId.mId);
					} else {
						bitmap = getImageThumbnail(fileId.mId);
					}
				}
				
				if (bitmap != null) {
					Log.v(TAG,"image is not null!");
					imgHolder.setImage(bitmap);
				} else {
					Bitmap icon = null;
					Log.v(TAG,"image is null! default!");
					if (isVideo) {
						icon = BitmapFactory.decodeResource(mContext.getResources(),
                            R.drawable.file_icon_video);
					} else {
						icon = BitmapFactory.decodeResource(mContext.getResources(),
	                            R.drawable.file_icon_picture);
					}
					imgHolder.setImage(icon);
				}
				break;
				
			case Apk:
				Drawable drawable = null;
				Log.v(TAG,"Ready to get apk icon[" + fileId.mPath + "]");
				try{
					drawable = Util.getApkIcon(mContext, fileId.mPath);
				} catch(Exception e){
					Log.e(TAG, "Failed to get apk icon[" + fileId.mPath + "]");
				}
				
				if (drawable != null) {
					imgHolder.setImage(drawable);
				} else {
					imgHolder.setImage(mContext.getResources().getDrawable(R.drawable.file_icon_default));
				}
				
				break;
			}
			
			imgHolder.state = 2;
			FileIconLoader.mImageCache.put(fileId.mPath, imgHolder);
			
			mMainThreadHandler.sendEmptyMessage(2);
			
			return true;
		}
		
		public void requestLoading() {
			if (mLoaderThreadHandler == null) {
				if (this.getLooper() == null) {
					Log.v(TAG, "getLooper null!!!");
				}
				
				mLoaderThreadHandler = new Handler(getLooper(), this);
			}
			mLoaderThreadHandler.sendEmptyMessage(0);
		}
		
		public LoaderThread(String name) {
			super(name);
		}
	}
	
	public static class FileId {
		public FileCategory mCategory;
		public long mId;
		public String mPath;

		public FileId(String s, long l, FileCategoryHelper.FileCategory filecategory) {
			mPath = s;
			mId = l;
			mCategory = filecategory;
		}
	}
	
	private static abstract class ImageHolder {
		int state;

		public static ImageHolder create(FileCategory category) {
			Object obj = null;
			
			switch(category) {
			case Apk:
				obj = new DrawableHolder();
				break;
			default:
				obj = new BitmapHolder();
				break;
			}
			
			return ((ImageHolder) (obj));
		}

		public abstract boolean isNull();

		public abstract void setImage(Object obj);

		public abstract boolean setImageView(ImageView imageview);

		private ImageHolder() {
		}
	}
	
	private static class DrawableHolder extends ImageHolder {
		SoftReference drawableRef;

		public boolean isNull() {
			return (drawableRef == null);
		}

		public void setImage(Object obj) {
			if (obj == null) {
				drawableRef = null;
			} else {
				Drawable drawable = (Drawable)obj;
				drawableRef = new SoftReference(drawable);
			}
		}

		public boolean setImageView(ImageView imageview) {
			if (drawableRef.get() == null) {
				return false;
			}
			
			Drawable drawable = (Drawable)drawableRef.get();
			imageview.setImageDrawable(drawable);
			
			return true;
		}

		private DrawableHolder() {
		}
	}
	
	private static class BitmapHolder extends ImageHolder {
		SoftReference bitmapRef;

		public boolean isNull() {
			return (bitmapRef == null);
		}

		public void setImage(Object obj) {
			if (obj == null) {
				bitmapRef = null;
			} else {
				Bitmap bitmap = (Bitmap)obj;
				bitmapRef = new SoftReference(bitmap);
			}
		}

		public boolean setImageView(ImageView imageview) {
			boolean flag;
			if (bitmapRef.get() == null) {
				return false;
			} 
			
			Bitmap bitmap = (Bitmap)bitmapRef.get();
			imageview.setImageBitmap(bitmap);
			
			return true;
		}

		private BitmapHolder() {
		}
	}
	
	@Override
	public boolean handleMessage(Message msg) {
		// TODO Auto-generated method stub
		
		switch(msg.what) {
		case 1:
			mLoadingRequested = false;
			if (!mPaused) {
				if (mLoaderThread == null) {
					mLoaderThread = new LoaderThread("iconLoader");
					mLoaderThread.start();
				}
				mLoaderThread.requestLoading();
			}
			
			return true;
			
		case 2:
			if (!mPaused) {
				processLoadedIcons();
			}
			
			return true;
		}
		
		return false;
	}

	private long getDbId(String path, boolean isVideo) {
		Uri uri = null;
		if (isVideo) {
			uri = MediaStore.Video.Media.getContentUri("external");
		} else {
			uri = MediaStore.Images.Media.getContentUri("external");
		}
		
		String selection = "_data=?";
		
		String args[] = new String[1];
		args[0] = path;
		
		String projection[] = new String[2];
		projection[0] = "_id";
		projection[1] = "_data";
		
		Cursor cursor = null;
		try{
			cursor = mMediaProvider.query(uri, projection, selection, args, null);
		} catch (Exception e){
			e.printStackTrace();
			
			return 0;
		}
		
		if (cursor == null) {
			return 0;
		}
		
		long id = -1;
		if (cursor.moveToNext()) {
			id = cursor.getLong(0);
		}
		
		cursor.close();
		
		return id;
	}
	
	public static interface IconLoadFinishListener {
		public abstract void onIconLoadFinished(ImageView imageview);
	}
	
	public boolean loadIcon(ImageView imageView, String path, long db, FileCategory category) {
		boolean ret = loadCachedIcon(imageView, path, category);
		if (ret) {
			mPendingRequests.remove(imageView);
			return true;
		}
		
		FileId fileId = new FileId(path, db, category);
		mPendingRequests.put(imageView, fileId);
		if (!mPaused) {
			requestLoading();
		}
		
		return false;
	}
	
	private boolean loadCachedIcon(ImageView imageView, String path, FileCategory category) {
		ImageHolder holder = (ImageHolder)mImageCache.get(path);
		if (holder == null) {
			holder = ImageHolder.create(category);
			
			if (holder == null) {
				return false;
			}
			holder.state = 0;
			mImageCache.put(path, holder);
			
			return false;
		}
		
		if (holder.state != 2) {
			holder.state = 0;
			return false;
		}
		
		if (holder.isNull()) {
			return false;
		}
		
		if (!holder.setImageView(imageView)) {
			holder.state = 0;
			return false;
		}
		
		return true;
	}
	
	private void requestLoading() {
		if (!mLoadingRequested) {
			mLoadingRequested = true;
			mMainThreadHandler.sendEmptyMessage(1);
		}
	}
	
	private void processLoadedIcons() {
		for (Iterator iterator = mPendingRequests.keySet().iterator(); iterator.hasNext();) {
			ImageView imageView = (ImageView)iterator.next();
			FileId fileId = (FileId)mPendingRequests.get(imageView);
			if (loadCachedIcon(imageView, fileId.mPath, fileId.mCategory)) {
				iterator.remove();
				mIconLoadListener.onIconLoadFinished(imageView);
			}
		}
		
		if (!mPendingRequests.isEmpty()) {
			requestLoading();
		} 
	}
}
