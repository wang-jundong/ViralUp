package com.qboxus.musictok.ActivitesFragment.Profile.UserVideos;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.qboxus.musictok.ActivitesFragment.WatchVideosA;
import com.qboxus.musictok.Constants;
import com.qboxus.musictok.Models.HomeModel;
import com.qboxus.musictok.Adapters.MyVideosAdapter;
import com.qboxus.musictok.R;
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
public class UserVideoF extends Fragment {

    RecyclerView recyclerView;
    ArrayList<HomeModel> dataList;
    MyVideosAdapter adapter;
    View view;
    Context context;
    String userId="",userName="";
    TextView tvTitleNoData,tvMessageNoData;
    RelativeLayout noDataLayout;
    NewVideoBroadCast mReceiver;

    int pageCount = 0;
    boolean ispostFinsh;
    ProgressBar loadMoreProgress;
    GridLayoutManager linearLayoutManager;

    String isUserAlreadyBlock;

    public UserVideoF() {

    }

    boolean is_my_profile = true;

    @SuppressLint("ValidFragment")
    public UserVideoF(boolean is_my_profile, String userId, String userName,String isUserAlreadyBlock) {
        this.is_my_profile = is_my_profile;
        this.userId = userId;
        this.userName=userName;
        this.isUserAlreadyBlock=isUserAlreadyBlock;
    }


    private class NewVideoBroadCast extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            Variables.reloadMyVideosInner = false;
            pageCount = 0;
            callApiMyvideos();
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_user_video, container, false);

        context = getContext();

        loadMoreProgress = view.findViewById(R.id.load_more_progress);
        recyclerView = view.findViewById(R.id.recylerview);
        linearLayoutManager = new GridLayoutManager(context, 3);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);


        dataList = new ArrayList<>();
        adapter = new MyVideosAdapter(context, dataList, (view, pos, object) -> {
            HomeModel item = (HomeModel) object;
            openWatchVideo(pos);

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
                        callApiMyvideos();
                    }
                }


            }
        });

        noDataLayout = view.findViewById(R.id.no_data_layout);

        tvTitleNoData=view.findViewById(R.id.tvTitleNoData);
        tvMessageNoData=view.findViewById(R.id.tvMessageNoData);
        Log.d(Constants.tag,"Exception : "+userId);
        Log.d(Constants.tag,"Exception : "+Functions.getSharedPreference(context).getString(Variables.U_ID, ""));

        mReceiver = new NewVideoBroadCast();
        getActivity().registerReceiver(mReceiver, new IntentFilter("newVideo"));

        return view;

    }

    private void setNoData() {
        if (is_my_profile)
        {
            tvTitleNoData.setVisibility(View.GONE);
            tvMessageNoData.setVisibility(View.GONE);
        }
        else
        {
            tvTitleNoData.setVisibility(View.GONE);
            tvMessageNoData.setVisibility(View.VISIBLE);
            tvMessageNoData.setText(view.getContext().getString(R.string.this_user_has_not_publish_any_video));
        }
    }

    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        if (visible) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                pageCount = 0;
                callApiMyvideos();
            }, 200);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mReceiver != null) {
            getActivity().unregisterReceiver(mReceiver);
            mReceiver = null;
        }
    }

    Boolean isApiRun = false;

    //this will get the all videos data of user and then parse the data
    private void callApiMyvideos() {
        if (isUserAlreadyBlock.equalsIgnoreCase("1"))
        {
            tvTitleNoData.setText(view.getContext().getString(R.string.alert));
            tvMessageNoData.setText(view.getContext().getString(R.string.you_are_block_by)+" "+userName);
        }
        else
        {
            setNoData();
        }
        if (dataList == null)
            dataList = new ArrayList<>();

        isApiRun = true;
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("user_id", Functions.getSharedPreference(context).getString(Variables.U_ID, ""));

            if (!is_my_profile) {
                parameters.put("other_user_id", userId);
            }
            parameters.put("starting_point", "" + pageCount);

        } catch (Exception e) {
            e.printStackTrace();
        }

        VolleyRequest.JsonPostRequest(getActivity(), ApiLinks.showVideosAgainstUserID, parameters,Functions.getHeaders(getActivity()), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.checkStatus(getActivity(),resp);
                isApiRun = false;
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
                ArrayList<HomeModel> temp_list = new ArrayList<>();


                JSONArray public_array = msg.optJSONArray("public");


                for (int i = 0; i < public_array.length(); i++) {
                    JSONObject itemdata = public_array.optJSONObject(i);

                    JSONObject video = itemdata.optJSONObject("Video");
                    JSONObject user = itemdata.optJSONObject("User");
                    JSONObject sound = itemdata.optJSONObject("Sound");
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

    public void updateUserData(String userId,String userName,String isUserAlreadyBlock)
    {
        pageCount = 0;
        this.userId=userId;
        this.userName=userName;
        this.isUserAlreadyBlock=isUserAlreadyBlock;
        callApiMyvideos();
    }


    // open the videos in full screen on click
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
                            Log.d(Constants.tag,"notify data : "+dataList.size());
                            dataList.clear();
                            dataList.addAll((ArrayList<HomeModel>) data.getSerializableExtra("arraylist"));
                            pageCount=data.getIntExtra("pageCount",0);
                            adapter.notifyDataSetChanged();

                            Log.d(Constants.tag,"notify data : "+dataList.size());
                        }
                    }
                }
            });


}
