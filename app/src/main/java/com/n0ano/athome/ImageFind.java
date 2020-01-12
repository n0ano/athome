package com.n0ano.athome;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
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
import java.util.ArrayList;
import java.util.HashMap;

public class ImageFind
{

private Activity act;
private String ss_server;
private String ss_host;
private String ss_list;
private String ss_user;
private String ss_pwd;

public int ss_generation = -1;

public ImageFind(Activity act, String ss_server, String ss_host, String ss_list, String ss_user, String ss_pwd)
{

    this.act = act;
    this.ss_server = ss_server;
    this.ss_host = ss_host;
    this.ss_list = ss_list;
    this.ss_user = ss_user;
    this.ss_pwd = ss_pwd;
}

public ArrayList<ImageEntry> find_local(ArrayList<ImageEntry> images)
{

if (true) return images;
    Uri uri;
    Cursor cursor;
    int column_index_data, column_index_folder_name;
    String path = null;
    uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

    String[] projection = { MediaStore.MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME };

    cursor = act.getContentResolver().query(uri, projection, null, null, null);

    column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
    column_index_folder_name = cursor .getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
    while (cursor.moveToNext()) {
        path = cursor.getString(column_index_data);

        images.add(new ImageEntry(path, C.IMAGE_LOCAL, 0));
    }
    return images;
}

public ArrayList<ImageEntry> find_remote(boolean hidden, ArrayList<ImageEntry> images)
{
    char type;
    String str;
    String url;
	InputStream in_rdr = null;

    HashMap<String, String> meta = new HashMap<String, String>();
    try {
        url = ss_server +
                C.CGI_BIN +
                "?names" +
                (hidden ? "&all" : "") +
                "&host=" + C.base(ss_host) +
                "&list=" + ss_list;
        Log.d("SS:get names from " + url);
        Authenticator.setDefault(new CustomAuthenticator(ss_user, ss_pwd));
        in_rdr = new URL(url).openStream();
        for (;;) {
            str = C.meta_line(in_rdr);
            type = str.charAt(0);
            if (type == 'E') {
                ss_generation = Integer.parseInt(str.substring(1), 10);
                return images;
            }
            if (type == 'T')
                images.add(new ImageEntry(str.substring(1), C.IMAGE_REMOTE));
            else
                Log.d("SS:Unexpected meta data - " + str);
        }
    } catch (Exception e) {
        Log.d("SS:image get execption - " + e);
    }
    return images;
}

}
