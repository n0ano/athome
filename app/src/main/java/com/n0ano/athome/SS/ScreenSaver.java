package com.n0ano.athome.SS;

import android.app.Activity;
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

//
//  Main loop interruption types
//
public final static int INTR_NONE =      0;
public final static int INTR_START =     1;  // start the saver
public final static int INTR_NEXT =      2;  // next image

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

int intr_type = INTR_NONE;
int flick_dir;
int fade_type;

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
    int first;
    ImageEntry img;

    if (images.size() <= 0)
        return null;

    if (images.size() < 2)
        return images.get(0);

    int next = pref.get("image_last", 0);
    if (delta == 0) {
        if (next >= images.size()) {
            next = images.size() - 1;
            pref.put("image_last", next);
        }
        images.get(next);
    }

    first = next;
    while ((next += delta) != first) {
        if (next >= images.size())
            next = 0;
        else if (next < 0)
            next = images.size() - 1;
        img = images.get(next);
Log.d("DDD-SS", "next(" + first +"," + ss_current + "): " + img.get_name() + ", check " + img.get_check());
        if (img.get_check()) {
            pref.put("image_last", next);
            return img;
        }
    }
    return null;
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

public void saver_click()
{

    intr_type = INTR_START;
    main_thread.interrupt();
}

public void saver_fling(int dir)
{

    flick_dir = dir;

    intr_type = INTR_NEXT;
    main_thread.interrupt();
}

public void intr(int type)
{

    intr_type = INTR_NONE;
    switch (type) {

    case INTR_START:
        if (state == SAVER_COUNTING)
            saver_start();
        else {
            if (state == ScreenSaver.SAVER_FROZEN) {
                callbacks.ss_icon(R.drawable.play);
                state = ScreenSaver.SAVER_SHOWING;
                ss_counter = ss_info.delay;
                ss_info.fade = fade_type;
                saver_fade(1);
            } else {
                callbacks.ss_icon(R.drawable.pause);
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

public void saver_start()
{

Log.d("DDD-SS", "saver: start");
    ss_info = callbacks.ss_start();
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

void do_toast(final String msg)
{

    Log.d("DDD-SS", msg);
    act.runOnUiThread(new Runnable() {
        public void run() {
            Toast.makeText(act.getApplicationContext(), msg, Toast.LENGTH_LONG).show();
        }
    });
}

public void get_names(int gen)
{
    String info, from;
    String name = "unknown";

    if (gen != ss_info.generation) {
        do_toast("Get new images, gen - " + Integer.valueOf(gen) + " > " + Integer.valueOf(ss_info.generation));
        images = image_find.find_local(new ArrayList<ImageEntry>());
        images = image_find.find_remote(true, images, false);
        Collections.sort(images);

        Log.d("DDD-SS", "get_names - " + images.size() + ", gen - " + image_find.ss_generation);
        HashMap<String, String> map = Utils.parse_images(pref.get("images", ""));
        for (ImageEntry img : images)
            img.enable(map.get(img.get_name()));

        if (ss_info.generation > 0) {
            from = "unknown";
            if (images.size() > 0) {
                name = images.get(0).get_name();
                if (map.get(name) == null)
                    from = Utils.get_from(name);
            }
            callbacks.ss_new(from);
        }
        ss_info.generation = image_find.ss_generation;
        ss_current = images.size();
    }
}

//
//  Main loop runs in its own thread
//
private void main_loop()
{

    String saved = pref.get("images", "");
    ss_info.generation = Utils.parse_gen(saved);

    images = Utils.parse_names(saved);
    ss_current = images.size();
    if (images.size() > 0) {
        state = SAVER_BLOCKED;
        screen_saver(SAVER_RESET);
    }

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

}
