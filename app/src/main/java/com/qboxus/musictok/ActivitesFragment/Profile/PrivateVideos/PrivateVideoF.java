package com.qboxus.musictok.ActivitesFragment.Profile.PrivateVideos;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
public class PrivateVideoF extends Fragment {

    public RecyclerView recyclerView;
    ArrayList<HomeModel> dataList;
    MyVideosAdapter adapter;
    View view;
    Context context;
    RelativeLayout noDataLayout;
    TextView tvTitleNoData,tvMessageNoData;
    NewVideoBroadCast mReceiver;


    int pageCount = 0;
    boolean ispostFinsh;
    ProgressBar loadMoreProgress;
    GridLayoutManager linearLayoutManager;


    public PrivateVideoF() {

    }



    private class NewVideoBroadCast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            Variables.reloadMyVideosInner = false;
            pageCount = 0;
            callApiForGetAllvideos();
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
                        callApiForGetAllvideos();
                    }
                }


            }
        });
        noDataLayout = view.findViewById(R.id.no_data_layout);

        tvTitleNoData=view.findViewById(R.id.tvTitleNoData);
        tvMessageNoData=view.findViewById(R.id.tvMessageNoData);
        tvTitleNoData.setVisibility(View.VISIBLE);
        tvMessageNoData.setVisibility(View.VISIBLE);
        tvTitleNoData.setText(view.getContext().getString(R.string.your_private_video));
        tvMessageNoData.setText(view.getContext().getString(R.string.to_make_your_video_visible_to_yourself_only));




        mReceiver = new NewVideoBroadCast();
        getActivity().registerReceiver(mReceiver, new IntentFilter("newVideo"));

        return view;

    }

    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        if (visible)
        {
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    pageCount = 0;
                    callApiForGetAllvideos();
                }
            },200);
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
    private void callApiForGetAllvideos() {
        isApiRun = true;
        if (dataList == null)
            dataList = new ArrayList<>();

        JSONObject parameters = new JSONObject();
        try {
            parameters.put("user_id", Functions.getSharedPreference(context).getString(Variables.U_ID, ""));
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

    // parse the video list data
    public void parseData(String responce) {

        try {
            JSONObject jsonObject = new JSONObject(responce);
            String code = jsonObject.optString("code");
            if (code.equals("200")) {
                JSONObject msg = jsonObject.optJSONObject("msg");
                JSONArray public_array = msg.optJSONArray("private");

                ArrayList<HomeModel> temp_list = new ArrayList<>();

                for (int i = 0; i < public_array.length(); i++) {
                    JSONObject itemdata = public_array.optJSONObject(i);

                    JSONObject video = itemdata.optJSONObject("Video");
                    JSONObject user = itemdata.optJSONObject("User");
                    JSONObject sound = itemdata.optJSONObject("Sound");
                    JSONObject userPrivacy = user.optJSONObject("PrivacySetting");
                    JSONObject userPushNotification = user.optJSONObject("PushNotification");

                    HomeModel item = Functions.parseVideoData(user, sound, video, userPrivacy, userPushNotification);

                    if (item.username!=null && !(item.username.equals("null")))
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


    // open the video in full screen
    private void openWatchVideo(int postion) {

        Intent intent = new Intent(getActivity(), WatchVideosA.class);
        intent.putExtra("arraylist", dataList);
        intent.putExtra("position", postion);
        intent.putExtra("pageCount", pageCount);
        intent.putExtra("userId",Functions.getSharedPreference(context).getString(Variables.U_ID, ""));
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
