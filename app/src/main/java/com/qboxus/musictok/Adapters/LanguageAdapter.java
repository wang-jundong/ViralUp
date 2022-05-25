package com.qboxus.musictok.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.qboxus.musictok.Interfaces.AdapterClickListener;
import com.qboxus.musictok.Models.LanguageModel;
import com.qboxus.musictok.R;
import java.util.ArrayList;


public class LanguageAdapter extends RecyclerView.Adapter<LanguageAdapter.ViewHolder> {

    private ArrayList<LanguageModel> list;

    private AdapterClickListener listener;

    public LanguageAdapter(ArrayList<LanguageModel> list, AdapterClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.language_adapter_list_view, null);
        return new ViewHolder(v);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LanguageModel model = list.get(position);

        holder.tvListitem.setText(model.getName());
        if (model.isSelected()){
            holder.ivTick.setVisibility(View.VISIBLE);
        }else {
            holder.ivTick.setVisibility(View.GONE);
        }

        holder.bind(position, model, listener);
    }



    @Override
    public int getItemCount() {
        return list.size();
    }




    class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvListitem;
        ImageView ivTick;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            ivTick = itemView.findViewById(R.id.ivTick);
            tvListitem = itemView.findViewById(R.id.tvName);

        }


        public void bind(final int pos, final LanguageModel model,
                         final AdapterClickListener listener) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(v,pos,model);
                }

            });


        }

    }


    public void filter(ArrayList<LanguageModel> filter_list) {
        this.list=filter_list;
        notifyDataSetChanged();
    }
}