package com.example.audiotest;


import static com.example.audiotest.ApplicationClass.ACTION_CLOSE;
import static com.example.audiotest.ApplicationClass.ACTION_NEXT;
import static com.example.audiotest.ApplicationClass.ACTION_PAUSE;
import static com.example.audiotest.ApplicationClass.ACTION_PLAY;
import static com.example.audiotest.ApplicationClass.ACTION_PREVIOUS;
import static com.example.audiotest.ApplicationClass.CHANNNEL_ID_2;
import static com.example.audiotest.PlayerActivity.listOfSongs;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.ArrayList;

import wseemann.media.FFmpegMediaMetadataRetriever;

public class MusicService extends Service implements MediaPlayer.OnCompletionListener {

    IBinder mBinder=new MyBinder();
    MediaPlayer mediaPlayer;
    ArrayList<MusicFiles> serviceMusicFiles=new ArrayList<>();
    int position=0;
    Uri uri;
    String sender;
    ActionPlaying actionPlaying;
    MediaSessionCompat mediaSessionCompat;
    String TAG="MUSIC_SERVICE+LOGS";

    @Override
    public void onCreate() {
        super.onCreate();
        mediaSessionCompat=new MediaSessionCompat(getBaseContext()," my media audio");

    }
    void nextButtonClicked(){
        if(actionPlaying!=null){
            actionPlaying.nextButtonClicked();
        }
    }
    void prevviuosButtonClicked(){
        if(actionPlaying!=null){
            actionPlaying.previousButtonCliked();
        }
    }
    void playpauseClicked(){
        if(actionPlaying!=null){
            actionPlaying.playPauseBUttonClicked();
        }
    }
    void start(){
        mediaPlayer.start();
    }
    void pause(){
        mediaPlayer.pause();
    }
    boolean isPlaying(){ return  mediaPlayer.isPlaying(); }
    void stop(){ mediaPlayer.stop();}
    void release(){
        mediaPlayer.release();
    }
    int getDuration(){
        return mediaPlayer.getDuration();
    }
    void seekTo(int postion){
        mediaPlayer.seekTo(postion);
    }

    void createMediaPlayer(Uri uriInner){
        mediaPlayer=MediaPlayer.create(getApplicationContext(),uriInner);
    }
    int getCurrentPosition(){
        return mediaPlayer.getCurrentPosition();
    }
    void OnCompleted(){
        mediaPlayer.setOnCompletionListener(this);
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if(actionPlaying!=null){
            actionPlaying.nextButtonClicked();
        }
    }




    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    public class MyBinder extends Binder{
        MusicService getService(){
              return MusicService.this;
          }
      }
    void setCallback(ActionPlaying actionPlaying){
        this.actionPlaying=actionPlaying;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int sentPosition=intent.getIntExtra("position",0);
        position=sentPosition;
        //        Uri sentUri=Uri.parse(intent.getStringExtra("uri"));
        sender=intent.getStringExtra("sender");
        String actionname=intent.getStringExtra("actionname");
        uri=Uri.parse(listOfSongs.get(sentPosition).getPath());

        Log.e(TAG,"start position in service :"+sentPosition);
        Log.e(TAG,"start uri in service :"+uri);
        Log.e(TAG,"start sender in service :"+sender);
        Log.e(TAG,"notification acton clicked :"+actionname);

        if(actionname!=null){
            switch (actionname){
                case "playpause":
                    Toast.makeText(this, "playpause", Toast.LENGTH_SHORT).show();
                    if(actionPlaying!=null){
                        actionPlaying.playPauseBUttonClicked();
                    }
                    break;
                case "next":
                    Toast.makeText(this, "next", Toast.LENGTH_SHORT).show();
                    if(actionPlaying!=null){
                        actionPlaying.nextButtonClicked();
                    }else{
                        Log.e(TAG,"next clicked but action playing empty");
                    }
                    break;
                case "previous":
                    Toast.makeText(this, "previous", Toast.LENGTH_SHORT).show();
                    if(actionPlaying!=null){
                        actionPlaying.previousButtonCliked();
                    }
                    break;
                case "close":
                    Toast.makeText(this, "stopping song", Toast.LENGTH_SHORT).show();
                    if(!mediaPlayer.isPlaying()){
                        stopForeground(true);
                        stopSelf();
                    }
                    else{
                        stopForeground(true);
                        stopSelf();
                    }

                    break;
            }
        }
if(sender.contains("notification")){
    Log.e(TAG,"plaing frm notification");
}

else{
    playMedia(uri);
}

        return START_STICKY;

    }
    private void prepForMiniplayer(Uri sentUri) {
        if(mediaPlayer!=null){
            mediaPlayer.stop();
            mediaPlayer.release();
            if(listOfSongs!=null){
                mediaPlayer=MediaPlayer.create(getApplicationContext(),sentUri);
            }
           }
        else{
            mediaPlayer=MediaPlayer.create(getApplicationContext(),sentUri);
        }
    }

