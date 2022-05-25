package com.qboxus.musictok.ActivitesFragment.Profile.Setting;

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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qboxus.musictok.R;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.Variables;

public class ManageProfileA extends AppCompatLocaleActivity implements View.OnClickListener{


    TextView tvPhoneNo,tvEmail;
    View hideAbleView;
    TextView tvAccountInformation;
    LinearLayout tabChangePhoneNo,tabChangeEmail,tabChangePassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Functions.setLocale(Functions.getSharedPreference(ManageProfileA.this).getString(Variables.APP_LANGUAGE_CODE,Variables.DEFAULT_LANGUAGE_CODE)
                , this, ManageProfileA.class,false);
        setContentView(R.layout.activity_manage_profile);

        InitControl();
    }

    private void InitControl() {
        hideAbleView=findViewById(R.id.hideAbleView);
        tvAccountInformation=findViewById(R.id.tvAccountInformation);
        findViewById(R.id.tabDeleteAccount).setOnClickListener(this);
        findViewById(R.id.back_btn).setOnClickListener(this);

        tvEmail=findViewById(R.id.tvEmail);
        tvPhoneNo=findViewById(R.id.tvPhoneNo);
        tabChangePhoneNo=findViewById(R.id.tabChangePhoneNo);
        tabChangePhoneNo.setOnClickListener(this);
        tabChangeEmail=findViewById(R.id.tabChangeEmail);
        tabChangeEmail.setOnClickListener(this);
        tabChangePassword=findViewById(R.id.tabChangePassword);
        tabChangePassword.setOnClickListener(this);


        setUpScreenData();

    }

    private void setUpScreenData() {
        if (TextUtils.isEmpty(Functions.getSharedPreference(ManageProfileA.this).getString(Variables.U_SOCIAL_ID,"")))
        {
            tvAccountInformation.setVisibility(View.VISIBLE);
            hideAbleView.setVisibility(View.VISIBLE);

           if (TextUtils.isEmpty(Functions.getSharedPreference(ManageProfileA.this).getString(Variables.U_PHONE_NO,"")))
            {
                tabChangeEmail.setVisibility(View.VISIBLE);
                tabChangePhoneNo.setVisibility(View.GONE);
                tabChangePassword.setVisibility(View.VISIBLE);
                tvEmail.setText(Functions.getSharedPreference(ManageProfileA.this).getString(Variables.U_EMAIL,""));
            }
            else
            {
                tabChangeEmail.setVisibility(View.GONE);
                tabChangePhoneNo.setVisibility(View.VISIBLE);
                tabChangePassword.setVisibility(View.GONE);
                tvPhoneNo.setText(Functions.getSharedPreference(ManageProfileA.this).getString(Variables.U_PHONE_NO,""));
            }

        }
        else
        {
            tabChangePhoneNo.setVisibility(View.GONE);
            tabChangeEmail.setVisibility(View.GONE);
            tabChangePassword.setVisibility(View.GONE);
            tvAccountInformation.setVisibility(View.GONE);
            hideAbleView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.tabDeleteAccount:
            {
                startActivity(new Intent(ManageProfileA.this, DeleteAccountA.class));
            }
            break;
            case R.id.tabChangePhoneNo:
            {
                Intent intent=new Intent(ManageProfileA.this, UpdateEmailPhoneA.class);
                intent.putExtra("type","phone");
                resultCallback.launch(intent);
            }
            break;
            case R.id.tabChangeEmail:
            {
                Intent intent=new Intent(ManageProfileA.this,UpdateEmailPhoneA.class);
                intent.putExtra("type","email");
                resultCallback.launch(intent);
            }
            break;
            case R.id.tabChangePassword:
            {
                startActivity(new Intent(ManageProfileA.this, ChangePasswordA.class));
            }
            break;
            case R.id.back_btn:
            {
                ManageProfileA.super.onBackPressed();
            }
            break;
        }
    }


    ActivityResultLauncher<Intent> resultCallback = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data.getBooleanExtra("isShow",false))
                        {
                            setUpScreenData();
                        }

                    }
                }
            });
}