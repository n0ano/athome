package com.n0ano.athome.SS;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.view.Display;

import com.n0ano.athome.Log;

import java.util.ArrayList;

public class ScreenInfo
{

//
//  Static info stored in preferences
//
public Activity act = null;
public String host = "";
public String list = "";
public String server = "";
public String user = "";
public String pwd = "";
public boolean enable = true;
public int start = 0;        // seconds, 0 = none
public int delay = 0;        // seconds
public int fade = 0;

//
//  Dynamic info set at run time
//
public int offset = 0;
public int width = 0;
public int height = 0;

public ScreenInfo(Activity act)
{

    this.act = act;
    P.init(act.getSharedPreferences(P.PREF_NAME, Context.MODE_PRIVATE));
    this.host = P.get("ss_host", "");
    this.list = C.suffix(host);
    this.server = P.get("ss_server", "");
    this.user = P.get("ss_user", "");
    this.pwd = P.get("ss_pwd", "");
    this.enable = P.get("ss_enable", true);
    this.start = P.get("ss_start", 0);
    this.delay = P.get("ss_delay", 0);
    this.fade = P.get("ss_fade", 0);

    Display d = act.getWindowManager().getDefaultDisplay();
    Point point = new Point();
    d.getSize(point);
    this.width = point.x;
    this.height = point.y;
}

}
