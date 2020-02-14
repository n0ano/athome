package com.n0ano.athome;

import android.view.GestureDetector;
import android.view.MotionEvent;

public class MyGesture extends GestureDetector.SimpleOnGestureListener
{

MainActivity act;

public MyGesture(MainActivity act)
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
    act.fling(velocityX < 0 ? 1 : -1);
    return true;
}

}
