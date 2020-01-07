package com.n0ano.athome;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

public class MainActivity extends AppCompatActivity
{

public final static int BATTERY_LOW  = 20;
public final static int BATTERY_HIGH = 90;
public final static int OUTLETS_COLS = 3;

private final static String CONFIG_URI = "/cgi-bin/athome/config";
private final static String CONFIG_LOAD =  "load";
private final static String CONFIG_SAVE =  "save";

Preferences pref;

Menu menu_bar;

public int general_layout;

public int egauge_layout;
public int egauge_progress;
public boolean egauge_clock;
public String egauge_url;

public int weather_layout;
public int weather_progress;

public String weather_id;

public int thermostat_layout;
public String ecobee_api;
public String ecobee_refresh;
public String ecobee_access;

private String url;

int degree = 0;
int debug = 0;

String log_uri = "";
String log_params = "";

String x10_url;
String x10_jwt;

String tplink_user;
String tplink_pwd;

String outlets_battery = "";
int outlets_cols;
int outlets_batt_min;
int outlets_batt_max;
int outlets_batt_level;

Weather weather;
Thermostat thermostat;
Egauge egauge;
Outlets outlets;

boolean running;

public Parse parse = new Parse();
public Popup popup;

private boolean screen = true;
private int screen_bright = -1;
private int ss_counter = 0;
private int ss_state = Common.SAVER_PAUSE;
private int ss_duration = 2000;
private int ss_offset = 0;
private int ss_viewid = 0;
private View[] ss_views = new View[2];

private Animation ss_fadein;
private Animation ss_fadeout;

public int ss_start = 0;        // seconds, 0 = none
public int ss_delay = 0;        // seconds
public int ss_fade = 0;

public String ss_host = "";
public String ss_list = "";
public String ss_server = "";
public String ss_user = "";
public String ss_pwd = "";

public int on_time = -1;        // (hour * 100) + minute, -1 = none
public int off_time = -1;       // (hour * 100) + minute, -1 = none

@Override
protected void onCreate(Bundle state)
{

    super.onCreate(state);

    setContentView(R.layout.activity_main);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
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
    pref = new Preferences(this);
    debug = pref.get("debug", 0);
        Log.cfg(debug, "", "");

    popup = new Popup(this, pref);

    ss_views[0] = (View) findViewById(R.id.saver_view1);
    ss_views[1] = (View) findViewById(R.id.saver_view2);
    ss_fadein = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
    ss_fadeout = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out);

    display(true);

    Log.d("MainActivity: onCreate");
}

@Override
public boolean dispatchTouchEvent(MotionEvent ev)
{

    if (ev.getY() < ss_offset)
        return super.dispatchTouchEvent(ev);
    if (ev.getAction() == MotionEvent.ACTION_DOWN) {
        boolean eat = (ss_state == Common.SAVER_SHOW);
        screen_saver(Common.SAVER_RESET);
        if (eat)
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

//test_parse();

    restore_state();
    doit();
}

@Override
protected void onPause()
{

    super.onPause();
    this.running = false;
    Log.d("MainActivity: onPause");
}

@Override
protected void onStop()
{

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

    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    menu_bar = menu;
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

public void start_browser(String uri)
{

    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
    startActivity(intent);
}

public void set_progress()
{
    ImageView iv;

    if (egauge_layout != Popup.LAYOUT_NONE) {
        if ((iv = (ImageView) findViewById(R.id.egauge_timeout)) != null)
            if (egauge_progress > 0)
                set_timeout(iv, egauge.period, Egauge.PERIOD);
            else
                iv.setVisibility(View.GONE);
    }

    if (weather_layout != Popup.LAYOUT_NONE) {
        if ((iv = (ImageView) findViewById(R.id.weather_timeout)) != null)
            if (weather_progress > 0)
                set_timeout(iv, weather.period, Weather.PERIOD);
            else
                iv.setVisibility(View.GONE);
    }
}

public void display_toggle(View V)
{

    display(!screen);
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
                Toast.makeText(getApplicationContext(), "Can't change brightness - permissions?", Toast.LENGTH_LONG).show();
            }
            View view = (View) findViewById(R.id.scroll_view);
            view.setVisibility(onoff ? View.VISIBLE : View.GONE);
            view.setAlpha(1.0f);
            view = (View) findViewById(R.id.toolbar);
            view.setVisibility(onoff ? View.VISIBLE : View.GONE);
            view = (View) findViewById(R.id.blank_view);
            view.setVisibility(onoff ? View.GONE : View.VISIBLE);
            ss_views[0].setVisibility(View.GONE);
            ss_views[0].setAlpha(0.0f);
            ss_views[1].setVisibility(View.GONE);
            ss_views[1].setAlpha(0.0f);
            if (menu_bar != null) {
                MenuItem icon = menu_bar.findItem(R.id.action_display);
                icon.setIcon(onoff ? R.drawable.light_on : R.drawable.light_off);
            }
        }
    });
}

