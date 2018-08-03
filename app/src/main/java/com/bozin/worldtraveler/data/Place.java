package com.bozin.worldtraveler.data;


import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

@Entity(tableName = "cities")
public class Place {


    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    private int placeID;

    @ColumnInfo(name = "city")
    @NonNull
    private String city_name;

    @ColumnInfo(name= "country")
    @NonNull
    private String country_name;

    @ColumnInfo(name = "latitude")
    private double latitude;

    @ColumnInfo(name = "longitude")
    private double longitude;

    @ColumnInfo(name = "picture_uris")
    private String picture_uris;


    public Place(int placeID, @NonNull String city_name, @NonNull String country_name, double latitude, double longitude, String picture_uris) {
        this.placeID = placeID;
        this.city_name = city_name;
        this.country_name = country_name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.picture_uris = picture_uris;
    }

    @Ignore
    public Place(@NonNull String city_name, @NonNull String country_name, double latitude, double longitude, String picture_uris) {
        this.city_name = city_name;
        this.country_name = country_name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.picture_uris = picture_uris;
    }

    public int getPlaceID() {
        return placeID;
    }

    public void setPlaceID(int placeID) {
        this.placeID = placeID;
    }

    public String getCity_name() {
        return city_name;
    }

    public void setCity_name(String city_name) {
        this.city_name = city_name;
    }

    public String getCountry_name() {
        return country_name;
    }

    public void setCountry_name(String country_name) {
        this.country_name = country_name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getPicture_uris() {
        return picture_uris;
    }

    public void setPicture_uris(String picture_uris) {
        this.picture_uris = picture_uris;
    }

}
