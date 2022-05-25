package com.qboxus.musictok.Models;

import java.util.ArrayList;

/**
 * Created by qboxus on 3/5/2019.
 */

public class CommentModel {
    public String video_id, fb_id, user_name, first_name, last_name, profile_pic, comments, created;
    public String comment_id;
    public String liked;
    public String like_count;
    public String item_count_replies;
    public ArrayList<CommentModel> arrayList;
    public boolean isExpand;

    public String comment_reply_id, comment_reply, reply_create_date, arraylist_size, replay_user_name, replay_user_url, parent_comment_id;
    public String comment_reply_liked, reply_liked_count;

}
