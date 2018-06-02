package com.example.android.popular_movie.DataModels;

import java.util.HashMap;

public class GenresData {
    private static HashMap<Integer,String> Genres = new HashMap<Integer, String>();
    private static boolean IsEmpty = true;
    public static void addGenre(int id , String Name){
        if(!Genres.containsKey(id)){
            Genres.put(new Integer(id) , Name);
            if(IsEmpty){
                IsEmpty = !IsEmpty;
            }
        }
    }

    public static String getGenreName(int id){
        Integer idObj =  new Integer(id);
        if(Genres.containsKey(idObj)){
            return Genres.get(idObj);
        }
        return null;
    }

    public static boolean getIsEmpty(){
        return IsEmpty;
    }
}
