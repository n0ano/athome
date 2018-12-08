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
import android.widget.TextView;

import com.n0ano.athome.Log;
import com.n0ano.athome.Version;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity
{

public String egauge_url;

public String wunder_id;

public String ecobee_api;
public String ecobee_refresh;
public String ecobee_access;
public int ecobee_which;
public String ecobee_name;
public String[] ecobee_thermos;

private String url;

int degree = 0;
int debug = 0;

public String x10_url;
String x10_jwt;
String x10_battery = "";
int x10_batt_min;
int x10_batt_max;
int x10_batt_level;
int x10_position;

Weather weather;
Egauge egauge;
X10 x10;

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

private void restore_state()
{

    Preferences pref = new Preferences(this);

    egauge_url = pref.get_string("egauge_url", "");

    wunder_id = pref.get_string("wunder_id", "");

    ecobee_api = pref.get_string("ecobee_api", "");
    ecobee_access = pref.get_string("ecobee_access", "");
    ecobee_refresh = pref.get_string("ecobee_refresh", "");
    ecobee_which = pref.get_int("ecobee_which", -1);
    ecobee_name = pref.get_string("ecobee_name", "");
    ecobee_thermos = new String[0];

    x10_url = pref.get_string("x10_url", "");
    x10_jwt = pref.get_string("x10_jwt", "none");
    x10_battery = pref.get_string("x10_battery", "");
    x10_batt_min = pref.get_int("x10_batt_min", Common.BATTERY_LOW);
    x10_batt_max = pref.get_int("x10_batt_max", Common.BATTERY_HIGH);
    x10_batt_level = pref.get_int("x10_batt_level", 0);

    debug = pref.get_int("debug", 0);
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
"  }\n" +
"}";
    Log.d("xml - " + xml);
    Log.d("json - " + json);

    Log.d("xml xyzzy() = " + parse.xml_get("xyzzy", xml, 1));
    Log.d("xml page(1:one) = " + parse.xml_get("page", xml));
    Log.d("xml page(2:two) = " + parse.xml_get("page", xml, 2));
    Log.d("xml page(3:) = " + parse.xml_get("page", xml, 3));
    Log.d("xml totalPages(1:10) = " + parse.xml_get("totalPages", xml));
    Log.d("xml pageSize(1:2) = " + parse.xml_get("pageSize", xml));
    Log.d("xml pageSize(2:1234) = " + parse.xml_get("pageSize", xml, 2));
    Log.d("xml max(1:59.0) = " + parse.xml_get("maxtemp", xml, 1));
    Log.d("xml min(1:39.9) = " + parse.xml_get("mintemp", xml, 1));
    Log.d("xml max_time(1:06:15PM) = " + parse.xml_get("maxtemp_time", xml, 1));
    Log.d("xml min_time(1:02:35PM) = " + parse.xml_get("mintemp_time", xml, 1));

    Log.d("json xyzzy() = " + parse.json_get("xyzzy", json, 1));
    Log.d("json page(1:one) = " + parse.json_get("page", json));
    Log.d("json page(2:two) = " + parse.json_get("page", json, 2));
    Log.d("json page(3:) = " + parse.json_get("page", json, 3));
    Log.d("json totalPages(1:10) = " + parse.json_get("totalPages", json));
    Log.d("json pageSize(1:2) = " + parse.json_get("pageSize", json));
    Log.d("json pageSize(2:1234) = " + parse.json_get("pageSize", json, 2));
    Log.d("json state(1:0000000000011001) = " + parse.json_get("state", json));
}


/*
 *  call an HTTP(S) uri to get a response.  Note that this will return
 *      a string with the `entire` response.  If the API returns a lot
 *      of data you might be better off using the open_url/read_url/close_url
 *      interface available right below this one.
 */
public String call_api(String type, String uri, String params, String auth)
{
    HttpURLConnection con = null;
    String res = "";

    if (debug > 0)
        Log.d(type + " - " + uri + ", params - " + params + ", auth - " + auth);
    if (!params.equals(""))
        uri = uri + "?" + params;
    try {
        URL url = new URL(uri);
        con = (HttpURLConnection) url.openConnection();
        if (type.equals("POST"))
            con.setDoOutput(true);
        if (!auth.equals(""))
            con.setRequestProperty("Authorization", auth);
        InputStreamReader in = new InputStreamReader(con.getInputStream());
        BufferedReader inp = new BufferedReader (in);
        StringBuilder response = new StringBuilder();
        for (String line; (line = inp.readLine()) != null; )
            response.append(line).append('\n');
        res = response.toString();
    } catch (Exception e) {
        Log.d("read failed - " + e);
        res = "";
    } finally {
        if (con != null)
            con.disconnect();
    }
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

    if (x10_batt_level != 0)
        return x10_batt_level;

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

private boolean working()
{

    return this.running;
}

private void doit()
{

    final Weather weather = new Weather(this);
    this.weather = weather;

    final Egauge egauge = new Egauge( this);
    this.egauge = egauge;

    final X10 x10 = new X10(this);
    this.x10 = x10;

    this.running = true;

    new Thread(new Runnable() {
        public void run() {
            while (working()) {

                if (weather != null)
                    weather.update();

                if (egauge != null)
                    egauge.update();

                if (x10 != null)
                    x10.update();

                SystemClock.sleep(1000);
            }
        }
    }).start();
}

}
