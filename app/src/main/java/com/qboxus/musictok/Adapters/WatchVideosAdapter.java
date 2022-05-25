package com.qboxus.musictok.Adapters;

import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.hendraanggrian.appcompat.widget.SocialTextView;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.qboxus.musictok.Constants;
import com.qboxus.musictok.Models.HomeModel;
import com.qboxus.musictok.R;
import com.google.android.exoplayer2.ui.PlayerView;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.Variables;

import java.util.ArrayList;

/**
 * Created by qboxus on 3/20/2018.
 */

public class WatchVideosAdapter extends RecyclerView.Adapter<WatchVideosAdapter.CustomViewHolder> {

    public Context context;
    private WatchVideosAdapter.OnItemClickListener listener;
    private ArrayList<HomeModel> dataList;
    private WatchVideosAdapter.LikedClicked likedListener;

    // meker the onitemclick listener interface and this interface is impliment in Chatinbox activity
    // for to do action when user click on item


    public interface OnItemClickListener {
        void onItemClick(int positon, HomeModel item, View view);
    }

    public interface LikedClicked {
        void like_clicked(View view, HomeModel item, int position);
    }

    public WatchVideosAdapter(Context context, ArrayList<HomeModel> dataList, WatchVideosAdapter.OnItemClickListener listener, WatchVideosAdapter.LikedClicked likedListener) {
        this.context = context;
        this.dataList = dataList;
        this.listener = listener;
        this.likedListener = likedListener;

    }

