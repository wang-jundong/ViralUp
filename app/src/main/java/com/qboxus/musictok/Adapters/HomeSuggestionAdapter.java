package com.qboxus.musictok.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.qboxus.musictok.Interfaces.AdapterClickListener;
import com.qboxus.musictok.Models.FollowingModel;
import com.qboxus.musictok.R;
import com.qboxus.musictok.SimpleClasses.Functions;

import java.util.ArrayList;

public class HomeSuggestionAdapter extends RecyclerView.Adapter<HomeSuggestionAdapter.CustomViewHolder> {

    ArrayList<FollowingModel> datalist;
    public AdapterClickListener listener;

    public HomeSuggestionAdapter( ArrayList<FollowingModel> arrayList, AdapterClickListener listener) {
        datalist = arrayList;
        this.listener = listener;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_home_suggestion_follower, viewGroup, false);
        return new CustomViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return datalist.size();
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {

        SimpleDraweeView userImage,userGif;
        TextView tvName,tvUserName,tvFollowBtn;
        ImageView ivCross;

        public CustomViewHolder(View view) {
            super(view);
            ivCross=view.findViewById(R.id.ivCross);
            userImage = view.findViewById(R.id.user_image);
            tvName = view.findViewById(R.id.tvName);
            tvUserName = view.findViewById(R.id.tvUserName);
            tvFollowBtn = view.findViewById(R.id.tvFollowBtn);
            userGif=view.findViewById(R.id.userGif);
        }

        public void bind(final int pos, final FollowingModel item, final AdapterClickListener listener) {


            tvFollowBtn.setOnClickListener(v -> {
                listener.onItemClick(v, pos, item);

            });

            userImage.setOnClickListener(v -> {
                listener.onItemClick(v, pos, item);

            });

            ivCross.setOnClickListener(v -> {
                listener.onItemClick(v, pos, item);

            });

        }


    }

    @Override
    public void onBindViewHolder(final CustomViewHolder holder, final int i) {
        holder.setIsRecyclable(false);

        FollowingModel item = datalist.get(i);

        holder.tvUserName.setText(item.username);
        holder.tvName.setText(item.first_name+" "+item.last_name);

        if (item.profile_pic != null && !item.profile_pic.equals("")) {

            holder.userImage.setController(Functions.frescoImageLoad(item.profile_pic,holder.userImage,false));

        }

        if (!(item.gifLink.isEmpty()))
        {
            holder.userGif.setController(Functions.frescoImageLoad(item.gifLink,holder.userGif,true));
        }
        else
        {
            holder.userGif.setController(Functions.frescoImageLoad(item.gifLink,holder.userGif,false));
        }

        holder.bind(i, datalist.get(i), listener);

    }

}