package com.qboxus.musictok.ActivitesFragment.Chat;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.qboxus.musictok.ActivitesFragment.Chat.ViewHolders.Alertviewholder;
import com.qboxus.musictok.ActivitesFragment.Chat.ViewHolders.ChatShareProfileViewholder;
import com.qboxus.musictok.ActivitesFragment.Chat.ViewHolders.ChatVideoviewholder;
import com.qboxus.musictok.ActivitesFragment.Chat.ViewHolders.Chataudioviewholder;
import com.qboxus.musictok.ActivitesFragment.Chat.ViewHolders.Chatimageviewholder;
import com.qboxus.musictok.ActivitesFragment.Chat.ViewHolders.Chatviewholder;
import com.qboxus.musictok.R;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.Variables;

import org.json.JSONObject;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


/**
 * Created by qboxus on 4/3/2018.
 */

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<ChatModel> mDataSet;
    String myID;
    private static final int MYCHAT = 1;
    private static final int FRIENDCHAT = 2;

    private static final int MYCHATIMAGE = 3;
    private static final int OTHERCHATIMAGE = 4;

    private static final int MYGIFIMAGE = 5;

    private static final int OTHERGIFIMAGE = 6;
    private static final int ALERT_MESSAGE = 7;

    private static final int MY_AUDIO_MESSAGE = 8;
    private static final int OTHER_AUDIO_MESSAGE = 9;

    private static final int my_video_message = 10;
    private static final int other_video_message = 11;

    private static final int my_profile_share = 12;
    private static final int other_profile_share = 13;

    Context context;
    Integer todayDay = 0;

    private ChatAdapter.OnItemClickListener listener;
    private ChatAdapter.OnLongClickListener long_listener;

    public interface OnItemClickListener {
        void onItemClick(ChatModel item, View view, int postion);
    }


    public interface OnLongClickListener {
        void onLongclick(ChatModel item, View view);
    }


    ChatAdapter(List<ChatModel> dataSet, String id, Context context, ChatAdapter.OnItemClickListener listener, ChatAdapter.OnLongClickListener long_listener) {
        mDataSet = dataSet;
        this.myID = id;
        this.context = context;
        this.listener = listener;
        this.long_listener = long_listener;
        Calendar cal = Calendar.getInstance();
        todayDay = cal.get(Calendar.DAY_OF_MONTH);

    }


    // this is the all types of view that is used in the chat
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
        View v = null;
        switch (viewtype) {
            case MYCHAT:
                v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_chat_my, viewGroup, false);
                Chatviewholder mychatHolder = new Chatviewholder(v);
                return mychatHolder;
            case FRIENDCHAT:
                v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_chat_other, viewGroup, false);
                Chatviewholder friendchatHolder = new Chatviewholder(v);
                return friendchatHolder;
            case MYCHATIMAGE:
                v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_chat_image_my, viewGroup, false);
                Chatimageviewholder mychatimageHolder = new Chatimageviewholder(v);
                return mychatimageHolder;
            case OTHERCHATIMAGE:
                v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_chat_image_other, viewGroup, false);
                Chatimageviewholder otherchatimageHolder = new Chatimageviewholder(v);
                return otherchatimageHolder;

            case MY_AUDIO_MESSAGE:
                v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_chat_audio_my, viewGroup, false);
                Chataudioviewholder chataudioviewholder = new Chataudioviewholder(v);
                return chataudioviewholder;

            case OTHER_AUDIO_MESSAGE:
                v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_chat_audio_other, viewGroup, false);
                Chataudioviewholder other = new Chataudioviewholder(v);
                return other;


            case my_video_message:
                v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_chat_video_my, viewGroup, false);
                return new ChatVideoviewholder(v);

            case other_video_message:
                v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_chat_video_other, viewGroup, false);
                return new ChatVideoviewholder(v);

            case MYGIFIMAGE:
                v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_chat_gif_my, viewGroup, false);
                Chatimageviewholder mychatgigHolder = new Chatimageviewholder(v);
                return mychatgigHolder;
            case OTHERGIFIMAGE:
                v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_chat_gif_other, viewGroup, false);
                Chatimageviewholder otherchatgifHolder = new Chatimageviewholder(v);
                return otherchatgifHolder;
            case ALERT_MESSAGE:
                v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_chat_alert, viewGroup, false);
                Alertviewholder alertviewholder = new Alertviewholder(v);
                return alertviewholder;
            case my_profile_share:
                v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_share_profile_my, viewGroup, false);
                ChatShareProfileViewholder myShareProfileHolder = new ChatShareProfileViewholder(v);
                return myShareProfileHolder;
            case other_profile_share:
                v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_share_profile_other, viewGroup, false);
                ChatShareProfileViewholder otherShareProfileHolder = new ChatShareProfileViewholder(v);
                return otherShareProfileHolder;
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ChatModel chat = mDataSet.get(position);

        if (chat.getType().equals("text")) {
            Chatviewholder chatviewholder = (Chatviewholder) holder;
            // check if the message is from sender or receiver
            if (chat.getSender_id().equals(myID)) {
                if (chat.getStatus().equals("1"))
                    chatviewholder.messageSeen.setText(context.getString(R.string.seen_at)+" " + changeDateTime(chat.getTime()));
                else
                    chatviewholder.messageSeen.setText(context.getString(R.string.sent));

            } else {
                chatviewholder.messageSeen.setText("");
            }
            // make the group of message by date set the gap of 1 min
            // means message send with in 1 min will show as a group
            if (position != 0) {
                ChatModel chat2 = mDataSet.get(position - 1);
                if (chat2.getTimestamp().substring(14, 16).equals(chat.getTimestamp().substring(14, 16))) {
                    chatviewholder.datetxt.setVisibility(View.GONE);
                } else {
                    chatviewholder.datetxt.setVisibility(View.VISIBLE);
                    chatviewholder.datetxt.setText(changeDate(chat.getTimestamp()));
                }
                chatviewholder.message.setText(chat.getText());
            } else {
                chatviewholder.datetxt.setVisibility(View.VISIBLE);
                chatviewholder.datetxt.setText(changeDate(chat.getTimestamp()));
                chatviewholder.message.setText(chat.getText());
            }

            chatviewholder.bind(chat, long_listener);

        }
        else
        if (chat.getType().equals("profileShare")) {
            ChatShareProfileViewholder shareProfileviewholder = (ChatShareProfileViewholder) holder;
            // check if the message is from sender or receiver
            if (chat.getSender_id().equals(myID)) {
                if (chat.getStatus().equals("1"))
                    shareProfileviewholder.messageSeen.setText(context.getString(R.string.seen_at)+" " + changeDateTime(chat.getTime()));
                else
                    shareProfileviewholder.messageSeen.setText(context.getString(R.string.sent));

            } else {
                shareProfileviewholder.messageSeen.setText("");
            }
            // make the group of message by date set the gap of 1 min
            // means message send with in 1 min will show as a group
            if (position != 0) {
                ChatModel chat2 = mDataSet.get(position - 1);
                if (chat2.getTimestamp().substring(14, 16).equals(chat.getTimestamp().substring(14, 16))) {
                    shareProfileviewholder.datetxt.setVisibility(View.GONE);
                } else {
                    shareProfileviewholder.datetxt.setVisibility(View.VISIBLE);
                    shareProfileviewholder.datetxt.setText(changeDate(chat.getTimestamp()));
                }
                try {
                    JSONObject jsonObject=new JSONObject(chat.getText());
                    String userId=jsonObject.optString("id");
                    String fullName=jsonObject.optString("fullName");
                    String username=jsonObject.optString("username");
                    String pic=jsonObject.optString("pic");
                    shareProfileviewholder.tvFullName.setText(fullName);
                    shareProfileviewholder.tvUsername.setText(Functions.showUsername(username));

                    shareProfileviewholder.userProfile.setController(Functions.frescoImageLoad(pic,shareProfileviewholder.userProfile,false));

                }
                catch (Exception e){}

            } else {
                shareProfileviewholder.datetxt.setVisibility(View.VISIBLE);
                shareProfileviewholder.datetxt.setText(changeDate(chat.getTimestamp()));
                try {
                    JSONObject jsonObject=new JSONObject(chat.getText());
                    String userId=jsonObject.optString("id");
                    String fullName=jsonObject.optString("fullName");
                    String username=jsonObject.optString("username");
                    String pic=jsonObject.optString("pic");

                    shareProfileviewholder.tvFullName.setText(fullName);
                    shareProfileviewholder.tvUsername.setText(username);

                    shareProfileviewholder.userProfile.setController(Functions.frescoImageLoad(pic,shareProfileviewholder.userProfile,false));

                }
                catch (Exception e){}
            }

            shareProfileviewholder.bind(chat, listener,position);

        }

        else if (chat.getType().equals("image")) {
            final Chatimageviewholder chatimageholder = (Chatimageviewholder) holder;
            // check if the message is from sender or receiver
            if (chat.getSender_id().equals(myID)) {
                if (chat.getStatus().equals("1"))
                    chatimageholder.message_seen.setText(context.getString(R.string.seen_at)+" " + changeDateTime(chat.getTime()));
                else
                    chatimageholder.message_seen.setText(context.getString(R.string.sent));

            } else {
                chatimageholder.message_seen.setText("");
            }
            if (chat.getPic_url().equals("none")) {
                if (ChatA.uploadingImageId.equals(chat.getChat_id())) {
                    chatimageholder.pBar.setVisibility(View.VISIBLE);
                    chatimageholder.message_seen.setText("");
                } else {
                    chatimageholder.pBar.setVisibility(View.GONE);
                    chatimageholder.notSendMessageIcon.setVisibility(View.VISIBLE);
                    chatimageholder.message_seen.setText(context.getString(R.string.not_delivered));
                }
            } else {
                chatimageholder.notSendMessageIcon.setVisibility(View.GONE);
                chatimageholder.pBar.setVisibility(View.GONE);
            }

            // make the group of message by date set the gap of 1 min
            // means message send with in 1 min will show as a group
            if (position != 0) {
                ChatModel chat2 = mDataSet.get(position - 1);
                if (chat2.getTimestamp().substring(14, 16).equals(chat.getTimestamp().substring(14, 16))) {
                    chatimageholder.datetxt.setVisibility(View.GONE);
                } else {
                    chatimageholder.datetxt.setVisibility(View.VISIBLE);
                    chatimageholder.datetxt.setText(changeDate(chat.getTimestamp()));
                }

                if (chat.getPic_url() != null && !chat.getPic_url().equals("")) {
                    Uri uri = Uri.parse(chat.getPic_url());
                    chatimageholder.chatimage.setImageURI(uri);
                }

            } else {
                chatimageholder.datetxt.setVisibility(View.VISIBLE);
                chatimageholder.datetxt.setText(changeDate(chat.getTimestamp()));


                if (chat.getPic_url() != null && !chat.getPic_url().equals("")) {
                    Uri uri = Uri.parse(chat.getPic_url());
                    chatimageholder.chatimage.setImageURI(uri);
                }


            }

            chatimageholder.bind(mDataSet.get(position), position, listener, long_listener);
        }

        else if (chat.getType().equals("audio")) {
            final Chataudioviewholder chataudioviewholder = (Chataudioviewholder) holder;
            // check if the message is from sender or receiver
            if (chat.getSender_id().equals(myID)) {
                if (chat.getStatus().equals("1"))
                    chataudioviewholder.message_seen.setText(context.getString(R.string.seen_at)+" " + changeDateTime(chat.getTime()));
                else
                    chataudioviewholder.message_seen.setText(context.getString(R.string.sent));

            } else {
                chataudioviewholder.message_seen.setText("");
            }


            if (chat.getPic_url().equals("none")) {
                if (ChatA.uploadingAudioId.equals(chat.getChat_id())) {
                    chataudioviewholder.pBar.setVisibility(View.VISIBLE);
                    chataudioviewholder.message_seen.setText("");
                } else {
                    chataudioviewholder.pBar.setVisibility(View.GONE);
                    chataudioviewholder.notSendMessageIcon.setVisibility(View.VISIBLE);
                    chataudioviewholder.message_seen.setText(context.getString(R.string.not_delivered));
                }
            } else {
                chataudioviewholder.notSendMessageIcon.setVisibility(View.GONE);
                chataudioviewholder.pBar.setVisibility(View.GONE);
            }

            // make the group of message by date set the gap of 1 min
            // means message send with in 1 min will show as a group
            if (position != 0) {
                ChatModel chat2 = mDataSet.get(position - 1);
                if (chat2.getTimestamp().substring(14, 16).equals(chat.getTimestamp().substring(14, 16))) {
                    chataudioviewholder.datetxt.setVisibility(View.GONE);
                } else {
                    chataudioviewholder.datetxt.setVisibility(View.VISIBLE);
                    chataudioviewholder.datetxt.setText(changeDate(chat.getTimestamp()));
                }
            } else {
                chataudioviewholder.datetxt.setVisibility(View.VISIBLE);
                chataudioviewholder.datetxt.setText(changeDate(chat.getTimestamp()));

            }

            if (ChatA.playingId.equals(chat.chat_id) && ChatA.mediaPlayer != null) {
                chataudioviewholder.playBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_pause_icon));
                chataudioviewholder.seekBar.setProgress(ChatA.mediaPlayerProgress);
            } else {
                chataudioviewholder.playBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_play_icon));
                chataudioviewholder.seekBar.setProgress(0);
            }


            if (ChatA.uploadingAudioId.equals("none")) {

            }

            File fullpath = new File(Environment.getExternalStorageDirectory() + "/Binder/" + chat.chat_id + ".mp3");
            if (fullpath.exists()) {
                chataudioviewholder.totalTime.setText(getfileduration(Uri.parse(fullpath.getAbsolutePath())));

            } else {
                chataudioviewholder.totalTime.setText(null);
            }


            chataudioviewholder.bind(mDataSet.get(position), position, listener, long_listener);

        }

        else if(chat.getType().equals("video")){

            final ChatVideoviewholder chatVideoviewholder=(ChatVideoviewholder) holder;

            if (position != 0) {
                ChatModel chat2 = mDataSet.get(position - 1);
                if (chat2.getTimestamp().substring(14, 16).equals(chat.getTimestamp().substring(14, 16))) {
                    chatVideoviewholder.datetxt.setVisibility(View.GONE);
                } else {
                    chatVideoviewholder.datetxt.setVisibility(View.VISIBLE);
                    chatVideoviewholder.datetxt.setText(changeDate(chat.getTimestamp()));
                }

                if(chat.getPic_url()!= null && !chat.getPic_url().equals("")){
                    Uri uri = Uri.parse(chat.getPic_url());
                    chatVideoviewholder.chatimage.setImageURI(uri);
                }
            }
            else {
                chatVideoviewholder.datetxt.setVisibility(View.VISIBLE);
                chatVideoviewholder.datetxt.setText(changeDate(chat.getTimestamp()));

                if(chat.getPic_url()!= null && !chat.getPic_url().equals("")){
                    Uri uri = Uri.parse(chat.getPic_url());
                    chatVideoviewholder.chatimage.setImageURI(uri);
                }

            }

            chatVideoviewholder.bind(position,mDataSet.get(position),listener);
        }


        else if (chat.getType().equals("gif")) {
            final Chatimageviewholder chatimageholder = (Chatimageviewholder) holder;
            // check if the message is from sender or receiver
            if (chat.getSender_id().equals(myID)) {
                if (chat.getStatus().equals("1"))
                    chatimageholder.message_seen.setText(context.getString(R.string.seen_at)+" " + changeDateTime(chat.getTime()));
                else
                    chatimageholder.message_seen.setText(context.getString(R.string.sent));

            } else {
                chatimageholder.message_seen.setText("");
            }
            // make the group of message by date set the gap of 1 min
            // means message send with in 1 min will show as a group
            if (position != 0) {
                ChatModel chat2 = mDataSet.get(position - 1);
                if (chat2.getTimestamp().substring(14, 16).equals(chat.getTimestamp().substring(14, 16))) {
                    chatimageholder.datetxt.setVisibility(View.GONE);
                } else {
                    chatimageholder.datetxt.setVisibility(View.VISIBLE);
                    chatimageholder.datetxt.setText(changeDate(chat.getTimestamp()));
                }

                chatimageholder.chatimage.setController(
                        Fresco.newDraweeControllerBuilder()
                                .setUri(Uri.parse(Variables.GIF_FIRSTPART + chat.getPic_url() + Variables.GIF_SECONDPART))
                                .setAutoPlayAnimations(true)
                                .build());

            } else {
                chatimageholder.datetxt.setVisibility(View.VISIBLE);
                chatimageholder.datetxt.setText(changeDate(chat.getTimestamp()));

                chatimageholder.chatimage.setController(
                        Fresco.newDraweeControllerBuilder()
                                .setUri(Uri.parse(Variables.GIF_FIRSTPART + chat.getPic_url() + Variables.GIF_SECONDPART))
                                .setAutoPlayAnimations(true)
                                .build());


            }

            chatimageholder.bind(mDataSet.get(position), position, listener, long_listener);
        }


        else if (chat.getType().equals("delete")) {
            Alertviewholder alertviewholder = (Alertviewholder) holder;
            alertviewholder.message.setTextColor(context.getResources().getColor(R.color.delete_message_text));
            alertviewholder.message.setBackground(context.getResources().getDrawable(R.drawable.d_round_gray_background_2));

            alertviewholder.message.setText(context.getString(R.string.this_message_is_deleted_by)+" " + chat.getSender_name());

            if (position != 0) {
                ChatModel chat2 = mDataSet.get(position - 1);
                if (chat2.getTimestamp().substring(11, 13).equals(chat.getTimestamp().substring(11, 13))) {
                    alertviewholder.datetxt.setVisibility(View.GONE);
                } else {
                    alertviewholder.datetxt.setVisibility(View.VISIBLE);
                    alertviewholder.datetxt.setText(changeDate(chat.getTimestamp()));
                }

            } else {
                alertviewholder.datetxt.setVisibility(View.VISIBLE);
                alertviewholder.datetxt.setText(changeDate(chat.getTimestamp()));

            }

        }


    }

    @Override
    public int getItemViewType(int position) {

        if (mDataSet.get(position).sender_id.equals(myID)) {
            return   mymessageviewtype(position);
        }
        else {
            return   othermessageviewtype(position);
        }

    }



    public int mymessageviewtype(int position){
        switch (mDataSet.get(position).getType()){
            case "text":
                return MYCHAT;

            case "image":
                return MYCHATIMAGE;

            case "audio":
                return MY_AUDIO_MESSAGE;

            case "video":
                return my_video_message;

            case "gif":
                return MYGIFIMAGE;
            case "profileShare":
                return my_profile_share;
            default:
                return ALERT_MESSAGE;

        }
    }


    public int othermessageviewtype(int position){

        switch (mDataSet.get(position).getType()){
            case "text":
                return FRIENDCHAT;

            case "image":
                return OTHERCHATIMAGE;

            case "audio":
                return OTHER_AUDIO_MESSAGE;

            case "video":
                return other_video_message;

            case "gif":
                return OTHERGIFIMAGE;
            case "profileShare":
                return other_profile_share;
            default:
                return ALERT_MESSAGE;

        }

    }



    // change the date into (today ,yesterday and date)
    private String changeDate(String date) {

        try {
            long currenttime = System.currentTimeMillis();
            long databasedate = 0;
            Date d = null;
            try {
                d = Variables.df.parse(date);
                databasedate = d.getTime();

            } catch (Exception e) {
                e.printStackTrace();
            }
            long difference = currenttime - databasedate;
            if (difference < 86400000) {
                int chatday = Functions.parseInterger(date.substring(0, 2));
                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);
                if (todayDay == chatday)
                    return "Today " + sdf.format(d);
                else if ((todayDay - chatday) == 1)
                    return "Yesterday " + sdf.format(d);
            } else if (difference < 172800000) {
                int chatday = Functions.parseInterger(date.substring(0, 2));
                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a",Locale.ENGLISH);
                if ((todayDay - chatday) == 1)
                    return "Yesterday " + sdf.format(d);
            }

            SimpleDateFormat sdf = new SimpleDateFormat("MMM-dd-yyyy hh:mm a",Locale.ENGLISH);
            return sdf.format(d);
        } catch (Exception e) {
            return date;
        }

    }


    // change the date into (today ,yesterday and date)
    private String changeDateTime(String date) {
        try {
            Date d = null;
            d = Variables.df2.parse(date);
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a",Locale.ENGLISH);
            return sdf.format(d);
        } catch (Exception e) {
            return date;
        }

    }

    // get the audio file duration that is store in our directory
    private String getfileduration(Uri uri) {
        try {

            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(context, uri);
            String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            final int file_duration = Functions.parseInterger(durationStr);

            long second = (file_duration / 1000) % 60;
            long minute = (file_duration / (1000 * 60)) % 60;

            return String.format("%02d:%02d", minute, second);
        } catch (Exception e) {

        }
        return null;
    }


}
