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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
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
import com.n0ano.athome.SS.SS_Callbacks;

public class MainActivity extends AppCompatActivity
{

private boolean paused = false;

public Menu menu_bar;

Http http = new Http();
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
    P_SS.init(getSharedPreferences(P_SS.PREF_NAME, Context.MODE_PRIVATE));
    Log.cfg(P.get_int("debug"), "", "");
Log.cfg(1, "", "");
    Log.d("MainActivity: onCreate");

    setContentView((P.get_int("general:layout") == Popup.LAYOUT_TABLET) ?
                                                        R.layout.activity_tab :
                                                        R.layout.activity_ph);

    Log.cfg(P.get_int("debug"), P.get_string("log:uri"), P.get_string("log:params"));
Log.cfg(1, "", "");

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

    ss_gesture = new GestureDetectorCompat(this, new MyGesture(this));

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

    ss_control(C.SS_OP_STOP);

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
    icon = menu_bar.findItem(R.id.action_progress);
    icon.setVisible(false);

    menu_bar.findItem(R.id.action_alerts).setVisible(false);

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
        Log.d("DDD-SS", "activity result - " + P.get_string("images:" + ss_info.list, ""));
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
    MenuItem icon;

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

public void fling(int dir)
{

    if (ss_saver != null)
        ss_saver.saver_fling(dir < 0 ? 1 : -1);
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

    ss_control(C.SS_OP_STOP);
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

public void action_click()
{

    if (ss_saver != null)
        ss_saver.action_click();
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
                        icon.setVisible(false);
                        icon = menu_bar.findItem(R.id.action_progress);
                        icon.setVisible(true);

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
                pause_threads(false);
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toolbar tb = (Toolbar) findViewById(R.id.toolbar);
                        tb.setTitle("AtHome");
                        MenuItem icon = menu_bar.findItem(R.id.action_saver);
                        icon.setIcon(R.drawable.ss_monitor);
                        icon.setVisible(true);
                        menu_icons(true);

                        View v = (View)findViewById(R.id.scroll_view);
                        v.setScaleX(1.0f);
                        v.setScaleY(1.0f);
                        v.setAlpha(1.0f);
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

                        MenuItem icon = menu_bar.findItem(R.id.action_saver);
                        switch (mode) {

                        case ScreenSaver.TB_PAUSE:
                            icon.setIcon(R.drawable.ss_pause);
                            break;

                        case ScreenSaver.TB_PLAY:
                            icon.setIcon(R.drawable.ss_play);
                            icon.setVisible(true);
                            icon = menu_bar.findItem(R.id.action_progress);
                            icon.setVisible(false);
                            break;

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
        break;

    case C.SS_OP_RESET:
        if (ss_saver != null)
            ss_saver.screen_saver(ScreenSaver.SAVER_RESET);
        break;

    case C.SS_OP_BLOCK:
        if (ss_saver != null)
            ss_saver.screen_saver(ScreenSaver.SAVER_BLOCK);
        break;

    case C.SS_OP_STOP:
        if (ss_saver != null) {
            ss_saver.screen_saver(ScreenSaver.SAVER_STOP);
            ss_saver = null;
        }
        break;

    case C.SS_OP_UPDATE:
        if (ss_saver != null)
            ss_saver.screen_saver(ScreenSaver.SAVER_UPDATE);
        break;

    }
}

//  Need to re-design how to do this (e.g. stream the logging
//    lines to an external server
//
//public void stream_log(String line)
//{
//
//    if (P.get_string("general:log_uri").equals(""))
//        return;
//    call_api_nolog("GET", P.get_string("general:log_uri"), P.get_string("general:log_params") + URLEncoder.encode(line), "", null);
//}

public void show_log()
{
    String line;
    TextView tv;

    Log.d("Show log");
    View vv = (View)findViewById(R.id.scroll_view);
    vv.setVisibility(View.GONE);
    vv = (View)findViewById(R.id.log_view);
    vv.setVisibility(View.VISIBLE);

    Button bt = findViewById(R.id.log_return);
    bt.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            View vv = (View)findViewById(R.id.scroll_view);
            vv.setVisibility(View.VISIBLE);
            vv = (View)findViewById(R.id.log_view);
            vv.setVisibility(View.GONE);
        }
    });

    bt = findViewById(R.id.log_clear);
    bt.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.clear();
            View vv = (View)findViewById(R.id.scroll_view);
            vv.setVisibility(View.VISIBLE);
            vv = (View)findViewById(R.id.log_view);
            vv.setVisibility(View.GONE);
        }
    });

    ArrayAdapter adapt = new LogAdapter(this, Log.size());
    ListView lv = (ListView)findViewById(R.id.log_list);
    lv.setAdapter(adapt);
    lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        @Override                               
        public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
            String l = Log.get(pos);   
            popup.log_detail_dialog(pos, l);
        }                                 
    });
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
    Log.cfg(P.get_int("debug"), P.get_string("general:log_uri"), P.get_string("general:log_params"));
}

