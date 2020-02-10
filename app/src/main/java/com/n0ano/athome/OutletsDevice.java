package com.n0ano.athome;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


// Created by n0ano on 10/10/16.
//
// Class to handle weather data
//
public class OutletsDevice {

public final static int TYPE_X10 =      0;
public final static int TYPE_TPLINK =   1;

private String name;
private int type;
private int index;
private String code;
private String url;
private boolean onoff;
private boolean hold;
private boolean online;
private View view;

private ImageView icon;
private TextView tv_name;

public OutletsDevice(String name, int id, String code, View view)
{

Log.d("outlet " + name + " = " + code + id);
    this.name = name;
    this.type = TYPE_X10;
    this.index = id;
    this.code = code;
    this.url = null;
    this.view = view;
    icon = view.findViewById(R.id.outlet);
    tv_name = view.findViewById(R.id.outlet_name);
    tv_name.setText(name);

    this.onoff = false;
    this.hold = false;
    this.online = true;
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

    this.onoff = false;
    this.hold = false;
    this.online = true;
}

public String get_name() { return name; }

public int get_type() { return this.type; }

public int get_index() { return index; }
public void set_index(int index) { this.index = index; }

public String get_code() { return code; }
public String get_url() { return this.url; }

public View get_view() { return view; }
public void set_view(View view) { this.view = view; }

public boolean get_onoff() { return onoff; }
public void set_onoff(boolean s) { onoff = s; }

public boolean get_hold() { return hold; }
public void set_hold(boolean h) { hold = h; }

public boolean get_online() { return online; }
public void set_online(boolean o) { online = o; }

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

public void show()
{
    final int draw;

    if (!get_online())
        draw = R.drawable.outlet_offline;
    else if (get_hold())
        draw = onoff ? R.drawable.outlet_on_blue : R.drawable.outlet_off_blue;
    else
        draw = onoff ? R.drawable.outlet_on_green : R.drawable.outlet_off_red;
    icon.setImageResource(draw);
}

}
