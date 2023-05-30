package com.example.projectmobile1;

import com.google.android.gms.maps.model.LatLng;

public class MarkerData {

    private String title;
    private String UserId;
    private double latitude;
    private double longitude;

    public MarkerData(String title, double latitude, double longitude, String UserId) {
        this.title = title;
        this.latitude = latitude;
        this.longitude = longitude;
        this.UserId = UserId;
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
