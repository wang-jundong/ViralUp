package com.qboxus.musictok.Models;

import java.io.Serializable;

public class LanguageModel implements Serializable {
    String name,key;
    boolean isSelected;

    public LanguageModel() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
