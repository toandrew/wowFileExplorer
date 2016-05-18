package com.mars.miuifilemanager.utils;

import java.io.FilenameFilter;
import java.util.HashMap;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.mars.miuifilemanager.R;
import com.mars.miuifilemanager.utils.FileSortHelper.SortMethod;

public class FileCategoryHelper {
	private static final String TAG = "FileCategoryHelper";
	
	private FileCategory mCategory;
	
	private HashMap mCategoryInfo;
	
	private Context mContext;
	
	public static HashMap mFilters = new HashMap();
	
	public static HashMap<FileCategory, Integer> mCategoryNames = new HashMap<FileCategory, Integer>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			put(FileCategory.Music, R.string.category_music);
			put(FileCategory.Video, R.string.category_video);
			put(FileCategory.Picture, R.string.category_picture);
			put(FileCategory.Theme, R.string.category_theme);
			put(FileCategory.Doc, R.string.category_document);
			put(FileCategory.Zip, R.string.category_zip);
			put(FileCategory.Apk, R.string.category_apk);
			put(FileCategory.Other, R.string.category_other);
			put(FileCategory.Favorite, R.string.category_favorite);
			put(FileCategory.Applications, R.string.category_applications);
			put(FileCategory.About, R.string.category_about);
		}
	};
	
	public static enum FileCategory {
		All,
		Music,
		Video,
		Picture,
		Theme,
		Doc,
		Zip,
		Apk,
		Custom,
		Other,
		Favorite,
		Applications,
		About
	}
	
	public class CategoryInfo {
		public long count;
		public long size;
	}
	
	public FileCategoryHelper(final Context context) {
		mCategoryInfo = new HashMap();
		mContext = context;
		mCategory= FileCategory.All;
	}
	
	public void setCurCategory(FileCategory filecategory) {
		mCategory = filecategory;
	}
	
	public FileCategory getCurCategory() {
		return mCategory;
	}
	
	public int getCurCategoryNameResId() {
		return ((Integer)mCategoryNames.get(mCategory)).intValue();
	}
	
	public void refreshCategoryInfo() {
		FileCategory categories[] = new FileCategory[]{
				FileCategory.Picture,
				FileCategory.Music,
				FileCategory.Video,
				FileCategory.Apk,
				FileCategory.Theme,
				FileCategory.Doc,
				FileCategory.Zip,
				FileCategory.Other,
				FileCategory.Applications
		};
		
		for (int i = 0; i < categories.length; i++) {
			setCategoryInfo(categories[i], 0, 0);
		}
		
		// music
		Uri uri = MediaStore.Audio.Media.getContentUri("external");
		refreshMediaCategory(FileCategory.Music, uri);
		
		// video
		uri = MediaStore.Video.Media.getContentUri("external");
		refreshMediaCategory(FileCategory.Video, uri);
		
		// picture
		uri = MediaStore.Images.Media.getContentUri("external");
		refreshMediaCategory(FileCategory.Picture, uri);
		
		//other
		refreshOtherCategory(FileCategory.Apk, FileConstants.CategoryList.CATEGORY_APK);
		refreshOtherCategory(FileCategory.Doc, FileConstants.CategoryList.CATEGORY_DOC);
		refreshOtherCategory(FileCategory.Zip, FileConstants.CategoryList.CATEGORY_ZIP);
		
		refreshAppCategory(FileCategory.Applications, FileConstants.CategoryList.CATEGORY_APPLICATIONS);
	}
	
	private void refreshAppCategory(FileCategory category, int app) {
		setCategoryInfo(category, 0, 0);
	}

	public static FileCategory getCategoryFromDbInt(int id) {
		switch (id) {
		case FileConstants.CategoryList.CATEGORY_APK:
			return FileCategory.Apk;
		case FileConstants.CategoryList.CATEGORY_DOC:
			return FileCategory.Doc;
		case FileConstants.CategoryList.CATEGORY_ZIP:
			return FileCategory.Zip;
		case FileConstants.CategoryList.CATEGORY_THEME:
			return FileCategory.Theme;
			
		case FileConstants.CategoryList.CATEGORY_OTHER:
		default:
			return FileCategory.Other;			
		}
	}
	
	private boolean refreshMediaCategory(FileCategory category, Uri uri) {
		String as[] = new String[2];
		as[0] = "COUNT(*)";
		as[1] = "SUM(_size)";
		
		ContentResolver resolver = mContext.getContentResolver();
		Cursor cursor = resolver.query(uri, as, null, null, null);
		if (cursor == null) {
			Log.e(TAG,"fail to query uri:" + uri.toString());
			
			return false;
		}
		
		if (cursor.moveToNext()) {
			long count = cursor.getLong(0);
			long size  = cursor.getLong(1);
			setCategoryInfo(category, count, size);
			cursor.close();
			Log.v(TAG, "Retrieved " + category.name() + " info >>> count:" + count + " size:" + size);

			return true;
		}
		
		return false;
	}
	
	private void setCategoryInfo(FileCategory category, long count, long size) {
		CategoryInfo categoryinfo = (CategoryInfo)mCategoryInfo.get(category);
		if (categoryinfo == null) {
			categoryinfo = new CategoryInfo();
			mCategoryInfo.put(category, categoryinfo);
		}
		categoryinfo.count = count;
		categoryinfo.size = size;
	}
	
	private boolean refreshOtherCategory(FileCategory category, int type) {
		String as[] = new String[2];
		as[0] = "COUNT(*)";
		as[1] = "SUM(_size)";
		
		String selection = FileConstants.CategoryList.TYPE + "=" + Integer.toString(type);
		
		ContentResolver resolver = mContext.getContentResolver();
		Cursor cursor = resolver.query(FileConstants.CategoryList.CONTENT_URI, as, selection, null, null);
		if (cursor == null) {
			Log.e(TAG,"fail to query uri:" + FileConstants.CategoryList.CONTENT_URI.toString());
			
			return false;
		}
		
		if (cursor.moveToNext()) {
			long count = cursor.getLong(0);
			long size  = cursor.getLong(1);
			setCategoryInfo(category, count, size);
			cursor.close();
			Log.v(TAG, "Retrieved " + category.name() + " info >>> count:" + count + " size:" + size);

			return true;
		}
		
		return false;
	}
	
	public HashMap getCategoryInfos() {
		return mCategoryInfo;
	}
	
	public Cursor query(FileCategory category, SortMethod sortmethod) {
		Uri uri = null;
		String s = null;
		String as[];
		String s1 = "title";
		String s2 = "mime_type";
		
		String sortMethod = null;
		
		String selection = null;
		switch(category) {
		case Music:
			uri = MediaStore.Audio.Media.getContentUri("external");
			break;
		case Video:
			uri = MediaStore.Video.Media.getContentUri("external");
			break;
		case Picture:
			uri = MediaStore.Images.Media.getContentUri("external");
			break;
		case Theme:
			//uri = MediaStore.Audio.Media.getContentUri("external"); 
			selection = FileConstants.CategoryList.TYPE + "=" + Integer.toString(FileConstants.CategoryList.CATEGORY_THEME);
			break;
		case Doc:
			//uri = MediaStore.Audio.Media.getContentUri("external");
			uri = FileConstants.CategoryList.CONTENT_URI;
			selection = FileConstants.CategoryList.TYPE + "=" + Integer.toString(FileConstants.CategoryList.CATEGORY_DOC);
			break;
		case Zip:
			//uri = MediaStore.Audio.Media.getContentUri("external");
			uri = FileConstants.CategoryList.CONTENT_URI;
			selection = FileConstants.CategoryList.TYPE + "=" + Integer.toString(FileConstants.CategoryList.CATEGORY_ZIP);
			break;
		case Apk:
			//uri = MediaStore.Audio.Media.getContentUri("external");
			uri = FileConstants.CategoryList.CONTENT_URI;
			selection = FileConstants.CategoryList.TYPE + "=" + Integer.toString(FileConstants.CategoryList.CATEGORY_APK);
			break;
		case Other:
			//uri = MediaStore.Audio.Media.getContentUri("external");
			uri = FileConstants.CategoryList.CONTENT_URI;
			selection = FileConstants.CategoryList.TYPE + "=" + Integer.toString(FileConstants.CategoryList.CATEGORY_OTHER);
			break;
		}
		
		if (uri == null) {
			return null;
		}
		
		String sortType = null;
		switch(sortmethod) {
		case name:
			sortType = "_display_name asc";
			break;
		case size:
			sortType = "_size desc";
			break;
		case date:
			sortType = "date_modified desc";
			break;
		case type:
			sortType = "mime_type asc";
			break;
		}

		if (category == FileCategory.Music || category == FileCategory.Video ||
			category == FileCategory.Picture) {
				
			String projection[] = new String[]{
				FileConstants.CategoryList._ID,
				FileConstants.CategoryList.DATA,
				FileConstants.CategoryList.SIZE,
				FileConstants.CategoryList.DATE_MODIFIED
			};
			return mContext.getContentResolver().query(uri, projection, null, null, sortType);
		}
		
		// other categories
		String projection[] = new String[]{
			FileConstants.CategoryList._ID,
			FileConstants.CategoryList.DATA,
			FileConstants.CategoryList.SIZE,
			FileConstants.CategoryList.DATE_MODIFIED,
			FileConstants.CategoryList.TYPE
		};
		
		switch(sortmethod) {
		case name:
			sortType = FileConstants.CategoryList.DATA + " asc";
			break;
		case size:
			sortType = FileConstants.CategoryList.SIZE + " desc";
			break;
		case date:
			sortType = FileConstants.CategoryList.DATE_MODIFIED + " desc";
			break;
		case type:
			sortType = FileConstants.CategoryList.TYPE + " asc";
			break;
		}
		return mContext.getContentResolver().query(uri, projection, selection, null, sortType);		
	}
	
	public static FileCategory getCategoryFromPath(String path) {
		
		FileCategory category = FileCategory.Other;
		Log.v(TAG,"getCategoryFromPath![" + path +"]");
		
		try{
			if (IntentBuilder.isAudioFileType(path)) {
				category = FileCategory.Music;
			} else if (IntentBuilder.isVideoFileType(path)) {
				category = FileCategory.Video;
			} else if (IntentBuilder.isImageFileType(path)) {
				category = FileCategory.Picture;
			} else {
				category = getCategoryFromDbInt(getFileCategoryFromPath(path));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return category;
	}
	
	public static int getFileCategoryFromPath(String path) {
		
		MediaFile.MediaFileType mimeFileType = MediaFile
				.getFileType(path);
		if (mimeFileType == null) {
			return FileConstants.CategoryList.CATEGORY_OTHER;
		}
		
		int type = mimeFileType.fileType;
		
		if (MediaFile.isApkFileType(type)) {
			return FileConstants.CategoryList.CATEGORY_APK;
		}
		
		if (MediaFile.isDocFileType(type)) {
			return FileConstants.CategoryList.CATEGORY_DOC;
		}
		
		if (MediaFile.isZipFileType(type)) {
			return FileConstants.CategoryList.CATEGORY_ZIP;
		}
		
		return FileConstants.CategoryList.CATEGORY_OTHER;
	}
	
	public FilenameFilter getFilter() {
		return (FilenameFilter)mFilters.get(mCategory);
	}
	
	public void setCustomCategory(String filter[]) {
		mCategory = FileCategory.Custom;
		if (mFilters.containsKey(FileCategory.Custom)) {
			mFilters.remove(FileCategory.Custom);
		}
		
		FilenameExtFilter fileFilter = new FilenameExtFilter(filter);
		mFilters.put(FileCategory.Custom, fileFilter);
	}
	
	
	public int delete(FileCategory category, long dbId) {
		Uri uri = null;
		
		switch(category) {
		case Music:
			uri = MediaStore.Audio.Media.getContentUri("external");
			break;
		case Video:
			uri = MediaStore.Video.Media.getContentUri("external");
			break;
		case Picture:
			uri = MediaStore.Images.Media.getContentUri("external");
			break;
		case Theme:
			break;
		case Doc:
			uri = FileConstants.CategoryList.CONTENT_URI;
			break;
		case Zip:
			uri = FileConstants.CategoryList.CONTENT_URI;
			break;
		case Apk:
			uri = FileConstants.CategoryList.CONTENT_URI;
			break;
		case Other:
			uri = FileConstants.CategoryList.CONTENT_URI;
			break;
		}
		
		if (uri == null) {
			return -1;
		}
		
		String where = "_id" + "=" + Long.toString(dbId);
		
		int ret = 0;
		try {
			ret = mContext.getContentResolver().delete(uri, where,null);
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return ret;
	}
}
