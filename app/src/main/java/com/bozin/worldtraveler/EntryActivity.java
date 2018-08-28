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
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;


import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by igorb on 21.12.2017.
 */

public class EntryActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private final String TAG = "EntryActivity";
    private static final int REQUEST_PERMISSIONS = 100;
    private static final int MARSHMALLOW = 23;
    private String jsonString;
    private SharedPreferences getPrefs;
    private Disposable mapStyleDisposable;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);
        ImageView logo = findViewById(R.id.iv_logo_entry_activity);
        Picasso.get().load(R.drawable.color_logo_transparent).fit().into(logo);
        permissionsRequest();

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS) {
            Log.i("PERMISSION", "Received response for Location permission request.");
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                startAppIntro();
                EntryActivity.this.finish();
            } else if (grantResults[0] == PackageManager.PERMISSION_DENIED || grantResults[1] == PackageManager.PERMISSION_DENIED) {
                EntryActivity.this.finish();
            }

        } else {
            EntryActivity.this.finish();
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    private void permissionsRequest() {

        int SPLASH_TIME_OUT = 1500;
        if (Build.VERSION.SDK_INT >= MARSHMALLOW) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.v("PERMISSION", "Permission is granted");

                new Handler().postDelayed(() -> {
                    startAppIntro();
                    EntryActivity.this.finish();
                }, SPLASH_TIME_OUT);

            } else {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSIONS);
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v("PERMISSION", "Permission is granted");
            new Handler().postDelayed(() -> {
                startAppIntro();
                EntryActivity.this.finish();
            }, SPLASH_TIME_OUT);
        }

    }


    private void startAppIntro() {

        mapStyleDisposable = setupMapStyle()
                .andThen(writeMapStyleToSharedPrefs())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableCompletableObserver() {
                    @Override
                    public void onStart() {
                        Log.d(TAG, "Started");
                    }

                    @Override
                    public void onError(Throwable error) {
                        Log.d(TAG, "Map style couldn't be loaded from the JSON file");
                    }

                    @Override
                    public void onComplete() {
                        //  Create a new boolean and preference and set it to true
                        boolean isFirstStart = getPrefs.getBoolean("firstStart", true);

                        //  If the activity has never started before...
                        if (isFirstStart) {

                            //  Launch app intro
                            final Intent i = new Intent(EntryActivity.this, IntroActivity.class);
                            startActivity(i);

                            //  Make a new preferences editor
                            SharedPreferences.Editor e = getPrefs.edit();

                            //  Edit preference to make it false because we don't want this to run again
                            e.putBoolean("firstStart", false);

                            //  Apply changes
                            e.apply();
                        } else {
                            Intent i = new Intent(EntryActivity.this, MainActivity.class);
                            startActivity(i);
                        }
                        Log.d(TAG, "Map style succesfully loaded!");
                    }
                });




    }


    private Completable writeMapStyleToSharedPrefs() {
        //  Initialize SharedPreferences
        return Completable.create(emitter -> {
            getPrefs = PreferenceManager
                    .getDefaultSharedPreferences(getBaseContext());
            SharedPreferences.Editor editor = getPrefs.edit();
            editor.putString(getString(R.string.sp_mapstyle_key), jsonString);
            editor.apply();
            emitter.onComplete();
        });

    }


    private Completable setupMapStyle() {
        return Completable.create(emitter -> {
            InputStream is = getResources().openRawResource(R.raw.map_style);
            Writer writer = new StringWriter();
            char[] buffer = new char[1024];
            try {
                Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            jsonString = writer.toString();
            jsonString = jsonString.replaceAll("\n", "");
            jsonString = jsonString.replaceAll("\r", "");
            emitter.onComplete();
        });
    }

    @Override
    protected void onStop() {
        mapStyleDisposable.dispose();
        super.onStop();
    }
}
