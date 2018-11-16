package com.n0ano.athome;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Point;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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

private static final int OPT_SETTINGS =     1001;

private String server;

private String url;

int degree = 0;

@Override
protected void onCreate(Bundle state)
{

    super.onCreate(state);
    Log.d("MainActivity: onCreate");

    setContentView(R.layout.activity_main);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    doit();
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

    case R.id.action_settings:
        settings_dialog();
        return true;

    case R.id.action_about:
        about_dialog();
        return true;
    }

    return super.onOptionsItemSelected(item);
}

private void settings_dialog()
{

    final Dialog dialog = new Dialog(this, R.style.AlertDialogCustom);
    dialog.setContentView(R.layout.bar_settings);

    final EditText et = (EditText) dialog.findViewById(R.id.opt_server);
    et.setText(server);

    Button cancel = (Button) dialog.findViewById(R.id.opt_cancel);
    cancel.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            dialog.dismiss();
        }
    });

    Button ok = (Button) dialog.findViewById(R.id.opt_ok);
    ok.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            server = et.getText().toString();
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

private String param(String search, String resp)
{

    int start = resp.indexOf(search);
    if (start >= 0) {
        start += search.length();
        int end = resp.indexOf("\"", start);
        return resp.substring(start, end);
    }
    return "";
}

public String xml_get(String name, String resp)
{

    return param("<" + name + Common.XML_SUF, resp);
}

public String xml_get(String name, String resp, String suf)
{

    return param("<" + name + suf, resp);
}

public String json_get(String name, String resp)
{

    return param ("\"" + name + Common.JSON_SUF, resp);
}

public String json_get(String name, String resp, String suf)
{

    return param ("\"" + name + suf, resp);
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

private void doit()
{

    final Weather weather = new Weather("KCOBOULD238", this);
    final Egauge egauge = new Egauge("http://n0ano-eg/", this);
    final X10 x10 = new X10(this);
    new Thread(new Runnable() {
        public void run() {
            for (;;) {

                weather.get_data();
                weather.show_data();

                egauge.get_data();
                egauge.show_data();

                x10.get_data();
                x10.show_data();

                SystemClock.sleep(Common.DATA_DELAY * 1000);
            }
        }
    }).start();
}

}
