package com.bozin.worldtraveler.viewModels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.net.Uri;

import com.bozin.worldtraveler.data.PlacesRepository;
import com.bozin.worldtraveler.model.Place;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.reactivex.Single;

public class MarkerViewModel extends AndroidViewModel {


    private PlacesRepository mPlacesRepository;
    private Single<Place> place;
    private ArrayList<Uri> uriArrayList;
    private int markerId;




    public MarkerViewModel(Application application, int markerId) {
        super(application);
        mPlacesRepository = new PlacesRepository(application);
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
        uriArrayList = picturePathList;
        return uriArrayList;
    }

    public String makePathString(ArrayList<Uri> uris) {
        StringBuilder uriString = new StringBuilder();
        for (Uri uri : uris) {
            uriString.append(uri.toString()).append(",");
        }
        return uriString.toString();
    }

}
