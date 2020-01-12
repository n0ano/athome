package com.n0ano.athome;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import java.util.Random;

public class SS_Faders
{
public final static String[] types = {
    "fade",             // 0
    "slide_up",         // 1
    "slide_down",       // 2
    "slide_left",       // 3
    "slide_right",      // 4
    "none",             // 5
    "random"            // 6
};

private Random rand;

private int duration = 2000;

public SS_Faders()
{

    rand = new Random();
}

public void fade(int type, View start, View end, int w, int h)
{

    if (type == 6)
        type = rand.nextInt(5);

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
       .setListener(null);

    start.animate()
         .alpha(0.0f)
         .setDuration(duration)
         .setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    fade_done(start, end);
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
       .setListener(null);

    start.animate()
         .translationYBy(-h)
         .setDuration(duration)
         .setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    fade_done(start, end);
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
       .setListener(null);

    start.animate()
         .translationYBy(h)
         .setDuration(duration)
         .setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    fade_done(start, end);
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
       .setListener(null);

    start.animate()
         .translationXBy(-w)
         .setDuration(duration)
         .setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    fade_done(start, end);
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
       .setListener(null);

    start.animate()
         .translationXBy(w)
         .setDuration(duration)
         .setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    fade_done(start, end);
                }
            });
}

private void fadenone(final View start, final View end)
{

    end.setVisibility(View.VISIBLE);
    fade_done(start, end);
}

private void fade_done(View start, View end)
{

    start.setVisibility(View.GONE);
    start.setTranslationX(0);
    start.setTranslationY(0);
    start.setAlpha(1.0f);
    TextView tv = (TextView)start.findViewById(R.id.title);
    if (tv != null)
        tv.setText("");
}

}
