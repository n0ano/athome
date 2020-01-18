package com.n0ano.athome;

import android.view.View;

// Created by n0ano on 10/10/16.
//
// Class to handle weather data
//
public class Tplink {

private final static int PERIOD = 60;   // weather only changes once a minute

final static String TPLINK_URL = "https://wap.tplinkcloud.com";

final static String TPLINK_UUID = "621dd649-160c-41dd-9ceb-5a46ad8fb90e";

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

    String state = act.call_api("POST",
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
    String onoff = act.parse.json_get("relay_state\\", state, 1);
    if (onoff != null)
        return onoff.equals("1");
    return false;
}

public void get_devices(OutletsAdapter outlets_adapter)
{
    int i;
    String resp;
    String alias;
    OutletsAdapter adapter;

    adapter = outlets_adapter;

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
    token = act.parse.json_get("token", resp, 1);
    resp = act.call_api("POST",
                        TPLINK_URL,
                        "token=" + token,
                        "",
                        "{\"method\":\"getDeviceList\"}"
                       );
    i = 0;
    while ((alias = act.parse.json_get("alias", resp, ++i)) != null) {
        String id = act.parse.json_get("deviceId", resp, i);
        String url = act.parse.json_get("appServerUrl", resp, i);
        OutletsDevice dev = new OutletsDevice(alias,
                                              id,
                                              url,
                                              View.inflate(act, R.layout.outlet, null));
        dev.set_state(current_state(dev), act);

        adapter.add_device(dev);
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

public void update(OutletsAdapter outlets_adapter)
{

    int max_devices = outlets_adapter.getCount();
    for (int i = 0; i < max_devices; i++) {
        OutletsDevice dev = outlets_adapter.getItem(i);
        if (dev.get_type() == OutletsDevice.TYPE_TPLINK) {
            boolean now = current_state(dev);
            if (now != dev.get_state())
                dev.set_state(now, act);
        }
    }
}

}
