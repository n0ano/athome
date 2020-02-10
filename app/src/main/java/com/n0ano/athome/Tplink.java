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

Http http = new Http();

private int period = PERIOD;        // Weather only changes once a minute

String token = "";

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
    Http.R resp;

    resp = http.call_api("POST",
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
        JSONObject json = C.str2json(resp.body);
        if (json.getInt("error_code") == TPLINK_OFFLINE) {
            dev.set_online(false);
            return false;
        }
        dev.set_online(true);
        JSONObject result = (JSONObject)json.get("result");
        JSONObject info = C.str2json(result.optString("responseData", ""));

        JSONObject system = (JSONObject)info.get("system");
        JSONObject sysinfo = (JSONObject)system.get("get_sysinfo");
        String onoff = sysinfo.optString("relay_state", "");
        return onoff.equals("1");
    } catch (Exception e) {
        dev.set_online(false);
        Log.d("TPLink: " + dev.get_name() + " offline, json parse(" + resp.body + ") error - " + e);
    }
    return false;
}

private boolean get_token()
{
    Http.R resp;
    JSONObject json, result;

    resp = http.call_api("POST",
                         TPLINK_URL,
                         "",
                         "",
                         "{" +
                            "\"method\":\"login\"," +
                            "\"params\":{" +
                            "\"appType\":\"Kasa_Android\"," +
                            "\"cloudUserName\":\"" + P.get_string("outlets:tplink_user") + "\"," +
                            "\"cloudPassword\":\"" + P.get_string("outlets:tplink_pwd") + "\"," +
                            "\"terminalUUID\":\"" + TPLINK_UUID + "\"" +
                            "}}"
                        );
    try {
        json = C.str2json(resp.body);
        result = (JSONObject)json.get("result");
    } catch (Exception e) {
        Log.d("TPLink get devices(" + resp.body + ") json parse error " + e);
        token = "";
        return false;
    }

    token = result.optString("token", "");
//Log.d("DDD", "tplink token - " + token);
    return true;
}

public void get_devices(OutletsAdapter adapter, DoitCallback cb)
{
    Http.R resp;
    JSONObject json, result, info;
    JSONArray list;
    OutletsDevice dev;

    if (!get_token()) {
        cb.doit(0, null);
        return;
    }
    resp = http.call_api("POST",
                        TPLINK_URL,
                        "token=" + token,
                        "",
                        "{\"method\":\"getDeviceList\"}"
                       );
    try {
        json = C.str2json(resp.body);
        result = (JSONObject)json.get("result");
        list = result.optJSONArray("deviceList");
    } catch (Exception e) {
        Log.d("TPLink get_devices(" + resp.body + ") parse error - " + e);
        cb.doit(0, null);
        return;
    }

    int max = list.length();
    for (int i = 0; i < max; i++) {
        info = (JSONObject)C.json_get(list, i);
        dev = new OutletsDevice(info.optString("alias", ""),
                                info.optString("deviceId", ""),
                                info.optString("appServerUrl", ""),
                                View.inflate(act, R.layout.outlet, null));
        dev.set_onoff(current_state(dev));
        adapter.add_device(dev);
    }

    cb.doit(0, null);
}

public void control(final OutletsDevice dev, boolean onoff)
{

    dev.set_onoff(onoff);
    final String state = onoff ? "1" : "0";
    new Thread(new Runnable() {
        public void run() {
            Http.R resp;

            resp = http.call_api("POST",
                                dev.get_url(),
                                "token=" + token,
                                "",
                                "{" +
                                    "\"method\":\"passthrough\"," +
                                    "\"params\":{" +
                                    "\"deviceId\":\"" + dev.get_code() + "\"," +
                                    "\"requestData\":\"{\\\"system\\\":{" +
                                        "\\\"set_relay_state\\\":{" +
                                            "\\\"state\\\":" + state + "}" +
                                        "}}\"" +
                                    "}}"
                               );
        }
    }).start();
}

public boolean get_data(OutletsAdapter adapter)
{

    boolean offline = false;
    int max_devices = adapter.getCount();
    for (int i = 0; i < max_devices; i++) {
        OutletsDevice dev = adapter.getItem(i);
        if (dev.get_type() == OutletsDevice.TYPE_TPLINK) {
            dev.set_onoff(current_state(dev));
            if (!dev.get_online())
                offline = true;
        }
    }

    if (offline)
        get_token();
    return true;
}

}
