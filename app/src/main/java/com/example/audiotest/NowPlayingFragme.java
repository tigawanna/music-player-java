package com.example.audiotest;


import static com.example.audiotest.MainActivity.musicFiles;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import java.util.ArrayList;
import wseemann.media.FFmpegMediaMetadataRetriever;

public class NowPlayingFragme extends Fragment implements ServiceConnection {

    ImageView miniArt,miniPlay,miniNext,miniPrev;
    TextView miniSongName,miniSongArtist;
    View view;
    MusicService musicService;
    MediaPlayer mediaPlayer;
    ArrayList<MusicFiles> listSong=new ArrayList<>();
    public NowPlayingFragme() {
        // Required empty public constructor
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        try{
            if(getContext()!=null&&musicService!=null){
                getContext().unbindService(this);
            }
        }catch(IllegalStateException e){
            e.printStackTrace();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view=inflater.inflate(R.layout.fragment_now_playing, container, false);
        miniArt=view.findViewById(R.id.miniplayer_album_art);
        miniNext=view.findViewById(R.id.miniplayer_next);
        miniPlay=view.findViewById(R.id.miniplayer_playpause);
        miniPrev=view.findViewById(R.id.miniplayer_prev);
        miniSongName=view.findViewById(R.id.miniplayer_songname);
        miniSongArtist=view.findViewById(R.id.miniplayer_artist);



        miniNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(musicService!=null){
                    miniPlay.setImageResource(R.drawable.ic_baseline_pause_24);
                    musicService.nextButtonClicked();
                    String pos= String.valueOf(musicService.position);
                    Toast.makeText(getContext(), "position next"+pos, Toast.LENGTH_SHORT).show();
                    setUpViews(pos);
                }

            }
        });
        miniPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(musicService!=null){
                    musicService.prevviuosButtonClicked();
                    miniPlay.setImageResource(R.drawable.ic_baseline_pause_24);
                    String pos= String.valueOf(musicService.position);
                    Toast.makeText(getContext(), "position prev"+pos, Toast.LENGTH_SHORT).show();
                    setUpViews(pos);
                }

            }
        });
        miniPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(musicService!=null&&musicService.isPlaying()){
                    miniPlay.setImageResource(R.drawable.ic_baseline_play_arrow_24);
                    musicService.pause();

                }else{
                    musicService.start();
                    miniPlay.setImageResource(R.drawable.ic_baseline_pause_24);



                }

            }
        });


        return view;
    }
    public byte[] getPic(String uri){
        FFmpegMediaMetadataRetriever mmr = new FFmpegMediaMetadataRetriever();
        mmr.setDataSource(uri);
        byte [] art = mmr.getEmbeddedPicture();
        return art;
    }
    public void setUpViews(String position){
        if(position!=null){
            int pos=Integer.parseInt(position);
            miniSongName.setText(musicFiles.get(pos).getTitle());
            miniSongArtist.setText(musicFiles.get(pos).getArtist());
            String pathy=musicFiles.get(pos).getPath();
            byte [] art=getPic(pathy);
            if(getContext()!=null){
                Glide.with(getContext()).load(art).placeholder(R.drawable.musicicon).into(miniArt);
            }


        }
    }


    @Override
    public void onServiceConnected(ComponentName componentName, IBinder service) {
        MusicService.MyBinder binder= (MusicService.MyBinder) service;
        musicService= binder.getService();
//        if(initialLoad){
//            getIntentMethod();
//            Log.e("whats up","initial load");
//        }



    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        musicService=null;
    }

    private void getIntentMethod() {
        listSong=musicFiles;


    }
}