package com.n0ano.athome.SS;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import com.n0ano.athome.R;
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
public final static int SAVER_UPDATE =  4;  // redo lists and do a SAVER_RESET
public final static int SAVER_STOP =    5;  // stop screen saver threads

//
//  Screen saver states
//
public final static int SS_UNKNOWN =     0;  // counting down to start
public final static int SS_COUNTING =    1;  // counting down to start
public final static int SS_SHOWING =     2;  // saver actively running
public final static int SS_FROZEN =      3;  // freeze saver with picture displayed

private final static int FADE_MAX =    100;

//
//  Main loop interruption types
//
public final static int INTR_NONE =      0;
public final static int INTR_START =     1;  // start the saver
public final static int INTR_NEXT =      2;  // next image

Activity act;
SS_Callbacks callbacks;
Thread main_thread = null;

ImageLists img_lists;

String menu_title = null;

private ScreenInfo ss_info;
private ImageFind finder;
private int new_gen = 0;

private boolean fling_ok = false;
private boolean running = true;
private int idle_counter;
private int disp_counter;
private int disp_list = 1;

private View first;
private View[] ss_views = new View[2];
private int ss_viewid = 0;
private Faders faders;

int fade_type;

ImageVM image_vm = null;

public ScreenSaver(View first, View v1, View v2, Activity act, final SS_Callbacks callbacks)
{

    Log.d("DDD-SS", "screen saver started");
    this.act = act;
    this.callbacks = callbacks;

    P.init(act.getSharedPreferences(P.PREF_NAME, Context.MODE_PRIVATE));

    ss_info = new ScreenInfo(act);

    finder = new ImageFind(act, null);

    img_lists = new ImageLists(ss_info.list, "", finder);

    //
    // this must be initialized before the first call to hide_views
    //
    faders = new Faders(this);

    this.first = first;
    ss_views[0] = v1;
    ss_views[1] = v2;
    hide_views();

    main_thread = new Thread(new Runnable() {
        public void run() {
            init_list(0);
            init_list(1);
            callbacks.ss_inited();
            main_loop();
        }
    });
    main_thread.start();
}

public void hide_views()
{

    faders.stop();
    ss_views[0].setVisibility(View.GONE);
    ss_views[0].setAlpha(0.0f);
    ss_views[1].setVisibility(View.GONE);
    ss_views[1].setAlpha(0.0f);
}

public void do_fade(View start, View end)
{

    faders.fade(ss_info.fade, start, end, ss_info.width, ss_info.height, new DoneCallback() {
        @Override
        public void done(Object obj) {
            if (ss_state() != SS_FROZEN)
                disp_counter = ss_info.delay;
        }
    });
}

private void image_name(String name)
{

//Log.d("DDD-SS", "image_name - " + name + ", title - " + title);
    callbacks.ss_toolbar((menu_title == null) ? name : menu_title, 0);
}

public void show_image(final ImageEntry entry, final View img_start, final View img_end)
{

    if (new_gen != 0)
        upd_list(new_gen);
    final ImageView iv = (ImageView)((RelativeLayout)img_end).findViewById(R.id.image);
    entry.get_bitmap(0, ss_info, new DoneCallback() {
        @Override
        public void done(final Object obj) {
            image_name("(" + (img_lists.get_index(entry) + 1) + "/" + img_lists.get_size() + ")" + C.last(entry.get_name()));
            act.runOnUiThread(new Runnable() {
                public void run() {
                    iv.setImageBitmap((Bitmap)obj);
                    TextView tv = (TextView)((RelativeLayout)img_end).findViewById(R.id.title);
                    tv.setText(entry.get_title());
                    if (img_start != null && ss_info.fade != 5)
                        do_fade(img_start, img_end);
                    else {
                        if (img_start != null)
                            img_start.setVisibility(View.GONE);
                        img_end.setVisibility(View.VISIBLE);
                        if (ss_state() != SS_FROZEN)
                            disp_counter = ss_info.delay;
                    }
                }
            });
            if (entry.generation != img_lists.get_generation())
                new_gen = entry.generation;
        }
    });
}

public void saver_fade(int delta)
{

    int old = ss_viewid;
    ss_viewid ^= 1;

    show_image(img_lists.next_image(delta), ss_views[old], ss_views[ss_viewid]);
}

public void ss_reset()
{

    P.rm_key("images:" + "");
    P.rm_key("image_last:" + "");
    P.rm_key("images:" + ss_info.list);
    P.rm_key("image_last:" + ss_info.list);
    redo_lists();
}

private void redo_lists()
{

    new Thread(new Runnable() {
        public void run() {
            init_list(0);
            init_list(1);
        }
    }).start();
}

private void init_list(int listno)
{

    img_lists.set_listno(listno);
    String saved = P.get("images:" + img_lists.get_name(), "");
    img_lists.parse(saved);
    Log.d("DDD-SS", "init_list: " + img_lists.get_name() + ", size - " + img_lists.get_size() + " => " + saved);
    int gen = C.parse_gen(saved);
    img_lists.set_generation(gen);
    if (gen <= 0)
        upd_list(0);
}

