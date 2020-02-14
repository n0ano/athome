package com.n0ano.athome;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

// Created by n0ano on 10/10/16.
//
// Class to handle weather data
//
public class WeatherPWS
{

private final static String WUNDER_URL = "https://api.weather.com";
private final static String WUNDER_API = "/v2/pws/observations/current";
private final static String WUNDER_ID = "stationId=";
private final static String WUNDER_KEY = "&apiKey=";
private final static String WUNDER_QUERY = "&units=e&format=json&numericPrecision=decimal";

private static JSONObject find_station(String id, JSONObject json)
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

private static boolean get_info(WeatherStation ws, JSONObject json)
{
    String key;
    String val;

    JSONObject station = find_station(ws.id, json);
    if (station == null)
        return false;
    JSONObject units = (JSONObject)station.opt("imperial");

    ws.set_temp(units.optDouble("temp", 1000.0));
    ws.set_baro(Double.valueOf(units.optDouble("pressure", 0.0)));
    ws.wind_dir = station.optInt("winddir", 0);
    ws.wind_speed = units.optDouble("windSpeed", 0.0);
    ws.humidity = station.optDouble("humidity", 0.0);
    ws.precip = units.optDouble("precipTotal", 0.0);

    return true;
}

public static boolean get_data(WeatherStation ws, Http http)
{
    JSONObject json;
    Http.R resp;

    resp = http.call_api(WUNDER_URL + WUNDER_API,
                         WUNDER_ID + ws.id +
                         WUNDER_KEY + ws.key +
                         WUNDER_QUERY);
    try {
        json = new JSONObject(resp.body);
    } catch (Exception e) {
        Log.d("get_wunder: no data for station " + ws.name);
        return false;
    }
    return get_info(ws, json);
}

}
