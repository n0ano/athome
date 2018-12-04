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

private final static int PERIOD = 60;   // weather only changes once a minute

private final static int MAX_BARO = 10; // barometer trends over 10 minutes

public final static String WUNDER_URL = "https://stationdata.wunderground.com";
public final static String WUNDER_API = "/cgi-bin/stationlookup";
public final static String WUNDER_STATION = "station=";
public final static String WUNDER_QUERY = "&units=english&v=2.0&format=xml";

public final static String ECO_URL = "https://api.ecobee.com";
public final static String ECO_AUTHORIZE = "/authorize";
public final static String ECO_REFRESH = "/token";
public final static String ECO_DATA = "/1/thermostat";
public final static String ECO_QUERY = "format=json&body={\"selection\":{\"selectionType\":\"registered\",\"selectionMatch\":\"\",\"includeRuntime\":true}}";

MainActivity act;

private int period = PERIOD;        // Weather only changes once a minute

private static int ecobee_thermos_checked;
private String ecobee_auth_token = "";

Map<String, String> data = new HashMap<String, String>();
Map<String, String> data_wunder = new HashMap<String, String>();
Map<String, String> data_ecobee = new HashMap<String, String>();

ArrayList<Float> baro_hist = new ArrayList<Float>();
Float baro_cum = 0.0f;
Float baro_avg = 0.0f;
int baro_icon;

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
        val = act.parse.xml_get(key, resp, 1);
        key = data_wunder.get(key);
        data.put(key, (val != null) ? val : "");
    }
    Float f = Float.valueOf(data.get("barometer"));
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
}

private void get_eco_thermos(String resp)
{
    String name;
    ArrayList<String> thermos = new ArrayList<String>();

    if (ecobee_thermos_checked++ != 0)
        return;
    int idx = 0;
    while ((name = act.parse.json_get("name", resp, ++idx)) != null) {
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
        val = act.parse.json_get(key, resp, which);
        if (val != null) {
            key = data_ecobee.get(key);
            data.put(key, val);
        }
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

//  The api token can be used to create a new PIN
//
//  Note: this routine makes asynchronous HTTP requests
//    so it better not run on the UI thread
//
public String ecobee_get_pin(String api)
{
    String resp;

    resp = act.call_api("GET",
                               ECO_URL + ECO_AUTHORIZE,
                               "response_type=ecobeePin&client_id=" + api +
                                   "&scope=smartWrite",
                               "");
    ecobee_auth_token = act.parse.json_get("code", resp, 1);
    String pin = act.parse.json_get("ecobeePin", resp, 1);
Log.d("ecobee get pin for api - " + api + " = " + pin + "/" + ecobee_auth_token);
    return pin;
}

private void ecobee_token(String type, String code, String api)
{
    String resp;
    String token;

    resp = act.call_api("POST",
                        ECO_URL + ECO_REFRESH,
                        "grant_type=" + type +
                            "&code=" + code +
                            "&client_id=" + api,
                        "");
    if ((token = act.parse.json_get("access_token", resp, 1)) == null)
        return;
    act.ecobee_access = act.parse.json_get("access_token", resp, 1);
    act.ecobee_refresh = act.parse.json_get("refresh_token", resp, 1);
    Preferences pref = new Preferences(act);
    pref.put_string("ecobee_access", act.ecobee_access);
    pref.put_string("ecobee_refresh", act.ecobee_refresh);
Log.d("ecoBee tokens access/refresh - " + act.ecobee_access + "/" + act.ecobee_refresh);
    return;
}

//  The token has been validated by the user so
//    it's legal to use the auth token to get
//    access and refresh tokens
//
//  Note: this routine makes asynchronous HTTP requests
//    so it better not run on the UI thread
//
public void ecobee_authorize(String api)
{

    ecobee_token("ecobeePin", ecobee_auth_token, api);
}

private void ecobee_refresh()
{

    ecobee_token("refresh_token", act.ecobee_refresh, act.ecobee_api);
}

private String ecobee_query()
{
    String resp;

    resp = act.call_api("GET",
                               ECO_URL + ECO_DATA,
                               ECO_QUERY,
                               "Bearer " + act.ecobee_access);
    String code = act.parse.json_get("code", resp, 1);
    if (code == null || !code.equals("0"))
        return null;
    else
        return resp;
}

private void get_ecobee()
{
    String resp;

    if ((resp = ecobee_query()) == null) {
        ecobee_refresh();
        if ((resp = ecobee_query()) == null)
            return;
    }
    get_info_ecobee(resp);
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
    if (period++ >= PERIOD) {
        data.put("in_temp", "--");
        data.put("out_temp", "--");
        get_wunder();
        get_ecobee();
        period = 1;
    }

    //
    //  Display it
    //
    act.runOnUiThread(new Runnable() {
        public void run() {
            TextView tv;
            ImageView iv;

            tv = (TextView) act.findViewById(R.id.weather_out_temp);
            tv.setText(data.get("out_temp"));

            tv = (TextView) act.findViewById(R.id.weather_in_temp);
            tv.setText(data.get("in_temp"));

            iv = (ImageView) act.findViewById(R.id.weather_dir);
            iv.setRotation(Integer.valueOf(data.get("winddir")));

            tv = (TextView) act.findViewById(R.id.weather_speed);
            tv.setText(data.get("windspeed"));

            tv = (TextView) act.findViewById(R.id.weather_rain);
            tv.setText(data.get("rain") + " in");

            iv = (ImageView) act.findViewById(R.id.weather_bar_dir);
            iv.setImageResource(baro_icon);

            tv = (TextView) act.findViewById(R.id.weather_barometer);
            tv.setText(data.get("barometer") + " in");

            iv = (ImageView) act.findViewById(R.id.weather_out_timeout);
            act.set_timeout(iv, period, PERIOD);

            iv = (ImageView) act.findViewById(R.id.weather_in_timeout);
            act.set_timeout(iv, period, PERIOD);
        }
    });
}

}
