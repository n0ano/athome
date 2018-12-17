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
public class OutletsDevice {

final static int TYPE_X10 =     0;
final static int TYPE_TPLINK =  1;

private String name;
private int type;
private int index;
private String code;
private String url;
private boolean state;
private boolean hold;
private View view;

private ImageView icon;
private TextView tv_name;

public OutletsDevice(String name, int id, String code, View view)
{

Log.d("device " + name + " = " + code + id);
    this.name = name;
    this.type = TYPE_X10;
    this.index = id;
    this.code = code;
    this.url = null;
    this.view = view;
    icon = view.findViewById(R.id.outlet);
    tv_name = view.findViewById(R.id.outlet_name);
    tv_name.setText(name);

    this.state = false;
    this.hold = false;
}

public OutletsDevice(String name, String code, String url, View view)
{

Log.d("device " + name + " = " + code);
    this.name = name;
    this.type = TYPE_TPLINK;
    this.index = 0;
    this.code = code;
    this.url = url;
    this.view = view;
    icon = view.findViewById(R.id.outlet);
    tv_name = view.findViewById(R.id.outlet_name);
    tv_name.setText(name);

    this.state = false;
    this.hold = false;
}

public String get_name()
{

    return name;
}

public int get_type()
{

    return this.type;
}

public int get_index()
{

    return index;
}

public void set_index(int index)
{

    this.index = index;
}

public String get_code()
{

    return code;
}

public String get_dev_code()
{

    switch (type) {

    case TYPE_X10:
        return code + index;

    case TYPE_TPLINK:
        return code;

    default:
        return "?";

    }
}

public String get_tname()
{

    switch (type) {

    case TYPE_X10:
        return "X10";

    case TYPE_TPLINK:
        return "TP-LINK";

    default:
        return "?";

    }
}

public String get_url()
{

    return this.url;
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

public boolean get_state()
{

    return this.state;
}

public void set_state(final boolean state, MainActivity act)
{
    final int draw;

    this.state = state;
//Log.d(name + "(" + code + index + ") set state to " + (state ? "on, " : "off, ") + (hold ? "holding" : "normal"));
    if (hold)
        draw = state ? R.drawable.outlet_on_red : R.drawable.outlet_off_red;
    else
        draw = state ? R.drawable.outlet_on : R.drawable.outlet_off;
    act.runOnUiThread(new Runnable() {
        public void run() {
            icon.setImageResource(draw);
        }
    });
}

}
