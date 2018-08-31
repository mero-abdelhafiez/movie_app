package com.example.android.popular_movie.Utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.android.popular_movie.DataModels.Genre;
import com.example.android.popular_movie.DataModels.GenresData;
import com.example.android.popular_movie.DataModels.Movie;
import com.example.android.popular_movie.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class JsonUtils {

    // Debug
    private static final String TAG = JsonUtils.class.getSimpleName();

    // Parsing
    private static final String Title = "title";
    private static final String Vote_Count = "vote_count";
    private static final String Rating = "vote_average";
    private static final String Image_Path ="poster_path";
    private static final String Backdrop_Path ="backdrop_path";
    private static final String Popularity ="popularity";
    private static final String Genres = "genre_ids";
    private static final String ID = "id";
    private static final String Video ="video";
    private static final String Origional_Title = "original_title";
    private static final String Adult = "adult";
    private static final String Overview ="overview";
    private static final String Release_Date = "release_date";
    private static final String Result = "results";

    private static final String GenreID = "id";
    private static final String GenreName = "name";
    private static final String GenresList = "genres";


    public static void parseGenreObject (String jsonData , Context context){
        if(jsonData != null) {
            try {
                JSONObject jsonGenres = new JSONObject(jsonData);
                JSONArray genresArray = jsonGenres.getJSONArray(GenresList);
                int length = genresArray.length();
                if (length > 0) {

                    SharedPreferences sharedPreferences = context.getSharedPreferences("GenresDataFile", 0);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    for (int i = 0; i < length; i++) {
                        JSONObject jsonGenre = genresArray.getJSONObject(i);
                        editor.putString(String.valueOf(jsonGenre.optInt(GenreID)), jsonGenre.optString(GenreName));
                    }
                    editor.apply();
                }
            } catch (JSONException e) {

            }
        }else {
            Log.d(TAG,"NullData in Genres");
        }
    }

    // Takes the fetched JSON and return a List of parsed Movie objects
    public static Movie[] parseWholeData(String queryResult) {
        Movie[] movies = null;
        if(queryResult != null) {
            try {
                JSONObject data = new JSONObject(queryResult);
                JSONArray result = data.getJSONArray(Result);
                int length = result.length();
                if (length > 0) {
                    movies = new Movie[length];
                    for (int i = 0; i < length; i++) {
                        Movie movie = parseMovieObject(result.optString(i));
                        movies[i] = movie;
                    }
                }
            } catch (JSONException e) {

            }
        }else{
            Log.d(TAG,"Null Movie Data");
        }

        return movies;
    }

    // Takes the Json Movie Str and return the parsed object
    public static Movie parseMovieObject(String jsonMovie){
        Movie parsedMovieObj = new Movie();
        try{
            JSONObject jsonMovieObj = new JSONObject(jsonMovie);
            parsedMovieObj.setTitle(jsonMovieObj.optString(Title));
            parsedMovieObj.setOrigionalName(jsonMovieObj.optString(Origional_Title));
            parsedMovieObj.setRating(jsonMovieObj.optDouble(Rating));

            parsedMovieObj.setAdult(jsonMovieObj.optBoolean(Adult));
            parsedMovieObj.setID(jsonMovieObj.optInt(ID));
            parsedMovieObj.setBackdropPath(jsonMovieObj.optString(Backdrop_Path));
            parsedMovieObj.setPoster_Path(jsonMovieObj.optString(Image_Path));
            parsedMovieObj.setHasVideoTrailer(jsonMovieObj.optBoolean(Video));
            parsedMovieObj.setPopularity(jsonMovieObj.optDouble(Popularity));
            parsedMovieObj.setRateCount(jsonMovieObj.optInt(Vote_Count));
            parsedMovieObj.setOverview(jsonMovieObj.optString(Overview));
            JSONArray jsonGenres = jsonMovieObj.getJSONArray(Genres);
            int length = jsonGenres.length();
            if(length > 0){
                int[] genres = new int[length];
                for(int i = 0 ; i < length ; i++){
                    genres[i] = jsonGenres.optInt(i);
                }
                parsedMovieObj.setGenres(genres);
            }
            String releaseDateStr = jsonMovieObj.optString(Release_Date);
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            Date date = format.parse(releaseDateStr);
            Log.d(TAG  , date.toString());
            parsedMovieObj.setReleaseDate(date);
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (JSONException e){
            e.printStackTrace();
        }
        return parsedMovieObj;
    }

}