    @Override
    public WatchVideosAdapter.CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_watch_layout, null);
        view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.MATCH_PARENT));
        WatchVideosAdapter.CustomViewHolder viewHolder = new WatchVideosAdapter.CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }


    @Override
    public void onBindViewHolder(final WatchVideosAdapter.CustomViewHolder holder, final int i) {
        final HomeModel item = dataList.get(i);
        try {

            holder.bind(i, item, listener);

            holder.username.setText(Functions.showUsername(item.username));
            if (item.profile_pic != null && !item.profile_pic.equals("")) {

                holder.userPic.setController(Functions.frescoImageLoad(item.profile_pic,holder.userPic,false));

            }


            holder.soundName.setSelected(true);
            holder.descTxt.setText("" + item.video_description);


            if (item.sound_id != null && ((item.sound_id.equals("") || item.sound_id.equals("0")))) {
                Functions.printLog(Constants.tag,context.getString(R.string.orignal_sound_)+" " + item.username);
                holder.soundName.setText(context.getString(R.string.orignal_sound_)+" " + item.username);
                if (item.profile_pic != null && !item.profile_pic.equals("")) {

                    holder.soundImage.setController(Functions.frescoImageLoad(item.profile_pic,holder.soundImage,false));

                }

            } else {
                Functions.printLog(Constants.tag,"Sound Name:"+item.sound_name);
                holder.soundName.setText(item.sound_name);
                if (item.sound_pic != null && !item.sound_pic.equals("")) {

                    holder.soundImage.setController(Functions.frescoImageLoad(item.sound_pic,holder.soundImage,false));

                }

            }


            if (item.liked.equals("1")) {
                holder.likeImage.setLikeDrawable(context.getResources().getDrawable(R.drawable.ic_heart_gradient));
                holder.likeImage.setLiked(true);
            } else {
                holder.likeImage.setLikeDrawable(context.getResources().getDrawable(R.drawable.ic_unliked));
                holder.likeImage.setLiked(false);
            }


            if (item.allow_comments != null && item.allow_comments.equalsIgnoreCase("false"))
                holder.commentLayout.setVisibility(View.GONE);
            else
                holder.commentLayout.setVisibility(View.VISIBLE);


            Functions.printLog(Constants.tag, item.like_count + "----" + item.video_comment_count);
            holder.likeTxt.setText(Functions.getSuffix(item.like_count));
            holder.commentTxt.setText(Functions.getSuffix(item.video_comment_count));


            if (item.verified != null && item.verified.equalsIgnoreCase("1")) {
                holder.varifiedBtn.setVisibility(View.VISIBLE);
            } else {
                holder.varifiedBtn.setVisibility(View.GONE);
            }

            if (item.duet_video_id != null && !item.duet_video_id.equals("") && !item.duet_video_id.equals("0")) {
                holder.duetLayoutUsername.setVisibility(View.VISIBLE);
                holder.duetUsername.setText(item.duet_username);
            }
            if (item.privacy_type.equals("private")) {
                holder.videoPrivacyType.setText(item.privacy_type);
                holder.privateVideoLayout.setVisibility(View.VISIBLE);
            }

            if (Functions.getSharedPreference(context).getBoolean(Variables.IS_LOGIN, false)) {
                holder.animateRlt.setVisibility(View.GONE);
                holder.likeImage.setOnLikeListener(new OnLikeListener() {
                    @Override
                    public void liked(LikeButton likeButton) {
                        likedListener.like_clicked(likeButton, dataList.get(i), i);
                    }

                    @Override
                    public void unLiked(LikeButton likeButton) {
                        likedListener.like_clicked(likeButton, dataList.get(i), i);
                    }
                });
            }


        } catch (Exception e) {
            Functions.printLog(Constants.tag, e.toString());
        }
    }


    class CustomViewHolder extends RecyclerView.ViewHolder {

        PlayerView playerview;
        TextView username, soundName, videoPrivacyType;
        SimpleDraweeView userPic, soundImage;
        ImageView varifiedBtn;
        RelativeLayout duetLayoutUsername, animateRlt;
        LinearLayout likeLayout, commentLayout, sharedLayout, soundImageLayout;
        LikeButton likeImage;
        ImageView commentImage;
        TextView likeTxt, commentTxt, duetUsername;
        SocialTextView descTxt;
        LinearLayout duetOpenVideo, privateVideoLayout;


        public CustomViewHolder(View view) {
            super(view);

            playerview = view.findViewById(R.id.playerview);
            duetOpenVideo = view.findViewById(R.id.duet_open_video);
            username = view.findViewById(R.id.username);

            userPic = view.findViewById(R.id.user_pic);
            soundName = view.findViewById(R.id.sound_name);
            soundImage = view.findViewById(R.id.sound_image);
            varifiedBtn = view.findViewById(R.id.varified_btn);
            duetUsername = view.findViewById(R.id.duet_username);
            likeLayout = view.findViewById(R.id.like_layout);
            likeImage = view.findViewById(R.id.likebtn);
            likeTxt = view.findViewById(R.id.like_txt);
            privateVideoLayout = view.findViewById(R.id.private_video_layout);
            animateRlt = view.findViewById(R.id.animate_rlt);
            duetLayoutUsername = view.findViewById(R.id.duet_layout_username);

            commentLayout = view.findViewById(R.id.comment_layout);
            commentImage = view.findViewById(R.id.comment_image);
            commentTxt = view.findViewById(R.id.comment_txt);

            descTxt = view.findViewById(R.id.desc_txt);
            videoPrivacyType = view.findViewById(R.id.video_privacy_type);

            soundImageLayout = view.findViewById(R.id.sound_image_layout);
            sharedLayout = view.findViewById(R.id.shared_layout);
        }

        public void bind(final int postion, final HomeModel item, final WatchVideosAdapter.OnItemClickListener listener) {

            itemView.setOnClickListener(v -> {
                listener.onItemClick(postion, item, v);

            });


            userPic.setOnClickListener(v -> {
                listener.onItemClick(postion, item, v);

            });

            duetOpenVideo.setOnClickListener(v -> {
                listener.onItemClick(postion, item, v);

            });

            username.setOnClickListener(v -> {
                listener.onItemClick(postion, item, v);

            });

            animateRlt.setOnClickListener(v -> {
                listener.onItemClick(postion, item, v);

            });


            commentLayout.setOnClickListener(v -> {
                listener.onItemClick(postion, item, v);

            });

            sharedLayout.setOnClickListener(v -> {
                listener.onItemClick(postion, item, v);

            });

            soundImageLayout.setOnClickListener(v -> {
                listener.onItemClick(postion, item, v);

            });




        }


    }


}