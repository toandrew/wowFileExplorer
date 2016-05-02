package com.mars.miuifilemanager.adapter;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.mars.miuifilemanager.ui.FileViewInteractionHub;
import com.mars.miuifilemanager.ui.FileViewInteractionHub.Mode;
import com.mars.miuifilemanager.utils.FileIconHelper;
import com.mars.miuifilemanager.utils.Util;

import com.mars.miuifilemanager.R;

public class FileListItem extends LinearLayout {
	OnClickListener mCheckClick;
	
	private Context mContext;
	
	FileViewInteractionHub mFileViewInteractionHub;
	
    //FavoriteDatabaseHelper favDbHelper = FavoriteDatabaseHelper.getInstance();

	public FileListItem(Context context) {
		super(context);

		init(context);
	}

	public FileListItem(Context context, AttributeSet attributeset) {
		super(context, attributeset);
		
		init(context);
	}
	
	public final void bind(FileInfo fileinfo, FileViewInteractionHub fileviewinteractionhub, FileIconHelper fileiconhelper) {
		mFileViewInteractionHub = fileviewinteractionhub;
		if (mFileViewInteractionHub.isMoveState()) {
			fileinfo.Selected = mFileViewInteractionHub.isFileSelected(fileinfo.filePath);
		}
		ImageView imageView = (ImageView)findViewById(R.id.file_checkbox); //0x7f08000b
		if (mFileViewInteractionHub.getMode() == Mode.Pick) {
			imageView.setVisibility(View.GONE);
		} else {
			if (mFileViewInteractionHub.canShowCheckBox()) {
				imageView.setVisibility(View.VISIBLE);
			} else {
				imageView.setVisibility(View.GONE);
			}
			
			if (fileinfo.Selected) {
				imageView.setImageResource(R.drawable.btn_check_on);
			} else {
				imageView.setImageResource(R.drawable.btn_check_off);
			}
			
			imageView.setTag(fileinfo);

			View view = findViewById(R.id.file_checkbox_area);
			view.setOnClickListener(mCheckClick);
			
			setSelected(fileinfo.Selected);
		}
		
		Util.setText(this, R.id.file_name, fileinfo.fileName);
		
		String fileCount;
		if (fileinfo.IsDir) {
			fileCount = "(" + fileinfo.Count + ")";
		} else {
			fileCount = "";
		}
		Util.setText(this, R.id.file_count, fileCount);
		
		Util.setText(this, R.id.modified_time, Util.formatDateString(mContext, fileinfo.ModifiedDate));
		if (!fileinfo.IsDir) {
			Util.setText(this, R.id.file_size, Util.convertStorage(fileinfo.fileSize));
		} else {
			Util.setText(this, R.id.file_size, "");
		}
		
		ImageView fileImageFrame = (ImageView)findViewById(R.id.file_image_frame);
		ImageView fileImage = (ImageView)findViewById(R.id.file_image);
		if (fileinfo.IsDir) {
			fileImageFrame.setVisibility(View.GONE);
			fileImage.setImageResource(R.drawable.folder);
		} else {
			fileiconhelper.setIcon(fileinfo, fileImage, fileImageFrame);
		}
	}

	private void init(Context context) {
		mCheckClick = new OnClickListener(){
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ImageView imageView = (ImageView)findViewById(R.id.file_checkbox); //0x7f08000b
				if (imageView == null || imageView.getTag() == null) {
					return;
				}
				
				FileInfo fileInfo = (FileInfo)imageView.getTag(); 
				fileInfo.Selected = !fileInfo.Selected;
				
				if (mFileViewInteractionHub.onCheckItem(fileInfo, v)) {
					if (fileInfo.Selected) {
						imageView.setImageResource(R.drawable.btn_check_on);
					} else {
						imageView.setImageResource(R.drawable.btn_check_off);
					}
					
					return;
				}
			}
		};
		
		mContext = context;
	}
}
