package com.mars.miuifilemanager.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.mars.miuifilemanager.utils.FileConstants;

/**
 * database help class for wowFileExplorer
 */
public class FileDatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "FileDatabaseHelper";

    private static FileDatabaseHelper mInstance = null;

    static final String DATABASE_NAME = "wowfileexplorer.db";
    
    static final int DATABASE_VERSION = 2;

    private FileDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Return a singleton helper for the wowFileExplorer database.
     */
    /* package */ static synchronized FileDatabaseHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new FileDatabaseHelper(context);
        }

        return mInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createFavoriteListTable(db);
        createCategoryTable(db);
    }

    /**
     * create favorite list table
     */
    private void createFavoriteListTable( SQLiteDatabase db ) {
        db.execSQL("CREATE TABLE " + FileConstants.FavoriteList.FAVORITE_TABLE_NAME + " ("
                                   + FileConstants.FavoriteList._ID + " INTEGER PRIMARY KEY autoincrement,"
                                   + FileConstants.FavoriteList.TITLE + " TEXT,"
                                   + FileConstants.FavoriteList.LOCATION + " TEXT" + ");");
    }

    /**
     * create category list table
     */
    private void createCategoryTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE "  + FileConstants.CategoryList.CATEGORY_TABLE_NAME + " (" 
                                    + FileConstants.CategoryList._ID + " INTEGER PRIMARY KEY autoincrement,"
                                    + FileConstants.CategoryList.DATA + " TEXT," 
                                    + FileConstants.CategoryList.SIZE + " INTEGER," 
                                    + FileConstants.CategoryList.DATE_MODIFIED + " INTEGER," 
                                    + FileConstants.CategoryList.TYPE + " INTEGER" + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion
                + " to " + newVersion + ", which will destory all old data.");

        Log.w(TAG, "Destroying all old data.");
        dropAll(db);

        Log.w(TAG, "Re-create those tables");
        onCreate(db);
    }

    private void dropAll(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + FileConstants.FavoriteList.FAVORITE_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + FileConstants.CategoryList.CATEGORY_TABLE_NAME);
    }
}
