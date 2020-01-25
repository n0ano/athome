package com.n0ano.athome;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.n0ano.athome.Log;

// Created by n0ano on 10/10/16.
//
// Class to handle application preferences
//
public class Preferences {

public static final int MAX_RECENT = 10;

private final String PREF_NAME = "AtHome_Preferences";

private final int PREF_VERSION = 2;

private static final int PREF_INT =     0;
private static final int PREF_STRING =  1;

private SharedPreferences pref;
Activity my_act;

// Preferences: class constructor
//
//   act - activity that instantiated the class
//
public Preferences(Activity act)
{

	my_act = act;
	pref = act.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    switch (get("pref_version", 0)) {

    default:
    case 0:
        cfg_v0_v2();
        break;

    case PREF_VERSION:
        break;

    }
}

private void cfg_v0_v2()
{

    Log.d("Convert preferences to version " + PREF_VERSION);
	put("debug", pref.getInt("debug", 0));
	put("egauge_layout", pref.getInt("egauge_layout", Popup.LAYOUT_TABLET));
	put("egauge_progress", pref.getInt("egauge_progress", 1));
	put("general_layout", pref.getInt("general_layout", Popup.LAYOUT_TABLET));
	put("outlets_batt_level", pref.getInt("outlets_batt_level", 0));
	put("outlets_cols", pref.getInt("outlets_cols", MainActivity.OUTLETS_COLS));
	put("outlets_batt_max", pref.getInt("outlets_batt_max", MainActivity.BATTERY_HIGH));
	put("outlets_batt_min", pref.getInt("outlets_batt_min", MainActivity.BATTERY_LOW));
	put("thermostat_layout", pref.getInt("thermostat_layout", Popup.LAYOUT_TABLET));
	put("weather_layout", pref.getInt("weather_layout", Popup.LAYOUT_TABLET));
	put("weather_progress", pref.getInt("weather_progress", 1));

    put("pref_version", PREF_VERSION);
}

public void rm_key(String key)
{

	SharedPreferences.Editor editor = pref.edit();
	editor.remove(key);
	editor.commit();
}

public int get(String key, int def)
{

    String str = pref.getString(key, Integer.toString(def));
    if (str.isEmpty())
        return 0;
    return Integer.parseInt(str);
}
public void put(String key, int value)
{

	SharedPreferences.Editor editor = pref.edit();
	editor.putString(key, Integer.toString(value));
	editor.commit();
}

public boolean get(String key, boolean def)
{

    String str = pref.getString(key, (def ? "true" : "false"));
    return str.equals("true");
}
public void put(String key, boolean value)
{

	SharedPreferences.Editor editor = pref.edit();
	editor.putString(key, (value ? "true" : "false"));
	editor.commit();
}

public String get(String key, String def)
{

    return pref.getString(key, def);
}
public void put(String key, String value)
{

	SharedPreferences.Editor editor = pref.edit();
	editor.putString(key, value);
	editor.commit();
}

}
