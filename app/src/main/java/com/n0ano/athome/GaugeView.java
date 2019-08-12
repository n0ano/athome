package com.n0ano.athome;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;

import java.util.Calendar;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class GaugeView extends View {

private Context ctx;

private Paint dial_paint;
private Paint value_paint;
private Paint num_paint;
private Paint hilo_paint;

private int margin;
private int hilo_w;
private int dial_w;

private float current;
private float low_a;
private float hi_a;

private float cur_value;
private float cur_min;
private float cur_max;
private boolean reset_minmax;

private float min_deg;
private float max_deg;

public GaugeView(Context ctx, AttributeSet attrs)
{
	super(ctx, attrs);

	this.ctx = ctx;

    margin = 20;
    hilo_w = 6;
    dial_w = 2;
    min_deg = 20f;
    max_deg = 100f;
    cur_value = 1000;
    cur_min = 1000;
    cur_max = -1000;
    reset_minmax = true;

	dial_paint = new Paint();
    dial_paint.setAntiAlias(true);
    dial_paint.setColor(0xffffffff);
	dial_paint.setStyle(Paint.Style.STROKE);
    dial_paint.setStrokeWidth(dial_w * 2);

	value_paint = new Paint();
    value_paint.setAntiAlias(true);
    value_paint.setColor(0xffffffff);
	value_paint.setStyle(Paint.Style.STROKE);
    value_paint.setTextSize(32f);

	num_paint = new Paint();
    num_paint.setAntiAlias(true);
    num_paint.setColor(0xffffffff);
	num_paint.setStyle(Paint.Style.STROKE);

    hilo_paint = new Paint();
    hilo_paint.setAntiAlias(true);
    hilo_paint.setColor(0xff00ff00);
	hilo_paint.setStyle(Paint.Style.STROKE);
    hilo_paint.setStrokeWidth(hilo_w * 2);
}

@Override
public void onDraw(Canvas canvas)
{
	super.onDraw(canvas);

	int w = getMeasuredWidth();
	int h = getMeasuredHeight();

//Paint bkg = new Paint();
//bkg.setStyle(Paint.Style.FILL);
//bkg.setAntiAlias(true);
//bkg.setColor(0xff000000);
//canvas.drawRect(0, 0, w, h, bkg);

    float r = (float)(margin + dial_w);

    // draw high/low arc
    if (cur_min != 1000)
        min_max(w, h, r, canvas, hilo_paint);

    // draw dial
    dial(w, h, (int)r, 20, 100, 8, canvas, dial_paint, num_paint);

    // draw current value tick
    if (cur_value != 1000) {
        canvas.drawText(String.format("%.1f", cur_value), (w/2) - 30, (h/2) + 8, value_paint);
        line((w/2) - (margin + dial_w) + 8, (w/2) - (margin + dial_w) - 40, deg2angle(cur_value), w/2, h/2, canvas, dial_paint);
    }
}

public void set_value(float val)
{

    cur_value = val;
    if (val < cur_min)
        cur_min = val;
    if (val > cur_max)
        cur_max = val;
    Calendar c = Calendar.getInstance();
    int h = c.get(Calendar.HOUR_OF_DAY);
    int m = c.get(Calendar.MINUTE);
    int s = c.get(Calendar.SECOND);
    if ((h == 0) && (m == 0) && (s == 0) && reset_minmax) {
Log.d("reset min/max");
        reset_minmax = false;
        cur_min = val;
        cur_max = val;
    } else {
        reset_minmax = true;
    }
    invalidate();
}

public void set_value(String val)
{

    try {
        Float v = Float.valueOf(val);
        set_value(v);

    } catch (Exception e) {
        Log.d("Temp: bad float - " + val);
    } 
}

private float deg2angle(float deg)
{

    if (deg < min_deg)
        deg = min_deg;
    if (deg > max_deg)
        deg = max_deg;
    return ((deg - min_deg)/(max_deg - min_deg) * 270f) + (90 + 45);
}

private void min_max(int w, int h, float r, Canvas c, Paint p)
{

    float off = r + (dial_w + hilo_w);
    Shader grad = new LinearGradient(0, 0, w, h/2, Color.BLUE, Color.RED, Shader.TileMode.MIRROR);
    hilo_paint.setShader(grad);
    float start = deg2angle(cur_min);
    float angle = deg2angle(cur_max) - deg2angle(cur_min);
    c.drawArc(off, off, (float)(w - off), (float)(h - off), start, angle, false, p);
}

private void dial(int w, int h, int m, int min, int max, int ticks, Canvas c, Paint p, Paint num_p)
{
    float a;

    c.drawArc(m, m, w - m, h - m, 90 + 45, 270, false, p);
    int delta = (max - min) / ticks;
    do {
        line((w/2) - m, (w/2) - m - 10, deg2angle(min), w/2, h/2, c, p);
        Path path = new Path();
        a = deg2angle(min);
        if (min > 99) {
            path.moveTo(polar_x((w/2) - m - 34, a) + (w/2), polar_y((w/2) - m - 34, a) + (w/2));
            path.lineTo(polar_x((w/2) - m - 12, a) + (w/2), polar_y((w/2) - m - 12, a) + (w/2));
        } else if (a > 260 && a < 280) {
            path.moveTo((w/2) - 10, m + 20);
            path.lineTo((w/2) + 10, m + 20);
        } else if (a < 270) {
            path.moveTo(polar_x((w/2) - m - 12, a) + (w/2), polar_y((w/2) - m - 12, a) + (w/2));
            path.lineTo(w/2, w/2);
        } else {
            path.moveTo(polar_x((w/2) - m - 26, a) + (w/2), polar_y((w/2) - m - 26, a) + (w/2));
            path.lineTo(polar_x((w/2) - m - 12, a) + (w/2), polar_y((w/2) - m - 12, a) + (w/2));
        }
        c.drawTextOnPath(Integer.toString(min), path, 0, 4, num_p);
        min += delta;
    } while (ticks-- > 0);
}

private void line(float max, float min, float a, int off_x, int off_y, Canvas c, Paint p)
{

    c.drawLine(polar_x(max, a) + off_x, polar_y(max, a) + off_y,
               polar_x(min, a) + off_x, polar_y(min, a) + off_y,
               p);
}

private float polar_x(float r, float a)
{

    return (float)(r * cos(Math.toRadians(a)));
}

private float polar_y(float r, float a)
{

    return (float)(r * sin(Math.toRadians(a)));
}

}
