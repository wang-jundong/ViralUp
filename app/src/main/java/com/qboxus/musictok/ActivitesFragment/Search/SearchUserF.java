package com.qboxus.musictok.ActivitesFragment.Search;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.qboxus.musictok.ActivitesFragment.Profile.ProfileA;
import com.qboxus.musictok.Adapters.UsersAdapter;
import com.qboxus.musictok.Models.UserModel;
import com.qboxus.musictok.Models.UsersModel;
import com.qboxus.musictok.MainMenu.MainMenuFragment;
import com.qboxus.musictok.MainMenu.RelateToFragmentOnBack.RootFragment;
import com.qboxus.musictok.R;
import com.qboxus.musictok.Interfaces.AdapterClickListener;
import com.qboxus.musictok.ApiClasses.ApiLinks;
import com.volley.plus.VPackages.VolleyRequest;
import com.volley.plus.interfaces.Callback;
import com.qboxus.musictok.SimpleClasses.DataParsing;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.Variables;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class SearchUserF extends RootFragment {

    View view;
    Context context;
    String type;
    ShimmerFrameLayout shimmerFrameLayout;
    RecyclerView recyclerView;
    LinearLayoutManager linearLayoutManager;
    RelativeLayout noDataLayout;
    ProgressBar loadMoreProgress;

    int pageCount = 0;
    boolean ispostFinsh;

    ArrayList<UsersModel> dataList;
    UsersAdapter usersAdapter;

    public SearchUserF(String type) {
        this.type = type;
    }

    public SearchUserF() {

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
        linearLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(linearLayoutManager);
        dataList = new ArrayList<>();
        usersAdapter = new UsersAdapter(context, dataList, new AdapterClickListener() {
            @Override
            public void onItemClick(View view, int pos, Object object) {
                UsersModel item = (UsersModel) object;
                Functions.hideSoftKeyboard(getActivity());
                openProfile(item.fb_id, item.username, item.profile_pic);

            }
        });
        recyclerView.setAdapter(usersAdapter);

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

        VolleyRequest.JsonPostRequest(getActivity(), ApiLinks.search, params, Functions.getHeaders(getActivity()),new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.checkStatus(getActivity(),resp);
                shimmerFrameLayout.stopShimmer();
                shimmerFrameLayout.setVisibility(View.GONE);

                if (type.equalsIgnoreCase("user")) {
                    parseUsers(resp);
                }


            }
        });

    }


    public void parseUsers(String responce) {


        try {
            JSONObject jsonObject = new JSONObject(responce);
            String code = jsonObject.optString("code");
            if (code.equalsIgnoreCase("200")) {

                JSONArray msg = jsonObject.optJSONArray("msg");
                ArrayList<UsersModel> temp_list = new ArrayList<>();
                for (int i = 0; i < msg.length(); i++) {
                    JSONObject data = msg.optJSONObject(i);
                    UserModel userDetailModel= DataParsing.getUserDataModel(data.optJSONObject("User"));

                    UsersModel user = new UsersModel();
                    user.fb_id = userDetailModel.getId();
                    user.username = userDetailModel.getUsername();
                    user.first_name = userDetailModel.getFirstName();
                    user.last_name = userDetailModel.getLastName();
                    user.gender = userDetailModel.getGender();

                    user.profile_pic = userDetailModel.getProfilePic();

                    user.followers_count = userDetailModel.getFollowersCount();
                    user.videos =userDetailModel.getVideoCount();

                    temp_list.add(user);


                }

                if (pageCount == 0) {

                    dataList.addAll(temp_list);

                    if (dataList.isEmpty()) {
                        noDataLayout.setVisibility(View.VISIBLE);
                    } else {
                        noDataLayout.setVisibility(View.GONE);

                        recyclerView.setAdapter(usersAdapter);
                    }
                } else {

                    if (temp_list.isEmpty())
                        ispostFinsh = true;
                    else {
                        dataList.addAll(temp_list);
                        usersAdapter.notifyDataSetChanged();
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


    public void openProfile(String fb_id, String username, String profile_pic) {
        if (Functions.getSharedPreference(context).getString(Variables.U_ID, "0").equals(fb_id)) {

            TabLayout.Tab profile = MainMenuFragment.tabLayout.getTabAt(4);
            profile.select();

        } else {


            Intent intent=new Intent(view.getContext(), ProfileA.class);
            intent.putExtra("user_id", fb_id);
            intent.putExtra("user_name", username);
            intent.putExtra("user_pic", profile_pic);
            startActivity(intent);
            getActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);

        }

    }

}
