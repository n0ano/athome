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

static boolean LOG = false;
static final String TAG = "DDD";

static String uri = "";
static String params = "";

static String log_buf[] = new String[LOG_SIZE];
static int log_next = 0;
static boolean log_full = false;

// cfg: check build type
//
//   Note: Enable logging if this is a debug build.  The main activity
//         must call this routine explicitly to enable this check.
//
public static void cfg(int debug, String http_uri, String http_params)
{

    if (debug > 0) {
        LOG = true;
        uri = http_uri;
        params = http_params;
    } else {
        LOG = false;
        uri = "";
        params = "";
    }
}

public static int size()
{

    return Log.log_full ? LOG_SIZE : Log.log_next;
}

public synchronized
static String get(int idx)
{

    if (log_full) {
        idx += Log.log_next;
        if (idx >= LOG_SIZE)
            idx -= LOG_SIZE;
    }
    return Log.log_buf[idx];
}

public synchronized
static void clear()
{

    Log.log_next = 0;
    Log.log_full = false;
}

public synchronized
static void s(String string)
{

    Log.log_buf[Log.log_next++] = string.replace("\n", "\\n");
	if (LOG) android.util.Log.d(TAG, Log.log_buf[Log.log_next - 1]);
    if (Log.log_next >= LOG_SIZE) {
        Log.log_next = 0;
        Log.log_full = true;
    }
    C.remote_log(P.get_string("general:log_uri"), P.get_string("general:log_params"), string);
}

public static void i(String string) { i(TAG, string); }
public static void e(String string) { e(TAG, string); }
public static void d(String string) { d(TAG, string); }
public static void v(String string) { v(TAG, string); }
public static void w(String string) { w(TAG, string); }

public static void i(String tag, String string) { if (LOG) android.util.Log.i(tag, string); }
public static void e(String tag, String string) { if (LOG) android.util.Log.e(tag, string); }
public static void d(String tag, String string) { if (LOG) android.util.Log.d(tag, string); }
public static void v(String tag, String string) { if (LOG) android.util.Log.v(tag, string); }
public static void w(String tag, String string) { if (LOG) android.util.Log.w(tag, string); }

}
