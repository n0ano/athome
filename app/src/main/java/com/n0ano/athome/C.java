package com.n0ano.athome;

import android.os.SystemClock;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

//
// Created by n0ano on 11/7/18.
//
public class C {

public static final int SS_OP_INIT =    0;
public static final int SS_OP_RESET =   1;
public static final int SS_OP_BLOCK =   2;
public static final int SS_OP_UPDATE =  3;
public static final int SS_OP_STOP =    4;

public final static int BATTERY_LOW  = 20;
public final static int BATTERY_HIGH = 90;

public final static int OUTLETS_COLS = 3;

public final static String CONFIG_URI = "/cgi-bin/athome/config";
public final static String CONFIG_LOAD =  "load";                
public final static String CONFIG_SAVE =  "save";

public static boolean paused = true;

public static String suffix(String str)
{

    int idx = str.lastIndexOf(":");
    return (idx >= 0) ? str.substring(idx + 1) : "";
}

public static int a2i(String num)
{

    return Integer.parseInt(num);
}

public static String i2a(int num)
{

    return Integer.toString(num);
}

public static String epoch_str(long epoch)
{

    Date date = new Date(epoch * 1000L);
    DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    format.setTimeZone(TimeZone.getDefault());
    return format.format(date);
}

public static String indent(String str)
{
    char c;
    String c1;

    boolean quote = false;
    String s = "";
    String pre = "";
    int max = str.length();
    for (int i = 0; i < max; i++) {
        if ((i < (max - 5)) && str.substring(i, i + 5).equals(" ==> ")) {
            s += " ==> \n";
            i += 4;
            continue;
        }
        if ((c = str.charAt(i)) == '"') {
            s += String.valueOf(c);
            quote = !quote;
            continue;
        } else if (quote) {
            s += String.valueOf(c);
            continue;
        }
        switch (c) {

        case '\\':
            if (str.charAt(i + 1) != 'n')
                s += "\\" + str.charAt(i + 1);
            ++i;
            break;

        case '{':
            pre += "  ";
            s += "{\n" + pre;
            break;

        case '}':
            if (pre.length() >= 2)
                pre = pre.substring(2);
            s += "}\n" + pre;
            break;

        case '[':
            pre += "  ";
            s += "[\n" + pre;
            break;

        case ']':
            if (pre.length() >= 2)
                pre = pre.substring(2);
            s += "]\n" + pre;
            break;

        case ',':
            s += ",\n" + pre;
            break;

        default:
            s += String.valueOf(c);
            break;

        }
    }
    return s;
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

    P.put("ss:enable", P_SS.get("ss_enable", false));
    P.put("ss:start", P_SS.get("ss_start", 0));
    P.put("ss:delay", P_SS.get("ss_delay", 0));
    P.put("ss:fade", P_SS.get("ss_fade", 0));
    return P.get_cfg(indent);
}

public static void new_cfg(String cfg)
{

    P.new_cfg(cfg);
    P_SS.put("ss_enable", P.get_boolean("ss:enable"));
    P_SS.put("ss_start", P.get_int("ss:start"));
    P_SS.put("ss_delay", P.get_int("ss:delay"));
    P_SS.put("ss_fade", P.get_int("ss:fade"));
}

}
