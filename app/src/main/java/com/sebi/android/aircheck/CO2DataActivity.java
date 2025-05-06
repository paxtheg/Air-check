package com.sebi.android.aircheck;

import android.graphics.Color;
import android.os.Bundle;

public class CO2DataActivity extends ChartActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        dataType = "co2";
        chartTitle = "CO2 Levels Over Time";
        valueSuffix = "ppm";
        color = Color.RED;
        super.onCreate(savedInstanceState);
    }
}