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
import com.bozin.worldtraveler.databinding.FragmentTutorialDeleteCityBinding;
import com.squareup.picasso.Picasso;

public class TutorialFragmentDeleteCity extends Fragment {


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentTutorialDeleteCityBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_tutorial_delete_city, container, false);
        binding.tvFragmentTutDeleteCityCaption.setText(getString(R.string.ai_delete_city_title));
        Picasso.get().load(R.drawable.screenshot_delete_city).into(binding.ivFragmentTutDeleteCitySh);
        binding.tvFragmentTutDeleteCityText.setText(getString(R.string.ai_delete_city_text));
        return binding.getRoot();
    }




}
