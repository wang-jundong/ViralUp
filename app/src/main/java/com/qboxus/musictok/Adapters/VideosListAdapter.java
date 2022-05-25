package com.qboxus.musictok.Adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.qboxus.musictok.Models.HomeModel;
import com.qboxus.musictok.R;
import com.qboxus.musictok.Interfaces.AdapterClickListener;
import com.qboxus.musictok.SimpleClasses.Functions;

import java.util.ArrayList;

/**
 * Created by qboxus on 3/20/2018.
 */

public class VideosListAdapter extends RecyclerView.Adapter<VideosListAdapter.CustomViewHolder> {
    public Context context;

    ArrayList<HomeModel> datalist;
    AdapterClickListener adapterClickListener;

    public VideosListAdapter(Context context, ArrayList<HomeModel> arrayList, AdapterClickListener adapterClickListener) {
        this.context = context;
        datalist = arrayList;
        this.adapterClickListener = adapterClickListener;
    }

    @Override
    public VideosListAdapter.CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_search_video_layout, viewGroup, false);
        view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
        VideosListAdapter.CustomViewHolder viewHolder = new VideosListAdapter.CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public int getItemCount() {
        return datalist.size();
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {

        ImageView image, userImage;

        TextView usernameTxt, descriptionTxt, firstLastNameTxt, likesCountTxt;


        public CustomViewHolder(View view) {
            super(view);
            userImage = view.findViewById(R.id.user_image);
            image = view.findViewById(R.id.image);
            usernameTxt = view.findViewById(R.id.username_txt);
            descriptionTxt = view.findViewById(R.id.description_txt);

            firstLastNameTxt = view.findViewById(R.id.first_last_name_txt);
            likesCountTxt = view.findViewById(R.id.likes_count_txt);
        }

        public void bind(final int pos, final HomeModel item, final AdapterClickListener listener) {

            itemView.setOnClickListener(v -> {
                listener.onItemClick(v, pos, item);

            });


        }


    }

    @Override
    public void onBindViewHolder(final VideosListAdapter.CustomViewHolder holder, final int i) {
        holder.setIsRecyclable(false);

        final HomeModel item = (HomeModel) datalist.get(i);

        holder.usernameTxt.setText(item.username);
        holder.descriptionTxt.setText(item.video_description);

        if (item.thum != null && !item.thum.equals("")) {
            Uri uri = Uri.parse(item.thum);
            holder.image.setImageURI(uri);
        }

        if (item.profile_pic != null && !item.profile_pic.equals("")) {
            Uri uri = Uri.parse(item.profile_pic);
            holder.userImage.setImageURI(uri);
        }

        holder.firstLastNameTxt.setText(item.first_name + " " + item.last_name);
        holder.likesCountTxt.setText(Functions.getSuffix(item.like_count));

        holder.bind(i, item, adapterClickListener);

    }

}