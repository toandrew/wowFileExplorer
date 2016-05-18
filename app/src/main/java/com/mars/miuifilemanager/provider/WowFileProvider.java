package com.mars.miuifilemanager.provider;

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.mars.miuifilemanager.utils.FileConstants;

public class WowFileProvider extends ContentProvider {
	private static final String TAG = "WowFileProvider";

    private static final Uri NOTIFICATION_URI = Uri.parse("content://wowfileexplorer");

    public static final String WOWFILEEXPLORER_AUTHORITY = "wowfileexplorerv3";

    private static final String DATABASE_NAME = "wowfileexplorer.db";

    private static final int DATABASE_VERSION = 1;

    private static HashMap<String, String> sCategoryListProjectionMap;
    
    private static HashMap<String, String> sFavoriteProjectionMap;

    private static final int FAVORITE_LIST 			= 1;
    private static final int FAVORITE_LIST_ID 		= 2;
    private static final int CATEGORY_LIST 			= 3;
    private static final int CATEGORY_LIST_ID 		= 4;

    private static final UriMatcher sUriMatcher;
    
    private FileDatabaseHelper mOpenHelper;

    @Override
    public boolean onCreate() {
        mOpenHelper = FileDatabaseHelper.getInstance(getContext());

        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        String orderBy;

        int match = sUriMatcher.match(uri);
        switch ( match ) {
        case FAVORITE_LIST:
            qb.setTables(FileConstants.FavoriteList.FAVORITE_TABLE_NAME);
            qb.setProjectionMap(sFavoriteProjectionMap);

            // If no sort order is specified use the default
            if (TextUtils.isEmpty(sortOrder)) {
                orderBy = FileConstants.FavoriteList.DEFAULT_SORT_ORDER;
            } else {
                orderBy = sortOrder;
            }
            break;

        case FAVORITE_LIST_ID:
            qb.setTables(FileConstants.FavoriteList.FAVORITE_TABLE_NAME);
            qb.setProjectionMap(sFavoriteProjectionMap);
            qb.appendWhere(FileConstants.FavoriteList._ID + "=" + uri.getPathSegments().get(1));

            // If no sort order is specified use the default
            if (TextUtils.isEmpty(sortOrder)) {
                orderBy = FileConstants.FavoriteList.DEFAULT_SORT_ORDER;
            } else {
                orderBy = sortOrder;
            }
            break;

        case CATEGORY_LIST:
            qb.setTables(FileConstants.CategoryList.CATEGORY_TABLE_NAME);
            //qb.setProjectionMap(sCategoryListProjectionMap);

            // If no sort order is specified use the default
            if (TextUtils.isEmpty(sortOrder)) {
                orderBy = FileConstants.CategoryList.DEFAULT_SORT_ORDER;
            } else {
                orderBy = sortOrder;
            }
            break;

        case CATEGORY_LIST_ID:
            qb.setTables(FileConstants.CategoryList.CATEGORY_TABLE_NAME);
            qb.setProjectionMap(sCategoryListProjectionMap);
            qb.appendWhere(FileConstants.CategoryList._ID + "=" + uri.getPathSegments().get(1));

            // If no sort order is specified use the default
            if (TextUtils.isEmpty(sortOrder)) {
                orderBy = FileConstants.CategoryList.DEFAULT_SORT_ORDER;
            } else {
                orderBy = sortOrder;
            }
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }


        // Get the database and run the query
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);

        // Tell the cursor what uri to watch, so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
        case FAVORITE_LIST:
            return FileConstants.FavoriteList.CONTENT_TYPE;

        case FAVORITE_LIST_ID:
            return FileConstants.FavoriteList.CONTENT_ITEM_TYPE;

        case CATEGORY_LIST:
            return FileConstants.CategoryList.CONTENT_TYPE;

