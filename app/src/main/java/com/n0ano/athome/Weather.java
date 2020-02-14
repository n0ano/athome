package com.n0ano.athome;

import android.app.Dialog;
import android.os.SystemClock;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

// Created by n0ano on 10/10/16.
//
// Class to handle weather data
//
public class Weather
{

public final static int PERIOD = (60*1000);   // weather only changes once a minute

private final static String DATA_TEMPLATE =
"{\"temp\":0.0,\"winddir\":0,\"windSpeed\":0.0,\"precipTotal\":0.0,\"pressure\":0.0,\"humidity\":0.0}";

Popup popup;

boolean running = true;
boolean paused = false;

Http http;

ArrayList<WeatherStation> stations;
int station_idx = 0;

JSONArray json;

// Weather: class constructor
//
public Weather(Popup popup, final DoitCallback cb)
{
    WeatherStation station;

    this.popup = popup;

    http = new Http();

    stations = new ArrayList<WeatherStation>();

    json = (JSONArray)P.get_JSONObject("weather:stations");
    if (json == null)
        json = new JSONArray();

Log.d("DDD", "weather: stations - " + json.toString());

    int max = json.length();
    for (int i = 0; i < max; i++)
        if ((station = json_entry(json, i)) != null)
            stations.add(station);

    new Thread(new Runnable() {
        public void run() {
            while (running) {
                //
                //  Get the data
                //
                if (!paused) {
                    get_data();
                    cb.doit(0, null);
                }

                SystemClock.sleep(PERIOD);
            }
        }
    }).start();
}

private WeatherStation json_entry(JSONArray stations, int idx)
{
    WeatherStation station;

    try {
        JSONObject entry = stations.getJSONObject(idx);
        station = new WeatherStation(entry.getInt("type"),
                                     entry.getString("name"),
                                     entry.getString("id"),
                                     entry.getString("key"));
    } catch (Exception e) {
        Log.d("DDD", "weather station JSON error - " + e);
        return null;
    }
    return station;
}

public void add(WeatherStation station)
{

    stations.add(station);
    json.put(station.to_json());
    P.put("weather:stations", json);
}

public void add(int type, String name, String id, String key)
{

    add(new WeatherStation(type, name, id, key));
}

public void update(int idx, int type, String name, String id, String key)
{

    stations.remove(idx);
    stations.add(idx, new WeatherStation(type, name, id, key));
}

public void remove(int idx)
{

    stations.remove(idx);
}

public void stop()
{

    running = false;
}

public void pause(boolean p) { paused = p; }

public void go_temp_detail(View v)
{

Log.d("DDD", "weather: temp detail WIP");
    //popup.detail_dialog((float)station.max_temp, (float)station.min_temp, station.max_time, station.min_time);
}

private void get_data()
{
    WeatherStation ws;

    int max = stations.size();
    for (int i = 0; i < max; i++)
        stations.get(i).get_data(http);
}

public void cycle(int dir, View v)
{

    station_idx += dir;
    int max = stations.size();
    if (max == 0) {
        station_idx = 0;
        return;
    } else if (station_idx < 0)
        station_idx = max - 1;
    else if (station_idx >= max)
        station_idx = 0;
    show(v);
}

public void show(View v)
{

    if (station_idx < stations.size())
        stations.get(station_idx).show(v);
}

}
