package com.qboxus.musictok.Models;

import java.io.Serializable;

public class InviteFriendModel implements Serializable {
    String name,phone,path;

    public InviteFriendModel() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
