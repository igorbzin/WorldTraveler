package com.bozin.worldtraveler;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.bozin.worldtraveler.databinding.FragmentRegisterBinding;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class RegisterFragment extends Fragment {

    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentRegisterBinding registerBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_register, container, false);
        mAuth = FirebaseAuth.getInstance();
        registerBinding.btnEmailRegister.setOnClickListener(view -> {

        });
        return registerBinding.getRoot();
    }
}
