package com.n0ano.athome;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.n0ano.athome.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
{

public final static int BATTERY_LOW  = 20;
public final static int BATTERY_HIGH = 90;

public int general_layout;

public int egauge_layout;
public int egauge_progress;
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

@Override
protected void onCreate(Bundle state)
{

    super.onCreate(state);
    Log.d("MainActivity: onCreate");

    setContentView(R.layout.activity_main);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    popup = new Popup(this);
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

public void view_show(int view_id, int[] ids, int main)
{
    int id;

    if (view_id > ids.length)
        view_id = 0;
    id = ids[view_id];
    View v = findViewById(main);
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
    view_show(weather_layout, Popup.layout_weather, R.id.weather_main);
    view_show(thermostat_layout, Popup.layout_thermostat, R.id.thermostat_main);
}

private void restore_state()
{

    Preferences pref = new Preferences(this);

    general_layout = pref.get_int("general_layout", Popup.LAYOUT_TABLET);

    egauge_layout = pref.get_int("egauge_layout", Popup.LAYOUT_TABLET);
    egauge_progress = pref.get_int("egauge_progress", 1);
    egauge_url = pref.get_string("egauge_url", "");

    weather_layout = pref.get_int("weather_layout", Popup.LAYOUT_TABLET);
    weather_progress = pref.get_int("weather_progress", 1);

    weather_id = pref.get_string("wunder_id", "");

    thermostat_layout = pref.get_int("thermostat_layout", Popup.LAYOUT_TABLET);
    ecobee_api = pref.get_string("ecobee_api", "");
    ecobee_access = pref.get_string("ecobee_access", "");
    ecobee_refresh = pref.get_string("ecobee_refresh", "");

    outlets_battery = pref.get_string("outlets_battery", "");
    outlets_batt_min = pref.get_int("outlets_batt_min", BATTERY_LOW);
    outlets_batt_max = pref.get_int("outlets_batt_max", BATTERY_HIGH);
    outlets_batt_level = pref.get_int("outlets_batt_level", 0);

    x10_url = pref.get_string("x10_url", "");
    x10_jwt = pref.get_string("x10_jwt", "none");

    tplink_user = pref.get_string("tplink_user", "");
    tplink_pwd = pref.get_string("tplink_pwd", "");

    log_uri = pref.get_string("log_uri", "");
    log_params = pref.get_string("log_params", "");

    debug = pref.get_int("debug", 0);
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
public String call_api_nolog(String type, String uri, String params, String auth, String body)
{
    HttpURLConnection con = null;
    String res = "";

    if (!params.equals(""))
        uri = uri + "?" + params;
    try {
        URL url = new URL(uri);
        con = (HttpURLConnection) url.openConnection();
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
    } catch (Exception e) {
        res = "";
    } finally {
        if (con != null)
            con.disconnect();
    }
    return res;
}

/*
 *  call an HTTP(S) uri to get a response and log the request & response
 */
public String call_api(String type, String uri, String params, String auth, String body)
{
    HttpURLConnection con = null;
    String res = "";

    res = call_api_nolog(type, uri, params, auth, body);
    Log.s(type + " - " + uri + ", params - " + params + ", auth - " + auth + ", body - " + body + " ==> " + res, this);
    return res;
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
