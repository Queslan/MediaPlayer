package com.example.mediaplayer;

import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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
    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setPlayList();
        bindButtons();
        setOnClickListeners();
        setupMediaPlayer();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.release();
        mediaPlayer = null;
        timer_thread.interrupt();

    }

    public void setTimerThread(){
        timer_thread = new Thread(){
            @Override
            public void run(){
                while(!isInterrupted()){
                    try{
                        Thread.sleep(1000);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                formatTimer(mediaPlayer.getCurrentPosition());
                                if(mediaPlayer.getCurrentPosition()+1 >= mediaPlayer.getDuration()){
                                    startNextTrack();
                                }
                            }
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
    }
    public void startNextTrack(){
        if(mediaPlayer == null)
            return;
        if(position + 1 >= playList.size())
            return;

        position++;
        setupCurrentTrack();
    }
    public void startPreviousTrack(){
        if(mediaPlayer == null)
            return;
        if(position - 1 < 0)
            return;

        position--;
        setupCurrentTrack();
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
                startNextTrack();
            }
        });

        previousTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPreviousTrack();
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
                    timeForwarded = mediaPlayer.getDuration();
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
                setupCurrentTrack();
            }
        });
    }

    public void setupCurrentTrack(){
        currentTrack = playList.get(position);
        Integer trackId = currentTrack.getId();
        String trackName = currentTrack.getName();
        title.setText(trackName);
        mediaPlayer.release();
        mediaPlayer = MediaPlayer.create(this, trackId);
        mediaPlayer.start();
    }
    public void setupMediaPlayer(){

        currentTrack = playList.get(position);
        Integer trackId = currentTrack.getId();
        String trackName = currentTrack.getName();
        title.setText(trackName);

        mediaPlayer = MediaPlayer.create(this, trackId);
        setTimerThread();
        timer_thread.start();
    }
    public void setPlayList(){
        position = 0;
        playList = new ArrayList<>();
        playList.add(new Track(R.raw.the_thing_that_should_not_be, "The Thing That Should Not Be"));
        playList.add(new Track(R.raw.battery, "Battery"));
        playList.add(new Track(R.raw.master_of_puppets, "Master of Puppets"));
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
    public void formatTimer(long timeInMiliseconds){
        int timeInSeconds = (int)(timeInMiliseconds / secondInMilliseconds);
        int minutes = timeInSeconds / 60;
        int seconds = timeInSeconds % 60;

        if (seconds > 9){
            timer.setText(minutes + ":" + seconds);
        } else {
            timer.setText(minutes + ":0" + seconds);
        }
    }

}
