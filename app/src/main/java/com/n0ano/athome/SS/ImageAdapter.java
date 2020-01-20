package com.n0ano.athome.SS;

import android.app.Activity;
import android.graphics.Bitmap;

import android.graphics.Matrix;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Collections;

import com.n0ano.athome.R;
import com.n0ano.athome.Log;

public class ImageAdapter extends BaseAdapter
{

ImageMgmt act;
ArrayList<ImageEntry> images = new ArrayList<ImageEntry>();

ImageFind image_find;

public ImageAdapter(ImageMgmt act)
{

    this.act = act;

    image_find = new ImageFind((Activity) act, act.screen_info);
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

    return images.get(position);
}

@Override
public View getView(int position, View view, ViewGroup parent)
{

    final ImageEntry image = images.get(position);
    if (view == null) {
        final LayoutInflater layoutInflater = LayoutInflater.from(act);
        view = layoutInflater.inflate(R.layout.grid_image, null);
    }

    final ImageView iv = (ImageView)view.findViewById(R.id.image);
    final ImageView ic = (ImageView)view.findViewById(R.id.mgmt_check);

    iv.setTag(image);
    if (image.bitmap == null)
        iv.setImageResource(R.drawable.no);
    else {
        if (image.get_rotate() == 0)
            iv.setImageBitmap(image.bitmap);
        else {
            Matrix matrix  = new Matrix();
            matrix.postRotate((float)image.get_rotate());
            iv.setImageBitmap(Bitmap.createBitmap(image.bitmap, 0, 0, image.bitmap.getWidth(), image.bitmap.getHeight(), matrix, true));
        }
    }

    view.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
            act.go_image(v, image);
            act.pref.put("image_last", images.indexOf(image) - 1);
        }
    });
    view.setOnLongClickListener(new View.OnLongClickListener() {
        public boolean onLongClick(View v) {
            act.go_info(v, image);
            act.pref.put("image_last", images.indexOf(image) - 1);
            return true;
        }
    });

    ic.setVisibility(image.get_check() ? View.VISIBLE : View.GONE);

    return view;
}

public void select(boolean check)
{

    for (ImageEntry img : images) {
        img.set_check(check);
    }
    notifyDataSetChanged();
}

private void get_names(final ImageFind image_find)
{

    new Thread(new Runnable() {
        public void run() {
            images = image_find.find_local(new ArrayList<ImageEntry>());
            images = image_find.find_remote(true, images, true);
            Collections.sort(images);
            act.screen_info.generation = image_find.ss_generation;
            done();
        }
    }).start();
}

private void done()
{

    for (ImageEntry img : images) {
        //Log.d("DDD-SS", "image: " + img.get_name() + " - " + act.saved_images.get(img.get_name()));
        img.enable(act.saved_images.get(img.get_name()));
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
