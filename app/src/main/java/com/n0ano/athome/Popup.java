package com.n0ano.athome;

import android.app.Dialog;
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

import com.n0ano.athome.SS.Faders;
import com.n0ano.athome.SS.ScreenSaver;

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

public final static int layout_outlets[] = {
    R.layout.outlets_hidden,                       
    R.layout.outlets_table,
    R.layout.outlets_table  
};

MainActivity act;

private int batt_pos;
private int type_pos;
public final static int SS_START = 30;
public final static int SS_DELAY = 30;

public Popup(MainActivity act)
{

    this.act = act;
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
        act.saver_click();
        return true;

    case R.id.action_display:
        display_dialog();
        return true;

    case R.id.action_general:
        general_dialog();
        return true;

    case R.id.action_screen:
        screen_dialog();
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
                end_dialog(dialog, false);
            }
        });
    }

    dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

    dialog.show();

    act.ss_control(C.SS_OP_BLOCK);

    return dialog;
}

public void end_dialog(Dialog dialog, boolean restart)
{

    act.ss_control(C.SS_OP_RESET);
    dialog.dismiss();
    if (restart)
        act.restart();
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

    tv = (TextView) dialog.findViewById(R.id.device_status);
    tv.setText(dev.get_state() == OutletsDevice.OFFLINE ? "offline" : "online");

    tv = (TextView) dialog.findViewById(R.id.device_type);
    tv.setText(dev.get_tname());

    tv = (TextView) dialog.findViewById(R.id.device_code);
    tv.setText(dev.get_dev_code());

    final CheckBox cb = (CheckBox) dialog.findViewById(R.id.device_hold);
    if (dev.get_state() == OutletsDevice.OFFLINE)
        cb.setEnabled(false);
    else {
        cb.setEnabled(true);
        cb.setChecked(dev.get_state() == OutletsDevice.HOLD);
    }

    Button ok = (Button) dialog.findViewById(R.id.ok);
    ok.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            dev.set_state((cb.isChecked() ? OutletsDevice.HOLD : OutletsDevice.ONLINE), true);
            dev.show();
            end_dialog(dialog, false);
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
    rg.check((P.get_int("general_layout") == LAYOUT_TABLET) ?
                                                R.id.general_tablet :
                                                R.id.general_phone);

    final CheckBox bt_egauge = (CheckBox) dialog.findViewById(R.id.general_egauge);
    bt_egauge.setChecked(P.get_int("egauge_layout") != LAYOUT_NONE);

    final CheckBox bt_weather = (CheckBox) dialog.findViewById(R.id.general_weather);
    bt_weather.setChecked(P.get_int("weather_layout") != LAYOUT_NONE);

    final CheckBox bt_thermostat = (CheckBox) dialog.findViewById(R.id.general_thermostat);
    bt_thermostat.setChecked(P.get_int("thermostat_layout") != LAYOUT_NONE);

    final CheckBox bt_outlets = (CheckBox) dialog.findViewById(R.id.general_outlets);
    bt_outlets.setChecked(P.get_int("outlets_layout") != LAYOUT_NONE);

    final EditText et_on = (EditText) dialog.findViewById(R.id.general_on);
    et_on.setText(C.encode_time(P.get("general_on", -1)));

    final EditText et_off = (EditText) dialog.findViewById(R.id.general_off);
    et_off.setText(C.encode_time(P.get("general_off", -1)));

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
            end_dialog(dialog, false);
        }
    });

    Button cfg = (Button) dialog.findViewById(R.id.general_config);
    cfg.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            act.show_cfg();
            end_dialog(dialog, false);
        }
    });

    Button ok = (Button) dialog.findViewById(R.id.ok);
    ok.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            int layout = (rg.getCheckedRadioButtonId() == R.id.general_tablet) ?
                                                            LAYOUT_TABLET :
                                                            LAYOUT_PHONE;
            P.put("general_layout", layout);

            P.put("egauge_layout", bt_egauge.isChecked() ? layout : LAYOUT_NONE);

            P.put("weather_layout", bt_weather.isChecked() ? layout : LAYOUT_NONE);

            P.put("thermostat_layout", bt_thermostat.isChecked() ? layout : LAYOUT_NONE);

            P.put("outlets_layout", bt_outlets.isChecked() ? layout : LAYOUT_NONE);

            P.put("general_on", C.decode_time(et_on.getText().toString()));
            P.put("general_off", C.decode_time(et_off.getText().toString()));

            end_dialog(dialog, true);
        }
    });
}

