package com.n0ano.athome;

//
// Created by n0ano on 11/7/18.
//
public class Common {

public final static int SAVER_RESET =   0;
public final static int SAVER_TICK =    1;
public final static int SAVER_PAUSE =   2;
public final static int SAVER_RUN =     3;
public final static int SAVER_SHOW =    4;

public static int a2i(String num)
{

    return Integer.parseInt(num);
}

public static String i2a(int num)
{

    return Integer.toString(num);
}

public static String suffix(String str)
{

    int idx = str.lastIndexOf(":");
    return (idx >= 0) ? str.substring(idx) : "";
}

}
