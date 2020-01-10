package com.n0ano.athome;

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

    Log.d("ImageMgmt: onCreate");

//    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//        if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
//        }
//        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
//                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
//        return;
//    }

    setup();

    find_images();
    //show_idx = 0;
    //show_next();
}

@Override
protected void onStart()
{

    super.onStart();
    Log.d("ImageMgmt: onStart");
}

@Override
protected void onRestart()
{

    super.onRestart();
    Log.d("ImageMgmt: onRestart");
}

@Override
protected void onResume()
{

    super.onResume();
    Log.d("ImageMgmt: onResume");
}

@Override
protected void onPause()
{

    super.onPause();
    Log.d("ImageMgmt: onPause");
}

@Override
protected void onStop()
{

    super.onStop();
    Log.d("ImageMgmt: onStop");
}

@Override
protected void onDestroy()
{

    super.onDestroy();
    Log.d("ImageMgmt: onDestroy");
}

@Override
public boolean onCreateOptionsMenu(Menu menu)
{

    super.onCreateOptionsMenu(menu);
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
Log.d("action bar created");
    return true;
}

private void find_images()
{

    new Thread(new Runnable() {
        public void run() {
            images = new ArrayList<String>();
            find_local();
            find_remote();
            done();
        }
    }).start();
}

public ArrayList<String> find_local()
{

    Uri uri;
    Cursor cursor;
    int column_index_data, column_index_folder_name;
    String path = null;
    uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

    String[] projection = { MediaStore.MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME };

    cursor = getContentResolver().query(uri, projection, null, null, null);

    column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
    column_index_folder_name = cursor .getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
    while (cursor.moveToNext()) {
        path = cursor.getString(column_index_data);

        images.add("file://" + path);
    }
    return images;
}

public ArrayList<String> find_remote()
{
    char type;
    String str;
    String url;
	InputStream in_rdr = null;

    HashMap<String, String> meta = new HashMap<String, String>();
    try {
        url = ss_server +
                Common.CGI_BIN +
                "?names" +
                "&host=" + Common.base(ss_host) +
                "&list=" + ss_list;
        Log.d("get names from " + url);
        Authenticator.setDefault(new CustomAuthenticator(ss_user, ss_pwd));
        in_rdr = new URL(url).openStream();
        for (;;) {
            str = Common.meta_line(in_rdr);
            type = str.charAt(0);
            if (type == 'E')
                return images;
            if (type == 'F')
                images.add("http://" + str.substring(1));
            else
                Log.d("Unexpected meta data - " + str);
        }
    } catch (Exception e) {
        Log.d("image get execption - " + e);
    }
    return images;
}

private void show_next()
{
	InputStream in_rdr;
    Bitmap bitmap;
    String img_uri;

    try {
        img_uri = images.get(show_idx++);
    } catch (Exception e) {
Log.d("last image - " + show_idx);
        finish();
        return;
    }
    Log.d("image uri - " + img_uri);
    try {
        in_rdr = new FileInputStream(new File(img_uri));
        bitmap = BitmapFactory.decodeStream(in_rdr);
        in_rdr.close();
	} catch (Exception e) {
		Log.d("get image failed - " + e);
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

    Log.d("Images:");
    for (String path : images)
        Log.d("image: " + path);

    finish();
}

private void setup()
{

    ss_host = pref.get("ss_host", "");
    ss_list = Common.suffix(ss_host);
    ss_server = pref.get("ss_server", "");
    ss_user = pref.get("ss_user", "");
    ss_pwd = pref.get("ss_pwd", "");
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
