package com.n0ano.athome;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.n0ano.athome.Log;

public class OnSwipeTouchListener implements View.OnTouchListener
{
private final GestureDetector gestureDetector;

public OnSwipeTouchListener(MainActivity act) {
    gestureDetector = new GestureDetector(act, new GestureListener());
}

public void onSwipeLeft() { }

public void onSwipeRight() { }

public void onSwipeUp() { }

public void onSwipeDown() { }

public boolean onTouch(View v, MotionEvent event)
{
    return gestureDetector.onTouchEvent(event);
}

private final class GestureListener extends GestureDetector.SimpleOnGestureListener
{

private static final int SWIPE_DISTANCE_THRESHOLD = 100;
private static final int SWIPE_VELOCITY_THRESHOLD = 100;

@Override
public boolean onDown(MotionEvent e)
{

    return true;
}

@Override
public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
{

    float distanceX = e2.getX() - e1.getX();
    float distanceY = e2.getY() - e1.getY();
    if (Math.abs(distanceX) > Math.abs(distanceY) && Math.abs(distanceX) > SWIPE_DISTANCE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
        if (distanceX > 0)
            onSwipeRight();
        else
            onSwipeLeft();
        return true;
    }
    if (Math.abs(distanceY) > Math.abs(distanceX) && Math.abs(distanceY) > SWIPE_DISTANCE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
        if (distanceY > 0)
            onSwipeDown();
        else
            onSwipeUp();
        return true;
    }
    return false;
}

}

}