private void egauge_dialog()
{

    final Dialog dialog = start_dialog(R.layout.bar_egauge);

    if (P.get_int("egauge_layout") == LAYOUT_NONE) {
        end_dialog(dialog, false);
        act.toast("eGauge disabled");
        return;
    }

    final RadioGroup rg = (RadioGroup) dialog.findViewById(R.id.general_house);
    rg.check(P.get_boolean("egauge_clock") ? R.id.egauge_clock :
                                             R.id.egauge_icon);

    final EditText et = (EditText) dialog.findViewById(R.id.egauge_url);
    et.setText(P.get_string("egauge_url"));

    Button ok = (Button) dialog.findViewById(R.id.ok);
    ok.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            P.put("egauge_clock", ((rg.getCheckedRadioButtonId() == R.id.egauge_clock) ?  true : false));

            P.put("egauge_url", et.getText().toString());

            end_dialog(dialog, true);
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
            act.start_browser(Ecobee.ECO_LOGIN);
            end_dialog(dialog, false);
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
            P.put("ecobee_api", api);
            new Thread(new Runnable() {
                public void run() {
                    act.thermostat.ecobee.ecobee_authorize(P.get_string("ecobee_api"));
                }
            }).start();
            end_dialog(dialog, false);
        }
    });
}

private void ecobee_dialog()
{
    int nt;

    final Dialog dialog = start_dialog(R.layout.bar_ecobee);

    final EditText api_tv = (EditText) dialog.findViewById(R.id.ecobee_api);
    api_tv.setText(P.get_string("ecobee_api"));

    final EditText access_tv = (EditText) dialog.findViewById(R.id.ecobee_access);
    access_tv.setText(P.get_string("ecobee_access"));

    final EditText refresh_tv = (EditText) dialog.findViewById(R.id.ecobee_refresh);
    refresh_tv.setText(P.get_string("ecobee_refresh"));

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
            end_dialog(dialog, false);
            ecobee_dopin(api);
        }
    });

    Button ok = (Button) dialog.findViewById(R.id.ok);
    ok.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            P.put("ecobee_api", api_tv.getText().toString());
            P.put("ecobee_access", access_tv.getText().toString());
            P.put("ecobee_refresh", refresh_tv.getText().toString());
            end_dialog(dialog, false);
        }
    });
}

private void weather_dialog()
{
    int i;
    String name, pname;

    final Dialog dialog = start_dialog(R.layout.bar_weather);

    if (P.get_int("weather_layout") == LAYOUT_NONE) {
        end_dialog(dialog, false);
        act.toast("Weather disabled");
        return;
    }

    final EditText et_id = (EditText) dialog.findViewById(R.id.weather_id);
    et_id.setText(P.get_string("wunder_id"));

    final EditText et_key = (EditText) dialog.findViewById(R.id.weather_key);
    et_key.setText(P.get_string("wunder_key"));

    Button ok = (Button) dialog.findViewById(R.id.ok);
    ok.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            P.put("wunder_id", et_id.getText().toString());

            P.put("wunder_key", et_key.getText().toString());

            end_dialog(dialog, true);
        }
    });
}

private void thermostat_dialog()
{
    int i;
    String name, pname;

    final Dialog dialog = start_dialog(R.layout.bar_thermostat);

    if (P.get_int("thermostat_layout") == LAYOUT_NONE) {
        end_dialog(dialog, false);
        act.toast("Thermostats disabled");
        return;
    }

    Button eb = (Button) dialog.findViewById(R.id.thermostat_ecobee);
    eb.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            ecobee_dialog();
            end_dialog(dialog, false);
        }
    });

    Button ok = (Button) dialog.findViewById(R.id.ok);
    ok.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            end_dialog(dialog, false);
        }
    });
}

