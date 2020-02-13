package com.n0ano.athome;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

// Created by n0ano on 10/10/16.
//
// Class to handle weather data
//
public class WeatherUnder
{

private final static String WUNDER_URL = "https://api.weather.com";
private final static String WUNDER_API = "/v2/pws/observations/current";
private final static String WUNDER_ID = "stationId=";
private final static String WUNDER_KEY = "&apiKey=";
private final static String WUNDER_QUERY = "&units=e&format=json&numericPrecision=decimal";

private final static int MAX_BARO = 10; // barometer trends over 10 minutes

String name;
String id;
String key;

Http http;

double temp = -999.9;
double min_temp = 999.9;
String min_time = "";
double max_temp = -999.9;
String max_time = "";

ArrayList<Double> baro_hist = new ArrayList<Double>();
double baro_cum = 0.0;
double baro_avg = 0.0;
double baro = -1.0;
int baro_icon = R.drawable.barometer;

int wind_dir = -1;
double wind_speed = -1.0;

double precip = -1.0;
double humidity = -1.0;

public WeatherUnder(String name, String id, String key)
{

    this.name = name;
    this.id = id;
    this.key = key;

    http = new Http();

    min_temp = Double.valueOf(P.get_int("weather:min_temp")) / 10.0f;
    min_time = P.get_string("weather:min_time");
    max_temp = Double.valueOf(P.get_int("weather:max_temp")) / 10.0f;
    max_time = P.get_string("weather:max_time");
}

private JSONObject find_station(String id, JSONObject json)
{
    JSONObject station;
    String sid;

    JSONArray observe = json.optJSONArray("observations");
    int max = observe.length();
    for (int i = 0; i < max; i++) {
        station = (JSONObject)observe.opt(0);
        sid = station.optString("stationID", "");
        if (sid.equals(id))
            return station;
    }
    return null;
}

private boolean get_info(JSONObject json)
{
    String key;
    String val;

    JSONObject station = find_station(id, json);
    if (station == null)
        return false;
    JSONObject units = (JSONObject)station.opt("imperial");

    wind_dir = station.optInt("winddir", 0);
    wind_speed = units.optDouble("windSpeed", 0.0);
    humidity = station.optDouble("humidity", 0.0);
    precip = units.optDouble("precitTotal", 0.0);

    temp = units.optDouble("temp", 1000.0);
    if (temp < 1000.0) {
        if (temp < min_temp) {
            min_temp = (float)temp;
            min_time = Calendar.getInstance().getTime().toString();
            P.put("weather:min_temp", (int)(min_temp * 10.0f));
            P.put("weather:min_time", min_time);
        }
        if (temp > max_temp) {
            max_temp = (float)temp;
            max_time = Calendar.getInstance().getTime().toString();
            P.put("weather:max_temp", (int)(min_temp * 10.0f));
            P.put("weather:max_time", max_time);
        }
    }

    baro = Double.valueOf(units.optDouble("pressure", 0.0));
    baro_hist.add(baro);
    int max = baro_hist.size();
    if (max > MAX_BARO) {
        baro_cum -= baro_hist.get(0);
        baro_hist.remove(--max);
    }
    baro_cum += baro;
    baro_avg = baro_cum / baro_hist.size();
    if (baro > baro_avg)
        baro_icon = R.drawable.barometer_up;
    else if (baro < baro_avg)
        baro_icon = R.drawable.barometer_down;
    else
        baro_icon = R.drawable.barometer;

    return true;
}

public void get_data()
{
    JSONObject json;
    Http.R resp;

    resp = http.call_api(WUNDER_URL + WUNDER_API,
                         WUNDER_ID + id +
                         WUNDER_KEY + key +
                         WUNDER_QUERY);
    try {
        json = new JSONObject(resp.body);
    } catch (Exception e) {
        Log.d("get_wunder: no data for station " + name);
        return;
    }
    if (!get_info(json))
        Log.d("get_wunder: bad data(" + resp.body + ") for station " + name);
}

}
