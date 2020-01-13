package com.n0ano.athome;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.InputStream;
import java.net.Authenticator;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class ScreenInfo
{

public String ss_host = "";
public String ss_list = "";
public String ss_server = "";
public String ss_user = "";
public String ss_pwd = "";

public ScreenInfo(Preferences pref)
{

    this.ss_host = pref.get("ss_host", "");
    this.ss_list = C.suffix(ss_host);
    this.ss_server = pref.get("ss_server", "");
    this.ss_user = pref.get("ss_user", "");
    this.ss_pwd = pref.get("ss_pwd", "");
}

}
