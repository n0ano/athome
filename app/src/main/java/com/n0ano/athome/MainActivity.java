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
import android.widget.EditText;
import android.widget.ImageView;
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
public String ecobee_auth;
public String ecobee_refresh;
public String ecobee_access;
public int ecobee_which;
private int ecobee_swhich;
public String ecobee_name;
public String[] ecobee_thermos = {"one", "two"};

private String url;

int degree = 0;

Weather weather;
Egauge egauge;
X10 x10;

public Parse parse = new Parse();

@Override
protected void onCreate(Bundle state)
{

    super.onCreate(state);
    Log.d("MainActivity: onCreate");

    setContentView(R.layout.activity_main);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
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

    switch (item.getItemId()) {

    case R.id.action_egauge:
        egauge_dialog();
        return true;

    case R.id.action_wunder:
        wunder_dialog();
        return true;

    case R.id.action_ecobee:
        ecobee_dialog();
        return true;

    case R.id.action_about:
        about_dialog();
        return true;
    }

    return super.onOptionsItemSelected(item);
}

private void egauge_dialog()
{
    final Preferences pref = new Preferences(this);

    final Dialog dialog = new Dialog(this, R.style.AlertDialogCustom);
    dialog.setContentView(R.layout.bar_egauge);

    final EditText et = (EditText) dialog.findViewById(R.id.egauge_url);
    et.setText(egauge_url);

    Button cancel = (Button) dialog.findViewById(R.id.egauge_cancel);
    cancel.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            dialog.dismiss();
        }
    });

    Button ok = (Button) dialog.findViewById(R.id.egauge_ok);
    ok.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            egauge_url = et.getText().toString();
            pref.put_string("egauge_url", egauge_url);
            dialog.dismiss();
        }
    });

    dialog.show();
}

private void wunder_dialog()
{
    final Preferences pref = new Preferences(this);

    final Dialog dialog = new Dialog(this, R.style.AlertDialogCustom);
    dialog.setContentView(R.layout.bar_wunder);

    final EditText et = (EditText) dialog.findViewById(R.id.wunder_id);
    et.setText(wunder_id);

    Button cancel = (Button) dialog.findViewById(R.id.wunder_cancel);
    cancel.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            dialog.dismiss();
        }
    });

    Button ok = (Button) dialog.findViewById(R.id.wunder_ok);
    ok.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            wunder_id = et.getText().toString();
            pref.put_string("wunder_id", wunder_id);
            dialog.dismiss();
        }
    });

    dialog.show();
}

private void ecobee_ra_auth()
{

    final Dialog dialog = new Dialog(this, R.style.AlertDialogCustom);
    dialog.setContentView(R.layout.bar_reauth);

    TextView tv = (TextView) dialog.findViewById(R.id.ecobee_ra_text);
    tv.setText(Html.fromHtml(getString(R.string.ecobee_auth)));

    Button cancel = (Button) dialog.findViewById(R.id.ecobee_ra_cancel);
    cancel.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            dialog.dismiss();
        }
    });

    Button ok = (Button) dialog.findViewById(R.id.ecobee_ra_start);
    ok.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(Common.ECOBEE_REAUTH));
            startActivity(intent);
            dialog.dismiss();
        }
    });

    dialog.show();
}

