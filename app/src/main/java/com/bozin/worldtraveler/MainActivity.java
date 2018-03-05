package com.bozin.worldtraveler;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Created by igorb on 21.12.2017.
 */

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int REQUEST_PERMISSIONS = 100;
    private static int SPLASH_TIME_OUT = 1500;
    private static final int MARSHMALLOW = 23;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        permissionsRequest();

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == REQUEST_PERMISSIONS) {
            Log.i("PERMISSION", "Received response for Location permission request.");
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                startAppIntro();
                MainActivity.this.finish();
            } else if (grantResults[0] == PackageManager.PERMISSION_DENIED || grantResults[1] == PackageManager.PERMISSION_DENIED) {
                MainActivity.this.finish();
            }

        } else {
            //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            MainActivity.this.finish();
        }
    }


    public void permissionsRequest() {

        if (Build.VERSION.SDK_INT >= MARSHMALLOW) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.v("PERMISSION", "Permission is granted");

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startAppIntro();
                        MainActivity.this.finish();
                    }
                }, SPLASH_TIME_OUT);

                return;
            } else {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSIONS);
                return;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v("PERMISSION", "Permission is granted");
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startAppIntro();
                    MainActivity.this.finish();
                }
            }, SPLASH_TIME_OUT);
            return;
        }

    }


    private void startAppIntro() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                //  Initialize SharedPreferences
                SharedPreferences getPrefs = PreferenceManager
                        .getDefaultSharedPreferences(getBaseContext());


                if(Build.VERSION.SDK_INT > 23){
                    String jsonArray = setupMapStyle();
                    SharedPreferences.Editor editor = getPrefs.edit();
                    editor.putString(getString(R.string.sp_mapstyle_key), jsonArray);
                    editor.apply();
                }



                //  Create a new boolean and preference and set it to true
                boolean isFirstStart = getPrefs.getBoolean("firstStart", true);

                //  If the activity has never started before...
                if (isFirstStart) {

                    //  Launch app intro
                    final Intent i = new Intent(MainActivity.this, IntroActivity.class);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            startActivity(i);
                        }
                    });

                    //  Make a new preferences editor
                    SharedPreferences.Editor e = getPrefs.edit();

                    //  Edit preference to make it false because we don't want this to run again
                    e.putBoolean("firstStart", false);

                    //  Apply changes
                    e.apply();
                } else {
                    Intent i = new Intent(MainActivity.this, MapsActivity.class);
                    i.setFlags(i.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY);
                    startActivity(i);
                }
            }
        });

        // Start the thread
        t.start();
    }



    public String setupMapStyle(){
        InputStream is = getResources().openRawResource(R.raw.map_style);
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }

        } catch (java.io.UnsupportedEncodingException e){
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String jsonString = writer.toString();
        jsonString = jsonString.replaceAll("\n", "");
        jsonString = jsonString.replaceAll("\r", "");
        return jsonString;
    }
}
