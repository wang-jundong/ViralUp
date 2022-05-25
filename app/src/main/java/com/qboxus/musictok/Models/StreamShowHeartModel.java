package com.qboxus.musictok.Models;

import java.io.Serializable;

public class StreamShowHeartModel implements Serializable {
    public String userId,otherUserId;

    public StreamShowHeartModel() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getOtherUserId() {
        return otherUserId;
    }

    public void setOtherUserId(String otherUserId) {
        this.otherUserId = otherUserId;
    }
}
