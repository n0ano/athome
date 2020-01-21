package com.n0ano.athome.SS;

import android.graphics.Bitmap;

import com.n0ano.athome.Log;
import com.n0ano.athome.C;

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
Bitmap bitmap;

public ImageEntry(String name, int gen, ScreenInfo info)
{
    int idx, ts;
    char t;

    this.checked = true;
    idx = 0;
    t = name.charAt(0);
    if (t == 'T') {
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
    this.width = 0;
    this.height = 0;
    this.bitmap = null;
    this.generation = gen;

    if (info != null)
        get_thumb(info);
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

public void enable(String info)
{

    checked = true;
    if (info != null) {
        if ((info.length() > 1) && (info.charAt(1) == 'R'))
            rotate = Integer.parseInt(info.substring(2,5));
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
    if (rotate != 0)
        inf = inf + "R" + String.format("%03d", rotate);
    return inf;
}

private void get_thumb(ScreenInfo info)
{

    bitmap = new ImageThumb(info, this, 150, 150).get_bitmap();
}

@Override
public int compareTo(ImageEntry e)
{

        return e.ts - ts;
}

}
