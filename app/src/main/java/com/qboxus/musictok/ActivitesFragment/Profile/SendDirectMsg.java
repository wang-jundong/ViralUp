package com.qboxus.musictok.ActivitesFragment.Profile;

import androidx.annotation.NonNull;
import com.qboxus.musictok.SimpleClasses.AppCompatLocaleActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.qboxus.musictok.ActivitesFragment.Chat.ChatA;
import com.qboxus.musictok.ActivitesFragment.Profile.Setting.NoInternetA;
import com.qboxus.musictok.Adapters.UserShareProfileAdapter;
import com.qboxus.musictok.ApiClasses.ApiLinks;
import com.qboxus.musictok.Models.UserModel;
import com.qboxus.musictok.SimpleClasses.DataParsing;
import com.volley.plus.VPackages.VolleyRequest;
import com.qboxus.musictok.Constants;
import com.qboxus.musictok.Interfaces.AdapterClickListener;
import com.volley.plus.interfaces.Callback;
import com.qboxus.musictok.Interfaces.InternetCheckCallback;
import com.qboxus.musictok.Models.FollowingModel;
import com.qboxus.musictok.R;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.Variables;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class SendDirectMsg extends AppCompatLocaleActivity implements  View.OnClickListener{

    private Timer timer = new Timer();
    private final long DELAY = 1000; // Milliseconds
    LinearLayout tabSearch,tabUser;
    EditText search_edit;
    RecyclerView recylerviewSearch,recylerviewFollowing,recylerviewFollower;
    LinearLayout tabMessageSend;
    TextView sendBtn;
    EditText edtMessage;
    SimpleDraweeView ivUserPic;
    String userId,userName,userPic,fullName;

    String shareType="";


    ArrayList<FollowingModel> followerList=new ArrayList<>();
    ArrayList<FollowingModel> followingList=new ArrayList<>();
    ArrayList<FollowingModel> searchList=new ArrayList<>();

    ArrayList<FollowingModel> selectedfollowerList=new ArrayList<>();
    ArrayList<FollowingModel> selectedfollowingList=new ArrayList<>();
    ArrayList<FollowingModel> selectedsearchList=new ArrayList<>();

    String senderId="",receiverId="";
    DatabaseReference rootref,adduserInbox;

    UserShareProfileAdapter adapterFollower,adapterFollowing,adapterSearch;
    int pageCount = 0;
    boolean ispostFinsh;
    ProgressBar loadMoreProgress;
    LinearLayoutManager linearLayoutManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Functions.setLocale(Functions.getSharedPreference(SendDirectMsg.this).getString(Variables.APP_LANGUAGE_CODE,Variables.DEFAULT_LANGUAGE_CODE)
                , this, SendDirectMsg.class,false);
        setContentView(R.layout.activity_send_direct_msg);
        initControl();
        ActionControl();
    }

    private void ActionControl() {
        search_edit.addTextChangedListener(
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
                                        SendDirectMsg.this.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                String search_txt = search_edit.getText().toString();
                                                pageCount=0;
                                                if (search_txt.length() > 0) {
                                                    tabUser.setVisibility(View.GONE);
                                                    tabSearch.setVisibility(View.VISIBLE);
                                                    callApiForGetAllSearchUser();
                                                }
                                                else
                                                {
                                                    tabUser.setVisibility(View.VISIBLE);
                                                    tabSearch.setVisibility(View.GONE);
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
        sendBtn.setOnClickListener(this);
        recylerviewSearch.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                if (userScrolled && (scrollOutitems == searchList.size() - 1)) {
                    userScrolled = false;

                    if (loadMoreProgress.getVisibility() != View.VISIBLE && !ispostFinsh) {
                        loadMoreProgress.setVisibility(View.VISIBLE);
                        pageCount = pageCount + 1;
                        if (search_edit.getText().toString().length()>0)
                        {
                            callApiForGetAllSearchUser();
                        }

                    }
                }


            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        callApiForGetAllfollowing();
        callApiForGetAllfollower();
    }


    private void callApiForGetAllSearchUser() {

        JSONObject parameters = new JSONObject();
        try {
            parameters.put("user_id", Functions.getSharedPreference(SendDirectMsg.this).getString(Variables.U_ID, ""));
            parameters.put("keyword", search_edit.getText().toString());
            parameters.put("starting_point", "" + pageCount);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        VolleyRequest.JsonPostRequest(SendDirectMsg.this, ApiLinks.searchFollowingOrFollowUsers, parameters, Functions.getHeaders(this),new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.checkStatus(SendDirectMsg.this,resp);
                parseSearchUserData(resp);
            }
        });


    }

    public void parseSearchUserData(String responce) {

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
                    item.bio = userDetailModel.getBio();
                    item.username = userDetailModel.getUsername();
                    item.first_name = userDetailModel.getFirstName();
                    item.last_name = userDetailModel.getLastName();
                    item.profile_pic = userDetailModel.getProfilePic();
                    item.follow_status_button = userDetailModel.getButton().toLowerCase();


                    temp_list.add(item);


                }

                if (pageCount == 0) {
                    searchList.clear();
                    searchList.addAll(temp_list);
                } else {
                    searchList.addAll(temp_list);
                }

                adapterSearch.notifyDataSetChanged();
            }

            if (searchList.isEmpty()) {
                findViewById(R.id.no_data_layout).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.no_data_layout).setVisibility(View.GONE);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            loadMoreProgress.setVisibility(View.GONE);
        }
    }


    private void callApiForGetAllfollower() {

        JSONObject parameters = new JSONObject();
        try {
            parameters.put("user_id", Functions.getSharedPreference(SendDirectMsg.this).getString(Variables.U_ID, ""));

        } catch (JSONException e) {
            e.printStackTrace();
        }


        VolleyRequest.JsonPostRequest(SendDirectMsg.this, ApiLinks.showFollowers, parameters,Functions.getHeaders(this), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.checkStatus(SendDirectMsg.this,resp);
                parseFollowerData(resp);
            }
        });


    }

    private void callApiForGetAllfollowing() {

        JSONObject parameters = new JSONObject();
        try {
            parameters.put("user_id", Functions.getSharedPreference(SendDirectMsg.this).getString(Variables.U_ID, ""));

        } catch (JSONException e) {
            e.printStackTrace();
        }


        VolleyRequest.JsonPostRequest(SendDirectMsg.this, ApiLinks.showFollowing, parameters,Functions.getHeaders(this), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.checkStatus(SendDirectMsg.this,resp);
                parseFollowingData(resp);
            }
        });


    }

    public void parseFollowerData(String responce) {

        try {
            JSONObject jsonObject = new JSONObject(responce);
            String code = jsonObject.optString("code");
            if (code.equals("200")) {
                JSONArray msgArray = jsonObject.getJSONArray("msg");
                for (int i = 0; i < msgArray.length(); i++) {

                    JSONObject object = msgArray.optJSONObject(i);
                    UserModel userDetailModel= DataParsing.getUserDataModel(object.optJSONObject("FollowerList"));

                    FollowingModel item = new FollowingModel();
                    item.fb_id = userDetailModel.getId();
                    item.bio = userDetailModel.getBio();
                    item.username = userDetailModel.getUsername();
                    item.first_name = userDetailModel.getFirstName();
                    item.last_name = userDetailModel.getLastName();

                    item.profile_pic = userDetailModel.getProfilePic();

                    item.follow_status_button = userDetailModel.getButton().toLowerCase();


                    followerList.add(item);

                }

                adapterFollower.notifyDataSetChanged();

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    public void parseFollowingData(String responce) {

        try {
            JSONObject jsonObject = new JSONObject(responce);
            String code = jsonObject.optString("code");
            if (code.equals("200")) {
                JSONArray msgArray = jsonObject.getJSONArray("msg");
                for (int i = 0; i < msgArray.length(); i++) {

                    JSONObject object = msgArray.optJSONObject(i);
                    UserModel userDetailModel=DataParsing.getUserDataModel(object.optJSONObject("FollowingList"));

                    FollowingModel item = new FollowingModel();
                    item.fb_id = userDetailModel.getId();
                    item.bio = userDetailModel.getBio();
                    item.username = userDetailModel.getUsername();
                    item.first_name = userDetailModel.getFirstName();
                    item.last_name = userDetailModel.getLastName();
                    item.profile_pic = userDetailModel.getProfilePic();

                    item.follow_status_button = userDetailModel.getButton().toLowerCase();


                    followingList.add(item);
                }

                adapterFollowing.notifyDataSetChanged();


            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }



    private void initControl() {
        shareType=getIntent().getStringExtra("type");
        userId=getIntent().getStringExtra("userId");
        userName=getIntent().getStringExtra("userName");
        userPic=getIntent().getStringExtra("userPic");
        fullName=getIntent().getStringExtra("fullName");

        senderId=Functions.getSharedPreference(SendDirectMsg.this).getString(Variables.U_ID, "");
        rootref = FirebaseDatabase.getInstance().getReference();
        adduserInbox = FirebaseDatabase.getInstance().getReference();

        tabSearch=findViewById(R.id.tabSearch);
        tabUser=findViewById(R.id.tabUser);
        search_edit=findViewById(R.id.search_edit);
        recylerviewSearch=findViewById(R.id.recylerviewSearch);
        recylerviewFollowing=findViewById(R.id.recylerviewFollowing);
        recylerviewFollower=findViewById(R.id.recylerviewFollower);
        tabMessageSend=findViewById(R.id.tabMessageSend);
        sendBtn=findViewById(R.id.sendBtn);
        edtMessage=findViewById(R.id.edtMessage);
        ivUserPic=findViewById(R.id.ivUserPic);

        setSearchAdapter();
        setFollowerAdapter();
        setFollowingAdapter();

        checkUserSelected();

        SetupScreenData();
    }

    private void SetupScreenData() {
        ivUserPic.setController(Functions.frescoImageLoad(userPic,ivUserPic,false));
    }


    ArrayList<FollowingModel> tempList;
    private void checkUserSelected() {
        if (selectedfollowerList.size()>0 || selectedfollowingList.size()>0 || selectedsearchList.size()>0)
        {
            tabMessageSend.setVisibility(View.VISIBLE);
        }
        else
        {
            tabMessageSend.setVisibility(View.GONE);
        }


        tempList=new ArrayList<>();
        tempList.addAll(selectedfollowerList);
        tempList.addAll(selectedfollowingList);
        tempList.addAll(selectedsearchList);

        try {
            HashMap<String,FollowingModel> mapData=new HashMap<>();
            for (FollowingModel itemData:tempList)
            {
                mapData.put(itemData.fb_id,itemData);
            }
            tempList.clear();
            for (String key : mapData.keySet())
            {
                Log.d(Constants.tag,"Data : "+key);
                tempList.add(mapData.get(key));
            }

        }
        catch (Exception e)
        {
            Log.d(Constants.tag,"Exception : "+e);
        }

        if(tempList.size()>0){
            sendBtn.setText(tempList.size()+" "+getString(R.string.send));
        }
        else {
            sendBtn.setText(getString(R.string.send));
        }

    }

    private void setSearchAdapter() {
        loadMoreProgress = findViewById(R.id.load_more_progress);
        linearLayoutManager = new LinearLayoutManager(SendDirectMsg.this);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        recylerviewSearch.setLayoutManager(linearLayoutManager);
        recylerviewSearch.setHasFixedSize(true);
        adapterSearch=new UserShareProfileAdapter(searchList, new AdapterClickListener() {
            @Override
            public void onItemClick(View view, int pos, Object object) {
                FollowingModel itemUpdate= searchList.get(pos);
                selectedsearchList=new ArrayList<>();
                if (itemUpdate.is_select)
                {
                    itemUpdate.is_select=false;
                    searchList.set(pos,itemUpdate);
                }
                else
                {
                    itemUpdate.is_select=true;
                    searchList.set(pos,itemUpdate);
                }
                adapterSearch.notifyDataSetChanged();

                for (int i = 0; i< searchList.size(); i++){

                    if (searchList.get(i).is_select)
                    {
                        selectedsearchList.add(searchList.get(i));
                    }
                }

                checkUserSelected();
            }
        });
        recylerviewSearch.setAdapter(adapterSearch);
    }

    private void setFollowerAdapter() {
        LinearLayoutManager layoutManager=new LinearLayoutManager(SendDirectMsg.this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        recylerviewFollower.setLayoutManager(layoutManager);
        adapterFollower=new UserShareProfileAdapter(followerList, new AdapterClickListener() {
            @Override
            public void onItemClick(View view, int pos, Object object) {
                FollowingModel itemUpdate= followerList.get(pos);
                selectedfollowerList=new ArrayList<>();
                if (itemUpdate.is_select)
                {
                    itemUpdate.is_select=false;
                    followerList.set(pos,itemUpdate);
                }
                else
                {
                    itemUpdate.is_select=true;
                    followerList.set(pos,itemUpdate);
                }
                adapterFollower.notifyDataSetChanged();

                for (int i = 0; i< followerList.size(); i++){

                    if (followerList.get(i).is_select)
                    {
                        selectedfollowerList.add(followerList.get(i));
                    }
                }

                checkUserSelected();
            }
        });
        recylerviewFollower.setAdapter(adapterFollower);
    }

    private void setFollowingAdapter() {
        LinearLayoutManager layoutManager=new LinearLayoutManager(SendDirectMsg.this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        recylerviewFollowing.setLayoutManager(layoutManager);
        adapterFollowing=new UserShareProfileAdapter(followingList, new AdapterClickListener() {
            @Override
            public void onItemClick(View view, int pos, Object object) {
                FollowingModel itemUpdate= followingList.get(pos);
                selectedfollowingList=new ArrayList<>();
                if (itemUpdate.is_select)
                {
                    itemUpdate.is_select=false;
                    followingList.set(pos,itemUpdate);
                }
                else
                {
                    itemUpdate.is_select=true;
                    followingList.set(pos,itemUpdate);
                }
                adapterFollowing.notifyDataSetChanged();

                for (int i = 0; i< followingList.size(); i++){

                    if (followingList.get(i).is_select)
                    {
                        selectedfollowingList.add(followingList.get(i));
                    }
                }

                checkUserSelected();
            }
        });
        recylerviewFollowing.setAdapter(adapterFollowing);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.sendBtn:
            {
                if(tempList.size()>0)
                {
                    for (FollowingModel item:tempList)
                    {
                        if (shareType.equalsIgnoreCase("profileShare"))
                        {
                            SendDirectMsg.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    sendProfileShareMsg(item);
                                    if (!(TextUtils.isEmpty(edtMessage.getText().toString())))
                                    {
                                        sendInboxMsg(item,edtMessage.getText().toString());
                                    }
                                }
                            });
                        }
                        else
                        if (shareType.equalsIgnoreCase("videoShare"))
                        {
                            sendvideo(item);
                            if (!(TextUtils.isEmpty(edtMessage.getText().toString())))
                            {
                                sendVideoInboxMsg(item,edtMessage.getText().toString());
                            }
                        }

                    }
                    Functions.showLoader(view.getContext(),false,false);
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            SendDirectMsg.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Functions.cancelLoader();
                                    Toast.makeText(SendDirectMsg.this, getString(R.string.profile_share_successfully_completed), Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            });
                        }
                    },1500);

                }
            }
            break;
        }
    }



    public void sendvideo(FollowingModel item) {

        DatabaseReference rootref= FirebaseDatabase.getInstance().getReference();
        String senderId=Functions.getSharedPreference(SendDirectMsg.this).getString(Variables.U_ID,"0");

        Date c = Calendar.getInstance().getTime();
        final String formattedDate = Variables.df.format(c);


        DatabaseReference dref = rootref.child("chat").child(senderId + "-" + item.fb_id).push();
        final String key = dref.getKey();

        String current_user_ref = "chat" + "/" + senderId + "-" + item.fb_id;
        String chat_user_ref = "chat" + "/" + item.fb_id + "-" + senderId;

        HashMap message_user_map = new HashMap<>();
        message_user_map.put("receiver_id", item.fb_id);
        message_user_map.put("sender_id", senderId);
        message_user_map.put("chat_id", key);
        message_user_map.put("text", "");
        message_user_map.put("type", "video");
        message_user_map.put("pic_url", getIntent().getStringExtra("thum"));
        message_user_map.put("video_id", getIntent().getStringExtra("videoId"));
        message_user_map.put("status", "0");
        message_user_map.put("time", "");
        message_user_map.put("sender_name", Functions.getSharedPreference(SendDirectMsg.this).getString(Variables.U_NAME, ""));
        message_user_map.put("timestamp", formattedDate);
        HashMap user_map = new HashMap<>();

        user_map.put(current_user_ref + "/" + key, message_user_map);
        user_map.put(chat_user_ref + "/" + key, message_user_map);

        rootref.updateChildren(user_map, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                String inbox_sender_ref = "Inbox" + "/" + senderId + "/" + item.fb_id;
                String inbox_receiver_ref = "Inbox" + "/" + item.fb_id + "/" + senderId;


                HashMap sendermap = new HashMap<>();
                sendermap.put("rid", senderId);
                sendermap.put("name", Functions.getSharedPreference(SendDirectMsg.this).getString(Variables.U_NAME, ""));
                sendermap.put("pic", Functions.getSharedPreference(SendDirectMsg.this).getString(Variables.U_PIC, ""));
                sendermap.put("msg", "Send an video...");
                sendermap.put("status", "0");
                sendermap.put("timestamp", -1 * System.currentTimeMillis());
                sendermap.put("date", formattedDate);

                HashMap receivermap = new HashMap<>();
                receivermap.put("rid", item.fb_id);
                receivermap.put("name", item.username);
                receivermap.put("pic", item.profile_pic);
                receivermap.put("msg", "Send an video...");
                receivermap.put("status", "1");
                receivermap.put("timestamp", -1 * System.currentTimeMillis());
                receivermap.put("date", formattedDate);

                HashMap both_user_map = new HashMap<>();
                both_user_map.put(inbox_sender_ref, receivermap);
                both_user_map.put(inbox_receiver_ref, sendermap);

                rootref.updateChildren(both_user_map).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        JSONObject notimap = new JSONObject();
                        try {
                            notimap.put("title", Functions.getSharedPreference(SendDirectMsg.this).getString(Variables.U_NAME, ""));
                            notimap.put("message", "You have a new message");
                            notimap.put("sender_id", senderId);
                            JSONArray receiverArray=new JSONArray();
                            receiverArray.put(new JSONObject().put("receiver_id",receiverId));
                            notimap.put("receivers", receiverArray);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        VolleyRequest.JsonPostRequest(SendDirectMsg.this, ApiLinks.sendPushNotification, notimap,Functions.getHeaders(SendDirectMsg.this), new Callback() {
                            @Override
                            public void onResponce(String resp) {
                Functions.checkStatus(SendDirectMsg.this,resp);

                            }
                        });
                    }
                });

            }
        });
    }

    private void sendVideoInboxMsg(FollowingModel item,String message) {
        receiverId=item.fb_id;
        Date c = Calendar.getInstance().getTime();
        final String formattedDate = Variables.df.format(c);

        final String current_user_ref = "chat" + "/" + senderId + "-" + receiverId;
        final String chat_user_ref = "chat" + "/" + receiverId + "-" + senderId;

        DatabaseReference reference = rootref.child("chat").child(senderId + "-" + receiverId).push();
        final String pushid = reference.getKey();

        final HashMap message_user_map = new HashMap<>();
        message_user_map.put("receiver_id", receiverId);
        message_user_map.put("sender_id", senderId);
        message_user_map.put("chat_id", pushid);
        message_user_map.put("text", message);
        message_user_map.put("type", "text");
        message_user_map.put("pic_url", "");
        message_user_map.put("status", "0");
        message_user_map.put("time", "");
        message_user_map.put("sender_name", Functions.getSharedPreference(SendDirectMsg.this).getString(Variables.U_NAME, ""));
        message_user_map.put("timestamp", formattedDate);

        final HashMap user_map = new HashMap<>();
        user_map.put(current_user_ref + "/" + pushid, message_user_map);
        user_map.put(chat_user_ref + "/" + pushid, message_user_map);

        rootref.updateChildren(user_map, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                //if first message then set the visibility of whoops layout gone
                String inbox_sender_ref = "Inbox" + "/" + senderId + "/" + receiverId;
                String inbox_receiver_ref = "Inbox" + "/" + receiverId + "/" + senderId;

                HashMap sendermap = new HashMap<>();
                sendermap.put("rid", senderId);
                sendermap.put("name", Functions.getSharedPreference(SendDirectMsg.this).getString(Variables.U_NAME, ""));
                sendermap.put("pic", Functions.getSharedPreference(SendDirectMsg.this).getString(Variables.U_PIC, ""));
                sendermap.put("msg", message);
                sendermap.put("status", "0");
                sendermap.put("timestamp", -1 * System.currentTimeMillis());
                sendermap.put("date", formattedDate);

                HashMap receivermap = new HashMap<>();
                receivermap.put("rid", receiverId);
                receivermap.put("name", item.username);
                receivermap.put("pic", item.profile_pic);
                receivermap.put("msg", message);
                receivermap.put("status", "1");
                receivermap.put("timestamp", -1 * System.currentTimeMillis());
                receivermap.put("date", formattedDate);

                HashMap both_user_map = new HashMap<>();
                both_user_map.put(inbox_sender_ref, receivermap);
                both_user_map.put(inbox_receiver_ref, sendermap);

                adduserInbox.updateChildren(both_user_map).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {


                        ChatA.sendPushNotification(SendDirectMsg.this, Functions.getSharedPreference(SendDirectMsg.this).getString(Variables.U_NAME, ""), message,
                                receiverId, senderId);


                    }
                });
            }
        });
    }

    private void sendInboxMsg(FollowingModel item,String message) {
        receiverId=item.fb_id;
        Date c = Calendar.getInstance().getTime();
        final String formattedDate = Variables.df.format(c);

        final String current_user_ref = "chat" + "/" + senderId + "-" + receiverId;
        final String chat_user_ref = "chat" + "/" + receiverId + "-" + senderId;

        DatabaseReference reference = rootref.child("chat").child(senderId + "-" + receiverId).push();
        final String pushid = reference.getKey();

        final HashMap message_user_map = new HashMap<>();
        message_user_map.put("receiver_id", receiverId);
        message_user_map.put("sender_id", senderId);
        message_user_map.put("chat_id", pushid);
        message_user_map.put("text", message);
        message_user_map.put("type", "text");
        message_user_map.put("pic_url", "");
        message_user_map.put("status", "0");
        message_user_map.put("time", "");
        message_user_map.put("sender_name", Functions.getSharedPreference(SendDirectMsg.this).getString(Variables.U_NAME, ""));
        message_user_map.put("timestamp", formattedDate);

        final HashMap user_map = new HashMap<>();
        user_map.put(current_user_ref + "/" + pushid, message_user_map);
        user_map.put(chat_user_ref + "/" + pushid, message_user_map);

        rootref.updateChildren(user_map, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                //if first message then set the visibility of whoops layout gone
                String inbox_sender_ref = "Inbox" + "/" + senderId + "/" + receiverId;
                String inbox_receiver_ref = "Inbox" + "/" + receiverId + "/" + senderId;

                HashMap sendermap = new HashMap<>();
                sendermap.put("rid", senderId);
                sendermap.put("name", Functions.getSharedPreference(SendDirectMsg.this).getString(Variables.U_NAME, ""));
                sendermap.put("pic", Functions.getSharedPreference(SendDirectMsg.this).getString(Variables.U_PIC, ""));
                sendermap.put("msg", message);
                sendermap.put("status", "0");
                sendermap.put("timestamp", -1 * System.currentTimeMillis());
                sendermap.put("date", formattedDate);

                HashMap receivermap = new HashMap<>();
                receivermap.put("rid", receiverId);
                receivermap.put("name", item.username);
                receivermap.put("pic", item.profile_pic);
                receivermap.put("msg", message);
                receivermap.put("status", "1");
                receivermap.put("timestamp", -1 * System.currentTimeMillis());
                receivermap.put("date", formattedDate);

                HashMap both_user_map = new HashMap<>();
                both_user_map.put(inbox_sender_ref, receivermap);
                both_user_map.put(inbox_receiver_ref, sendermap);

                adduserInbox.updateChildren(both_user_map).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {


                        ChatA.sendPushNotification(SendDirectMsg.this, Functions.getSharedPreference(SendDirectMsg.this).getString(Variables.U_NAME, ""), message,
                                receiverId, senderId);


                    }
                });
            }
        });
    }

    private void sendProfileShareMsg(FollowingModel item) {
        receiverId=item.fb_id;
        String message="You shared a profile: "+Functions.showUsername(userName);

        Date c = Calendar.getInstance().getTime();
        final String formattedDate = Variables.df.format(c);

        final String current_user_ref = "chat" + "/" + senderId + "-" + receiverId;
        final String chat_user_ref = "chat" + "/" + receiverId + "-" + senderId;

        DatabaseReference reference = rootref.child("chat").child(senderId + "-" + receiverId).push();
        final String pushid = reference.getKey();

        JSONObject object=new JSONObject();
        try {
            object.put("id",userId);
            object.put("fullName",fullName);
            object.put("username",userName);
            object.put("pic",userPic);
        }catch (Exception e){}

        final HashMap message_user_map = new HashMap<>();
        message_user_map.put("receiver_id", receiverId);
        message_user_map.put("sender_id", senderId);
        message_user_map.put("chat_id", pushid);
        message_user_map.put("text", ""+object);
        message_user_map.put("type", "profileShare");
        message_user_map.put("pic_url", "");
        message_user_map.put("status", "0");
        message_user_map.put("time", "");
        message_user_map.put("sender_name", Functions.getSharedPreference(SendDirectMsg.this).getString(Variables.U_NAME, ""));
        message_user_map.put("timestamp", formattedDate);

        final HashMap user_map = new HashMap<>();
        user_map.put(current_user_ref + "/" + pushid, message_user_map);
        user_map.put(chat_user_ref + "/" + pushid, message_user_map);

        rootref.updateChildren(user_map, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                //if first message then set the visibility of whoops layout gone
                String inbox_sender_ref = "Inbox" + "/" + senderId + "/" + receiverId;
                String inbox_receiver_ref = "Inbox" + "/" + receiverId + "/" + senderId;

                HashMap sendermap = new HashMap<>();
                sendermap.put("rid", senderId);
                sendermap.put("name", Functions.getSharedPreference(SendDirectMsg.this).getString(Variables.U_NAME, ""));
                sendermap.put("pic", Functions.getSharedPreference(SendDirectMsg.this).getString(Variables.U_PIC, ""));
                sendermap.put("msg", message);
                sendermap.put("status", "0");
                sendermap.put("timestamp", -1 * System.currentTimeMillis());
                sendermap.put("date", formattedDate);

                HashMap receivermap = new HashMap<>();
                receivermap.put("rid", receiverId);
                receivermap.put("name", item.username);
                receivermap.put("pic", item.profile_pic);
                receivermap.put("msg", message);
                receivermap.put("status", "1");
                receivermap.put("timestamp", -1 * System.currentTimeMillis());
                receivermap.put("date", formattedDate);

                HashMap both_user_map = new HashMap<>();
                both_user_map.put(inbox_sender_ref, receivermap);
                both_user_map.put(inbox_receiver_ref, sendermap);

                adduserInbox.updateChildren(both_user_map).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {


                        JSONObject notimap = new JSONObject();
                        try {
                            notimap.put("title", Functions.getSharedPreference(SendDirectMsg.this).getString(Variables.U_NAME, ""));
                            notimap.put("message", "You have a new message");
                            notimap.put("sender_id", senderId);
                            JSONArray receiverArray=new JSONArray();
                            receiverArray.put(new JSONObject().put("receiver_id",receiverId));
                            notimap.put("receivers", receiverArray);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        VolleyRequest.JsonPostRequest(SendDirectMsg.this, ApiLinks.sendPushNotification, notimap,Functions.getHeaders(SendDirectMsg.this), new Callback() {
                            @Override
                            public void onResponce(String resp) {
                Functions.checkStatus(SendDirectMsg.this,resp);

                            }
                        });

                    }
                });
            }
        });
    }


}