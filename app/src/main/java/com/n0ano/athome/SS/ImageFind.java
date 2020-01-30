package com.n0ano.athome.SS;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.InputStream;
import java.net.Authenticator;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import com.n0ano.athome.Log;

public class ImageFind
{

private Activity act;
private ImageVM image_vm;

ScreenInfo info;

ArrayList<ImageEntry> images;

public ImageFind(Activity act, ImageVM image_vm)
{

    this.act = act;
    this.image_vm = image_vm;
}

public ArrayList<ImageEntry> scan(ScreenInfo info, String list, boolean thumb)
{

    this.info = info;
    images = new ArrayList<ImageEntry>();
    find_local(list);
    find_remote(list, thumb);
    Collections.sort(images);
    return images;
}

public void find_local(String list)
{

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

        images.add(new ImageEntry("f" + cursor.getPosition() + ":" + path, list, 0));
    }
    return;
}

public void find_remote(String list, boolean thumb)
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
                "&all" +
                "&host=" + C.base(info.host) +
                "&list=" + list;
        Log.d("DDD-SS", "get names from " + url);
        Authenticator.setDefault(new CustomAuthenticator(info.user, info.pwd));
        in_rdr = new URL(url).openStream();
        for (;;) {
            final String str = C.meta_line(in_rdr);
            type = str.charAt(0);
            switch (type) {

            case 'E':
                return;

            case 'G':
                gen = Integer.parseInt(str.substring(1), 10);
                break;

            case 'F':
                final String name = str.substring(str.indexOf(":"));
                if (C.loading_name != null)
                    act.runOnUiThread(new Runnable() {
                        public void run() {
                            C.loading_name.setText(name);
                        }
                    });
                ImageEntry img = null;
                if (image_vm != null)
                    img = image_vm.get(name, gen, C.THUMB_X, C.THUMB_Y);
                if (img == null) {
                    img = new ImageEntry(str, list, gen);
                    if (image_vm != null)
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
        Log.d("DDD-SS", "image name execption - " + e);
    }
    return;
}

}
