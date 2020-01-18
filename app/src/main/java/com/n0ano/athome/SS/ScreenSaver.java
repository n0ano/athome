package com.n0ano.athome.SS;

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
import java.util.Collections;
import java.util.Iterator;

import com.n0ano.athome.SS.Faders;

import com.n0ano.athome.R;
import com.n0ano.athome.MainActivity;
import com.n0ano.athome.C;
import com.n0ano.athome.Log;

public class ScreenSaver
{

MainActivity act;

ArrayList<ImageEntry> images;
private int ss_current;

public int ss_generation = -1;
private int ss_counter = 0;
public int ss_state = C.SAVER_BLOCKED;
private int ss_viewid = 0;
private View[] ss_views = new View[2];
private Faders faders;

public ScreenSaver(MainActivity act, View v1, View v2)
{

    Log.d("DDD-SS", "screen saver started");
    this.act = act;
    ss_views[0] = v1;
    ss_views[1] = v2;
    hide_views();

    images = new ArrayList<ImageEntry>();
    faders = new Faders(this);
    ss_state = C.SAVER_BLOCKED;

    set_images();
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

    faders.fade(act.ss_fade, start, end, ss_width(), ss_height());
}

public void cancel_fade()
{

    View v = (View)act.findViewById(R.id.scroll_view);
    v.setAlpha(1.0f);
    act.display(act.screen);
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

private ImageEntry ss_next(int delta)
{

    if (images.size() <= 0)
        return null;
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
    ImageGet ig = new ImageGet(this, act.ss_info, ss_next(delta), ss_width(), ss_height(), ss_views[old], ss_views[ss_viewid]);
    ig.start();
}

public void saver_fling(int dir)
{

//Log.d("DDD-SS", "state - " + ss_state + ", " + dir);
    if (ss_state == C.SAVER_FROZEN)
        saver_fade(dir);
}

public void saver_click()
{

    if (ss_state == C.SAVER_COUNTING)
        saver_start();
    else {
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

public void saver_start()
{

Log.d("DDD-SS", "saver: start");
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
    ImageGet ig = new ImageGet(this, act.ss_info, ss_next(1), ss_width(), ss_height(), (View)act.findViewById(R.id.scroll_view), ss_views[ss_viewid]);
    ig.start();
}

public void screen_saver(int tick)
{

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
        if (ss_state == C.SAVER_SHOWING) {
            MenuItem icon = act.menu_bar.findItem(R.id.action_saver);
            icon.setIcon(R.drawable.monitor);
            cancel_fade();
            hide_views();
        }
        ss_counter = act.ss_start;
        ss_state = ((act.ss_start == 0) ? C.SAVER_BLOCKED : C.SAVER_COUNTING);
        break;

    }
}

public boolean touch()
{

    if ((ss_state == C.SAVER_SHOWING) && !faders.fading()) {
        screen_saver(C.SAVER_RESET);
        return false;
    }
    return true;
}

public void get_names(final ImageFind image_find, final int gen)
{

    if (gen != ss_generation)
        new Thread(new Runnable() {
            public void run() {
                images = image_find.find_local(new ArrayList<ImageEntry>());
                images = image_find.find_remote(true, images, false);
                Collections.sort(images);
Log.d("DDD-SS", "get_names - " + images.size() + ", gen - " + image_find.ss_generation);
                ss_generation = image_find.ss_generation;
                ss_current = images.size();
                if (ss_current > 0) {
                    ss_state = C.SAVER_BLOCKED;
                    screen_saver(C.SAVER_RESET);
                }
            }
        }).start();
}

private void set_images()
{
    int t, r, off;

    String str = act.pref.get("images", "");
    String[] imgs = str.split(";");
    if (imgs.length > 1) {
        ss_generation = Integer.parseInt(imgs[0], 10);
        for (int i = 1; i < imgs.length; i++) {
            String name = imgs[i];
            if (!name.isEmpty()) {
                t = ((name.charAt(0) == 'L') ? C.IMAGE_LOCAL : C.IMAGE_REMOTE);
                r = 0;
                off = 1;
                if (name.charAt(off) == 'R') {
                    r = Integer.parseInt(name.substring(off + 1, off + 4), 10);
                    off += 4;
                }
                images.add(new ImageEntry(name.substring(off), t, i, r, null));
            }
        }
    }
    ss_current = images.size();
Log.d("DDD-SS", "set_images - " + images.size());
    if (images.size() > 0) {
        ss_state = C.SAVER_BLOCKED;
        screen_saver(C.SAVER_RESET);
    }
}

}
