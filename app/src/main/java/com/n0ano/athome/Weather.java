package com.n0ano.athome;

import android.app.Activity;
import android.widget.ImageView;
import android.widget.TextView;

import com.n0ano.athome.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

// Created by n0ano on 10/10/16.
//
// Class to handle weather data
//
public class Weather {

public final static String WUNDER_URL = "https://stationdata.wunderground.com";
public final static String WUNDER_API = "/cgi-bin/stationlookup";
public final static String WUNDER_STATION = "station=";
public final static String WUNDER_QUERY = "&units=english&v=2.0&format=xml";

public final static String ECO_URL = "https://api.ecobee.com";
public final static String ECO_REFRESH = "/token";
public final static String ECO_DATA = "/1/thermostat";
public final static String ECO_QUERY = "format=json&body={\"selection\":{\"selectionType\":\"registered\",\"selectionMatch\":\"\",\"includeRuntime\":true}}";

MainActivity act;

Map<String, String> data = new HashMap<String, String>();
Map<String, String> data_under = new HashMap<String, String>();
Map<String, String> data_ecobee = new HashMap<String, String>();

// Weather: class constructor
//
//   act - activity that instantiated the class
//
public Weather(MainActivity act)
{

	this.act = act;
    init_data();
}

// Java doesn't have associative arrays but we can accomplish the
//   same thing with a HashMap.  Unfortunately, we can't statically\
//   initialize a HashMap so we have do that in a function;
//
private void init_data()
{

    data.put("in_temp", "70");
    data.put("out_temp", "70");
    data.put("winddir", "0");

    data_under.put("tempf", "out_temp");
    data_under.put("winddir", "winddir");
    data_under.put("windspeedmph", "windspeed");
    data_under.put("dailyrainin", "rain");
    data_under.put("baromin", "barometer");

    data_ecobee.put("actualTemperature", "in_temp");
}

private void get_info_xml(String resp)
{
    String key;
    String val;

    Set keys = data_under.keySet();
    for (Iterator itr = keys.iterator(); itr.hasNext();) {
        key = (String)itr.next();
        val = Common.xml_get(key, resp, 1);
        if (!val.isEmpty()) {
            key = data_under.get(key);
            data.put(key, val);
        }
    }
}

private void get_info_json(String resp)
{
    String key;
    int val;

    Set keys = data_ecobee.keySet();
    for (Iterator itr = keys.iterator(); itr.hasNext();) {
        key = (String)itr.next();
        val = Common.json_get_int(key, resp, 2);
        data.put(data_ecobee.get(key), String.format("%.1f", (float)val/10));
    }
}

private void get_wunder()
{

    String resp = act.call_api("GET",
                               WUNDER_URL + WUNDER_API,
                               WUNDER_STATION + act.wunder_id + WUNDER_QUERY,
                               "");
    if (resp.isEmpty() || resp.contains("<conds></conds>"))
        Log.d("get_wunder: no data for station " + act.wunder_id);
    else
        get_info_xml(resp);
}

private void get_ecobee()
{
    String resp;

    resp = act.call_api("GET",
                               ECO_URL + ECO_DATA,
                               ECO_QUERY,
                               "Bearer " + act.ecobee_access);
//Log.d("ecobee:" + resp);
    int code = Common.json_get_int("code", resp, 1);
    if (resp.isEmpty() || code != 0) {
        resp = act.call_api("POST",
                            ECO_URL + ECO_REFRESH,
                            "grant_type=refresh_token&code=" + act.ecobee_refresh +
                                "&client_id=" + act.ecobee_api,
                            "");
        act.ecobee_access = Common.json_get("access_token", resp, 1);
        act.ecobee_refresh = Common.json_get("refresh_token", resp, 1);
        Preferences pref = new Preferences(act);
        pref.put_string("ecobee_access", act.ecobee_access);
        pref.put_string("ecobee_refresh", act.ecobee_refresh);
Log.d("get_ecobee access/refresh - " + act.ecobee_access + "/" + act.ecobee_refresh);
    } else {
        get_info_json(resp);
    }

}

public void get_data()
{

    data.put("in_temp", "--");
    data.put("out_temp", "--");
    get_wunder();
    get_ecobee();
}

public void show_data()
{

    act.runOnUiThread(new Runnable() {
        public void run() {
            TextView tv;

            tv = (TextView) act.findViewById(R.id.weather_out_temp);
            tv.setText(data.get("out_temp"));

            tv = (TextView) act.findViewById(R.id.weather_in_temp);
            tv.setText(data.get("in_temp"));

            ImageView iv = (ImageView) act.findViewById(R.id.weather_dir);
            iv.setRotation(Integer.valueOf(data.get("winddir")));

            tv = (TextView) act.findViewById(R.id.weather_speed);
            tv.setText(data.get("windspeed"));

            tv = (TextView) act.findViewById(R.id.weather_rain);
            tv.setText(data.get("rain") + " in");

            tv = (TextView) act.findViewById(R.id.weather_barometer);
            tv.setText(data.get("barometer") + " in");
        }
    });
}

}
