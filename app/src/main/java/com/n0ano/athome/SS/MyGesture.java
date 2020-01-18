package com.n0ano.athome.SS;

import android.view.GestureDetector;
import android.view.MotionEvent;

public class MyGesture extends GestureDetector.SimpleOnGestureListener
{

ScreenSaver act;

public MyGesture(ScreenSaver act)
{

    this.act = act;
}

@Override
public boolean onDown(MotionEvent event)
{

    return true;
}

@Override
public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY)
{

    //  velocityX > 0 => swipe right
    //            < 0 => swipe left
    act.saver_fling(velocityX < 0 ? 1 : -1);
    return true;
}

}