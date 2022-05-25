package com.qboxus.musictok.ActivitesFragment;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.danikula.videocache.HttpProxyCacheServer;
import com.downloader.Error;
import com.downloader.OnDownloadListener;
import com.downloader.OnProgressListener;
import com.downloader.PRDownloader;
import com.downloader.Progress;
import com.downloader.request.DownloadRequest;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultAllocator;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.material.tabs.TabLayout;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.qboxus.musictok.ActivitesFragment.Profile.ProfileA;
import com.qboxus.musictok.ActivitesFragment.Profile.ReportTypeA;
import com.qboxus.musictok.ActivitesFragment.SoundLists.VideoSoundA;
import com.qboxus.musictok.ActivitesFragment.VideoRecording.VideoRecoderDuetA;
import com.qboxus.musictok.Adapters.ViewPagerStatAdapter;
import com.qboxus.musictok.ApiClasses.ApiLinks;
import com.qboxus.musictok.SimpleClasses.OnSwipeTouchListener;
import com.volley.plus.VPackages.VolleyRequest;
import com.qboxus.musictok.Constants;
import com.volley.plus.interfaces.APICallBack;
import com.volley.plus.interfaces.Callback;
import com.qboxus.musictok.Interfaces.FragmentCallBack;
import com.qboxus.musictok.Interfaces.FragmentDataSend;
import com.qboxus.musictok.MainMenu.MainMenuFragment;
import com.qboxus.musictok.MainMenu.RelateToFragmentOnBack.RootFragment;
import com.qboxus.musictok.Models.HomeModel;
import com.qboxus.musictok.R;
import com.qboxus.musictok.SimpleClasses.FriendsTagHelper;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.PermissionUtils;
import com.qboxus.musictok.SimpleClasses.TicTic;
import com.qboxus.musictok.SimpleClasses.Variables;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import fr.castorflex.android.verticalviewpager.VerticalViewPager;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * A simple {@link Fragment} subclass.
 */

// this is the main view which is show all  the video in list
public class VideosListF extends RootFragment implements Player.Listener, View.OnClickListener, FragmentDataSend {

    View view;
    Context context;
    LinearLayout sideMenu,videoInfoLayout;
    VerticalViewPager menuPager;
    HomeModel item;
    FragmentCallBack fragmentCallBack;
    boolean showad;
    int fragmentContainerId;
    PermissionUtils takePermissionUtils;

    public VideosListF(boolean showad, HomeModel item, VerticalViewPager menuPager, FragmentCallBack fragmentCallBack,int fragmentContainerId) {
        this.showad=showad;
        this.item = item;
        this.menuPager=menuPager;
        this.fragmentCallBack = fragmentCallBack;
        this.fragmentContainerId=fragmentContainerId;

    }


