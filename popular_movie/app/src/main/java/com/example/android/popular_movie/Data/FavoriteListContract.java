package com.example.android.popular_movie.Data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Amira on 8/6/2018.
 */

public class FavoriteListContract {
    public static final String AUTHORITY = "com.example.android.popular_movie";
    public static final String FAV_PATH = "fav";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    public static final class FavoriteListEntry implements BaseColumns
    {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(FAV_PATH).build();

        /// Table Name
        public static final String TABLE_NAME = "fav_movies";

        // Table columns
        public static final String MOVIE_TITLE_COL = "title";
        public static final String VOTE_CNT_COL = "vote_count";
        public static final String RATING_COL = "vote_average";
        public static final String IMAGE_COL ="poster_path";
        public static final String BACKDROP_COL ="backdrop_path";
        public static final String POP_COL ="popularity";
        public static final String GENRES_COL = "genre_ids";
        public static final String ID = "id";
        public static final String VIDEO_COL ="video";
        public static final String Origional_Title = "original_title";
        public static final String Adult = "adult";
        public static final String Overview ="overview";
        public static final String Release_Date = "release_date";
        public static final String Result = "results";

    }
}
