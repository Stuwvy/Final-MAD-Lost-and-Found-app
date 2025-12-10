package com.example.back2me;

import android.app.Application;

public class Back2MeApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize the AuthData shared preference utility
        AuthData.initialize(this);
    }
}