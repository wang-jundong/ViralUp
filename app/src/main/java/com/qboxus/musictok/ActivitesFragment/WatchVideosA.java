package com.qboxus.musictok.ActivitesFragment;

import androidx.annotation.Nullable;
import com.qboxus.musictok.SimpleClasses.AppCompatLocaleActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.qboxus.musictok.ActivitesFragment.Profile.Setting.NoInternetA;
import com.qboxus.musictok.Adapters.ViewPagerStatAdapter;
import com.qboxus.musictok.ApiClasses.ApiLinks;
import com.volley.plus.VPackages.VolleyRequest;
import com.qboxus.musictok.Constants;
import com.volley.plus.interfaces.Callback;
import com.qboxus.musictok.Interfaces.FragmentCallBack;
import com.qboxus.musictok.Interfaces.InternetCheckCallback;
import com.qboxus.musictok.MainMenu.MainMenuFragment;
import com.qboxus.musictok.Models.HomeModel;
import com.qboxus.musictok.R;
import com.qboxus.musictok.Services.UploadService;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.Variables;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

import fr.castorflex.android.verticalviewpager.VerticalViewPager;
import io.paperdb.Paper;


public class WatchVideosA extends AppCompatLocaleActivity implements View.OnClickListener,FragmentCallBack {


    Context context;
    ArrayList<HomeModel> dataList=new ArrayList<>();
    SwipeRefreshLayout swiperefresh;
    int pageCount = 0;
    boolean isApiRuning = false;
    Handler handler;
    RelativeLayout uploadVideoLayout;
    ImageView uploadingThumb;
    UploadingVideoBroadCast mReceiver;
    String whereFrom="";
    String userId="";
    int currentPositon=0;

    private static ProgressBar progressBar;
    private static TextView tvProgressCount;

    //this is use for use same class functionality from different activities
    int fragmentConainerId;

    @Override
    public void onResponce(Bundle bundle) {
        if (bundle.getString("action").equalsIgnoreCase("deleteVideo"))
        {
            dataList.remove(bundle.getInt("position"));
            Log.d(Constants.tag,"notify data : "+dataList.size());
            if (dataList.size()==0)
            {
                onBackPressed();
            }
        }
    }


