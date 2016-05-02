package com.mars.miuifilemanager.ui;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.mars.miuifilemanager.utils.EncodingDetect;
import com.mars.miuifilemanager.utils.Util;
import com.mars.miuifilemanager.R;

public class TextEditorActivity extends Activity implements DialogInterface.OnClickListener, TextWatcher{
	private static final String TAG = "TextEditorActivity";
	
	public static String smCurrentCodec = "UTF-8";
	
	Thread mThread;
	Handler mHandler;
	
	private Uri mUri;
	String mRealPath;
	
	EditText mEditText;
	TextView mTitleView;
	ProgressBar mProgressBar;
	ScrollView mTextEditorScrollView;
	
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.text_editor);
		
		mHandler = new Handler();
		
		Intent intent = getIntent();
		mUri = intent.getData();
		if (mUri == null) {
			finish();
			return;
		}
		
		if (intent.hasExtra("realPath")) {
			mRealPath = intent.getStringExtra("realPath");
		}
		
		if ("content".equalsIgnoreCase(mUri.getScheme())) {
			try {
				
			} catch (Exception e){
				Log.e(TAG, e.toString());
				finish();
				return;
			}
		}
		
		mEditText = (EditText)findViewById(R.id.text_editor);
		mEditText.setVisibility(View.GONE);
		
		mTitleView = (TextView)findViewById(R.id.title);
		
		File file = new File(mUri.getPath());
		mTitleView.setText(file.getName()); // should set title
		
		mProgressBar = (ProgressBar)findViewById(R.id.loading_view);
		
		mTextEditorScrollView = (ScrollView)findViewById(R.id.text_editor_scroll);
		mTextEditorScrollView.setVisibility(View.VISIBLE);
		mTextEditorScrollView.setSmoothScrollingEnabled(true);
		
		runThread();
		
		overridePendingTransition(R.anim.go_right, R.anim.go_left);
	}
	
	private void stopCurrentThread() {
		if (mThread != null && !mThread.isInterrupted()) {
			mThread.interrupt();
			mThread = null;
		}
	}
	
	final class CheckContentThread implements Runnable{
		@Override
		public void run() {
			// TODO Auto-generated method stub
			
			mHandler.postDelayed(new Runnable(){
				@Override
				public void run() {
					// TODO Auto-generated method stub
					if (mThread != null && mThread.isAlive() && mThread.isInterrupted()) {
						hideTextViews(true);
					}
				}
				
			}, 100);
			
			if (mUri != null && (mUri.getPath() != null)) {
				File file = new File(mUri.getPath());
				if (file.length() >= 200000) {
					Intent intent = new Intent(TextEditorActivity.this, TextReaderActivity.class);
					intent.putExtra("filepath", file.getPath());
					startActivity(intent);
					finish();
					
					return;
				}

				if (!file.canRead()) {
					// need hack it
					
					return;
				}
				
				
				try {
					String currentEncodec = EncodingDetect.getJavaEncode(mUri.getPath()); 
					Log.e(TAG,"current encodec:" + currentEncodec);
					BufferedInputStream bIs = new BufferedInputStream(new FileInputStream(file));
					final String text = Util.getFileContents(bIs, currentEncodec);

					if (Thread.currentThread().equals(mThread)) {
						mHandler.post(new Thread() {
							public final void run() {
								
								hideTextViews(false);
								
								if (!TextUtils.isEmpty(text)) {
									mEditText.setText(text);
									mEditText.addTextChangedListener(TextEditorActivity.this);
								}
								
								stopCurrentThread();
							}
						});
					}
				} catch (Exception e) {
				}
			}
		}
	};
	
	final class ResetViewsThread implements Runnable {
		@Override
		public void run() {

		}
		
	}
	 
	final void hideTextViews(boolean hide) {
		if (hide) {
			mEditText.setVisibility(View.GONE);
			mTitleView.setVisibility(View.GONE);
		} else {
			mEditText.setVisibility(View.VISIBLE);
			mTitleView.setVisibility(View.VISIBLE);
		}
	}
	
	private void runThread() {
		stopCurrentThread();
		
		mThread = new Thread(new CheckContentThread());
		mThread.start();
	}
	
	protected Dialog onCreateDialog(int paramInt) {
		/*
	    if (paramInt == 1)
	        return new AlertDialog.Builder(this).setItems(EditorConstants.CODEC, new ba(this)).create();
	      return null;
	    */
		
		return null;
	}
	 
	@Override
	public void afterTextChanged(Editable s) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		// TODO Auto-generated method stub
		
	}
}
