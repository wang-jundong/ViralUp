package com.qboxus.musictok.Adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.hendraanggrian.appcompat.widget.SocialTextView;
import com.hendraanggrian.appcompat.widget.SocialView;
import com.qboxus.musictok.ApiClasses.ApiLinks;
import com.qboxus.musictok.Constants;
import com.qboxus.musictok.Models.CommentModel;
import com.qboxus.musictok.R;
import com.qboxus.musictok.SimpleClasses.Functions;

import java.util.ArrayList;

/**
 * Created by qboxus on 3/20/2018.
 */

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CustomViewHolder> {

    public Context context;
    public CommentsAdapter.OnItemClickListener listener;
    public CommentsAdapter.onRelyItemCLickListener onRelyItemCLickListener;
    LinkClickListener linkClickListener;
    private ArrayList<CommentModel> dataList;
    public Comments_Reply_Adapter commentsReplyAdapter;


    // meker the onitemclick listener interface and this interface is impliment in Chatinbox activity
    // for to do action when user click on item

    public interface LinkClickListener {

        void onLinkClicked(SocialView view, String matchedText);
    }


    public interface OnItemClickListener {
        void onItemClick(int positon, CommentModel item, View view);
    }

    public interface onRelyItemCLickListener {
        void onItemClick(ArrayList<CommentModel> arrayList, int postion, View view);
    }


    public CommentsAdapter(Context context, ArrayList<CommentModel> dataList, CommentsAdapter.OnItemClickListener listener, CommentsAdapter.onRelyItemCLickListener onRelyItemCLickListener, LinkClickListener linkClickListener) {
        this.context = context;
        this.dataList = dataList;
        this.listener = listener;
        this.linkClickListener = linkClickListener;
        this.onRelyItemCLickListener = onRelyItemCLickListener;

    }

    @Override
    public CommentsAdapter.CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_comment_layout, viewGroup,false);
        return new CommentsAdapter.CustomViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }


    @Override
    public void onBindViewHolder(final CommentsAdapter.CustomViewHolder holder, final int i) {
        final CommentModel item = dataList.get(i);

        holder.setIsRecyclable(false);
        holder.username.setText(item.user_name);


        if (item.profile_pic != null && !item.profile_pic.equals("")) {

            holder.userPic.setController(Functions.frescoImageLoad(item.profile_pic,holder.userPic,false));
        }

        if (item.liked != null && !item.equals("")) {
            if (item.liked.equals("1")) {
                holder.likeImage.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_like_fill));
            } else {
                holder.likeImage.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_heart_gray_out));
            }
        }

        holder.likeTxt.setText(Functions.getSuffix(item.like_count));

        holder.message.setText(item.comments);
        if (item.isExpand) {
            holder.lessLayout.setVisibility(View.VISIBLE);
            holder.replyCount.setVisibility(View.GONE);
        }

        if (item.arrayList != null && item.arrayList.size() > 0) {
            holder.replyCount.setText(context.getString(R.string.view_replies)+" (" + item.arrayList.size() + ")");
        } else {
            holder.replyCount.setVisibility(View.GONE);
        }


        holder.replyCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                item.isExpand = true;
                holder.lessLayout.setVisibility(View.VISIBLE);
                holder.replyCount.setVisibility(View.GONE);
            }
        });

        holder.showLessTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                item.isExpand = false;
                holder.lessLayout.setVisibility(View.GONE);
                holder.replyCount.setVisibility(View.VISIBLE);
            }
        });

        commentsReplyAdapter = new Comments_Reply_Adapter(context, item.arrayList);
        holder.replyRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        holder.replyRecyclerView.setAdapter(commentsReplyAdapter);
        holder.replyRecyclerView.setHasFixedSize(false);
        holder.bind(i, item, listener);

    }


    class CustomViewHolder extends RecyclerView.ViewHolder {

        TextView username, message, replyCount, likeTxt, showLessTxt;
        SimpleDraweeView userPic;
        ImageView likeImage;
        LinearLayout messageLayout, lessLayout, likeLayout;
        RecyclerView replyRecyclerView;

        public CustomViewHolder(View view) {
            super(view);

            username = view.findViewById(R.id.username);
            userPic = view.findViewById(R.id.user_pic);
            message = view.findViewById(R.id.message);
            replyCount = view.findViewById(R.id.reply_count);
            likeImage = view.findViewById(R.id.like_image);
            messageLayout = view.findViewById(R.id.message_layout);
            likeTxt = view.findViewById(R.id.like_txt);
            replyRecyclerView = view.findViewById(R.id.reply_recycler_view);
            lessLayout = view.findViewById(R.id.less_layout);
            showLessTxt = view.findViewById(R.id.show_less_txt);
            likeLayout = view.findViewById(R.id.like_layout);
        }

        public void bind(final int postion, final CommentModel item, final CommentsAdapter.OnItemClickListener listener) {

            itemView.setOnClickListener(v -> {
                listener.onItemClick(postion, item, v);
            });

            userPic.setOnClickListener(v -> {
                listener.onItemClick(postion, item, v);
            });

            messageLayout.setOnClickListener(v -> {
                listener.onItemClick(postion, item, v);
            });

            likeLayout.setOnClickListener(v -> {
                listener.onItemClick(postion, item, v);
            });

        }


    }


    public class Comments_Reply_Adapter extends RecyclerView.Adapter<Comments_Reply_Adapter.CustomViewHolder> {

        public Context context;
        private ArrayList<CommentModel> dataList;

        public Comments_Reply_Adapter(Context context, ArrayList<CommentModel> dataList) {
            this.context = context;
            this.dataList = dataList;

        }

        @Override
        public Comments_Reply_Adapter.CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_comment_reply_layout, null);
            view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
            Comments_Reply_Adapter.CustomViewHolder viewHolder = new Comments_Reply_Adapter.CustomViewHolder(view);
            return viewHolder;
        }

        @Override
        public int getItemCount() {
            return dataList.size();
        }


        @Override
        public void onBindViewHolder(final Comments_Reply_Adapter.CustomViewHolder holder, final int i) {
            final CommentModel item = dataList.get(i);
            holder.setIsRecyclable(false);
            holder.username.setText(item.replay_user_name);


            if (item.replay_user_url != null && !item.replay_user_url.equals("")) {

                holder.user_pic.setController(Functions.frescoImageLoad(Constants.BASE_URL + item.replay_user_url,holder.user_pic,false));

            }


            holder.message.setText(item.comment_reply);


            Functions.printLog("tictic_logged", "itemlike" + item.comment_reply_liked);
            if (item.comment_reply_liked != null && !item.comment_reply_liked.equals("")) {
                if (item.comment_reply_liked.equals("1")) {
                    holder.reply_like_image.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_like_fill));
                } else {
                    holder.reply_like_image.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_heart_gray_out));
                }
            }

            holder.like_txt.setText(Functions.getSuffix(item.reply_liked_count));

            holder.message.setOnMentionClickListener(new SocialView.OnClickListener() {
                @Override
                public void onClick(@NonNull SocialView view, @NonNull CharSequence text) {
                    linkClickListener.onLinkClicked(view, text.toString());
                }
            });


            holder.bind(i, dataList, onRelyItemCLickListener);

        }


        class CustomViewHolder extends RecyclerView.ViewHolder {

            TextView username, like_txt;
            SocialTextView message;
            SimpleDraweeView user_pic;
            ImageView reply_like_image;
            LinearLayout reply_layout, like_layout;

            public CustomViewHolder(View view) {
                super(view);

                username = view.findViewById(R.id.username);
                user_pic = view.findViewById(R.id.user_pic);
                message = view.findViewById(R.id.message);
                reply_layout = view.findViewById(R.id.reply_layout);
                reply_like_image = view.findViewById(R.id.reply_like_image);
                like_layout = view.findViewById(R.id.like_layout);
                like_txt = view.findViewById(R.id.like_txt);
            }

            public void bind(final int postion, ArrayList<CommentModel> datalist, final CommentsAdapter.onRelyItemCLickListener listener) {

                itemView.setOnClickListener(v -> {
                    CommentsAdapter.this.onRelyItemCLickListener.onItemClick(datalist, postion, v);
                });

                user_pic.setOnClickListener(v -> {
                    CommentsAdapter.this.onRelyItemCLickListener.onItemClick(datalist, postion, v);
                });

                reply_layout.setOnClickListener(v -> {
                    CommentsAdapter.this.onRelyItemCLickListener.onItemClick(datalist, postion, v);
                });

                like_layout.setOnClickListener(v -> {
                    CommentsAdapter.this.onRelyItemCLickListener.onItemClick(datalist, postion, v);
                });
            }
        }
    }


}