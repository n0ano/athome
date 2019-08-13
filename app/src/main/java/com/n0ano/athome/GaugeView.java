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

private int w;
private int h;
private int cx;
private int cy;

private float density;

private Paint dial_paint;
private Paint deg_paint;
private Paint value_paint;
private Paint num_paint;
private Paint hilo_paint;

private int margin;

private float dial_w;
private float hilo_w;
private float tick_max;
private float tick_min;
private float deg_size;
private float tick_size;

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

    density = getResources().getDisplayMetrics().density;
    margin = 20;

    dial_w = 1 * density;
    hilo_w = 14 * density;
    deg_size = 32 * density;
    tick_size = 10 * density;
    tick_max = 8 * density;
    tick_min = -40 * density;

    min_deg = 20f;
    max_deg = 100f;
    cur_value = 1000;
    cur_min = 1000;
    cur_max = -1000;
    reset_minmax = true;

	dial_paint = brush(0xff00ffff, dial_w * 2);

    deg_paint = brush(0xffffffff, dial_w * 2);

	value_paint = brush(0xffffffff, 1);
	value_paint.setStyle(Paint.Style.FILL);
    value_paint.setTextSize(deg_size);

	num_paint = brush(0xff00ffff, 1);
	num_paint.setStyle(Paint.Style.FILL);
    num_paint.setTextSize(tick_size);

    hilo_paint = brush(0xff00ff00, hilo_w * 2);
}

@Override
public void onDraw(Canvas canvas)
{
	super.onDraw(canvas);

	w = getMeasuredWidth();
	h = getMeasuredHeight();
    cx = w/2;
    cy = h/2;

//Paint bkg = new Paint();
//bkg.setStyle(Paint.Style.FILL);
//bkg.setAntiAlias(true);
//bkg.setColor(0xff000000);
//canvas.drawRect(0, 0, w, h, bkg);

    // draw high/low arc
    if (cur_min != 1000)
        hilo(canvas, hilo_paint);

    // draw dial
    dial(20, 100, 8, canvas, dial_paint, num_paint);

    // draw current value tick
    if (cur_value != 1000) {
        pointer(w, h, margin, cur_value, canvas, value_paint, deg_paint);
    }
}

public void set_dimensions(int tick_max, int tick_min, int hilo_w, int deg_size, int tick_size)
{

    this.tick_max = tick_max;
    this.tick_min = tick_min;

    this.hilo_w = hilo_w;
    hilo_paint.setStrokeWidth(hilo_w);

    this.deg_size = deg_size;
    value_paint.setTextSize(deg_size);

    this.tick_size = tick_size;
    num_paint.setTextSize(tick_size);
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

private Paint brush(int color, float width)
{

    Paint p = new Paint();
    p.setAntiAlias(true);
	p.setStyle(Paint.Style.STROKE);
    p.setColor(color);
    p.setStrokeWidth(width);
    return p;
}

private float deg2angle(float deg)
{

    if (deg < min_deg)
        deg = min_deg;
    if (deg > max_deg)
        deg = max_deg;
    return ((deg - min_deg)/(max_deg - min_deg) * 270f) + (90 + 45);
}

private void hilo(Canvas c, Paint p)
{

    float off = margin + (dial_w*2) + hilo_w;
    Shader grad = new LinearGradient(0, 0, w, h/2, Color.BLUE, Color.RED, Shader.TileMode.MIRROR);
    hilo_paint.setShader(grad);
    float start = deg2angle(cur_min);
    float angle = deg2angle(cur_max) - deg2angle(cur_min);
    c.drawArc(off, off, (float)(w - off), (float)(h - off), start, angle, false, p);
}

private void dial(int min, int max, int ticks, Canvas c, Paint p, Paint num_p)
{
    float a, tw;
    String num;

    float m = margin + dial_w;
    float l = 10 * density;
    c.drawArc(m, m, w - m, h - m, 90 + 45, 270, false, p);
    int delta = (max - min) / ticks;
    do {
        num = Integer.toString(min);
        tw = num_p.measureText(num);
        line(cx - m, cx - m - l, deg2angle(min), c, p);
        Path path = new Path();
        a = deg2angle(min);
        if (a > 260 && a < 280) {
            path.moveTo(cx - (tw/2), m + l + tick_size);
            path.lineTo(cx + (tw/2), m + l + tick_size);
        } else if (a < 270) {
            path.moveTo(polar_x(cx - m - l, a), polar_y(cx - m - l, a));
            path.lineTo(cx, cx);
        } else {
            path.moveTo(polar_x(cx - m - l - tw, a), polar_y(cx - m - l - tw, a));
            path.lineTo(polar_x(cx - m - l, a), polar_y(cx - m - l, a));
        }
        c.drawTextOnPath(num, path, 0, 4, num_p);
        min += delta;
    } while (ticks-- > 0);
}

private void pointer(int w, int h, int m, float v, Canvas c, Paint dp, Paint pp)
{

    String deg = String.format("%.1f", v);
    float tw = dp.measureText(deg);
    c.drawText(deg, cx - (tw/2), cy + 8, dp);
    line(cx - (m + dial_w) + tick_max, cx - (m + dial_w) + tick_min, deg2angle(v), c, pp);
}

private void line(float max, float min, float a, Canvas c, Paint p)
{

    c.drawLine(polar_x(max, a), polar_y(max, a),
               polar_x(min, a), polar_y(min, a),
               p);
}

private float polar_x(float r, float a)
{

    return (float)((r * cos(Math.toRadians(a))) + cx);
}

private float polar_y(float r, float a)
{

    return (float)((r * sin(Math.toRadians(a))) + cx);
}

}
