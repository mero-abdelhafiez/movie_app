package com.example.android.popular_movie.Data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by Amira on 8/7/2018.
 */

public class MovieDBContentProvider extends ContentProvider {
    private MovieDBHelper mHelper;

    private static final int FAV_MOVIE = 100;
    private static final int FAV_MOVIE_WITH_ID = 101;


    private static UriMatcher sUriMatcher = buildUriMatcher();

    public static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(FavoriteListContract.AUTHORITY , FavoriteListContract.FAV_PATH , FAV_MOVIE);
        uriMatcher.addURI(FavoriteListContract.AUTHORITY , FavoriteListContract.FAV_PATH + "/#" , FAV_MOVIE_WITH_ID);
        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        mHelper = new MovieDBHelper(context);
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase db = mHelper.getReadableDatabase();

        int resultId = sUriMatcher.match(uri);
        Cursor cursor = null;
        switch (resultId){
            case FAV_MOVIE:
                cursor = db.query(FavoriteListContract.FavoriteListEntry.TABLE_NAME ,
                        projection ,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);

                break;
            case FAV_MOVIE_WITH_ID:
                break;
            default:
                throw new UnsupportedOperationException("Invalid uri is " +  uri.toString());
        }

        cursor.setNotificationUri(getContext().getContentResolver() , uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        SQLiteDatabase db = mHelper.getWritableDatabase();

        int resultId = sUriMatcher.match(uri);
        Uri retUri;
        switch (resultId){
            case FAV_MOVIE:
                long id = db.insert(FavoriteListContract.FavoriteListEntry.TABLE_NAME , null , values);
                if(id > 0){
                    retUri = ContentUris.withAppendedId(FavoriteListContract.FavoriteListEntry.CONTENT_URI , id);
                }else{
                    throw new android.database.SQLException("Invalid uri " + uri);
                }

                break;
            default:
                throw new UnsupportedOperationException("Uri " + uri);
        }
        getContext().getContentResolver().notifyChange(uri , null);
        return retUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = mHelper.getWritableDatabase();

        int resultId = sUriMatcher.match(uri);
        int deletedCount = 0;
        switch (resultId){
            case FAV_MOVIE:
                deletedCount = db.delete(FavoriteListContract.FavoriteListEntry.TABLE_NAME , selection , selectionArgs);
                break;
            case FAV_MOVIE_WITH_ID:
                String id = uri.getPathSegments().get(1);
                deletedCount = db.delete(FavoriteListContract.FavoriteListEntry.TABLE_NAME , "_id=?" , new String[]{id});
            default:
                throw new UnsupportedOperationException("Uri " + uri);
        }
        if(deletedCount > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return deletedCount;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = mHelper.getWritableDatabase();

        int resultId = sUriMatcher.match(uri);
        int updatedCount = 0;
        switch (resultId){
            case FAV_MOVIE_WITH_ID:
                updatedCount = db.update(FavoriteListContract.FavoriteListEntry.TABLE_NAME , values , selection , selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Uri " + uri);
        }
        getContext().getContentResolver().notifyChange(uri , null);
        return updatedCount;
    }
}