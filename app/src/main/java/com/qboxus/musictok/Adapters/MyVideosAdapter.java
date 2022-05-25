package com.qboxus.musictok.Adapters;

import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.qboxus.musictok.Constants;
import com.qboxus.musictok.Models.HomeModel;
import com.qboxus.musictok.R;
import com.qboxus.musictok.Interfaces.AdapterClickListener;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.Variables;

import java.util.ArrayList;


/**
 * Created by qboxus on 3/20/2018.
 */

public class MyVideosAdapter extends RecyclerView.Adapter<MyVideosAdapter.CustomViewHolder> {

    public Context context;
    private ArrayList<HomeModel> dataList;
    AdapterClickListener adapterClickListener;

    public MyVideosAdapter(Context context, ArrayList<HomeModel> dataList, AdapterClickListener adapterClickListener) {
        this.context = context;
        this.dataList = dataList;
        this.adapterClickListener = adapterClickListener;

    }

    @Override
    public MyVideosAdapter.CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_myvideo_layout, null);
        view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
        MyVideosAdapter.CustomViewHolder viewHolder = new MyVideosAdapter.CustomViewHolder(view);
        return viewHolder;
    }


    @Override
    public int getItemCount() {
        return dataList.size();
    }


    class CustomViewHolder extends RecyclerView.ViewHolder {
        SimpleDraweeView thumbImage;
        TextView viewTxt;

        public CustomViewHolder(View view) {
            super(view);

            thumbImage = view.findViewById(R.id.thumb_image);
            viewTxt = view.findViewById(R.id.view_txt);

        }

        public void bind(final int position, final HomeModel item, final AdapterClickListener listener) {
            itemView.setOnClickListener(v -> {
                adapterClickListener.onItemClick(v, position, item);

            });

        }

    }


    @Override
    public void onBindViewHolder(final MyVideosAdapter.CustomViewHolder holder, final int i) {
        final HomeModel item = dataList.get(i);
        holder.setIsRecyclable(false);


        try {

            if (Constants.IS_SHOW_GIF) {

                holder.thumbImage.setController(Functions.frescoImageLoad(item.gif,holder.thumbImage,true));


            } else {
                if (item.thum != null && !item.thum.equals("")) {

                    holder.thumbImage.setController(Functions.frescoImageLoad(item.thum,holder.thumbImage,false));

                }
            }
        } catch (Exception e) {
            Functions.printLog(Constants.tag, e.toString());
        }


        holder.viewTxt.setText(item.views);
        holder.viewTxt.setText(Functions.getSuffix(item.views));


        holder.bind(i, item, adapterClickListener);

    }



}