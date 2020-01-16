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

        images.add(new ImageEntry(path, C.IMAGE_LOCAL, 0, 0, null));
    }
    return images;
}

public ArrayList<ImageEntry> find_remote(boolean hidden, ArrayList<ImageEntry> images, boolean thumb)
{
    char type;
    String str;
    String url;
	InputStream in_rdr = null;

    HashMap<String, String> meta = new HashMap<String, String>();
    try {
        url = info.ss_server +
                C.CGI_BIN +
                "?names" +
                (hidden ? "&all" : "") +
                "&host=" + C.base(info.ss_host) +
                "&list=" + info.ss_list;
        Log.d("SS:get names from " + url);
        Authenticator.setDefault(new CustomAuthenticator(info.ss_user, info.ss_pwd));
        in_rdr = new URL(url).openStream();
        for (;;) {
            str = C.meta_line(in_rdr);
            type = str.charAt(0);
            if (type == 'E') {
                ss_generation = Integer.parseInt(str.substring(1), 10);
                return images;
            }
            if (type == 'T') {
                final String name = str.substring(1);
                if (C.loading_name != null)
                    act.runOnUiThread(new Runnable() {
                        public void run() {
                            C.loading_name.setText(name.substring(name.indexOf(":")));
                        }
                    });
                images.add(new ImageEntry(name, C.IMAGE_REMOTE, (thumb ? info : null)));
            } else
                Log.d("SS:Unexpected meta data - " + str);
        }
    } catch (Exception e) {
        Log.d("SS:image get execption - " + e);
    }
    return images;
}

}
