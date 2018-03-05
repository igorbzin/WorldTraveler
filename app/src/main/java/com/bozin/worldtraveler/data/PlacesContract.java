package com.bozin.worldtraveler.data;

import android.provider.BaseColumns;

/**
 * Created by igorb on 19.12.2017.
 */

public class PlacesContract  {

    public static final class PlacesEntry implements BaseColumns{
        public static final String TABLE_NAME = "places";
        public static final String COLUMN_CITY = "city";
        public static final String COLUMN_COUNTRY = "country";
        public static final String COLUMN_LATITUDE = "latitude";
        public static final String COLUMN_LONGITUDE = "longitude";
        public static final String COLUMN_PICTURE_URIS = "picture_uris";
    }

}
