package com.n0ano.athome;

import android.view.View;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

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

final static int OFFLINE_MAX =  4;

int offline_count = 0;

MainActivity act;

Http http = new Http();

Map<String, String> onoff_state = new HashMap<String, String>();

// X10: class constructor
//
//   act - activity that instantiated the class
//
public X10(final MainActivity act)
{

	this.act = act;
}

public void get_devices(OutletsAdapter adapter, DoitCallback cb)
{
    JSONObject json, info;
    JSONArray devices, house;
    String code, name;

    Http.R resp = http.call_api(P.get_string("outlets:x10_url") + X10_API,
                                     X10_LIST + "&token=" + P.get_string("outlets:x10_jwt"));

    try {
        json = C.str2json(resp.body);
        house = json.optJSONArray("house");
    } catch (Exception e) {
        Log.d("X10 get devices(" + resp.body + ") json parse error - " + e);
        cb.doit(0, null);
        return;
    }

    int max = (house == null) ? 0 : house.length();
    for (int i = 0; i < max; i++) {
        if ((info = (JSONObject)C.json_get(house, i)) == null)
            continue;
        code = info.optString("code", "");
        onoff_state.put(code, "0000000000000000");
        devices = info.optJSONArray("devices");
        int dmax = devices.length();
        for (int j = 0; j < dmax; j++) {
            try {
                name = (String)devices.get(j);
                if (!name.isEmpty())
                    adapter.add_device(new OutletsDevice(name,
                                                         j + 1,
                                                         code,
                                                         View.inflate(act, R.layout.outlet, null)));
            } catch (Exception e) {
                Log.d("X10 JSON array reference error - " + e);
            }
        }
    }

    cb.doit(0, null);
}

public void control(final OutletsDevice dev, boolean onoff)
{

    dev.set_onoff(onoff);
    final String state = onoff ? "on" : "off";
    new Thread(new Runnable() {
        public void run() {
            Http.R resp = http.call_api(P.get_string("outlets:x10_url") + X10_API,
                                       X10_SET + "&code=" + dev.get_dev_code() +
                                                 "&state=" + state +
                                                 "&token=" + P.get_string("outlets:x10_jwt"));
        }
    }).start();
}

private boolean get_onoff(int idx, String map)
{

    if (map.length() < 16) {
        Log.d("X10 Bad state map - " + map);
        return false;
    }
    return ((map.charAt(16 - idx) == '0') ? false : true);
}

private void set_onoff(OutletsAdapter adapter)
{
    boolean onoff;

    int max_devices = adapter.getCount();
    for (int i = 0; i < max_devices; i++) {
        OutletsDevice dev = adapter.getItem(i);
        if (dev.get_type() == OutletsDevice.TYPE_X10) {
            String code = dev.get_code();
            int idx = dev.get_index();
            onoff = get_onoff(idx, onoff_state.get(code));
            dev.set_onoff(onoff);
        }
    }
}

private void x10_state(OutletsAdapter adapter, int state)
{

    int max_devices = adapter.getCount();
    for (int i = 0; i < max_devices; i++) {
        OutletsDevice dev = adapter.getItem(i);
        dev.set_state(state, false);
    }
}

public boolean get_data(OutletsAdapter adapter)
{
    JSONObject json, info;
    String code, value, onoff;

    //
    //  Get the data and display any changes
    //
    Http.R resp = http.call_api(P.get_string("outlets:x10_url") + X10_API,
                               X10_GET);

    if ((json = C.str2json(resp.body)) == null) {
        if (++offline_count > OFFLINE_MAX)
            x10_state(adapter, OutletsDevice.OFFLINE);
        return false;
    }
    x10_state(adapter, OutletsDevice.ONLINE);
    offline_count = 0;

    JSONArray house = json.optJSONArray("house");
    if (house == null) {
        Log.d("X10 house info missing - " + resp.body);
        return false;
    }

    boolean ret = false;
    int max = house.length();
    for (int i = 0; i < max; i++) {
        if ((info = (JSONObject)C.json_get(house, i)) == null)
            continue;
        code = info.optString("code", "");
        value = info.optString("state", "");
        if (onoff_state.get(code) != value) {
            ret = true;
            onoff_state.put(code, value);
        }
    }
    set_onoff(adapter);
    return ret;
}

}
