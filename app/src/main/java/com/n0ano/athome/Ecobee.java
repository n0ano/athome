package com.n0ano.athome;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.RadioGroup;
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
// Class to handle Ecobee thermostat data
//
public class Ecobee {

public final static int PERIOD = 60;   // temperature only changes once a minute

public final static String ECO_URL = "https://api.ecobee.com";
public final static String ECO_AUTHORIZE = "/authorize";
public final static String ECO_REFRESH = "/token";
public final static String ECO_DATA = "/1/thermostat";
public final static String ECO_QUERY = "format=json&body={\"selection\":{\"selectionType\":\"registered\",\"selectionMatch\":\"\",\"includeRuntime\":true}}";

MainActivity act;

public int period = PERIOD;        // Weather only changes once a minute

Map<String, String> data = new HashMap<String, String>();

// Ecobee: class constructor
//
//   act - activity that instantiated the class
//
public Ecobee(MainActivity act)
{

	this.act = act;
}

public void get_devices(ThermostatAdapter adapter)
{
    String resp;
    String name, id;
    String mode, hmin, hmax, cmin, cmax, cdelta;

    if ((resp = runtime()) == null)
        return;
    int idx = 0;
    while ((name = act.parse.json_get("name", resp, ++idx)) != null) {
        id = act.parse.json_get("identifier", resp, idx);
        adapter.add_device(new ThermostatDevice(name,
                                                idx,
                                                id,
                                                View.inflate(act, R.layout.thermostat, null)));
    }
    if ((resp = ecobee_query("includeSettings", "")) == null)
        return;

    --idx;
    for (int i = 0; i < idx; i++) {
        mode = act.parse.json_get("hvacMode", resp, i + 1);
        hmin = act.parse.json_get("heatRangeLow", resp, i + 1);
        hmax = act.parse.json_get("heatRangeHigh", resp, i + 1);
        cmin = act.parse.json_get("coolRangeLow", resp, i + 1);
        cmax = act.parse.json_get("coolRangeHigh", resp, i + 1);
        cdelta = act.parse.json_get("heatCoolMinDelta", resp, i + 1);
        adapter.getItem(i).set_range(mode,
                                     Common.a2i(hmin),
                                     Common.a2i(hmax),
                                     Common.a2i(cmin),
                                     Common.a2i(cmax),
                                     Common.a2i(cdelta));
    }

    act.runOnUiThread(new Runnable() {
        public void run() {
            act.thermostat.init_view();
        }
    });
}

private String ecobee_param(String info, String id)
{

    String select = id.isEmpty() ? "registered" : "thermostats";
    return "format=json&body={\"selection\":{\"selectionType\":\"" + select +
           "\",\"selectionMatch\":\"" + id +
           "\",\"" + info + "\":true}}";
}

//  The api token can be used to create a new PIN
//
//  Note: this routine makes asynchronous HTTP requests
//    so it better not run on the UI thread
//
public String ecobee_get_pin(String api)
{
    String resp;

    resp = act.call_api("GET",
                               ECO_URL + ECO_AUTHORIZE,
                               "response_type=ecobeePin&client_id=" + api +
                                   "&scope=smartWrite",
                               "",
                               null);
    act.ecobee_access = act.parse.json_get("code", resp, 1);
    Preferences pref = act.pref;
    pref.put("ecobee_access", act.ecobee_access);
    String pin = act.parse.json_get("ecobeePin", resp, 1);
    return pin;
}

private void ecobee_token(String type, String code, String api)
{
    String resp;
    String token;

    resp = act.call_api("POST",
                        ECO_URL + ECO_REFRESH,
                        "grant_type=" + type +
                            "&code=" + code +
                            "&client_id=" + api,
                        "",
                        null);
    if ((token = act.parse.json_get("access_token", resp, 1)) == null)
        return;
    act.ecobee_access = act.parse.json_get("access_token", resp, 1);
    act.ecobee_refresh = act.parse.json_get("refresh_token", resp, 1);
    Preferences pref = act.pref;
    pref.put("ecobee_access", act.ecobee_access);
    pref.put("ecobee_refresh", act.ecobee_refresh);
    return;
}

//  The token has been validated by the user so
//    it's legal to use the auth token to get
//    access and refresh tokens
//
//  Note: this routine makes asynchronous HTTP requests
//    so it better not run on the UI thread
//
public void ecobee_authorize(String api)
{

    ecobee_token("ecobeePin", act.ecobee_access, api);
}

private void ecobee_refresh()
{

    ecobee_token("refresh_token", act.ecobee_refresh, act.ecobee_api);
}

private String ecobee_query(String info, String id)
{
    String resp;

    resp = act.call_api("GET",
                               ECO_URL + ECO_DATA,
                               ecobee_param(info, id),
                               "Bearer " + act.ecobee_access,
                               null);
    String code = act.parse.json_get("code", resp, 1);
    if (code == null || !code.equals("0"))
        return null;
    else
        return resp;
}

public void ecobee_resume(ThermostatDevice dev)
{

    String code = dev.get_code();
    String body = "{" + "\n" +
                  " \"selection\":{" + "\n" +
                  "  \"selectionType\":\"thermostats\"," + "\n" +
                  "  \"selectionMatch\":\"" + code + "\"" + "\n" +
                  " }," + "\n" +
                  " \"functions\":[" + "\n" +
                  "  {" + "\n" +
                  "   \"type\":\"resumeProgram\"," + "\n" +
                  "   \"params\":{" + "\n" +
                  "    \"resumeAll\":false" + "\n" +
                  "   }" + "\n" +
                  "  }" + "\n" +
                  " ]" + "\n" +
                  "}";
    String resp = act.call_api("POST",
                               ECO_URL + ECO_DATA,
                               "format=json",
                               "Bearer " + act.ecobee_access,
                               body);
}

public void ecobee_hold(int heat, ThermostatDevice dev)
{

    int type = dev.get_hold();
    if (type == Thermostat.HOLD_RUNNING)
        return;
    String code = dev.get_code();
    int cool = heat;
    if (dev.get_mode().equals("auto"))
        cool += dev.get_c_delta();
    String hmode = (type == Thermostat.HOLD_TEMPORARY) ? "nextTransition" : "indefinite";
    String body = "{" + "\n" +
                  " \"selection\":{" + "\n" +
                  "  \"selectionType\":\"thermostats\"," + "\n" +
                  "  \"selectionMatch\":\"" + code + "\"" + "\n" +
                  " }," + "\n" +
                  " \"functions\":[" + "\n" +
                  "  {" + "\n" +
                  "   \"type\":\"setHold\"," + "\n" +
                  "   \"params\":{" + "\n" +
                  "    \"holdType\":\"" + hmode + "\"," + "\n" +
                  "    \"heatHoldTemp\":" + heat + "," + "\n" +
                  "    \"coolHoldTemp\":" + cool + "\n" +
                  "   }" + "\n" +
                  "  }" + "\n" +
                  " ]" + "\n" +
                  "}";
    String resp = act.call_api("POST",
                               ECO_URL + ECO_DATA,
                               "format=json",
                               "Bearer " + act.ecobee_access,
                               body);
}

private String runtime()
{
    String resp;

    if ((resp = ecobee_query("includeRuntime", "")) == null) {
        ecobee_refresh();
        if ((resp = ecobee_query("includeRuntime", "")) == null)
            return null;
    }
    return resp;
}

public void ecobee_getstate(ThermostatDevice dev)
{

    String resp = ecobee_query("includeEvents", dev.get_code());
    if (resp == null)
        return;
    int mode = Thermostat.HOLD_RUNNING;
    if (act.parse.json_get("type", resp, dev.get_index()).equals("hold"))
        mode = act.parse.json_get("endDate", resp, dev.get_index()).equals("2035-01-01") ?
                        Thermostat.HOLD_PERMANENT :
                        Thermostat.HOLD_TEMPORARY;
    dev.set_hold(mode);
}

private void get_data(String resp, int idx, ThermostatDevice dev)
{

    dev.set_temp(act.parse.json_get("%1,actualTemperature", resp, idx));
    dev.set_humid(act.parse.json_get("actualHumidity", resp, idx));
}

public void update(ThermostatAdapter adapter)
{
    int i;
    String resp;
    ThermostatDevice dev;

    //
    //  Get the data
    //
    if ((resp = runtime()) == null)
        return;
    for (i = 0; i < adapter.getCount(); i++) {
        dev = adapter.getItem(i);
        get_data(resp, dev.get_index(), dev);
    }
}

}
