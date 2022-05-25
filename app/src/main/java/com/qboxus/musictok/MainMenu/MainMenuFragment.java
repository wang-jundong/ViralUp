package com.qboxus.musictok.MainMenu;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.qboxus.musictok.ActivitesFragment.Chat.ChatA;
import com.qboxus.musictok.ActivitesFragment.NotificationF;
import com.google.android.material.tabs.TabLayout;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.core.content.ContextCompat;

import android.os.Handler;
import android.os.Looper;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.qboxus.musictok.ActivitesFragment.DiscoverF;
import com.qboxus.musictok.ActivitesFragment.HomeF;
import com.qboxus.musictok.MainMenu.RelateToFragmentOnBack.OnBackPressListener;
import com.qboxus.musictok.MainMenu.RelateToFragmentOnBack.RootFragment;
import com.qboxus.musictok.ActivitesFragment.Profile.ProfileTabF;
import com.qboxus.musictok.R;
import com.qboxus.musictok.Services.UploadService;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.PermissionUtils;
import com.qboxus.musictok.SimpleClasses.Variables;
import com.qboxus.musictok.ActivitesFragment.VideoRecording.VideoRecoderA;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;



public class MainMenuFragment extends RootFragment {

    public static TabLayout tabLayout;
    protected CustomViewPager pager;
    private ViewPagerAdapter adapter;
    Context context;


    PermissionUtils takePermissionUtils;

