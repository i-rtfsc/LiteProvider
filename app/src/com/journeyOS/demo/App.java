package com.journeyOS.demo;

import android.app.Application;

import com.journeyOS.liteprovider.globals.GlobalsManager;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        GlobalsManager.initialize(this);
    }
}
