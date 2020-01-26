package com.n0ano.athome.SS;

import android.widget.TextView;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

import com.n0ano.athome.Log;

//
// Created by n0ano on 11/7/18.
//
public class C {

public final static String CGI_BIN = "/cgi-bin/explore.dd/get_frame";

public final static int IMAGE_LOCAL =   0;
public final static int IMAGE_REMOTE =  1;

public static final int GRID_SHOW =   0;
public static final int GRID_CHECK =   1;

public static int grid_type = GRID_SHOW;

public static final int THUMB_X =     150;
public static final int THUMB_Y =     150;

public static TextView loading_name = null;

public static String suffix(String str)
{

    int idx = str.lastIndexOf(":");
    return (idx >= 0) ? str.substring(idx + 1) : "";
}

public static String base(String str)
{

    int idx = str.lastIndexOf(":");
    return (idx >= 0) ? str.substring(0, idx) : str;
}

private static int b2int(byte[] b)
{

    int i = ((b[3] & 0xFF) << 24) | 
            ((b[2] & 0xFF) << 16) | 
            ((b[1] & 0xFF) << 8 ) | 
            ((b[0] & 0xFF) << 0 );
    return i;
}

private static int read_it(InputStream in, byte[] buf, int off, int n)
{
    int l, count;

    count = 0;
    while (n > 0) {
        try {
            l = in.read(buf, off, n);
            if (l <= 0)
                return count;
            off += l;
            n -= l;
            count += l;
        } catch (Exception e) {
            Log.d("DDD-SS", "read error - " + e);
            return count;
        }
    }
    return count;
}

public static String meta_line(InputStream in)
{
    String str;
    byte[] len = new byte[4];

    if (read_it(in, len, 0, 4) != 4) {
        Log.d("DDD-SS", "short read on count");
        return "E";
    }
    int n = b2int(len);
    byte[] buf = new byte[n];
    if (read_it(in, buf, 0, n) != n) {
        Log.d("DDD-SS", "short read on line");
        return "E";
    }
    str = new String(buf, StandardCharsets.UTF_8);
    return str;
}

public static HashMap<String, String> get_meta(InputStream in, HashMap<String, String> map)
{
    String str;
    String type;
    byte[] len = new byte[4];

    for (;;) {
        str = meta_line(in);
        type = str.substring(0, 1);
        str = str.substring(1);
        map.put(type, str);
        switch (type.charAt(0)) {

        case 'E':       // end of meta data
            //Log.d("DDD-SS", "E:end of metadata");
            return map;

        case 'X':       // end of meta data, no such file
            //Log.d("DDD-SS", "X:no file");
            return map;

        case 'F':       // file name
            //Log.d("DDD-SS", "F:" + str);
            break;

        case 'T':       // title
            //Log.d("DDD-SS", "T:" + str);
            break;

        default:        // unknown tag
            //Log.d("DDD-SS", type + ":" + str);
            break;

        }
    }
}

public static String get_from(String str)
{

    int idx = str.indexOf("/");
    return (idx < 0) ? str : str.substring(0, idx);
}

public static int parse_gen(String str)
{

    if (str.isEmpty())
        return 0;
    int idx = str.indexOf(";");
    if (idx >= 0)
        str = str.substring(0, idx);
    return Integer.parseInt(str);
}

public static String last(String name)
{

    int idx = name.lastIndexOf("/");
    return (idx < 0) ? name : name.substring(idx + 1);
}

private static String get_info(String name)
{

    return (name.charAt(1) == 'R') ? name.substring(0, 5) : name.substring(0, 1);
}

public static String list2str(int gen, ArrayList<ImageEntry> images)
{
    ImageEntry entry;

    String list = "" + gen;
    for (int i = 0; i < images.size(); i++) {
        entry = images.get(i);
        list = list + ";" + entry.info() + entry.get_name();
    }
    return list;
}

public static ArrayList<ImageEntry> parse_images(String str, String list)
{
    int gen;
    String name;

    ArrayList<ImageEntry> images = new ArrayList<ImageEntry>();
    String[] imgs = str.split(";");
    if (imgs.length > 1) {
        gen = Integer.parseInt(imgs[0], 10);
        for (int i = 1; i < imgs.length; i++) {
            name = imgs[i];
            if (!name.isEmpty()) {
                images.add(new ImageEntry(name, list, gen));
            }
        }
    }
    return images;
}

public static HashMap<String, String> parse_names(String str)
{
    String name, info;

    HashMap<String, String> map = new HashMap<String, String>();
    String[] imgs = str.split(";");
    if (imgs.length > 1) {
        for (int i = 1; i < imgs.length; i++) {
            name = imgs[i];
            if (!name.isEmpty()) {
                info = get_info(name);
                map.put(name.substring(info.length()), info);
            }
        }
    }
    return map;
}

}
