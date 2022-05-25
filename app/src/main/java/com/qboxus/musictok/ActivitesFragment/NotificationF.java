package com.qboxus.musictok.ActivitesFragment;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.qboxus.musictok.ActivitesFragment.Profile.ProfileA;
import com.qboxus.musictok.Adapters.NotificationAdapter;
import com.qboxus.musictok.Constants;
import com.qboxus.musictok.MainMenu.MainMenuFragment;
import com.qboxus.musictok.MainMenu.RelateToFragmentOnBack.RootFragment;
import com.qboxus.musictok.Models.NotificationModel;
import com.qboxus.musictok.R;
import com.qboxus.musictok.ApiClasses.ApiLinks;
import com.volley.plus.VPackages.VolleyRequest;
import com.volley.plus.interfaces.Callback;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.Variables;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class NotificationF extends RootFragment implements View.OnClickListener {

    View view;
    Context context;

    NotificationAdapter adapter;
    RecyclerView recyclerView;
    ArrayList<NotificationModel> datalist;
    SwipeRefreshLayout swiperefresh;

    LinearLayout dataContainer;
    ShimmerFrameLayout shimmerFrameLayout;

    int pageCount = 0;
    boolean ispostFinsh;

    ProgressBar loadMoreProgress;
    LinearLayoutManager linearLayoutManager;

    boolean isApiCall=false;

    public NotificationF() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_notification, container, false);
        context = getContext();


        datalist = new ArrayList<>();

        shimmerFrameLayout = view.findViewById(R.id.shimmer_view_container);
        dataContainer=view.findViewById(R.id.dataContainer);
        recyclerView = (RecyclerView) view.findViewById(R.id.recylerview);
        linearLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);


        adapter = new NotificationAdapter(context, datalist, new NotificationAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int postion, NotificationModel item) {

                switch (view.getId()) {
                    case R.id.watch_btn:
                        if (item.type.equals("live")) {
                            openLivedUser();
                        } else
                            openWatchVideo(item);
                        break;
                    default:
                        openProfile(item);
                        break;
                }
            }
        });
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            boolean userScrolled;
            int scrollOutitems;

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

                scrollOutitems = linearLayoutManager.findLastVisibleItemPosition();

                Functions.printLog("resp", "" + scrollOutitems);
                if (userScrolled && (scrollOutitems == datalist.size() - 1)) {
                    userScrolled = false;

                    if (loadMoreProgress.getVisibility() != View.VISIBLE && !ispostFinsh) {
                        loadMoreProgress.setVisibility(View.VISIBLE);
                        pageCount = pageCount + 1;
                        callApi();
                    }
                }


            }
        });

        loadMoreProgress = view.findViewById(R.id.load_more_progress);


        view.findViewById(R.id.inbox_btn).setOnClickListener(this);
        swiperefresh = view.findViewById(R.id.swiperefresh);
        swiperefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (datalist.size()<1)
                {
                    dataContainer.setVisibility(View.GONE);
                    shimmerFrameLayout.setVisibility(View.VISIBLE);
                    shimmerFrameLayout.startShimmer();
                }
                pageCount = 0;
                callApi();
            }
        });


        return view;
    }


    // load the banner and show on below of the screen
    AdView adView;

    @Override
    public void onStart() {
        super.onStart();
        adView = view.findViewById(R.id.bannerad);
        if (!Constants.IS_REMOVE_ADS) {
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
        } else {
            adView.setVisibility(View.GONE);
        }

    }


    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        if (((pageCount == 0 && visible)) || Variables.reloadMyNotification) {
           new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
               @Override
               public void run() {
                   Variables.reloadMyNotification = false;

                   if (datalist.size()<1)
                   {
                       dataContainer.setVisibility(View.GONE);
                       shimmerFrameLayout.setVisibility(View.VISIBLE);
                       shimmerFrameLayout.startShimmer();
                   }

                   pageCount = 0;
                   callApi();
               }
           },200);
        }
    }


    // get the all notification from the server against the profile id
    public void callApi() {
        if (isApiCall)
        {
            return;
        }
        isApiCall=true;

        if (datalist == null)
            datalist = new ArrayList<>();

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("user_id", Functions.getSharedPreference(getContext()).getString(Variables.U_ID, "0"));
            jsonObject.put("starting_point", "" + pageCount);


        } catch (Exception e) {
            e.printStackTrace();
        }

        VolleyRequest.JsonPostRequest(getActivity(), ApiLinks.showAllNotifications, jsonObject,Functions.getHeaders(getActivity()), new Callback() {
            @Override
            public void onResponce(String resp) {
                isApiCall=false;
                shimmerFrameLayout.stopShimmer();
                shimmerFrameLayout.setVisibility(View.GONE);
                dataContainer.setVisibility(View.VISIBLE);
                swiperefresh.setRefreshing(false);
                parseData(resp);
            }
        });

    }


    // parse the data of the notification and place then on data model list
    public void parseData(String resp) {
        try {
            JSONObject jsonObject = new JSONObject(resp);
            String code = jsonObject.optString("code");
            if (code.equals("200")) {
                JSONArray msg = jsonObject.getJSONArray("msg");
                ArrayList<NotificationModel> temp_list = new ArrayList<>();

                for (int i = 0; i < msg.length(); i++) {
                    JSONObject data = msg.getJSONObject(i);

                    JSONObject notification = data.optJSONObject("Notification");
                    JSONObject video = data.optJSONObject("Video");
                    JSONObject sender = data.optJSONObject("Sender");
                    JSONObject receiver = data.optJSONObject("Receiver");

                    NotificationModel item = new NotificationModel();

                    item.id = notification.optString("id");

                    item.user_id = sender.optString("id");
                    item.username = sender.optString("username");
                    item.first_name = sender.optString("first_name");
                    item.last_name = sender.optString("last_name");

                    item.profile_pic = sender.optString("profile_pic", "");
                    if (!item.profile_pic.contains(Variables.http)) {
                        item.profile_pic = Constants.BASE_URL + item.profile_pic;
                    }

                    item.effected_fb_id = receiver.optString("id");

                    item.type = notification.optString("type");

                    if (item.type.equalsIgnoreCase("video_comment") || item.type.equalsIgnoreCase("video_like")) {

                        item.video_id = video.optString("id");
                        item.video = video.optString("video");
                        item.thum = video.optString("thum");
                        item.gif = video.optString("gif");

                    }

                    item.string = notification.optString("string");
                    item.created = notification.optString("created");

                    temp_list.add(item);


                }

                if (pageCount == 0) {
                    datalist.clear();
                    datalist.addAll(temp_list);
                } else {
                    datalist.addAll(temp_list);
                }

                adapter.notifyDataSetChanged();

            }

            if (datalist.isEmpty()) {
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


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.inbox_btn:
                openInboxF();
                break;
        }
    }


    private void openInboxF() {
        Intent intent=new Intent(view.getContext(),InboxA.class);
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top);
    }

    // open the broad cast live user streaming on notification receive
    private void openLivedUser() {
        Intent intent=new Intent(view.getContext(),LiveUsersA.class);
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
    }

    private void openWatchVideo(NotificationModel item) {
        Intent intent = new Intent(view.getContext(), WatchVideosA.class);
        intent.putExtra("video_id", item.video_id);
        intent.putExtra("position", 0);
        intent.putExtra("pageCount", 0);
        intent.putExtra("userId",Functions.getSharedPreference(view.getContext()).getString(Variables.U_ID,""));
        intent.putExtra("whereFrom","IdVideo");
        startActivity(intent);
    }


    // open the profile of the user which notification we have receive
    public void openProfile(NotificationModel item) {
        if (Functions.getSharedPreference(context).getString(Variables.U_ID, "0").equals(item.user_id)) {

            TabLayout.Tab profile = MainMenuFragment.tabLayout.getTabAt(4);
            profile.select();

        } else {

            Intent intent=new Intent(view.getContext(), ProfileA.class);
            intent.putExtra("user_id", item.user_id);
            intent.putExtra("user_name", item.username);
            intent.putExtra("user_pic", item.profile_pic);
            startActivity(intent);
            getActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);

        }

    }


}
