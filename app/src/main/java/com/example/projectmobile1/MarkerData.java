package com.example.projectmobile1;

public class MarkerData {

    private String title, uid, colors="";
    private double latitude;
    private double longitude;
    private final int sizeOfTheSquare = 5;

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

    public MarkerData(String title, double latitude, double longitude, String uid) {
        this.title = title;
        this.latitude = latitude;
        this.longitude = longitude;
        this.uid = uid;

        for (int i = 0 ; i < sizeOfTheSquare*sizeOfTheSquare; i++)
        {
            colors += "b";
        }
    }

    public MarkerData() {
        // Required empty constructor for Firebase
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


}
