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
}

public int get(String key, int def)
{

    String str = pref.getString(key, Integer.toString(def));
    if (str.isEmpty())
        return 0;
    return Integer.parseInt(str);
}

public boolean get(String key, boolean def)
{

    String str = pref.getString(key, (def ? "true" : "false"));
    return str.equals("true");
}

public String get(String key, String def)
{

    return pref.getString(key, def);
}

public void put(String key, int value)
{

	SharedPreferences.Editor editor = pref.edit();
	editor.putString(key, Integer.toString(value));
	editor.commit();
}

public void put(String key, boolean value)
{

	SharedPreferences.Editor editor = pref.edit();
	editor.putString(key, (value ? "true" : "false"));
	editor.commit();
}

public void put(String key, String value)
{

	SharedPreferences.Editor editor = pref.edit();
	editor.putString(key, value);
	editor.commit();
}

}
