package com.example.mediaplayer;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    final private long secondInMilliseconds = 1000;
    private Button start;
    private Button pause;
    private Button nextTrack;
    private Button previousTrack;
    private Button forward5s;
    private Button backward5s;
    private Button randomiseTracks;
    private MediaPlayer mediaPlayer;
    private ArrayList <Track> playList;
    private TextView title;
    private TextView timer;
    private Thread timer_thread;
    private Track currentTrack;
    private int position = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getPermission();
        bindButtons();
        setOnClickListeners();
        setPlayList();
        try {
            setupMediaPlayer();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void getPermission() {
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
    }


    public void setPlayList(){
        ContentResolver contentResolver = getContentResolver();
        Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor songCursor = contentResolver.query(songUri, null, null, null, null);

        playList = new ArrayList<>();
        position = 0;

        if(songCursor != null && songCursor.moveToFirst()){
            int songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int data = songCursor.getColumnIndex(MediaStore.Audio.Media.DATA);

            do{
                String currentTitle = songCursor.getString(songTitle);
                String currentData = songCursor.getString(data);
                playList.add(new Track(currentData, currentTitle));
            }while (songCursor.moveToNext());
        }
    }


    public void setTimerThread(){
        timer_thread = new Thread(){
            @Override
            public void run(){
                while(!isInterrupted()){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                formatTimer(mediaPlayer.getCurrentPosition());
                            }
                        });
                }
            }
        };
    }
    public void startNextTrack() throws IOException {
        if(mediaPlayer == null)
            return;
        if(position + 1 >= playList.size())
            return;

        position++;
        playCurrentTrack();
    }
    public void startPreviousTrack() throws IOException {
        if(mediaPlayer == null)
            return;
        if(position - 1 < 0)
            return;

        position--;
        playCurrentTrack();
    }

    public void setOnClickListeners(){
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.start();
            }
        });
        nextTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startNextTrack();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        previousTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startPreviousTrack();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.pause();
            }
        });
        forward5s.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                long timeForwarded = mediaPlayer.getCurrentPosition() + 5 * secondInMilliseconds;
                if(timeForwarded > mediaPlayer.getDuration()){
                    return;
                }
                mediaPlayer.seekTo(timeForwarded, MediaPlayer.SEEK_PREVIOUS_SYNC);
            }
        });

        backward5s.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                long timeBackward = mediaPlayer.getCurrentPosition() - 5 * secondInMilliseconds;
                if(timeBackward < 0){
                    timeBackward = 0;
                }
                mediaPlayer.seekTo(timeBackward, MediaPlayer.SEEK_PREVIOUS_SYNC);
            }
        });
        randomiseTracks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(playList == null)
                    return;
                position = 0;
                Collections.shuffle(playList);
                try {
                    playCurrentTrack();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void playCurrentTrack() throws IOException {
        mediaPlayer.reset();
        setCurrentTrack();
        mediaPlayer.start();
    }

    public void setCurrentTrack() throws IOException {
        currentTrack = playList.get(position);
        title.setText(currentTrack.getName());
        mediaPlayer.setDataSource(currentTrack.getData());
        mediaPlayer.prepare();
    }

    public void setupMediaPlayer() throws IOException {
        mediaPlayer = new MediaPlayer();
        setCurrentTrack();
        setTimerThread();
        timer_thread.start();
        setupOnCompletionListener();
    }

    private void setupOnCompletionListener() {
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                try {
                    startNextTrack();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    public void bindButtons(){
        start = findViewById(R.id.button_start);
        pause = findViewById(R.id.button_pause);
        nextTrack = findViewById(R.id.button_next_track);
        title = findViewById(R.id.title);
        timer = findViewById(R.id.time);
        forward5s = findViewById(R.id.button_forward5s);
        previousTrack = findViewById(R.id.button_previous_track);
        backward5s = findViewById(R.id.button_backward5s);
        randomiseTracks = findViewById(R.id.button_randomise_tracks);

    }
    public void formatTimer(long timeInMilliseconds){
        int timeInSeconds = (int)(timeInMilliseconds / secondInMilliseconds);
        int minutes = timeInSeconds / 60;
        int seconds = timeInSeconds % 60;

        if (seconds > 9){
            timer.setText(minutes + ":" + seconds);
        } else {
            timer.setText(minutes + ":0" + seconds);
        }
    }

}
