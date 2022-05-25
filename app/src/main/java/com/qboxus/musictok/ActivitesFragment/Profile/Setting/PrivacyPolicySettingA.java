package com.qboxus.musictok.ActivitesFragment.Profile.Setting;

import androidx.appcompat.app.AlertDialog;
import com.qboxus.musictok.SimpleClasses.AppCompatLocaleActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.qboxus.musictok.ApiClasses.ApiLinks;
import com.volley.plus.VPackages.VolleyRequest;
import com.volley.plus.interfaces.Callback;
import com.qboxus.musictok.Models.PrivacyPolicySettingModel;
import com.qboxus.musictok.R;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.Variables;

import org.json.JSONObject;

import io.paperdb.Paper;

public class PrivacyPolicySettingA extends AppCompatLocaleActivity implements View.OnClickListener {


    TextView allowDownloadTxt, allowCommenetTxt, allowDirectMesgTxt, allowDuetTxt,
            allowViewLikevidTxt;

    String cancel = "";

    PrivacyPolicySettingModel privacyPolicySettingModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Functions.setLocale(Functions.getSharedPreference(PrivacyPolicySettingA.this).getString(Variables.APP_LANGUAGE_CODE,Variables.DEFAULT_LANGUAGE_CODE)
                , this, PrivacyPolicySettingA.class,false);
        setContentView(R.layout.activity_privacy_policy_setting);
        initControl();
    }


    // initialize the all views
    private void initControl() {
        cancel=getString(R.string.cancel_);
        privacyPolicySettingModel = (PrivacyPolicySettingModel) Paper.book(Variables.PrivacySetting).read(Variables.PrivacySettingModel);
        allowDownloadTxt = findViewById(R.id.allow_download_txt);
        allowCommenetTxt = findViewById(R.id.allow_commenet_txt);
        allowDirectMesgTxt = findViewById(R.id.allow_direct_mesg_txt);
        allowDuetTxt = findViewById(R.id.allow_duet_txt);
        allowViewLikevidTxt = findViewById(R.id.allow_view_likevid_txt);

        setUpScreenData();

       findViewById(R.id.PrivacyPolicySetting_F_img_back).setOnClickListener(this);
       findViewById(R.id.allow_download_layout).setOnClickListener(this);
       findViewById(R.id.allow_commenet_layout).setOnClickListener(this);
       findViewById(R.id.allow_dmesges_layout).setOnClickListener(this);
       findViewById(R.id.allow_duet_layout).setOnClickListener(this);
       findViewById(R.id.allow_view_likevid_layout).setOnClickListener(this);
    }

    private void setUpScreenData() {
        try {
            if (privacyPolicySettingModel.getVideos_download().equals("1")) {
                allowDownloadTxt.setText(Functions.stringParseFromServerRestriction(getString(R.string.on)));
            } else {
                allowDownloadTxt.setText(Functions.stringParseFromServerRestriction(getString(R.string.off)));
            }
            allowCommenetTxt.setText(Functions.stringParseFromServerRestriction(privacyPolicySettingModel.getVideo_comment()));
            allowDirectMesgTxt.setText(Functions.stringParseFromServerRestriction(privacyPolicySettingModel.getDirect_message()));
            allowDuetTxt.setText(Functions.stringParseFromServerRestriction(privacyPolicySettingModel.getDuet()));
            allowViewLikevidTxt.setText(Functions.stringParseFromServerRestriction(privacyPolicySettingModel.getLiked_videos()));
        } catch (Exception e) {
            e.getStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.PrivacyPolicySetting_F_img_back:
               PrivacyPolicySettingA.super.onBackPressed();
                break;
            case R.id.allow_download_layout:
                final CharSequence[] options = {getString(R.string.on), getString(R.string.off), getString(R.string.cancel_)};
                selectimage(getString(R.string.select_download_option), options, (TextView) view.findViewById(R.id.allow_download_txt), 1);
                break;
            case R.id.allow_commenet_layout:
                final CharSequence[] Commentoptions = {getString(R.string.everyone), getString(R.string.friend), getString(R.string.no_one), getString(R.string.cancel_)};
                selectimage(getString(R.string.select_comment_option), Commentoptions, (TextView) view.findViewById(R.id.allow_commenet_txt), 2);
                break;
            case R.id.allow_dmesges_layout:
                final CharSequence[] messgeoptions = {getString(R.string.everyone), getString(R.string.friend), getString(R.string.no_one), getString(R.string.cancel_)};
                selectimage(getString(R.string.select_message_option), messgeoptions, allowDirectMesgTxt, 3);
                break;
            case R.id.allow_duet_layout:
                final CharSequence[] duetoption = {getString(R.string.everyone), getString(R.string.friend), getString(R.string.no_one), getString(R.string.cancel_)};
                selectimage(getString(R.string.select_duet_option), duetoption, allowDuetTxt, 4);
                break;
            case R.id.allow_view_likevid_layout:
                final CharSequence[] likevidoption = {getString(R.string.everyone), getString(R.string.only_me), getString(R.string.cancel_)};
                selectimage(getString(R.string.select_like_video_option), likevidoption, allowViewLikevidTxt, 5);
                break;

        }
    }

    private void selectimage(String title, final CharSequence[] options, final TextView textView, final int Selected_box) {
        AlertDialog.Builder builder = new AlertDialog.Builder(PrivacyPolicySettingA.this, R.style.AlertDialogCustom);
        builder.setTitle(title);
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                CharSequence op = options[item];
                if (!op.equals(cancel)) {
                    textView.setText(("" + options[item]).toUpperCase());

                    switch (Selected_box) {
                        case 1:
                            if (op.toString().equalsIgnoreCase(getString(R.string.on))) {
                                strVideoDownload = "1";
                            } else {
                                strVideoDownload = "0";
                            }
                            break;
                        case 2:
                            if (op.toString().equalsIgnoreCase(getString(R.string.everyone))) {
                                strVideoComment = Functions.stringParseIntoServerRestriction("Everyone");
                            } else if (op.toString().equalsIgnoreCase(getString(R.string.friend))) {
                                strVideoComment = Functions.stringParseIntoServerRestriction("Friend");
                            } else {
                                strVideoComment = Functions.stringParseIntoServerRestriction("No One");
                            }
                            break;
                        case 3:
                            if (op.toString().equalsIgnoreCase(getString(R.string.everyone))) {
                                strDirectMessage = Functions.stringParseIntoServerRestriction("Everyone");
                            } else if (op.toString().equalsIgnoreCase(getString(R.string.friend))) {
                                strDirectMessage = Functions.stringParseIntoServerRestriction("Friend");
                            } else {
                                strDirectMessage = Functions.stringParseIntoServerRestriction("No One");
                            }
                            break;
                        case 4:
                            if (op.toString().equalsIgnoreCase(getString(R.string.everyone))) {
                                strDuet = Functions.stringParseIntoServerRestriction("Everyone");
                            } else if (op.toString().equalsIgnoreCase(getString(R.string.friend))) {
                                strDuet = Functions.stringParseIntoServerRestriction("Friend");
                            } else {
                                strDuet = Functions.stringParseIntoServerRestriction("No One");
                            }
                            break;
                        case 5:
                            if (op.toString().equalsIgnoreCase(getString(R.string.everyone))) {
                                strLikedVideo = Functions.stringParseIntoServerRestriction("Everyone");
                            } else {
                                strLikedVideo = Functions.stringParseIntoServerRestriction("Only Me");
                            }

                            break;

                    }
                    callApi();
                }
            }
        });
        builder.show();
    }


    String strVideoDownload, strDirectMessage, strDuet, strLikedVideo, strVideoComment;

    public void callApi() {

        JSONObject params = new JSONObject();
        try {
            params.put("videos_download", strVideoDownload);
            params.put("direct_message", strDirectMessage);
            params.put("duet", strDuet);
            params.put("liked_videos", strLikedVideo);
            params.put("video_comment", strVideoComment);
            params.put("user_id", Functions.getSharedPreference(PrivacyPolicySettingA.this).getString(Variables.U_ID, ""));

        } catch (Exception e) {
            e.printStackTrace();
        }

        VolleyRequest.JsonPostRequest(PrivacyPolicySettingA.this, ApiLinks.addPolicySetting, params,Functions.getHeaders(this), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.checkStatus(PrivacyPolicySettingA.this,resp);

                parseData(resp);
            }
        });

    }

    // parse the privacy data for change the ui
    public void parseData(String responce) {

        try {
            JSONObject jsonObject = new JSONObject(responce);
            String code = jsonObject.optString("code");
            if (code.equals("200")) {
                JSONObject msg = jsonObject.optJSONObject("msg");
                JSONObject privacy_policy_setting = msg.optJSONObject("PrivacySetting");
                privacyPolicySettingModel = new PrivacyPolicySettingModel();
                privacyPolicySettingModel.setVideos_download("" + privacy_policy_setting.optString("videos_download"));
                privacyPolicySettingModel.setDirect_message("" + privacy_policy_setting.optString("direct_message"));
                privacyPolicySettingModel.setDuet("" + privacy_policy_setting.optString("duet"));
                privacyPolicySettingModel.setLiked_videos("" + privacy_policy_setting.optString("liked_videos"));
                privacyPolicySettingModel.setVideo_comment("" + privacy_policy_setting.optString("video_comment"));
                Paper.book(Variables.PrivacySetting).write(Variables.PrivacySettingModel, privacyPolicySettingModel);

                Toast.makeText(this, getString(R.string.privacy_setting_updated), Toast.LENGTH_SHORT).show();
            } else {
                Functions.showToast(PrivacyPolicySettingA.this, jsonObject.optString("msg"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}