package com.example.styledmap.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by igorb on 19.12.2017.
 */

public class PlacesDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "traveler.db";
    private static final int DATABASE_VERSION = 1;

    public PlacesDBHelper(Context c){
        super(c, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        final String sql_create_table = "CREATE TABLE " + PlacesContract.PlacesEntry.TABLE_NAME + " (" +
                PlacesContract.PlacesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                PlacesContract.PlacesEntry.COLUMN_CITY + " TEXT NOT NULL, " +
                PlacesContract.PlacesEntry.COLUMN_LATITUDE + " DOUBLE NOT NULL, " +
                PlacesContract.PlacesEntry.COLUMN_LONGITUDE + " DOUBLE NOT NULL, " +
                PlacesContract.PlacesEntry.COLUMN_PICTURE_URIS + " TEXT " +
                "); ";
        db.execSQL(sql_create_table);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + PlacesContract.PlacesEntry.TABLE_NAME);
        onCreate(db);
    }
}
