package com.qboxus.musictok.ActivitesFragment.LiveStreaming.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Dialog;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.qboxus.musictok.ActivitesFragment.LiveStreaming.LiveCommentModel;
import com.qboxus.musictok.ActivitesFragment.LiveStreaming.LiveCommentsAdapter;
import com.qboxus.musictok.ActivitesFragment.LiveStreaming.LiveUserModel;
import com.qboxus.musictok.ActivitesFragment.LiveStreaming.LiveUserViewAdapter;
import com.qboxus.musictok.ActivitesFragment.LiveStreaming.stats.LocalStatsData;
import com.qboxus.musictok.ActivitesFragment.LiveStreaming.stats.RemoteStatsData;
import com.qboxus.musictok.ActivitesFragment.LiveStreaming.stats.StatsData;
import com.qboxus.musictok.ActivitesFragment.LiveStreaming.ui.VideoGridContainer;
import com.qboxus.musictok.ActivitesFragment.SendGift.StickerGiftF;
import com.qboxus.musictok.ActivitesFragment.SendGift.StickerModel;
import com.qboxus.musictok.ApiClasses.ApiLinks;
import com.qboxus.musictok.Models.StreamShowHeartModel;
import com.qboxus.musictok.Models.UserOnlineModel;
import com.qboxus.musictok.SimpleClasses.OnSwipeTouchListener;
import com.qboxus.musictok.SimpleClasses.StreamingLikes.HeartView;
import com.volley.plus.VPackages.VolleyRequest;
import com.qboxus.musictok.Interfaces.AdapterClickListener;
import com.qboxus.musictok.Interfaces.FragmentCallBack;
import com.qboxus.musictok.R;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.Variables;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.video.VideoEncoderConfiguration;

public class LiveActivity extends RtcBaseActivity implements View.OnClickListener {

    private VideoGridContainer mVideoGridContainer;
    ImageView ivChatView,ivJoinBc, ivGift;
    private VideoEncoderConfiguration.VideoDimensions mVideoDimension;
    DatabaseReference rootref;

    String userId, userName, userPicture;
    int userRole;
    EditText messageEdit;
    RelativeLayout bottom_tab_view,bottom_chat_view;
    TextView tvGiftCount,tvOtherUserLikes;
    SimpleDraweeView ivGiftCount;
    View animationCapture;
    LinearLayout tabGiftCount;
    LinearLayout tabLiveUser;
    HeartView streamLikeView;
    boolean isLikeStream=true;
    TextView tvCurrentJoin,liveUserCount;
    private Random mRandom = new Random();
    ArrayList<String> alertList=new ArrayList<>();
    View tabMainView;
    ViewFlipper viewflliper,innerViewflliper;
    RelativeLayout viewOne,viewTwo,viewThree,viewFour;
    boolean isFirstTimeFlip=true;


    RecyclerView liveUserViewRecyclerView;
    TextView tvNoViewData;
    LiveUserViewAdapter liveUserViewAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Functions.setLocale(Functions.getSharedPreference(LiveActivity.this).getString(Variables.APP_LANGUAGE_CODE,Variables.DEFAULT_LANGUAGE_CODE)
                , this, LiveActivity.class,false);
        setContentView(R.layout.activity_live_room);

        rootref = FirebaseDatabase.getInstance().getReference();
        ivChatView =findViewById(R.id.iv_chat_view);
        ivChatView.setOnClickListener(this);
        ivJoinBc =findViewById(R.id.ivJoinBc);
        ivJoinBc.setOnClickListener(this);
        ivGift =findViewById(R.id.iv_gift);
        ivGift.setOnClickListener(this);
        tabMainView=findViewById(R.id.tabMainView);
        viewflliper=findViewById(R.id.viewflliper);
        innerViewflliper=findViewById(R.id.innerViewflliper);
        viewOne=findViewById(R.id.viewOne);
        viewTwo=findViewById(R.id.viewTwo);
        viewThree=findViewById(R.id.viewThree);
        viewFour=findViewById(R.id.viewFour);
        tvNoViewData=findViewById(R.id.tvNoViewData);
        liveUserViewRecyclerView=findViewById(R.id.liveUserViewRecyclerView);
        Intent bundle = getIntent();
        if (bundle != null) {
            userId = bundle.getStringExtra("user_id");
            userName = bundle.getStringExtra("user_name");
            userPicture = bundle.getStringExtra("user_picture");

            userRole = bundle.getIntExtra("user_role", Constants.CLIENT_ROLE_BROADCASTER);
        }

