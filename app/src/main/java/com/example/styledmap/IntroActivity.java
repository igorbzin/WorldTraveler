package com.example.styledmap;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntro2Fragment;

/**
 * Created by igorb on 28.02.2018.
 */

public class IntroActivity extends AppIntro {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addSlide(AppIntro2Fragment.newInstance(getText(R.string.ai_welcome),getText( R.string.ai_welcome_text), R.drawable.screenshot_welcome, getColor(R.color.colorPrimary)));
        addSlide(AppIntro2Fragment.newInstance(getText(R.string.ai_add_city_title),getText( R.string.ai_add_city_text), R.drawable.screenshot_add_city, getColor(R.color.colorAccent)));
        addSlide(AppIntro2Fragment.newInstance(getText(R.string.ai_add_photos_title),getText( R.string.ai_add_photos_text), R.drawable.screenshot_add_photo, getColor(R.color.colorPrimaryDark)));

    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        Intent intent = new Intent(IntroActivity.this, MapsActivity.class);
        startActivity(intent);
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        Intent intent = new Intent(IntroActivity.this, MapsActivity.class);
        startActivity(intent);
    }
}
