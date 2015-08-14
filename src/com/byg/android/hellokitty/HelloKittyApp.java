package com.byg.android.hellokitty;

import com.baidu.mapapi.SDKInitializer;

import android.app.Application;

public class HelloKittyApp extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		SDKInitializer.initialize(getApplicationContext());
	}
}
