package com.qboxus.musictok.ActivitesFragment.LiveStreaming;

import java.io.Serializable;

/**
 * Created by qboxus on 3/5/2019.
 */

public class LiveCommentModel implements Serializable {

    public String key,userId, userName, userPicture, comment, commentTime,type;

    public LiveCommentModel() {
        key="";
        userId="";
        userName="";
        userPicture="";
        comment="";
        commentTime="";
        type="";
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
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

    public String getUserPicture() {
        return userPicture;
    }

    public void setUserPicture(String userPicture) {
        this.userPicture = userPicture;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCommentTime() {
        return commentTime;
    }

    public void setCommentTime(String commentTime) {
        this.commentTime = commentTime;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
