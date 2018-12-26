package com.n0ano.athome;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.RadioGroup;
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

public final static int PERIOD = 60;   // weather only changes once a minute

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

public int period = PERIOD;        // Weather only changes once a minute

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
    String name, id;
    String mode, hmin, hmax, cmin, cmax, cdelta;

    if (act.ecobee_data.size() > 0)
        return;
    int idx = 0;
    while ((name = act.parse.json_get("name", resp, ++idx)) != null) {
        id = act.parse.json_get("identifier", resp, idx);
        act.ecobee_data.add(new EcobeeData(name, id));
Log.d("thermostat: " + name);
    }
    --idx;
    if ((resp = ecobee_query("includeSettings", "")) == null)
        return;
    for (int i = 0; i < idx; i++) {
        mode = act.parse.json_get("hvacMode", resp, i + 1);
        hmin = act.parse.json_get("heatRangeLow", resp, i + 1);
        hmax = act.parse.json_get("heatRangeHigh", resp, i + 1);
        cmin = act.parse.json_get("coolRangeLow", resp, i + 1);
        cmax = act.parse.json_get("coolRangeHigh", resp, i + 1);
        cdelta = act.parse.json_get("heatCoolMinDelta", resp, i + 1);
        act.ecobee_data.get(i).set_range(mode,
                                         Integer.parseInt(hmin),
                                         Integer.parseInt(hmax),
                                         Integer.parseInt(cmin),
                                         Integer.parseInt(cmax),
                                         Integer.parseInt(cdelta));
    }
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
    ecobee_getstate(act.ecobee_which);
}

private void get_wunder()
{

    String resp = act.call_api("GET",
                               WUNDER_URL + WUNDER_API,
                               WUNDER_STATION + act.wunder_id + WUNDER_QUERY,
                               "",
                               null);
//Log.d("under:" + resp);
    if (resp.isEmpty() || resp.contains("<conds></conds>"))
        Log.d("get_wunder: no data for station " + act.wunder_id);
    else
        get_info_wunder(resp);
}

private String ecobee_param(String info, String id)
{

    String select = id.isEmpty() ? "registered" : "thermostats";
    return "format=json&body={\"selection\":{\"selectionType\":\"" + select +
           "\",\"selectionMatch\":\"" + id +
           "\",\"" + info + "\":true}}";
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
                               "",
                               null);
    act.ecobee_access = act.parse.json_get("code", resp, 1);
    Preferences pref = new Preferences(act);
    pref.put_string("ecobee_access", act.ecobee_access);
    String pin = act.parse.json_get("ecobeePin", resp, 1);
Log.d("ecobee get pin for api - " + api + " = " + pin + "/" + act.ecobee_access);
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
                        "",
                        null);
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

    ecobee_token("ecobeePin", act.ecobee_access, api);
}

private void ecobee_refresh()
{

    ecobee_token("refresh_token", act.ecobee_refresh, act.ecobee_api);
}

private String ecobee_query(String info, String id)
{
    String resp;

    resp = act.call_api("GET",
                               ECO_URL + ECO_DATA,
                               ecobee_param(info, id),
                               "Bearer " + act.ecobee_access,
                               null);
    String code = act.parse.json_get("code", resp, 1);
    if (code == null || !code.equals("0"))
        return null;
    else
        return resp;
}

private void ecobee_test()
{
    String resp;
    String get_settings = "format=json&body={\"selection\":{\"selectionType\":\"registered\",\"selectionMatch\":\"\",\"includeSettings\":true}}";

    resp = act.call_api("GET",
                               ECO_URL + ECO_DATA,
                               get_settings,
                               "Bearer " + act.ecobee_access,
                               null);
Log.d("settings(" + resp.length() + ") - " + resp);

}

