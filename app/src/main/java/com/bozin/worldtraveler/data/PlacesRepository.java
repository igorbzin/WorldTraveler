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


    public ArrayList<Uri> getPicturePaths(int id) {
        Place place = mPlacesDao.loadPlaceById(id);
        String pictureUriString = place.getPicture_uris();
        if (pictureUriString != null) {
            List<String> pathStringsArrayList = Arrays.asList(pictureUriString.split(","));
            for (int i = 0; i < pathStringsArrayList.size(); i++) {
                String path = pathStringsArrayList.get(i);
                mPicturePaths.add(Uri.parse(path));
            }
        }
        return mPicturePaths;
    }


    public void updatePicturePaths(int id, String picturePaths){
        mPlacesDao.updatePlace(id, picturePaths);
    }


    public void deletePlaceByID(int id){
        mPlacesDao.deleteByPlaceId(id);
    }
}
