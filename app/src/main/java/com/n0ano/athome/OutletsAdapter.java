package com.n0ano.athome;

import android.widget.ArrayAdapter;

import java.util.ArrayList;

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

void add_device(OutletsDevice dev)
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

public OutletsDevice findItem(String name)
{
    OutletsDevice dev;

    for (int i = 0; i < max; ++i) {
        dev = devices.get(i);
        if (name.equals(dev.get_name()))
            return dev;
    }
    return null;
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
