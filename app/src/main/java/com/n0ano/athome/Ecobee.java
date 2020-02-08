package com.n0ano.athome;

import android.view.View;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

// Created by n0ano on 10/10/16.
//
// Class to handle Ecobee thermostat data
//
public class Ecobee {

public final static int PERIOD = 60;   // temperature only changes once a minute

public final static String ECO_LOGIN = "https://ecobee.com";
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
    JSONObject json, info;
    JSONArray list;
    int max;
    String resp;
    String name, id;
    String mode, hmin, hmax, cmin, cmax, cdelta;

    if ((json = runtime()) == null)
        return;

    list = json.optJSONArray("thermostatList");
    if (list == null)
        return;

    max = list.length();
    for (int i = 0; i < max; i++) {
        info = (JSONObject)C.json_get(list, i);
        adapter.add_device(new ThermostatDevice(info.optString("name", ""),
                                                i + 1,
                                                info.optString("identifier", ""),
                                                View.inflate(act, R.layout.thermostat, null)));
    }

    if ((json = ecobee_query("includeSettings", "")) == null)
        return;

    list = json.optJSONArray("thermostatList");
    if (list == null)
        return;

    max = list.length();
    for (int i = 0; i < max; i++) {
        info = (JSONObject)C.json_get(list, i);
        JSONObject settings = info.optJSONObject("settings");
        if (settings != null) {
            mode = settings.optString("hvacMode", "");
            hmin = settings.optString("heatRangeLow", "");
            hmax = settings.optString("heatRangeHigh", "");
            cmin = settings.optString("coolRangeLow", "");
            cmax = settings.optString("coolRangeHigh", "");
            cdelta = settings.optString("heatCoolMinDelta", "");
            adapter.getItem(i).set_range(mode,
                                         C.a2i(hmin),
                                         C.a2i(hmax),
                                         C.a2i(cmin),
                                         C.a2i(cmax),
                                         C.a2i(cdelta));
        }
    }
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
    JSONObject json;
    String resp;

    resp = act.call_api(ECO_URL + ECO_AUTHORIZE,
                        "response_type=ecobeePin&client_id=" + api + "&scope=smartWrite");
    if ((json = C.str2json(resp)) == null)
        return "";

    P.put("thermostat:ecobee_access", json.optString("code", ""));
    String pin = json.optString("ecobeePin", "");
    return pin;
}

private void ecobee_token(String type, String code, String api)
{
    JSONObject json;
    String resp;
    String token;

    resp = act.call_api("POST",
                        ECO_URL + ECO_REFRESH,
                        "grant_type=" + type +
                            "&code=" + code +
                            "&client_id=" + api,
                        "",
                        null);
    if ((json = C.str2json(resp)) == null)
        return;

    token = json.optString("access_token", "");
    if (token.isEmpty())
        return;
    P.put("thermostat:ecobee_access", token);
    P.put("thermostat:ecobee_refresh", json.optString("refresh_token", ""));
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

    ecobee_token("ecobeePin", P.get_string("thermostat:ecobee_access"), api);
}

private void ecobee_refresh()
{

    ecobee_token("refresh_token", P.get_string("thermostat:ecobee_refresh"), P.get_string("thermostat:ecobee_api"));
}

private JSONObject ecobee_query(String info, String id)
{
    JSONObject json;
    JSONObject status;

    String resp = act.call_api("GET",
                               ECO_URL + ECO_DATA,
                               ecobee_param(info, id),
                               "Bearer " + P.get_string("thermostat:ecobee_access"),
                               null);
    try {
        json = C.str2json(resp);
        status = (JSONObject)json.get("status");
        if (status.getInt("code") == 0)
            return json;
        Log.d("Ecobee: query(" + resp + ") bad status");
    } catch (Exception e) {
        Log.d("Ecobee: query(" + resp + ") json parse error - " + e);
    }
    return null;
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
                               "Bearer " + P.get_string("thermostat:ecobee_access"),
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
                               "Bearer " + P.get_string("thermostat:ecobee_access"),
                               body);
}

private JSONObject runtime()
{
    JSONObject resp;

    if ((resp = ecobee_query("includeRuntime", "")) == null) {
        ecobee_refresh();
        if ((resp = ecobee_query("includeRuntime", "")) == null)
            return null;
    }
    return resp;
}

public void get_therm(JSONObject info, ThermostatDevice dev)
{

    if (info != null) {
        dev.set_temp(((float)info.optLong("actualTemperature", 0)) / 10.0f);
        dev.set_humid(info.optLong("actualHumidity", 0));
    }
}

public void get_data(ThermostatAdapter adapter)
{
    int i;
    JSONObject resp, info;
    JSONArray list;
    ThermostatDevice dev;

    //
    //  Get the data
    //
    if ((resp = runtime()) == null)
        return;
    list = resp.optJSONArray("thermostatList");
    if (list == null)
        return;

    for (i = 0; i < adapter.getCount(); i++) {
        dev = adapter.getItem(i);
        info = (JSONObject)C.json_get(list, dev.get_index() - 1);
        get_therm(info.optJSONObject("runtime"), dev);
    }
}

}
