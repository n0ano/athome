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

import com.n0ano.athome.C;
import com.n0ano.athome.Log;

public class ImageLists
{

int listno;
String name;

int generation;
ArrayList<ImageEntry> images;

public ImageLists(int listno, String name)
{

    this.listno = listno;
    this.name = name;

    generation = 0;
}

public int get_listno() { return listno; }

public String get_name() { return name; }

public int get_generation() { return generation; }
public void set_generation(int g) { generation = g; }

public ArrayList<ImageEntry> get_images() { return images; }
public void set_images(ArrayList<ImageEntry> imgs) { images = imgs; }

}
