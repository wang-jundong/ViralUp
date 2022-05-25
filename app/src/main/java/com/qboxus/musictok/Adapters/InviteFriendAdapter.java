package com.qboxus.musictok.Adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.qboxus.musictok.Interfaces.AdapterClickListener;
import com.qboxus.musictok.Models.InviteFriendModel;
import com.qboxus.musictok.R;
import com.qboxus.musictok.SimpleClasses.Functions;
import java.util.ArrayList;

public class InviteFriendAdapter extends RecyclerView.Adapter<InviteFriendAdapter.CustomViewHolder> {

    ArrayList<InviteFriendModel> datalist;
    public AdapterClickListener listener;

    public InviteFriendAdapter(ArrayList<InviteFriendModel> datalist, AdapterClickListener listener) {
        this.datalist = datalist;
        this.listener = listener;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_invite_friend_view, viewGroup, false);
        return new CustomViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return datalist.size();
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {

        SimpleDraweeView userImage;
        TextView userName;
        TextView userPhone;
        TextView actionTxt;



        public CustomViewHolder(View view) {
            super(view);
            userImage = view.findViewById(R.id.userImage);
            userName = view.findViewById(R.id.userName);
            userPhone = view.findViewById(R.id.userPhone);
            actionTxt=view.findViewById(R.id.action_txt);
        }

        public void bind(final int pos, final Object item, final AdapterClickListener listener) {

            actionTxt.setOnClickListener(v -> {
                listener.onItemClick(v, pos, item);

            });


        }


    }



    @Override
    public void onBindViewHolder(final CustomViewHolder holder, final int i) {
        InviteFriendModel item = datalist.get(i);

        holder.userName.setText(item.getName());
        holder.userPhone.setText(item.getPhone());

        if (item.getPath() != null && !item.getPath().equals("")) {
            ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(item.getPath())).build();
            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setImageRequest(request)
                    .setOldController(holder.userImage.getController())
                    .build();
            holder.userImage.setController(controller);
        }
        else
        {
            holder.userImage.setImageResource(R.drawable.ic_user_icon);
        }

        holder.bind(i, datalist.get(i), listener);

    }


}