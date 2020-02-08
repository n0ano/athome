package com.n0ano.athome;

import android.app.Dialog;
import android.os.SystemClock;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

// Created by n0ano on 10/10/16.
//
// Class to handle weather data
//
public class Weather
{

public final static int PERIOD = (60*1000);   // weather only changes once a minute

private final static int MAX_BARO = 10; // barometer trends over 10 minutes

private final static int MINMAX_NONE =  0;  // No min/max data yet
private final static int MINMAX_VALID = 1;  // min/max data available
private final static int MINMAX_SET =   2;  // min/max data set in GaugeView

public final static String WUNDER_URL = "https://api.weather.com";
public final static String WUNDER_API = "/v2/pws/observations/current";
public final static String WUNDER_ID = "stationId=";
public final static String WUNDER_KEY = "&apiKey=";
public final static String WUNDER_QUERY = "&units=e&format=json&numericPrecision=decimal";

private final static String DATA_TEMPLATE =
"{\"temp\":0.0,\"winddir\":0,\"windSpeed\":0.0,\"precipTotal\":0.0,\"pressure\":0.0,\"humidity\":0.0}";

MainActivity act;
Popup popup;

boolean running = true;
boolean paused = false;

JSONObject w_data;

ArrayList<Double> baro_hist = new ArrayList<Double>();
Double baro_cum = 0.0;
Double baro_avg = 0.0;
int baro_icon;

float min_temp = 200.0f;
float max_temp = -100.0f;
String min_temp_time;
String max_temp_time;

// Weather: class constructor
//
//   act - activity that instantiated the class
//
public Weather(MainActivity act, Popup popup, final DoitCallback cb)
{

	this.act = act;
    this.popup = popup;

    try {
        w_data = new JSONObject(DATA_TEMPLATE);
    } catch (Exception e) {
        Log.d("DDD", "Weather can't create basic data object - " + e);
        return;
    }

    new Thread(new Runnable() {
        public void run() {
            while (running) {
                //
                //  Get the data
                //
                if (!paused) {
                    get_wunder();
                    cb.doit(null);
                }

                SystemClock.sleep(PERIOD);
            }
        }
    }).start();
}

public void stop() { running = false; }
public void pause(boolean p) { paused = p; }

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

private boolean get_info_wunder(JSONObject json)
{
    String key;
    String val;

    JSONObject station = find_station(P.get_string("weather:wunder_id"), json);
    if (station == null)
        return false;
    JSONObject units = (JSONObject)station.opt("imperial");

    int wdir = station.optInt("winddir", 0);
    double humid = station.optDouble("humidity", 0.0);

    double temp = units.optDouble("temp", 1000.0);
    if (temp < 1000.0) {
        if (temp < min_temp) {
            min_temp = (float)temp;
            min_temp_time = Calendar.getInstance().getTime().toString();
        }
        if (temp > max_temp) {
            max_temp = (float)temp;
            max_temp_time = Calendar.getInstance().getTime().toString();
        }
    }

    double f = Double.valueOf(units.optDouble("pressure", 0.0));
    baro_hist.add(f);
    int max = baro_hist.size();
    if (max > MAX_BARO) {
        baro_cum -= baro_hist.get(0);
        baro_hist.remove(--max);
    }
    baro_cum += f;
    baro_avg = baro_cum / baro_hist.size();
    if (f > baro_avg)
        baro_icon = R.drawable.barometer_up;
    else if (f < baro_avg)
        baro_icon = R.drawable.barometer_down;
    else
        baro_icon = R.drawable.barometer;

    double windSpeed = units.optDouble("windSpeed", 0.0);
    double pressure = units.optDouble("pressure", 0.0);
    double precipTotal = units.optDouble("precitTotal", 0.0);

    try {
        w_data.put("temp", temp);
        w_data.put("humidity", humid);
        w_data.put("winddir", wdir);
        w_data.put("windSpeed", windSpeed);
        w_data.put("pressure", pressure);
        w_data.put("precipTotal", precipTotal);
    } catch (Exception e) {
        Log.d("error putting winddir/humidity - " + e);
        return false;
    }
    return true;
}

private void get_wunder()
{

    String resp = act.call_api(WUNDER_URL + WUNDER_API,
                               WUNDER_ID + P.get_string("weather:wunder_id") +
                               WUNDER_KEY + P.get_string("weather:wunder_key") +
                               WUNDER_QUERY);
    try {
        JSONObject json = new JSONObject(resp);
        get_info_wunder(json);
    } catch (Exception e) {
        Log.d("get_wunder: no data for station " + P.get_string("weather:wunder_id"));
    }
}

public void go_temp_detail(View v)
{

    popup.detail_dialog(max_temp, min_temp, max_temp_time, min_temp_time);
}

public void show(View v)
{

    //
    //  Display it
    //
    TextView tv;
    ImageView iv;
    GaugeView gv;

    if ((gv = (GaugeView) v.findViewById(R.id.weather_temp)) != null) {
        gv.set_minmax(min_temp, max_temp);
        gv.set_value((float)w_data.optDouble("temp", 0.0));
    }

    if ((iv = (ImageView) v.findViewById(R.id.weather_dir)) != null)
        iv.setRotation(Integer.valueOf(w_data.optInt("winddir", 0)));

    if ((tv = (TextView) v.findViewById(R.id.weather_speed)) != null)
        tv.setText(String.format("%.1f", (float)w_data.optDouble("windSpeed", 0.0)));

    if ((tv = (TextView) v.findViewById(R.id.weather_rain)) != null)
        tv.setText(String.format("%.1f", (float)w_data.optDouble("precipTotal", 0.0)));

    if ((iv = (ImageView) v.findViewById(R.id.weather_bar_dir)) != null)
        iv.setImageResource(baro_icon);

    if ((tv = (TextView) v.findViewById(R.id.weather_barometer)) != null)
        tv.setText(String.format("%.1f", w_data.optDouble("pressure", 0.0)));
}

}
