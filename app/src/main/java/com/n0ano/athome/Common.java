package com.n0ano.athome;

//
// Created by n0ano on 11/7/18.
//
public class Common {

public final static int DATA_DELAY = 10;

public final static String JSON_SUF = "\"";
public final static String XML_SUF  = " val=\"";

private static int find(String search, String resp, int which)
{

    int start = 0;
    while (which-- > 0) {
        start = resp.indexOf(search, start);
        if (start < 0)
            return -1;
        start += search.length();
    }
    return start;
}

public static String xml_get(String name, String resp, int which)
{

    String search = "<" + name + " val=\"";
    int start = find(search, resp, which);
    if (start < 0)
        return "";
    int end = resp.indexOf("\"", start);
    return resp.substring(start, end);
}

public static String json_get(String name, String resp, int which)
{

    String search = "\"" + name + "\"";
    int start = find(search, resp, which);
    if (start < 0)
        return "";
    start = resp.indexOf("\"", start) + 1;
    int end = resp.indexOf("\"", start);
    return resp.substring(start, end);
}

public static int json_get_int(String name, String resp, int which)
{
    int res;

    String search = "\"" + name + "\"";
    int start = find(search, resp, which);
    if (start < 0)
        return 0;
    while (resp.charAt(start) == ':' || resp.charAt(start) == ' ')
        start++;
    int end = resp.indexOf(",", start);
    String str = resp.substring(start, end);
    try {
        res = Integer.parseInt(str);
    } catch (Exception e) {
        Log.d(name + ": bad integer " + str);
        res = 0;
    }
    return res;
}

}
