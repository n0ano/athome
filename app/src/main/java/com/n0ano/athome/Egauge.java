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

private String eg_server;
MainActivity act;

int use_watt;
int gen_watt;

// Egauge: class constructor
//
//   act - activity that instantiated the class
//
public Egauge(String eg_server, MainActivity act)
{

    this.eg_server = eg_server;
	this.act = act;
}

private int get_value(String name, String resp)
{
    int w = 0;

    int end = resp.indexOf(",\"cname\":\"" + name);
    if (end >= 0) {
        while (resp.charAt(--end) != '.')
            if (end <= 0)
                return 0;
        int start = end;
        while (resp.charAt(--start) != ':')
            if (start <= 0)
                return 0;
        w = Integer.parseInt(resp.substring(start + 1, end));
    }
    return w;
}

public void get_data()
{

    String resp = act.call_api("GET",
                               eg_server + "cgi-bin/egauge-show",
                               "json&I&a&m&Y=0,60",
                               "");
    use_watt = get_value("use", resp);
    gen_watt = get_value("gen", resp);
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

public void show_data()
{

    act.runOnUiThread(new Runnable() {
        public void run() {
            TextView tv;
            ImageView iv;

            int grid_watt = gen_watt - use_watt;

            tv = (TextView) act.findViewById(R.id.grid_watt);
            tv.setText(k_watts(grid_watt));

            tv = (TextView) act.findViewById(R.id.house_watt);
            tv.setText(k_watts(use_watt));

            tv = (TextView) act.findViewById(R.id.panel_watt);
            tv.setText(k_watts(gen_watt));

            iv = (ImageView) act.findViewById(R.id.grid_arrow);
            set_arrow(iv, grid_watt, R.drawable.arrow_left_green, R.drawable.arrow_right_red);

            iv = (ImageView) act.findViewById(R.id.panel_arrow);
            set_arrow(iv, gen_watt, R.drawable.arrow_left, R.drawable.arrow_right);
        }
    });
}

}
