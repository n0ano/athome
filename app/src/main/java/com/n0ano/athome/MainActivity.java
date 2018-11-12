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

private void doit()
{

    final Weather weather = new Weather("KCOBOULD238", this);
    final Egauge egauge = new Egauge("http://n0ano-eg/", this);
    new Thread(new Runnable() {
        public void run() {
            for (;;) {

                weather.get_data();
                weather.show_data();

                egauge.get_data();
                egauge.show_data();

                SystemClock.sleep(Common.DATA_DELAY * 1000);
            }
        }
    }).start();
}

}
