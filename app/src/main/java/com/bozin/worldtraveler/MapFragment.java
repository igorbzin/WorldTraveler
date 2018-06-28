package com.bozin.worldtraveler;

import android.app.Activity;
import android.app.ActivityOptions;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.constraint.motion.MotionLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import com.bozin.worldtraveler.Adapters.CustomInfoWindowAdapter;
import com.bozin.worldtraveler.data.AppDatabase;
import com.bozin.worldtraveler.data.AppExecutor;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by igorb on 03/03/2018.
 */

public class MapFragment extends android.support.v4.app.Fragment implements OnMapReadyCallback {

    private final String TAG = "Room observer";
    private MapView mMapView;
    public AppDatabase mDb;
    private GoogleMap mMap;
    private List<com.bozin.worldtraveler.data.Place> placesList = new ArrayList<>();
    private Geocoder gcd;
    private LatLng mLatLong;
    private Location location;
    private TextView tv_delete;
    private Button mAddButton;
    private Animation mStartFadeInAnimation;
    private Animation mStartFadeOutAnimation;
    private int mCameraPosition;
    public LinkedHashMap<Integer, MarkerOptions> markerHashMap;
    private LinkedHashMap<Integer, String> mCountriesVisited;
    private PlacesViewModel viewModel;
    private MotionLayout motionLayout;

    private final int REQUEST_CODE_SEARCH_ACTIVITY = 1;


    MapFragmentStatisticsListener mCallback;

    public static MapFragment newInstance() {
        MapFragment mapFragment = new MapFragment();
        return mapFragment;
    }

    // Container Activity must implement this interface
    public interface MapFragmentStatisticsListener {
        void statisticsUpdate(int numberOfCities, int numberOfCountries);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_map, container, false);
        motionLayout = rootView.findViewById(R.id.motion02_Layout_map_fragment);
        mAddButton = rootView.findViewById(R.id.btn_add_place);
        mMapView = rootView.findViewById(R.id.google_map);

        //Set up views
        tv_delete = rootView.findViewById(R.id.tv_delete_marker);

        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(this);
        return rootView;

    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mCountriesVisited = new LinkedHashMap<>();


        //Set up location manager
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider;

        try {
            provider = locationManager.getBestProvider(criteria, false);
            location = locationManager.getLastKnownLocation(provider);
        } catch (
                NullPointerException e) {
            e.printStackTrace();
        } catch (SecurityException exception) {
            exception.printStackTrace();
        }
        try {
            mLatLong = new LatLng(location.getLatitude(), location.getLongitude());
        } catch (Exception e) {
            e.printStackTrace();
        }

        markerHashMap = new LinkedHashMap<>();

        //Retrieve markers from db
        viewModel = ViewModelProviders.of(this).get(PlacesViewModel.class);
        mDb = AppDatabase.getInstance(getContext());
        setupViewModel();

