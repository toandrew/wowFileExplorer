package com.mars.miuifilemanager.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.TextView;

public class MiTextView extends TextView{
	private int mCurrentBottom;
	ViewUpdater mViewUpdater;
	
	public MiTextView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public MiTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public MiTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public void setViewdateListener(ViewUpdater updater) {
		mViewUpdater = updater;
	}
	
	protected void onDraw(Canvas canvas) {
		int bottom = getBottom();
		if (mCurrentBottom == bottom) {
			super.onDraw(canvas);
			return;
		}
		
		mCurrentBottom = getBottom();
		
		if (mViewUpdater == null) {
			super.onDraw(canvas);
			return;
		}
		
		mViewUpdater.onUpdate(mCurrentBottom);
		super.onDraw(canvas);
	}
}
