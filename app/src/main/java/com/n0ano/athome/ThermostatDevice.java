package com.n0ano.athome;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.n0ano.athome.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

// Created by n0ano on 10/10/16.
//
// Class to handle weather data
//
public class ThermostatDevice {

private String name;
private int index;
private String code;
private String url;
private String temp;
private String humid;
private String mode;
private int h_min;
private int h_max;
private int c_min;
private int c_max;
private int c_delta;
private int hold;
private View view;

public ThermostatDevice(String name, int id, String code, View view)
{

Log.d("Thermostat(" + id + ")" + name + " = " + code);
    this.name = name;
    this.index = id;
    this.code = code;
    this.url = null;
    this.view = view;

    TextView tv = (TextView) view.findViewById(R.id.thermostat_name);
    tv.setText(name);
    this.hold = Thermostat.HOLD_RUNNING;
}

public String get_name() { return name; }

public int get_index() { return index; }

public void set_index(int index) { this.index = index; }

public String get_code() { return code; }

public void set_range(String mode, int h_min, int h_max, int c_min, int c_max, int c_delta)
{

    this.mode = mode;
    this.h_min = h_min;
    this.h_max = h_max;
    this.c_min = c_min;
    this.c_max = c_max;
    this.c_delta = c_delta;
}

public int get_h_min() { return h_min; }

public int get_h_max() { return h_max; }

public int get_c_min() { return c_min; }

public int get_c_max() { return c_max; }

public int get_c_delta() { return c_delta; }

public View get_view() { return view; }

public void set_view(View view) { this.view = view; }

public String get_temp() { return temp; }

public void set_temp(final String temp) { this.temp = temp; }

public String get_humid() { return humid; }

public void set_humid(final String humid) { this.humid = humid; }

public String get_mode() { return mode; }

public int get_hold() { return hold; }

public void set_hold(int hold) { this.hold = hold; }

}
