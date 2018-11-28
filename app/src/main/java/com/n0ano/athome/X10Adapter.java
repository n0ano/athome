package com.n0ano.athome;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.n0ano.athome.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

// Created by n0ano on 10/10/16.
//
public class X10Adapter extends ArrayAdapter<X10Device> {

MainActivity act;

int max;
ArrayList<X10Device> devices;

public X10Adapter(MainActivity act)
{

    super(act, 0);

    this.act = act;

    devices = new ArrayList<X10Device>();
    max = 0;
}

void add_device(int idx, X10Device dev)
{

    devices.add(idx, dev);
    ++max;
}

@Override
public void clear()
{

    devices = new ArrayList<X10Device>();
    max = 0;
}

@Override
public int getCount()
{

    return max;
}

@Override
public X10Device getItem(int idx)
{

    return devices.get(idx);
}

}
