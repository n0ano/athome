package com.n0ano.athome;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.n0ano.athome.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

// Created by n0ano on 10/10/16.
//
// Class to ecoBee thermostat data
//
public class EcobeeData {

public final static int HOLD_RUNNING   = 0;
public final static int HOLD_TEMPORARY = 1;
public final static int HOLD_PERMANENT = 2;

private String name;
private String id;
private String mode;
private int hold_mode;
private int h_min;
private int h_max;
private int c_min;
private int c_max;
private int c_delta;

public EcobeeData(String name, String id)
{

    this.name = name;
    this.id = id;

    this.hold_mode = HOLD_RUNNING;
    this.h_min = 400;
    this.h_max = 990;
    this.c_min = 400;
    this.c_max = 990;
    this.c_delta = 50;
}

public void set_range(String mode, int h_min, int h_max, int c_min, int c_max, int c_delta)
{

    this.mode = mode;
    this.h_min = h_min;
    this.h_max = h_max;
    this.c_min = c_min;
    this.c_max = c_max;
    this.c_delta = c_delta;
}

public String get_name()
{

    return this.name;
}

public String get_id()
{

    return this.id;
}

public String get_mode()
{

    return this.mode;
}

public void set_hold(int mode)
{

    this.hold_mode = mode;
}

public int get_hold()
{

    return this.hold_mode;
}

public int get_min()
{

    return (this.h_min < this.c_min) ? this.h_min : this.c_min;
}

public int get_max()
{

    return (this.h_max > this.c_max) ? this.h_max : this.c_max;
}

public int get_delta()
{

    return this.c_delta;
}

}
