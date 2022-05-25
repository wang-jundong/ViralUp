package com.qboxus.musictok.ActivitesFragment.Chat;

import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;
import com.qboxus.musictok.Constants;
import com.qboxus.musictok.R;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.Variables;

import java.util.ArrayList;

/**
 * Created by qboxus on 3/20/2018.
 */

public class GifAdapter extends RecyclerView.Adapter<GifAdapter.CustomViewHolder> {
    public Context context;
    ArrayList<String> gifList;
    private GifAdapter.OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(String item);
    }

    public GifAdapter(Context context, ArrayList<String> datalist, GifAdapter.OnItemClickListener listener) {
        this.context = context;
        this.gifList = datalist;
        this.listener = listener;

    }

    @Override
    public GifAdapter.CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_gif_layout, null);
        GifAdapter.CustomViewHolder viewHolder = new GifAdapter.CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public int getItemCount() {
        return gifList.size();
    }


    class CustomViewHolder extends RecyclerView.ViewHolder {
        SimpleDraweeView gifImage;

        public CustomViewHolder(View view) {
            super(view);
            gifImage = view.findViewById(R.id.gif_image);
        }

        public void bind(final String item, final GifAdapter.OnItemClickListener listener) {

            itemView.setOnClickListener(v -> {
                    listener.onItemClick(item);

            });


        }

    }


    @Override
    public void onBindViewHolder(final GifAdapter.CustomViewHolder holder, final int i) {
        holder.bind(gifList.get(i), listener);


        // show the gif images by fresco
         String url=Variables.GIF_FIRSTPART + gifList.get(i) + Variables.GIF_SECONDPART;
        holder.gifImage.setController(Functions.frescoImageLoad(url,holder.gifImage,true));

        Functions.printLog(Constants.tag, Variables.GIF_FIRSTPART + gifList.get(i) + Variables.GIF_SECONDPART);
    }


}