private void outlets_dialog()
{
    int i;
    String name, pname;

    final Dialog dialog = start_dialog(R.layout.bar_outlets);

    if (P.get_int("outlets_layout") == LAYOUT_NONE) {
        end_dialog(dialog, false);
        act.toast("Outlets disabled");
        return;
    }
    int max_dev = act.outlets.outlets_adapter.getCount();
    String[] names = new String[max_dev + 1];
    names[0] = "- none -";
    batt_pos = 0;
    pname = P.get_string("outlets_battery");
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
    cols.setText(C.i2a(P.get_int("outlets_cols")));

    final TextView min = (TextView) dialog.findViewById(R.id.outlets_batt_min);
    min.setText(C.i2a(P.get_int("outlets_batt_min")));

    final TextView max = (TextView) dialog.findViewById(R.id.outlets_batt_max);
    max.setText(C.i2a(P.get_int("outlets_batt_max")));

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
            String str;
            if (batt_pos <= 0)
                str = "";
            else
                str = act.outlets.outlets_adapter.getItem(batt_pos - 1).get_name();
            P.put("outlets_battery", str);
            P.put("outlets_cols", C.a2i(cols.getText().toString()));
            P.put("outlets_batt_min", C.a2i(min.getText().toString()));
            P.put("outlets_batt_max", C.a2i(max.getText().toString()));
            end_dialog(dialog, true);
        }
    });
}

private void x10_dialog()
{
    int i;

    final Dialog dialog = start_dialog(R.layout.bar_x10);

    final EditText et = (EditText) dialog.findViewById(R.id.x10_url);
    et.setText(P.get_string("x10_url"));

    final EditText jt = (EditText) dialog.findViewById(R.id.x10_jwt);
    jt.setText(P.get_string("x10_jwt"));

    Button ok = (Button) dialog.findViewById(R.id.ok);
    ok.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            P.put("x10_url", et.getText().toString());
            P.put("x10_jwt", jt.getText().toString());
            end_dialog(dialog, true);
        }
    });
}

private void tplink_dialog()
{
    int i;

    final Dialog dialog = start_dialog(R.layout.bar_tplink);

    final EditText ut = (EditText) dialog.findViewById(R.id.tplink_user);
    ut.setText(P.get_string("tplink_user"));

    final EditText pt = (EditText) dialog.findViewById(R.id.tplink_pwd);
    pt.setText(P.get_string("tplink_pwd"));

    Button ok = (Button) dialog.findViewById(R.id.ok);
    ok.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            P.put("tplink_user", ut.getText().toString());
            P.put("tplink_pwd", pt.getText().toString());
            end_dialog(dialog, true);
        }
    });
}

