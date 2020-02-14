package com.n0ano.athome;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

// Created by n0ano on 10/10/16.
//
// Class to handle weather data
//
public class WeatherCity
{

private final static String WOPEN_URL = "http://api.openweathermap.org";
private final static String WOPEN_API = "/data/2.5/weather";
private final static String WOPEN_ID = "id=";
private final static String WOPEN_KEY = "&appid=";
private final static String WOPEN_QUERY = "&units=imperial";

private final static double HPA2IN = 33.863886666667;

private static boolean get_info(WeatherStation ws, JSONObject json)
{
    String key;
    String val;

    JSONObject main = (JSONObject)json.opt("main");
    ws.set_temp(main.optDouble("temp", 1000.0));
    ws.set_baro(Double.valueOf(main.optDouble("pressure", 0.0)) / HPA2IN);
    ws.humidity = main.optInt("humidity", 0);

    JSONObject wind = (JSONObject)json.opt("wind");
    ws.wind_dir = wind.optInt("deg", 0);
    ws.wind_speed = wind.optDouble("speed", 0.0);

    return true;
}

public static boolean get_data(WeatherStation ws, Http http)
{
    JSONObject json;
    Http.R resp;

    resp = http.call_api(WOPEN_URL + WOPEN_API,
                         WOPEN_ID + ws.id +
                         WOPEN_KEY + ws.key +
                         WOPEN_QUERY);
    try {
        json = new JSONObject(resp.body);
    } catch (Exception e) {
        Log.d("get_open: no data for station " + ws.name);
        return false;
    }
    return get_info(ws, json);
}

}
