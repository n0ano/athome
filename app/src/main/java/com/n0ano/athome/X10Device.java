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
public class X10Device {

private String name;
private String code;
private View view;

private ImageView icon;
private TextView tv_name;

private int state;

public X10Device(String name, String code, View view)
{

    this.name = name;
    this.code = code;
    this.view = view;
    icon = view.findViewById(R.id.outlet);
    tv_name = view.findViewById(R.id.outlet_name);
    tv_name.setText(name);
    state = 0;
}

public String get_name() {

    return name;
}

public void set_name(String name) {

    this.name = name;
}

public String get_code() {

    return code;
}

public void set_code(String code) {

    this.code = code;
}

public View get_view() {

    return view;
}

public void set_view(View view) {

    this.view = view;
}

public int get_state() {

    return state;
}

public void set_state(int state)
{

    this.state = state;
    icon.setImageResource((state == 0) ? R.drawable.outlet_off : R.drawable.outlet_on);
Log.d(name + " set state to " + state);
}

}