private void ecobee_resume()
{

    if (act.ecobee_which < 0)
        return;
    EcobeeData dev = act.ecobee_data.get(act.ecobee_which);
    String id = dev.get_id();
    String body = "{" + "\n" +
                  " \"selection\":{" + "\n" +
                  "  \"selectionType\":\"thermostats\"," + "\n" +
                  "  \"selectionMatch\":\"" + id + "\"" + "\n" +
                  " }," + "\n" +
                  " \"functions\":[" + "\n" +
                  "  {" + "\n" +
                  "   \"type\":\"resumeProgram\"," + "\n" +
                  "   \"params\":{" + "\n" +
                  "    \"resumeAll\":false" + "\n" +
                  "   }" + "\n" +
                  "  }" + "\n" +
                  " ]" + "\n" +
                  "}";
    String resp = act.call_api("POST",
                               ECO_URL + ECO_DATA,
                               "format=json",
                               "Bearer " + act.ecobee_access,
                               body);
}

private void ecobee_hold(int heat, int type)
{

    if (act.ecobee_which < 0)
        return;
    EcobeeData dev = act.ecobee_data.get(act.ecobee_which);
    String id = dev.get_id();
    int cool = heat;
    if (dev.get_mode().equals("auto"))
        cool += dev.get_delta();
    if (type == -1)
        type = R.id.hold_temporary;
    String hmode = (type == R.id.hold_temporary) ? "nextTransition" : "indefinite";
Log.d("hold at " + heat + ", until " + hmode);
    String body = "{" + "\n" +
                  " \"selection\":{" + "\n" +
                  "  \"selectionType\":\"thermostats\"," + "\n" +
                  "  \"selectionMatch\":\"" + id + "\"" + "\n" +
                  " }," + "\n" +
                  " \"functions\":[" + "\n" +
                  "  {" + "\n" +
                  "   \"type\":\"setHold\"," + "\n" +
                  "   \"params\":{" + "\n" +
                  "    \"holdType\":\"" + hmode + "\"," + "\n" +
                  "    \"heatHoldTemp\":" + heat + "," + "\n" +
                  "    \"coolHoldTemp\":" + cool + "\n" +
                  "   }" + "\n" +
                  "  }" + "\n" +
                  " ]" + "\n" +
                  "}";
    String resp = act.call_api("POST",
                               ECO_URL + ECO_DATA,
                               "format=json",
                               "Bearer " + act.ecobee_access,
                               body);
}

private void get_ecobee()
{
    String resp;

    if ((resp = ecobee_query("includeRuntime", "")) == null) {
        ecobee_refresh();
        if ((resp = ecobee_query("includeRuntime", "")) == null)
            return;
    }
    get_info_ecobee(resp);
//ecobee_test();
}

private void ecobee_getstate(int idx)
{

    if (idx < 0)
        return;
    EcobeeData dev = act.ecobee_data.get(idx);
    String resp = ecobee_query("includeEvents", dev.get_id());
    if (resp == null)
        return;
    int mode = EcobeeData.HOLD_RUNNING;
    if (act.parse.json_get("type", resp, idx).equals("hold"))
        mode = act.parse.json_get("endDate", resp, idx).equals("2035-01-01") ?
                        EcobeeData.HOLD_PERMANENT :
                        EcobeeData.HOLD_TEMPORARY;
    dev.set_hold(mode);
}

private int ecobee_temp_color()
{

    int color = act.getResources().getColor(R.color.colorPrimary);
    if (act.ecobee_which >= 0 && act.ecobee_which < act.ecobee_data.size())
        if (act.ecobee_data.get(act.ecobee_which).get_hold() != EcobeeData.HOLD_RUNNING)
            color = act.getResources().getColor(R.color.colorHilight);
    return color;
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

    Button ok = (Button) dialog.findViewById(R.id.ok);
    ok.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            dialog.dismiss();
        }
    });

    dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

    dialog.show();
}

