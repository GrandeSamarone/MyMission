package com.example.mymission;

import android.app.Application;

public class App extends Application {

    private static MyAppsNotificationManager notificationManager;

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = MyAppsNotificationManager.getInstance(this);
        notificationManager.registerNotificationChannelChannel(
                "123",
                "BackgroundService",
                "BackgroundService");
    }

    public static MyAppsNotificationManager getMyAppsNotificationManager(){
        return notificationManager;
    }
}
