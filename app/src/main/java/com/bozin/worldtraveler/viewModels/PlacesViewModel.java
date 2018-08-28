package com.bozin.worldtraveler.viewModels;

import android.annotation.SuppressLint;
import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;

import com.bozin.worldtraveler.R;
import com.bozin.worldtraveler.data.PlacesRepository;
import com.bozin.worldtraveler.model.Place;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;


public class PlacesViewModel extends AndroidViewModel {
    private PlacesRepository mPlacesRepository;
    private LiveData<List<Place>> placesList;
    private LinkedHashMap<Integer, String> countriesVisited;
    private LinkedHashMap<Integer, MarkerOptions> placesHashMap;

    public PlacesViewModel(@NonNull Application application) {
        super(application);
        mPlacesRepository = new PlacesRepository(application);
        placesList = mPlacesRepository.getAllPlaces();
        countriesVisited = new LinkedHashMap<>();
        placesHashMap = new LinkedHashMap<>();
    }

    public LiveData<List<Place>> getPlacesList() {
        return placesList;
    }

    public void insertPlace(Place place) {
        mPlacesRepository.insertPlace(place);
    }

    public void deletePlaceById(int id) {
        mPlacesRepository.deletePlaceByID(id);
    }


    //NEED TO CALL THIS FIRST TO INIT PLACES HASHMAP
    public void createMarkersFromPlaces(List<Place> list, Object... objects) {
        for (Place place : list) {

            String city = place.getCity_name();
            String country = place.getCountry_name();
            double latitude = place.getLatitude();
            double longitude = place.getLongitude();
            int markerId = place.getPlaceID();
            LatLng latLng = new LatLng(latitude, longitude);

            if (!countriesVisited.containsValue(country)) {
                countriesVisited.put(markerId, country);
            }

            MarkerOptions markerOptions;

            int customBitmap = objects.length > 0 ? (int) objects[0] : 0;
            Context context = objects.length > 1 ? (Context)objects[1] : null;

            if (customBitmap == 1 && context != null) {
                Bitmap b = getBitmapFromVectorDrawable(context, R.drawable.ic_location_on_black_24dp);
                Bitmap bHalfSize = Bitmap.createScaledBitmap(b, b.getWidth() / 2, b.getHeight() / 2, false);

                markerOptions = new MarkerOptions().title(city)
                        .snippet(country).position(latLng).draggable(true)
                        .icon(BitmapDescriptorFactory.fromBitmap(bHalfSize));
            } else {
                markerOptions = new MarkerOptions().title(city)
                        .snippet("" + markerId).position(latLng).draggable(true)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            }


            placesHashMap.put(place.getPlaceID(), markerOptions);
        }
    }



    @SuppressLint("ObsoleteSdkInt")
    private static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
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

    public LinkedHashMap<Integer, MarkerOptions> getPlacesHashMap() {
        return placesHashMap;
    }

    public LinkedHashMap<Integer, String> getCountriesVisited() {
        return countriesVisited;
    }

    public void removeCountry(int id) {
        countriesVisited.remove(id);
    }

    public void removeMarker(int id) {
        placesHashMap.remove(id);
    }


}
