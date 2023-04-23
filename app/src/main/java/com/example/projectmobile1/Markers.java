package com.example.projectmobile1;

import android.net.Uri;

public class Markers {

    private String id;
    private String title;
    private String author;
    private String link;
    private double y;
    private double x;


    public Markers() {
    }

    public Markers(String id, String title, String author, String link, double y, double x) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.link = link;
        this.y = y;
        this.x = x;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getLink() {
        return link;
    }

    public double getY() {
        return y;
    }

    public double getX() {
        return x;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setX(double x) {
        this.x = x;
    }
}
