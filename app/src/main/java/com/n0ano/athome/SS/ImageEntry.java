package com.n0ano.athome.SS;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import com.n0ano.athome.Log;

import java.io.InputStream;
import java.net.Authenticator;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;

public class ImageEntry implements Comparable<ImageEntry>
{

String name;
int width;
int height;
int type;
int ts;
boolean checked;
int rotate;

int generation;
String list;
String title = "";
static Bitmap bitmap;
Bitmap bitmap_th;

private HashMap<String, String> meta;

public ImageEntry(String name, String list, int gen)
{
    int idx, ts;
    char t;

    this.checked = true;
    idx = 0;
    t = name.charAt(0);
    if (t == 'F') {
        // name from image server list
        this.type = C.IMAGE_REMOTE;
        idx = name.indexOf(":");
        this.ts = Integer.parseInt(name.substring(1, idx), 10);
    } else {
        this.type = ((t == 'L' || t == 'l') ? C.IMAGE_LOCAL : C.IMAGE_REMOTE);
        if (t == 'l' || t == 'r')
            this.checked = false;
        if (name.charAt(1) == 'R') {
            this.rotate = Integer.parseInt(name.substring(idx + 1, idx + 4), 10);
            idx += 4;
        }
        this.ts = 0;
    }
    this.name = name.substring(idx + 1);
    this.list = list;
    this.width = 0;
    this.height = 0;
    this.bitmap = null;
    this.bitmap_th = null;
    this.generation = gen;
}

public String get_name() { return name; }

public int get_type() { return type; }

public int get_ts() { return ts; }

public int get_width() { return width; }
public void set_width(int w) { width = w; }

public int get_height() { return height; }
public void set_height(int h) { height = h; }

public int get_rotate() { return rotate; }
public void set_rotate(int r) { rotate = r; }

public boolean get_check() { return checked; }
public void set_check(boolean ck) { checked = ck; }

public int get_generation() { return generation; }
public void set_generation(int g) { generation = g; }

public String get_title() { return title; }
public void set_title(String t) { title = t; }

public void enable(String info)
{

    checked = true;
    if (info != null) {
        if (info.charAt(0) == 'l' || info.charAt(0) == 'r')
            checked = false;
        if ((info.length() > 1) && (info.charAt(1) == 'R'))
            rotate = Integer.parseInt(info.substring(2,5));
    }
}

public String info()
{
    String inf;

    if (checked)
        inf = ((type == C.IMAGE_LOCAL) ? "L" : "R");
    else
        inf = ((type == C.IMAGE_LOCAL) ? "l" : "r");
    if (rotate != 0)
        inf = inf + "R" + String.format("%03d", rotate);
    return inf;
}

public void get_bitmap(final Activity act, final ScreenInfo ss_info, final ImageView view, final DoneCallback callback)
{

    new Thread(new Runnable() {
        public void run() {
            bitmap = get_bits(ss_info, ss_info.width, ss_info.height);
            width = ss_info.width;
            height = ss_info.height;
            act.runOnUiThread(new Runnable() {
                public void run() {
                    view.setImageBitmap(bitmap);
                }
            });
            if (callback != null)
                callback.done();
        }
    }).start();
}

public void get_thumb(final Activity act, final ScreenInfo ss_info, final ImageView view, final DoneCallback callback)
{

    new Thread(new Runnable() {
        public void run() {
            if (bitmap_th == null)
                bitmap_th = get_bits(ss_info, C.THUMB_X, C.THUMB_Y);
            act.runOnUiThread(new Runnable() {
                public void run() {
                    view.setImageBitmap(bitmap_th);
                }
            });
            if (callback != null)
                callback.done();
        }
    }).start();
}

private Bitmap get_bits(ScreenInfo ss_info, int width, int height)
{
	InputStream in_rdr;

	try {
        in_rdr = open_image(ss_info, width, height);
        bitmap = BitmapFactory.decodeStream(in_rdr);
        in_rdr.close();
        generation = Integer.parseInt(meta.get("E"), 10);
	} catch (Exception e) {
		Log.d("DDD-SS", "get image failed - " + e);
		return null;
	}
    if (bitmap == null)
        Log.d("DDD-SS", "image decode failed");
    return bitmap;
}

private InputStream open_http(ScreenInfo ss_info, int width, int height)
{
    String url;
	InputStream in_rdr;

	try {
        url = ss_info.server +
                C.CGI_BIN +
                "?get" +
                "&host=" + C.base(ss_info.host) +
                "&list=" + list +
                "&name=" + URLEncoder.encode(name) +
                "&w=" + width +
                "&h=" + height +
                "&r=" + rotate;
        Log.d("DDD-SS", "get image from " + url);
        Authenticator.setDefault(new CustomAuthenticator(ss_info.user, ss_info.pwd));
		in_rdr = new URL(url).openStream();
        meta = C.get_meta(in_rdr, new HashMap<String, String>());
	} catch (Exception e) {
		Log.d("DDD-SS", "get http image failed - " + e);
		return null;
	}
    title = meta.get("T");
    title = ((title != null) ? title : "");
    return in_rdr;
}

private InputStream open_image(ScreenInfo ss_info, int width, int height)
{

    if (type == C.IMAGE_REMOTE)
        return open_http(ss_info, width, height);
//    else if (type == C.IMAGE_LOCAL)
//        return file open
    return null;
}

@Override
public int compareTo(ImageEntry e)
{

        return e.ts - ts;
}

}
