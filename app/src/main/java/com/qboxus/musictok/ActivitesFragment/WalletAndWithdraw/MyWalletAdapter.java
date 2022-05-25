package com.qboxus.musictok.ActivitesFragment.WalletAndWithdraw;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.qboxus.musictok.Interfaces.AdapterClickListener;
import com.qboxus.musictok.R;

import java.util.ArrayList;

public class MyWalletAdapter extends RecyclerView.Adapter<MyWalletAdapter.ViewHolder> {

    Context context ;
    ArrayList<WalletModel> wallet_modelArrayList = new ArrayList<>();
    AdapterClickListener adapter_click_listener;

    public MyWalletAdapter(Context context, ArrayList<WalletModel> wallet_modelArrayList, AdapterClickListener adapter_click_listener) {
        this.context = context;
        this.wallet_modelArrayList = wallet_modelArrayList;
        this.adapter_click_listener=adapter_click_listener;
    }

    @NonNull
    @Override
    public MyWalletAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_wallet_list,null);
        view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
        MyWalletAdapter.ViewHolder viewHolder = new MyWalletAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyWalletAdapter.ViewHolder holder, int position) {

        final WalletModel item=wallet_modelArrayList.get(position);
        holder.coins.setText(item.getCoins()+" "+context.getString(R.string.coins));
        holder.price.setText(item.getPrice());

        if(!item.getImage().equals("")) {
            Uri uri = Uri.parse(item.getImage());
            holder.image.setImageURI(uri);
        }

        holder.bind(position,item,adapter_click_listener);
    }

    @Override
    public int getItemCount() {
        return wallet_modelArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView image ;
        TextView coins , price ;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.iv_profile);
            coins = itemView.findViewById(R.id.tv_coins);
            price = itemView.findViewById(R.id.tv_price);
        }

        public void bind(final int postion, final WalletModel item, final AdapterClickListener listener) {

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(v,postion,item);
                }
            });

        }
    }
}
