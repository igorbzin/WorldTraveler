package com.example.styledmap;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.example.styledmap.data.PlacesContract;
import com.example.styledmap.data.PlacesDBHelper;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A styled map using JSON styles from a raw resource.
 */
public class MapsActivityRaw extends AppCompatActivity
        implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ArrayList<MarkerOptions> markers = new ArrayList<>();
    private Geocoder gcd;
    private static final String TAG = MapsActivityRaw.class.getSimpleName();
    private LatLng mLatLong;
    private String city;
    private Location location;
    private PlaceAutocompleteFragment placeAutoComplete;
    private SQLiteDatabase mDb;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_maps_raw);


        //Set up Database
        PlacesDBHelper placesDBHelper = new PlacesDBHelper(this);
        mDb = placesDBHelper.getWritableDatabase();


        // Get the SupportMapFragment and register for the callback
        // when the map is ready for use.
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        /*
        //Get Location Permission on runtime
        if (ActivityCompat.checkSelfPermission(MapsActivityRaw.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivityRaw.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MapsActivityRaw.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        */

        if (isPermissionGranted()) {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            String provider = locationManager.getBestProvider(criteria, false);
            location = locationManager.getLastKnownLocation(provider);
            mLatLong = new LatLng(location.getLatitude(), location.getLongitude());
        }


        //Retrieve markers from db
        retrieveMarkers();


        //Set up search bar for places
        placeAutoComplete = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete);
        placeAutoComplete.setHint("Add visited places");
        placeAutoComplete.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                //Retrieve information about selected place and zoom into it
                mLatLong = place.getLatLng();
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(mLatLong.latitude, mLatLong.longitude))      // Sets the center of the map to location user
                        .zoom(4)                   // Sets the zoom
                        .build();                   // Creates a CameraPosition from the builder
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                Log.d("Maps", "Place selected: " + place.getName());


                //Retrieve location information from latitude and longitude
                gcd = new Geocoder(MapsActivityRaw.this, Locale.getDefault());
                try {
                    List<Address> address = gcd.getFromLocation(mLatLong.latitude, mLatLong.longitude, 1);
                    city = address.get(0).getLocality();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Initialize the dialog object
                AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivityRaw.this);
                builder.setTitle(city);
                builder.setMessage("This is not the city you were looking for? Press cancel and try again.");


                //Set positive button for marker placement
                builder.setPositiveButton("ADD PLACE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //add marker on positive click on map
                        MarkerOptions currentMarker = new MarkerOptions()
                                .position(mLatLong)
                                .title(city)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                        markers.add(currentMarker);
                        mMap.addMarker(currentMarker);

                        //add place into database
                        addNewPlace(city, mLatLong.latitude, mLatLong.longitude);

                    }
                });


                //Set button to cancel add action
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });


                // Set up the buttons
                builder.show();
            }


            @Override
            public void onError(Status status) {
                Log.d("Maps", "An error occurred: " + status);
            }
        });

    }


    /**
     * Manipulates the map when it's available.
     * The API invokes this callback when the map is ready for use.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        //Initialize the map
        mMap = googleMap;


        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Intent intent = new Intent(MapsActivityRaw.this, MarkerActivity.class);
                startActivity(intent);
            }
        });

        mMap.setOnInfoWindowLongClickListener(new GoogleMap.OnInfoWindowLongClickListener() {
            @Override
            public void onInfoWindowLongClick(Marker marker) {
                LatLng markerPos = marker.getPosition();
                double markerLat = markerPos.latitude;
                double markerLon = markerPos.longitude;
                boolean test = removeMarker(markerLat, markerLon);
                mMap.clear();
                retrieveMarkers();
                setMarkers();
            }
        });


        //Set markers retrieved from database on now available map
        setMarkers();

        // Set the button for retrieving the current location and moving camera to it
        try {
            mMap.setMyLocationEnabled(true);
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        //Styling map, setting default map rules
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(false);

        //Style the map
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.map_style));

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }


        // Position the map's camera
        if (location != null) {
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(mLatLong)      // Sets the center of the map to location user
                    .zoom(3)                   // Sets the zoom
                    .build();                   // Creates a CameraPosition from the builder
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }


    // Database function for retrieving current database data in a cursor
    private Cursor getAllPlaces() {
        return mDb.query(
                PlacesContract.PlacesEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                PlacesContract.PlacesEntry._ID
        );
    }


    //Function to add new places into the database
    private long addNewPlace(String name, double latitude, double longitude) {
        ContentValues cv = new ContentValues();
        cv.put(PlacesContract.PlacesEntry.COLUMN_CITY, name);
        cv.put(PlacesContract.PlacesEntry.COLUMN_LATITUDE, latitude);
        cv.put(PlacesContract.PlacesEntry.COLUMN_LONGITUDE, longitude);
        return mDb.insert(PlacesContract.PlacesEntry.TABLE_NAME, null, cv);
    }

    //Remove marker from db
    private boolean removeMarker(double latitude, double longitude) {
        return mDb.delete(PlacesContract.PlacesEntry.TABLE_NAME, PlacesContract.PlacesEntry.COLUMN_LATITUDE + "=" + latitude + " AND " + PlacesContract.PlacesEntry.COLUMN_LONGITUDE + "=" + longitude, null) > 0;
    }


    //Function to set all markers retrieved from database
    private void setMarkers() {
        for (int i = 0; i < markers.size(); i++) {
            MarkerOptions option = markers.get(i);
            mMap.addMarker(option);
        }
    }


    //Retrieve all markers from database
    private void retrieveMarkers() {
        markers.clear();
        Cursor cursor = getAllPlaces();
        for (int i = 0; i < cursor.getCount(); i++) {
            if (!cursor.moveToPosition(i)) {

            } else {
                String cCity = cursor.getString(cursor.getColumnIndex(PlacesContract.PlacesEntry.COLUMN_CITY));
                double cLatitude = cursor.getDouble(cursor.getColumnIndex(PlacesContract.PlacesEntry.COLUMN_LATITUDE));
                double cLongitude = cursor.getDouble(cursor.getColumnIndex(PlacesContract.PlacesEntry.COLUMN_LONGITUDE));
                LatLng cLatLng = new LatLng(cLatitude, cLongitude);
                MarkerOptions cMarkerOptions = new MarkerOptions().title(cCity).position(cLatLng).draggable(true).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                markers.add(cMarkerOptions);
            }
        }
    }


    public boolean isPermissionGranted() {

        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted");
                return true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION
                }, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted");
            return true;
        }

    }

}