private void hold_dialog()
{

    final Dialog dialog = new Dialog(act, R.style.AlertDialogCustom);
    dialog.setContentView(R.layout.hold);

    if (act.ecobee_which < 0)
        return;

    final EcobeeData dev = act.ecobee_data.get(act.ecobee_which);

    final RadioGroup rg = (RadioGroup) dialog.findViewById(R.id.hold_type);

    TextView tv = (TextView) dialog.findViewById(R.id.hold_status);
    switch (dev.get_hold()) {

    case EcobeeData.HOLD_RUNNING:
        tv.setText("Running");
        break;

    case EcobeeData.HOLD_TEMPORARY:
        tv.setText("Hold (temporary)");
        rg.check(R.id.hold_temporary);
        break;

    case EcobeeData.HOLD_PERMANENT:
        tv.setText("Hold (permanent)");
        rg.check(R.id.hold_permanent);
        break;


    }

    final NumberPicker np = (NumberPicker) dialog.findViewById(R.id.hold_temp);
    np.setMinValue(dev.get_min()/10);
    np.setMaxValue(dev.get_max()/10);
    String t = data.get("in_temp");
    int temp = Integer.parseInt(t.substring(0, t.length() - 2));
    np.setValue(temp);
    np.setWrapSelectorWheel(false);

    Button cancel = (Button) dialog.findViewById(R.id.cancel);
    cancel.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            dialog.dismiss();
        }
    });

    Button resume = (Button) dialog.findViewById(R.id.resume);
    resume.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            dialog.dismiss();
            new Thread(new Runnable() {
                public void run() {
                    ecobee_resume();
                    ecobee_getstate(act.ecobee_which);
                }
            }).start();
        }
    });

    Button hold = (Button) dialog.findViewById(R.id.hold);
    hold.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            final String t = np.getValue() + "0";
            dialog.dismiss();
            new Thread(new Runnable() {
                public void run() {
                    ecobee_hold(Integer.parseInt(t), rg.getCheckedRadioButtonId());
                    ecobee_getstate(act.ecobee_which);
                }
            }).start();
        }
    });

    dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

    dialog.show();
}

public void go_temp_detail(View v)
{

    int id = v.getId();
Log.d("get temp detail for - " + ((id == R.id.weather_out_icon) ? "weather underground" : "ecoBee"));
    detail_dialog();
}

public void go_hold(View v)
{

    hold_dialog();
}

public void update()
{

    //
    //  Get the data
    //
    if (period++ >= PERIOD) {
        data.put("in_temp", "--");
        data.put("out_temp", "--");
        data.put("barometer", "--");
        data.put("windspeed", "--");
        data.put("rain", "--");
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

            if ((tv = (TextView) act.findViewById(R.id.weather_out_temp)) != null)
                tv.setText(data.get("out_temp"));

            if ((tv = (TextView) act.findViewById(R.id.weather_in_temp)) != null) {
                tv.setText(data.get("in_temp"));
                tv.setTextColor(ecobee_temp_color());
            }

            if ((iv = (ImageView) act.findViewById(R.id.weather_dir)) != null)
                iv.setRotation(Integer.valueOf(data.get("winddir")));

            if ((tv = (TextView) act.findViewById(R.id.weather_speed)) != null)
                tv.setText(data.get("windspeed"));

            if ((tv = (TextView) act.findViewById(R.id.weather_rain)) != null)
                tv.setText(data.get("rain") + " in");

            if ((iv = (ImageView) act.findViewById(R.id.weather_bar_dir)) != null)
                iv.setImageResource(baro_icon);

            if ((tv = (TextView) act.findViewById(R.id.weather_barometer)) != null)
                tv.setText(data.get("barometer") + " in");

            if ((iv = (ImageView) act.findViewById(R.id.weather_out_timeout)) != null)
                act.set_timeout(iv, period, PERIOD);

            if ((iv = (ImageView) act.findViewById(R.id.weather_in_timeout)) != null)
                act.set_timeout(iv, period, PERIOD);
        }
    });
}

}
