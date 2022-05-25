package com.qboxus.musictok.ActivitesFragment.SoundLists;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import com.qboxus.musictok.SimpleClasses.AppCompatLocaleActivity;

import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import com.qboxus.musictok.ActivitesFragment.Profile.Setting.NoInternetA;
import com.qboxus.musictok.Interfaces.InternetCheckCallback;
import com.qboxus.musictok.MainMenu.CustomViewPager;
import com.qboxus.musictok.R;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.Variables;

public class SoundListMainA extends AppCompatLocaleActivity implements View.OnClickListener {

    protected TabLayout tablayout;

    protected CustomViewPager pager;

    private ViewPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Functions.setLocale(Functions.getSharedPreference(SoundListMainA.this).getString(Variables.APP_LANGUAGE_CODE,Variables.DEFAULT_LANGUAGE_CODE)
                , this, SoundListMainA.class,false);
        setContentView(R.layout.activity_sound_list_main);

        tablayout = (TabLayout) findViewById(R.id.groups_tab);
        pager = findViewById(R.id.viewpager);
        pager.setOffscreenPageLimit(2);
        pager.setPagingEnabled(false);

        // Note that we are passing childFragmentManager, not FragmentManager
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(adapter);
        tablayout.setupWithViewPager(pager);


        findViewById(R.id.goBack).setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.goBack:
                onBackPressed();
                break;
        }
    }


    class ViewPagerAdapter extends FragmentPagerAdapter {


        SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();


        public ViewPagerAdapter(FragmentManager fm) {

            super(fm, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @Override
        public Fragment getItem(int position) {
            final Fragment result;
            switch (position) {
                case 0:
                    result = new DiscoverSoundListF();
                    break;
                case 1:
                    result = new FavouriteSoundF();
                    break;
                default:
                    result = null;
                    break;
            }

            return result;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(final int position) {
            switch (position) {
                case 0:
                    return getString(R.string.discover);
                case 1:
                    return getString(R.string.my_fav);

                default:
                    return null;

            }


        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            registeredFragments.put(position, fragment);
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            registeredFragments.remove(position);
            super.destroyItem(container, position, object);
        }

    }


}
