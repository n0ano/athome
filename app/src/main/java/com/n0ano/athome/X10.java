package com.n0ano.athome;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
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
public class X10 {

final static int MAX_DEVICES = 17;

final static String X10_URL = "http://n0ano.com";
final static String X10_API = "/cgi-bin/athome/x10device";
final static String X10_GET = "get";
final static String X10_SET = "set";

final static String X10_STATE_MAP = " \"state\":\"";

MainActivity act;

int max_devices;
int x10_power = 5;
String state_map;

X10Device[] x10_devices = new X10Device[MAX_DEVICES];

// X10: class constructor
//
//   act - activity that instantiated the class
//
public X10(MainActivity act)
{

	this.act = act;
    init_data();
}

private void init_data()
{
    int i;

    max_devices = 1;
    x10_devices[max_devices++] = new X10Device("Crystal lamp", "d1", View.inflate(act, R.layout.x10_outlet, null));
    x10_devices[max_devices++] = new X10Device("Driveway/Xmas", "d2", View.inflate(act, R.layout.x10_outlet, null));
    x10_devices[max_devices++] = new X10Device("Patio Lights", "d3", View.inflate(act, R.layout.x10_outlet, null));
    x10_devices[max_devices++] = new X10Device("Patio Fountain", "d4", View.inflate(act, R.layout.x10_outlet, null));
    x10_devices[max_devices++] = new X10Device("AtHome Display", "d5", View.inflate(act, R.layout.x10_outlet, null));
    x10_devices[max_devices++] = new X10Device("Don's Office", "d6", View.inflate(act, R.layout.x10_outlet, null));
    x10_devices[max_devices++] = new X10Device("Office Fountain", "d7", View.inflate(act, R.layout.x10_outlet, null));
    x10_devices[max_devices++] = new X10Device("Office Acquarium", "d8", View.inflate(act, R.layout.x10_outlet, null));
    TableLayout tl = (TableLayout) act.findViewById(R.id.x10_table);
    tl.removeAllViews();
    TableRow tr = null;
    TableLayout.LayoutParams params = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
    params.setMargins(0, 40, 0, 0); /* left, top, right, bottom */
    int row = 3;
    for (i = 1; i < max_devices; i++) {
        X10Device dev = x10_devices[i];
        if (++row > 3) {
            row = 1;
            if (tr != null)
                tl.addView(tr, params);
            tr = new TableRow(act);
        }
        View v = dev.get_view();
        v.setTag(dev);
        v.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                go_control(v);
            }
        });
        v.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View v) {
                Log.d("long click");
                X10Device dev = (X10Device) v.getTag();
                dev.set_hold(!dev.get_hold());
                dev.set_state(dev.get_state(), act);
                return true;
            }
        });
        tr.addView(v);
    }
    if (tr != null)
        tl.addView(tr, params);

}

private void go_control(View v)
{

    final X10Device dev = (X10Device) v.getTag();
    int state = dev.get_state() ^ 1;
    dev.set_state(state, act);
    final String onoff = (state == 0) ? "off" : "on";
    new Thread(new Runnable() {
        public void run() {
            act.call_api("GET",
                         X10_URL + X10_API,
                         X10_SET + "&code=" + dev.get_code() + "&state=" + onoff,
                         "");
        }
    }).start();
           
}

public void power(int state)
{

    X10Device dev = x10_devices[x10_power];
    if (!dev.get_hold())
        if (dev.get_state() != state)
            dev.set_state(state, act);
}

private void on_off(String line)
{
    int i;
    int onoff;

    if (!line.isEmpty()) {
        for (i = 1; i < max_devices; i++) {
            X10Device dev = x10_devices[i];
            onoff = ((line.charAt(16 - i) == '0') ? 0 : 1);
            if (dev.get_state() != onoff)
                dev.set_state(onoff, act);
        }
    }
}

public void update()
{

    //
    //  Get the data and display any changes
    //
    String resp = act.call_api("GET",
                               X10_URL + X10_API,
                               X10_GET,
                               "");
    on_off(act.parse.json_get("state", resp, 1));
}

}