    public VideosListF() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.item_home_layout, container, false);
        context = view.getContext();

        initializePlayer();
        initalize_views();

        return view;
    }


    TextView username, descTxt, soundName, skipBtn;
    SimpleDraweeView userPic, soundImage,thumb_image;
    ImageView varifiedBtn;
    RelativeLayout duetLayoutUsername, animateRlt, mainlayout;
    LinearLayout duetOpenVideo;
    LinearLayout likeLayout, commentLayout, sharedLayout, soundImageLayout;
    LikeButton likeImage;
    ImageView commentImage;
    TextView likeTxt, commentTxt, duetUsername;
    PlayerView playerView;
    Handler handler;
    Runnable runnable;
    Boolean animationRunning = false;
    ProgressBar pbar;

    public void initalize_views() {
        sideMenu=view.findViewById(R.id.side_menu);
        videoInfoLayout=view.findViewById(R.id.video_info_layout);
        mainlayout = view.findViewById(R.id.mainlayout);
        playerView = view.findViewById(R.id.playerview);
        duetLayoutUsername = view.findViewById(R.id.duet_layout_username);
        duetUsername = view.findViewById(R.id.duet_username);
        duetOpenVideo = view.findViewById(R.id.duet_open_video);
        username = view.findViewById(R.id.username);
        userPic = view.findViewById(R.id.user_pic);
        thumb_image=view.findViewById(R.id.thumb_image);
        soundName = view.findViewById(R.id.sound_name);
        soundImage = view.findViewById(R.id.sound_image);
        varifiedBtn = view.findViewById(R.id.varified_btn);
        likeLayout = view.findViewById(R.id.like_layout);
        likeImage = view.findViewById(R.id.likebtn);
        likeTxt = view.findViewById(R.id.like_txt);
        animateRlt = view.findViewById(R.id.animate_rlt);
        skipBtn = view.findViewById(R.id.skip_btn);
        descTxt = view.findViewById(R.id.desc_txt);
        commentLayout = view.findViewById(R.id.comment_layout);
        commentImage = view.findViewById(R.id.comment_image);
        commentTxt = view.findViewById(R.id.comment_txt);
        soundImageLayout = view.findViewById(R.id.sound_image_layout);
        sharedLayout = view.findViewById(R.id.shared_layout);
        pbar = view.findViewById(R.id.p_bar);


        duetOpenVideo.setOnClickListener(this::onClick);
        userPic.setOnClickListener(this::onClick);
        animateRlt.setOnClickListener(this::onClick);
        username.setOnClickListener(this::onClick);
        commentLayout.setOnClickListener(this::onClick);
        sharedLayout.setOnClickListener(this::onClick);
        soundImageLayout.setOnClickListener(this::onClick);

        likeImage.setOnLikeListener(new OnLikeListener() {
            @Override
            public void liked(LikeButton likeButton) {
                likeVideo(item);
            }

            @Override
            public void unLiked(LikeButton likeButton) {
                likeVideo(item);
            }
        });

        skipBtn.setOnClickListener(this::onClick);


        thumb_image.setController(Functions.frescoImageLoad(item.thum,thumb_image,false));

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                setData();
            }
        },200);

    }

    public void setData() {

        if(view==null && item!=null)
            return;
        else {


            username.setText(""+Functions.showUsername(""+item.username));

            userPic.setController(Functions.frescoImageLoad(item.profile_pic,userPic,false));

            if ((item.sound_name == null || item.sound_name.equals("") || item.sound_name.equals("null"))) {
                soundName.setText(context.getString(R.string.orignal_sound_)+" " + item.username);
                item.sound_pic = item.profile_pic;
            }
            else {
                soundName.setText(item.sound_name);
            }
            soundName.setSelected(true);


            soundImage.setController(Functions.frescoImageLoad(item.sound_pic,soundImage,false));
            descTxt.setText(item.video_description);
            FriendsTagHelper.Creator.create(getResources().getColor(R.color.colorPrimary),getResources().getColor(R.color.white), new FriendsTagHelper.OnFriendsTagClickListener() {
                @Override
                public void onFriendsTagClicked(String friendsTag) {
                    onPause();
                    if (friendsTag.contains("#"))
                    {
                        Log.d(Constants.tag,"Hash "+friendsTag);
                        if (friendsTag.charAt(0)=='#')
                        {
                            friendsTag=friendsTag.substring(1);
                            openHashtag(friendsTag);
                        }
                    }
                    else
                    if (friendsTag.contains("@"))
                    {
                        Log.d(Constants.tag,"Friends "+friendsTag);
                        if (friendsTag.charAt(0)=='@')
                        {
                            friendsTag=friendsTag.substring(1);
                            openUserProfile(friendsTag);
                        }
                    }

                }
            }).handle(descTxt);

            setLikeData();

            if (item.allow_comments != null && item.allow_comments.equalsIgnoreCase("false")) {
                commentLayout.setVisibility(View.GONE);
            } else {
                commentLayout.setVisibility(View.VISIBLE);
            }
            commentTxt.setText(Functions.getSuffix(item.video_comment_count));


            if (item.verified != null && item.verified.equalsIgnoreCase("1")) {
                varifiedBtn.setVisibility(View.VISIBLE);
            } else {
                varifiedBtn.setVisibility(View.GONE);
            }


            if (item.duet_video_id != null && !item.duet_video_id.equals("") && !item.duet_video_id.equals("0")) {
                duetLayoutUsername.setVisibility(View.VISIBLE);
                duetUsername.setText(item.duet_username);
            }


            if (Functions.getSharedPreference(context).getBoolean(Variables.IS_LOGIN, false)) {
                animateRlt.setVisibility(View.GONE);
            }

            Functions.printLog(Constants.tag, "SetData" + item.video_id);
        }
    }

    public void setLikeData() {
        if (item.liked.equals("1")) {
            likeImage.animate().start();
            likeImage.setLikeDrawable(context.getResources().getDrawable(R.drawable.ic_heart_gradient));
            likeImage.setLiked(true);
        } else {
            likeImage.setLikeDrawable(context.getResources().getDrawable(R.drawable.ic_unliked));
            likeImage.setLiked(false);
            likeImage.animate().cancel();
        }

        likeTxt.setText(Functions.getSuffix(item.like_count));

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.user_pic:
                onPause();
                openProfile(item, false);
                break;

            case R.id.username:
                onPause();
                openProfile(item, false);
                break;

            case R.id.comment_layout:
                if (Functions.checkLoginUser(getActivity()))
                {
                    openComment(item);
                }
                break;

            case R.id.animate_rlt:
                if(Functions.checkLoginUser(getActivity())){
                    animateRlt.setVisibility(View.GONE);
                    likeVideo(item);
                }
                break;

            case R.id.shared_layout:

                final VideoActionF fragment = new VideoActionF(item.video_id, new FragmentCallBack() {
                    @Override
                    public void onResponce(Bundle bundle) {
                        if (bundle.getString("action").equals("save")) {
                            saveVideo(item);

                        } else if (bundle.getString("action").equals("duet")) {
                            if (Functions.checkLoginUser(getActivity()))
                            {
                                duetVideo(item);
                            }
                        } else if (bundle.getString("action").equals("privacy")) {
                            onPause();
                            if (Functions.checkLoginUser(getActivity()))
                            {
                                openVideoSetting(item);
                            }

                        } else if (bundle.getString("action").equals("delete")) {
                            if (Functions.checkLoginUser(getActivity()))
                            {
                                deleteListVideo(item);
                            }
                        } else if (bundle.getString("action").equals("favourite")) {
                            if (Functions.checkLoginUser(getActivity()))
                            {
                                favouriteVideo(item);
                            }
                        } else if (bundle.getString("action").equals("not_intrested")) {
                            if (Functions.checkLoginUser(getActivity()))
                            {
                                notInterestVideo(item);
                            }
                        } else if (bundle.getString("action").equals("report")) {
                            if (Functions.checkLoginUser(getActivity()))
                            {
                                openVideoReport(item);
                            }
                        }


                    }
                });

                Bundle bundle = new Bundle();
                bundle.putString("videoId", item.video_id);
                bundle.putString("userId", item.user_id);
                bundle.putString("userName", item.username);
                bundle.putString("userPic", item.profile_pic);
                bundle.putString("fullName", item.first_name+" "+item.last_name);
                bundle.putSerializable("data", item);
                fragment.setArguments(bundle);
                fragment.show(getChildFragmentManager(), "");


                break;


            case R.id.sound_image_layout:
                takePermissionUtils=new PermissionUtils(getActivity(),mPermissionResult);
                if (takePermissionUtils.isCameraRecordingPermissionGranted()) {
                   openSoundByScreen();
                }
                else
                {
                    takePermissionUtils.showCameraRecordingPermissionDailog(view.getContext().getString(R.string.we_need_camera_and_recording_permission_for_make_video_on_sound));
                }
                break;

            case R.id.duet_open_video:
            {

            }
                openDuetVideo(item);
                break;

            case R.id.skip_btn:
                hideAd();
                break;
        }

    }

    private void openSoundByScreen() {
        Intent intent = new Intent(view.getContext(), VideoSoundA.class);
        intent.putExtra("data", item);
        startActivity(intent);
    }

    private void deleteListVideo(HomeModel item) {
        Functions.showLoader(context, false, false);
        Functions.callApiForDeleteVideo(getActivity(), item.video_id, new APICallBack() {
            @Override
            public void arrayData(ArrayList arrayList) {
                //return data in case of array list
            }

            @Override
            public void onSuccess(String responce) {
                ViewPagerStatAdapter pagerAdapter= (ViewPagerStatAdapter) menuPager.getAdapter();
                Bundle bundle = new Bundle();
                bundle.putString("action", "deleteVideo");
                bundle.putInt("position", menuPager.getCurrentItem());
                fragmentCallBack.onResponce(bundle);
                pagerAdapter.refreshStateSet(true);
                pagerAdapter.removeFragment(menuPager.getCurrentItem());
                pagerAdapter.refreshStateSet(false);
            }

            @Override
            public void onFail(String responce) {
            }
        });
    }

    private void openVideoSetting(HomeModel item) {
        Intent intent=new Intent(view.getContext(),PrivacyVideoSettingA.class);
        intent.putExtra("video_id", item.video_id);
        intent.putExtra("privacy_value", item.privacy_type);
        intent.putExtra("duet_value", item.allow_duet);
        intent.putExtra("comment_value", item.allow_comments);
        intent.putExtra("duet_video_id", item.duet_video_id);
        resultVideoSettingCallback.launch(intent);
        getActivity().overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top);
    }


    ActivityResultLauncher<Intent> resultVideoSettingCallback = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK ) {
                        Intent data = result.getData();
                        if (data.getBooleanExtra("isShow",false))
                        {
                            callApiForSinglevideos();
                        }

                    }
                }
            });


    // initlize the player for play video
    private void initializePlayer() {
        if(exoplayer==null && item!=null){

            ExecutorService executorService= Executors.newSingleThreadExecutor();
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    LoadControl loadControl = new DefaultLoadControl.Builder()
                            .setAllocator(new DefaultAllocator(true, 16))
                            .setBufferDurationsMs(1 * 1024, 1 * 1024, 500, 1024)
                            .setTargetBufferBytes(-1)
                            .setPrioritizeTimeOverSizeThresholds(true)
                            .build();



                    DefaultTrackSelector trackSelector = new DefaultTrackSelector(context);
                    try {
                    exoplayer = new SimpleExoPlayer.Builder(context).
                            setTrackSelector(trackSelector)
                            .setLoadControl(loadControl)
                            .build();


                        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(view.getContext(), context.getString(R.string.app_name));
                        MediaSource videoSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(item.video_url));
                        exoplayer.setThrowsWhenUsingWrongThread(false);
                        exoplayer.addMediaSource(videoSource);
                        exoplayer.prepare();
                        exoplayer.addListener(VideosListF.this);
                        exoplayer.setRepeatMode(Player.REPEAT_MODE_ALL);


                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                                    .setUsage(C.USAGE_MEDIA)
                                    .setContentType(C.CONTENT_TYPE_MOVIE)
                                    .build();
                            exoplayer.setAudioAttributes(audioAttributes, true);
                        }
                    }
                    catch (Exception e)
                    {
                        Log.d(Constants.tag,"Exception audio focus : "+e);
                    }

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            playerView = view.findViewById(R.id.playerview);
                            playerView.findViewById(R.id.exo_play).setVisibility(View.GONE);
                            if(exoplayer!=null) {
                                playerView.setPlayer(exoplayer);
                            }
                        }
                    });

                }
            });

        }

    }

    public void setPlayer(boolean isVisibleToUser) {

        if (exoplayer != null) {

            if(exoplayer!=null) {
                if (isVisibleToUser)
                {
                    exoplayer.setPlayWhenReady(true);
                }
                else
                {
                    exoplayer.setPlayWhenReady(false);
                    playerView.findViewById(R.id.exo_play).setAlpha(1);
                }


            }
            playerView.setOnTouchListener(new OnSwipeTouchListener(context) {

                public void onSwipeLeft() {
                    openProfile(item, true);
                }

                @Override
                public void onLongClick() {
                    if(isVisibleToUser)
                    {
                        showVideoOption(item);
                    }
                }

                @Override
                public void onSingleClick() {
                    if (!exoplayer.getPlayWhenReady()) {
                        exoplayer.setPlayWhenReady(true);
                        playerView.findViewById(R.id.exo_play).setAlpha(0);
                        countdownTimer(true);
                    } else {
                        countdownTimer(false);
                        exoplayer.setPlayWhenReady(false);
                        playerView.findViewById(R.id.exo_play).setAlpha(1);
                    }
                }

                @Override
                public void onDoubleClick(MotionEvent e) {
                    if (!exoplayer.getPlayWhenReady()) {
                        exoplayer.setPlayWhenReady(true);
                    }
                    if (Functions.checkLoginUser(getActivity()))
                    {
                        if (!animationRunning) {

                            if (handler != null && runnable != null) {
                                handler.removeCallbacks(runnable);

                            }
                            handler = new Handler(Looper.getMainLooper());
                            runnable = new Runnable() {
                                public void run() {
                                    if (!(item.liked.equalsIgnoreCase("1")))
                                    {
                                        likeVideo(item);
                                    }
                                    showHeartOnDoubleTap(item, mainlayout, e);

                                }
                            };
                            handler.postDelayed(runnable, 200);


                        }
                    }
                }

            });

            if ((item.promote != null && item.promote.equals("1")) && showad)
            {
                item.promote="0";
                showAd();
            }
            else
            {
                hideAd();
            }

        }

    }


    public void updateVideoView(){
        if (Functions.getSharedPreference(context).getBoolean(Variables.IS_LOGIN, false))
        {
            Functions.callApiForUpdateView(getActivity(), item.video_id);
        }
//        callApiForSinglevideos();
    }
    // show a video as a ad
    boolean  isAddAlreadyShow;
    public void showAd() {


        playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
        soundImageLayout.setAnimation(null);
        sideMenu.setVisibility(View.GONE);videoInfoLayout.setVisibility(View.GONE);soundImageLayout.setVisibility(View.GONE);
//        sideMenu.animate().alpha(0).setDuration(400).start();
//        soundImageLayout.setAnimation(null);
//        soundImageLayout.animate().alpha(0).setDuration(400).start();
//        videoInfoLayout.animate().alpha(0).setDuration(400).start();
        skipBtn.setVisibility(View.VISIBLE);

        Bundle bundle = new Bundle();
        bundle.putString("action", "showad");
        fragmentCallBack.onResponce(bundle);

        countdownTimer(true);

    }



    CountDownTimer countDownTimer;
    public void countdownTimer(boolean starttimer) {

        if (countDownTimer != null)
            countDownTimer.cancel();

        if (view.findViewById(R.id.skip_btn).getVisibility() == View.VISIBLE) {

            if (starttimer) {
                countDownTimer = new CountDownTimer(100000, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {

                        if (exoplayer!=null)
                        {
                            if (exoplayer.getCurrentPosition() > 7000) {

                                hideAd();
                                countdownTimer(false);

                            }
                        }

                    }

                    @Override
                    public void onFinish() {
                        hideAd();
                    }
                };
                countDownTimer.start();
            }

        }

    }

    // hide the ad of video after some time
    public void hideAd() {
        isAddAlreadyShow = true;
        sideMenu.setVisibility(View.VISIBLE);videoInfoLayout.setVisibility(View.VISIBLE);soundImageLayout.setVisibility(View.VISIBLE);
//        sideMenu.animate().alpha(1).setDuration(400).start();
//        videoInfoLayout.animate().alpha(1).setDuration(400).start();
//        soundImageLayout.animate().alpha(1).setDuration(400).start();
        Animation aniRotate = AnimationUtils.loadAnimation(context, R.anim.d_clockwise_rotation);
        soundImageLayout.startAnimation(aniRotate);

        skipBtn.setVisibility(View.GONE);

        Bundle bundle = new Bundle();
        bundle.putString("action", "hidead");
        fragmentCallBack.onResponce(bundle);
    }



    boolean isVisibleToUser;
    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        isVisibleToUser = visible;

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {

                if (exoplayer != null && visible) {
                    setPlayer(isVisibleToUser);
                    updateVideoView();
                }

            }
        },200);

    }



    public void mainMenuVisibility(boolean isvisible) {

        if (exoplayer != null && isvisible) {
            exoplayer.setPlayWhenReady(true);
        }

        else if (exoplayer != null && !isvisible) {
            exoplayer.setPlayWhenReady(false);
            playerView.findViewById(R.id.exo_play).setAlpha(1);
        }


    }


    // when we swipe for another video this will relaese the privious player
    SimpleExoPlayer exoplayer;
    public void releasePriviousPlayer() {
        if (exoplayer != null) {
            exoplayer.removeListener(this);
            exoplayer.release();
            exoplayer = null;
        }
    }


    @Override
    public void onDestroy() {
        releasePriviousPlayer();
        super.onDestroy();

    }


    private void openDuetVideo(HomeModel item) {
        Intent intent = new Intent(view.getContext(), WatchVideosA.class);
        intent.putExtra("video_id", item.duet_video_id);
        intent.putExtra("position", 0);
        intent.putExtra("pageCount", 0);
        intent.putExtra("userId",Functions.getSharedPreference(view.getContext()).getString(Variables.U_ID,""));
        intent.putExtra("whereFrom","IdVideo");
        startActivity(intent);
    }

    // this will open the profile of user which have uploaded the currenlty running video
    private void openHashtag(String tag) {

        Intent intent=new Intent(view.getContext(),TagedVideosA.class);
        intent.putExtra("tag", tag);
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top);
    }


    // this will open the profile of user which have uploaded the currenlty running video
    private void openUserProfile(String tag) {

        Intent intent=new Intent(view.getContext(),ProfileA.class);
        intent.putExtra("user_name", tag);
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top);
    }


    @Override
    public void onPause() {
        super.onPause();
        if (exoplayer != null) {
            exoplayer.setPlayWhenReady(false);
            playerView.findViewById(R.id.exo_play).setAlpha(1);
        }
    }


    @Override
    public void onStop() {
        super.onStop();
        if (exoplayer != null) {
            exoplayer.setPlayWhenReady(false);
            playerView.findViewById(R.id.exo_play).setAlpha(1);
        }
    }



    // handle that call on the player state change
    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

        if (playbackState == Player.STATE_BUFFERING) {
            pbar.setVisibility(View.VISIBLE);
        }
        else if (playbackState == Player.STATE_READY) {
            thumb_image.setVisibility(View.GONE);

            pbar.setVisibility(View.GONE);
        }


    }


    // show a heart animation on double tap
    public boolean showHeartOnDoubleTap(HomeModel item, final RelativeLayout mainlayout, MotionEvent e) {
        try {

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int x = (int) e.getX() - 100;
                    int y = (int) e.getY() - 100;
                    RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.WRAP_CONTENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT);
                    final ImageView iv = new ImageView(getApplicationContext());
                    lp.setMargins(x, y, 0, 0);
                    iv.setLayoutParams(lp);
                    if (item.liked.equals("1")){
                        iv.setImageDrawable(getResources().getDrawable(
                                R.drawable.ic_like_fill));
                    }
                    mainlayout.addView(iv);
                    iv.animate().alpha(0).translationY(-200).setDuration(500).setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            if (iv!=null)
                                mainlayout.removeView(iv);
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {
                            super.onAnimationCancel(animation);
                            if (iv!=null)
                            mainlayout.removeView(iv);
                        }
                    }).start();
                }
            });

        }
        catch (Exception excep)
        {
         Log.d(Constants.tag,"Exception : "+excep);
        }

        return true;
    }


    // this function will call for like the video and Call an Api for like the video
    public void likeVideo(final HomeModel home_model) {
        String action = home_model.liked;

        if (action.equals("1")) {
            action = "0";
            home_model.like_count = "" + (Functions.parseInterger(home_model.like_count) - 1);
        } else {
            action = "1";
            home_model.like_count = "" + (Functions.parseInterger(home_model.like_count) + 1);
        }

        home_model.liked = action;

        setLikeData();

        Functions.callApiForLikeVideo(getActivity(), home_model.video_id, action, null);

    }


    // this will open the comment screen
    private void openComment(HomeModel item) {

        int comment_counnt = Functions.parseInterger(item.video_comment_count);

        FragmentDataSend fragment_data_send = this;

        CommentF comment_f = new CommentF(comment_counnt, fragment_data_send);
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.in_from_bottom, R.anim.out_to_top, R.anim.in_from_top, R.anim.out_from_bottom);
        Bundle args = new Bundle();
        args.putString("video_id", item.video_id);
        args.putString("user_id", item.user_id);
        args.putSerializable("data", item);
        comment_f.setArguments(args);
        transaction.addToBackStack(null);
        transaction.replace(fragmentContainerId, comment_f).commit();

    }



    public static FragmentCallBack videoListCallback;
    // this will open the profile of user which have uploaded the currenlty running video
    private void openProfile(HomeModel item, boolean from_right_to_left) {

        if (Variables.sharedPreferences.getString(Variables.U_ID, "0").equals(item.user_id)) {

            TabLayout.Tab profile = MainMenuFragment.tabLayout.getTabAt(4);
            profile.select();

        }

        else {


            videoListCallback=new FragmentCallBack() {
                @Override
                public void onResponce(Bundle bundle) {
                    if (bundle.getBoolean("isShow"))
                    {
                        callApiForSinglevideos();
                    }
                }
            };

            Intent intent=new Intent(view.getContext(), ProfileA.class);
            intent.putExtra("user_id", item.user_id);
            intent.putExtra("user_name", item.username);
            intent.putExtra("user_pic", item.profile_pic);
            resultCallback.launch(intent);
            if (from_right_to_left)
            {
                getActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }
            else
            {
                getActivity().overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top);
            }

        }

    }


    ActivityResultLauncher<Intent> resultCallback = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK ) {
                        Intent data = result.getData();
                        if (data.getBooleanExtra("isShow",false))
                        {
                            callApiForSinglevideos();
                        }
                    }
                }
            });


    // show the diolge of video options
   private void showVideoOption(final HomeModel homeModel) {


        final Dialog alertDialog = new Dialog(context);
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.setContentView(R.layout.alert_label_editor);
        alertDialog.getWindow().setBackgroundDrawable(context.getResources().getDrawable(R.drawable.d_round_white_background));

        RelativeLayout btn_add_to_fav = alertDialog.findViewById(R.id.btn_add_to_fav);
        RelativeLayout btn_not_insterested = alertDialog.findViewById(R.id.btn_not_insterested);
        RelativeLayout btn_report = alertDialog.findViewById(R.id.btn_report);
        RelativeLayout btnDelete=alertDialog.findViewById(R.id.btnDelete);

        TextView fav_unfav_txt = alertDialog.findViewById(R.id.fav_unfav_txt);


        if (homeModel.favourite != null && homeModel.favourite.equals("1"))
            fav_unfav_txt.setText(context.getString(R.string.added_to_favourite));
        else
            fav_unfav_txt.setText(context.getString(R.string.add_to_favourite));


        if (homeModel.user_id.equalsIgnoreCase(Functions.getSharedPreference(context).getString(Variables.U_ID, ""))) {
            btn_report.setVisibility(View.GONE);
            btn_not_insterested.setVisibility(View.GONE);
            btnDelete.setVisibility(View.VISIBLE);
        }


        btn_add_to_fav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                if (Functions.checkLoginUser(getActivity())) {
                    favouriteVideo(item);
                }
            }
        });


        btn_not_insterested.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                alertDialog.dismiss();
                if (Functions.checkLoginUser(getActivity()))
                {
                    notInterestVideo(item);
                }
            }
        });


        btn_report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                if (Functions.checkLoginUser(getActivity()))
                {
                    openVideoReport(item);
                }
            }
        });

       btnDelete.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               alertDialog.dismiss();
               if (Functions.checkLoginUser(getActivity()))
               {
                   deleteListVideo(item);
               }
           }
       });

        alertDialog.show();
    }



    // this method will be favourite the video
    public void favouriteVideo(final HomeModel item) {

        JSONObject params = new JSONObject();
        try {
            params.put("video_id", item.video_id);
            params.put("user_id", Variables.sharedPreferences.getString(Variables.U_ID, ""));

        } catch (Exception e) {
            e.printStackTrace();
        }

        Functions.showLoader(context, false, false);
        VolleyRequest.JsonPostRequest(getActivity(), ApiLinks.addVideoFavourite, params,Functions.getHeaders(getActivity()), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.checkStatus(getActivity(),resp);
                Functions.cancelLoader();

                try {
                    JSONObject jsonObject = new JSONObject(resp);

                    String code = jsonObject.optString("code");
                    if (code.equals("200")) {
                        Functions.showToast(context, "Successfully added to your favourite list!");
                        if (item.favourite != null && item.favourite.equals("0"))
                            item.favourite = "1";
                        else
                            item.favourite = "0";

                        setData();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        });


    }

    // call the api if a user is not intersted the video then the video will not show again to him/her
    public void notInterestVideo(final HomeModel item) {

        JSONObject params = new JSONObject();
        try {
            params.put("video_id", item.video_id);
            params.put("user_id", Variables.sharedPreferences.getString(Variables.U_ID, ""));

        } catch (Exception e) {
            e.printStackTrace();
        }


        Functions.showLoader(context, false, false);
        VolleyRequest.JsonPostRequest(getActivity(), ApiLinks.notInterestedVideo, params,Functions.getHeaders(getActivity()), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.checkStatus(getActivity(),resp);
                Functions.cancelLoader();
                try {
                    JSONObject jsonObject = new JSONObject(resp);
                    String code = jsonObject.optString("code");
                    if (code.equals("200")) {
                        ViewPagerStatAdapter pagerAdapter= (ViewPagerStatAdapter) menuPager.getAdapter();
                        Bundle bundle = new Bundle();
                        bundle.putString("action", "removeList");
                        fragmentCallBack.onResponce(bundle);
                        pagerAdapter.refreshStateSet(true);
                        pagerAdapter.removeFragment(menuPager.getCurrentItem());
                        pagerAdapter.refreshStateSet(false);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        });


    }

    public void openVideoReport(HomeModel home_model) {
        onPause();
        Intent intent=new Intent(view.getContext(), ReportTypeA.class);
        intent.putExtra("video_id", home_model.video_id);
        intent.putExtra("isFrom",false);
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top);
    }


    // save the video in to local directory
    public void saveVideo(final HomeModel item) {

        JSONObject params = new JSONObject();
        try {
            params.put("video_id", item.video_id);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Functions.showLoader(context, false, false);
        VolleyRequest.JsonPostRequest(getActivity(), ApiLinks.downloadVideo, params,Functions.getHeaders(getActivity()), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.checkStatus(getActivity(),resp);
                Functions.cancelLoader();
                try {
                    JSONObject responce = new JSONObject(resp);
                    String code = responce.optString("code");
                    if (code.equals("200")) {
                        final String download_url = responce.optString("msg");
                        if (download_url != null) {

                            String downloadDirectory="";
                            if (Build.VERSION.SDK_INT>Build.VERSION_CODES.P)
                            {
                                downloadDirectory=Functions.getAppFolder(view.getContext());
                            }
                            else
                            {
                                downloadDirectory=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath()+"/Camera/";
                            }

                            File file=new File(downloadDirectory);
                            if (!(file.exists()))
                            {
                                Log.d(Constants.tag,"Camera directory created again");
                                file.mkdirs();
                            }

                            Functions.showDeterminentLoader(context, false, false);
                            PRDownloader.initialize(getActivity().getApplicationContext());
                            DownloadRequest prDownloader = PRDownloader.download(Constants.BASE_URL + download_url, downloadDirectory, item.video_id + ".mp4")
                                    .build()

                                    .setOnProgressListener(new OnProgressListener() {
                                        @Override
                                        public void onProgress(Progress progress) {

                                            int prog = (int) ((progress.currentBytes * 100) / progress.totalBytes);
                                            Functions.showLoadingProgress(prog);

                                        }
                                    });


                            String finalDownloadDirectory = downloadDirectory;
                            prDownloader.start(new OnDownloadListener() {
                                @Override
                                public void onDownloadComplete() {
                                    Functions.cancelDeterminentLoader();
                                    if (Build.VERSION.SDK_INT>Build.VERSION_CODES.P)
                                    {
                                        downloadAEVideo(finalDownloadDirectory,item.video_id + ".mp4");
                                    }
                                    else
                                    {
                                        deleteWaterMarkeVideo(download_url);
                                        scanFile(finalDownloadDirectory);
                                    }
                                }

                                @Override
                                public void onError(Error error) {

                                    Functions.printLog(Constants.tag, "Error : "+error.getConnectionException());
                                    Functions.cancelDeterminentLoader();
                                }


                            });



                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        });
    }


    public void downloadAEVideo(String path, String videoName) {

        ContentValues valuesvideos;
        valuesvideos = new ContentValues();
        valuesvideos.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM+File.separator+"Camera");
        valuesvideos.put(MediaStore.MediaColumns.TITLE, videoName);
        valuesvideos.put(MediaStore.MediaColumns.DISPLAY_NAME, videoName);
        valuesvideos.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");
        valuesvideos.put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis() / 1000);
        valuesvideos.put(MediaStore.MediaColumns.DATE_TAKEN, System.currentTimeMillis());
        valuesvideos.put(MediaStore.MediaColumns.IS_PENDING, 1);
        ContentResolver resolver = getActivity().getContentResolver();
        Uri collection = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        Uri uriSavedVideo = resolver.insert(collection, valuesvideos);

        ParcelFileDescriptor pfd;

        try {
            pfd = getActivity().getContentResolver().openFileDescriptor(uriSavedVideo, "w");

            FileOutputStream out = new FileOutputStream(pfd.getFileDescriptor());

            File imageFile = new File(path+videoName);

            FileInputStream in = new FileInputStream(imageFile);


            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {

                out.write(buf, 0, len);
            }


            out.close();
            in.close();
            pfd.close();



        } catch (Exception e) {

            e.printStackTrace();
        }


        valuesvideos.clear();
        valuesvideos.put(MediaStore.MediaColumns.IS_PENDING, 0);
        getActivity().getContentResolver().update(uriSavedVideo, valuesvideos, null, null);
    }



    public void deleteWaterMarkeVideo(String video_url) {

        JSONObject params = new JSONObject();
        try {
            params.put("video_url", video_url);
        } catch (Exception e) {
            e.printStackTrace();
        }

        VolleyRequest.JsonPostRequest(getActivity(), ApiLinks.deleteWaterMarkVideo, params, Functions.getHeaders(getActivity()),null);


    }


    public void scanFile(String downloadDirectory) {

        MediaScannerConnection.scanFile(getActivity(),
                new String[]{downloadDirectory+item.video_id + ".mp4"},
                null,
                new MediaScannerConnection.OnScanCompletedListener() {

                    public void onScanCompleted(String path, Uri uri) {
                    }
                });
    }



    // download the video for duet with
    public void duetVideo(final HomeModel item) {

        Functions.printLog(Constants.tag, item.video_url);
        if (item.video_url != null) {

            String deletePath=Functions.getAppFolder(getActivity())+item.video_id + ".mp4";
            File deleteFile=new File(deletePath);
            if (deleteFile.exists())
            {
                openDuetRecording(item);
                return;
            }
            Functions.showDeterminentLoader(context, false, false);
            PRDownloader.initialize(getActivity().getApplicationContext());
            DownloadRequest prDownloader = PRDownloader.download(item.video_url, Functions.getAppFolder(getActivity()), item.video_id + ".mp4")
                    .build()
                    .setOnProgressListener(new OnProgressListener() {
                        @Override
                        public void onProgress(Progress progress) {
                            int prog = (int) ((progress.currentBytes * 100) / progress.totalBytes);
                            Functions.showLoadingProgress(prog);

                        }
                    });


            prDownloader.start(new OnDownloadListener() {
                @Override
                public void onDownloadComplete() {
                    Functions.cancelDeterminentLoader();

                    openDuetRecording(item);

                }

                @Override
                public void onError(Error error) {

                    Functions.printLog(Constants.tag, "Error : "+error.getConnectionException());
                    Functions.cancelDeterminentLoader();
                }


            });

        }

    }


    public void openDuetRecording(HomeModel item) {

        Intent intent = new Intent(getActivity(), VideoRecoderDuetA.class);
        intent.putExtra("data", item);
        startActivity(intent);

    }


    // call api for refersh the video details
    private void callApiForSinglevideos() {

        JSONObject parameters = new JSONObject();
        try {
            if (Variables.sharedPreferences.getString(Variables.U_ID, null) != null)
                parameters.put("user_id", Variables.sharedPreferences.getString(Variables.U_ID, "0"));

            parameters.put("video_id", item.video_id);

        }

        catch (Exception e) {
            e.printStackTrace();
        }


        VolleyRequest.JsonPostRequest(getActivity(), ApiLinks.showVideoDetail, parameters,Functions.getHeaders(getActivity()), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.checkStatus(getActivity(),resp);
                singalVideoParseData(resp);
            }
        });

    }

    // parse the data for a video
    public void singalVideoParseData(String responce) {

        try {
            JSONObject jsonObject = new JSONObject(responce);
            String code = jsonObject.optString("code");
            if (code.equals("200")) {
                JSONObject msg = jsonObject.optJSONObject("msg");

                JSONObject video = msg.optJSONObject("Video");
                JSONObject user = msg.optJSONObject("User");
                JSONObject sound = msg.optJSONObject("Sound");
                JSONObject userprivacy = user.optJSONObject("PrivacySetting");
                JSONObject userPushNotification = user.optJSONObject("PushNotification");

                item = Functions.parseVideoData(user, sound, video, userprivacy, userPushNotification);
                setData();

            } else {
                Functions.showToast(getActivity(), jsonObject.optString("msg"));
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

    }

    @Override
    public void onDataSent(String yourData) {
        int comment_count = Functions.parseInterger(yourData);
        item.video_comment_count = "" + comment_count;
        commentTxt.setText(Functions.getSuffix(item.video_comment_count));
    }



    private ActivityResultLauncher<String[]> mPermissionResult = registerForActivityResult(
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
                            blockPermissionCheck.add(Functions.getPermissionStatus(getActivity(),key));
                        }
                    }
                    if (blockPermissionCheck.contains("blocked"))
                    {
                        Functions.showPermissionSetting(view.getContext(),view.getContext().getString(R.string.we_need_camera_and_recording_permission_for_make_video_on_sound));
                    }
                    else
                    if (allPermissionClear)
                    {
                        openSoundByScreen();
                    }

                }
            });



    @Override
    public void onDetach() {
        super.onDetach();
        mPermissionResult.unregister();
    }
}
