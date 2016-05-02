package com.mars.miuifilemanager.utils;

import android.net.Uri;
import android.provider.BaseColumns;

import com.mars.miuifilemanager.provider.WowFileProvider;

public class FileConstants {
	
	private FileConstants() {}
	
	public static final class FavoriteList implements BaseColumns {
        public static final String FAVORITE_TABLE_NAME = "favorite";
        
        public static final String DEFAULT_SORT_ORDER = " title DESC";

        public static final String AUTHORITY = WowFileProvider.WOWFILEEXPLORER_AUTHORITY;

        private FavoriteList() {}
        
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/favorite");

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.wowfileexplorer.favorite";

        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.wowfileexplorer.favorite.favoritelistitem";

        public static final String TITLE= "title";

        public static final String LOCATION = "location";
	}

    public static final class CategoryList implements BaseColumns{
    	
        public static final String CATEGORY_TABLE_NAME = "category";
        
        public static final String DEFAULT_SORT_ORDER = "date_modified DESC";

        public static final String AUTHORITY = WowFileProvider.WOWFILEEXPLORER_AUTHORITY;

        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/category");

        private CategoryList() {}

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.wowfileexplorer.category";

        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.wowfileexplorer.category.categorylistitem";

        public static final String DATA            		= "_data";
        public static final String SIZE              	= "_size";
        public static final String DATE_MODIFIED        = "date_modified";
        public static final String TYPE			        = "type";
        
        public static final int CATEGORY_OTHER   	= 0;
        public static final int CATEGORY_APK  		= 1;
        public static final int CATEGORY_DOC   		= 2;
        public static final int CATEGORY_ZIP    	= 3;
        public static final int CATEGORY_THEME 		= 4;
        public static final int CATEGORY_APPLICATIONS = 5;
    }
}
