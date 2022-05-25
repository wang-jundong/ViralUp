package com.qboxus.musictok.ActivitesFragment.Profile.LikedVideos;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.qboxus.musictok.ActivitesFragment.Profile.SettingAndPrivacyA;
import com.qboxus.musictok.ActivitesFragment.WatchVideosA;
import com.qboxus.musictok.Models.HomeModel;
import com.qboxus.musictok.Adapters.MyVideosAdapter;
import com.qboxus.musictok.R;
import com.qboxus.musictok.Interfaces.AdapterClickListener;
import com.qboxus.musictok.ApiClasses.ApiLinks;
import com.volley.plus.VPackages.VolleyRequest;
import com.volley.plus.interfaces.Callback;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.Variables;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class LikedVideoF extends Fragment {

    public RecyclerView recyclerView;
    ArrayList<HomeModel> dataList;
    MyVideosAdapter adapter;

    View view;
    Context context;
    TextView tvTitleNoData,tvMessageNoData;
    String userId,userName;
    RelativeLayout noDataLayout;


    int pageCount = 0;
    boolean ispostFinsh;
    ProgressBar loadMoreProgress;
    GridLayoutManager linearLayoutManager;


    public LikedVideoF() {
        // Required empty public constructor
    }

    boolean isMyProfile = true;
    boolean isLikeVideoShow=false;
    String isUserAlreadyBlock;

    public void updateLikeVideoState(boolean isLikeVideoShow)
    {
        this.isLikeVideoShow=isLikeVideoShow;
    }

    @SuppressLint("ValidFragment")
    public LikedVideoF(boolean isMyProfile, String userId, String userName, boolean isLikeVideoShow,String isUserAlreadyBlock) {
        this.userId = userId;
        this.userName=userName;
        this.isMyProfile = isMyProfile;
        this.isLikeVideoShow=isLikeVideoShow;
        this.isUserAlreadyBlock=isUserAlreadyBlock;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_user_likedvideo, container, false);

        context = getContext();
        dataList = new ArrayList<>();

        loadMoreProgress = view.findViewById(R.id.load_more_progress);
        recyclerView = view.findViewById(R.id.recylerview);
        linearLayoutManager = new GridLayoutManager(context, 3);
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new MyVideosAdapter(context, dataList, new AdapterClickListener() {
            @Override
            public void onItemClick(View view, int pos, Object object) {
                HomeModel item = (HomeModel) object;
                openWatchVideo(pos);
            }
        });

        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            boolean userScrolled;
            int scrollOutitems,scrollInItem;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    userScrolled = true;
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                scrollInItem=linearLayoutManager.findFirstVisibleItemPosition();
                scrollOutitems = linearLayoutManager.findLastVisibleItemPosition();

                if (scrollInItem == 0)
                {
                    recyclerView.setNestedScrollingEnabled(true);
                }
                else
                {
                    recyclerView.setNestedScrollingEnabled(false);
                }

                if (userScrolled && (scrollOutitems == dataList.size() - 1)) {
                    userScrolled = false;

                    if (loadMoreProgress.getVisibility() != View.VISIBLE && !ispostFinsh) {
                        loadMoreProgress.setVisibility(View.VISIBLE);
                        pageCount = pageCount + 1;
                        callApiLikedvideos();
                    }
                }


            }
        });

        noDataLayout = view.findViewById(R.id.no_data_layout);

        tvTitleNoData=view.findViewById(R.id.tvTitleNoData);
        tvMessageNoData=view.findViewById(R.id.tvMessageNoData);


        return view;
    }

    private void setNoData() {
        if (isMyProfile)
        {
            tvTitleNoData.setVisibility(View.VISIBLE);
            tvMessageNoData.setVisibility(View.VISIBLE);
            tvTitleNoData.setText(view.getContext().getString(R.string.only_you_can_see_which_video_you_liked));
            tvMessageNoData.setText(Html.fromHtml(view.getContext().getString(R.string.you_can_change_this_in)+ "  <font color='#c52127'> "+view.getContext().getString(R.string.privacy_setting)+" </font>"));

            tvMessageNoData.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openSettingScreen();
                }
            });
        }
        else
        {
            tvTitleNoData.setVisibility(View.VISIBLE);
            tvMessageNoData.setVisibility(View.VISIBLE);
            tvTitleNoData.setText(view.getContext().getString(R.string.this_user_liked_video_are_private));
            tvMessageNoData.setText(view.getContext().getString(R.string.videos_liked_by)+" "+userName+" "+view.getContext().getString(R.string.are_currently_hidden));
        }
    }

    private void openSettingScreen() {
        Intent intent=new Intent(view.getContext(), SettingAndPrivacyA.class);
        startActivity(intent);
    }

    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        if (visible)
        {
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (isLikeVideoShow)
                    {
                        pageCount = 0;
                        callApiLikedvideos();
                    }
                    else
                    {
                        if (dataList.isEmpty()) {
                            noDataLayout.setVisibility(View.VISIBLE);
                        } else
                            noDataLayout.setVisibility(View.GONE);
                    }
                }
            }, 200);
        }

    }

    public void updateUserData(String userId,String userName,String isUserAlreadyBlock)
    {
        pageCount = 0;
        this.userId=userId;
        this.userName=userName;
        this.isUserAlreadyBlock=isUserAlreadyBlock;
        callApiLikedvideos();
    }


    Boolean isApiRun = false;

    //this will get the all liked videos data of user and then parse the data
    private void callApiLikedvideos() {
        if (isUserAlreadyBlock.equalsIgnoreCase("1"))
        {
            tvTitleNoData.setText(view.getContext().getString(R.string.alert));
            tvMessageNoData.setText(view.getContext().getString(R.string.you_are_block_by)+" "+userName);
        }
        else
        {
            setNoData();
        }
        isApiRun = true;
        if (dataList == null)
            dataList = new ArrayList<>();

        JSONObject parameters = new JSONObject();
        try {
            parameters.put("user_id", Functions.getSharedPreference(context).getString(Variables.U_ID, ""));

            if (!isMyProfile) {
                parameters.put("other_user_id", userId);
            }
            parameters.put("starting_point", "" + pageCount);

        } catch (Exception e) {
            e.printStackTrace();
        }

        VolleyRequest.JsonPostRequest(getActivity(), ApiLinks.showUserLikedVideos, parameters,Functions.getHeaders(getActivity()), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.checkStatus(getActivity(),resp);
                isApiRun = false;
                parseData(resp);
            }
        });


    }


    // parse the video list data
    public void parseData(String responce) {

        try {
            JSONObject jsonObject = new JSONObject(responce);
            String code = jsonObject.optString("code");
            if (code.equals("200")) {
                JSONArray msgArray = jsonObject.getJSONArray("msg");
                ArrayList<HomeModel> temp_list = new ArrayList<>();


                for (int i = 0; i < msgArray.length(); i++) {
                    JSONObject itemdata = msgArray.optJSONObject(i);

                    JSONObject video = itemdata.optJSONObject("Video");
                    JSONObject user = video.optJSONObject("User");
                    JSONObject sound = video.optJSONObject("Sound");
                    JSONObject userPrivacy = user.optJSONObject("PrivacySetting");
                    JSONObject userPushNotification = user.optJSONObject("PushNotification");

                    HomeModel item = Functions.parseVideoData(user, sound, video, userPrivacy, userPushNotification);


                    if (!(isUserAlreadyBlock.equalsIgnoreCase("1")))
                    {
                        temp_list.add(item);
                    }


                }

                if (pageCount == 0) {
                    dataList.clear();
                    dataList.addAll(temp_list);
                } else {
                    dataList.addAll(temp_list);
                }

                adapter.notifyDataSetChanged();
            }
            else
            {
                if (pageCount==0)
                {
                    pageCount=0;
                    dataList.clear();
                    adapter.notifyDataSetChanged();
                }
            }

            if (dataList.isEmpty()) {
                view.findViewById(R.id.no_data_layout).setVisibility(View.VISIBLE);
            } else {
                view.findViewById(R.id.no_data_layout).setVisibility(View.GONE);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            loadMoreProgress.setVisibility(View.GONE);
        }
    }


    // open the videos in full screen
    private void openWatchVideo(int postion) {
        Intent intent = new Intent(getActivity(), WatchVideosA.class);
        intent.putExtra("arraylist", dataList);
        intent.putExtra("position", postion);
        intent.putExtra("pageCount", pageCount);
        intent.putExtra("userId",userId);
        intent.putExtra("whereFrom","userVideo");
        resultCallback.launch(intent);
    }

    ActivityResultLauncher<Intent> resultCallback = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data.getBooleanExtra("isShow",false))
                        {
                            dataList.clear();
                            dataList.addAll((ArrayList<HomeModel>) data.getSerializableExtra("arraylist"));
                            pageCount=data.getIntExtra("pageCount",0);
                            adapter.notifyDataSetChanged();
                        }

                    }
                }
            });



}
