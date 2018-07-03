package com.bozin.worldtraveler;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by igorb on 03/03/2018.
 */

public class StatisticsFragment extends Fragment {


    public static StatisticsFragment newInstance(int numberOfCities, int numberOfCountries){
        StatisticsFragment statisticsFragment = new StatisticsFragment();
        Bundle args = new Bundle();
        args.putInt("numberOfCities", numberOfCities);
        args.putInt("numberOfCountries", numberOfCountries);
        statisticsFragment.setArguments(args);
        return statisticsFragment;
    }

    @SuppressLint("SetTextI18n")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_statistics, container, false);
        TextView numberOfCities = rootView.findViewById(R.id.tv_number_of_cities_visited);
        TextView numberOfCountries = rootView.findViewById(R.id.tv_number_of_countries_visited);
        if (getArguments() != null) {
            numberOfCities.setText(Integer.toString(getArguments().getInt("numberOfCities")));
            numberOfCountries.setText(Integer.toString(getArguments().getInt("numberOfCountries")));
        }

        return rootView;
    }







}