//
// Cross fade from start to end
//
private void ss_xfade(final View start, final View end)
{

    end.setAlpha(0.0f);
    end.setVisibility(View.VISIBLE);
    end.animate()
       .alpha(1.0f)
       .setDuration(ss_duration)
       .setListener(null);

    start.animate()
         .alpha(0.0f)
         .setDuration(ss_duration)
         .setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    start.setVisibility(View.GONE);
                }
            });
}

private void ss_fadeup(final View start, final View end)
{

    end.setTranslationY(ss_height());
    end.setVisibility(View.VISIBLE);
    end.animate()
       .translationYBy(-ss_height())
       .setDuration(ss_duration)
       .setListener(null);

    start.animate()
         .translationYBy(-ss_height())
         .setDuration(ss_duration)
         .setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    start.setTranslationY(0);
                    start.setVisibility(View.GONE);
                }
            });
}

public void do_fade(View start, View end)
{

    ss_xfade(start, end);
    //ss_fadeup(start, end);
}

private int ss_width()
{

    View v = (View)findViewById(R.id.scroll_view);
    return v.getWidth();
}

private int ss_height()
{

    View v = (View)findViewById(R.id.scroll_view);
    return v.getHeight();
}

private void saver_fade()
{

    int old = ss_viewid;
    ss_viewid ^= 1;
    //
    //  ImageGet will call do_fade once the new image is loaded
    //
    ImageGet ig = new ImageGet(this, ss_server, ss_list, ss_user, ss_pwd, ss_width(), ss_height(), ss_views[old], ss_views[ss_viewid]);
    ig.start();
}

public void saver_start()
{

Log.d("saver: start");
    ss_state = Common.SAVER_SHOW;
    ss_counter = ss_delay;
    ss_views[0].setVisibility(View.GONE);
    ss_views[0].setAlpha(1.0f);
    ss_views[0].setScaleX(1.0f);
    ss_views[0].setScaleY(1.0f);
    ss_views[1].setVisibility(View.GONE);
    ss_views[1].setAlpha(1.0f);
    ss_views[1].setScaleX(1.0f);
    ss_views[1].setScaleY(1.0f);
    ss_viewid = 0;
    //
    //  ImageGet will call do_fade once the new image is loaded
    //
    ImageGet ig = new ImageGet(this, ss_server, ss_list, ss_user, ss_pwd, ss_width(), ss_height(), (View)findViewById(R.id.scroll_view), ss_views[ss_viewid]);
    ig.start();
}

public void screen_saver(int tick)
{

    switch (tick) {

    case Common.SAVER_PAUSE:
Log.d("saver paused");
        ss_state = Common.SAVER_PAUSE;
        break;

    case Common.SAVER_TICK:
        if (ss_state != Common.SAVER_PAUSE) {
            if (--ss_counter == 0) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        ss_counter = ss_delay;
                        if (ss_state == Common.SAVER_SHOW)
                            saver_fade();
                        else
                            saver_start();
                    }
                });
            }
        }
        break;

    case Common.SAVER_RESET:
Log.d("saver - reset to " + ss_start + " seconds, state - " + ss_state);
        if (ss_state == Common.SAVER_SHOW)
            display(screen);
        ss_counter = ss_start;
        ss_state = ((ss_start == 0) ? Common.SAVER_PAUSE : Common.SAVER_RUN);
        break;

    }
}