    private class UploadingVideoBroadCast extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (Functions.isMyServiceRunning(context, UploadService.class)) {
                uploadVideoLayout.setVisibility(View.VISIBLE);
                Bitmap bitmap = Functions.base64ToBitmap(Functions.getSharedPreference(context).getString(Variables.UPLOADING_VIDEO_THUMB, ""));
                if (bitmap != null)
                    uploadingThumb.setImageBitmap(bitmap);

            } else {
                uploadVideoLayout.setVisibility(View.GONE);
            }

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try { getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); }catch (Exception e){}
        Functions.setLocale(Functions.getSharedPreference(WatchVideosA.this).getString(Variables.APP_LANGUAGE_CODE,Variables.DEFAULT_LANGUAGE_CODE)
                , this, WatchVideosA.class,false);
        setContentView(R.layout.activity_watch_videos);
        fragmentConainerId=R.id.watchVideo_F;
        context = WatchVideosA.this;

        tvProgressCount=findViewById(R.id.tvProgressCount);
        progressBar=findViewById(R.id.progressBar);
        whereFrom=getIntent().getStringExtra("whereFrom");
        userId=getIntent().getStringExtra("userId");
        pageCount=getIntent().getIntExtra("pageCount",0);
        currentPositon=getIntent().getIntExtra("position",0);
        if (whereFrom.equalsIgnoreCase("IdVideo"))
        {
            dataList= new ArrayList<>();
            callApiForSinglevideos(getIntent().getStringExtra("video_id"));
        }
        else
        {
            dataList= (ArrayList<HomeModel>) getIntent().getSerializableExtra("arraylist");
        }



        handler = new Handler(Looper.getMainLooper());
        findViewById(R.id.goBack).setOnClickListener(this);
        swiperefresh = findViewById(R.id.swiperefresh);
        swiperefresh.setProgressViewOffset(false, 0, 200);

        swiperefresh.setColorSchemeResources(R.color.black);
        swiperefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pageCount = 0;
                dataList.clear();
                callVideoApi();
            }
        });


        uploadVideoLayout = findViewById(R.id.upload_video_layout);
        uploadingThumb = findViewById(R.id.uploading_thumb);
        mReceiver = new UploadingVideoBroadCast();
        registerReceiver(mReceiver, new IntentFilter("uploadVideo"));


        if (Functions.isMyServiceRunning(context, UploadService.class)) {
            uploadVideoLayout.setVisibility(View.VISIBLE);
            Bitmap bitmap = Functions.base64ToBitmap(Functions.getSharedPreference(context).getString(Variables.UPLOADING_VIDEO_THUMB, ""));
            if (bitmap != null)
                uploadingThumb.setImageBitmap(bitmap);
        }

        setTabs(false);
        setUpPreviousScreenData();
    }

    private void setUpPreviousScreenData() {
        for (HomeModel item : dataList) {
            pagerSatetAdapter.addFragment(new VideosListF(false, item, menuPager, this,fragmentConainerId), "");
        }
        pagerSatetAdapter.refreshStateSet(false);
        pagerSatetAdapter.notifyDataSetChanged();

        menuPager.setCurrentItem(currentPositon,true);
    }


    public static FragmentCallBack uploadingCallback=new FragmentCallBack() {
        @Override
        public void onResponce(Bundle bundle) {
            if (bundle.getBoolean("isShow"))
            {
                int currentProgress=bundle.getInt("currentpercent",0);
                if (progressBar!=null && tvProgressCount!=null)
                {

                    progressBar.setProgress(currentProgress);
                    tvProgressCount.setText(currentProgress+"%");
                }
            }
        }
    };


    // set the fragments for all the videos list
    protected VerticalViewPager menuPager;
    ViewPagerStatAdapter pagerSatetAdapter;


    public void setTabs(boolean isListSet) {

        if (isListSet)
        {
            dataList = new ArrayList<>();
        }


        pagerSatetAdapter = new ViewPagerStatAdapter(getSupportFragmentManager(), menuPager, false, this);
        menuPager =  findViewById(R.id.viewpager);
        menuPager.setAdapter(pagerSatetAdapter);
        menuPager.setOffscreenPageLimit(1);
        menuPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position==0)
                {
                    swiperefresh.setEnabled(true);
                }
                else
                {
                    swiperefresh.setEnabled(false);
                }
                if (position == 0 && (pagerSatetAdapter !=null && pagerSatetAdapter.getCount()>0)) {
                    VideosListF fragment = (VideosListF) pagerSatetAdapter.getItem(menuPager.getCurrentItem());
                    fragment.setData();
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            fragment.setPlayer(true);
                        }
                    }, 200);

                }

                Log.d(Constants.tag,"Check : check "+(position+1)+"    "+(dataList.size()-1)+"      "+(dataList.size() > 2 && (dataList.size() - 1) == position));
                Log.d(Constants.tag,"Test : Test "+(position+1)+"    "+(dataList.size()-5)+"      "+(dataList.size() > 5 && (dataList.size() - 5) == (position+1)));
                if (dataList.size() > 5 && (dataList.size() -5) == (position+1)) {
                    if(!isApiRuning) {
                        pageCount++;
                        callVideoApi();
                    }
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }


    private void callApiForSinglevideos(String videoId) {

        try {
            JSONObject parameters = new JSONObject();
            parameters.put("user_id", userId);
            parameters.put("video_id", videoId);

            VolleyRequest.JsonPostRequest(this, ApiLinks.showVideoDetail, parameters,Functions.getHeaders(this), new Callback() {
                @Override
                public void onResponce(String resp) {
                    swiperefresh.setRefreshing(false);
                    singalVideoParseData(resp);
                }
            });

        }catch (Exception e) {
            Functions.printLog(Constants.tag, e.toString());
        }
    }





    public void singalVideoParseData(String responce) {

        try {
            JSONObject jsonObject = new JSONObject(responce);
            String code = jsonObject.optString("code");
            if (code.equals("200")) {
                JSONObject msg = jsonObject.optJSONObject("msg");
                ArrayList<HomeModel> temp_list = new ArrayList<>();

                JSONObject video = msg.optJSONObject("Video");
                JSONObject user = msg.optJSONObject("User");
                JSONObject sound = msg.optJSONObject("Sound");
                JSONObject userPrivacy = user.optJSONObject("PrivacySetting");
                JSONObject pushNotification = user.optJSONObject("PushNotification");

                {
                    HomeModel item = Functions.parseVideoData(user, sound, video, userPrivacy, pushNotification);

                    if (item.username!=null && !(item.username.equals("null")))
                    {
                        temp_list.add(item);
                    }


                    if(dataList.isEmpty()){
                        setTabs(true);
                    }
                    dataList.addAll(temp_list);
                }

                for (HomeModel item : temp_list) {
                    pagerSatetAdapter.addFragment(new VideosListF(false, item, menuPager, this,fragmentConainerId), "");
                }
                pagerSatetAdapter.refreshStateSet(false);
                pagerSatetAdapter.notifyDataSetChanged();


            }

        } catch (Exception e) {
            e.printStackTrace();

            if(pageCount >0)
                pageCount--;

        } finally {
            isApiRuning = false;
        }

    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.goBack:
                onBackPressed();
                break;

        }

    }

    public void callVideoApi() {
        isApiRuning = true;

        if (whereFrom.equalsIgnoreCase("userVideo")) {

            callApiForUserVideos();
        }
        else
        if (whereFrom.equalsIgnoreCase("likedVideo")) {

            callApiForLikedVideos();
        }
        else
        if (whereFrom.equalsIgnoreCase("privateVideo")) {

            callApiForPrivateVideos();
        }
        else
        if (whereFrom.equalsIgnoreCase("tagedVideo")) {

            callApiForTagedVideos();
        }
        else
        if (whereFrom.equalsIgnoreCase("videoSound"))
        {
            callApiForSoundVideos();
        }
        else
        if (whereFrom.equalsIgnoreCase("IdVideo"))
        {
            callApiForSinglevideos(getIntent().getStringExtra("video_id"));
        }


    }

    // api for get the videos list from server
    private void callApiForSoundVideos() {
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("sound_id", getIntent().getStringExtra("soundId"));
            parameters.put("device_id", getIntent().getStringExtra("deviceId"));
            parameters.put("starting_point", "" + pageCount);

        } catch (Exception e) {
            e.printStackTrace();
        }

        VolleyRequest.JsonPostRequest(WatchVideosA.this, ApiLinks.showVideosAgainstSound, parameters,Functions.getHeaders(this), new Callback() {
            @Override
            public void onResponce(String resp) {
                swiperefresh.setRefreshing(false);
                parseSoundVideoData(resp);
            }
        });


    }

    public void parseSoundVideoData(String responce) {

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

                    temp_list.add(item);
                }

                if(dataList.isEmpty()){
                    setTabs(true);
                }
                dataList.addAll(temp_list);

                for (HomeModel item : temp_list) {
                    pagerSatetAdapter.addFragment(new VideosListF(false, item, menuPager,this,fragmentConainerId), "");
                }
                pagerSatetAdapter.refreshStateSet(false);
                pagerSatetAdapter.notifyDataSetChanged();


            }

        } catch (Exception e) {
            e.printStackTrace();

            if(pageCount >0)
                pageCount--;

        } finally {
            isApiRuning = false;
        }

    }

    // api for get the videos list from server
    private void callApiForTagedVideos() {
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("user_id", userId);
            parameters.put("hashtag", getIntent().getStringExtra("hashtag"));
            parameters.put("starting_point", "" + pageCount);


        } catch (Exception e) {
            e.printStackTrace();
        }

        VolleyRequest.JsonPostRequest(WatchVideosA.this, ApiLinks.showVideosAgainstHashtag, parameters,Functions.getHeaders(this), new Callback() {
            @Override
            public void onResponce(String resp) {
                swiperefresh.setRefreshing(false);
                parseHashtagVideoData(resp);
            }
        });


    }



    public void parseHashtagVideoData(String responce) {

        try {
            JSONObject jsonObject = new JSONObject(responce);
            String code = jsonObject.optString("code");
            if (code.equals("200")) {
                JSONArray msgArray = jsonObject.getJSONArray("msg");
                ArrayList<HomeModel> temp_list = new ArrayList<>();

                for (int i = 0; i < msgArray.length(); i++) {
                    JSONObject itemdata = msgArray.optJSONObject(i);
                    JSONObject video = itemdata.optJSONObject("Video");
                    JSONObject user = video.optJSONObject("User");
                    JSONObject sound = video.optJSONObject("Sound");
                    JSONObject userPrivacy = user.optJSONObject("PrivacySetting");
                    JSONObject userPushNotification = user.optJSONObject("PushNotification");

                    HomeModel item = Functions.parseVideoData(user, sound, video, userPrivacy, userPushNotification);

                    if (item.username!=null && !(item.username.equals("null")))
                    {
                        temp_list.add(item);
                    }



                }

                if(dataList.isEmpty()){
                    setTabs(true);
                }
                dataList.addAll(temp_list);

                for (HomeModel item : temp_list) {
                    pagerSatetAdapter.addFragment(new VideosListF(false, item, menuPager, this,fragmentConainerId), "");
                }
                pagerSatetAdapter.refreshStateSet(false);
                pagerSatetAdapter.notifyDataSetChanged();


            }

        } catch (Exception e) {
            e.printStackTrace();

            if(pageCount >0)
                pageCount--;

        } finally {
            isApiRuning = false;
        }

    }

    public void parsePrivateVideoData(String responce) {

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

                if(dataList.isEmpty()){
                    setTabs(true);
                }
                dataList.addAll(temp_list);

                for (HomeModel item : temp_list) {
                    pagerSatetAdapter.addFragment(new VideosListF(false, item, menuPager,this,fragmentConainerId), "");
                }
                pagerSatetAdapter.refreshStateSet(false);
                pagerSatetAdapter.notifyDataSetChanged();


            }

        } catch (Exception e) {
            e.printStackTrace();

            if(pageCount >0)
                pageCount--;

        } finally {
            isApiRuning = false;
        }

    }

    public void parseLikedVideoData(String responce) {

        try {
            JSONObject jsonObject = new JSONObject(responce);
            String code = jsonObject.optString("code");
            if (code.equals("200")) {
                JSONArray msgArray = jsonObject.getJSONArray("msg");
                ArrayList<HomeModel> temp_list = new ArrayList<>();


                for (int i = 0; i < msgArray.length(); i++) {
                    JSONObject itemdata = msgArray.optJSONObject(i);

                    JSONObject video = itemdata.optJSONObject("Video");
                    JSONObject user = video.optJSONObject("User");
                    JSONObject sound = video.optJSONObject("Sound");
                    JSONObject userPrivacy = user.optJSONObject("PrivacySetting");
                    JSONObject userPushNotification = user.optJSONObject("PushNotification");

                    HomeModel item = Functions.parseVideoData(user, sound, video, userPrivacy, userPushNotification);


                    if (item.username!=null && !(item.username.equals("null")))
                    {
                        temp_list.add(item);
                    }


                }

                if(dataList.isEmpty()){
                    setTabs(true);
                }
                dataList.addAll(temp_list);

                for (HomeModel item : temp_list) {
                    pagerSatetAdapter.addFragment(new VideosListF(false, item, menuPager,this,fragmentConainerId), "");
                }
                pagerSatetAdapter.refreshStateSet(false);
                pagerSatetAdapter.notifyDataSetChanged();


            }

        } catch (Exception e) {
            e.printStackTrace();

            if(pageCount >0)
                pageCount--;

        } finally {
            isApiRuning = false;
        }

    }

    public void parseMyVideoData(String responce) {

        try {
            JSONObject jsonObject = new JSONObject(responce);
            String code = jsonObject.optString("code");
            if (code.equals("200")) {
                JSONObject msg = jsonObject.optJSONObject("msg");
                ArrayList<HomeModel> temp_list = new ArrayList<>();


                JSONArray public_array = msg.optJSONArray("public");


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


                if(dataList.isEmpty()){
                    setTabs(true);
                }
                dataList.addAll(temp_list);

                for (HomeModel item : temp_list) {
                    pagerSatetAdapter.addFragment(new VideosListF(false, item, menuPager,this,fragmentConainerId), "");
                }
                pagerSatetAdapter.refreshStateSet(false);
                pagerSatetAdapter.notifyDataSetChanged();


            }

        } catch (Exception e) {
            e.printStackTrace();

            if(pageCount >0)
                pageCount--;

        } finally {
            isApiRuning = false;
        }

    }


    // api for get the videos list from server
    private void callApiForUserVideos() {
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("user_id", Functions.getSharedPreference(context).getString(Variables.U_ID, ""));

            if (!(userId.equalsIgnoreCase(Functions.getSharedPreference(context).getString(Variables.U_ID, "")))) {
                parameters.put("other_user_id", userId);
            }
            parameters.put("starting_point", "" + pageCount);

        } catch (Exception e) {
            e.printStackTrace();
        }

        VolleyRequest.JsonPostRequest(WatchVideosA.this, ApiLinks.showVideosAgainstUserID, parameters,Functions.getHeaders(this), new Callback() {
            @Override
            public void onResponce(String resp) {
                swiperefresh.setRefreshing(false);
                parseMyVideoData(resp);
            }
        });


    }

    // api for get the videos list from server
    private void callApiForLikedVideos() {
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("user_id", userId);
            parameters.put("starting_point", "" + pageCount);

        } catch (Exception e) {
            e.printStackTrace();
        }
        VolleyRequest.JsonPostRequest(WatchVideosA.this, ApiLinks.showUserLikedVideos, parameters,Functions.getHeaders(this), new Callback() {
            @Override
            public void onResponce(String resp) {
                swiperefresh.setRefreshing(false);
                parseLikedVideoData(resp);
            }
        });


    }

    // api for get the videos list from server
    private void callApiForPrivateVideos() {
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("user_id", userId);
            parameters.put("starting_point", "" + pageCount);

        } catch (Exception e) {
            e.printStackTrace();
        }
        VolleyRequest.JsonPostRequest(WatchVideosA.this, ApiLinks.showVideosAgainstUserID, parameters,Functions.getHeaders(this), new Callback() {
            @Override
            public void onResponce(String resp) {
                swiperefresh.setRefreshing(false);
                parsePrivateVideoData(resp);
            }
        });


    }



    private static int callbackVideoLisCode=3292;
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(Constants.tag,"Callback check : "+requestCode);
        if (requestCode==callbackVideoLisCode)
        {
            Bundle bundle=new Bundle();
            bundle.putBoolean("isShow",true);
            VideosListF.videoListCallback.onResponce(bundle);
        }
    }



    @Override
    protected void onStart() {
        super.onStart();
        if (pagerSatetAdapter != null && pagerSatetAdapter.getCount() > 0) {
            VideosListF fragment = (VideosListF) pagerSatetAdapter.getItem(menuPager.getCurrentItem());
            fragment.mainMenuVisibility(true);
        }
    }


    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("isShow", true);
        intent.putExtra("arraylist", dataList);
        intent.putExtra("pageCount", pageCount);
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }

    @Override
    public void onDestroy() {
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
            mReceiver = null;
        }
        super.onDestroy();

    }


    @Override
    protected void onPause() {
        super.onPause();
        if(pagerSatetAdapter !=null && pagerSatetAdapter.getCount()>0) {

            VideosListF fragment = (VideosListF) pagerSatetAdapter.getItem(menuPager.getCurrentItem());
            fragment.mainMenuVisibility(false);
        }
    }


    public void moveBack()
    {
        Intent intent = new Intent();
        intent.putExtra("isShow", true);
        setResult(RESULT_OK, intent);
        finish();
    }
}
