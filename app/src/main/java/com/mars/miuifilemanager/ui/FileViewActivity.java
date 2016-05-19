package com.mars.miuifilemanager.ui;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.LayoutInflater.Factory;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import android.util.AttributeSet;



//import com.wooboo.adlib_android.WoobooAdView;
import com.mars.miuifilemanager.adapter.FileInfo;
import com.mars.miuifilemanager.adapter.FileListAdapter;
import com.mars.miuifilemanager.ui.FileViewInteractionHub.Mode;
import com.mars.miuifilemanager.utils.ActivitiesManager;
import com.mars.miuifilemanager.utils.FileCategoryHelper;
import com.mars.miuifilemanager.utils.FileIconHelper;
import com.mars.miuifilemanager.utils.FileSortHelper;
import com.mars.miuifilemanager.utils.FileSortHelper.SortMethod;
import com.mars.miuifilemanager.utils.Settings;
import com.mars.miuifilemanager.utils.Util;
import com.mars.miuifilemanager.R;
import com.umeng.message.PushAgent;

public class FileViewActivity extends Activity implements IFileInteractionListener{
	private static final String TAG = "FileViewActivity";
	
	private ArrayList<FileInfo> mFileNameList = new ArrayList<FileInfo>();
	
	private ArrayList<PathScrollPositionItem> mScrollPositionList = new ArrayList<PathScrollPositionItem>();
	
	private FileCategoryHelper mFileCagetoryHelper;
	
	private FileViewInteractionHub mFileViewInteractionHub;
	
	private FileIconHelper mFileIconHelper;
	
	private ArrayAdapter mAdapter;
	
	private ListView mFileListView;
	
	private boolean mBackspaceExit;
	
	private OnBackPressedListener mOnBackPressedListener;
	
	private String mPreviousPath;

	private AdManager mAdManager = null;
	
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if (!intent.getAction().equals(Intent.ACTION_MEDIA_MOUNTED) || !intent.getAction().equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
				return;
			}
			
