package com.mars.miuifilemanager.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.mars.miuifilemanager.utils.EncodingDetect;
import com.mars.miuifilemanager.view.BorderScrollView;
import com.mars.miuifilemanager.view.BorderScrollView.OnBorderListener;
import com.mars.miuifilemanager.view.MiTextView;
import com.mars.miuifilemanager.view.ViewUpdater;
import com.mars.miuifilemanager.R;

public class TextReaderActivity extends Activity implements ViewUpdater {
	private final static String TAG = "TextReaderActivity";

	public String DEFAULT_ENCODEC = "UTF-8";

	private File mFile; // o
	private InputStreamReader mInputStreamReader; // d
	private StringBuilder mText; // l

	TextView mTitleView;
	BorderScrollView mTextReaderScrollView;

	MiTextView mTextReader;

	private boolean mQuit = false; // k
	private int mPreScrollY = -1;// f

	private int b = -1;

	TextLoadTask mLoadTask;

	Handler p = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			String ts = (String) msg.obj;
			Log.e(TAG,
					"handleMessage:" + msg.arg1 + " len:"
							+ (ts != null ? ts.length() : 0) + " b:" + b);
			switch (msg.what) {
			case 1:
				String text = (String) msg.obj;
				mTextReader.setText(text);
				mTextReaderScrollView.scrollTo(0, b);
				break;
			case 2:
				String t = (String) msg.obj;
				mTextReader.setText(t);
				break;
			case 3:
				mTextReaderScrollView.scrollTo(0, msg.arg2);
				break;
			}
		}
	};
	int scrollViewY;

	private boolean mScrollTop = false;
	private boolean mScrollBottom = true;
	private boolean mScrollTopDone = false;
	private boolean mScrollBottomDone = false;

	private boolean mFirstRun = true;
 
	private String mFilePath;
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);

		setContentView(R.layout.text_reader);

		mFilePath = getIntent().getStringExtra("filepath");
		mFile = new File(mFilePath);

		if (!mFile.exists()) {
			finish();
			return;
		}

		mTitleView = (TextView) findViewById(R.id.title);
		mTitleView.setText(mFile.getName());

		// exec task
		mLoadTask = new TextLoadTask();
		mLoadTask.execute();

		mTextReaderScrollView = (BorderScrollView) findViewById(R.id.text_reader_scroll);
		mTextReaderScrollView.setVisibility(View.VISIBLE);
		mTextReaderScrollView.setSmoothScrollingEnabled(true);
		mTextReaderScrollView.setOnBorderListener(new OnBorderListener() {

			@Override
			public void onBottom() {
				// TODO Auto-generated method stub
				Log.e(TAG, "onBottom");

				mReadForward = true;
				mReadBack = false;
				synchronized (mLock) {
					mLock.notify();
				}
			}

			@Override
			public void onTop() {
				// TODO Auto-generated method stub
				Log.e(TAG, "onTop");

				mReadForward = false;
				mReadBack = true;
				synchronized (mLock) {
					// mPreScrollY = 0;
					mLock.notify();
				}
			}
		});

		mTextReader = (MiTextView) findViewById(R.id.text_reader);
		mTextReader.setVisibility(View.VISIBLE);
		mTextReader.setViewdateListener(this);

		p.postDelayed(new Runnable(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				
				mReadForward = true;
				mReadBack = false;
				synchronized (mLock) {
					mLock.notify();
				}
			}
		}, 500);
		overridePendingTransition(R.anim.go_right, R.anim.go_left);
	}

	private int mCurTextBottom;

	public final void onUpdate(int textHeight) {
		int height = mTextReaderScrollView.getHeight();
		textOtherHeight = textHeight - height;

		if (m == null || m.isEmpty()) {
			Log.e(TAG, "m is empty!bottom[" + textHeight + "][" + mReadBack
					+ "]");
			mCurTextBottom = textHeight;
			int dy = mCurTextBottom - mPreTextBottom;
			if (dy > 0 && mReadBack) {
				mReadBack = false;
				Message msg = p.obtainMessage(3);
				msg.obj = m;
				msg.arg1 = 3;
				msg.arg2 = dy;
				p.sendMessage(msg);
			}

			return;
		}
		Log.e(TAG, "m is not empty!bottom[" + textHeight + "]");

		mPreTextBottom = textHeight;

		int sy = mTextReaderScrollView.getScrollY();
		b = e - sy;
		Log.e(TAG, "textHeight:" + textHeight + " sy:" + sy + " e:" + e + " b:"
				+ b);
		Message msg = p.obtainMessage(2);
		msg.obj = m;
		msg.arg1 = 2;
		p.sendMessage(msg);

		m = null;
	}

	int textOtherHeight = -1;
	int readTimes = 0;
	boolean mReadForward = true;

	int e = -1;
	String m;
	boolean mReadBack = false;

	Object mLock = new Object();

	private String mCurrentEncodec = DEFAULT_ENCODEC;
	
	final void readFile() {
		mCurrentEncodec = EncodingDetect.getJavaEncode(mFilePath); 
		
		mText = new StringBuilder(); // l
		try {
			mInputStreamReader = new InputStreamReader(new FileInputStream(
					mFile), mCurrentEncodec);
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG,"use default codec![" + DEFAULT_ENCODEC + "]");
			try {
				mInputStreamReader = new InputStreamReader(new FileInputStream(
						mFile), DEFAULT_ENCODEC);
			} catch(Exception ex) {
				ex.printStackTrace();
			} 
			
			mCurrentEncodec = DEFAULT_ENCODEC;
		}

		Log.e(TAG, "Current encodec:" + mCurrentEncodec);
		char[] buf = new char[2048];

		while (true) {
			try {
				synchronized (mLock) {
					mLock.wait();
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (mQuit) { // k
				Log.e(TAG, "quit read task!");
				return;
			}

			if (!mReadForward) {
				// cond_5
				;
			} else {
				try {
					int len = mInputStreamReader.read(buf);
					if (len <= 0) {
						// cond_5
						;
					} else {
						mReadForward = false;

						readTimes++;

						int length = mText.length();
						if (length > 4096) {
							// cond_4
							mText.delete(0, 2048);
							e = textOtherHeight;
							Message msg = p.obtainMessage(2);
							msg.obj = mText.toString();
							msg.arg1 = 1;
							p.sendMessage(msg);
							m = mText.append(buf, 0, len).toString();
						} else {
							length = mText.length();
							if (length < 4096) {
								do {
									mText.append(buf);
									mInputStreamReader.read(buf);
									readTimes++;
									length = mText.length();
								} while (length < 4096);
							}
							mText.append(buf);
							m = mText.toString();
							e = textOtherHeight;
							Message msg = p.obtainMessage(2);
							msg.obj = mText.toString();
							msg.arg1 = 1;
							p.sendMessage(msg);
						}
					}
				} catch (Exception e) {
					Log.e(TAG, e.toString());
				}
			}
			Log.d(TAG, "bottom:" + mTextReader.getBottom() + " readTimes:"
					+ readTimes + " mText len:" + mText.length() + " m len:"
					+ (m != null ? m.length() : 0) + " TextView len:"
					+ mTextReader.length() + " mScroolTopDone:"
					+ mScrollTopDone + " mScrollBottomDone:"
					+ mScrollBottomDone);

			// cond_5
			if (!mReadBack || readTimes <= 3) {
				continue;
			} else {
				Log.d(TAG, "Prepare to read back: len: " + mText.length()
						+ " m len:" + (m != null ? m.length() : 0)
						+ " readTimes:" + readTimes);
				// mReadBack = false;
				try {
					mInputStreamReader.close();
				} catch (Exception e) {
				}

				Thread bj = new bj();
				bj.start();
			}
		}
	}

	final class TextLoadTask extends AsyncTask<Object, String, Uri> {
		protected Uri doInBackground(Object... params) {
			try {
				readFile();
				return null;
			} catch (Exception e) {
				Log.d("TextReader", e.toString());

				publishProgress(e.getMessage());
			}
			return null;
		}

		protected final void onPostExecute(String o) {
			Log.d(TAG, "Send broadcast!" + o);
		}

		protected final void onProgressUpdate(String progress) {
			super.onProgressUpdate(progress);
		}
	}

	private int mPreTextBottom = 0;

	final class bj extends Thread {
		public void run() {
			try {
				mInputStreamReader = new InputStreamReader(new FileInputStream(
						mFile), mCurrentEncodec);				
				int length = mText.length();
				if (length > 4096) {
					mText.delete(4096, length);
					int skipCount = ((readTimes - 3) - 1) * 2048;
					if (skipCount > 0) {
						mInputStreamReader.skip(skipCount);
					}
				}

				char[] buf = new char[2048];
				int len = mInputStreamReader.read(buf);

				e = textOtherHeight;

				mPreTextBottom = mTextReader.getBottom();

				readTimes--;
				Message msg = p.obtainMessage(2);
				msg.obj = mText.toString();
				msg.arg1 = 3;
				p.sendMessage(msg);

				mText.insert(0, buf);
				m = mText.toString();
				/*
				 * mText.delete(0, length); int i = 0;
				 * 
				 * char[] buf = new char[2048]; while(true) { i++;
				 * 
				 * if ((readTimes -3) <= i) { break; } else { int len =
				 * mInputStreamReader.read(buf); if (len > 0) { continue; } } }
				 * 
				 * readTimes--; mInputStreamReader.read(buf); mText.append(buf);
				 * length = mText.length(); if (length < 4096) { do {
				 * mText.append(buf); mInputStreamReader.read(buf); length =
				 * mText.length(); } while (length < 4096); } mText.append(buf);
				 * m = mText.toString(); e=textOtherHeight; Message msg =
				 * p.obtainMessage(2); msg.obj = mText.toString(); msg.arg1 = 3;
				 * p.sendMessage(msg);
				 */
			} catch (Exception e) {
				Log.e(TAG, e.getMessage());
			}
		};
	};

	public void finish() {
		Log.v(TAG, "finish!!!!!!!");
		mQuit = true;

		try {
			if (mLoadTask != null) {

				synchronized (mLock) {
					mLock.notify();
				}

				mLoadTask.cancel(true);
			}
		} catch (Exception e) {
		}

		super.finish();
	}
}
