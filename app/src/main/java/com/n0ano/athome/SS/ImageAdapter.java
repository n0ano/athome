package com.n0ano.athome.SS;

import android.app.Activity;
import android.graphics.Bitmap;

import android.graphics.Matrix;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;

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
public View getView(final int position, View view, ViewGroup parent)
{

    final ImageEntry image = images.get(position);
    if (view == null) {
        final LayoutInflater layoutInflater = LayoutInflater.from(act);
        view = layoutInflater.inflate(R.layout.ss_grid_image, null);
    }

    final ImageView iv = (ImageView)view.findViewById(R.id.image);
    final ImageView ic = (ImageView)view.findViewById(R.id.mgmt_check);
    final ProgressBar pb = (ProgressBar)view.findViewById(R.id.mgmt_progress);
    iv.setTag(position);

    if (image.bitmap_th != null) {
        pb.setVisibility(View.GONE);
        iv.setImageBitmap(image.bitmap_th);
        ic.setVisibility(image.get_check() ? View.VISIBLE : View.GONE);
    } else {
        pb.setVisibility(View.VISIBLE);
        iv.setImageBitmap(null);
        ic.setVisibility(View.GONE);
        image.get_thumb(0, act.ss_info, new DoneCallback() {
            @Override
            public void done() {
                if ((int)iv.getTag() == position) {
                    act.runOnUiThread(new Runnable() {
                        public void run() {
                            pb.setVisibility(View.GONE);
                            iv.setImageBitmap(image.bitmap_th);
                            ic.setVisibility(image.get_check() ? View.VISIBLE : View.GONE);
                        }
                    });
                }
            }
        });
    }

    view.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
            if (C.grid_type == C.GRID_SHOW)
                act.go_image(v, image, 0);
            else if (C.grid_type == C.GRID_CHECK) {
                image.set_check(!image.get_check());
                notifyDataSetChanged();
            }
            act.pref.put("image_last:" + act.ss_info.list, images.indexOf(image) - 1);
        }
    });

    return view;
}

public void select(boolean check)
{

    for (ImageEntry img : images) {
        img.set_check(check);
    }
    notifyDataSetChanged();
}

public void rm_thumbs()
{

    for (ImageEntry img : images) {
        img.bitmap_th = null;
    }
    notifyDataSetChanged();
}

private void get_names(final ImageFind image_find)
{

    new Thread(new Runnable() {
        public void run() {
            images = image_find.scan(act.ss_info, act.ss_info.list, true);
            int gen = ((images.size() > 0) ? images.get(0).get_generation() : act.generation);
            if (gen != act.generation)
                rm_thumbs();
            act.generation = gen;
            ImageEntry img = images.get(0);
            img.get_thumb(0, act.ss_info, new DoneCallback() {
                @Override
                public void done() {
                    all_done();
                }
            });
        }
    }).start();
}

private void all_done()
{

    for (ImageEntry img : images) {
        //Log.d("DDD-SS", "image: " + img.get_name() + " - " + act.saved_images.get(img.get_name()));
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
