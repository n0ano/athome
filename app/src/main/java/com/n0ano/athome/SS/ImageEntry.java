package com.n0ano.athome.SS;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.os.SystemClock;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.n0ano.athome.Log;

import java.io.File;
import java.io.FileInputStream;
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
long ts;
boolean checked;

int generation;
String list;
String title = "";
Bitmap bitmap_th;

private HashMap<String, String> meta;

public ImageEntry(String name, String list, int gen)
{
    int idx;
    long ts;
    char t;

    this.checked = true;
    idx = 0;
    t = name.charAt(0);
    if (t == 'F' || t == 'f') {
        // name from image server list
        this.type = ((t == 'F') ? C.IMAGE_REMOTE : C.IMAGE_LOCAL);
        idx = name.indexOf(":");
        this.ts = Long.parseLong(name.substring(1, idx), 10);
    } else {
        this.type = ((t == 'L' || t == 'l') ? C.IMAGE_LOCAL : C.IMAGE_REMOTE);
        if (t == 'l' || t == 'r')
            this.checked = false;
        this.ts = 0;
    }
    this.name = name.substring(idx + 1);
    this.list = list;
    this.width = 0;
    this.height = 0;
    this.bitmap_th = null;
    this.generation = gen;
//if (type == C.IMAGE_LOCAL) {
//    try {
//        ExifInterface exif = new ExifInterface(this.name);
//        //Log.d("DDD-SS", this.name + " => " + exif.getAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION));
//    } catch (Exception e) {
//        //Log.d("DDD-SS", this.name + " => no Comment attribute");
//    }
//}
}

public String get_name() { return name; }

public int get_type() { return type; }

public long get_ts() { return ts; }

public int get_width() { return width; }
public void set_width(int w) { width = w; }

public int get_height() { return height; }
public void set_height(int h) { height = h; }

public void do_rotate(final int r, final ScreenInfo info, final DoneCallback cb)
{

    if (r == 0)
        return;
    if (type == C.IMAGE_LOCAL)
        return;
    new Thread(new Runnable() {
        public void run() {
            rotate_remote(r, info);
            cb.done(null);
        }
    }).start();
}

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
    }
}

public String info()
{
    String inf;

    if (checked)
        inf = ((type == C.IMAGE_LOCAL) ? "L" : "R");
    else
        inf = ((type == C.IMAGE_LOCAL) ? "l" : "r");
    return inf;
}

public void get_bitmap(final int r, final ScreenInfo ss_info, final DoneCallback cb)
{

    new Thread(new Runnable() {
        public void run() {
            Bitmap bitmap = get_bits(ss_info, r, ss_info.width, ss_info.height, C.ASPECT_FIT);
            cb.done(bitmap);
        }
    }).start();
}

public void get_thumb(final int r, final ScreenInfo ss_info, final DoneCallback cb)
{

    new Thread(new Runnable() {
        public void run() {
            bitmap_th = get_bits(ss_info, r, C.THUMB_X, C.THUMB_Y, C.ASPECT_STRETCH);
            cb.done(null);
        }
    }).start();
}

private Bitmap get_bits(ScreenInfo ss_info, int r, int w, int h, int aspect)
{
	InputStream in_rdr;
    Bitmap b, bitmap;

	try {
        in_rdr = open_image(ss_info, r, w, h);
        b = BitmapFactory.decodeStream(in_rdr);
        in_rdr.close();
        if (type == C.IMAGE_LOCAL) {
            width = b.getWidth();
            height = b.getHeight();
            if (aspect == C.ASPECT_FIT) {
                w = C.scalex(ss_info.width, ss_info.height, width, height);
                h = C.scaley(ss_info.width, ss_info.height, width, height);
            } else {
                w = ss_info.width;
                h = ss_info.height;
            }
            bitmap = Bitmap.createScaledBitmap(b, w, h, false);
            generation = 0;
        } else {
            bitmap = b;
            generation = Integer.parseInt(meta.get("E"), 10);
            String str = meta.get("W");
            if (str != null)
                width = Integer.parseInt(str, 10);
            str = meta.get("H");
            if (str != null)
                height = Integer.parseInt(str, 10);
        }
	} catch (Exception e) {
		Log.d("DDD-SS", "get image failed - " + e);
		return null;
	}
    if (bitmap == null)
        Log.d("DDD-SS", name + ":image decode failed");
    return bitmap;
}

private void rotate_remote(int r, ScreenInfo ss_info)
{
    String url;
	InputStream in_rdr;

	try {
        url = ss_info.server +
                C.CGI_BIN +
                "?rotate" +
                "&list=" + list +
                "&name=" + URLEncoder.encode(name) +
                "&r=" + r;
        Log.d("DDD-SS", "rotate image " + url);
        Authenticator.setDefault(new CustomAuthenticator(ss_info.user, ss_info.pwd));
		in_rdr = new URL(url).openStream();
        meta = C.get_meta(in_rdr, new HashMap<String, String>());
	} catch (Exception e) {
		Log.d("DDD-SS", "get http image failed - " + e);
	}
}

private InputStream open_http(ScreenInfo ss_info, int r, int width, int height)
{
    String url;
	InputStream in_rdr;
    long stime, etime;

	try {
        stime = SystemClock.elapsedRealtime();
        url = ss_info.server +
                C.CGI_BIN +
                "?get" +
                "&host=" + C.base(ss_info.host) +
                "&list=" + list +
                "&name=" + URLEncoder.encode(name) +
                "&w=" + width +
                "&h=" + height +
                "&r=" + r;
        Authenticator.setDefault(new CustomAuthenticator(ss_info.user, ss_info.pwd));
		in_rdr = new URL(url).openStream();
        etime = SystemClock.elapsedRealtime();
        Log.d("DDD-SS", "get image(" + (etime - stime) + "):..." + url.substring((ss_info.server + C.CGI_BIN).length()));
        meta = C.get_meta(in_rdr, new HashMap<String, String>());
	} catch (Exception e) {
		Log.d("DDD-SS", "get http image failed - " + e);
		return null;
	}
    title = meta.get("T");
    title = ((title != null) ? title : "");
    return in_rdr;
}

private InputStream open_local(ScreenInfo ss_info, int r, int width, int height)
{

    try {
        InputStream inp = new FileInputStream(new File(name));
        meta = new HashMap<String, String>();
        meta.put("E", "0");
        return inp;
    } catch (Exception e) {
        return null;
    }
}

private InputStream open_image(ScreenInfo ss_info, int r, int width, int height)
{

    if (type == C.IMAGE_REMOTE)
        return open_http(ss_info, r, width, height);
    else if (type == C.IMAGE_LOCAL)
        return open_local(ss_info, r, width, height);
    return null;
}

@Override
public int compareTo(ImageEntry e)
{

        if (e == null) {
            Log.d("DDD-TS", "null compare object");
            return 1;
        }
        if (e.ts > ts)
            return 1;
        else if (e.ts < ts)
            return -1;
        return 0;
}

}
