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

final static String X10_API = "/cgi-bin/athome/x10device";
final static String X10_GET = "get";
final static String X10_SET = "set";
final static String X10_LIST = "list";

final static String X10_STATE_MAP = " \"state\":\"";

MainActivity act;

int x10_power = -1;
String state_map = "0000000000000000";

X10Adapter x10_adapter;

// X10: class constructor
//
//   act - activity that instantiated the class
//
public X10(final MainActivity act)
{

	this.act = act;

    x10_adapter = new X10Adapter(act);
    new Thread(new Runnable() {
        public void run() {
            final String resp = act.call_api("GET",
                                       act.x10_url + X10_API,
                                       X10_LIST,
                                       "");
            act.runOnUiThread(new Runnable() {
                public void run() {
                    init_data(resp);
                }
            });
        }
    }).start();
}

private void init_data(String resp)
{
    int i;
    String name;

    x10_adapter.clear();
    String hcode = act.parse.json_get("code", resp, 1);
    if (hcode == null)
        return;
    i = 0;
    while ((name = act.parse.json_get("name", resp, ++i)) != null) {
        x10_adapter.add_device(i - 1,
                               new X10Device(name,
                                             hcode + i,
                                             View.inflate(act, R.layout.x10_outlet, null)));
    }
    set_power(act.x10_battery);
    TableLayout tl = (TableLayout) act.findViewById(R.id.x10_table);
    tl.removeAllViews();
    TableRow tr = null;
    TableLayout.LayoutParams params = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
    params.setMargins(0, 40, 0, 0); /* left, top, right, bottom */
    int row = 3;
    int max_devices = x10_adapter.getCount();
    for (i = 0; i < max_devices; i++) {
        X10Device dev = x10_adapter.getItem(i);
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

private void do_control(final X10Device dev, boolean state)
{

    dev.set_state(state, act);
    final String onoff = state ? "on" : "off";
    new Thread(new Runnable() {
        public void run() {
            act.call_api("GET",
                         act.x10_url + X10_API,
                         X10_SET + "&code=" + dev.get_code() + "&state=" + onoff,
                         "");
        }
    }).start();
           
}

private void go_control(View v)
{

    X10Device dev = (X10Device) v.getTag();
    do_control(dev, dev.get_state() ? false : true);
}

public void set_power(String name)
{

    x10_power = -1;
    if (name.isEmpty())
        return;

    int max = x10_adapter.getCount();
    for (int i = 0; i < max; i++)
        if (x10_adapter.getItem(i).get_name().equals(name)) {
            x10_power = i;
            return;
        }
}

public void set_power(int i)
{

    x10_power = i;
    act.x10_battery = x10_adapter.getItem(i).get_name();
    return;
}

public void power(boolean state)
{

    if (x10_power < 0)
        return;

    X10Device dev = x10_adapter.getItem(x10_power);
    if (!dev.get_hold())
        if (dev.get_state() != state)
            do_control(dev, state);
}

private void battery(String map)
{

    if (x10_power < 0)
        return;

    boolean state = get_state(x10_power, map);
    int chg = act.get_battery();
    if (chg < act.x10_batt_min && !state)
        power(true);
    else if (chg > act.x10_batt_max && state)
        power(false);
}

private boolean get_state(int idx, String map)
{

    return ((map.charAt(15 - idx) == '0') ? false : true);
}

private void on_off(String line)
{
    int i;
    boolean then;
    boolean now;

    if (!line.equals(state_map)) {
        int max_devices = x10_adapter.getCount();
        if (!line.isEmpty()) {
            for (i = 0; i < max_devices; i++) {
                X10Device dev = x10_adapter.getItem(i);
                then = get_state(i, state_map);
                now = get_state(i, line);
                if ((then != now) && (dev.get_state() != now))
                    dev.set_state(now, act);
            }
        }
    }
    state_map = line;
    battery(state_map);
}

public void update()
{

    //
    //  Get the data and display any changes
    //
    String resp = act.call_api("GET",
                               act.x10_url + X10_API,
                               X10_GET,
                               "");
    String l = act.parse.json_get("state", resp, 1);
    if (l == null || l.length() < 16)
        Log.d("X10 bad data: " + l + " => " + resp);
    else
        on_off(l);
}

}
