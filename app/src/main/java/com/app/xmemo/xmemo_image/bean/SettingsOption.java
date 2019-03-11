package com.app.xmemo.xmemo_image.bean;

/**
 * Created by Khalid Khan on 12,June,2017
 * Email khalid.khan@ratufa.com.
 */
public class SettingsOption {

    private int image;
    private String name;

    public SettingsOption() {
    }

    public SettingsOption(int image, String name) {
        this.image = image;
        this.name = name;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