    public MainMenuFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_menu, container, false);
        context = view.getContext();

        tabLayout = (TabLayout) view.findViewById(R.id.tabs);
        pager = view.findViewById(R.id.viewpager);
        pager.setOffscreenPageLimit(4);
        pager.setPagingEnabled(false);

        getFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {

                int count = getActivity().getSupportFragmentManager().getBackStackEntryCount();
                if (count == 0) {
                    int selected_postion = tabLayout.getSelectedTabPosition();
                    if (selected_postion == 0) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            Functions.blackStatusBar(getActivity());
                        }
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            Functions.whiteStatusBar(getActivity());
                        }
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        Functions.whiteStatusBar(getActivity());
                    }
                }
            }
        });



        return view;
    }






    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Note that we are passing childFragmentManager, not FragmentManager
        adapter = new ViewPagerAdapter(getChildFragmentManager());
        pager.setAdapter(adapter);
        tabLayout.setupWithViewPager(pager);
        setupTabIcons();

    }



    public boolean onBackPressed() {
        // currently visible tab Fragment
        OnBackPressListener currentFragment = (OnBackPressListener) adapter.getRegisteredFragment(pager.getCurrentItem());

        if (currentFragment != null) {
            // lets see if the currentFragment or any of its childFragment can handle onBackPressed
            return currentFragment.onBackPressed();
        }

        // this Fragment couldn't handle the onBackPressed call
        return false;
    }




    // this function will set all the icon and text in
    // Bottom tabs when we open an activity
    private void setupTabIcons() {

        View view1 = LayoutInflater.from(context).inflate(R.layout.item_tablayout, null);
        ImageView imageView1 = view1.findViewById(R.id.image);
        TextView title1 = view1.findViewById(R.id.text);
        imageView1.setImageDrawable(getResources().getDrawable(R.drawable.ic_home_white));
        title1.setText(context.getString(R.string.home));
        title1.setTextColor(context.getResources().getColor(R.color.white));
        tabLayout.getTabAt(0).setCustomView(view1);


        View view2 = LayoutInflater.from(context).inflate(R.layout.item_tablayout, null);
        ImageView imageView2 = view2.findViewById(R.id.image);
        TextView title2 = view2.findViewById(R.id.text);
        imageView2.setImageDrawable(getResources().getDrawable(R.drawable.ic_discovery_gray));
        imageView2.setColorFilter(ContextCompat.getColor(context, R.color.colorwhite_50), android.graphics.PorterDuff.Mode.SRC_IN);
        title2.setText(context.getString(R.string.discover));
        title2.setTextColor(context.getResources().getColor(R.color.colorwhite_50));
        tabLayout.getTabAt(1).setCustomView(view2);


        View view3 = LayoutInflater.from(context).inflate(R.layout.item_add_tab_layout, null);
        tabLayout.getTabAt(2).setCustomView(view3);


        View view4 = LayoutInflater.from(context).inflate(R.layout.item_tablayout, null);
        ImageView imageView4 = view4.findViewById(R.id.image);
        TextView title4 = view4.findViewById(R.id.text);
        imageView4.setImageDrawable(getResources().getDrawable(R.drawable.ic_notification_gray));
        imageView4.setColorFilter(ContextCompat.getColor(context, R.color.colorwhite_50), android.graphics.PorterDuff.Mode.SRC_IN);
        title4.setText(context.getString(R.string.inbox));
        title4.setTextColor(context.getResources().getColor(R.color.colorwhite_50));
        tabLayout.getTabAt(3).setCustomView(view4);


        View view5 = LayoutInflater.from(context).inflate(R.layout.item_tablayout, null);
        ImageView imageView5 = view5.findViewById(R.id.image);
        TextView title5 = view5.findViewById(R.id.text);
        imageView5.setImageDrawable(getResources().getDrawable(R.drawable.ic_profile_gray));
        imageView5.setColorFilter(ContextCompat.getColor(context, R.color.colorwhite_50), android.graphics.PorterDuff.Mode.SRC_IN);
        title5.setText(context.getString(R.string.profile));
        title5.setTextColor(context.getResources().getColor(R.color.colorwhite_50));
        tabLayout.getTabAt(4).setCustomView(view5);


        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {


            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                View v = tab.getCustomView();
                ImageView image = v.findViewById(R.id.image);
                TextView title = v.findViewById(R.id.text);

                switch (tab.getPosition()) {
                    case 0:
                        Functions.blackStatusBar(getActivity());
                        onHomeClick();
                        image.setImageDrawable(getResources().getDrawable(R.drawable.ic_home_white));
                        title.setTextColor(context.getResources().getColor(R.color.white));
                        break;

                    case 1:
                        Functions.whiteStatusBar(getActivity());
                        onotherTabClick();
                        image.setImageDrawable(getResources().getDrawable(R.drawable.ic_discover_red));
                        image.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary), android.graphics.PorterDuff.Mode.SRC_IN);
                        title.setTextColor(context.getResources().getColor(R.color.colorPrimary));
                        break;


                    case 3:
                        Functions.whiteStatusBar(getActivity());
                        onotherTabClick();
                        image.setImageDrawable(getResources().getDrawable(R.drawable.ic_notification_red));
                        image.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary), android.graphics.PorterDuff.Mode.SRC_IN);
                        title.setTextColor(context.getResources().getColor(R.color.colorPrimary));
                        break;
                    case 4:
                        Functions.whiteStatusBar(getActivity());
                        onotherTabClick();
                        image.setImageDrawable(getResources().getDrawable(R.drawable.ic_profile_red));
                        image.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary), android.graphics.PorterDuff.Mode.SRC_IN);
                        title.setTextColor(context.getResources().getColor(R.color.colorPrimary));
                        break;
                }
                tab.setCustomView(v);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                View v = tab.getCustomView();
                ImageView image = v.findViewById(R.id.image);
                TextView title = v.findViewById(R.id.text);

                switch (tab.getPosition()) {
                    case 0:
                        image.setImageDrawable(getResources().getDrawable(R.drawable.ic_home_gray));
                        title.setTextColor(context.getResources().getColor(R.color.darkgray));
                        break;
                    case 1:
                        image.setImageDrawable(getResources().getDrawable(R.drawable.ic_discovery_gray));
                        title.setTextColor(context.getResources().getColor(R.color.darkgray));
                        break;

                    case 3:
                        image.setImageDrawable(getResources().getDrawable(R.drawable.ic_notification_gray));
                        title.setTextColor(context.getResources().getColor(R.color.darkgray));
                        break;
                    case 4:
                        image.setImageDrawable(getResources().getDrawable(R.drawable.ic_profile_gray));
                        title.setTextColor(context.getResources().getColor(R.color.darkgray));
                        break;
                }
                tab.setCustomView(v);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }

        });


        final LinearLayout tabStrip = ((LinearLayout) tabLayout.getChildAt(0));
        tabStrip.setEnabled(false);

        tabStrip.getChildAt(2).setClickable(false);
        view3.setOnClickListener(v -> {
            takePermissionUtils=new PermissionUtils(getActivity(),mPermissionResult);
            if (takePermissionUtils.isStorageCameraRecordingPermissionGranted()) {

                uploadNewVideo();
            }
            else
            {
                takePermissionUtils.showStorageCameraRecordingPermissionDailog(context.getString(R.string.we_need_storage_camera_recording_permission_for_make_new_video));
            }
        });


        tabStrip.getChildAt(3).setClickable(false);
        view4.setOnClickListener(v -> {
            if (Functions.checkLoginUser(getActivity())) {

                TabLayout.Tab tab = tabLayout.getTabAt(3);
                tab.select();
            }
        });

        tabStrip.getChildAt(4).setClickable(false);
        view5.setOnClickListener(v -> {
            if (Functions.checkLoginUser(getActivity())) {

                TabLayout.Tab tab = tabLayout.getTabAt(4);
                tab.select();
            }


        });

        onHomeClick();

        if (MainMenuActivity.intent != null) {

            if (MainMenuActivity.intent.hasExtra("action_type")) {


                if (Functions.getSharedPreference(context).getBoolean(Variables.IS_LOGIN, false)) {
                    String action_type = MainMenuActivity.intent.getExtras().getString("action_type");

                    if (action_type.equals("message")) {

                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                TabLayout.Tab tab = tabLayout.getTabAt(3);
                                tab.select();
                            }
                        }, 1500);


                        String id = MainMenuActivity.intent.getExtras().getString("senderid");
                        String name = MainMenuActivity.intent.getExtras().getString("title");
                        String icon = MainMenuActivity.intent.getExtras().getString("icon");

                        chatFragment(id, name, icon);

                    }
                }

            }

        }


    }

    private void uploadNewVideo() {
        Functions.makeDirectry(Functions.getAppFolder(getActivity())+Variables.APP_HIDED_FOLDER);
        Functions.makeDirectry(Functions.getAppFolder(getActivity())+Variables.DRAFT_APP_FOLDER);
        if (Functions.checkLoginUser(getActivity()))
        {
            if (Functions.isMyServiceRunning(getActivity(), new UploadService().getClass())) {
                Toast.makeText(getActivity(), context.getString(R.string.video_already_in_progress), Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(getActivity(), VideoRecoderA.class);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top);

            }
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
                    result = new HomeF();
                    break;

                case 1:
                    result = new DiscoverF();
                    break;

                case 2:
                    result = new BlankFragment();
                    break;

                case 3:
                    result = new NotificationF();
                    break;

                case 4:
                    result = new ProfileTabF();
                    break;

                default:
                    result = new HomeF();
                    break;
            }

            return result;
        }

        @Override
        public int getCount() {
            return 5;
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


        public Fragment getRegisteredFragment(int position) {

            return registeredFragments.get(position);

        }


    }


    // add the listener of home bth which will open the recording screen
    public void onHomeClick() {

        TabLayout.Tab tab1 = tabLayout.getTabAt(1);
        View view1 = tab1.getCustomView();
        ImageView imageView1 = view1.findViewById(R.id.image);
        imageView1.setColorFilter(ContextCompat.getColor(context, R.color.colorwhite_50), android.graphics.PorterDuff.Mode.SRC_IN);
        TextView tex1 = view1.findViewById(R.id.text);
        tex1.setTextColor(context.getResources().getColor(R.color.colorwhite_50));
        tab1.setCustomView(view1);

        TabLayout.Tab tab2 = tabLayout.getTabAt(2);
        View view2 = tab2.getCustomView();
        ImageView image = view2.findViewById(R.id.image);
        image.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_add_white));
        tab2.setCustomView(view2);

        TabLayout.Tab tab3 = tabLayout.getTabAt(3);
        View view3 = tab3.getCustomView();
        ImageView imageView3 = view3.findViewById(R.id.image);
        imageView3.setColorFilter(ContextCompat.getColor(context, R.color.colorwhite_50), android.graphics.PorterDuff.Mode.SRC_IN);
        TextView tex3 = view3.findViewById(R.id.text);
        tex3.setTextColor(context.getResources().getColor(R.color.colorwhite_50));
        tab3.setCustomView(view3);


        TabLayout.Tab tab4 = tabLayout.getTabAt(4);
        View view4 = tab4.getCustomView();
        ImageView imageView4 = view4.findViewById(R.id.image);
        imageView4.setColorFilter(ContextCompat.getColor(context, R.color.colorwhite_50), android.graphics.PorterDuff.Mode.SRC_IN);
        TextView tex4 = view4.findViewById(R.id.text);
        tex4.setTextColor(context.getResources().getColor(R.color.colorwhite_50));
        tab4.setCustomView(view4);

        tabLayout.setBackground(getResources().getDrawable(R.drawable.d_top_white_line));

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getActivity().getWindow().setNavigationBarColor(ContextCompat.getColor(context, R.color.black));
        }
    }


    // profile and notification tab click listener handler when user is not login into app
    public void onotherTabClick() {

        TabLayout.Tab tab1 = tabLayout.getTabAt(1);
        View view1 = tab1.getCustomView();
        TextView tex1 = view1.findViewById(R.id.text);
        ImageView imageView1 = view1.findViewById(R.id.image);
        imageView1.setColorFilter(ContextCompat.getColor(context, R.color.darkgray), android.graphics.PorterDuff.Mode.SRC_IN);
        tex1.setTextColor(context.getResources().getColor(R.color.darkgray));
        tab1.setCustomView(view1);

        TabLayout.Tab tab2 = tabLayout.getTabAt(2);
        View view2 = tab2.getCustomView();
        ImageView image = view2.findViewById(R.id.image);
        image.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_add_black));
        tab2.setCustomView(view2);

        TabLayout.Tab tab3 = tabLayout.getTabAt(3);
        View view3 = tab3.getCustomView();
        ImageView imageView3 = view3.findViewById(R.id.image);
        imageView3.setColorFilter(ContextCompat.getColor(context, R.color.darkgray), android.graphics.PorterDuff.Mode.SRC_IN);
        TextView tex3 = view3.findViewById(R.id.text);
        tex3.setTextColor(context.getResources().getColor(R.color.darkgray));
        tab3.setCustomView(view3);


        TabLayout.Tab tab4 = tabLayout.getTabAt(4);
        View view4 = tab4.getCustomView();
        ImageView imageView4 = view4.findViewById(R.id.image);
        imageView4.setColorFilter(ContextCompat.getColor(context, R.color.darkgray), android.graphics.PorterDuff.Mode.SRC_IN);
        TextView tex4 = view4.findViewById(R.id.text);
        tex4.setTextColor(context.getResources().getColor(R.color.darkgray));
        tab4.setCustomView(view4);

        tabLayout.setBackgroundColor(getResources().getColor(R.color.white));

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getActivity().getWindow().setNavigationBarColor(ContextCompat.getColor(context, R.color.white));
        }

    }


    // open the chat fragment when click on notification of message
    public void chatFragment(String receiverid, String name, String picture) {

        Intent intent=new Intent(context,ChatA.class);
        intent.putExtra("user_id", receiverid);
        intent.putExtra("user_name", name);
        intent.putExtra("user_pic", picture);
        resultChatCallback.launch(intent);
        getActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
    }


    ActivityResultLauncher<Intent> resultChatCallback = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data.getBooleanExtra("isShow",false))
                        {

                        }
                    }
                }
            });






    private ActivityResultLauncher<String[]> mPermissionResult = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onActivityResult(Map<String, Boolean> result) {

                    boolean allPermissionClear=true;
                    List<String> blockPermissionCheck=new ArrayList<>();
                    for (String key : result.keySet())
                    {
                        if (!(result.get(key)))
                        {
                            allPermissionClear=false;
                            blockPermissionCheck.add(Functions.getPermissionStatus(getActivity(),key));
                        }
                    }
                    if (blockPermissionCheck.contains("blocked"))
                    {
                        Functions.showPermissionSetting(context,context.getString(R.string.we_need_storage_camera_recording_permission_for_make_new_video));
                    }
                    else
                    if (allPermissionClear)
                    {
                        uploadNewVideo();
                    }

                }
            });



    @Override
    public void onDetach() {
        super.onDetach();
        mPermissionResult.unregister();

    }
}