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
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;

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

public ScreenInfo screen_info;

private ImageFind image_find;

ImageAdapter image_adapt;

@Override
protected void onCreate(Bundle state)
{

    super.onCreate(state);
    setContentView(R.layout.activity_image_mgmt);

    pref = new Preferences(this);
    debug = pref.get("debug", 0);
        Log.cfg(debug, "", "");

    setTitle("Screen mgmt");

    Log.d("SS:ImageMgmt: onCreate");

//    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//        if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
//        }
//        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
//                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
//        return;
//    }

    setup();

    GridView gv = (GridView) findViewById(R.id.gridview);
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

private void setup()
{

    screen_info = new ScreenInfo(pref);
}

private void set_view(int visible, int invisible)
{
    View v;

    v = (View)findViewById(visible);
    v.setVisibility(View.VISIBLE);
    v = (View)findViewById(invisible);
    v.setVisibility(View.GONE);
}

public void go_image(View v)
{

    final ImageEntry entry = (ImageEntry)v.getTag();
Log.d("SS: image clicked - " + entry.get_name());
    set_view(R.id.imageview, R.id.gridview);
    final ImageView iv = (ImageView)findViewById(R.id.imageview);
    iv.setImageResource(R.drawable.no);
    new Thread(new Runnable() {
        public void run() {
            final Bitmap bitmap = new ImageThumb(screen_info, entry, 1024, 1024).get_bitmap();
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

    set_view(R.id.gridview, R.id.imageview);
}

public void ok(View v)
{

    finish();
}

}
