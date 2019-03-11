package com.app.xmemo.xmemo_image.bean;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Khalid Khan on 06,June,2017
 * Email khalid.khan@ratufa.com.
 */
public class Image {

    private String name;
    private String imageUrl;
    private String time;

    public Image() {
    }

    public Image(String name, String imageUrl) {
        this.name = name;
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

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
