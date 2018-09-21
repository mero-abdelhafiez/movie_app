package com.example.android.popular_movie.Data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Amira on 8/6/2018.
 */

public class MovieDBHelper extends SQLiteOpenHelper {

    public static String DBName = "movies.db";

    public static int DBVersion = 2;

    public MovieDBHelper(Context context){
        super(context , DBName , null , DBVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String CREATE_TABLE_MOVIES = "CREATE TABLE " + FavoriteListContract.FavoriteListEntry.TABLE_NAME
                +  " ( " + FavoriteListContract.FavoriteListEntry.ID + " INTEGER PRIMARY KEY AUTOINCREMENT , "
                + FavoriteListContract.FavoriteListEntry.MOVIE_TITLE_COL + " TEXT NOT NULL , "
                + FavoriteListContract.FavoriteListEntry.Overview + " TEXT NOT NULL , "
                + FavoriteListContract.FavoriteListEntry.Adult + " BIT , "
                + FavoriteListContract.FavoriteListEntry.BACKDROP_COL + " TEXT , "
                + FavoriteListContract.FavoriteListEntry.GENRES_COL + " TEXT , "
                + FavoriteListContract.FavoriteListEntry.IMAGE_COL + " TEXT , "
                + FavoriteListContract.FavoriteListEntry.Origional_Title + " TEXT , "
                + FavoriteListContract.FavoriteListEntry.POP_COL + " REAL , "
                + FavoriteListContract.FavoriteListEntry.RATING_COL + " REAL , "
                + FavoriteListContract.FavoriteListEntry.Release_Date + " TEXT , "
                + FavoriteListContract.FavoriteListEntry.VOTE_CNT_COL + " INTEGER , "
                + FavoriteListContract.FavoriteListEntry.VIDEO_COL + " TEXT " + " )";
        db.execSQL(CREATE_TABLE_MOVIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Can't think of any table I want to add , the new_column is just for testing
        final String ALERT_TABLE_MOVIES = "ALTER TABLE "
                + FavoriteListContract.FavoriteListEntry.TABLE_NAME + " ADD COLUMN new_column string;";

        if(oldVersion < 2){
            db.execSQL(ALERT_TABLE_MOVIES);
        }
    }
}
