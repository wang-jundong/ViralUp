package com.qboxus.musictok.ActivitesFragment.Chat.ViewHolders;

import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.qboxus.musictok.ActivitesFragment.Chat.ChatAdapter;
import com.qboxus.musictok.ActivitesFragment.Chat.ChatModel;
import com.qboxus.musictok.R;


/**
 * Created by qboxus on 1/10/2019.
 */

public class ChatVideoviewholder extends RecyclerView.ViewHolder {
   public ImageView chatimage;

    public TextView datetxt,msg_date;
    public ProgressBar p_bar;
    public ImageView not_send_message_icon;
    public View view;
    RelativeLayout chatimage_layout;

    public ChatVideoviewholder(View itemView) {
        super(itemView);
        view = itemView;

        msg_date=view.findViewById(R.id.msg_date);
        this.chatimage = view.findViewById(R.id.chatimage);
        this.datetxt=view.findViewById(R.id.datetxt);
        not_send_message_icon=view.findViewById(R.id.not_send_messsage);
        p_bar=view.findViewById(R.id.p_bar);
        chatimage_layout=view.findViewById(R.id.chatimage_layout);

    }

    public void bind(int pos, final ChatModel item, final ChatAdapter.OnItemClickListener listener) {

        chatimage_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(item,v,pos);
            }
        });


    }



}
