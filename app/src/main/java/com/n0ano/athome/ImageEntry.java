package com.n0ano.athome;

public class ImageEntry implements Comparable<ImageEntry>
{

String name;
int type;
int ts;

public ImageEntry(String name, int type, int ts)
{

    this.name = name;
    this.type = type;
    this.ts = ts;
}

public ImageEntry(String name, int type)
{

    int idx = name.indexOf(":");
    this.name = name.substring(idx + 1);
    this.type = type;
    this.ts = Integer.parseInt(name.substring(0, idx), 10);
}

public String get_name() { return name; }

public int get_type() { return type; }

public int get_ts() { return ts; }

@Override
public int compareTo(ImageEntry e)
{

        return e.ts - ts;
}

}
