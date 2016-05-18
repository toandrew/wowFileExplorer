package com.mars.miuifilemanager.view;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import com.mars.miuifilemanager.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class CategoryBar extends View {
	private static final String TAG = "CategoryBar";
	
	private ArrayList<Category> mCategories;
	
	private long mFullValue;
	
	private Timer mTimer;
	
	public CategoryBar(Context context) {
		this(context, null);
		// TODO Auto-generated constructor stub
	}
	
	public CategoryBar(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		// TODO Auto-generated constructor stub
	}
	
	public CategoryBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		
		mCategories = new ArrayList<Category>();
	}
	
	private Drawable getDrawable(int resId) {
		return getContext().getResources().getDrawable(resId);
	}
	
	private void stepAnimation() {
		if (mTimer == null) {
			return;
		}
		
		int count = 0;
		
		for (Iterator<Category> iterator = mCategories.iterator(); iterator.hasNext();) {
			Category category = (Category)iterator.next();
			category.tmpValue += category.aniStep;
			if (category.tmpValue < category.value) {
				continue;
			}
			
			category.tmpValue = category.value;
			
			count++;
		}

		//Log.v(TAG, "stepAnimation");
		
		postInvalidate();

		if (count >= mCategories.size()) {
			mTimer.cancel();
			mTimer = null;
			
			Log.v(TAG, "Animation stopped!");
		}		
	}
	
	public void addCategory(int i) {
		Category category = new Category();
		category.resImg = i;
		mCategories.add(category);
	}
	
	protected void onDraw(Canvas canvas) {
		Drawable drawable = getDrawable(R.drawable.category_bar_empty); //0x7f020002
		
		int width = getWidth() - 24;

		int left = 12;
		int right = width + left;
		int bottom = drawable.getIntrinsicHeight();
		
		Rect rect = new Rect(left, 0, right, bottom);
		drawable.setBounds(rect);
		drawable.draw(canvas);
		
		// draw categories
		if (mFullValue != 0) {
			for (Iterator<Category> iterator = mCategories.iterator();iterator.hasNext();) {
				Category category = (Category)iterator.next();
				
				long val = category.value;
				if (mTimer != null) {
					val = category.tmpValue;
				}

				int step = (int) (((long)width * val)/mFullValue);
				//Log.v(TAG,"category.value[" + category.value + "]category.tmpValue[" + category.tmpValue + "]val[" + val+ "]step[" + step + "]");
				
				if (step != 0) {
					rect.left = left;
					rect.right = left + step;
					
					Drawable img = getDrawable(category.resImg);
					rect.bottom = rect.top + img.getIntrinsicHeight();
					img.setBounds(rect);
					img.draw(canvas);
					
					left += step;
				}
			}
		}
		
		rect.left = 0;
		rect.right = rect.left + getWidth();
		
		Drawable mask = getDrawable(R.drawable.category_bar_mask); // 0x7f020003
		mask.setBounds(rect);
		mask.draw(canvas);
	}
	
	public boolean setCategoryValue(int i, long val) {
		if (i < 0 || i >= mCategories.size()) {
			return false;
		}
		
		mCategories.get(i).value = val;
		invalidate();
		
		return true;
	}
	
	public void setFullValue(long val) {
		mFullValue = val;
	}
	
	public void startAnimation() {
		if (mTimer != null) {
			return;
		}
		
		Log.v(TAG, "startAnimation!");
		
		for (Iterator<Category> iterator = mCategories.iterator(); iterator.hasNext();) {
			Category category = (Category)iterator.next();
			category.tmpValue = 0;
			category.aniStep = category.value/10L;
		}
		
		TimerTask timeTask = new TimerTask(){
			public void run() {
				// TODO Auto-generated method stub
				
				stepAnimation();
			}
		};
		
		mTimer = new Timer();
		mTimer.scheduleAtFixedRate(timeTask, 0, 100);
	}
	
	private class Category{
		public long aniStep;
		public int resImg;
		public long tmpValue;
		public long value;
	}
}