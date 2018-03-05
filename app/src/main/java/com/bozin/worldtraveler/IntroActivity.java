package com.bozin.worldtraveler;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntro2Fragment;

/**
 * Created by igorb on 28.02.2018.
 */

public class IntroActivity extends AppIntro {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int white = ContextCompat.getColor(this, R.color.textWhite);
        int black = ContextCompat.getColor(this, R.color.textBlack);
        int logo_green = ContextCompat.getColor(this, R.color.colorPrimaryDark);
        int bluegray = ContextCompat.getColor(this, R.color.blueGrey);

        addSlide(AppIntro2Fragment.newInstance(getText(R.string.ai_welcome),getText( R.string.ai_welcome_text), R.drawable.color_logo_transparent, white, logo_green, black));
        addSlide(AppIntro2Fragment.newInstance(getText(R.string.ai_add_city_title),getText( R.string.ai_add_city_text), R.drawable.screenshot_add_city,white, logo_green, black));
        addSlide(AppIntro2Fragment.newInstance(getText(R.string.ai_marker_title), getText(R.string.ai_marker_text), R.drawable.screenshot_marker,white, logo_green, black));
        addSlide(AppIntro2Fragment.newInstance(getText(R.string.ai_add_photos_title),getText( R.string.ai_add_photos_text), R.drawable.screenshot_add_phot, white, logo_green, black));
        setColorDoneText(white);
        setColorSkipButton(white);
        setBarColor(logo_green);
        setIndicatorColor(white, white  );
        setNextArrowColor(white);

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
