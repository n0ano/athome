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

public class ClockView extends View {

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
private Paint hr_paint;
private Paint min_paint;
private Paint sec_paint;
private Paint dot_paint;

private int margin;

private float dial_w;
private float tick_max;
private float tick_min;
private float deg_size;
private float tick_size;

private float min_deg;
private float max_deg;

public ClockView(Context ctx, AttributeSet attrs)
{
	super(ctx, attrs);

	this.ctx = ctx;

    density = getResources().getDisplayMetrics().density;
    margin = 20;

    dial_w = 1 * density;
    deg_size = 32 * density;
    tick_size = 10 * density;
    tick_max = 8 * density;
    tick_min = -40 * density;

    min_deg = 20f;
    max_deg = 100f;

	dial_paint = brush(0xff00ffff, dial_w * 2);
    hr_paint = brush(0xffffffff, dial_w * 8);
    min_paint = brush(0xffffffff, dial_w * 4);
    sec_paint = brush(0xffff0000, dial_w * 2);
    dot_paint = brush(0xff00ffff, dial_w * 2);
	dot_paint.setStyle(Paint.Style.FILL);

    deg_paint = brush(0xffffffff, dial_w * 2);

	value_paint = brush(0xffffffff, 1);
	value_paint.setStyle(Paint.Style.FILL);
    value_paint.setTextSize(deg_size);

	num_paint = brush(0xff00ffff, 1);
	num_paint.setStyle(Paint.Style.FILL);
    num_paint.setTextSize(tick_size);
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

    // draw dial
    dial(canvas, dial_paint, num_paint);

    // draw hands
    hands(canvas, hr_paint, min_paint, sec_paint, dot_paint);
}

public void update()
{

    invalidate();
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

private void dial(Canvas c, Paint p, Paint num_p)
{
    int deg;
    float a, tw;
    String num;
    Path path;

    float m = margin + dial_w;
    float l = 10 * density;
    c.drawCircle(cx, cy, cx - m, p);
    for (deg = 0; deg < 360; deg += 30)
        line(cx - m, cx - m - l, (float)deg, c, p);

    tw = num_p.measureText("12");
    path = new Path();
    path.moveTo(cx - (tw/2), m + l + tick_size);
    path.lineTo(cx + (tw/2), m + l + tick_size);
    c.drawTextOnPath("12", path, 0, 4, num_p);

    tw = num_p.measureText("6");
    path  = new Path();
    path.moveTo(cx - (tw/2), h - m - l - tick_size);
    path.lineTo(cx + (tw/2), h - m - l - tick_size);
    c.drawTextOnPath("6", path, 0, 4, num_p);

    tw = num_p.measureText("9");
    path = new Path();
    path.moveTo(polar_x(cx - m - l, 180f), polar_y(cx - m - l, 180f));
    path.lineTo(cx, cx);
    c.drawTextOnPath("9", path, 0, 4, num_p);

    tw = num_p.measureText("3");
    path = new Path();
    path.moveTo(polar_x(cx - m - l - tw, 0f), polar_y(cx - m - l - tw, 0f));
    path.lineTo(polar_x(cx - m - l, 0f), polar_y(cx - m - l, 0f));
    c.drawTextOnPath("3", path, 0, 4, num_p);
/*
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
 */
}

private void hands(Canvas c, Paint hr_p, Paint min_p, Paint sec_p, Paint dot_p)
{
    float m = margin + dial_w;

    Calendar cal = Calendar.getInstance();
    int hr = cal.get(Calendar.HOUR);
    int min = cal.get(Calendar.MINUTE);
    int sec = cal.get(Calendar.SECOND);
    float a = ((360f / 12f) * hr) + ((360f / 12f / 60f) * min) - 90f;
    line((cx/2), 0, a, c, hr_p);
    line(cx - m - tick_size, 0, (((float)min / 60f) * 360f) - 90f, c, min_p);
    line(cx - m - tick_size, 0, (((float)sec / 60f) * 360f) - 90f, c, sec_p);
    c.drawCircle(cx, cy, 8, dot_p);
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
