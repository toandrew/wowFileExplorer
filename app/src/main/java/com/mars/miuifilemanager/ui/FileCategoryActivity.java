package com.mars.miuifilemanager.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

//import com.feedback.NotificationType;
//import com.feedback.UMFeedbackService;
//import com.wooboo.adlib_android.WoobooAdView;
import com.mars.miuifilemanager.FileExplorerTabActivity;
import com.mars.miuifilemanager.R;
import com.mars.miuifilemanager.adapter.AppList;
import com.mars.miuifilemanager.adapter.FavoriteList;
import com.mars.miuifilemanager.adapter.FileInfo;
import com.mars.miuifilemanager.adapter.FileListCursorAdapter;
import com.mars.miuifilemanager.service.FileScanService;
import com.mars.miuifilemanager.ui.FileViewInteractionHub.Mode;
import com.mars.miuifilemanager.utils.ActivitiesManager;
import com.mars.miuifilemanager.utils.FavoriteDatabaseHelper;
import com.mars.miuifilemanager.utils.FileCategoryHelper;
import com.mars.miuifilemanager.utils.FileCategoryHelper.CategoryInfo;
import com.mars.miuifilemanager.utils.FileCategoryHelper.FileCategory;
import com.mars.miuifilemanager.utils.FileIconHelper;
import com.mars.miuifilemanager.utils.FileSortHelper;
import com.mars.miuifilemanager.utils.FileSortHelper.SortMethod;
import com.mars.miuifilemanager.utils.PackageInstallHelper;
import com.mars.miuifilemanager.utils.Util;
import com.mars.miuifilemanager.utils.Util.SDCardInfo;
import com.mars.miuifilemanager.view.CategoryBar;

