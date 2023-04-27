package com.example.projectmobile1;

import com.google.android.gms.maps.model.LatLng;

public class MarkerData {
    private String id;
    private String title;
    private LatLng position;
    private double latitude;
    private double longitude;
    private String pixelGridId; // add pixelGridId field to reference the pixel grid in Firebase
    // other fields and methods
    private String code;

    public MarkerData(String id, String title, double latitude, double longitude, String pixelGrid, String code) {
        this.id = id;
        this.title = title;
        this.latitude = latitude;
        this.longitude = longitude;
        this.pixelGridId = pixelGrid;
        this.code = code;
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

    public String getPixelGridId() {
        return pixelGridId;
    }

    public void setPixelGridId(String pixelGridId) {
        this.pixelGridId = pixelGridId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LatLng getPosition() {
        return position;
    }

    public void setPosition(LatLng position) {
        this.position = position;
    }
}
