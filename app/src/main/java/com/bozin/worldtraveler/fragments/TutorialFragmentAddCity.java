package com.bozin.worldtraveler.fragments;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bozin.worldtraveler.R;
import com.bozin.worldtraveler.databinding.FragmentTutorialAddCityBinding;
import com.squareup.picasso.Picasso;

public class TutorialFragmentAddCity extends Fragment {



        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            FragmentTutorialAddCityBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_tutorial_add_city, container, false);
            binding.tvFragmentTutAddCityCaption.setText(getString(R.string.ai_add_city_title));
            Picasso.get().load(R.drawable.screenshot_add_city).into(binding.ivFragmentTutAddCitySh);
            binding.tvFragmentTutAddCityText.setText(getString(R.string.ai_add_city_text));
            return binding.getRoot();
        }




}
