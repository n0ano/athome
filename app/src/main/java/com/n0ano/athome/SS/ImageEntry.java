package com.n0ano.athome.SS;

import android.graphics.Bitmap;

public class ImageEntry implements Comparable<ImageEntry>
{

String name;
int width;
int height;
int type;
int ts;
boolean checked;
int rotate;
Bitmap bitmap;

public ImageEntry(String name, int type, int ts, int rotate, ScreenInfo info)
{

    this.name = name;
    this.type = type;
    this.ts = ts;
    this.rotate = rotate;
    this.width = 0;
    this.height = 0;
    this.bitmap = null;
    this.checked = true;
    if (info != null)
        get_thumb(info);
}

public ImageEntry(String name, int type, ScreenInfo info)
{

    int idx = name.indexOf(":");
    this.name = name.substring(idx + 1);
    this.type = type;
    this.ts = Integer.parseInt(name.substring(0, idx), 10);
    this.rotate = 0;
    this.width = 0;
    this.height = 0;
    this.bitmap = null;
    this.checked = true;
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

public void enable(String info)
{

    checked = false;
    if (info != null) {
        if ((info.length() > 1) && (info.charAt(1) == 'R'))
            rotate = Integer.parseInt(info.substring(2,5));
        checked = true;
    }
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
