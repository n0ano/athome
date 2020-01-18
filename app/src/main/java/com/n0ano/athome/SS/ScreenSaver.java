package com.n0ano.athome.SS;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
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
import android.widget.RelativeLayout;
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
import com.n0ano.athome.Preferences;
import com.n0ano.athome.C;
import com.n0ano.athome.Log;

public class ScreenSaver
{

//
//  Commands to the scren saver controller
//
public final static int SAVER_RESET =   0;  // reset the counter
public final static int SAVER_TICK =    1;  // timer tic occurred
public final static int SAVER_BLOCK =   2;  // block saver while popups displayed
public final static int SAVER_FREEZE =  3;  // toggle frozen state

//
//  Screen saver states
//
public final static int SAVER_COUNTING =    0;  // counting down to start
public final static int SAVER_SHOWING =     1;  // saver actively running
public final static int SAVER_BLOCKED =     2;  // block for popups
public final static int SAVER_FROZEN =      3;  // freeze saver with picture displayed

Activity act;
SS_Callbacks callbacks;
Preferences pref;
Thread main_thread;

ArrayList<ImageEntry> images;
private int ss_current;

private ScreenInfo ss_info;
private ImageFind image_find;

private int ss_counter = 0;
public int state = SAVER_BLOCKED;

private View first;
private View[] ss_views = new View[2];
private int ss_viewid = 0;
private Faders faders;

boolean click = false;
boolean flick = false;
int flick_dir;

public ScreenSaver(View first, View v1, View v2, Activity act, SS_Callbacks callbacks)
{

    Log.d("DDD-SS", "screen saver started");
    this.act = act;
    this.callbacks = callbacks;

    pref = new Preferences(act);

    ss_info = new ScreenInfo(pref);

    image_find = new ImageFind(act, ss_info);

    this.first = first;
    ss_views[0] = v1;
    ss_views[1] = v2;
    hide_views();

    images = new ArrayList<ImageEntry>();
    faders = new Faders(this);
    state = SAVER_BLOCKED;

    //
    //  Start the main loop in its own thread
    //
    main_thread = new Thread(new Runnable() {
        public void run() {
            main_loop();
        }
    });
    main_thread.start();
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

    faders.fade(ss_info.fade, start, end, ss_info.width, ss_info.height);
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

public void show_image(final Bitmap bitmap, final String title, final View img_start, final View img_end, int gen)
{

    act.runOnUiThread(new Runnable() {
        public void run() {
            ImageView iv = (ImageView)((RelativeLayout)img_end).findViewById(R.id.image);
            if (bitmap == null)
                iv.setImageResource(R.drawable.no);
            else
                iv.setImageBitmap(bitmap);
            TextView tv = (TextView)((RelativeLayout)img_end).findViewById(R.id.title);
            tv.setText(title);
            if (img_start != null)
                do_fade(img_start, img_end);
        }
    });
    get_names(gen);
}

public void saver_fade(int delta)
{

    int old = ss_viewid;
    ss_viewid ^= 1;
    ss_counter = ss_info.delay;

    //
    //  ImageGet will call do_fade once the new image is loaded
    //
    ImageGet ig = new ImageGet(this, ss_info, ss_next(delta), ss_views[old], ss_views[ss_viewid]);
}

public void saver_fling(int dir)
{

Log.d("DDD-SS", "fling state - " + state + ", " + dir);
    flick = true;
    flick_dir = dir;
    main_thread.interrupt();
}

public void flicked()
{

    if (flick) {
        if (state == SAVER_FROZEN)
            saver_fade(flick_dir);
    }
    flick = false;
}

public void saver_click()
{

    click = true;
    main_thread.interrupt();
}

public void clicked()
{

    if (!click)
        return;
    click = false;

    if (state == SAVER_COUNTING)
        saver_start();
    else {
        if (state == ScreenSaver.SAVER_FROZEN) {
            state = ScreenSaver.SAVER_SHOWING;
            callbacks.ss_icon(R.drawable.play);
            ss_counter = ss_info.delay;
            saver_fade(1);
        } else {
            state = SAVER_FROZEN;
            callbacks.ss_icon(R.drawable.pause);
        }
    }
}

public void saver_start()
{

Log.d("DDD-SS", "saver: start");
    ss_info = callbacks.ss_start();
    state = SAVER_SHOWING;
    ss_counter = ss_info.delay;

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
    ImageGet ig = new ImageGet(this, ss_info, ss_next(1), first, ss_views[ss_viewid]);
}

public void screen_saver(int tick)
{

    if (state == SAVER_FROZEN)
        return;

    switch (tick) {

    case SAVER_BLOCK:
        state = SAVER_BLOCKED;
        break;

    case SAVER_FREEZE:
        state = ((state == SAVER_FROZEN) ? SAVER_SHOWING : SAVER_FROZEN);
        break;

    case SAVER_TICK:
        if (state != SAVER_BLOCKED) {
            if (--ss_counter == 0) {
                ss_counter = ss_info.delay;
                if (state == SAVER_SHOWING)
                    saver_fade(1);
                else
                    saver_start();
            }
        }
        break;

    case SAVER_RESET:
        if (state == SAVER_SHOWING) {
            callbacks.ss_stop();
            hide_views();
        }
        ss_counter = ss_info.start;
        state = ((ss_info.start == 0) ? SAVER_BLOCKED : SAVER_COUNTING);
        break;

    }
}

public boolean touch()
{

    if ((state == SAVER_SHOWING) && !faders.fading()) {
        screen_saver(SAVER_RESET);
        return false;
    }
    return true;
}

public void get_names(int gen)
{

    if (gen != ss_info.generation) {
        images = image_find.find_local(new ArrayList<ImageEntry>());
        images = image_find.find_remote(true, images, false);
        Collections.sort(images);
Log.d("DDD-SS", "get_names - " + images.size() + ", gen - " + image_find.ss_generation);
        ss_info.generation = image_find.ss_generation;
    }
}

private void set_images()
{
    int t, r, off;

    String str = pref.get("images", "");
    String[] imgs = str.split(";");
    if (imgs.length > 1) {
        ss_info.generation = Integer.parseInt(imgs[0], 10);
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
        state = SAVER_BLOCKED;
        screen_saver(SAVER_RESET);
    }
}

//
//  Main loop runs in its own thread
//
private void main_loop()
{

    set_images();

    for (;;) {
        try {
            Thread.sleep(1000);
            screen_saver(SAVER_TICK);
        } catch (Exception e) {
            Log.d("DDD-SS", "main loop interrupted");
            clicked();
            flicked();
        }
    }
}

}
