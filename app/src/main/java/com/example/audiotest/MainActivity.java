package com.example.audiotest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener,
        ServiceConnection {
    public static  boolean SHOW_MINI_PLAYER = false;
    public static final int REQUEST_CODE=1;
    private String MY_SORT_PREF="sortOrder";
    static ArrayList<MusicFiles> musicFiles;
    String TAG="MAIN_ACTIVITY_LOGS";
    MusicService musicService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        changestatusBarColor();
        permisioon();
        initViewPager();
        Log.e(TAG,"music files in main activity : "+musicFiles.size());
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        Intent intent =new Intent(this,MusicService.class);
//        bindService(intent,this,BIND_AUTO_CREATE);
//    }
//    @Override
//    protected void onPause() {
//        super.onPause();
//        unbindService(this);
//    }

  private void changestatusBarColor(){
        if (Build.VERSION.SDK_INT >= 21) {
          Window window = this.getWindow();
          window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
          window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
          window.setStatusBarColor(this.getResources().getColor(R.color.black));
      }
    }
    private void initViewPager() {
        ViewPager viewPager=findViewById(R.id.view_pager);
        TabLayout tabLayout=findViewById(R.id.tab_layout);
        ViewPagerAdapter viewPagerAdapter=new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragments(new SongsFragment(),"Songs");
        viewPagerAdapter.addFragments(new AlbumFragment(),"Albums");
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

    }

    public static  class ViewPagerAdapter extends FragmentPagerAdapter {

        private ArrayList<Fragment> fragments;
        private ArrayList<String> titles;
        public ViewPagerAdapter(@NonNull FragmentManager fm) {
            super(fm);
            this.fragments=new ArrayList<>();
            this.titles=new ArrayList<>();
        }
        void addFragments(Fragment fragment,String title){
            fragments.add(fragment);
            titles.add(title);
        }
        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
//            return super.getPageTitle(position);
            return titles.get(position);
        }
    }
    private void permisioon() {
        if(ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]
                    {Manifest.permission.READ_EXTERNAL_STORAGE},REQUEST_CODE);
        }
        else{
            musicFiles=loadAudio();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==REQUEST_CODE){
            if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
                musicFiles=loadAudio();
            }
            else{
                ActivityCompat.requestPermissions(MainActivity.this,new String[]
                        {Manifest.permission.READ_EXTERNAL_STORAGE},REQUEST_CODE);
            }
        }
    }

    private ArrayList<MusicFiles> loadAudio() {
        SharedPreferences sharedPreferences=getSharedPreferences(MY_SORT_PREF,MODE_PRIVATE);
        String mySortOrder=sharedPreferences.getString("sorting","sortByName");
        String order=null;
        ContentResolver contentResolver = getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        switch(mySortOrder){
            case "sortByName":
                order=MediaStore.MediaColumns.DISPLAY_NAME+ " ASC";
                break;
            case "sortByDate":
                order=MediaStore.MediaColumns.DATE_ADDED+ " DESC";
                break;
            case "sortBySize":
                order=MediaStore.MediaColumns.SIZE+ " DESC";
                break;
        }

        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";

        Cursor cursor = contentResolver.query(uri, null, selection, null, order);

        ArrayList<String> duplicate=new ArrayList<>();

        ArrayList<MusicFiles> tempaudiolist=new ArrayList<>();
        if (cursor != null && cursor.getCount() > 0) {
            musicFiles = new ArrayList<>();
            while (cursor.moveToNext()) {
                @SuppressLint("Range") String path =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                @SuppressLint("Range") String  duration =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                @SuppressLint("Range") String title =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                @SuppressLint("Range") String album =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                @SuppressLint("Range") String artist =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                @SuppressLint("Range") String id =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID));

                MusicFiles musicFiles=new MusicFiles(path,title,artist,album,duration,id);
                tempaudiolist.add(musicFiles);

            }
            cursor.close();
        }

        return tempaudiolist;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search,menu);
        MenuItem menuItem=menu.findItem(R.id.search_option);

        SearchView searchView=(SearchView) menuItem.getActionView();
        searchView.setOnQueryTextListener(this);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        SharedPreferences.Editor editor=getSharedPreferences(MY_SORT_PREF,MODE_PRIVATE).edit();
        switch (item.getItemId()){
            case R.id.by_name:
                editor.putString("sorting","sortByName");
                editor.apply();
                this.recreate();
                break;
            case R.id.by_date:
                editor.putString("sorting","sortByDate");
                editor.apply();
                this.recreate();
                break;
            case R.id.by_size:
                editor.putString("sorting","sortBySize");
                editor.apply();
                this.recreate();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        String userInput=newText.toLowerCase();
        ArrayList<MusicFiles> myFiles=new ArrayList<>();
        for(MusicFiles song:musicFiles){
            if(song.getTitle().toLowerCase().contains(userInput)){
                myFiles.add(song);
            }
        }
        SongsFragment.musicAdapter.updateList(myFiles);
        return true;
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        MusicService.MyBinder myBinder= (MusicService.MyBinder) iBinder;
        musicService=myBinder.getService();
        Toast.makeText(getApplicationContext(), "Service connected", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        musicService=null;
        Toast.makeText(getApplicationContext(), "Service disconnected", Toast.LENGTH_SHORT).show();
    }

}