public void view_show(int view_id, int[] ids, int main)
{
    int id;

    if (view_id > ids.length)
        view_id = 0;
    id = ids[view_id];
    View v = findViewById(main);
    if (v == null)
        return;
    ViewGroup parent = (ViewGroup) v.getParent();
    int index = parent.indexOfChild(v);
    parent.removeView(v);
    v = getLayoutInflater().inflate(id, parent, false);
    parent.addView(v, index);
    set_progress();
}

public void show_views()
{

    view_show(egauge_layout, Popup.layout_egauge, R.id.egauge_main);
    final ClockView cv = (ClockView)findViewById(R.id.clock_view);
    if (cv != null) {
        ImageView h_img = (ImageView)findViewById(R.id.house_image);
        h_img.setVisibility(egauge_clock ? View.GONE :    View.VISIBLE);
        cv.setVisibility(egauge_clock    ? View.VISIBLE : View.GONE);
    }

    view_show(weather_layout, Popup.layout_weather, R.id.weather_main);

    view_show(thermostat_layout, Popup.layout_thermostat, R.id.thermostat_main);
}

public String encode_time(int t)
{

    if (t < 0)
        return "";
    int hr = t / 100;
    t -= hr * 100;
    return String.valueOf(hr) + ":" + String.format("%02d", t);
}
public int decode_time(String t)
{

    if (t.isEmpty())
        return -1;
    int idx = t.indexOf(":");
    if (idx < 0)
        return Integer.parseInt(t);
    else
        return (Integer.parseInt(t.substring(0, idx)) * 100) + Integer.parseInt(t.substring(idx + 1));
}

private void restore_state()
{

    general_layout = pref.get("general_layout", Popup.LAYOUT_TABLET);
    on_time = pref.get("general_on", -1);
    off_time = pref.get("general_off", -1);

    ss_start = pref.get("ss_start", 0);
    ss_delay = pref.get("ss_delay", 0);
    ss_fade = pref.get("ss_fade", 0);
    ss_host = pref.get("ss_host", "");
    ss_list = Common.suffix(ss_host);
    ss_server = pref.get("ss_server", "");
    ss_user = pref.get("ss_user", "");
    ss_pwd = pref.get("ss_pwd", "");
    screen_saver(Common.SAVER_RESET);

    egauge_layout = pref.get("egauge_layout", Popup.LAYOUT_TABLET);
    egauge_progress = pref.get("egauge_progress", 1);
    egauge_url = pref.get("egauge_url", "");
    egauge_clock = pref.get("egauge_clock", false);

    weather_layout = pref.get("weather_layout", Popup.LAYOUT_TABLET);
    weather_progress = pref.get("weather_progress", 1);

    weather_id = pref.get("wunder_id", "");

    thermostat_layout = pref.get("thermostat_layout", Popup.LAYOUT_TABLET);
    ecobee_api = pref.get("ecobee_api", "");
    ecobee_access = pref.get("ecobee_access", "");
    ecobee_refresh = pref.get("ecobee_refresh", "");

    outlets_battery = pref.get("outlets_battery", "");
    outlets_cols = pref.get("outlets_cols", OUTLETS_COLS);
    outlets_batt_min = pref.get("outlets_batt_min", BATTERY_LOW);
    outlets_batt_max = pref.get("outlets_batt_max", BATTERY_HIGH);
    outlets_batt_level = pref.get("outlets_batt_level", 0);

    x10_url = pref.get("x10_url", "");
    x10_jwt = pref.get("x10_jwt", "none");

    tplink_user = pref.get("tplink_user", "");
    tplink_pwd = pref.get("tplink_pwd", "");

    log_uri = pref.get("log_uri", "");
    log_params = pref.get("log_params", "");

    debug = pref.get("debug", 0);
    Log.cfg(debug, log_uri, log_params);
}

