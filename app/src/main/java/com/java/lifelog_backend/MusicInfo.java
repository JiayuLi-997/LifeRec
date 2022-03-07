package com.java.lifelog_backend;

public class MusicInfo {
    public String musicMid;
    public String musicName;
    public String musicSinger;
    public String musicValence;
    public String musicURL;

    MusicInfo() {
    }

    MusicInfo(String mid, String name, String singer, String valence, String url) {
        musicMid = mid;
        musicName = name;
        musicSinger = singer;
        musicValence = valence;
        musicURL = url;
    }

    public void print() {
        System.out.println(musicMid + "," + musicName + "," + musicSinger + "," + musicValence + "," + musicURL);
    }
}
