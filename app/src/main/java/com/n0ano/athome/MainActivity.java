package com.n0ano.athome;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Iterator;

import com.n0ano.athome.SS.ImageMgmt;
import com.n0ano.athome.SS.ScreenInfo;
import com.n0ano.athome.SS.ScreenSaver;
import com.n0ano.athome.SS.MyGesture;
import com.n0ano.athome.SS.SS_Callbacks;

public class MainActivity extends AppCompatActivity
{

private final static String CONFIG_URI = "/cgi-bin/athome/config";
private final static String CONFIG_LOAD =  "load";
private final static String CONFIG_SAVE =  "save";

private boolean paused = false;

public Menu menu_bar;

private String url;

int degree = 0;

Weather weather;
Thermostat thermostat;
Egauge egauge;
Outlets outlets;

public Popup popup;

public boolean screen = true;
private int screen_bright = -1;

public ScreenSaver ss_saver;
private GestureDetectorCompat ss_gesture;

public ScreenInfo ss_info;
private int ss_offset = 0;

@Override
protected void onCreate(Bundle state)
{

    super.onCreate(state);

    P.init(getSharedPreferences(P.PREF_NAME, Context.MODE_PRIVATE));
    Log.cfg(P.get_int("debug"), "", "");
Log.cfg(1, "", "");
    Log.d("MainActivity: onCreate");

    setContentView((P.get_int("general_layout") == Popup.LAYOUT_TABLET) ?
                                                        R.layout.activity_tab :
                                                        R.layout.activity_ph);

    Log.cfg(P.get_int("debug"), P.get_string("log_uri"), P.get_string("log_params"));

    ss_info = new ScreenInfo(this);
}

@Override
public boolean dispatchTouchEvent(MotionEvent ev)
{

    if (ss_saver == null)
        return super.dispatchTouchEvent(ev);
    if (ev.getY() < ss_offset)
        return super.dispatchTouchEvent(ev);
    if (ev.getAction() == MotionEvent.ACTION_DOWN) {
        if (!ss_saver.touch())
            return false;
    }
    return super.dispatchTouchEvent(ev);
}

@Override
protected void onStart()
{

    super.onStart();
    Log.d("MainActivity: onStart");
}

@Override
protected void onRestart()
{

    super.onRestart();
    Log.d("MainActivity: onRestart");
}

@Override
protected void onResume()
{

    super.onResume();
    Log.d("MainActivity: onResume");

    start_home();

    ss_control(C.SS_OP_INIT);

    ss_gesture = new GestureDetectorCompat(this, new MyGesture(ss_saver));

    doit();
}

@Override
protected void onPause()
{

    super.onPause();
    Log.d("MainActivity: onPause");
}

@Override
protected void onStop()
{

    if (egauge != null)
        egauge.stop();
    if (weather != null)
        weather.stop();
    if (thermostat != null)
        thermostat.stop();
    if (outlets != null)
        outlets.stop();

    super.onStop();
    Log.d("MainActivity: onStop");
}

@Override
protected void onDestroy()
{

    super.onDestroy();
    Log.d("MainActivity: onDestroy");
}

@Override
public void onBackPressed()
{

    super.onBackPressed();
    Log.d("MainActivity: onBackPressed");
}

@Override
protected void onSaveInstanceState(Bundle state)
{

    super.onSaveInstanceState(state);

    //
    //  Save state needed on the next onCreate, e.g.
    //      state.putString("KEY", "value");
    Log.d("MainActivity: onSaveInstanceState");
}

@Override
public boolean onCreateOptionsMenu(Menu menu)
{

    Log.d("MainActivity: onCreateOptionsMenu");
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    menu_bar = menu;
    MenuItem icon = menu_bar.findItem(R.id.action_saver);
    icon.setVisible(ss_info.enable);
    return true;
}

@Override
public boolean onOptionsItemSelected(MenuItem item)
{

    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.

    if (popup.menu_click(item.getItemId()))
        return true;
    return super.onOptionsItemSelected(item);
}

@Override
public boolean onTouchEvent(MotionEvent event)
{

    ss_gesture.onTouchEvent(event);
    return super.onTouchEvent(event);
}

@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == ImageMgmt.IMAGES_UPDATE) {
        if (resultCode == RESULT_OK) {
            Log.d("image update worked");
        }
Log.d("DDD-SS", "activity result - " + P.get("images:" + ss_info.list, ""));
        runOnUiThread(new Runnable() {
            public void run() {
                Toolbar tb = (Toolbar) findViewById(R.id.toolbar);
                tb.setTitle("AtHome");
            }
        });
        ss_control(C.SS_OP_UPDATE);
    }
}