private void test_parse()
{

    String xml =
"<conds><KCOBOULD238>\n" +
"<page val=\"one\"></page>\n" +
"<totalPages val=\"10\"></totalPages>\n" +
"<pageSize val=\"2\"></pageSise>\n" +
"<page val=\"two\"></page>\n" +
"<totalPages val=\"20\"></totalPages>\n" +
"<pageSize val=\"1234\"></pageSise>\n" +
"<maxtemp val=\"59.0\"></maxtemp>\n" +
"<maxtemp_time val=\"06:15PM\"></maxtemp_time>\n" +
"<mintemp val=\"39.9\"></mintemp>\n" +
"<mintemp_time val=\"02:35PM\"></mintemp_time>\n" +
"</conds>";
    String json =
"{\n" +
"  \"folio\": {\n" +
"    \"page\": \"one\",\n" +
"    \"totalPages\": 10,\n" +
"    \"pageSize\": 2\n" +
"  },\n" +
"  \"folio\": {\n" +
"    \"page\": \"two\",\n" +
"    \"totalPages\": 20,\n" +
"    \"pageSize\": \"1234\"\n" +
"    \"state\":\"0000000000011001\",\n" +
"    \"result\":\"OK\"\n" +
"  },\n" +
" \"house\":[\n" +
"  {\n" +
"   \"code\":\"d\",\n" +
"   \"devices\":[\n" +
"     \"Crystal Lamp\",\n" +
"     \"\",\n" +
"     \"Patio Lights\",\n" +
"     \"Patio Fountain\",\n" +
"     \"AtHome Display\",\n" +
"     \"Don's Office\",\n" +
"     \"Office Fountain\",\n" +
"     \"Office Acquarium\"\n" +
"   ]\n" +
"  },\n" +
"  {\n" +
"   \"code\":\"e\",\n" +
"   \"devices\":[\n" +
"     \"Router\"\n" +
"   ]\n" +
"  }\n" +
" ],\n" +
" \"result\":\"OK\"\n" +
"}";

    Log.d("Parsing test start");
    Log.d("xml - \n" + xml);
    Log.d("json - \n" + json);

    Log.d("xml xyzzy() null = " + parse.xml_get("xyzzy", xml, 1));
    Log.d("xml page(1,2,3) one,two,null = " + parse.xml_get("page", xml) + "," + parse.xml_get("page", xml, 2) + "," + parse.xml_get("page", xml, 3));
    Log.d("xml totalPages(1) 10 = " + parse.xml_get("totalPages", xml));
    Log.d("xml pageSize(1,2) 2,1234 = " + parse.xml_get("pageSize", xml) + "," + parse.xml_get("pageSize", xml, 2));
    Log.d("xml max(1) 59.0 = " + parse.xml_get("maxtemp", xml, 1));
    Log.d("xml min(1) 39.9 = " + parse.xml_get("mintemp", xml, 1));
    Log.d("xml max_time(1) 06:15PM = " + parse.xml_get("maxtemp_time", xml, 1));
    Log.d("xml min_time(1) 02:35PM = " + parse.xml_get("mintemp_time", xml, 1));

    Log.d("json xyzzy() xyzzy = " + parse.json_get("xyzzy", json, 1));
    Log.d("json page(1,2,3) one,two,null = " + parse.json_get("page", json) + "," + parse.json_get("page", json, 2) + "," + parse.json_get("page", json, 3));
    Log.d("json totalPages(1) 10 = " + parse.json_get("totalPages", json));
    Log.d("json pageSize(1,2) 2,1234 = " + parse.json_get("pageSize", json) + "," + parse.json_get("pageSize", json, 2));
    Log.d("json state(1) 0000000000011001 = " + parse.json_get("state", json));

    Log.d("json code(1,2) d,e = " + parse.json_get("code", json) + "," + parse.json_get("code", json, 2));
    Log.d("json devices(1,2) ... = " + parse.json_get("devices", json) + "," + parse.json_get("devices", json, 2));

    Log.d("Parsing test ended");
}

public void stream_log(String line)
{

    if (log_uri.equals(""))
        return;
    call_api_nolog("GET", log_uri, log_params + URLEncoder.encode(line), "", null);
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
    this.running = false;
    setContentView(R.layout.log);

    Button bt = findViewById(R.id.log_return);
    bt.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            setContentView(R.layout.activity_main);
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
            setContentView(R.layout.activity_main);
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

    return key + ": " + pref.get(key, "") + "\n";
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
            pref.put(key, value);
        }
    } catch (Exception e) {
        Log.d("JSON format error " + e);
    }
    restore_state();
}

