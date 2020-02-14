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
import android.widget.NumberPicker;
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
        act.action_click();
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
    tv.setText(dev.get_online() ? "online" : "offline");

    tv = (TextView) dialog.findViewById(R.id.device_type);
    tv.setText(dev.get_tname());

    tv = (TextView) dialog.findViewById(R.id.device_code);
    tv.setText(dev.get_dev_code());

    final CheckBox cb = (CheckBox) dialog.findViewById(R.id.device_hold);
    if (!dev.get_online())
        cb.setEnabled(false);
    else {
        cb.setEnabled(true);
        cb.setChecked(dev.get_hold());
    }

    Button ok = (Button) dialog.findViewById(R.id.ok);
    ok.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            dev.set_hold(cb.isChecked());
            dev.show();
            end_dialog(dialog, false);
        }
    });
}

public void log_detail_dialog(int i, final String l)
{

    final Dialog dialog = start_dialog(R.layout.bar_log_detail);

    TextView lv = (TextView) dialog.findViewById(R.id.log_lineno);
    lv.setText("" + i);

    final TextView tv = (TextView) dialog.findViewById(R.id.log_detail);
    tv.setText(l);

    final Button but = (Button)dialog.findViewById(R.id.log_json);
    but.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (but.getText().toString().equals("JSON")) {
                but.setText("RAW");
                tv.setText(C.indent(l));
            } else {
                but.setText("JSON");
                tv.setText(l);
            }
        }
    });
}

private void config_dialog()
{
    int nt;

    final Dialog dialog = start_dialog(R.layout.bar_config);

    final TextView tv = (TextView) dialog.findViewById(R.id.config_table);
    tv.setText(C.get_cfg(2));

    final TextView cfg_url = (TextView) dialog.findViewById(R.id.config_url);
    cfg_url.setText(P.get_string("general:config_url"));

    Button b_save = (Button) dialog.findViewById(R.id.save);
    b_save.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            String host = cfg_url.getText().toString();
            P.put("general:config_url", cfg_url.getText().toString());
            act.remote_doit(host, C.CONFIG_SAVE, C.get_cfg(2));
            end_dialog(dialog, false);
        }
    });

    Button b_load = (Button) dialog.findViewById(R.id.load);
    b_load.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            String host = cfg_url.getText().toString();
            P.put("general:config_url", cfg_url.getText().toString());
            act.remote_doit(host, C.CONFIG_LOAD, null);
            end_dialog(dialog, false);
        }
    });

    Button ok = (Button) dialog.findViewById(R.id.ok);
    ok.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            P.put("general:config_url", cfg_url.getText().toString());
            end_dialog(dialog, false);
        }
    });
}

private void general_dialog()
{
    int i;
    String name, pname;

    final Dialog dialog = start_dialog(R.layout.bar_general);

    final RadioGroup rg = (RadioGroup) dialog.findViewById(R.id.general_layout);
    rg.check((P.get_int("general:layout") == LAYOUT_TABLET) ?
                                                R.id.general_tablet :
                                                R.id.general_phone);

    final CheckBox bt_egauge = (CheckBox) dialog.findViewById(R.id.general_egauge);
    bt_egauge.setChecked(P.get_int("egauge:layout") != LAYOUT_NONE);

    final CheckBox bt_weather = (CheckBox) dialog.findViewById(R.id.general_weather);
    bt_weather.setChecked(P.get_int("weather:layout") != LAYOUT_NONE);

    final CheckBox bt_thermostat = (CheckBox) dialog.findViewById(R.id.general_thermostat);
    bt_thermostat.setChecked(P.get_int("thermostat:layout") != LAYOUT_NONE);

    final CheckBox bt_outlets = (CheckBox) dialog.findViewById(R.id.general_outlets);
    bt_outlets.setChecked(P.get_int("outlets:layout") != LAYOUT_NONE);

    final EditText et_on = (EditText) dialog.findViewById(R.id.general_on);
    et_on.setText(C.encode_time(P.get_int("general:on")));

    final EditText et_off = (EditText) dialog.findViewById(R.id.general_off);
    et_off.setText(C.encode_time(P.get_int("general:off")));

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
            config_dialog();
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
            P.put("general:layout", layout);

            P.put("egauge:layout", bt_egauge.isChecked() ? layout : LAYOUT_NONE);

            P.put("weather:layout", bt_weather.isChecked() ? layout : LAYOUT_NONE);

            P.put("thermostat:layout", bt_thermostat.isChecked() ? layout : LAYOUT_NONE);

            P.put("outlets:layout", bt_outlets.isChecked() ? layout : LAYOUT_NONE);

            P.put("general:on", C.decode_time(et_on.getText().toString()));
            P.put("general:off", C.decode_time(et_off.getText().toString()));

            end_dialog(dialog, true);
        }
    });
}