public void restart()
{

    Log.d("DDD-SS", "MainActivity: restarting");
    Intent intent = getIntent();
    finish();
    startActivity(intent);
}

private void menu_icons(boolean vis)
{
    MenuItem icon = menu_bar.findItem(R.id.action_saver);

	icon = menu_bar.findItem(R.id.action_display);
    icon.setVisible(vis);
	icon = menu_bar.findItem(R.id.action_general);
    icon.setVisible(vis);
	icon = menu_bar.findItem(R.id.action_screen);
    icon.setVisible(vis);
	icon = menu_bar.findItem(R.id.action_egauge);
    icon.setVisible(vis);
	icon = menu_bar.findItem(R.id.action_weather);
    icon.setVisible(vis);
	icon = menu_bar.findItem(R.id.action_thermostat);
    icon.setVisible(vis);
	icon = menu_bar.findItem(R.id.action_outlets);
    icon.setVisible(vis);
	icon = menu_bar.findItem(R.id.action_about);
    icon.setVisible(vis);
}

private void start_home()
{

    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    toolbar.setTitle("AtHome");
    toolbar.setTitleTextColor(Color.WHITE);
    setSupportActionBar(toolbar);
    try {
        ContentResolver resolver = this.getApplicationContext().getContentResolver();
        screen_bright = Settings.System.getInt(resolver, Settings.System.SCREEN_BRIGHTNESS);

        Field titleField = Toolbar.class.getDeclaredField("mTitleTextView");
        titleField.setAccessible(true);
        TextView barTitleView = (TextView) titleField.get(toolbar);
        barTitleView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("title clicked");
                display_toggle(null);
            }
        });
    } catch (Exception e) {
        Log.d("tool bar exception " + e);
    }
    ViewGroup.LayoutParams params = toolbar.getLayoutParams();
    ss_offset = params.height;

    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    popup = new Popup(this);

    display(true);
}

public void start_browser(String uri)
{

    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
    startActivity(intent);
}

public void display_toggle(View V)
{

    display(!screen);
}

public void toast(String msg)
{

    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
}

public void display(final boolean onoff)
{

    final ContentResolver resolver = this.getApplicationContext().getContentResolver();
    runOnUiThread(new Runnable() {
        public void run() {
            screen = onoff;
            try {
                Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS, onoff ? screen_bright : 0);
            } catch (Exception e) {
                Log.d("can't change brightness " + e);
                toast("Can't change brightness - permissions?");
            }
            View view = (View) findViewById(R.id.scroll_view);
            view.setVisibility(onoff ? View.VISIBLE : View.GONE);
            view.setAlpha(1.0f);
            view = (View) findViewById(R.id.toolbar);
            view.setVisibility(onoff ? View.VISIBLE : View.GONE);
            view = (View) findViewById(R.id.blank_view);
            view.setVisibility(onoff ? View.GONE : View.VISIBLE);
            if (menu_bar != null) {
                MenuItem icon = menu_bar.findItem(R.id.action_display);
                icon.setIcon(onoff ? R.drawable.light_on : R.drawable.light_off);
            }
        }
    });
}

public void screen_mgmt(View v)
{

    Intent intent = new Intent(this, ImageMgmt.class);
    startActivityForResult(intent, ImageMgmt.IMAGES_UPDATE);
}

public View view_show(int layout, int view_id)
{

    View v = (View)findViewById(view_id);
    if (layout == Popup.LAYOUT_NONE)
        v.setVisibility(View.GONE);
    else
        v.setVisibility(View.VISIBLE);
    return v;
}

public void saver_click()
{

    if (ss_saver != null)
        ss_saver.saver_click();
}

