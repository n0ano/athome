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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;

public class ScreenSaver
{

MainActivity act;

ArrayList<String> images;
private int ss_current;

public int ss_generation = -1;
private int ss_counter = 0;
private int ss_state = C.SAVER_FROZEN;
private int ss_viewid = 0;
private View[] ss_views = new View[2];
private SS_Faders ss_faders;

public ScreenSaver(MainActivity act, View v1, View v2)
{

    this.act = act;
    ss_views[0] = v1;
    ss_views[1] = v2;

    images = new ArrayList<String>();
    ss_faders = new SS_Faders();
    ss_state = C.SAVER_FROZEN;
}

public int state()
{

    return ss_state;
}

public void hide_views()
{
    ss_views[0].setVisibility(View.GONE);
    ss_views[0].setAlpha(0.0f);
    ss_views[1].setVisibility(View.GONE);
    ss_views[1].setAlpha(0.0f);
}

public void do_fade(View start, View end)
{

    ss_faders.fade(act.ss_fade, start, end, ss_width(), ss_height());
}

private int ss_width()
{

    View v = (View)act.findViewById(R.id.scroll_view);
    return v.getWidth();
}

private int ss_height()
{

    View v = (View)act.findViewById(R.id.scroll_view);
    return v.getHeight();
}

private String ss_next(int delta)
{

    if (images.size() <= 0)
        return "";
    ss_current += delta;
    if (ss_current >= images.size())
        ss_current = 0;
    else if (ss_current < 0)
        ss_current = images.size() - 1;
    return images.get(ss_current);
}

public void saver_fade(int delta)
{

    int old = ss_viewid;
    ss_viewid ^= 1;
    ss_counter = act.ss_delay;

    //
    //  ImageGet will call do_fade once the new image is loaded
    //
    ImageGet ig = new ImageGet(this, act.ss_server, act.ss_list, ss_next(delta), act.ss_user, act.ss_pwd, ss_width(), ss_height(), ss_views[old], ss_views[ss_viewid]);
    ig.start();
}

public void saver_click()
{

    saver_start();
}

public void saver_start()
{

Log.d("saver: start");
    if (ss_state == C.SAVER_COUNTING) {
        ss_state = C.SAVER_SHOWING;
        ss_counter = act.ss_delay;
        act.ss_fade = act.pref.get("ss_fade", 0);
        MenuItem icon = act.menu_bar.findItem(R.id.action_saver);
        icon.setIcon(R.drawable.play);

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
        ImageGet ig = new ImageGet(this, act.ss_server, act.ss_list, ss_next(1), act.ss_user, act.ss_pwd, ss_width(), ss_height(), (View)act.findViewById(R.id.scroll_view), ss_views[ss_viewid]);
        ig.start();
    } else {
        MenuItem icon = act.menu_bar.findItem(R.id.action_saver);
        if (ss_state == C.SAVER_FROZEN) {
            ss_state = C.SAVER_SHOWING;
            ss_counter = act.ss_delay;
            act.ss_fade = act.pref.get("ss_fade", 0);
            icon.setIcon(R.drawable.play);
            saver_fade(1);
        } else {
            ss_state = C.SAVER_FROZEN;
            act.ss_fade = 5;
            icon.setIcon(R.drawable.pause);
        }
    }
}

public void screen_saver(int tick)
{

//Log.d("SS: saver state - " + ss_state);
    if (ss_state == C.SAVER_FROZEN)
        return;

    switch (tick) {

    case C.SAVER_BLOCK:
        ss_state = C.SAVER_BLOCKED;
        break;

    case C.SAVER_FREEZE:
        ss_state = ((ss_state == C.SAVER_FROZEN) ? C.SAVER_SHOWING : C.SAVER_FROZEN);
        break;

    case C.SAVER_TICK:
        if (ss_state != C.SAVER_BLOCKED) {
            if (--ss_counter == 0) {
                act.runOnUiThread(new Runnable() {
                    public void run() {
                        ss_counter = act.ss_delay;
                        if (ss_state == C.SAVER_SHOWING)
                            saver_fade(1);
                        else
                            saver_start();
                    }
                });
            }
        }
        break;

    case C.SAVER_RESET:
Log.d("saver - reset to " + act.ss_start + " seconds, state - " + ss_state);
        if (ss_state == C.SAVER_SHOWING) {
            MenuItem icon = act.menu_bar.findItem(R.id.action_saver);
            icon.setIcon(R.drawable.monitor);
            act.display(act.screen);
        }
        ss_counter = act.ss_start;
        ss_state = ((act.ss_start == 0) ? C.SAVER_BLOCKED : C.SAVER_COUNTING);
        break;

    }
}

public void get_names(final ImageFind image_find, final int gen)
{

    if (gen != ss_generation)
        new Thread(new Runnable() {
            public void run() {
                images = image_find.find_local(new ArrayList<String>());
                images = image_find.find_remote(true, images);
Log.d("SS:get_names - " + images.size() + ", gen - " + image_find.ss_generation);
                ss_generation = image_find.ss_generation;
                ss_current = images.size();
                if (ss_current > 0) {
                    ss_state = C.SAVER_BLOCKED;
                    screen_saver(C.SAVER_RESET);
                }
            }
        }).start();
}

}
