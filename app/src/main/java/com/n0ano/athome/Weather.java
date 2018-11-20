package com.n0ano.athome;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.n0ano.athome.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
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

private static int ecobee_thermos_checked;

Map<String, String> data = new HashMap<String, String>();
Map<String, String> data_wunder = new HashMap<String, String>();
Map<String, String> data_ecobee = new HashMap<String, String>();

Parse parse;

// Weather: class constructor
//
//   act - activity that instantiated the class
//
public Weather(MainActivity act)
{

	this.act = act;
    parse = act.parse;
    init_data();
}

// Java doesn't have associative arrays but we can accomplish the
//   same thing with a HashMap.  Unfortunately, we can't statically\
//   initialize a HashMap so we have do that in a function;
//
private void init_data()
{

    data.put("in_temp", "--");
    data.put("out_temp", "--");
    data.put("winddir", "0");

    data_wunder.put("tempf", "out_temp");
    data_wunder.put("maxtemp", "out_max");
    data_wunder.put("maxtemp_time", "out_max_time");
    data_wunder.put("mintemp", "out_min");
    data_wunder.put("mintemp_time", "out_min_time");
    data_wunder.put("winddir", "winddir");
    data_wunder.put("windspeedmph", "windspeed");
    data_wunder.put("dailyrainin", "rain");
    data_wunder.put("baromin", "barometer");
    data_wunder.put("humidity", "out_humid");

    data_ecobee.put("%1,actualTemperature", "in_temp");
    data_ecobee.put("actualHumidity", "in_humid");

    ecobee_thermos_checked = 0;
}

private void get_info_wunder(String resp)
{
    String key;
    String val;

    Set keys = data_wunder.keySet();
    for (Iterator itr = keys.iterator(); itr.hasNext();) {
        key = (String)itr.next();
        val = parse.xml_get(key, resp, 1);
        key = data_wunder.get(key);
        data.put(key, (val != null) ? val : "");
    }
}

private void get_eco_thermos(String resp)
{
    String name;
    ArrayList<String> thermos = new ArrayList<String>();

    if (ecobee_thermos_checked++ != 0)
        return;
    int idx = 0;
    while ((name = parse.json_get("name", resp, ++idx)) != null) {
Log.d("thermostat name - " + name);
        thermos.add(name);
    }
    act.ecobee_thermos = new String[--idx];
    for (int i = 0; i < idx; ++i)
        act.ecobee_thermos[i] = thermos.get(i);
}

private void get_info_ecobee(String resp)
{
    String key;
    String val;

    get_eco_thermos(resp);
    Set keys = data_ecobee.keySet();
    int which = act.ecobee_which + 1;
    if (which < 1)
        which = 1;
    for (Iterator itr = keys.iterator(); itr.hasNext();) {
        key = (String)itr.next();
        val = parse.json_get(key, resp, which);
        key = data_ecobee.get(key);
        data.put(key, val);
    }
}

private void get_wunder()
{

    String resp = act.call_api("GET",
                               WUNDER_URL + WUNDER_API,
                               WUNDER_STATION + act.wunder_id + WUNDER_QUERY,
                               "");
//Log.d("under:" + resp);
    if (resp.isEmpty() || resp.contains("<conds></conds>"))
        Log.d("get_wunder: no data for station " + act.wunder_id);
    else
        get_info_wunder(resp);
}

private void ecobee_auth()
{
    String resp;
    String token;

    resp = act.call_api("POST",
                        ECO_URL + ECO_REFRESH,
                        "grant_type=ecobeePin&code=" + act.ecobee_auth +
                            "&client_id=" + act.ecobee_api,
                        "");
    if ((token = parse.json_get("access_token", resp, 1)) == null)
        return;
    act.ecobee_access = parse.json_get("access_token", resp, 1);
    act.ecobee_refresh = parse.json_get("refresh_token", resp, 1);
    Preferences pref = new Preferences(act);
    pref.put_string("ecobee_access", act.ecobee_access);
    pref.put_string("ecobee_refresh", act.ecobee_refresh);
Log.d("ecoBee auth access/refresh - " + act.ecobee_access + "/" + act.ecobee_refresh);
}

private boolean ecobee_refresh()
{
    String resp;
    String token;

    resp = act.call_api("POST",
                        ECO_URL + ECO_REFRESH,
                        "grant_type=refresh_token&code=" + act.ecobee_refresh +
                            "&client_id=" + act.ecobee_api,
                        "");
    if ((token = parse.json_get("access_token", resp, 1)) == null)
        return false;
    act.ecobee_access = parse.json_get("access_token", resp, 1);
    act.ecobee_refresh = parse.json_get("refresh_token", resp, 1);
    Preferences pref = new Preferences(act);
    pref.put_string("ecobee_access", act.ecobee_access);
    pref.put_string("ecobee_refresh", act.ecobee_refresh);
Log.d("ecoBee refresh access/refresh - " + act.ecobee_access + "/" + act.ecobee_refresh);
    return true;
}

private void get_ecobee()
{
    String resp;

    resp = act.call_api("GET",
                               ECO_URL + ECO_DATA,
                               ECO_QUERY,
                               "Bearer " + act.ecobee_access);
//Log.d("ecobee:" + resp);
    String code = parse.json_get("code", resp, 1);
    if (code == null || !code.equals("0")) {
        if (!ecobee_refresh())
            ecobee_auth();
    } else {
        get_info_ecobee(resp);
    }

}

private void detail_dialog()
{

    final Dialog dialog = new Dialog(act, R.style.AlertDialogCustom);
    dialog.setContentView(R.layout.detail);

    TextView tv = (TextView) dialog.findViewById(R.id.detail_max);
    tv.setText(data.get("out_max"));

    tv = (TextView) dialog.findViewById(R.id.detail_min);
    tv.setText(data.get("out_min"));

    tv = (TextView) dialog.findViewById(R.id.detail_max_time);
    tv.setText(data.get("out_max_time"));

    tv = (TextView) dialog.findViewById(R.id.detail_min_time);
    tv.setText(data.get("out_min_time"));

    Button ok = (Button) dialog.findViewById(R.id.detail_ok);
    ok.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            dialog.dismiss();
        }
    });

    dialog.show();
}

public void go_temp_detail(View v)
{

    int id = v.getId();
Log.d("get temp detail for - " + ((id == R.id.weather_out_icon) ? "weather underground" : "ecoBee"));
    detail_dialog();
}

public void update()
{

    //
    //  Get the data
    //
    data.put("in_temp", "--");
    data.put("out_temp", "--");
    get_wunder();
    get_ecobee();

    //
    //  Display it
    //
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