public void ss_control(int op)
{

    switch (op) {

    case C.SS_OP_INIT:
        //
        //  onCreateOptionsMenu will do this on startup but we need to
        //    do it here in case the settings change at run time
        //
        if (menu_bar != null) {
            MenuItem icon = menu_bar.findItem(R.id.action_saver);
            icon.setVisible(ss_info.enable);
        }

        if (!ss_info.enable) {
            ss_saver = null;
            return;
        }

        if (ss_saver == null) {
            ProgressBar pb = (ProgressBar)findViewById(R.id.main_progress);
            pb.setVisibility(View.VISIBLE);
            pause_threads(true);
            ss_saver = new ScreenSaver((View)findViewById(R.id.scroll_view), (View)findViewById(R.id.saver_view1), (View)findViewById(R.id.saver_view2), (Activity)this, new SS_Callbacks() {
                @Override
                public ScreenInfo ss_start()
                {
                    Log.d("DDD-SS", "screen saver start");
                    pause_threads(true);

                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toolbar tb = (Toolbar) findViewById(R.id.toolbar);
                            tb.setTitle("AtHome");

                            MenuItem icon = menu_bar.findItem(R.id.action_saver);
                            icon.setIcon(R.drawable.ss_play);
                            menu_icons(false);
                            View v = (View)findViewById(R.id.scroll_view);
                            v.setAlpha(1.0f);
                        }
                    });

                    return ss_info;
                }

                @Override
                public void ss_stop()
                {
                    Log.d("DDD-SS", "screen saver stop");
                    pause_threads(false);
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toolbar tb = (Toolbar) findViewById(R.id.toolbar);
                            tb.setTitle("AtHome");
                            MenuItem icon = menu_bar.findItem(R.id.action_saver);
                            icon.setIcon(R.drawable.ss_monitor);
                            menu_icons(true);

                            display(screen);
                        }
                    });
                }

                @Override
                public void ss_toolbar(final String from, final int mode)
                {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            if (from != null) {
                                Toolbar tb = (Toolbar) findViewById(R.id.toolbar);
                                tb.setTitle(from);
                            }

                            if (mode != 0) {
                                MenuItem icon = menu_bar.findItem(R.id.action_saver);
                                icon.setIcon(mode);
                            }
                        }
                    });
                }

                @Override
                public void ss_inited()
                {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            ProgressBar pb = (ProgressBar)findViewById(R.id.main_progress);
                            pb.setVisibility(View.GONE);
                        }
                    });
                    pause_threads(false);
                }
            });
        }
        break;

    case C.SS_OP_RESET:
        if (ss_saver != null)
            ss_saver.screen_saver(ScreenSaver.SAVER_RESET);
        break;

    case C.SS_OP_BLOCK:
        if (ss_saver != null)
            ss_saver.screen_saver(ScreenSaver.SAVER_BLOCK);
        break;

    case C.SS_OP_UPDATE:
        if (ss_saver != null)
            ss_saver.screen_saver(ScreenSaver.SAVER_UPDATE);
        break;

    }
}

public void stream_log(String line)
{

    if (P.get_string("log_uri").equals(""))
        return;
    call_api_nolog("GET", P.get_string("log_uri"), P.get_string("log_params") + URLEncoder.encode(line), "", null);
}

/*
 *  call an HTTP(S) uri to get a response.  Note that this will return
 *      a string with the `entire` response.  If the API returns a lot
 *      of data you might be better off using the open_url/read_url/close_url
 *      interface available right below this one.
 */
public Tuple<String> call_api_nolog(String type, String uri, String params, String auth, String body)
{
    HttpURLConnection con = null;
    String res = "";
    String except = null;

    if (!params.equals(""))
        uri = uri + "?" + params;
    try {
        URL url = new URL(uri);
        con = (HttpURLConnection) url.openConnection();
        con.setConnectTimeout(1000);
        con.setReadTimeout(1000);
        if (type.equals("POST"))
            con.setDoOutput(true);
        if (!auth.equals(""))
            con.setRequestProperty("Authorization", auth);
        if (body != null) {
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoInput(true);
            OutputStream os = con.getOutputStream();
            BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            wr.write(body);
            wr.flush();
            wr.close();
        }
        InputStreamReader in = new InputStreamReader(con.getInputStream());
        BufferedReader inp = new BufferedReader (in);
        StringBuilder response = new StringBuilder();
        for (String line; (line = inp.readLine()) != null; )
            response.append(line).append('\n');
        res = response.toString();
    } catch (java.net.SocketTimeoutException e) {
        except = e.toString();
        res = "";
    } catch (Exception e) {
        except = e.toString();
        res = "";
    } finally {
        if (con != null)
            con.disconnect();
    }
    return new Tuple<String>(except, res);
}

