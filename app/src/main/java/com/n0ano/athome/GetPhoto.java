package com.n0ano.athome;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import java.io.InputStream;
import java.util.ArrayList;

public class GetPhoto extends AppCompatActivity
{

Preferences pref;
int debug = 0;
ArrayList<Uri> uris;
int show_idx;

@Override
protected void onCreate(Bundle state)
{

    super.onCreate(state);
    setContentView(R.layout.activity_get_photo);

    pref = new Preferences(this);
    debug = pref.get("debug", 0);
        Log.cfg(debug, "", "");

    Log.d("GetPhoto: onCreate");
Log.d("ss host - " + pref.get("ss_host", ""));

    Intent intent = getIntent();
    String action = intent.getAction();
    String type = intent.getType();
    if (Intent.ACTION_SEND.equals(action) && type != null) {
        if (type.startsWith("image/")) {
            got_image(intent); // Handle single image being sent
        }
    } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
        if (type.startsWith("image/")) {
            got_images(intent); // Handle multiple images being sent
        }
    }
}

@Override
protected void onStart()
{

    super.onStart();
    Log.d("GetPhoto: onStart");
}

@Override
protected void onRestart()
{

    super.onRestart();
    Log.d("GetPhoto: onRestart");
}

@Override
protected void onResume()
{

    super.onResume();
    Log.d("GetPhoto: onResume");
}

@Override
protected void onPause()
{

    super.onPause();
    Log.d("GetPhoto: onPause");
}

@Override
protected void onStop()
{

    super.onStop();
    Log.d("GetPhoto: onStop");
}

@Override
protected void onDestroy()
{

    super.onDestroy();
    Log.d("GetPhoto: onDestroy");
}

private void got_image(Intent intent)
{

    Uri img_uri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
    uris = new ArrayList<Uri>(1);
    uris.add(img_uri);
    show_idx = 0;
    show_next();
}

private void got_images(Intent intent)
{

    uris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
    show_idx = 0;
    show_next();
}

private void show_next()
{
	InputStream in_rdr;
    Bitmap bitmap;
    Uri img_uri;

    try {
        img_uri = uris.get(show_idx++);
    } catch (Exception e) {
Log.d("last image - " + show_idx);
        finish();
        return;
    }
    ContentResolver resolver = getApplicationContext() .getContentResolver();
    Log.d("image uri - " + img_uri);
    try {
        in_rdr = resolver.openInputStream(img_uri);
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

public void cancel(View v)
{

Log.d("Reject the image");
    show_next();
}

public void ok(View v)
{

Log.d("Accept the image");
    show_next();
}

}