public void remote_doit(final String host, final String type, final String cfg)
{

    new Thread(new Runnable() {

        public void run() {
            if (type.equals(C.CONFIG_SAVE))
                http.call_api("POST", host + C.CONFIG_URI, type, "", cfg);
            else {
                Http.R resp = http.call_api(host + C.CONFIG_URI, type);
                if (resp.code == Http.OK) {
                    C.new_cfg(resp.body.substring(1));
                    restart();
                }
            }
        }
    }).start();
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

    if (P.get_int("outlets:batt_level") != 0)
        return P.get_int("outlets:batt_level");

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
    if (P.get_int("general:off") >= 0 && time == P.get_int("general:off") && screen)
        display(false);
    else if (P.get_int("general:on") >= 0 && time == P.get_int("general:on") && !screen)
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

public void alerts_ack()
{

    if (egauge != null)
        P.put("egauge:ts", egauge.alerts_ack());
    sh_alerts();
}

public String alerts_str()
{
    int pri;
    Alert alert;

    if (egauge == null || egauge.alerts_count() <= 0)
        return "";

    pri = egauge.alerts_item(0).pri;
    String str = "";
    int max = egauge.alerts_count();
    for (int i = 0; i < max; ++i) {
        alert = egauge.alerts_item(i);
        if (alert.pri != pri) {
            str += "\n";
            pri = alert.pri;
        }
        str += alert.pri + ":";
        str += C.epoch_str(alert.ts);
        str += " - " + alert.name;
        if (alert.detail != null)
            str += "(" + alert.detail + ")";
        str += "\n";
    }
    return str;
}

private void sh_alerts()
{
    boolean vis = false;

    if (egauge != null && egauge.alerts_count() > 0)
        vis = true;
    if (menu_bar != null)
        menu_bar.findItem(R.id.action_alerts).setVisible(vis);
}

public void weather_search(final String name, final String key, final DoitCallback cb)
{

    new Thread(new Runnable() {
        public void run() {
            weather.search(name, key, new DoitCallback() {
                @Override
                public void doit(final int res, final Object obj) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            cb.doit(res, obj);
                        }
                    });
                }
            });
        }
    }).start();
}

private void doit()
{

    final View egauge_view = view_show(P.get_int("egauge:layout"), R.id.egauge_main);
    ClockView cv = (ClockView)findViewById(R.id.clock_view);
    if (cv != null) {
        ImageView h_img = (ImageView)findViewById(R.id.house_image);
        h_img.setVisibility(P.get_boolean("egauge:clock") ? View.GONE :    View.VISIBLE);
        cv.setVisibility(P.get_boolean("egauge:clock")    ? View.VISIBLE : View.GONE);
    }
    if (egauge_view != null)
        egauge = new Egauge(egauge_view, P.get_long("egauge:ts"), new DoitCallback() {
            @Override
            public void doit(int res, Object obj) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        egauge.show(egauge_view);
                        sh_alerts();
                    }
                });
            }
        });

    final View weather_view = view_show(P.get_int("weather:layout"), R.id.weather_main);
    if (weather_view != null) {
            weather_view.setOnTouchListener(new OnSwipeTouchListener(this) {
                @Override
                public void onSwipeLeft() {
                    weather.cycle(1, weather_view);
                }

                @Override
                public void onSwipeRight() {
                    weather.cycle(-1, weather_view);
                }
            });
        weather = new Weather(popup, new DoitCallback() {
            @Override
            public void doit(int res, Object obj) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        weather.show(weather_view);
                    }
                });
            }
        });
    }

    final View thermostat_view = view_show(P.get_int("thermostat:layout"), R.id.thermostats_table);
    if (thermostat_view != null)
        thermostat = new Thermostat(this, thermostat_view, popup, new DoitCallback() {
            @Override
            public void doit(int res, Object obj) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        thermostat.show(thermostat_view,
                                        getResources().getColor(R.color.fore),
                                        getResources().getColor(R.color.hold));
                    }
                });
            }
        });

    final View outlets_view = view_show(P.get_int("outlets:layout"), R.id.outlets_table);
    if (outlets_view != null)
        outlets = new Outlets(this, outlets_view, new DoitCallback() {
            @Override
            public void doit(int res, Object obj) {
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
