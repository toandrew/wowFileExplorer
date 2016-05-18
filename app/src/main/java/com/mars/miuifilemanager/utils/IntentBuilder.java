package com.mars.miuifilemanager.utils;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import com.mars.miuifilemanager.adapter.FileInfo;
import com.mars.miuifilemanager.R;

public class IntentBuilder {
	private static final String TAG = "IntentBuilder";

	public static final int FILE_TYPE_UNKNOWN = 100;

	public IntentBuilder() {
	}

	public static Intent buildSendFile(ArrayList<FileInfo> arraylist) {
		ArrayList<Uri> sendUris = new ArrayList<Uri>();

		String type = "*/*";

		for (Iterator<FileInfo> iterator = arraylist.iterator(); iterator
				.hasNext();) {
			FileInfo fileInfo = (FileInfo) iterator.next();
			if (!fileInfo.IsDir) {
				File file = new File(fileInfo.filePath);

				try {
					type = getMimeType(fileInfo.fileName);
				} catch (Exception e) {
					Log.e(TAG, "failed to find mime type![" + fileInfo.fileName
							+ "]e[" + e.toString() + "] try another method...");

					MediaFile.MediaFileType mimeFileType = MediaFile
							.getFileType(fileInfo.fileName);
					if (mimeFileType != null) {
						type = mimeFileType.mimeType;
					}
				}

				// send apk by using "zip" format
				if (fileInfo.fileName.endsWith(".apk")) {
					type = "application/zip";
				}
				
				Uri uri = Uri.fromFile(file);
				sendUris.add(uri);
			}
		}

		if (sendUris.size() == 0) {
			return null;
		}

		String action = Intent.ACTION_SEND;
		if (sendUris.size() > 1) {
			action = Intent.ACTION_SEND_MULTIPLE;
		}

		Intent intent = new Intent(action);
		if (sendUris.size() > 1) {
			intent.setType("*/*");
			intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, sendUris);
		} else {
			intent.putExtra("subject", arraylist.get(0).fileName);
			intent.putExtra("body", "Send file!");
			intent.setType(type);
			Parcelable data = (Parcelable) sendUris.get(0);
			intent.putExtra(Intent.EXTRA_STREAM, data);
		}

