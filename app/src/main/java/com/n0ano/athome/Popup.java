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

public final static String ATHOME_MANUAL = "https://github.com/n0ano/athome/wiki/User-Manual";

public final static int LAYOUT_NONE =       0;
public final static int LAYOUT_TABLET =     1;
public final static int LAYOUT_PHONE =      2;

public final static int layout_egauge[] = {
    R.layout.egauge,                       
    R.layout.egauge_tab,
    R.layout.egauge_ph  
};

public final static int layout_weather[] = {
    R.layout.weather,                       
    R.layout.weather_tab,
    R.layout.weather_ph  
};

public final static int layout_thermostat[] = {
    R.layout.thermostat_hidden,                       
    R.layout.thermostats_table,
    R.layout.thermostats_table  
};

MainActivity act;

private int batt_pos;
private int type_pos;
public final static int SS_START = 30;
public final static int SS_DELAY = 30;

Preferences pref;

public Popup(MainActivity act, Preferences pref)
{

    this.act = act;
    this.pref = pref;
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

    case R.id.action_saver:
        act.ss_saver.saver_click();
        return true;

    case R.id.action_display:
        display_dialog();
        return true;

    case R.id.action_general:
        general_dialog();
        return true;

    case R.id.action_egauge:
        egauge_dialog();
        return true;

    case R.id.action_weather:
        weather_dialog();
        return true;

    case R.id.action_thermostat:
        thermostat_dialog();
        return true;

    case R.id.action_outlets:
        outlets_dialog();
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
    dialog.setCanceledOnTouchOutside(false);

    Button cancel = (Button) dialog.findViewById(R.id.cancel);
    if (cancel != null) {
        cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                end_dialog(dialog);
            }
        });
    }

    dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

    dialog.show();

    act.ss_saver.screen_saver(C.SAVER_BLOCK);

    return dialog;
}

public void end_dialog(Dialog dialog)
{

    act.ss_saver.screen_saver(C.SAVER_RESET);
    dialog.dismiss();
}

public void display_dialog()
{

    act.display_toggle(null);
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
            end_dialog(dialog);
        }
    });
}

public void log_detail_dialog(int i, String l)
{
    TextView tv;

    final Dialog dialog = start_dialog(R.layout.bar_log_detail);

    tv = (TextView) dialog.findViewById(R.id.log_lineno);
    tv.setText(":" + i);

    tv = (TextView) dialog.findViewById(R.id.log_detail);
    tv.setText(l);
}

private void general_dialog()
{
    int i;
    String name, pname;

    final Dialog dialog = start_dialog(R.layout.bar_general);

    final RadioGroup rg = (RadioGroup) dialog.findViewById(R.id.general_layout);
    rg.check((act.general_layout == LAYOUT_TABLET) ?
                    R.id.general_tablet :
                    R.id.general_phone);

    final CheckBox bt_egauge = (CheckBox) dialog.findViewById(R.id.general_egauge);
    bt_egauge.setChecked(act.egauge_layout != LAYOUT_NONE);

    final CheckBox bt_weather = (CheckBox) dialog.findViewById(R.id.general_weather);
    bt_weather.setChecked(act.weather_layout != LAYOUT_NONE);

    final CheckBox bt_thermostat = (CheckBox) dialog.findViewById(R.id.general_thermostat);
    bt_thermostat.setChecked(act.thermostat_layout != LAYOUT_NONE);

    final EditText et_on = (EditText) dialog.findViewById(R.id.general_on);
    et_on.setText(act.encode_time(act.on_time));

    final EditText et_off = (EditText) dialog.findViewById(R.id.general_off);
    et_off.setText(act.encode_time(act.off_time));

    Button sb = (Button) dialog.findViewById(R.id.general_screen);
    sb.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            end_dialog(dialog);
            screen_dialog();
        }
    });

    Button db = (Button) dialog.findViewById(R.id.general_developer);
    db.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            developer_dialog();
        }
    });

    Button lb = (Button) dialog.findViewById(R.id.general_log);
    lb.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            act.show_log();
            end_dialog(dialog);
        }
    });

    Button cfg = (Button) dialog.findViewById(R.id.general_config);
    cfg.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            act.show_cfg();
            end_dialog(dialog);
        }
    });

    Button ok = (Button) dialog.findViewById(R.id.ok);
    ok.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            act.general_layout = (rg.getCheckedRadioButtonId() == R.id.general_tablet) ?
                                            LAYOUT_TABLET :
                                            LAYOUT_PHONE;
            pref.put("general_layout", act.general_layout);

            act.egauge_layout = (bt_egauge.isChecked() ? act.general_layout : LAYOUT_NONE);
            pref.put("egauge_layout", act.egauge_layout);

            act.weather_layout = (bt_weather.isChecked() ? act.general_layout : LAYOUT_NONE);
            pref.put("weather_layout", act.weather_layout);

            act.thermostat_layout = (bt_thermostat.isChecked() ? act.general_layout : LAYOUT_NONE);
            pref.put("thermostat_layout", act.thermostat_layout);

            act.on_time = act.decode_time(et_on.getText().toString());
            pref.put("general_on", act.on_time);
            act.off_time = act.decode_time(et_off.getText().toString());
            pref.put("general_off", act.off_time);

            act.show_views();
            if (act.thermostat_layout != LAYOUT_NONE)
                act.thermostat.init_view();
            end_dialog(dialog);
        }
    });
}

