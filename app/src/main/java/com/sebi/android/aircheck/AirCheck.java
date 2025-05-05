package com.sebi.android.aircheck;


import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

public class AirCheck extends AppCompatActivity {

    private TextView co2TextView, tempTextView, humidityTextView, dustTextView, gasTextView;
    private ProgressBar progressBar;
    private static final long REFRESH_INTERVAL = 5000; // 5 seconds
    private Handler refreshHandler;
    private Runnable refreshRunnable;
    private static final String CHANNEL_ID = "air_quality_channel";
    private static final int NOTIFICATION_ID = 1;
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1001;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_air_check);

        // Initialize views
        co2TextView = findViewById(R.id.co2TextView);
        tempTextView = findViewById(R.id.tempTextView);
        humidityTextView = findViewById(R.id.humidityTextView);
        dustTextView = findViewById(R.id.dustTextView);
        gasTextView = findViewById(R.id.gasTextView);
        progressBar = findViewById(R.id.progressBar);

        // Create notification channel
        createNotificationChannel();

        // Fetch data when activity starts
        fetchAirQualityData();

        setupAutoRefresh();
    }

    private void fetchAirQualityData() {
        new FetchDataTask().execute("http://air-check.local/");
    }

    private class FetchDataTask extends AsyncTask<String, Void, AirQualityData> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected AirQualityData doInBackground(String... urls) {
            try {
                // Connect to the website and get HTML
                Document doc = Jsoup.connect(urls[0]).get();
                String html = doc.body().text();

                // Parse the data (simple string parsing based on your image)
                AirQualityData data = new AirQualityData();

                // CO2
                int co2Index = html.indexOf("CO2:") + 4;
                int co2EndIndex = html.indexOf("ppm", co2Index);
                data.co2 = html.substring(co2Index, co2EndIndex).trim();

                // Temperature
                int tempIndex = html.indexOf("Temperature:") + 11;
                int tempEndIndex = html.indexOf("C", tempIndex);
                data.temperature = html.substring(tempIndex, tempEndIndex).trim();

                // Humidity
                int humidityIndex = html.indexOf("Humidity:") + 9;
                int humidityEndIndex = html.indexOf("%", humidityIndex);
                data.humidity = html.substring(humidityIndex, humidityEndIndex).trim();

                // Dust Sensor
                int dustIndex = html.indexOf("Dust:") + 5;
                int dustEndIndex = html.indexOf("ug/m", dustIndex);
                data.dust = html.substring(dustIndex, dustEndIndex).trim();

                // Gas Sensor
                int gasIndex = html.indexOf("MQ-4 Voltage Data:") + 18;
                data.gas = html.substring(gasIndex).trim();

                return data;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(AirQualityData data) {
            progressBar.setVisibility(View.GONE);

            if (data != null) {
                co2TextView.setText("CO2: " + data.co2 + " ppm");
                tempTextView.setText("Temperature: " + data.temperature + " °C");
                humidityTextView.setText("Humidity: " + data.humidity + "%");
                dustTextView.setText("PM2.5 (Dust): " + data.dust + " µg/m³");
                gasTextView.setText("Gas Sensor Voltage: " + data.gas);

                // Check gas concentration and show notification if needed
                try {
                    double gasValue = Double.parseDouble(data.gas);
                    if (gasValue > 1.5) {
                        showGasAlertNotification();
                    }
                } catch (NumberFormatException e) {
                    // Handle case where gas value couldn't be parsed
                    e.printStackTrace();
                }

            } else {
                Toast.makeText(AirCheck.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            CharSequence name = "Air Quality Alerts";
            String description = "Notifications for air quality alerts";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void showGasAlertNotification() {
        // Check if we have notification permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {

            // Request the permission if we don't have it
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_REQUEST_CODE
                );
            }
            return;
        }

        // Create and show the notification if we have permission
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_warning_notification)
                .setContentTitle("Air Quality Alert")
                .setContentText("High gas concentration detected!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            // If request is cancelled, the result arrays are empty
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted, show the notification
                showGasAlertNotification();
            } else {
                // Permission denied - you might want to show a message to the user
                Toast.makeText(this, "Notification permission is needed for gas alerts", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Helper class to store air quality data
    private static class AirQualityData {
        String co2 = "--";
        String temperature = "--";
        String humidity = "--";
        String dust = "--";
        String gas = "--";
    }

    private void setupAutoRefresh() {
        refreshHandler = new Handler();
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                fetchAirQualityData();
                refreshHandler.postDelayed(this, REFRESH_INTERVAL);
            }
        };

        // Start the auto-refresh
        refreshHandler.postDelayed(refreshRunnable, REFRESH_INTERVAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop refreshing when app is in background
        refreshHandler.removeCallbacks(refreshRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Restart refreshing when app comes to foreground
        refreshHandler.postDelayed(refreshRunnable, REFRESH_INTERVAL);
    }
}