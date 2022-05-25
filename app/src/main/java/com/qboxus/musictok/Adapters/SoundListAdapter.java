package com.qboxus.musictok.Adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.qboxus.musictok.Constants;
import com.qboxus.musictok.R;
import com.qboxus.musictok.Interfaces.AdapterClickListener;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.Variables;
import com.qboxus.musictok.Models.SoundsModel;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;

/**
 * Created by qboxus on 3/19/2019.
 */


public class SoundListAdapter extends RecyclerView.Adapter<SoundListAdapter.CustomViewHolder> {
    public Context context;

    ArrayList<Object> datalist;
    AdapterClickListener adapterClickListener;

    public SoundListAdapter(Context context, ArrayList<Object> arrayList, AdapterClickListener listener) {
        this.context = context;
        datalist = arrayList;
        this.adapterClickListener = listener;
    }

    @Override
    public SoundListAdapter.CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_sound_layout, viewGroup, false);
        SoundListAdapter.CustomViewHolder viewHolder = new SoundListAdapter.CustomViewHolder(view);
        return viewHolder;
    }


    @Override
    public int getItemCount() {
        return datalist.size();
    }


    @Override
    public void onBindViewHolder(final SoundListAdapter.CustomViewHolder holder, final int i) {
        holder.setIsRecyclable(false);

        SoundsModel item = (SoundsModel) datalist.get(i);

        try {

            holder.soundName.setText(item.sound_name);
            holder.descriptionTxt.setText(item.description);
            holder.durationTimeTxt.setText(item.duration);

            if (item.thum != null && !item.thum.equals("")) {
                Functions.printLog(Constants.tag, item.thum);

                holder.soundImage.setController(Functions.frescoImageLoad(item.thum,holder.soundImage,false));

            }



            if (item.fav.equals("1"))
                holder.favBtn.setImageDrawable(context.getDrawable(R.drawable.ic_my_favourite));
            else
                holder.favBtn.setImageDrawable(context.getDrawable(R.drawable.ic_my_un_favourite));

            holder.bind(i, item, adapterClickListener);


        } catch (Exception e) {

        }

    }

    class CustomViewHolder extends RecyclerView.ViewHolder {

        ImageButton done, favBtn;
        TextView soundName, descriptionTxt, durationTimeTxt;
        SimpleDraweeView soundImage;

        public CustomViewHolder(View view) {
            super(view);
            done = view.findViewById(R.id.done);
            favBtn = view.findViewById(R.id.fav_btn);


            soundName = view.findViewById(R.id.sound_name);
            descriptionTxt = view.findViewById(R.id.description_txt);
            soundImage = view.findViewById(R.id.sound_image);

            durationTimeTxt = view.findViewById(R.id.duration_time_txt);
        }

        public void bind(final int pos, final SoundsModel item, final AdapterClickListener listener) {

            itemView.setOnClickListener(v -> {
                listener.onItemClick(v, pos, item);

            });

            done.setOnClickListener(v -> {
                listener.onItemClick(v, pos, item);

            });

            favBtn.setOnClickListener(v -> {
                listener.onItemClick(v, pos, item);

            });

        }


    }


}

