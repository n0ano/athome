package com.n0ano.athome.SS;

import com.n0ano.athome.C;
import com.n0ano.athome.Preferences;

public class ScreenInfo
{

//
//  Static info stored in preferences
//
public String host = "";
public String list = "";
public String server = "";
public String user = "";
public String pwd = "";
public int start = 0;        // seconds, 0 = none
public int delay = 0;        // seconds
public int fade = 0;

//
//  Dynamic info set at run time
//
public int generation = -1;
public int offset = 0;
public int width = 0;
public int height = 0;

public ScreenInfo(Preferences pref)
{

    this.host = pref.get("ss_host", "");
    this.list = C.suffix(host);
    this.server = pref.get("ss_server", "");
    this.user = pref.get("ss_user", "");
    this.pwd = pref.get("ss_pwd", "");
    this.start = pref.get("ss_start", 0);
    this.delay = pref.get("ss_delay", 0);
    this.fade = pref.get("ss_fade", 0);
}

}