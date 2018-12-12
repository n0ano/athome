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
public class OutletsAdapter extends ArrayAdapter<OutletsDevice> {

MainActivity act;

int max;
ArrayList<OutletsDevice> devices;

public OutletsAdapter(MainActivity act)
{

    super(act, 0);

    this.act = act;

    devices = new ArrayList<OutletsDevice>();
    max = 0;
}

void add_device(OutletsDevice dev)
{

    devices.add(max++, dev);
}

@Override
public void clear()
{

    devices = new ArrayList<OutletsDevice>();
    max = 0;
}

@Override
public int getCount()
{

    return max;
}

@Override
public OutletsDevice getItem(int idx)
{

    return devices.get(idx);
}

}
