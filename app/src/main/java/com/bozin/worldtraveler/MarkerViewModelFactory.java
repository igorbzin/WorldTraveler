package com.bozin.worldtraveler;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import com.bozin.worldtraveler.data.AppDatabase;

public class MarkerViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    // COMPLETED (2) Add two member variables. One for the database and one for the taskId
    private final AppDatabase mDb;
    private final int markerID;

    // COMPLETED (3) Initialize the member variables in the constructor with the parameters received
    public MarkerViewModelFactory(AppDatabase database, int markerID) {
        mDb = database;
        this.markerID = markerID;
    }


    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        //noinspection unchecked
        return (T) new MarkerViewModel(mDb, markerID);
    }
}
