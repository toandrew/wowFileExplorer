package com.mars.miuifilemanager;

import android.Manifest;
import android.app.Activity;
import android.app.LocalActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.mars.miuifilemanager.ui.FileCategoryActivity;
import com.mars.miuifilemanager.ui.FileViewActivity;
import com.mars.miuifilemanager.ui.ServerControlActivity;
import com.mars.miuifilemanager.utils.ActivitiesManager;
import com.mars.miuifilemanager.utils.PermissionHelper;
import com.mars.miuifilemanager.view.TabActivity;
import com.mars.miuifilemanager.view.TabHost;
import com.mars.miuifilemanager.view.TabHost.OnTabChangeListener;

import java.util.ArrayList;
import java.util.List;

public class FileExplorerTabActivity extends TabActivity {
    private static final String TAG = "FileExplorerTabActivity";

    TabHost mTabHost = null;

    private int[] mTabNames = {R.string.tab_category, R.string.tab_sd,
            R.string.tab_remote};

    public final Context context = FileExplorerTabActivity.this;

    private ViewPager mPager = null;

    List<View> mListViews;

    LocalActivityManager mManager = null;

    private static final int REQUEST_READ_STORAGE_CODE = 100;
    PermissionHelper mPermissionHelper;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.e(TAG, "onCreate!");

        setContentView(R.layout.file_explorer_tabhost);

        ActivitiesManager.getInstance().registerActivity("FileExplorerTab",
                this);

        mManager = new LocalActivityManager(this, true);
        mManager.dispatchCreate(savedInstanceState);

        mTabHost = getTabHost();

        mPermissionHelper = new PermissionHelper(this, new PermissionHelper.PermissionListener() {

            @Override
            public void haveAllPerms(int requestCode) {
                initViews();
            }

            @Override
            public void notAllPerms(int requestCode, String... perms) {
                Toast.makeText(FileExplorerTabActivity.this.getApplicationContext(), R.string.noperm, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void notShowDialog(int requestCode, String... perms) {
                Toast.makeText(FileExplorerTabActivity.this.getApplicationContext(), R.string.noperm, Toast.LENGTH_SHORT).show();
            }
        });


        if (Build.VERSION.SDK_INT >= 23) {
            mPermissionHelper.getPermission(REQUEST_READ_STORAGE_CODE, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE});
        } else {
            initViews();
        }

//		PushAgent mPushAgent = PushAgent.getInstance(context);
//		mPushAgent.enable();
//
//		PushAgent.getInstance(context).onAppStart();
    }

    private void initViews() {
        int i = getIntent().getIntExtra("TAB", 0);
        if (i < 0 || i > 2) {
            i = 0;
        }

        setTabHostAppearance(i);
    }

