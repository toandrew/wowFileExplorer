package com.mars.miuifilemanager.ui;

import android.app.Activity;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.mars.miuifilemanager.R;

public class FullAdManager {
//    private Activity mActivity;
    InterstitialAd mAdView;

    public FullAdManager(Activity activity) {
//        mActivity = activity;

        MobileAds.initialize(activity.getApplicationContext(), "ca-app-pub-7879734750226076~9357339142");

        mAdView = new InterstitialAd(activity);
        mAdView.setAdUnitId("ca-app-pub-7879734750226076/7154737034");
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // Code to be executed when an ad request fails.
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when the ad is displayed.
            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when when the interstitial ad is closed.

                mAdView.loadAd(new AdRequest.Builder().build());
            }
        });
        mAdView.loadAd(new AdRequest.Builder().build());
    }

    public void onCreate() {
        if (mAdView.isLoaded()) {
            mAdView.show();
        } else {
            Log.d("TAG", "The interstitial wasn't loaded yet.");
        }
    }

    public void onResume() {
    }

    public void onPause() {
    }

    public void onDestroy() {
    }
}
