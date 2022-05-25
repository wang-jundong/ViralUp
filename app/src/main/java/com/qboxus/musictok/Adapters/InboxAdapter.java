package com.qboxus.musictok.Adapters;

import android.content.Context;
import android.graphics.Typeface;

import androidx.recyclerview.widget.RecyclerView;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.qboxus.musictok.Models.InboxModel;
import com.qboxus.musictok.R;
import com.qboxus.musictok.SimpleClasses.Functions;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by qboxus on 3/20/2018.
 */

public class InboxAdapter extends RecyclerView.Adapter<InboxAdapter.CustomViewHolder> implements Filterable {
    public Context context;
    ArrayList<InboxModel> inboxDataList = new ArrayList<>();
    ArrayList<InboxModel> inboxDataListFilter = new ArrayList<>();
    private InboxAdapter.OnItemClickListener listener;
    private InboxAdapter.OnLongItemClickListener longlistener;

    Integer today_day = 0;

    // meker the onitemclick listener interface and this interface is impliment in Chatinbox activity
    // for to do action when user click on item
    public interface OnItemClickListener {
        void onItemClick(InboxModel item);
    }

    public interface OnLongItemClickListener {
        void onLongItemClick(InboxModel item);
    }

    public InboxAdapter(Context context, ArrayList<InboxModel> user_dataList, InboxAdapter.OnItemClickListener listener, InboxAdapter.OnLongItemClickListener longlistener) {
        this.context = context;
        this.inboxDataList = user_dataList;
        this.inboxDataListFilter = user_dataList;
        this.listener = listener;
        this.longlistener = longlistener;

        // get the today as a integer number to make the dicision the chat date is today or yesterday
        Calendar cal = Calendar.getInstance();
        today_day = cal.get(Calendar.DAY_OF_MONTH);

    }

    @Override
    public InboxAdapter.CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_inbox_list, null);
        view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
        InboxAdapter.CustomViewHolder viewHolder = new InboxAdapter.CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public int getItemCount() {
        return inboxDataListFilter.size();
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {
        TextView username, lastMessage, dateCreated;
        SimpleDraweeView userImage;

        public CustomViewHolder(View view) {
            super(view);
            userImage = itemView.findViewById(R.id.user_image);
            username = itemView.findViewById(R.id.username);
            lastMessage = itemView.findViewById(R.id.message);
            dateCreated = itemView.findViewById(R.id.datetxt);
        }

        public void bind(final InboxModel item, final InboxAdapter.OnItemClickListener listener, final InboxAdapter.OnLongItemClickListener longItemClickListener) {

            itemView.setOnClickListener(v -> {
                listener.onItemClick(item);

            });


        }

    }


    @Override
    public void onBindViewHolder(final InboxAdapter.CustomViewHolder holder, final int i) {

        final InboxModel item = inboxDataListFilter.get(i);
        holder.username.setText(item.getName());
        holder.lastMessage.setText(item.getMsg());
        holder.dateCreated.setText(Functions.changeDateTodayYesterday(context, item.getDate()));

        if (item.getPic() != null && !item.getPic().equals("")) {

            holder.userImage.setController(Functions.frescoImageLoad(item.getPic(),holder.userImage,false));

        }

        String status = "" + item.getStatus();
        if (status.equals("0")) {
            holder.lastMessage.setTypeface(null, Typeface.BOLD);
            holder.lastMessage.setTextColor(context.getResources().getColor(R.color.black));
        } else {
            holder.lastMessage.setTypeface(null, Typeface.NORMAL);
            holder.lastMessage.setTextColor(context.getResources().getColor(R.color.darkgray));
        }


        holder.bind(item, listener, longlistener);

    }


    // that function will filter the result
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    inboxDataListFilter = inboxDataList;
                } else {
                    ArrayList<InboxModel> filteredList = new ArrayList<>();
                    for (InboxModel row : inboxDataList) {

                        if (row.getName().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row);
                        }
                    }

                    inboxDataListFilter = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = inboxDataListFilter;
                return filterResults;

            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                inboxDataListFilter = (ArrayList<InboxModel>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }


}