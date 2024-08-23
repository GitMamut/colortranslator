package com.mintfrost.colortranslator2;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final String[] colorDescriptionText = {
            "This is <strong>%s</strong>, also known as '<strong>%s</strong>'. %s",
            "This is <strong>%s</strong>. A graphic designer could call it '<strong>%s</strong>'. %s",
            "This looks like <strong>%s</strong>. A fashionista would probably say it is '<strong>%s</strong>'. %s",
            "This is <strong>%s</strong>. An inspired interior decorator could name it '<strong>%s</strong>'. %s",
            "This is <strong>%s</strong>. A clothes designer could talk about '<strong>%s</strong>'. %s",
            "This is <strong>%s</strong>. Your nail polish label would say '<strong>%s</strong>'. %s",
            "This is <strong>%s</strong>. Your wall paint color name would be '<strong>%s</strong>'. %s",
            "This is <strong>%s</strong>. Your color savvy friend would call it '<strong>%s</strong>'. %s",
            "This is <strong>%s</strong>. Pantone expert would call it '<strong>%s</strong>'. %s",
            "This is <strong>%s</strong>. An impressionist painter would call it '<strong>%s</strong>'. %s",
            "This is <strong>%s</strong>, an ardent florist would call it '<strong>%s</strong>'. %s",
            "This looks like one of the 50 shades of <strong>%s</strong>. More precisely it is '<strong>%s</strong>'. %s",
            "Did you think this is <strong>%s</strong>? More like '<strong>%s</strong>'! %s"
    };
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static int CAMERA_REQUEST_CODE = 1;
    private List<ColorDesc> colors = new ArrayList();
    private CameraPreview mPreview;
    private String capturedColorHex = null;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadColors();

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (deviceHasCamera()) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
            } else {
                initCameraRelatedObjects();
            }
        } else {
            Snackbar.make(findViewById(R.id.main_layout), "No usable camera detected.", Snackbar.LENGTH_LONG).show();
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private boolean deviceHasCamera() {
        return this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initCameraRelatedObjects();
            } else {
                Snackbar.make(findViewById(R.id.main_layout), "Camera permission request was denied.", Snackbar.LENGTH_LONG).show();
            }
        }
    }

    private void initCameraRelatedObjects() {
        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        ImageView leftView = (ImageView) findViewById(R.id.imageView3);
        ImageView rightView = (ImageView) findViewById(R.id.imageView4);

        mPreview = new CameraPreview(this, fab, leftView, rightView);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int capturedColor = mPreview.captureColor();
                fab.setBackgroundTintList(ColorStateList.valueOf((capturedColor)));
                ClosestColor closestColor = findClosestColor(capturedColor);
                ((TextView) findViewById(R.id.textView2)).setText(getColorDescriptionText(closestColor));
                if (closestColor != null) {
                    capturedColorHex = closestColor.getMatchingColor().getHex();
                }
            }
        });

        final TextView colorDescriptionTextField = (TextView) findViewById(R.id.textView2);
        colorDescriptionTextField.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (capturedColorHex != null) {
                    copyHexColorToClipboard(capturedColorHex);
                    Snackbar.make(findViewById(R.id.main_layout), capturedColorHex + " copied to clipboard", Snackbar.LENGTH_SHORT).show();
                }
                return true;
            }
        });

    }

    private void copyHexColorToClipboard(String capturedColorHex) {
        ClipboardManager clipboard = (ClipboardManager)
                getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("HEX color", capturedColorHex);
        clipboard.setPrimaryClip(clip);
    }

    private CharSequence getColorDescriptionText(ClosestColor closestColor) {
        if (closestColor == null) {
            return "Could not recognize color";
        }
        ColorDesc matchingColor = closestColor.getMatchingColor();
        return Html.fromHtml(String.format(getRandomColorDescriptionText(),
                matchingColor.getSimpleDescription(), matchingColor.getRichDescription(), matchingColor.getHex()));
        /*return Html.fromHtml(String.format("This is color <strong>%s</strong>, known also as '<strong>%s</strong>'<br/>%s -> %s (%.2f)",
                matchingColor.getSimpleDescription(), matchingColor.getRichDescription(),
                GraphicalTools.getHexText(closestColor.getOriginalColor()), matchingColor.getHex(), closestColor.getDistance()));*/
    }


    private ClosestColor findClosestColor(int capturedColor) {
        double distance = 442.0;
        ColorDesc closestColor = null;
        for (ColorDesc probedColor : colors) {
            if (probedColor.getDistance(capturedColor) < distance) {
                distance = probedColor.getDistance(capturedColor);
                closestColor = probedColor;
            }
        }
        return new ClosestColor(capturedColor, closestColor, distance);
    }

    private void loadColors() {
        String json;
        try {
            InputStream is = getAssets().open("colors.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
            JSONObject obj = new JSONObject(json);
            JSONArray jsonArray = obj.getJSONArray("colors");
            for (int i = 0; i < jsonArray.length(); ++i) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                ColorDesc colorDesc = new ColorDesc(jsonObject.getString("hex"), jsonObject.getString("simpleDescription"), jsonObject.getString("richDescription"));
                colors.add(colorDesc);
                Log.d(LOG_TAG, "Loaded " + colorDesc);
            }
        } catch (IOException | JSONException ex) {
            ex.printStackTrace();
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.flashlight_menu_item) {
            item.setChecked(!item.isChecked());
            mPreview.setFlashlightMode(item.isChecked());
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public String getRandomColorDescriptionText() {
        return colorDescriptionText[new Random().nextInt(colorDescriptionText.length - 1)];
    }
}
