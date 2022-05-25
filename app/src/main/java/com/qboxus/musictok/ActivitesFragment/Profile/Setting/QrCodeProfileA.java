package com.qboxus.musictok.ActivitesFragment.Profile.Setting;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import com.qboxus.musictok.SimpleClasses.AppCompatLocaleActivity;
import androidx.core.content.FileProvider;
import com.facebook.drawee.view.SimpleDraweeView;
import com.mindorks.Screenshot;
import com.mindorks.properties.Quality;
import com.qboxus.musictok.ActivitesFragment.Profile.ShareItemViaIntentA;
import com.qboxus.musictok.Constants;
import com.qboxus.musictok.Interfaces.ShareIntentCallback;
import com.qboxus.musictok.R;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.ImageSaver;
import com.qboxus.musictok.SimpleClasses.PermissionUtils;
import com.qboxus.musictok.SimpleClasses.Variables;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class QrCodeProfileA extends AppCompatLocaleActivity implements View.OnClickListener{

    SimpleDraweeView ivUserProfile,imgQrCode;
    TextView tvName;
    RelativeLayout qrContainerBg,tabScreenShot;
    PermissionUtils takePermissionUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Functions.setLocale(Functions.getSharedPreference(QrCodeProfileA.this).getString(Variables.APP_LANGUAGE_CODE,Variables.DEFAULT_LANGUAGE_CODE)
                , this, QrCodeProfileA.class,false);
        setContentView(R.layout.activity_qr_code_profile);

        inItControl();
        String profielLink = Variables.http+"://"+getString(R.string.share_profile_domain_second)+getString(R.string.share_profile_endpoint_second) + Functions.getSharedPreference(QrCodeProfileA.this).getString(Variables.U_ID,"");
        GenrateQrCode(imgQrCode,profielLink);
    }

    private void inItControl() {
        tabScreenShot=findViewById(R.id.tabScreenShot);
        tabScreenShot.setOnClickListener(this);
        qrContainerBg=findViewById(R.id.qrContainerBg);
        qrContainerBg.setOnClickListener(this);
        tvName=findViewById(R.id.tvName);
        ivUserProfile=findViewById(R.id.ivUserProfile);
        imgQrCode=findViewById(R.id.ivQr);
        findViewById(R.id.ivBack).setOnClickListener(this);
        findViewById(R.id.ivShareQrCode).setOnClickListener(this);
        findViewById(R.id.tabSaveQr).setOnClickListener(this);
        findViewById(R.id.tabScanQr).setOnClickListener(this);


        setUpScreenData();
    }

    private void setUpScreenData() {
        String fullName=Functions.getSharedPreference(QrCodeProfileA.this).getString(Variables.F_NAME,"")+" "+
                Functions.getSharedPreference(QrCodeProfileA.this).getString(Variables.L_NAME,"");
        tvName.setText(fullName);
        String imgUrl=Functions.getSharedPreference(QrCodeProfileA.this).getString(Variables.U_PIC,"");

        ivUserProfile.setController(Functions.frescoImageLoad(imgUrl,ivUserProfile,false));
    }


    private void GenrateQrCode(SimpleDraweeView img_qr, String qr_string) {

        WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        int width = point.x;
        int height = point.y;
        int smallerDimension = width < height ? width : height;
        smallerDimension = smallerDimension * 3 / 4;

        QRGEncoder qrgEncoder = new QRGEncoder(
                qr_string, null,
                QRGContents.Type.TEXT,
                smallerDimension);
        try {
            Bitmap bitmap = qrgEncoder.encodeAsBitmap();
            File uriPath=Functions.getBitmapToUri(QrCodeProfileA.this,bitmap,"qrCodeTemporary.jpg");

            img_qr.setController(Functions.frescoImageLoad(Uri.fromFile(uriPath),false));
        } catch (Exception e) {
            Functions.printLog(Constants.tag,"Exception : "+e);
        }

    }


    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.ivBack:
            {
                QrCodeProfileA.super.onBackPressed();
            }
            break;
            case R.id.ivShareQrCode:
            {
                Bitmap bitmap=Screenshot.INSTANCE.with(QrCodeProfileA.this)
                        .setView(tabScreenShot)
                        .setQuality(Quality.HIGH)
                        .getScreenshot();
                String imgName="QrScreenshot.png";
                new ImageSaver(QrCodeProfileA.this,true).
                        setFileName(imgName).
                        setDirectoryName("Screenshots").
                        save(bitmap);

                final ShareItemViaIntentA fragment = new ShareItemViaIntentA(new ShareIntentCallback() {
                    @Override
                    public void onResponse(ResolveInfo resolveInfo) {
                        Bitmap bitmap=Screenshot.INSTANCE.with(QrCodeProfileA.this)
                                .setView(tabScreenShot)
                                .setQuality(Quality.HIGH)
                                .getScreenshot();
                        shareProfile(bitmap,resolveInfo);
                    }
                });
                fragment.show(getSupportFragmentManager(), "");

            }
            break;
            case R.id.tabSaveQr:
            {
                takePermissionUtils=new PermissionUtils(QrCodeProfileA.this, mPermissionStorageResult);
                if (takePermissionUtils.isStoragePermissionGranted()) {
                    saveQrPicture();
                }
                else
                {
                    takePermissionUtils.showStoragePermissionDailog(getString(R.string.we_need_storage_permission_for_save_qr_code));
                }
            }
            break;
            case R.id.tabScanQr:
            {
                takePermissionUtils=new PermissionUtils(QrCodeProfileA.this, mPermissionCameraResult);
                if (takePermissionUtils.isCameraPermissionGranted())
                {
                    moveScannerScreen();
                }
                else
                {
                    takePermissionUtils.showCameraPermissionDailog(getString(R.string.we_need_camera_permission_for_qr_scan));
                }
            }
            break;
            case R.id.tabScreenShot:
            {
                changeBgColorRandom();
            }
            break;
        }
    }

    private void moveScannerScreen() {
        startActivity(new Intent(QrCodeProfileA.this,QrCodeScannerA.class));
    }


    private void saveQrPicture() {
        Bitmap bitmap=Screenshot.INSTANCE.with(QrCodeProfileA.this)
                .setView(tabScreenShot)
                .setQuality(Quality.HIGH)
                .getScreenshot();
        String imgName=Functions.getCurrentDate("yyyy-MM-dd")+"screenShot.png";

        if (Build.VERSION.SDK_INT>Build.VERSION_CODES.P)
        {
            saveAEImage(bitmap,imgName);
        }
        else
        {
            new ImageSaver(QrCodeProfileA.this,false).
                    setFileName(imgName).
                    setDirectoryName("Screenshots").
                    save(bitmap);
        }

    }

    private void saveAEImage(Bitmap bitmap, String imageName) {
        OutputStream outputStream;
        try {
            ContentResolver resolver=getContentResolver();
            ContentValues contentValues=new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME,imageName);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE,"image/jpg");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH,Environment.DIRECTORY_DCIM+File.separator+"Screenshots");
            Uri imagePhoto=resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);
            outputStream=resolver.openOutputStream(Objects.requireNonNull(imagePhoto));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
            Objects.requireNonNull(outputStream);
            Toast.makeText(QrCodeProfileA.this, getString(R.string.image_save_sucessfully), Toast.LENGTH_SHORT).show();
        }
        catch (Exception e)
        {
            Functions.printLog(Constants.tag,"Exception : "+e);
        }
    }


    private void shareProfile(Bitmap bitmap,ResolveInfo resolveInfo) {

        Uri uri = getmageToShare(bitmap);
        Intent intent = new Intent(Intent.ACTION_SEND);

        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.putExtra(Intent.EXTRA_TEXT, "");
        intent.putExtra(Intent.EXTRA_SUBJECT, "");
        ActivityInfo activity = resolveInfo.activityInfo;
        ComponentName name = new ComponentName(activity.applicationInfo.packageName,
                activity.name);
        intent.setComponent(name);
        intent.setType("image/png");
        startActivity(Intent.createChooser(intent, ""));
    }


    private Uri getmageToShare(Bitmap bitmap) {
        File imagefolder = new File(getCacheDir(), "images");
        Uri uri = null;
        try {
            imagefolder.mkdirs();
            File file = new File(imagefolder, "shared_image.png");
            FileOutputStream outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream);
            outputStream.flush();
            outputStream.close();
            uri = FileProvider.getUriForFile(this, getPackageName()+".fileprovider", file);
        } catch (Exception e) {
            Log.d(Constants.tag,"Exception : "+e);
        }
        return uri;
    }

    private void changeBgColorRandom() {
    String colorArray[]=getResources().
            getStringArray(R.array.bg_color_array);

        int random = new Random().nextInt(colorArray.length);
        qrContainerBg.setBackgroundColor(Color.parseColor(colorArray[random]));
        tabScreenShot.setBackgroundColor(Color.parseColor(colorArray[random]));
    }



    private ActivityResultLauncher<String[]> mPermissionStorageResult = registerForActivityResult(
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
                            blockPermissionCheck.add(Functions.getPermissionStatus(QrCodeProfileA.this,key));
                        }
                    }
                    if (blockPermissionCheck.contains("blocked"))
                    {
                        Functions.showPermissionSetting(QrCodeProfileA.this,getString(R.string.we_need_storage_permission_for_save_qr_code));
                    }
                    else
                    if (allPermissionClear)
                    {
                        saveQrPicture();
                    }

                }
            });


    private ActivityResultLauncher<String[]> mPermissionCameraResult = registerForActivityResult(
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
                            blockPermissionCheck.add(Functions.getPermissionStatus(QrCodeProfileA.this,key));
                        }
                    }
                    if (blockPermissionCheck.contains("blocked"))
                    {
                        Functions.showPermissionSetting(QrCodeProfileA.this,getString(R.string.we_need_camera_permission_for_qr_scan));
                    }
                    else
                    if (allPermissionClear)
                    {
                        moveScannerScreen();
                    }

                }
            });


    @Override
    protected void onDestroy() {
        mPermissionStorageResult.unregister();
        mPermissionCameraResult.unregister();
        super.onDestroy();
    }

}