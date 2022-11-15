package com.cabandt.navmap;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.cabandt.navmap.R;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SplashActivity extends AppCompatActivity {

    //variables
    public final int splashDelay = 700;
    private static FirebaseAuth mAuth;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        FirebaseApp.initializeApp(SplashActivity.this);
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

    private void loadFromDB()
    {
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(new Runnable() {
            @Override
            public void run() {
                //pre-execute
                SplashActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog = ProgressDialog.show(SplashActivity.this,
                                "Loading...", "Downloading bookmark data");
                    }
                });

                //background
                try {
                    // do in background
                } catch (Exception e) {
                    Log.e(TAG, "Couldn't load bookmarks");
                    e.printStackTrace();
                }

                //onPost-execute
                SplashActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();

                        //do after
                    }
                });
            }
        });
    }
}