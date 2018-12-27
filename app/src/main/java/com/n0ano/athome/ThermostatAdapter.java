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
public class ThermostatAdapter extends ArrayAdapter<ThermostatDevice> {

MainActivity act;

int max;
ArrayList<ThermostatDevice> devices;

public ThermostatAdapter(MainActivity act)
{

    super(act, 0);

    this.act = act;

    devices = new ArrayList<ThermostatDevice>();
    max = 0;
}

private int search(String str, int i, int j)
{
    int c, k;

    for (;;) {
        k = ((j - i)/2) + i;
        c = str.compareTo(devices.get(i).get_name());
        if (c == 0)
            return k;
        if (c < 0) {
            j = k - 1;
            if (i > j)
                return j;
        } else {
            i = k + 1;
            if (i > j)
                return k;
        }
    }
}

void add_device(ThermostatDevice dev)
{
    int i;

    for (i = 0; i < max; i++)
        if (devices.get(i).get_name().compareTo(dev.get_name()) >= 0) {
            devices.add(i, dev);
            max++;
            return;
        }
    devices.add(max++, dev);
}

@Override
public void clear()
{

    devices = new ArrayList<ThermostatDevice>();
    max = 0;
}

@Override
public int getCount()
{

    return max;
}

@Override
public ThermostatDevice getItem(int idx)
{

    return devices.get(idx);
}

}
