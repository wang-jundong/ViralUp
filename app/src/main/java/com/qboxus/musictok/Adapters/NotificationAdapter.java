package com.qboxus.musictok.Adapters;

import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.qboxus.musictok.Models.NotificationModel;
import com.qboxus.musictok.R;
import com.facebook.drawee.view.SimpleDraweeView;
import com.qboxus.musictok.SimpleClasses.Functions;

import java.util.ArrayList;

/**
 * Created by qboxus on 3/20/2018.
 */

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.CustomViewHolder> {
    public Context context;

    ArrayList<NotificationModel> datalist;

    public interface OnItemClickListener {
        void onItemClick(View view, int postion, NotificationModel item);
    }

    public NotificationAdapter.OnItemClickListener listener;

    public NotificationAdapter(Context context, ArrayList<NotificationModel> arrayList, NotificationAdapter.OnItemClickListener listener) {
        this.context = context;
        datalist = arrayList;
        this.listener = listener;
    }

    @Override
    public NotificationAdapter.CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_notification, viewGroup, false);
        view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
        NotificationAdapter.CustomViewHolder viewHolder = new NotificationAdapter.CustomViewHolder(view);
        return viewHolder;
    }


    @Override
    public int getItemCount() {
        return datalist.size();
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {

        SimpleDraweeView userImage;
        TextView username, message, watchBtn;

        public CustomViewHolder(View view) {
            super(view);
            userImage = view.findViewById(R.id.user_image);
            username = view.findViewById(R.id.username);
            message = view.findViewById(R.id.message);
            watchBtn = view.findViewById(R.id.watch_btn);


        }

        public void bind(final int pos, final NotificationModel item, final NotificationAdapter.OnItemClickListener listener) {

            itemView.setOnClickListener(v -> {
                listener.onItemClick(v, pos, item);

            });

            watchBtn.setOnClickListener(v -> {
                listener.onItemClick(v, pos, item);


            });

        }


    }

    @Override
    public void onBindViewHolder(final NotificationAdapter.CustomViewHolder holder, final int i) {
        holder.setIsRecyclable(false);

        final NotificationModel item = datalist.get(i);
        holder.username.setText(item.username);

        if (item.profile_pic != null && !item.profile_pic.equals("")) {

            holder.userImage.setController(Functions.frescoImageLoad(item.profile_pic,holder.userImage,false));

        }

        holder.message.setText(item.string);

        if (item.type.equalsIgnoreCase("video_comment")) {
            holder.watchBtn.setVisibility(View.VISIBLE);
        } else if (item.type.equalsIgnoreCase("video_like")) {
            holder.watchBtn.setVisibility(View.VISIBLE);
        } else if (item.type.equalsIgnoreCase("following")) {
            holder.watchBtn.setVisibility(View.GONE);
        }


        holder.bind(i, datalist.get(i), listener);

    }


}