        if (userRole == Constants.CLIENT_ROLE_BROADCASTER) {

            rootref.child("LiveUsers").child(userId).keepSynced(true);
            rootref.child("LiveUsers").child(userId).onDisconnect().removeValue();
            ivJoinBc.setVisibility(View.VISIBLE);
            addFirebaseNode();
            addStreamInternetConnection();
            sendLiveNotification();
            addStreamerLikesClickListener();
        }
        else
        {
            ivJoinBc.setVisibility(View.GONE);
            listenerNode();
            AddJoinNode();
            addStreamerOnlineStatus();
            addStreameeLikesClickListener();

        }
        addLikeStream();
        ListenerJoinNode();
        if(userId.equalsIgnoreCase(Variables.sharedPreferences.getString(Variables.U_ID,"")) ||
                !(Functions.getSharedPreference(LiveActivity.this).getBoolean(Variables.IsExtended, false)))
        {
            ivGift.setVisibility(View.GONE);
        }
        else {
            ivGift.setVisibility(View.VISIBLE);
        }
        tvOtherUserLikes=findViewById(R.id.tvOtherUserLikes);
        tvGiftCount=findViewById(R.id.tvGiftCount);
        ivGiftCount=findViewById(R.id.ivGiftCount);
        tabGiftCount=findViewById(R.id.tabGiftCount);
        animationCapture=findViewById(R.id.animationCapture);
        tabLiveUser=findViewById(R.id.tabLiveUser);
        tabLiveUser.setOnClickListener(this);
        liveUserCount=findViewById(R.id.liveUserCount);
        tvCurrentJoin=findViewById(R.id.tvCurrentJoin);
        bottom_tab_view=findViewById(R.id.bottom_tab_view);
        bottom_chat_view=findViewById(R.id.bottom_chat_view);
        streamLikeView=findViewById(R.id.streamLikeView);
        streamLikeView.setOnClickListener(this);

        TextView live_user_name = findViewById(R.id.live_user_name);
        live_user_name.setText(userName);
        setUpJoinRecycler();
        initCommentAdapter();
        initUI();
        initData();

        messageEdit = findViewById(R.id.edtMessage);



        findViewById(R.id.cross_btn).setOnClickListener(this);
        findViewById(R.id.tvSend).setOnClickListener(this);



        getCommentData();

