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
private boolean hold;

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
    hold = false;
}

public String get_name()
{

    return name;
}

public void set_name(String name)
{

    this.name = name;
}

public String get_code()
{

    return code;
}

public void set_code(String code)
{

    this.code = code;
}

public View get_view()
{

    return view;
}

public void set_view(View view)
{

    this.view = view;
}

public boolean get_hold()
{

    return this.hold;
}

public void set_hold(boolean hold)
{

    this.hold = hold;
}

public int get_state()
{

    return this.state;
}

public void set_state(final int state, MainActivity act)
{
    final int draw;

    this.state = state;
Log.d(name + " set state to " + ((state == 0) ? "off, " : "on, ") + (hold ? "holding" : "normal"));
    if (hold)
        draw = (state == 0) ? R.drawable.outlet_off_red : R.drawable.outlet_on_red;
    else
        draw = (state == 0) ? R.drawable.outlet_off : R.drawable.outlet_on;
    act.runOnUiThread(new Runnable() {
        public void run() {
            icon.setImageResource(draw);
        }
    });
}

}
