package com.qboxus.musictok.ActivitesFragment.VideoRecording;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import com.qboxus.musictok.SimpleClasses.AppCompatLocaleActivity;

import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.util.Log;
import com.qboxus.musictok.Constants;
import com.qboxus.musictok.R;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.Variables;
import com.qboxus.musictok.ActivitesFragment.SoundLists.SoundListMainA;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import java.io.File;
import java.io.IOException;

public class GallerySelectedVideoA extends AppCompatLocaleActivity implements View.OnClickListener, Player.Listener {

    String path;
    TextView addSoundTxt;
    String draftFile, isSelected;
    String soundFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Functions.setLocale(Functions.getSharedPreference(GallerySelectedVideoA.this).getString(Variables.APP_LANGUAGE_CODE,Variables.DEFAULT_LANGUAGE_CODE)
                , this, GallerySelectedVideoA.class,false);
        hideNavigation();
        setContentView(R.layout.activity_gallery_selected_video);

        Intent intent = getIntent();
        if (intent != null) {
            path = intent.getStringExtra("video_path");
            draftFile = intent.getStringExtra("draft_file");
        }

        Variables.selectedSoundId = "null";

        findViewById(R.id.goBack).setOnClickListener(this);

        addSoundTxt = findViewById(R.id.add_sound_txt);
        addSoundTxt.setOnClickListener(this);

        findViewById(R.id.next_btn).setOnClickListener(this);

        setPlayer();


        if (getIntent().hasExtra("sound_name"))
        {
            isSelected = getIntent().getStringExtra("isSelected");
            if (isSelected.equals("yes")) {
                addSoundTxt.setText(getIntent().getStringExtra("sound_name"));
                Variables.selectedSoundId = getIntent().getStringExtra("sound_id");
                soundFilePath = getIntent().getStringExtra("outputFile");
                videoPlayer.setDeviceMuted(true);
                preparedAudio();
            }
        }

    }

    // this will call when swipe for another video and
    // this function will set the player to the current video
    SimpleExoPlayer videoPlayer;

    public void setPlayer() {


        DefaultTrackSelector trackSelector = new DefaultTrackSelector(this);

        videoPlayer = new SimpleExoPlayer.Builder(this).
                setTrackSelector(trackSelector)
                .build();

        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(GallerySelectedVideoA.this, getString(R.string.app_name));
        MediaSource videoSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(path));
        videoPlayer.setThrowsWhenUsingWrongThread(false);
        videoPlayer.addMediaSource(videoSource);
        videoPlayer.prepare();

        videoPlayer.setRepeatMode(Player.REPEAT_MODE_OFF);
        videoPlayer.addListener(this);


        final PlayerView playerView = findViewById(R.id.playerview);
        playerView.setPlayer(videoPlayer);
//        videoPlayer.setDeviceMuted(false);
        playerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });


        videoPlayer.setPlayWhenReady(true);


    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.goBack:
                finish();
                overridePendingTransition(R.anim.in_from_top, R.anim.out_from_bottom);
                break;

            case R.id.add_sound_txt:
                Intent intent = new Intent(this, SoundListMainA.class);
                resultCallback.launch(intent);
                overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top);
                break;

            case R.id.next_btn:

                if (videoPlayer != null) {
                    videoPlayer.setPlayWhenReady(false);
                }
                if (audio != null) {
                    audio.pause();
                }

                goToPreviewActivity();

                break;
        }
    }

    ActivityResultLauncher<Intent> resultCallback = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            isSelected = data.getStringExtra("isSelected");
                            if (isSelected.equals("yes")) {
                                videoPlayer.setDeviceMuted(true);
                                addSoundTxt.setText(data.getStringExtra("sound_name"));
                                Variables.selectedSoundId = data.getStringExtra("sound_id");
                                soundFilePath = data.getStringExtra("outputFile");
                                preparedAudio();
                            }

                        }

                    }
                }
            });



    // this will play the sound with the video when we select the audio
    MediaPlayer audio;

    public void preparedAudio() {
        videoPlayer.setVolume(0);

        File file = new File(Functions.getAppFolder(this)+Variables.APP_HIDED_FOLDER + Variables.SelectedAudio_AAC);
        if (file.exists()) {
            audio = new MediaPlayer();
            try {
                audio.setDataSource(Functions.getAppFolder(this)+Variables.APP_HIDED_FOLDER + Variables.SelectedAudio_AAC);
                audio.prepare();
                audio.setLooping(true);


                videoPlayer.seekTo(0);
                videoPlayer.setPlayWhenReady(true);
                audio.start();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public void goToPreviewActivity() {

        try {
            File videoPath=new File(path);
            Log.d(Constants.tag,"Preview Path : "+videoPath.getAbsolutePath());
            Functions.copyFile(videoPath,
                    new File(Functions.getAppFolder(this)+Variables.outputfile2));
        }
        catch (Exception e) {
            Log.d(Constants.tag,"Exception : "+e);
        }

        Intent intent = new Intent(this, PreviewVideoA.class);
        intent.putExtra("draft_file", draftFile);
        intent.putExtra("fromWhere", "video_recording");
        intent.putExtra("isSoundSelected", isSelected);
        startActivity(intent);
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
        finish();

    }


    // play the video again on resume
    @Override
    protected void onResume() {
        super.onResume();
        if (videoPlayer != null) {
            videoPlayer.setPlayWhenReady(true);
        }
    }


    @Override
    public void onStop() {
        super.onStop();
        try {
            if (videoPlayer != null) {
                videoPlayer.setPlayWhenReady(false);
            }
            if (audio != null) {
                audio.pause();
            }
        } catch (Exception e) {

        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (videoPlayer != null) {
            videoPlayer.release();
        }

        if (audio != null) {
            audio.pause();
            audio.release();
        }
    }


    // handle that will be call on player state change
    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

        if (playbackState == Player.STATE_ENDED) {

            videoPlayer.seekTo(0);
            videoPlayer.setPlayWhenReady(true);

            if (audio != null) {
                audio.seekTo(0);
                audio.start();
            }

        }

    }


    // this will hide the bottom mobile navigation controll
    public void hideNavigation() {

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        // This work only for android 4.4+
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

            getWindow().getDecorView().setSystemUiVisibility(flags);

            // Code below is to handle presses of Volume up or Volume down.
            // Without this, after pressing volume buttons, the navigation bar will
            // show up and won't hide
            final View decorView = getWindow().getDecorView();
            decorView
                    .setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {

                        @Override
                        public void onSystemUiVisibilityChange(int visibility) {
                            if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                                decorView.setSystemUiVisibility(flags);
                            }
                        }
                    });
        }

    }


    @SuppressLint("NewApi")
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }


    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }


}