/*
 *  call an HTTP(S) uri to get a response and log the request & response
 */
public String call_api(String type, String uri, String params, String auth, String body)
{
    HttpURLConnection con = null;

    Tuple<String>res = call_api_nolog(type, uri, params, auth, body);
    Log.s(type + " - " + uri + ", params - " + params + ", auth - " + auth +
        ", body - " + body + " ==> " +
        ((res.first() == null) ?  res.second() : "[" + res.first() + "]"),
        this);
    return res.second();
}

public BufferedReader open_url(String url, String auth)
{
    URL server;
    InputStreamReader in_rdr;
    BufferedReader inp;

    try {
        Log.d("get data from " + url);
        server = new URL(url);
        HttpURLConnection url_con = (HttpURLConnection) server.openConnection();
        in_rdr = new InputStreamReader(url_con.getInputStream());
        inp = new BufferedReader (in_rdr);
    } catch (Exception e) {
        Log.d("open_url failed - " + e);
        return null;
    }
    return inp;
}

public String read_url(BufferedReader inp)
{
    String line = null;

    try {
        line = inp.readLine();
    } catch (Exception e) {
        Log.d("read_url failed - " + e);
        return null;
    }
    return line;
}

public void close_url(BufferedReader inp)
{

    try {
        inp.close();
    } catch (Exception e) {
        Log.d("close_url failed - " + e);
    }
}

public void show_log()
{
    String line;
    TextView tv;

    Log.d("Show log");
    setContentView(R.layout.log);

    Button bt = findViewById(R.id.log_return);
    bt.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            setContentView((P.get_int("general_layout") == Popup.LAYOUT_TABLET) ?
                                                                R.layout.activity_tab :
                                                                R.layout.activity_ph);
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            doit();
        }
    });

    bt = findViewById(R.id.log_clear);
    bt.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.clear();
            setContentView((P.get_int("general_layout") == Popup.LAYOUT_TABLET) ?
                                                                R.layout.activity_tab :
                                                                R.layout.activity_ph);
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            doit();
        }
    });

    TableLayout tl = (TableLayout) findViewById(R.id.log_table);
    tl.removeAllViews();
    TableLayout.LayoutParams params = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
    params.setMargins(0, 0, 0, 0); /* left, top, right, bottom */
    int max = Log.size();
    for (int i = 0; i < max; i++) {
        line = Log.get(i);
        View v = View.inflate(this, R.layout.log_line, null);
        v.setTag(i);
        v.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View lv) {
                int i = (int)lv.getTag();
                String l = Log.get(i);
                popup.log_detail_dialog(i, l);
            }
        });
        tv = (TextView) v.findViewById(R.id.line_no);
        tv.setText(i + ":");
        tv = (TextView) v.findViewById(R.id.line_text);
        int len = (line.length() < Log.LOG_BRIEF) ? line.length() : Log.LOG_BRIEF;
        tv.setText(line.substring(0, len));
        tl.addView(v, params);
    }
}

private String cfg_param(String key)
{

    return key + ": " + P.get(key, "") + "\n";
}

private void set_cfg(String cfg)
{
    String key, value;

    Log.d("Set new configuration");
    try {
        JSONObject json = new JSONObject(cfg);
        for (Iterator itr = json.keys(); itr.hasNext();) {
            key = (String)itr.next();
            value = json.getString(key);
            P.put(key, value);
        }
    } catch (Exception e) {
        Log.d("JSON format error " + e);
    }
    Log.cfg(P.get_int("debug"), P.get_string("log_uri"), P.get_string("log_params"));
}

public void remote_doit(String type, String url, String cfg)
{

    P.put("remote_server", url);
    if (type.equals(CONFIG_SAVE)) {
        call_api("POST", url + CONFIG_URI, type, "", cfg);
    } else {
        String resp = call_api("GET", url + CONFIG_URI, type, "", null);
        final boolean ok = (resp.length() > 1);
        if (ok)
            set_cfg(resp.substring(1));
        runOnUiThread(new Runnable() {
            public void run() {
                setContentView((P.get_int("general_layout") == Popup.LAYOUT_TABLET) ?
                                                                    R.layout.activity_tab :
                                                                    R.layout.activity_ph);
                Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
                setSupportActionBar(toolbar);
                if (!ok)
                    toast("No config - bad host name?");
                doit();
            }
        });
    }
}

