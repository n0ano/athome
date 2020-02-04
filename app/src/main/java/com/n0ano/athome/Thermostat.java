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
Ecobee ecobee;

boolean running = true;
boolean paused = false;

ThermostatAdapter thermostat_adapter;

// Thermostat: class constructor
//
//   act - activity that instantiated the class
//
public Thermostat(final MainActivity act, View v, final DoitCallback cb)
{

	this.act = act;

    this.ecobee = new Ecobee(act);

    thermostat_adapter = new ThermostatAdapter(act);

    //
    //  Enumerate thermostats
    //
    enumerate(v, new DoitCallback() {
        @Override
        public void doit(Object obj) {
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
                            cb.doit(null);
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
                    cb.doit(null);
                }
            });
        }
    }).start();
}

private int hold_type(int t)
{

    switch (t) {

    case R.id.hold_temporary:
        return Thermostat.HOLD_TEMPORARY;

    case R.id.hold_permanent:
        return Thermostat.HOLD_PERMANENT;

    }
    return Thermostat.HOLD_TEMPORARY;
}

private void hold_dialog(final ThermostatDevice dev)
{

    final Dialog dialog = new Dialog(act, R.style.AlertDialogCustom);
    dialog.setContentView(R.layout.hold);

    final RadioGroup rg = (RadioGroup) dialog.findViewById(R.id.hold_type);

    TextView tv = (TextView) dialog.findViewById(R.id.hold_status);
    switch (dev.get_hold()) {

    case EcobeeData.HOLD_RUNNING:
        tv.setText("Running");
        break;

    case EcobeeData.HOLD_TEMPORARY:
        tv.setText("Hold (temporary)");
        rg.check(R.id.hold_temporary);
        break;

    case EcobeeData.HOLD_PERMANENT:
        tv.setText("Hold (permanent)");
        rg.check(R.id.hold_permanent);
        break;

    }

    final NumberPicker np = (NumberPicker) dialog.findViewById(R.id.hold_temp);
    np.setMinValue(dev.get_h_min()/10);
    np.setMaxValue(dev.get_h_max()/10);
    int temp = (int)dev.get_temp();
    np.setValue(temp);
    np.setWrapSelectorWheel(false);

    Button cancel = (Button) dialog.findViewById(R.id.cancel);
    cancel.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            dialog.dismiss();
        }
    });

    Button resume = (Button) dialog.findViewById(R.id.resume);
    resume.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            dialog.dismiss();
            new Thread(new Runnable() {
                public void run() {
                    dev.set_hold(Thermostat.HOLD_RUNNING);
                    ecobee.ecobee_resume(dev);
                }
            }).start();
        }
    });

    Button hold = (Button) dialog.findViewById(R.id.hold);
    hold.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final String t = np.getValue() + "0";
            dialog.dismiss();
            new Thread(new Runnable() {
                public void run() {
                    dev.set_hold(hold_type(rg.getCheckedRadioButtonId()));
                    ecobee.ecobee_hold(C.a2i(t), dev);
                }
            }).start();
        }
    });

    dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

    dialog.show();
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
                hold_dialog((ThermostatDevice)v.getTag());
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

    hold_dialog((ThermostatDevice) v.getTag());
}

public void show(View v)
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
//      tv.setTextColor((dev.get_hold() == Thermostat.HOLD_RUNNING) ?
//                                            act.getResources().getColor(R.color.fore) :
//                                            act.getResources().getColor(R.color.hold));
    }
}

}
