package com.ramlaxmaninnovation;

import android.app.Application;
import android.content.Context;

public class App  extends Application {
    private static Context appContext;
    private static App instance;

    public static App getInstance() {
        return instance;
    }

    public static Context getContext() {
        return appContext;
    }

    public void setAppContext(Context mAppContext) {
        appContext = mAppContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        this.setAppContext(getApplicationContext());
    }

}