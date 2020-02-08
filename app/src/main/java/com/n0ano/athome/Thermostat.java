package com.n0ano.athome;

import android.app.Dialog;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

// Created by n0ano on 10/10/16.
//
// Class to handle weather data
//
public class Thermostat
{

public final static int HOLD_RUNNING   = 0;
public final static int HOLD_TEMPORARY = 1;
public final static int HOLD_PERMANENT = 2;

private final static int PERIOD = (10 * 1000);   // check outlets every 10 seconds

MainActivity act;
Popup popup;

Ecobee ecobee;

boolean running = true;
boolean paused = false;

ThermostatAdapter thermostat_adapter;

// Thermostat: class constructor
//
//   act - activity that instantiated the class
//
public Thermostat(final MainActivity act, View v, Popup popup, final DoitCallback cb)
{

	this.act = act;
    this.popup = popup;

    this.ecobee = new Ecobee(act);

    thermostat_adapter = new ThermostatAdapter(act);

    //
    //  Enumerate thermostats
    //
    enumerate(v, new DoitCallback() {
        @Override
        public void doit(int res, Object obj) {
            //
            //  Thread to get data from the thermostats
            //
            new Thread(new Runnable() {
                public void run() {
                    while (running) {
                        //
                        //  Get the data
                        //
                        if (!paused) {
                            ecobee.get_data(thermostat_adapter);
                            cb.doit(0, null);
                        }

                        SystemClock.sleep(PERIOD);
                    }
                }
            }).start();
        }
    });
}

public void stop() { running = false; }
public void pause(boolean p) { paused = p; }

public void enumerate(final View v, final DoitCallback cb)
{

    new Thread(new Runnable() {
        public void run() {
            thermostat_adapter.clear();
            ecobee.get_devices(thermostat_adapter);

            act.runOnUiThread(new Runnable() {
                public void run() {
                    init_view(v);
                    cb.doit(0, null);
                }
            });
        }
    }).start();
}

public void init_view(View main)
{
    int i;

    TableLayout tl = (TableLayout) main.findViewById(R.id.thermostats_table);
    if (tl == null)
        return;
    tl.removeAllViews();
    TableLayout.LayoutParams params = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
    params.setMargins(0, 40, 0, 0); /* left, top, right, bottom */

    TableRow tr = null;
    int max_devices = thermostat_adapter.getCount();
    int rows = ((P.get_int("general:layout") == Popup.LAYOUT_PHONE) ? 1 : 2);
    int row =  rows;
    for (i = 0; i < max_devices; i++) {
        ThermostatDevice dev = thermostat_adapter.getItem(i);
        if (++row > rows) {
            row = 1;
            if (tr != null)
                tl.addView(tr, params);
            tr = new TableRow(act);
        }
        View v = dev.get_view();
        v.setTag(dev);
        ImageView iv = (ImageView) v.findViewById(R.id.thermostat_icon);
        iv.setTag(dev);
        iv.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                popup.hold_dialog((ThermostatDevice)v.getTag(), ecobee);
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

public void go_hold(View v)
{

    popup.hold_dialog((ThermostatDevice) v.getTag(), ecobee);
}

public void show(View v, int color_fore, int color_hold)
{

    //
    //  Show the data
    //
    int max_devices = thermostat_adapter.getCount();
    for (int i = 0; i < max_devices; i++) {
        ThermostatDevice dev = thermostat_adapter.getItem(i);
        View dv = dev.get_view();
        GaugeView gv = (GaugeView) dv.findViewById(R.id.thermostat_temp);
        gv.set_value(dev.get_temp());
//      tv.setTextColor((dev.get_hold() == Thermostat.HOLD_RUNNING) ? color_fore : color_hole);
    }
}

}
