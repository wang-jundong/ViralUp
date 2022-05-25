package com.qboxus.musictok.SimpleClasses;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.qboxus.musictok.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageSaver{

        private String directoryName = "images";
        private String fileName = "image.png";
        private Activity context;
        boolean isSecure=false;


        public ImageSaver(Activity context,boolean isSecure) {
            this.context = context;
            this.isSecure=isSecure;
        }

        public ImageSaver setFileName(String fileName) {
            this.fileName = fileName;
            return this;
        }


        public ImageSaver setDirectoryName(String directoryName) {
            this.directoryName = directoryName;
            return this;
        }

        public void save(Bitmap bitmapImage) {
            FileOutputStream fileOutputStream = null;
            File outPutFile=createFile();
            try {
                fileOutputStream = new FileOutputStream(outPutFile);
                bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            MediaScannerConnection.scanFile(context,
                    new String[]{outPutFile.getAbsolutePath()},
                    null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                            context.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (!(isSecure))
                                    Toast.makeText(context, context.getString(R.string.image_save_sucessfully), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
        }

        @NonNull
        private File createFile() {
            File directory;
            if (isSecure)
            {
                directory=new File(Functions.getAppFolder(context)+"/"+directoryName+"/");
            }
            else
            {
                directory=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)+"/"+directoryName+"/");
            }
            if (!directory.exists())
                directory.mkdirs();
            return new File(directory, fileName);
        }


}