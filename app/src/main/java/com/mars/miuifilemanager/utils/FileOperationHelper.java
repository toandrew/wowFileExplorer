package com.mars.miuifilemanager.utils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Iterator;

import android.os.AsyncTask;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.mars.miuifilemanager.adapter.FileInfo;

public class FileOperationHelper {
	private static final String TAG = "FileOperationHelper";
	
	private ArrayList<FileInfo> mCurFileNameList;
	private IOperationProgressListener mOperationListener;
	private FilenameFilter mFilter;
	
	private boolean mMoving;
	
	public static interface IOperationProgressListener {
		public abstract void onFileChanged(String s);
		public abstract void onFinish();
		public abstract void onDelete(FileInfo fileInfo);
	}
	
	public FileOperationHelper(IOperationProgressListener listener) {
		mCurFileNameList = new ArrayList<FileInfo>();
		mOperationListener = listener;
	}
	
	public void Copy(ArrayList<FileInfo> arraylist) {
		copyFileList(arraylist);
	}
	
	public boolean createFolder(String path, String pathName) {
		String realPath = Util.makePath(path, pathName);
		
		boolean flag = false;
		File file = new File(realPath);
		if (file.exists()) {
			flag = true;
		} else {
			flag = file.mkdir();
		}
		
		return flag;
	}
	
	public boolean Delete(ArrayList<FileInfo> arraylist) {
		copyFileList(arraylist);
		
		asnycExecute(new Runnable(){
			public void run() {
				// TODO Auto-generated method stub
				for (Iterator<FileInfo> iterator = mCurFileNameList.iterator(); iterator.hasNext(); ) {
					FileInfo fileInfo = (FileInfo)iterator.next();
					DeleteFile(fileInfo);
				}
				
				String s = Environment.getExternalStorageDirectory().getAbsolutePath();
				mOperationListener.onFileChanged(s);
				clear();
			}
		});
		
		return true;
	}
	
	class MyAsnycTask extends AsyncTask<Integer, Integer, Integer> {
		Runnable _r;
		
		public void start(final Runnable _r ) {
			this._r = _r;
			execute();
		}
		
		protected Integer doInBackground(Integer... params) {
			// TODO Auto-generated method stub
			Log.v(TAG, "doInBackground!!!");
			synchronized(mCurFileNameList) {
				_r.run();
			}
			
			Log.v(TAG, "doInBackground!!!ready to listener!!!");
			
			if (mOperationListener != null) {
				Log.v(TAG, "doInBackground!!!ready to listener!!onFinish");
				mOperationListener.onFinish();
			}
			
			return null;
		}
	};
	
	private void asnycExecute(Runnable r) {
		MyAsnycTask task = new MyAsnycTask();
		task.start(r);
	}
	
	private void copyFileList(ArrayList<FileInfo> arraylist) {
		synchronized(mCurFileNameList) {
			mCurFileNameList.clear();
			for (Iterator<FileInfo> iterator= arraylist.iterator(); iterator.hasNext(); ) {
				FileInfo fileInfo = (FileInfo)iterator.next();
				mCurFileNameList.add(fileInfo);
			}
		}
	}
	
	protected void DeleteFile(FileInfo fileinfo) {
		if ( fileinfo == null) {
			Log.v(TAG,"DeleteFile: null parameter");
			return;
		}
		
		String s = fileinfo.filePath;
		File file = new File(s);
		
		if (file.isDirectory()) {
			File[] files = file.listFiles(mFilter);
			
			for (int k = 0; k < files.length; k++) {
				File f = files[k];
				if (Util.isNormalFile(f.getAbsolutePath())) {
					FileInfo fileInfo = Util.getFileInfo(f, mFilter, true);
					DeleteFile(fileInfo);
				}
			}
		}
		
		file.delete();
		
		mOperationListener.onDelete(fileinfo);
	}
	
	private void copyFile(FileInfo fileInfo, String path) {
		if (fileInfo == null || path == null) {
			Log.e(TAG,"copyFile: null parameter");
			return;
		}

		File file = new File(fileInfo.filePath);
		if (file.isDirectory()) {
			String dirPath = Util.makePath(path, fileInfo.fileName);
			File dir = new File(dirPath);
			
			for (int i = 1; dir.exists(); i++) {
				String seq = i + "";
				dirPath = Util.makePath(path, fileInfo.fileName + " " + seq);
				dir = new File(dirPath);
			}
			
			File files[] = file.listFiles(mFilter);
			for (int i = 0; i < files.length; i++) {
				File f = files[i];
				if (!f.isHidden() && Util.isNormalFile(f.getAbsolutePath())) {
					boolean flag = Settings.instance().getShowDotAndHiddenFiles();
					FileInfo fileinfo1 = Util.GetFileInfo(f, mFilter, flag);
					copyFile(fileinfo1, dirPath);
				}
			}
		} else {
			Util.copyFile(fileInfo.filePath, path);
		}

		Log.v(TAG,"CopyFile >>>" + fileInfo.filePath + "," + path);
	}
	
