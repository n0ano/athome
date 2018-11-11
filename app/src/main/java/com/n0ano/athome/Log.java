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

static boolean LOG = true;
static final String TAG = "DDD";

// cfg: check build type
//
//   Note: Enable logging if this is a debug build.  The main activity
//         must call this routine explicitly to enable this check.
//
public static void cfg()
{

	LOG = true;
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
