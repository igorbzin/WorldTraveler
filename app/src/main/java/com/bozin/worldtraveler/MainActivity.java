package com.bozin.worldtraveler;


import android.content.Intent;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;


import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.util.Log;
import android.view.Gravity;



import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


/**
 * A styled map using JSON styles from a raw resource.
 */
public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener, MapFragment.MapFragmentStatisticsListener {


    private DrawerLayout mDrawerLayout;
    private int mFirstFragmentCommit;
    private NavigationView mNavigationView;
    private FragmentManager mFragmentManager;
    private ArrayList<Integer> mBackstackItems;
    private int mNumberOfCities;
    private int mNumberOfCountries;
    private String mMapstyle;
    private int backStackCount;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.tb_actionbar);
        this.setSupportActionBar(toolbar);
        toolbar.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.colorPrimaryDark));
        toolbar.setTitleTextColor(ContextCompat.getColor(MainActivity.this, R.color.textWhite));

        backStackCount = 0;

        mDrawerLayout = findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(MainActivity.this, mDrawerLayout, R.string.open_drawer, R.string.closed_drawer);
        mDrawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        toolbar.setNavigationOnClickListener(view -> {
            if (!mDrawerLayout.isDrawerOpen(Gravity.START)) {
                mDrawerLayout.openDrawer(Gravity.START);
            } else {
                mDrawerLayout.closeDrawers();
            }
        });
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);
        if (actionBar != null) actionBar.setHomeButtonEnabled(true);


        mFragmentManager = getSupportFragmentManager();
        mBackstackItems = new ArrayList<>();

        mNavigationView = findViewById(R.id.nav_view);
        mNavigationView.getMenu().getItem(0).setChecked(true);


        mFirstFragmentCommit = 0;
        Fragment mMapFragment = new MapFragment();
        switchFragment(mFragmentManager, mMapFragment, getString(R.string.fragment_map), 0);


        this.getSupportFragmentManager().addOnBackStackChangedListener(
                () -> {
                    int currentBackStackCount = mFragmentManager.getBackStackEntryCount();

                    if (currentBackStackCount > backStackCount) {
                        backStackCount += 1;
                    } else if (currentBackStackCount < backStackCount) {
                        backStackCount -= 1;
                        int backStackFragment = mBackstackItems.get(mBackstackItems.size() - 2);
                        mNavigationView.getMenu().getItem(backStackFragment).setChecked(true);
                        mBackstackItems.remove(mBackstackItems.size() - 1);
                    }

                });

        mNavigationView.setNavigationItemSelectedListener(item ->

        {
            item.setChecked(true);
            mDrawerLayout.closeDrawers();

            int id = item.getItemId();
            switch (id) {
                case R.id.menu_item_maps:
                    Fragment maps = new MapFragment();
                    switchFragment(mFragmentManager, maps, getString(R.string.fragment_map), 0);
                    break;
                case R.id.menu_item_statistics:
                    Fragment statistics = StatisticsFragment.newInstance(mNumberOfCities, mNumberOfCountries);
                    switchFragment(mFragmentManager, statistics, getString(R.string.fragment_statistics), 1);
                    break;
                case R.id.menu_item_settings:
                    Fragment settings = new SettingsFragment();
                    switchFragment(mFragmentManager, settings, getString(R.string.fragment_settings), 2);
                    break;
                case R.id.menu_item_login:
                    Fragment login = new LoginFragment();
                    switchFragment(mFragmentManager, login, getString(R.string.fragment_login), 3);
                    break;
                default:
                    Fragment defaultmaps = new MapFragment();
                    switchFragment(mFragmentManager, defaultmaps, getString(R.string.fragment_map), 0);
                    break;
            }
            return true;
        });
        setupSharedPreferences();
        mMapstyle = setMapStyle();
    }


    private void switchFragment(@NonNull FragmentManager fragmentManager, @NonNull Fragment mFragment, String fragmentName, int navigationPosition) {


        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        fragmentTransaction.replace(R.id.container, mFragment);


        if (mFirstFragmentCommit >= 1) {
            fragmentTransaction.addToBackStack(fragmentName);
        } else {
            mFirstFragmentCommit += 1;
        }
        fragmentTransaction.setReorderingAllowed(true)
                .commit();


        mBackstackItems.add(navigationPosition);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        /*for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
        */
    }


    @Override
    public void statisticsUpdate(int numberOfCities, int numberOfCountries) {
        mNumberOfCities = numberOfCities;
        mNumberOfCountries = numberOfCountries;
    }


    public String setMapStyle() {
        SharedPreferences sharedPreferences = android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences(this);
        boolean mShowCityNames = sharedPreferences.getBoolean(getString(R.string.settings_show_cities_key), false);
        boolean mShowCountryNames = sharedPreferences.getBoolean(getString(R.string.settings_show_countries_key), true);

        String mapStyle = sharedPreferences.getString(getString(R.string.sp_mapstyle_key), "0");
        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(mapStyle);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (jsonArray != null) {
            jsonArray = setPreferenceValue(jsonArray, "administrative.country", mShowCountryNames);
            jsonArray = setPreferenceValue(jsonArray, "administrative.locality", mShowCityNames);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(getString(R.string.sp_mapstyle_key), jsonArray.toString());
            editor.putBoolean(getString(R.string.settings_show_cities_key), mShowCityNames);
            editor.putBoolean(getString(R.string.settings_show_countries_key), mShowCountryNames);
            editor.apply();
            return jsonArray.toString();
        } else {
            return null;
        }
    }


    private void setupSharedPreferences() {
        SharedPreferences sharedPreferences = android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.settings_show_countries_key))) {
            setMapStyle();
        } else if (key.equals(getString(R.string.settings_show_cities_key))) {
            setMapStyle();
        }

    }


    private JSONArray setPreferenceValue(JSONArray jsonArray, String preference, boolean visibility) {
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(jsonArray.get(i).toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (jsonObject != null && jsonObject.has("featureType")) {
                    if (jsonObject.get("featureType").equals(preference)) {
                        JSONArray stylers = jsonObject.getJSONArray("stylers");
                        JSONObject secondJSONObject;
                        for (int j = 0; j < stylers.length(); j++) {
                            secondJSONObject = new JSONObject(stylers.get(j).toString());
                            if (!visibility) {
                                secondJSONObject.put("visibility", "off");
                            } else {
                                secondJSONObject.put("visibility", "on");
                            }
                            stylers.put(j, secondJSONObject);
                        }

                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                jsonArray.put(i, jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return jsonArray;
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.v("OnSaveInstanceState", "OnSaveinstancestate called");
        outState.putString("mapStyle", mMapstyle);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mMapstyle = savedInstanceState.getString("mapStyle");
        Log.v("OnRestoreInstanceState", "Sucess: " + mMapstyle);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v("OnResume", "OnResume called");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.v("OnStop", "OnStop called");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v("OnDestroy", "Ondestroy called");
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }


}
