package com.n0ano.athome;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.n0ano.athome.Log;

public class Popup extends MainActivity
{

public final static String ECOBEE_REAUTH = "https://www.ecobee.com";
public final static String ATHOME_MANUAL = "https://github.com/n0ano/athome/wiki/User-Manual";

public final static int EGAUGE_NONE =       0;
public final static int EGAUGE_TABLET =     1;
public final static int EGAUGE_PHONE =      2;
public final static int radio_egauge[] = {
    R.id.egauge_none,
    R.id.egauge_tablet,
    R.id.egauge_phone
};
public final static int layout_egauge[] = {
    R.layout.egauge,
    R.layout.egauge_tab,
    R.layout.egauge_ph
};

public final static int WEATHER_NONE =      0;
public final static int WEATHER_TABLET =    1;
public final static int WEATHER_PHONE =     2;
public final static int radio_weather[] = {
    R.id.weather_none,
    R.id.weather_tablet,
    R.id.weather_phone
};
public final static int layout_weather[] = {
    R.layout.weather,
    R.layout.weather_tab,
    R.layout.weather_ph
};

MainActivity act;

private int ecobee_swhich;

private int batt_pos;

Preferences pref;

public Popup(MainActivity act)
{

    this.act = act;
    pref = new Preferences(act);
}

private int index_id(int index, int[] ids)
{
    int i, max;

    max = ids.length;
    for (i = 0; i < max; i++)
        if (ids[i] == index)
            return i;
    return 0;
}

public boolean menu_click(int item)
{

    switch (item) {

    case R.id.action_egauge:
        egauge_dialog();
        return true;

    case R.id.action_weather:
        weather_dialog();
        return true;

    case R.id.action_outlets:
        outlets_dialog();
        return true;

    case R.id.action_developer:
        developer_dialog();
        return true;

    case R.id.action_about:
        about_dialog();
        return true;
    }
    return false;
}

private Dialog start_dialog(int id)
{

    final Dialog dialog = new Dialog(act, R.style.AlertDialogCustom);
    dialog.setContentView(id);

    Button cancel = (Button) dialog.findViewById(R.id.cancel);
    if (cancel != null) {
        cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

    dialog.show();

    return dialog;
}

public void device_dialog(final OutletsDevice dev)
{

    final Dialog dialog = start_dialog(R.layout.bar_device);

    TextView tv = (TextView) dialog.findViewById(R.id.device_name);
    tv.setText(dev.get_name());

    tv = (TextView) dialog.findViewById(R.id.device_type);
    tv.setText(dev.get_tname());

    tv = (TextView) dialog.findViewById(R.id.device_code);
    tv.setText(dev.get_dev_code());

    final CheckBox cb = (CheckBox) dialog.findViewById(R.id.device_hold);
    cb.setChecked(dev.get_hold());

    Button ok = (Button) dialog.findViewById(R.id.ok);
    ok.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            dev.set_hold(cb.isChecked());
            dev.set_state(dev.get_state(), act);
            dialog.dismiss();
        }
    });
}

private void egauge_dialog()
{

    final Dialog dialog = start_dialog(R.layout.bar_egauge);

    final RadioGroup rg = (RadioGroup) dialog.findViewById(R.id.egauge_layout);
    rg.check(radio_egauge[act.egauge_layout]);

    final EditText et = (EditText) dialog.findViewById(R.id.egauge_url);
    et.setText(act.egauge_url);

    Button ok = (Button) dialog.findViewById(R.id.ok);
    ok.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            act.egauge_layout = index_id(rg.getCheckedRadioButtonId(), radio_egauge);
            pref.put_int("egauge_layout", act.egauge_layout);
            act.egauge_url = et.getText().toString();
            pref.put_string("egauge_url", act.egauge_url);
            act.view_show(act.egauge_layout, Popup.layout_egauge, R.id.egauge_main);
            dialog.dismiss();
        }
    });
}

private void wunder_dialog()
{

    final Dialog dialog = start_dialog(R.layout.bar_wunder);

    final EditText et = (EditText) dialog.findViewById(R.id.wunder_id);
    et.setText(act.wunder_id);

    Button ok = (Button) dialog.findViewById(R.id.ok);
    ok.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            act.wunder_id = et.getText().toString();
            pref.put_string("wunder_id", act.wunder_id);
            dialog.dismiss();
        }
    });
}

private void ecobee_how()
{

    final Dialog dialog = start_dialog(R.layout.bar_ecobee_how);

    TextView tv = (TextView) dialog.findViewById(R.id.ecobee_ra_text);
    tv.setText(Html.fromHtml(act.getString(R.string.ecobee_auth)));

    Button ok = (Button) dialog.findViewById(R.id.ok);
    ok.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            act.start_browser(ECOBEE_REAUTH);
            dialog.dismiss();
        }
    });
}

