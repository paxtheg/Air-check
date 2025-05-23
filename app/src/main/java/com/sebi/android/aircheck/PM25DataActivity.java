package com.sebi.android.aircheck;

import android.os.Bundle;
import android.graphics.Color;

public class PM25DataActivity extends ChartActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        dataType = "pm25";
        chartTitle = "PM2.5 Levels over the last 25 minutes";
        valueSuffix = "µg/m³";
        color = Color.BLUE;
        super.onCreate(savedInstanceState);
    }
}