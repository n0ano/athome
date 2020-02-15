package com.n0ano.athome;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

// Created by n0ano on 10/10/16.
//
// Class to handle weather data
//
public class WeatherStation
{

public final static int UNDER =     0;
public final static int OPEN =      1;

public final static String[] types = {
    "Personal WS",
    "City"
};

private final static int MAX_BARO = 10; // barometer trends over 10 minutes

int type;
String name;
String id;
String key;

public double temp = -999.9;
public double min_temp = 999.9;
public String min_time = "";
public double max_temp = -999.9;
public String max_time = "";

ArrayList<Double> baro_hist = new ArrayList<Double>();
double baro_cum = 0.0;
double baro_avg = 0.0;
public double baro = -1.0;
public int baro_icon = R.drawable.barometer;

public int wind_dir = -1;
public double wind_speed = -1.0;

public double precip = -1.0;
public double humidity = -1.0;

private WeatherPWS wunder;
private WeatherCity wopen;

public WeatherStation(int type, String name, String id, String key)
{

    this.type = type;
    this.name = name;
    this.id = id;
    this.key = key;

    min_temp = Double.valueOf(P.get_int("weather:min_temp")) / 10.0f;
    min_time = P.get_string("weather:min_time");
    max_temp = Double.valueOf(P.get_int("weather:max_temp")) / 10.0f;
    max_time = P.get_string("weather:max_time");
}

public String get_name() { return name; }
public String get_id() { return id; }
public String get_key() { return key; }

public void set_temp(double t)
{
    if (t < 1000.0) {
        if (t < min_temp) {
            min_temp = t;
            min_time = Calendar.getInstance().getTime().toString();
        }
        if (t > max_temp) {
            max_temp = t;
            max_time = Calendar.getInstance().getTime().toString();
        }
    }
    temp = t;
}

public void set_baro(double b)
{

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
}

public JSONObject to_json()
{

    JSONObject json = new JSONObject();
    try {
        json.put("type", type);
        json.put("name", name);
        json.put("id", id);
        json.put("key", key);
        json.put("min_temp", min_temp);
        json.put("min_time", min_time);
        json.put("max_temp", max_temp);
        json.put("max_time", max_time);
    } catch (Exception e) {
        Log.d("DDD", "to_json put error - " + e);
    }
Log.d("DDD", "weather: to_json => " + json.toString());
    return json;
}

public void get_data(Http http)
{

    switch (type) {

    case UNDER:
        WeatherPWS.get_data(this, http);
        break;

    case OPEN:
        WeatherCity.get_data(this, http);
        break;

    default:
        Log.d("DDD", name + ": bad station type - " + type);
        return;

    }
}

public void show(View v)
{

    //
    //  Display it
    //
    TextView tv;
    ImageView iv;
    GaugeView gv;
    LinearLayout ll;

    if ((gv = (GaugeView) v.findViewById(R.id.weather_temp)) != null) {
        gv.set_minmax((float)min_temp, (float)max_temp);
        gv.set_name(name);
        gv.set_value((float)temp);
    }

    if ((iv = (ImageView) v.findViewById(R.id.weather_dir)) != null)
        iv.setRotation(wind_dir);

    if ((tv = (TextView) v.findViewById(R.id.weather_speed)) != null)
        tv.setText(String.format("%.1f", wind_speed));

    if ((ll = (LinearLayout)v.findViewById(R.id.weather_precip)) != null) {
        float rain = (float)precip;
        if (rain < 0)
            ll.setVisibility(View.GONE);
        else {
            ll.setVisibility(View.VISIBLE);
            if ((tv = (TextView) v.findViewById(R.id.weather_rain)) != null)
                tv.setText(String.format("%.1f", (float)precip));
        }
    }

    if ((iv = (ImageView) v.findViewById(R.id.weather_bar_dir)) != null)
        iv.setImageResource(baro_icon);

    if ((tv = (TextView) v.findViewById(R.id.weather_barometer)) != null)
        tv.setText(String.format("%.1f", baro));
}

}
