package com.bozin.worldtraveler;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

import java.util.Objects;

/**
 * Created by igorb on 03/03/2018.
 */

public class SettingsFragment extends PreferenceFragmentCompat {


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_maps);
    }

    @Override
    public void onResume() {
        super.onResume();
        Objects.requireNonNull(getActivity()).setTitle(R.string.fragment_settings);
    }
}