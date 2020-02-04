package com.n0ano.athome;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

// Created by n0ano on 10/10/16.
//
// Class to handle application preferences
//
public class P_SS {

public static final String PREF_NAME = "SS_Preferences";

public static final int PREF_VERSION = 2;

private static SharedPreferences pref = null;

// Preferences: class constructor
//
//   act - activity that instantiated the class
//
public static void init(SharedPreferences pref)
{

    if (P_SS.pref != null)
        return;
	P_SS.pref = pref;
}

public static void rm_key(String key)
{

	SharedPreferences.Editor editor = pref.edit();
	editor.remove(key);
	editor.commit();
}

public static boolean get(String key, boolean def) { return pref.getBoolean(key, def); }
public static void put(String key, boolean value)
{

	SharedPreferences.Editor editor = pref.edit();
	editor.putBoolean(key, value);
	editor.commit();
}

public static int get(String key, int def) { return pref.getInt(key, def); }
public static void put(String key, int value)
{

	SharedPreferences.Editor editor = pref.edit();
	editor.putInt(key, value);
	editor.commit();
}

public static String get(String key, String def) { return pref.getString(key, def); }
public static void put(String key, String value)
{

	SharedPreferences.Editor editor = pref.edit();
	editor.putString(key, value);
	editor.commit();
}

}
