package com.n0ano.athome;

import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;

import java.util.HashMap;

// Created by n0ano on 10/10/16.
//
// Class to handle weather data
//
public class Outlets
{

private final static int PERIOD = (10 * 1000);   // check outlets every 10 seconds

MainActivity act;
DoitCallback cb_show;

boolean running = true;
boolean paused = false;

X10 x10;
Tplink tplink;

OutletsDevice outlets_power = null;

OutletsAdapter outlets_adapter;

HashMap<String, String> hold = new HashMap<String, String>();

// Outlets: class constructor
//
//   act - activity that instantiated the class
//
public Outlets(final MainActivity act, View v, final DoitCallback cb)
{

	this.act = act;
    this.cb_show = cb;

    x10 = new X10(act);

    tplink = new Tplink(act);

    outlets_adapter = new OutletsAdapter(act);

    //
    // Enumerate the outlets
    //
    enumerate(outlets_adapter, v, new DoitCallback() {
        @Override
        public void doit(int res, Object obj) {
            //
            //  Thread to get data from the outlets
            //
            new Thread(new Runnable() {
                public void run() {
                    load_holds();
                    while (running) {
                        //
                        //  Get the data
                        //
                        battery();

                        boolean x10_change = x10.get_data(outlets_adapter);
                        boolean tp_change = tplink.get_data(outlets_adapter);
                        cb.doit(0, x10_change || tp_change);

                        SystemClock.sleep(PERIOD);
                    }
                }
            }).start();
        }
    });
}

private void set_hold(String name)
{

    OutletsDevice dev = outlets_adapter.findItem(name);
    if (dev != null)
        dev.set_hold(true);
}

private void load_holds()
{

    String hold = P.get_string("outlets:hold");
    if (hold.isEmpty())
        return;

    String[] holds = hold.split(";");
    int max = holds.length;
    for (int i = 0; i < max; i++) {
Log.d("DDD", "load_hold - " + holds[i]);
        set_hold(holds[i]);
    }
}

private void save_holds()
{
    OutletsDevice outlet;

    String save = "";
    String pre = "";
    int max = outlets_adapter.getCount();
    for (int i = 0; i < max; ++i) {
        outlet = outlets_adapter.getItem(i);
        if (outlet.get_hold()) {
            save += pre + outlet.get_name();
            pre = ";";
        }
    }
    P.put("outlets:hold", save);
}

public void stop()
{

    running = false;

    save_holds();
}
public void pause(boolean p) { paused = p; }

public void enumerate(final OutletsAdapter adapter, final View main, final DoitCallback cb)
{

    new Thread(new Runnable() {
        public void run() {
            adapter.clear();

            x10.get_devices(adapter, new DoitCallback() {
                @Override
                public void doit(int res, Object obj) {

                    tplink.get_devices(adapter, new DoitCallback() {
                        @Override
                        public void doit(int res, Object obj) {

                            act.runOnUiThread(new Runnable() {
                                public void run() {
                                    init_view(main);
                                    cb.doit(0, null);
                                }
                            });
                        }
                    });
                }
            });
        }
    }).start();
}

public void init_view(View main)
{
    int i;

    set_power(P.get_string("outlets:battery"));
    TableLayout tl = (TableLayout) main.findViewById(R.id.outlets_table);
    if (tl == null)
        return;
    tl.removeAllViews();
    TableRow tr = null;
    TableLayout.LayoutParams params = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
    params.setMargins(0, 40, 0, 0); /* left, top, right, bottom */
    int row = P.get_int("outlets:cols");
    int max_devices = outlets_adapter.getCount();
    for (i = 0; i < max_devices; i++) {
        OutletsDevice dev = outlets_adapter.getItem(i);
        if (++row > P.get_int("outlets:cols")) {
            row = 1;
            if (tr != null)
                tl.addView(tr, params);
            tr = new TableRow(act);
        }
        View v = dev.get_view();
        v.setTag(dev);
        v.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                OutletsDevice dev = (OutletsDevice) v.getTag();
                go_control(dev, -1);
            }
        });
        v.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View v) {
                OutletsDevice dev = (OutletsDevice) v.getTag();
                Popup popup = act.popup;
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

private void go_control(OutletsDevice dev, int toggle)
{
    boolean new_onoff;

    if (dev.get_hold() || !dev.get_online())
        return;

    if (toggle < 0)
        new_onoff = (dev.get_onoff() ? false : true);
    else
        new_onoff = (toggle == 0 ? false : true);

    switch (dev.get_type()) {

    case OutletsDevice.TYPE_X10:
        x10.control(dev, new_onoff);
        break;

    case OutletsDevice.TYPE_TPLINK:
        tplink.control(dev, new_onoff);
        break;

    default:
        Log.d(dev.get_name() + ": invalid type - " + dev.get_type());
        break;

    }
    cb_show.doit(0, null);
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

    if ((outlets_power == null) || outlets_power.get_hold() || !outlets_power.get_online())
        return;

    boolean onoff = outlets_power.get_onoff();
    int chg = act.get_battery();
    if (P.get_int("debug") > 0)
            Log.d("battery: " + outlets_power.get_name() +
                            (onoff ? "(on)" : "(off)") + " => " +
                            P.get_int("outlets:batt_min") + " < " +
                            chg + " > " +
                            P.get_int("outlets:batt_max"));
    if ((chg < P.get_int("outlets:batt_min")) && !onoff)
        go_control(outlets_power, 1);
    else if ((chg > P.get_int("outlets:batt_max")) && onoff)
        go_control(outlets_power, 0);
}

public void show(View v)
{

    int max = outlets_adapter.getCount();
    for (int i = 0; i < max; i++)
        outlets_adapter.getItem(i).show();
}

}