    private void playMedia(Uri sentUri) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            createMediaPlayer(sentUri);
            mediaPlayer.start();
            showNotification(R.drawable.ic_baseline_pause_24,position,uri);
        }else{
            createMediaPlayer(sentUri);
            mediaPlayer.start();
            showNotification(R.drawable.ic_baseline_pause_24,position,uri);
        }

    }

    private void notificationPlayPause(){
        if(mediaPlayer!=null){
            if(mediaPlayer.isPlaying()){
                pause();
            }else{
                start();
            }
        }
    }

    public void showNotification(int playPauseBtn,int position,Uri uri){
        Log.e(TAG,"position inside show notification "+position);
        Intent intent=new Intent(this,PlayerActivity.class);
        PendingIntent contentIntent=PendingIntent.getActivity(this,0,intent,0);

        Intent prevIntent=new Intent(this,NotificationReciever.class).setAction(ACTION_PREVIOUS);
        PendingIntent prevPending=PendingIntent.getBroadcast(this,0,
                prevIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        Intent nextIntent=new Intent(this,NotificationReciever.class).setAction(ACTION_NEXT);
        PendingIntent nextPending=PendingIntent.getBroadcast(this,0,
                nextIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        Intent playIntent=new Intent(this,NotificationReciever.class).setAction(ACTION_PLAY);
        PendingIntent playPending=PendingIntent.getBroadcast(this,0,
                playIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        Intent pauseIntent=new Intent(this,NotificationReciever.class).setAction(ACTION_PAUSE);
        PendingIntent pausePending=PendingIntent.getBroadcast(this,0,
                pauseIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        Intent closeIntent=new Intent(this,NotificationReciever.class).setAction(ACTION_CLOSE);
        PendingIntent closePending=PendingIntent.getBroadcast(this,0,
                closeIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        byte[] picture=null;
        Bitmap thumb=null;

        picture=getPic(uri.toString());
        if(picture!=null){
            thumb= BitmapFactory.decodeByteArray(picture,0,picture.length);
        }
        else{
            thumb=BitmapFactory.decodeResource(getResources(),R.drawable.musicicon);
        }
        Intent resultIntent = new Intent(this, PlayerActivity.class);
        resultIntent.putExtra( "sender" , "wholenotification" ) ;
        resultIntent.putExtra("position",position);
        resultIntent.putExtra("currentposition",getCurrentPosition());
        resultIntent.addCategory(Intent. CATEGORY_LAUNCHER ) ;
        resultIntent.setAction(Intent. ACTION_MAIN ) ;
        resultIntent.setFlags(Intent. FLAG_ACTIVITY_CLEAR_TOP | Intent. FLAG_ACTIVITY_SINGLE_TOP ) ;
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification=new NotificationCompat.Builder(this,CHANNNEL_ID_2).
                setSmallIcon(playPauseBtn)
                .setLargeIcon(thumb)
                .setContentTitle(listOfSongs.get(position).getTitle())
                .setContentText(listOfSongs.get(position).getArtist())
                .addAction(R.drawable.ic_baseline_skip_previous_24,"prevoius",prevPending)
                .addAction(playPauseBtn,"playpause",playPending)
                .addAction(R.drawable.ic_baseline_skip_next_24,"next",nextPending)
                .addAction(R.drawable.ic_baseline_close_24,"close",closePending)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()

                        .setMediaSession(mediaSessionCompat.getSessionToken())
                        .setShowActionsInCompactView(0,1,2,4 /* #1: pause button */)

                )
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOnlyAlertOnce(true)
                .setAutoCancel(true)
                .setContentIntent(resultPendingIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build();
        startForeground(2,notification);
    }
    public byte[] getPic(String uri){
        FFmpegMediaMetadataRetriever mmr = new FFmpegMediaMetadataRetriever();
        mmr.setDataSource(uri);
        byte [] art = mmr.getEmbeddedPicture();
        return art;
    }
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stop();
        release();
        stopSelf();
        Toast.makeText(getApplicationContext(), "Service disconnected", Toast.LENGTH_SHORT).show();
    }


}
