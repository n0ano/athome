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

public class ImageGet extends Thread
{

private ScreenSaver act;
private MainActivity m_act;

private String server;
private String user;
private String pwd;

private ImageEntry entry;
private String list;
private String title;

private View img_start;
private View img_end;

private int max_w;
private int max_h;

private HashMap<String, String> meta;

public ImageGet(ScreenSaver act, String server, String list, ImageEntry entry, String user, String pwd, int max_w, int max_h, View img_start, View img_end)
{

    this.act = act;
    this.m_act = act.act;
    this.server = server;
    this.list = list;
    this.entry = entry;
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

private InputStream open_http(String image)
{
    String url;
	InputStream in_rdr;

	try {
        url = server +
                C.CGI_BIN +
                "?get" +
                "&host=" + C.base(m_act.ss_host) +
                "&list=" + list +
                "&name=" + image +
                "&w=" + max_w +
                "&h=" + max_h;
        Log.d("SS:get image from " + url);
        Authenticator.setDefault(new CustomAuthenticator(user, pwd));
		in_rdr = new URL(url).openStream();
        meta = C.get_meta(in_rdr, new HashMap<String, String>());
	} catch (Exception e) {
		Log.d("SS:get http image failed - " + e);
		return null;
	}
    title = meta.get("T");
    title = ((title != null) ? title : "");
    return in_rdr;
}

private InputStream open_image(ImageEntry entry)
{

    if (entry == null)
        return null;
    if (entry.get_type() == C.IMAGE_REMOTE)
        return open_http(entry.get_name());
//    else if (entry.get_type() == C.IMAGE_LOCAL)
//        return file open
    return null;
}

public void run()
{
	InputStream in_rdr;
    final Bitmap bitmap;
    int gen = 0;

	try {
        in_rdr = open_image(entry);
        bitmap = BitmapFactory.decodeStream(in_rdr);
        in_rdr.close();
        gen = Integer.parseInt(meta.get("E"), 10);
        if (gen != act.ss_generation)
            act.get_names(m_act.image_find, gen);
	} catch (Exception e) {
		Log.d("SS:get image failed - " + e);
		return;
	}
    if (bitmap == null)
        Log.d("SS:image decode failed");
    else
        Log.d("SS:image retrieved, generation - " + meta.get("E") + ", size - " + bitmap.getWidth() + "x" + bitmap.getHeight());

    m_act.runOnUiThread(new Runnable() {
        public void run() {
            ImageView iv = (ImageView)((RelativeLayout)img_end).findViewById(R.id.image);
            if (bitmap == null)
                iv.setImageResource(R.drawable.no);
            else
                iv.setImageBitmap(bitmap);
            TextView tv = (TextView)((RelativeLayout)img_end).findViewById(R.id.title);
            tv.setText(title);
            act.do_fade(img_start, img_end);
        }
    });
}

}
