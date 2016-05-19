package com.mars.miuifilemanager.ui;

import android.app.Activity;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.AdListener;
import com.mars.miuifilemanager.R;

import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import android.view.ViewGroup;

public class AdManager {
    private Activity mActivity;
    AdView mAdView;

    public AdManager(Activity activity) {
        mActivity = activity;


        ViewGroup contentView = (ViewGroup)activity.findViewById(R.id.file_browse_list);
        mAdView = new AdView(activity);
        mAdView.setAdUnitId("ca-app-pub-7879734750226076/3171204749");
        mAdView.setAdSize(AdSize.BANNER);
        mAdView.setBackgroundResource(R.drawable.list_item_background_normal);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        contentView.addView(mAdView, params);
    }

    public void onCreate() {
        if (mAdView != null) {
            mAdView.loadAd(new AdRequest.Builder().build());
        }
    }

    public void onResume() {
        if (mAdView != null) {
            mAdView.resume();
        }
    }

    public void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
    }

    public void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
    }
}
