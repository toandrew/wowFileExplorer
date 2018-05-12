package com.mars.miuifilemanager.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import org.swiftp.FtpUtil;

import android.app.Service;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;

import com.mars.miuifilemanager.adapter.FileInfo;
import com.mars.miuifilemanager.utils.FileCategoryHelper.FileCategory;
import com.mars.miuifilemanager.utils.FileConstants;
import com.mars.miuifilemanager.utils.Util;

public class FileScanService extends Service implements Runnable {
	private static final String TAG = "FileScanService";

	private ServiceHandler mServiceHandler;

	private Context mContext;

	private Looper mServiceLooper;

	protected static Thread mServerThread = null;

	protected boolean mShouldExit = false;

	public static final int WAKE_INTERVAL_MS = 1000; // milliseconds

	private ArrayList<FileInfo> mApkFileList = new ArrayList<FileInfo>();

	private ArrayList<FileInfo> mCompressedFileList = new ArrayList<FileInfo>();

	private ArrayList<FileInfo> mDocFileList = new ArrayList<FileInfo>();

	/*
	 * public static final String APK_EXT = ".apk";
	 * 
	 * public static final String DOC_TXT_EXT = ".txt"; public static final
	 * String DOC_PDF_EXT = ".pdf"; public static final String DOC_EPUB_EXT =
	 * ".epub"; public static final String DOC_DOC_EXT = ".doc";
	 * 
	 * public static final String COM_ZIP_EXT = ".zip"; public static final
	 * String COM_RAR_EXT = ".rar"; public static final String COM_TAR_EXT =
	 * ".tar"; public static final String COM_TAR_GZ_EXT = ".tar.gz"; public
	 * static final String COM_TGZ_EXT = ".tgz"; public static final String
	 * COM_7Z_EXT = ".7z";
	 */

