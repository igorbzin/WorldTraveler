package com.bozin.worldtraveler.viewModels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.net.Uri;
import android.util.Log;

import com.bozin.worldtraveler.data.PlacesRepository;
import com.bozin.worldtraveler.model.Place;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;

import io.reactivex.Completable;
import io.reactivex.Single;

public class MarkerViewModel extends AndroidViewModel {


    private PlacesRepository mPlacesRepository;
    private Single<Place> place;
    private Application application;
    private ArrayList<Uri> uriArrayList;

    private int markerId;


    public MarkerViewModel(Application application, int markerId) {
        super(application);
        mPlacesRepository = new PlacesRepository(application);
        this.application = application;
        this.markerId = markerId;
        this.uriArrayList = new ArrayList<>();
    }


    public Single<Place> getPlace() {
        place = mPlacesRepository.getPlace(markerId);
        return place;
    }


    public void updatePicturePaths(String picturePaths) {
          mPlacesRepository.updatePlace(markerId, picturePaths);
    }


    //Get Picture Uris from DB
    public ArrayList<Uri> getPicturePaths(Place place) {
        ArrayList<Uri> picturePathList = new ArrayList<>();
        String pictureUriString = place.getPicture_uris();
        if (pictureUriString != null && !pictureUriString.equals("")) {
            List<String> pathStringsArrayList = Arrays.asList(pictureUriString.split(","));
            for (int i = 0; i < pathStringsArrayList.size(); i++) {
                String path = pathStringsArrayList.get(i);
                picturePathList.add(Uri.parse(path));
            }
        }
        return picturePathList;
    }

    public String makePathString(ArrayList<Uri> uriArrayList) {
        StringBuilder uriString = new StringBuilder();
        for (Uri uri : uriArrayList) {
            uriString.append(uri.toString()).append(",");
        }
        return uriString.toString();
    }


}
