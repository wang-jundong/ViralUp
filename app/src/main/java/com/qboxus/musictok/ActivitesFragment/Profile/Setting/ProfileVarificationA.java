package com.qboxus.musictok.ActivitesFragment.Profile.Setting;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import com.qboxus.musictok.SimpleClasses.AppCompatLocaleActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.qboxus.musictok.ApiClasses.ApiLinks;
import com.volley.plus.VPackages.VolleyRequest;
import com.volley.plus.interfaces.Callback;
import com.qboxus.musictok.R;
import com.qboxus.musictok.SimpleClasses.FileUtils;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.PermissionUtils;
import com.qboxus.musictok.SimpleClasses.Variables;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProfileVarificationA extends AppCompatLocaleActivity implements View.OnClickListener {

    EditText usernameEdit, fullnameEdit;
    TextView fileNameTxt,chooseFileBtn,tvTitle,tvInstruction;
    String base64;
    File image_file;
    PermissionUtils takePermissionUtils;
    Button sendBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Functions.setLocale(Functions.getSharedPreference(ProfileVarificationA.this).getString(Variables.APP_LANGUAGE_CODE,Variables.DEFAULT_LANGUAGE_CODE)
                , this, ProfileVarificationA.class,false);
        setContentView(R.layout.activity_profile_varification);

        InitControl();

    }

    private void InitControl() {
        takePermissionUtils=new PermissionUtils(ProfileVarificationA.this,mPermissionResult);
        findViewById(R.id.goBack).setOnClickListener(this);
        tvTitle=findViewById(R.id.tvTitle);
        tvInstruction=findViewById(R.id.tvInstruction);
        chooseFileBtn=findViewById(R.id.choose_file_btn);
        chooseFileBtn.setOnClickListener(this);
        sendBtn=findViewById(R.id.send_btn);
        sendBtn.setOnClickListener(this);
        fileNameTxt =findViewById(R.id.file_name_txt);
        usernameEdit = findViewById(R.id.username_edit);
        fullnameEdit = findViewById(R.id.fullname_edit);


        setUpScreendata();
    }

    private void setUpScreendata() {
        String applyForverification=getString(R.string.apply_for)+" "+getString(R.string.app_name)+" "+getString(R.string.verification);
        tvTitle.setText(applyForverification);
        fileNameTxt.setText(applyForverification);
        String instruction=getString(R.string.verification_instruction_one)+getString(R.string.app_name)+" "+getString(R.string.verification_instruction_two);
        tvInstruction.setText(instruction);

        if (Functions.getSharedPreference(ProfileVarificationA.this).getString(Variables.IS_VERIFICATION_APPLY, "0").equalsIgnoreCase("1"))
        {
            fileNameTxt.setTextColor(ContextCompat.getColor(ProfileVarificationA.this,R.color.greenColor));
            fileNameTxt.setText(getString(R.string.verification_request_already_apply));
            sendBtn.setVisibility(View.GONE);
            chooseFileBtn.setVisibility(View.GONE);
        }
        else
        {
            sendBtn.setVisibility(View.VISIBLE);
            chooseFileBtn.setVisibility(View.VISIBLE);
        }
        usernameEdit.setText(Functions.getSharedPreference(ProfileVarificationA.this).getString(Variables.U_NAME, ""));
        fullnameEdit.setText(Functions.getSharedPreference(ProfileVarificationA.this).getString(Variables.F_NAME, "") + " " + Functions.getSharedPreference(ProfileVarificationA.this).getString(Variables.L_NAME, ""));

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.goBack:
                ProfileVarificationA.super.onBackPressed();
                break;

            case R.id.choose_file_btn:
                if (takePermissionUtils.isStorageCameraPermissionGranted()) {
                    selectImage();
                }
                else
                {
                    takePermissionUtils.
                            showStorageCameraPermissionDailog(getString(R.string.we_need_storage_and_camera_permission_for_upload_verification_pic));
                }
                break;
            case R.id.send_btn:
                if (checkValidation()) {
                    callApi();
                }
                break;
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
                            blockPermissionCheck.add(Functions.getPermissionStatus(ProfileVarificationA.this,key));
                        }
                    }
                    if (blockPermissionCheck.contains("blocked"))
                    {
                        Functions.showPermissionSetting(ProfileVarificationA.this,getString(R.string.we_need_storage_and_camera_permission_for_upload_profile_pic));
                    }
                    else
                    if (allPermissionClear)
                    {
                        selectImage();
                    }

                }
            });


    // this method will show the dialog of selete the either take a picture form camera or pick the image from gallary
    private void selectImage() {

        final CharSequence[] options = {getString(R.string.take_photo), getString(R.string.choose_from_gallery), getString(R.string.cancel_)};
        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileVarificationA.this, R.style.AlertDialogCustom);
        builder.setTitle(getString(R.string.add_photo_));
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (options[item].equals(getString(R.string.take_photo))) {
                    openCameraIntent();
                } else if (options[item].equals(getString(R.string.choose_from_gallery))) {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    resultCallbackForGallery.launch(intent);
                } else if (options[item].equals(getString(R.string.cancel_))) {

                    dialog.dismiss();


                }

            }

        });

        builder.show();

    }

    ActivityResultLauncher<Intent> resultCallbackForGallery = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        Uri selectedImage = data.getData();
                        try {
                            image_file = FileUtils.getFileFromUri(ProfileVarificationA.this, selectedImage);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        InputStream imageStream = null;
                        try {
                            imageStream = ProfileVarificationA.this.getContentResolver().openInputStream(selectedImage);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        final Bitmap imagebitmap = BitmapFactory.decodeStream(imageStream);

                        String path = getPath(selectedImage);
                        Matrix matrix = new Matrix();
                        ExifInterface exif = null;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                            try {
                                exif = new ExifInterface(path);
                                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
                                switch (orientation) {
                                    case ExifInterface.ORIENTATION_ROTATE_90:
                                        matrix.postRotate(90);
                                        break;
                                    case ExifInterface.ORIENTATION_ROTATE_180:
                                        matrix.postRotate(180);
                                        break;
                                    case ExifInterface.ORIENTATION_ROTATE_270:
                                        matrix.postRotate(270);
                                        break;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        Bitmap rotatedBitmap = Bitmap.createBitmap(imagebitmap, 0, 0, imagebitmap.getWidth(), imagebitmap.getHeight(), matrix, true);


                        Bitmap resized = Bitmap.createScaledBitmap(rotatedBitmap, (int) (rotatedBitmap.getWidth() * 0.5), (int) (rotatedBitmap.getHeight() * 0.5), true);

                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        resized.compress(Bitmap.CompressFormat.JPEG, 20, baos);

                        base64 = Functions.bitmapToBase64(resized);

                        if (image_file != null)
                            fileNameTxt.setText(image_file.getName());


                    }
                }
            });

    ActivityResultLauncher<Intent> resultCallbackForCamera = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {

                        Matrix matrix = new Matrix();
                        try {
                            ExifInterface exif = new ExifInterface(imageFilePath);
                            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
                            switch (orientation) {
                                case ExifInterface.ORIENTATION_ROTATE_90:
                                    matrix.postRotate(90);
                                    break;
                                case ExifInterface.ORIENTATION_ROTATE_180:
                                    matrix.postRotate(180);
                                    break;
                                case ExifInterface.ORIENTATION_ROTATE_270:
                                    matrix.postRotate(270);
                                    break;
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        image_file = new File(imageFilePath);
                        Uri selectedImage = (Uri.fromFile(image_file));

                        InputStream imageStream = null;
                        try {
                            imageStream = ProfileVarificationA.this.getContentResolver().openInputStream(selectedImage);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        final Bitmap imagebitmap = BitmapFactory.decodeStream(imageStream);
                        Bitmap rotatedBitmap = Bitmap.createBitmap(imagebitmap, 0, 0, imagebitmap.getWidth(), imagebitmap.getHeight(), matrix, true);

                        Bitmap resized = Bitmap.createScaledBitmap(rotatedBitmap, (int) (rotatedBitmap.getWidth() * 0.7), (int) (rotatedBitmap.getHeight() * 0.7), true);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        resized.compress(Bitmap.CompressFormat.JPEG, 20, baos);

                        base64 = Functions.bitmapToBase64(resized);

                        if (image_file != null)
                            fileNameTxt.setText(image_file.getName());
                    }
                }
            });


    // below three method is related with taking the picture from camera
    private void openCameraIntent() {
        Intent pictureIntent = new Intent(
                MediaStore.ACTION_IMAGE_CAPTURE);
        if (pictureIntent.resolveActivity(ProfileVarificationA.this.getPackageManager()) != null) {
            //Create a file to store the image
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (Exception ex) {

            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(ProfileVarificationA.this.getApplicationContext(), ProfileVarificationA.this.getPackageName() + ".fileprovider", photoFile);
                pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                resultCallbackForCamera.launch(pictureIntent);
            }
        }
    }

    String imageFilePath;
    private File createImageFile() throws Exception {
        String timeStamp =
                new SimpleDateFormat("yyyyMMdd_HHmmss",
                        Locale.ENGLISH).format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir =
                ProfileVarificationA.this.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        imageFilePath = image.getAbsolutePath();
        return image;
    }

    public String getPath(Uri uri) {
        String result = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = ProfileVarificationA.this.getContentResolver().query(uri, proj, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int column_index = cursor.getColumnIndexOrThrow(proj[0]);
                result = cursor.getString(column_index);
            }
            cursor.close();
        }
        if (result == null) {
            result = "Not found";
        }
        return result;
    }






    // this will check the validations like none of the field can be the empty
    public boolean checkValidation() {

        String uname = usernameEdit.getText().toString();
        String fullname = fullnameEdit.getText().toString();

        if (TextUtils.isEmpty(uname) || uname.length() < 2) {
            Functions.showToast(ProfileVarificationA.this, getString(R.string.enter_valid_username));
            return false;
        } else if (TextUtils.isEmpty(fullname)) {
            Functions.showToast(ProfileVarificationA.this, getString(R.string.enter_full_name));
            return false;
        } else if (base64 == null) {
            Functions.showToast(ProfileVarificationA.this, getString(R.string.select_image));
            return false;
        }

        return true;
    }


    public void callApi() {
        JSONObject params = new JSONObject();
        try {
            params.put("user_id", Functions.getSharedPreference(ProfileVarificationA.this).getString(Variables.U_ID, ""));
            JSONObject file_data = new JSONObject();
            file_data.put("file_data", base64);
            params.put("attachment", file_data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Functions.showLoader(ProfileVarificationA.this, false, false);
        VolleyRequest.JsonPostRequest(ProfileVarificationA.this, ApiLinks.userVerificationRequest, params,Functions.getHeaders(this), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.checkStatus(ProfileVarificationA.this,resp);
                Functions.cancelLoader();
                try {
                    JSONObject jsonObject = new JSONObject(resp);
                    String code = jsonObject.optString("code");
                    if (code.equalsIgnoreCase("200")) {
                        Functions.showToast(ProfileVarificationA.this, getString(R.string.request_sent_sucessfully));
                        ProfileVarificationA.super.onBackPressed();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

    }

    @Override
    protected void onDestroy() {
        mPermissionResult.unregister();
        super.onDestroy();
    }
}
