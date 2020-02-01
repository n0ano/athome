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

public static boolean running = true;

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
        return null;
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

public static boolean working()
{

    return running;
}

public static Thread data_thread(final int period, final DoitCallback cb)
{

    Thread thread = new Thread(new Runnable() {
        public void run() {
            while (working()) {
                //
                //  Get the data
                //
                cb.doit(null);

                SystemClock.sleep(period);
            }
        }
    });
    thread.start();
    return thread;
}

}
