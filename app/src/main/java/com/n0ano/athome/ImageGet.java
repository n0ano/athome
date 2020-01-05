package com.n0ano.athome;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.io.InputStream;
import java.net.Authenticator;
import java.net.URL;
import java.net.URLEncoder;

public class ImageGet extends Thread
{

private final static String CGI_BIN = "/cgi-bin/explore.dd/get_frame";
private MainActivity act;

private String server;
private String user;
private String pwd;

private View img_start;
private ImageView img_end;

private int max_w;
private int max_h;

public ImageGet(MainActivity act, String server, String user, String pwd, int max_w, int max_h, View img_start, View img_end)
{

    this.act = act;
    this.server = server;
    this.user = user;
    this.pwd = pwd;
    this.max_w = max_w;
    this.max_h = max_h;
    this.img_start = img_start;
    this.img_end = (ImageView)img_end;
}

public void run()
{
    String url;
	InputStream in_rdr;
    final Bitmap bitmap;

	try {
        url = server +
                CGI_BIN +
                "?get" +
                "&host=" + act.ss_host +
                "&w=" + max_w +
                "&h=" + max_h;
        Log.d("get image from " + url);
        Authenticator.setDefault(new CustomAuthenticator(user, pwd));
		in_rdr = new URL(url).openStream();
        bitmap = BitmapFactory.decodeStream(in_rdr);
        in_rdr.close();
	} catch (Exception e) {
		Log.d("get image failed - " + e);
		return;
	}
    if (bitmap == null)
        Log.d("image decode failed");
    else
        Log.d("image retrieved - " + bitmap.getWidth() + "x" + bitmap.getHeight());

    act.runOnUiThread(new Runnable() {
        public void run() {
            if (bitmap == null)
                img_end.setImageResource(R.drawable.no);
            else
                img_end.setImageBitmap(bitmap);
            act.do_fade(img_start, img_end);
        }
    });
}

}
