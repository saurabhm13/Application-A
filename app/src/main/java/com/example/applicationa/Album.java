package com.example.applicationa;

public class Album {

    private String title;
    private String artist;

    public Album(String title, String artist) {
        this.title = title;
        this.artist = artist;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }
}
