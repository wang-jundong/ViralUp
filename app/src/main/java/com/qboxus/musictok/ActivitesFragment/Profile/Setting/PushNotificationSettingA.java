package com.qboxus.musictok.ActivitesFragment.Profile.Setting;

import com.qboxus.musictok.SimpleClasses.AppCompatLocaleActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.qboxus.musictok.ApiClasses.ApiLinks;
import com.volley.plus.VPackages.VolleyRequest;
import com.volley.plus.interfaces.Callback;
import com.qboxus.musictok.Models.PushNotificationSettingModel;
import com.qboxus.musictok.R;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.Variables;

import org.json.JSONObject;

import io.paperdb.Paper;

public class PushNotificationSettingA extends AppCompatLocaleActivity implements View.OnClickListener{

    ImageView imgBack;
    Switch stLikes, stComment, stNewFollow, stMention, stDirectMessage, stVideoUpdate;
    String strLikes = "1", strComment = "1", strNewFollow = "1", strMention = "1", strDirectMessage = "1", str_video_update = "1";
    PushNotificationSettingModel pushNotificationSettingModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Functions.setLocale(Functions.getSharedPreference(PushNotificationSettingA.this).getString(Variables.APP_LANGUAGE_CODE,Variables.DEFAULT_LANGUAGE_CODE)
                , this, PushNotificationSettingA.class,false);
        setContentView(R.layout.activity_push_notification_setting);
        initControl();

    }


    private void initControl() {
        pushNotificationSettingModel = (PushNotificationSettingModel) Paper.book(Variables.PrivacySetting).read(Variables.PushSettingModel);
        imgBack =findViewById(R.id.back_btn);
        imgBack.setOnClickListener(this);
        stLikes =findViewById(R.id.Push_Notification_Setting_F_st_likes);
        stLikes.setOnClickListener(this);
        stComment =findViewById(R.id.Push_Notification_Setting_F_st_comments);
        stComment.setOnClickListener(this);
        stNewFollow =findViewById(R.id.Push_Notification_Setting_F_st_new_follower);
        stNewFollow.setOnClickListener(this);
        stMention =findViewById(R.id.Push_Notification_Setting_F_st_mention);
        stMention.setOnClickListener(this);
        stDirectMessage =findViewById(R.id.Push_Notification_Setting_F_st_direct_message);
        stDirectMessage.setOnClickListener(this);
        stVideoUpdate =findViewById(R.id.Push_Notification_Setting_F_st_video_update);
        stVideoUpdate.setOnClickListener(this);

        setUpScreenData();

    }


    private void setUpScreenData() {
        try {
            strLikes = pushNotificationSettingModel.getLikes();
            stLikes.setChecked(getTrueFalseCondition(strLikes));

            str_video_update = pushNotificationSettingModel.getVideoupdates();
            stVideoUpdate.setChecked(getTrueFalseCondition(str_video_update));

            strDirectMessage = pushNotificationSettingModel.getDirectmessage();
            stDirectMessage.setChecked(getTrueFalseCondition(strDirectMessage));

            strMention = pushNotificationSettingModel.getMentions();
            stMention.setChecked(getTrueFalseCondition(strMention));

            strNewFollow = pushNotificationSettingModel.getNewfollowers();
            stNewFollow.setChecked(getTrueFalseCondition(strNewFollow));

            strComment = pushNotificationSettingModel.getComments();
            stComment.setChecked(getTrueFalseCondition(strComment));
        } catch (Exception e) {
            e.getStackTrace();
        }
    }


    private boolean getTrueFalseCondition(String str) {
        if (str.equalsIgnoreCase("1"))
            return true;
        else
            return false;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.back_btn:
                PushNotificationSettingA.super.onBackPressed();
                break;
            case R.id.Push_Notification_Setting_F_st_likes:
                if (stLikes.isChecked()) {
                    strLikes = "1";
                } else {
                    strLikes = "0";
                }
                callApi();
                break;
            case R.id.Push_Notification_Setting_F_st_comments:
                if (stComment.isChecked()) {
                    strComment = "1";
                } else {
                    strComment = "0";
                }
                callApi();
                break;
            case R.id.Push_Notification_Setting_F_st_new_follower:
                if (stNewFollow.isChecked()) {
                    strNewFollow = "1";
                } else {
                    strNewFollow = "0";
                }
                callApi();
                break;
            case R.id.Push_Notification_Setting_F_st_mention:
                if (stMention.isChecked()) {
                    strMention = "1";
                } else {
                    strMention = "0";
                }
                callApi();
                break;
            case R.id.Push_Notification_Setting_F_st_direct_message:
                if (stDirectMessage.isChecked()) {
                    strDirectMessage = "1";
                } else {
                    strDirectMessage = "0";
                }
                callApi();
                break;
            case R.id.Push_Notification_Setting_F_st_video_update:
                if (stVideoUpdate.isChecked()) {
                    str_video_update = "1";
                } else {
                    str_video_update = "0";
                }
                callApi();
                break;
        }
    }


    // call the api for update the pushnotification settings

    public void callApi() {

        JSONObject params = new JSONObject();
        try {
            params.put("likes", strLikes);
            params.put("comments", strComment);
            params.put("new_followers", strNewFollow);
            params.put("mentions", strMention);
            params.put("video_updates", str_video_update);
            params.put("direct_messages", strDirectMessage);
            params.put("user_id", Functions.getSharedPreference(PushNotificationSettingA.this).getString(Variables.U_ID, ""));

        } catch (Exception e) {
            e.printStackTrace();
        }

        VolleyRequest.JsonPostRequest(PushNotificationSettingA.this, ApiLinks.updatePushNotificationSetting, params,Functions.getHeaders(this), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.checkStatus(PushNotificationSettingA.this,resp);

                parsedata(resp);


            }
        });

    }


    // set the already selected pushnotification ui
    public void parsedata(String responce) {

        try {
            JSONObject jsonObject = new JSONObject(responce);
            String code = jsonObject.optString("code");

            if (code.equals("200")) {

                JSONObject msg = jsonObject.optJSONObject("msg");
                JSONObject push_notification_setting = msg.optJSONObject("PushNotification");
                pushNotificationSettingModel = new PushNotificationSettingModel();
                pushNotificationSettingModel.setComments("" + push_notification_setting.optString("comments"));
                pushNotificationSettingModel.setLikes("" + push_notification_setting.optString("likes"));
                pushNotificationSettingModel.setNewfollowers("" + push_notification_setting.optString("new_followers"));
                pushNotificationSettingModel.setMentions("" + push_notification_setting.optString("mentions"));
                pushNotificationSettingModel.setDirectmessage("" + push_notification_setting.optString("direct_messages"));
                pushNotificationSettingModel.setVideoupdates("" + push_notification_setting.optString("video_updates"));
                Paper.book(Variables.PrivacySetting).write(Variables.PushSettingModel, pushNotificationSettingModel);

                Toast.makeText(this, getString(R.string.push_notification_setting_updated), Toast.LENGTH_SHORT).show();
            } else {
                Functions.showToast(PushNotificationSettingA.this, jsonObject.optString("msg"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}