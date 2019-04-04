package com.n0ano.athome;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
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
public class Thermostat {

public final static int HOLD_RUNNING   = 0;
public final static int HOLD_TEMPORARY = 1;
public final static int HOLD_PERMANENT = 2;

private final static int PERIOD = 10;   // check outlets every 10 seconds

private int period = PERIOD;        // Weather only changes once a minute

MainActivity act;
Ecobee ecobee;

ThermostatAdapter thermostat_adapter;

// Thermostat: class constructor
//
//   act - activity that instantiated the class
//
public Thermostat(final MainActivity act)
{

	this.act = act;

    this.ecobee = new Ecobee(act);

    thermostat_adapter = new ThermostatAdapter(act);
    startup();
}

public void startup()
{

    new Thread(new Runnable() {
        public void run() {
            thermostat_adapter.clear();

            ecobee.get_devices(thermostat_adapter);
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
    String t = dev.get_temp();
    int temp = Common.a2i(t.substring(0, t.length() - 2));
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
                    ecobee.ecobee_hold(Common.a2i(t), dev);
                }
            }).start();
        }
    });

    dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

    dialog.show();
}

public void init_view()
{
    int i;

    TableLayout tl = (TableLayout) act.findViewById(R.id.thermostats_table);
    if (tl == null)
        return;
    tl.removeAllViews();
    TableLayout.LayoutParams params = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
    params.setMargins(0, 40, 0, 0); /* left, top, right, bottom */

    TableRow tr = null;
    int row = 2;
    int max_devices = thermostat_adapter.getCount();
    for (i = 0; i < max_devices; i++) {
        ThermostatDevice dev = thermostat_adapter.getItem(i);
        if (++row > 2) {
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

public void update()
{

    if (period++ >= PERIOD) {
        period = 1;

        //
        //  Get the data
        //
        ecobee.update(thermostat_adapter);

        //
        //  Show the data
        //
        act.runOnUiThread(new Runnable() {
            public void run() {
                int max_devices = thermostat_adapter.getCount();
                for (int i = 0; i < max_devices; i++) {
                    ThermostatDevice dev = thermostat_adapter.getItem(i);
                    View v = dev.get_view();
                    TextView tv = (TextView) v.findViewById(R.id.thermostat_temp);
                    tv.setText(dev.get_temp());
                    tv.setTextColor((dev.get_hold() == Thermostat.HOLD_RUNNING) ?
                                            act.getResources().getColor(R.color.fore) :
                                            act.getResources().getColor(R.color.hold));
                }
            }
        });
    }
}

}