public void saver_start(int listno)
{

    fling_ok = false;
    ss_info = callbacks.ss_start();
    menu_title = null;

    img_lists.set_listno(listno);

    act.runOnUiThread(new Runnable() {
        public void run() {
            ss_views[0].setVisibility(View.GONE);
            ss_views[0].setAlpha(1.0f);
            ss_views[0].setScaleX(1.0f);
            ss_views[0].setScaleY(1.0f);
            ss_views[1].setVisibility(View.GONE);
            ss_views[1].setAlpha(1.0f);
            ss_views[1].setScaleX(1.0f);
            ss_views[1].setScaleY(1.0f);
        }
    });
    ss_viewid = 0;
    show_image(img_lists.next_image(1), first, ss_views[ss_viewid]);
}

public void screen_saver(int cmd)
{

Log.d("DDD-SS", "screen_saver command - " + cmd);

    switch (cmd) {

    case SAVER_BLOCK:
        idle_counter = -1;
        disp_counter = -FADE_MAX;
        break;

    case SAVER_STOP:
        running = false;
        break;

    case SAVER_UPDATE:
        redo_lists();
        break;

    }
}

public boolean touch()
{

    //
    //  SS is paused, ignore the touch
    //
    if (ss_state() == SS_FROZEN)
        return true;

    //
    //  SS is counting down, start cycling now
    //
    if (ss_state() == SS_COUNTING) {
        idle_counter = ss_info.start;
        return true;
    }

    callbacks.ss_stop();
    hide_views();
    disp_counter = -FADE_MAX;
    disp_list = 1;
    idle_counter = ss_info.start;
    return false;
}

void do_toast(final String msg)
{

    Log.d("DDD-SS", msg);
    act.runOnUiThread(new Runnable() {
        public void run() {
            Toast.makeText(act.getApplicationContext(), msg, Toast.LENGTH_LONG).show();
        }
    });
}

public void upd_list(final int gen)
{
    String from;
    String name = "unknown";

Log.d("DDD-SS", "update list, gen - " + gen);
    int count = img_lists.get_size();
    do_toast("Get new images, gen - " + Integer.valueOf(gen) + " > " + Integer.valueOf(img_lists.get_generation()));

    HashMap<String, String> map = C.parse_names(P.get("images:" + img_lists.get_name(), ""));
    img_lists.scan(map, ss_info, false);

    if (img_lists.get_size() > count) {
        name = img_lists.get_image(0).get_name();
        if (map.get(name) == null)
            from = "new image:" + C.get_from(name);
        else
            from = "new image:-unknown-";
    } else if (img_lists.get_size() < count)
        from = "list:-deleted-";
    else
        from = "list:-changed-";
    menu_title = from;
Log.d("DDD-SS", "new menu - " + menu_title);

    img_lists.set_generation((img_lists.get_size() > 0) ? img_lists.get_image(0).get_generation() : gen);
    String image_list = img_lists.list2str();
    P.put("images:" + img_lists.get_name(), image_list);
    P.put("image_last:" + img_lists.get_name(), img_lists.get_size());
    new_gen = 0;
}

public void saver_fling(int dir)
{

    if (fling_ok)
        saver_fade(dir);
}

//
//  User tapped on the saver icon in the action bar
//
public void action_click()
{

    //
    //  SS_COUNTING: Screen saver counting down, start cycling images
    //
    if (ss_state() == SS_COUNTING) {
        idle_counter = 1;
        disp_list = 0;
	    return;
    }

    //
    //  SS_FROZEN: SS is frozen for manual image changes, start cycling again
    //    NB: This depends upon a fade taking less than
    //        FADE_MAX(100) seconds
    //
    if (ss_state() == SS_FROZEN) {
        fling_ok = false;
        callbacks.ss_toolbar(null, R.drawable.ss_play);
        ss_info.fade = fade_type;
        disp_counter = 1;
        return;
    }

    //
    //  SS_SHOWING: SS is cycling through images, freeze
    //    in order to manually change images
    //
    fling_ok = true;
    callbacks.ss_toolbar(null, R.drawable.ss_pause);
    fade_type = ss_info.fade;
    ss_info.fade = 5;
    disp_counter = -FADE_MAX;
}

private int ss_state()
{

    if (idle_counter > 0)
        return SS_COUNTING;
    if (disp_counter > -FADE_MAX)
        return SS_SHOWING;
    if (disp_counter <= -FADE_MAX)
        return SS_FROZEN;
    return SS_UNKNOWN;
}


//
//  This routine must be called on a separte
//  thread from the UI thread
//
private void main_loop()
{
    int d, i;

Log.d("DDD-SS", "Screen saver main loop started");
    disp_counter = -FADE_MAX;
    idle_counter = ss_info.start;
    while (running) {
//Log.d("DDD-SS", "idle - " + idle_counter + ", " + disp_counter);
        i = --idle_counter;
        if (i == 0)
            disp_counter = 1;
        d = --disp_counter;
        if (d == 0) {
            if (i == 0)
                saver_start(disp_list);
            else
                saver_fade(1);
        }
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            Log.d("DDD-SS", "main loop interrupted");
        }
    }
}

}
