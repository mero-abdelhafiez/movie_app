package com.example.android.popular_movie.DataModels;

public class UserPreference {
    private static int SortType = 1;

    public static void setSortType(int sort){
        SortType = sort;
    }

    public static int getSortType(){
        return SortType;
    }
}
