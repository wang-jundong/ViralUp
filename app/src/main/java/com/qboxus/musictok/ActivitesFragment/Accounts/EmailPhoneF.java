package com.qboxus.musictok.ActivitesFragment.Accounts;

import android.content.Intent;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.qboxus.musictok.Models.UserRegisterModel;
import com.qboxus.musictok.MainMenu.RelateToFragmentOnBack.RootFragment;
import com.qboxus.musictok.R;

import static com.qboxus.musictok.ActivitesFragment.Accounts.PhoneF.RESOLVE_HINT;

// this fragment is a email or phone fragment
public class EmailPhoneF extends RootFragment implements View.OnClickListener {
    View view;
    protected TabLayout tabLayout;
    TextView signupTxt;
    protected ViewPager pager;
    private ViewPagerAdapter adapter;
    String fromWhere;
    UserRegisterModel userRegisterModel = new UserRegisterModel();

    public EmailPhoneF(String fromWhere) {
        this.fromWhere = fromWhere;
    }

    public EmailPhoneF() {
        //empty constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_sign_up, container, false);
        init();

        signupTxt = view.findViewById(R.id.signup_txt);
        Bundle bundle = getArguments();
        userRegisterModel = (UserRegisterModel) bundle.getSerializable("user_model");
        if (fromWhere != null && fromWhere != null) {
            if (fromWhere.equals("login")) {
                signupTxt.setText(view.getContext().getString(R.string.login));
            }
        }

        view.findViewById(R.id.goBack).setOnClickListener(this::onClick);
        return view;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.goBack:
                getActivity().onBackPressed();
                break;
        }

    }

    // this method will initionalize all the views and set up the tabs
    private void init() {
        tabLayout = (TabLayout) view.findViewById(R.id.tabs);
        pager = view.findViewById(R.id.pager);
        pager.setOffscreenPageLimit(3);

        adapter = new ViewPagerAdapter(getChildFragmentManager());
        pager.setAdapter(adapter);
        tabLayout.setupWithViewPager(pager);
        setupTabIcons();
    }

    // this method will change the text and style tabs
    private void setupTabIcons() {

        View view1 = LayoutInflater.from(getActivity()).inflate(R.layout.item_tabs_signup, null);
        TextView text_history = view1.findViewById(R.id.text_history);
        text_history.setText(view.getContext().getString(R.string.phone));
        tabLayout.getTabAt(0).setCustomView(view1);

        View view2 = LayoutInflater.from(getActivity()).inflate(R.layout.item_tabs_signup, null);
        TextView text_history1 = view2.findViewById(R.id.text_history);
        text_history1.setText(view.getContext().getString(R.string.email));
        tabLayout.getTabAt(1).setCustomView(view2);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                View v = tab.getCustomView();
                TextView text_history = v.findViewById(R.id.text_history);

                switch (tab.getPosition()) {
                    case 0:
                        text_history.setTextColor(getResources().getColor(R.color.colorPrimary));
                        break;

                    case 1:
                        text_history.setTextColor(getResources().getColor(R.color.colorPrimary));
                        break;
                }
                tab.setCustomView(v);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                View v = tab.getCustomView();
                TextView text_history = v.findViewById(R.id.text_history);

                switch (tab.getPosition()) {
                    case 0:
                        text_history.setTextColor(getResources().getColor(R.color.black));
                        break;
                    case 1:
                        text_history.setTextColor(getResources().getColor(R.color.black));
                        break;

                }

                tab.setCustomView(v);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }

        });


    }


    class ViewPagerAdapter extends FragmentPagerAdapter {

        SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();

        public ViewPagerAdapter( FragmentManager fm) {
            super(fm,FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @Override
        public Fragment getItem(int position) {
            final Fragment result;
            switch (position) {
                case 0:
                    result = new PhoneF(userRegisterModel, fromWhere);
                    break;
                case 1:
                    result = new EmailF(userRegisterModel, fromWhere);
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
            return null;
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