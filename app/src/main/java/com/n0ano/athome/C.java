package com.n0ano.athome;

import android.widget.TextView;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

//
// Created by n0ano on 11/7/18.
//
public class C {

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
    return (idx >= 0) ? str.substring(idx + 1) : "";
}

}
