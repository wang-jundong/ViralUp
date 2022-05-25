package com.qboxus.musictok.ActivitesFragment.SoundLists;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import com.qboxus.musictok.SimpleClasses.AppCompatLocaleActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.qboxus.musictok.ActivitesFragment.WatchVideosA;
import com.qboxus.musictok.Constants;
import com.qboxus.musictok.Models.HomeModel;
import com.qboxus.musictok.Adapters.MyVideosAdapter;
import com.qboxus.musictok.R;
import com.qboxus.musictok.Interfaces.AdapterClickListener;
import com.qboxus.musictok.ApiClasses.ApiLinks;
import com.volley.plus.VPackages.VolleyRequest;
import com.volley.plus.interfaces.Callback;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.PermissionUtils;
import com.qboxus.musictok.SimpleClasses.Variables;
import com.qboxus.musictok.ActivitesFragment.VideoRecording.VideoRecoderA;
import com.downloader.Error;
import com.downloader.OnDownloadListener;
import com.downloader.PRDownloader;
import com.downloader.request.DownloadRequest;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VideoSoundA extends AppCompatLocaleActivity implements View.OnClickListener {

    HomeModel item;
    TextView soundName, descriptionTxt;
    ImageView soundImage;
    File audioFile;
    RecyclerView recyclerView;
    GridLayoutManager linearLayoutManager;
    ProgressBar loadMoreProgress;
    int pageCount = 0;
    boolean ispostFinsh;
    ArrayList<HomeModel> dataList;
    MyVideosAdapter adapter;


    PermissionUtils takePermissionUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Functions.setLocale(Functions.getSharedPreference(VideoSoundA.this).getString(Variables.APP_LANGUAGE_CODE,Variables.DEFAULT_LANGUAGE_CODE)
                , this, VideoSoundA.class,false);
        setContentView(R.layout.activity_video_sound);

        Functions.makeDirectry(Functions.getAppFolder(this)+Variables.APP_HIDED_FOLDER);
        Functions.makeDirectry(Functions.getAppFolder(this)+Variables.DRAFT_APP_FOLDER);


        Intent intent = getIntent();
        if (intent.hasExtra("data")) {
            item = (HomeModel) intent.getSerializableExtra("data");
        }

        takePermissionUtils=new PermissionUtils(VideoSoundA.this,mPermissionResult);
        soundName = findViewById(R.id.sound_name);
        descriptionTxt = findViewById(R.id.description_txt);
        soundImage = findViewById(R.id.sound_image);

        if ((item.sound_name == null || item.sound_name.equals("") || item.sound_name.equals("null"))) {
            soundName.setText(getString(R.string.orignal_sound_)+" " + item.first_name + " " + item.last_name);
        } else {
            soundName.setText(item.sound_name);
        }
        descriptionTxt.setText(item.video_description);


        findViewById(R.id.back_btn).setOnClickListener(this);

        findViewById(R.id.save_btn).setOnClickListener(this);
        findViewById(R.id.create_btn).setOnClickListener(this);

        findViewById(R.id.play_btn).setOnClickListener(this);
        findViewById(R.id.pause_btn).setOnClickListener(this);


        Uri uri = Uri.parse(item.sound_pic);
        soundImage.setImageURI(uri);

        Functions.printLog(Constants.tag, item.sound_pic);
        Functions.printLog(Constants.tag, item.sound_url_acc);

        saveAudio();


        recyclerView = findViewById(R.id.recylerview);
        linearLayoutManager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(linearLayoutManager);

        dataList = new ArrayList<>();

        adapter = new MyVideosAdapter(this, dataList, new AdapterClickListener() {
            @Override
            public void onItemClick(View view, int pos, Object object) {

                HomeModel item = (HomeModel) object;
                openWatchVideo(pos);


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


        loadMoreProgress = findViewById(R.id.load_more_progress);

        pageCount = 0;
        callApi();


    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.back_btn:
                onBackPressed();
                break;
            case R.id.save_btn:

                // save the audio file in local directry
            {
                if (takePermissionUtils.isStoragePermissionGranted())
                {
                    saveAudiointoExternalStorage();
                }
                else
                {
                    takePermissionUtils.showStoragePermissionDailog(getString(R.string.we_need_storage_permission_for_save_sound));
                }

            }
                break;

            case R.id.create_btn:
                // make the video against specific sound id
                if (Functions.checkLoginUser(VideoSoundA.this)) {
                    if (audioFile != null && audioFile.exists()) {
                        stopPlaying();
                        openVideoRecording();
                    }
                }
                break;

            case R.id.play_btn:
                if (audioFile != null && audioFile.exists())
                    playaudio();

                break;

            case R.id.pause_btn:
                stopPlaying();
                break;
        }
    }

    private void saveAudiointoExternalStorage() {
        if (audioFile != null && audioFile.exists()) {
            try {

                String soundPath="";
                if (Build.VERSION.SDK_INT>Build.VERSION_CODES.P)
                {
                    soundPath=Functions.getAppFolder(VideoSoundA.this);
                }
                else
                {
                    soundPath=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath();
                }

                progressDialog = new ProgressDialog(this);
                progressDialog.setMessage(getString(R.string.please_wait_));
                progressDialog.setCancelable(false);
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
                String fileName=Functions.getRandomString(5)+Variables.SelectedAudio_AAC;

                prDownloader = PRDownloader.download(item.sound_url_acc, soundPath, fileName)
                        .build();

                String finalSoundPath = soundPath;
                prDownloader.start(new OnDownloadListener() {
                    @Override
                    public void onDownloadComplete() {
                        progressDialog.dismiss();
                        if (Build.VERSION.SDK_INT>Build.VERSION_CODES.P)
                        {
                            downloadAEAudio(finalSoundPath,fileName);
                        }
                        else
                        {
                            scanFile(finalSoundPath);
                        }

                    }

                    @Override
                    public void onError(Error error) {
                        progressDialog.dismiss();
                    }
                });


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public void downloadAEAudio(String path, String audioName) {

        ContentValues valuesaudio;
        valuesaudio = new ContentValues();
        valuesaudio.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_MUSIC);
        valuesaudio.put(MediaStore.MediaColumns.TITLE, audioName);
        valuesaudio.put(MediaStore.Audio.Media.ARTIST, "");
        valuesaudio.put(MediaStore.Audio.Media.ALBUM, "");
        valuesaudio.put(MediaStore.MediaColumns.DISPLAY_NAME, audioName);
        valuesaudio.put(MediaStore.MediaColumns.MIME_TYPE, "audio/aac");
        valuesaudio.put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis() / 1000);
        valuesaudio.put(MediaStore.MediaColumns.DATE_TAKEN, System.currentTimeMillis());
        valuesaudio.put(MediaStore.MediaColumns.IS_PENDING, 1);
        ContentResolver resolver = getContentResolver();
        Uri uriSavedAudio = resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, valuesaudio);

        ParcelFileDescriptor pfd;

        try {
            pfd = getContentResolver().openFileDescriptor(uriSavedAudio, "w");

            FileOutputStream out = new FileOutputStream(pfd.getFileDescriptor());

            File audioFile = new File(path+audioName);

            FileInputStream in = new FileInputStream(audioFile);


            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {

                out.write(buf, 0, len);
            }


            out.close();
            in.close();
            pfd.close();


            Functions.showAlert(VideoSoundA.this, getString(R.string.audio_saved), getString(R.string.this_sound_is_successfully_saved));

        } catch (Exception e) {

            e.printStackTrace();
        }


        valuesaudio.clear();
        valuesaudio.put(MediaStore.MediaColumns.IS_PENDING, 0);
        getContentResolver().update(uriSavedAudio, valuesaudio, null, null);
    }

    public void scanFile(String downloadDirectory) {

        MediaScannerConnection.scanFile(VideoSoundA.this,
                new String[]{downloadDirectory+Variables.SelectedAudio_AAC},
                null,
                new MediaScannerConnection.OnScanCompletedListener() {

                    public void onScanCompleted(String path, Uri uri) {
                        VideoSoundA.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Functions.showAlert(VideoSoundA.this, getString(R.string.audio_saved), getString(R.string.this_sound_is_successfully_saved));
                            }
                        });
                    }
                });
    }


    // get the video list sound id
    public void callApi() {


        JSONObject params = new JSONObject();
        try {

            params.put("sound_id", item.sound_id);
            params.put("starting_point", "" + pageCount);
            params.put("device_id", Functions.getSharedPreference(this).getString(Variables.DEVICE_ID, ""));


        } catch (Exception e) {
            e.printStackTrace();
        }

        VolleyRequest.JsonPostRequest(this, ApiLinks.showVideosAgainstSound, params,Functions.getHeaders(this), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.checkStatus(VideoSoundA.this,resp);
                parseVideo(resp);


            }
        });

    }


    // parse the data of the video list against sound id
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

                if (temp_list.isEmpty())
                    ispostFinsh = true;
                else {
                    dataList.addAll(temp_list);
                    adapter.notifyDataSetChanged();
                }


            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            loadMoreProgress.setVisibility(View.GONE);
        }

    }


    // open the video in full screen
    private void openWatchVideo(int postion) {
        Intent intent = new Intent(VideoSoundA.this, WatchVideosA.class);
        intent.putExtra("arraylist", dataList);
        intent.putExtra("position", postion);
        intent.putExtra("pageCount", pageCount);
        intent.putExtra("soundId", item.sound_id);
        intent.putExtra("device_id", Functions.getSharedPreference(this).getString(Variables.DEVICE_ID, ""));
        intent.putExtra("userId",Functions.getSharedPreference(VideoSoundA.this).getString(Variables.U_ID,""));
        intent.putExtra("whereFrom","videoSound");
        startActivity(intent);
    }


    // initialize the player for the audio

    SimpleExoPlayer player;

    public void playaudio() {

        DefaultTrackSelector trackSelector = new DefaultTrackSelector(this);

        player = new SimpleExoPlayer.Builder(this).
                setTrackSelector(trackSelector)
                .build();


        DataSource.Factory cacheDataSourceFactory = new DefaultDataSourceFactory(VideoSoundA.this, getString(R.string.app_name));
        MediaSource videoSource = new ProgressiveMediaSource.Factory(cacheDataSourceFactory).createMediaSource(MediaItem.fromUri(item.video_url));
        player.addMediaSource(videoSource);
        player.prepare();
        player.setPlayWhenReady(true);

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setUsage(C.USAGE_MEDIA)
                        .setContentType(C.CONTENT_TYPE_MOVIE)
                        .build();
                player.setAudioAttributes(audioAttributes, true);
            }
        }
        catch (Exception e)
        {
            Log.d(Constants.tag,"Exception audio focus : "+e);
        }

        showPlayingState();
    }


    public void stopPlaying() {
        if (player != null) {
            player.setPlayWhenReady(false);
        }
        showPauseState();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        stopPlaying();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopPlaying();
        Functions.printLog(Constants.tag, "onStop");

    }


    // show the player state
    public void showPlayingState() {
        findViewById(R.id.play_btn).setVisibility(View.GONE);
        findViewById(R.id.pause_btn).setVisibility(View.VISIBLE);
    }

    public void showPauseState() {
        findViewById(R.id.play_btn).setVisibility(View.VISIBLE);
        findViewById(R.id.pause_btn).setVisibility(View.GONE);
    }

    DownloadRequest prDownloader;
    ProgressDialog progressDialog;

    public void saveAudio() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.please_wait_));
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        prDownloader = PRDownloader.download(item.sound_url_acc, Functions.getAppFolder(this)+Variables.APP_HIDED_FOLDER, Variables.SelectedAudio_AAC)
                .build();

        prDownloader.start(new OnDownloadListener() {
            @Override
            public void onDownloadComplete() {
                progressDialog.dismiss();
                audioFile = new File(Functions.getAppFolder(VideoSoundA.this)+Variables.APP_HIDED_FOLDER + Variables.SelectedAudio_AAC);
            }

            @Override
            public void onError(Error error) {
                progressDialog.dismiss();
            }
        });


    }


    // open the camera for recording video
    public void openVideoRecording() {
        Intent intent = new Intent(VideoSoundA.this, VideoRecoderA.class);
        intent.putExtra("sound_name", soundName.getText().toString());
        intent.putExtra("sound_id", item.sound_id);
        intent.putExtra("isSelected", "yes");
        startActivity(intent);
        overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top);

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
                            blockPermissionCheck.add(Functions.getPermissionStatus(VideoSoundA.this,key));
                        }
                    }
                    if (blockPermissionCheck.contains("blocked"))
                    {
                        Functions.showPermissionSetting(VideoSoundA.this,getString(R.string.we_need_storage_permission_for_save_sound));
                    }
                    else
                    if (allPermissionClear)
                    {
                        saveAudiointoExternalStorage();
                    }

                }
            });


    @Override
    protected void onDestroy() {
        mPermissionResult.unregister();
        super.onDestroy();
    }
}
