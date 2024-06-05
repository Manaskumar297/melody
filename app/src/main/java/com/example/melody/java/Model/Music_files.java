package com.example.melody.java.Model;

public class Music_files {
    private String title;
    private String artist;
    private String path;

    public Music_files(String title, String artist, String path) {
        this.title = title;
        this.artist = artist;
        this.path = path;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getPath() {
        return path;
    }
}

