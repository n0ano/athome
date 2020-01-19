package com.n0ano.athome.SS;

import java.util.ArrayList;
import java.util.HashMap;

import com.n0ano.athome.C;
import com.n0ano.athome.Log;

public class Utils
{

public static int parse_gen(String str)
{

    int idx = str.indexOf(";");
    if (idx >= 0)
        str = str.substring(0, idx);
    return Integer.parseInt(str);
}

public static String img_info(ImageEntry entry)
{
    String info;

    if (entry.get_check())
        info = ((entry.get_type() == C.IMAGE_LOCAL) ? "L" : "R");
    else
        info = ((entry.get_type() == C.IMAGE_LOCAL) ? "l" : "r");
    int r = entry.get_rotate();
    if (r != 0)
        info = info + "R" + String.format("%03d", r);
    return info;
}

public static ArrayList<ImageEntry> parse_names(String str)
{

Log.d("DDD-SS", "parse list - " + str.substring(0, (str.length() < 64) ? str.length() : 64) + "...");
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
Log.d("DDD-SS", "parse_images - " + images.size());
    return images;
}

public static HashMap<String, String> parse_images(String str)
{
    ImageEntry img;

    HashMap<String, String> map = new HashMap<String, String>();
    ArrayList<ImageEntry> images = parse_names(str);
    for (int i = 0; i < images.size(); i++) {
        img = images.get(i);
        map.put(img.get_name(), img_info(img));
    }
    return map;
}

}
