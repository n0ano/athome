package com.n0ano.athome;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.n0ano.athome.Log;

import org.json.JSONObject;

import java.util.HashMap;

// Created by n0ano on 10/10/16.
//
// Class to handle application preferences
//
public class P {

public static final int MAX_RECENT = 10;

public static final String PREF_NAME = "AtHome_Preferences";

public static final int PREF_VERSION = 3;

private static final int PREF_INT =     0;
private static final int PREF_STRING =  1;

private static SharedPreferences pref = null;

private static JSONObject json_cfg;
private static HashMap<String, Integer> def_int = new HashMap<String, Integer>();

// Preferences: class constructor
//
//   act - activity that instantiated the class
//
public static void init(SharedPreferences pref)
{

    if (P.pref != null)
        return;
	P.pref = pref;
    init_defaults();
    json_cfg = C.str2json(pref.getString("config", "{\"pref_version\":0}"));
}

private static void init_defaults()
{

    //
    //  Turns out, only the integer parameters have different
    //    defaults, the only boolean defaults to false, the only
    //    long defaults to 0,  and all of the strings default to ""
    //
	def_int.put("general:layout", new Integer(Popup.LAYOUT_TABLET));
	def_int.put("general:on", new Integer(-1));
	def_int.put("general:off", new Integer(-1));
	def_int.put("egauge:layout", new Integer(Popup.LAYOUT_TABLET));
	def_int.put("egauge:progress", new Integer(1));
	def_int.put("weather:layout", new Integer(Popup.LAYOUT_TABLET));
	def_int.put("weather:progress", new Integer(1));
    def_int.put("weather:min_temp", new Integer(9999));
    def_int.put("weather:max_temp", new Integer(-9999));
	def_int.put("thermostat:layout", new Integer(Popup.LAYOUT_TABLET));
	def_int.put("outlets:layout", new Integer(Popup.LAYOUT_TABLET));
	def_int.put("outlets:cols", new Integer(C.OUTLETS_COLS));
	def_int.put("outlets:batt_min", new Integer(C.BATTERY_LOW));
	def_int.put("outlets:batt_max", new Integer(C.BATTERY_HIGH));
	//def_int.put("outlets:batt_level", new Integer(0);
	//def_int.put("general:log_length", new Integer(0);
	//def_int.put("debug", new Integer(0);

    //
    //  If there were any other boolean defaults
    //
	//def_boolean("egauge:clock", false);

    //
    //  If there were any other String defaults
    //
	//def_string.put("egauge:url", "");
	//def_string.put("egauge:user", "");
	//def_string.put("egauge:pwd", "");
	//def_string.put("weather:wunder_id", "");
	//def_string.put("weather:wunder_key", "");
    //def_string.put("weather:min_time", "");
    //def_string.put("weather:max_time", "");
	//def_string.put("thermostat:ecobee_api", "");
	//def_string.put("thermostat:ecobee_access", "");
	//def_string.put("thermostat:ecobee_refresh", "");
	//def_string.put("outlets:battery", "");
	//def_string.put("outlets:hold", "");
	//def_string.put("outlets:x10_url", "");
	//def_string.put("outlets:x10_jwt", "");
	//def_string.put("outlets:tplink_user", "");
	//def_string.put("outlets:tplink_pwd", "");
	//def_string.put("general:log_uri", "");
	//def_string.put("general:log_params", "");
}

public static String get_cfg(int indent)
{

    if (indent == 0)
        return C.json2str(json_cfg);
    try {
        return json_cfg.toString(indent);
    } catch (Exception e) {
        Log.d("get_cfg JSON to string error - " + e);
        return "";
    }
}

public static void new_cfg(String cfg)
{

    try {
        json_cfg = new JSONObject(cfg);
    } catch (Exception e) {
        Log.d("bad config string(" + cfg + ") - " + e);
        return;
    }
    Log.d("new config loaded");
	SharedPreferences.Editor editor = pref.edit();
	editor.putString("config", C.json2str(json_cfg));
	editor.commit();
}

private static String get_json(String key)
{

    int idx = key.indexOf(":");
    if (idx < 0)
        return null;
    return key.substring(0, idx);
}

private static String get_key(String key)
{

    int idx = key.indexOf(":");
    if (idx < 0)
        return key;
    return key.substring(idx + 1);
}

private static JSONObject get_obj(String name)
{

    String obj_name = get_json(name);
    if (obj_name == null)
        return json_cfg;
    JSONObject obj = (JSONObject)json_cfg.opt(obj_name);
    if (obj == null)
        return new JSONObject();
    return obj;
}

public static void rm_key(String key)
{

    try {
        String json = get_json(key);
        if (json == null)
            json_cfg.remove(get_key(key));
        else {
            JSONObject obj = get_obj(key);
            obj.remove(get_key(key));
            json_cfg.put(json, obj);
        }
    } catch (Exception e) {
        Log.d("JSON put error(" + key + ") - " + e);
        return;
    }
    
	SharedPreferences.Editor editor = pref.edit();
	editor.putString("config", C.json2str(json_cfg));
	editor.commit();
}

public static int get_int(String key)
{
    
    JSONObject obj = get_obj(key);
    Integer def = def_int.get(key);
    return obj.optInt(get_key(key), def == null ? 0 : def.intValue());
}
public static void put(String key, int value)
{

    try {
        String json = get_json(key);
        if (json == null)
            json_cfg.put(get_key(key), value);
        else {
            JSONObject obj = get_obj(key);
            obj.put(get_key(key), value);
            json_cfg.put(json, obj);
        }
    } catch (Exception e) {
        Log.d("Preferences JSON put error:" + key + "=" + value + " - " + e);
        return;
    }
	SharedPreferences.Editor editor = pref.edit();
	editor.putString("config", C.json2str(json_cfg));
	editor.commit();
}

public static long get_long(String key)
{
    
    JSONObject obj = get_obj(key);
    return obj.optLong(get_key(key), 0);
}
public static void put(String key, long value)
{

    try {
        String json = get_json(key);
        if (json == null)
            json_cfg.put(get_key(key), value);
        else {
            JSONObject obj = get_obj(key);
            obj.put(get_key(key), value);
            json_cfg.put(json, obj);
        }
    } catch (Exception e) {
        Log.d("Preferences JSON put error:" + key + "=" + value + " - " + e);
        return;
    }
	SharedPreferences.Editor editor = pref.edit();
	editor.putString("config", C.json2str(json_cfg));
	editor.commit();
}

public static boolean get_boolean(String key)
{
    
    JSONObject obj = get_obj(key);
    return obj.optBoolean(get_key(key), false);
}
public static void put(String key, boolean value)
{

    try {
        String json = get_json(key);
        if (json == null)
            json_cfg.put(get_key(key), value);
        else {
            JSONObject obj = get_obj(key);
            obj.put(get_key(key), value);
            json_cfg.put(json, obj);
        }
    } catch (Exception e) {
        Log.d("Preferences JSON put error:" + key + "=" + value + " - " + e);
        return;
    }
	SharedPreferences.Editor editor = pref.edit();
	editor.putString("config", C.json2str(json_cfg));
	editor.commit();
}

public static double get_double(String key)
{
    
    JSONObject obj = get_obj(key);
    return obj.optDouble(get_key(key), 0.0);
}
public static double get_double(String key, double def)
{
    
    JSONObject obj = get_obj(key);
    return obj.optDouble(get_key(key), def);
}
public static void put(String key, double value)
{

    try {
        String json = get_json(key);
        if (json == null)
            json_cfg.put(get_key(key), value);
        else {
            JSONObject obj = get_obj(key);
            obj.put(get_key(key), value);
            json_cfg.put(json, obj);
        }
    } catch (Exception e) {
        Log.d("Preferences JSON put error:" + key + "=" + value + " - " + e);
        return;
    }
	SharedPreferences.Editor editor = pref.edit();
	editor.putString("config", C.json2str(json_cfg));
	editor.commit();
}

public static String get_string(String key)
{
    
    JSONObject obj = get_obj(key);
    return obj.optString(get_key(key), "");
}
public static void put(String key, String value)
{

    try {
        String json = get_json(key);
        if (json == null)
            json_cfg.put(get_key(key), value);
        else {
            JSONObject obj = get_obj(key);
            obj.put(get_key(key), value);
            json_cfg.put(json, obj);
        }
    } catch (Exception e) {
        Log.d("Preferences JSON put error:" + key + "=" + value + " - " + e);
        return;
    }
	SharedPreferences.Editor editor = pref.edit();
	editor.putString("config", C.json2str(json_cfg));
	editor.commit();
}

public static Object get_JSONObject(String key)
{
    
    JSONObject obj = get_obj(key);
    return obj.opt(get_key(key));
}
public static void put(String key, Object value)
{

    try {
        String json = get_json(key);
        if (json == null)
            json_cfg.put(get_key(key), value);
        else {
            JSONObject obj = get_obj(key);
            obj.put(get_key(key), value);
            json_cfg.put(json, obj);
        }
    } catch (Exception e) {
        Log.d("Preferences JSON put error:" + key + "=" + value + " - " + e);
        return;
    }
	SharedPreferences.Editor editor = pref.edit();
	editor.putString("config", C.json2str(json_cfg));
	editor.commit();
}

public static int get_old(String key, int def)
{

    String str = pref.getString(key, Integer.toString(def));
    if (str.isEmpty())
        return 0;
    return Integer.parseInt(str);
}

public static boolean get_old(String key, boolean def)
{

    String str = pref.getString(key, (def ? "true" : "false"));
    return str.equals("true");
}

public static String get_old(String key, String def)
{

    return pref.getString(key, def);
}

//
//  These two are special in that they don't store their
//    keys in the configuration JSON structure, these are
//    sui generis.
//
public static String get_string(String key, String def)
{

    return pref.getString(key, def);
}
public static void put_string(String key, String value)
{

	SharedPreferences.Editor editor = pref.edit();
	editor.putString(key, value);
	editor.commit();
}

}
