package com.bozin.worldtraveler.data;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;


import java.util.List;

@Dao
public interface PlacesDao {

    @Query("SELECT * FROM places ORDER BY _ID")
    LiveData<List<Place>> loadAllPlaces();

    @Query("SELECT * FROM places WHERE _ID = :placeId")
    Place loadPlaceById(int placeId);

    @Query("UPDATE places SET picture_uris=:pictureUris WHERE _ID = :id")
    void updatePlace(int id, String pictureUris);

    @Insert
    void insertPlace(Place place);

    @Delete
    void deletePlace(Place place);


    @Query("DELETE FROM places WHERE _ID = :placeId")
    void deleteByPlaceId(int placeId);



}
