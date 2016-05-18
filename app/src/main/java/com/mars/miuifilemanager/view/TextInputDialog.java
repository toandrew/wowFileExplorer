package com.mars.miuifilemanager.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.mars.miuifilemanager.ui.FileViewInteractionHub;
import com.mars.miuifilemanager.R;

public class TextInputDialog extends AlertDialog {
	private String mTitle;
	
	private String mMsg;
	
	private String mInputText;
	
	private OnFinishListener mListener;
	
	private Context mContext;
	
	private View mView;
	
	private EditText mFolderName;
	
	private FileViewInteractionHub mFileViewInteractionHub;
	
	public static interface OnFinishListener {
		public abstract boolean onFinish(String s);
		public abstract boolean onCancel();
	}

	public TextInputDialog(Context context, String title, String msg, String inputText, OnFinishListener listener) {
		super(context);
		
		mTitle = title;
		mMsg = msg;
		mInputText = inputText;
		
		mListener = listener;
		mContext = context;
	}
	
	protected void onCreate(Bundle bundle) {
		mView = getLayoutInflater().inflate(R.layout.textinput_dialog, null); //0x7f030008
		setTitle(mTitle);
		setMessage(mMsg);

		mFolderName =  (EditText)mView.findViewById(R.id.text); //0x7f080052
		mFolderName.setText(mInputText);
		
		setView(mView);
		
		setButton(BUTTON_POSITIVE, mContext.getString(R.string.confirm), new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
				if (which != BUTTON_POSITIVE) {
					return;
				}
				
				mInputText = mFolderName.getText().toString();
				
				if (mListener.onFinish(mInputText)) {
					dismiss();
				}
			}
		});
		
		setButton(BUTTON_NEGATIVE, mContext.getString(R.string.cancel), new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
				if (mListener.onCancel()) {
					dismiss();
				}
			}
			
		});
		
		super.onCreate(bundle);
	}
}
