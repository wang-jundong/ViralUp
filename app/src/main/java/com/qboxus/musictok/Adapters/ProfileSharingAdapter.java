package com.qboxus.musictok.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.qboxus.musictok.Interfaces.AdapterClickListener;
import com.qboxus.musictok.Models.ShareAppModel;
import com.qboxus.musictok.R;
import java.util.ArrayList;


public class ProfileSharingAdapter extends RecyclerView.Adapter<ProfileSharingAdapter.CustomViewHolder> {

    public Context context;
    private AdapterClickListener listener;
    private ArrayList<ShareAppModel> dataList;



    public ProfileSharingAdapter(Context context, ArrayList<ShareAppModel> dataList, AdapterClickListener listener) {
        this.context = context;
        this.dataList = dataList;
        this.listener = listener;

    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_profilesharingapps_layout,viewGroup,false);
        return new CustomViewHolder(view);
    }


    @Override
    public int getItemCount() {
        return dataList.size();
    }


    @Override
    public void onBindViewHolder(final CustomViewHolder holder, final int i) {
        final ShareAppModel item = dataList.get(i);
        holder.setIsRecyclable(false);

        try {



            holder.name_txt.setText(item.getName());
            holder.image.setImageDrawable(ContextCompat.getDrawable(holder.itemView.getContext(),item.getIcon()));


        } catch (Exception e) {

        }

        holder.bind(i, item, listener);
    }


    public class CustomViewHolder extends RecyclerView.ViewHolder {

        TextView name_txt;
        ImageView image;


        public CustomViewHolder(View view) {
            super(view);

            name_txt=view.findViewById(R.id.name_txt);
            image = view.findViewById(R.id.image);
        }

        public void bind(final int postion, final ShareAppModel item, final AdapterClickListener listener) {

            itemView.setOnClickListener(v -> {
                listener.onItemClick(v,postion, item);

            });


        }


    }


}