public void show_cfg()
{
    String line;
    TextView tv;

    Log.d("Show cfg");
    setContentView((P.get_int("general_layout") == Popup.LAYOUT_TABLET) ?
                                                            R.layout.activity_tab :
                                                            R.layout.activity_ph);

    Button bt = findViewById(R.id.cfg_return);
    bt.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            setContentView((P.get_int("general_layout") == Popup.LAYOUT_TABLET) ?
                                                                R.layout.activity_tab :
                                                                R.layout.activity_ph);
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            doit();
        }
    });

    final EditText et = (EditText) findViewById(R.id.cfg_table);
    bt = findViewById(R.id.cfg_set);
    bt.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            set_cfg(et.getText().toString());
            setContentView((P.get_int("general_layout") == Popup.LAYOUT_TABLET) ?
                                                                R.layout.activity_tab :
                                                                R.layout.activity_ph);
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            doit();
        }
    });

    bt = findViewById(R.id.cfg_save);
    bt.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            popup.remote_server(CONFIG_SAVE, et.getText().toString());
        }
    });

    bt = findViewById(R.id.cfg_load);
    bt.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            popup.remote_server(CONFIG_LOAD, "");
        }
    });

    JSONObject cfg = new JSONObject();
    try {
        cfg.put("debug", P.get("debug", ""));
        cfg.put("egauge_clock", P.get("egauge_clock", ""));
        cfg.put("egauge_layout", P.get("egauge_layout", ""));
        cfg.put("general_layout", P.get("general_layout", ""));
        cfg.put("ss_enable", P.get("ss_start", ""));
        cfg.put("ss_start", P.get("ss_start", ""));
        cfg.put("ss_delay", P.get("ss_delay", ""));
        cfg.put("ss_fade", P.get("ss_fade", ""));
        cfg.put("ss_host", P.get("ss_host", ""));
        cfg.put("ss_list", P.get("ss_list", ""));
        cfg.put("ss_server", P.get("ss_server", ""));
        cfg.put("ss_user", P.get("ss_user", ""));
        cfg.put("ss_pwd", P.get("ss_pwd", ""));
        cfg.put("outlets_cols", P.get("outlets_cols", ""));
        cfg.put("outlets_batt_level", P.get("outlets_batt_level", ""));
        cfg.put("outlets_batt_max", P.get("outlets_batt_max", ""));
        cfg.put("outlets_batt_min", P.get("outlets_batt_min", ""));
        cfg.put("thermostat_layout", P.get("thermostat_layout", ""));
        cfg.put("weather_layout", P.get("weather_layout", ""));
        cfg.put("ecobee_api", P.get("ecobee_api", ""));
        cfg.put("egauge_url", P.get("egauge_url", ""));
        cfg.put("log_params", P.get("log_params", ""));
        cfg.put("log_uri", P.get("log_uri", ""));
        cfg.put("outlets_battery", P.get("outlets_battery", ""));
        cfg.put("pref_version", P.get("pref_version", ""));
        cfg.put("remote_server", P.get("remote_server", ""));
        cfg.put("tplink_pwd", P.get("tplink_pwd", ""));
        cfg.put("tplink_user", P.get("tplink_user", ""));
        cfg.put("wunder_id", P.get("wunder_id", ""));
        cfg.put("wunder_key", P.get("wunder_key", ""));
        cfg.put("x10_jwt", P.get("x10_jwt", ""));
        cfg.put("x10_url", P.get("x10_url", ""));
        et.setText(cfg.toString(2));
    } catch (Exception e) {
        Log.d("JSON error " + e);
    }
}

public void go_log_return()
{

    Log.d("Log return");
    setContentView((P.get_int("general_layout") == Popup.LAYOUT_TABLET) ?
                                                        R.layout.activity_tab :
                                                        R.layout.activity_ph);
}

