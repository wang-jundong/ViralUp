package com.qboxus.musictok.ActivitesFragment.Profile.FollowTab;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.giphy.sdk.core.models.User;
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
import java.util.Timer;
import java.util.TimerTask;


public class FollowingUserF extends Fragment {


    View view;
    Context context;
    ShimmerFrameLayout shimmerFrameLayout;
    RecyclerView recyclerView;
    ArrayList<FollowingModel> datalist;
    FollowingAdapter adapter;
    String userId,userName,fromWhere="following";
    boolean isSelf;
    CardView searchLayout;
    EditText edtSearch;
    SwipeRefreshLayout refreshLayout;
    private Timer timer = new Timer();
    private final long DELAY = 1000; // Milliseconds

    int pageCount = 0;
    boolean ispostFinsh;
    ProgressBar loadMoreProgress;
    LinearLayoutManager linearLayoutManager;

    public FollowingUserF()
    {

    }

    public FollowingUserF(String userId, String userName, boolean isSelf)
    {
        this.userId=userId;
        this.userName=userName;
        this.isSelf=isSelf;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view= inflater.inflate(R.layout.fragment_following_user_, container, false);
        context = getContext();
        shimmerFrameLayout = view.findViewById(R.id.shimmer_view_container);
        shimmerFrameLayout.startShimmer();
        refreshLayout=view.findViewById(R.id.refreshLayout);
        datalist = new ArrayList<>();

        loadMoreProgress = view.findViewById(R.id.load_more_progress);
        recyclerView = (RecyclerView) view.findViewById(R.id.recylerview);
        linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        searchLayout=view.findViewById(R.id.search_layout);
        if (isSelf)
        {
            searchLayout.setVisibility(View.VISIBLE);
            Functions.hideSoftKeyboard(getActivity());
        }
        else
        {
            searchLayout.setVisibility(View.GONE);
        }

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
                        selectNotificationPriority(postion);
                        break;

                }

            }
        }
        );

        edtSearch=view.findViewById(R.id.search_edit);
        edtSearch.addTextChangedListener(
                new TextWatcher() {
                    @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
                    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                    @Override
                    public void afterTextChanged(final Editable s) {
                        timer.cancel();
                        timer = new Timer();
                        timer.schedule(
                                new TimerTask() {
                                    @Override
                                    public void run() {
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                String search_txt = edtSearch.getText().toString();
                                                pageCount=0;
                                                if (search_txt.length() > 0) {
                                                    callApiForFollowingSearch();
                                                }
                                                else
                                                {
                                                    callFollowingApi();
                                                }
                                            }
                                        });
                                    }
                                },
                                DELAY
                        );
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
                        if (edtSearch.getText().toString().length()>0)
                        {
                            callApiForFollowingSearch();
                        }
                        else
                        {
                            callFollowingApi();
                        }
                    }
                }


            }
        });

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshLayout.setRefreshing(false);
                pageCount=0;
                if (edtSearch.getText().toString().length()>0)
                {
                    callApiForFollowingSearch();
                }
                else
                {
                    callFollowingApi();
                }
            }
        });


        callFollowingApi();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            recyclerView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View view, int i, int i1, int i2, int i3) {
                    Log.d(Constants.tag,"recyclerView : "+i);
                }
            });
        }

        return view;
    }

    private void callApiForFollowingSearch() {
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("user_id",userId);
            parameters.put("type", "following");
            parameters.put("keyword", edtSearch.getText().toString());
            parameters.put("starting_point", "" + pageCount);
        } catch (Exception e) {
            e.printStackTrace();
        }


        VolleyRequest.JsonPostRequest(getActivity(), ApiLinks.search, parameters,Functions.getHeaders(getActivity()), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.checkStatus(getActivity(),resp);
                parseFollowingData(resp);
            }
        });
    }




    private void selectNotificationPriority(int position) {
        boolean isFriend=false;
        if (datalist.get(position).follow_status_button.equalsIgnoreCase("Follow"))
        {
            isFriend=false;
        }
        else
        {
            isFriend=true;
        }
        NotificationPriorityF f = new NotificationPriorityF(datalist.get(position).notificationType,isFriend,
                datalist.get(position).username,datalist.get(position).fb_id,new FragmentCallBack() {
            @Override
            public void onResponce(Bundle bundle) {
                if (bundle.getBoolean("isShow",false))
                {
                    FollowingModel itemUpdate=datalist.get(position);
                    itemUpdate.notificationType=bundle.getString("type");

                    datalist.set(position,itemUpdate);
                    adapter.notifyDataSetChanged();
                }
                else
                {

                    FollowingModel itemUpdte=datalist.get(position);
                    if (itemUpdte.follow_status_button.equalsIgnoreCase("Follow"))
                    {
                        itemUpdte.follow_status_button="Following";
                    }
                    else
                    {
                        itemUpdte.follow_status_button="Follow";
                    }

                    datalist.set(position,itemUpdte);
                    adapter.notifyDataSetChanged();
                }
            }
        });
        f.show(getChildFragmentManager(), "");
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
                                    UserModel userDetailModel= DataParsing.getUserDataModel(msg.optJSONObject("User"));
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
    public void callFollowingApi() {

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


        VolleyRequest.JsonPostRequest(getActivity(), ApiLinks.showFollowing, parameters, Functions.getHeaders(getActivity()),new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.checkStatus(getActivity(),resp);
                shimmerFrameLayout.stopShimmer();
                shimmerFrameLayout.setVisibility(View.GONE);
                parseFollowingData(resp);
            }
        });


    }

    // parse the list of user that follow the profile
    public void parseFollowingData(String responce) {

        try {
            JSONObject jsonObject = new JSONObject(responce);
            String code = jsonObject.optString("code");
            if (code.equals("200")) {
                JSONArray msgArray = jsonObject.getJSONArray("msg");

                ArrayList<FollowingModel> temp_list = new ArrayList<>();

                for (int i = 0; i < msgArray.length(); i++) {

                    JSONObject object = msgArray.optJSONObject(i);
                    UserModel userDetailModel=DataParsing.getUserDataModel(object.optJSONObject("FollowingList"));

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
                    if (userStatus.equalsIgnoreCase("following"))
                    {
                        item.follow_status_button = "Following";
                    }
                    else
                    if (userStatus.equalsIgnoreCase("friends"))
                    {
                        item.follow_status_button = "Friends";
                    }
                    else
                    if (userStatus.equalsIgnoreCase("follow back"))
                    {
                        item.follow_status_button = "Follow back";
                    }
                    else
                    {
                        item.follow_status_button = "Follow";
                    }
                    item.notificationType=userDetailModel.getNotification();

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