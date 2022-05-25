package com.qboxus.musictok.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.qboxus.musictok.Interfaces.AdapterClickListener;
import com.qboxus.musictok.Models.FollowingModel;
import com.qboxus.musictok.R;
import com.qboxus.musictok.SimpleClasses.Functions;

import java.util.ArrayList;

public class UserShareProfileAdapter extends RecyclerView.Adapter<UserShareProfileAdapter.CustomViewHolder> {


    ArrayList<FollowingModel> datalist;
    public AdapterClickListener listener;

    public UserShareProfileAdapter(ArrayList<FollowingModel> datalist, AdapterClickListener listener) {
        this.datalist = datalist;
        this.listener = listener;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_user_share_profile_list_view, viewGroup, false);
        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final CustomViewHolder holder, final int i) {

        FollowingModel item = datalist.get(i);

        holder.userName.setText(item.username);
        holder.fullName.setText(item.first_name + " " + item.last_name);

        if (item.profile_pic != null && !item.profile_pic.equals("")) {

            holder.userImage.setController(Functions.frescoImageLoad(item.profile_pic,holder.userImage,false));
        }
        if (item.is_select)
        {
            holder.ivSelection.setImageDrawable(ContextCompat.getDrawable(holder.itemView.getContext(),R.drawable.ic_selection));
        }
        else
        {
            holder.ivSelection.setImageDrawable(ContextCompat.getDrawable(holder.itemView.getContext(),R.drawable.ic_unselection));
        }


        holder.bind(i, datalist.get(i), listener);

    }


    @Override
    public int getItemCount() {
        return datalist.size();
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {

        SimpleDraweeView userImage;
        TextView userName;
        TextView fullName;
        ImageView ivSelection;
        RelativeLayout mainlayout;


        public CustomViewHolder(View view) {
            super(view);
            ivSelection =view.findViewById(R.id.ivSelection);
            userImage = view.findViewById(R.id.user_image);
            userName = view.findViewById(R.id.userName);
            fullName = view.findViewById(R.id.fullName);
            mainlayout=view.findViewById(R.id.mainlayout);
        }

        public void bind(final int pos, final FollowingModel item, final AdapterClickListener listener) {


            mainlayout.setOnClickListener(v -> {
                listener.onItemClick(v, pos, item);
            });

        }


    }





}