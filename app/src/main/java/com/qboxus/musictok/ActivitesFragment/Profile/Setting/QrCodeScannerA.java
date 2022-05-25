package com.qboxus.musictok.ActivitesFragment.Profile.Setting;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import com.qboxus.musictok.SimpleClasses.AppCompatLocaleActivity;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.blikoon.qrcodescanner.decode.DecodeImageCallback;
import com.blikoon.qrcodescanner.decode.DecodeImageThread;
import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.google.zxing.Result;
import com.qboxus.musictok.ActivitesFragment.Profile.ProfileA;
import com.qboxus.musictok.ApiClasses.ApiLinks;
import com.volley.plus.VPackages.VolleyRequest;
import com.qboxus.musictok.Constants;
import com.volley.plus.interfaces.Callback;
import com.qboxus.musictok.Models.UserModel;
import com.qboxus.musictok.R;
import com.qboxus.musictok.SimpleClasses.DataParsing;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.PermissionUtils;
import com.qboxus.musictok.SimpleClasses.Variables;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class QrCodeScannerA extends AppCompatLocaleActivity implements View.OnClickListener{

    private CodeScanner mCodeScanner;
    String userId;
    ImageView ivFlash;
    ProgressBar progressBar;
    PermissionUtils takePermissionUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Functions.setLocale(Functions.getSharedPreference(QrCodeScannerA.this).getString(Variables.APP_LANGUAGE_CODE,Variables.DEFAULT_LANGUAGE_CODE)
                , this, QrCodeScannerA.class,false);
        setContentView(R.layout.activity_qr_code_scanner);

        InitControl();
    }

    private void InitControl() {
        takePermissionUtils=new PermissionUtils(QrCodeScannerA.this,mPermissionResult);
        mQrCodeExecutor = Executors.newSingleThreadExecutor();

        findViewById(R.id.ivBack).setOnClickListener(this);
        findViewById(R.id.tabQrCode).setOnClickListener(this);
        findViewById(R.id.tvAlbum).setOnClickListener(this);
        progressBar=findViewById(R.id.progressBar);
        ivFlash=findViewById(R.id.ivFlash);
        ivFlash.setOnClickListener(this);

        setUpScannerView();
    }

    private void setUpScannerView() {
        CodeScannerView scannerView = findViewById(R.id.scanner_view);
        mCodeScanner = new CodeScanner(this, scannerView);
        mCodeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull final Result result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(Constants.tag,"QR Code "+result.getText());
                        if (result.getText().contains(Variables.http+"://"+getString(R.string.share_profile_domain)+"/profile/"))
                        {
                            try {
                                String[] parts = result.getText().split(Variables.http+"://"+getString(R.string.share_profile_domain)+"/profile/");
                                userId = parts[1];
                            }catch (Exception e){}
                            hitgetUserProfile();
                        }
                        else {
                            Toast.makeText(QrCodeScannerA.this, getString(R.string.user_not_found), Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            }
        });
        scannerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCodeScanner.startPreview();
            }
        });

        if (mCodeScanner.isFlashEnabled())
        {
            ivFlash.setImageDrawable(ContextCompat.getDrawable(QrCodeScannerA.this,R.drawable.ic_scan_flash_on));
        }
        else
        {
            ivFlash.setImageDrawable(ContextCompat.getDrawable(QrCodeScannerA.this,R.drawable.ic_scan_flash_off));
        }
    }

    private void hitgetUserProfile() {

        if (getIntent() == null) {
            userId = Functions.getSharedPreference(QrCodeScannerA.this).getString(Variables.U_ID, "0");
        }

        JSONObject parameters = new JSONObject();
        try {

            if (Functions.getSharedPreference(QrCodeScannerA.this).getBoolean(Variables.IS_LOGIN, false) && userId != null) {
                parameters.put("user_id", Functions.getSharedPreference(QrCodeScannerA.this).getString(Variables.U_ID, ""));
                parameters.put("other_user_id", userId);
            } else if (userId != null) {
                parameters.put("user_id", userId);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        ivFlash.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        VolleyRequest.JsonPostRequest(QrCodeScannerA.this, ApiLinks.showUserDetail, parameters,Functions.getHeaders(this), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.checkStatus(QrCodeScannerA.this,resp);
                progressBar.setVisibility(View.GONE);
                ivFlash.setVisibility(View.VISIBLE);

                parseData(resp);
            }
        });

    }


    public void parseData(String responce) {

        try {
            JSONObject jsonObject = new JSONObject(responce);
            String code = jsonObject.optString("code");
            if (code.equals("200")) {
                JSONObject msg = jsonObject.optJSONObject("msg");


                UserModel userDetailModel= DataParsing.getUserDataModel(msg.optJSONObject("User"));

                moveToProfile(userDetailModel.getId()
                        ,userDetailModel.getUsername()
                        ,userDetailModel.getProfilePic());


            } else {
                Functions.showToast(QrCodeScannerA.this, getString(R.string.user_not_found));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }




    private void moveToProfile(String id,String username,String pic) {
        Intent intent=new Intent(QrCodeScannerA.this, ProfileA.class);
        intent.putExtra("user_id", id);
        intent.putExtra("user_name", username);
        intent.putExtra("user_pic", pic);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCodeScanner.startPreview();
    }

    @Override
    protected void onPause() {
        mCodeScanner.releaseResources();
        super.onPause();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.ivBack:
            {
                QrCodeScannerA.super.onBackPressed();
            }
            break;

            case R.id.tabQrCode:
            {
                QrCodeScannerA.super.onBackPressed();
            }
            break;

            case R.id.tvAlbum:
            {
                if (takePermissionUtils.isStoragePermissionGranted())
                {
                    takePictureFromGallery();
                }
                else
                {
                    takePermissionUtils.showStoragePermissionDailog(getString(R.string.we_need_storage_permission_for_upload_qr_pic));
                }
            }
            break;

            case R.id.ivFlash:
            {
                if (mCodeScanner.isFlashEnabled())
                {
                    ivFlash.setImageDrawable(ContextCompat.getDrawable(QrCodeScannerA.this,R.drawable.ic_scan_flash_off));
                    mCodeScanner.setFlashEnabled(false);
                }
                else
                {
                    ivFlash.setImageDrawable(ContextCompat.getDrawable(QrCodeScannerA.this,R.drawable.ic_scan_flash_on));
                    mCodeScanner.setFlashEnabled(true);
                }
            }
        }
    }

    private void takePictureFromGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        resultCallbackForGallery.launch(intent);
    }


    ActivityResultLauncher<Intent> resultCallbackForGallery = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        Uri uri = data.getData();
                        String imgPath = getPathFromUri(uri);
                        if (imgPath != null && !TextUtils.isEmpty(imgPath) && null != mQrCodeExecutor) {
                            mQrCodeExecutor.execute(new DecodeImageThread(imgPath, mDecodeImageCallback));
                        }

                    }
                }
            });




    private Executor mQrCodeExecutor;

    private DecodeImageCallback mDecodeImageCallback = new DecodeImageCallback() {
        @Override
        public void decodeSucceed(Result result) {
            Log.d(Constants.tag,"QR Code "+result.getText());
            QrCodeScannerA.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String profileURL=Variables.http+"://"+getString(R.string.share_profile_domain_second)+getString(R.string.share_profile_endpoint_second);
                    if (result.getText().contains(profileURL))
                    {
                        try {
                            String[] parts = result.getText().split(profileURL);
                            userId = parts[1];
                        }catch (Exception e){}
                        hitgetUserProfile();
                    }
                    else {
                        Toast.makeText(QrCodeScannerA.this, getString(R.string.user_not_found), Toast.LENGTH_SHORT).show();
                    }

                }
            });

        }

        @Override
        public void decodeFail(int type, String reason) {
            Log.d(Constants.tag, reason);
            QrCodeScannerA.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(QrCodeScannerA.this, getString(R.string.user_not_found), Toast.LENGTH_SHORT).show();
                }
            });
        }
    };

    public String getPathFromUri(Uri uri) {
       try {
           Cursor cursor = getContentResolver().query(uri, null, null, null, null);
           cursor.moveToFirst();
           String document_id = cursor.getString(0);
           document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
           cursor.close();

           cursor = getContentResolver().query(
                   android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                   null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
           cursor.moveToFirst();
           String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
           cursor.close();
           return path;
       }
       catch (Exception e)
       {
           return "";
       }
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
                            blockPermissionCheck.add(Functions.getPermissionStatus(QrCodeScannerA.this,key));
                        }
                    }
                    if (blockPermissionCheck.contains("blocked"))
                    {
                        Functions.showPermissionSetting(QrCodeScannerA.this,getString(R.string.we_need_storage_permission_for_upload_qr_pic));
                    }
                    else
                    if (allPermissionClear)
                    {
                        takePictureFromGallery();
                    }

                }
            });



    @Override
    protected void onDestroy() {
        mPermissionResult.unregister();
        super.onDestroy();
    }

}