package com.sebi.android.aircheck;

import android.os.Bundle;
import android.graphics.Color;

public class TemperatureDataActivity extends ChartActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        dataType = "temp";
        chartTitle = "Temperature over the last 25 minutes";
        valueSuffix = "°C";
        color = Color.GREEN;
        super.onCreate(savedInstanceState);
    }
}