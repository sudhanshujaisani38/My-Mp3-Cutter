package com.example.mymp3cutter;

import android.app.Application;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.MobileAdsInitProvider;

public class MyMp3Cutter extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		MobileAds.initialize(this,getResources().getString(R.string.google_app_id));
	}
}
