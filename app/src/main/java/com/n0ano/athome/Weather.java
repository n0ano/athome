package com.n0ano.athome;

import android.app.Dialog;
import android.os.SystemClock;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

// Created by n0ano on 10/10/16.
//
// Class to handle weather data
//
public class Weather
{

public final static int TYPE_WUNDER =   0;
public final static int TYPE_OPEN =     1;

public final static int PERIOD = (60*1000);   // weather only changes once a minute

private final static String DATA_TEMPLATE =
"{\"temp\":0.0,\"winddir\":0,\"windSpeed\":0.0,\"precipTotal\":0.0,\"pressure\":0.0,\"humidity\":0.0}";

Popup popup;

boolean running = true;
boolean paused = false;

WeatherOpen station;

// Weather: class constructor
//
public Weather(Popup popup, final DoitCallback cb)
{

    this.popup = popup;

    //station = new WeatherUnder("Home", P.get_string("weather:wunder_id"), P.get_string("weather:wunder_key"));
    station = new WeatherOpen("Boulder", "5574999", "8603a6e96af5de08ddbeeb5f4a244622");

    new Thread(new Runnable() {
        public void run() {
            while (running) {
                //
                //  Get the data
                //
                if (!paused) {
                    station.get_data();
                    cb.doit(0, null);
                }

                SystemClock.sleep(PERIOD);
            }
        }
    }).start();
}

public void stop()
{

    running = false;
}

public void pause(boolean p) { paused = p; }

public void go_temp_detail(View v)
{

    popup.detail_dialog((float)station.max_temp,
                        (float)station.min_temp,
                        station.max_time,
                        station.min_time);
}

public void show(View v)
{

    //
    //  Display it
    //
    TextView tv;
    ImageView iv;
    GaugeView gv;
    LinearLayout ll;

    if ((gv = (GaugeView) v.findViewById(R.id.weather_temp)) != null) {
        gv.set_minmax((float)station.min_temp, (float)station.max_temp);
        gv.set_name(station.name);
        gv.set_value((float)station.temp);
    }

    if ((iv = (ImageView) v.findViewById(R.id.weather_dir)) != null)
        iv.setRotation(station.wind_dir);

    if ((tv = (TextView) v.findViewById(R.id.weather_speed)) != null)
        tv.setText(String.format("%.1f", station.wind_speed));

    if ((ll = (LinearLayout)v.findViewById(R.id.weather_precip)) != null) {
        float rain = (float)station.precip;
        if (rain < 0)
            ll.setVisibility(View.GONE);
        else {
            ll.setVisibility(View.VISIBLE);
            if ((tv = (TextView) v.findViewById(R.id.weather_rain)) != null)
                tv.setText(String.format("%.1f", (float)station.precip));
        }
    }

    if ((iv = (ImageView) v.findViewById(R.id.weather_bar_dir)) != null)
        iv.setImageResource(station.baro_icon);

    if ((tv = (TextView) v.findViewById(R.id.weather_barometer)) != null)
        tv.setText(String.format("%.1f", station.baro));
}

}