        case CATEGORY_LIST_ID:
            return FileConstants.CategoryList.CONTENT_ITEM_TYPE;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    private Uri insertFavoriteList(ContentValues initialValues) {
        if (initialValues == null) {
        	Log.e(TAG, "insert favorite failed: null parameters!!!");
        	return null;
        }
        
        ContentValues values = new ContentValues(initialValues);

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long rowId = db.insert(FileConstants.FavoriteList.FAVORITE_TABLE_NAME, null,values);
        if (rowId > 0) {
            Uri uri = ContentUris.withAppendedId(FileConstants.FavoriteList.CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(uri, null);
            return uri;
        }

        return null;
    }
    
    private Uri insertCategory(ContentValues initialValues) {
        if (initialValues == null) {
        	Log.e(TAG, "insert category failed: null parameters!!!");
        	return null;
        }

        ContentValues values = new ContentValues(initialValues);

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long rowId = db.insert(FileConstants.CategoryList.CATEGORY_TABLE_NAME, null,values);
        if (rowId > 0) {
            Uri uri = ContentUris.withAppendedId(FileConstants.CategoryList.CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(uri, null);
            return uri;
        }

        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        Uri ret = null;

        int match = sUriMatcher.match(uri);

        switch( match ) {
            case FAVORITE_LIST:
                ret = insertFavoriteList(initialValues);
                break;

            case CATEGORY_LIST:
                ret = insertCategory(initialValues);
                break;

            default:
                Log.e(TAG, "Invalid request: " + uri);
                return null;
        }

        return ret;
    }
    
    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
        case FAVORITE_LIST:
            count = db.delete(FileConstants.FavoriteList.FAVORITE_TABLE_NAME, where, whereArgs);
            break;

        case FAVORITE_LIST_ID:
            String id = uri.getPathSegments().get(1);
            count = db.delete(FileConstants.FavoriteList.FAVORITE_TABLE_NAME, FileConstants.FavoriteList._ID + "=" + id
                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
            break;

        case CATEGORY_LIST:
            count = db.delete(FileConstants.CategoryList.CATEGORY_TABLE_NAME, where, whereArgs);
            break;

        case CATEGORY_LIST_ID:
            String category_id = uri.getPathSegments().get(1);
            count = db.delete(FileConstants.CategoryList.CATEGORY_TABLE_NAME, FileConstants.CategoryList._ID + "=" + category_id + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
    
    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
        case FAVORITE_LIST:
            count = db.update(FileConstants.FavoriteList.FAVORITE_TABLE_NAME, values, where, whereArgs);
            break;

        case FAVORITE_LIST_ID:
            String id = uri.getPathSegments().get(1);
            count = db.update(FileConstants.FavoriteList.FAVORITE_TABLE_NAME, values, FileConstants.FavoriteList._ID + "=" + id
                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
            break;
            
        case CATEGORY_LIST:
            count = db.update(FileConstants.CategoryList.CATEGORY_TABLE_NAME, values, where, whereArgs);
            break;

        case CATEGORY_LIST_ID:
            String category_id = uri.getPathSegments().get(1);
            count = db.update(FileConstants.CategoryList.CATEGORY_TABLE_NAME, values, FileConstants.CategoryList._ID + "=" + category_id + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : "") , whereArgs);
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
    
    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(WOWFILEEXPLORER_AUTHORITY, "favorite", FAVORITE_LIST);
        sUriMatcher.addURI(WOWFILEEXPLORER_AUTHORITY, "favorite/#", FAVORITE_LIST_ID);
        sUriMatcher.addURI(WOWFILEEXPLORER_AUTHORITY, "category", CATEGORY_LIST);
        sUriMatcher.addURI(WOWFILEEXPLORER_AUTHORITY, "category/#", CATEGORY_LIST_ID);

        // category list
        sCategoryListProjectionMap = new HashMap<String, String>();
        sCategoryListProjectionMap.put(FileConstants.CategoryList._ID, FileConstants.CategoryList._ID);
        sCategoryListProjectionMap.put(FileConstants.CategoryList.DATA, FileConstants.CategoryList.DATA);
        sCategoryListProjectionMap.put(FileConstants.CategoryList.SIZE, FileConstants.CategoryList.SIZE);
        sCategoryListProjectionMap.put(FileConstants.CategoryList.DATE_MODIFIED, FileConstants.CategoryList.DATE_MODIFIED);
        sCategoryListProjectionMap.put(FileConstants.CategoryList.TYPE, FileConstants.CategoryList.TYPE);

        // favorite list
        sFavoriteProjectionMap = new HashMap<String, String>();
        sFavoriteProjectionMap.put(FileConstants.FavoriteList._ID, FileConstants.FavoriteList._ID);
        sFavoriteProjectionMap.put(FileConstants.FavoriteList.TITLE, FileConstants.FavoriteList.TITLE);
        sFavoriteProjectionMap.put(FileConstants.FavoriteList.LOCATION,FileConstants.FavoriteList.LOCATION);
    }
}
