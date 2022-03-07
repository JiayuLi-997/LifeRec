package com.java.lifelog_backend;

public class Trace {
    /**
     * 时间
     */
    private String time;
    /**
     * 事件
     */
    private String event;
    /**
     * 心情
     */
    private double[] mood;


    public Trace() {
    }

    public Trace(String time, String event, double moodx, double moody) {
        this.time = time;
        this.event = event;
        double array[] = {moodx, moody};
        this.mood = array;
    }

    public Trace(String time, String event, double[] mood) {
        this.time = time;
        this.event = event;
        this.mood = mood;
    }


    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public double[] getMood() {
        return mood;
    }

    public void setMood(double[] mood) {
        this.mood = mood;
    }
}
