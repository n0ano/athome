package com.n0ano.athome.SS;

import android.app.Activity;
import android.graphics.Bitmap;

import android.graphics.Matrix;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

import com.n0ano.athome.R;
import com.n0ano.athome.Log;

public class FilesAdapter extends BaseAdapter
{

ImageMgmt act;
ArrayList<String> files = new ArrayList<String>();

public FilesAdapter(ImageMgmt act)
{

    this.act = act;
}

@Override
public int getCount()
{

    return act.image_adapt.getCount();
}

@Override
public long getItemId(int position)
{

    return 0;
}

@Override
public Object getItem(int position)
{

    return ((ImageEntry)(act.image_adapt.getItem(position))).get_name();
}

@Override
public View getView(int position, View view, ViewGroup parent)
{

    final String file = ((ImageEntry)(act.image_adapt.getItem(position))).get_name();
    if (view == null) {
        final LayoutInflater layoutInflater = LayoutInflater.from(act);
        view = layoutInflater.inflate(R.layout.ss_log_line, null);
    }

    TextView tv = (TextView) view.findViewById(R.id.line_no);
    tv.setText((position + 1) + ":");
    tv = (TextView) view.findViewById(R.id.line_text);
    tv.setText(file);

    return view;
}

}
