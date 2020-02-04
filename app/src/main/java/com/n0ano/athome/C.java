package com.n0ano.athome;

import android.os.SystemClock;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

//
// Created by n0ano on 11/7/18.
//
public class C {

public static final int SS_OP_INIT =    0;
public static final int SS_OP_RESET =   1;
public static final int SS_OP_BLOCK =   2;
public static final int SS_OP_UPDATE =  3;

public final static int BATTERY_LOW  = 20;
public final static int BATTERY_HIGH = 90;

public final static int OUTLETS_COLS = 3;

public final static String CONFIG_URI = "/cgi-bin/athome/config";
public final static String CONFIG_LOAD =  "load";                
public final static String CONFIG_SAVE =  "save";

public static boolean paused = true;

public static int a2i(String num)
{

    return Integer.parseInt(num);
}

public static String i2a(int num)
{

    return Integer.toString(num);
}

public static String suffix(String str)
{

    int idx = str.lastIndexOf(":");
    return (idx >= 0) ? str.substring(idx + 1) : "";
}

public static JSONObject str2json(String str)
{
    JSONObject json;

    try {
        json = new JSONObject(str);
    } catch (Exception e) {
        Log.d("JSON parse error(" + str + ") - " + e);
        json = new JSONObject();
    }
    return json;
}

public static String json2str(JSONObject json)
{

    try {
        return json.toString();
    } catch (Exception e) {
        return "";
    }
}

public static Object json_get(JSONArray json, int i)
{

    try {
        return json.get(i);
    } catch (Exception e) {
        Log.d("JSON get error - " + e);
        return null;
    }
}

public static String encode_time(int t)
{

    if (t < 0)
        return "";
    int hr = t / 100;
    t -= hr * 100;
    return String.valueOf(hr) + ":" + String.format("%02d", t);
}

public static int decode_time(String t)
{

    if (t.isEmpty())
        return -1;
    int idx = t.indexOf(":");
    if (idx < 0)
        return Integer.parseInt(t);
    else
        return (Integer.parseInt(t.substring(0, idx)) * 100) + Integer.parseInt(t.substring(idx + 1));
}

public static String get_cfg(int indent)
{

    P.put("ss_enable", P_SS.get("ss_enable", false));
    P.put("ss_start", P_SS.get("ss_start", 0));
    P.put("ss_delay", P_SS.get("ss_delay", 0));
    P.put("ss_fade", P_SS.get("ss_fade", 0));
    return P.get_cfg(indent);
}

public static void new_cfg(String cfg)
{

    P.new_cfg(cfg);
    P_SS.put("ss_enable", P.get_boolean("ss_enable"));
    P_SS.put("ss_start", P.get_int("ss_start"));
    P_SS.put("ss_delay", P.get_int("ss_delay"));
    P_SS.put("ss_fade", P.get_int("ss_fade"));
}

}
