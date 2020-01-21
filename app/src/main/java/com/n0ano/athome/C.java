package com.n0ano.athome;

import android.widget.TextView;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

//
// Created by n0ano on 11/7/18.
//
public class C {

public final static String CGI_BIN = "/cgi-bin/explore.dd/get_frame";

public final static int IMAGE_LOCAL =   0;
public final static int IMAGE_REMOTE =  1;

public static TextView loading_name = null;

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

public static String base(String str)
{

    int idx = str.lastIndexOf(":");
    return (idx >= 0) ? str.substring(0, idx) : str;
}

private static int b2int(byte[] b)
{

    int i = ((b[3] & 0xFF) << 24) | 
            ((b[2] & 0xFF) << 16) | 
            ((b[1] & 0xFF) << 8 ) | 
            ((b[0] & 0xFF) << 0 );
    return i;
}

private static int read_it(InputStream in, byte[] buf, int off, int n)
{
    int l, count;

    count = 0;
    while (n > 0) {
        try {
            l = in.read(buf, off, n);
            if (l <= 0)
                return count;
            off += l;
            n -= l;
            count += l;
        } catch (Exception e) {
            Log.d("read error - " + e);
            return count;
        }
    }
    return count;
}

public static String meta_line(InputStream in)
{
    String str;
    byte[] len = new byte[4];

    if (read_it(in, len, 0, 4) != 4) {
        Log.d("short read on count");
        return "E";
    }
    int n = b2int(len);
    byte[] buf = new byte[n];
    if (read_it(in, buf, 0, n) != n) {
        Log.d("short read on line");
        return "E";
    }
    str = new String(buf, StandardCharsets.UTF_8);
    return str;
}

public static HashMap<String, String> get_meta(InputStream in, HashMap<String, String> map)
{
    String str;
    String type;
    byte[] len = new byte[4];

    for (;;) {
        str = meta_line(in);
        type = str.substring(0, 1);
        str = str.substring(1);
        map.put(type, str);
        switch (type.charAt(0)) {

        case 'E':       // end of meta data
            //Log.d("E:end of metadata");
            return map;

        case 'X':       // end of meta data, no such file
            //Log.d("X:no file");
            return map;

        case 'F':       // file name
            //Log.d("F:" + str);
            break;

        case 'T':       // title
            //Log.d("T:" + str);
            break;

        default:        // unknown tag
            //Log.d(type + ":" + str);
            break;

        }
    }
}

}
