package com.qboxus.musictok.ActivitesFragment.Profile;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import com.qboxus.musictok.SimpleClasses.AppCompatLocaleActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.qboxus.musictok.ActivitesFragment.Profile.InviteFriends.InviteFriendsFromContactsA;
import com.qboxus.musictok.ActivitesFragment.Profile.Setting.NoInternetA;
import com.qboxus.musictok.ActivitesFragment.Profile.Setting.QrCodeScannerA;
import com.qboxus.musictok.Adapters.FollowingAdapter;
import com.qboxus.musictok.ApiClasses.ApiLinks;
import com.volley.plus.VPackages.VolleyRequest;
import com.qboxus.musictok.Constants;
import com.volley.plus.interfaces.APICallBack;
import com.volley.plus.interfaces.Callback;
import com.qboxus.musictok.Interfaces.InternetCheckCallback;
import com.qboxus.musictok.Models.FollowingModel;
import com.qboxus.musictok.Models.UserModel;
import com.qboxus.musictok.R;
import com.qboxus.musictok.SimpleClasses.DataParsing;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.PermissionUtils;
import com.qboxus.musictok.SimpleClasses.Variables;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InviteFriendsA extends AppCompatLocaleActivity implements View.OnClickListener {

    TextView tvInviteFriendSubtitle;
    RecyclerView recyclerView;
    ArrayList<FollowingModel> datalist;
    FollowingAdapter adapter;

    int pageCount = 0;
    boolean ispostFinsh;
    ProgressBar loadMoreProgress;
    LinearLayoutManager linearLayoutManager;


    PermissionUtils takePermissionUtils;
    String inviteFrom="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Functions.setLocale(Functions.getSharedPreference(InviteFriendsA.this).getString(Variables.APP_LANGUAGE_CODE,Variables.DEFAULT_LANGUAGE_CODE)
                , this, InviteFriendsA.class,false);
        setContentView(R.layout.activity_invite_friends);


        InitControl();
    }

    private void InitControl() {
        findViewById(R.id.ivBack).setOnClickListener(this);
        findViewById(R.id.ivQrCode).setOnClickListener(this);
        findViewById(R.id.btnFindInviteFriend).setOnClickListener(this);
        findViewById(R.id.btnFindInviteFriendsByContacts).setOnClickListener(this);
        findViewById(R.id.btnFindInviteFacebookFriend).setOnClickListener(this);
        findViewById(R.id.search_layout).setOnClickListener(this);

        tvInviteFriendSubtitle=findViewById(R.id.tvInviteFriendSubtitle);

        setUpSuggesionAdapter();
        setupScreenData();
    }

    private void setUpSuggesionAdapter() {
        datalist = new ArrayList<>();
        loadMoreProgress =findViewById(R.id.load_more_progress);
        recyclerView =findViewById(R.id.recylerview);
        linearLayoutManager = new LinearLayoutManager(InviteFriendsA.this);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);

        adapter = new FollowingAdapter(InviteFriendsA.this,true,"", datalist, new FollowingAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int postion, FollowingModel item) {

                switch (view.getId()) {
                    case R.id.action_txt:
                        if (Functions.checkLoginUser(InviteFriendsA.this)) {
                            if (!item.fb_id.equals(Functions.getSharedPreference(InviteFriendsA.this).getString(Variables.U_ID, "")))
                                followUnFollowUser(item, postion);
                        }
                        break;

                    case R.id.mainlayout:
                        openProfile(item);
                        break;
                    case R.id.ivCross:
                        datalist.remove(postion);
                        adapter.notifyDataSetChanged();
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
                        callSuggestionApi();
                    }
                }


            }
        });

    }


    // get the list of videos that you favourite
    public void callSuggestionApi() {


        JSONObject parameters = new JSONObject();
        try {
            parameters.put("user_id", Functions.getSharedPreference(InviteFriendsA.this).getString(Variables.U_ID,""));
            parameters.put("starting_point", "" + pageCount);
        } catch (Exception e) {
            e.printStackTrace();
        }


        VolleyRequest.JsonPostRequest(InviteFriendsA.this, ApiLinks.showSuggestedUsers, parameters, Functions.getHeaders(this),new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.checkStatus(InviteFriendsA.this,resp);
                parseSuggestData(resp);
            }
        });


    }

    // parse the list of user that follow the profile
    public void parseSuggestData(String responce) {
        try {
            JSONObject jsonObject = new JSONObject(responce);
            String code = jsonObject.optString("code");
            if (code.equals("200")) {
                JSONArray msgArray = jsonObject.getJSONArray("msg");

                ArrayList<FollowingModel> temp_list = new ArrayList<>();

                for (int i = 0; i < msgArray.length(); i++) {

                    JSONObject object = msgArray.optJSONObject(i);
                    UserModel userDetailModel=DataParsing.getUserDataModel(object.optJSONObject("User"));


                    FollowingModel item = new FollowingModel();
                    item.fb_id = userDetailModel.getId();
                    item.first_name = userDetailModel.getFirstName();
                    item.last_name = userDetailModel.getLastName();
                    item.bio = userDetailModel.getBio();
                    item.username = userDetailModel.getUsername();

                    item.profile_pic = userDetailModel.getProfilePic();

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
               findViewById(R.id.no_data_layout).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.no_data_layout).setVisibility(View.GONE);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            loadMoreProgress.setVisibility(View.GONE);
        }
    }


    public void followUnFollowUser(final FollowingModel item, final int position) {

        Functions.callApiForFollowUnFollow(InviteFriendsA.this,
                Functions.getSharedPreference(InviteFriendsA.this).getString(Variables.U_ID, ""),
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
                                        itemUpdte.follow_status_button=Functions.getFollowButtonStatus(userStatus,InviteFriendsA.this);
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

    // this will open the profile of user which have uploaded the currenlty running video
    private void openProfile(final FollowingModel item) {
        Intent intent=new Intent(InviteFriendsA.this, ProfileA.class);
        intent.putExtra("user_id", item.fb_id);
        intent.putExtra("user_name", item.username);
        intent.putExtra("user_pic", item.profile_pic);
        startActivity(intent);
       overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
    }



    private void setupScreenData() {
        tvInviteFriendSubtitle.setText(getString(R.string.stay_connected_on)+" "+getString(R.string.app_name));

        callSuggestionApi();
    }

    public void openSearch() {

        Intent intent=new Intent(InviteFriendsA.this, SearchAllUserA.class);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }





    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.search_layout:
                openSearch();
                break;
            case R.id.ivBack:
                InviteFriendsA.super.onBackPressed();
                break;
            case R.id.ivQrCode:
            {
                takePermissionUtils=new PermissionUtils(InviteFriendsA.this,mPermissionCameraResult);
                if (takePermissionUtils.isCameraPermissionGranted())
                {
                    openQrScanner();
                }
                else
                {
                    takePermissionUtils.showCameraPermissionDailog(getString(R.string.we_need_camera_permission_for_qr_scan));
                }
            }
                break;
            case R.id.btnFindInviteFriend:
                takePermissionUtils=new PermissionUtils(InviteFriendsA.this,mPermissionContactResult);
                if (takePermissionUtils.isContactPermissionGranted())
                {
                    inviteFrom="1";
                    openInviteFriendsByContacts(inviteFrom);
                }
                else
                {
                    String contactMsg=getString(R.string.contact_permission_get_part_one)+" "+getString(R.string.app_name)+". "+getString(R.string.contact_permission_get_part_two);
                    takePermissionUtils.showContactPermissionDailog(contactMsg);
                }
                break;
            case R.id.btnFindInviteFriendsByContacts:
                takePermissionUtils=new PermissionUtils(InviteFriendsA.this,mPermissionContactResult);
                if (takePermissionUtils.isContactPermissionGranted())
                {
                    inviteFrom="2";
                    openInviteFriendsByContacts(inviteFrom);
                }
                else
                {
                    String contactMsg=getString(R.string.contact_permission_get_part_one)+" "+getString(R.string.app_name)+". "+getString(R.string.contact_permission_get_part_two);
                    takePermissionUtils.showContactPermissionDailog(contactMsg);
                }
                break;
            case R.id.btnFindInviteFacebookFriend:
            {
                inviteFrom="3";
                openInviteFriendsByContacts(inviteFrom);
            }
                break;
        }
    }

    private void openInviteFriendsByContacts(String counter) {
        Intent intent=new Intent(InviteFriendsA.this, InviteFriendsFromContactsA.class);
        intent.putExtra("fromWhere",counter);
        startActivity(intent);
    }

    private void openQrScanner() {
        startActivity(new Intent(InviteFriendsA.this, QrCodeScannerA.class));
    }





    private ActivityResultLauncher<String[]> mPermissionContactResult = registerForActivityResult(
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
                            blockPermissionCheck.add(Functions.getPermissionStatus(InviteFriendsA.this,key));
                        }
                    }
                    if (blockPermissionCheck.contains("blocked"))
                    {
                        String contactMsg=getString(R.string.contact_permission_get_part_one)+" "+getString(R.string.app_name)+". "+getString(R.string.contact_permission_get_part_two);
                        Functions.showPermissionSetting(InviteFriendsA.this,contactMsg);
                    }
                    else
                    if (allPermissionClear)
                    {
                        openInviteFriendsByContacts(inviteFrom);
                    }

                }
            });





    private ActivityResultLauncher<String[]> mPermissionCameraResult = registerForActivityResult(
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
                            blockPermissionCheck.add(Functions.getPermissionStatus(InviteFriendsA.this,key));
                        }
                    }
                    if (blockPermissionCheck.contains("blocked"))
                    {
                        Functions.showPermissionSetting(InviteFriendsA.this,getString(R.string.we_need_camera_permission_for_qr_scan));
                    }
                    else
                    if (allPermissionClear)
                    {
                        openQrScanner();
                    }

                }
            });



    @Override
    protected void onDestroy() {
        mPermissionContactResult.unregister();
        mPermissionCameraResult.unregister();
        super.onDestroy();
    }
}