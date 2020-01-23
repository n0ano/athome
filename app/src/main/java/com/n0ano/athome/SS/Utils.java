package com.n0ano.athome.SS;

import java.util.ArrayList;
import java.util.HashMap;

import com.n0ano.athome.C;
import com.n0ano.athome.Log;

public class Utils
{

public static int THUMB_X =     150;
public static int THUMB_Y =     150;

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

public static ArrayList<ImageEntry> parse_names(String str)
{

    ArrayList<ImageEntry> images = new ArrayList<ImageEntry>();
    String[] imgs = str.split(";");
    if (imgs.length > 1) {
        int gen = Integer.parseInt(imgs[0], 10);
        for (int i = 1; i < imgs.length; i++) {
            String name = imgs[i];
            if (!name.isEmpty()) {
                images.add(new ImageEntry(name, gen, null));
            }
        }
    }
    return images;
}

public static HashMap<String, String> parse_images(String str)
{
    ImageEntry img;

    HashMap<String, String> map = new HashMap<String, String>();
    ArrayList<ImageEntry> images = parse_names(str);
    for (int i = 0; i < images.size(); i++) {
        img = images.get(i);
        map.put(img.get_name(), img.info());
    }
    return map;
}

}
