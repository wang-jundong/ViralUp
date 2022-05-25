package com.qboxus.musictok.ActivitesFragment;

import androidx.appcompat.app.AlertDialog;
import com.qboxus.musictok.SimpleClasses.AppCompatLocaleActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.qboxus.musictok.ApiClasses.ApiLinks;
import com.volley.plus.VPackages.VolleyRequest;
import com.qboxus.musictok.Constants;
import com.volley.plus.interfaces.Callback;
import com.qboxus.musictok.R;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.Variables;

import org.json.JSONObject;

public class PrivacyVideoSettingA extends AppCompatLocaleActivity implements View.OnClickListener{

    TextView viewVideo;
    Switch allowCommentSwitch, allowDuetSwitch;
    String videoId, commentValue, duetValue, privacyValue, duetVideoId;
    RelativeLayout allowDuetLayout;
    Boolean callApi = false;
    String viewVideoType="Private";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Functions.setLocale(Functions.getSharedPreference(PrivacyVideoSettingA.this).getString(Variables.APP_LANGUAGE_CODE,Variables.DEFAULT_LANGUAGE_CODE)
                , this, PrivacyVideoSettingA.class,false);
        setContentView(R.layout.activity_privacy_video_setting);
        viewVideo =findViewById(R.id.view_video);
        allowDuetLayout =findViewById(R.id.allow_duet_layout);

        allowCommentSwitch =findViewById(R.id.allow_comment_switch);
        allowCommentSwitch.setOnClickListener(this);

        allowDuetSwitch =findViewById(R.id.allow_duet_switch);
        allowDuetSwitch.setOnClickListener(this);

       findViewById(R.id.view_video_layout).setOnClickListener(this);
       findViewById(R.id.back_btn).setOnClickListener(this);

        videoId = getIntent().getStringExtra("video_id");
        privacyValue = getIntent().getStringExtra("privacy_value");
        duetValue = getIntent().getStringExtra("duet_value");
        commentValue = getIntent().getStringExtra("comment_value");
        duetVideoId = getIntent().getStringExtra("duet_video_id");

        viewVideo.setText(privacyValue);
        viewVideoType=privacyValue;

        allowCommentSwitch.setChecked(commentValue(commentValue));

        allowDuetSwitch.setChecked(getTrueFalseCondition(duetValue));

        if (Functions.getSharedPreference(PrivacyVideoSettingA.this).getBoolean(Variables.IsExtended,false) && (duetVideoId != null && duetVideoId.equalsIgnoreCase("0"))) {
            allowDuetLayout.setVisibility(View.VISIBLE);
        }
    }


    private boolean getTrueFalseCondition(String str) {
        if (str.equalsIgnoreCase("1"))
            return true;
        else
            return false;
    }


    private boolean commentValue(String str) {
        if (str.equalsIgnoreCase("true"))
            return true;
        else
            return false;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.view_video_layout:
                openDialogForPrivacy(PrivacyVideoSettingA.this);
                break;

            case R.id.back_btn:
                onBackPressed();
                break;

            case R.id.allow_duet_switch:
                if (allowDuetSwitch.isChecked()) {
                    duetValue = "1";
                } else {
                    duetValue = "0";
                }
                callApi();
                break;

            case R.id.allow_comment_switch:
                if (allowCommentSwitch.isChecked()) {
                    commentValue = "true";
                } else {
                    commentValue = "false";
                }
                callApi();
                break;

            default:
                break;


        }

    }


    // call api for change the privacy setting of profile
    public void callApi() {

        JSONObject params = new JSONObject();
        try {
            params.put("video_id", videoId);
            params.put("allow_comments", commentValue);
            params.put("allow_duet", duetValue);
            params.put("privacy_type", viewVideoType);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Functions.printLog(Constants.tag, "params at video_setting: " + params);

        VolleyRequest.JsonPostRequest(PrivacyVideoSettingA.this, ApiLinks.updateVideoDetail, params,Functions.getHeaders(this), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.checkStatus(PrivacyVideoSettingA.this,resp);

                parseDate(resp);


            }
        });

    }


    public void parseDate(String responce) {

        try {
            JSONObject jsonObject = new JSONObject(responce);
            String code = jsonObject.optString("code");

            if (code.equals("200")) {
                Functions.showToast(PrivacyVideoSettingA.this,  getString(R.string.setting_updated_successfully));

                callApi = true;
            } else {
                Functions.showToast(PrivacyVideoSettingA.this, jsonObject.optString("msg"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    // open the dialog for privacy public or private options
    private void openDialogForPrivacy(Context context) {
        final CharSequence[] options = {getString(R.string.public_), getString(R.string.private_)};

        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogCustom);

        builder.setTitle(null);

        builder.setItems(options, new DialogInterface.OnClickListener() {

            @Override

            public void onClick(DialogInterface dialog, int item) {
                viewVideo.setText(options[item]);
                if (item==0)
                {
                    viewVideoType="Public";
                }
                else
                {
                    viewVideoType="Private";
                }
                callApi();
                dialog.dismiss();

            }

        });

        builder.show();

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("isShow", callApi);
        intent.putExtra("video_id", videoId);
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }
}