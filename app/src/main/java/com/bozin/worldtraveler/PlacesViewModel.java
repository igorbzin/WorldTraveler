package com.bozin.worldtraveler;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.bozin.worldtraveler.data.Place;
import com.bozin.worldtraveler.data.PlacesRepository;


import java.util.List;


public class PlacesViewModel extends AndroidViewModel {

    private PlacesRepository mPlacesRepository;
    private LiveData<List<Place>> placesList;


    public PlacesViewModel(@NonNull Application application) {
        super(application);
        mPlacesRepository = new PlacesRepository(application);
        placesList = mPlacesRepository.getAllPlaces();
    }

    public LiveData<List<Place>> getPlacesList() {
        return placesList;
    }



    public void insertPlace (Place place){
        mPlacesRepository.insertPlace(place);
    }

    public void deletePlaceById (int id){
        mPlacesRepository.deletePlaceByID(id);
    }



}
