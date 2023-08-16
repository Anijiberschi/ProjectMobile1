package com.example.projectmobile1;

import java.io.Serializable;

public class User implements Serializable {
    private String name, email, description;
    private MarkerData markers;
    private Timer timer;

    public User(){}

    public MarkerData getMarkers() {
        return markers;
    }

    public void setMarkers(MarkerData markers) {
        this.markers = markers;
    }


    public User(String name, String email, String description){
        this.name= name;
        this.email = email;
        this.description = description;
        this.timer=new Timer();
    }

    // getters and setters for the private fields
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Timer getTimer() {
        return timer;
    }

    public void setTimer(Timer timer) {
        this.timer = timer;
    }
}
