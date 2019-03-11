package com.app.xmemo.xmemo_image.bean;

/**
 * Created by Khalid Khan on 08,June,2017
 * Email khalid.khan@ratufa.com.
 */
public class Folder {

    private String name;
    private String imageUrl;
    private String time;
    private long fileCount;
    private String key;

    public Folder(String name, String imageUrl, String time, long fileCount, String key) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.time = time;
        this.fileCount = fileCount;
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public long getFileCount() {
        return fileCount;
    }

    public void setFileCount(long fileCount) {
        this.fileCount = fileCount;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
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
}
