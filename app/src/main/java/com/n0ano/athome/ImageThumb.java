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

public class ImageThumb
{

private ScreenInfo info;
private ImageEntry entry;
private String title;

private int max_w;
private int max_h;

private HashMap<String, String> meta;

public ImageThumb(ScreenInfo info, ImageEntry entry, int max_w, int max_h)
{

    this.info = info;
    this.entry = entry;
    this.max_w = max_w;
    this.max_h = max_h;
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
                "&h=" + max_h;
        Log.d("SS:get image from " + url);
        Authenticator.setDefault(new CustomAuthenticator(info.ss_user, info.ss_pwd));
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

public Bitmap get_bitmap()
{
	InputStream in_rdr;
    final Bitmap bitmap;
    int gen = 0;

	try {
        in_rdr = open_image(entry);
        bitmap = BitmapFactory.decodeStream(in_rdr);
        in_rdr.close();
	} catch (Exception e) {
		Log.d("SS:get image failed - " + e);
		return null;
	}
    if (bitmap == null)
        Log.d("SS:image decode failed");
    else
        Log.d("SS:image retrieved, generation - " + meta.get("E") + ", size - " + bitmap.getWidth() + "x" + bitmap.getHeight());

    return bitmap;
}

}
