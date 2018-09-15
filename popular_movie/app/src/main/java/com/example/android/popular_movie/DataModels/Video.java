package com.example.android.popular_movie.DataModels;

public class Video {
    private String Id;
    private String ISO639;
    private String ISO3166;
    private String Name;
    private String Key;
    private String Site;
    private int Size;
    private String Type;

    public void setName(String name) {
        Name = name;
    }

    public String getName() {
        return Name;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public int getSize() {
        return Size;
    }

    public void setSize(int size) {
        Size = size;
    }

    public String getISO639() {
        return ISO639;
    }

    public void setISO639(String ISO639) {
        this.ISO639 = ISO639;
    }

    public String getISO3166() {
        return ISO3166;
    }

    public void setISO3166(String ISO3166) {
        this.ISO3166 = ISO3166;
    }

    public String getKey() {
        return Key;
    }

    public void setKey(String key) {
        Key = key;
    }

    public String getType() {
        return Type;
    }

    public void setSite(String site) {
        Site = site;
    }

    public String getSite() {
        return Site;
    }

    public void setType(String type) {
        Type = type;
    }
}
