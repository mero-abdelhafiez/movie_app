package com.example.android.popular_movie.DataModels;

public class Review {
    private String Author;
    private String Content;
    private String Id;
    private String URL;

    public void setId(String id) {
        Id = id;
    }

    public String getId() {
        return Id;
    }

    public void setAuthor(String author) {
        Author = author;
    }

    public String getAuthor() {
        return Author;
    }

    public void setContent(String content) {
        Content = content;
    }

    public String getContent() {
        return Content;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    public String getURL() {
        return URL;
    }
}
