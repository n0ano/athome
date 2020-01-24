package com.n0ano.athome.SS;


import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;

import java.io.InputStream;
import java.net.Authenticator;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
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

public ScreenInfo ss_info;

ImageAdapter image_adapt;
ImageEntry cur_image;
ImageVM image_vm;

public Map<String, String> saved_images;

@Override
public void onCreate(Bundle state)
{

    super.onCreate(state);
    setContentView(R.layout.ss_activity_image_mgmt);

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

    ss_info = new ScreenInfo(this, pref);
    if (ss_info.host.isEmpty()) {
        Intent intent = new Intent();
        setResult(Activity.RESULT_CANCELED, intent);
        finish();
        return;
    }

    image_vm = ViewModelProviders.of(this).get(ImageVM.class);
Log.d("DDD-SS", "view model - " + image_vm.size());

    saved_images = Utils.parse_names(pref.get("images:" + ss_info.list_real, ""));

    GridView gv = (GridView) findViewById(R.id.mgmt_grid);
    image_adapt = new ImageAdapter(this, image_vm);
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
    getMenuInflater().inflate(R.menu.ss_menu, menu);
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

public void set_view(int visible)
{
    View v;

    v = (View)findViewById(R.id.mgmt_loading);
    v.setVisibility(View.GONE);
    v = (View)findViewById(R.id.mgmt_gridview);
    v.setVisibility(View.GONE);
    v = (View)findViewById(R.id.mgmt_imageview);
    v.setVisibility(View.GONE);
    v = (View)findViewById(R.id.mgmt_fileview);
    v.setVisibility(View.GONE);

    mgmt_view = visible;
    v = (View)findViewById(visible);
    v.setVisibility(View.VISIBLE);

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
        item = menu_bar.findItem(R.id.action_mode);
        item.setVisible(true);
        item = menu_bar.findItem(R.id.action_none);
        item.setVisible(true);
        item = menu_bar.findItem(R.id.action_files);
        item.setVisible(true);
        break;

    case R.id.mgmt_imageview:
        item = menu_bar.findItem(R.id.action_left);
        item.setVisible(true);
        item = menu_bar.findItem(R.id.action_right);
        item.setVisible(true);
        item = menu_bar.findItem(R.id.action_all);
        item.setVisible(false);
        item = menu_bar.findItem(R.id.action_mode);
        item.setVisible(false);
        item = menu_bar.findItem(R.id.action_none);
        item.setVisible(false);
        item = menu_bar.findItem(R.id.action_files);
        item.setVisible(false);
        break;

    }
}

public void set_title(final ImageEntry image, final String title)
{

    Log.d("DDD-SS", image.get_name() + ": set new title - " + title);

    new Thread(new Runnable() {
        public void run() {
            try {
                String url;
                InputStream in_rdr = null;
                HashMap<String, String> meta;

                url = ss_info.server +
                        C.CGI_BIN +
                        "?title" +
                        "&name=" + URLEncoder.encode(image.get_name()) +
                        "&text=" + URLEncoder.encode(title);
                Log.d("DDD-SS", image.get_name() + ":title - " + url);
                Authenticator.setDefault(new CustomAuthenticator(ss_info.user, ss_info.pwd));
                in_rdr = new URL(url).openStream();
                meta = C.get_meta(in_rdr, new HashMap<String, String>());
            } catch (Exception e) {
                Log.d("DDD-SS", "image get execption - " + e);
            }
        }
    }).start();

    image.set_title(title);
    TextView tv = (TextView)findViewById(R.id.mgmt_title);
    tv.setText(title);
}

public void go_image(View v, final ImageEntry entry)
{

    set_menu(R.id.mgmt_imageview);

    ScreenInfo info = ss_info;

    cur_image = entry;
    set_view(R.id.mgmt_imageview);
    final ImageView iv = (ImageView)findViewById(R.id.mgmt_image);
    iv.setImageResource(R.drawable.ss_no);
    entry.get_bitmap(this, info, iv, new BitmapCallbacks() {
        @Override
        public void gotit() {
        }
    });

    iv.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
            go_back(null);
        }
    });
    iv.setOnLongClickListener(new View.OnLongClickListener() {
        public boolean onLongClick(View v) {
            popup.info_dialog(entry);
            return true;
        }
    });

    TextView tv = (TextView)findViewById(R.id.mgmt_title);
    tv.setText(entry.get_title());
}

public void go_grid()
{

    set_menu(R.id.mgmt_gridview);
    set_view(R.id.mgmt_gridview);
}

public void go_back(View v)
{

    switch (mgmt_view) {

    case R.id.mgmt_imageview:
        go_grid();
        break;

    case R.id.mgmt_gridview:
        Intent intent = new Intent();
        setResult(Activity.RESULT_OK, intent);
        finish();
        break;

    case R.id.mgmt_fileview:
        set_view(R.id.mgmt_gridview);
        break;
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
    else {
        save_list(ss_info.list);
        go_back(null);
    }
}

public void select(boolean all)
{

    image_adapt.select(all);
}

public void grid_mode()
{
    int icon_id = R.drawable.ss_check;

    if (Utils.grid_type == Utils.GRID_SHOW) {
        Utils.grid_type = Utils.GRID_CHECK;
        icon_id = R.drawable.ss_check;
    } else if (Utils.grid_type == Utils.GRID_CHECK) {
        Utils.grid_type = Utils.GRID_SHOW;
        icon_id = R.drawable.ss_image;
    }
    MenuItem icon = menu_bar.findItem(R.id.action_mode);
    icon.setIcon(icon_id);
}

private void save_image()
{

    save_list(ss_info.list);
    go_back(null);
}

private void save_list(String list)
{

    ImageEntry entry;
    int r;

    StringBuilder inf = new StringBuilder();
    inf.append(ss_info.generation);
    for (int i = 0; i < image_adapt.getCount(); i++) {
        entry = (ImageEntry)image_adapt.getItem(i);
        inf.append(";" + entry.info() + entry.get_name());
        image_vm.put(entry.get_name(), entry);
    }
    image_adapt.notifyDataSetChanged();
    pref.put("images:" + list, inf.toString());
}

public void show_files()
{
    String line;
    TextView tv;
    ImageEntry entry;

    Log.d("Show files");
    set_view(R.id.mgmt_fileview);

    ListView lv = (ListView) findViewById(R.id.mgmt_files);
    FilesAdapter file_adapt = new FilesAdapter(this);
    lv.setAdapter(file_adapt);
}

}
