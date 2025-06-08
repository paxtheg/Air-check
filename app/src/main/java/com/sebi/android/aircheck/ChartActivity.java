package com.sebi.android.aircheck;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import android.widget.TextView;

import java.util.Locale;
import java.util.ArrayList;
import java.util.List;

public abstract class ChartActivity extends AppCompatActivity {
    protected LineChart chart;
    protected String dataType;
    protected String chartTitle;
    protected String valueSuffix;
    protected int color;
    protected float WARNING_THRESHOLD = 1000;
    protected float DANGER_THRESHOLD = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);

        chart = findViewById(R.id.chart);

        TextView titleTextView = findViewById(R.id.chartTitle);
        titleTextView.setText(chartTitle);

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
        legend.setCustom(new ArrayList<LegendEntry>() {{
            add(new LegendEntry("Safe", Legend.LegendForm.CIRCLE, 8f, 8f, null, Color.GREEN));
            add(new LegendEntry("Warning", Legend.LegendForm.CIRCLE, 8f, 8f, null, Color.parseColor("#FFA500")));
            add(new LegendEntry("Danger", Legend.LegendForm.CIRCLE, 8f, 8f, null, Color.RED));
        }});

        Description description = chart.getDescription();
        description.setTextColor(textColor);

        leftAxis.setGridColor(Color.parseColor("#888888"));
        xAxis.setGridColor(Color.parseColor("#888888"));

        SimpleMarkerView mv = new SimpleMarkerView(this, R.layout.simple_marker_view);
        mv.setChartView(chart);
        chart.setMarker(mv);

        chart.setBackgroundColor(Color.TRANSPARENT);
        chart.getAxisRight().setEnabled(false);
    }

    private class SimpleMarkerView extends MarkerView {
        private TextView tvValue;

        public SimpleMarkerView(Context context, int layoutResource) {
            super(context, layoutResource);
            tvValue = findViewById(R.id.tvValue);
        }

        @Override
        public void refreshContent(Entry e, Highlight highlight) {
            tvValue.setText(String.format(Locale.getDefault(), "%.1f %s", e.getY(), valueSuffix));
            super.refreshContent(e, highlight);
        }

        @Override
        public MPPointF getOffset() {
            return new MPPointF(-(getWidth() / 2), -getHeight());
        }
    }


    protected void updateChart() {
        List<Float> dataValues = DataStorageHelper.loadData(this, dataType);
        List<Entry> entries = new ArrayList<>();

        for (int i = 0; i < dataValues.size(); i++) {
            entries.add(new Entry(i, dataValues.get(i)));
        }

        if (!entries.isEmpty()) {
            LineDataSet dataSet = new LineDataSet(entries, chartTitle);
            //dataSet.setColor(color);
            dataSet.setValueTextColor(Color.WHITE);
            dataSet.setLineWidth(2f);
            dataSet.setCircleColor(color);
            dataSet.setCircleRadius(4f);
            dataSet.setValueTextSize(10f);
            dataSet.setDrawValues(false);


            dataSet.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return String.format("%.1f %s", value, valueSuffix);
                }
            });

            applyDynamicColors(dataSet, entries);

            LineData lineData = new LineData(dataSet);
            chart.setData(lineData);
            chart.invalidate();
        }
    }

    private void applyDynamicColors(LineDataSet dataSet, List<Entry> entries) {
        int[] circleColors = new int[entries.size()];
        int[] lineColors = new int[entries.size() - 1];

        for (int i = 0; i < entries.size(); i++) {
            float value = entries.get(i).getY();


            if (value > DANGER_THRESHOLD) {
                circleColors[i] = Color.RED;
            } else if (value > WARNING_THRESHOLD) {
                circleColors[i] = Color.parseColor("#FFA500");
            } else {
                circleColors[i] = Color.GREEN;
            }


            if (i > 0) {
                float prevValue = entries.get(i-1).getY();
                float avgValue = (value + prevValue) / 2;

                if (avgValue > DANGER_THRESHOLD) {
                    lineColors[i-1] = Color.RED;
                } else if (avgValue > WARNING_THRESHOLD) {
                    lineColors[i-1] = Color.parseColor("#FFA500");
                } else {
                    lineColors[i-1] = Color.GREEN;
                }
            }
        }

        dataSet.setCircleColors(circleColors);
        dataSet.setColors(lineColors);
    }
}