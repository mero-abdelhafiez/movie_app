package com.example.android.popular_movie.DataModels;

import android.os.Parcel;
import android.os.Parcelable;
import android.widget.RatingBar;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class Movie implements Parcelable {
    private int ID;
    private boolean IsAdult;
    private boolean HasVideoTrailer;
    private String Title;
    private String Poster_Path;
    private double Rating = 0.0;
    private int VoteCount = 0;
    private double Popularity;
    private Date ReleaseDate;
    private String overview;
    private int[] genres;
    private String BackdropPath;
    private String OrigionalName;

    public Movie(){}
    public Movie(Parcel source) {
        ID = source.readInt();
        Title = source.readString();
        Popularity = source.readDouble();
        overview = source.readString();
        OrigionalName = source.readString();
        ReleaseDate = new Date(source.readLong());
        Rating = source.readDouble();
        VoteCount = source.readInt();
        Poster_Path = source.readString();
        BackdropPath = source.readString();
        genres = source.createIntArray();
        IsAdult = (source.readInt() == 1 ? true : false);
        HasVideoTrailer = (source.readInt() == 1 ? true : false);
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getTitle() {
        return Title;
    }

    public void setPopularity(double popularity) {
        Popularity = popularity;
    }

    public double getPopularity() {
        return Popularity;
    }

    public double getRating() {
        return Rating;
    }

    public void setRating(double rating) {
        Rating = rating;
    }

    public int getRateCount() {
        return VoteCount;
    }

    public void setRateCount(int rateCount) {
        VoteCount = rateCount;
    }

    public int getYear() {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(ReleaseDate);
        int year = calendar.get(Calendar.YEAR);
        return year;
    }

    public void setReleaseDate(Date date) {
        ReleaseDate = date;
    }

    public Date getReleaseDate(){
        return ReleaseDate;
    }
    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public int[] getGenres() {
        return genres;
    }

    public String getGenresString(){
        StringBuilder stringBuilder = new StringBuilder();
        for (int genre: genres)
        {
            stringBuilder.append(Integer.toString(genre) + ",");
        }
        return stringBuilder.toString();
    }

    public int[] getGenresFromString(String genresStr){
        String[] tokens = genresStr.split(",");
        int len = tokens.length;
        int[] genresIds = new int[len];
        for(int i = 0 ; i < len ; i++){
            genresIds[i] = Integer.parseInt(tokens[i]);
        }
        return genresIds;
    }

    public void setGenres(int[] genres) {
        this.genres = genres;
    }

    public String getPoster_Path() {
        return Poster_Path;
    }

    public void setPoster_Path(String poster_Path) {
        Poster_Path = poster_Path;
    }

    public String getOrigionalName() {
        return OrigionalName;
    }

    public void setOrigionalName(String origionalName) {
        OrigionalName = origionalName;
    }

    public String getBackdropPath() {
        return BackdropPath;
    }

    public void setBackdropPath(String backdropPath) {
        BackdropPath = backdropPath;
    }

    public void setAdult(boolean adult) {
        IsAdult = adult;
    }
    public boolean getAdult() {
        return IsAdult;
    }

    public void setHasVideoTrailer(boolean hasVideoTrailer) {
        HasVideoTrailer = hasVideoTrailer;
    }

    public boolean getHasVideoTrailer(){
        return HasVideoTrailer;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public int getID() {
        return ID;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(ID);
        dest.writeString(Title);
        dest.writeDouble(Popularity);
        dest.writeString(overview);
        dest.writeString(OrigionalName);
        dest.writeLong(ReleaseDate.getTime());
        dest.writeDouble(Rating);
        dest.writeInt(VoteCount);
        dest.writeString(Poster_Path);
        dest.writeString(BackdropPath);
        dest.writeIntArray(genres);
        dest.writeInt((Boolean) IsAdult ? 1 : 0 );
        dest.writeInt((Boolean) HasVideoTrailer ? 1 : 0 );
    }
    public static final Creator<Movie> CREATOR = new Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel source) {
            return new Movie(source);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };
}
