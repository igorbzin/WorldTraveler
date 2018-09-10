package com.bozin.worldtraveler.data;

import android.app.Application;
import android.arch.lifecycle.LiveData;

import com.bozin.worldtraveler.model.Place;

import java.util.List;
import java.util.Observable;

import io.reactivex.Completable;
import io.reactivex.Single;

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

    public Single<Place> getPlace(int id) {
        return mPlacesDao.loadPlaceById(id);
    }

    public void updatePlace(int markerId, String picturePaths) {
        mPlacesDao.updatePlace(markerId, picturePaths);
    }

    public void insertPlace(Place place) {
         mPlacesDao.insertPlace(place);
    }


    public void deletePlaceByID(int id) {
        mPlacesDao.deleteByPlaceId(id);
    }
}
