package com.qboxus.musictok.ActivitesFragment.Profile.Setting;

import com.qboxus.musictok.SimpleClasses.AppCompatLocaleActivity;

import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.facebook.binaryresource.BinaryResource;
import com.facebook.binaryresource.FileBinaryResource;
import com.facebook.cache.common.CacheKey;
import com.facebook.cache.disk.FileCache;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.cache.DefaultCacheKeyFactory;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.core.ImagePipelineFactory;
import com.facebook.imagepipeline.request.ImageRequest;
import com.qboxus.musictok.Constants;
import com.qboxus.musictok.R;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.Variables;

import java.io.Console;
import java.io.File;

public class AppSpaceClearA extends AppCompatLocaleActivity implements View.OnClickListener {


    TextView tvCache,tvDownload;
    File cacheFile,downloadFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Functions.setLocale(Functions.getSharedPreference(AppSpaceClearA.this).getString(Variables.APP_LANGUAGE_CODE,Variables.DEFAULT_LANGUAGE_CODE)
                , this, AppSpaceClearA.class,false);
        setContentView(R.layout.activity_app_space_clear);
        InitControl();
    }

    private void InitControl() {
        findViewById(R.id.back_btn).setOnClickListener(this);
        findViewById(R.id.tvCacheClear).setOnClickListener(this);
        findViewById(R.id.tvDownloadClear).setOnClickListener(this);
        tvCache=findViewById(R.id.tvCache);
        tvDownload=findViewById(R.id.tvDownload);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.back_btn:
            {
                AppSpaceClearA.super.onBackPressed();
            }
            break;
            case R.id.tvCacheClear:
            {
                runClearCacheMethod();
            }
            break;
            case R.id.tvDownloadClear:
            {
                runClearDownloadMethod();
            }
            break;
        }
    }

    private void runClearDownloadMethod() {
        Functions.showLoader(AppSpaceClearA.this,false,false);
        AppSpaceClearA.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                deleteDir(downloadFile);
            }
        });
    }





    private boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    calculateUsageSpace();
                    return false;
                }
            }
            calculateUsageSpace();
            return dir.delete();
        } else if(dir!= null && dir.isFile()) {
            calculateUsageSpace();
            return dir.delete();
        } else {
            calculateUsageSpace();
            return false;
        }
    }


    private void runClearCacheMethod() {
        Functions.showLoader(AppSpaceClearA.this,false,false);
        AppSpaceClearA.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                deleteDir(cacheFile);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        calculateUsageSpace();
    }

    private void calculateUsageSpace() {
        try {
            Functions.cancelLoader();
        }catch (Exception e){}
        tvCache.setText(getString(R.string.cache)+" : "+getUsageCache());
        tvDownload.setText(getString(R.string.download)+" : "+getUsageDownload());

    }


    private String getUsageDownload() {
        String totalDownload="";
        downloadFile=new File(Functions.getAppFolder(AppSpaceClearA.this));
        Log.d(Constants.tag,"Check : "+downloadFile.getAbsolutePath());
        if (downloadFile.exists())
        {
            totalDownload=Functions.getDirectorySize(downloadFile.getAbsolutePath());
        }
        else
        {
            totalDownload="";
        }

        return totalDownload;
    }

    private String getUsageCache() {
        String totalCache="";
        cacheFile=AppSpaceClearA.this.getCacheDir();
        Log.d(Constants.tag,"Check : "+cacheFile.getAbsolutePath());
        if (cacheFile.exists())
        {
            totalCache=Functions.getDirectorySize(cacheFile.getAbsolutePath());
        }
        else
        {
            totalCache="";
        }

        return totalCache;
    }
}