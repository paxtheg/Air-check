package com.sebi.android.aircheck;

import android.graphics.Color;
import android.os.Bundle;

public class CO2DataActivity extends ChartActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        dataType = "co2";
        chartTitle = "CO2 Levels over the last 25 minutes";
        valueSuffix = "ppm";
        WARNING_THRESHOLD = 1000;
        DANGER_THRESHOLD = 1500;
        super.onCreate(savedInstanceState);
    }
}