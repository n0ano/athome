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

MainActivity act;

public int period = PERIOD;        // Weather only changes once a minute

Map<String, String> data = new HashMap<String, String>();

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

    /*
     * Data we need an initial value for
     */
    data.put("tempf", "--");
    data.put("winddir", "0");
    data.put("windspeedmph", "--");
    data.put("dailyrainin", "--");
    data.put("baromin", "--");
    /*
     * Data we care about
     */
    data.put("maxtemp", "");
    data.put("maxtemp_time", "");
    data.put("mintemp", "");
    data.put("mintemp_time", "");
    data.put("humidity", "");
}

private void get_info_wunder(String resp)
{
    String key;
    String val;

    Set keys = data.keySet();
    for (Iterator itr = keys.iterator(); itr.hasNext();) {
        key = (String)itr.next();
        val = act.parse.xml_get(key, resp, 1);
        data.put(key, (val != null) ? val : "");
    }
    Float f = Float.valueOf(data.get("baromin"));
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

private void detail_dialog()
{

    final Dialog dialog = new Dialog(act, R.style.AlertDialogCustom);
    dialog.setContentView(R.layout.detail);

    TextView tv = (TextView) dialog.findViewById(R.id.detail_max);
    tv.setText(data.get("maxtemp"));

    tv = (TextView) dialog.findViewById(R.id.detail_min);
    tv.setText(data.get("mintemp"));

    tv = (TextView) dialog.findViewById(R.id.detail_max_time);
    tv.setText(data.get("maxtemp_time"));

    tv = (TextView) dialog.findViewById(R.id.detail_min_time);
    tv.setText(data.get("mintemp_time"));

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

public void go_temp_detail(View v)
{

    detail_dialog();
}

public void update()
{

    //
    //  Get the data
    //
    if (period++ >= PERIOD) {
        get_wunder();
        period = 1;
    }

    //
    //  Display it
    //
    act.runOnUiThread(new Runnable() {
        public void run() {
            TextView tv;
            ImageView iv;

            if ((tv = (TextView) act.findViewById(R.id.weather_temp)) != null)
                tv.setText(data.get("tempf"));

            if ((iv = (ImageView) act.findViewById(R.id.weather_dir)) != null)
                iv.setRotation(Integer.valueOf(data.get("winddir")));

            if ((tv = (TextView) act.findViewById(R.id.weather_speed)) != null)
                tv.setText(data.get("windspeedmph"));

            if ((tv = (TextView) act.findViewById(R.id.weather_rain)) != null)
                tv.setText(data.get("dailyrainin") + " in");

            if ((iv = (ImageView) act.findViewById(R.id.weather_bar_dir)) != null)
                iv.setImageResource(baro_icon);

            if ((tv = (TextView) act.findViewById(R.id.weather_barometer)) != null)
                tv.setText(data.get("baromin") + " in");

            if ((iv = (ImageView) act.findViewById(R.id.weather_timeout)) != null)
                act.set_timeout(iv, period, PERIOD);
        }
    });
}

}
