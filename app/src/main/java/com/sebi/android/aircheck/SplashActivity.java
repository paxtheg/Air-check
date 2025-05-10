package com.sebi.android.aircheck;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    private static final int SPLASH_DURATION = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(() -> {
            // Start main activity after delay
            Intent intent = new Intent(SplashActivity.this, AirCheck.class);
            startActivity(intent);
            finish();
        }, SPLASH_DURATION);
    }
}