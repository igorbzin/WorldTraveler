package com.bozin.worldtraveler.data;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.net.Uri;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PlacesRepository {

    private PlacesDao mPlacesDao;
    private LiveData<List<Place>> mPlacesList;
    private ArrayList<Uri> mPicturePaths;


    public PlacesRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        mPlacesDao = db.placesDao();
        mPlacesList = mPlacesDao.loadAllPlaces();
        mPicturePaths = new ArrayList<>();
    }

    public LiveData<List<Place>> getAllPlaces() {
        return mPlacesList;
    }


    public void insertPlace(Place place){
        mPlacesDao.insertPlace(place);
    }







    public void deletePlaceByID(int id){
        mPlacesDao.deleteByPlaceId(id);
    }
}
