package com.telnyx.videodemo;

import android.app.Application;

import com.google.android.datatransport.backend.cct.BuildConfig;

import timber.log.Timber;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Timber.plant(new Timber.DebugTree());

    }
}