package com.n0ano.athome;


//
// Created by n0ano on 10/23/16.
//
// Class to handle loggint
//
//   Note: This replaces the sytem Log class.  The only difference is that
//         this class will only do logging on a debug build.  A release build
//         goes out with logging disabled.
//
public class Log {

static final int LOG_SIZE = 128;
static final int LOG_BRIEF = 60;    // length of shortened log line to display

static boolean LOG = true;
static final String TAG = "DDD";

static String log_buf[] = new String[LOG_SIZE];
static int log_next = 0;
static boolean log_full = false;

// cfg: check build type
//
//   Note: Enable logging if this is a debug build.  The main activity
//         must call this routine explicitly to enable this check.
//
public static void cfg(int debug)
{

    if (debug > 0)
        LOG = true;
}

public static int size()
{

    return Log.log_full ? LOG_SIZE : Log.log_next;
}

public static String get(int idx)
{

    if (log_full) {
        idx += Log.log_next;
        if (idx >= LOG_SIZE)
            idx -= LOG_SIZE;
    }
    return Log.log_buf[idx];
}

public static void clear()
{

    Log.log_next = 0;
    Log.log_full = false;
}

public static void s(String string)
{

    Log.log_buf[Log.log_next++] = string.replace("\n", "\\n");
	if (LOG) android.util.Log.d(TAG, Log.log_buf[Log.log_next - 1]);
    if (Log.log_next >= LOG_SIZE) {
        Log.log_next = 0;
        Log.log_full = true;
    }
}

public static void i(String string)
{
	if (LOG) android.util.Log.i(TAG, string);
}
public static void e(String string)
{
	if (LOG) android.util.Log.e(TAG, string);
}
public static void d(String string)
{
	if (LOG) android.util.Log.d(TAG, string);
}
public static void v(String string)
{
	if (LOG) android.util.Log.v(TAG, string);
}
public static void w(String string)
{
	if (LOG) android.util.Log.w(TAG, string);
}

}