private void screen_dialog()
{

    final Dialog dialog = start_dialog(R.layout.bar_screen);

    final CheckBox cb_enable = (CheckBox) dialog.findViewById(R.id.screen_enable);
    cb_enable.setChecked(act.ss_info.enable);

    final EditText ss_start = (EditText) dialog.findViewById(R.id.screen_start);
    ss_start.setText(act.ss_info.start == 0 ? "" : C.i2a(act.ss_info.start));

    final EditText ss_delay = (EditText) dialog.findViewById(R.id.screen_delay);
    ss_delay.setText(act.ss_info.delay == 0 ? "" : C.i2a(act.ss_info.delay));

    final Spinner ss_fade = (Spinner) dialog.findViewById(R.id.screen_type);
    ArrayAdapter<String> adapter = new ArrayAdapter<String>(act, R.layout.text_spinner, Faders.types);
    adapter.setDropDownViewResource(R.layout.text_spinner);
    ss_fade.setAdapter(adapter);
    ss_fade.setSelection(type_pos = act.ss_info.fade);
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
    et_host.setText(act.ss_info.host);

    final EditText et_server = (EditText) dialog.findViewById(R.id.screen_server);
    et_server.setText(act.ss_info.server);

    final EditText et_user = (EditText) dialog.findViewById(R.id.screen_user);
    et_user.setText(act.ss_info.user);

    final EditText et_pwd = (EditText) dialog.findViewById(R.id.screen_pwd);
    et_pwd.setText(act.ss_info.pwd);

    final CheckBox cb = (CheckBox) dialog.findViewById(R.id.screen_reset);
    cb.setChecked(false);

    Button mgmt = (Button) dialog.findViewById(R.id.screen_mgmt);
    mgmt.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            //  Don't call end_dialog, we still want the screen saver blocked
            dialog.dismiss();
            act.screen_mgmt(v);
        }
    });

    Button ok = (Button) dialog.findViewById(R.id.ok);
    ok.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            act.ss_info.enable = cb_enable.isChecked();
            P.put("ss_enable", act.ss_info.enable);
            act.ss_info.host = et_host.getText().toString();
            P.put("ss_host", act.ss_info.host);
            act.ss_info.list = C.suffix(act.ss_info.host);
            act.ss_info.server = et_server.getText().toString();
            P.put("ss_server", act.ss_info.server);
            act.ss_info.user = et_user.getText().toString();
            P.put("ss_user", act.ss_info.user);
            act.ss_info.pwd = et_pwd.getText().toString();
            P.put("ss_pwd", act.ss_info.pwd);

            try {
                act.ss_info.start = C.a2i(ss_start.getText().toString());
            } catch (Exception e) {
                act.ss_info.start = 0;
            }
            P.put("ss_start", act.ss_info.start);

            try {
                act.ss_info.delay = C.a2i(ss_delay.getText().toString());
            } catch (Exception e) {
                act.ss_info.delay = SS_DELAY;
            }
            P.put("ss_delay", act.ss_info.delay);

            act.ss_info.fade = type_pos;
            P.put("ss_fade", act.ss_info.fade);

            if (cb.isChecked())
                act.ss_saver.ss_reset();

            act.ss_control(C.SS_OP_INIT);

            end_dialog(dialog, true);
        }
    });
}

private void developer_dialog()
{

    final Dialog dialog = start_dialog(R.layout.bar_developer);

    final CheckBox cb = (CheckBox) dialog.findViewById(R.id.about_debug);
    cb.setChecked(P.get_int("debug") != 0);

    final TextView level = (TextView) dialog.findViewById(R.id.outlets_batt_level);
    level.setText(C.i2a(P.get_int("outlets_batt_level")));

    final TextView uri = (TextView) dialog.findViewById(R.id.log_uri);
    uri.setText(P.get_string("log_uri"));

    final TextView params = (TextView) dialog.findViewById(R.id.log_params);
    params.setText(P.get_string("log_params"));

    Button ok = (Button) dialog.findViewById(R.id.ok);
    ok.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            P.put("outlets_batt_level", C.a2i(level.getText().toString()));

            P.put("log_uri", uri.getText().toString());
            P.put("log_params", params.getText().toString());

            P.put("debug", (cb.isChecked() ? 1 : 0));
            Log.cfg(P.get_int("debug"), P.get_string("log_uri"), P.get_string("log_params"));

            end_dialog(dialog, true);
        }
    });
}

public void remote_server(final String type, final String cfg)
{

    final Dialog dialog = start_dialog(R.layout.bar_remote);

    TextView tv = (TextView)dialog.findViewById(R.id.remote_title);
    tv.setText("Remote server - " + type);

    final EditText et = (EditText)dialog.findViewById(R.id.remote_url);
    et.setText(P.get("remote_server", ""));

    Button ok = (Button) dialog.findViewById(R.id.ok);
    ok.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            end_dialog(dialog, false);
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
            end_dialog(dialog, false);
        }
    });

    Button ok = (Button) dialog.findViewById(R.id.ok);
    ok.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            end_dialog(dialog, false);
        }
    });
}

private void ecobee_get(Dialog dialog, String api)
{

    return;
}

}
