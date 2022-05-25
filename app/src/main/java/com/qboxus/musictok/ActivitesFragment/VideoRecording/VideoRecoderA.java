package com.qboxus.musictok.ActivitesFragment.VideoRecording;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import com.qboxus.musictok.SimpleClasses.AppCompatLocaleActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.coremedia.iso.boxes.Container;
import com.daasuu.gpuv.composer.GPUMp4Composer;
import com.qboxus.musictok.Constants;
import com.qboxus.musictok.R;
import com.qboxus.musictok.Interfaces.ProgressBarListener;
import com.qboxus.musictok.SimpleClasses.SegmentedProgressBar;
import com.volley.plus.interfaces.Callback;
import com.qboxus.musictok.Interfaces.FragmentCallBack;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.Variables;
import com.qboxus.musictok.ActivitesFragment.SoundLists.SoundListMainA;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;
import com.qboxus.musictok.TrimModule.TrimType;
import com.qboxus.musictok.TrimModule.TrimVideo;
import com.qboxus.musictok.TrimModule.TrimmerUtils;
import com.wonderkiln.camerakit.CameraKit;
import com.wonderkiln.camerakit.CameraProperties;
import com.wonderkiln.camerakit.CameraView;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class VideoRecoderA extends AppCompatLocaleActivity implements View.OnClickListener {


    CameraView cameraView;

    int number = 0;

    ArrayList<String> videopaths = new ArrayList<>();

    ImageButton recordImage;
    ImageButton doneBtn;
    boolean isRecording = false;
    boolean isFlashOn = false;
    String isSelected;
    ImageButton flashBtn;
    SegmentedProgressBar videoProgress;
    LinearLayout cameraOptions;
    ImageButton rotateCamera, cutVideoBtn;



    TextView addSoundTxt;

    int secPassed = 0;
    long timeInMilis = 0;

    TextView countdownTimerTxt;
    boolean isRecordingTimerEnable;
    int recordingTime = 3;

    TextView shortVideoTimeTxt, longVideoTimeTxt;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Functions.setLocale(Functions.getSharedPreference(VideoRecoderA.this).getString(Variables.APP_LANGUAGE_CODE,Variables.DEFAULT_LANGUAGE_CODE)
                , this, VideoRecoderA.class,false);
        setContentView(R.layout.activity_video_recoder);

        Variables.selectedSoundId = "null";
        Constants.RECORDING_DURATION = 15000;


        cameraView = findViewById(R.id.camera);
        cameraOptions = findViewById(R.id.camera_options);
        recordImage = findViewById(R.id.record_image);

        findViewById(R.id.upload_layout).setOnClickListener(this);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage(getString(R.string.please_wait_));


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

        addSoundTxt = findViewById(R.id.add_sound_txt);
        addSoundTxt.setOnClickListener(this);

        findViewById(R.id.time_btn).setOnClickListener(this);

        Intent intent = getIntent();
        if (intent.hasExtra("sound_name")) {
            addSoundTxt.setText(intent.getStringExtra("sound_name"));
            Variables.selectedSoundId = intent.getStringExtra("sound_id");
            isSelected = intent.getStringExtra("isSelected");
            findViewById(R.id.time_layout).setVisibility(View.INVISIBLE);
            preparedAudio();
        }


        recordImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startOrStopRecording();
            }
        });
        countdownTimerTxt = findViewById(R.id.countdown_timer_txt);


        shortVideoTimeTxt = findViewById(R.id.short_video_time_txt);
        longVideoTimeTxt = findViewById(R.id.long_video_time_txt);
        shortVideoTimeTxt.setOnClickListener(this);
        longVideoTimeTxt.setOnClickListener(this);

        initlizeVideoProgress();

    }


    // start trimming activity
    ActivityResultLauncher<Intent> takeOrSelectVideoResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK ) {
                        Intent data = result.getData();
                        if (TrimmerUtils.getDuration(VideoRecoderA.this,data.getData())<Constants.MIN_TRIM_TIME){
                            Toast.makeText(VideoRecoderA.this,getString(R.string.video_must_be_larger_then_second),Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (data.getData() != null) {
                            openTrimActivity(String.valueOf(data.getData()));
                        }
                    }
                }
            });


    ActivityResultLauncher<Intent> videoTrimResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Uri uri = Uri.parse(TrimVideo.getTrimmedVideoPath(result.getData(),Variables.gallery_trimed_video));

                        String filepath = String.valueOf(uri);
                        mProgressDialog.dismiss();
                        changeVideoSize(filepath, Functions.getAppFolder(VideoRecoderA.this)+Variables.gallery_resize_video);

                    } else
                        Log.d(Constants.tag,"videoTrimResultLauncher data is null");
                }
            });



    // initialize the video progress for video recording percentage
    public void initlizeVideoProgress() {

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

            File file = new File(Functions.getAppFolder(this)+Variables.APP_HIDED_FOLDER + "myvideo" + (number) + ".mp4");
            videopaths.add(Functions.getAppFolder(this)+Variables.APP_HIDED_FOLDER + "myvideo" + (number) + ".mp4");
            cameraView.captureVideo(file);


            if (audio != null) {
                audio.start();
            }

            doneBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_not_done));
            doneBtn.setEnabled(false);

            videoProgress.resume();


            recordImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_recoding_yes));

            cutVideoBtn.setVisibility(View.GONE);


            findViewById(R.id.time_layout).setVisibility(View.INVISIBLE);
            findViewById(R.id.upload_layout).setEnabled(false);
            cameraOptions.setVisibility(View.GONE);
            addSoundTxt.setClickable(false);
            rotateCamera.setVisibility(View.GONE);

        } else if (isRecording) {

            isRecording = false;

            videoProgress.pause();
            videoProgress.addDivider();

            if (audio != null) {
                if (audio.isPlaying())
                {
                    audio.pause();
                }
            }

            cameraView.stopVideo();


            checkDoneBtnEnable();

            cutVideoBtn.setVisibility(View.VISIBLE);

            findViewById(R.id.upload_layout).setEnabled(true);
            recordImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_recoding_no));
            cameraOptions.setVisibility(View.VISIBLE);

        } else if (secPassed > (Constants.RECORDING_DURATION / 1000)) {
            Functions.showAlert(this, getString(R.string.alert), getString(R.string.video_only_can_be_a)+" " + (int) Constants.RECORDING_DURATION / 1000 + " S");
        }

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
        final ProgressDialog progressDialog = new ProgressDialog(VideoRecoderA.this);
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
                            retriever.setDataSource(VideoRecoderA.this, Uri.fromFile(file));
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
                    if (cameraView.isFacingFront()) {
                        outputFilePath = Functions.getAppFolder(VideoRecoderA.this)+Variables.output_frontcamera;
                    } else {
                        outputFilePath = Functions.getAppFolder(VideoRecoderA.this)+Variables.outputfile2;
                    }
                    FileOutputStream fos = new FileOutputStream(new File(outputFilePath));
                    out.writeContainer(fos.getChannel());
                    fos.close();

                    runOnUiThread(new Runnable() {
                        public void run() {

                            progressDialog.dismiss();

                            if (cameraView.isFacingFront()) {
                                changeFlipedVideo(Functions.getAppFolder(VideoRecoderA.this)+Variables.output_frontcamera,Functions.getAppFolder(VideoRecoderA.this)+ Variables.outputfile2);
                            } else {
                                goToPreviewActivity();
                            }

                        }
                    });


                } catch (Exception e) {
                }
            }
        }).start();


        return true;
    }


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
                                goToPreviewActivity();

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

                                    Functions.showToast(VideoRecoderA.this, getString(R.string.try_again));
                                } catch (Exception e) {
                                }
                            }
                        });

                    }
                })
                .start();


    }


    public void rotateCamera() {
        cameraView.toggleFacing();

        if (cameraView.getFacing() == CameraKit.Constants.FACING_FRONT)
            cameraView.setScaleX(1);

        CameraProperties properties = cameraView.getCameraProperties();
        Functions.printLog(Constants.tag, properties.verticalViewingAngle + "--" + properties.horizontalViewingAngle);

    }


    public void removeLastSection() {

        if (videopaths.size() > 0) {
            File file = new File(videopaths.get(videopaths.size() - 1));
            if (file.exists()) {

                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(VideoRecoderA.this, Uri.fromFile(file));
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
                    if (audio != null) {
                        int audio_backtime = (int) (audio.getCurrentPosition() - timeInMillisec);
                        audio.seekTo(audio_backtime);
                    }

                    secPassed = (int) (timeInMilis / 1000);

                    checkDoneBtnEnable();

                }
            }

            if (videopaths.isEmpty()) {

                findViewById(R.id.time_layout).setVisibility(View.VISIBLE);
                cutVideoBtn.setVisibility(View.GONE);
                addSoundTxt.setClickable(true);
                rotateCamera.setVisibility(View.VISIBLE);

                initlizeVideoProgress();

                if (audio != null) {
                    preparedAudio();
                }

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

            case R.id.upload_layout:
                pickVideoFromGallery();

                break;

            case R.id.done:
                append();
                break;

            case R.id.cut_video_btn:

                Functions.showAlert(this, "", getString(R.string.descard_the_last_clip_), getString(R.string.delete).toUpperCase(), getString(R.string.cancel_).toUpperCase(), new Callback() {
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

            case R.id.add_sound_txt:
                Intent intent = new Intent(this, SoundListMainA.class);
                resultCallback.launch(intent);
                overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top);
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


            case R.id.short_video_time_txt:
                RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                param.addRule(RelativeLayout.CENTER_HORIZONTAL);
                shortVideoTimeTxt.setLayoutParams(param);

                RelativeLayout.LayoutParams param4 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                param4.addRule(RelativeLayout.START_OF, R.id.short_video_time_txt);
                longVideoTimeTxt.setLayoutParams(param4);

                shortVideoTimeTxt.setTextColor(getResources().getColor(R.color.white));
                longVideoTimeTxt.setTextColor(getResources().getColor(R.color.graycolor2));

                Constants.RECORDING_DURATION = 60000;

                initlizeVideoProgress();
                break;


            case R.id.long_video_time_txt:
                RelativeLayout.LayoutParams param2 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                param2.addRule(RelativeLayout.CENTER_HORIZONTAL);
                longVideoTimeTxt.setLayoutParams(param2);

                RelativeLayout.LayoutParams param3 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                param3.addRule(RelativeLayout.END_OF, R.id.long_video_time_txt);
                shortVideoTimeTxt.setLayoutParams(param3);

                shortVideoTimeTxt.setTextColor(getResources().getColor(R.color.graycolor2));
                longVideoTimeTxt.setTextColor(getResources().getColor(R.color.white));

                Constants.RECORDING_DURATION = 60000;

                initlizeVideoProgress();
                break;

            default:
                return;

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
                                addSoundTxt.setText(data.getStringExtra("sound_name"));
                                Variables.selectedSoundId = data.getStringExtra("sound_id");
                                preparedAudio();
                            }

                        }
                    }
                }
            });


    // open the intent for get the video from gallery
    public void pickVideoFromGallery() {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        takeOrSelectVideoResultLauncher.launch(Intent.createChooser(intent, "Select Video"));
    }



    private void openTrimActivity(String data) {
        TrimVideo.activity(data)
                .setTrimType(TrimType.MIN_MAX_DURATION)
                .setMinToMax(Constants.MIN_TRIM_TIME, (Constants.RECORDING_DURATION/1000))
                .setMinDuration(Constants.MAX_TRIM_TIME)
                .setTitle("")//seconds
                .setMaxTimeCheck(Constants.RECORDING_DURATION)
                .start(this,videoTrimResultLauncher);
    }




    // change the video size
    public void changeVideoSize(String src_path, String destination_path) {

        try {
            Functions.copyFile(new File(src_path),
                    new File(destination_path));

            File file = new File(src_path);
            if (file.exists())
                file.delete();


            if (getIntent().hasExtra("sound_name")) {
                Intent intent = new Intent(VideoRecoderA.this, GallerySelectedVideoA.class);
                intent.putExtra("video_path", Functions.getAppFolder(this)+Variables.gallery_resize_video);
                intent.putExtra("sound_name",getIntent().getStringExtra("sound_name"));
                intent.putExtra("isSelected", "yes");
                intent.putExtra("sound_id", Variables.selectedSoundId);
                startActivity(intent);
            }
            else
            {
                Intent intent = new Intent(VideoRecoderA.this, GallerySelectedVideoA.class);
                intent.putExtra("video_path", Functions.getAppFolder(this)+Variables.gallery_resize_video);
                startActivity(intent);
            }




        } catch (Exception e) {
            e.printStackTrace();
            Functions.printLog(Constants.tag, e.toString());
        }
    }


    // this will play the sound with the video when we select the audio
    MediaPlayer audio;

    public void preparedAudio() {

        File file = new File(Functions.getAppFolder(this)+Variables.APP_HIDED_FOLDER + Variables.SelectedAudio_AAC);
        if (file.exists()) {
            try {
                audio = new MediaPlayer();
                try {
                    audio.setDataSource(Functions.getAppFolder(this)+Variables.APP_HIDED_FOLDER + Variables.SelectedAudio_AAC);
                    audio.prepare();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                mmr.setDataSource(this, Uri.fromFile(file));
                String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                final int file_duration = Functions.parseInterger(durationStr);

                if (file_duration < Constants.MAX_RECORDING_DURATION) {
                    Constants.RECORDING_DURATION = file_duration;
                    initlizeVideoProgress();
                }
            }
            catch (Exception e)
            {
                Log.d(Constants.tag,"Exception : "+e);
                Toast.makeText(this, getString(R.string.you_cannot_create_video_using_this_sound), Toast.LENGTH_SHORT).show();
                finish();
            }

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

            if (audio != null) {
                audio.stop();
                audio.reset();
                audio.release();
            }
            cameraView.stop();

        } catch (Exception e) {

        }
        deleteFile();
    }


    // show a alert before close the activity
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


    public void goToPreviewActivity() {

        Intent intent = new Intent(this, PreviewVideoA.class);
        intent.putExtra("fromWhere", "video_recording");
        intent.putExtra("isSoundSelected", isSelected);
        startActivity(intent);
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
    }


    // this will delete all the video parts that is create during priviously created video
    public void deleteFile() {

        File output = new File(Functions.getAppFolder(VideoRecoderA.this)+Variables.outputfile);
        File output2 = new File(Functions.getAppFolder(VideoRecoderA.this)+Variables.outputfile2);

        File gallery_trimed_video = new File(Functions.getAppFolder(VideoRecoderA.this)+Variables.gallery_trimed_video);
        File gallery_resize_video = new File(Functions.getAppFolder(VideoRecoderA.this)+Variables.gallery_resize_video);


        if (output.exists() && !output.delete()) {
            Functions.printLog(Constants.tag, "output File Not delete");
        }
        if (output2.exists() && !output2.delete()) {

            Functions.printLog(Constants.tag, "output2 File Not delete");
        }


        if (gallery_trimed_video.exists() && !gallery_trimed_video.delete()) {
            Functions.printLog(Constants.tag, "gallery_trimed_video File Not delete");
        }

        if (gallery_resize_video.exists() && !gallery_resize_video.delete()) {
            Functions.printLog(Constants.tag, "gallery_resize_video File Not delete");
        }

        for (int i = 0; i <= 12; i++) {

            File file = new File(Functions.getAppFolder(this)+Variables.APP_HIDED_FOLDER + "myvideo" + (i) + ".mp4");
            if (file.exists() && !file.delete()) {
                Functions.printLog(Constants.tag, "File Not delete");
            }

        }


    }


}
