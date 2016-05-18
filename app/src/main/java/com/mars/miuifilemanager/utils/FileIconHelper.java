package com.mars.miuifilemanager.utils;

import java.util.HashMap;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import com.mars.miuifilemanager.adapter.FileInfo;
import com.mars.miuifilemanager.utils.FileCategoryHelper.FileCategory;
import com.mars.miuifilemanager.R;

public class FileIconHelper implements FileIconLoader.IconLoadFinishListener {
	private FileIconLoader mIconLoader;
	
	private static HashMap mFileExtToIcons = new HashMap();
	
	private static HashMap mImageFrames = new HashMap();
	
	static 
	{
		String as[] = new String[1];
		as[0] = "mp3";
		addItem(as, R.drawable.file_icon_mp3); //0x7f020025
		
		String as1[] = new String[1];
		as1[0] = "wma";
		addItem(as1, R.drawable.file_icon_wma); //0x7f02002e
		
		String as2[] = new String[1];
		as2[0] = "wav";
		addItem(as2, R.drawable.file_icon_wav); //0x7f02002d
		
		String as3[] = new String[1];
		as3[0] = "mid";
		addItem(as3, R.drawable.file_icon_mid); // 0x7f020024
		String as4[] = new String[9];
		as4[0] = "mp4";
		as4[1] = "wmv";
		as4[2] = "mpeg";
		as4[3] = "m4v";
		as4[4] = "3gp";
		as4[5] = "3gpp";
		as4[6] = "3g2";
		as4[7] = "3gpp2";
		as4[8] = "asf";
		addItem(as4, R.drawable.file_icon_video); //0x7f02002c
		
		String as5[] = new String[6];
		as5[0] = "jpg";
		as5[1] = "jpeg";
		as5[2] = "gif";
		as5[3] = "png";
		as5[4] = "bmp";
		as5[5] = "wbmp";
		addItem(as5, R.drawable.file_icon_picture); //0x7f020028
		
		String as6[] = new String[5];
		as6[0] = "txt";
		as6[1] = "log";
		as6[2] = "xml";
		as6[3] = "ini";
		as6[4] = "lrc";
		addItem(as6, R.drawable.file_icon_txt); //0x7f02002b
		String as7[] = new String[6];
		
		as7[0] = "doc";
		as7[1] = "ppt";
		as7[2] = "docx";
		as7[3] = "pptx";
		as7[4] = "xsl";
		as7[5] = "xslx";
		addItem(as7, R.drawable.file_icon_office); //0x7f020026
		
		String as8[] = new String[1];
		as8[0] = "pdf";
		addItem(as8, R.drawable.file_icon_pdf); //0x7f020027
		
		String as9[] = new String[1];
		as9[0] = "zip";
		addItem(as9, R.drawable.file_icon_zip); //0x7f02002f
		
		String as10[] = new String[1];
		as10[0] = "mtz";
		addItem(as10, R.drawable.file_icon_theme); //0x7f02002a
		
		String as11[] = new String[1];
		as11[0] = "rar";
		addItem(as11, R.drawable.file_icon_rar); //0x7f020029
	}
	
	public FileIconHelper(Context context) {
		mIconLoader = new FileIconLoader(context, this);
	}

	public void setIcon(FileInfo fileinfo, ImageView fileImage, ImageView frameImage) {
		FileCategory category = FileCategoryHelper.getCategoryFromPath(fileinfo.filePath);
		
		String ext = Util.getExtFromFilename(fileinfo.filePath);
		frameImage.setVisibility(View.GONE);
			
		int resId = getFileIcon(ext);
		fileImage.setImageResource(resId);
		
		boolean ret = true;
		switch(category) {
		case Picture:
		case Video:
			ret = mIconLoader.loadIcon(fileImage, fileinfo.filePath, fileinfo.dbId, category);
			if (ret) {
				frameImage.setVisibility(View.GONE);
			} else {
				if (category == FileCategory.Picture) {
					fileImage.setImageResource(R.drawable.file_icon_picture);
				} else {
					fileImage.setImageResource(R.drawable.file_icon_video);
				}
				mImageFrames.put(fileImage, frameImage);
				
				ret = true;
			}
			break;
			
		case Apk:
			ret = mIconLoader.loadIcon(fileImage, fileinfo.filePath, fileinfo.dbId, category);			
			break;
			
		}

		if (!ret){
			fileImage.setImageResource(R.drawable.file_icon_default);
		}
	}
	
	public void onIconLoadFinished(ImageView imageView) {
		// TODO Auto-generated method stub
		
		ImageView view = (ImageView)mImageFrames.get(imageView);
		if (view == null) {
			return;
		}
		
		view.setVisibility(View.GONE); // frame Imageview?!
		mImageFrames.remove(imageView);
	}
	
	private static void addItem(String as[], int id) {
		if (as == null) {
			return;
		}
		
		for (int i = 0; i < as.length; i++) {
			mFileExtToIcons.put(as[i].toLowerCase(), id);
		}
	}
	
	public static int getFileIcon(String ext){
		Integer integer = (Integer)mFileExtToIcons.get(ext.toLowerCase());
		int i;
		if (integer != null)
			return integer.intValue();

		return R.drawable.file_icon_default; //0x7f020023
	}

}
