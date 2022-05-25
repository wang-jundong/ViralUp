package com.qboxus.musictok.Models;

import java.io.Serializable;

public class PushNotificationSettingModel implements Serializable {
    String likes, comments, newfollowers, mentions, directmessage, videoupdates;

    public PushNotificationSettingModel() {
    }

    public String getLikes() {
        return likes;
    }

    public void setLikes(String likes) {
        this.likes = likes;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getNewfollowers() {
        return newfollowers;
    }

    public void setNewfollowers(String newfollowers) {
        this.newfollowers = newfollowers;
    }

    public String getMentions() {
        return mentions;
    }

    public void setMentions(String mentions) {
        this.mentions = mentions;
    }

    public String getDirectmessage() {
        return directmessage;
    }

    public void setDirectmessage(String directmessage) {
        this.directmessage = directmessage;
    }

    public String getVideoupdates() {
        return videoupdates;
    }

    public void setVideoupdates(String videoupdates) {
        this.videoupdates = videoupdates;
    }
}