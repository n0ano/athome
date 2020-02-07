package com.n0ano.athome;

import android.view.View;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Alert implements Comparable<Alert>
{

public int pri;
public long ts;
public String name;
public String detail;

public Alert(int pri, long ts, String name, String detail)
{

    this.pri = pri;
    this.ts = ts;
    this.name = name;
    this.detail = detail;
}

@Override
public int compareTo(Alert a)
{

    if (pri != a.pri)
        return a.pri - pri;
    return (int)(a.ts - ts);
}

}