public void remote_doit(String type, String url, String cfg)
{

    pref.put("remote_server", url);
    if (type.equals(CONFIG_SAVE)) {
        call_api("POST", url + CONFIG_URI, type, "", cfg);
    } else {
        String resp = call_api("GET", url + CONFIG_URI, type, "", null);
        set_cfg(resp.substring(1));
        runOnUiThread(new Runnable() {
            public void run() {
                setContentView(R.layout.activity_main);
                Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
                setSupportActionBar(toolbar);
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
    this.running = false;
    setContentView(R.layout.config);

    Button bt = findViewById(R.id.cfg_return);
    bt.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            setContentView(R.layout.activity_main);
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
            setContentView(R.layout.activity_main);
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
        cfg.put("debug", pref.get("debug", ""));
        cfg.put("egauge_clock", pref.get("egauge_clock", ""));
        cfg.put("egauge_layout", pref.get("egauge_layout", ""));
        cfg.put("egauge_progress", pref.get("egauge_progress", ""));
        cfg.put("general_layout", pref.get("general_layout", ""));
        cfg.put("outlets_cols", pref.get("outlets_cols", ""));
        cfg.put("outlets_batt_level", pref.get("outlets_batt_level", ""));
        cfg.put("outlets_batt_max", pref.get("outlets_batt_max", ""));
        cfg.put("outlets_batt_min", pref.get("outlets_batt_min", ""));
        cfg.put("thermostat_layout", pref.get("thermostat_layout", ""));
        cfg.put("weather_layout", pref.get("weather_layout", ""));
        cfg.put("weather_progress", pref.get("weather_progress", ""));
        cfg.put("ecobee_api", pref.get("ecobee_api", ""));
        cfg.put("egauge_url", pref.get("egauge_url", ""));
        cfg.put("log_params", pref.get("log_params", ""));
        cfg.put("log_uri", pref.get("log_uri", ""));
        cfg.put("outlets_battery", pref.get("outlets_battery", ""));
        cfg.put("pref_version", pref.get("pref_version", ""));
        cfg.put("remote_server", pref.get("remote_server", ""));
        cfg.put("tplink_pwd", pref.get("tplink_pwd", ""));
        cfg.put("tplink_user", pref.get("tplink_user", ""));
        cfg.put("wunder_id", pref.get("wunder_id", ""));
        cfg.put("x10_jwt", pref.get("x10_jwt", ""));
        cfg.put("x10_url", pref.get("x10_url", ""));
        et.setText(cfg.toString(2));
    } catch (Exception e) {
        Log.d("JSON error " + e);
    }
}

public void go_log_return()
{

    Log.d("Log return");
    setContentView(R.layout.activity_main);
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

    if (outlets_batt_level != 0)
        return outlets_batt_level;

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

private boolean working()
{

    return this.running;
}

private void clock()
{

    Calendar cal = Calendar.getInstance();
    int time = (cal.get(Calendar.HOUR_OF_DAY) * 100) + cal.get(Calendar.MINUTE);
    if (off_time >= 0 && time == off_time && screen)
        display(false);
    else if (on_time >= 0 && time == on_time && !screen)
        display(true);

    screen_saver(Common.SAVER_TICK);

    final ClockView cv = (ClockView)findViewById(R.id.clock_view);
    if (cv == null)
        return;
    runOnUiThread(new Runnable() {
        public void run() {
            cv.update();
        }
    });
}

private void doit()
{

    final Weather weather = new Weather(this);
    this.weather = weather;

    final Thermostat thermostat = new Thermostat(this);
    this.thermostat = thermostat;

    final Egauge egauge = new Egauge( this);
    this.egauge = egauge;

    final Outlets outlets = new Outlets(this);
    this.outlets = outlets;

    this.running = true;

    show_views();

    new Thread(new Runnable() {
        public void run() {
            while (working()) {

                clock();

                if (weather != null)
                    weather.update();

                if (thermostat != null)
                    thermostat.update();

                if (egauge != null && egauge_layout != 0)
                    egauge.update();

                if (outlets != null)
                    outlets.update();

                SystemClock.sleep(1000);
            }
        }
    }).start();
}

}
