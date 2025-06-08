package com.sebi.android.aircheck;

import android.os.Bundle;
import android.graphics.Color;

public class TemperatureDataActivity extends ChartActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        dataType = "temp";
        chartTitle = "Temperature over the last 25 minutes";
        valueSuffix = "°C";
        WARNING_THRESHOLD = 35;
        DANGER_THRESHOLD = 45;
        super.onCreate(savedInstanceState);
    }
}