package com.qboxus.musictok.ActivitesFragment.Profile;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import com.qboxus.musictok.SimpleClasses.AppCompatLocaleActivity;
import androidx.core.content.FileProvider;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.qboxus.musictok.ApiClasses.ApiLinks;
import com.volley.plus.VPackages.VolleyRequest;
import com.qboxus.musictok.Constants;
import com.volley.plus.interfaces.APICallBack;
import com.volley.plus.interfaces.Callback;
import com.qboxus.musictok.Interfaces.KeyboardHeightObserver;
import com.qboxus.musictok.Models.UserModel;
import com.qboxus.musictok.R;
import com.qboxus.musictok.SimpleClasses.DataParsing;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.KeyboardHeightProvider;
import com.qboxus.musictok.SimpleClasses.PermissionUtils;
import com.qboxus.musictok.SimpleClasses.Variables;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONArray;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EditProfileA extends AppCompatLocaleActivity implements View.OnClickListener{


    Context context;
    SimpleDraweeView profileImage;
    EditText usernameEdit, firstnameEdit, lastnameEdit, websiteEdit, userBioEdit;
    RadioButton maleBtn, femaleBtn;
    TextView usernameCountTxt, bioCountTxt;

    //for Permission taken
    PermissionUtils takePermissionUtils;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Functions.setLocale(Functions.getSharedPreference(EditProfileA.this).getString(Variables.APP_LANGUAGE_CODE,Variables.DEFAULT_LANGUAGE_CODE), this, EditProfileA.class,false);
        setContentView(R.layout.activity_edit_profile);
        context = EditProfileA.this;

        takePermissionUtils=new PermissionUtils(EditProfileA.this,mPermissionResult);

        findViewById(R.id.goBack).setOnClickListener(this);
        findViewById(R.id.save_btn).setOnClickListener(this);
        findViewById(R.id.upload_pic_btn).setOnClickListener(this);


        usernameEdit = findViewById(R.id.username_edit);
        profileImage = findViewById(R.id.profile_image);
        firstnameEdit = findViewById(R.id.firstname_edit);
        lastnameEdit = findViewById(R.id.lastname_edit);
        websiteEdit = findViewById(R.id.website_edit);
        userBioEdit = findViewById(R.id.user_bio_edit);

        usernameEdit.setText(Functions.getSharedPreference(context).getString(Variables.U_NAME, ""));
        firstnameEdit.setText(Functions.getSharedPreference(context).getString(Variables.F_NAME, ""));
        lastnameEdit.setText(Functions.getSharedPreference(context).getString(Variables.L_NAME, ""));

        String pic = Functions.getSharedPreference(context).getString(Variables.U_PIC, "");

        profileImage.setController(Functions.frescoImageLoad(pic,profileImage,false));

        maleBtn = findViewById(R.id.male_btn);
        femaleBtn = findViewById(R.id.female_btn);


        usernameCountTxt = findViewById(R.id.username_count_txt);
        bioCountTxt = findViewById(R.id.bio_count_txt);


        // add the input filter to eidt text of username
        InputFilter[] username_filters = new InputFilter[1];
        username_filters[0] = new InputFilter.LengthFilter(Constants.USERNAME_CHAR_LIMIT);
        usernameEdit.setFilters(username_filters);
        usernameEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                usernameCountTxt.setText(usernameEdit.getText().length() + "/" + Constants.USERNAME_CHAR_LIMIT);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        // add the input filter to edittext of userbio
        InputFilter[] filters = new InputFilter[1];
        filters[0] = new InputFilter.LengthFilter(Constants.BIO_CHAR_LIMIT);
        userBioEdit.setFilters(filters);
        userBioEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                bioCountTxt.setText(userBioEdit.getText().length() + "/" + Constants.BIO_CHAR_LIMIT);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        callApiForUserDetails();
        setKeyboardListener();
    }


    //intialize the keyboard listener
    int priviousHeight = 0;

    private void setKeyboardListener() {

        KeyboardHeightProvider keyboardHeightProvider = new KeyboardHeightProvider(EditProfileA.this);
        keyboardHeightProvider.setKeyboardHeightObserver(new KeyboardHeightObserver() {
            @Override
            public void onKeyboardHeightChanged(int height, int orientation) {
                Functions.printLog(Constants.tag, "" + height);
                if (height < 0) {
                    priviousHeight = Math.abs(height);
                }

                LinearLayout main_layout = findViewById(R.id.main_layout);

                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(main_layout.getWidth(), main_layout.getHeight());
                params.bottomMargin = height + priviousHeight;
                main_layout.setLayoutParams(params);
            }
        });


       findViewById(R.id.edit_Profile_F).post(new Runnable() {
            public void run() {
                keyboardHeightProvider.start();
            }
        });


    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.goBack:
                EditProfileA.super.onBackPressed();
                break;

            case R.id.save_btn:
                if (checkValidation()) {
                    callApiForEditProfile();
                }
                break;
            case R.id.upload_pic_btn:
                if (takePermissionUtils.isStorageCameraPermissionGranted()) {
                    selectImage();
                }
                else
                {
                    takePermissionUtils.
                            showStorageCameraPermissionDailog(getString(R.string.we_need_storage_and_camera_permission_for_upload_profile_pic));
                }
                break;

            default:
                return;
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
                            blockPermissionCheck.add(Functions.getPermissionStatus(EditProfileA.this,key));
                        }
                    }
                    if (blockPermissionCheck.contains("blocked"))
                    {
                        Functions.showPermissionSetting(EditProfileA.this,getString(R.string.we_need_storage_and_camera_permission_for_upload_profile_pic));
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

        final CharSequence[] options = {getString(R.string.take_photo),
                getString(R.string.choose_from_gallery), getString(R.string.cancel_)};


        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogCustom);

        builder.setTitle(getString(R.string.add_photo_));

        builder.setItems(options, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (options[item].equals(getString(R.string.take_photo))) {
                    openCameraIntent();
                } else if (options[item].equals(getString(R.string.choose_from_gallery))) {
                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    resultCallbackForGallery.launch(intent);
                } else if (options[item].equals(getString(R.string.cancel_))) {

                    dialog.dismiss();

                }

            }


        });

        builder.show();

    }


    ActivityResultLauncher<Intent> resultCallbackForCrop = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        CropImage.ActivityResult cropResult = CropImage.getActivityResult(data);
                        handleCrop(cropResult.getUri());
                    }
                }
            });


    ActivityResultLauncher<Intent> resultCallbackForGallery = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        Uri selectedImage = data.getData();
                        beginCrop(selectedImage);

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
                            android.media.ExifInterface exif = new android.media.ExifInterface(imageFilePath);
                            int orientation = exif.getAttributeInt(android.media.ExifInterface.TAG_ORIENTATION, 1);
                            switch (orientation) {
                                case android.media.ExifInterface.ORIENTATION_ROTATE_90:
                                    matrix.postRotate(90);
                                    break;
                                case android.media.ExifInterface.ORIENTATION_ROTATE_180:
                                    matrix.postRotate(180);
                                    break;
                                case android.media.ExifInterface.ORIENTATION_ROTATE_270:
                                    matrix.postRotate(270);
                                    break;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Uri selectedImage = (Uri.fromFile(new File(imageFilePath)));
                        beginCrop(selectedImage);
                    }
                }
            });


    // below three method is related with taking the picture from camera
    private void openCameraIntent() {
        Intent pictureIntent = new Intent(
                MediaStore.ACTION_IMAGE_CAPTURE);
        if (pictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (Exception ex) {
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(context.getApplicationContext(), getPackageName() + ".fileprovider", photoFile);
                pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                resultCallbackForCamera.launch(pictureIntent);
            }
        }
    }

    // create a temp image file
    String imageFilePath;

    private File createImageFile() throws Exception {
        String timeStamp =
                new SimpleDateFormat("yyyyMMdd_HHmmss",
                        Locale.ENGLISH).format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir =
                getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        imageFilePath = image.getAbsolutePath();
        return image;
    }

    // this will check the validations like none of the field can be the empty
    public boolean checkValidation() {

        String uname = usernameEdit.getText().toString();
        String firstname = firstnameEdit.getText().toString();
        String lastname = lastnameEdit.getText().toString();

        if (TextUtils.isEmpty(uname)) {
            usernameEdit.setError(getString(R.string.please_correct_user_name));
            usernameEdit.setFocusable(true);
            return false;
        } else if (uname.length() < 4 || uname.length() > 14) {
            usernameEdit.setError(getString(R.string.username_length_between_valid));
           usernameEdit.setFocusable(true);
            return false;
        } else
        if (!(UserNameTwoCaseValidate(uname)))
        {
            usernameEdit.setError(getString(R.string.username_must_contain_alphabet));
            usernameEdit.setFocusable(true);
            return false;
        }else if (TextUtils.isEmpty(firstname)) {
            firstnameEdit.setError(getString(R.string.please_enter_first_name));
            firstnameEdit.setFocusable(true);
            return false;
        } else if (TextUtils.isEmpty(lastname)) {
            lastnameEdit.setError(getString(R.string.please_enter_last_name));
            lastnameEdit.setFocusable(true);
            return false;
        } else if (!maleBtn.isChecked() && !femaleBtn.isChecked()) {
            Toast.makeText(context, getString(R.string.please_select_your_gender), Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private boolean UserNameTwoCaseValidate(String name) {

        Pattern let_p = Pattern.compile("[a-z]", Pattern.CASE_INSENSITIVE);
        Matcher let_m = let_p.matcher(name);
        boolean let_str = let_m.find();

        if (let_str)
        {
            return true;
        }
        return false;
    }


    String imageBas64Small,imageBas64Big;

    private void beginCrop(Uri source) {
        Intent intent=CropImage.activity(source).setCropShape(CropImageView.CropShape.OVAL)
                .setAspectRatio(1,1).getIntent(EditProfileA.this);
        resultCallbackForCrop.launch(intent);
    }

    // get the image uri after the image crope
    private void handleCrop(Uri userimageuri) {

        InputStream imageStream = null;
        try {
            imageStream = getContentResolver().openInputStream(userimageuri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        final Bitmap imagebitmap = BitmapFactory.decodeStream(imageStream);

        String path = userimageuri.getPath();
        Matrix matrix = new Matrix();
        android.media.ExifInterface exif = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            try {
                exif = new android.media.ExifInterface(path);
                int orientation = exif.getAttributeInt(android.media.ExifInterface.TAG_ORIENTATION, 1);
                switch (orientation) {
                    case android.media.ExifInterface.ORIENTATION_ROTATE_90:
                        matrix.postRotate(90);
                        break;
                    case android.media.ExifInterface.ORIENTATION_ROTATE_180:
                        matrix.postRotate(180);
                        break;
                    case android.media.ExifInterface.ORIENTATION_ROTATE_270:
                        matrix.postRotate(270);
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        Bitmap rotatedBitmap = Bitmap.createBitmap(imagebitmap, 0, 0, imagebitmap.getWidth(), imagebitmap.getHeight(), matrix, true);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        imageBas64Big=Functions.bitmapToBase64(rotatedBitmap);
        Bitmap converetdImage = getResizedBitmap(rotatedBitmap, Constants.PROFILE_IMAGE_SQUARE_SIZE);
        imageBas64Small = Functions.bitmapToBase64(converetdImage);

        callApiForImage();
    }


    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }


    // call api for upload the image on server
    public void callApiForImage() {

        JSONObject parameters = new JSONObject();
        try {
            parameters.put("user_id", Functions.getSharedPreference(context).getString(Variables.U_ID, "0"));

            JSONObject fileBig = new JSONObject();
            fileBig.put("file_data", imageBas64Big);
            parameters.put("profile_pic", fileBig);

            JSONObject fileSmall = new JSONObject();
            fileSmall.put("file_data", imageBas64Small);
            parameters.put("profile_pic_small", fileSmall);

        } catch (Exception e) {
            e.printStackTrace();
        }
        Functions.showLoader(context, false, false);
        VolleyRequest.JsonPostRequest(EditProfileA.this, ApiLinks.addUserImage, parameters,Functions.getHeaders(this), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.checkStatus(EditProfileA.this,resp);
                Functions.cancelLoader();
                try {
                    JSONObject response = new JSONObject(resp);
                    String code = response.optString("code");
                    JSONObject msg = response.optJSONObject("msg");
                    if (code.equals("200")) {


                        UserModel userDetailModel= DataParsing.getUserDataModel(msg.optJSONObject("User"));

                        Functions.getSharedPreference(context).edit().putString(Variables.U_PIC, userDetailModel.getProfilePic()).commit();

                        profileImage.setController(Functions.frescoImageLoad(Functions.getSharedPreference(context).getString(Variables.U_PIC, ""),profileImage,false));




                        Functions.showToast(context, getString(R.string.image_update_successfully));
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });


    }


    // this will update the latest info of user in database
    public void callApiForEditProfile() {

        Functions.showLoader(context, false, false);

        String uname = usernameEdit.getText().toString().toLowerCase().replaceAll("\\s", "");
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("username", uname.replaceAll("@", ""));
            parameters.put("user_id", Functions.getSharedPreference(context).getString(Variables.U_ID, "0"));
            parameters.put("first_name", firstnameEdit.getText().toString());
            parameters.put("last_name", lastnameEdit.getText().toString());

            if (maleBtn.isChecked()) {
                parameters.put("gender", "Male");

            } else if (femaleBtn.isChecked()) {
                parameters.put("gender", "Female");
            }

            parameters.put("website", websiteEdit.getText().toString());
            parameters.put("bio", userBioEdit.getText().toString());

        } catch (Exception e) {
            e.printStackTrace();
        }

        VolleyRequest.JsonPostRequest(EditProfileA.this, ApiLinks.editProfile, parameters,Functions.getHeaders(this), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.checkStatus(EditProfileA.this,resp);
                Functions.cancelLoader();
                try {
                    JSONObject response = new JSONObject(resp);
                    String code = response.optString("code");
                    JSONArray msg = response.optJSONArray("msg");
                    if (code.equals("200")) {

                        SharedPreferences.Editor editor = Functions.getSharedPreference(context).edit();

                        String u_name = usernameEdit.getText().toString();
                        if (!u_name.contains("@"))
                            u_name = "@" + u_name;

                        editor.putString(Variables.U_NAME, u_name);
                        editor.putString(Variables.F_NAME, firstnameEdit.getText().toString());
                        editor.putString(Variables.L_NAME, lastnameEdit.getText().toString());
                        editor.putString(Variables.U_BIO, userBioEdit.getText().toString());
                        editor.putString(Variables.U_LINK, websiteEdit.getText().toString());
                        editor.commit();
                        moveBack();

                    } else {
                        if (msg != null) {
                            JSONObject jsonObject = msg.optJSONObject(0);
                            Functions.showToast(context, jsonObject.optString("response"));
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void moveBack() {
        Intent intent = new Intent();
        intent.putExtra("isShow", true);
        setResult(RESULT_OK, intent);
        finish();
    }


    // this will get the user data and parse the data and show the data into views
    public void callApiForUserDetails() {
        Functions.showLoader(context, false, false);
        Functions.callApiForGetUserData(EditProfileA.this,
                Functions.getSharedPreference(context).getString(Variables.U_ID, ""),
                new APICallBack() {
                    @Override
                    public void arrayData(ArrayList arrayList) {

                    }

                    @Override
                    public void onSuccess(String responce) {
                        Functions.cancelLoader();
                        parseUserData(responce);
                    }

                    @Override
                    public void onFail(String responce) {

                    }
                });
    }

    public void parseUserData(String responce) {
        try {
            JSONObject jsonObject = new JSONObject(responce);

            String code = jsonObject.optString("code");

            if (code.equals("200")) {
                JSONObject msg = jsonObject.optJSONObject("msg");

                UserModel userDetailModel=DataParsing.getUserDataModel(msg.optJSONObject("User"));

                firstnameEdit.setText(userDetailModel.getFirstName());
                lastnameEdit.setText(userDetailModel.getLastName());

                String picture = userDetailModel.getProfilePic();

                profileImage.setController(Functions.frescoImageLoad(picture,profileImage,false));


                String gender = userDetailModel.getGender();
                if (gender != null && gender.equalsIgnoreCase("male")) {
                    maleBtn.setChecked(true);
                } else if (gender != null && gender.equalsIgnoreCase("female")) {
                    femaleBtn.setChecked(true);
                }


                websiteEdit.setText(userDetailModel.getWebsite());
                userBioEdit.setText(userDetailModel.getBio());

                showTextLimit();

            } else {
                Functions.showToast(context, jsonObject.optString("msg"));

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // show the txt char limit of username and userbio
    public void showTextLimit() {
        usernameCountTxt.setText(usernameEdit.getText().length() + "/" + Constants.USERNAME_CHAR_LIMIT);
        bioCountTxt.setText(userBioEdit.getText().length() + "/" + Constants.BIO_CHAR_LIMIT);

    }


    @Override
    protected void onDestroy() {
        mPermissionResult.unregister();

        Functions.hideSoftKeyboard(EditProfileA.this);
        super.onDestroy();
    }
}
