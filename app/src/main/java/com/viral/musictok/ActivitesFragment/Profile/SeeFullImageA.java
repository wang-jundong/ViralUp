package com.viral.musictok.ActivitesFragment.Profile;

import com.viral.musictok.SimpleClasses.AppCompatLocaleActivity;

import android.os.Bundle;
import android.widget.ImageButton;

import com.facebook.drawee.view.SimpleDraweeView;
import com.viral.musictok.R;
import com.viral.musictok.SimpleClasses.Functions;
import com.viral.musictok.SimpleClasses.Variables;

public class SeeFullImageA extends AppCompatLocaleActivity {

    ImageButton closeGallery;
    SimpleDraweeView singleImage;
    String imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Functions.setLocale(Functions.getSharedPreference(SeeFullImageA.this).getString(Variables.APP_LANGUAGE_CODE,Variables.DEFAULT_LANGUAGE_CODE)
                , this, SeeFullImageA.class,false);
        setContentView(R.layout.activity_see_full_image);




        imageUrl = getIntent().getStringExtra("image_url");

        closeGallery = findViewById(R.id.close_gallery);
        closeGallery.setOnClickListener(v -> {
           SeeFullImageA.super.onBackPressed();

        });


        singleImage = findViewById(R.id.single_image);
        if (imageUrl != null && !imageUrl.equalsIgnoreCase("")) {

            singleImage.setController(Functions.frescoImageLoad(imageUrl,singleImage,false));


        }
    }
}