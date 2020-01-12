package com.n0ano.athome;

import android.graphics.Bitmap;

public class ImageEntry implements Comparable<ImageEntry>
{

String name;
int type;
int ts;
Bitmap bitmap;

public ImageEntry(String name, int type, int ts, ScreenInfo info)
{

    this.name = name;
    this.type = type;
    this.ts = ts;
    this.bitmap = null;
    if (info != null)
        get_thumb(info);
}

public ImageEntry(String name, int type, ScreenInfo info)
{

    int idx = name.indexOf(":");
    this.name = name.substring(idx + 1);
    this.type = type;
    this.ts = Integer.parseInt(name.substring(0, idx), 10);
    this.bitmap = null;
    if (info != null)
        get_thumb(info);
}

public String get_name() { return name; }

public int get_type() { return type; }

public int get_ts() { return ts; }

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
