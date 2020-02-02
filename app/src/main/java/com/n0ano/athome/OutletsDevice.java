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

public final static int HOLD =          0;
public final static int ONLINE =        1;
public final static int OFFLINE =       2;

private String name;
private int type;
private int index;
private String code;
private String url;
private boolean onoff;
private int state;
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
    this.state = OFFLINE;
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
    this.state = OFFLINE;
}

public String get_name() { return name; }

public int get_type() { return this.type; }

public int get_index() { return index; }

public void set_index(int index) { this.index = index; }

public String get_code() { return code; }

public String get_url() { return this.url; }

public View get_view() { return view; }

public void set_view(View view) { this.view = view; }

public int get_state() { return state; }
public void set_state(int s, boolean force)
{
    if (force || (state != HOLD))
        state = s;
}

public boolean get_onoff() { return this.onoff; }
public void set_onoff(boolean s) { onoff = s; }

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

    switch (state) {

    case HOLD:
        draw = onoff ? R.drawable.outlet_on_blue : R.drawable.outlet_off_blue;
        break;

    case ONLINE:
        draw = onoff ? R.drawable.outlet_on_green : R.drawable.outlet_off_red;
        break;

    default:
    case OFFLINE:
        draw = R.drawable.outlet_offline;
        break;

    }
    icon.setImageResource(draw);
}

}