private void alerts_dialog()
{

    final Dialog dialog = start_dialog(R.layout.bar_alerts);

    TextView tv = (TextView)dialog.findViewById(R.id.alerts_list);
    tv.setText(act.alerts_str());

    final CheckBox cb = (CheckBox) dialog.findViewById(R.id.alerts_ack);
    cb.setChecked(false);

    Button ok = (Button) dialog.findViewById(R.id.ok);
    ok.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (cb.isChecked())
                act.alerts_ack();
            end_dialog(dialog, false);
        }
    });
}

private void egauge_dialog()
{

    final Dialog dialog = start_dialog(R.layout.bar_egauge);

    if (P.get_int("egauge:layout") == LAYOUT_NONE) {
        end_dialog(dialog, false);
        act.toast("eGauge disabled");
        return;
    }

    final RadioGroup rg = (RadioGroup) dialog.findViewById(R.id.general_house);
    rg.check(P.get_boolean("egauge:clock") ? R.id.egauge_clock :
                                             R.id.egauge_icon);

    final EditText et = (EditText) dialog.findViewById(R.id.egauge_url);
    et.setText(P.get_string("egauge:url"));

    final EditText ut = (EditText) dialog.findViewById(R.id.egauge_user);
    ut.setText(P.get_string("egauge:user"));

    final EditText pt = (EditText) dialog.findViewById(R.id.egauge_pwd);
    pt.setText(P.get_string("egauge:pwd"));

    Button show = (Button) dialog.findViewById(R.id.egauge_alerts);
    show.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            end_dialog(dialog, false);
            alerts_dialog();
        }
    });

    Button ok = (Button) dialog.findViewById(R.id.ok);
    ok.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            P.put("egauge:clock", ((rg.getCheckedRadioButtonId() == R.id.egauge_clock) ?  true : false));

            P.put("egauge:url", et.getText().toString());
            P.put("egauge:user", ut.getText().toString());
            P.put("egauge:pwd", pt.getText().toString());

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
            P.put("thermostat:ecobee_api", api);
            end_dialog(dialog, true);
        }
    });
}

private void ecobee_dialog()
{
    int nt;

    final Dialog dialog = start_dialog(R.layout.bar_ecobee);

    final EditText api_tv = (EditText) dialog.findViewById(R.id.ecobee_api);
    api_tv.setText(P.get_string("thermostat:ecobee_api"));

    final EditText access_tv = (EditText) dialog.findViewById(R.id.ecobee_access);
    access_tv.setText(P.get_string("thermostat:ecobee_access"));

    final EditText refresh_tv = (EditText) dialog.findViewById(R.id.ecobee_refresh);
    refresh_tv.setText(P.get_string("thermostat:ecobee_refresh"));

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
            P.put("thermostat:ecobee_api", api_tv.getText().toString());
            P.put("thermostat:ecobee_access", access_tv.getText().toString());
            P.put("thermostat:ecobee_refresh", refresh_tv.getText().toString());
            end_dialog(dialog, false);
        }
    });
}

