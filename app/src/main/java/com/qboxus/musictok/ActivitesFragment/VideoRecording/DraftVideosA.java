package com.qboxus.musictok.ActivitesFragment.VideoRecording;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;

import com.qboxus.musictok.SimpleClasses.AppCompatLocaleActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.qboxus.musictok.Adapters.DraftVideosAdapter;
import com.qboxus.musictok.Constants;
import com.qboxus.musictok.Models.DraftVideoModel;
import com.qboxus.musictok.R;
import com.qboxus.musictok.Services.UploadService;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.Variables;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Locale;

public class DraftVideosA extends AppCompatLocaleActivity implements View.OnClickListener {

    ArrayList<DraftVideoModel> dataList = new ArrayList<>();;
    public RecyclerView recyclerView;
    DraftVideosAdapter adapter;

    ProgressBar pbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Functions.setLocale(Functions.getSharedPreference(DraftVideosA.this).getString(Variables.APP_LANGUAGE_CODE,Variables.DEFAULT_LANGUAGE_CODE)
                , this, DraftVideosA.class,false);
        setContentView(R.layout.activity_gallery_videos);

        pbar = findViewById(R.id.pbar);


        recyclerView = findViewById(R.id.recylerview);
        final GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);



        adapter = new DraftVideosAdapter(this, dataList, new DraftVideosAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int postion, DraftVideoModel item, View view) {

                if (view.getId() == R.id.cross_btn) {
                    File file_data = new File(item.video_path);
                    if (file_data.exists()) {
                        file_data.delete();
                    }
                    dataList.remove(postion);
                    adapter.notifyItemRemoved(postion);
                    adapter.notifyItemChanged(postion);

                }

                else {

                    MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                    Bitmap bmp = null;
                    try {
                        retriever.setDataSource(item.video_path);
                        bmp = retriever.getFrameAtTime();
                        int videoHeight = bmp.getHeight();
                        int videoWidth = bmp.getWidth();

                        Functions.printLog(Constants.tag, "" + videoWidth + "---" + videoHeight);

                    } catch (Exception e) {
                        Log.d(Constants.tag,"Exception: "+e);
                    }

                    if (item.video_duration_ms <= Constants.MAX_RECORDING_DURATION) {

                        if (!Functions.isMyServiceRunning(DraftVideosA.this, new UploadService().getClass())) {

                            changeSmallVideoSize(item.video_path, Functions.getAppFolder(DraftVideosA.this)+Variables.gallery_resize_video);
                        } else {
                            Toast.makeText(DraftVideosA.this, getString(R.string.please_wait_video_uploading_is_already_in_progress), Toast.LENGTH_SHORT).show();
                        }


                    } else {
                        try {
                            changeVideoSize(item.video_path, Functions.getAppFolder(DraftVideosA.this)+Variables.gallery_resize_video);
                        } catch (Exception e) {
                            Log.d(Constants.tag,"Exception: "+e);
                        }
                    }

                }

            }
        });

        recyclerView.setAdapter(adapter);
        getAllVideoPathDraft();


        findViewById(R.id.goBack).setOnClickListener(this::onClick);


    }


    // get the videos from loacal directory and show them in list
    public void getAllVideoPathDraft() {


        String path = Functions.getAppFolder(this)+Variables.DRAFT_APP_FOLDER;
        File directory = new File(path);
        File[] files = directory.listFiles();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            DraftVideoModel item = new DraftVideoModel();
            item.video_path = file.getAbsolutePath();
            item.video_duration_ms = getfileduration(Uri.parse(file.getAbsolutePath()));

            Functions.printLog(Constants.tag, "" + item.video_duration_ms);

            if (item.video_duration_ms > 5000) {
                item.video_time = changeSecToTime(item.video_duration_ms);
                dataList.add(item);
            }
        }

    }


    // get the audio file duration that is store in our directory
    public long getfileduration(Uri uri) {
        try {

            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(this, uri);
            String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            final int file_duration = Functions.parseInterger(durationStr);

            return file_duration;
        } catch (Exception e) {
            Log.d(Constants.tag,"Exception: "+e);
        }
        return 0;
    }


    public String changeSecToTime(long file_duration) {
        long second = (file_duration / 1000) % 60;
        long minute = (file_duration / (1000 * 60)) % 60;

        return String.format(Locale.ENGLISH,"%02d:%02d", minute, second);

    }


    // change the video size before post
    public void changeSmallVideoSize(String src_path, String destination_path) {

        File source = new File(src_path);
        File destination = new File(destination_path);
        try {
            if (source.exists()) {

                InputStream in = new FileInputStream(source);
                OutputStream out = new FileOutputStream(destination);

                byte[] buf = new byte[1024];
                int len;

                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }

                in.close();
                out.close();

                Intent intent = new Intent(DraftVideosA.this, GallerySelectedVideoA.class);
                intent.putExtra("video_path", Functions.getAppFolder(this)+Variables.gallery_resize_video);
                intent.putExtra("draft_file", src_path);
                startActivity(intent);

            } else {
                Functions.showToast(DraftVideosA.this, getString(R.string.fail_to_get_video_from_draft));
            }

        } catch (Exception e) {
            Log.d(Constants.tag,"Exception: "+e);
        }

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
                Intent intent = new Intent(DraftVideosA.this, GallerySelectedVideoA.class);
                intent.putExtra("video_path", Functions.getAppFolder(this)+Variables.gallery_resize_video);
                intent.putExtra("sound_name",getIntent().getStringExtra("sound_name"));
                intent.putExtra("isSelected", "yes");
                intent.putExtra("sound_id", Variables.selectedSoundId);
                startActivity(intent);
            }
            else
            {
                Intent intent = new Intent(DraftVideosA.this, GallerySelectedVideoA.class);
                intent.putExtra("video_path", Functions.getAppFolder(this)+Variables.gallery_resize_video);
                startActivity(intent);
            }




        } catch (Exception e) {
            e.printStackTrace();
            Functions.printLog(Constants.tag, e.toString());
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        deleteFile();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        deleteFile();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        deleteFile();
    }

    // delete the files if exist
    public void deleteFile() {
        File output = new File(Functions.getAppFolder(this)+Variables.outputfile);
        File output2 = new File(Functions.getAppFolder(this)+Variables.outputfile2);
        File gallery_trim_video = new File(Functions.getAppFolder(this)+Variables.gallery_trimed_video);
        File gallery_resize_video = new File(Functions.getAppFolder(this)+Variables.gallery_resize_video);

        if (output.exists()) {
            output.delete();
        }
        if (output2.exists()) {
            output2.delete();
        }


        if (gallery_trim_video.exists()) {
            gallery_trim_video.delete();
        }

        if (gallery_resize_video.exists()) {
            gallery_resize_video.delete();
        }


    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.goBack:
                finish();
                overridePendingTransition(R.anim.in_from_top, R.anim.out_from_bottom);

                break;

        }
    }
}
