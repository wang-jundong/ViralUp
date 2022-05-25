package com.qboxus.musictok.ActivitesFragment.Profile.FollowTab;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.qboxus.musictok.ActivitesFragment.Profile.ProfileA;
import com.qboxus.musictok.Adapters.FollowingAdapter;
import com.qboxus.musictok.ApiClasses.ApiLinks;
import com.volley.plus.VPackages.VolleyRequest;
import com.qboxus.musictok.Constants;
import com.volley.plus.interfaces.APICallBack;
import com.volley.plus.interfaces.Callback;
import com.qboxus.musictok.Interfaces.FragmentCallBack;
import com.qboxus.musictok.Models.FollowingModel;
import com.qboxus.musictok.Models.UserModel;
import com.qboxus.musictok.R;
import com.qboxus.musictok.SimpleClasses.DataParsing;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.Variables;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;


public class FollowerUserF extends Fragment {


    View view;
    Context context;
    ShimmerFrameLayout shimmerFrameLayout;
    RecyclerView recyclerView;
    ArrayList<FollowingModel> datalist=new ArrayList<>();
    FollowingAdapter adapter;
    String userId,fromWhere="fan";
    boolean isSelf;
    SwipeRefreshLayout refreshLayout;

    int pageCount = 0;
    boolean ispostFinsh;
    ProgressBar loadMoreProgress;
    LinearLayoutManager linearLayoutManager;


    public FollowerUserF()
    {

    }

    public FollowerUserF(String userId, boolean isSelf)
    {
        this.userId=userId;
        this.isSelf=isSelf;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view= inflater.inflate(R.layout.fragment_follower_user_, container, false);
        context = getContext();
        shimmerFrameLayout = view.findViewById(R.id.shimmer_view_container);
        shimmerFrameLayout.startShimmer();
        refreshLayout=view.findViewById(R.id.refreshLayout);
        loadMoreProgress = view.findViewById(R.id.load_more_progress);
        recyclerView = (RecyclerView) view.findViewById(R.id.recylerview);
        linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);

