package com.bozin.worldtraveler;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Objects;

/**
 * Created by igorb on 03/03/2018.
 */

public class StatisticsFragment extends Fragment {


    public static StatisticsFragment newInstance(int numberOfCities, int numberOfCountries) {
        StatisticsFragment statisticsFragment = new StatisticsFragment();
        Bundle args = new Bundle();
        args.putInt("numberOfCities", numberOfCities);
        args.putInt("numberOfCountries", numberOfCountries);
        statisticsFragment.setArguments(args);
        return statisticsFragment;
    }

    private Handler handler = new Handler();
    private int pStatus = 0;

    @SuppressLint("SetTextI18n")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_statistics, container, false);
        TextView numberOfCities = rootView.findViewById(R.id.tv_number_of_cities_visited);
        TextView numberOfCountries = rootView.findViewById(R.id.tv_number_of_countries_visited);
        TextView tv_percentage = rootView.findViewById(R.id.tv_percentage);
        TextView tv_percentage_caption = rootView.findViewById(R.id.tv_statistics_percentage_caption);
        tv_percentage_caption.setText(getString(R.string.tv_statistics_percentage_caption));


        int countriesNumber = Objects.requireNonNull(getArguments()).getInt("numberOfCountries");
        int citiesNumber = Objects.requireNonNull(getArguments()).getInt("numberOfCities");
        int totalNumberOfCountries = 193;
        float percentage_traveled = (((float) countriesNumber) / totalNumberOfCountries) * 100;


        Resources res = getResources();
        Drawable drawable = res.getDrawable(R.drawable.progress);
        final ProgressBar mProgress = rootView.findViewById(R.id.pb_percentage_world);
        mProgress.setProgress(0);   // Main Progress
        mProgress.setSecondaryProgress(100); // Secondary Progress
        mProgress.setMax(100); // Maximum Progress
        mProgress.setProgressDrawable(drawable);
        if (percentage_traveled == 0.0) {
            mProgress.setProgress(pStatus);
            tv_percentage.setText("0%");
        } else {
            new Thread(() -> {
                // TODO Auto-generated method stub
                while (pStatus < percentage_traveled) {
                    pStatus += 1;

                    handler.post(() -> {
                        // TODO Auto-generated method stub
                        mProgress.setProgress(pStatus);
                        tv_percentage.setText(pStatus + "%");

                    });
                    try {
                        // Sleep for 200 milliseconds.
                        // Just to display the progress slowly
                        Thread.sleep(150); //thread will take approx 1.5 seconds to finish
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }


        if (getArguments() != null) {
            numberOfCities.setText(String.valueOf(citiesNumber));
            numberOfCountries.setText(String.valueOf(countriesNumber));
        }

        return rootView;
    }


    @Override
    public void onResume() {
        super.onResume();
        Objects.requireNonNull(getActivity()).setTitle(R.string.fragment_statistics);
    }
}
