package com.example.projectmobile1;

public class MarkerData {

    private String title;
    private String uid;
    private String colors = "";
    private double latitude;
    private double longitude;
    private final int sizeOfTheSquare = 5;

    public MarkerData(String title, double latitude, double longitude, String uid) {
        this.title = title;
        this.latitude = latitude;
        this.longitude = longitude;
        this.uid = uid;

        for (int i = 0; i < sizeOfTheSquare * sizeOfTheSquare; i++) {
            colors += "w";
        }
    }

    public MarkerData() {
        // Required empty constructor for Firebase
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getColors() {
        return colors;
    }

    public void setColors(String colors) {
        this.colors = colors;
    }

    public int getSizeOfTheSquare() {
        return sizeOfTheSquare;
    }
}
