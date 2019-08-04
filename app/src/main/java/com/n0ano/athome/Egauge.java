package com.n0ano.athome;

import android.app.Activity;
import android.widget.ImageView;
import android.widget.TextView;

import com.n0ano.athome.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

// Created by n0ano on 10/10/16.
//
// Class to handle weather data
//
public class Egauge {

public final static int PERIOD = 10;   // check eGauge every 10 seconds

public final static String EGAUGE_API = "/cgi-bin/egauge";
public final static String EGAUGE_QUERY = "tot&inst";

MainActivity act;

int period = PERIOD;

int use_watt;
int gen_watt;

// Egauge: class constructor
//
//   act - activity that instantiated the class
//
public Egauge(MainActivity act)
{

	this.act = act;
}

private int get_value(String name, String resp)
{
    int w = 0;

    int start = resp.indexOf("n=\"" + name + "\">");
    if (start >= 0) {
        start = resp.indexOf("<i>", start);
        if (start < 0)
            return w;
        start += 3;
        int end = resp.indexOf(".", start);
        if (end < 0)
            return w;
Log.d("data - " + resp.substring(start, end));
        w = Common.a2i(resp.substring(start, end));
    }
    return w;
}

private void set_arrow(ImageView iv, int watt, int left, int right)
{

    int arrow = (watt > 0) ? left : right;
    iv.setImageResource(arrow);
}

private String k_watts(int w)
{

    w = Math.abs(w);
    if (w >= 1000) {
        String s = w + "";
        String u = s.substring(0,1);
        String d = s.substring(1);
        return u + "." + d + " kW";
    } else {
        return w + " W";
    }
}

private int gen_icon(int watts)
{
    int w_max = 6000;

    if (watts > (w_max * 0.75))
        return R.drawable.panel_green;
    if (watts > (w_max * 0.25))
        return R.drawable.panel_cyan;
    if (watts >= 0)
        return R.drawable.panel_white;
    return R.drawable.panel_red;
}

public void update()
{

    //
    // Get the data
    //
    if (period++ >= PERIOD) {
        String resp = act.call_api("GET",
                                   act.egauge_url + EGAUGE_API,
                                   EGAUGE_QUERY,
                                   "",
                                   null);
        use_watt = get_value("Total Usage", resp);
        gen_watt = get_value("Total Generation", resp);
        period = 1;
    }

    //
    //  Display it
    //
    act.runOnUiThread(new Runnable() {
        public void run() {
            TextView tv;
            ImageView iv;

            int grid_watt = gen_watt - use_watt;

            if ((tv = (TextView) act.findViewById(R.id.grid_watt)) != null)
                tv.setText(k_watts(grid_watt));

            if ((tv = (TextView) act.findViewById(R.id.house_watt)) != null)
                tv.setText(k_watts(use_watt));

            if ((tv = (TextView) act.findViewById(R.id.panel_watt)) != null)
                tv.setText(k_watts(gen_watt));
            if ((iv = (ImageView) act.findViewById(R.id.panel_image)) != null)
                iv.setImageResource(gen_icon(gen_watt));

            if ((iv = (ImageView) act.findViewById(R.id.grid_arrow)) != null)
                set_arrow(iv, grid_watt, R.drawable.arrow_left_green, R.drawable.arrow_right_red);

            if ((iv = (ImageView) act.findViewById(R.id.panel_arrow)) != null)
                set_arrow(iv, gen_watt, R.drawable.arrow_left, R.drawable.arrow_right);

            if ((iv = (ImageView) act.findViewById(R.id.egauge_timeout)) != null)
                act.set_timeout(iv, period, PERIOD);
        }
    });
}

}
