package com.qboxus.musictok.Models;

import java.io.Serializable;

public class ShareAppModel implements Serializable {
    String name;
    int icon;

    public ShareAppModel() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }
}
