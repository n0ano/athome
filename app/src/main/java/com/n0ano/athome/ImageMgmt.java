package com.n0ano.athome;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class ImageMgmt extends AppCompatActivity
{
 
// MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE is an app unique number
public static int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1001;

Preferences pref;
int debug = 0;
int show_idx;

public PopupImage popup;
Menu menu_bar;

public ScreenInfo screen_info;

private ImageFind image_find;

ImageAdapter image_adapt;
ImageEntry cur_image;

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

    Log.d("SS:ImageMgmt: onCreate");

//    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//        if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
//        }
//        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
//                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
//        return;
//    }

    screen_info = new ScreenInfo(pref);
    if (screen_info.ss_host.isEmpty()) {
        finish();
        return;
    }

    GridView gv = (GridView) findViewById(R.id.mgmt_grid);
    image_adapt = new ImageAdapter(this, screen_info);
    gv.setAdapter(image_adapt);
}

@Override
protected void onStart()
{

    super.onStart();
    Log.d("SS:ImageMgmt: onStart");
}

@Override
protected void onRestart()
{

    super.onRestart();
    Log.d("SS:ImageMgmt: onRestart");
}

@Override
protected void onResume()
{

    super.onResume();
    Log.d("SS:ImageMgmt: onResume");
}

@Override
protected void onPause()
{

    super.onPause();
    Log.d("SS:ImageMgmt: onPause");
}

@Override
protected void onStop()
{

    super.onStop();
    Log.d("SS:ImageMgmt: onStop");
}

@Override
protected void onDestroy()
{

    super.onDestroy();
    Log.d("SS:ImageMgmt: onDestroy");
}

@Override
public boolean onCreateOptionsMenu(Menu menu)
{

    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_screen, menu);
    menu_bar = menu;
    set_menu(false);
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
    ImageEntry entry;
    int r;

    Log.d("SS:ImageMgmt: onBackPressed");
    String list = "" + image_adapt.get_generation();
    for (int i = 0; i < image_adapt.getCount(); i++) {
        entry = (ImageEntry)image_adapt.getItem(i);
        if (entry.get_check()) {
            list = list + ";" + ((entry.get_type() == C.IMAGE_LOCAL) ? "L" : "R");
            r = entry.get_rotate();
            if (r != 0)
                list = list + "R" + String.format("%03d", r);
            list = list + entry.get_name();
        }
    }
    pref.put("images", list);
    super.onBackPressed();
}

public void set_view(int visible, int invisible)
{
    View v;

    v = (View)findViewById(visible);
    v.setVisibility(View.VISIBLE);
    v = (View)findViewById(invisible);
    v.setVisibility(View.GONE);
}

private void set_menu(boolean visible)
{
    MenuItem item;

    item = menu_bar.findItem(R.id.action_undo);
    item.setVisible(visible);
    item = menu_bar.findItem(R.id.action_left);
    item.setVisible(visible);
    item = menu_bar.findItem(R.id.action_right);
    item.setVisible(visible);
    item = menu_bar.findItem(R.id.action_info);
    item.setVisible(visible);
}

public void go_image(View v, final ImageEntry entry)
{

    set_menu(true);

    View vv = (View)findViewById(R.id.mgmt_gridview);
    final int w = vv.getWidth();
    final int h = vv.getHeight();

    cur_image = entry;
Log.d("SS: image clicked - " + entry.get_name());
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

public void go_grid(View v)
{

    set_menu(false);
    set_view(R.id.mgmt_gridview, R.id.mgmt_imageview);
}

public void image_save(View v)
{

Log.d(cur_image.get_name() + ": save the new image");
    go_grid(null);
}

public void ok(View v)
{

    finish();
}

}
