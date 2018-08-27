package com.bozin.worldtraveler.viewModels;

import android.app.Application;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.bozin.worldtraveler.data.PlacesRepository;

public class MarkerViewModelFactory extends ViewModelProvider.AndroidViewModelFactory {

    // COMPLETED (2) Add two member variables. One for the database and one for the taskId
    private PlacesRepository placesRepository;
    private Application application;
    private final int markerID;

    // COMPLETED (3) Initialize the member variables in the constructor with the parameters received
    public MarkerViewModelFactory(Application application, int markerID) {
        super(application);
        placesRepository = new PlacesRepository(application);
        this.markerID = markerID;
    }


    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        //noinspection unchecked
        return (T) new MarkerViewModel(application, markerID);
    }
}
