package com.n0ano.athome.SS;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.InputStream;
import java.net.Authenticator;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import com.n0ano.athome.Log;

public class ImageLists
{

int listno;
String name;

String name_1st;
int generation_1st;
ArrayList<ImageEntry> images_1st;

String name_2nd;
int generation_2nd;
ArrayList<ImageEntry> images_2nd;

ImageFind finder;

public ImageLists(String name1, String name2, ImageFind finder)
{

    this.name_1st = name1;
    this.generation_1st = 0;
    this.images_1st = new ArrayList<ImageEntry>();

    this.name_2nd = name2;
    this.generation_2nd = 0;
    this.images_2nd = new ArrayList<ImageEntry>();

    this.listno = 0;
    this.finder = finder;
}

public int get_listno() { return listno; }
public void set_listno(int i)
{

    if (name_1st.isEmpty())
        listno = 0;
    else
        listno = i;
}

public String get_name() { return (listno == 0) ? name_1st : name_2nd; }

public int get_size() { return (listno == 0) ? images_1st.size() : images_2nd.size(); }

public ImageEntry get_image(int idx) { return (listno == 0) ? images_1st.get(idx) : images_2nd.get(idx); }

public int get_generation() { return (listno == 0) ? generation_1st : generation_2nd; }
public void set_generation(int g)
{

    if (listno == 0)
        generation_1st = g;
    else
        generation_2nd = g;
}

public void set_images(ArrayList<ImageEntry> imgs)
{
    if (listno == 0)
        images_1st = imgs;
    else
        images_2nd = imgs;
}

public int get_index(ImageEntry e) { return (listno == 0) ? images_1st.indexOf(e) : images_2nd.indexOf(e); }

public String list2str()
{

    String list = C.list2str(get_generation(), ((listno == 0) ? images_1st : images_2nd));
    return list;
}

public ImageEntry next_image(int delta)
{
    int first;
    ImageEntry img;

    if (get_size() <= 0)
        return null;

    if (get_size() < 2)
        return get_image(0);

    String max = P.get("image_last:" + get_name(), Integer.toString(get_size()));
    int next = Integer.parseInt(max, 10);
    if (delta == 0) {
        if (next >= get_size()) {
            next = get_size() - 1;
            P.put("image_last:" + get_name(), Integer.toString(next));
        }
        return get_image(next);
    }

    first = next;
    while ((next += delta) != first) {
        if (next >= get_size())
            next = 0;
        else if (next < 0)
            next = get_size() - 1;
        img = get_image(next);
        if (img.get_check()) {
            P.put("image_last:" + get_name(), Integer.toString(next));
            return img;
        }
    }
    return null;
}

public void scan(HashMap<String, String> map, ScreenInfo info, boolean thumb)
{

    if (listno == 0)
        images_1st = finder.scan(info, name_1st, thumb);
    else
        images_2nd = finder.scan(info, name_2nd, thumb);

    for (ImageEntry img : ((listno == 0) ? images_1st : images_2nd))
        img.enable(map.get(img.get_name()));
}

public void parse(String saved)
{

    if (listno == 0)
        images_1st = C.parse_images(saved, name_1st);
    else
        images_2nd = C.parse_images(saved, name_2nd);
}

}
