package com.qboxus.musictok.ActivitesFragment.Profile;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;

import com.facebook.drawee.view.SimpleDraweeView;
import com.qboxus.musictok.ActivitesFragment.Accounts.LoginA;
import com.qboxus.musictok.ActivitesFragment.Accounts.ManageAccountsF;
import com.qboxus.musictok.ActivitesFragment.LiveStreaming.activities.StreamingMain_A;
import com.qboxus.musictok.ActivitesFragment.Profile.PrivateVideos.PrivateVideoF;
import com.qboxus.musictok.ActivitesFragment.WebviewA;
import com.qboxus.musictok.ApiClasses.ApiLinks;
import com.volley.plus.VPackages.VolleyRequest;
import com.volley.plus.interfaces.Callback;
import com.qboxus.musictok.Interfaces.FragmentCallBack;
import com.google.android.material.tabs.TabLayout;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qboxus.musictok.MainMenu.RelateToFragmentOnBack.RootFragment;
import com.qboxus.musictok.ActivitesFragment.Profile.LikedVideos.LikedVideoF;
import com.qboxus.musictok.ActivitesFragment.Profile.UserVideos.UserVideoF;
import com.qboxus.musictok.Models.PrivacyPolicySettingModel;
import com.qboxus.musictok.Models.PushNotificationSettingModel;
import com.qboxus.musictok.Models.UserModel;
import com.qboxus.musictok.R;
import com.qboxus.musictok.SimpleClasses.DataParsing;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.PermissionUtils;
import com.qboxus.musictok.SimpleClasses.Variables;

import org.json.JSONObject;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.paperdb.Paper;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileTabF extends RootFragment implements View.OnClickListener {
    View view;
    Context context;
    private TextView username, username2Txt,tvBio,tvLink,tvEditProfile;
    private SimpleDraweeView imageView;
    private TextView followCountTxt, fansCountTxt, heartCountTxt;
    String totalLikes="";
    private LinearLayout tabAccount,tabLink;
    ImageView settingBtn,favBtn;


    protected TabLayout tabLayout;

    protected ViewPager pager;

    private ViewPagerAdapter adapter;
    private String picUrl,followerCount,followingCount;
    private LinearLayout createPopupLayout,tabPrivacyLikes;
    private int myvideoCount = 0;

    PushNotificationSettingModel pushNotificationSettingModel;
    PrivacyPolicySettingModel privacyPolicySettingModel;


    PermissionUtils takePermissionUtils;

    public ProfileTabF() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_profile_tab, container, false);
        context = getContext();


        return init();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.live_btn:
            {
                if (takePermissionUtils.isCameraRecordingPermissionGranted())
                {
                    goLive();
                }
                else
                {
                    takePermissionUtils.showCameraRecordingPermissionDailog(getString(R.string.we_need_camera_and_recording_permission_for_live_streaming));
                }

            }
                break;


            case R.id.user_image:
                openProfileShareTab();
                break;

            case R.id.edit_profile_btn:
                openEditProfile();
                break;
            case R.id.message_btn:
            {
                Intent intent=new Intent(view.getContext(),SettingAndPrivacyA.class);
                startActivity(intent);
            }
                break;
            case R.id.tabLink:
                openWebUrl(view.getContext().getString(R.string.web_browser),""+tvLink.getText().toString());
                break;

            case R.id.tabAccount:
                openManageMultipleAccounts();
                break;

            case R.id.following_layout:
                openFollowing();
                break;

            case R.id.fans_layout:
                openFollowers();
                break;
            case R.id.invite_btn:
                openInviteFriends();
                break;
            case R.id.favBtn:
            openFavouriteVideos();
            break;

            case R.id.tabPrivacyLikes:
                showMyLikesCounts();
                break;

