package com.example.android.popular_movie;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SplashActivity extends AppCompatActivity {
    private static final int SPLASH_DISPLAY_LENGTH = 2000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                launchMainActivity();
            }
        },SPLASH_DISPLAY_LENGTH);
    }

    private void launchMainActivity(){
        Intent intent = new Intent(SplashActivity.this , MainActivity.class);
        startActivity(intent);
        SplashActivity.this.finish();
    }

}
