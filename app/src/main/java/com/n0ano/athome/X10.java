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

final static String X10_API = "/cgi-bin/athome/x10device";
final static String X10_GET = "get";
final static String X10_SET = "set";
final static String X10_LIST = "list";

final static String X10_STATE_MAP = " \"state\":\"";

MainActivity act;

String state_map = "0000000000000000";

// X10: class constructor
//
//   act - activity that instantiated the class
//
public X10(final MainActivity act)
{

	this.act = act;
}

public void get_devices(OutletsAdapter adapter)
{
    int i, idx;
    String name;

    final String resp = act.call_api("GET",
                               act.x10_url + X10_API,
                               X10_LIST + "&token=" + act.x10_jwt,
                               "",
                               null);

    String hcode = act.parse.json_get("code", resp, 1);
    if (hcode != null) {
        i = 0;
        String devices = act.parse.json_get("devices", resp, 1);
        while ((name = act.parse.json_list(devices, ++i)) != null) {
            if (!name.isEmpty())
                adapter.add_device(new OutletsDevice(name,
                                                     i,
                                                     hcode,
                                                     View.inflate(act, R.layout.outlet, null)));
        }
    }

    act.runOnUiThread(new Runnable() {
        public void run() {
            act.outlets.init_view();
        }
    });
}

public void control(final OutletsDevice dev, boolean state)
{

    dev.set_state(state, act);
    final String onoff = state ? "on" : "off";
    new Thread(new Runnable() {
        public void run() {
            String resp = act.call_api("GET",
                                       act.x10_url + X10_API,
                                       X10_SET + "&code=" + dev.get_code() + dev.get_index() +
                                                 "&state=" + onoff +
                                                 "&token=" + act.x10_jwt,
                                       "",
                                       null);
        }
    }).start();
}

private boolean get_state(int idx, String map)
{

    return ((map.charAt(16 - idx) == '0') ? false : true);
}

private void on_off(String line, OutletsAdapter outlets_adapter)
{
    int i;
    boolean then;
    boolean now;

    if (!line.equals(state_map) && !line.isEmpty()) {
        int max_devices = outlets_adapter.getCount();
        for (i = 0; i < max_devices; i++) {
            OutletsDevice dev = outlets_adapter.getItem(i);
            if (dev.get_type() == OutletsDevice.TYPE_X10) {
                int idx = dev.get_index();
                then = get_state(idx, state_map);
                now = get_state(idx, line);
                if (then != now)
                    dev.set_state(now, act);
            }
        }
    }
    state_map = line;
}

public void update(OutletsAdapter outlets_adapter)
{

    //
    //  Get the data and display any changes
    //
    String resp = act.call_api("GET",
                               act.x10_url + X10_API,
                               X10_GET,
                               "",
                               null);
    String l = act.parse.json_get("state", resp, 1);
    if (l == null || l.length() < 16)
        Log.d("X10 bad data: " + l + " => " + resp);
    else
        on_off(l, outlets_adapter);
}

}
