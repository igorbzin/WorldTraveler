package com.bozin.worldtraveler.fragments;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.view.View;

import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;

import com.bozin.worldtraveler.R;

import java.util.Objects;

public class BaseFragment extends Fragment {


    public void showProgressDialog(ProgressBar progressBar) {
        progressBar.setVisibility(View.VISIBLE);

    }


    public void hideProgressDialog(ProgressBar progressBar) {
        progressBar.setVisibility(View.INVISIBLE);
    }

    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) Objects.requireNonNull(getActivity()).getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(getActivity().findViewById(R.id.cl_user_fragment).getWindowToken(), 0);
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        //hideProgressDialog();
    }

}