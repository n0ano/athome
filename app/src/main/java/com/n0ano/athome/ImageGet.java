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

public class ImageGet extends Thread
{

private MainActivity act;

private String server;
private String user;
private String pwd;

private String list;
private int delta;

private View img_start;
private View img_end;

private int max_w;
private int max_h;

private String title = null;

public ImageGet(MainActivity act, String server, String list, int delta, String user, String pwd, int max_w, int max_h, View img_start, View img_end)
{

    this.act = act;
    this.server = server;
    this.list = list;
    this.delta = delta;
    this.user = user;
    this.pwd = pwd;
    this.max_w = max_w;
    this.max_h = max_h;
    this.img_start = img_start;
    this.img_end = img_end;
}

private int b2int(byte[] b)
{

    int i = ((b[3] & 0xFF) << 24) | 
            ((b[2] & 0xFF) << 16) | 
            ((b[1] & 0xFF) << 8 ) | 
            ((b[0] & 0xFF) << 0 );
    return i;
}

private void get_meta(InputStream in)
{
    String str;
    char type;
    byte[] len = new byte[4];

    for (;;) {
        try {
            in.read(len, 0, 4);
            int n = b2int(len);
            byte[] buf = new byte[n];
            in.read(buf, 0, n);
            str = new String(buf, StandardCharsets.UTF_8);
        } catch (Exception e) {
            Log.d("image stream read error - " + e);
            return;
        }
        type = str.charAt(0);
        str = str.substring(1);
        switch (type) {

        case 'E':       // end of meta data
            Log.d("E:end of metadata");
            return;

        case 'F':       // file name
            str = str.substring(0);
            Log.d("F:" + str);
            break;

        case 'T':       // title
            title = str.substring(0);
            Log.d("T:" + title);
            break;

        default:        // unknown tag
            str = str.substring(0);
            Log.d(type + ":" + str);
            break;

        }
    }
}

public void run()
{
    String url;
	InputStream in_rdr;
    final Bitmap bitmap;

	try {
        url = server +
                C.CGI_BIN +
                "?get" +
                "&host=" + C.base(act.ss_host) +
                "&list=" + list +
                "&delta=" + delta +
                "&w=" + max_w +
                "&h=" + max_h;
        Log.d("get image from " + url);
        Authenticator.setDefault(new CustomAuthenticator(user, pwd));
		in_rdr = new URL(url).openStream();
        get_meta(in_rdr);
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
            ImageView iv = (ImageView)((RelativeLayout)img_end).findViewById(R.id.image);
            if (bitmap == null)
                iv.setImageResource(R.drawable.no);
            else
                iv.setImageBitmap(bitmap);
            TextView tv = (TextView)((RelativeLayout)img_end).findViewById(R.id.title);
            tv.setText((title != null) ? title : "");
            act.do_fade(img_start, img_end);
        }
    });
}

}
