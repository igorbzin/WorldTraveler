package com.bozin.worldtraveler;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.TextView;

import com.bozin.worldtraveler.databinding.ActivityIntroBinding;
import com.bozin.worldtraveler.fragments.TutorialFragmentAddCity;
import com.bozin.worldtraveler.fragments.TutorialFragmentAddPhotos;
import com.bozin.worldtraveler.fragments.TutorialFragmentDeleteCity;
import com.bozin.worldtraveler.fragments.TutorialFragmentWelcome;

/**
 * Created by igorb on 28.02.2018.
 */

public class IntroActivity extends FragmentActivity {


    private static final int NUM_PAGES = 4;
    private TextView skip;
    private TextView done;

    ActivityIntroBinding binding;
    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private ViewPager mPager;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private ScreenSlidePagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_intro);


        //Instantiate views

        skip = binding.tvSkipTut;
        done = binding.tvDoneTut;
        skip.setText(getString(R.string.ai_tv_tutorial_skip));
        skip.setOnClickListener(v -> {
            Intent intent = new Intent(IntroActivity.this, MainActivity.class);
            startActivity(intent);
        });
        done.setText(getString(R.string.ai_tv_tutorial_done));
        done.setOnClickListener(v -> {
            Intent intent = new Intent(IntroActivity.this, MainActivity.class);
            startActivity(intent);
        });
        done.setVisibility(View.GONE);


        // Instantiate a ViewPager and a PagerAdapter.
        mPager = findViewById(R.id.intro_viewpager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        TabLayout tabLayout = findViewById(R.id.tabDots);
        tabLayout.setupWithViewPager(mPager, true);




        mPager.setAdapter(mPagerAdapter);
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if(position == 3){
                    skip.setVisibility(View.GONE);
                    done.setVisibility(View.VISIBLE);
                } else {
                    skip.setVisibility(View.VISIBLE);
                    done.setVisibility(View.GONE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }




    @Override
    public void onBackPressed() {
        if (mPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }

    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {


        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
             switch (position) {
                case 0:
                    return new TutorialFragmentWelcome();
                case 1:
                    return new TutorialFragmentAddCity();
                case 2:
                    return new TutorialFragmentDeleteCity();
                case 3:
                    return new TutorialFragmentAddPhotos();
                default:
                    return new TutorialFragmentWelcome();
            }
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }
}


