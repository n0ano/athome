package com.n0ano.athome;

import android.view.View;

import org.json.JSONArray;
import org.json.JSONObject;

// Created by n0ano on 10/10/16.
//
// Class to handle weather data
//
public class Tplink {

private final static int PERIOD = 60;   // weather only changes once a minute

private final static int TPLINK_OFFLINE = -20571;

private final static String TPLINK_URL = "https://wap.tplinkcloud.com";

private final static String TPLINK_UUID = "621dd649-160c-41dd-9ceb-5a46ad8fb90e";

MainActivity act;

private int period = PERIOD;        // Weather only changes once a minute

String token;

// Tplink: class constructor
//
//   act - activity that instantiated the class
//
public Tplink(final MainActivity act)
{

	this.act = act;
}

private boolean current_state(OutletsDevice dev)
{

    String resp = act.call_api("POST",
                                dev.get_url(),
                                "token=" + token,
                                "",
                                "{" +
                                    "\"method\":\"passthrough\"," +
                                    "\"params\":{" +
                                    "\"deviceId\":\"" + dev.get_code() + "\"," +
                                    "\"requestData\":\"{\\\"system\\\":{\\\"get_sysinfo\\\":{}}}\"" +
                                    "}}"
                               );
    try {
        JSONObject json = C.str2json(resp);
        if (json.getInt("error_code") == TPLINK_OFFLINE) {
            Log.d("TPLink: " + dev.get_name() + " offline");
            return false;
        }
        JSONObject result = (JSONObject)json.get("result");
        JSONObject info = C.str2json(result.optString("responseData", ""));

        JSONObject system = (JSONObject)info.get("system");
        JSONObject sysinfo = (JSONObject)system.get("get_sysinfo");
        String onoff = sysinfo.optString("relay_state", "");
        return onoff.equals("1");
    } catch (Exception e) {
        Log.d("TPLink current state(" + resp + ") json parse error - " + e);
    }
    return false;
}

public void get_devices(OutletsAdapter adapter, DoitCallback cb)
{
    String resp;
    JSONObject json, result, info;
    JSONArray list;
    OutletsDevice dev;

    resp = act.call_api("POST",
                        TPLINK_URL,
                        "",
                        "",
                        "{" +
                            "\"method\":\"login\"," +
                            "\"params\":{" +
                            "\"appType\":\"Kasa_Android\"," +
                            "\"cloudUserName\":\"" + act.tplink_user + "\"," +
                            "\"cloudPassword\":\"" + act.tplink_pwd + "\"," +
                            "\"terminalUUID\":\"" + TPLINK_UUID + "\"" +
                            "}}"
                       );
    try {
        json = C.str2json(resp);
        result = (JSONObject)json.get("result");
    } catch (Exception e) {
        Log.d("TPLink get devices(" + resp + ") json parse error " + e);
        cb.doit(null);
        return;
    }

    token = result.optString("token", "");
    resp = act.call_api("POST",
                        TPLINK_URL,
                        "token=" + token,
                        "",
                        "{\"method\":\"getDeviceList\"}"
                       );
    try {
        json = C.str2json(resp);
        result = (JSONObject)json.get("result");
        list = result.optJSONArray("deviceList");
    } catch (Exception e) {
        Log.d("TPLink get_devices(" + resp + ") parse error - " + e);
        cb.doit(null);
        return;
    }

    int max = list.length();
    for (int i = 0; i < max; i++) {
        info = (JSONObject)C.json_get(list, i);
        dev = new OutletsDevice(info.optString("alias", ""),
                                info.optString("deviceId", ""),
                                info.optString("appServerUrl", ""),
                                View.inflate(act, R.layout.outlet, null));
        dev.set_state(current_state(dev));
        adapter.add_device(dev);
    }

    cb.doit(null);
}

public void control(final OutletsDevice dev, boolean state)
{

    dev.set_state(state);
    final String onoff = state ? "1" : "0";
    new Thread(new Runnable() {
        public void run() {
            String resp = act.call_api("POST",
                                        dev.get_url(),
                                        "token=" + token,
                                        "",
                                        "{" +
                                            "\"method\":\"passthrough\"," +
                                            "\"params\":{" +
                                            "\"deviceId\":\"" + dev.get_code() + "\"," +
                                            "\"requestData\":\"{\\\"system\\\":{" +
                                                "\\\"set_relay_state\\\":{" +
                                                    "\\\"state\\\":" + onoff + "}" +
                                                "}}\"" +
                                            "}}"
                                       );
        }
    }).start();
}

public boolean get_data(OutletsAdapter adapter)
{

    int max_devices = adapter.getCount();
    for (int i = 0; i < max_devices; i++) {
        OutletsDevice dev = adapter.getItem(i);
        if (dev.get_type() == OutletsDevice.TYPE_TPLINK)
            dev.set_state(current_state(dev));
    }
    return true;
}

}