//            case R.id.draft_btn:
//                Intent upload_intent = new Intent(getActivity(), DraftVideos_A.class);
//                startActivity(upload_intent);
//                getActivity().overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top);
//                break;

        }
    }

    private void goLive() {
        String user_id = Functions.getSharedPreference(context).getString(Variables.U_ID, "");
        String user_name = Functions.getSharedPreference(context).getString(Variables.F_NAME, "") + " " +
                Functions.getSharedPreference(context).getString(Variables.L_NAME, "");
        String user_image = Functions.getSharedPreference(context).getString(Variables.U_PIC, "");
        openTicticLive(user_id, user_name, user_image, io.agora.rtc.Constants.CLIENT_ROLE_BROADCASTER);
    }

    private void openInviteFriends() {
        Intent intent=new Intent(view.getContext(), InviteFriendsA.class);
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
    }

    private void openProfileShareTab() {

        final ShareAndViewProfileF fragment = new ShareAndViewProfileF(Functions.getSharedPreference(view.getContext()).getString(Variables.U_ID,"")
                ,true,picUrl, new FragmentCallBack() {
            @Override
            public void onResponce(Bundle bundle) {
                if (bundle.getString("action").equals("profileShareMessage")) {
                    if (Functions.checkLoginUser(getActivity()))
                    {
                        // firebase sharing
                    }
                }

            }
        });
        fragment.show(getChildFragmentManager(), "");

    }


    private void showMyLikesCounts() {
        final Dialog dialog = new Dialog(context);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.show_likes_alert_popup_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        final TextView tvMessage,tvDone;
        tvDone=dialog.findViewById(R.id.tvDone);
        tvMessage=dialog.findViewById(R.id.tvMessage);

        tvMessage.setText(username.getText()+" "+view.getContext().getString(R.string.received_a_total_of)+" "+totalLikes+" "+view.getContext().getString(R.string.likes_across_all_video));
        tvDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public void openWebUrl(String title, String url) {
        Intent intent=new Intent(view.getContext(), WebviewA.class);
        intent.putExtra("url", url);
        intent.putExtra("title", title);
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
    }



    private void openManageMultipleAccounts() {
        ManageAccountsF f = new ManageAccountsF(new FragmentCallBack() {
            @Override
            public void onResponce(Bundle bundle) {
                if (bundle.getBoolean("isShow",false))
                {
                    Functions.hideSoftKeyboard(getActivity());
                    Intent intent = new Intent(getActivity(), LoginA.class);
                    startActivity(intent);
                    getActivity().overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top);
                }
            }
        });
        f.show(getChildFragmentManager(), "");
    }




    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        if (visible) {
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (Functions.getSharedPreference(context).getBoolean(Variables.IS_LOGIN, false)) {
                        updateProfile();
                        callApiForGetAllvideos();
                    }
                }
            }, 200);
        }

    }


    @Override
    public void onResume() {
        super.onResume();
        showDraftCount();

    }

    private View init() {

        takePermissionUtils=new PermissionUtils(getActivity(),mPermissionResult);
        view.findViewById(R.id.live_btn).setOnClickListener(this::onClick);

        username = view.findViewById(R.id.username);
        username2Txt = view.findViewById(R.id.username2_txt);
        tvLink=view.findViewById(R.id.tvLink);
        tvBio=view.findViewById(R.id.tvBio);
        imageView = view.findViewById(R.id.user_image);
        imageView.setOnClickListener(this);

        followCountTxt = view.findViewById(R.id.follow_count_txt);
        fansCountTxt = view.findViewById(R.id.fan_count_txt);
        heartCountTxt = view.findViewById(R.id.heart_count_txt);

        showDraftCount();

        tvEditProfile=view.findViewById(R.id.edit_profile_btn);
        tvEditProfile.setOnClickListener(this);

        tabAccount=view.findViewById(R.id.tabAccount);
        tabAccount.setOnClickListener(this);

        tabLink=view.findViewById(R.id.tabLink);
        tabLink.setOnClickListener(this);

        settingBtn = view.findViewById(R.id.message_btn);
        settingBtn.setOnClickListener(this);

        favBtn=view.findViewById(R.id.favBtn);
        favBtn.setOnClickListener(this);

        tabPrivacyLikes=view.findViewById(R.id.tabPrivacyLikes);
        tabPrivacyLikes.setOnClickListener(this);

        tabLayout = (TabLayout) view.findViewById(R.id.tabs);
        pager = view.findViewById(R.id.pager);
        pager.setOffscreenPageLimit(3);

        adapter = new ViewPagerAdapter(getChildFragmentManager());
        pager.setAdapter(adapter);
        tabLayout.setupWithViewPager(pager);

        setupTabIcons();


        createPopupLayout = view.findViewById(R.id.create_popup_layout);


        view.findViewById(R.id.following_layout).setOnClickListener(this);
        view.findViewById(R.id.fans_layout).setOnClickListener(this);
        view.findViewById(R.id.invite_btn).setOnClickListener(this);
        return view;
    }

    public void showDraftCount() {
        try {

            String path = Functions.getAppFolder(getActivity())+Variables.DRAFT_APP_FOLDER;
            File directory = new File(path);
            File[] files = directory.listFiles();
            if (files.length <= 0) {
                //draf gone
            } else {
               //draf visible
            }
        } catch (Exception e) {

        }
    }


    // place the profile data
    private void updateProfile() {
        username2Txt.setText(Functions.showUsername(Functions.getSharedPreference(context).getString(Variables.U_NAME, "")));
        String firstName = Functions.getSharedPreference(context).getString(Variables.F_NAME, "");
        String lastName = Functions.getSharedPreference(context).getString(Variables.L_NAME, "");
        if (firstName.equalsIgnoreCase("") && lastName.equalsIgnoreCase("")) {
            username.setText(Functions.getSharedPreference(context).getString(Variables.U_NAME, ""));
        } else {
            username.setText(firstName + " " + lastName);
        }


        if (TextUtils.isEmpty(Functions.getSharedPreference(context).getString(Variables.U_BIO, "")))
        {
            tvBio.setVisibility(View.GONE);
        }
        else
        {
            tvBio.setVisibility(View.VISIBLE);
            tvBio.setText(Functions.getSharedPreference(context).getString(Variables.U_BIO, ""));
        }

        if (TextUtils.isEmpty(Functions.getSharedPreference(context).getString(Variables.U_LINK, "")))
        {
            tabLink.setVisibility(View.GONE);
        }
        else
        {
            tabLink.setVisibility(View.VISIBLE);
            tvLink.setText(Functions.getSharedPreference(context).getString(Variables.U_LINK, ""));
        }

        picUrl = Functions.getSharedPreference(context).getString(Variables.U_PIC, "null");

        imageView.setController(Functions.frescoImageLoad(picUrl,imageView,false));

    }


    // change the icons of the tab
    private void setupTabIcons() {

        View view1 = LayoutInflater.from(context).inflate(R.layout.item_tabs_profile_menu, null);
        ImageView imageView1 = view1.findViewById(R.id.image);
        imageView1.setImageDrawable(getResources().getDrawable(R.drawable.ic_my_video_color));
        tabLayout.getTabAt(0).setCustomView(view1);

        View view2 = LayoutInflater.from(context).inflate(R.layout.item_tabs_profile_menu, null);
        ImageView imageView2 = view2.findViewById(R.id.image);
        imageView2.setImageDrawable(getResources().getDrawable(R.drawable.ic_liked_video_gray));
        tabLayout.getTabAt(1).setCustomView(view2);

        View view3 = LayoutInflater.from(context).inflate(R.layout.item_tabs_profile_menu, null);
        ImageView imageView3 = view3.findViewById(R.id.image);
        imageView3.setImageDrawable(getResources().getDrawable(R.drawable.ic_lock_gray));
        tabLayout.getTabAt(2).setCustomView(view3);


        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {


            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                View v = tab.getCustomView();
                ImageView image = v.findViewById(R.id.image);

                switch (tab.getPosition()) {
                    case 0:

                        if (myvideoCount > 0) {
                            createPopupLayout.setVisibility(View.GONE);
                        } else {
                            createPopupLayout.setVisibility(View.VISIBLE);
                            Animation aniRotate = AnimationUtils.loadAnimation(context, R.anim.up_and_down_animation);
                            createPopupLayout.startAnimation(aniRotate);
                        }

                        image.setImageDrawable(getResources().getDrawable(R.drawable.ic_my_video_color));
                        break;

                    case 1:
                        createPopupLayout.clearAnimation();
                        createPopupLayout.setVisibility(View.GONE);
                        image.setImageDrawable(getResources().getDrawable(R.drawable.ic_liked_video_color));
                        break;

                    case 2:
                        createPopupLayout.clearAnimation();
                        createPopupLayout.setVisibility(View.GONE);
                        image.setImageDrawable(getResources().getDrawable(R.drawable.ic_lock_black));
                        break;
                }
                tab.setCustomView(v);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                View v = tab.getCustomView();
                ImageView image = v.findViewById(R.id.image);

                switch (tab.getPosition()) {
                    case 0:
                        image.setImageDrawable(getResources().getDrawable(R.drawable.ic_my_video_gray));
                        break;
                    case 1:
                        image.setImageDrawable(getResources().getDrawable(R.drawable.ic_liked_video_gray));
                        break;

                    case 2:
                        image.setImageDrawable(getResources().getDrawable(R.drawable.ic_lock_gray));
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


        public ViewPagerAdapter(FragmentManager fm) {
            super(fm, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @Override
        public Fragment getItem(int position) {
            final Fragment result;
            switch (position) {
                case 0:
                    result = new UserVideoF(true, Functions.getSharedPreference(context).getString(Variables.U_ID, ""),Functions.getSharedPreference(context).getString(Variables.U_NAME, ""),"");
                    break;
                case 1:
                    result = new LikedVideoF(true, Functions.getSharedPreference(context).getString(Variables.U_ID, ""),Functions.getSharedPreference(context).getString(Variables.U_NAME, ""),true,"");
                    break;

                case 2:
                    result = new PrivateVideoF();
                    break;

                default:
                    result = null;
                    break;
            }

            return result;
        }

        @Override
        public int getCount() {
            return 3;
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


        /**
         * Get the Fragment by position
         *
         * @param position tab position of the fragment
         * @return
         */
        public Fragment getRegisteredFragment(int position) {
            return registeredFragments.get(position);
        }


    }


    //this will get the all videos data of user and then parse the data
    private void callApiForGetAllvideos() {

        JSONObject parameters = new JSONObject();
        try {
            parameters.put("user_id", Functions.getSharedPreference(context).getString(Variables.U_ID, ""));

        } catch (Exception e) {
            e.printStackTrace();
        }

        VolleyRequest.JsonPostRequest(getActivity(), ApiLinks.showUserDetail, parameters,Functions.getHeaders(getActivity()), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.checkStatus(getActivity(),resp);
                parseData(resp);
            }
        });


    }

    public void parseData(String responce) {


        try {
            JSONObject jsonObject = new JSONObject(responce);
            String code = jsonObject.optString("code");
            if (code.equals("200")) {


                JSONObject msg = jsonObject.optJSONObject("msg");
                JSONObject pushNotificationSetting = msg.optJSONObject("PushNotification");
                JSONObject privacyPolicySetting = msg.optJSONObject("PrivacySetting");

                UserModel userDetailModel= DataParsing.getUserDataModel(msg.optJSONObject("User"));

                pushNotificationSettingModel = new PushNotificationSettingModel();
                if (pushNotificationSetting != null) {
                    pushNotificationSettingModel.setComments("" + pushNotificationSetting.optString("comments"));
                    pushNotificationSettingModel.setLikes("" + pushNotificationSetting.optString("likes"));
                    pushNotificationSettingModel.setNewfollowers("" + pushNotificationSetting.optString("new_followers"));
                    pushNotificationSettingModel.setMentions("" + pushNotificationSetting.optString("mentions"));
                    pushNotificationSettingModel.setDirectmessage("" + pushNotificationSetting.optString("direct_messages"));
                    pushNotificationSettingModel.setVideoupdates("" + pushNotificationSetting.optString("video_updates"));
                }

                privacyPolicySettingModel = new PrivacyPolicySettingModel();
                if (privacyPolicySetting != null) {
                    privacyPolicySettingModel.setVideos_download("" + privacyPolicySetting.optString("videos_download"));
                    privacyPolicySettingModel.setDirect_message("" + privacyPolicySetting.optString("direct_message"));
                    privacyPolicySettingModel.setDuet("" + privacyPolicySetting.optString("duet"));
                    privacyPolicySettingModel.setLiked_videos("" + privacyPolicySetting.optString("liked_videos"));
                    privacyPolicySettingModel.setVideo_comment("" + privacyPolicySetting.optString("video_comment"));
                }

                Paper.book(Variables.PrivacySetting).write(Variables.PushSettingModel, pushNotificationSettingModel);
                Paper.book(Variables.PrivacySetting).write(Variables.PrivacySettingModel, privacyPolicySettingModel);

                username2Txt.setText(Functions.showUsername(userDetailModel.getUsername()));


                if (TextUtils.isEmpty(userDetailModel.getBio()))
                {
                    tvBio.setVisibility(View.GONE);
                }
                else
                {
                    tvBio.setVisibility(View.VISIBLE);
                    tvBio.setText(userDetailModel.getBio());
                }

                if (TextUtils.isEmpty(userDetailModel.getWebsite()))
                {
                    tabLink.setVisibility(View.GONE);
                }
                else
                {
                    tabLink.setVisibility(View.VISIBLE);
                    tvLink.setText(userDetailModel.getWebsite());
                }

                String firstName = userDetailModel.getFirstName();
                String lastName = userDetailModel.getLastName();

                if (firstName.equalsIgnoreCase("") && lastName.equalsIgnoreCase("")) {
                    username.setText(userDetailModel.getUsername());
                } else {
                    username.setText(firstName + " " + lastName);
                }

                picUrl = userDetailModel.getProfilePic();

                imageView.setController(Functions.frescoImageLoad(picUrl,imageView,false));

                SharedPreferences.Editor editor = Functions.getSharedPreference(view.getContext()).edit();
                editor.putString(Variables.U_PIC,picUrl);
                editor.putString(Variables.U_WALLET, ""+userDetailModel.getWallet());
                editor.commit();

                followingCount=userDetailModel.getFollowingCount();
                followerCount=userDetailModel.getFollowersCount();
                totalLikes=userDetailModel.getLikesCount();
                followCountTxt.setText(Functions.getSuffix(followingCount));
                fansCountTxt.setText(Functions.getSuffix(followerCount));
                heartCountTxt.setText(Functions.getSuffix(totalLikes));

                myvideoCount = Functions.parseInterger(userDetailModel.getVideoCount());

                if (myvideoCount != 0) {
                    createPopupLayout.setVisibility(View.GONE);
                    createPopupLayout.clearAnimation();
                } else {

                    createPopupLayout.setVisibility(View.VISIBLE);
                    Animation aniRotate = AnimationUtils.loadAnimation(context, R.anim.up_and_down_animation);
                    createPopupLayout.startAnimation(aniRotate);

                }

                String verified = userDetailModel.getVerified();
                if (verified != null && verified.equalsIgnoreCase("1")) {
                    view.findViewById(R.id.varified_btn).setVisibility(View.VISIBLE);
                }


            } else {
                Functions.showToast(getActivity(), jsonObject.optString("msg"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private void openEditProfile() {
        Intent intent=new Intent(view.getContext(),EditProfileA.class);
        resultCallback.launch(intent);
        getActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
    }

    ActivityResultLauncher<Intent> resultCallback = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data.getBooleanExtra("isShow",false))
                        {
                            updateProfile();
                        }

                    }
                }
            });


    // open the favourite videos fragment
    private void openFavouriteVideos() {
        Intent intent=new Intent(view.getContext(), FavouriteMainA.class);
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    // open the following fragment
    private void openFollowing() {
        Intent intent=new Intent(view.getContext(), FollowsMainTabA.class);
        intent.putExtra("id", Functions.getSharedPreference(context).getString(Variables.U_ID, ""));
        intent.putExtra("from_where", "following");
        intent.putExtra("userName", Functions.getSharedPreference(context).getString(Variables.U_NAME, ""));
        intent.putExtra("followingCount",""+followingCount);
        intent.putExtra("followerCount",""+followerCount);
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);


    }


    // open the followers fragment
    private void openFollowers() {
        Intent intent=new Intent(view.getContext(), FollowsMainTabA.class);
        intent.putExtra("id", Functions.getSharedPreference(context).getString(Variables.U_ID, ""));
        intent.putExtra("from_where", "fan");
        intent.putExtra("userName", Functions.getSharedPreference(context).getString(Variables.U_NAME, ""));
        intent.putExtra("followingCount",""+followingCount);
        intent.putExtra("followerCount",""+followerCount);
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    // this will erase all the user info store in locally and logout the user



    // open the live streaming
    public void openTicticLive(String user_id, String user_name, String user_image, int role) {
        Intent intent = new Intent(getActivity(), StreamingMain_A.class);
        intent.putExtra("user_id", user_id);
        intent.putExtra("user_name", user_name);
        intent.putExtra("user_picture", user_image);
        intent.putExtra("user_role", role);
        startActivity(intent);
    }



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
                        Functions.showPermissionSetting(view.getContext(),getString(R.string.we_need_camera_and_recording_permission_for_live_streaming));
                    }
                    else
                    if (allPermissionClear)
                    {
                        goLive();
                    }

                }
            });



    @Override
    public void onDetach() {
        super.onDetach();
        mPermissionResult.unregister();
    }


}
