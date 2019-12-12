package com.example.cooltimer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private ImageView imageView;
    private SeekBar seekBar;
    private TextView textView;
    private boolean isTimerOn;
    private Button button;
    private CountDownTimer countDownTimer;
    private int defaultInterval;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        seekBar = findViewById(R.id.seekBar);
        textView = findViewById(R.id.textView);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        button = findViewById(R.id.button);


        seekBar.setMax(3600);
        isTimerOn = false;

        setIntevalFromSharedPreferences(sharedPreferences);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                long progressInMillis = i * 1000;
                updateTimer(progressInMillis);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    public void start(View view) {
        if (!isTimerOn){
            button.setText("Stop");
            seekBar.setEnabled(false);
            isTimerOn = true;
            countDownTimer =  new CountDownTimer(seekBar.getProgress() * 1000,1000) {
                @Override
                public void onTick(long l) {
                    updateTimer(l);
                }

                @Override
                public void onFinish() {
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    if (sharedPreferences.getBoolean("enable_sound",true)){
                        String melodyName = sharedPreferences.getString("timer_melody","bell");
                        if (melodyName.equals("bell")){
                            MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(),R.raw.bell_sound);
                            mediaPlayer.start();
                        }else if (melodyName.equals("alarm_siren")){
                            MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(),R.raw.alarm_siren_sound);
                            mediaPlayer.start();
                        }else if (melodyName.equals("bip")){
                            MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(),R.raw.bip_sound);
                            mediaPlayer.start();
                        }
                    }
                    resetTimer();
                }
            };
            countDownTimer.start();
        }else {
            resetTimer();
        }
    }

    private void updateTimer(long l){
        int minutes = (int) l/1000/60;
        int seconds = (int) l/1000 - (minutes * 60);

        String minutesString = "";
        String secondsString = "";

        if (minutes<10){
            minutesString = "0" + minutes;
        }else {
            minutesString = String.valueOf(minutes);
        }
        if (seconds<10){
            secondsString = "0" + seconds;
        }else {
            secondsString = String.valueOf(seconds);
        }
        textView.setText(minutesString + ":"+ secondsString);
    }
    private void resetTimer(){
        countDownTimer.cancel();
        button.setText("start");
        seekBar.setEnabled(true);
        isTimerOn = false;
        setIntevalFromSharedPreferences(sharedPreferences);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.timer_menu,menu);
        return  true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings){
            Intent openSetting = new Intent(this,SettingsActivity.class);
            startActivity(openSetting);
            return  true;
        }else if (id == R.id.action_about){
            Intent openAbout = new Intent(this,AboutActivity.class);
            startActivity(openAbout);
            return  true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setIntevalFromSharedPreferences(SharedPreferences sharedPreferences){
//        try {
//            defaultInterval = Integer.valueOf(sharedPreferences.getString("timer_default_interval","30"));
//        }catch (NumberFormatException nef ){
//            Toast.makeText(this,"Some error number format exception",Toast.LENGTH_SHORT).show();
//        }catch (Exception e){
//            Toast.makeText(this, " Some error exception", Toast.LENGTH_SHORT).show();
//        }
        defaultInterval = Integer.valueOf(sharedPreferences.getString("timer_default_interval","30"));
        long defaultIntervalInMillis = defaultInterval *1000;
        updateTimer(defaultIntervalInMillis);
//        textView.setText("00:" + defaultInterval);
        seekBar.setProgress(defaultInterval);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.equals("timer_default_interval")){
            setIntevalFromSharedPreferences(sharedPreferences);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }
}
