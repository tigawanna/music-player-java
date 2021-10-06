package com.example.audiotest;

import static com.example.audiotest.ApplicationClass.ACTION_CLOSE;
import static com.example.audiotest.ApplicationClass.ACTION_NEXT;
import static com.example.audiotest.ApplicationClass.ACTION_PLAY;
import static com.example.audiotest.ApplicationClass.ACTION_PREVIOUS;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NotificationReciever extends BroadcastReceiver {
    String TAG="NOTIFICATION_RECIEVER_LOGS";
    @Override
    public void onReceive(Context context, Intent intent) {
  String actionName=intent.getAction();
  Intent serviceIntent=new Intent(context,MusicService.class);
        Log.e(TAG,"notification acton clicked :"+actionName);
  if(actionName!=null){
      switch(actionName){
          case ACTION_PLAY:
              serviceIntent.putExtra("sender","notification");
              serviceIntent.putExtra("actionname","playpause");
              context.startService(serviceIntent);
           break;
          case ACTION_NEXT:
              serviceIntent.putExtra("sender","notification");
              serviceIntent.putExtra("actionname","next");
              context.startService(serviceIntent);
              break;
          case ACTION_PREVIOUS:
              serviceIntent.putExtra("sender","notification");
              serviceIntent.putExtra("actionname","previous");
              context.startService(serviceIntent);
              break;
          case ACTION_CLOSE:
//              Log.e("action close","closing action close service");
              serviceIntent.putExtra("sender","notification");
              serviceIntent.putExtra("actionname","close");
              context.startService(serviceIntent);
              break;
      }
  }
    }
}
