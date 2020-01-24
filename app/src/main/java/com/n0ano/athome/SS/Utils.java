package com.n0ano.athome.SS;

import java.util.ArrayList;
import java.util.HashMap;

import com.n0ano.athome.C;
import com.n0ano.athome.Log;

public class Utils
{

public static final int GRID_SHOW =   0;
public static final int GRID_CHECK =   1;

public static int grid_type = GRID_SHOW;

public static final int THUMB_X =     150;
public static final int THUMB_Y =     150;

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

public static ArrayList<ImageEntry> parse_images(String str)
{
    int gen;
    String name;

Log.d("DDD-SS", "parse_images - " + str);
    ArrayList<ImageEntry> images = new ArrayList<ImageEntry>();
    String[] imgs = str.split(";");
    if (imgs.length > 1) {
        gen = Integer.parseInt(imgs[0], 10);
        for (int i = 1; i < imgs.length; i++) {
            name = imgs[i];
            if (!name.isEmpty()) {
                images.add(new ImageEntry(name, gen));
            }
        }
    }
    return images;
}

public static HashMap<String, String> parse_names(String str)
{
    String name, info;

Log.d("DDD-SS", "parse_names - " + str);

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
