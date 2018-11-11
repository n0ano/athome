package com.n0ano.athome;

import android.app.Activity;
import android.widget.ImageView;
import android.widget.TextView;

import com.n0ano.athome.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

// Created by n0ano on 10/10/16.
//
// Class to handle weather data
//
public class Weather {

String station;
MainActivity act;

Map<String, String> data = new HashMap<String, String>();
Map<String, String> data_under = new HashMap<String, String>();
Map<String, String> data_ecobee = new HashMap<String, String>();

// Preferences: class constructor
//
//   act - activity that instantiated the class
//
public Weather(String station, MainActivity act)
{

    this.station = station;
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

    data_ecobee.put("temp", "in_temp");
}

private void proc_under(String line)
{
    String search = "";
    String key = "";
    String val = "";

    Set keys = data_under.keySet();
    for (Iterator itr = keys.iterator(); itr.hasNext();) {
        key = (String)itr.next();
        search = "<" + key + " val=\"";
        if (line.startsWith(search)) {
            key = data_under.get(key);
            line = line.substring(search.length());
            val = line.substring(0, line.indexOf("\""));
            data.put(key, val);
            return;
        }
    }
}

public void get_data()
{
    String url;
    String line;
    URL server;
    InputStreamReader in_rdr;
    BufferedReader inp;

    try {
        url = Common.WEATHER_URL
                    + "?station=" + station
                    + "&units=english&v=2.0&format=xml";
        Log.d("get data from " + url);
        server = new URL(url);
        in_rdr = new InputStreamReader(server.openStream());
        inp = new BufferedReader (in_rdr);
        while ((line = inp.readLine()) != null) {
            proc_under(line);
        }
    } catch (Exception e) {
        Log.d("get file failed - " + e);
        return;
    }
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
        }
    });
}

}
