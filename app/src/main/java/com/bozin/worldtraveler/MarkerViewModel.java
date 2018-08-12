package com.bozin.worldtraveler;

import android.arch.lifecycle.ViewModel;
import com.bozin.worldtraveler.data.AppDatabase;
import com.bozin.worldtraveler.data.Place;

class MarkerViewModel extends ViewModel {


    private AppDatabase mDb;
    private Place place;

    private int id;


     MarkerViewModel(AppDatabase appDatabase, int id) {
        mDb = appDatabase;
        this.id = id;
        place = mDb.placesDao().loadPlaceById(id);

    }


    public Place getPlace() {
        return place;

    }


    public void updatePicturePaths(String picturePaths) {
        mDb.placesDao().updatePlace(id, picturePaths);
    }
}