			runOnUiThread(new Runnable(){
				public void run() {
					// TODO Auto-generated method stub
					
					updateUI();
				}
			});
		}
	};
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.e(TAG, "onCreate!");
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        setContentView(R.layout.file_explorer_list);
        
        ActivitiesManager.getInstance().registerActivity("FileView", this);
        
        mFileCagetoryHelper = new FileCategoryHelper(this);
        
        mFileViewInteractionHub = new FileViewInteractionHub(this);
        
        Intent intent = getIntent();
        String action = intent.getAction();
        
        if (!TextUtils.isEmpty(action) && action.equals(Intent.ACTION_PICK)) {
        	mFileViewInteractionHub.setMode(Mode.Pick);
        	if (!intent.getBooleanExtra("pick_folder", false)) {
        		String filter[] = intent.getStringArrayExtra("ext_filter");
				if (filter != null) {
					mFileCagetoryHelper.setCustomCategory(filter);
				}
        	} else {
        		mFileCagetoryHelper.setCustomCategory(null);
        		findViewById(R.id.pick_operation_bar).setVisibility(View.VISIBLE);
        		View pickView = findViewById(R.id.button_pick_confirm);
        		pickView.setVisibility(View.VISIBLE);
        		pickView.setOnClickListener(new OnClickListener(){
					public void onClick(View v) {
						// TODO Auto-generated method stub
						
						try{
							Intent intent = Intent.parseUri(mFileViewInteractionHub.getCurrentPath(), 0);
							setResult(Activity.RESULT_OK, intent);
							finish();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
        		});
        		
        		View pickCancelView = findViewById(R.id.button_pick_cancel);
        		pickCancelView.setVisibility(View.VISIBLE);
        		pickCancelView.setOnClickListener(new OnClickListener(){
					public void onClick(View v) {
						// TODO Auto-generated method stub
						
						finish();
					}
        		});
        	}
        } else {
        	mFileViewInteractionHub.setMode(Mode.View);
        	mFileViewInteractionHub.onSortChanged(SortMethod.name);
        }
        
        mFileIconHelper = new FileIconHelper(this);
        
        mAdapter = new FileListAdapter(this, R.layout.file_browse_item, mFileNameList, mFileViewInteractionHub, mFileIconHelper);
        mFileViewInteractionHub.setRootPath("/");
        
        boolean flag = intent.getBooleanExtra("key_base_sd", true);
        String sdPath = Util.getSdDirectory();
        String rootPath = intent.getStringExtra("root_directory");
        if (!TextUtils.isEmpty(rootPath)) {
        	if (flag) {
        		if (sdPath.startsWith(rootPath)) {
        			rootPath = sdPath;
        		}
        	}
        } else if (flag) {
        	rootPath = sdPath;
        } else {
        	rootPath = "/";
        }
        mFileViewInteractionHub.setRootPath(rootPath);
        
        Uri uri = intent.getData();
        if (uri != null) {
        	String path = uri.getPath();
        	if (flag) {
        		if (path.startsWith(sdPath)) {
        			mFileViewInteractionHub.setCurrentPath(path);
        		} else {
        			mFileViewInteractionHub.setCurrentPath(sdPath);
        		}
        	} else {
        		mFileViewInteractionHub.setCurrentPath(path);
        	}
        } 
        
        if (Util.getSavedFavoriteDirPath() != null ) {
        	Log.v(TAG, "start from favorite list!");
        	
    		String path = mFileViewInteractionHub.getRootPath();
    		if (Util.getSavedFavoriteDirPath().startsWith(path)) {
	    		mFileViewInteractionHub.setCurrentPath(Util.getSavedFavoriteDirPath());
    		}
        }

        
        if (uri != null && ( TextUtils.isEmpty(action) || !action.equals(Intent.ACTION_PICK))) {
        	mBackspaceExit = true;
        } else {
        	mBackspaceExit = false;
        }
        
        mFileListView = (ListView)findViewById(R.id.file_path_list);
        mFileListView.setAdapter(mAdapter);
        
        mFileViewInteractionHub.refreshFileList();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        filter.addDataScheme("file");
        registerReceiver(mReceiver, filter);
        
        updateUI();
        
        if (Util.getSavedFavoriteDirListener() != null) {
    		mOnBackPressedListener = Util.getSavedFavoriteDirListener();
        }
        
        ArrayList<FileInfo> arrayList = Util.getSavedCopyMoveArraylist();
        if (arrayList != null) {
        	if (Util.getCopyMoveFlag() == Util.ACTION_COPY) {
        		copyFile(arrayList);
        	} else if (Util.getCopyMoveFlag() == Util.ACTION_MOVE) {
        		moveToFile(arrayList);
        	} else {
        		Log.e(TAG, "reset copy move action!!!!");
        		Util.clearSavedCopyArraylist();
        	}
        }
        
        Util.resetFavoriteDirPath();
        
        mHandler.sendEmptyMessageDelayed(MSG_ID_AD, 1000);

		mAdManager = new AdManager(this);
		mAdManager.onCreate();

		PushAgent.getInstance(this).onAppStart();
    }
    
	@Override
	protected void onResume() {
		super.onResume();
		Log.e(TAG, "onResume");

		mAdManager.onResume();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		Log.e(TAG, "onPause");

		mAdManager.onPause();
	}
	
	protected void onDestroy() {
		super.onDestroy();
		
		if (mReceiver != null) {
			unregisterReceiver(mReceiver);
		}
		
		Log.e(TAG,"onDestroy");

		mAdManager.onDestroy();
		
		ActivitiesManager.getInstance().unRegisterActivity("FileView");
	}
    
    public void refresh() {
    	mFileViewInteractionHub.refreshFileList();
    }
    
	public static interface OnBackPressedListener {
		public abstract boolean OnBack();
	}
	
	public static interface SelectFilesCallback {
		public abstract void selected(ArrayList arraylist);
	}
	
	private class PathScrollPositionItem {
		String path;
		int pos;
		
		PathScrollPositionItem(String s, int i) {
			path = s;
			pos = i;
		}
	}

	@Override
	public void addSingleFile(FileInfo fileInfo) {
		// TODO Auto-generated method stub
		
		mFileNameList.add(fileInfo);
		onDataChanged();
	}

	public Collection getAllFiles() {
		// TODO Auto-generated method stub
		
		return mFileNameList;
	}

	@Override
	public Context getContext() {
		// TODO Auto-generated method stub
		
		return this;
	}

	@Override
	public String getDisplayPath(String path) {
		// TODO Auto-generated method stub
		
		String s = getString(R.string.sd_folder);
		
		String rootPath = mFileViewInteractionHub.getRootPath();
		if (rootPath.equals(path)) {
			return s;
		}
		
		String subS = path;
		if (!rootPath.equals("/") && path.indexOf(rootPath) == 0) {
				int i = rootPath.length();
				subS = path.substring(i);
		}
		
		return s+subS;
	}

	@Override
	public FileIconHelper getFileIconHelper() {
		// TODO Auto-generated method stub
		return mFileIconHelper;
	}

	@Override
	public FileInfo getItem(int i) {
		// TODO Auto-generated method stub
		
		if (i < 0 || i >= mFileNameList.size()) {
			return null;
		} 
		
		return (FileInfo)mFileNameList.get(i);
	}

	@Override
	public int getItemCount() {
		// TODO Auto-generated method stub
		
		return mFileNameList.size();
	}

	@Override
	public String getRealPath(String path) {
		// TODO Auto-generated method stub
		
		String str;
		String rootPath = mFileViewInteractionHub.getRootPath();
		String s = getString(R.string.sd_folder); //0x7f060046
		if (path.equals(s)) {
			str = rootPath;
		} else {
			int i = path.indexOf("/"); 
			str = path.substring(i);

			if (!rootPath.equals("/")) {
				str = rootPath + str;
			}	
		}

		return str;
	}

	@Override
	public View getViewById(int id) {
		// TODO Auto-generated method stub
		
		return findViewById(id);
	}

	@Override
	public void onDataChanged() {
		// TODO Auto-generated method stub
		runOnUiThread(new Runnable() {
			public void run() {
				// TODO Auto-generated method stub
				
				if (mAdapter != null) {
					mAdapter.notifyDataSetChanged();
				}
			}
		});
	}

	@Override
	public boolean onNavigation(String s) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onOperation(int i) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onPick(FileInfo fileInfo) {
		// TODO Auto-generated method stub
		
		try{
			Intent intent = Intent.parseUri(Uri.fromFile(new File(fileInfo.filePath)).toString(), 0);
			setResult(-1, intent);
			finish();
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public boolean onRefreshFileList(String path, FileSortHelper filesorthelper) {
		// TODO Auto-generated method stub
		
		mOnBackPressedListener = null;
		
		File file = new File(path);
		if (!file.exists() || !file.isDirectory()) {
			return false;
		}
		
		final int pos = computeScrollPosition(path);
		mFileNameList.clear();
		
		FilenameFilter filter = mFileCagetoryHelper.getFilter();
		File files[] = file.listFiles(filter);
		if (files == null) {
			return true;
		}
		
		for (int i = 0; i < files.length; i++) {
			File f = files[i];
			/*
			if (mFileViewInteractionHub.isMoveState()) {
				continue;
			}
			*/
			
			if (mFileViewInteractionHub.isFileSelected(f.getPath())) {
				continue;
			}
			
			String absPath = f.getAbsolutePath();
			if (Util.isNormalFile(absPath) && Util.shouldShowFile(absPath)) {
				FilenameFilter fileFilter = mFileCagetoryHelper.getFilter();
				
				boolean flag = Settings.instance().getShowDotAndHiddenFiles();
				
				FileInfo fileInfo = Util.GetFileInfo(f, fileFilter, flag);
				if (fileInfo != null) {
					mFileNameList.add(fileInfo);
				}
			}
		}
		
		sortCurrentList(filesorthelper); // ????
		
		showEmptyView(mFileNameList.size() == 0);
		
		mFileListView.post(new Runnable(){
			public void run() {
				// TODO Auto-generated method stub
				
				mFileListView.setSelection(pos);
			}
		});
		
		return true;
	}

	@Override
	public boolean shouldHideMenu(int itemId) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void sortCurrentList(FileSortHelper filesorthelper) {
		// TODO Auto-generated method stub
		
		Comparator comparator = filesorthelper.getComparator();
		Collections.sort(mFileNameList, comparator);
		
		onDataChanged();
	}
	
	// need more study
	private int computeScrollPosition(String pos) {
		if (mPreviousPath == null) {
			Log.v(TAG,"computeScrollPosition: result pos: " + pos + " stack count:" +  mScrollPositionList.size());
			mPreviousPath = pos;
			
			return 0;
		}
		
		if (pos.startsWith(mPreviousPath)) {
			int j = mFileListView.getFirstVisiblePosition();
			if (mScrollPositionList.size() != 0)  {
				int last = mScrollPositionList.size() - 1;
				String path = ((PathScrollPositionItem)mScrollPositionList.get(last)).path;
				if (mPreviousPath.equals(path)) {
					((PathScrollPositionItem)mScrollPositionList.get(last)).pos = j;
					
					Log.v(TAG,"computeScrollPosition: update item: " + mPreviousPath + " stack count:" + mScrollPositionList.size());
					
					
					Log.v(TAG,"computeScrollPosition: result pos: " + pos + " stack count:" + mScrollPositionList.size());
					
					mPreviousPath = pos;
					
					return 0;
				} else {
					PathScrollPositionItem item = new PathScrollPositionItem(mPreviousPath,j);
					mScrollPositionList.add(item);
					mPreviousPath = pos;	
				}
			} else {
				PathScrollPositionItem item = new PathScrollPositionItem(mPreviousPath,j);
				mScrollPositionList.add(item);
				mPreviousPath = pos;
				return 0;
			}
		} else {
			int i = 0;
			String s = null;
			for (i = mScrollPositionList.size() -1; i >= 0; i--) {
				s = ((PathScrollPositionItem)mScrollPositionList.get(i)).path;
				if (pos.startsWith(s)) {
					break;
				}
			}
				int ret = 0;
				if (i >=0) {
					 ret = ((PathScrollPositionItem)mScrollPositionList.get(i)).pos;
				}
				mPreviousPath = s;
				
				/*if (mScrollPositionList.size() - 1 > i-1 || mScrollPositionList.size() - 1 < 0) {
					mPreviousPath = s;
					return i;
				} else {
				*/
				if (mScrollPositionList.size() > 0) {
					mScrollPositionList.remove(mScrollPositionList.size() - 1);
				}
				//}
				
				return ret;
		}
		
		return 0;
	}
	
	private void showEmptyView(boolean flag) {
		View view = findViewById(R.id.empty_view); //0x7f080015
		if (view == null) {
			return;
		}
		
		view.setVisibility(flag?View.VISIBLE:View.GONE);
	}
	
	private void updateUI() {
		boolean flag = Util.isSDCardReady();
		View view = findViewById(R.id.sd_not_available_page); //0x7f080018
		view.setVisibility(flag?View.GONE:View.VISIBLE);
		
		View navView = findViewById(R.id.navigation_bar); //0x7f08000d
		navView.setVisibility(flag?View.VISIBLE:View.GONE);
		
		mFileListView.setVisibility(flag?View.VISIBLE:View.GONE);
		
		if (flag) {
			mFileViewInteractionHub.refreshFileList();
		}
	}
	
	public void copyFile(ArrayList arraylist) {
		mFileViewInteractionHub.onOperationCopy(arraylist);
		
		Util.clearSavedCopyArraylist();
	}
	
	public void moveToFile(ArrayList arraylist) {
		mFileViewInteractionHub.moveFileFrom(arraylist);
		
		Util.clearSavedCopyArraylist();
	}
	
	public void onBackPressed() {
		Log.v(TAG, "onBackPressed");
		
		if (mBackspaceExit) {
			 super.onBackPressed();
			return;
		}
		
		if (mOnBackPressedListener != null && mOnBackPressedListener.OnBack()) {
			mOnBackPressedListener = null;
			return;
		}
		
		if (mFileViewInteractionHub.onBackPressed()) {
			return;
		}
		 super.onBackPressed();
	}
	
	public boolean onPrepareOptionsMenu(Menu menu) {
		return mFileViewInteractionHub.onPrepareOptionsMenu(menu);
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
		return mFileViewInteractionHub.onCreateOptionsMenu(menu);
	}

	public boolean setPath(String path, OnBackPressedListener onbackpressedlistener) {
		String rootPath = mFileViewInteractionHub.getRootPath();
		if (!path.startsWith(rootPath)) {
			Log.e(TAG,"Invalid target path: " + path + "  [root]:" + mFileViewInteractionHub.getRootPath());
			return false;
		}
		
		mFileViewInteractionHub.setCurrentPath(path);
		mFileViewInteractionHub.refreshFileList();
		mOnBackPressedListener = onbackpressedlistener;
		
		return true;
	}
	
	public void resetCurrentPath() {
		Log.v(TAG,"resetCurrentPath[" + mFileViewInteractionHub.getRootPath() + "]");
		mFileViewInteractionHub.setCurrentPath(mFileViewInteractionHub.getRootPath());
	}

	@Override
	public void onDelete(FileInfo fileInfo) {
		// TODO Auto-generated method stub
		
	}
	
	//private WoobooAdView mAdView;
	private static final int MSG_ID_AD		= 1;
	public Handler mHandler = new Handler(){
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_ID_AD:
				/*
		        LinearLayout layout = (LinearLayout)findViewById(R.id.file_browse_list);
		        mAdView = new WoobooAdView(FileViewActivity.this, "e7bcf32c331b4727b2990aeccb5a0c8e",0xFF000000, 0xFFFFFFFF, false, 60, null);
				LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
				mAdView.setLayoutParams(params);
				layout.addView(mAdView);
				*/
				break;
			}
		}
	};
}