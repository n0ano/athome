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
public class Outlets {

final static int MAX_DEVICES = 17;

final static String X10_API = "/cgi-bin/athome/x10device";
final static String X10_GET = "get";
final static String X10_SET = "set";
final static String X10_LIST = "list";

final static String X10_STATE_MAP = " \"state\":\"";

MainActivity act;

int outlets_power = -1;
String state_map = "0000000000000000";

OutletsAdapter outlets_adapter;

// Outlets: class constructor
//
//   act - activity that instantiated the class
//
public Outlets(final MainActivity act)
{

	this.act = act;

    outlets_adapter = new OutletsAdapter(act);
    startup();
}

private void get_x10_devices()
{
    int i, idx;
    String name;

    final String resp = act.call_api("GET",
                               act.x10_url + X10_API,
                               X10_LIST + "&token=" + act.x10_jwt,
                               "");

    String hcode = act.parse.json_get("code", resp, 1);
    if (hcode != null) {
        i = 0;
        while ((name = act.parse.json_get("name", resp, ++i)) != null) {
            if (!name.isEmpty())
                outlets_adapter.add_device(new OutletsDevice(name,
                                                             hcode + i,
                                                             View.inflate(act, R.layout.outlet, null)));
        }
    }
}

private void get_tplink_devices()
{

}

public void startup()
{

    new Thread(new Runnable() {
        public void run() {
            outlets_adapter.clear();

            get_x10_devices();

            get_tplink_devices();

            act.runOnUiThread(new Runnable() {
                public void run() {
                    init_view();
                }
            });
        }
    }).start();
}

private void init_view()
{
    int i;

    set_power(act.outlets_battery);
    TableLayout tl = (TableLayout) act.findViewById(R.id.outlets_table);
    tl.removeAllViews();
    TableRow tr = null;
    TableLayout.LayoutParams params = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
    params.setMargins(0, 40, 0, 0); /* left, top, right, bottom */
    int row = 3;
    int max_devices = outlets_adapter.getCount();
    for (i = 0; i < max_devices; i++) {
        OutletsDevice dev = outlets_adapter.getItem(i);
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
                OutletsDevice dev = (OutletsDevice) v.getTag();
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

private void do_control(final OutletsDevice dev, boolean state)
{

    dev.set_state(state, act);
    final String onoff = state ? "on" : "off";
    new Thread(new Runnable() {
        public void run() {
            String resp = act.call_api("GET",
                                       act.x10_url + X10_API,
                                       X10_SET + "&code=" + dev.get_code() +
                                                 "&state=" + onoff +
                                                 "&token=" + act.x10_jwt,
                                       "");
        }
    }).start();
           
}

private void go_control(View v)
{

    OutletsDevice dev = (OutletsDevice) v.getTag();
    do_control(dev, dev.get_state() ? false : true);
}

public void set_power(String name)
{

    outlets_power = -1;
    if (name.isEmpty())
        return;

    int max = outlets_adapter.getCount();
    for (int i = 0; i < max; i++)
        if (outlets_adapter.getItem(i).get_name().equals(name)) {
            outlets_power = i;
            return;
        }
}

public void set_power(int i)
{

    outlets_power = i;
    act.outlets_battery = outlets_adapter.getItem(i).get_name();
    return;
}

public void power(boolean state)
{

    if (outlets_power < 0)
        return;

    OutletsDevice dev = outlets_adapter.getItem(outlets_power);
    if (!dev.get_hold())
        if (dev.get_state() != state)
            do_control(dev, state);
}

private void battery(String map)
{

    if (outlets_power < 0)
        return;

    boolean state = get_state(outlets_power, map);
    int chg = act.get_battery();
    if (chg < act.outlets_batt_min && !state)
        power(true);
    else if (chg > act.outlets_batt_max && state)
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
        int max_devices = outlets_adapter.getCount();
        if (!line.isEmpty()) {
            for (i = 0; i < max_devices; i++) {
                OutletsDevice dev = outlets_adapter.getItem(i);
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

private void x10_update()
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

private void tplink_update()
{

}

public void update()
{

    x10_update();

    tplink_update();
}

}
