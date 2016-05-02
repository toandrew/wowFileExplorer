package com.mars.miuifilemanager.view;

import java.io.File;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;

import com.mars.miuifilemanager.adapter.FileInfo;
import com.mars.miuifilemanager.utils.FileIconHelper;
import com.mars.miuifilemanager.utils.Util;
import com.mars.miuifilemanager.R;

public class InformationDialog extends AlertDialog {
	private static final String TAG = "InformationDialog";
	
	private static final int MSG_GET_SIZE = 100;
		
	private View mView;
	
	private FileInfo mFileInfo;
	
	private Context mContext;

	private AsyncTask mTask;
	
	public InformationDialog(Context context, FileInfo fileInfo, FileIconHelper fileiconhelper) {
		super(context);
		
		mFileInfo = fileInfo;
		mContext = context;
	}
	
	private Handler mHandler = new Handler(){
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_GET_SIZE:
				long size  = msg.getData().getLong("SIZE");
				String sizeStr = formatFileSizeString(size);
				TextView textview = (TextView)mView.findViewById(R.id.information_size);				
				textview.setText(sizeStr);				
				break;
			default:
			}
		}
	};
	
	private void asyncGetSize() {
		AsyncTask task = new AsyncTask() {
			private long size;
			
			private void getSize(String path) {
				if (isCancelled()) {
					return;
				}
				
				File file = new File(path);
				if (file.isDirectory()) {
					File files[] = file.listFiles();
					if (files == null) {
						return;
					}
					
					for (int i = 0; i < files.length; i++) {
						File f = files[i];
						if (isCancelled()) {
							return;
						}
						
						getSize(f.getPath());
					}
				} else {
					size += file.length();
					onSize(size);
				}
			}
			
			protected Object doInBackground(Object... params) {
				// TODO Auto-generated method stub
				
				String s = (String)params[0];
				size = 0;
				getSize(s);
				
				return null;
			}
		};
		
		mTask = task.execute(mFileInfo.filePath);
	}
	
	private String formatFileSizeString(long size) {
		String sizeStr;
		if (size >= 1024L) {
			sizeStr = Util.convertStorage(size) + " (" + mContext.getResources().getString(R.string.file_size, size) + ")";
		} else {
			sizeStr = mContext.getResources().getString(R.string.file_size, size);
		}
		
		return sizeStr;		
	}
	
	private void onSize(long size) {
		Message message = mHandler.obtainMessage();
		message.what = MSG_GET_SIZE;
		Bundle bundle = new Bundle();
		bundle.putLong("SIZE", size);
		message.setData(bundle);
		mHandler.sendMessage(message);
	}
	
	protected void onCreate(Bundle bundle) {
		mView = getLayoutInflater().inflate(R.layout.information_dialog, null);//0x7f030006
		
		if (mFileInfo.IsDir) {
			setIcon(R.drawable.folder); //0x7f020030
			asyncGetSize();
		} else {
			setIcon(R.drawable.file_icon_default); //0x7f020023
		}
		
		setTitle(mFileInfo.fileName);
		
		TextView textView = (TextView)mView.findViewById(R.id.information_size); //0x7f080046
		textView.setText(formatFileSizeString(mFileInfo.fileSize));
		
		textView = (TextView)mView.findViewById(R.id.information_location); //0x7f080045
		textView.setText(mFileInfo.filePath);
		
		String dateStr = Util.formatDateString(mContext, mFileInfo.ModifiedDate);
		textView = (TextView)mView.findViewById(R.id.information_modified); //0x7f080047
		textView.setText(dateStr);
		
		textView = (TextView)mView.findViewById(R.id.information_canread); //0x7f080048
		if (mFileInfo.canRead) {
			textView.setText(R.string.yes);
		} else {
			textView.setText(R.string.no);
		}
		
		textView = (TextView)mView.findViewById(R.id.information_canwrite); //0x7f080049
		if (mFileInfo.canWrite) {
			textView.setText(R.string.yes);
		} else {
			textView.setText(R.string.no);
		}
		
		textView = (TextView)mView.findViewById(R.id.information_ishidden); //0x7f08004a
		if (mFileInfo.isHidden) {
			textView.setText(R.string.yes);
		} else {
			textView.setText(R.string.no);
		}
		
		setView(mView);
		
		setButton(BUTTON_POSITIVE, mContext.getString(R.string.confirm_know), (DialogInterface.OnClickListener)null); //0x7f06000f
		
		super.onCreate(bundle);
	}
}
