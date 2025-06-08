package com.sebi.android.aircheck;

import android.os.Bundle;
import android.graphics.Color;

public class HumidityDataActivity extends ChartActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        dataType = "humidity";
        chartTitle = "Humidity over the last 25 minutes";
        valueSuffix = "%";
        WARNING_THRESHOLD = 60;
        DANGER_THRESHOLD = 70;
        super.onCreate(savedInstanceState);
    }
}