package com.bozin.worldtraveler.data;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import java.util.List;

public class PlacesRepository {

    private PlacesDao mPlacesDao;
    private LiveData<List<Place>> mPlacesList;



    public PlacesRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        mPlacesDao = db.placesDao();
        mPlacesList = mPlacesDao.loadAllPlaces();
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
