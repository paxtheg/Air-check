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
    private static final int MAX_DATA_POINTS = 50;

    public static void saveData(Context context, String type, float value) throws JSONException {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        String key = getKeyForType(type);
        List<Float> data = loadDataList(context, key);

        // Add new value and maintain only the last MAX_DATA_POINTS values
        data.add(value);
        while (data.size() > MAX_DATA_POINTS) {
            data.remove(0);
        }

        // Convert to JSON manually
        JSONArray jsonArray = new JSONArray();
        for (float val : data) {
            jsonArray.put((double)val);  // Convert float to double before putting
        }

        editor.putString(key, jsonArray.toString());
        editor.apply();
    }

    public static List<Float> loadData(Context context, String type) {
        String key = getKeyForType(type);
        return loadDataList(context, key);
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
                // Get as double and cast to float
                data.add((float)jsonArray.getDouble(i));
            }
            return data;
        } catch (JSONException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
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
}