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
        act.go_back(null);
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

    final Dialog dialog = new Dialog(act, R.style.AlertDialogCustom);
    dialog.setContentView(R.layout.bar_info);
    dialog.setCanceledOnTouchOutside(false);

    TextView tv = (TextView) dialog.findViewById(R.id.info_name);
    tv.setText(act.cur_image.get_name());

    CheckBox cb = (CheckBox) dialog.findViewById(R.id.info_selected);
    cb.setChecked(act.cur_image.get_check());

    tv = (TextView) dialog.findViewById(R.id.info_type);
    tv.setText((act.cur_image.get_type() == C.IMAGE_LOCAL) ? "local" : "remote");

    tv = (TextView) dialog.findViewById(R.id.info_rotate);
    tv.setText(String.valueOf(act.cur_image.get_rotate()));

    tv = (TextView) dialog.findViewById(R.id.info_width);
    tv.setText(String.valueOf(act.cur_image.get_width()));

    tv = (TextView) dialog.findViewById(R.id.info_height);
    tv.setText(String.valueOf(act.cur_image.get_height()));

    Button ok = (Button) dialog.findViewById(R.id.ok);
    ok.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            dialog.dismiss();
        }
    });

    dialog.show();
}

}
