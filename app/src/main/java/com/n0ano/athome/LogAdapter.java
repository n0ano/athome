package com.n0ano.athome;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class LogAdapter extends ArrayAdapter<String>
{

int max;

public LogAdapter(Activity act, int max)
{

    super(act, 0);
    this.max = max;
}

public int getCount()
{

    return max;
}


public String getItem(int i)
{

    return Log.get(i);
}

@Override

public View getView(int position, View view, ViewGroup parent)
{

    String line = getItem(position);    
    if (view == null) {
        view = LayoutInflater.from(getContext()).inflate(R.layout.log_line, parent, false);
    }
    TextView tv = (TextView) view.findViewById(R.id.log_lineno);
    tv.setText(String.valueOf(position));
    tv = (TextView)view.findViewById(R.id.log_line);
    tv.setText(line);
    return view;
}

}
