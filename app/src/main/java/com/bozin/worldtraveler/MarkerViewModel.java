package com.bozin.worldtraveler;

import android.arch.lifecycle.ViewModel;
import android.arch.persistence.room.PrimaryKey;
import android.net.Uri;

import com.bozin.worldtraveler.data.AppDatabase;
import com.bozin.worldtraveler.data.Place;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MarkerViewModel extends ViewModel {


    private AppDatabase mDb;
    private Place place;

    private int id;


    public MarkerViewModel(AppDatabase appDatabase, int id) {
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
