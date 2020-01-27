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
import com.n0ano.athome.Preferences;
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

//
//  Screen saver states
//
public final static int SAVER_COUNTING =    0;  // counting down to start
public final static int SAVER_SHOWING =     1;  // saver actively running
public final static int SAVER_BLOCKED =     2;  // block for popups
public final static int SAVER_FROZEN =      3;  // freeze saver with picture displayed

//
//  Main loop interruption types
//
public final static int INTR_NONE =      0;
public final static int INTR_START =     1;  // start the saver
public final static int INTR_NEXT =      2;  // next image

Activity act;
SS_Callbacks callbacks;
Preferences pref;
Thread main_thread = null;

ImageLists img_lists;

String menu_title = null;

private ScreenInfo ss_info;
private ImageFind finder;

private int ss_counter = 0;
public int state = SAVER_BLOCKED;

private View first;
private View[] ss_views = new View[2];
private int ss_viewid = 0;
private Faders faders;

int intr_type = INTR_NONE;
int flick_dir;
int fade_type;

ImageVM image_vm = null;

public ScreenSaver(View first, View v1, View v2, Activity act, SS_Callbacks callbacks)
{

    Log.d("DDD-SS", "screen saver started");
    this.act = act;
    this.callbacks = callbacks;

    pref = new Preferences(act);

    ss_info = new ScreenInfo(act, pref);

    finder = new ImageFind(act, null);

    img_lists = new ImageLists(ss_info.list, "", pref, finder);

    this.first = first;
    ss_views[0] = v1;
    ss_views[1] = v2;
    hide_views();

    faders = new Faders(this);
    state = SAVER_BLOCKED;
    screen_saver(SAVER_RESET);

    do_loop();
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

    faders.fade(ss_info.fade, start, end, ss_info.width, ss_info.height, new DoneCallback() {
        @Override
        public void done() {
            ss_counter = ss_info.delay;
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

    final ImageView iv = (ImageView)((RelativeLayout)img_end).findViewById(R.id.image);
    act.runOnUiThread(new Runnable() {
        public void run() {
            iv.setImageResource(R.drawable.ss_no);
        }
    });
    entry.get_bitmap(act, 0, ss_info, iv, new DoneCallback() {
        @Override
        public void done() {
            image_name("(" + (img_lists.get_index(entry) + 1) + "/" + img_lists.get_size() + ")" + C.last(entry.get_name()));
            act.runOnUiThread(new Runnable() {
                public void run() {
                    TextView tv = (TextView)((RelativeLayout)img_end).findViewById(R.id.title);
                    tv.setText(entry.get_title());
                    if (img_start != null)
                        do_fade(img_start, img_end);
                }
            });
            if (entry.generation != img_lists.get_generation())
                upd_list(entry.generation, null);
        }
    });
}

public void saver_fade(int delta)
{

    int old = ss_viewid;
    ss_viewid ^= 1;
    ss_counter = ss_info.delay;

    show_image(img_lists.next_image(delta), ss_views[old], ss_views[ss_viewid]);
}

public void saver_click()
{

    intr_type = INTR_START;
    if (main_thread != null)
        main_thread.interrupt();
}

public void saver_fling(int dir)
{

    flick_dir = dir;

    intr_type = INTR_NEXT;
    if (main_thread != null)
        main_thread.interrupt();
}

public void intr(int type)
{

    intr_type = INTR_NONE;
    switch (type) {

    case INTR_START:
        if (state == SAVER_COUNTING)
            saver_start(0);
        else {
            if (state == ScreenSaver.SAVER_FROZEN) {
                callbacks.ss_toolbar(null, R.drawable.ss_play);
                state = ScreenSaver.SAVER_SHOWING;
                ss_counter = ss_info.delay;
                ss_info.fade = fade_type;
                saver_fade(1);
            } else {
                callbacks.ss_toolbar(null, R.drawable.ss_pause);
                state = SAVER_FROZEN;
                fade_type = ss_info.fade;
                ss_info.fade = 5;
            }
        }
        break;

    case INTR_NEXT:
        if (state == SAVER_FROZEN)
            saver_fade(flick_dir);
        break;

    }
}

public void ss_reset()
{

    pref.rm_key("images:" + "");
    pref.rm_key("image_last:" + "");
    pref.rm_key("images:" + ss_info.list);
    pref.rm_key("image_last:" + ss_info.list);
    redo_lists();
}

private void redo_lists()
{

    new Thread(new Runnable() {
        public void run() {
            init_list(0, null);
            init_list(1, null);
        }
    }).start();
}

private void init_list(int listno, DoneCallback cb)
{

    img_lists.set_listno(listno);
    String saved = pref.get("images:" + img_lists.get_name(), "");
    img_lists.parse(saved);
    Log.d("DDD-SS", "init_list: " + img_lists.get_name() + ", size - " + img_lists.get_size() + " => " + saved);
    upd_list(0, cb);
}

public void saver_start(int listno)
{

    ss_info = callbacks.ss_start();
    menu_title = null;

    img_lists.set_listno(listno);
    state = SAVER_SHOWING;
    ss_counter = ss_info.delay;

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
        if (state != SAVER_BLOCKED && ss_counter > 0) {
            if (--ss_counter == 0) {
                if (state == SAVER_SHOWING)
                    saver_fade(1);
                else
                    saver_start(1);
            }
        }
        break;

    case SAVER_UPDATE:
        redo_lists();

        // fall thru */
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

void do_toast(final String msg)
{

    Log.d("DDD-SS", msg);
    act.runOnUiThread(new Runnable() {
        public void run() {
            Toast.makeText(act.getApplicationContext(), msg, Toast.LENGTH_LONG).show();
        }
    });
}

public void upd_list(final int gen, final DoneCallback cb)
{
    String from;
    String name = "unknown";

    int count = img_lists.get_size();
    do_toast("Get new images, gen - " + Integer.valueOf(gen) + " > " + Integer.valueOf(img_lists.get_generation()));

    HashMap<String, String> map = C.parse_names(pref.get("images:" + img_lists.get_name(), ""));
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

    img_lists.set_generation((img_lists.get_size() > 0) ? img_lists.get_image(0).get_generation() : gen);
    String image_list = img_lists.list2str();
    pref.put("images:" + img_lists.get_name(), image_list);
    pref.put("image_last:" + img_lists.get_name(), img_lists.get_size());
    if (cb != null)
        cb.done();
}

//
//  This routine must be called on a separte
//  thread from the UI thread
//
private void main_loop()
{

Log.d("DDD-SS", "Screen saver main loop started");
    for (;;) {
        try {
            Thread.sleep(1000);
            screen_saver(SAVER_TICK);
        } catch (Exception e) {
            Log.d("DDD-SS", "main loop interrupted");
            intr(intr_type);
        }
    }
}

private void do_loop()
{
    //
    //  Start the main loop in its own thread
    //
    main_thread = new Thread(new Runnable() {
        public void run() {
            init_list(0, new DoneCallback() {
                @Override
                public void done() {
                    init_list(1, new DoneCallback() {
                        @Override
                        public void done() {
                            main_loop();
                        }
                    });
                }
            });
        }
    });
    main_thread.start();
}

}