public void detail_dialog(float max_temp, float min_temp, String max_temp_time, String min_temp_time)
{

    final Dialog dialog = new Dialog(act, R.style.AlertDialogCustom);
    dialog.setContentView(R.layout.detail);

    TextView tv = (TextView) dialog.findViewById(R.id.detail_max);
    tv.setText(String.format("%.1f", max_temp));

    tv = (TextView) dialog.findViewById(R.id.detail_min);
    tv.setText(String.format("%.1f", min_temp));

    tv = (TextView) dialog.findViewById(R.id.detail_max_time);
    tv.setText(max_temp_time);

    tv = (TextView) dialog.findViewById(R.id.detail_min_time);
    tv.setText(min_temp_time);

    Button ok = (Button) dialog.findViewById(R.id.ok);
    ok.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            dialog.dismiss();
        }
    });

    dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

    dialog.show();
}

private void station_dialog()
{
    int i;
    String name, pname;

    final Dialog dialog = start_dialog(R.layout.bar_station);

    final EditText tv_name = (EditText)dialog.findViewById(R.id.weather_name);
    final EditText tv_id = (EditText)dialog.findViewById(R.id.weather_id);
    final EditText tv_key = (EditText)dialog.findViewById(R.id.weather_key);
    final Spinner sv_type = (Spinner)dialog.findViewById(R.id.weather_type);
    ArrayAdapter<String> adapter = new ArrayAdapter<String>(act, R.layout.text_spinner, WeatherStation.types);
    adapter.setDropDownViewResource(R.layout.text_spinner);
    sv_type.setAdapter(adapter);
    sv_type.setOnItemSelectedListener(new OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        }
        
        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            // TODO Auto-generated method stub
        }

    });

    Button ok = (Button) dialog.findViewById(R.id.ok);
    ok.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
Log.d("DDD", "weather new station = " + tv_name.getText().toString() + ", " + sv_type.getSelectedItemId());
            end_dialog(dialog, true);
        }
    });
}

private void weather_dialog()
{
    int i;
    String name, pname;

    final Dialog dialog = start_dialog(R.layout.bar_weather);

    if (P.get_int("weather:layout") == LAYOUT_NONE) {
        end_dialog(dialog, false);
        act.toast("Weather disabled");
        return;
    }

    final TextView tv_type = (TextView)dialog.findViewById(R.id.weather_type);
    final TextView tv_id = (TextView)dialog.findViewById(R.id.weather_id);
    final TextView tv_key = (TextView)dialog.findViewById(R.id.weather_key);

    int max = act.weather.stations.size();
    String[] names = new String[max];
    for (i = 0; i < max; i++)
        names[i] = act.weather.stations.get(i).name;
    final Spinner sv = (Spinner) dialog.findViewById(R.id.weather_stations);
    ArrayAdapter<String> adapter = new ArrayAdapter<String>(act, R.layout.text_spinner, names);
    adapter.setDropDownViewResource(R.layout.text_spinner);
    sv.setAdapter(adapter);
    sv.setOnItemSelectedListener(new OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            WeatherStation ws = act.weather.stations.get(position);
            tv_type.setText(WeatherStation.types[ws.type]);
            tv_id.setText(ws.id);
            tv_key.setText(ws.key);
        }
        
        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            // TODO Auto-generated method stub
        }

    });
    if (max > 0) {
        WeatherStation ws = act.weather.stations.get(0);
        tv_type.setText(WeatherStation.types[ws.type]);
        tv_id.setText(ws.id);
        tv_key.setText(ws.key);
    }

    Button add = (Button) dialog.findViewById(R.id.add);
    add.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            end_dialog(dialog, false);
            station_dialog();
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

public void hold_dialog(final ThermostatDevice dev, final Ecobee ecobee)
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

private void thermostat_dialog()
{
    int i;
    String name, pname;

    final Dialog dialog = start_dialog(R.layout.bar_thermostat);

    if (P.get_int("thermostat:layout") == LAYOUT_NONE) {
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

    if (P.get_int("outlets:layout") == LAYOUT_NONE) {
        end_dialog(dialog, false);
        act.toast("Outlets disabled");
        return;
    }
    int max_dev = act.outlets.outlets_adapter.getCount();
    String[] names = new String[max_dev + 1];
    names[0] = "- none -";
    batt_pos = 0;
    pname = P.get_string("outlets:battery");
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
    cols.setText(C.i2a(P.get_int("outlets:cols")));

    final TextView min = (TextView) dialog.findViewById(R.id.outlets_batt_min);
    min.setText(C.i2a(P.get_int("outlets:batt_min")));

    final TextView max = (TextView) dialog.findViewById(R.id.outlets_batt_max);
    max.setText(C.i2a(P.get_int("outlets:batt_max")));

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
            P.put("outlets:battery", str);
            P.put("outlets:cols", C.a2i(cols.getText().toString()));
            P.put("outlets:batt_min", C.a2i(min.getText().toString()));
            P.put("outlets:batt_max", C.a2i(max.getText().toString()));
            end_dialog(dialog, true);
        }
    });
}

