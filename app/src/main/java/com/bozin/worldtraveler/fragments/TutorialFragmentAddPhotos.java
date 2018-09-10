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
import com.bozin.worldtraveler.databinding.FragmentTutorialAddPhotosBinding;
import com.squareup.picasso.Picasso;

public class TutorialFragmentAddPhotos extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentTutorialAddPhotosBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_tutorial_add_photos, container, false);
        binding.tvFragmentTutAddPicturesCaption.setText(getString(R.string.ai_add_photos_title));
        Picasso.get().load(R.drawable.screenshot_add_pictures).into(binding.ivFragmentTutAddPicturesSh);
        binding.tvFragmentTutAddPicturesText.setText(getString(R.string.ai_add_photos_text));
        return binding.getRoot();
    }

}