private void egauge_dialog()
{

    final Dialog dialog = start_dialog(R.layout.bar_egauge);

    final CheckBox cb = (CheckBox) dialog.findViewById(R.id.egauge_progress);
    cb.setChecked(act.egauge_progress != 0);

    final RadioGroup rg = (RadioGroup) dialog.findViewById(R.id.general_house);
    rg.check(act.egauge_clock ? R.id.egauge_clock :
                                R.id.egauge_icon);

    final EditText et = (EditText) dialog.findViewById(R.id.egauge_url);
    et.setText(act.egauge_url);

    Button ok = (Button) dialog.findViewById(R.id.ok);
    ok.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            act.egauge_progress = (cb.isChecked() ? 1 : 0);
            pref.put("egauge_progress", act.egauge_progress);

            act.egauge_clock = ((rg.getCheckedRadioButtonId() == R.id.egauge_clock) ?  true : false);
            pref.put("egauge_clock", act.egauge_clock);

            act.egauge_url = et.getText().toString();
            pref.put("egauge_url", act.egauge_url);

            act.show_views();
            end_dialog(dialog);
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
            act.start_browser(Ecobee.ECO_URL);
            end_dialog(dialog);
        }
    });
}

private void ecobee_dopin(final String api)
{


    new Thread(new Runnable() {
        public void run() {
            final String pin = act.thermostat.ecobee.ecobee_get_pin(api);
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
            end_dialog(dialog);
            act.ecobee_api = api;
            pref.put("ecobee_api", act.ecobee_api);
            new Thread(new Runnable() {
                public void run() {
                    act.thermostat.ecobee.ecobee_authorize(act.ecobee_api);
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
            end_dialog(dialog);
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
            pref.put("ecobee_api", act.ecobee_api);
            pref.put("ecobee_access", act.ecobee_access);
            pref.put("ecobee_refresh", act.ecobee_refresh);
            end_dialog(dialog);
        }
    });
}

private void weather_dialog()
{
    int i;
    String name, pname;

    final Dialog dialog = start_dialog(R.layout.bar_weather);

    final CheckBox cb = (CheckBox) dialog.findViewById(R.id.weather_progress);
    cb.setChecked(act.weather_progress != 0);

    final EditText et = (EditText) dialog.findViewById(R.id.weather_id);
    et.setText(act.weather_id);

    Button ok = (Button) dialog.findViewById(R.id.ok);
    ok.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            act.weather_progress = (cb.isChecked() ? 1 : 0);
            pref.put("weather_progress", act.weather_progress);

            act.weather_id = et.getText().toString();
            pref.put("wunder_id", act.weather_id);

            act.show_views();
            end_dialog(dialog);
        }
    });
}

private void thermostat_dialog()
{
    int i;
    String name, pname;

    final Dialog dialog = start_dialog(R.layout.bar_thermostat);

    Button eb = (Button) dialog.findViewById(R.id.thermostat_ecobee);
    eb.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            ecobee_dialog();
            end_dialog(dialog);
        }
    });

    Button ok = (Button) dialog.findViewById(R.id.ok);
    ok.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            end_dialog(dialog);
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

    final TextView cols = (TextView) dialog.findViewById(R.id.outlets_cols);
    cols.setText(C.i2a(act.outlets_cols));

    final TextView min = (TextView) dialog.findViewById(R.id.outlets_batt_min);
    min.setText(C.i2a(act.outlets_batt_min));

    final TextView max = (TextView) dialog.findViewById(R.id.outlets_batt_max);
    max.setText(C.i2a(act.outlets_batt_max));

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
            pref.put("outlets_battery", act.outlets_battery);
            act.outlets_cols = C.a2i(cols.getText().toString());
            pref.put("outlets_cols", act.outlets_cols);
            act.outlets_batt_min = C.a2i(min.getText().toString());
            pref.put("outlets_batt_min", act.outlets_batt_min);
            act.outlets_batt_max = C.a2i(max.getText().toString());
            pref.put("outlets_batt_max", act.outlets_batt_max);
            end_dialog(dialog);
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
            pref.put("x10_url", act.x10_url);
            act.x10_jwt = jt.getText().toString();
            pref.put("x10_jwt", act.x10_jwt);
            end_dialog(dialog);
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
            pref.put("tplink_user", act.tplink_user);
            act.tplink_pwd = pt.getText().toString();
            pref.put("tplink_pwd", act.tplink_pwd);
            end_dialog(dialog);
            act.outlets.startup();
        }
    });
}

