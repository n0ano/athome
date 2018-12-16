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

Map<String, String> onoff_state = new HashMap<String, String>();

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
    int i, hi, idx;
    String name, hcode;

    final String resp = act.call_api("GET",
                               act.x10_url + X10_API,
                               X10_LIST + "&token=" + act.x10_jwt,
                               "",
                               null);

    hi = 0;
    while ((hcode = act.parse.json_get("code", resp, ++hi)) != null) {
        i = 0;
        onoff_state.put(hcode, "0000000000000000");
        String devices = act.parse.json_get("devices", resp, hi);
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

    if (map.length() < 16) {
        Log.d("Bad state map - " + map);
        return false;
    }
    return ((map.charAt(16 - idx) == '0') ? false : true);
}

private boolean status_changed(String resp)
{
    int i;
    boolean changed;
    String code, line;

    i = 0;
    changed = false;
    while ((code = act.parse.json_get("code", resp, ++i)) != null) {
        if ((line = act.parse.json_get("state", resp, i)) == null) {
            Log.d("X10 bad state data: " + resp);
            return false;
        }
        if (!line.equals(onoff_state.get(code))) {
            onoff_state.put(code, line);
            changed = true;
        }
    }
    return changed;
}

private void on_off(String resp, OutletsAdapter outlets_adapter)
{
    int i;
    boolean state;

    if (status_changed(resp)) {
        int max_devices = outlets_adapter.getCount();
        for (i = 0; i < max_devices; i++) {
            OutletsDevice dev = outlets_adapter.getItem(i);
            if (dev.get_type() == OutletsDevice.TYPE_X10) {
                String code = dev.get_code();
                int idx = dev.get_index();
                state = get_state(idx, onoff_state.get(code));
                if (state != dev.get_state())
                    dev.set_state(state, act);
            }
        }
    }
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
    if (act.parse.json_get("house", resp, 1) == null)
        Log.d("X10 bad get data: " + resp);
    else
        on_off(resp, outlets_adapter);
}

}