private void ecobee_dialog()
{
    final Preferences pref = new Preferences(this);

    final Dialog dialog = new Dialog(this, R.style.AlertDialogCustom);
    dialog.setContentView(R.layout.bar_ecobee);

    final EditText api_tv = (EditText) dialog.findViewById(R.id.ecobee_api);
    api_tv.setText(ecobee_api);

    final EditText auth_tv = (EditText) dialog.findViewById(R.id.ecobee_auth);
    auth_tv.setText(ecobee_auth);

    final EditText access_tv = (EditText) dialog.findViewById(R.id.ecobee_access);
    access_tv.setText(ecobee_access);

    final EditText refresh_tv = (EditText) dialog.findViewById(R.id.ecobee_refresh);
    refresh_tv.setText(ecobee_refresh);

    final Spinner sv = (Spinner) dialog.findViewById(R.id.ecobee_which);
    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.ecobee_spinner, ecobee_thermos);
    adapter.setDropDownViewResource(R.layout.ecobee_spinner);
    sv.setAdapter(adapter);
    ecobee_swhich = ecobee_which;
    if (ecobee_which >= 0)
        sv.setSelection(ecobee_swhich = ecobee_which);
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

    Button reauth = (Button) dialog.findViewById(R.id.ecobee_reauth);
    reauth.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            ecobee_ra_auth();
        }
    });

    Button cancel = (Button) dialog.findViewById(R.id.ecobee_cancel);
    cancel.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            dialog.dismiss();
        }
    });

    Button ok = (Button) dialog.findViewById(R.id.ecobee_ok);
    ok.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            ecobee_api = api_tv.getText().toString();
            ecobee_auth = auth_tv.getText().toString();
            ecobee_access = access_tv.getText().toString();
            ecobee_refresh = refresh_tv.getText().toString();
            ecobee_which = ecobee_swhich;
            if (ecobee_which >= 0)
                ecobee_name = ecobee_thermos[ecobee_which];
            pref.put_string("ecobee_api", ecobee_api);
            pref.put_string("ecobee_auth", ecobee_auth);
            pref.put_string("ecobee_access", ecobee_access);
            pref.put_string("ecobee_refresh", ecobee_refresh);
            pref.put_int("ecobee_which", ecobee_which);
            pref.put_string("ecobee_name", ecobee_name);
            dialog.dismiss();
        }
    });

    dialog.show();
}

private void about_dialog()
{

    final Dialog dialog = new Dialog(this, R.style.AlertDialogCustom);
    dialog.setContentView(R.layout.bar_about);

    final TextView et = (TextView) dialog.findViewById(R.id.about_version);
    et.setText("Version: " + Version.VER_MAJOR + "." + Version.VER_MINOR + Version.VER_DEBUG);

    Button ok = (Button) dialog.findViewById(R.id.about_ok);
    ok.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            dialog.dismiss();
        }
    });

    dialog.show();
}

private void restore_state()
{

    Preferences pref = new Preferences(this);

    egauge_url = pref.get_string("egauge_url", "");
    wunder_id = pref.get_string("wunder_id", "");
    ecobee_api = pref.get_string("ecobee_api", "");
    ecobee_auth = pref.get_string("ecobee_auth", "");
    ecobee_access = pref.get_string("ecobee_access", "");
    ecobee_refresh = pref.get_string("ecobee_refresh", "");
    ecobee_which = pref.get_int("ecobee_which", -1);
    ecobee_name = pref.get_string("ecobee_name", "");
    ecobee_thermos = new String[0];
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

private int get_battery()
{
    String chg;

    IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
    Intent bs = this.registerReceiver(null, ifilter);

    int level = bs.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
    int scale = bs.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
    return (level * 100) / scale;
}

private void battery()
{

    if (x10.x10_power == 0)
        return;

    int chg = get_battery();
    if (chg < Common.BATTERY_LOW)
        x10.power(1);
    else if (chg > Common.BATTERY_HIGH)
        x10.power(0);
}

private void chores()
{

    battery();
}

public void go_temp_detail(View v)
{

    weather.go_temp_detail(v);
}

private void doit()
{

    final Weather weather = new Weather(this);
    this.weather = weather;

    final Egauge egauge = new Egauge( this);
    this.egauge = egauge;

    final X10 x10 = new X10(this);
    this.x10 = x10;

    new Thread(new Runnable() {
        public void run() {
            for (;;) {
                chores();

                weather.update();

                egauge.update();

                x10.update();

                SystemClock.sleep(Common.DATA_DELAY * 1000);
            }
        }
    }).start();
}

}
