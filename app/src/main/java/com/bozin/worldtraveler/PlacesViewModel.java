package com.bozin.worldtraveler;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.bozin.worldtraveler.data.Place;
import com.bozin.worldtraveler.data.PlacesRepository;

import java.util.ArrayList;

import java.util.List;


public class PlacesViewModel extends AndroidViewModel {

    private PlacesRepository mPlacesRepository;
    private LiveData<List<Place>> placesList;
    private ArrayList<Uri> picturePathList;
    private int id;

    public PlacesViewModel(@NonNull Application application) {
        super(application);
        mPlacesRepository = new PlacesRepository(application);
        placesList = mPlacesRepository.getAllPlaces();
    }

    public LiveData<List<Place>> getPlacesList() {
        return placesList;
    }

    public ArrayList<Uri> getPicturePaths(int id) {
        picturePathList = mPlacesRepository.getPicturePaths(id);
        return picturePathList;
    }

    public void updatePicturePaths (int id, String picturePaths){
        mPlacesRepository.updatePicturePaths(id, picturePaths);
    }

    public void insertPlace (Place place){
        mPlacesRepository.insertPlace(place);
    }

    public void deletePlaceById (int id){
        mPlacesRepository.deletePlaceByID(id);
    }



}
