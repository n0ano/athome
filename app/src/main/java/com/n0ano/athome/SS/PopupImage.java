package com.n0ano.athome.SS;

import android.app.Dialog;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.n0ano.athome.R;
import com.n0ano.athome.Log;

public class PopupImage extends ImageMgmt
{

ImageMgmt act;

public PopupImage(ImageMgmt act)
{

    this.act = act;
}

public boolean menu_click(int item)
{

    switch (item) {

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

    case R.id.action_files:
        act.show_files();
        return true;

    case R.id.action_save:
        act.save();
        return true;

    case R.id.action_undo:
        act.go_back(null);
        return true;

    case R.id.action_mode:
        act.grid_mode();
        return true;

    }

    return false;
}

public void info_dialog(final ImageEntry entry)
{

    final Dialog dialog = new Dialog(act, R.style.AlertDialogCustom);
    dialog.setContentView(R.layout.ss_bar_info);
    dialog.setCanceledOnTouchOutside(false);

    TextView tv = (TextView) dialog.findViewById(R.id.info_name);
    tv.setText(entry.get_name());

    final CheckBox cb = (CheckBox) dialog.findViewById(R.id.info_selected);
    cb.setChecked(entry.get_check());

    final CheckBox db = (CheckBox) dialog.findViewById(R.id.info_delete);
    db.setChecked(false);

    tv = (TextView) dialog.findViewById(R.id.info_type);
    tv.setText((entry.get_type() == C.IMAGE_LOCAL) ? "local" : "remote");

    tv = (TextView) dialog.findViewById(R.id.info_width);
    tv.setText(String.valueOf(entry.get_width()));

    tv = (TextView) dialog.findViewById(R.id.info_height);
    tv.setText(String.valueOf(entry.get_height()));

    final String title = entry.get_title();
    final TextView titlev = (TextView) dialog.findViewById(R.id.info_title);
    titlev.setText(String.valueOf(entry.get_title()));

    Button cancel = (Button) dialog.findViewById(R.id.cancel);
    cancel.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            dialog.dismiss();
        }
    });

    Button ok = (Button) dialog.findViewById(R.id.ok);
    ok.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            entry.set_check(cb.isChecked());
            String tstr = titlev.getText().toString();
            if (!title.equals(tstr))
                act.set_title(entry, tstr);
            if (db.isChecked())
                act.rm_image(entry);
            act.image_adapt.notifyDataSetChanged();
            dialog.dismiss();
        }
    });

    dialog.show();
}

}
