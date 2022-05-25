package com.qboxus.musictok.Models;

import java.io.Serializable;

public class UserOnlineModel implements Serializable {
    public String userId,userName,userPic;

    public UserOnlineModel() {
        userId="";
        userName="";
        userPic="";
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPic() {
        return userPic;
    }

    public void setUserPic(String userPic) {
        this.userPic = userPic;
    }
}
