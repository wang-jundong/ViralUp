package com.qboxus.musictok.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.qboxus.musictok.Interfaces.AdapterClickListener;
import com.qboxus.musictok.R;

import java.util.ArrayList;

/**
 * Created by qboxus on 3/19/2019.
 */


public class RecentSearchAdapter extends RecyclerView.Adapter<RecentSearchAdapter.CustomViewHolder> implements Filterable {
    public Context context;

    ArrayList<String> datalist;
    ArrayList<String> datalistFilter;
    AdapterClickListener adapterClickListener;

    public RecentSearchAdapter(Context context, ArrayList<String> arrayList, AdapterClickListener adapterClickListener) {
        this.context = context;
        datalist = arrayList;
        datalistFilter = arrayList;
        this.adapterClickListener = adapterClickListener;
    }

    @Override
    public RecentSearchAdapter.CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_recent_search_list, viewGroup, false);
        RecentSearchAdapter.CustomViewHolder viewHolder = new RecentSearchAdapter.CustomViewHolder(view);
        return viewHolder;
    }


    @Override
    public int getItemCount() {
        return datalistFilter.size();
    }


    @Override
    public void onBindViewHolder(final RecentSearchAdapter.CustomViewHolder holder, final int i) {
        holder.setIsRecyclable(false);
        holder.nameTxt.setText(datalistFilter.get(i));

        holder.bind(i, datalistFilter.get(i), adapterClickListener);

    }

    class CustomViewHolder extends RecyclerView.ViewHolder {

        TextView nameTxt;
        ImageButton deleteBtn;

        public CustomViewHolder(View view) {
            super(view);

            nameTxt = view.findViewById(R.id.name_txt);

            deleteBtn = view.findViewById(R.id.delete_btn);
        }

        public void bind(final int pos, final Object item, final AdapterClickListener listener) {

            itemView.setOnClickListener(v -> {

                listener.onItemClick(v, pos, item);

            });

            deleteBtn.setOnClickListener(v -> {
                listener.onItemClick(v, pos, item);
            });
        }


    }


    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    datalistFilter = datalist;
                } else {
                    ArrayList<String> filteredList = new ArrayList<>();
                    for (String row : datalist) {
                        if (row.contains(charString.toLowerCase())) {
                            filteredList.add(row);
                        }
                    }

                    datalistFilter = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = datalistFilter;
                return filterResults;

            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                datalistFilter = (ArrayList<String>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

}

