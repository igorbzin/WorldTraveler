package com.bozin.worldtraveler;


import android.annotation.SuppressLint;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bozin.worldtraveler.data.AppDatabase;
import com.bozin.worldtraveler.data.AppExecutor;
import com.bozin.worldtraveler.data.Place;
import com.bozin.worldtraveler.databinding.FragmentProfileBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;


public class ProfileFragment extends Fragment implements OnMapReadyCallback {


    private final String TAG = "ProfileFragment";
    private GoogleMap mMap;
    private SupportMapFragment supportMapFragment;
    private PlacesViewModel viewModel;
    private List<Place> placesList = new ArrayList<>();
    private LinkedHashMap<Integer, MarkerOptions> markerHashMap;
    private LinkedHashMap<Integer, String> mCountriesVisited;
    private FragmentProfileBinding profileBinding;
    private int citiesVisited;
    private int countriesVisited;


    public interface onSignedOutHandler {
        void onSignedOut();
    }

    public static ProfileFragment newInstance(String userName) {
        ProfileFragment profileFragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString("user_name", userName);
        profileFragment.setArguments(args);
        return profileFragment;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        profileBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile, container, false);
        GoogleMapOptions options = new GoogleMapOptions().liteMode(true);
        markerHashMap = new LinkedHashMap<>();
        mCountriesVisited = new LinkedHashMap<>();

        try {
            String userName = (String) Objects.requireNonNull(getArguments()).get("user_name");
            profileBinding.textView2.setText(userName);
        } catch (Exception e) {
            e.printStackTrace();
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            profileBinding.textView2.setText(Objects.requireNonNull(mAuth.getCurrentUser()).getDisplayName());
        }

        if (supportMapFragment == null) {
            supportMapFragment = SupportMapFragment.newInstance(options);
            supportMapFragment.getMapAsync(this);
        }
        getChildFragmentManager().beginTransaction().replace(R.id.fragment_profile_map, supportMapFragment).commit();

        return profileBinding.getRoot();

    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        viewModel = ViewModelProviders.of(getActivity()).get(PlacesViewModel.class);
    }


    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        AppExecutor.getInstance().diskIO().execute(() -> {
            AppDatabase.getInstance(getContext());
            setupViewModel();
        });

        //Style the map

        String mapStyle = getMapStyle();
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = mMap.setMapStyle(new MapStyleOptions(mapStyle));

            if (!success) {
                Log.e("MAP_STYLE", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("MAP_STYLE", "Can't find style. Error: ", e);
        }


        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(30, 31), 0));

        mMap.getUiSettings().setMapToolbarEnabled(false);


        mMap.setOnMapClickListener(latLng -> openImage());

        profileBinding.btnFragmentProfileShare.setOnClickListener(view -> openShareImageDialog());

        mMap.setOnMapLoadedCallback(this::snapShot);
    }


    private void setupViewModel() {
        viewModel = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(PlacesViewModel.class);
        viewModel.getPlacesList().observe(this, places -> {
            Log.d(TAG, "Updating list of places from LiveData in ViewModel");
            placesList = places;
            createMarkersFromPlaces();
            AppExecutor.getInstance().mainThread().execute(() -> {
                setMarkers();
                updateStatisticNumbers();
            });

        });
    }

    private String getMapStyle() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        return sharedPreferences.getString(getString(R.string.sp_mapstyle_key), "0");
    }

    private void createMarkersFromPlaces() {
        for (int i = 0; i < placesList.size(); i++) {
            com.bozin.worldtraveler.data.Place place = placesList.get(i);
            MarkerOptions markerOptions = createMarkerOptions(place);
            markerHashMap.put(place.getPlaceID(), markerOptions);
        }
    }

    private MarkerOptions createMarkerOptions(com.bozin.worldtraveler.data.Place place) {
        String city = place.getCity_name();
        String country = place.getCountry_name();
        double latitude = place.getLatitude();
        double longitude = place.getLongitude();
        int markerId = place.getPlaceID();
        LatLng latLng = new LatLng(latitude, longitude);


        if (!mCountriesVisited.containsValue(country)) {
            mCountriesVisited.put(markerId, country);
        }

        Bitmap b = getBitmapFromVectorDrawable(getContext(), R.drawable.ic_location_on_black_24dp);
        Bitmap bhalfsize = Bitmap.createScaledBitmap(b, b.getWidth() / 2, b.getHeight() / 2, false);

        return new MarkerOptions().title(city)
                .snippet(country).position(latLng).draggable(true)
                .icon(BitmapDescriptorFactory.fromBitmap(bhalfsize));
    }


    private void setMarkers() {
        mMap.clear();
        ArrayList<MarkerOptions> markerOptionsList = new ArrayList<>(markerHashMap.values());
        for (int i = 0; i < markerOptionsList.size(); i++) {
            MarkerOptions option = markerOptionsList.get(i);
            mMap.addMarker(option);
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    public static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(Objects.requireNonNull(drawable))).mutate();
        }

        Bitmap bitmap = Bitmap.createBitmap(Objects.requireNonNull(drawable).getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }


    public void snapShot() {

        GoogleMap.SnapshotReadyCallback callback = new GoogleMap.SnapshotReadyCallback() {
            Bitmap bitmap;
            File file;

            @Override
            public void onSnapshotReady(Bitmap snapshot) {
                bitmap = snapshot;
                try {

                    file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "map.png");
                    FileOutputStream fout = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, fout);

                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        };
        mMap.snapshot(callback);
    }

    public void openShareImageDialog() {
        createPolicyRules();
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "map.png");
        if (file.exists()) {
            Uri uri = Uri.parse("file://" + file.getAbsolutePath());
            Intent share = new Intent(Intent.ACTION_SEND);
            share.putExtra(Intent.EXTRA_STREAM, uri);
            share.putExtra(Intent.EXTRA_TEXT, getString(R.string.fragment_profile_share_message));
            share.setType("image/*");
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Objects.requireNonNull(getContext()).startActivity(Intent.createChooser(share, "Share file"));
        }
    }

    public void openImage() {
        createPolicyRules();
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "map.png");
        if (file.exists()) {
            Uri uri = Uri.parse("file://" + file.getAbsolutePath());
            Intent open = new Intent(Intent.ACTION_VIEW);
            open.setDataAndType(uri, "image/*");
            startActivity(open);
        }
    }

    public void createPolicyRules() {

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
    }

    public void updateStatisticNumbers() {
        citiesVisited = markerHashMap.size();
        countriesVisited = mCountriesVisited.size();
        profileBinding.tvProfileNumberOfCitiesVisited.setText(String.valueOf(citiesVisited));
        profileBinding.tvProfileNumberOfCountriesVisited.setText(String.valueOf(countriesVisited));
        MapFragment.MapFragmentStatisticsListener statisticsListener = (MapFragment.MapFragmentStatisticsListener) Objects.requireNonNull(getContext());
        statisticsListener.statisticsUpdate(citiesVisited, countriesVisited);
        if(countriesVisited < 40){
            profileBinding.ivProfileBadge.setImageDrawable(getResources().getDrawable(R.drawable.bronze_badge));
        } else if(countriesVisited >= 40 && countriesVisited <= 80){
            profileBinding.ivProfileBadge.setImageDrawable(getResources().getDrawable(R.drawable.silver_badge));
        } else if(countriesVisited > 80){
            profileBinding.ivProfileBadge.setImageDrawable(getResources().getDrawable(R.drawable.golden_badge));
        }
    }

}
