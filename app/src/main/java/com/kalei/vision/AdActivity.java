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

import com.mopub.common.MoPub;
import com.mopub.common.SdkConfiguration;
import com.mopub.common.SdkInitializationListener;
import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubInterstitial;
import com.mopub.mobileads.MoPubInterstitial.InterstitialAdListener;

import android.content.Intent;
import android.os.Bundle;

public class AdActivity extends AppCompatActivity implements InterstitialAdListener {
    InterstitialAd mInterstitialAd;
    private MoPubInterstitial mInterstitial;
    private static boolean moPubInitialized = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad);

        mopubstuff();
//        googlestuff();

    }

    private SdkInitializationListener initSdkListener() {
        return new SdkInitializationListener() {
            @Override
            public void onInitializationFinished() {
                moPubInitialized = true;
                loadInterstitial();
            }
        };
    }

    private void loadInterstitial() {

        if (moPubInitialized) {
            mInterstitial = new MoPubInterstitial(AdActivity.this, "3979621b5b9d492d8bb69530a00e6abf");
            mInterstitial.setInterstitialAdListener(AdActivity.this);
            mInterstitial.load();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadInterstitial();
    }

    private void mopubstuff() {
        SdkConfiguration sdkConfiguration = new SdkConfiguration.Builder("3979621b5b9d492d8bb69530a00e6abf")
//                .withMediationSettings("MEDIATION_SETTINGS")
//                .withNetworksToInit(networksToInit)
                .build();

        MoPub.initializeSdk(this, sdkConfiguration, initSdkListener());
    }

    private void googlestuff() {
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

    @Override
    protected void onDestroy() {
        if (mInterstitial != null) {
            mInterstitial.destroy();
        }
        super.onDestroy();
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

    @Override
    public void onInterstitialLoaded(final MoPubInterstitial interstitial) {
        if (mInterstitial.isReady()) {
            mInterstitial.show();
        }
    }

    @Override
    public void onInterstitialFailed(final MoPubInterstitial interstitial, final MoPubErrorCode errorCode) {
        int x = 9;
        gotoActivity();
    }

    @Override
    public void onInterstitialShown(final MoPubInterstitial interstitial) {
        int x = 9;
    }

    @Override
    public void onInterstitialClicked(final MoPubInterstitial interstitial) {
        int x = 9;
    }

    @Override
    public void onInterstitialDismissed(final MoPubInterstitial interstitial) {
        gotoActivity();
    }
}
