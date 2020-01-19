package com.n0ano.athome.SS;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.InputStream;
import java.net.Authenticator;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import com.n0ano.athome.C;
import com.n0ano.athome.Log;

public class ImageFind
{

private Activity act;
private ScreenInfo info;

public int ss_generation = -1;

public ImageFind(Activity act, ScreenInfo info)
{

    this.act = act;
    this.info = info;
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

        images.add(new ImageEntry("l:" + path, info.generation, null));
    }
    return images;
}

public ArrayList<ImageEntry> find_remote(boolean hidden, ArrayList<ImageEntry> images, boolean thumb)
{
    int idx, ts;
    char type;
    String url;
	InputStream in_rdr = null;

    HashMap<String, String> meta = new HashMap<String, String>();
    try {
        url = info.server +
                C.CGI_BIN +
                "?names" +
                (hidden ? "&all" : "") +
                "&host=" + C.base(info.host) +
                "&list=" + info.list;
        Log.d("DDD-SS", "get names from " + url);
        Authenticator.setDefault(new CustomAuthenticator(info.user, info.pwd));
        in_rdr = new URL(url).openStream();
        for (;;) {
            final String str = C.meta_line(in_rdr);
            type = str.charAt(0);
            if (type == 'E') {
                ss_generation = Integer.parseInt(str.substring(1), 10);
                return images;
            }
            if (type == 'T') {
                idx = str.indexOf(":");
                if (C.loading_name != null)
                    act.runOnUiThread(new Runnable() {
                        public void run() {
                            C.loading_name.setText(str.substring(str.indexOf(":")));
                        }
                    });
                images.add(new ImageEntry(str, info.generation, (thumb ? info : null)));
            } else
                Log.d("DDD-SS", "Unexpected meta data - " + str);
        }
    } catch (Exception e) {
        Log.d("DDD-SS", "image get execption - " + e);
    }
    return images;
}

}
