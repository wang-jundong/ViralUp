package com.qboxus.musictok.ActivitesFragment.LiveStreaming.activities;

import android.Manifest;
import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.qboxus.musictok.ActivitesFragment.LiveStreaming.CallBack;
import com.qboxus.musictok.ActivitesFragment.LiveStreaming.Constants;
import com.qboxus.musictok.R;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.Variables;


public class StreamingMain_A extends BaseActivity {

    // Permission request code of any integer value
    private static final int PERMISSION_REQ_CODE = 1 << 4;

    private String[] PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA
    };
    private EditText mTopicEdit;
    private TextView mStartBtn;


    String userId, userName, userPicture;
    int user_role;


    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            // Do nothing
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            // Do nothing
        }

        @Override
        public void afterTextChanged(Editable editable) {
            mStartBtn.setEnabled(!TextUtils.isEmpty(editable));
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Functions.setLocale(Functions.getSharedPreference(StreamingMain_A.this).getString(Variables.APP_LANGUAGE_CODE,Variables.DEFAULT_LANGUAGE_CODE)
                , this, StreamingMain_A.class,false);
        setContentView(R.layout.activity_main_streaming);

        Intent bundle = getIntent();
        if (bundle != null) {
            userId = bundle.getStringExtra("user_id");
            userName = bundle.getStringExtra("user_name");
            userPicture = bundle.getStringExtra("user_picture");
            user_role = bundle.getIntExtra("user_role", io.agora.rtc.Constants.CLIENT_ROLE_BROADCASTER);

        }
        initUI();
    }

    private void initUI() {
        mTopicEdit = findViewById(R.id.topic_edit);
        mTopicEdit.addTextChangedListener(mTextWatcher);


        mStartBtn = findViewById(R.id.start_broadcast_button);
        if (TextUtils.isEmpty(mTopicEdit.getText())) mStartBtn.setEnabled(false);

        mTopicEdit.setText(userName);
    }



    public void onSettingClicked(View view) {
        Intent i = new Intent(this, SettingsActivity.class);
        startActivity(i);
    }

    public void onStartBroadcastClicked(View view) {
        checkPermission();
    }

    private void checkPermission() {
        boolean granted = true;
        for (String per : PERMISSIONS) {
            if (!permissionGranted(per)) {
                granted = false;
                break;
            }
        }

        if (granted) {
            resetLayoutAndForward();
        } else {
            requestPermissions();
        }
    }

    // check the camera and mic permission before start the live streaming
    private boolean permissionGranted(String permission) {
        return ContextCompat.checkSelfPermission(
                this, permission) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_REQ_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQ_CODE) {
            boolean granted = true;
            for (int result : grantResults) {
                granted = (result == PackageManager.PERMISSION_GRANTED);
                if (!granted) break;
            }

            if (granted) {
                resetLayoutAndForward();
            } else {
                toastNeedPermissions();
            }
        }
    }

    private void resetLayoutAndForward() {
        closeImeDialogIfNeeded();
        gotoRoleActivity();
    }

    private void closeImeDialogIfNeeded() {
        InputMethodManager manager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.hideSoftInputFromWindow(mTopicEdit.getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }

    // open  the live streaming of othe user or open userself
    public void gotoRoleActivity() {

        final Intent intent = new Intent();
        intent.putExtra("user_id", userId);
        intent.putExtra("user_name", userName);
        intent.putExtra("user_picture", userPicture);
        intent.putExtra("user_role", user_role);
        intent.putExtra(Constants.KEY_CLIENT_ROLE, user_role);
        intent.setClass(StreamingMain_A.this, LiveActivity.class);

        if (user_role == io.agora.rtc.Constants.CLIENT_ROLE_BROADCASTER && !Functions.getSharedPreference(this).getBoolean(Variables.is_puchase, false)) {

            if (com.qboxus.musictok.Constants.STREAMING_LIMIT) {
                    Functions.showAlert(this, getString(R.string.alert), getString(R.string.for_demo_purpose_we_only_allow_to)+" " + com.qboxus.musictok.Constants.MAX_STREMING_TIME / 1000 + "s "+getString(R.string.live_streaming), new CallBack() {
                        @Override
                        public void getResponse(String requestType, String response) {

                            config().setChannelName(userId);
                            startActivity(intent);
                            finish();
                        }
                    });

            } else {
                config().setChannelName(userId);
                startActivity(intent);
                finish();
            }

        } else {

            config().setChannelName(userId);
            startActivity(intent);
            finish();
        }

    }

    private void toastNeedPermissions() {
        Toast.makeText(this, R.string.need_necessary_permissions, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        resetUI();
    }

    private void resetUI() {
        closeImeDialogIfNeeded();
    }




}
