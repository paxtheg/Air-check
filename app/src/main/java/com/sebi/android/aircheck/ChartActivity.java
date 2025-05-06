package com.sebi.android.aircheck;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.Description;
import java.util.ArrayList;
import java.util.List;

public abstract class ChartActivity extends AppCompatActivity {
    protected LineChart chart;
    protected String dataType;
    protected String chartTitle;
    protected String valueSuffix;
    protected int color;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);

        chart = findViewById(R.id.chart);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        setupChart();
        updateChart();
    }

    private void setupChart() {
        // Configure chart appearance
        chart.getDescription().setEnabled(true);
        chart.getDescription().setText(chartTitle);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(true);
        chart.setDrawGridBackground(false);

        int textColor = Color.WHITE;

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(textColor);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setTextColor(textColor);

        Legend legend = chart.getLegend();
        legend.setTextColor(textColor);

        Description description = chart.getDescription();
        description.setTextColor(textColor);

        leftAxis.setGridColor(Color.parseColor("#888888"));
        xAxis.setGridColor(Color.parseColor("#888888"));

        chart.setBackgroundColor(Color.BLACK);
        chart.getAxisRight().setEnabled(false);
    }

    protected void updateChart() {
        List<Float> dataValues = DataStorageHelper.loadData(this, dataType);
        List<Entry> entries = new ArrayList<>();

        for (int i = 0; i < dataValues.size(); i++) {
            entries.add(new Entry(i, dataValues.get(i)));
        }

        if (!entries.isEmpty()) {
            LineDataSet dataSet = new LineDataSet(entries, chartTitle);
            dataSet.setColor(color);
            dataSet.setValueTextColor(Color.BLACK);
            dataSet.setLineWidth(2f);
            dataSet.setCircleColor(color);
            dataSet.setCircleRadius(4f);
            dataSet.setValueTextSize(10f);

            // Create a proper ValueFormatter
            dataSet.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return String.format("%.1f %s", value, valueSuffix);
                }
            });

            LineData lineData = new LineData(dataSet);
            chart.setData(lineData);
            chart.invalidate(); // refresh
        }
    }
}