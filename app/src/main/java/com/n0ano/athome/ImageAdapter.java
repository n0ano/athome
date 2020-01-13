package com.n0ano.athome;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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
import java.util.Collections;
import java.util.HashMap;

public class ImageAdapter extends BaseAdapter
{

ImageMgmt act;
ArrayList<ImageEntry> images = new ArrayList<ImageEntry>();
ScreenInfo info;

ImageFind image_find;

public ImageAdapter(ImageMgmt act, ScreenInfo info)
{

    this.act = act;
    this.info = info;

    image_find = new ImageFind((Activity) act, info);
    get_names(image_find);
}

@Override
public int getCount()
{

    return images.size();
}

@Override
public long getItemId(int position)
{

    return 0;
}

@Override
public Object getItem(int position)
{

    return null;
}

@Override
public View getView(int position, View convertView, ViewGroup parent)
{

    final ImageEntry image = images.get(position);
    if (convertView == null) {
        final LayoutInflater layoutInflater = LayoutInflater.from(act);
        convertView = layoutInflater.inflate(R.layout.grid_image, null);
    }
    ImageView iv = (ImageView)convertView.findViewById(R.id.image);
    iv.setTag(image);
    if (image.bitmap == null)
        iv.setImageResource(R.drawable.no);
    else
        iv.setImageBitmap(image.bitmap);
    return convertView;
}

private void get_names(final ImageFind image_find)
{

    new Thread(new Runnable() {
        public void run() {
            images = image_find.find_local(new ArrayList<ImageEntry>());
            images = image_find.find_remote(true, images, true);
            Collections.sort(images);
            done();
        }
    }).start();
}

private void done()
{

    Log.d("SS:Images:");
    for (ImageEntry img : images) {
        Log.d("SS:image: " + img.get_name());
    }

    final ImageAdapter me = this;
    act.runOnUiThread(new Runnable() {
        public void run() {
            me.notifyDataSetChanged();
            act.set_view(R.id.mgmt_gridview, R.id.mgmt_loading);
        }
    });
}

}
