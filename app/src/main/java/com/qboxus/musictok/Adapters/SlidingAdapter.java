package com.qboxus.musictok.Adapters;

import android.content.Context;
import android.net.Uri;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.viewpager.widget.PagerAdapter;

import com.facebook.drawee.view.SimpleDraweeView;
import com.qboxus.musictok.Constants;
import com.qboxus.musictok.Models.SliderModel;
import com.qboxus.musictok.R;
import com.qboxus.musictok.Interfaces.AdapterClickListener;
import com.qboxus.musictok.ApiClasses.ApiLinks;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.Variables;

import java.util.ArrayList;


public class SlidingAdapter extends PagerAdapter {


    private ArrayList<SliderModel> imageList;
    private LayoutInflater inflater;
    private Context context;

    AdapterClickListener adapterClickListener;

    public SlidingAdapter(Context context, ArrayList<SliderModel> IMAGES, AdapterClickListener click_listener) {
        this.context = context;
        this.imageList = IMAGES;
        this.adapterClickListener = click_listener;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return imageList.size();
    }

    @Override
    public Object instantiateItem(ViewGroup view, final int position) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View imageLayout = inflater.inflate(R.layout.item_slider_layout, view, false);

        assert imageLayout != null;

        final SimpleDraweeView imageView = imageLayout.findViewById(R.id.save_image);
        final RelativeLayout slider_rlt = imageLayout.findViewById(R.id.slider_rlt);

        String url = imageList.get(position).image;
        if (url != null) {
            String imageUrl = Constants.BASE_URL + url;
            imageView.setController(Functions.frescoImageLoad(imageUrl,imageView,false));


            Functions.printLog(Constants.tag, imageUrl);
        }

        slider_rlt.setOnClickListener(v -> {
            adapterClickListener.onItemClick(v, position, imageList.get(position));

        });

        view.addView(imageLayout, 0);


        return imageLayout;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
    }

    @Override
    public Parcelable saveState() {
        return null;
    }


}