	private boolean moveFile(FileInfo fileInfo, String path) {
		Log.v(TAG, "MoveFile >>> " + fileInfo.filePath + "," + path);
		
		if (fileInfo == null || path == null) {
			Log.e(TAG,"moveFile: null parameter");
			return false;
		}
		
		File file = new File(fileInfo.filePath);
		String destPath = Util.makePath(path, fileInfo.fileName); 
		File destFile = new File(destPath);
		
		try{
			file.renameTo(destFile);
		} catch (Exception e) {
			Log.e(TAG, "Failed to move file," + e.toString());
		}
		
		return true;
	}
	
	public boolean isMoveState() {
		return mMoving;
	}
	
	public boolean canMove(String path) {
		boolean ret = true;
		
		for (Iterator<FileInfo> iterator = mCurFileNameList.iterator();iterator.hasNext();) {
			FileInfo fileInfo = (FileInfo)iterator.next();
			if (!fileInfo.IsDir || !Util.containsPath(fileInfo.filePath, path)) {
				continue;
			}
			
			ret = false;
			
			break;
		}
		
		return ret;
	}
	
	public void startMove(ArrayList<FileInfo> arraylist) {
		if (mMoving) {
			return;
		} else {
			mMoving = true;
			copyFileList(arraylist);
			return;
		}
	}

	public boolean endMove(String path) {
		if (!mMoving) {
			return false;
		}
		
		mMoving = false;
		
		if(TextUtils.isEmpty(path)) {
			return false;
		}
		
		final String _path = path;
		
		Runnable r = new Runnable() {
			public void run() {
				// TODO Auto-generated method stub
				for (Iterator<FileInfo> iterator = mCurFileNameList.iterator(); iterator.hasNext(); ) {
					FileInfo fileInfo = (FileInfo)iterator.next();
					moveFile(fileInfo, _path);
				}
				
				String absPath = Environment.getExternalStorageDirectory().getAbsolutePath();
				mOperationListener.onFileChanged(absPath);
				clear();
			}
		};
		
		asnycExecute(r);
		
		return true;
	}
	
	public void clear() {
		synchronized (mCurFileNameList) {
			mCurFileNameList.clear();
		}
	}
	
	public boolean canPaste() {
		if (mCurFileNameList.size() > 0) {
			return true;
		}
		
		return false;
	}
	
	public boolean Paste(final String path) {
		if (mCurFileNameList.size() == 0) {
			return false;
		}
		
		asnycExecute(new Runnable(){
			public void run() {
				Log.v(TAG, "in Run!!!!");
				
				// TODO Auto-generated method stub
				for (Iterator<FileInfo> iterator = mCurFileNameList.iterator(); iterator.hasNext(); ) {
					FileInfo fileInfo = (FileInfo)iterator.next();
					copyFile(fileInfo, path);
				}
				
				String absPath = Environment.getExternalStorageDirectory().getAbsolutePath();
				mOperationListener.onFileChanged(absPath);
				clear();	
				
				Log.v(TAG, "after Run!!!!");
			}
		});
		
		return true;
	}
	
	public boolean Rename(FileInfo fileInfo, String fileName) {
		boolean ret = false;
		
		if (fileInfo == null || fileName == null) {
			Log.v(TAG, "Rename: null parameter");
			return false;
		}
		
		File file = new File(fileInfo.filePath);
		String destPath = Util.makePath(Util.getPathFromFilepath(fileInfo.filePath), fileName);
		try {
			boolean isFile = file.isFile();
			
			File destFile = new File(destPath);
			boolean ok = file.renameTo(destFile);
			if (ok) {
				if (isFile) {
					mOperationListener.onFileChanged(fileInfo.filePath);
				}
				
				mOperationListener.onFileChanged(destPath);
			}
			
			ret = ok;
		} catch (Exception e){
			Log.e(TAG, "Failed to rename file," + e.toString());
			
			return false;
		}
		
		return ret;
	}
	
	public boolean isFileSelected(String filePath) {
		for (Iterator<FileInfo> iterator = mCurFileNameList.iterator(); iterator.hasNext();) {
			FileInfo fileInfo = (FileInfo)(FileInfo)iterator.next();
			if (fileInfo.filePath.equalsIgnoreCase(filePath)) {
				return true;
			}
		}
		
		return false;
	}
}
