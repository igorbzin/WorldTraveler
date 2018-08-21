package com.bozin.worldtraveler;


import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bozin.worldtraveler.ViewModels.PlacesViewModel;
import com.bozin.worldtraveler.ViewModels.PlacesViewModelFactory;
import com.bozin.worldtraveler.data.User;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;




/**
 * A styled map using JSON styles from a raw resource.
 */
public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener,
        MapFragment.MapFragmentStatisticsListener, LoginFragment.onLoggedInHandler, ProfileFragment.onSignedOutHandler {


    private final String TAG = "MainActivity";
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private NavigationView mNavigationView;
    private FragmentManager mFragmentManager;
    private int mNumberOfCities;
    private int mNumberOfCountries;
    private String mMapstyle;
    private DatabaseReference mdatabaseReference;
    private SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.tb_actionbar);
        this.setSupportActionBar(toolbar);
        toolbar.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.colorPrimaryDark));
        toolbar.setTitleTextColor(ContextCompat.getColor(MainActivity.this, R.color.textWhite));


        PlacesViewModelFactory factory = new PlacesViewModelFactory(getApplication());
        PlacesViewModel placesViewModel = ViewModelProviders.of(this, factory).get(PlacesViewModel.class);


        mDrawerLayout = findViewById(R.id.drawer_layout);

        drawerToggle = new ActionBarDrawerToggle(MainActivity.this, mDrawerLayout, R.string.open_drawer, R.string.closed_drawer);
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


        mNavigationView = findViewById(R.id.nav_view);


        //Add first fragment
        LoginFragment mapFragment = new LoginFragment();
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        fragmentTransaction.add(R.id.container, mapFragment)
                .setReorderingAllowed(true)
                .commit();


        createMenu();
        setupSharedPreferences();
        mMapstyle = setMapStyle();
    }


    private void createMenu() {
        mNavigationView.setNavigationItemSelectedListener(item ->
        {
            mDrawerLayout.closeDrawers();

            int id = item.getItemId();
            switch (id) {
                case R.id.menu_item_maps:
                    Fragment maps = new MapFragment();
                    switchFragment(mFragmentManager, maps, getString(R.string.fragment_map));
                    break;
                case R.id.menu_item_statistics:
                    Fragment statistics = StatisticsFragment.newInstance(mNumberOfCities, mNumberOfCountries);
                    switchFragment(mFragmentManager, statistics, getString(R.string.fragment_statistics));
                    break;
                case R.id.menu_item_settings:
                    Fragment settings = new SettingsFragment();
                    switchFragment(mFragmentManager, settings, getString(R.string.fragment_settings));
                    break;
                case R.id.menu_item_login:
                    Fragment login = new LoginFragment();
                    switchFragment(mFragmentManager, login, getString(R.string.fragment_user));
                    break;
                default:
                    Fragment defaultMap = new MapFragment();
                    switchFragment(mFragmentManager, defaultMap, getString(R.string.fragment_map));
                    break;
            }
            return true;
        });
    }

    private void createMenuLoggedIn() {
        mNavigationView.setNavigationItemSelectedListener(item ->
        {
            mDrawerLayout.closeDrawers();

            int id = item.getItemId();
            switch (id) {
                case R.id.menu_item_maps:
                    Fragment maps = new MapFragment();
                    switchFragment(mFragmentManager, maps, getString(R.string.fragment_map));
                    break;
                case R.id.menu_item_statistics:
                    Fragment statistics = StatisticsFragment.newInstance(mNumberOfCities, mNumberOfCountries);
                    switchFragment(mFragmentManager, statistics, getString(R.string.fragment_statistics));
                    break;
                case R.id.menu_item_settings:
                    Fragment settings = new SettingsFragment();
                    switchFragment(mFragmentManager, settings, getString(R.string.fragment_settings));
                    break;
                case R.id.menu_item_user:
                    Fragment profile = new ProfileFragment();
                    switchFragment(mFragmentManager, profile, getString(R.string.fragment_user));
                    break;
                default:
                    Fragment defaultMap = new MapFragment();
                    switchFragment(mFragmentManager, defaultMap, getString(R.string.fragment_map));
                    break;
            }
            return true;
        });
    }


    public void setNavItemChecked(int resId) {
        Menu m = mNavigationView.getMenu();
        MenuItem menuItem = m.findItem(resId);
        menuItem.setChecked(true);
    }

    public void uncheckNavItem(int resId) {
        Menu m = mNavigationView.getMenu();
        MenuItem menuItem = m.findItem(resId);
        menuItem.setChecked(false);
    }


    private void switchFragment(@NonNull FragmentManager fragmentManager, @NonNull Fragment mFragment, String fragmentName) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                .replace(R.id.container, mFragment)
                .addToBackStack(fragmentName)
                .setReorderingAllowed(true)
                .commit();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void statisticsUpdate(int numberOfCities, int numberOfCountries) {
        mNumberOfCities = numberOfCities;
        mNumberOfCountries = numberOfCountries;
    }


    private String setMapStyle() {
        sharedPreferences = android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences(this);
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
        sharedPreferences = android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences(this);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        int loggedIn = sharedPreferences.getInt(getString(R.string.sp_logged_in_status), 0);
        if (loggedIn == 1) {
            inflater.inflate(R.menu.menu, menu);
        } else {
            inflater.inflate(R.menu.menu_logged_out, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // toggle nav drawer on selecting action bar app icon/title
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action bar actions click
        switch (item.getItemId()) {
            case R.id.action_logout:
                if (sharedPreferences.getInt(getString(R.string.sp_logged_in_status), 0) == 1) {
                    onSignedOut();
                } else {
                    Toast.makeText(this, "You are already logged out", Toast.LENGTH_LONG).show();
                    return false;
                }
                return true;
            case R.id.action_login:
                if (sharedPreferences.getInt(getString(R.string.sp_logged_in_status), 0) == 0) {
                    LoginFragment loginFragment = LoginFragment.newInstance(1);
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.container, loginFragment)
                            .setReorderingAllowed(true)
                            .commit();
                    return true;
                } else {
                    return false;
                }


            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateOptionsMenu() {
        supportInvalidateOptionsMenu();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }


    @Override
    public void onLoginUpdate(FirebaseUser user) {
        if (user != null) {
            mNavigationView.getMenu().clear();
            mNavigationView.inflateMenu(R.menu.drawermenu_logged_in);
            View headerLayout = mNavigationView.getHeaderView(0);
            mNavigationView.removeHeaderView(headerLayout);
            mNavigationView.inflateHeaderView(R.layout.drawer_header_logged_in);
            headerLayout = mNavigationView.getHeaderView(0);
            CircularImageView profilePicture = headerLayout.findViewById(R.id.iv_profile_picture);
            TextView profile_nameTV = headerLayout.findViewById(R.id.tv_profile_name);
            Uri photoUrl = user.getPhotoUrl();
            if (photoUrl != null && profilePicture != null) {
                Picasso.get().load(photoUrl).into(profilePicture);
            }
            String profile_name = user.getDisplayName();

            if (profile_name != null && profile_nameTV != null) {
                profile_nameTV.setText(profile_name);
            }
            createMenuLoggedIn();

            //Add user to firebase database
            mdatabaseReference = FirebaseDatabase.getInstance().getReference("users");
            addUserToDatabase(user);

            //Update actionbar menu
            updateOptionsMenu();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(getString(R.string.sp_logged_in_status), 1);
            editor.apply();


            Fragment profileFragment = ProfileFragment.newInstance(profile_name);
            FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            fragmentTransaction.replace(R.id.container, profileFragment)
                    .setReorderingAllowed(true)
                    .commit();
        }
    }

    @Override
    public void onSignedOut() {
        //Update actionbar menu
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(getString(R.string.sp_logged_in_status), 0);
        editor.apply();
        updateOptionsMenu();

        LoginFragment loginFragment = LoginFragment.newInstance(1);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.container, loginFragment)
                .setReorderingAllowed(true)
                .commit();
        createMenu();
        loginFragment.signOut();

    }


    private void addUserToDatabase(FirebaseUser currentUser) {
        String id = currentUser.getUid();
        User user = new User(id, currentUser.getDisplayName(), "", "");
        mdatabaseReference.child(id).setValue(user);
    }
}