private void x10_dialog()
{
    int i;

    final Dialog dialog = start_dialog(R.layout.bar_x10);

    final EditText et = (EditText) dialog.findViewById(R.id.x10_url);
    et.setText(P.get_string("outlets:x10_url"));

    final EditText jt = (EditText) dialog.findViewById(R.id.x10_jwt);
    jt.setText(P.get_string("outlets:x10_jwt"));

    Button ok = (Button) dialog.findViewById(R.id.ok);
    ok.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            P.put("outlets:x10_url", et.getText().toString());
            P.put("outlets:x10_jwt", jt.getText().toString());
            end_dialog(dialog, true);
        }
    });
}

private void tplink_dialog()
{
    int i;

    final Dialog dialog = start_dialog(R.layout.bar_tplink);

    final EditText ut = (EditText) dialog.findViewById(R.id.tplink_user);
    ut.setText(P.get_string("outlets:tplink_user"));

    final EditText pt = (EditText) dialog.findViewById(R.id.tplink_pwd);
    pt.setText(P.get_string("outlets:tplink_pwd"));

    Button ok = (Button) dialog.findViewById(R.id.ok);
    ok.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            P.put("outlets:tplink_user", ut.getText().toString());
            P.put("outlets:tplink_pwd", pt.getText().toString());
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
            P_SS.put("ss_enable", act.ss_info.enable);
            act.ss_info.host = et_host.getText().toString();
            P_SS.put("ss_host", act.ss_info.host);
            act.ss_info.list = C.suffix(act.ss_info.host);
            act.ss_info.server = et_server.getText().toString();
            P_SS.put("ss_server", act.ss_info.server);
            act.ss_info.user = et_user.getText().toString();
            P_SS.put("ss_user", act.ss_info.user);
            act.ss_info.pwd = et_pwd.getText().toString();
            P_SS.put("ss_pwd", act.ss_info.pwd);

            try {
                act.ss_info.start = C.a2i(ss_start.getText().toString());
            } catch (Exception e) {
                act.ss_info.start = 0;
            }
            P_SS.put("ss_start", act.ss_info.start);

            try {
                act.ss_info.delay = C.a2i(ss_delay.getText().toString());
            } catch (Exception e) {
                act.ss_info.delay = SS_DELAY;
            }
            P_SS.put("ss_delay", act.ss_info.delay);

            act.ss_info.fade = type_pos;
            P_SS.put("ss_fade", act.ss_info.fade);

            if (cb.isChecked() && act.ss_saver != null)
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
    level.setText(C.i2a(P.get_int("outlets:batt_level")));

    final TextView uri = (TextView) dialog.findViewById(R.id.log_uri);
    uri.setText(P.get_string("general:log_uri"));

    final TextView params = (TextView) dialog.findViewById(R.id.log_params);
    params.setText(P.get_string("general:log_params"));

    final TextView llen = (TextView) dialog.findViewById(R.id.log_line_length);
    llen.setText(C.i2a(P.get_int("general:log_length")));

    Button ok = (Button) dialog.findViewById(R.id.ok);
    ok.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            P.put("outlets:batt_level", C.a2i(level.getText().toString()));

            P.put("general:log_uri", uri.getText().toString());
            P.put("general:log_params", params.getText().toString());
            P.put("general:log_length", C.a2i(llen.getText().toString()));

            P.put("debug", (cb.isChecked() ? 1 : 0));
            Log.cfg(P.get_int("debug"), P.get_string("general:log_uri"), P.get_string("general:log_params"));

            end_dialog(dialog, true);
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
