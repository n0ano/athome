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
}

public String get_string(String key, String def)
{

	return pref.getString(key, def);
}

public void put_string(String key, String value)
{

	SharedPreferences.Editor editor = pref.edit();
	editor.putString(key, value);
	editor.commit();
}

public int get_int(String key, int def)
{

	return pref.getInt(key, def);
}

public void put_int(String key, int value)
{

	SharedPreferences.Editor editor = pref.edit();
	editor.putInt(key, value);
	editor.commit();
}

public boolean get_boolean(String key, boolean def)
{

	return pref.getBoolean(key, def);
}

public void put_boolean(String key, boolean value)
{

	SharedPreferences.Editor editor = pref.edit();
	editor.putBoolean(key, value);
	editor.commit();
}

}
