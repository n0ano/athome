package com.n0ano.athome;

import android.os.SystemClock;
import android.widget.TextView;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

//
// Created by n0ano on 11/7/18.
//
public class C {

public static final int SS_OP_INIT =    0;
public static final int SS_OP_RESET =   1;
public static final int SS_OP_BLOCK =   2;
public static final int SS_OP_UPDATE =  3;

public static boolean running = true;

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

public static boolean working()
{

    return running;
}

public static Thread data_thread(final int period, final DoitCallback cb)
{

    Thread thread = new Thread(new Runnable() {
        public void run() {
            while (working()) {
                //
                //  Get the data
                //
                cb.doit(null);

                SystemClock.sleep(period);
            }
        }
    });
    thread.start();
    return thread;
}

}
