package com.n0ano.athome;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.n0ano.athome.Log;

public class PopupImage extends ImageMgmt
{

ImageMgmt act;
Preferences pref;

public PopupImage(ImageMgmt act, Preferences pref)
{

    this.act = act;
    this.pref = pref;
}

public boolean menu_click(int item)
{

    switch (item) {

    case R.id.action_undo:
        act.go_back();
        return true;

    case R.id.action_left:
        act.rotate(-90);
        return true;

    case R.id.action_right:
        act.rotate(90);
        return true;

    case R.id.action_all:
        act.select(true);
        return true;

    case R.id.action_none:
        act.select(false);
        return true;

    case R.id.action_save:
        act.save();
        return true;

    case R.id.action_info:
        info_action();
        return true;

    }

    return false;
}

public void info_action()
{

Log.d(act.cur_image.get_name() + "image info");
}

}