		return intent;
	}

	public static void viewFile(final Context context, final String filePath) {
		Log.v(TAG, "viewFile[" + filePath);
		String mimeType = "*/*";

		try {
			mimeType = getMimeType(filePath);
		} catch (Exception e) {
			Log.e(TAG,
					"failed to find mime type![" + filePath + "]e["
							+ e.toString() + "] try another method...");

			MediaFile.MediaFileType mimeFileType = MediaFile
					.getFileType(filePath);
			if (mimeFileType != null) {
				mimeType = mimeFileType.mimeType;
			}
		}

		Log.v(TAG, "mimeType[" + mimeType + "]");
		if (filePath.endsWith(".apk")) {
			mimeType = "application/vnd.android.package-archive";
			Log.v(TAG, "change mimeType to [" + mimeType + "]");
		}
		
		if (!TextUtils.isEmpty(mimeType) && !TextUtils.equals(mimeType, "*/*")) {
			Intent intent = new Intent();
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setAction(Intent.ACTION_VIEW);
			Uri uri = Uri.fromFile(new File(filePath));
			intent.setDataAndType(uri, mimeType);
			context.startActivity(intent);
		} else {
			AlertDialog.Builder builder = new android.app.AlertDialog.Builder(
					context);
			builder.setTitle(R.string.dialog_select_type);

			CharSequence[] items = new CharSequence[4];
			items[0] = context.getString(R.string.dialog_type_text);
			items[1] = context.getString(R.string.dialog_type_audio);
			items[2] = context.getString(R.string.dialog_type_video);
			items[3] = context.getString(R.string.dialog_type_image);

			builder.setItems(items, new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub

					String mimeType = "*/*";
					switch (which) {
					case 0:
						mimeType = "text/plain";
						break;
					case 1:
						mimeType = "audio/*";
						break;
					case 2:
						mimeType = "video/*";
						break;
					case 3:
						mimeType = "image/*";
						break;
					}

					Intent intent = new Intent();
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intent.setAction(Intent.ACTION_VIEW);
					Uri uri = Uri.fromFile(new File(filePath));
					intent.setDataAndType(uri, mimeType);
					context.startActivity(intent);
				}
			});

			// builder.setContextMenuMode(true);
			builder.show();
		}
	}

	public static boolean isAudioFileType(final String filePath)
			throws ClassNotFoundException, IllegalAccessException,
			InstantiationException, SecurityException, NoSuchMethodException,
			IllegalArgumentException, InvocationTargetException,
			NoSuchFieldException {

		Class<?> classType = Class.forName("android.media.MediaFile");
		Class<?> classMediaFileType = Class
				.forName("android.media.MediaFile$MediaFileType");
		Object invokeOperation = classType.newInstance();

		Method getFileTypeMethod = classType.getMethod("getFileType",
				String.class);
		Object ret = getFileTypeMethod.invoke(invokeOperation, new String(
				filePath));

		if (ret == null) {
			return false;
		}

		Method isAudioFileTypeMethod = classType.getMethod("isAudioFileType",
				int.class);

		Field[] f = classMediaFileType.getDeclaredFields();

		for (int i = 0; i < f.length; i++) {
			makeAccessible(f[i]);

			Log.v(TAG, "media type[" + f[i].get(ret) + "]");
			if (f[i].get(ret) instanceof Integer) {
				Integer type = (Integer) f[i].get(ret);

				return (Boolean) isAudioFileTypeMethod.invoke(invokeOperation,
						new Integer(type));
			}
		}

		return false;
	}

	public static boolean isVideoFileType(final String filePath)
			throws ClassNotFoundException, IllegalAccessException,
			InstantiationException, SecurityException, NoSuchMethodException,
			IllegalArgumentException, InvocationTargetException,
			NoSuchFieldException {

		Class<?> classType = Class.forName("android.media.MediaFile");
		Class<?> classMediaFileType = Class
				.forName("android.media.MediaFile$MediaFileType");
		Object invokeOperation = classType.newInstance();

		Method getFileTypeMethod = classType.getMethod("getFileType",
				String.class);
		Object ret = getFileTypeMethod.invoke(invokeOperation, new String(
				filePath));

		if (ret == null) {
			return false;
		}

		Method isVideoFileTypeMethod = classType.getMethod("isVideoFileType",
				int.class);

		Field[] f = classMediaFileType.getDeclaredFields();

		for (int i = 0; i < f.length; i++) {
			makeAccessible(f[i]);
			Log.v(TAG, "media type[" + f[i].get(ret) + "]");
			if (f[i].get(ret) instanceof Integer) {
				Integer type = (Integer) f[i].get(ret);

				return (Boolean) isVideoFileTypeMethod.invoke(invokeOperation,
						new Integer(type));
			}
		}

		return false;
	}

	public static boolean isImageFileType(final String filePath)
			throws ClassNotFoundException, IllegalAccessException,
			InstantiationException, SecurityException, NoSuchMethodException,
			IllegalArgumentException, InvocationTargetException,
			NoSuchFieldException {

		Class<?> classType = Class.forName("android.media.MediaFile");
		Class<?> classMediaFileType = Class
				.forName("android.media.MediaFile$MediaFileType");
		Object invokeOperation = classType.newInstance();

		Method getFileTypeMethod = classType.getMethod("getFileType",
				String.class);
		Object ret = getFileTypeMethod.invoke(invokeOperation, new String(
				filePath));

		if (ret == null) {
			return false;
		}

		Method isImageFileTypeMethod = classType.getMethod("isImageFileType",
				int.class);

		Field[] f = classMediaFileType.getDeclaredFields();

		for (int i = 0; i < f.length; i++) {
			makeAccessible(f[i]);
			Log.v(TAG, "media type[" + f[i].get(ret) + "]");
			if (f[i].get(ret) instanceof Integer) {
				Integer type = (Integer) f[i].get(ret);

				return (Boolean) isImageFileTypeMethod.invoke(invokeOperation,
						new Integer(type));
			}
		}

		return false;
	}

	private static void makeAccessible(final Field field) {
		if (!Modifier.isPublic(field.getModifiers())
				|| !Modifier.isPublic(field.getDeclaringClass().getModifiers())) {
			field.setAccessible(true);
		}
	}

	private static String getMimeType(final String filePath)
			throws ClassNotFoundException, IllegalAccessException,
			InstantiationException, SecurityException, NoSuchMethodException,
			IllegalArgumentException, InvocationTargetException,
			NoSuchFieldException {

		Class<?> classType = Class.forName("android.media.MediaFile");
		Class<?> classMediaFileType = Class
				.forName("android.media.MediaFile$MediaFileType");
		Object invokeOperation = classType.newInstance();

		Method getFileTypeMethod = classType.getMethod("getFileType",
				String.class);
		Object ret = getFileTypeMethod.invoke(invokeOperation, new String(
				filePath));

		if (ret == null) {
			MediaFile.MediaFileType mimeFileType = MediaFile
					.getFileType(filePath);
			if (mimeFileType == null) {
				return "*/*";
			}
			
			String mimeType = mimeFileType.mimeType;
			if (mimeType != null) {
				return mimeType;
			}

			return "*/*";
		}

		Field[] f = classMediaFileType.getDeclaredFields();

		for (int i = 0; i < f.length; i++) {
			makeAccessible(f[i]);
			Log.v(TAG, "media type[" + f[i].get(ret) + "]");
			if (f[i].get(ret) instanceof String) {
				return (String) f[i].get(ret);
			}
		}

		return "*/*";
	}
}
