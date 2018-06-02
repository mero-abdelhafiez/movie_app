package com.example.android.popular_movie.Utilities;

import android.media.Rating;
import android.net.Uri;
import android.util.Log;

import com.example.android.popular_movie.DataModels.UserPreference;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class NetworkUtils {
    // Debug
    private static final String TAG = NetworkUtils.class.getSimpleName();

    // Authentication
    private static final String V3KEY = "c1a676d5a618b1591ae9746d3eb8562e";
    private static final String V4KEY = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJjMWE2NzZkNWE2MThiMT" +
            "U5MWFlOTc0NmQzZWI4NTYyZSIsInN1YiI6IjViMGEwYjA3YzNhMzY4NGE0YzAwMGFmNCIsInNjb3BlcyI6WyJhcGlfcmVhZ" +
            "CJdLCJ2ZXJzaW9uIjoxfQ.E496eEe5sFnhiHmd7cDIiVTRJ77-3ie8m7ASN_uYjQw";

    // Base URLS
    private static final String BASE_URL = "http://api.themoviedb.org/3/movie/";
    private static final String BASE_GENRE_URL = "https://api.themoviedb.org/3/genre/movie/list";
    private static final String IMAGE_BASE_URL = "http://image.tmdb.org/t/p/";

    // Paths
    private static final String POPULARITY = "popular";
    private static final String RATING = "top_rated";

    // Params
    private static final String KEY_PARAM = "api_key";
    private static final String SORT_PARAM = "sort_by";
    private static final String LANG_PARAM = "language";

    // Data
    private static final String[] AVAILABLE_IMAGE_SIZES = {"w92", "w154", "w185", "w342", "w500", "w780","original"};

    // Build the GET movie list query URL
    public static URL BuildQueryMovieURL (){
        String sortBy = "";
        if(UserPreference.getSortByRating()){
            sortBy = RATING;
        }else{
            sortBy = POPULARITY;
        }
        Uri uri = Uri.parse(BASE_URL).buildUpon()
                .appendPath(sortBy)
                .appendQueryParameter(KEY_PARAM , V3KEY)
                .build();
        URL url = null;
        try{
            url = new URL(uri.toString());

        }catch(MalformedURLException e){
            Log.e(TAG , e.getStackTrace().toString());
        }
        return url;
    }

    // Build GET genres query URL
    public static URL BuildQueryGenreURL (){
        Uri uri = Uri.parse(BASE_GENRE_URL).buildUpon()
                .appendQueryParameter(KEY_PARAM , V3KEY)
                .build();
        URL url = null;
        try{
            url = new URL(uri.toString());
        }catch (MalformedURLException e){

        }
        return url;
    }

    // Send Request to the MovieDB Api and receive JSON data
    public static String getQueryResult(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try{
            InputStream in = connection.getInputStream();
            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");
            boolean hasNext = scanner.hasNext();
            if(hasNext){
                return scanner.next();
            }else{
                return null;
            }
        } finally {
            connection.disconnect();
        }
    }

    // Takes the image relative path and return the full path
    public static String formImageFullPath(String relativePath , boolean isBackdrop){
        int pos = 2;
        if(isBackdrop){
            pos = 4;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(IMAGE_BASE_URL);
        stringBuilder.append("/" + AVAILABLE_IMAGE_SIZES[pos]).append("/" + relativePath);
        return stringBuilder.toString();
    }
}
