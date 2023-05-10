package com.example.musicplayer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatSeekBar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.musicplayer.R;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private SharedPreferences mPreferences;
    private String sharedPrefFile =
            "com.example.mediaplayer";
    private final String MUSIC_INDEX = "musicIndex";
    private final String CURRENT_TIME = "currentTime";
    private final String REPEAT = "repeat";
    private final String SHUFFLE = "shuffle";
    private final String BACKGROUND_IMAGE = "backgroundImage";
    static private View main_view;;
    static private AppCompatSeekBar music_seek_progressbar;
    static public FloatingActionButton btn_play;
    static public ImageView backgroundImage;
    static public TextView music_current_duration, music_total_duration, musicTitleTextView,headTitleTextView,musicNumber;
    static public CircularImageView image;
    static public MediaPlayer mp;
    static public Handler mHandler = new Handler();
    static String headTitle="";
    static ArrayList<String> musicPath_vector = new ArrayList<String>();
    static ArrayList<String> musicTitle_vector = new ArrayList<String>();
    static boolean isLooping = false,isShuffle = false,isRestartApp = true;
    static int musicIndex = 0,currentTime = 0,backgroundImageNumber = 0;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        verifyStoragePermissions(MainActivity.this);
        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        backgroundImage = findViewById(R.id.backgroundImage);
        setSupportActionBar(toolbar);
        mPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE);

        musicIndex = mPreferences.getInt(MUSIC_INDEX,0);
        currentTime = mPreferences.getInt(CURRENT_TIME,0);
        isShuffle = mPreferences.getBoolean(SHUFFLE,false);
        isLooping = mPreferences.getBoolean(REPEAT,false);
        backgroundImageNumber = mPreferences.getInt(BACKGROUND_IMAGE,0);
        changeBackgroundImage(backgroundImageNumber);
        setMusicPlayerComponents();
    }

    // android 6.0以上需要權限認證
    public static void verifyStoragePermissions(Activity activity){
        try{
            int permission = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.READ_EXTERNAL_STORAGE");
            if(permission!=PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(activity,new String[]{"android.permission.READ_EXTERNAL_STORAGE"},100);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){

        }else{
            System.exit(0);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor preferencesEditor = mPreferences.edit();
        preferencesEditor.putInt(CURRENT_TIME, mp.getCurrentPosition());
        preferencesEditor.putInt(MUSIC_INDEX, musicIndex);
        preferencesEditor.putInt(BACKGROUND_IMAGE, backgroundImageNumber);
        preferencesEditor.putBoolean(REPEAT, isLooping);
        preferencesEditor.putBoolean(SHUFFLE, isShuffle);
        preferencesEditor.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.scanMusic:
                ScanMusic(main_view);
                break;
            case R.id.showList:
                Intent listIntent = new Intent(this,ListActivity.class);
                startActivity(listIntent);
                break;
            case R.id.about:
                Intent aboutIntent = new Intent(this,AboutActivity.class);
                startActivity(aboutIntent);
                break;
            case R.id.imageDefault:
                backgroundImage.setImageResource(R.drawable.background);
                backgroundImageNumber = 0;
                break;
            case R.id.metal:
                backgroundImage.setImageResource(R.drawable.metal);
                backgroundImageNumber = 1;
                break;
            case R.id.fracture:
                backgroundImage.setImageResource(R.drawable.fracture);
                backgroundImageNumber = 2;
                break;
            case R.id.universe:
                backgroundImage.setImageResource(R.drawable.universe);
                backgroundImageNumber = 3;
                break;
            case R.id.science:
                backgroundImage.setImageResource(R.drawable.science);
                backgroundImageNumber = 4;
                break;
            case R.id.exit:
                this.onPause();
                finish();
                System.exit(0);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void changeBackgroundImage(int num)
    {
        switch(num) {
            case 0:
                backgroundImage.setImageResource(R.drawable.background);
                break;
            case 1:
                backgroundImage.setImageResource(R.drawable.metal);
                break;
            case 2:
                backgroundImage.setImageResource(R.drawable.fracture);
                break;
            case 3:
                backgroundImage.setImageResource(R.drawable.universe);
                break;
            case 4:
                backgroundImage.setImageResource(R.drawable.science);
                break;
        }
    }

    private void getMusicInfo()
    {
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = MainActivity.this.getContentResolver().query(uri, null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);//内容提供器
        if(musicTitle_vector.size()!=0)
        {
            musicTitle_vector.clear();
            musicPath_vector.clear();
        }
        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToNext();
            int isMusic = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));
            if (isMusic != 0){
                String title = cursor.getString((cursor
                        .getColumnIndex(MediaStore.Audio.Media.TITLE)));
                String url = cursor.getString(cursor
                        .getColumnIndex(MediaStore.Audio.Media.DATA));
                musicTitle_vector.add(title);
                musicPath_vector.add(url);
            }
        }
    }

    //掃描歌曲并提示找到的歌曲總數量
    public void ScanMusic(View view) {
        String displayText="";
        int songNum = 0;
        getMusicInfo();
        switch(musicTitle_vector.size()){
            case 0:
                displayText = "No song was found";
                break;
            case 1:
                displayText = "1 song was found";
                songNum = 1;
                break;
            default:
                displayText = Integer.toString(musicTitle_vector.size())+" songs were found";
                songNum = musicIndex+1;
                break;
        }
        Toast.makeText(this,displayText,Toast.LENGTH_SHORT).show();
        musicNumber.setText(Integer.toString(songNum) + "/" + Integer.toString(musicTitle_vector.size()));
    }


    //設置音樂播放相關的元件
    private void setMusicPlayerComponents()
    {
        main_view = findViewById(R.id.parent_view);
        music_seek_progressbar = findViewById(R.id.seek_song_progressbar);
        btn_play = findViewById(R.id.btn_play);
        music_current_duration = findViewById(R.id.tv_song_current_duration);
        music_total_duration = findViewById(R.id.total_duration);
        musicTitleTextView = findViewById(R.id.songTitle);
        headTitleTextView = findViewById(R.id.headTitle);
        musicNumber = findViewById(R.id.muiscNumber);
        Resources res = getResources();
        image = findViewById(R.id.image);
        mp = new MediaPlayer();

        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer){
                SharedPreferences.Editor preferencesEditor = mPreferences.edit();
                preferencesEditor.remove(CURRENT_TIME);
                preferencesEditor.remove(MUSIC_INDEX);
                btn_play.setImageResource(R.drawable.ic_play);
                headTitleTextView.setText("Ready to Play");
                if(isShuffle){
                    if(!isLooping) {
                        Random ran = new Random();
                        musicIndex = ran.nextInt(musicTitle_vector.size());
                    }
                    headTitleTextView.setText("Now Playing");
                    btn_play.setImageResource(R.drawable.ic_pause);
                    PrepareMusicPlayer();
                    mp.start();
                }else if(!isShuffle){
                    if(!isLooping){
                        if(musicIndex<musicTitle_vector.size()-1)
                        {
                            musicIndex++;
                            headTitleTextView.setText("Now Playing");
                            btn_play.setImageResource(R.drawable.ic_pause);
                            PrepareMusicPlayer();
                            mp.start();
                        }else{
                            musicIndex = 0;
                            PrepareMusicPlayer();
                        }
                    }else{
                        headTitleTextView.setText("Now Playing");
                        btn_play.setImageResource(R.drawable.ic_pause);
                        PrepareMusicPlayer();
                        mp.start();
                    }
                }
            }
        });
        toggleButtonColor((ImageButton) findViewById(R.id.btn_repeat),isLooping);
        toggleButtonColor((ImageButton) findViewById(R.id.btn_suffle),isShuffle);
        getMusicInfo();
        PrepareMusicPlayer();

        music_seek_progressbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                int totalDuration = mp.getDuration();
                int currentPosition = progressToTimer(progress,totalDuration);
                music_current_duration.setText(milliSecondsToTime(currentPosition));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mHandler.removeCallbacks(mUpdateTimeTask);  //停止動作
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mHandler.removeCallbacks(mUpdateTimeTask);  //停止動作
                int totalDuration = mp.getDuration();
                int currentPosition = progressToTimer(seekBar.getProgress(),totalDuration);
                mp.seekTo(currentPosition);
                mHandler.post(mUpdateTimeTask); //執行動作
            }
        });
        buttonPlayerAction();
        updateTimerAndSeeker();
    }

    //準備開始播放音樂的動作
    public static void PrepareMusicPlayer()
    {
        if(musicTitle_vector.size()<1){
            musicNumber.setText("0/0");
        }else {
            try{
                mp.reset();
                if(musicIndex>musicTitle_vector.size()-1){
                    musicIndex = 0;
                }
                ///////////////////////取得歌曲封面
                MediaMetadataRetriever mediaMetadataRetriever=new MediaMetadataRetriever();
                mediaMetadataRetriever.setDataSource(musicPath_vector.get(musicIndex));
                Bitmap bitmap=null;
                if(mediaMetadataRetriever.getEmbeddedPicture()!=null){
                    byte[] picture = mediaMetadataRetriever.getEmbeddedPicture();
                    bitmap= BitmapFactory.decodeByteArray(picture,0,picture.length);
                    image.setImageBitmap(bitmap);
                }else{
                    image.setImageResource(R.drawable.ntut_music);
                }
                /////////////////////
                mp.setDataSource(musicPath_vector.get(musicIndex));
                if(isLooping){
                    mp.setLooping(true);
                }else{
                    mp.setLooping(false);
                }
                musicTitleTextView.setText(musicTitle_vector.get(musicIndex));
                mp.prepare();
                if(isRestartApp){
                    isRestartApp = false;
                }else{
                    currentTime = 0;
                }
                mp.seekTo(currentTime);
                mHandler.post(mUpdateTimeTask);
            } catch (IOException e) {
                Snackbar.make(main_view,"Could not load audio file.", Snackbar.LENGTH_LONG).show();
                e.printStackTrace();
            }
            musicNumber.setText(Integer.toString(musicIndex + 1) + "/" + Integer.toString(musicTitle_vector.size()));
        }
    }

    //播放按鈕的按下去的事件
    private void buttonPlayerAction()
    {
        btn_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View agr0) {
                if(musicTitle_vector.size()==0){
                    Snackbar.make(main_view,"No Song to Play",Snackbar.LENGTH_SHORT).show();
                    return;}
                if(mp.isPlaying())
                {
                    mp.pause();
                    btn_play.setImageResource(R.drawable.ic_play);
                    mHandler.removeCallbacks(mUpdateTimeTask);
                    headTitle = "Pause";

                }else{
                    mp.start();
                    btn_play.setImageResource(R.drawable.ic_pause);
                    mHandler.post(mUpdateTimeTask);
                    headTitle = "Now Playing";
                }
                rotateTheDisk();
                headTitleTextView.setText(headTitle);
            }
        });
    }

    //xml佈局中onclick方法
    public void controlClick(View v)
    {
        int id = v.getId();
        switch(id)
        {
            case R.id.btn_next:{
                nextMusic();
                break;
            }
            case R.id.btn_prev:{
                preMusic();
                break;
            }
            case R.id.btn_repeat:{
                if(isLooping) {
                    isLooping = false;
                    mp.setLooping(false);
                    Snackbar.make(main_view,"Loop one off",Snackbar.LENGTH_SHORT).show();
                }else{
                    isLooping = true;
                    mp.setLooping(true);
                    Snackbar.make(main_view,"Loop one on",Snackbar.LENGTH_SHORT).show();
                }
                toggleButtonColor((ImageButton) findViewById(R.id.btn_repeat),isLooping);
                break;
            }
            case R.id.btn_suffle:{
                if(isShuffle){
                    isShuffle = false;
                    Snackbar.make(main_view,"Shuffle off",Snackbar.LENGTH_SHORT).show();
                }else{
                    isShuffle = true;
                    Snackbar.make(main_view,"Shuffle on",Snackbar.LENGTH_SHORT).show();
                }
                toggleButtonColor((ImageButton) findViewById(R.id.btn_suffle),isShuffle);
                break;
            }
        }
    }

    // 點擊按鈕后轉換顏色的動作
    private void toggleButtonColor(ImageButton bt,boolean flag)
    {
        if(flag)
        {
            bt.setColorFilter(getResources().getColor(R.color.colorDarkOrange), PorterDuff.Mode.SRC_ATOP);
        }else{
            bt.setColorFilter(getResources().getColor(R.color.colorWhite),PorterDuff.Mode.SRC_ATOP);
        }
    }

    //CircularImageView旋轉的動作
    public static void rotateTheDisk()
    {
        if(!mp.isPlaying()) return;
        image.animate().setDuration(100).rotation(image.getRotation()+2f).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                rotateTheDisk();
                super.onAnimationEnd(animation);
            }
        });
    }

    //執行緒的動作
    public static Runnable mUpdateTimeTask = new Runnable() {
        @Override
        public void run() {
            updateTimerAndSeeker();
            if(mp.isPlaying())
            {
                mHandler.postDelayed(this,100);
            }
        }
    };

    //更新進度條與顯示時間
    private static void updateTimerAndSeeker()
    {
        if(musicTitle_vector.size()==0){
            return;}
        long totalDuration = mp.getDuration();
        long currentDuration = mp.getCurrentPosition();

        music_total_duration.setText(milliSecondsToTime(totalDuration));
        music_current_duration.setText(milliSecondsToTime(currentDuration));

        int progress = (int) (getProgressSeekBar(currentDuration,totalDuration));
        music_seek_progressbar.setProgress(progress);
    }

    //播放下一首的動作
    public void nextMusic() {
        SharedPreferences.Editor preferencesEditor = mPreferences.edit();
        preferencesEditor.remove(CURRENT_TIME);
        preferencesEditor.remove(MUSIC_INDEX);
        if(isShuffle){
            Random ran = new Random();
            musicIndex = ran.nextInt(musicTitle_vector.size());
            mHandler.removeCallbacks(mUpdateTimeTask);
            mp.stop();
            headTitleTextView.setText("Now Playing");
            btn_play.setImageResource(R.drawable.ic_pause);
            PrepareMusicPlayer();
            mp.start();
            mHandler.post(mUpdateTimeTask);
        }else{
            if(mp != null && musicIndex < musicTitle_vector.size()-1) {
                mHandler.removeCallbacks(mUpdateTimeTask);
                mp.stop();
                if(!isShuffle){
                    musicIndex++;
                }
                headTitleTextView.setText("Now Playing");
                btn_play.setImageResource(R.drawable.ic_pause);
                PrepareMusicPlayer();
                mp.start();
                mHandler.post(mUpdateTimeTask);
            }else{
                Snackbar.make(main_view,"No Next Music",Snackbar.LENGTH_SHORT).show();
            }
        }
        rotateTheDisk();
    }

    //播放上一首的動作
    public void preMusic() {
        SharedPreferences.Editor preferencesEditor = mPreferences.edit();
        preferencesEditor.remove(CURRENT_TIME);
        preferencesEditor.remove(MUSIC_INDEX);
        if(isShuffle){
            Random ran = new Random();
            musicIndex = ran.nextInt(musicTitle_vector.size());
            mHandler.removeCallbacks(mUpdateTimeTask);
            mp.stop();
            headTitleTextView.setText("Now Playing");
            btn_play.setImageResource(R.drawable.ic_pause);
            PrepareMusicPlayer();
            mp.start();
            mHandler.post(mUpdateTimeTask);
        }else{
            if(mp != null && musicIndex > 0) {
                mHandler.removeCallbacks(mUpdateTimeTask);
                mp.stop();
                musicIndex--;
                headTitleTextView.setText("Now Playing");
                btn_play.setImageResource(R.drawable.ic_pause);
                PrepareMusicPlayer();
                mp.start();
                mHandler.post(mUpdateTimeTask);
            }else{
                Snackbar.make(main_view,"No Pre Music",Snackbar.LENGTH_SHORT).show();
            }
        }
        rotateTheDisk();
    }

    public static String milliSecondsToTime(long milliseconds)
    {
        String finalTimerString = "";
        String secondsString = "";
        int hours = (int) (milliseconds/(1000*60*60));
        int minutes = (int) (milliseconds%(1000*60*60))/(1000*60);
        int seconds = (int) ((milliseconds%(1000*60*60))%(1000*60)/1000);

        if(hours>0)
        {
            finalTimerString = hours+":";
        }
        if(seconds>=0 && seconds<=9)
        {
            secondsString = "0"+seconds;
        }
        else
        {
            secondsString = ""+seconds;
        }
        finalTimerString+=minutes+":"+secondsString;
        return finalTimerString;
    }

    public static int getProgressSeekBar(long currentDuration, long totalDuration)
    {
        Double progress = (double) 0;
        progress = ((double) currentDuration/totalDuration)*100;
        return progress.intValue();
    }

    public static  int progressToTimer(int progress, int totalDuration)
    {
        int currentDuration = 0;
        currentDuration = (int)(((double)progress/100)*totalDuration);
        return currentDuration;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mUpdateTimeTask);
        mp.stop();
    }

}

