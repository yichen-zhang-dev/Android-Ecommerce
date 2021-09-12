package com.example.ecommerce;

public class Movie {

    private String id;
    private String imageURL;
    private String title;
    private String titleType;
    private int year;

    public Movie() { }

    public Movie(String id, String imageURL, String title, String titleType, int year) {
        this.id = id;
        this.imageURL = imageURL;
        this.title = title;
        this.titleType = titleType;
        this.year = year;
    }

    public String getID() { return id; }

    public String getImageURL() { return imageURL; }

    public String getTitle() { return title; }

    public String getTitleType() { return titleType; }

    public int getYear() { return year; }
}
