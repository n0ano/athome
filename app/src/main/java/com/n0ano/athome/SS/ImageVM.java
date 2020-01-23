package com.n0ano.athome.SS;

import android.arch.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.HashMap;

public class ImageVM extends ViewModel
{

private static int generation = -1;
private static HashMap<String, ImageEntry> map = new HashMap<String, ImageEntry>();

public ImageEntry get(String name, int gen, int x, int y)
{

    return map.get(name);
}

public void put(String name, ImageEntry image)
{

    map.put(name, image);
}

public int size() { return map.size(); }

public void clear() { map = new HashMap<String, ImageEntry>(); }

}
