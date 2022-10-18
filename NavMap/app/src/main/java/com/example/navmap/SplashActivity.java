package com.example.navmap;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Timer;
import java.util.TimerTask;

public class SplashActivity extends AppCompatActivity {

    //variables
    public final int splashDelay = 700;
    private static FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser = mAuth.getCurrentUser();

        //no night mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        //create and set the timer
        Timer RunSplash = new Timer();

        //give timer task
        TimerTask ShowSplash = new TimerTask() {
            @Override
            public void run() {
                //launch second screen
                Intent intent;
                if (mUser!=null)
                {
                    //go to main activity
                    intent = new Intent(SplashActivity.this,NavMain.class);
                } else{
                    //go to login
                    intent = new Intent(SplashActivity.this,LoginActivity.class);
                }
                startActivity(intent);

                finish();
            }
        };
        //Start Timer
        RunSplash.schedule(ShowSplash, splashDelay);
    }
}