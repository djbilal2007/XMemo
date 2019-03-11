package com.app.xmemo.xmemo_image.bean;

import java.io.Serializable;

/**
 * Created by Khalid Khan on 16,June,2017
 * Email khalid.khan@ratufa.com.
 */
public class Contact implements Serializable {

    private String imageUrl;
    private String fullName;
    private String email;
    private String phoneNumber;
    private boolean isSelected = false;
    private String imageName;
    private String key;

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public Contact() {
    }

    public Contact(String imageUrl, String imageName, String fullName, String email, String phoneNumber) {
        this.imageUrl = imageUrl;
        this.imageName = imageName;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
