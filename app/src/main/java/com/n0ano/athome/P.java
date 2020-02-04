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

public static final int PREF_VERSION = 2;

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

    switch (get_int("pref_version")) {

    default:
    case 0:
        cfg_v0_v2();
        break;

    case P.PREF_VERSION:
        break;

    }
}

private static void cfg_v0_v2()
{

    Log.d("Convert preferences to version " + P.PREF_VERSION);

	put("general_layout", get_old("general_layout", Popup.LAYOUT_TABLET));
	put("general_on", get_old("general_on", -1));
	put("general_off", get_old("general_off", -1));
	put("egauge_layout", get_old("egauge_layout", Popup.LAYOUT_TABLET));
	put("egauge_progress", get_old("egauge_progress", 1));
	put("egauge_url", get_old("egauge_url", ""));
	put("egauge_clock", get_old("egauge_clock", false));
	put("weather_layout", get_old("weather_layout", Popup.LAYOUT_TABLET));
	put("weather_progress", get_old("weather_progress", 1));
	put("wunder_id", get_old("wunder_id", ""));
	put("wunder_key", get_old("wunder_key", ""));
	put("thermostat_layout", get_old("thermostat_layout", Popup.LAYOUT_TABLET));
	put("ecobee_api", get_old("ecobee_api", ""));
	put("ecobee_access", get_old("ecobee_access", ""));
	put("ecobee_refresh", get_old("ecobee_refresh", ""));
	put("outlets_layout", get_old("outlets_layout", Popup.LAYOUT_TABLET));
	put("outlets_battery", get_old("outlets_battery", ""));
	put("outlets_cols", get_old("outlets_cols", C.OUTLETS_COLS));
	put("outlets_batt_min", get_old("outlets_batt_min", C.BATTERY_LOW));
	put("outlets_batt_max", get_old("outlets_batt_max", C.BATTERY_HIGH));
	put("outlets_batt_level", get_old("outlets_batt_level", 0));
	put("x10_url", get_old("x10_url", ""));
	put("x10_jwt", get_old("x10_jwt", "none"));
	put("tplink_user", get_old("tplink_user", ""));
	put("tplink_pwd", get_old("tplink_pwd", ""));
	put("log_uri", get_old("log_uri", ""));
	put("log_params", get_old("log_params", ""));
	put("debug", get_old("debug", 0));

    put("pref_version", P.PREF_VERSION);
}

private static void init_defaults()
{

    //
    //  Turns out, only the integer parameters have different
    //    defaults, the only boolean defaults to false and all
    //    of the strings default to ""
    //
	def_int.put("general_layout", new Integer(Popup.LAYOUT_TABLET));
	def_int.put("general_on", new Integer(-1));
	def_int.put("general_off", new Integer(-1));
	def_int.put("egauge_layout", new Integer(Popup.LAYOUT_TABLET));
	def_int.put("egauge_progress", new Integer(1));
	def_int.put("weather_layout", new Integer(Popup.LAYOUT_TABLET));
	def_int.put("weather_progress", new Integer(1));
	def_int.put("thermostat_layout", new Integer(Popup.LAYOUT_TABLET));
	def_int.put("outlets_layout", new Integer(Popup.LAYOUT_TABLET));
	def_int.put("outlets_cols", new Integer(C.OUTLETS_COLS));
	def_int.put("outlets_batt_min", new Integer(C.BATTERY_LOW));
	def_int.put("outlets_batt_max", new Integer(C.BATTERY_HIGH));
	//def_int.put("outlets_batt_level", new Integer(0);
	//def_int.put("debug", new Integer(0);

    //
    //  If there were any other boolean defaults
    //
	//def_boolean("egauge_clock", false);

    //
    //  If there were any other String defaults
    //
	//def_string.put("egauge_url", "");
	//def_string.put("wunder_id", "");
	//def_string.put("wunder_key", "");
	//def_string.put("ecobee_api", "");
	//def_string.put("ecobee_access", "");
	//def_string.put("ecobee_refresh", "");
	//def_string.put("outlets_battery", "");
	//def_string.put("x10_url", "");
	//def_string.put("x10_jwt", "");
	//def_string.put("tplink_user", "");
	//def_string.put("tplink_pwd", "");
	//def_string.put("log_uri", "");
	//def_string.put("log_params", "");
	//def_string.put("config_uri", "");
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
        JSONObject json = new JSONObject(cfg);
        json_cfg = json;
    } catch (Exception e) {
        Log.d("bad config string(" + cfg + ") - " + e);
    }
}

public static void rm_key(String key)
{

    json_cfg.remove(key);
	SharedPreferences.Editor editor = pref.edit();
	editor.remove(key);
	editor.commit();
}

public static int get_int(String key)
{
    
    Integer def = def_int.get(key);
    return json_cfg.optInt(key, def == null ? 0 : def.intValue());
}
public static void put(String key, int value)
{

    try {
        json_cfg.put(key, value);
    } catch (Exception e) {
        Log.d("Preferences JSON put error on int - " + e);
    }
	SharedPreferences.Editor editor = pref.edit();
	editor.putString("config", C.json2str(json_cfg));
	editor.commit();
}

public static boolean get_boolean(String key) { return json_cfg.optBoolean(key, false); }
public static void put(String key, boolean value)
{

    try {
        json_cfg.put(key, value);
    } catch (Exception e) {
        Log.d("Preferences JSON put error on int - " + e);
    }
	SharedPreferences.Editor editor = pref.edit();
	editor.putString("config", C.json2str(json_cfg));
	editor.commit();
}

public static String get_string(String key) { return json_cfg.optString(key, ""); }
public static void put(String key, String value)
{

    try {
        json_cfg.put(key, value);
    } catch (Exception e) {
        Log.d("Preferences JSON put error on int - " + e);
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