private void screen_dialog()
{

    final Dialog dialog = start_dialog(R.layout.bar_screen);

    final EditText ss_start = (EditText) dialog.findViewById(R.id.screen_start);
    ss_start.setText(act.ss_start > 0 ? C.i2a(act.ss_start) : "");

    final EditText ss_delay = (EditText) dialog.findViewById(R.id.screen_delay);
    ss_delay.setText(act.ss_start > 0 ? C.i2a(act.ss_delay) : "");

    final Spinner ss_fade = (Spinner) dialog.findViewById(R.id.screen_type);
    ArrayAdapter<String> adapter = new ArrayAdapter<String>(act, R.layout.text_spinner, SS_Faders.types);
    adapter.setDropDownViewResource(R.layout.text_spinner);
    ss_fade.setAdapter(adapter);
    ss_fade.setSelection(type_pos = act.ss_fade);
    ss_fade.setOnItemSelectedListener(new OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            type_pos = position;
        }
        
        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            // TODO Auto-generated method stub
        }

    });

    final EditText et_host = (EditText) dialog.findViewById(R.id.screen_host);
    et_host.setText(act.ss_info.ss_host);

    final EditText et_server = (EditText) dialog.findViewById(R.id.screen_server);
    et_server.setText(act.ss_info.ss_server);

    final EditText et_user = (EditText) dialog.findViewById(R.id.screen_user);
    et_user.setText(act.ss_info.ss_user);

    final EditText et_pwd = (EditText) dialog.findViewById(R.id.screen_pwd);
    et_pwd.setText(act.ss_info.ss_pwd);

    Button mgmt = (Button) dialog.findViewById(R.id.screen_mgmt);
    mgmt.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            end_dialog(dialog);
            act.screen_mgmt(v);
        }
    });

    Button ok = (Button) dialog.findViewById(R.id.ok);
    ok.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            act.ss_info.ss_host = et_host.getText().toString();
            pref.put("ss_host", act.ss_info.ss_host);
            act.ss_info.ss_list = C.suffix(act.ss_info.ss_host);
            act.ss_info.ss_server = et_server.getText().toString();
            pref.put("ss_server", act.ss_info.ss_server);
            act.ss_info.ss_user = et_user.getText().toString();
            pref.put("ss_user", act.ss_info.ss_user);
            act.ss_info.ss_pwd = et_pwd.getText().toString();
            pref.put("ss_pwd", act.ss_info.ss_pwd);

            try {
                act.ss_start = C.a2i(ss_start.getText().toString());
            } catch (Exception e) {
                act.ss_start = 0;
            }
            pref.put("ss_start", act.ss_start);
            if (act.ss_start > 0) {
                try {
                    act.ss_delay = C.a2i(ss_delay.getText().toString());
                } catch (Exception e) {
                    act.ss_delay = SS_DELAY;
                }
                pref.put("ss_delay", act.ss_delay);
            }
            act.ss_fade = type_pos;
            pref.put("ss_fade", act.ss_fade);
            act.ss_saver.screen_saver(C.SAVER_RESET);

            end_dialog(dialog);
        }
    });
}

private void developer_dialog()
{

    final Dialog dialog = start_dialog(R.layout.bar_developer);

    final CheckBox cb = (CheckBox) dialog.findViewById(R.id.about_debug);
    cb.setChecked(act.debug != 0);

    final TextView level = (TextView) dialog.findViewById(R.id.outlets_batt_level);
    level.setText(C.i2a(act.outlets_batt_level));

    final TextView uri = (TextView) dialog.findViewById(R.id.log_uri);
    uri.setText(act.log_uri);

    final TextView params = (TextView) dialog.findViewById(R.id.log_params);
    params.setText(act.log_params);

    Button ok = (Button) dialog.findViewById(R.id.ok);
    ok.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            act.outlets_batt_level = C.a2i(level.getText().toString());
            pref.put("outlets_batt_level", act.outlets_batt_level);

            act.log_uri = uri.getText().toString();
            pref.put("log_uri", act.log_uri);
            act.log_params = params.getText().toString();
            pref.put("log_params", act.log_params);

            act.debug = (cb.isChecked() ? 1 : 0);
            pref.put("debug", act.debug);
            Log.cfg(act.debug, act.log_uri, act.log_params);

            end_dialog(dialog);
        }
    });
}

public void remote_server(final String type, final String cfg)
{

    final Dialog dialog = start_dialog(R.layout.bar_remote);

    TextView tv = (TextView)dialog.findViewById(R.id.remote_title);
    tv.setText("Remote server - " + type);

    final EditText et = (EditText)dialog.findViewById(R.id.remote_url);
    et.setText(pref.get("remote_server", ""));

    Button ok = (Button) dialog.findViewById(R.id.ok);
    ok.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            end_dialog(dialog);
            new Thread(new Runnable() {
                public void run() {
                    act.remote_doit(type, et.getText().toString(), cfg);
                }
            }).start();
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
            end_dialog(dialog);
        }
    });

    Button ok = (Button) dialog.findViewById(R.id.ok);
    ok.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            end_dialog(dialog);
        }
    });
}

private void ecobee_get(Dialog dialog, String api)
{

    return;
}

}
