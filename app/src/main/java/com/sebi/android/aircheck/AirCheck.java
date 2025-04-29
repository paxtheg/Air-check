package com.sebi.android.aircheck;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

public class AirCheck extends AppCompatActivity {

    private TextView co2TextView, tempTextView, humidityTextView, dustTextView, gasTextView;
    private ProgressBar progressBar;
    private Button refreshButton;

    private static final long REFRESH_INTERVAL = 5000; // 5 seconds
    private Handler refreshHandler;
    private Runnable refreshRunnable;

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
        refreshButton = findViewById(R.id.refreshButton);

        refreshButton.setOnClickListener(v -> fetchAirQualityData());

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
            refreshButton.setEnabled(false);
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
                int dustEndIndex = html.indexOf("pcs/cm", dustIndex);
                data.dust = html.substring(dustIndex, dustEndIndex).trim();

                // Gas Sensor
                int gasIndex = html.indexOf("Gas Sensor (MQ-4):") + 18;
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
            refreshButton.setEnabled(true);

            if (data != null) {
                co2TextView.setText("CO2: " + data.co2 + " ppm");
                tempTextView.setText("Temperature: " + data.temperature + " °C");
                humidityTextView.setText("Humidity: " + data.humidity + "%");
                dustTextView.setText("Dust: " + data.dust + " pcs/cm³");
                gasTextView.setText("Gas Sensor: " + data.gas);
            } else {
                Toast.makeText(AirCheck.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
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