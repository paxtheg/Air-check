package com.sebi.android.aircheck;

import android.os.Bundle;
import android.graphics.Color;

public class HumidityDataActivity extends ChartActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        dataType = "humidity";
        chartTitle = "Humidity over the last 25 minutes";
        valueSuffix = "%";
        color = Color.MAGENTA;
        super.onCreate(savedInstanceState);
    }
}