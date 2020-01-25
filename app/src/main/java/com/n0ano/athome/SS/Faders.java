package com.n0ano.athome.SS;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.View;
import android.widget.TextView;

import java.util.Random;

import com.n0ano.athome.R;

public class Faders
{

private final static int FADE_START =   0;
private final static int FADE_END =     1;

public final static String[] types = {
    "fade",             // 0
    "slide_up",         // 1
    "slide_down",       // 2
    "slide_left",       // 3
    "slide_right",      // 4
    "none",             // 5
    "random"            // 6
};

private ScreenSaver ctx;

private Random rand;

private int duration = 2000;

private int active;
private DoneCallback callback;

public Faders(ScreenSaver ctx)
{

    this.ctx = ctx;
    rand = new Random();
}

public void fade(int type, View start, View end, int w, int h, DoneCallback callback)
{

    this.callback = callback;

    if (type == 6)
        type = rand.nextInt(5);

    active = 2;

    switch (type) {

    case 0:
        xfade(start, end);
        return;

    case 1:
        fadeup(start, end, w, h);
        return;

    case 2:
        fadedown(start, end, w, h);
        return;

    case 3:
        fadeleft(start, end, w, h);
        return;

    case 4:
        faderight(start, end, w, h);
        return;

    case 5:
        fadenone(start, end);
        return;

    }
}

public boolean fading()
{

    return active > 0;
}

//
// Cross fade from start to end
//
private void xfade(final View start, final View end)
{

    end.setAlpha(0.0f);
    end.setVisibility(View.VISIBLE);
    end.animate()
       .alpha(1.0f)
       .setDuration(duration)
       .setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                fade_done(end, FADE_END);
            }
       });

    start.animate()
         .alpha(0.0f)
         .setDuration(duration)
         .setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    fade_done(start, FADE_START);
                }
            });
}

private void fadeup(final View start, final View end, final int w, final int h)
{

    end.setTranslationY(h);
    end.setVisibility(View.VISIBLE);
    end.animate()
       .translationYBy(-h)
       .setDuration(duration)
       .setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                fade_done(end, FADE_END);
            }
       });

    start.animate()
         .translationYBy(-h)
         .setDuration(duration)
         .setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    fade_done(start, FADE_START);
                }
            });
}

private void fadedown(final View start, final View end, final int w, final int h)
{

    end.setTranslationY(-h);
    end.setVisibility(View.VISIBLE);
    end.animate()
       .translationYBy(h)
       .setDuration(duration)
       .setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                fade_done(end, FADE_END);
            }
       });

    start.animate()
         .translationYBy(h)
         .setDuration(duration)
         .setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    fade_done(start, FADE_START);
                }
            });
}

private void fadeleft(final View start, final View end, final int w, final int h)
{

    end.setTranslationX(w);
    end.setVisibility(View.VISIBLE);
    end.animate()
       .translationXBy(-w)
       .setDuration(duration)
       .setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                fade_done(end, FADE_END);
            }
       });

    start.animate()
         .translationXBy(-w)
         .setDuration(duration)
         .setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    fade_done(start, FADE_START);
                }
            });
}

private void faderight(final View start, final View end, final int w, final int h)
{

    end.setTranslationX(-w);
    end.setVisibility(View.VISIBLE);
    end.animate()
       .translationXBy(w)
       .setDuration(duration)
       .setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                fade_done(end, FADE_END);
            }
       });

    start.animate()
         .translationXBy(w)
         .setDuration(duration)
         .setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    fade_done(start, FADE_START);
                }
            });
}

private void fadenone(final View start, final View end)
{

    fade_done(end, FADE_END);
    fade_done(start, FADE_START);
}

private void fade_done(View view, int which)
{

    switch (which) {

    case FADE_START:
        view.setVisibility(View.GONE);
        view.setTranslationX(0);
        view.setTranslationY(0);
        view.setAlpha(1.0f);
        TextView tv = (TextView)view.findViewById(R.id.title);
        if (tv != null)
            tv.setText("");
        break;

    case FADE_END:
        view.setVisibility(View.VISIBLE);
        break;

    }
    if (--active <= 0 && callback != null)
        callback.done();
        
}

}
