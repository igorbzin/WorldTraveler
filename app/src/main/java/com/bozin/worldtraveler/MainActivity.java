package com.bozin.worldtraveler;


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

import com.bozin.worldtraveler.fragments.LoginFragment;
import com.bozin.worldtraveler.fragments.MapFragment;
import com.bozin.worldtraveler.fragments.ProfileFragment;
import com.bozin.worldtraveler.fragments.SettingsFragment;
import com.bozin.worldtraveler.fragments.StatisticsFragment;
import com.bozin.worldtraveler.viewModels.MainViewModel;
import com.google.firebase.auth.FirebaseUser;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;


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
    private SharedPreferences sharedPreferences;
    private MainViewModel viewModel;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.tb_actionbar);
        this.setSupportActionBar(toolbar);
        toolbar.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.colorPrimaryDark));
        toolbar.setTitleTextColor(ContextCompat.getColor(MainActivity.this, R.color.textWhite));


        //Set up viewmodel
        viewModel = MainViewModel.getViewModel(this);

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
        mMapstyle = viewModel.setMapStyle(this);
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
        if (menuItem != null) {
            menuItem.setChecked(true);
        }
    }

    public void uncheckNavItem(int resId) {
        Menu m = mNavigationView.getMenu();
        MenuItem menuItem = m.findItem(resId);
        if (menuItem != null) {
            menuItem.setChecked(false);
        }
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
    public void statisticsUpdate(int numberOfCities, int numberOfCountries) {
        mNumberOfCities = numberOfCities;
        mNumberOfCountries = numberOfCountries;
    }


    private void setupSharedPreferences() {
        sharedPreferences = android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.settings_show_countries_key))) {
            viewModel.setMapStyle(this);
        } else if (key.equals(getString(R.string.settings_show_cities_key))) {
            viewModel.setMapStyle(this);
        } else if (key.equals(getString(R.string.pref_search_key))) {
            viewModel.updateDbUserData(this);
        }

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
    public void onLoginUpdate() {
        FirebaseUser currentUser = viewModel.getFireBaseUser();
        if (currentUser != null) {
            mNavigationView.getMenu().clear();
            mNavigationView.inflateMenu(R.menu.drawermenu_logged_in);
            View headerLayout = mNavigationView.getHeaderView(0);
            mNavigationView.removeHeaderView(headerLayout);
            mNavigationView.inflateHeaderView(R.layout.drawer_header_logged_in);
            headerLayout = mNavigationView.getHeaderView(0);
            CircularImageView profilePicture = headerLayout.findViewById(R.id.iv_profile_picture);
            TextView profile_nameTV = headerLayout.findViewById(R.id.tv_profile_name);
            Uri photoUrl = currentUser.getPhotoUrl();
            if (photoUrl != null && profilePicture != null) {
                Picasso.get().load(photoUrl).into(profilePicture);
            } else {
                Picasso.get().load(R.drawable.earth_icon).into(profilePicture);
            }
            String profile_name = currentUser.getDisplayName();

            if (profile_name != null && profile_nameTV != null) {
                profile_nameTV.setText(profile_name);
            }
            createMenuLoggedIn();

            //Add user to firebase database
            viewModel.addUserToDatabase();
            Log.d(TAG, "User added to online database");

            //Update actionbar menu
            updateOptionsMenu();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(getString(R.string.sp_logged_in_status), 1);
            editor.apply();


            Fragment profileFragment = new ProfileFragment();
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
        Log.d(TAG, "USER SIGNED OUT");

    }


}
