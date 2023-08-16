package com.example.projectmobile1;

public class Timer {
    private static long  startTime;

    public Timer() {
        this.startTime = 0;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
}