        adapter = new FollowingAdapter(context,isSelf,fromWhere, datalist, new FollowingAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int postion, FollowingModel item) {

                switch (view.getId()) {
                    case R.id.action_txt:
                        if (Functions.checkLoginUser(getActivity())) {
                            if (!item.fb_id.equals(Functions.getSharedPreference(context).getString(Variables.U_ID, "")))
                                followUnFollowUser(item, postion);
                        }
                        break;
                    case R.id.mainlayout:
                        openProfile(item);
                        break;
                    case R.id.ivCross:
                        removeFollower(postion);
                        break;

                }

            }
        }
        );
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
                        callFollowerApi();
                    }
                }


            }
        });

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshLayout.setRefreshing(false);
                pageCount=0;
                callFollowerApi();
            }
        });

        callFollowerApi();

        return view;
    }

    private void removeFollower(int position) {
        final Dialog dialog = new Dialog(context);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.show_remove_follower_popup_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        final TextView tvRemoveFollower,tvCancel;
        tvCancel=dialog.findViewById(R.id.tvCancel);
        tvRemoveFollower=dialog.findViewById(R.id.tvRemoveFollower);

        tvRemoveFollower.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                Functions.showDoubleButtonAlert(view.getContext(),
                        view.getContext().getString(R.string.remove_this_follower),
                        datalist.get(position).username + " "+view.getContext().getString(R.string.will_no_longer_follow_you_and_wont_bt_notified_that_you_removed_them),
                        view.getContext().getString(R.string.cancel_),
                        view.getContext().getString(R.string.remove),false,
                        new FragmentCallBack() {
                            @Override
                            public void onResponce(Bundle bundle) {
                                if (bundle.getBoolean("isShow",false))
                                {
                                    hitRemoveFollowerAPI(position);
                                }
                            }
                        }
                );

            }
        });
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void hitRemoveFollowerAPI(int position) {
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("user_id", Functions.getSharedPreference(context).getString(Variables.U_ID, ""));
            parameters.put("follower_id", datalist.get(position).fb_id);

        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d(Constants.tag,"check working");

        VolleyRequest.JsonPostRequest(getActivity(), ApiLinks.deleteFollower, parameters,Functions.getHeaders(getActivity()), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.checkStatus(getActivity(),resp);
                try {
                    JSONObject jsonObject = new JSONObject(resp);
                    String code = jsonObject.optString("code");
                    if (code.equals("200")) {
                        datalist.remove(position);
                        adapter.notifyDataSetChanged();
                    }
                }catch (Exception e)
                {
                    Log.d(Constants.tag,"Exception : "+e);
                }
            }
        });

    }



    // this will open the profile of user which have uploaded the currenlty running video
    private void openProfile(final FollowingModel item) {

        String userName="";
        if (view != null) {
            userName=item.username;
        }
        else
        {
            userName=item.first_name + " " + item.last_name;
        }
        Intent intent=new Intent(view.getContext(), ProfileA.class);
        intent.putExtra("user_id", item.fb_id);
        intent.putExtra("user_name", userName);
        intent.putExtra("user_pic", item.profile_pic);
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);


    }



    public void followUnFollowUser(final FollowingModel item, final int position) {

        Functions.callApiForFollowUnFollow(getActivity(),
                Functions.getSharedPreference(context).getString(Variables.U_ID, ""),
                item.fb_id,
                new APICallBack() {
                    @Override
                    public void arrayData(ArrayList arrayList) {


                    }

                    @Override
                    public void onSuccess(String responce) {
                        try {
                            JSONObject jsonObject=new JSONObject(responce);
                            String code=jsonObject.optString("code");
                            if(code.equalsIgnoreCase("200")){
                                JSONObject msg=jsonObject.optJSONObject("msg");
                                if(msg!=null){
                                    UserModel userDetailModel = DataParsing.getUserDataModel(msg.optJSONObject("User"));
                                    if(!(TextUtils.isEmpty(userDetailModel.getId()))){
                                        FollowingModel itemUpdte=item;
                                        String userStatus=userDetailModel.getButton().toLowerCase();
                                        itemUpdte.follow_status_button=Functions.getFollowButtonStatus(userStatus,view.getContext());
                                        datalist.set(position,itemUpdte);
                                        adapter.notifyDataSetChanged();

                                    }
                                }
                            }
                        } catch (Exception e) {
                            Log.d(Constants.tag,"Exception : "+e);
                        }
                    }

                    @Override
                    public void onFail(String responce) {

                    }

                });


    }


    // get the list of videos that you favourite
    public void callFollowerApi() {
        if (datalist == null)
            datalist = new ArrayList<>();

        JSONObject parameters = new JSONObject();
        try {
            if (Functions.getSharedPreference(context).getString(Variables.U_ID, "0").equals(userId))
            {
                parameters.put("user_id", Functions.getSharedPreference(context).getString(Variables.U_ID, ""));
            }
            else {

                parameters.put("user_id", Functions.getSharedPreference(context).getString(Variables.U_ID, ""));
                parameters.put("other_user_id", userId);
            }
            parameters.put("starting_point", "" + pageCount);

        } catch (Exception e) {
            e.printStackTrace();
        }


        VolleyRequest.JsonPostRequest(getActivity(), ApiLinks.showFollowers, parameters,Functions.getHeaders(getActivity()), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.checkStatus(getActivity(),resp);
                shimmerFrameLayout.stopShimmer();
                shimmerFrameLayout.setVisibility(View.GONE);
                parseFansData(resp);
            }
        });

    }


    // parse the list of all the follower list
    public void parseFansData(String responce) {
        try {
            JSONObject jsonObject = new JSONObject(responce);
            String code = jsonObject.optString("code");
            if (code.equals("200")) {
                JSONArray msgArray = jsonObject.getJSONArray("msg");
                ArrayList<FollowingModel> temp_list = new ArrayList<>();

                for (int i = 0; i < msgArray.length(); i++) {
                    JSONObject object = msgArray.optJSONObject(i);
                    UserModel userDetailModel=DataParsing.getUserDataModel(object.optJSONObject("FollowerList"));

                    FollowingModel item = new FollowingModel();
                    item.fb_id = userDetailModel.getId();
                    item.first_name = userDetailModel.getFirstName();
                    item.last_name = userDetailModel.getLastName();
                    item.bio = userDetailModel.getBio();
                    item.username = userDetailModel.getUsername();

                    item.profile_pic = userDetailModel.getProfilePic();

                    if (isSelf)
                    {
                        item.isFollow=false;
                    }
                    else
                    {
                        item.isFollow=true;
                    }

                    String userStatus=userDetailModel.getButton().toLowerCase();
                    item.follow_status_button=Functions.getFollowButtonStatus(userStatus,view.getContext());

                    item.notificationType="normal";

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


}