private void ecobee_dopin(final String api)
{


    new Thread(new Runnable() {
        public void run() {
            final String pin = act.weather.ecobee_get_pin(api);
            runOnUiThread(new Runnable() {
                public void run() {
                    ecobee_doauth(api, pin);
                }
            });
        }
    }).start();
}

private void ecobee_doauth(final String api, final String pin)
{

    final Dialog dialog = start_dialog(R.layout.bar_ecobee_auth);

    final TextView api_tv = (TextView) dialog.findViewById(R.id.ecobee_api);
    api_tv.setText(api);

    final TextView pin_tv = (TextView) dialog.findViewById(R.id.ecobee_pin);
    pin_tv.setText(pin);

    Button ok = (Button) dialog.findViewById(R.id.ok);
    ok.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            dialog.dismiss();
            act.ecobee_api = api;
            pref.put_string("ecobee_api", act.ecobee_api);
            new Thread(new Runnable() {
                public void run() {
                    act.weather.ecobee_authorize(act.ecobee_api);
                }
            }).start();
        }
    });
}

private void ecobee_dialog()
{
    int nt;

    final Dialog dialog = start_dialog(R.layout.bar_ecobee);

    final EditText api_tv = (EditText) dialog.findViewById(R.id.ecobee_api);
    api_tv.setText(act.ecobee_api);

    final EditText access_tv = (EditText) dialog.findViewById(R.id.ecobee_access);
    access_tv.setText(act.ecobee_access);

    final EditText refresh_tv = (EditText) dialog.findViewById(R.id.ecobee_refresh);
    refresh_tv.setText(act.ecobee_refresh);

    nt = act.ecobee_data.size();
    String[] thermos = new String[nt];
    for (int i = 0; i < nt; ++i)
        thermos[i] = act.ecobee_data.get(i).get_name();
    final Spinner sv = (Spinner) dialog.findViewById(R.id.ecobee_which);
    ArrayAdapter<String> adapter = new ArrayAdapter<String>(act, R.layout.text_spinner, thermos);
    adapter.setDropDownViewResource(R.layout.text_spinner);
    sv.setAdapter(adapter);
    if (nt <= 0)
        act.ecobee_which = -1;
    ecobee_swhich = act.ecobee_which;
    if (act.ecobee_which >= 0)
        sv.setSelection(ecobee_swhich = act.ecobee_which);
    sv.setOnItemSelectedListener(new OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            ecobee_swhich = position;
            //Log.d("which item - " + (String) parent.getItemAtPosition(position));
        }
        
        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            // TODO Auto-generated method stub
        }

    });

    Button bv_how = (Button) dialog.findViewById(R.id.ecobee_how);
    bv_how.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            ecobee_how();
        }
    });

    Button bv_auth = (Button) dialog.findViewById(R.id.ecobee_dopin);
    bv_auth.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            String api = api_tv.getText().toString();
            dialog.dismiss();
            ecobee_dopin(api);
        }
    });

    Button ok = (Button) dialog.findViewById(R.id.ok);
    ok.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            act.ecobee_api = api_tv.getText().toString();
            act.ecobee_access = access_tv.getText().toString();
            act.ecobee_refresh = refresh_tv.getText().toString();
            act.ecobee_which = ecobee_swhich;
            if (act.ecobee_which >= 0)
                act.ecobee_name = act.ecobee_data.get(act.ecobee_which).get_name();
            pref.put_string("ecobee_api", act.ecobee_api);
            pref.put_string("ecobee_access", act.ecobee_access);
            pref.put_string("ecobee_refresh", act.ecobee_refresh);
            pref.put_int("ecobee_which", act.ecobee_which);
            pref.put_string("ecobee_name", act.ecobee_name);
            dialog.dismiss();
        }
    });
}

private void weather_dialog()
{
    int i;
    String name, pname;

    final Dialog dialog = start_dialog(R.layout.bar_weather);

    final RadioGroup rg = (RadioGroup) dialog.findViewById(R.id.weather_layout);
    rg.check(radio_weather[act.weather_layout]);

    Button bv_how = (Button) dialog.findViewById(R.id.weather_ecobee);
    bv_how.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            ecobee_dialog();
        }
    });

    Button bv_auth = (Button) dialog.findViewById(R.id.weather_under);
    bv_auth.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            wunder_dialog();
        }
    });

    Button ok = (Button) dialog.findViewById(R.id.ok);
    ok.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            act.weather_layout = index_id(rg.getCheckedRadioButtonId(), radio_weather);
            pref.put_int("weather_layout", act.weather_layout);
            act.view_show(act.weather_layout, Popup.layout_weather, R.id.weather_main);
            dialog.dismiss();
        }
    });
}

