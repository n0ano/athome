package com.n0ano.athome;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.n0ano.athome.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

// Created by n0ano on 10/10/16.
//
// Class to handle weather data
//
public class Outlets {

private final static int PERIOD = 10;   // check outlets every 10 seconds

private int period = PERIOD;        // Weather only changes once a minute

MainActivity act;
X10 x10;
Tplink tplink;

OutletsDevice outlets_power = null;

OutletsAdapter outlets_adapter;

// Outlets: class constructor
//
//   act - activity that instantiated the class
//
public Outlets(final MainActivity act)
{

	this.act = act;

    this.x10 = new X10(act);

    this.tplink = new Tplink(act);

    outlets_adapter = new OutletsAdapter(act);
    startup();
}

public void startup()
{

    new Thread(new Runnable() {
        public void run() {
            outlets_adapter.clear();

            x10.get_devices(outlets_adapter);

            tplink.get_devices(outlets_adapter);
        }
    }).start();
}

public void init_view()
{
    int i;

    set_power(act.outlets_battery);
    TableLayout tl = (TableLayout) act.findViewById(R.id.outlets_table);
    tl.removeAllViews();
    TableRow tr = null;
    TableLayout.LayoutParams params = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
    params.setMargins(0, 40, 0, 0); /* left, top, right, bottom */
    int row = 3;
    int max_devices = outlets_adapter.getCount();
    for (i = 0; i < max_devices; i++) {
        OutletsDevice dev = outlets_adapter.getItem(i);
        if (++row > 3) {
            row = 1;
            if (tr != null)
                tl.addView(tr, params);
            tr = new TableRow(act);
        }
        View v = dev.get_view();
        v.setTag(dev);
        v.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                go_control(v);
            }
        });
        v.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View v) {
                OutletsDevice dev = (OutletsDevice) v.getTag();
Log.d("long press on " + dev.get_name());
                //dev.set_hold(!dev.get_hold());
                //dev.set_state(dev.get_state(), act);
                Popup popup = new Popup(act);
                popup.device_dialog(dev);
                return true;
            }
        });
        ViewGroup parent = (ViewGroup)v.getParent();
        if (parent != null)
            parent.removeView(v);
        tr.addView(v);
    }
    if (tr != null)
        tl.addView(tr, params);

}

private void go_control(View v)
{

    OutletsDevice dev = (OutletsDevice) v.getTag();
    if (dev.get_hold())
        return;
    switch (dev.get_type()) {

    case OutletsDevice.TYPE_X10:
        x10.control(dev, dev.get_state() ? false : true);
        break;

    case OutletsDevice.TYPE_TPLINK:
        tplink.control(dev, dev.get_state() ? false : true);
        break;

    default:
        Log.d(dev.get_name() + ": invalid type - " + dev.get_type());
        break;

    }
}

public void set_power(String name)
{
    OutletsDevice dev;

    outlets_power = null;
    if (name.isEmpty())
        return;

    int max = outlets_adapter.getCount();
    for (int i = 0; i < max; i++) {
        dev = outlets_adapter.getItem(i);
        if (dev.get_name().equals(name)) {
            outlets_power = dev;
            return;
        }
    }
}

private void battery()
{

    if (outlets_power == null || outlets_power.get_hold())
        return;

    boolean state = outlets_power.get_state();
    int chg = act.get_battery();
    if (chg < act.outlets_batt_min && !state)
        x10.control(outlets_power, true);
    else if (chg > act.outlets_batt_max && state)
        x10.control(outlets_power, false);
}

public void update()
{

    //
    //  Get the data
    //
    if (period++ >= PERIOD) {
        x10.update(outlets_adapter);

        tplink.update(outlets_adapter);

        battery();

        period = 1;
    }
}

}