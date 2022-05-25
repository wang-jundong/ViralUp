package com.qboxus.musictok.ActivitesFragment.Profile;

import com.qboxus.musictok.SimpleClasses.AppCompatLocaleActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.tabs.TabLayout;
import com.qboxus.musictok.ActivitesFragment.Profile.Favourite.FavouriteVideosF;
import com.qboxus.musictok.ActivitesFragment.Profile.Setting.NoInternetA;
import com.qboxus.musictok.ActivitesFragment.Search.SearchHashTagsF;
import com.qboxus.musictok.ActivitesFragment.SoundLists.FavouriteSoundF;
import com.qboxus.musictok.Adapters.ViewPagerAdapter;
import com.qboxus.musictok.Interfaces.InternetCheckCallback;
import com.qboxus.musictok.R;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.Variables;

public class FavouriteMainA extends AppCompatLocaleActivity {

    Context context;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Functions.setLocale(Functions.getSharedPreference(FavouriteMainA.this).getString(Variables.APP_LANGUAGE_CODE,Variables.DEFAULT_LANGUAGE_CODE)
                , this, FavouriteMainA.class,false);
        setContentView(R.layout.activity_favourite_main_);
        context = FavouriteMainA.this;

        findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FavouriteMainA.super.onBackPressed();
            }
        });

        Set_tabs();
    }


    // set up the favourite video sound and hastage fragment
    protected TabLayout tabLayout;
    protected ViewPager menuPager;
    ViewPagerAdapter adapter;

    public void Set_tabs() {

        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        menuPager = (ViewPager) findViewById(R.id.viewpager);
        menuPager.setOffscreenPageLimit(3);
        tabLayout = (TabLayout) findViewById(R.id.tabs);


        adapter.addFrag(new FavouriteVideosF(), getString(R.string.videos));
        adapter.addFrag(new FavouriteSoundF(), getString(R.string.sounds));
        adapter.addFrag(new SearchHashTagsF("favourite"), getString(R.string.hashtag));


        menuPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(menuPager);

    }



}
