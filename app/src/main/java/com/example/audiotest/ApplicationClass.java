package com.example.audiotest;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class ApplicationClass extends Application {

    public static final String CHANNNEL_ID_1="channel1";
    public static final String CHANNNEL_ID_2="channel2";
    public static final String ACTION_PREVIOUS="actionprevious";
    public static final String ACTION_NEXT="actionnext";
    public static final String ACTION_PLAY="actionplay";
    public static final String ACTION_PAUSE="actionpause";
    public static final String ACTION_CLOSE="actionclose";

    @Override
    public void onCreate() {
        super.onCreate();
        createNOtificationChannel();
    }

    private void createNOtificationChannel() {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            NotificationChannel channel1=
                    new NotificationChannel(CHANNNEL_ID_1,"Channel(1)",
                            NotificationManager.IMPORTANCE_HIGH
                    );
            NotificationChannel channel2=
                    new NotificationChannel(CHANNNEL_ID_2,"Channel(2)",
                            NotificationManager.IMPORTANCE_LOW
                    );

            NotificationManager notificationManager=getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel1);
            notificationManager.createNotificationChannel(channel2);
        }
    }
}
