package com.yflink.virtualscreentranslation;

import android.app.Application;
import android.content.Intent;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Intent intent = new Intent();
        intent.setClass(this, VideoTranslationService.class);
        startService(intent);
    }
}
