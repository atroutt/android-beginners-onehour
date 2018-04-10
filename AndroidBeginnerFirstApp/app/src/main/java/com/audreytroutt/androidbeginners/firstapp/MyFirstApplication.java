package com.audreytroutt.androidbeginners.firstapp;

import android.app.Application;
import android.util.Log;

public class MyFirstApplication extends Application {

    private static final String TAG = "MyFirstApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        // If you want to see this log message, open "Logcat" here in Android Studio
        // TIP: open Logcat from the button in the bottom of the Android Studio window OR go to View -> Tool Windows -> Logcat OR cmd+6 on a Mac
        Log.d(TAG, "MyFirstApplication has been created!");
    }
}
