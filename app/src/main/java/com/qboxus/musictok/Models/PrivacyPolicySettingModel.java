package com.qboxus.musictok.Models;

import java.io.Serializable;

public class PrivacyPolicySettingModel implements Serializable {
    String videos_download, direct_message, duet, liked_videos, video_comment;

    public PrivacyPolicySettingModel() {
    }

    public String getVideos_download() {
        return videos_download;
    }

    public void setVideos_download(String videos_download) {
        this.videos_download = videos_download;
    }

    public String getDirect_message() {
        return direct_message;
    }

    public void setDirect_message(String direct_message) {
        this.direct_message = direct_message;
    }

    public String getDuet() {
        return duet;
    }

    public void setDuet(String duet) {
        this.duet = duet;
    }

    public String getLiked_videos() {
        return liked_videos;
    }

    public void setLiked_videos(String liked_videos) {
        this.liked_videos = liked_videos;
    }

    public String getVideo_comment() {
        return video_comment;
    }

    public void setVideo_comment(String video_comment) {
        this.video_comment = video_comment;
    }
}