	public static final String ACTION_UPDATE_UI = "ACTION_UPDATE_UI";
	public static final String ACTION_UPDATE_UI_ARGS = "ACTION_UPDATE_UI_ARGS";

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public void onCreate() {
		super.onCreate();

		mContext = getApplicationContext();

		HandlerThread thread = new HandlerThread(TAG,
				Process.THREAD_PRIORITY_BACKGROUND);
		thread.start();

		mServiceLooper = thread.getLooper();
		mServiceHandler = new ServiceHandler(mServiceLooper);
	}

	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);

		mShouldExit = false;

		int attempts = 10;

		// The previous server thread may still be cleaning up, wait for it
		// to finish.
		while (mServerThread != null) {
			Log.v(TAG, "Won't start, server thread exists");
			if (attempts > 0) {
				attempts--;
				FtpUtil.sleepIgnoreInterupt(1000);
			} else {
				Log.e(TAG, "FileScan Server thread already exists");
				return;
			}
		}

		Log.v(TAG, "Creating FileScan server thread");

		Message msg = mServiceHandler.obtainMessage();
		msg.what = startId;
		msg.obj = intent;
		msg.sendToTarget();
	}

	public void onDestroy() {
		Log.v(TAG, "onDestroy!!!");
		try {
			Thread.sleep(10);
		} catch (Exception e) {
		}

		mServiceLooper.quit();

		mShouldExit = true;
		if (mServerThread == null) {
			Log.e(TAG, "Stopping FileScanService with null serverThread");
		} else {
			mServerThread.interrupt();
			try {
				mServerThread.join(10000); // wait 10 sec for server thread to
											// finish
			} catch (InterruptedException e) {
			}
			if (mServerThread.isAlive()) {
				Log.e(TAG, "FileScan Server thread failed to exit");
				// it may still exit eventually if we just leave the
				// shouldExit flag set
			} else {
				Log.e(TAG, "FileScan serverThread join()ed ok");
				mServerThread = null;
			}
		}

		super.onDestroy();
	}

	private final class ServiceHandler extends Handler {
		public ServiceHandler(Looper looper) {
			super(looper);
		}

		public void handleMessage(Message msg) {
			Log.i(TAG, "handle message: " + msg);

			dispatchMsg(msg);
		}

		private void dispatchMsg(Message msg) {
			switch (msg.what) {
			default:
				Log.v(TAG, "dispatchMsg:create file scan thread!");
				mServerThread = new Thread(FileScanService.this);
				mServerThread.start();

				break;
			}
		}
	}

	public void run() {
		// TODO Auto-generated method stub
		Log.v(TAG, "running!");

		Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND
				+ Process.THREAD_PRIORITY_LESS_FAVORABLE);

		if (shouldStopScan()) {
			cleanupAndStopService();
			return;
		}

		while (!mShouldExit) {
			try {
				String state = Environment.getExternalStorageState();
				if (Environment.MEDIA_MOUNTED.equals(state)) {
					// now we can scan files
					Log.v(TAG, "Scanning files...");

					mApkFileList.clear();
					mCompressedFileList.clear();
					mDocFileList.clear();

					scanFiles(Environment.getExternalStorageDirectory());
					Log.v(TAG, "apk[" + mApkFileList.size() + "]com["
							+ mCompressedFileList.size() + "]doc["
							+ mDocFileList.size() + "]");

					updateCategoryDbs();

				} else {
					Thread.sleep(WAKE_INTERVAL_MS);
				}

				mShouldExit = true;
			} catch (InterruptedException e) {
				Log.e(TAG, "FileScan Thread interrupted");
			}
		}

		terminateAllSessions();

		mShouldExit = false;
		Log.v(TAG, "Exiting FileScanService cleanly, returning from run()");

		FileScanService.this.stopSelf();
	}

	private ContentValues createValues(String data, long size,
			long date_modified, int type) {
		ContentValues val = new ContentValues();
		val.put(FileConstants.CategoryList.DATA, data);
		val.put(FileConstants.CategoryList.SIZE, size);
		val.put(FileConstants.CategoryList.DATE_MODIFIED, date_modified);
		val.put(FileConstants.CategoryList.TYPE, type);

		return val;
	}

	private boolean shouldInsertItem(FileInfo fileInfo, int type) {
		String selection = FileConstants.CategoryList.DATA + "=?";
		Cursor cursor = mContext.getContentResolver().query(
				FileConstants.CategoryList.CONTENT_URI, null, selection,
				new String[] { fileInfo.filePath }, null);
		if (cursor != null && cursor.getCount() > 0) {
			cursor.close();
			return false;
		}

		if (cursor != null) {
			cursor.close();
		}

		return true;
	}

	private int getUpdateItemId(FileInfo fileInfo, int type) {
		String selection = FileConstants.CategoryList.DATA + "=?" + " AND "
				+ "(" + FileConstants.CategoryList.SIZE + "!=?" + " OR "
				+ FileConstants.CategoryList.DATE_MODIFIED + "!=?" + " OR "
				+ FileConstants.CategoryList.TYPE + "!=?" + ")";

		String selectionArgs[] = new String[] { fileInfo.filePath,
				Long.toString(fileInfo.fileSize),
				Long.toString(fileInfo.ModifiedDate), Integer.toString(type) };

		Cursor cursor = mContext.getContentResolver().query(
				FileConstants.CategoryList.CONTENT_URI, null, selection,
				selectionArgs, null);
		if (cursor != null && cursor.moveToFirst()) {
			int col = cursor.getColumnIndex(FileConstants.CategoryList._ID);
			int id = cursor.getInt(col);

			cursor.close();

			return id;
		}

		if (cursor != null) {
			cursor.close();
		}

		return -1;
	}

	private boolean insertDbItems(FileInfo fileInfo, int type) {
		// check whether we need insert this items
		int id = -1;
		try {
			if (shouldInsertItem(fileInfo, type)) {
				ContentValues values = createValues(fileInfo.filePath,
						fileInfo.fileSize, fileInfo.ModifiedDate, type);
				mContext.getContentResolver().insert(
						FileConstants.CategoryList.CONTENT_URI, values);
				return true;
			} else if ((id = getUpdateItemId(fileInfo, type)) > 0) {
				ContentValues values = createValues(fileInfo.filePath,
						fileInfo.fileSize, fileInfo.ModifiedDate, type);
				Uri uri = ContentUris.withAppendedId(
						FileConstants.CategoryList.CONTENT_URI, id);
				mContext.getContentResolver().update(uri, values, null, null);

				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	private boolean removeUselessItemsInDb() {
		ArrayList<Integer> ids = new ArrayList<Integer>();
		Cursor cursor = mContext.getContentResolver().query(
				FileConstants.CategoryList.CONTENT_URI, null, null, null, null);
		if (cursor != null && cursor.moveToFirst()) {
			do {
				int col = cursor
						.getColumnIndex(FileConstants.CategoryList.DATA);
				String filePath = cursor.getString(col);
				File file = new File(filePath);
				if (file != null && !file.exists()) {
					int idCol = cursor
							.getColumnIndex(FileConstants.CategoryList._ID);
					int id = cursor.getInt(idCol);

					ids.add(id);
				}
			} while (cursor.moveToNext());

			for (int i = 0; i < ids.size(); i++) {
				int id = ids.get(i);
				Uri uri = ContentUris.withAppendedId(
						FileConstants.CategoryList.CONTENT_URI, id);
				mContext.getContentResolver().delete(uri, null, null);
			}
		}

		if (cursor != null) {
			cursor.close();
		}

		return (ids.size() > 0);
	};

	private void updateCategoryDbs() {
		// TODO Auto-generated method stub

		boolean inserted = false;

		// reset apks?
		if (mApkFileList.size() > 0) {
			for (Iterator<FileInfo> iterator = mApkFileList.iterator(); iterator
					.hasNext();) {
				FileInfo fileInfo = (FileInfo) iterator.next();
				boolean ret = insertDbItems(fileInfo,
						FileConstants.CategoryList.CATEGORY_APK);
				if (ret) {
					inserted = true;
				}
			}
		}

		if (mCompressedFileList.size() > 0) {
			for (Iterator<FileInfo> iterator = mCompressedFileList.iterator(); iterator
					.hasNext();) {
				FileInfo fileInfo = (FileInfo) iterator.next();
				boolean ret = insertDbItems(fileInfo,
						FileConstants.CategoryList.CATEGORY_ZIP);
				if (ret) {
					inserted = true;
				}
			}
		}

		if (mDocFileList.size() > 0) {
			for (Iterator<FileInfo> iterator = mDocFileList.iterator(); iterator
					.hasNext();) {
				FileInfo fileInfo = (FileInfo) iterator.next();
				boolean ret = insertDbItems(fileInfo,
						FileConstants.CategoryList.CATEGORY_DOC);
				if (ret) {
					inserted = true;
				}
			}
		}

		// now remove all useless items in db
		boolean removed = removeUselessItemsInDb();

		// send Broadcast intent to update ui
		Intent intent = new Intent(ACTION_UPDATE_UI);
		if (inserted || removed) {
			Log.v(TAG, "send " + ACTION_UPDATE_UI + " event to update ui!");
			intent.putExtra(ACTION_UPDATE_UI_ARGS, true);
		} else {
			intent.putExtra(ACTION_UPDATE_UI_ARGS, false);
		}

		sendBroadcast(intent);
	}

	private boolean shouldStopScan() {
		return false;
	}

	/*
	 * return true if and only if a server Thread is running
	 */
	public static boolean isRunning() {
		if (mServerThread == null) {
			Log.e(TAG, "FileScan Server is not running (null serverThread)");
			return false;
		}

		if (!mServerThread.isAlive()) {
			Log.v(TAG, "FileScan serverThread non-null but !isAlive()");
		} else {
			Log.v(TAG, "FileScan Server is alive");
		}
		return true;
	}

	/*
	 * Call the Android Service shutdown function
	 */
	public void cleanupAndStopService() {
		Context context = getApplicationContext();
		Intent intent = new Intent(context, FileScanService.class);
		context.stopService(intent);
	}

	private void terminateAllSessions() {
		Log.v(TAG, "terminateAllSessions");
	}

	public void stopScan() {
		mShouldExit = true;
	}

	public void startScan(FileCategory category) {
		switch (category) {
		case Theme:
			break;
		case Doc:
			break;
		case Zip:
			break;
		case Apk:
			break;
		case Other:
			break;
		}

		if (mServerThread == null) {

		}
	}

	public void scanFiles(File file) {
		if (file == null || !file.exists()) {
			return;
		}

		File[] files = file.listFiles();
		if (files == null) {
			return;
		}

		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				scanFiles(files[i]);
			} else if (files[i].isFile()) {
				FileInfo fileInfo = Util
						.getFileInfo(files[i].getAbsolutePath());
				if (Util.isApkFile(files[i].getName())) {
					mApkFileList.add(fileInfo);
				} else if (Util.isDocFile(files[i].getName())) {
					mDocFileList.add(fileInfo);
				} else if (Util.isZipFile(files[i].getName())) {
					mCompressedFileList.add(fileInfo);
				}
			}
		}
	}
}
