package com.qboxus.musictok.ActivitesFragment.VideoRecording;

import com.qboxus.musictok.SimpleClasses.AppCompatLocaleActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.coremedia.iso.boxes.Container;
import com.daasuu.gpuv.composer.GPUMp4Composer;
import com.google.android.exoplayer2.C;
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
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;
import com.qboxus.musictok.Constants;
import com.qboxus.musictok.Models.HomeModel;
import com.qboxus.musictok.R;
import com.qboxus.musictok.Interfaces.ProgressBarListener;
import com.qboxus.musictok.SimpleClasses.SegmentedProgressBar;
import com.volley.plus.interfaces.Callback;
import com.qboxus.musictok.Interfaces.FragmentCallBack;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.Variables;
import com.wonderkiln.camerakit.CameraKit;
import com.wonderkiln.camerakit.CameraView;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class VideoRecoderDuetA extends AppCompatLocaleActivity implements View.OnClickListener {


    CameraView cameraView;
    int number = 0;

    ArrayList<String> videopaths = new ArrayList<>();

    ImageButton recordImage;
    ImageButton doneBtn;
    ImageView imgDuetOrientation;
    boolean isRecording = false;
    boolean isFlashOn = false;

    ImageButton flashBtn;
    SegmentedProgressBar videoProgress;
    LinearLayout cameraOptions;
    ImageButton rotateCamera, cutVideoBtn;


    int secPassed = 0;
    long timeInMilis = 0;

    TextView countdownTimerTxt;
    boolean isRecordingTimerEnable;
    int recordingTime = 3;

    HomeModel item;

    boolean duetOrientation = false;
    LinearLayout tabLayoutOrientation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Functions.setLocale(Functions.getSharedPreference(VideoRecoderDuetA.this).getString(Variables.APP_LANGUAGE_CODE,Variables.DEFAULT_LANGUAGE_CODE)
                , this, VideoRecoderDuetA.class,false);
        setContentView(R.layout.activity_video_recoder_duet);

        cameraView = findViewById(R.id.camera);
        cameraOptions = findViewById(R.id.camera_options);
        imgDuetOrientation = findViewById(R.id.orientation_btn);
        imgDuetOrientation.setOnClickListener(this);
        tabLayoutOrientation = findViewById(R.id.layout_orientation);


        recordImage = findViewById(R.id.record_image);

        cutVideoBtn = findViewById(R.id.cut_video_btn);
        cutVideoBtn.setVisibility(View.GONE);
        cutVideoBtn.setOnClickListener(this);

        doneBtn = findViewById(R.id.done);
        doneBtn.setEnabled(false);
        doneBtn.setOnClickListener(this);


        rotateCamera = findViewById(R.id.rotate_camera);
        rotateCamera.setOnClickListener(this);
        flashBtn = findViewById(R.id.flash_camera);
        flashBtn.setOnClickListener(this);

        findViewById(R.id.goBack).setOnClickListener(this);

        findViewById(R.id.time_btn).setOnClickListener(this);

        Intent intent = getIntent();
        if (intent.hasExtra("data")) {
            item = (HomeModel) intent.getSerializableExtra("data");
        }

        Constants.RECORDING_DURATION = (int) Functions.getFileDuration(this, Uri.parse(Functions.getAppFolder(VideoRecoderDuetA.this) + item.video_id + ".mp4"));

        setPlayer();

        recordImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startOrStopRecording();
            }
        });
        countdownTimerTxt = findViewById(R.id.countdown_timer_txt);


        initlize_Video_progress();


    }


    public void initlize_Video_progress() {
        secPassed = 0;
        videoProgress = findViewById(R.id.video_progress);
        videoProgress.enableAutoProgressView(Constants.RECORDING_DURATION);
        videoProgress.setDividerColor(Color.WHITE);
        videoProgress.setDividerEnabled(true);
        videoProgress.setDividerWidth(4);
        videoProgress.setShader(new int[]{Color.CYAN, Color.CYAN, Color.CYAN});

        videoProgress.SetListener(new ProgressBarListener() {
            @Override
            public void timeinMill(long mills) {
                timeInMilis = mills;
                secPassed = (int) (mills / 1000);

                if (secPassed > (Constants.RECORDING_DURATION / 1000) - 1) {
                    startOrStopRecording();
                }

                if (isRecordingTimerEnable && secPassed >= recordingTime) {
                    isRecordingTimerEnable = false;
                    startOrStopRecording();
                }


            }
        });
    }

    // if the Recording is stop then it we start the recording
    // and if the mobile is recording the video then it will stop the recording
    public void startOrStopRecording() {

        if (!isRecording && secPassed < (Constants.RECORDING_DURATION / 1000) - 1) {
            number = number + 1;

            isRecording = true;

            new Thread(new Runnable() {
                @Override
                public void run() {

                    File file = new File(Functions.getAppFolder(VideoRecoderDuetA.this)+Variables.APP_HIDED_FOLDER + "myvideo" + (number) + ".mp4");
                    videopaths.add(Functions.getAppFolder(VideoRecoderDuetA.this)+Variables.APP_HIDED_FOLDER + "myvideo" + (number) + ".mp4");
                    cameraView.captureVideo(file);

                    videoProgress.resume();

                    video_player.setPlayWhenReady(true);

                }
            }).start();


            doneBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_not_done));
            doneBtn.setEnabled(false);


            recordImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_recoding_yes));

            cutVideoBtn.setVisibility(View.GONE);


            cameraOptions.setVisibility(View.GONE);
            rotateCamera.setVisibility(View.GONE);

        } else if (isRecording) {

            isRecording = false;


            new Thread(new Runnable() {
                @Override
                public void run() {

                    videoProgress.pause();
                    videoProgress.addDivider();

                    video_player.setPlayWhenReady(false);

                    cameraView.stopVideo();

                }
            }).start();


            checkDoneBtnEnable();

            cutVideoBtn.setVisibility(View.VISIBLE);

            recordImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_recoding_no));
            cameraOptions.setVisibility(View.VISIBLE);

        } else if (secPassed > (Constants.RECORDING_DURATION / 1000)) {
            Functions.showAlert(this, getString(R.string.alert), getString(R.string.video_only_can_be_a)+" " + (int) Constants.RECORDING_DURATION / 1000 + " S");
        }


    }


    // set the player for play the duet video
    SimpleExoPlayer video_player;

    public void setPlayer() {

        DefaultTrackSelector trackSelector = new DefaultTrackSelector(this);

        video_player = new SimpleExoPlayer.Builder(this).
                setTrackSelector(trackSelector)
                .build();


        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(VideoRecoderDuetA.this, getString(R.string.app_name));
        MediaSource videoSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(Functions.getAppFolder(VideoRecoderDuetA.this) + item.video_id + ".mp4"));
        video_player.setThrowsWhenUsingWrongThread(false);
        video_player.addMediaSource(videoSource);
        video_player.prepare();
        video_player.setRepeatMode(Player.REPEAT_MODE_OFF);


        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setUsage(C.USAGE_MEDIA)
                        .setContentType(C.CONTENT_TYPE_MOVIE)
                        .build();
                video_player.setAudioAttributes(audioAttributes, true);
            }
        }
        catch (Exception e)
        {
            Log.d(Constants.tag,"Exception audio focus : "+e);
        }


        final PlayerView playerView = findViewById(R.id.playerview);
        playerView.setPlayer(video_player);

        playerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        video_player.setPlayWhenReady(false);
    }


    public void checkDoneBtnEnable() {
        if (secPassed > (Constants.MIN_TIME_RECORDING / 1000)) {
            doneBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_done_red));
            doneBtn.setEnabled(true);
        } else {
            doneBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_not_done));
            doneBtn.setEnabled(false);
        }
    }


    // this will apped all the videos parts in one  fullvideo
    private boolean append() {
        final ProgressDialog progressDialog = new ProgressDialog(VideoRecoderDuetA.this);
        new Thread(new Runnable() {
            @Override
            public void run() {


                runOnUiThread(new Runnable() {
                    public void run() {

                        progressDialog.setMessage(getString(R.string.please_wait_));
                        progressDialog.show();
                    }
                });

                ArrayList<String> video_list = new ArrayList<>();
                for (int i = 0; i < videopaths.size(); i++) {

                    File file = new File(videopaths.get(i));
                    if (file.exists()) {
                        try {
                            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                            retriever.setDataSource(VideoRecoderDuetA.this, Uri.fromFile(file));
                            String hasVideo = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO);
                            boolean isVideo = "yes".equals(hasVideo);

                            if (isVideo && file.length() > 3000) {
                                Functions.printLog("resp", videopaths.get(i));
                                video_list.add(videopaths.get(i));
                            }
                        } catch (Exception e) {
                            Functions.printLog(Constants.tag, e.toString());
                        }
                    }
                }

                try {

                    Movie[] inMovies = new Movie[video_list.size()];

                    for (int i = 0; i < video_list.size(); i++) {

                        inMovies[i] = MovieCreator.build(video_list.get(i));
                    }


                    List<Track> videoTracks = new LinkedList<Track>();
                    List<Track> audioTracks = new LinkedList<Track>();
                    for (Movie m : inMovies) {
                        for (Track t : m.getTracks()) {
                            if (t.getHandler().equals("soun")) {
                                audioTracks.add(t);
                            }
                            if (t.getHandler().equals("vide")) {
                                videoTracks.add(t);
                            }
                        }
                    }
                    Movie result = new Movie();
                    if (audioTracks.size() > 0) {
                        result.addTrack(new AppendTrack(audioTracks.toArray(new Track[audioTracks.size()])));
                    }
                    if (videoTracks.size() > 0) {
                        result.addTrack(new AppendTrack(videoTracks.toArray(new Track[videoTracks.size()])));
                    }

                    Container out = new DefaultMp4Builder().build(result);

                    String outputFilePath = null;
                    outputFilePath = Functions.getAppFolder(VideoRecoderDuetA.this)+Variables.outputfile2;


                    FileOutputStream fos = new FileOutputStream(new File(outputFilePath));
                    out.writeContainer(fos.getChannel());
                    fos.close();

                    runOnUiThread(new Runnable() {
                        public void run() {

                            progressDialog.dismiss();

                            if (cameraView.isFacingFront()) {
                                changeFlipedVideo(Functions.getAppFolder(VideoRecoderDuetA.this)+Variables.outputfile2, Functions.getAppFolder(VideoRecoderDuetA.this)+Variables.output_filter_file);
                            } else
                                goToPostActivity();

                        }
                    });


                } catch (Exception e) {

                }


            }
        }).start();


        return true;
    }


    public void rotateCamera() {

        cameraView.toggleFacing();
    }


    public void removeLastSection() {

        if (videopaths.size() > 0) {
            File file = new File(videopaths.get(videopaths.size() - 1));
            if (file.exists()) {

                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(VideoRecoderDuetA.this, Uri.fromFile(file));
                String hasVideo = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO);
                String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                long timeInMillisec = Long.parseLong(time);
                boolean isVideo = "yes".equals(hasVideo);
                if (isVideo) {
                    timeInMilis = timeInMilis - timeInMillisec;
                    videoProgress.removeDivider();
                    videopaths.remove(videopaths.size() - 1);
                    videoProgress.updateProgress(timeInMilis);
                    videoProgress.back_countdown(timeInMillisec);

                    if (video_player != null) {
                        int audio_backtime = (int) (video_player.getCurrentPosition() - timeInMillisec);
                        if (audio_backtime < 0)
                            audio_backtime = 0;

                        video_player.seekTo(audio_backtime);
                    }

                    secPassed = (int) (timeInMilis / 1000);

                    checkDoneBtnEnable();

                }
            }

            if (videopaths.isEmpty()) {

                cutVideoBtn.setVisibility(View.GONE);
                rotateCamera.setVisibility(View.VISIBLE);

                initlize_Video_progress();

            }

            file.delete();
        }

    }


    @SuppressLint("WrongConstant")
    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.rotate_camera:
                rotateCamera();
                break;


            case R.id.done:
                append();
                break;
            case R.id.orientation_btn:
                if (duetOrientation) {
                    FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) findViewById(R.id.layout_orientation).getLayoutParams();
                    layoutParams.width = FrameLayout.LayoutParams.MATCH_PARENT;
                    layoutParams.height = FrameLayout.LayoutParams.MATCH_PARENT;
                    PlayerView playerView = findViewById(R.id.playerview);
                    playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT);
                    tabLayoutOrientation.setLayoutParams(layoutParams);
                    tabLayoutOrientation.setOrientation(LinearLayout.VERTICAL);
                    imgDuetOrientation.animate().rotation(0f).setDuration(500).start();

                    duetOrientation = false;
                } else {
                    FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) findViewById(R.id.layout_orientation).getLayoutParams();
                    layoutParams.width = FrameLayout.LayoutParams.MATCH_PARENT;
                    layoutParams.height = 800;
                    PlayerView playerView = findViewById(R.id.playerview);
                    playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
                    tabLayoutOrientation.setLayoutParams(layoutParams);
                    tabLayoutOrientation.setOrientation(LinearLayout.HORIZONTAL);
                    imgDuetOrientation.animate().rotation(90f).setDuration(500).start();
                    duetOrientation = true;
                }
                break;

            case R.id.cut_video_btn:

                Functions.showAlert(this, "", getString(R.string.discard_the_last_clip), getString(R.string.delete).toUpperCase(), getString(R.string.cancel_).toUpperCase(), new Callback() {
                    @Override
                    public void onResponce(String resp) {
                        if (resp.equalsIgnoreCase("yes")) {
                            removeLastSection();
                        }
                    }
                });

                break;

            case R.id.flash_camera:

                if (isFlashOn) {
                    isFlashOn = false;
                    cameraView.setFlash(0);
                    flashBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_flash_on));
                } else {
                    isFlashOn = true;
                    cameraView.setFlash(CameraKit.Constants.FLASH_TORCH);
                    flashBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_flash_off));
                }

                break;

            case R.id.goBack:
                onBackPressed();
                break;

            case R.id.time_btn:
                if (secPassed + 1 < Constants.RECORDING_DURATION / 1000) {
                    RecordingTimeRangF recordingTimeRang_f = new RecordingTimeRangF(new FragmentCallBack() {
                        @Override
                        public void onResponce(Bundle bundle) {
                            if (bundle != null) {
                                isRecordingTimerEnable = true;
                                recordingTime = bundle.getInt("end_time");
                                countdownTimerTxt.setText("3");
                                countdownTimerTxt.setVisibility(View.VISIBLE);
                                recordImage.setClickable(false);
                                final Animation scaleAnimation = new ScaleAnimation(1.0f, 0.0f, 1.0f, 0.0f,
                                        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                                new CountDownTimer(4000, 1000) {
                                    @Override
                                    public void onTick(long millisUntilFinished) {

                                        countdownTimerTxt.setText("" + (millisUntilFinished / 1000));
                                        countdownTimerTxt.setAnimation(scaleAnimation);

                                    }

                                    @Override
                                    public void onFinish() {
                                        recordImage.setClickable(true);
                                        countdownTimerTxt.setVisibility(View.GONE);
                                        startOrStopRecording();
                                    }
                                }.start();

                            }
                        }
                    });
                    Bundle bundle = new Bundle();
                    if (secPassed < (Constants.RECORDING_DURATION / 1000) - 3)
                        bundle.putInt("end_time", (secPassed + 3));
                    else
                        bundle.putInt("end_time", (secPassed + 1));

                    bundle.putInt("total_time", (Constants.RECORDING_DURATION / 1000));
                    recordingTimeRang_f.setArguments(bundle);
                    recordingTimeRang_f.show(getSupportFragmentManager(), "");
                }
                break;


        }


    }


    @Override
    protected void onResume() {
        super.onResume();
        cameraView.start();
    }


    @Override
    protected void onDestroy() {
        releaseResources();
        super.onDestroy();

    }


    public void releaseResources() {
        try {
            cameraView.stop();

            if (video_player != null) {
                video_player.setPlayWhenReady(false);
                video_player.release();
            }


        } catch (Exception e) {

        }
        deleteFile();
    }

    @Override
    public void onBackPressed() {

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.alert))
                .setMessage(getString(R.string.are_you_sure_if_you_back))
                .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                        releaseResources();

                        finish();
                        overridePendingTransition(R.anim.in_from_top, R.anim.out_from_bottom);

                    }
                }).show();


    }


    public void goToPostActivity() {
        try {
            Functions.copyFile(new File(Functions.getAppFolder(this)+Variables.outputfile2),
                    new File(Functions.getAppFolder(this)+Variables.output_filter_file));
        } catch (Exception e) {
            e.printStackTrace();
        }

        openPostActivity();
    }


    // this function will add the filter to video and save that same video for post the video in post video screen
    public void changeFlipedVideo(String srcMp4Path, final String destMp4Path) {

        Functions.showDeterminentLoader(this, false, false);
        new GPUMp4Composer(srcMp4Path, destMp4Path)
                .flipHorizontal(true)
                .listener(new GPUMp4Composer.Listener() {
                    @Override
                    public void onProgress(double progress) {
                        Functions.printLog("resp", "" + (int) (progress * 100));
                        Functions.showLoadingProgress((int) (progress * 100));
                    }

                    @Override
                    public void onCompleted() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                Functions.cancelDeterminentLoader();
                                openPostActivity();


                            }
                        });
                    }

                    @Override
                    public void onCanceled() {
                        Functions.printLog("resp", "onCanceled");
                    }

                    @Override
                    public void onFailed(Exception exception) {

                        Functions.printLog("resp", exception.toString());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {

                                    Functions.cancelDeterminentLoader();

                                    Functions.showToast(VideoRecoderDuetA.this, getString(R.string.try_again));
                                } catch (Exception e) {
                                }
                            }
                        });

                    }
                })
                .start();


    }


    public void openPostActivity() {
        String duet = "";
        if (duetOrientation)
            duet = "h";
        else
            duet = "v";

        Intent intent = new Intent(this, PostVideoA.class);
        intent.putExtra("duet_video_id", item.video_id);
        intent.putExtra("duet_orientation", duet);
        intent.putExtra("duet_video_username", item.username);
        startActivity(intent);
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);

    }


    // this will delete all the video parts that is create during priviously created video
    public void deleteFile() {

        File output = new File(Functions.getAppFolder(this)+Variables.outputfile);
        File output2 = new File(Functions.getAppFolder(this)+Variables.outputfile2);

        File gallery_trimed_video = new File(Functions.getAppFolder(this)+Variables.gallery_trimed_video);
        File gallery_resize_video = new File(Functions.getAppFolder(this)+Variables.gallery_resize_video);

        if (output.exists()) {
            output.delete();
        }

        if (output2.exists()) {

            output2.delete();
        }

        if (gallery_trimed_video.exists()) {
            gallery_trimed_video.delete();
        }

        if (gallery_resize_video.exists()) {
            gallery_resize_video.delete();
        }

        for (int i = 0; i <= 12; i++) {

            File file = new File(Functions.getAppFolder(VideoRecoderDuetA.this)+Variables.APP_HIDED_FOLDER + "myvideo" + (i) + ".mp4");
            if (file.exists()) {
                file.delete();
            }

        }

    }

}
