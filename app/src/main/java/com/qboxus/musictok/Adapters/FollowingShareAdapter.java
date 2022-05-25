package com.qboxus.musictok.Adapters;

import android.content.Context;
import android.net.Uri;
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

/**
 * Created by qboxus on 3/20/2018.
 */

public class FollowingShareAdapter extends RecyclerView.Adapter<FollowingShareAdapter.CustomViewHolder > {
    public Context context;


    ArrayList<FollowingModel> datalist;

    AdapterClickListener adapter_clickListener;
    public FollowingShareAdapter(Context context , ArrayList<FollowingModel> arrayList, AdapterClickListener listener) {
        this.context = context;
        datalist= arrayList;
        this.adapter_clickListener=listener;
    }

    @Override
    public FollowingShareAdapter.CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_followers_share_layout,viewGroup,false);
         FollowingShareAdapter.CustomViewHolder viewHolder = new FollowingShareAdapter.CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public int getItemCount() {
        return datalist.size();
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {

        SimpleDraweeView user_image;
        TextView user_name;
        ImageView tick_icon;
        public CustomViewHolder(View view) {
            super(view);

            tick_icon=view.findViewById(R.id.tick_icon);
            user_image=view.findViewById(R.id.user_image);
            user_name=view.findViewById(R.id.user_name);

        }

        public void bind(final int pos , final FollowingModel item, final AdapterClickListener listener) {

            itemView.setOnClickListener(v->{
                listener.onItemClick(v,pos,item);
            });

        }


    }

    @Override
    public void onBindViewHolder(final FollowingShareAdapter.CustomViewHolder holder, final int i) {
        holder.setIsRecyclable(false);

        FollowingModel item=datalist.get(i);

        holder.user_name.setText(item.username);

        if(item.profile_pic!=null && !item.profile_pic.equals("")) {

            holder.user_image.setController(Functions.frescoImageLoad(item.profile_pic,holder.user_image,false));

        }

        if(item.is_select){
            holder.tick_icon.setVisibility(View.VISIBLE);
            holder.user_image.setAlpha((float) 0.5);
        }

        else {
            holder.tick_icon.setVisibility(View.GONE);
            holder.user_image.setAlpha((float) 1.0);
        }

        holder.bind(i,datalist.get(i),adapter_clickListener);

    }

}