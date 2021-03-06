package com.bozin.worldtraveler.data;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;


import com.bozin.worldtraveler.model.Place;

import java.util.List;
import java.util.Observable;

import io.reactivex.Completable;
import io.reactivex.Single;

@Dao
public interface PlacesDao {

    @Query("SELECT * FROM cities ORDER BY _ID")
    LiveData<List<Place>> loadAllPlaces();

    @Query("SELECT * FROM cities WHERE _ID = :placeId")
    Single<Place> loadPlaceById(int placeId);

    @Query("UPDATE cities SET picture_uris=:pictureUris WHERE _ID = :id")
    void updatePlace(int id, String pictureUris);

    @Insert
    void insertPlace(Place place);


    @Query("DELETE FROM cities WHERE _ID = :placeId")
    void deleteByPlaceId(int placeId);



}