private void outlets_dialog()
{
    int i;
    String name, pname;

    final Dialog dialog = start_dialog(R.layout.bar_outlets);

    int max_dev = act.outlets.outlets_adapter.getCount();
    String[] names = new String[max_dev + 1];
    names[0] = "- none -";
    batt_pos = 0;
    pname = act.outlets_battery;
    for (i = 0; i < max_dev; i++) {
        name = act.outlets.outlets_adapter.getItem(i).get_name();
        names[i + 1] = name;
        if (name.equals(pname))
            batt_pos = i + 1;
    }
    final Spinner sv = (Spinner) dialog.findViewById(R.id.outlets_battery);
    ArrayAdapter<String> adapter = new ArrayAdapter<String>(act, R.layout.text_spinner, names);
    adapter.setDropDownViewResource(R.layout.text_spinner);
    sv.setAdapter(adapter);
    sv.setSelection(batt_pos);
    sv.setOnItemSelectedListener(new OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            batt_pos = position;
        }
        
        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            // TODO Auto-generated method stub
        }

    });

    final TextView min = (TextView) dialog.findViewById(R.id.outlets_batt_min);
    min.setText(Integer.toString(act.outlets_batt_min));

    final TextView max = (TextView) dialog.findViewById(R.id.outlets_batt_max);
    max.setText(Integer.toString(act.outlets_batt_max));

    Button bv_how = (Button) dialog.findViewById(R.id.outlets_x10);
    bv_how.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            x10_dialog();
        }
    });

    Button bv_auth = (Button) dialog.findViewById(R.id.outlets_tplink);
    bv_auth.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            tplink_dialog();
        }
    });

    Button ok = (Button) dialog.findViewById(R.id.ok);
    ok.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (batt_pos <= 0)
                act.outlets_battery = "";
            else
                act.outlets_battery = act.outlets.outlets_adapter.getItem(batt_pos - 1).get_name();
            act.outlets.set_power(outlets_battery);
            pref.put_string("outlets_battery", act.outlets_battery);
            act.outlets_batt_min = Integer.parseInt(min.getText().toString());
            pref.put_int("outlets_batt_min", act.outlets_batt_min);
            act.outlets_batt_max = Integer.parseInt(max.getText().toString());
            pref.put_int("outlets_batt_max", act.outlets_batt_max);
            dialog.dismiss();
        }
    });
}

private void x10_dialog()
{
    int i;

    final Dialog dialog = start_dialog(R.layout.bar_x10);

    final EditText et = (EditText) dialog.findViewById(R.id.x10_url);
    et.setText(act.x10_url);

    final EditText jt = (EditText) dialog.findViewById(R.id.x10_jwt);
    jt.setText(act.x10_jwt);

    Button ok = (Button) dialog.findViewById(R.id.ok);
    ok.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            act.x10_url = et.getText().toString();
            pref.put_string("x10_url", act.x10_url);
            act.x10_jwt = jt.getText().toString();
            pref.put_string("x10_jwt", act.x10_jwt);
            dialog.dismiss();
            act.outlets.startup();
        }
    });
}

private void tplink_dialog()
{
    int i;

    final Dialog dialog = start_dialog(R.layout.bar_tplink);

    final EditText ut = (EditText) dialog.findViewById(R.id.tplink_user);
    ut.setText(act.tplink_user);

    final EditText pt = (EditText) dialog.findViewById(R.id.tplink_pwd);
    pt.setText(act.tplink_pwd);

    Button ok = (Button) dialog.findViewById(R.id.ok);
    ok.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            act.tplink_user = ut.getText().toString();
            pref.put_string("tplink_user", act.tplink_user);
            act.tplink_pwd = pt.getText().toString();
            pref.put_string("tplink_pwd", act.tplink_pwd);
            dialog.dismiss();
            act.outlets.startup();
        }
    });
}

private void developer_dialog()
{

    final Dialog dialog = start_dialog(R.layout.bar_developer);

    final CheckBox cb = (CheckBox) dialog.findViewById(R.id.about_debug);
    cb.setChecked(act.debug != 0);

    final TextView level = (TextView) dialog.findViewById(R.id.outlets_batt_level);
    level.setText(Integer.toString(act.outlets_batt_level));

    Button ok = (Button) dialog.findViewById(R.id.ok);
    ok.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            act.debug = (cb.isChecked() ? 1 : 0);
            pref.put_int("debug", act.debug);
            act.outlets_batt_level = Integer.parseInt(level.getText().toString());
            dialog.dismiss();
        }
    });
}

private void about_dialog()
{

    final Dialog dialog = start_dialog(R.layout.bar_about);

    Button bt = (Button) dialog.findViewById(R.id.about_manual);
    bt.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            act.start_browser(ATHOME_MANUAL);
            dialog.dismiss();
        }
    });

    Button ok = (Button) dialog.findViewById(R.id.ok);
    ok.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            dialog.dismiss();
        }
    });
}

private void ecobee_get(Dialog dialog, String api)
{

Log.d("ecobee_get() api - " + api);
    return;
}

}
