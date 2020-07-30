package com.matts.EpicMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.mapbox.mapboxsdk.geometry.LatLng;

public class DBHelper extends SQLiteOpenHelper{

    public DBHelper(@Nullable Context context) {
        super(context, "tripHistory.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("Create table trip(uuid text, date text, transportMethod text, originPoint text, destinationPoint text)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("Drop table if exists trip");
    }

    public boolean tripInsert(String uuid, String datetime, String transportMethod, LatLng originPoint, LatLng destinationPoint) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("uuid", uuid);
        cv.put("date", datetime);
        cv.put("transportMethod", transportMethod);
        cv.put("originPoint", originPoint.getLatitude() + " " + originPoint.getLongitude());
        cv.put("destinationPoint", destinationPoint.getLatitude() + " " + destinationPoint.getLongitude());

        long insert = db.insert("trip", null, cv);
        if (insert==-1) return false;
        else
            return true;
    }

    public Cursor getTrips(String uuid) {

        // Gets the database reference
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("Select uuid, date, transportMethod, originPoint, destinationPoint from trip where uuid=? order by date desc", new String[] {uuid});
        return cursor;

    }

}