public void set_timeout(ImageView v, int p, int maxp)
{
    int id = R.drawable.timeout_00;

    int i = (p * 10) / maxp;
    switch (i) {

    case 0:
        id = R.drawable.timeout_00;
        break;

    case 1:
        id = R.drawable.timeout_01;
        break;

    case 2:
        id = R.drawable.timeout_02;
        break;

    case 3:
        id = R.drawable.timeout_03;
        break;

    case 4:
        id = R.drawable.timeout_04;
        break;

    case 5:
        id = R.drawable.timeout_05;
        break;

    case 6:
        id = R.drawable.timeout_06;
        break;

    case 7:
        id = R.drawable.timeout_07;
        break;

    case 8:
        id = R.drawable.timeout_08;
        break;

    case 9:
        id = R.drawable.timeout_09;
        break;

    case 10:
        id = R.drawable.timeout_10;
        break;

    }
    v.setImageResource(id);
}

public int get_battery()
{
    String chg;

    if (P.get_int("outlets_batt_level") != 0)
        return P.get_int("outlets_batt_level");

    IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
    Intent bs = this.registerReceiver(null, ifilter);

    int level = bs.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
    int scale = bs.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
    return (level * 100) / scale;
}

public void go_temp_detail(View v)
{

    weather.go_temp_detail(v);
}

public void go_hold(View v)
{

    thermostat.go_hold(v);
}

private void clock()
{

    Calendar cal = Calendar.getInstance();
    int time = (cal.get(Calendar.HOUR_OF_DAY) * 100) + cal.get(Calendar.MINUTE);
    if (P.get_int("general_off") >= 0 && time == P.get_int("general_off") && screen)
        display(false);
    else if (P.get_int("general_on") >= 0 && time == P.get_int("general_on") && !screen)
        display(true);

    final ClockView cv = (ClockView)findViewById(R.id.clock_view);
    if (cv == null)
        return;
    runOnUiThread(new Runnable() {
        public void run() {
            cv.update();
        }
    });
}

private void sh_rain()
{

Log.d("DDD-SS", "set rain to --");
    TextView tv = (TextView)findViewById(R.id.weather_rain);
    tv.setText("--");
tv.invalidate();
}

private void pause_threads(boolean p)
{

    paused = p;
    if (egauge != null)
        egauge.pause(p);
    if (weather != null)
        weather.pause(p);
    if (thermostat != null)
        thermostat.pause(p);
    if (outlets != null)
        outlets.pause(p);
}

private void doit()
{

    final View egauge_view = view_show(P.get_int("egauge_layout"), R.id.egauge_main);
    ClockView cv = (ClockView)findViewById(R.id.clock_view);
    if (cv != null) {
        ImageView h_img = (ImageView)findViewById(R.id.house_image);
        h_img.setVisibility(P.get_boolean("egauge_clock") ? View.GONE :    View.VISIBLE);
        cv.setVisibility(P.get_boolean("egauge_clock")    ? View.VISIBLE : View.GONE);
    }
    if (egauge_view != null)
        egauge = new Egauge(this, new DoitCallback() {
            @Override
            public void doit(Object obj) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        egauge.show(egauge_view);
                    }
                });
            }
        });

    final View weather_view = view_show(P.get_int("weather_layout"), R.id.weather_main);
    if (weather_view != null)
        weather = new Weather(this, new DoitCallback() {
            @Override
            public void doit(Object obj) {
                runOnUiThread(new Runnable() {
                    public void run() {
sh_rain();
                        weather.show(weather_view);
                    }
                });
            }
        });

    final View thermostat_view = view_show(P.get_int("thermostat_layout"), R.id.thermostats_table);
    if (thermostat_view != null)
        thermostat = new Thermostat(this, thermostat_view, new DoitCallback() {
            @Override
            public void doit(Object obj) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        thermostat.show(thermostat_view);
                    }
                });
            }
        });

    final View outlets_view = view_show(P.get_int("outlets_layout"), R.id.outlets_table);
    if (outlets_view != null)
        outlets = new Outlets(this, outlets_view, new DoitCallback() {
            @Override
            public void doit(Object obj) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        outlets.show(outlets_view);
                    }
                });
            }
        });

    pause_threads(false);

    new Thread(new Runnable() {
        public void run() {
            for (;;) {
                if (!paused)
                    clock();
                SystemClock.sleep(1000);
            }
        }
    }).start();
}

}
