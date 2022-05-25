package com.qboxus.musictok.ActivitesFragment.Chat.ViewHolders;

import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.qboxus.musictok.ActivitesFragment.Chat.ChatAdapter;
import com.qboxus.musictok.ActivitesFragment.Chat.ChatModel;
import com.qboxus.musictok.R;

public class Chataudioviewholder extends RecyclerView.ViewHolder {
    public TextView datetxt, message_seen;
    public ProgressBar pBar;
    public ImageView notSendMessageIcon;
    public ImageView playBtn;
    public SeekBar seekBar;
    public TextView totalTime;
    public LinearLayout audioBubble;


    View view;

    public Chataudioviewholder(View itemView) {
        super(itemView);
        view = itemView;
        audioBubble = view.findViewById(R.id.audio_bubble);
        datetxt = view.findViewById(R.id.datetxt);
        message_seen = view.findViewById(R.id.message_seen);
        notSendMessageIcon = view.findViewById(R.id.not_send_messsage);
        pBar = view.findViewById(R.id.p_bar);
        this.playBtn = view.findViewById(R.id.play_btn);
        this.seekBar = (SeekBar) view.findViewById(R.id.seek_bar);
        this.totalTime = (TextView) view.findViewById(R.id.total_time);

    }

    public void bind(final ChatModel item, int position, final ChatAdapter.OnItemClickListener listener, final ChatAdapter.OnLongClickListener long_listener) {


        audioBubble.setOnClickListener(v -> {
            listener.onItemClick(item, v, position);

        });

        audioBubble.setOnLongClickListener(v -> {
            long_listener.onLongclick(item, v);
            return false;

        });

        seekBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

    }


}
