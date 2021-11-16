package com.piford.amessenger.models;

import java.io.Serializable;

public class User implements Serializable {
    private String name;
    private String phone;
    private String profilePic;

    public String getName () {
        return name;
    }

    public void setName (String name) {
        this.name = name;
    }

    public String getPhone () {
        return phone;
    }

    public void setPhone (String phone) {
        this.phone = phone;
    }

    public String getProfilePic () {
        return profilePic;
    }

    public void setProfilePic (String profilePic) {
        this.profilePic = profilePic;
    }
}
