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

public Egauge(String eg_server, MainActivity act)
{

    this.eg_server = eg_server;
	this.act = act;
}

private int watt(String line)
{

    line = line.substring(line.indexOf("\"i\":") + 4);
    line = line.substring(0, line.indexOf("."));
    return Integer.parseInt(line);
}

private void proc_line(String line)
{
    int val;
    int end;

    if (!line.contains("\"cname\":"))
        return;
    if (line.contains("\"use\""))
            use_watt = watt(line);
    else if (line.contains("\"gen\""))
            gen_watt = watt(line);
}

public void get_data()
{
    String url;
    String line;
    URL server;
    InputStreamReader in_rdr;
    BufferedReader inp;

    try {
        url = eg_server + "cgi-bin/egauge-show"
                        + "?json&I&a&m&Y=0,60";
        Log.d("get data from " + url);
        server = new URL(url);
        in_rdr = new InputStreamReader(server.openStream());
        inp = new BufferedReader (in_rdr);
        while ((line = inp.readLine()) != null)
            proc_line(line);
Log.d("use - " + use_watt + ", gen - " + gen_watt);
        in_rdr.close();
    } catch (Exception e) {
        Log.d("get file failed - " + e);
        return;
    }
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