    private void InitViewPager() {
        mPager = findViewById(R.id.viewpager);

        mPager.setAdapter(new MyPageAdapter(mListViews));

        mPager.setCurrentItem(0);
        mPager.setOffscreenPageLimit(3);

        mPager.setOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                //当viewPager发生改变时，同mTabHost时改变tabhost上面的currentTab
                mTabHost.setCurrentTab(position);

                View v0 = mListViews.get(0);
                View v1 = mListViews.get(1);
                View v2 = mListViews.get(2);
                Log.e(TAG, "BEFORE v0[" + v0.getVisibility() + "]v1[" + v1.getVisibility() + "]v2[" + v2.getVisibility() + "]");
                if (v0.getVisibility() != View.VISIBLE) {
                    v0.setVisibility(View.VISIBLE);
                }
                if (v1.getVisibility() != View.VISIBLE) {
                    v1.setVisibility(View.VISIBLE);
                }

                if (v2.getVisibility() != View.VISIBLE) {
                    v2.setVisibility(View.VISIBLE);
                }

                Log.e(TAG, "AFTER v0[" + v0.getVisibility() + "]v1[" + v1.getVisibility() + "]v2[" + v2.getVisibility() + "]");
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
                //mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
                //mAdapter.notifyDataSetChanged();
            }
        });

        mTabHost.setOnTabChangedListener(new OnTabChangeListener() {

            @Override
            public void onTabChanged(String tabId) {
                // TODO Auto-generated method stub

                Log.e(TAG, "tabId:" + tabId);
                if (tabId.equals(getString(mTabNames[0]))) {
                    mPager.setCurrentItem(0);
                } else if (tabId.equals(getString(mTabNames[1]))) {
                    mPager.setCurrentItem(1);
                } else if (tabId.equals(getString(mTabNames[2]))) {
                    mPager.setCurrentItem(2);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        mManager.dispatchResume();

        Log.e(TAG, "onResume: cat:" + mManager.getCurrentId());
    }

    @Override
    protected void onPause() {
        super.onPause();
        mManager.dispatchPause(isFinishing());
        Log.e(TAG, "onPause");
    }

    private View getView(String id, Intent intent) {
        //intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return mManager.startActivity(id, intent).getDecorView();
    }

    MyPageAdapter mAdapter;

    private void setTabHostAppearance(int index) {
        mTabHost = getTabHost();

        Intent i1 = new Intent(this, FileCategoryActivity.class);
        //i1.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        Intent i2 = new Intent(this, FileViewActivity.class);
        //i2.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        Intent i3 = new Intent(this, ServerControlActivity.class);
        //i2.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        mTabHost.addTab(mTabHost.newTabSpec(getString(mTabNames[0])).setIndicator(
                getString(mTabNames[0])).setContent(i1));
        mTabHost.addTab(mTabHost.newTabSpec(getString(mTabNames[1])).setIndicator(
                getString(mTabNames[1])).setContent(i2));
        mTabHost.addTab(mTabHost.newTabSpec(getString(mTabNames[2])).setIndicator(
                getString(mTabNames[2])).setContent(i3));

        mListViews = new ArrayList<View>();
        View View2 = getView(getString(mTabNames[2]), i3);
        View View1 = getView(getString(mTabNames[1]), i2);
        View View0 = getView(getString(mTabNames[0]), i1);

        mListViews.add(View0);
        mListViews.add(View1);
        mListViews.add(View2);

        InitViewPager();

        mTabHost.setup();
        mTabHost.setup(mManager);

        mTabHost.setCurrentTab(index);
    }

    public void setCurrentTab(int i) {
        if (i < 0 || i > 2) {
            return;
        }

        mTabHost.setCurrentTab(i);
    }

    public void showTabWidget(int val) {
        mTabHost.getTabWidget().setVisibility(val);
    }

    protected void onStop() {
        super.onStop();
        mManager.dispatchStop();
        Log.e(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mManager.dispatchDestroy(isFinishing());

        /*
         * if (FavoriteDatabaseHelper.getInstance() != null) {
         * FavoriteDatabaseHelper.getInstance().close(); }
         */

        Log.v(TAG, "onDestroy");
        ActivitiesManager.getInstance().unRegisterActivity("FileExplorerTab");

        /*
         * if (FileScanService.isRunning()) { Context context =
         * getApplicationContext(); Intent intent = new Intent(context,
         * FileScanService.class); context.stopService(intent); }
         */
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    private class MyPageAdapter extends PagerAdapter {

        private List<View> list;

        private MyPageAdapter(List<View> list) {
            this.list = list;
        }

        @Override
        public void destroyItem(View view, int position, Object arg2) {
            ViewPager pViewPager = ((ViewPager) view);
            pViewPager.removeView(list.get(position));
            Log.e(TAG, "destroyItem:" + position);
        }

        @Override
        public void finishUpdate(View arg0) {
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object instantiateItem(View view, int position) {
            ViewPager pViewPager = ((ViewPager) view);
            View v = list.get(position);
            v.setVisibility(View.VISIBLE);
            pViewPager.addView(v);
            Log.e(TAG, "instantiateItem:" + position);
            return list.get(position);
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public void restoreState(Parcelable arg0, ClassLoader arg1) {
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public void startUpdate(View arg0) {
        }

        public int getItemPosition(Object object) {
            return POSITION_UNCHANGED;
        }
    }

    public void onBackPressed() {
        Log.e(TAG, "onBackPressed:" + mManager.getCurrentId());
        Activity currentActivity = mManager.getCurrentActivity();
        if (currentActivity != null) {
            currentActivity.onBackPressed();
        } else {
            Log.w(TAG, "currentActivity is null!");
            super.onBackPressed();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mPermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