public class FileCategoryActivity extends Activity 
	implements FavoriteDatabaseHelper.FavoriteDatabaseListener, IFileInteractionListener {
	
	private static final String TAG = "FileCategoryActivity";
	
	private FileViewInteractionHub mFileViewInteractionHub;
	
	private FileIconHelper mFileIconHelper;
	
	private FavoriteList mFavoriteList;
	
	private AppList mAppList;
	
	private FileListCursorAdapter mAdapter;
	
	private View mButtonInstall;
	
	private ScannerReceiver mScannerReceiver;
	
	private PackageReceiver mPackageReceiver;
	
	private FileCategoryHelper mFileCagetoryHelper;
	
	private CategoryBar mCategoryBar;

	private HashMap mCategoryIndex = new HashMap();
	
	private Timer mTimer;
	
	private static final int MSG_INIT_UPDATE_UI 		= 99;
	private static final int MSG_UPDATE_UI 				= 100;
	private static final int MSG_START_SCAN				= 101;
	public static final int MSG_APP_UPDATED				= 102;
	private static final int MSG_REFRESH_APP 			= 103;
	private static final int MSG_STOP_SCAN_PROGRESS_BAR = 104;
	public static final int MSG_FAV_UPDATED				= 105;

	private FullAdManager mAdManager = null;
	
	public static enum ViewPage {
		Home,
		Favorite,
		Category,
		App,
		NoSD,
		About,
		Invalid
	};
	
	private static final String CATEGORY_ENTRY_KEY = "CATEGORY_ENTRY_KEY";
	
	private ViewPage mPreViewPage = ViewPage.Invalid;
	
	private ViewPage mCurViewPage = ViewPage.Invalid;
	
	private static HashMap<Integer, FileCategory> mButton2Category = new HashMap<Integer, FileCategory>(){
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			put(R.id.category_music, FileCategory.Music);
			put(R.id.category_video, FileCategory.Video);
			put(R.id.category_picture, FileCategory.Picture);
			put(R.id.category_theme, FileCategory.Theme);
			put(R.id.category_document, FileCategory.Doc);
			put(R.id.category_zip, FileCategory.Zip);
			put(R.id.category_apk, FileCategory.Apk);
			put(R.id.category_other, FileCategory.Other);
			put(R.id.category_favorite, FileCategory.Favorite);
			put(R.id.category_applications, FileCategory.Applications);
			put(R.id.category_about, FileCategory.About);
		}
	};
	
	private OnClickListener mOnClickListener = new OnClickListener() {
		public void onClick(View v) {
			// TODO Auto-generated method stub
			
			Log.v(TAG,"mOnClickListener[" + v.getId() + "]");
			
			switch(v.getId()) {

			case R.id.button_operation_install:
				onOperationInstall();
				break;

			case R.id.about_app_support:
				Toast.makeText(FileCategoryActivity.this, R.string.thanks, Toast.LENGTH_SHORT).show();
				
				mAdManager.onCreate();
				break;

			default:
				
				FileCategory category = (FileCategoryHelper.FileCategory)mButton2Category.get(v.getId());
				if (category == null) {
					return;
				}
				
				if (category == FileCategory.About) {
					//return;
				}
				
				onCategorySelected(category);
				break;
			}
		}
	};
	
	public Handler getHandler() {
		return mHandler;
	}
	
	private Handler mHandler = new Handler(){
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch(msg.what) {
			case MSG_INIT_UPDATE_UI:
				setupCategoryInfo();
				
				initUI();
				
				// begin scan files!
				sendEmptyMessageDelayed(MSG_START_SCAN, 2000);
				break;
				
			case MSG_UPDATE_UI:
				updateUI();
				break;
				
			case MSG_START_SCAN:
		        
		        mFavoriteList.initList();
		     
		        mAppList.initList();
		        
				Context context = getContext();
				Intent intent = new Intent(context, FileScanService.class);
				if (!FileScanService.isRunning()) {
					Log.v(TAG,"First Start File Scan Service!!!");
					context.startService(intent);
				}
				
				ProgressBar scanProgress = (ProgressBar)findViewById(R.id.scan_progressbar);
				if (scanProgress != null) {
					scanProgress.setVisibility(View.VISIBLE);
				}
				
				break;
				
			case MSG_REFRESH_APP:
				mAppList.update();
				break;
				
			case MSG_APP_UPDATED:
				mAppList.getArrayAdapter().notifyDataSetChanged();
				
				long count = mAppList.getCount();
				if (mCurViewPage == ViewPage.App) {
					if (count == 0) {
						showEmptyView(true,true);
					} else {
						showEmptyView(false,true);
					}
				}
				setCategoryCount(FileCategory.Applications, count);
				
				break;
			case MSG_STOP_SCAN_PROGRESS_BAR:
				ProgressBar scanProgressBar = (ProgressBar)findViewById(R.id.scan_progressbar);
				if (scanProgressBar != null) {
					scanProgressBar.setVisibility(View.GONE);
				}
				break;
				
			case MSG_FAV_UPDATED:
				mFavoriteList.getArrayAdapter().notifyDataSetChanged();
				break;
			}
			
			super.handleMessage(msg);
		}
	};
	
	private void onCategorySelected(FileCategory filecategory) {
		if (mFileCagetoryHelper.getCurCategory() != filecategory) {
			mFileCagetoryHelper.setCurCategory(filecategory);
			
			String path = mFileViewInteractionHub.getRootPath() + getString(mFileCagetoryHelper.getCurCategoryNameResId());
			mFileViewInteractionHub.setCurrentPath(path);
			mFileViewInteractionHub.refreshFileList();

			/*
			if (filecategory == FileCategory.Apk) {
				mButtonInstall.setVisibility(View.GONE);
			} else {
				mButtonInstall.setVisibility(View.GONE);
			}
			*/
		}
		
		if (filecategory == FileCategory.Favorite) {
			showPage(ViewPage.Favorite);
		} else if (filecategory == FileCategory.Applications) {
			showPage(ViewPage.App);
		} else if (filecategory == FileCategory.About) {
			showPage(ViewPage.About);
		} else {
			showPage(ViewPage.Category);
		}
	}
	
	private void showPage(ViewPage viewpage) {
		// TODO Auto-generated method stub
		
		if (mCurViewPage == viewpage) {
			return;
		}
		
		mCurViewPage = viewpage;
		
		showView(R.id.file_path_list, false); //0x7f080013
		showView(R.id.navigation_bar, false); //0x7f08000d
		showView(R.id.category_page, false);  //0x7f080019
		showView(R.id.operation_bar, false); //	
		showView(R.id.sd_not_available_page, false); //0x7f080018
		mFavoriteList.show(false);
		showView(R.id.about_page, false);
		
		mAppList.show(false);
		
		showEmptyView(false,false);

		switch(viewpage) {
		case Home:
			showView(R.id.category_page, true); //0x7f080019
			break;
			
		case Favorite:
			showView(R.id.navigation_bar, true); //0x7f08000d
			mFavoriteList.show(true);
			if (mFavoriteList.getCount() == 0) {
				showEmptyView(true,false);
			} else {
				showEmptyView(false,false);
			}
			break;
			
		case App:
			showView(R.id.navigation_bar, true); //0x7f08000d
			mAppList.show(true);
			if (mAppList.getCount() == 0) {
				showEmptyView(true,true);
			} else {
				showEmptyView(false,true);
			}
			break;			
			
		case Category:
			showView(R.id.navigation_bar, true); //0x7f08000d
			showView(R.id.file_path_list, true); //0x7f080013
			if (mAdapter.getCount() == 0) {
				showEmptyView(true,false);
			} else {
				showEmptyView(false,false);
			}
			
			break;
			
		case About:
			showView(R.id.navigation_bar, true); //0x7f08000d
			TextView appNameView = (TextView)findViewById(R.id.about_app_name);
			String ver = getVersion();
			appNameView.setText(getString(R.string.app_name) + " V"+ ver);

			TextView appInfoView = (TextView)findViewById(R.id.about_app_info);
			final String contact = "www.crossker.com\n";
			appInfoView.setAutoLinkMask(Linkify.ALL);
			appInfoView.setText(contact);

			TextView emailView = (TextView)findViewById(R.id.about_app_email);
			final String email = "Email: andalululu@sina.com\n";
			emailView.setAutoLinkMask(Linkify.ALL);
			emailView.setText(email);

			showView(R.id.about_page, true);
			break;
			
		case NoSD:
			showView(R.id.sd_not_available_page, true); //0x7f080018
			break;
			
		case Invalid:
			break;
		}
	}

	private void showEmptyView(boolean flag, boolean isApp) {
		// TODO Auto-generated method stub
		
		View view = findViewById(R.id.empty_view);
		TextView textView = (TextView)findViewById(R.id.empty_file);
		if (view == null || textView == null) {
			return;
		}

		if (flag) {
			view.setVisibility(View.VISIBLE);
			if (isApp) {
				if (FileScanService.isRunning() || mAppList.isScanning()) {
					textView.setText(R.string.scanning);
				} else {
					textView.setText(R.string.no_app);
				}
			} else {
				if (!FileScanService.isRunning()) {
					textView.setText(R.string.no_file);
				} else {
					if (mCurViewPage == ViewPage.Category &&
							(mFileCagetoryHelper.getCurCategory() == FileCategory.Picture
							||mFileCagetoryHelper.getCurCategory() == FileCategory.Music 
							||mFileCagetoryHelper.getCurCategory() == FileCategory.Video
							||mFileCagetoryHelper.getCurCategory() == FileCategory.Favorite)){
						textView.setText(R.string.no_file);
					} else {
						textView.setText(R.string.scanning);
					}
				}
			}
		} else {
			view.setVisibility(View.GONE);
		}
	}

	private void showView(int viewId, boolean flag) {
		// TODO Auto-generated method stub
		View view = findViewById(viewId);
		if (flag) {
			view.setVisibility(View.VISIBLE);
		} else {
			view.setVisibility(View.GONE);
		}
	}

	protected void onOperationInstall() {
		// TODO Auto-generated method stub
		
		mFileViewInteractionHub.showOperationBar(false);
		PackageInstallHelper.install(mFileViewInteractionHub.getSelectedFileList(), this);
		mFileViewInteractionHub.clearSelection();
	}

	private class ScannerReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
			Log.v(TAG,"received action[" + intent.getAction() + "]");			
			if (action.equalsIgnoreCase(Intent.ACTION_MEDIA_MOUNTED) || action.equalsIgnoreCase(Intent.ACTION_MEDIA_SCANNER_FINISHED) || action.equalsIgnoreCase(Intent.ACTION_MEDIA_UNMOUNTED)) {
				notifyFileChanged();
			} else if (action.equalsIgnoreCase(FileScanService.ACTION_UPDATE_UI)) {
				boolean notify = intent.getBooleanExtra(FileScanService.ACTION_UPDATE_UI_ARGS, false);
				if (notify) {
					notifyFileChanged();
				}
				
				// stop progress bar
				mHandler.sendEmptyMessage(MSG_STOP_SCAN_PROGRESS_BAR);
			}
		}
	}
	
	public void notifyFileChanged() {
		if (mTimer != null) {
			mTimer.cancel();
		}
		
		mTimer = new Timer();
		mTimer.schedule(new TimerTask(){
			public void run() {
				// TODO Auto-generated method stub
				
				mTimer = null;
				
				mHandler.sendEmptyMessage(MSG_UPDATE_UI);
			}
		}, 1000L);
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
		//UMFeedbackService.enableNewReplyNotification(this, NotificationType.AlertDialog);

        Log.e(TAG, "onCreate!");
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        setContentView(R.layout.file_explorer_category);
        
		mFileViewInteractionHub = new FileViewInteractionHub(this);
        mFileViewInteractionHub.setMode(Mode.View);
        mFileViewInteractionHub.setRootPath("/");
        
        mFileIconHelper = new FileIconHelper(this);
     
        ListView listview = (ListView)findViewById(R.id.favorite_list);
        mFavoriteList = new FavoriteList(this, listview, this, mFileIconHelper);
        
        ListView appListView = (ListView)findViewById(R.id.app_list);
        mAppList = new AppList(this, appListView, mFileIconHelper);
        
        mAdapter = new FileListCursorAdapter(this, null, mFileViewInteractionHub, mFileIconHelper);

        ListView fileListView = (ListView)findViewById(R.id.file_path_list);
        fileListView.setAdapter(mAdapter);
        
        setupCategoryInfo();


        
        registerReceivers();
        
        ActivitiesManager.getInstance().registerActivity("FileCategory", this);
        


//		PushAgent.getInstance(this).onAppStart();

		mHandler.sendEmptyMessageDelayed(MSG_INIT_UPDATE_UI,500);

		mAdManager = new FullAdManager(this);
    }

    //private WoobooAdView mAdView;
	private void initUI() {
        updateUI();
        
        setupClick();
        
        /*
        LinearLayout layout = (LinearLayout)findViewById(R.id.file_browse_category);
        mAdView = new WoobooAdView(this, "e7bcf32c331b4727b2990aeccb5a0c8e",0xFF000000, 0xFFFFFFFF, false, 60, null);
		LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		mAdView.setLayoutParams(params);
		layout.addView(mAdView);
		*/
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

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		Log.e(TAG,"onDestroy");
		
		if (mScannerReceiver != null) {
			unregisterReceiver(mScannerReceiver);
			mScannerReceiver = null;
		}
		
		if (mPackageReceiver != null) {
			unregisterReceiver(mPackageReceiver);
			mPackageReceiver = null;
		}

		ActivitiesManager.getInstance().unRegisterActivity("FileCategory");


		mAdManager.onDestroy();
	}
	
	protected void onSaveInstanceState(Bundle outState) {
		Log.v(TAG,"onSaveInstanceState!![" + mFileCagetoryHelper.getCurCategory() + "]");
		if (mFileCagetoryHelper.getCurCategory() != FileCategory.All) {
			outState.putInt(CATEGORY_ENTRY_KEY, mFileCagetoryHelper.getCurCategory().ordinal());
		}
		
		super.onSaveInstanceState(outState);
	}
	
	protected void onRestoreInstanceState(Bundle outState) {
		int id = outState.getInt(CATEGORY_ENTRY_KEY, -1);
		Log.v(TAG,"onRestoreInstanceState!![" + id + "]");
		if (id != -1) {
			FileCategory category = FileCategory.values()[id];
			onCategorySelected(category);
		}
		
		super.onRestoreInstanceState(outState);
	}
	
	public boolean onPrepareOptionsMenu(Menu menu) {

		if (isHomePage() 
			|| mFileCagetoryHelper.getCurCategory() == FileCategory.Favorite
			|| mFileCagetoryHelper.getCurCategory() == FileCategory.Applications
			|| mFileCagetoryHelper.getCurCategory() == FileCategory.About) {
			MenuItem menuItem = menu.findItem(FileViewInteractionHub.OPTION_MENU_SORT);
			if (menuItem != null) {
				menuItem.setVisible(false);
			}
			
			menuItem = menu.findItem(FileViewInteractionHub.OPTION_MENU_REFRESH);
			if (menuItem != null) {
				menuItem.setVisible(false);
			}
			
			menuItem = menu.findItem(FileViewInteractionHub.OPTION_MENU_SEL_UNSEL);
			if (menuItem != null) {
				menuItem.setVisible(false);
			}
			
			return super.onCreateOptionsMenu(menu);
		}

		MenuItem menuItem = menu.findItem(FileViewInteractionHub.OPTION_MENU_SORT);
		if (menuItem != null) {
			menuItem.setVisible(true);
		}
		
		menuItem = menu.findItem(FileViewInteractionHub.OPTION_MENU_REFRESH);
		if (menuItem != null) {
			menuItem.setVisible(true);
		}
		
		menuItem = menu.findItem(FileViewInteractionHub.OPTION_MENU_SEL_UNSEL);
		if (menuItem != null) {
			menuItem.setVisible(true);
		}
		
		return mFileViewInteractionHub.onPrepareOptionsMenu(menu);
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
		return  mFileViewInteractionHub.onCreateOptionsMenu(menu);
	}
	
	private void registerReceivers() {
		// TODO Auto-generated method stub
		
		mScannerReceiver = new ScannerReceiver();
		IntentFilter intentfilter = new IntentFilter();
		intentfilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
		intentfilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
		intentfilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
		intentfilter.addAction(FileScanService.ACTION_UPDATE_UI);
		intentfilter.addDataScheme("file");
		registerReceiver(mScannerReceiver, intentfilter);
		
		
		mPackageReceiver = new PackageReceiver();
		IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
		filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
		filter.addDataScheme("package");
		registerReceiver(mPackageReceiver, filter);
	}

	private void updateUI() {
		// TODO Auto-generated method stub
		
		if (!Util.isSDCardReady()) {
			Util.showTab(0);
			mPreViewPage = mCurViewPage;
			showPage(ViewPage.NoSD);
		} else {
			View view = findViewById(R.id.sd_not_available_page);
			view.setVisibility(View.GONE);
			
			if (mPreViewPage == ViewPage.Invalid && mCurViewPage == ViewPage.Invalid ) {
				Log.v(TAG,"updateUI:show home page");
				showPage(ViewPage.Home);
			} else if (mPreViewPage != ViewPage.Invalid){
				Log.v(TAG,"updateUI:show pre page[" + mPreViewPage + "]");
				showPage(mPreViewPage);
				
				mPreViewPage = ViewPage.Invalid;
			}
			Log.v(TAG, "update category info!");
			refreshCategoryInfo();
			mFileViewInteractionHub.refreshFileList();
			
			/*
			FileViewActivity fileviewactivity = (FileViewActivity)ActivitiesManager.getInstance().getActivity("FileView");
			if (fileviewactivity != null) {
				fileviewactivity.refresh();
			}
			*/
		}
		
		FileExplorerTabActivity fileexplorertabactivity = (FileExplorerTabActivity)ActivitiesManager.getInstance().getActivity("FileExplorerTab");
		if (fileexplorertabactivity != null) {
			fileexplorertabactivity.showTabWidget(0);
		}
	}

	private void setupCategoryInfo() {
		// TODO Auto-generated method stub
		
		mFileCagetoryHelper = new FileCategoryHelper(this);
		
		mCategoryBar = (CategoryBar)findViewById(R.id.category_bar);
		
		int resImgs[] = {
			R.drawable.category_bar_picture,
			R.drawable.category_bar_music,
			R.drawable.category_bar_video,
			R.drawable.category_bar_apk,
			R.drawable.category_bar_theme,
			R.drawable.category_bar_document,
			R.drawable.category_bar_zip,
			R.drawable.category_bar_other
		};
		
		for (int i = 0; i < resImgs.length; i++) {
			mCategoryBar.addCategory(resImgs[i]);
		}
		
		FileCategory filecategorys[] = new FileCategory[] {
			FileCategory.Picture,
			FileCategory.Music,
			FileCategory.Video,
			FileCategory.Apk,
			FileCategory.Theme,
			FileCategory.Doc,
			FileCategory.Zip,
			FileCategory.Other
		};
		
		for (int i = 0;i < filecategorys.length; i++) {
			mCategoryIndex.put(filecategorys[i], i);
		}
	}

	private void setupClick() {
		// TODO Auto-generated method stub
		
		setupClick(R.id.category_music); //0x7f08001b
		setupClick(R.id.category_video); //0x7f08001d
		setupClick(R.id.category_picture); //0x7f08001f
		setupClick(R.id.category_theme); //0x7f080021
		setupClick(R.id.category_document); //0x7f080023
		setupClick(R.id.category_zip); //0x7f080025
		setupClick(R.id.category_apk); //0x7f080027
		setupClick(R.id.category_other); //0x7f080029
		setupClick(R.id.category_favorite); //0x7f08002b
		
		setupClick(R.id.category_applications);
		setupClick(R.id.category_about);
		
		setupClick(R.id.about_app_support);
	}

	private void setupClick(int id) {
		View view = findViewById(id);
		view.setOnClickListener(mOnClickListener);
	}
	
	@Override
	public void onFavoriteDatabaseChanged() {
		// TODO Auto-generated method stub
		
		runOnUiThread(new Runnable(){
			public void run() {
				// TODO Auto-generated method stub
				setCategoryCount(FileCategory.Favorite, mFavoriteList.getCount());
			};
		});
	}

	@Override
	public void addSingleFile(FileInfo fileinfo) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Collection getAllFiles() {
		// TODO Auto-generated method stub
		
		return mAdapter.getAllFiles();
	}

	@Override
	public Context getContext() {
		// TODO Auto-generated method stub
		
		return this;
	}

	@Override
	public String getDisplayPath(String path) {
		return getString(R.string.tab_category) + path;
	}

	@Override
	public FileIconHelper getFileIconHelper() {
		// TODO Auto-generated method stub
		
		return mFileIconHelper;
	}

	@Override
	public FileInfo getItem(int i) {
		// TODO Auto-generated method stub
		
		return mAdapter.getFileItem(i);
	}

	@Override
	public int getItemCount() {
		// TODO Auto-generated method stub
		
		return mAdapter.getCount();
	}

	@Override
	public String getRealPath(String s) {
		// TODO Auto-generated method stub
		
		return "";
	}

	@Override
	public View getViewById(int resId) {
		// TODO Auto-generated method stub
		
		return findViewById(resId);
	}

	@Override
	public void onDataChanged() {
		// TODO Auto-generated method stub
		runOnUiThread(new Runnable(){
			public void run() {
				// TODO Auto-generated method stub
				
				mAdapter.notifyDataSetChanged();
				mFavoriteList.getArrayAdapter().notifyDataSetChanged();
				
				if (mAdapter.getCount() == 0) {
					showEmptyView(true, false);
				} else {
					showEmptyView(false, false);
				}
			}
		});
	}

	@Override
	public boolean onNavigation(String s) {
		// TODO Auto-generated method stub
		
		// reset category
		mFileCagetoryHelper.setCurCategory(FileCategory.All);
		
		showPage(ViewPage.Home);
		
		return true;
	}

	@Override
	public boolean onOperation(int action) {
		// TODO Auto-generated method stub
		
		switch (action) {
		case FileViewInteractionHub.MENU_ID_COPY: // copy
		case R.id.button_operation_copy:
			copyFileInFileView(mFileViewInteractionHub.getSelectedFileList());
			mFileViewInteractionHub.clearSelection();			
			break;
			
		case FileViewInteractionHub.MENU_ID_CUT: // move
		case R.id.button_operation_move:
			startMoveToFileView(mFileViewInteractionHub.getSelectedFileList());
			mFileViewInteractionHub.clearSelection();			
			break;
			
		case 3:
			// reset category
			mFileCagetoryHelper.setCurCategory(FileCategory.All);
			
			showPage(ViewPage.Home);
			break;
		default:
			return false;
		}
		
		return true;
	}

	@Override
	public void onPick(FileInfo fileinfo) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean onRefreshFileList(String s, FileSortHelper filesorthelper) {
		// TODO Auto-generated method stub

		FileCategory category = mFileCagetoryHelper.getCurCategory();
		if (category == FileCategory.Favorite) {
			return false;
		}
		
		if (category == FileCategory.All) {
			return false;
		}
		
		if (category == FileCategory.Applications) {
			return false;
		}
		
		if (category == FileCategory.About) {
			return false;
		}
		
		SortMethod sort = filesorthelper.getSortMethod();
		Cursor cursor = mFileCagetoryHelper.query(category, sort);
		
		boolean ret = false;
		if (cursor == null || cursor.getCount() == 0) {
			ret = true;
		}
		
		showEmptyView(ret,false);
		mAdapter.changeCursor(cursor);
		
		return true;
	}

	@Override
	public boolean shouldHideMenu(int id) {
		// TODO Auto-generated method stub

		if (id == FileViewInteractionHub.OPTION_MENU_CREATE_FOLDER
			|| id == FileViewInteractionHub.OPTION_MENU_HIDE_SHOW
			|| id == FileViewInteractionHub.OPTION_MENU_FAV_UNFAV) {
		
			return true;
		}
		
		return false;
	}

	@Override
	public void sortCurrentList(FileSortHelper filesorthelper) {
		// TODO Auto-generated method stub
		
		refreshList();
	}
	
	private void refreshList() {
		mFileViewInteractionHub.refreshFileList();
	}
	
	private void setTextView(int resId, String s) {
		((TextView)findViewById(resId)).setText(s);
	}
	
	private static int getCategoryCountId(FileCategory category) {
		int resId = 0;
		
		switch (category) {
		case Music:
			resId = R.id.category_music_count;
			break;
		case Video:
			resId = R.id.category_video_count;
			break;
		case Picture:
			resId = R.id.category_picture_count;
			break;
		case Theme:
			resId = R.id.category_theme_count;
			break;
		case Doc:
			resId = R.id.category_document_count;
			break;
		case Zip:
			resId = R.id.category_zip_count;
			break;
		case Apk:
			resId = R.id.category_apk_count;
			break;
		case Other:
			resId = R.id.category_other_count;
			break;
		case Favorite:
			resId = R.id.category_favorite_count;
			break;
		case Applications:
			resId = R.id.category_app_count;
			break;			
		}
		
		return resId;
	}
	
	private void setCategoryCount(FileCategory category, long count) {
		int resId = getCategoryCountId(category);
		if (resId == 0) {
			return;
		}
		
		setTextView(resId, "(" + count +")");
	}
	
	private void setCategorySize(FileCategory category, long size) {
		int nameId = -1;
		int viewId = -1;
		switch (category) {
		case Music:
			nameId = R.string.category_music;
			viewId = R.id.category_legend_music;
			break;
		case Video:
			nameId = R.string.category_video;
			viewId = R.id.category_legend_video;
			break;
		case Picture:
			nameId = R.string.category_picture;
			viewId = R.id.category_legend_picture;
			break;
		case Theme:
			nameId = R.string.category_theme;
			viewId = R.id.category_legend_theme;
			break;
		case Doc:
			nameId = R.string.category_document;
			viewId = R.id.category_legend_document;
			break;
		case Zip:
			nameId = R.string.category_zip;
			viewId = R.id.category_legend_zip;
			break;
		case Apk:
			nameId = R.string.category_apk;
			viewId = R.id.category_legend_apk;
			break;
		case Other:
			nameId = R.string.category_other;
			viewId = R.id.category_legend_other;
			break;
		case Applications:
			nameId = R.string.category_applications;
			viewId = R.id.category_legend_other;
			break;			
		}
		
		if (nameId != -1 && viewId != -1) {
			String value =  Util.convertStorage(size);
			String name = getString(nameId);
			setTextView(viewId, name+":" + value);
		}
	}
	
	private void setCategoryBarValue(FileCategory category, long val) {
		if (mCategoryBar == null) {
			mCategoryBar = (CategoryBar)findViewById(R.id.category_bar); //0x7f08002f
		}
		
		int index = ((Integer)mCategoryIndex.get(category)).intValue();
		mCategoryBar.setCategoryValue(index, val);
	}
	
	public void refreshCategoryInfo() {
		SDCardInfo cardInfo = Util.getSDCardInfo();
		if (cardInfo != null) {
			mCategoryBar.setFullValue(cardInfo.total);
			String total = Util.convertStorage(cardInfo.total);
			setTextView(R.id.sd_card_capacity, total);
			
			String free = Util.convertStorage(cardInfo.free);
			setTextView(R.id.sd_card_available, free);
		}
		
		mFileCagetoryHelper.refreshCategoryInfo();
		
		long parts = 0;
		
		for(Iterator iterator = mFileCagetoryHelper.getCategoryInfos().keySet().iterator(); iterator.hasNext();) {
			FileCategory category = (FileCategory)iterator.next();
			CategoryInfo categoryInfo = (CategoryInfo) mFileCagetoryHelper.getCategoryInfos().get(category);
			setCategoryCount(category, categoryInfo.count);
			
			if (category != FileCategory.Other && category != FileCategory.Applications) {
				setCategorySize(category, categoryInfo.size);
				setCategoryBarValue(category, categoryInfo.size);
				parts += categoryInfo.size;
			}
		}
		
		if (cardInfo != null) {
			long other_size = cardInfo.total - cardInfo.free - parts;
			setCategorySize(FileCategory.Other, other_size);
			setCategoryBarValue(FileCategory.Other, other_size);
		}
		
		// favorite
		long count = mFavoriteList.getCount();
		setCategoryCount(FileCategory.Favorite, count);
		
		// app list
		count = mAppList.getCount();
		setCategoryCount(FileCategory.Applications, count);
		
		if (mCategoryBar.getVisibility() != View.VISIBLE) {
			return;
		}
		
		mCategoryBar.startAnimation();
	}

	private boolean isHomePage() {
		return (mCurViewPage == ViewPage.Home);
	}
	
	public void onBackPressed() {
		Log.v(TAG, "onBackPressed");
		if (!isHomePage() && mCurViewPage != ViewPage.NoSD) {
			mFileViewInteractionHub.onBackPressed();
		} else {
			super.onBackPressed();
		}
	}
	
	private void copyFileInFileView(ArrayList<FileInfo> arraylist) {
		if (arraylist.size() == 0)
			return;
		
		FileViewActivity activity = (FileViewActivity)ActivitiesManager.getInstance().getActivity("FileView");
		if (activity != null)
			activity.copyFile(arraylist);
		else {
			Util.setCopyMoveFlag(Util.ACTION_COPY);
			Util.setCopyMoveArrayList(arraylist);
		}
		
		Util.showTab(1);
	}
	
	private void startMoveToFileView(ArrayList<FileInfo> arraylist) {
		if (arraylist.size() == 0)
			return;
		FileViewActivity activity = (FileViewActivity)ActivitiesManager.getInstance().getActivity("FileView");
		if (activity != null) {
			activity.moveToFile(arraylist);
		} else {
			Util.setCopyMoveFlag(Util.ACTION_MOVE);
			Util.setCopyMoveArrayList(arraylist);
		}
		Util.showTab(1);
	}
	
	private class PackageReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			
			String action = intent.getAction();
			if (action.equals(Intent.ACTION_PACKAGE_ADDED)) {
				mHandler.sendEmptyMessage(MSG_REFRESH_APP); //same
			} else if (action.equals(Intent.ACTION_PACKAGE_REMOVED)) {
				mHandler.sendEmptyMessage(MSG_REFRESH_APP); //???
			} else {
				mHandler.sendEmptyMessage(MSG_REFRESH_APP); //???
			}
		}
	}
	
    /** 
     * Retrieves application's version number from the manifest 
     *  
     * @return 
     */  
    public String getVersion() {  
        String version = "0.0.0";  
          
        PackageManager packageManager = getPackageManager();  
        try {  
            PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(), 0);  
            version = packageInfo.versionName;  
        } catch (NameNotFoundException e) {  
            e.printStackTrace();  
        }  
          
        return version;  
    }

	@Override
	public void onDelete(FileInfo fileInfo) {
		// TODO Auto-generated method stub
		
		if (fileInfo != null && fileInfo.dbId != 0) {
			mFileCagetoryHelper.delete(mFileCagetoryHelper.getCurCategory() , fileInfo.dbId);
		}
	}
}
