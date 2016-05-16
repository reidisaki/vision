package com.kalei.vision;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import android.content.Intent;
import android.os.Bundle;

public class AdActivity extends AppCompatActivity {
    InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad);
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.interstitial));

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                mInterstitialAd.show();
            }

            @Override
            public void onAdFailedToLoad(final int errorCode) {
                super.onAdFailedToLoad(errorCode);
                gotoActivity();
            }

            @Override
            public void onAdClosed() {
                gotoActivity();
            }
        });

        requestNewInterstitial();
    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("SEE_YOUR_LOGCAT_TO_GET_YOUR_DEVICE_ID")
                .build();

        mInterstitialAd.loadAd(adRequest);
    }

    private void gotoActivity() {
        startActivity(new Intent(AdActivity.this, MainActivity.class));
    }
}
