package com.mars.miuifilemanager.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.StatFs;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.mars.miuifilemanager.FileExplorerTabActivity;
import com.mars.miuifilemanager.adapter.FavoriteItem;
import com.mars.miuifilemanager.adapter.FileInfo;
import com.mars.miuifilemanager.ui.FileViewActivity.OnBackPressedListener;

public class Util {
	private static final String TAG = "Util";

	private static String ANDROID_SECURE = "/mnt/sdcard/.android_secure";

	private static final int COPY_BUFFER_SIZE = 32 * 1024;

	public static class SDCardInfo {
		public long free;
		public long total;

		public SDCardInfo() {

		}
	}

	public Util() {

	}

	public static String makePath(String path, String pathName) {
		String sep = File.separator;
		String realPath = null;
		if (path.endsWith(sep)) {
			realPath = path + pathName;
		} else {
			realPath = path + sep + pathName;
		}

		return realPath;
	}

	public static boolean isNormalFile(String fileName) {
		boolean flag = false;

		if (!fileName.equals(ANDROID_SECURE)) {
			flag = true;
		}

		return flag;
	}

	public static FileInfo getFileInfo(File file, FilenameFilter filter,
			boolean flag) {
		FileInfo fileInfo = new FileInfo();

		fileInfo.canRead = file.canRead();
		fileInfo.canWrite = file.canWrite();
		fileInfo.isHidden = file.isHidden();
		fileInfo.fileName = file.getName();
		fileInfo.ModifiedDate = file.lastModified();
		fileInfo.IsDir = file.isDirectory();
		fileInfo.filePath = file.getPath();

		if (!fileInfo.IsDir) {
			fileInfo.fileSize = file.length();
		} else {
			int count = 0;
			File[] files = file.listFiles(filter);
			if (files != null) {
				for (int k = 0; k < files.length; k++) {
					File f = files[k];
					if ((!f.isHidden() || flag)
							&& isNormalFile(f.getAbsolutePath())) {
						count++;
					}
				}

				fileInfo.Count = count;
			} else {
				return null;
			}
		}

		return fileInfo;
	}

	public static FileInfo getFileInfo(String s) {
		File file = new File(s);
		FileInfo fileInfo = null;
		if (file.exists()) {
			fileInfo = new FileInfo();
			fileInfo.canRead = file.canRead();
			fileInfo.canWrite = file.canWrite();
			fileInfo.isHidden = file.isHidden();
			fileInfo.fileName = getNameFromFilepath(s);
			fileInfo.ModifiedDate = file.lastModified();
			fileInfo.IsDir = file.isDirectory();
			fileInfo.filePath = s;
			fileInfo.fileSize = file.length();
			fileInfo.dbId = -1;
		}

		return fileInfo;
	}

	public static boolean containsPath(String path, String subPath) {
		String s = subPath;

		do {
			if (s == null) {
				return false;
			}

			if (s.equalsIgnoreCase(path)) {
				return true;
			}

			if (s.equals("/")) {
				return false;
			}

			s = (new File(s)).getParent();
		} while (s != null);

		return false;
	}

	public static String convertStorage(long size) {
		long KB = 1024L * 1024L;
		long KM = KB * 1024L;

		String sizeStr = null;
		if (size > KM) {
			Float length = Float.valueOf((float) size / (float) KM);

			sizeStr = String.format("%.1f GB", length);
		} else if (size > KB) {
			String format = null;
			float sizeF = (float) size / (float) KB;
			if (sizeF > 100F) {
				format = "%.0f MB";
			} else {
				format = "%.1f MB";
			}

			sizeStr = String.format(format, sizeF);
		} else if (size > 1024L) {
			String format = null;
			float sizeF = (float) size / 1024L;

			if (sizeF > 100F) {
				format = "%.0f KB";
			} else {
				format = "%.1f KB";
			}

			sizeStr = String.format(format, sizeF);
		} else {
			sizeStr = String.format("%d B", Long.valueOf(size));
		}

		return sizeStr;
	}