        if(com.qboxus.musictok.Constants.STREAMING_LIMIT)
        {
            try {
               if (userRole == Constants.CLIENT_ROLE_BROADCASTER)
               {
                   new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                       @Override
                       public void run() {
                           LiveActivity.this.runOnUiThread(new Runnable() {
                               @Override
                               public void run() {
                                   Toast.makeText(LiveActivity.this,  getString(R.string.you_have_reached_out_of_your_streaming_limit), Toast.LENGTH_SHORT).show();
                                   finish();
                               }
                           });
                       }
                   },com.qboxus.musictok.Constants.MAX_STREMING_TIME);
               }
            }
            catch (Exception e)
            {
                Log.d(com.qboxus.musictok.Constants.tag,"Exception streaming : "+e);
            }
        }
    }


    // initailze the adapter
    ArrayList<LiveCommentModel> dataList=new ArrayList<>();
    RecyclerView recyclerView;
    LiveCommentsAdapter adapter;
    public void initCommentAdapter() {
        dataList.clear();
        recyclerView = (RecyclerView) findViewById(R.id.recylerview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(LiveActivity.this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerView.setHasFixedSize(true);

        adapter = new LiveCommentsAdapter(LiveActivity.this, dataList, new AdapterClickListener() {
            @Override
            public void onItemClick(View view, int pos, Object object) {
                LiveCommentModel itemUpdate=dataList.get(pos);
            }
        });
        recyclerView.setAdapter(adapter);

        alertList.clear();
        alertList.add(getString(R.string.streaming_instruction_one));
        alertList.add(getString(R.string.streaming_instruction_two));
        alertList.add(getString(R.string.streaming_instruction_three)+" "+getString(R.string.app_name)+". "+getString(R.string.for_more_info_check_support_services));
        int idx = mRandom.nextInt(alertList.size());
        addMessagesToList("alert",alertList.get(idx));
    }


    private void addStreameeLikesClickListener() {
        final Animation inAnim = AnimationUtils.loadAnimation(LiveActivity.this, R.anim.in_from_right);
        final Animation outAnim = AnimationUtils.loadAnimation(LiveActivity.this, R.anim.out_to_left);
        final Animation inPrevAnim = AnimationUtils.loadAnimation(LiveActivity.this, R.anim.in_from_left);
        final Animation outPrevAnim = AnimationUtils.loadAnimation(LiveActivity.this, R.anim.out_to_right);

        tabMainView.setOnTouchListener(new OnSwipeTouchListener(LiveActivity.this) {
            public void onSwipeTop() {

            }
            public void onSwipeRight() {

                viewflliper.setInAnimation(inPrevAnim);
                viewflliper.setOutAnimation(outPrevAnim);
                innerViewflliper.setInAnimation(inPrevAnim);
                innerViewflliper.setOutAnimation(outPrevAnim);

                if (viewTwo==viewflliper.getCurrentView())
                {
                    if (viewFour==innerViewflliper.getCurrentView())
                    {
                        innerViewflliper.showPrevious();
                    }
                    else
                    {
                        viewflliper.showPrevious();
                    }
                }
                else
                {
                    viewflliper.showPrevious();
                }

            }
            public void onSwipeLeft() {
                viewflliper.setInAnimation(inAnim);
                viewflliper.setOutAnimation(outAnim);
                innerViewflliper.setInAnimation(inAnim);
                innerViewflliper.setOutAnimation(outAnim);

                if (viewTwo==viewflliper.getCurrentView())
                {
                    if (viewThree==innerViewflliper.getCurrentView())
                    {
                        innerViewflliper.showNext();
                    }

                }
                else
                {
                    viewflliper.showNext();
                }


            }
            public void onSwipeBottom() {

            }

            @Override
            public void onDoubleClick(MotionEvent e) {
                if (isSendHeart)
                {
                    isSendHeart=true;
                    addLikeIntoStream();
                }
            }

            public void onSingleClick(){

            }
        });

        if (isFirstTimeFlip)
        {
            isFirstTimeFlip=false;
            if (viewOne==viewflliper.getCurrentView())
            {
                viewflliper.showNext();
            }
        }
    }

    private void addStreamerLikesClickListener() {
        final Animation inAnim = AnimationUtils.loadAnimation(LiveActivity.this, R.anim.in_from_right);
        final Animation outAnim = AnimationUtils.loadAnimation(LiveActivity.this, R.anim.out_to_left);
        final Animation inPrevAnim = AnimationUtils.loadAnimation(LiveActivity.this, R.anim.in_from_left);
        final Animation outPrevAnim = AnimationUtils.loadAnimation(LiveActivity.this, R.anim.out_to_right);

        tabMainView.setOnTouchListener(new OnSwipeTouchListener(LiveActivity.this) {
            public void onSwipeTop() {

            }
            public void onSwipeRight() {

                viewflliper.setInAnimation(inPrevAnim);
                viewflliper.setOutAnimation(outPrevAnim);
                innerViewflliper.setInAnimation(inPrevAnim);
                innerViewflliper.setOutAnimation(outPrevAnim);

                if (viewTwo==viewflliper.getCurrentView())
                {
                    if (viewFour==innerViewflliper.getCurrentView())
                    {
                        innerViewflliper.showPrevious();
                    }
                    else
                    {
                        viewflliper.showPrevious();
                    }
                }
                else
                {
                    viewflliper.showPrevious();
                }

            }
            public void onSwipeLeft() {
                viewflliper.setInAnimation(inAnim);
                viewflliper.setOutAnimation(outAnim);
                innerViewflliper.setInAnimation(inAnim);
                innerViewflliper.setOutAnimation(outAnim);
                if (viewTwo==viewflliper.getCurrentView())
                {
                    if (viewThree==innerViewflliper.getCurrentView())
                    {
                        innerViewflliper.showNext();
                    }

                }
                else
                {
                    viewflliper.showNext();
                }
            }
            public void onSwipeBottom() {

            }
            public void onDoubleClick() {

            }
            public void onSingleClick(){

            }
        });

        if (isFirstTimeFlip)
        {
            isFirstTimeFlip=false;
            if (viewOne==viewflliper.getCurrentView())
            {
                viewflliper.showNext();
            }
        }
    }


    private void setUpJoinRecycler() {
        GridLayoutManager layoutManager=new GridLayoutManager(LiveActivity.this,2);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        liveUserViewRecyclerView.setLayoutManager(layoutManager);
        liveUserViewAdapter=new LiveUserViewAdapter(jointUserList, new AdapterClickListener() {
            @Override
            public void onItemClick(View view, int pos, Object object) {

            }
        });
        liveUserViewRecyclerView.setAdapter(liveUserViewAdapter);
    }

    ArrayList<LiveUserModel> jointUserList=new ArrayList<>();
    ValueEventListener joinValueEventListener;
    private void ListenerJoinNode() {
        joinValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                jointUserList.clear();
                if (dataSnapshot.exists())
                {
                    for(DataSnapshot joinSnapsot:dataSnapshot.getChildren())
                    {
                        Log.d(com.qboxus.musictok.Constants.tag,"Data JSON : "+joinSnapsot.getValue().toString());
                        if (!(TextUtils.isEmpty(joinSnapsot.getValue().toString())))
                        {
                            LiveUserModel model=joinSnapsot.getValue(LiveUserModel.class);
                            jointUserList.add(model);
                        }

                    }
                    liveUserViewAdapter.notifyDataSetChanged();
                    if (jointUserList.size()>0)
                    {
                        LiveUserModel currentModel=jointUserList.get(0);
                        tvCurrentJoin.setText(currentModel.getUser_name()+" "+getString(R.string.joined));
                        liveUserCount.setText(""+jointUserList.size());
                    }
                    else
                    {
                        liveUserCount.setText(""+jointUserList.size());
                    }

                }
                else
                {
                    liveUserCount.setText(""+jointUserList.size());
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        rootref.child("LiveUsers").child(userId).child("JoinStream").addValueEventListener(joinValueEventListener);
    }

    public void removeJoinListener() {
        if (rootref != null && joinValueEventListener != null) {
            rootref.child("LiveUsers").child(userId).child("JoinStream").removeEventListener(joinValueEventListener);
        }
    }

    private void AddJoinNode() {
        HashMap map = new HashMap();
        map.put("user_id", Functions.getSharedPreference(LiveActivity.this).getString(Variables.U_ID, ""));
        map.put("user_name", Functions.getSharedPreference(LiveActivity.this).getString(Variables.U_NAME, ""));
        map.put("user_picture", Functions.getSharedPreference(LiveActivity.this).getString(Variables.U_PIC, ""));
        rootref.child("LiveUsers").child(userId).child("JoinStream")
                .child(Functions.getSharedPreference(LiveActivity.this).getString(Variables.U_ID, ""))
                .setValue(map);
    }



    // initialize the views of activity
    private void initUI() {

        Functions.printLog(com.qboxus.musictok.Constants.tag, "initUI");

        initUserIcon();

        boolean isBroadcaster = (userRole == Constants.CLIENT_ROLE_BROADCASTER);


        isAudioActivated=!isBroadcaster;isVideoActivated=!isBroadcaster;
        isbeautyActivated=true;
        rtcEngine().setBeautyEffectOptions(isbeautyActivated,
                com.qboxus.musictok.ActivitesFragment.LiveStreaming.Constants.DEFAULT_BEAUTY_OPTIONS);

        mVideoGridContainer = findViewById(R.id.live_video_grid_layout);
        mVideoGridContainer.setStatsManager(statsManager());
        mVideoGridContainer.setOnClickListener(this);

        rtcEngine().setClientRole(userRole);
        if (isBroadcaster) startBroadcast();
    }

    // set the user profile picture
    private void initUserIcon() {
        Functions.printLog(com.qboxus.musictok.Constants.tag, "initUserIcon");
        SimpleDraweeView iconView = findViewById(R.id.live_name_board_icon);
        if (userPicture != null && !userPicture.equals("")) {
            iconView.setController(Functions.frescoImageLoad(userPicture,iconView,false));
        }
    }

    private void initData() {
        Functions.printLog(com.qboxus.musictok.Constants.tag, "initData");
        mVideoDimension = com.qboxus.musictok.ActivitesFragment.LiveStreaming.Constants.VIDEO_DIMENSIONS[
                config().getVideoDimenIndex()];
    }



    private void startBroadcast() {
        rtcEngine().setClientRole(Constants.CLIENT_ROLE_BROADCASTER);
        SurfaceView surface = prepareRtcVideo(0, true);
        mVideoGridContainer.addUserVideoSurface(0, surface, true);
    }

    private void stopBroadcast() {
        rtcEngine().setClientRole(Constants.CLIENT_ROLE_AUDIENCE);
        removeRtcVideo(0, true);
        mVideoGridContainer.removeUserVideo(0, true);
    }



    @Override
    public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
        // Do nothing at the moment
    }

    @Override
    public void onUserJoined(int uid, int elapsed) {
        // Do nothing at the moment
    }

    @Override
    public void onUserOffline(final int uid, int reason) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                removeRemoteUser(uid);
            }
        });
    }

    @Override
    public void onFirstRemoteVideoDecoded(final int uid, int width, int height, int elapsed) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Functions.printLog(com.qboxus.musictok.Constants.tag, "onFirstRemoteVideoDecoded");
                renderRemoteUser(uid);
            }
        });
    }

    private void renderRemoteUser(int uid) {
        Functions.printLog(com.qboxus.musictok.Constants.tag, "renderRemoteUser");
        SurfaceView surface = prepareRtcVideo(uid, false);
        mVideoGridContainer.addUserVideoSurface(uid, surface, false);
    }

    private void removeRemoteUser(int uid) {
        removeRtcVideo(uid, false);
        mVideoGridContainer.removeUserVideo(uid, false);
    }

    @Override
    public void onLocalVideoStats(IRtcEngineEventHandler.LocalVideoStats stats) {
        if (!statsManager().isEnabled()) return;

        LocalStatsData data = (LocalStatsData) statsManager().getStatsData(0);
        if (data == null) return;

        data.setWidth(mVideoDimension.width);
        data.setHeight(mVideoDimension.height);
        data.setFramerate(stats.sentFrameRate);
    }

    @Override
    public void onRtcStats(IRtcEngineEventHandler.RtcStats stats) {
        Functions.printLog(com.qboxus.musictok.Constants.tag, "onRtcStats");
        if (!statsManager().isEnabled()) return;

        LocalStatsData data = (LocalStatsData) statsManager().getStatsData(0);
        if (data == null) return;

        data.setLastMileDelay(stats.lastmileDelay);
        data.setVideoSendBitrate(stats.txVideoKBitRate);
        data.setVideoRecvBitrate(stats.rxVideoKBitRate);
        data.setAudioSendBitrate(stats.txAudioKBitRate);
        data.setAudioRecvBitrate(stats.rxAudioKBitRate);
        data.setCpuApp(stats.cpuAppUsage);
        data.setCpuTotal(stats.cpuAppUsage);
        data.setSendLoss(stats.txPacketLossRate);
        data.setRecvLoss(stats.rxPacketLossRate);
    }

    // check the network quality
    @Override
    public void onNetworkQuality(int uid, int txQuality, int rxQuality) {
        Functions.printLog(com.qboxus.musictok.Constants.tag, "onNetworkQuality");
        if (!statsManager().isEnabled()) return;

        StatsData data = statsManager().getStatsData(uid);
        if (data == null) return;

        data.setSendQuality(statsManager().qualityToString(txQuality));
        data.setRecvQuality(statsManager().qualityToString(rxQuality));
    }

    @Override
    public void onRemoteVideoStats(IRtcEngineEventHandler.RemoteVideoStats stats) {
        Functions.printLog(com.qboxus.musictok.Constants.tag, "onRemoteVideoStats");
        if (!statsManager().isEnabled()) return;

        RemoteStatsData data = (RemoteStatsData) statsManager().getStatsData(stats.uid);
        if (data == null) return;

        data.setWidth(stats.width);
        data.setHeight(stats.height);
        data.setFramerate(stats.rendererOutputFrameRate);
        data.setVideoDelay(stats.delay);
    }

    @Override
    public void onRemoteAudioStats(IRtcEngineEventHandler.RemoteAudioStats stats) {
        Functions.printLog(com.qboxus.musictok.Constants.tag, "onRemoteAudioStats");
        if (!statsManager().isEnabled()) return;

        RemoteStatsData data = (RemoteStatsData) statsManager().getStatsData(stats.uid);
        if (data == null) return;

        data.setAudioNetDelay(stats.networkTransportDelay);
        data.setAudioNetJitter(stats.jitterBufferDelay);
        data.setAudioLoss(stats.audioLossRate);
        data.setAudioQuality(statsManager().qualityToString(stats.quality));
    }

    @Override
    public void finish() {
        super.finish();
        statsManager().clearAllData();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userRole == Constants.CLIENT_ROLE_BROADCASTER) {
            removeNode();
            removeJoinListener();
            removeStreamInternetConnection();
        } else {
            removeJoinListener();
            removeJoinNode();
            removeListener();
            removeStreamerOnlineStatus();
        }
        removeLikeStream();
        removeCommentListener();


    }


    ValueEventListener streamerConnectCheckListener;
    DatabaseReference streamerConnectedRef;
    private Timer streamerTimer = new Timer();
    private final long streamerDELAY = 20000;
    private void addStreamInternetConnection() {
        if (streamerConnectCheckListener ==null)
        {
            streamerConnectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");

            streamerConnectCheckListener =new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    boolean connected = snapshot.getValue(Boolean.class);
                    if (connected) {
                        Log.d(com.qboxus.musictok.Constants.tag, "connected");
                        streamerTimer.cancel();
                    } else {
                        Log.d(com.qboxus.musictok.Constants.tag, "not connected");
                        streamerTimer.cancel();
                        streamerTimer = new Timer();
                        streamerTimer.schedule(
                                new TimerTask() {
                                    @Override
                                    public void run() {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                onBackPressed();
                                            }
                                        });
                                    }
                                },
                                streamerDELAY
                        );

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.w(com.qboxus.musictok.Constants.tag, "Listener was cancelled");
                }
            };
            streamerConnectedRef.addValueEventListener(streamerConnectCheckListener);
        }

    }

    public void removeStreamInternetConnection() {
        if (streamerConnectedRef != null && streamerConnectCheckListener != null) {
            streamerConnectedRef.removeEventListener(streamerConnectCheckListener);
        }
    }


    private Timer timer = new Timer();
    private final long DELAY = 20000;
    ChildEventListener  streamerOnlineListener;
    private void addStreamerOnlineStatus() {
        if(streamerOnlineListener==null)
        {
            streamerOnlineListener=new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    if (!(TextUtils.isEmpty(snapshot.getValue().toString())))
                    {
                        UserOnlineModel itemUpdate=snapshot.getValue(UserOnlineModel.class);
                        if (userId.equalsIgnoreCase(itemUpdate.getUserId()))
                        {
                            if (timer!=null)
                            {
                                Functions.cancelIndeterminentLoader();
                                timer.cancel();
                            }
                        }

                    }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                    if (!(TextUtils.isEmpty(snapshot.getValue().toString())))
                    {
                        LiveActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                UserOnlineModel itemUpdate=snapshot.getValue(UserOnlineModel.class);
                                if (userId.equalsIgnoreCase(itemUpdate.getUserId()))
                                {
                                    Functions.showIndeterminentLoader(LiveActivity.this,itemUpdate.getUserName()+" "+LiveActivity.this.getString(R.string.single_is_week)
                                            ,false,false);
                                    timer.cancel();
                                    timer = new Timer();
                                    timer.schedule(
                                            new TimerTask() {
                                                @Override
                                                public void run() {
                                                    LiveActivity.this.runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            Functions.cancelIndeterminentLoader();
                                                            rootref.child("LiveUsers").child(userId).removeValue();
                                                        }
                                                    });
                                                }
                                            },
                                            DELAY
                                    );
                                }

                            }
                        });

                    }
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            };
            rootref.child(Variables.onlineUser).addChildEventListener(streamerOnlineListener);

        }
    }

    private void removeStreamerOnlineStatus() {
        if (rootref!=null && streamerOnlineListener != null)
        {
            rootref.child(Variables.onlineUser).removeEventListener(streamerOnlineListener);
            streamerOnlineListener =null;
        }
    }


    public void addFirebaseNode() {
        HashMap map = new HashMap();
        map.put("user_id", userId);
        map.put("user_name", userName);
        map.put("user_picture", userPicture);
        rootref.child("LiveUsers").child(userId).setValue(map);
    }

    // when user goes to offline then change the value status on firebase
    public void removeNode() {
        rootref.child("LiveUsers").child(userId).removeValue();
    }

    // when user goes to offline then change the value status on firebase
    public void removeJoinNode() {
        rootref.child("LiveUsers").child(userId).child("JoinStream")
                .child(Functions.getSharedPreference(LiveActivity.this).getString(Variables.U_ID, ""))
                .removeValue();
    }


    // check the current live user status eighter user is live or not when users goes offline this callback will hit
    ValueEventListener valueEventListener;

    public void listenerNode() {

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (!dataSnapshot.exists()) {
                    finish();
                }
                else
                {

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        rootref.child("LiveUsers").child(userId).addValueEventListener(valueEventListener);
    }

    public void removeListener() {
        if (rootref != null && valueEventListener != null) {
            rootref.child("LiveUsers").child(userId).removeEventListener(valueEventListener);
        }
    }


    @Override
    public void onBackPressed() {
        if (bottom_chat_view.getVisibility()==View.VISIBLE)
        {
            bottom_chat_view.setVisibility(View.INVISIBLE);
            bottom_tab_view.setVisibility(View.VISIBLE);
        }
        else
        {
            super.onBackPressed();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cross_btn:
            {
                onBackPressed();
            }
                break;
            case R.id.tvSend:

                if (!TextUtils.isEmpty(messageEdit.getText().toString())) {
                   addMessages("comment");
                }
                break;
            case R.id.live_video_grid_layout:
            {
                if(!(userId.equalsIgnoreCase(Variables.sharedPreferences.getString(Variables.U_ID,""))))
                {
                    if (isLikeStream)
                    {
                        addLikeComment("like");
                        isLikeStream=false;
                    }
                }

            }
            break;
            case R.id.iv_gift:
            {
                ShowGiftSheet();
            }
            break;
            case R.id.iv_chat_view:
            {
                if (bottom_chat_view.getVisibility()==View.INVISIBLE)
                {
                    bottom_chat_view.setVisibility(View.VISIBLE);
                    bottom_tab_view.setVisibility(View.INVISIBLE);
                }
                else
                {
                    bottom_chat_view.setVisibility(View.INVISIBLE);
                    bottom_tab_view.setVisibility(View.VISIBLE);
                }
            }
            break;
            case R.id.tabLiveUser:
            {

            }
            break;
            case R.id.ivJoinBc:
            {
                ShowDailogForJoinBroadcast();
            }
            break;
        }
    }


    ChildEventListener  likeValueEventListener;
    int heartCounter=0;
    private void addLikeStream() {
        if(likeValueEventListener==null)
        {

            likeValueEventListener=new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    if (snapshot.exists())
                    {
                        StreamShowHeartModel likeData=snapshot.getValue(StreamShowHeartModel.class);

                        LiveActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                heartCounter=heartCounter+1;
                                tvOtherUserLikes.setText(Functions.getSuffix(""+heartCounter)+" "+LiveActivity.this.getString(R.string.likes));
                                heartsShow();
                            }
                        });
                    }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            };
            rootref.child("LiveUsers").child(userId).child("LikesStream").addChildEventListener(likeValueEventListener);

        }
    }

    public void removeLikeStream() {
        if (rootref!=null && likeValueEventListener != null)
        {
            rootref.child("LiveUsers").child(userId).child("LikesStream").removeEventListener(likeValueEventListener);
            likeValueEventListener =null;
        }


    }
    boolean isSendHeart=true;
    private void addLikeIntoStream() {
        StreamShowHeartModel likeData=new StreamShowHeartModel();
        likeData.setUserId(""+ Functions.getSharedPreference(LiveActivity.this).getString(Variables.U_ID,""));
        likeData.setOtherUserId(""+userId);
        rootref.child("LiveUsers").child(userId).child("LikesStream").push().setValue(likeData).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isComplete())
                {
                    isSendHeart=true;
                }
            }
        });
    }


    public void heartsShow()
    {
        streamLikeView.addHeart(new Random().nextInt(6));
    }


    boolean isAudioActivated=true,isVideoActivated=true,isbeautyActivated=true;
    private void ShowDailogForJoinBroadcast() {
        final Dialog alertDialog = new Dialog(LiveActivity.this);
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.setContentView(R.layout.live_join_broadcast_view);
        alertDialog.getWindow().setBackgroundDrawable(LiveActivity.this.getDrawable(R.drawable.d_round_white_background));

        ImageView swith_camera_btn=alertDialog.findViewById(R.id.swith_camera_btn);
        ImageView live_btn_mute_audio=alertDialog.findViewById(R.id.live_btn_mute_audio);
        ImageView live_btn_beautification=alertDialog.findViewById(R.id.live_btn_beautification);
        ImageView live_btn_mute_video=alertDialog.findViewById(R.id.live_btn_mute_video);
        LinearLayout tab_cancel = alertDialog.findViewById(R.id.tab_cancel);

        live_btn_mute_audio.setActivated(!isAudioActivated);
        live_btn_mute_video.setActivated(!isVideoActivated);
        live_btn_beautification.setActivated(!isbeautyActivated);

        rtcEngine().setBeautyEffectOptions(live_btn_mute_video.isActivated(),
                com.qboxus.musictok.ActivitesFragment.LiveStreaming.Constants.DEFAULT_BEAUTY_OPTIONS);

        tab_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        swith_camera_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                rtcEngine().switchCamera();
            }
        });
        live_btn_mute_audio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                isAudioActivated=live_btn_mute_video.isActivated();
                if (!isAudioActivated) return;
                rtcEngine().muteLocalAudioStream(isAudioActivated);
                view.setActivated(!isAudioActivated);
            }
        });
        live_btn_beautification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                isbeautyActivated=view.isActivated();
                view.setActivated(!isbeautyActivated);
                rtcEngine().setBeautyEffectOptions(isbeautyActivated,
                        com.qboxus.musictok.ActivitesFragment.LiveStreaming.Constants.DEFAULT_BEAUTY_OPTIONS);
            }
        });
        live_btn_mute_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                isVideoActivated=view.isActivated();
                if (isVideoActivated) {
                    stopBroadcast();
                } else {
                    startBroadcast();
                }
                view.setActivated(!isVideoActivated);
            }
        });
        alertDialog.show();
    }

    private void ShowGiftSheet() {
        StickerGiftF giftFragment = new StickerGiftF(userId, userName, userPicture, new FragmentCallBack() {
            @Override
            public void onResponce(Bundle bundle) {
                if (bundle.getBoolean("isShow",false))
                {
                    StickerModel model= (StickerModel) bundle.getSerializable("Data");
                    String counter=bundle.getString("count");
                    addGiftComment("gift",counter,model);
                }
                else
                {
                    if (bundle.getBoolean("showCount",false))
                    {

                        StickerModel model= (StickerModel) bundle.getSerializable("Data");
                        tvGiftCount.setText(" X "+bundle.getString("count")+" "+ model.name);

                        ivGiftCount.setController(Functions.frescoImageLoad(model.image,ivGiftCount,false));

                        tabGiftCount.animate().translationY(animationCapture.getY()).setDuration(700).setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                super.onAnimationStart(animation);
                                tabGiftCount.setAlpha(1);
                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                tabGiftCount.clearAnimation();
                                tabGiftCount.animate().alpha(0).translationY(0).setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        super.onAnimationEnd(animation);
                                        tabGiftCount.clearAnimation();
                                    }
                                }).start();

                            }
                        }).start();

                    }
                }
            }
        });
        giftFragment.show(getSupportFragmentManager(), "");
    }


    SimpleDraweeView ivGiftProfile,ivGiftItem;
    LinearLayout tabGiftTitle;
    RelativeLayout tabGiftMain;
    View animationGiftCapture,animationResetAnimation;
    TextView tvGiftTitle,tvGiftCountTitle,tvSendGiftCount;
    private void ShowGiftAnimation(LiveCommentModel item) {
        ivGiftProfile=findViewById(R.id.ivGiftProfile);
        tabGiftTitle=findViewById(R.id.tabGiftTitle);
        tabGiftMain=findViewById(R.id.tabGiftMain);
        animationResetAnimation=findViewById(R.id.animationResetAnimation);
        tvGiftTitle=findViewById(R.id.tvGiftTitle);
        tvGiftCountTitle=findViewById(R.id.tvGiftCountTitle);
        ivGiftItem=findViewById(R.id.ivGiftItem);
        tvSendGiftCount=findViewById(R.id.tvSendGiftCount);
        animationGiftCapture=findViewById(R.id.animationGiftCapture);

        String[] str=item.getComment().split("=====");

        Uri imageUri = Uri.parse(str[2]);

        ivGiftProfile.setController(Functions.frescoImageLoad(item.getUserPicture(),ivGiftProfile,false));

        ivGiftItem.setController(Functions.frescoImageLoad(""+imageUri,ivGiftItem,false));
        tvGiftTitle.setText(item.getUserName());
        tvGiftCountTitle.setText(getString(R.string.gave_you_a)+" "+str[1]);
        tvSendGiftCount.setText("X "+str[0]);

        tabGiftMain.animate().alpha(1).translationX(animationGiftCapture.getX()).setDuration(3000).setListener(
                new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        tabGiftMain.animate().translationY(animationCapture.getY()).setDuration(1000).setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                tabGiftMain.clearAnimation();
                                tabGiftMain.animate().alpha(0).translationY(animationResetAnimation.getY()).translationX(animationResetAnimation.getX()).setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        super.onAnimationEnd(animation);
                                        tabGiftMain.clearAnimation();
                                    }
                                }).start();
                            }
                        }).start();
                    }


                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                        PlayGiftSound();
                    }
                }).start();
    }

    MediaPlayer player;
    Handler handler;
    private void PlayGiftSound() {
        handler=new Handler(Looper.getMainLooper());
        player = MediaPlayer.create(getApplicationContext(), R.raw.gift_tone);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setVolume(100,100);
        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
            }
        });
        handler.postDelayed(runnable,2000);
    }

    Runnable runnable=new Runnable() {
        @Override
        public void run() {
            onTuneStop();
        }
    };

    public void onTuneStop() {
        if(player!=null && player.isPlaying()){
            player.stop();
        }
        if (handler!=null)
        {
            handler.removeCallbacks(runnable);
        }
    }


    // send the comment to the live user
    public void addMessages(String type) {

        DatabaseReference dref = rootref.child("LiveUsers").child(userId).child("Chat").push();

        final String key = dref.getKey();
        String my_id = Functions.getSharedPreference(this).getString(Variables.U_ID, "");
        String my_name = Functions.getSharedPreference(this).getString(Variables.F_NAME, "") + " " + Functions.getSharedPreference(this).getString(Variables.L_NAME, "");
        String my_image = Functions.getSharedPreference(this).getString(Variables.U_PIC, "null");

        Date c = Calendar.getInstance().getTime();
        final String formattedDate = Variables.df.format(c);

        LiveCommentModel commentItem=new LiveCommentModel();
        commentItem.setKey(key);
        commentItem.setUserId(my_id);
        commentItem.setUserName(my_name);
        commentItem.setUserPicture(my_image);
        commentItem.setComment(""+messageEdit.getText().toString());
        commentItem.setType(type);
        commentItem.setCommentTime(formattedDate);
        rootref.child("LiveUsers").child(userId).child("Chat").child(key).setValue(commentItem);

        messageEdit.setText(null);

    }


    // send the comment to the live user
    public void addLikeComment(String type) {

        DatabaseReference dref = rootref.child("LiveUsers").child(userId).child("Chat").push();

        final String key = dref.getKey();
        String my_id = Functions.getSharedPreference(this).getString(Variables.U_ID, "");
        String my_name = Functions.getSharedPreference(this).getString(Variables.F_NAME, "") + " " + Functions.getSharedPreference(this).getString(Variables.L_NAME, "");
        String my_image = Functions.getSharedPreference(this).getString(Variables.U_PIC, "null");

        Date c = Calendar.getInstance().getTime();
        final String formattedDate = Variables.df.format(c);

        LiveCommentModel commentItem=new LiveCommentModel();
        commentItem.setKey(key);
        commentItem.setUserId(my_id);
        commentItem.setUserName(my_name);
        commentItem.setUserPicture(my_image);
        commentItem.setComment(my_name+" "+getString(R.string.like_this_stream));
        commentItem.setType(type);
        commentItem.setCommentTime(formattedDate);
        rootref.child("LiveUsers").child(userId).child("Chat").child(key).setValue(commentItem);

        messageEdit.setText(null);

    }


    // send the comment to the live user
    public void addGiftComment(String type, String count, StickerModel model) {

        DatabaseReference dref = rootref.child("LiveUsers").child(userId).child("Chat").push();

        final String key = dref.getKey();
        String my_id = Functions.getSharedPreference(this).getString(Variables.U_ID, "");
        String my_name = Functions.getSharedPreference(this).getString(Variables.F_NAME, "") + " " + Functions.getSharedPreference(this).getString(Variables.L_NAME, "");
        String my_image = Functions.getSharedPreference(this).getString(Variables.U_PIC, "null");

        Date c = Calendar.getInstance().getTime();
        final String formattedDate = Variables.df.format(c);

        LiveCommentModel commentItem=new LiveCommentModel();
        commentItem.setKey(key);
        commentItem.setUserId(my_id);
        commentItem.setUserName(my_name);
        commentItem.setUserPicture(my_image);
        commentItem.setComment(count+"====="+model.name+"====="+model.image);
        commentItem.setType(type);
        commentItem.setCommentTime(formattedDate);
        rootref.child("LiveUsers").child(userId).child("Chat").child(key).setValue(commentItem);

        messageEdit.setText(null);

    }


    // send the init alert to the live user
    public void addMessagesToList(String type,String message) {

        Date c = Calendar.getInstance().getTime();
        final String formattedDate = Variables.df.format(c);

        LiveCommentModel commentItem=new LiveCommentModel();
        commentItem.setKey("");
        commentItem.setUserId("");
        commentItem.setUserName("");
        commentItem.setUserPicture("");
        commentItem.setComment(message);
        commentItem.setType(type);
        commentItem.setCommentTime(formattedDate);

        dataList.add(commentItem);
        adapter.notifyDataSetChanged();
    }

    ChildEventListener childEventListener;
    Calendar current_cal;
    public void getCommentData() {
        current_cal = Calendar.getInstance();
        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                LiveCommentModel model = dataSnapshot.getValue(LiveCommentModel.class);
                dataList.add(model);

                if (Functions.checkTimeDiffernce(current_cal,model.getCommentTime()))
                {
                    if(model.getType().equalsIgnoreCase("gift"))
                    {
                        LiveActivity.super.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ShowGiftAnimation(model);
                            }
                        });
                    }
                }


                adapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(dataList.size() - 1);

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        rootref.child("LiveUsers").child(userId).child("Chat").addChildEventListener(childEventListener);
    }

    public void removeCommentListener() {
        if (rootref != null && childEventListener != null)
            rootref.child("LiveUsers").child(userId).child("Chat").removeEventListener(childEventListener);

    }


    // send notification to all of it follower when user live
    public void sendLiveNotification() {
        JSONObject params = new JSONObject();
        try {
            params.put("user_id", Functions.getSharedPreference(this).getString(Variables.U_ID, ""));
        } catch (Exception e) {
            e.printStackTrace();
        }

        VolleyRequest.JsonPostRequest(this, ApiLinks.sendLiveStreamPushNotfication, params,Functions.getHeaders(this), null);

    }



}
