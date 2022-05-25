package com.qboxus.musictok.ActivitesFragment.VideoRecording;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.qboxus.musictok.SimpleClasses.AppCompatLocaleActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.crystal.crystalrangeseekbar.widgets.CrystalRangeSeekbar;
import com.crystal.crystalrangeseekbar.widgets.CrystalSeekbar;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.gson.Gson;
import com.qboxus.musictok.ActivitesFragment.Profile.SeeFullImageA;
import com.qboxus.musictok.Constants;
import com.qboxus.musictok.R;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.Variables;
import com.qboxus.musictok.TrimModule.CompressOption;
import com.qboxus.musictok.TrimModule.CustomProgressView;
import com.qboxus.musictok.TrimModule.FileUtils;
import com.qboxus.musictok.TrimModule.TrimVideo;
import com.qboxus.musictok.TrimModule.TrimVideoOptions;
import com.qboxus.musictok.TrimModule.TrimmerUtils;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ActVideoTrimmer  extends AppCompatLocaleActivity implements View.OnClickListener{

    private PlayerView playerView;
    private SimpleExoPlayer videoPlayer;
    private ImageView imagePlayPause,btnNext,btnBack;
    private SimpleDraweeView[] imageViews;
    private long totalDuration;
    private Dialog dialog;
    private Uri uri;
    private TextView txtStartDuration, txtEndDuration;
    private CrystalRangeSeekbar seekbar;
    private long lastMinValue = 0;
    private long lastMaxValue = 0;
    private MenuItem menuDone;
    private CrystalSeekbar seekbarController;
    private boolean isValidVideo = true, isVideoEnded;
    private Handler seekHandler;
    private Bundle bundle;
    private ProgressBar progressBar;
    private TrimVideoOptions trimVideoOptions;

    private long currentDuration, lastClickedTime;
    Runnable updateSeekbar = new Runnable() {
        @Override
        public void run() {
            try {
                currentDuration = videoPlayer.getCurrentPosition() / 1000;
                if (!videoPlayer.getPlayWhenReady())
                    return;
                if (currentDuration <= lastMaxValue)
                    seekbarController.setMinStartValue((int) currentDuration).apply();
                else
                    videoPlayer.setPlayWhenReady(false);
            } finally {
                seekHandler.postDelayed(updateSeekbar, 1000);
            }
        }
    };
    private CompressOption compressOption;
    private String outputPath;
    private int trimType;
    private long fixedGap, minGap, minFromGap, maxToGap;
    private boolean hidePlayerSeek, isAccurateCut, showFileLocationAlert;
    private CustomProgressView progressView;
    int recordingDuration;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Functions.setLocale(Functions.getSharedPreference(ActVideoTrimmer.this).getString(Variables.APP_LANGUAGE_CODE,Variables.DEFAULT_LANGUAGE_CODE)
                , this, ActVideoTrimmer.class,false);
        setContentView(R.layout.activity_act_video_trimmer);

        bundle = getIntent().getExtras();
        recordingDuration=bundle.getInt("recordingDuration",0);
        Gson gson = new Gson();
        String videoOption = bundle.getString(TrimVideo.TRIM_VIDEO_OPTION);
        trimVideoOptions = gson.fromJson(videoOption, TrimVideoOptions.class);
        progressView = new CustomProgressView(this);
        btnNext=findViewById(R.id.btnNext);
        btnNext.setOnClickListener(this);
        btnBack=findViewById(R.id.btnBack);
        btnBack.setOnClickListener(this);
    }






    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        playerView = findViewById(R.id.player_view_lib);
        imagePlayPause = findViewById(R.id.image_play_pause);
        seekbar = findViewById(R.id.range_seek_bar);
        txtStartDuration = findViewById(R.id.txt_start_duration);
        txtEndDuration = findViewById(R.id.txt_end_duration);
        seekbarController = findViewById(R.id.seekbar_controller);

        progressBar = findViewById(R.id.progress_circular);
        SimpleDraweeView imageOne = findViewById(R.id.image_one);
        SimpleDraweeView imageTwo = findViewById(R.id.image_two);
        SimpleDraweeView imageThree = findViewById(R.id.image_three);
        SimpleDraweeView imageFour = findViewById(R.id.image_four);
        SimpleDraweeView imageFive = findViewById(R.id.image_five);
        SimpleDraweeView imageSix = findViewById(R.id.image_six);
        SimpleDraweeView imageSeven = findViewById(R.id.image_seven);
        SimpleDraweeView imageEight = findViewById(R.id.image_eight);
        imageViews = new SimpleDraweeView[]{imageOne, imageTwo, imageThree,
                imageFour, imageFive, imageSix, imageSeven, imageEight};
        seekHandler = new Handler(Looper.getMainLooper());
        initPlayer();
        setDataInView();
    }

    /**
     * SettingUp exoplayer
     **/
    private void initPlayer() {
        try {
            videoPlayer = new SimpleExoPlayer.Builder(this).build();
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
            playerView.setPlayer(videoPlayer);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setUsage(C.USAGE_MEDIA)
                        .setContentType(C.CONTENT_TYPE_MOVIE)
                        .build();
                videoPlayer.setAudioAttributes(audioAttributes, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setDataInView() {
        try {
            Runnable fileUriRunnable = () -> {
                uri = Uri.parse(bundle.getString(TrimVideo.TRIM_VIDEO_URI));
//              String path = FileUtils.getPath(ActVideoTrimmer.this, uri);
                String path= FileUtils.getRealPath(ActVideoTrimmer.this,uri);
                uri = Uri.parse(path);
                runOnUiThread(() -> {
                    Log.d(Constants.tag,"Real uri : "+uri);
                    progressBar.setVisibility(View.GONE);
                    totalDuration = TrimmerUtils.getDuration(ActVideoTrimmer.this, uri);
                    imagePlayPause.setOnClickListener(v ->
                            onVideoClicked());
                    Objects.requireNonNull(playerView.getVideoSurfaceView()).setOnClickListener(v ->
                            onVideoClicked());
                    initTrimData();
                    buildMediaSource(uri);
                    loadThumbnails();
                    setUpSeekBar();
                });
            };
            Executors.newSingleThreadExecutor().execute(fileUriRunnable);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initTrimData() {
        try {
            assert trimVideoOptions != null;
            trimType = TrimmerUtils.getTrimType(trimVideoOptions.trimType);
            hidePlayerSeek = trimVideoOptions.hideSeekBar;
            isAccurateCut = trimVideoOptions.accurateCut;
            compressOption = trimVideoOptions.compressOption;
            showFileLocationAlert = trimVideoOptions.showFileLocationAlert;
            fixedGap = trimVideoOptions.fixedDuration;
            fixedGap = fixedGap != 0 ? fixedGap : totalDuration;
            minGap = trimVideoOptions.minDuration;
            minGap = minGap != 0 ? minGap : totalDuration;
            if (trimType == 3) {
                minFromGap = trimVideoOptions.minToMax[0];
                maxToGap = trimVideoOptions.minToMax[1];
                minFromGap = minFromGap != 0 ? minFromGap : totalDuration;
                maxToGap = maxToGap != 0 ? maxToGap : totalDuration;
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    private void onVideoClicked() {
        try {
            if (isVideoEnded) {
                seekTo(lastMinValue);
                videoPlayer.setPlayWhenReady(true);
                return;
            }
            if ((currentDuration - lastMaxValue) > 0)
                seekTo(lastMinValue);
            videoPlayer.setPlayWhenReady(!videoPlayer.getPlayWhenReady());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void seekTo(long sec) {
        if (videoPlayer != null)
            videoPlayer.seekTo(sec * 1000);
    }

    private void buildMediaSource(Uri mUri) {
        try {
            DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, getString(R.string.app_name));
            MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(mUri));
            videoPlayer.addMediaSource(mediaSource);
            videoPlayer.prepare();
            videoPlayer.setPlayWhenReady(true);
            videoPlayer.addListener(new Player.Listener() {
                @Override
                public void onPlayWhenReadyChanged(boolean playWhenReady, int reason) {
                    imagePlayPause.setVisibility(playWhenReady ? View.GONE :
                            View.VISIBLE);
                }

                @Override
                public void onPlaybackStateChanged(int state) {
                    switch (state) {
                        case Player.STATE_ENDED:
                            Log.d(Constants.tag,"onPlayerStateChanged: Video ended.");
                            imagePlayPause.setVisibility(View.VISIBLE);
                            isVideoEnded = true;
                            break;
                        case Player.STATE_READY:
                            isVideoEnded = false;
                            startProgress();
                            Log.d(Constants.tag,"onPlayerStateChanged: Ready to play.");
                            break;
                        default:
                            break;
                        case Player.STATE_BUFFERING:
                            Log.d(Constants.tag,"onPlayerStateChanged: STATE_BUFFERING.");
                            break;
                        case Player.STATE_IDLE:
                            Log.d(Constants.tag,"onPlayerStateChanged: STATE_IDLE.");
                            break;
                    }
                }

            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     *  loading thumbnails
     * */
    private void loadThumbnails() {
        try {
            long diff = totalDuration / 8;
            int sec = 1;
            for (SimpleDraweeView img : imageViews) {
                img.setController(Functions.frescoImageLoad(Uri.parse(bundle.getString(TrimVideo.TRIM_VIDEO_URI)),false));
                if (sec < totalDuration)
                    sec++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setUpSeekBar() {
        seekbar.setVisibility(View.VISIBLE);
        txtStartDuration.setVisibility(View.VISIBLE);
        txtEndDuration.setVisibility(View.VISIBLE);

        seekbarController.setMaxValue(totalDuration).apply();
        seekbar.setMaxValue(totalDuration).apply();
        seekbar.setMaxStartValue((float) totalDuration).apply();
        if (trimType == 1) {
            seekbar.setFixGap(fixedGap).apply();
            lastMaxValue = totalDuration;
        } else if (trimType == 2) {
            seekbar.setMaxStartValue((float) minGap);
            seekbar.setGap(minGap).apply();
            lastMaxValue = totalDuration;
        } else if (trimType == 3) {
            seekbar.setMaxStartValue((float) maxToGap);
            seekbar.setGap(minFromGap).apply();
            lastMaxValue = maxToGap;
        } else {
            seekbar.setGap(2).apply();
            lastMaxValue = totalDuration;
        }
        if (hidePlayerSeek)
            seekbarController.setVisibility(View.GONE);

        seekbar.setOnRangeSeekbarFinalValueListener((minValue, maxValue) -> {
            if (!hidePlayerSeek)
                seekbarController.setVisibility(View.VISIBLE);
        });

        seekbar.setOnRangeSeekbarChangeListener((minValue, maxValue) -> {
            long minVal = (long) minValue;
            long maxVal = (long) maxValue;
            if (lastMinValue != minVal) {
                seekTo((long) minValue);
                if (!hidePlayerSeek)
                    seekbarController.setVisibility(View.INVISIBLE);
            }
            lastMinValue = minVal;
            lastMaxValue = maxVal;
            txtStartDuration.setText(TrimmerUtils.formatSeconds(minVal));
            txtEndDuration.setText(TrimmerUtils.formatSeconds(maxVal));
            if (trimType == 3)
                setDoneColor(minVal, maxVal);
        });

        seekbarController.setOnSeekbarFinalValueListener(value -> {
            long value1 = (long) value;
            if (value1 < lastMaxValue && value1 > lastMinValue) {
                seekTo(value1);
                return;
            }
            if (value1 > lastMaxValue)
                seekbarController.setMinStartValue((int) lastMaxValue).apply();
            else if (value1 < lastMinValue) {
                seekbarController.setMinStartValue((int) lastMinValue).apply();
                if (videoPlayer.getPlayWhenReady())
                    seekTo(lastMinValue);
            }
        });
    }

    /**
     * will be called whenever seekBar range changes
     * it checks max duration is exceed or not.
     * and disabling and enabling done menuItem
     *
     * @param minVal left thumb value of seekBar
     * @param maxVal right thumb value of seekBar
     */
    private void setDoneColor(long minVal, long maxVal) {
        try {
            if (menuDone == null)
                return;
            //changed value is less than maxDuration
            if ((maxVal - minVal) <= maxToGap) {
                menuDone.getIcon().setColorFilter(
                        new PorterDuffColorFilter(ContextCompat.getColor(this, R.color.white)
                                , PorterDuff.Mode.SRC_IN)
                );
                isValidVideo = true;
            } else {
                menuDone.getIcon().setColorFilter(
                        new PorterDuffColorFilter(ContextCompat.getColor(this, R.color.white)
                                , PorterDuff.Mode.SRC_IN)
                );
                isValidVideo = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        videoPlayer.setPlayWhenReady(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (videoPlayer != null)
            videoPlayer.release();
        if (progressView != null && progressView.isShowing())
            progressView.dismiss();
        stopRepeatingTask();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_done, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menuDone = menu.findItem(R.id.action_done);
        return super.onPrepareOptionsMenu(menu);
    }



    private void trimVideo() {
        if (isValidVideo) {
            //not exceed given maxDuration if has given
            outputPath = Functions.getAppFolder(ActVideoTrimmer.this)+Variables.gallery_trimed_video;
            Log.d(Constants.tag,"outputPath::" + outputPath );
            Log.d(Constants.tag,"sourcePath::" + uri);
            videoPlayer.setPlayWhenReady(false);
            showProcessingDialog();
            String[] complexCommand;
            if (compressOption != null)
                complexCommand = getCompressionCmd();
            else if (isAccurateCut) {
                //no changes in video quality
                //faster trimming command and given duration will be accurate
                complexCommand = getAccurateCmd();
            } else {
                //no changes in video quality
                //fastest trimming command however, result duration
                //will be low accurate(2-3 secs)
                complexCommand = new String[]{"-ss", TrimmerUtils.formatCSeconds(lastMinValue),
                        "-i", String.valueOf(uri),
                        "-t",
                        TrimmerUtils.formatCSeconds(lastMaxValue - lastMinValue),
                        "-async", "1", "-strict", "-2", "-c", "copy", outputPath};
            }
            execFFmpegBinary(complexCommand, true);
        } else
            Toast.makeText(this, getString(R.string.txt_smaller) + " " + TrimmerUtils.getLimitedTimeFormatted(maxToGap), Toast.LENGTH_SHORT).show();
    }

    private String[] getCompressionCmd() {
        MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
        metaRetriever.setDataSource(String.valueOf(uri));
        String height = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
        String width = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
        int w = TrimmerUtils.clearNull(width).isEmpty() ? 0 : Integer.parseInt(width);
        int h = Integer.parseInt(height);
        int rotation = TrimmerUtils.getVideoRotation(this, uri);
        if (rotation == 90 || rotation == 270) {
            int temp = w;
            w = h;
            h = temp;
        }
        //Default compression option
        if (compressOption.getWidth() != 0 || compressOption.getHeight() != 0
                || !compressOption.getBitRate().equals("0k")) {
            return new String[]{"-ss", TrimmerUtils.formatCSeconds(lastMinValue),
                    "-i", String.valueOf(uri), "-s", compressOption.getWidth() + "x" +
                    compressOption.getHeight(),
                    "-r", String.valueOf(compressOption.getFrameRate()),
                    "-vcodec", "mpeg4", "-b:v",
                    compressOption.getBitRate(), "-b:a", "48000", "-ac", "2", "-ar",
                    "22050", "-t",
                    TrimmerUtils.formatCSeconds(lastMaxValue - lastMinValue), outputPath};
        }
        //Dividing high resolution video by 2(ex: taken with camera)
        else if (w >= 800) {
            w = w / 2;
            h = Integer.parseInt(height) / 2;
            return new String[]{"-ss", TrimmerUtils.formatCSeconds(lastMinValue),
                    "-i", String.valueOf(uri),
                    "-s", w + "x" + h, "-r", "30",
                    "-vcodec", "mpeg4", "-b:v",
                    "1M", "-b:a", "48000", "-ac", "2", "-ar", "22050",
                    "-t",
                    TrimmerUtils.formatCSeconds(lastMaxValue - lastMinValue), outputPath};
        } else {
            return new String[]{"-ss", TrimmerUtils.formatCSeconds(lastMinValue),
                    "-i", String.valueOf(uri), "-s", w + "x" + h, "-r",
                    "30", "-vcodec", "mpeg4", "-b:v",
                    "400K", "-b:a", "48000", "-ac", "2", "-ar", "22050",
                    "-t",
                    TrimmerUtils.formatCSeconds(lastMaxValue - lastMinValue), outputPath};
        }
    }

    private void execFFmpegBinary(final String[] command, boolean retry) {
        try {
            new Thread(() -> {
                int result = FFmpeg.execute(command);
                if (result == 0) {
                    dialog.dismiss();
                    if (showFileLocationAlert)
                        showLocationAlert();
                    else {

                        Log.d(Constants.tag,"Path1: "+outputPath );
                        long videoDuration=Functions.getfileduration(ActVideoTrimmer.this,Uri.parse(outputPath));
                        if(videoDuration<(recordingDuration+1000))
                        {
                            Intent intent = new Intent();
                            intent.putExtra(Variables.gallery_trimed_video, outputPath);
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                        else
                        {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    File delResource=new File(outputPath);
                                    delResource.delete();
                                    String timeStr="";
                                    if (recordingDuration==15000)
                                    {
                                        timeStr="15sec";
                                    }
                                    else
                                    if (recordingDuration==60000)
                                    {
                                        timeStr="60sec";
                                    }
                                    else
                                    if (recordingDuration==300000)
                                    {
                                        timeStr="5mint";
                                    }
                                    Toast.makeText(ActVideoTrimmer.this, getText(R.string.video_is_larger_then)+" "+timeStr, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                    }
                } else if (result == 255) {
                    Log.d(Constants.tag,"Command cancelled");
                    if (dialog.isShowing())
                        dialog.dismiss();
                } else {
                    // Failed case:
                    // line 489 command fails on some devices in
                    // that case retrying with accurateCmt as alternative command
                    if (retry && !isAccurateCut && compressOption == null) {
                        execFFmpegBinary(getAccurateCmd(), false);
                    } else {
                        if (dialog.isShowing())
                            dialog.dismiss();
                        runOnUiThread(() ->
                                Toast.makeText(ActVideoTrimmer.this, "Failed to trim", Toast.LENGTH_SHORT).show());
                    }
                }
            }).start();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showLocationAlert() {
        // dialog to ask user to open file location in file manager or not
        AlertDialog openFileLocationDialog = new AlertDialog.Builder(ActVideoTrimmer.this).create();
        openFileLocationDialog.setTitle(getString(R.string.open_file_location));
        openFileLocationDialog.setCancelable(true);

        // when user click yes
        openFileLocationDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.yes), (dialogInterface, i) -> {
            // open file location
            Intent chooser = new Intent(Intent.ACTION_GET_CONTENT);
            Uri uriFile = Uri.parse(outputPath);
            chooser.addCategory(Intent.CATEGORY_OPENABLE);
            chooser.setDataAndType(uriFile, "*/*");
            startActivity(chooser);
        });

        // when user click no and finish current activity
        openFileLocationDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.no), (dialogInterface, i) -> openFileLocationDialog.dismiss());

        // when user click no and finish current activity
        openFileLocationDialog.setOnDismissListener(dialogInterface -> {
            Log.d(Constants.tag,"Path2: "+outputPath );
            long videoDuration=Functions.getfileduration(ActVideoTrimmer.this,Uri.parse(outputPath));
            if(videoDuration<(recordingDuration+1000))
            {
                Intent intent = new Intent();
                intent.putExtra(Functions.getAppFolder(ActVideoTrimmer.this)+ Variables.APP_HIDED_FOLDER, outputPath);
                setResult(RESULT_OK, intent);
                finish();
            }
            else
            {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        File delResource=new File(outputPath);
                        delResource.delete();
                        String timeStr="";
                        if (recordingDuration==15000)
                        {
                            timeStr="15sec";
                        }
                        else
                        if (recordingDuration==60000)
                        {
                            timeStr="60sec";
                        }
                        else
                        if (recordingDuration==300000)
                        {
                            timeStr="5mint";
                        }
                        Toast.makeText(ActVideoTrimmer.this, getText(R.string.video_is_larger_then)+" "+timeStr, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        openFileLocationDialog.show();
    }

    private String[] getAccurateCmd() {
        return new String[]{"-ss", TrimmerUtils.formatCSeconds(lastMinValue)
                , "-i", String.valueOf(uri), "-t",
                TrimmerUtils.formatCSeconds(lastMaxValue - lastMinValue),
                "-async", "1", outputPath};
    }

    private void showProcessingDialog() {
        try {
            dialog = new Dialog(this);
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.alert_convert);
            TextView txtCancel = dialog.findViewById(R.id.txt_cancel);
            dialog.setCancelable(false);
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            txtCancel.setOnClickListener(v -> {
                dialog.dismiss();
                FFmpeg.cancel();
            });
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    void startProgress() {
        updateSeekbar.run();
    }

    void stopRepeatingTask() {
        seekHandler.removeCallbacks(updateSeekbar);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.btnBack:
            {
                ActVideoTrimmer.super.onBackPressed();
            }
                break;
            case R.id.btnNext:
            {
                //prevent multiple clicks
                if (SystemClock.elapsedRealtime() - lastClickedTime < 800)
                    return ;
                lastClickedTime = SystemClock.elapsedRealtime();
                trimVideo();
            }
                break;
        }
    }
}
