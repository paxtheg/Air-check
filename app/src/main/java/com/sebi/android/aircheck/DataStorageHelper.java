package com.sebi.android.aircheck;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONException;
import java.util.ArrayList;
import java.util.List;

public class DataStorageHelper {
    private static final String PREFS_NAME = "AirQualityPrefs";
    private static final String CO2_KEY = "co2_data";
    private static final String PM25_KEY = "pm25_data";
    private static final String TEMP_KEY = "temp_data";
    private static final String HUMIDITY_KEY = "humidity_data";
    private static final String CO2_TIMESTAMPS = "co2_timestamps";
    private static final String PM25_TIMESTAMPS = "pm25_timestamps";
    private static final String TEMP_TIMESTAMPS = "temp_timestamps";
    private static final String HUMIDITY_TIMESTAMPS = "humidity_timestamps";
    private static final int MAX_DATA_POINTS = 50;

    public static void saveData(Context context, String type, float value) throws JSONException {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Save value data
        String key = getKeyForType(type);
        List<Float> data = loadDataList(context, key);
        data.add(value);
        while (data.size() > MAX_DATA_POINTS) {
            data.remove(0);
        }
        editor.putString(key, convertListToJson(data));

        // Save timestamp
        String timestampKey = getTimestampKeyForType(type);
        List<Long> timestamps = loadTimestampsList(context, timestampKey);
        timestamps.add(System.currentTimeMillis());
        while (timestamps.size() > MAX_DATA_POINTS) {
            timestamps.remove(0);
        }
        editor.putString(timestampKey, convertTimestampsToJson(timestamps));

        editor.apply();
    }

    public static List<Float> loadData(Context context, String type) {
        String key = getKeyForType(type);
        return loadDataList(context, key);
    }

    public static List<Long> loadTimestamps(Context context, String type) {
        String timestampKey = getTimestampKeyForType(type);
        return loadTimestampsList(context, timestampKey);
    }

    private static List<Float> loadDataList(Context context, String key) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(key, "");

        if (json.isEmpty()) {
            return new ArrayList<>();
        }

        try {
            JSONArray jsonArray = new JSONArray(json);
            List<Float> data = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                data.add((float)jsonArray.getDouble(i));
            }
            return data;
        } catch (JSONException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private static List<Long> loadTimestampsList(Context context, String key) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(key, "");

        if (json.isEmpty()) {
            return new ArrayList<>();
        }

        try {
            JSONArray jsonArray = new JSONArray(json);
            List<Long> timestamps = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                timestamps.add(jsonArray.getLong(i));
            }
            return timestamps;
        } catch (JSONException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private static String convertListToJson(List<Float> list) throws JSONException {
        JSONArray jsonArray = new JSONArray();
        for (float val : list) {
            jsonArray.put((double)val);
        }
        return jsonArray.toString();
    }

    private static String convertTimestampsToJson(List<Long> list) throws JSONException {
        JSONArray jsonArray = new JSONArray();
        for (long val : list) {
            jsonArray.put(val);
        }
        return jsonArray.toString();
    }

    private static String getKeyForType(String type) {
        switch (type) {
            case "co2": return CO2_KEY;
            case "pm25": return PM25_KEY;
            case "temp": return TEMP_KEY;
            case "humidity": return HUMIDITY_KEY;
            default: return "";
        }
    }

    private static String getTimestampKeyForType(String type) {
        switch (type) {
            case "co2": return CO2_TIMESTAMPS;
            case "pm25": return PM25_TIMESTAMPS;
            case "temp": return TEMP_TIMESTAMPS;
            case "humidity": return HUMIDITY_TIMESTAMPS;
            default: return "";
        }
    }
}