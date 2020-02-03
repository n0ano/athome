package com.n0ano.athome.SS;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.n0ano.athome.Log;

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

private static SharedPreferences pref;

// Preferences: class constructor
//
//   act - activity that instantiated the class
//
public static void init(SharedPreferences pref)
{

	P.pref = pref;

    switch (get("pref_version", 0)) {

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
	put("debug", pref.getInt("debug", 0));
	put("egauge_progress", pref.getInt("egauge_progress", 1));
	put("outlets_batt_level", pref.getInt("outlets_batt_level", 0));
	put("weather_progress", pref.getInt("weather_progress", 1));

    put("pref_version", P.PREF_VERSION);
}

public static void rm_key(String key)
{

	SharedPreferences.Editor editor = pref.edit();
	editor.remove(key);
	editor.commit();
}

public static int get(String key, int def)
{

    String str = pref.getString(key, Integer.toString(def));
    if (str.isEmpty())
        return 0;
    return Integer.parseInt(str);
}
public static void put(String key, int value)
{

	SharedPreferences.Editor editor = pref.edit();
	editor.putString(key, Integer.toString(value));
	editor.commit();
}

public static boolean get(String key, boolean def)
{

    String str = pref.getString(key, (def ? "true" : "false"));
    return str.equals("true");
}
public static void put(String key, boolean value)
{

	SharedPreferences.Editor editor = pref.edit();
	editor.putString(key, (value ? "true" : "false"));
	editor.commit();
}

public static String get(String key, String def)
{

    return pref.getString(key, def);
}
public static void put(String key, String value)
{

	SharedPreferences.Editor editor = pref.edit();
	editor.putString(key, value);
	editor.commit();
}

}
