package com.n0ano.athome.SS;

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

import com.n0ano.athome.MainActivity;
import com.n0ano.athome.R;
import com.n0ano.athome.C;
import com.n0ano.athome.Log;

public class ImageGet extends Thread
{

private ScreenSaver act;
private MainActivity m_act;

private ScreenInfo info;
private ImageEntry entry;
private String title;

private View img_start;
private View img_end;

private int max_w;
private int max_h;

private HashMap<String, String> meta;

public ImageGet(ScreenSaver act, ScreenInfo info, ImageEntry entry, int max_w, int max_h, View img_start, View img_end)
{

    this.act = act;
    this.m_act = act.act;
    this.info = info;
    this.entry = entry;
    this.max_w = max_w;
    this.max_h = max_h;
    this.img_start = img_start;
    this.img_end = img_end;
}

private InputStream open_http(String image)
{
    String url;
	InputStream in_rdr;

	try {
        url = info.ss_server +
                C.CGI_BIN +
                "?get" +
                "&host=" + C.base(info.ss_host) +
                "&list=" + info.ss_list +
                "&name=" + URLEncoder.encode(image) +
                "&w=" + max_w +
                "&h=" + max_h +
                "&r=" + entry.get_rotate();
        Log.d("DDD-SS", "get image from " + url);
        Authenticator.setDefault(new CustomAuthenticator(info.ss_user, info.ss_pwd));
		in_rdr = new URL(url).openStream();
        meta = C.get_meta(in_rdr, new HashMap<String, String>());
	} catch (Exception e) {
		Log.d("DDD-SS", "get http image failed - " + e);
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
		Log.d("DDD-SS", "get image failed - " + e);
		return;
	}
    if (bitmap == null)
        Log.d("DDD-SS", "image decode failed");
    else
        Log.d("DDD-SS", "image retrieved, generation - " + meta.get("E") + ", size - " + bitmap.getWidth() + "x" + bitmap.getHeight());

    m_act.runOnUiThread(new Runnable() {
        public void run() {
            ImageView iv = (ImageView)((RelativeLayout)img_end).findViewById(R.id.image);
            if (bitmap == null)
                iv.setImageResource(R.drawable.no);
            else
                iv.setImageBitmap(bitmap);
            TextView tv = (TextView)((RelativeLayout)img_end).findViewById(R.id.title);
            tv.setText(title);
            if (img_start != null)
                act.do_fade(img_start, img_end);
        }
    });
}

}
