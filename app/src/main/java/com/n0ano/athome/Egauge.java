package com.n0ano.athome;

import android.content.res.Resources;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

// Created by n0ano on 10/10/16.
//
// Class to handle weather data
//
public class Egauge
{

public final static int PERIOD = (10 * 1000);   // check eGauge every 10 seconds
public final static int PERIOD_ALERTS = 6;      // check for alerts every 6*PERIOD seconds

public final static String EGAUGE_API = "/cgi-bin/egauge";
public final static String EGAUGE_QUERY = "tot&inst";
public final static String EGAUGE_ALERTS = "/cgi-bin/alert";

MainActivity act;
View view;

boolean running = true;
boolean paused = false;

int use_watt = 0;
int gen_watt = 0;

ArrayList<Alert> alerts;
long alert_ts;

int width = 0;

// Egauge: class constructor
//
//   act - activity that instantiated the class
//
public Egauge(MainActivity act, View view, long ts, final DoitCallback cb)
{

	this.act = act;
    this.view = view;
    this.alert_ts = ts;

    new Thread(new Runnable() {
        public void run() {
            int ck_alert = 0;
            boolean show;
            while (running) {
                show = false;

                //
                //  Get the data & alerts
                //
                if (--ck_alert < 0) {
                    ck_alert = PERIOD_ALERTS - 1;
                    show = (get_alerts() > 0);
                }

                if (!paused)
                    show = get_data();

                if (show)
                    cb.doit(null);

                SystemClock.sleep(PERIOD);
            }
        }
    }).start();
}

public void stop() { running = false; }
public void pause(boolean p) { paused = p; }

private boolean get_data()
{

    //
    // Get the data
    //
    String resp = act.call_api(P.get_string("egauge:url") + EGAUGE_API,
                               EGAUGE_QUERY);
    use_watt = get_value("Total Usage", resp);
    gen_watt = get_value("Total Generation", resp);
    return true;
}

private int get_alerts()
{
    Alert alert;
    int pri;
    long ts;
    String name, detail;

    //
    // Get the alerts
    //
    alerts = new ArrayList<Alert>();
    String resp = act.call_api(P.get_string("egauge:url") + EGAUGE_ALERTS, "");
    int idx = 0;
    while ((idx = resp.indexOf("<prio>", idx)) >= 0) {
        pri = (int)get_long("<prio>", 10, resp, idx);
        ts = get_long("<last_time>0x", 16, resp, idx);
        name = get_string("<name>", resp, idx);
        detail = get_string("<detail>", resp, idx);
        if (ts > this.alert_ts)
            alerts.add(new Alert(pri, ts, name, detail));
        ++idx;
    }
    Collections.sort(alerts);
    return alerts.size();
}

public int alerts_count()
{

    return alerts.size();
}

public Alert alerts_item(int idx)
{

    return alerts.get(idx);
}

public long alerts_ack()
{
    Alert alert;

Log.d("DDD", "egauge: acknowledge newest alert");
    int max = alerts_count();
    for (int i = 0; i < max; ++i) {
        alert = alerts_item(i);
        if (alert.ts > this.alert_ts)
            this.alert_ts = alert.ts;
    }
    alerts = new ArrayList<Alert>();
    if (view != null)
        show(view);
    return this.alert_ts;
}

private int get_value(String name, String resp)
{
    int w = 0;

    int start = resp.indexOf("n=\"" + name + "\">");
    if (start >= 0) {
        start = resp.indexOf("<i>", start);
        if (start < 0)
            return w;
        start += 3;
        int end = resp.indexOf(".", start);
        if (end < 0)
            return w;
        w = C.a2i(resp.substring(start, end));
    }
    return w;
}

private String get_string(String key, String resp, int idx)
{
    int l, start, end;

    l = key.length();
    if ((start = resp.indexOf(key, idx)) < 0)
        return null;
    end = resp.indexOf("<", start + l);
    return resp.substring(start + l, end);
}

private long get_long(String key, int radix, String resp, int idx)
{

    return Long.parseLong(get_string(key, resp, idx), radix);
}

private void set_arrow(ImageView iv, int watt, int left, int right)
{

    int arrow = (watt > 0) ? left : right;
    iv.setImageResource(arrow);
}

private String k_watts(int w)
{

    w = Math.abs(w);
    if (w >= 1000) {
        String s = w + "";
        String u = s.substring(0,1);
        String d = s.substring(1);
        return u + "." + d + " kW";
    } else {
        return w + " W";
    }
}

private int gen_arrow(int watts)
{
    int w_max = 6000;

    if (watts > (w_max * 0.75))
        return R.drawable.arrow_left_green;
    if (watts > (w_max * 0.25))
        return R.drawable.arrow_left_cyan;
    if (watts >= 0)
        return R.drawable.arrow_left;
    return R.drawable.arrow_right_red;
}

private int gen_icon(int watts)
{
    int w_max = 6000;

    if (watts > (w_max * 0.75))
        return R.drawable.panel_green;
    if (watts > (w_max * 0.25))
        return R.drawable.panel_cyan;
    if (watts >= 0)
        return R.drawable.panel_white;
    return R.drawable.panel_red;
}

public void show(View v)
{

    //
    //  Display it
    //
    TextView tv;
    ImageView iv;

    int disp_x = Resources.getSystem().getDisplayMetrics().widthPixels;
    int disp_y = Resources.getSystem().getDisplayMetrics().heightPixels;
    //Log.d("display = " + disp_x + "x" + disp_y);

    //LinearLayout ll = (LinearLayout)v.findViewById(R.id.egauge);
    //Logandroid and.d("layout = " + ll.getWidth() + "x" + ll.getHeight() + " => " + disp_x + "x" + disp_y);

    //ClockView vv = (ClockView)v.findViewById(R.id.clock_view);
    //ViewGroup.LayoutParams vp = vv.getLayoutParams();
    //Log.d("clock = " + vp.width + "x" + vp.height);

    if (width == 0 && disp_x < 1000) {
        width = 180;
        ClockView vv = (ClockView)v.findViewById(R.id.clock_view);
        if (vv != null) {
            ViewGroup.LayoutParams vp = vv.getLayoutParams();
            vp.width = width;
            vp.height = width;
            vv.setLayoutParams(vp);
        }
    }

    int grid_watt = gen_watt - use_watt;

    if ((tv = (TextView) v.findViewById(R.id.grid_watt)) != null) {
        tv.setText(k_watts(grid_watt));

        ((TextView)v.findViewById(R.id.house_watt)).setText(k_watts(use_watt));

        ((TextView)v.findViewById(R.id.panel_watt)).setText(k_watts(gen_watt));
        ((ImageView)v.findViewById(R.id.panel_image)).setImageResource(gen_icon(gen_watt));
        ((ImageView)v.findViewById(R.id.panel_arrow)).setImageResource(gen_arrow(gen_watt));
        LinearLayout ll = (LinearLayout)v.findViewById(R.id.egauge_alert);
        int num = alerts.size();
        if (num <= 0)
            ll.setVisibility(View.GONE);
        else {
            ll.setVisibility(View.VISIBLE);
            tv = (TextView)v.findViewById(R.id.alert_count);
            tv.setText(num + ((num == 1) ? " Alert" : " Alerts") + " pending");
            tv = (TextView)v.findViewById(R.id.alert_msg);
            tv.setText(alerts.get(0).name);
        }

        if ((iv = (ImageView) v.findViewById(R.id.grid_arrow)) != null)
            set_arrow(iv, grid_watt, R.drawable.arrow_left_green, R.drawable.arrow_right_red);
    }
}

}
