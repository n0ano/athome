package com.n0ano.athome.SS;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import com.n0ano.athome.R;
import com.n0ano.athome.C;
import com.n0ano.athome.Log;
import com.n0ano.athome.Preferences;

public class ImageMgmt extends AppCompatActivity
{
 
// MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE is an app unique number
public static int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1001;

public static int IMAGES_UPDATE =       2001;

Preferences pref;
int debug = 0;
int show_idx;

public PopupImage popup;
Menu menu_bar;
int mgmt_view;

public ScreenInfo screen_info;

ImageAdapter image_adapt;
ImageEntry cur_image;

public Map<String, String> saved_images;

@Override
protected void onCreate(Bundle state)
{

    super.onCreate(state);
    setContentView(R.layout.activity_image_mgmt);

    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    View v = (View) findViewById(R.id.mgmt_loading);
    v.setVisibility(View.VISIBLE);
    C.loading_name = (TextView) findViewById(R.id.mgmt_name);

    pref = new Preferences(this);
    debug = pref.get("debug", 0);
        Log.cfg(debug, "", "");

    popup = new PopupImage(this, pref);

    setTitle("Image mgmt");

    Log.d("DDD-SS", "ImageMgmt: onCreate");

//    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//        if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
//        }
//        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
//                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
//        return;
//    }

    screen_info = new ScreenInfo(pref);
    if (screen_info.host.isEmpty()) {
        Intent intent = new Intent();
        setResult(Activity.RESULT_CANCELED, intent);
        finish();
        return;
    }

    saved_images = Utils.parse_images(pref.get("images", ""));

    GridView gv = (GridView) findViewById(R.id.mgmt_grid);
    image_adapt = new ImageAdapter(this);
    gv.setAdapter(image_adapt);
}

@Override
protected void onStart()
{

    super.onStart();
    Log.d("DDD-SS", "ImageMgmt: onStart");
}

@Override
protected void onRestart()
{

    super.onRestart();
    Log.d("DDD-SS", "ImageMgmt: onRestart");
}

@Override
protected void onResume()
{

    super.onResume();
    Log.d("DDD-SS", "ImageMgmt: onResume");
}

@Override
protected void onPause()
{

    super.onPause();
    Log.d("DDD-SS", "ImageMgmt: onPause");
}

@Override
protected void onStop()
{

    super.onStop();
    Log.d("DDD-SS", "ImageMgmt: onStop");
}

@Override
protected void onDestroy()
{

    super.onDestroy();
    Log.d("DDD-SS", "ImageMgmt: onDestroy");
}

@Override
public boolean onCreateOptionsMenu(Menu menu)
{

    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_screen, menu);
    menu_bar = menu;
    set_menu(R.id.mgmt_gridview);
    return true;
}

@Override
public boolean onOptionsItemSelected(MenuItem item)
{

    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.

    if (popup.menu_click(item.getItemId()))
        return true;
    return super.onOptionsItemSelected(item);
}

@Override
public void onBackPressed()
{
    super.onBackPressed();
}

public void set_view(int visible, int invisible)
{
    View v;

    v = (View)findViewById(visible);
    v.setVisibility(View.VISIBLE);
    v = (View)findViewById(invisible);
    v.setVisibility(View.GONE);

    mgmt_view = visible;
}

private void set_menu(int id)
{
    MenuItem item;

    switch (id) {

    case R.id.mgmt_gridview:
        item = menu_bar.findItem(R.id.action_left);
        item.setVisible(false);
        item = menu_bar.findItem(R.id.action_right);
        item.setVisible(false);
        item = menu_bar.findItem(R.id.action_all);
        item.setVisible(true);
        item = menu_bar.findItem(R.id.action_none);
        item.setVisible(true);
        break;

    case R.id.mgmt_imageview:
        item = menu_bar.findItem(R.id.action_left);
        item.setVisible(true);
        item = menu_bar.findItem(R.id.action_right);
        item.setVisible(true);
        item = menu_bar.findItem(R.id.action_all);
        item.setVisible(false);
        item = menu_bar.findItem(R.id.action_none);
        item.setVisible(false);
        break;

    }
}

public void go_info(View v, final ImageEntry entry)
{

Log.d("DDD-SS", "info popup for " + entry.get_name());
    popup.info_dialog(entry);
}

public void go_image(View v, final ImageEntry entry)
{

    set_menu(R.id.mgmt_imageview);

    View vv = (View)findViewById(R.id.mgmt_gridview);
    final int w = vv.getWidth();
    final int h = vv.getHeight();

    cur_image = entry;
    set_view(R.id.mgmt_imageview, R.id.mgmt_gridview);
    final ImageView iv = (ImageView)findViewById(R.id.mgmt_image);
    iv.setImageResource(R.drawable.no);
    new Thread(new Runnable() {
        public void run() {
            final Bitmap bitmap = new ImageThumb(screen_info, entry, w, h).get_bitmap();
            runOnUiThread(new Runnable() {
                public void run() {
                    if (bitmap != null)
                        iv.setImageBitmap(bitmap);
                }
            });
        }
    }).start();
}

public void go_grid()
{

    set_menu(R.id.mgmt_gridview);
    set_view(R.id.mgmt_gridview, R.id.mgmt_imageview);
}

public void go_back(View v)
{

    if (mgmt_view == R.id.mgmt_imageview)
        go_grid();
    else {
        Intent intent = new Intent();
        setResult(Activity.RESULT_OK, intent);
        finish();
    }
}

public void rotate(int deg)
{

    int r = cur_image.get_rotate() + deg;
    while (r < 0)
        r += 360;
    while (r >= 360)
        r -= 360;
Log.d("DDD-SS", cur_image.get_name() + ": angle - " + r);
    cur_image.set_rotate(r);
    go_image(null, cur_image);
}

public void save()
{

    if (mgmt_view == R.id.mgmt_imageview)
        save_image();
    else
        save_list();
}

public void select(boolean all)
{

    image_adapt.select(all);
}

private void save_image()
{

    save_list();
}

private void save_list()
{

    ImageEntry entry;
    int r;

    String list = "" + screen_info.generation;
    for (int i = 0; i < image_adapt.getCount(); i++) {
        entry = (ImageEntry)image_adapt.getItem(i);
        list = list + ";" + Utils.img_info(entry) + entry.get_name();
    }
    image_adapt.notifyDataSetChanged();
    try {
        pref.put("images", list);
    } catch (Exception e) {
        Log.d("DDD-SS", "pref put failed - " + e);
    }
    go_back(null);
}

public void get_saved()
{
    int off;

    String str = pref.get("images", "");
    String[] strs = str.split(";");
    screen_info.generation = Integer.parseInt(strs[0]);

    saved_images = new HashMap<String, String>();

    for (int i = 1; i < strs.length; i++) {
        str = strs[i];
        off = 1;
        if (str.charAt(1) == 'R')
            off += 4;
        saved_images.put(str.substring(off), str.substring(0, off));
    }
}

}