        //Set up add button
        mAddButton.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View v) {
                refresh();
                try {
                    AutocompleteFilter filter = new AutocompleteFilter.Builder().setTypeFilter(5).build();
                    ActivityOptionsCompat options =
                            ActivityOptionsCompat.makeCustomAnimation(getContext(), R.anim.slide_in, R.anim.slide_out);
                    /*
                    PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

                    startActivityForResult(builder.build(getActivity()), REQUEST_CODE_SEARCH_ACTIVITY, options.toBundle());
                    */

                    Intent intent =
                            new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                                    .setFilter(filter)
                                    .build(getActivity());

                    startActivityForResult(intent, REQUEST_CODE_SEARCH_ACTIVITY, options.toBundle());

                } catch (GooglePlayServicesRepairableException e) {
                    // TODO: Handle the error.
                } catch (GooglePlayServicesNotAvailableException e) {
                    // TODO: Handle the error.
                }
            }
        });




        //Animations setup
        mStartFadeInAnimation = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.fade_in);
        mStartFadeInAnimation.setFillAfter(true);
        mStartFadeOutAnimation = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.fade_out);
        mStartFadeOutAnimation.setFillAfter(true);

        //camera position
        mCameraPosition = 0;

        //updateStatisticNumbers();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (MapFragmentStatisticsListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement MapfragmentStatisticsListener");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        motionLayout.transitionToEnd();
    }

    private void setupViewModel() {
        viewModel.getPlacesList().observe(this, new Observer<List<com.bozin.worldtraveler.data.Place>>() {
            @Override
            public void onChanged(@Nullable List<com.bozin.worldtraveler.data.Place> places) {
                Log.d(TAG, "Updating list of places from LiveData in ViewModel");
                placesList = places;
                createMarkersFromPlaces();
                if(markerHashMap != null){
                    setMarkers();
                    updateStatisticNumbers();
                }
            }
        });
    }


    public MarkerOptions createMarkerOptions(com.bozin.worldtraveler.data.Place place) {
        String city = place.getCity_name();
        String country = place.getCountry_name();
        double latitude = place.getLatitude();
        double longitude = place.getLongitude();
        int markerId = place.getPlaceID();
        LatLng latLng = new LatLng(latitude, longitude);

        if (!mCountriesVisited.containsValue(country)) {
            mCountriesVisited.put(markerId, country);
        }

        MarkerOptions cMarkerOptions = new MarkerOptions().title(city)
                .snippet("" + markerId).position(latLng).draggable(true)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        return cMarkerOptions;
    }


    public void createMarkersFromPlaces() {
        for (int i = 0; i < placesList.size(); i++) {
            com.bozin.worldtraveler.data.Place place = placesList.get(i);
            MarkerOptions markerOptions = createMarkerOptions(place);
            markerHashMap.put(place.getPlaceID(), markerOptions);
        }
    }


    //Function to set all markers retrieved from database
    public void setMarkers() {
        mMap.clear();
        ArrayList<MarkerOptions> markerOptionsList = new ArrayList<>(markerHashMap.values());
        for (int i = 0; i < markerOptionsList.size(); i++) {
            MarkerOptions option = markerOptionsList.get(i);
            mMap.addMarker(option);
        }
    }



    public void insertPlace(com.bozin.worldtraveler.data.Place place){
        viewModel.insertPlace(place);
    }


    public void deletePlaceById(int id){
        viewModel.deletePlaceById(id);
    }


    public void refresh() {
        String mapStyle = getMapStyle();
        //Style the map
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = mMap.setMapStyle(new MapStyleOptions(mapStyle));

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_CODE_SEARCH_ACTIVITY) {
            if (resultCode == Activity.RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(getActivity(), data);


                //Retrieve information about selected place and zoom into it
                mLatLong = place.getLatLng();
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(mLatLong.latitude, mLatLong.longitude))      // Sets the center of the map to location user
                        .zoom(4)                   // Sets the zoom
                        .build();                   // Creates a CameraPosition from the builder
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                Log.d("Maps", "Place selected: " + place.getName());

                String city = null;
                String country = null;


                //Retrieve location information from latitude and longitude
                gcd = new Geocoder(getActivity(), Locale.getDefault());
                try {
                    List<Address> address = gcd.getFromLocation(mLatLong.latitude, mLatLong.longitude, 1);
                    if (place.getLocale() != null) {
                        city = address.get(0).getLocality();
                    } else {
                        city = place.getName().toString();
                    }

                    try {
                        country = address.get(0).getCountryName();
                    } catch (Exception e) {
                        e.printStackTrace();
                        country = "No country data available";
                        return;
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }


                //Check if selected place was already added


                if (markerHashMap.size() != 0) {
                    for (int i = 0; i < markerHashMap.size(); i++) {
                        MarkerOptions marker = (new ArrayList<>(markerHashMap.values()).get(i));
                        if (marker.getPosition().latitude == mLatLong.latitude && marker.getPosition().longitude == mLatLong.longitude) {
                            Snackbar duplicate = Snackbar.make(getActivity().findViewById(R.id.drawer_layout), R.string.snackbar_duplicate, Snackbar.LENGTH_LONG);
                            View sb_duplicateView = duplicate.getView();
                            TextView tv_sb_duplicate = sb_duplicateView.findViewById(android.support.design.R.id.snackbar_text);
                            tv_sb_duplicate.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                            sb_duplicateView.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorAccent));
                            duplicate.show();
                            return;
                        }

                    }
                }


                Snackbar placeAdded = Snackbar.make(getActivity().findViewById(R.id.drawer_layout), R.string.snackbar_place_added, Snackbar.LENGTH_LONG);
                View sb_placeAddedView = placeAdded.getView();
                sb_placeAddedView.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark));
                TextView tv_sb_placeAdded = sb_placeAddedView.findViewById(android.support.design.R.id.snackbar_text);
                tv_sb_placeAdded.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                placeAdded.show();


                final com.bozin.worldtraveler.data.Place dbPlace = new com.bozin.worldtraveler.data.Place(city, country, mLatLong.latitude, mLatLong.longitude, null);
                AppExecutor.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        insertPlace(dbPlace);
                    }
                });
            }

            if (resultCode == Activity.RESULT_CANCELED) {
                Snackbar noPlaceAdded = Snackbar.make(getActivity().findViewById(R.id.drawer_layout), R.string.snackbar_no_place_added, Snackbar.LENGTH_LONG);
                View sb_noPlaceAddedView = noPlaceAdded.getView();
                sb_noPlaceAddedView.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorAccent));
                TextView tv_sb_noPlaceAdded = sb_noPlaceAddedView.findViewById(android.support.design.R.id.snackbar_text);
                tv_sb_noPlaceAdded.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

            }
            onMapReady(mMap);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {


        //Initialize the map
        mMap = googleMap;

        //Setting default map rules
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setRotateGesturesEnabled(false);

        //Disable Map Toolbar:
        mMap.getUiSettings().setMapToolbarEnabled(false);

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                marker.showInfoWindow();
                return true;
            }
        });


        //Set onclicklistener for Marker dragging to delete it
        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
                tv_delete.setTextColor(Color.BLACK);
                tv_delete.startAnimation(mStartFadeInAnimation);
                mAddButton.startAnimation(mStartFadeOutAnimation);
            }

            @Override
            public void onMarkerDrag(Marker marker) {
                LatLng position = marker.getPosition();
                Projection projection = mMap.getProjection();
                Point screenPosition = projection.toScreenLocation(position);
                int tv_delete_top_boundry = tv_delete.getTop();
                if (screenPosition.y >= tv_delete_top_boundry) {
                    tv_delete.setTextColor(Color.RED);
                } else {
                    tv_delete.setTextColor(Color.BLACK);
                }


            }

            @Override
            public void onMarkerDragEnd(final Marker marker) {
                String string_id = marker.getSnippet();
                final int id = Integer.parseInt(string_id);
                LatLng position = marker.getPosition();
                Projection projection = mMap.getProjection();
                Point screenPosition = projection.toScreenLocation(position);
                int tv_delete_top_boundry = tv_delete.getTop();

                if (screenPosition.y >= tv_delete_top_boundry) {
                    AppExecutor.getInstance().diskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            deletePlaceById(id);
                            mCountriesVisited.remove(id);
                            markerHashMap.remove(id);
                            updateStatisticNumbers();
                        }
                    });

                } else {
                    setMarkers();
                }


                tv_delete.startAnimation(mStartFadeOutAnimation);
                mAddButton.startAnimation(mStartFadeInAnimation);

            }
        });


        //Configure custom infowindow through InfoWindowAdapter
        CustomInfoWindowAdapter adapter = new CustomInfoWindowAdapter(getActivity());
        mMap.setInfoWindowAdapter(adapter);

        //Set onclicklistener for InfoWindow
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                String markerId = marker.getSnippet();
                int id = Integer.valueOf(markerId);
                Intent intent = new Intent(getContext(), MarkerActivity.class);
                intent.putExtra("MarkerID", id);
                startActivity(intent);
            }
        });


        //Set markers retrieved from database on now available map
        //setMarkers();

        // Set the button for retrieving the current location and moving camera to it
        try {
            mMap.setMyLocationEnabled(true);
        } catch (SecurityException e) {
            e.printStackTrace();
        }


        //Style the map

        String mapStyle = getMapStyle();
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(new MapStyleOptions(mapStyle));

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }


        // Position the map's camera
        if (mCameraPosition == 0) {
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(44.66278, 20.93))      // Sets the center of the map to location user
                    .zoom(1)                   // Sets the zoom
                    .build();                   // Creates a CameraPosition from the builder
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            mCameraPosition = 1;

        }
    }


    public void updateStatisticNumbers() {
        int numberofCities = markerHashMap.size();
        int numberOfCountries = mCountriesVisited.size();
        mCallback.statisticsUpdate(numberofCities, numberOfCountries);
    }


    private String getMapStyle() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String mapStyle = sharedPreferences.getString(getString(R.string.sp_mapstyle_key), "0");
        return mapStyle;
    }




    @Override
    public void onPause() {
        super.onPause();
        motionLayout.transitionToStart();
    }
}

