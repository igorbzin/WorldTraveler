package com.bozin.worldtraveler.fragments;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bozin.worldtraveler.R;
import com.bozin.worldtraveler.databinding.FragmentTutorialWelcomeBinding;
import com.squareup.picasso.Picasso;

public class TutorialFragmentWelcome extends Fragment {

    private FragmentTutorialWelcomeBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_tutorial_welcome, container, false);
        binding.tvFragmentTutWelcomeCaption.setText(getString(R.string.ai_welcome));
        Picasso.get().load(R.drawable.color_logo_transparent).into(binding.ivFragmentTutWelcomeLogo);
        binding.tvFragmentTutWelcomeText.setText(getString(R.string.ai_welcome_text));
        return binding.getRoot();
    }


}
