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


    public ArrayList<Uri> getPicturePaths() {
        ArrayList<Uri> picturePathList = new ArrayList<>();
        String pictureUriString = place.getPicture_uris();
        if (pictureUriString != null) {
            List<String> pathStringsArrayList = Arrays.asList(pictureUriString.split(","));
            for (int i = 0; i < pathStringsArrayList.size(); i++) {
                String path = pathStringsArrayList.get(i);
                picturePathList.add(Uri.parse(path));
            }
        }
        return picturePathList;
    }


    public void updatePicturePaths(String picturePaths) {
        mDb.placesDao().updatePlace(id, picturePaths);
    }
}
