package com.qboxus.musictok.ActivitesFragment.Chat.ViewHolders;

import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.qboxus.musictok.ActivitesFragment.Chat.ChatAdapter;
import com.qboxus.musictok.ActivitesFragment.Chat.ChatModel;
import com.qboxus.musictok.R;

public class Chatimageviewholder extends RecyclerView.ViewHolder {
    public SimpleDraweeView chatimage;
    public TextView datetxt, message_seen;
    public ProgressBar pBar;
    public ImageView notSendMessageIcon;
    View view;

    public Chatimageviewholder(View itemView) {
        super(itemView);
        view = itemView;
        this.chatimage = view.findViewById(R.id.chatimage);
        this.datetxt = view.findViewById(R.id.datetxt);
        message_seen = view.findViewById(R.id.message_seen);
        notSendMessageIcon = view.findViewById(R.id.not_send_messsage);
        pBar = view.findViewById(R.id.p_bar);
    }

    public void bind(final ChatModel item, int position, final ChatAdapter.OnItemClickListener listener, final ChatAdapter.OnLongClickListener long_listener) {

        chatimage.setOnClickListener(v -> {
            listener.onItemClick(item, v, position);

        });

        chatimage.setOnLongClickListener(v -> {
            long_listener.onLongclick(item, v);
            return false;
        });
    }

}