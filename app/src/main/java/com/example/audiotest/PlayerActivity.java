package com.example.audiotest;



import static android.content.ContentValues.TAG;
import static com.example.audiotest.MainActivity.musicFiles;

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.palette.graphics.Palette;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import wseemann.media.FFmpegMediaMetadataRetriever;

public class PlayerActivity extends AppCompatActivity implements  ServiceConnection,ActionPlaying,
        MediaPlayer.OnCompletionListener {
TextView playinfSong,songName,artistName,timeElapsed,totalTime,songNumber;
ImageView backButtob,menuButton,albumArt,repeatButton,previousButton,nextButton,shuffleButton;
SeekBar seekbar;
RelativeLayout mcontainer,buttonslayout,timeslayout;
LinearLayout namesLayout;
FloatingActionButton playPause;

int statusBarColor= Color.rgb(0,0,0);
int position=-1;
String sender;
static int playingPosition;

static ArrayList<MusicFiles> listOfSongs=new ArrayList<>();
Uri uri;
static boolean isShuffling=false,isReapeating=false;
static Palette.Swatch swatch,bgswatch;

private Handler handler=new Handler();
private Thread playPauseThread,previousThread,nextThread;

String TAG="PLAYER_ACTIVITY_LOGS";
//MediaPlayer mediaPlayer;
MusicService musicService;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        getSupportActionBar().hide();
        changestatusBarColor();
        initViews();
        getIntentMethod();
        seekbarStuu();
        shuffleReaoeat();
        PlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(musicService!=null){
                    int mCurrentposition=musicService.getCurrentPosition()/1000;
//                    Log.e("m current position", String.valueOf(mCurrentposition));
                    seekbar.setProgress(mCurrentposition);
                    timeElapsed.setText(formattedText(mCurrentposition));
                    playingPosition = musicService.getCurrentPosition();
                }
                handler.postDelayed(this,1000);
            }

        });
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu=new PopupMenu(getApplicationContext(),view);
                popupMenu.getMenuInflater().inflate(R.menu.songoptions,popupMenu.getMenu());
                popupMenu.show();
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()){
                            case R.id.delete_song:
                                Toast.makeText(getApplicationContext(), "deleting...", Toast.LENGTH_SHORT).show();
                                deleteSong(position);
                                break;
                        }
                        return true;
                    }
                });
            }
        });

    }

    private void deleteSong(int position) {
        Uri contentUri= ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                Long.parseLong(listOfSongs.get(position).getId()));
        File file=new File(listOfSongs.get(position).getPath());
        boolean deleted=file.delete();
        if(deleted){
            getApplicationContext().getContentResolver().delete(contentUri,null,null);
            Toast.makeText(getApplicationContext(),
                    listOfSongs.get(position).getTitle()+" was deleted", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(getApplicationContext(),"deletion failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void shuffleReaoeat() {
        shuffleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isShuffling){
                    isShuffling=false;
                shuffleButton.setImageResource(R.drawable.ic_baseline_shuffle_24);
                }
                else{
                    isShuffling=true;
                    isReapeating = false;
                    repeatButton.setImageResource(R.drawable.ic_baseline_repeat_24);
                    shuffleButton.setImageResource(R.drawable.ic_baseline_shuffle_active);
                }
            }
        });
        repeatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isReapeating) {
                    isReapeating = false;
                    repeatButton.setImageResource(R.drawable.ic_baseline_repeat_24);
                }
                else{
                    isReapeating = true;
                    isShuffling=false;
                    shuffleButton.setImageResource(R.drawable.ic_baseline_shuffle_24);
                    repeatButton.setImageResource(R.drawable.ic_baseline_repeat_active);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        Intent intent =new Intent(this,MusicService.class);
        bindService(intent,this,BIND_AUTO_CREATE);
        playPauseThreadbtn();
        nextThreadbtn();
        previousThreadbtn();
        super.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        unbindService(this);
    }



    private void nextThreadbtn() {
        nextThread=new Thread(){
            @Override
            public void run() {
                super.run();

                nextButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                   nextButtonClicked();
                    }
                });
            }
        };
        nextThread.start();
    }

    public void nextButtonClicked() {
        if(isShuffling&&!isReapeating){
            position=randomInex(listOfSongs.size()-1);
//          Toast.makeText(getApplicationContext(),"shuffle on",Toast.LENGTH_SHORT).show();
        }
        else if(!isReapeating&&!isShuffling){
            Log.e(TAG,"next position "+((position+1)%listOfSongs.size()));
            position=((position+1)%listOfSongs.size());
//            Toast.makeText(getApplicationContext(),"regular playback",Toast.LENGTH_SHORT).show();
        }
        String yuri=listOfSongs.get(position).getPath();
        uri=Uri.parse(yuri);
        Log.e("skipping to ", String.valueOf(position));
        if(musicService!=null){
            musicService.stop();
            musicService.release();
            metaData(uri);
            setTexts(position);
            musicService.createMediaPlayer(uri);
            musicService.start();
            playPause.setImageResource(R.drawable.ic_baseline_pause_24);
            musicService.showNotification(R.drawable.ic_baseline_pause_24,position,uri);
            musicService.OnCompleted();
        }
        else{
            metaData(uri);
            setTexts(position);
            musicService.start();
            playPause.setImageResource(R.drawable.ic_baseline_pause_24);
            musicService.showNotification(R.drawable.ic_baseline_pause_24,position,uri);
            musicService.OnCompleted();
        }
    }

    private int randomInex(int size) {
        Random random=new Random();
        Log.e("random index", String.valueOf(random.nextInt(size+1)));
        return random.nextInt(size+1);
    }

    private void previousThreadbtn() {
        previousThread=new Thread(){
            @Override
            public void run() {
                super.run();

                previousButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                  previousButtonCliked();
                    }
                });
            }
        };
        previousThread.start();
    }

    public void previousButtonCliked() {
        position=((position-1)%listOfSongs.size());
        String yuri=listOfSongs.get(position).getPath();
        uri=Uri.parse(yuri);
        if(musicService.isPlaying()){
            musicService.stop();
            musicService.release();
            metaData(uri);
            setTexts(position);
            playPause.setImageResource(R.drawable.ic_baseline_pause_24);
            musicService.createMediaPlayer(uri);
            musicService.start();
            playPause.setImageResource(R.drawable.ic_baseline_pause_24);
            musicService.showNotification(R.drawable.ic_baseline_pause_24,position,uri);
            musicService.OnCompleted();
        }
        else{
            metaData(uri);
            setTexts(position);
            musicService.start();
            playPause.setImageResource(R.drawable.ic_baseline_pause_24);
            musicService.showNotification(R.drawable.ic_baseline_pause_24,position,uri);
            musicService.OnCompleted();
        }


    }

    private void playPauseThreadbtn() {
        playPauseThread=new Thread(){
            @Override
            public void run() {
                super.run();
                playPause.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        playPauseBUttonClicked();
                    }
                });
            }
        };
        playPauseThread.start();
    }

    public void playPauseBUttonClicked() {
        if(musicService.isPlaying()) {
            musicService.pause();
            playPause.setImageResource(R.drawable.ic_baseline_play_arrow_24);
            musicService.showNotification(R.drawable.ic_baseline_play_arrow_24,position,uri);
            seekbar.setMax(musicService.getDuration()/1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(musicService!=null){
                        int mCurrentposition=musicService.getCurrentPosition()/1000;
                        seekbar.setProgress(mCurrentposition);
                        timeElapsed.setText(formattedText(mCurrentposition));
                    }
                    handler.postDelayed(this,1000);
                }

            });
        }
        else{
            if(musicService!=null){
                musicService.start();
                playPause.setImageResource(R.drawable.ic_baseline_pause_24);
                musicService.showNotification(R.drawable.ic_baseline_pause_24,position,uri);
                musicService.OnCompleted();
                seekbar.setMax(musicService.getDuration()/1000);
                PlayerActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(musicService!=null){
                            int mCurrentposition=musicService.getCurrentPosition()/1000;
//                                Log.e("m current position", String.valueOf(mCurrentposition));
                            seekbar.setProgress(mCurrentposition);
                            timeElapsed.setText(formattedText(mCurrentposition));
                        }
                        handler.postDelayed(this,1000);
                    }

                });
            }

        }

    }

    private void seekbarStuu() {
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean fromUser) {
                if(musicService!=null&&fromUser){
                    musicService.seekTo(i*1000);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void getIntentMethod() {
        listOfSongs=musicFiles;
        if(listOfSongs!=null){
            playPause.setImageResource(R.drawable.ic_baseline_pause_24);
            sender=getIntent().getStringExtra("sender");
            position=getIntent().getIntExtra("position",0);

            if(sender.contains("wholenotification")) {
              Log.e(TAG,"position in full notification "+position);
                String yuri=musicFiles.get(position).getPath();
                uri=Uri.parse(yuri);
            }
            else{
                uri = Uri.parse(getIntent().getStringExtra("uri"));
                String yuri=musicFiles.get(position).getPath();
                uri=Uri.parse(yuri);
            }

            Log.e(TAG,"position from music adapter is : "+position +sender);



            String title=musicFiles.get(position).getTitle();
            songName.setText(title);
            playinfSong.setText(title);

            String artist=musicFiles.get(position).getArtist();
            artistName.setText(artist);

            String totalTyme=musicFiles.get(position).getDuration();
            int durationtotal=Integer.parseInt(totalTyme)/1000;
            totalTime.setText(formattedText(durationtotal));

            String numPlaying=position+"/"+listOfSongs.size();
            songNumber.setText(numPlaying);
            if(sender.contains("wholenotification")){
              Log.e(TAG,"trying to open notification");
            }else{
                Intent intent= new Intent(this,MusicService.class);
                intent.putExtra("position",position);
                intent.putExtra("sender",sender);
                intent.putExtra("uri",uri.toString());
                startService(intent);
            }



        }


    }
    void keepPlaying(){

    }

    private void setTexts(int position) {
        String title=listOfSongs.get(position).getTitle();
        songName.setText(title);
        playinfSong.setText(title);
        String artist=listOfSongs.get(position).getArtist();
        artistName.setText(artist);
        String totalTyme=listOfSongs.get(position).getDuration();
        int durationtotal=Integer.parseInt(totalTyme)/1000;
        totalTime.setText(formattedText(durationtotal));
        String numPlaying=position+"/"+listOfSongs.size();
        songNumber.setText(numPlaying);
    }

    private String formattedText(int mCurrentposition) {
        String totalOut="";
        String totalNew="";
        String seconds=String.valueOf(mCurrentposition%60);
        String minutes=String.valueOf(mCurrentposition/60);
        totalOut=minutes+":"+seconds;
        totalNew=minutes+":"+0+seconds;
        if(seconds.length()==1){

            return totalNew;
        }
        else {

            return totalOut;

        }
    }

    private void initViews() {
        playinfSong=findViewById(R.id.playing_song);
        songName=findViewById(R.id.song_name);
        artistName=findViewById(R.id.song_artist);
        timeElapsed=findViewById(R.id.time_played);
        totalTime=findViewById(R.id.total_time);
        backButtob=findViewById(R.id.back_button);
        menuButton=findViewById(R.id.menu_button);
        albumArt=findViewById(R.id.cover_art);
        repeatButton=findViewById(R.id.repeat_btn);
        previousButton=findViewById(R.id.prev_btn);
        nextButton=findViewById(R.id.next_btn);
        shuffleButton=findViewById(R.id.shuffle_btn);
        seekbar=findViewById(R.id.seekbar);
        playPause=findViewById(R.id.play_pause);
        mcontainer = findViewById(R.id.m_container);
        buttonslayout=findViewById(R.id.buttons_layout);
        namesLayout=findViewById(R.id.names_layout);
        timeslayout=findViewById(R.id.times_layout);
        songNumber=findViewById(R.id.song_number);
    }

    public void metaData(Uri uri){
        FFmpegMediaMetadataRetriever mmr = new FFmpegMediaMetadataRetriever();
        mmr.setDataSource(uri.toString());
        byte [] art = mmr.getEmbeddedPicture();
        if(art!=null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(art, 0, art.length);
            Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                @Override
                public void onGenerated(@Nullable Palette palette) {
                    swatch = palette.getVibrantSwatch();
                    bgswatch = palette.getDarkMutedSwatch();
                    if (swatch != null&&bgswatch!=null) {
                        mcontainer.setBackgroundColor(swatch.getRgb());
                        seekbar.getProgressDrawable().setColorFilter(swatch.getRgb(),
                                PorterDuff.Mode.MULTIPLY);
                        seekbar.getThumb().setColorFilter(swatch.getRgb(),PorterDuff.Mode.SRC_IN);
                        if(isShuffling){
                            shuffleButton.getDrawable().setColorFilter
                                    (swatch.getRgb(),PorterDuff.Mode.SRC_IN);
                        }
                        if(isReapeating){
                            repeatButton.getDrawable().setColorFilter
                                    (swatch.getRgb(),PorterDuff.Mode.SRC_IN);
                        }
                        playPause.setBackgroundTintList(ColorStateList.valueOf(swatch.getRgb()));
                        buttonslayout.setBackgroundColor(bgswatch.getRgb());
                        namesLayout.setBackgroundColor(bgswatch.getRgb());
                        timeslayout.setBackgroundColor(bgswatch.getRgb());
                    }
                    changestatusBarColor();
                }
            });
            Glide.with(getApplicationContext())
                    .load(art)
                    .placeholder(R.drawable.musicicon)
                    .into(albumArt);
        }
        else{
            Glide.with(getApplicationContext())
                    .load(R.drawable.musicicon)
                    .into(albumArt);

        }
    }

    private void changestatusBarColor(){
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            if(bgswatch!=null){
                window.setStatusBarColor(bgswatch.getRgb());
            }
            else{
                window.setStatusBarColor(statusBarColor);
            }

        }
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        nextButtonClicked();
        mediaPlayer.setOnCompletionListener(this);
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        MusicService.MyBinder myBinder= (MusicService.MyBinder) iBinder;
        musicService=myBinder.getService();
        seekbar.setMax(musicService.getDuration()/1000);
        metaData(uri);
        musicService.setCallback(this);
        Toast.makeText(getApplicationContext(), "Service connected", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        musicService=null;
        Toast.makeText(getApplicationContext(), "Service disconnected", Toast.LENGTH_SHORT).show();
    }
}