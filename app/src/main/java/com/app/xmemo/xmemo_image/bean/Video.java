package com.app.xmemo.xmemo_image.bean;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Khalid Khan on 08,June,2017
 * Email khalid.khan@ratufa.com.
 */
public class Video {

    private String name;
    private String fileUrl;
    private String imageUrl;
    private String time;

    public Video() {
    }

    public Video(String name, String fileUrl, String imageUrl) {
        this.name = name;
        this.fileUrl = fileUrl;
        this.imageUrl = imageUrl;
        DateFormat formatter = new SimpleDateFormat("MMM dd, hh:mm a");
        time = formatter.format(new Date(System.currentTimeMillis()));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}