	/**
	 * To do
	 * 
	 * @param src
	 * @param dest
	 * @return
	 */
	public static String copyFile(String src, String dest) {
		File srcFile = new File(src);
		if (!srcFile.exists() || srcFile.isDirectory()) {
			Log.v(TAG, "copyFile: file not exist or is directory," + src);
			return null;
		}
		File dFile = new File(dest);

		if (!dFile.exists() && !dFile.mkdirs()) {
			return null;
		}

		String srcFileName = srcFile.getName();
		String newFileName = makePath(dest, srcFileName);
		File destFile = new File(newFileName);
		for (int i = 1; destFile.exists(); i++) {
			String fileName = getNameFromFilename(srcFile.getName());
			String fileExt = getExtFromFilename(srcFile.getName());

			newFileName = makePath(dest, fileName + " " + i + "." + fileExt);

			destFile = new File(newFileName);
		}

		try {
			if (!destFile.createNewFile()) {
				Log.v(TAG, "failed to copy file[" + destFile.getAbsolutePath()
						+ "]");
				return null;
			}

			FileInputStream input = new FileInputStream(srcFile);
			FileOutputStream output = new FileOutputStream(destFile);

			byte[] buffer = new byte[COPY_BUFFER_SIZE];
			while (true) {
				int bytes = input.read(buffer);
				if (bytes <= 0) {
					break;
				}

				output.write(buffer, 0, bytes);
			}

			output.close();
			input.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static String formatDateString(Context context, long date) {
		java.text.DateFormat dateformat = DateFormat.getDateFormat(context);
		java.text.DateFormat dateformat1 = DateFormat.getTimeFormat(context);
		Date d = new Date(date);
		StringBuilder stringbuilder = new StringBuilder();
		String s = dateformat.format(d);
		StringBuilder stringbuilder1 = stringbuilder.append(s).append(" ");
		String s1 = dateformat1.format(d);
		return stringbuilder1.append(s1).toString();
	}

	public static Drawable getApkIcon(Context context, String Path) {
		String apkPath = Path;
		String PATH_PackageParser = "android.content.pm.PackageParser";
		String PATH_AssetManager = "android.content.res.AssetManager";
		
		try {
			Class<?> pkgParserCls = Class.forName(PATH_PackageParser);
			Class<?>[] typeArgs = { String.class };
			Constructor<?> pkgParserCt = pkgParserCls.getConstructor(typeArgs);
			Object[] valueArgs = { apkPath };
			Object pkgParser = pkgParserCt.newInstance(valueArgs);
			DisplayMetrics metrics = new DisplayMetrics();
			metrics.setToDefaults();
			typeArgs = new Class<?>[] { File.class, String.class,
			DisplayMetrics.class, int.class };

			Method pkgParser_parsePackageMtd = pkgParserCls.getDeclaredMethod("parsePackage", typeArgs);
			valueArgs = new Object[] { new File(apkPath), apkPath, metrics, 0 };
			Object pkgParserPkg = pkgParser_parsePackageMtd.invoke(pkgParser, valueArgs);

			Field appInfoFld = pkgParserPkg.getClass().getDeclaredField("applicationInfo");

			ApplicationInfo info = (ApplicationInfo) appInfoFld.get(pkgParserPkg);
			Class<?> assetMagCls = Class.forName(PATH_AssetManager);
			Object assetMag = assetMagCls.newInstance();
			typeArgs = new Class[1];
			typeArgs[0] = String.class;
			
			Method assetMag_addAssetPathMtd = assetMagCls.getDeclaredMethod("addAssetPath", typeArgs);
			valueArgs = new Object[1];
			valueArgs[0] = apkPath;
			assetMag_addAssetPathMtd.invoke(assetMag, valueArgs);
			Resources res = context.getResources();
			typeArgs = new Class[3];
			typeArgs[0] = assetMag.getClass();
			typeArgs[1] = res.getDisplayMetrics().getClass();
			typeArgs[2] = res.getConfiguration().getClass();
			Constructor<Resources> resCt = Resources.class.getConstructor(typeArgs);
			valueArgs = new Object[3];
			valueArgs[0] = assetMag;
			valueArgs[1] = res.getDisplayMetrics();
			valueArgs[2] = res.getConfiguration();

			res = (Resources) resCt.newInstance(valueArgs);
			if (info != null) {
				if (info.icon != 0) {
					Drawable icon = res.getDrawable(info.icon);
					return icon;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/*
	public static Drawable getApkIcon(Context context, String packageName) {
		PackageManager pkgManager = context.getPackageManager();
		PackageInfo packInfo = null;
		try {
			packInfo = pkgManager.getPackageArchiveInfo(packageName,
					PackageManager.GET_ACTIVITIES);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (packInfo == null) {
			return null;
		}
		// ApplicationInfo applicationinfo = packInfo.applicationInfo;
		// return context.getResources().getDrawable(applicationinfo.icon);
		Drawable d = pkgManager.getApplicationIcon(packInfo.applicationInfo);
		return d;
		// return pkgManager.getApplicationIcon(packInfo.applicationInfo);
		// return applicationinfo.loadIcon(pkgManager);
	}
	*/
	
	public static ArrayList<FavoriteItem> getDefaultFavorites() {
		ArrayList<FavoriteItem> arrayList = new ArrayList<FavoriteItem>();

		String cameraPath = makePath(getSdDirectory(), "DCIM/Camera");
		FavoriteItem camItem = new FavoriteItem("Camera", cameraPath);
		arrayList.add(camItem);

		FavoriteItem sdItem = new FavoriteItem("SD card", getSdDirectory());
		arrayList.add(sdItem);

		// To do

		return arrayList;
	}

	public static String getExtFromFilename(String fileName) {
		String ext;
		int idx = fileName.lastIndexOf('.');
		if (idx != -1) {
			ext = fileName.substring(idx + 1, fileName.length());
		} else {
			ext = "";
		}

		return ext;
	}

	public static String getNameFromFilename(String fileName) {
		String name;
		int idx = fileName.lastIndexOf('.');
		if (idx != -1) {
			name = fileName.substring(0, idx);
		} else {
			name = "";
		}

		return name;
	}

	public static String getSdDirectory() {
		return Environment.getExternalStorageDirectory().getPath();
	}

	public static String getNameFromFilepath(String path) {
		int i = path.lastIndexOf('/');
		String name;
		if (i != -1) {
			int j = i + 1;
			name = path.substring(j);
		} else {
			name = "";
		}

		return name;
	}

	public static String getPathFromFilepath(String filePath) {
		String path;
		int i = filePath.lastIndexOf('/');
		if (i != -1) {
			path = filePath.substring(0, i);
		} else {
			path = "";
		}

		return path;
	}

	public static SDCardInfo getSDCardInfo() {
		if (!Environment.getExternalStorageState().equals("mounted")) {
			return null;
		}

		File dir = Environment.getExternalStorageDirectory();
		SDCardInfo info = new SDCardInfo();
		StatFs fs = new StatFs(dir.getPath());

		long blockCount = fs.getBlockCount();
		long blockSize = fs.getBlockSize();
		long blocks = fs.getAvailableBlocks();

		info.total = blockCount * blockSize;
		info.free = blocks * blockSize;

		return info;
	}

	public static boolean isSDCardReady() {
		return Environment.getExternalStorageState().equals("mounted");
	}

	public static boolean setText(View view, int resId, String content) {
		TextView textView = (TextView) view.findViewById(resId);
		if (textView == null) {
			return false;
		}

		textView.setText(content);

		return true;
	}

	/**
	 * 
	 * @param file
	 * @return
	 */
	public static boolean shouldShowFile(File file) {
		if (Settings.instance().getShowDotAndHiddenFiles()) {
			return true;
		}

		if (file.isHidden()) {
			return false;
		}

		if (file.getName().startsWith(".")) {
			return false;
		}

		return true;
	}

	public static boolean shouldShowFile(String filePath) {
		return shouldShowFile(new File(filePath));
	}

	public static void showNotification(Context context, Intent intent,
			String content, int flag) {
		// To do
	}

	public static void showTab(int i) {
		FileExplorerTabActivity fileexplorertabactivity = (FileExplorerTabActivity) ActivitiesManager
				.getInstance().getActivity("FileExplorerTab");
		if (fileexplorertabactivity != null) {
			fileexplorertabactivity.setCurrentTab(i);
		}
	}

	public static FileInfo GetFileInfo(File file,
			FilenameFilter filenamefilter, boolean flag) {
		FileInfo fileInfo = new FileInfo();

		File f = new File(file.getPath());

		fileInfo.canRead = f.canRead();
		fileInfo.canWrite = f.canWrite();
		fileInfo.isHidden = f.isHidden();
		fileInfo.fileName = f.getName();
		fileInfo.ModifiedDate = f.lastModified();
		fileInfo.IsDir = f.isDirectory();
		fileInfo.filePath = f.getPath();
		if (!fileInfo.IsDir) {
			fileInfo.fileSize = f.length();
		} else {
			File files[] = f.listFiles(filenamefilter);
			if (files == null) {
				return null;
			}

			int count = 0;
			for (int i = 0; i < files.length; i++) {
				File ff = files[i];
				if ((!ff.isHidden() || flag)
						&& isNormalFile(ff.getAbsolutePath())) {
					count++;
				}
			}

			fileInfo.Count = count;
		}

		fileInfo.fileSize = f.length();

		return fileInfo;
	}

	private static String mCurrentFavDirPath;
	private static OnBackPressedListener mListener;

	public static void saveFavoriteDirPath(String path,
			OnBackPressedListener onbackpressedlistener) {
		mCurrentFavDirPath = path;
		mListener = onbackpressedlistener;
	}

	public static String getSavedFavoriteDirPath() {
		return mCurrentFavDirPath;
	}

	public static OnBackPressedListener getSavedFavoriteDirListener() {
		return mListener;
	}

	public static void resetFavoriteDirPath() {
		mCurrentFavDirPath = null;
		mListener = null;
	}

	private static ArrayList<FileInfo> mCopyMoveArraylist;

	public static void setCopyMoveArrayList(ArrayList<FileInfo> arrayList) {
		mCopyMoveArraylist = (ArrayList<FileInfo>) arrayList.clone();
	}

	public static ArrayList<FileInfo> getSavedCopyMoveArraylist() {
		return mCopyMoveArraylist;
	}

	public static void clearSavedCopyArraylist() {
		if (mCopyMoveArraylist != null) {
			mCopyMoveArraylist.clear();
			mCopyMoveArraylist = null;
		}

		setCopyMoveFlag(ACTION_NONE);
	}

	public static final int ACTION_NONE = 0;
	public static final int ACTION_COPY = 100;
	public static final int ACTION_MOVE = 101;

	private static int mCopyMoveFlag = ACTION_NONE;

	public static void setCopyMoveFlag(int flag) {
		mCopyMoveFlag = flag;
	}

	public static int getCopyMoveFlag() {
		return mCopyMoveFlag;
	}
	
	public static boolean isApkFile(final String filePath) {
		if (filePath == null) {
			return false;
		}
		
		MediaFile.MediaFileType mimeFileType = MediaFile
				.getFileType(filePath);
		if (mimeFileType == null) {
			return false;
		}

		return MediaFile.isApkFileType(mimeFileType.fileType);
	}
	
	public static boolean isDocFile(final String filePath) {
		if (filePath == null) {
			return false;
		}
		
		MediaFile.MediaFileType mimeFileType = MediaFile
				.getFileType(filePath);
		if (mimeFileType == null) {
			return false;
		}

		return MediaFile.isDocFileType(mimeFileType.fileType);
	}
	
	public static boolean isZipFile(final String filePath) {
		if (filePath == null) {
			return false;
		}
		
		MediaFile.MediaFileType mimeFileType = MediaFile
				.getFileType(filePath);
		if (mimeFileType == null) {
			return false;
		}

		return MediaFile.isZipFileType(mimeFileType.fileType);
	}
	
	public static String getFileContents(InputStream in, String codec) {
		return getFileContents(in,codec,"\n");
	}
	
	public static String getFileContents(InputStream in, String codec, String newLine) {
		if (in == null) {
			return "";
		}
		
		StringBuilder text = new StringBuilder();
		InputStreamReader isReader = null;
		try {
			isReader = new InputStreamReader(in, codec);
		}catch (UnsupportedEncodingException e) {
			Log.e(TAG, "failed to read content:" + codec);
			
			try {
				isReader = new InputStreamReader(in, "UTF-8");
			}catch (Exception ex) {
				Log.e(TAG, "failed to read content:" + "UTF-8");
			}
		}
		
		try {
			BufferedReader br = new BufferedReader(isReader);
			
			String line = br.readLine();
			do {
				if (line != null) {
					text.append(line);
					text.append(newLine);
				}
			} while((line = br.readLine())!=null);
			
			in.close();
			in = null;
			
			br.close();
			br = null;
			
			isReader.close();
			isReader = null;
			
			return text.toString();
		}catch(Exception e) {
			Log.e(TAG, "getFileContents exception:"+e);
		} finally {
			try {
				if (in != null) {
					in.close();
				}
				if (isReader != null) {
					isReader.close();
				}
			}catch(Exception e) {
			}
		}
		
		return "";
	}
}
