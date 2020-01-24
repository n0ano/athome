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
private ImageVM image_vm;

public ImageFind(Activity act, ImageVM image_vm)
{

    this.act = act;
    this.image_vm = image_vm;
}

public ArrayList<ImageEntry> find_local(ArrayList<ImageEntry> images, ScreenInfo info)
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

        images.add(new ImageEntry("l:" + path, 1));
    }
    return images;
}

public ArrayList<ImageEntry> find_remote(boolean hidden, ArrayList<ImageEntry> images, boolean thumb, ScreenInfo info)
{
    int idx, ts;
    int gen = 0;
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
            switch (str.charAt(0)) {

            case 'E':
                return images;

            case 'G':
                gen = Integer.parseInt(str.substring(1), 10);
                break;

            case 'T':
                final String name = str.substring(str.indexOf(":"));
                if (C.loading_name != null)
                    act.runOnUiThread(new Runnable() {
                        public void run() {
                            C.loading_name.setText(name);
                        }
                    });
                ImageEntry img = null;
                if (image_vm != null)
                    img = image_vm.get(name, gen, Utils.THUMB_X, Utils.THUMB_Y);
                if (img == null) {
                    img = new ImageEntry(str, gen);
                    image_vm.put(name, img);
                }
                images.add(img);
                break;

            default:
                Log.d("DDD-SS", "Unexpected meta data - " + str);
                break;

            }
        }
    } catch (Exception e) {
        Log.d("DDD-SS", "image get execption - " + e);
    }
    return images;
}

}
