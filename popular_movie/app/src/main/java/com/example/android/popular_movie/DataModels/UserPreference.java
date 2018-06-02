package com.example.android.popular_movie.DataModels;

public class UserPreference {
    private static boolean SortByRating = false;

    public static void setSortByRating(boolean sort){
        SortByRating = sort;
    }

    public static boolean getSortByRating(){
        return SortByRating;
    }
}
