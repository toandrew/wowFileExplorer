package com.mars.miuifilemanager.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mars.miuifilemanager.adapter.FileInfo;
import com.mars.miuifilemanager.ui.FileViewActivity.SelectFilesCallback;
import com.mars.miuifilemanager.utils.FavoriteDatabaseHelper;
import com.mars.miuifilemanager.utils.FileIconHelper;
import com.mars.miuifilemanager.utils.FileOperationHelper;
import com.mars.miuifilemanager.utils.FileSortHelper;
import com.mars.miuifilemanager.utils.FileSortHelper.SortMethod;
import com.mars.miuifilemanager.utils.IntentBuilder;
import com.mars.miuifilemanager.utils.Settings;
import com.mars.miuifilemanager.utils.Util;
import com.mars.miuifilemanager.view.InformationDialog;
import com.mars.miuifilemanager.view.TextInputDialog;
import com.mars.miuifilemanager.view.TextInputDialog.OnFinishListener;
import com.mars.miuifilemanager.R;

public class FileViewInteractionHub implements
		FileOperationHelper.IOperationProgressListener {
	private static final String TAG = "FileViewInteractionHub";

	private static final int MENU_ID_FAV = 100;
	public static final int MENU_ID_COPY = 101;
	public static final int MENU_ID_CUT = 102;
	private static final int MENU_ID_SEND = 103;
	private static final int MENU_ID_RENAME = 104;
	private static final int MENU_ID_DEL = 105;
	private static final int MENU_ID_INFO = 106;

	private static final int MENU_ID_UNFAV = 107;
	private static final int MENU_ID_PASTE = 108;

	public static final int OPTION_MENU_SORT = 200;
	private static final int OPTION_MENU_SORT_NAME = 201;
	private static final int OPTION_MENU_SORT_SIZE = 202;
	private static final int OPTION_MENU_SORT_DATE = 203;
	private static final int OPTION_MENU_SORT_TYPE = 204;

	public static final int OPTION_MENU_REFRESH = 210;
	public static final int OPTION_MENU_SEL_UNSEL = 220;
	public static final int OPTION_MENU_CREATE_FOLDER = 230;
	public static final int OPTION_MENU_FAV_UNFAV = 240;
	public static final int OPTION_MENU_HIDE_SHOW = 250;

	public static enum Mode {
		View, Pick
	};

	private ArrayList<FileInfo> mCheckedFileNameList;

	OnClickListener mButtonClick;

	OnClickListener mNavigationClick;

	OnCreateContextMenuListener mListViewContextMenuListener;

	OnMenuItemClickListener mMenuItemClick;

	private IFileInteractionListener mFileViewListener;

	private FileOperationHelper mFileOperationHelper;

	private FileSortHelper mFileSortHelper;

	private String mCurrentPath;

	private String mRoot;

	private int mListViewContextMenuSelectedItem;

	private Mode mCurrentMode;

	private View mNavigationBar;

	private TextView mNavigationBarText;

	private ImageView mNavigationBarUpDownArrow;

	private View mDropdownNavigation;

	private ListView mFileListView;

	private View mOperationBar;

	private View mConfirmOperationBar;

	private static final Rect ARROW_INDICATOR_MARGIN = new Rect(0, 0, 0, 0);

	private SelectFilesCallback mSelectFilesCallback;

	private ProgressDialog mProgressDialog;

	private View mScanButtonView;

	public FileViewInteractionHub(IFileInteractionListener listener) {
		mCheckedFileNameList = new ArrayList();

		mButtonClick = new OnClickListener() {
			public void onClick(View arg0) {
				// TODO Auto-generated method stub

				if (mFileViewListener.onOperation(arg0.getId())) {
					mOperationBar.setVisibility(View.GONE);
					return;
				}

				switch (arg0.getId()) {
				case R.id.path_pane_up_level:
					onOperationUpLevel();
					break;
				case R.id.current_path_pane:
					Log.v(TAG, "mButtonClick:navigation_bar");
					onNavigationBarClick();
					break;
				case R.id.button_operation_cancel:
					Log.v(TAG, "operation cancel!!!!");
					onOperationSelectAllOrCancel();
					break;
				case R.id.button_moving_confirm:
					onOperationButtonConfirm();
					break;
				case R.id.button_moving_cancel:
					onOperationButtonCancel();
					break;
				case R.id.button_operation_copy:
					onOperationCopy();
					break;
				case R.id.button_operation_delete:
					onOperationDelete();
					break;
				case R.id.button_operation_install:
					break;
				case R.id.button_operation_move:
					onOperationMove();
					break;
				case R.id.button_operation_send:
					onOperationSend();
					break;
				}

			}
		};

		mNavigationClick = new OnClickListener() {
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String path = (String) v.getTag();
				if (path == null) {
					return;
				}

				showDropdownNavigation(false);

				if (mFileViewListener.onNavigation(path)) {
					return;
				}

				mCurrentPath = path;

				refreshFileList();
			}
		};

		mListViewContextMenuListener = new OnCreateContextMenuListener() {
			public void onCreateContextMenu(ContextMenu arg0, View arg1,
					ContextMenuInfo arg2) {
				// TODO Auto-generated method stub

				if (isInSelection()) {
					return;
				}

				if (isMoveState()) {
					return;
				}

				showDropdownNavigation(false);

				AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) arg2;

				FavoriteDatabaseHelper favDbHelper = FavoriteDatabaseHelper
						.getInstance();

				int pos = menuInfo.position;

				FileInfo fileInfo = mFileViewListener.getItem(pos);
				if (fileInfo == null) {
					Log.e(TAG, "fileInfo is null!");
					String absPath = Environment.getExternalStorageDirectory()
							.getAbsolutePath();
					onFileChanged(absPath);
					return;
				}

				if (favDbHelper != null && fileInfo != null) {
					if (favDbHelper.isFavorite(fileInfo.filePath)) {
						addMenuItem(arg0, MENU_ID_UNFAV, 0,
								R.string.operation_unfavorite);
					} else {
						addMenuItem(arg0, MENU_ID_FAV, 0,
								R.string.operation_favorite);
					}
				}

				arg0.setHeaderTitle(fileInfo.fileName);
				if (fileInfo.IsDir) {
					arg0.setHeaderIcon(R.drawable.folder);
				} else {
					String ext = Util.getExtFromFilename(fileInfo.filePath);
					int resId = FileIconHelper.getFileIcon(ext);
					arg0.setHeaderIcon(resId);
				}

				addMenuItem(arg0, MENU_ID_COPY, 0, R.string.operation_copy);
				addMenuItem(arg0, MENU_ID_CUT, 0, R.string.operation_move);
				addMenuItem(arg0, MENU_ID_SEND, 0, R.string.operation_send);
				addMenuItem(arg0, MENU_ID_RENAME, 0, R.string.operation_rename);
				addMenuItem(arg0, MENU_ID_DEL, 0, R.string.operation_delete);
				addMenuItem(arg0, MENU_ID_INFO, 0, R.string.operation_info);

				if (canPaste()) {
					return;
				}
				// do others ???
			}
		};

		mMenuItemClick = new OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem menuInfo) {
				// TODO Auto-generated method stub

				AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo
						.getMenuInfo();
				int pos = -1;
				if (info != null) {
					pos = info.position;
				}

				mListViewContextMenuSelectedItem = pos;

				if (mFileViewListener.onOperation(menuInfo.getItemId())) {
					return true;
				}

				switch (menuInfo.getItemId()) {
				case MENU_ID_UNFAV:
				case MENU_ID_FAV:
				case OPTION_MENU_FAV_UNFAV:
					onOperationFavorite();
					break;
				case MENU_ID_PASTE:
					onOperationPaste();
					break;
				case MENU_ID_COPY:
					onOperationCopy();
					break;
				case MENU_ID_CUT:
					onOperationMove();
					break;
				case MENU_ID_SEND:
					onOperationSend();
					break;
				case MENU_ID_RENAME:
					onOperationRename();
					break;
				case MENU_ID_DEL:
					onOperationDelete();
					break;
				case MENU_ID_INFO:
					onOperationInfo();
					break;

				case OPTION_MENU_REFRESH:
					onOperationReferesh();
					break;

				case OPTION_MENU_HIDE_SHOW:
					onOperationShowSysFiles();
					break;

				case OPTION_MENU_CREATE_FOLDER:
					onOperationCreateFolder();
					break;

				case OPTION_MENU_SEL_UNSEL:
					onOperationSelectAllOrCancel();
					break;

				case OPTION_MENU_SORT_NAME:
					menuInfo.setChecked(true);
					onSortChanged(SortMethod.name);
					break;
				case OPTION_MENU_SORT_SIZE:
					menuInfo.setChecked(true);
					onSortChanged(SortMethod.size);
					break;
				case OPTION_MENU_SORT_DATE:
					menuInfo.setChecked(true);
					onSortChanged(SortMethod.date);
					break;
				case OPTION_MENU_SORT_TYPE:
					menuInfo.setChecked(true);
					onSortChanged(SortMethod.type);
					break;
				}

				return true;
			}

		};

		mFileViewListener = listener;

		setup();

		mFileOperationHelper = new FileOperationHelper(this);

		mFileSortHelper = new FileSortHelper();
	}

	public void onFileChanged(String path) {
		// TODO Auto-generated method stub

		notifyFileSystemChanged(path);
	}

	public void onFinish() {
		// TODO Auto-generated method stub

		if (mProgressDialog != null) {
			try {
				mProgressDialog.dismiss();
			} catch (Exception e) {
			}

			mProgressDialog = null;
		}

		mFileViewListener.runOnUiThread(new Runnable() {
			public void run() {
				// TODO Auto-generated method stub

				mOperationBar.setVisibility(View.GONE);
				showConfirmOperationBar(false);
				clearSelection();
				refreshFileList();
			}
		});
	}

	private void setup() {
		setupNaivgationBar();
		setupFileListView();
		setupOperationPane();
		
		//setupScanButton();
	}

	private void setupNaivgationBar() {
		mNavigationBar = mFileViewListener.getViewById(R.id.navigation_bar); // 0x7f08000d
		mNavigationBarText = (TextView) mFileViewListener
				.getViewById(R.id.current_path_view); // 0x7f080010
		mNavigationBarUpDownArrow = (ImageView) mFileViewListener
				.getViewById(R.id.path_pane_arrow); // 0x7f080011

		View view = mFileViewListener.getViewById(R.id.operation_bar); // 0x7f08000e
		view.setOnClickListener(mButtonClick);

		mDropdownNavigation = mFileViewListener
				.getViewById(R.id.dropdown_navigation); // 0x7f080016

		setupClick(mNavigationBar, R.id.path_pane_up_level); // 0x7f080012

		setupClick(mNavigationBar, R.id.current_path_pane); // 0x7f080012??
	}

	private void setupFileListView() {
		mFileListView = (ListView) mFileViewListener
				.getViewById(R.id.file_path_list); // 0x7f080013
		mFileListView.setLongClickable(true);
		mFileListView
				.setOnCreateContextMenuListener(mListViewContextMenuListener);
		mFileListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub

				Log.v(TAG, "mFileListView:onItemClick!!!");

				onListItemClick(arg0, arg1, arg2, arg3);
			}
		});
	}

	/*
	private void setupScanButton() {
		mScanButtonView = mFileViewListener.getViewById(R.id.scan_button);
		if (mScanButtonView != null) {
			mScanButtonView.setOnClickListener(new OnClickListener(){
				public void onClick(View v) {
					// TODO Auto-generated method stub
					
					Context context = getContext();
					Intent intent = new Intent(context, FileScanService.class);
					if (!FileScanService.isRunning()) {
						Log.v(TAG,"start File Scan Service!!!");
						context.startService(intent);
						return;
					}
					
					Log.v(TAG,"stop File Scan Service!!!");
					
					context.stopService(intent);
				}
			});
			
			TextView scanButotnTextView = (TextView) mScanButtonView.findViewById(R.id.scan_button_text);
			if (scanButotnTextView != null) {
				if (true) {
					if (FileScanService.isRunning()) {
						scanButotnTextView.setText(R.string.stop_scanning_files);
						scanButotnTextView.setCompoundDrawablesWithIntrinsicBounds(
								R.drawable.scanning_icon, 0, 0, 0);
						scanButotnTextView.setTextColor(R.color.black);
					} else {
						scanButotnTextView.setText(R.string.scan_files);
						scanButotnTextView.setCompoundDrawablesWithIntrinsicBounds(
								R.drawable.scan_icon, 0, 0, 0);
						scanButotnTextView.setTextColor(R.color.red);
					}
				}
			}
		}
	}
	*/
	
	private void setupOperationPane() {

		mOperationBar = mFileViewListener.getViewById(R.id.operation_bar); //0x7f08003b
		setupClick(mOperationBar, R.id.button_operation_delete);
		setupClick(mOperationBar, R.id.button_operation_install);
		setupClick(mOperationBar, R.id.button_operation_copy);
		setupClick(mOperationBar, R.id.button_operation_move);
		setupClick(mOperationBar, R.id.button_operation_send);
		setupClick(mOperationBar, R.id.button_operation_cancel);
		
		/*
		ScreenView screenview = (ScreenView)mOperationBar;
		Rect rect = ARROW_INDICATOR_MARGIN;
		screenview.setArrowIndicatorMarginRect(rect);
		screenview.setOverScrollRatio(0);
		*/
		
		mConfirmOperationBar = mFileViewListener.getViewById(R.id.moving_operation_bar);
		setupClick(mConfirmOperationBar, R.id.button_moving_confirm); //0x7f080039
		setupClick(mConfirmOperationBar, R.id.button_moving_cancel); //0x7f08003a
	}

	private void addMenuItem(Menu menu, int itemId, int order, int titleRes) {
		addMenuItem(menu, itemId, order, titleRes, -1);
	}

	private void addMenuItem(Menu menu, int itemId, int order, int titleRes,
			int iconRes) {
		if (mFileViewListener.shouldHideMenu(itemId)) {
			return;
		}

		MenuItem item = menu.add(0, itemId, order, titleRes);
		item.setOnMenuItemClickListener(mMenuItemClick);

		if (iconRes > 0) {
			item.setIcon(iconRes);
		}
	}

	private boolean doCreateFolder(String folder) {
		if (TextUtils.isEmpty(folder)) {
			return false;
		}

		if (!mFileOperationHelper.createFolder(mCurrentPath, folder)) {
			Context context = getContext();
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			String msg = getContext().getString(R.string.fail_to_create_folder);
			builder.setMessage(msg).setPositiveButton(R.string.confirm, null)
					.create().show();

			return false;
		}

		FileInfo fileInfo = Util.getFileInfo(Util
				.makePath(mCurrentPath, folder));
		mFileViewListener.addSingleFile(fileInfo);
		int i = mFileListView.getCount() - 1;
		mFileListView.setSelection(i);

		return true;
	}

	private void doOperationDelete(final ArrayList<FileInfo> selectedFileList) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setMessage(getContext().getString(
				R.string.operation_delete_confirm_message));
		builder.setPositiveButton(R.string.confirm,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub

						if (!mFileOperationHelper.Delete(selectedFileList)) {
							return;
						}

						showProgress(getContext().getString(
								R.string.operation_deleting));
					}
				});

		builder.setNegativeButton(R.string.cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub

						clearSelection();
					}
				});
		builder.create().show();
	}

	private boolean doRename(FileInfo fileInfo, String path) {
		if (TextUtils.isEmpty(path)) {
			return false;
		}

		if (!mFileOperationHelper.Rename(fileInfo, path)) {

			AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
			String msg = getContext().getString(R.string.fail_to_rename); // 0x7f060013
			builder.setMessage(msg);
			builder.setPositiveButton(R.string.confirm, null);
			builder.create().show();

			return false;
		}

		fileInfo.fileName = path;
		// mFileViewListener.onDataChanged();

		return true;
	}

	private String getAbsoluteName(String path, String name) {
		String absName;

		if (path.equals("/")) {
			absName = path + name;
		} else {
			absName = path + File.separator + name;
		}

		return absName;
	}

	private Context getContext() {
		return mFileViewListener.getContext();
	}

	private boolean isSelectedAll() {
		if (mFileViewListener.getItemCount() == 0) {
			return false;
		}

		if (mCheckedFileNameList.size() == mFileViewListener.getItemCount()) {
			return true;
		}

		return false;
	}

	private boolean isSelectingFiles() {
		return (mSelectFilesCallback != null ? true : false);
	}

	private void notifyFileSystemChanged(String fileName) {
		if (fileName == null) {
			return;
		}

		Intent intent;
		if ((new File(fileName)).isDirectory()) {
			intent = new Intent(Intent.ACTION_MEDIA_MOUNTED);
			Uri uri = Uri.fromFile(Environment.getExternalStorageDirectory());
			intent.setData(uri);
			Log.v(TAG, "directory changed, send broadcast:" + intent.toString());
		} else {
			intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
			Uri uri1 = Uri.fromFile(new File(fileName));
			intent.setData(uri1);
			Log.v(TAG, "file changed, send broadcast:" + intent.toString());
		}
		try {
			getContext().sendBroadcast(intent);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void onOperationCopy() {
		onOperationCopy(getSelectedFileList());
	}

	private void onOperationFavorite() {
		String path = mCurrentPath;
		if (mListViewContextMenuSelectedItem != -1) {
			path = mFileViewListener.getItem(mListViewContextMenuSelectedItem).filePath;
		}

		onOperationFavorite(path);
	}

	private void onOperationFavorite(String path) {
		FavoriteDatabaseHelper favDbHelper = FavoriteDatabaseHelper
				.getInstance();
		if (favDbHelper == null) {
			return;
		}

		int toastResId;
		if (favDbHelper.isFavorite(path)) {
			favDbHelper.delete(path);

			toastResId = R.string.removed_favorite;
		} else {
			String fileName = Util.getNameFromFilepath(path);
			favDbHelper.insert(fileName, path);
			toastResId = R.string.added_favorite;
		}

		Toast.makeText(mFileViewListener.getContext(), toastResId,
				Toast.LENGTH_SHORT).show();
	}

	private void onOperationMove() {
		mFileOperationHelper.startMove(getSelectedFileList());
		clearSelection();
		showConfirmOperationBar(true);
		mConfirmOperationBar.findViewById(R.id.button_moving_confirm)
				.setEnabled(false); // 0x7f080039
		refreshFileList();
	}

	private void onOperationPaste() {
		if (!mFileOperationHelper.Paste(mCurrentPath)) {
			return;
		}

		showProgress(getContext().getString(R.string.operation_pasting));
	}

	private void onOperationReferesh() {
		refreshFileList();
	}

	private void onOperationSelectAllOrCancel() {
		if (isSelectedAll()) {
			clearSelection();
			mOperationBar.setVisibility(View.GONE);
		} else {
			mCheckedFileNameList.clear();
			for (Iterator<FileInfo> iterator = mFileViewListener.getAllFiles().iterator(); iterator
					.hasNext();) {
				FileInfo fileInfo = (FileInfo) iterator.next();
				fileInfo.Selected = true;
				mCheckedFileNameList.add(fileInfo);
			}

			mFileViewListener.onDataChanged();
			mOperationBar.setVisibility(View.VISIBLE);
		}

		updateOperationButtonStatus();
	}

	private void onOperationShowSysFiles() {
		Settings settings = Settings.instance();
		boolean flag = false;
		if (!Settings.instance().getShowDotAndHiddenFiles()) {
			flag = true;
		}
		settings.setShowDotAndHiddenFiles(flag);
		refreshFileList();
	}

	private void setupClick(View view, int id) {

		View v = null;
		if (view != null) {
			v = view.findViewById(id);
		} else {
			v = mFileViewListener.getViewById(id);
		}

		if (v != null) {
			v.setOnClickListener(mButtonClick);
		}
	}

	private void showConfirmOperationBar(boolean flag) {
		mConfirmOperationBar.setVisibility(flag ? View.VISIBLE : View.GONE);
	}

	private void showDropdownNavigation(boolean flag) {
		mDropdownNavigation.setVisibility(flag ? View.VISIBLE : View.GONE);
		if (mDropdownNavigation.getVisibility() == View.VISIBLE) {
			mNavigationBarUpDownArrow.setImageResource(R.drawable.arrow_up);
		} else {
			mNavigationBarUpDownArrow.setImageResource(R.drawable.arrow_down);
		}
	}

	private void showProgress(String s) {
		if (mProgressDialog != null) {
			try {
				mProgressDialog.dismiss();
			} catch (Exception e) {
			}
		}

		Context context = mFileViewListener.getContext();
		mProgressDialog = new ProgressDialog(context);
		;
		mProgressDialog.setMessage(s);
		mProgressDialog.setIndeterminate(true);
		mProgressDialog.setCancelable(false);
		mProgressDialog.show();
	}

	private void updateConfirmButtons() {
		if (mConfirmOperationBar.getVisibility() == View.GONE) {
			return;
		}
		int resId = R.string.operation_paste;

		Button button = (Button) mConfirmOperationBar
				.findViewById(R.id.button_moving_confirm); // 0x7f080039
		if (!isSelectingFiles()) {
			if (isMoveState()) {
				boolean canMove = mFileOperationHelper.canMove(mCurrentPath);
				button.setEnabled(canMove);
			}
		} else {
			button.setEnabled(mCheckedFileNameList.size() != 0);

			resId = R.string.operation_move;
		}

		button.setText(resId);
	}

	private void updateMenuItems(Menu menu) {
		MenuItem menuitem = menu.findItem(OPTION_MENU_SEL_UNSEL);
		if (menuitem != null) {
			if (isSelectedAll()) {
				menuitem.setTitle(R.string.operation_cancel_selectall);
			} else {
				menuitem.setTitle(R.string.operation_selectall);
			}

			if (mCurrentMode == Mode.Pick) {
				menuitem.setEnabled(false);
			} else {
				menuitem.setEnabled(true);
			}
		}

		menuitem = menu.findItem(OPTION_MENU_HIDE_SHOW);
		if (menuitem != null) {
			if (Settings.instance().getShowDotAndHiddenFiles()) {
				menuitem.setTitle(R.string.operation_hide_sys);
			} else {
				menuitem.setTitle(R.string.operation_show_sys);
			}

			return;
		}

		FavoriteDatabaseHelper favDbHelper = FavoriteDatabaseHelper
				.getInstance();
		if (favDbHelper == null) {
			return;
		}

		menuitem = menu.findItem(OPTION_MENU_FAV_UNFAV);
		if (menuitem == null) {
			return;
		}

		if (favDbHelper.isFavorite(mCurrentPath)) {
			menuitem.setTitle(R.string.operation_unfavorite);
		} else {
			menuitem.setTitle(R.string.operation_favorite);
		}
	}

	private void updateNavigationPane() {
		View view = mFileViewListener.getViewById(R.id.path_pane_up_level); // 0x7f080012
		if (mCurrentPath.equals(mRoot)) {
			view.setVisibility(View.INVISIBLE);

			mNavigationBarUpDownArrow.setVisibility(View.GONE);
		} else {
			view.setVisibility(View.VISIBLE);

			mNavigationBarUpDownArrow.setVisibility(View.VISIBLE);
		}

		String path = mFileViewListener.getDisplayPath(mCurrentPath);
		mNavigationBarText.setText(path);
	}

	private void updateOperationButtonStatus() {
		TextView textView = (TextView) mOperationBar
				.findViewById(R.id.button_operation_cancel);
		if (textView == null) {
			return;
		}

		if (isSelectedAll()) {
			textView.setText(R.string.operation_cancel);
			textView.setCompoundDrawablesWithIntrinsicBounds(0,
					R.drawable.operation_button_cancel, 0, 0);
		} else {
			textView.setText(R.string.operation_selectall);
			textView.setCompoundDrawablesWithIntrinsicBounds(0,
					R.drawable.operation_button_selectall, 0, 0);
		}
	}

	private void viewFile(FileInfo fileInfo) {
		try {
			IntentBuilder.viewFile(mFileViewListener.getContext(),
					fileInfo.filePath);
		} catch (ActivityNotFoundException e) {
			Log.e(TAG, "Fail to view file:" + e.toString());
			Toast.makeText(mFileViewListener.getContext(), R.string.failed_open,
					Toast.LENGTH_SHORT).show();
		}
	}

	public boolean canPaste() {
		return mFileOperationHelper.canPaste();
	}

	public boolean canShowCheckBox() {
		return mConfirmOperationBar.getVisibility() != View.VISIBLE;
	}

	public void clearSelection() {
		if (mCheckedFileNameList.size() <= 0) {
			return;
		}

		for (Iterator<FileInfo> iterator = mCheckedFileNameList.iterator(); iterator
				.hasNext();) {
			FileInfo fileInfo = (FileInfo) iterator.next();
			if (fileInfo != null)
				fileInfo.Selected = false;
		}

		mCheckedFileNameList.clear();
		mFileViewListener.onDataChanged();
	}

	public String getCurrentPath() {
		return mCurrentPath;
	}

	public FileInfo getItem(int i) {
		return mFileViewListener.getItem(i);
	}

	public String getRootPath() {
		return mRoot;
	}

	public ArrayList<FileInfo> getSelectedFileList() {
		if (mCheckedFileNameList.size() == 0) {
			if (mListViewContextMenuSelectedItem != -1) {
				FileInfo fileinfo = mFileViewListener
						.getItem(mListViewContextMenuSelectedItem);
				if (fileinfo != null)
					mCheckedFileNameList.add(fileinfo);
			}
		}
		return mCheckedFileNameList;
	}

	public boolean isFileSelected(String s) {
		return mFileOperationHelper.isFileSelected(s);
	}

	public boolean isInSelection() {
		if (mCheckedFileNameList.size() > 0) {
			return true;
		}

		return false;
	}

	public boolean isMoveState() {
		if (mFileOperationHelper.isMoveState()
				|| mFileOperationHelper.canPaste()) {
			return true;
		}

		return false;
	}

	public void moveFileFrom(ArrayList<FileInfo> arraylist) {
		mFileOperationHelper.startMove(arraylist);
		mOperationBar.setVisibility(View.GONE);
		showConfirmOperationBar(true);
		updateConfirmButtons();
		refreshFileList();
	}

	public boolean onBackPressed() {
		if (mDropdownNavigation.getVisibility() == View.VISIBLE) {
			mDropdownNavigation.setVisibility(View.GONE);
		} else {
			if (isInSelection()) {
				clearSelection();

				if (mOperationBar != null) {
					mOperationBar.setVisibility(View.GONE);
				}
			} else {
				if (!onOperationUpLevel()) {
					return false;
				}
			}
		}

		return true;
	}

	public boolean onCheckItem(FileInfo fileInfo, View view) {
		if (isMoveState()) {
			return false;
		}

		if (isSelectingFiles() && fileInfo.IsDir) {
			return false;
		}

		if (fileInfo.Selected) {
			mCheckedFileNameList.add(fileInfo);
		} else {
			mCheckedFileNameList.remove(fileInfo);
		}

		/*
		 * if (!isSelectingFiles()) { return false; }
		 */

		updateConfirmButtons();

		if (mFileListView == null) {
			return true;
		}
		
		int i = mFileListView.getPositionForView(view);

		int j = mFileListView.getFirstVisiblePosition();

		int k = mFileListView.getLastVisiblePosition();

		if (mCheckedFileNameList.size() != 0) {
			mOperationBar.setVisibility(View.VISIBLE);
		} else {
			mOperationBar.setVisibility(View.GONE);
		}

		updateOperationButtonStatus();

		if (Math.abs(i - k) <= 1 && mCheckedFileNameList.size() != 0) {
			int i1 = j + 2;
			int j1 = Math.abs(i - k);
			final int adjustedPosition = i1 - j1;
			if (adjustedPosition < k) {
				mFileListView.post(new Runnable() {
					public void run() {
						// TODO Auto-generated method stub

						mFileListView.setSelection(adjustedPosition);
					}
				});
			}
		}

		return true;
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		clearSelection();
		mOperationBar.setVisibility(View.GONE);

		showDropdownNavigation(false);
		addMenuItem(menu, OPTION_MENU_SEL_UNSEL, 0,
				R.string.operation_selectall, R.drawable.ic_menu_select_all); // 16
		SubMenu submenu = menu.addSubMenu(0, OPTION_MENU_SORT, 1,
				R.string.menu_item_sort).setIcon(R.drawable.ic_menu_sort); // 3
		addMenuItem(submenu, OPTION_MENU_SORT_NAME, 0,
				R.string.menu_item_sort_name);// 11
		addMenuItem(submenu, OPTION_MENU_SORT_SIZE, 1,
				R.string.menu_item_sort_size);// 12
		addMenuItem(submenu, OPTION_MENU_SORT_DATE, 2,
				R.string.menu_item_sort_date);// 13
		addMenuItem(submenu, OPTION_MENU_SORT_TYPE, 3,
				R.string.menu_item_sort_type);// 14
		submenu.setGroupCheckable(0, true, true);
		SortMethod sortMethod = mFileSortHelper.getSortMethod();
		switch (sortMethod) {
		case name:
			submenu.getItem(0).setChecked(true);
			break;
		case size:
			submenu.getItem(1).setChecked(true);
			break;
		case date:
			submenu.getItem(2).setChecked(true);
			break;
		case type:
			submenu.getItem(3).setChecked(true);
			break;
		}

		addMenuItem(menu, OPTION_MENU_CREATE_FOLDER, 3,
				R.string.operation_create_folder, R.drawable.ic_menu_new_folder); // 100
		addMenuItem(menu, OPTION_MENU_FAV_UNFAV, 4,
				R.string.operation_favorite, R.drawable.ic_menu_delete_favorite); // 101
		addMenuItem(menu, OPTION_MENU_HIDE_SHOW, 5,
				R.string.operation_show_sys, R.drawable.ic_menu_show_sys); // 117
		addMenuItem(menu, OPTION_MENU_REFRESH, 6, R.string.operation_refresh,
				R.drawable.ic_menu_refresh); // 15
		return true;
	}

	public void onListItemClick(AdapterView adapterview, View view, int i,
			long l) {
		FileInfo fileInfo = mFileViewListener.getItem(i);
		showDropdownNavigation(false);

		if (fileInfo == null) {
			Log.e(TAG, "File does not exist on position:" + i);
			return;
		}

		if (!fileInfo.IsDir) {
			if (mCurrentMode == Mode.Pick) {
				mFileViewListener.onPick(fileInfo);
			} else {
				viewFile(fileInfo);
			}
		} else {
			String path = getAbsoluteName(mCurrentPath, fileInfo.fileName);

			mCurrentPath = path;

			refreshFileList();
		}

	}

	protected void onNavigationBarClick() {
		if (mDropdownNavigation.getVisibility() == View.VISIBLE) {
			showDropdownNavigation(false);
			return;
		}

		LinearLayout linearlayout = (LinearLayout) mDropdownNavigation
				.findViewById(R.id.dropdown_navigation_list);// 0x7f080017
		linearlayout.removeAllViews();

		String path = mFileViewListener.getDisplayPath(mCurrentPath);

		int idx = 0;
		int start = 0;
		int padding = 0;
		boolean first = true;
		while ((idx = path.indexOf('/', start)) != -1) {
			View view = LayoutInflater.from(mFileViewListener.getContext())
					.inflate(R.layout.dropdown_item, null);// 0x7f030000
			view.findViewById(R.id.list_item).setPadding(padding, 0, 0, 0); // 0x7f080000

			padding += 20;

			ImageView imageView = (ImageView) view.findViewById(R.id.item_icon); // 0x7f080001
			if (first) {
				first = false;

				imageView.setImageResource(R.drawable.dropdown_icon_root); // 0x7f02001e
			} else {
				imageView.setImageResource(R.drawable.dropdown_icon_folder); // 0x7f02001d
			}

			TextView textView = (TextView) view.findViewById(R.id.path_name); // 0x7f080002
			String name = path.substring(start, idx);
			textView.setText(name);

			view.setOnClickListener(mNavigationClick);

			String startName = path.substring(0, idx);
			String realPath = mFileViewListener.getRealPath(startName);
			view.setTag(realPath);

			start = idx + 1;

			linearlayout.addView(view);
		}

		if (linearlayout.getChildCount() <= 0) {
			return;
		}

		showDropdownNavigation(true);
	}

	public void onOperationButtonCancel() {
		mFileOperationHelper.clear();
		showConfirmOperationBar(false);
		if (isSelectingFiles()) {
			mSelectFilesCallback.selected(null);
			mSelectFilesCallback = null;
			clearSelection();
			return;
		}

		if (mFileOperationHelper.isMoveState()) {
			mFileOperationHelper.endMove(null);

		}

		refreshFileList();
	}

	public void onOperationButtonConfirm() {
		if (isSelectingFiles()) {
			mSelectFilesCallback.selected(mCheckedFileNameList);
			mSelectFilesCallback = null;
			clearSelection();

			return;
		}

		if (mFileOperationHelper.isMoveState()) {
			if (!mFileOperationHelper.endMove(mCurrentPath)) {
				return;
			}

			showProgress(getContext().getString(R.string.operation_moving));
		} else {
			onOperationPaste();
		}
	}

	public void onOperationCopy(ArrayList arraylist) {
		mFileOperationHelper.Copy(arraylist);
		mOperationBar.setVisibility(View.GONE);
		clearSelection();
		showConfirmOperationBar(true);
		mConfirmOperationBar.findViewById(R.id.button_moving_confirm)
				.setEnabled(false);// 0x7f080039
		refreshFileList();
	}

	public void onOperationCreateFolder() {
		String title = getContext().getString(R.string.operation_create_folder);
		String msg = getContext().getString(
				R.string.operation_create_folder_message);
		String inputText = getContext().getString(R.string.new_folder_name);

		(new TextInputDialog(getContext(), title, msg, inputText,
				new TextInputDialog.OnFinishListener() {
					public boolean onFinish(String path) {
						return doCreateFolder(path);
					}

					public boolean onCancel() {
						// TODO Auto-generated method stub
						return false;
					}
				})).show();
	}

	private void onOperationDelete() {
		ArrayList<FileInfo> arraylist = getSelectedFileList();
		doOperationDelete(arraylist);
	}

	public void onOperationInfo() {
		mOperationBar.setVisibility(View.GONE);
		if (getSelectedFileList().size() == 0) {
			return;
		}

		FileInfo fileInfo = (FileInfo) getSelectedFileList().get(0);
		if (fileInfo == null) {
			return;
		}

		Context context = mFileViewListener.getContext();
		FileIconHelper fileiconhelper = mFileViewListener.getFileIconHelper();
		(new InformationDialog(context, fileInfo, fileiconhelper)).show();

		clearSelection();
	}

	public void onOperationRename() {
		if (mListViewContextMenuSelectedItem == -1
				|| getSelectedFileList().size() == 0) {
			return;
		}

		final FileInfo fileInfo = (FileInfo) getSelectedFileList().get(0);

		String title = getContext().getString(R.string.operation_rename);
		String msg = getContext().getString(R.string.operation_rename_message);
		String inputText = fileInfo.fileName;
		(new TextInputDialog(getContext(), title, msg, inputText,
				new OnFinishListener() {
					public boolean onFinish(String path) {
						// TODO Auto-generated method stub

						boolean ret = doRename(fileInfo, path);

						clearSelection();

						return ret;
					}

					public boolean onCancel() {
						// TODO Auto-generated method stub

						clearSelection();

						return true;
					}
				})).show();
	}

	public void onOperationSearch() {
		// To do
	}

	public void onOperationScan() {
		// To do
	}

	public void onOperationSend() {
		// to do

		ArrayList<FileInfo> arraylist = getSelectedFileList();
		for (Iterator<FileInfo> iterator = arraylist.iterator(); iterator
				.hasNext();) {
			if (((FileInfo) iterator.next()).IsDir) {
				Context context = mFileViewListener.getContext();
				(new android.app.AlertDialog.Builder(context))
						.setMessage(R.string.error_info_cant_send_folder)
						.setPositiveButton(R.string.confirm, null).create()
						.show();

				clearSelection();
				mOperationBar.setVisibility(View.GONE);
				return;
			}
		}

		Intent intent = IntentBuilder.buildSendFile(arraylist);
		if (intent != null) {
			try {
				mFileViewListener.startActivity(intent);
			} catch (ActivityNotFoundException e) {
				Log.e(TAG, "fail to view file:" + e.toString());
				Toast.makeText(mFileViewListener.getContext(), R.string.failed_send,
						Toast.LENGTH_SHORT).show();
			}
		}

		clearSelection();
		mOperationBar.setVisibility(View.GONE);
	}

	public boolean onOperationUpLevel() {
		showDropdownNavigation(false);
		if (mFileViewListener.onOperation(3)) {
			return false;
		}

		if (mCurrentPath.equals(mRoot)) {
			return false;
		}

		String parent = (new File(mCurrentPath)).getParent();
		mCurrentPath = parent;
		refreshFileList();

		return true;
	}

	public boolean onPrepareOptionsMenu(Menu menu) {
		updateMenuItems(menu);
		return true;
	}

	public void onSortChanged(FileSortHelper.SortMethod sortmethod) {
		if (mFileSortHelper.getSortMethod() != sortmethod) {
			mFileSortHelper.setSortMethog(sortmethod);
			sortCurrentList();
		}
	}

	public void refreshFileList() {
		clearSelection();

		if (mOperationBar != null) {
			mOperationBar.setVisibility(View.GONE);
		}

		updateNavigationPane();

		mFileViewListener.onRefreshFileList(mCurrentPath, mFileSortHelper);

		updateConfirmButtons();
	}

	public void setCurrentPath(String path) {
		mCurrentPath = path;
	}

	public void setMode(Mode mode) {
		mCurrentMode = mode;
	}

	public void setRootPath(String path) {
		mRoot = path;
		mCurrentPath = path;
	}

	public void showOperationBar(boolean flag) {
		mOperationBar.setVisibility(flag ? View.VISIBLE : View.GONE);
	}

	public void sortCurrentList() {
		mFileViewListener.sortCurrentList(mFileSortHelper);
	}

	public Mode getMode() {
		return mCurrentMode;
	}

	@Override
	public void onDelete(FileInfo fileInfo) {
		// TODO Auto-generated method stub
		
		mFileViewListener.onDelete(fileInfo);
	}
}
