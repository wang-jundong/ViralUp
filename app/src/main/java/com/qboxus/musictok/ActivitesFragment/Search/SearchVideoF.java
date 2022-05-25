package com.qboxus.musictok.ActivitesFragment.Search;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.qboxus.musictok.ActivitesFragment.WatchVideosA;
import com.qboxus.musictok.Adapters.VideosListAdapter;
import com.qboxus.musictok.Models.HomeModel;
import com.qboxus.musictok.MainMenu.RelateToFragmentOnBack.RootFragment;
import com.qboxus.musictok.R;
import com.qboxus.musictok.Interfaces.AdapterClickListener;
import com.qboxus.musictok.ApiClasses.ApiLinks;
import com.volley.plus.VPackages.VolleyRequest;
import com.volley.plus.interfaces.Callback;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.Variables;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class SearchVideoF extends RootFragment {

    View view;
    Context context;
    String type;
    ShimmerFrameLayout shimmerFrameLayout;
    RecyclerView recyclerView;
    GridLayoutManager linearLayoutManager;
    RelativeLayout noDataLayout;
    ProgressBar loadMoreProgress;

    int pageCount = 0;
    boolean ispostFinsh;

    ArrayList<HomeModel> dataList;
    VideosListAdapter adapter;

    public SearchVideoF(String type) {
        this.type = type;
    }

    public SearchVideoF() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_search, container, false);
        context = getContext();

        shimmerFrameLayout = view.findViewById(R.id.shimmer_view_container);
        shimmerFrameLayout.startShimmer();

        recyclerView = view.findViewById(R.id.recylerview);
        noDataLayout = view.findViewById(R.id.no_data_layout);
        linearLayoutManager = new GridLayoutManager(context, 2);
        recyclerView.setLayoutManager(linearLayoutManager);

        dataList = new ArrayList<>();
        adapter = new VideosListAdapter(context, dataList, new AdapterClickListener() {
            @Override
            public void onItemClick(View view, int pos, Object object) {

                HomeModel item = (HomeModel) object;
                openWatchVideo(item.video_id);


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
                if (userScrolled && (scrollOutitems == dataList.size() - 1)) {
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

        pageCount = 0;
        callApi();

        return view;
    }


    public void callApi() {

        JSONObject params = new JSONObject();
        try {

            params.put("type", type);
            params.put("keyword", SearchMainA.searchEdit.getText().toString());
            params.put("starting_point", "" + pageCount);

        } catch (Exception e) {
            e.printStackTrace();
        }

        VolleyRequest.JsonPostRequest(getActivity(), ApiLinks.search, params,Functions.getHeaders(getActivity()), new Callback() {
            @Override
            public void onResponce(String resp) {
                shimmerFrameLayout.stopShimmer();
                shimmerFrameLayout.setVisibility(View.GONE);

                if (type.equals("video"))
                    parseVideo(resp);


            }
        });

    }


    public void parseVideo(String responce) {

        try {
            JSONObject jsonObject = new JSONObject(responce);
            String code = jsonObject.optString("code");
            if (code.equals("200")) {
                JSONArray msgArray = jsonObject.getJSONArray("msg");

                ArrayList<HomeModel> temp_list = new ArrayList<>();
                for (int i = 0; i < msgArray.length(); i++) {
                    JSONObject itemdata = msgArray.optJSONObject(i);

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

                    dataList.addAll(temp_list);

                    if (dataList.isEmpty()) {
                        noDataLayout.setVisibility(View.VISIBLE);
                    } else {
                        noDataLayout.setVisibility(View.GONE);

                        recyclerView.setAdapter(adapter);
                    }
                } else {

                    if (temp_list.isEmpty())
                        ispostFinsh = true;
                    else {
                        dataList.addAll(temp_list);
                        adapter.notifyDataSetChanged();
                    }

                }


            } else {
                if (dataList.isEmpty())
                    noDataLayout.setVisibility(View.VISIBLE);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            loadMoreProgress.setVisibility(View.GONE);
        }

    }


    private void openWatchVideo(String videoId) {
        Intent intent = new Intent(view.getContext(), WatchVideosA.class);
        intent.putExtra("video_id", videoId);
        intent.putExtra("position", 0);
        intent.putExtra("pageCount", 0);
        intent.putExtra("userId",Functions.getSharedPreference(view.getContext()).getString(Variables.U_ID,""));
        intent.putExtra("whereFrom","IdVideo");
        startActivity(intent);
    }


}
