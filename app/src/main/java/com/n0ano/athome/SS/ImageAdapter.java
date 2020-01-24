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
ImageVM image_vm;

ImageFind image_find;

public ImageAdapter(ImageMgmt act, ImageVM image_vm)
{

    this.act = act;
    this.image_vm = image_vm;

    image_find = new ImageFind((Activity) act, image_vm);
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
        view = layoutInflater.inflate(R.layout.ss_grid_image, null);
    }

    final ImageView iv = (ImageView)view.findViewById(R.id.image);
    final ImageView ic = (ImageView)view.findViewById(R.id.mgmt_check);

    if (image.bitmap_th != null)
        iv.setImageBitmap(image.bitmap_th);
    else {
        iv.setImageResource(R.drawable.ss_no);
        image.get_thumb(act, act.ss_info, iv, new BitmapCallbacks() {
            @Override
            public void gotit() {
            }
        });
    }

    view.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
            if (Utils.grid_type == Utils.GRID_SHOW)
                act.go_image(v, image);
            else if (Utils.grid_type == Utils.GRID_CHECK) {
                image.set_check(!image.get_check());
                notifyDataSetChanged();
            }
            act.pref.put("image_last:" + act.ss_info.list, images.indexOf(image) - 1);
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
            images = image_find.find_local(new ArrayList<ImageEntry>(), act.ss_info);
            images = image_find.find_remote(true, images, true, act.ss_info);
            Collections.sort(images);
            act.ss_info.generation = ((images.size() > 0) ? images.get(0).get_generation() : 0);
            done();
        }
    }).start();
}

private void done()
{

    for (ImageEntry img : images) {
        Log.d("DDD-SS", "image: " + img.get_name() + " - " + act.saved_images.get(img.get_name()));
        img.enable(act.saved_images.get(img.get_name()));
    }

    final ImageAdapter me = this;
    act.runOnUiThread(new Runnable() {
        public void run() {
            me.notifyDataSetChanged();
            act.set_view(R.id.mgmt_gridview);
        }
    });
}

}
