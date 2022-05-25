package com.qboxus.musictok.ActivitesFragment.SendGift;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.qboxus.musictok.Interfaces.FragmentCallBack;
import com.qboxus.musictok.R;
import com.smarteist.autoimageslider.SliderViewAdapter;

import java.util.ArrayList;
import java.util.List;

public class SendGiftVHAdapter  extends
        SliderViewAdapter<SendGiftVHAdapter.SliderAdapterVH> {

    private List<List<StickerModel>> list = new ArrayList<>();
    FragmentCallBack callBack;
    Button tab_send_gift;

    public SendGiftVHAdapter(List<List<StickerModel>> list, Button tab_send_gift, FragmentCallBack callBack) {
        this.list = list;
        this.tab_send_gift=tab_send_gift;
        this.callBack = callBack;
    }

    @Override
    public SliderAdapterVH onCreateViewHolder(ViewGroup parent) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_slider_layout_item, null);
        return new SliderAdapterVH(inflate);
    }

    @Override
    public void onBindViewHolder(SliderAdapterVH viewHolder, final int position) {
        List<StickerModel> data_list= list.get(position);

        GridLayoutManager layoutManager=new GridLayoutManager(viewHolder.itemView.getContext(),3);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        viewHolder.recylerview.setLayoutManager(layoutManager);
       viewHolder.adapter=new StickerAdapter(viewHolder.itemView.getContext(), data_list
                , new StickerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(StickerModel item) {

                for (int i=0;i<data_list.size();i++)
                {
                    StickerModel model=data_list.get(i);
                    if (model.id==item.id)
                    {
                        if (model.isSelected)
                        {
                            tab_send_gift.setClickable(false);
                            tab_send_gift.setEnabled(false);
                            model.isSelected=false;
                            model.count=0;
                            data_list.set(i,model);
                        }
                        else
                        {
                            tab_send_gift.setClickable(true);
                            tab_send_gift.setEnabled(true);
                            model.isSelected=true;
                            model.count=1;
                            data_list.set(i,model);

                            Bundle bundle=new Bundle();
                            bundle.putBoolean("isShow",true);
                            bundle.putSerializable("Data",model);
                            callBack.onResponce(bundle);
                        }

                    }
                    else
                    {
                        model.isSelected=false;
                        model.count=0;
                        data_list.set(i,model);
                    }
                    viewHolder.adapter.notifyDataSetChanged();
                }

            }
        });
        viewHolder.recylerview.setAdapter(viewHolder.adapter);
    }


    @Override
    public int getCount() {
        //slider view count could be dynamic size
        return list.size();
    }


    public class SliderAdapterVH extends SliderViewAdapter.ViewHolder {

        private View itemView;
        private RecyclerView recylerview;
        public StickerAdapter adapter;



        public SliderAdapterVH(View itemView) {
            super(itemView);
            recylerview = itemView.findViewById(R.id.recylerview);
            this.itemView = itemView;
        }
    }

}
