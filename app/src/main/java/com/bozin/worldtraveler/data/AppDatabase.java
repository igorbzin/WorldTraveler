package com.bozin.worldtraveler.data;


import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;
import android.util.Log;

import com.bozin.worldtraveler.model.Place;

@Database(entities = {Place.class}, version = 2 , exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static final String LOG_TAG = AppDatabase.class.getSimpleName();
    private static final String DATABASE_NAME = "traveler.db";
    private static AppDatabase sInstance;

    public static AppDatabase getInstance(Context context) {
        if (sInstance == null) {
            synchronized (AppDatabase.class) {
                Log.d(LOG_TAG, "Creating new database instance");
                sInstance = Room.databaseBuilder(context.getApplicationContext(),
                        AppDatabase.class, AppDatabase.DATABASE_NAME)
                        .addMigrations(MIGRATION_1_2)
                        .build();
            }
        }
        Log.d(LOG_TAG, "Getting the database instance");
        return sInstance;

    }

    /**
     * Migrate from:
     * version 1 - using the SQLiteOpenHelper\SQLiteDatabase * to
     * version 2 - using Room
     */
    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE `cities` (`_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `city` TEXT NOT NULL, `country` TEXT NOT NULL," +
                    " `latitude` REAL NOT NULL, `longitude` REAL NOT NULL, `picture_uris` TEXT);");
            database.execSQL("INSERT INTO cities SELECT * FROM places;");
        }
    };


    public abstract PlacesDao placesDao();
}
