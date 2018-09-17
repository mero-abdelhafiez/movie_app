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
    //TODO: Add Movie API Authentication Key here
    private static final String V3KEY = "c1a676d5a618b1591ae9746d3eb8562e";
    private static final String V4KEY = "";

    // Base URLS
    private static final String BASE_URL = "http://api.themoviedb.org/3/movie/";
    private static final String BASE_GENRE_URL = "https://api.themoviedb.org/3/genre/movie/list";
    private static final String BASE_TRAILER_URL = "http://api.themoviedb.org/3/movie/{id}/videos";
    private static final String BASE_REVIEWS_URL = "http://api.themoviedb.org/3/movie/{id}/reviews";
    private static final String IMAGE_BASE_URL = "http://image.tmdb.org/t/p/";

    private static final String YOUTUBE_BASE_URL = "https://www.youtube.com/watch";
    private static final String YOUTUBE_THUMBNAIL = "http://img.youtube.com/vi/{key}/0.jpg";

    // Paths
    private static final String POPULARITY = "popular";
    private static final String RATING = "top_rated";

    // Params
    private static final String KEY_PARAM = "api_key";
    private static final String SORT_PARAM = "sort_by";
    private static final String LANG_PARAM = "language";
    private static final String VIDEO_PARAM = "v";

    // Data
    private static final String[] AVAILABLE_IMAGE_SIZES = {"w92", "w154", "w185", "w342", "w500", "w780","original"};

    // Build the GET movie list query URL
    public static URL BuildQueryMovieURL (){
        String sortBy = "";
        if(UserPreference.getSortType() == 1){
            sortBy = POPULARITY;
        }else{
            sortBy = RATING;
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

    // Build GET trailers query URL
    public static URL BuildQueryTrailerUrl(String movieId){
        String baseUrlWithID = BASE_TRAILER_URL.replace("{id}" , movieId);
        Uri  uri = Uri.parse(baseUrlWithID).buildUpon()
                .appendQueryParameter(KEY_PARAM , V3KEY)
                .build();
        URL url = null;
        try{
            url = new URL(uri.toString());
        }catch(MalformedURLException e){

        }
        return url;
    }

    // Build GET reviews query URL
    public static URL BuildQueryReviewsUrl(String movieId){
        String baseUrlWithID = BASE_REVIEWS_URL.replace("{id}" , movieId);
        Uri  uri = Uri.parse(baseUrlWithID).buildUpon()
                .appendQueryParameter(KEY_PARAM , V3KEY)
                .build();
        URL url = null;
        try{
            url = new URL(uri.toString());
        }catch(MalformedURLException e){

        }
        return url;
    }

    public static Uri BuildYoutubeUrl(String videoId){
        Uri uri = Uri.parse(YOUTUBE_BASE_URL).buildUpon()
                .appendQueryParameter(VIDEO_PARAM , videoId)
                .build();
        return uri;
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

    public static String getVideoThumbnail(String key){
        return YOUTUBE_THUMBNAIL.replace("{key}" , key);
    }
}
