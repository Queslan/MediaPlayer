package com.example.mediaplayer;

public class Track {
    private String name;

    private String data;

    public Track(String data, String name) {
        this.data = data;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getData() {
        return data;
    }
    public void setData(String data) {
        this.data = data;
    }
}
