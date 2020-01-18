package com.n0ano.athome.SS;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;

import java.io.InputStream;
import java.net.Authenticator;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;

import com.n0ano.athome.C;
import com.n0ano.athome.Log;

public class ImageGet
{

private ScreenSaver act;

private ScreenInfo info;
private ImageEntry entry;
private String title;

private View img_start;
private View img_end;

private HashMap<String, String> meta;

public ImageGet(ScreenSaver act, ScreenInfo info, ImageEntry entry, View img_start, View img_end)
{
	InputStream in_rdr;
    final Bitmap bitmap;
    int gen = 0;

    this.act = act;
    this.info = info;
    this.entry = entry;
    this.img_start = img_start;
    this.img_end = img_end;

	try {
        in_rdr = open_image(entry);
        bitmap = BitmapFactory.decodeStream(in_rdr);
        in_rdr.close();
        gen = Integer.parseInt(meta.get("E"), 10);
	} catch (Exception e) {
		Log.d("DDD-SS", "get image failed - " + e);
		return;
	}
    if (bitmap == null)
        Log.d("DDD-SS", "image decode failed");
    else
        Log.d("DDD-SS", "image retrieved, generation - " + meta.get("E") + ", size - " + bitmap.getWidth() + "x" + bitmap.getHeight());

    act.show_image(bitmap, title, img_start, img_end, gen);
}

private InputStream open_http(String image)
{
    String url;
	InputStream in_rdr;

	try {
        url = info.server +
                C.CGI_BIN +
                "?get" +
                "&host=" + C.base(info.host) +
                "&list=" + info.list +
                "&name=" + URLEncoder.encode(image) +
                "&w=" + info.width +
                "&h=" + info.height +
                "&r=" + entry.get_rotate();
        Log.d("DDD-SS", "get image from " + url);
        Authenticator.setDefault(new CustomAuthenticator(info.user, info.pwd));
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

}
