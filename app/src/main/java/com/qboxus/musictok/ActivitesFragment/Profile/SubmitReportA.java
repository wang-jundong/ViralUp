package com.qboxus.musictok.ActivitesFragment.Profile;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import com.qboxus.musictok.SimpleClasses.AppCompatLocaleActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.qboxus.musictok.ApiClasses.ApiLinks;
import com.volley.plus.VPackages.VolleyRequest;
import com.volley.plus.interfaces.Callback;
import com.qboxus.musictok.Interfaces.FragmentCallBack;
import com.qboxus.musictok.R;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.Variables;

import org.json.JSONException;
import org.json.JSONObject;

public class SubmitReportA extends AppCompatLocaleActivity implements View.OnClickListener {


    TextView reportTypeTxt;
    EditText reportDescriptionTxt;
    String reportId, txtReportType, videoId, userId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Functions.setLocale(Functions.getSharedPreference(SubmitReportA.this).getString(Variables.APP_LANGUAGE_CODE,Variables.DEFAULT_LANGUAGE_CODE)
                , this, SubmitReportA.class,false);
        setContentView(R.layout.activity_submit_report);

        reportId = getIntent().getStringExtra("report_id");
        txtReportType = getIntent().getStringExtra("report_type");
        videoId = getIntent().getStringExtra("video_id");
        userId = getIntent().getStringExtra("user_id");


        reportTypeTxt = findViewById(R.id.report_type);
        reportTypeTxt.setText(txtReportType);

        reportDescriptionTxt = findViewById(R.id.report_description_txt);


        findViewById(R.id.back_btn).setOnClickListener(this);
        findViewById(R.id.report_reason_layout).setOnClickListener(this);
        findViewById(R.id.submit_btn).setOnClickListener(this);


    }


    public boolean checkValidation() {

        if (TextUtils.isEmpty(reportTypeTxt.getText())) {
            Functions.showToast(SubmitReportA.this, getString(R.string.please_give_some_reason));
            return false;
        }

        return true;
    }


    // call the api for report against video
    public void callApiReportVideo() {

        JSONObject params = new JSONObject();
        try {

            params.put("user_id", Functions.getSharedPreference(SubmitReportA.this).getString(Variables.U_ID, ""));
            params.put("video_id", videoId);
            params.put("report_reason_id", reportId);
            params.put("description", reportDescriptionTxt.getText());

        } catch (Exception e) {
            e.printStackTrace();
        }


        Functions.showLoader(SubmitReportA.this, false, false);
        VolleyRequest.JsonPostRequest(SubmitReportA.this, ApiLinks.reportVideo, params,Functions.getHeaders(this), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.cancelLoader();

                try {
                    JSONObject jsonObject = new JSONObject(resp);
                    String code = jsonObject.optString("code");
                    if (code.equals("200")) {
                        Functions.showToast(SubmitReportA.this, getString(R.string.report_submitted_successfully));
                        moveBack();

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


    // call the api for report against apis
    public void callApiReportUser() {

        JSONObject params = new JSONObject();
        try {

            params.put("user_id", Functions.getSharedPreference(SubmitReportA.this).getString(Variables.U_ID, ""));
            params.put("report_user_id", userId);
            params.put("report_reason_id", reportId);
            params.put("description", reportDescriptionTxt.getText());

        } catch (Exception e) {
            e.printStackTrace();
        }


        Functions.showLoader(SubmitReportA.this, false, false);
        VolleyRequest.JsonPostRequest(SubmitReportA.this, ApiLinks.reportUser, params,Functions.getHeaders(this), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.cancelLoader();

                try {
                    JSONObject jsonObject = new JSONObject(resp);
                    String code = jsonObject.optString("code");
                    if (code.equals("200")) {
                        Functions.showToast(SubmitReportA.this, getString(R.string.report_submitted_successfully));
                        moveBack();

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.submit_btn:
                if (videoId != null && checkValidation())
                    callApiReportVideo();

                else if (userId != null && checkValidation())
                    callApiReportUser();

                break;

            case R.id.report_reason_layout:
                Intent intent=new Intent(SubmitReportA.this, ReportTypeA.class);
                intent.putExtra("video_id", videoId);
                intent.putExtra("user_id", userId);
                intent.putExtra("isFrom",true);
                resultCallback.launch(intent);
                overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top);
                break;

            case R.id.back_btn:
                SubmitReportA.super.onBackPressed();
                break;
        }
    }


    ActivityResultLauncher<Intent> resultCallback = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK ) {
                        Intent data = result.getData();
                        if (data.getBooleanExtra("isShow",false))
                        {
                            String report_reason = data.getStringExtra("reason");
                            reportTypeTxt.setText(report_reason);
                        }

                    }
                }
            });


}