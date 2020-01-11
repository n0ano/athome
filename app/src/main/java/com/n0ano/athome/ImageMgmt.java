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
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class ImageMgmt extends AppCompatActivity
{
 
// MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE is an app unique number
public static int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1001;

Preferences pref;
int debug = 0;
ArrayList<String> images;
int show_idx;

public String ss_host = "";
public String ss_list = "";
public String ss_server = "";
public String ss_user = "";
public String ss_pwd = "";

private ImageFind image_find;
private int ss_generation = -1;

@Override
protected void onCreate(Bundle state)
{

    super.onCreate(state);
    setContentView(R.layout.activity_image_mgmt);

    pref = new Preferences(this);
    debug = pref.get("debug", 0);
        Log.cfg(debug, "", "");

    Toolbar tb = findViewById(R.id.toolbar);
    tb.inflateMenu(R.menu.menu_screen);
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
    //show_idx = 0;
    //show_next();
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

    super.onCreateOptionsMenu(menu);
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
Log.d("SS:action bar created");
    return true;
}

private void get_names(final ImageFind image_find, final int gen)
{

    if (gen != ss_generation)
        new Thread(new Runnable() {
            public void run() {
                images = image_find.find_local(new ArrayList<String>());
                images = image_find.find_remote(true, images);
                ss_generation = image_find.ss_generation;
                done();
            }
        }).start();
}

private void show_next()
{
	InputStream in_rdr;
    Bitmap bitmap;
    String img_uri;

    try {
        img_uri = images.get(show_idx++);
    } catch (Exception e) {
Log.d("SS:last image - " + show_idx);
        finish();
        return;
    }
    Log.d("SS:image uri - " + img_uri);
    try {
        in_rdr = new FileInputStream(new File(img_uri));
        bitmap = BitmapFactory.decodeStream(in_rdr);
        in_rdr.close();
	} catch (Exception e) {
		Log.d("SS:get image failed - " + e);
		return;
	}
    ImageView iv = (ImageView)findViewById(R.id.image);
    if (bitmap == null)
        iv.setImageResource(R.drawable.no);
    else
        iv.setImageBitmap(bitmap);
}

private void done()
{

    Log.d("SS:Images:");
    for (String path : images) {
        Log.d("SS:image: " + path);
    }
    finish();
}

private void setup()
{

    ss_host = pref.get("ss_host", "");
    ss_list = C.suffix(ss_host);
    ss_server = pref.get("ss_server", "");
    ss_user = pref.get("ss_user", "");
    ss_pwd = pref.get("ss_pwd", "");

    image_find = new ImageFind((Activity) this, ss_server, ss_host, ss_list, ss_user, ss_pwd);
    get_names(image_find, 0);
}

public void cancel(View v)
{

    show_next();
}

public void ok(View v)
{

    show_next();
}

}
