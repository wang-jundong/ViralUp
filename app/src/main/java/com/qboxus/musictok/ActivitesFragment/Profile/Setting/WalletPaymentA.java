package com.qboxus.musictok.ActivitesFragment.Profile.Setting;

import com.qboxus.musictok.SimpleClasses.AppCompatLocaleActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.qboxus.musictok.R;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.Variables;

public class WalletPaymentA extends AppCompatLocaleActivity implements View.OnClickListener{

    ImageView btnBack;
    TextView tvEmail,tvAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Functions.setLocale(Functions.getSharedPreference(WalletPaymentA.this).getString(Variables.APP_LANGUAGE_CODE,Variables.DEFAULT_LANGUAGE_CODE)
                , this, WalletPaymentA.class,false);
        setContentView(R.layout.activity_wallet_payment);
        METHOD_initViews();
    }


    private void METHOD_initViews() {
        tvAdd = findViewById(R.id.tvAdd);
        tvAdd.setOnClickListener(this);
        btnBack = findViewById(R.id.back_btn);
        btnBack.setOnClickListener(this);
        tvEmail = findViewById(R.id.tvEmail);
        tvEmail.setOnClickListener(this);
        SetupScreenData();

    }



    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tvAdd:
            {
                METHOD_openAddCard_F(false);
            }
            break;
            case R.id.back_btn:
            {
                WalletPaymentA.super.onBackPressed();
            }
            break;
            case R.id.tvEmail:
            {
                METHOD_openAddCard_F(true);
            }
            break;
            default:
                break;
        }
    }


    private void METHOD_openAddCard_F(boolean isEdit) {
        if (!(Functions.isValidEmail(tvEmail.getText().toString())))
        {
            isEdit=false;
        }

        Intent intent=new Intent(WalletPaymentA.this,AddPayoutMethodA.class);
        intent.putExtra("email",tvEmail.getText().toString());
        intent.putExtra("isEdit",isEdit);
        startActivity(intent);
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
    }

    private void SetupScreenData() {
        tvEmail.setText(Functions.getSharedPreference(WalletPaymentA.this).getString(Variables.U_PAYOUT_ID,""));
    }

    @Override
    protected void onResume() {
        super.onResume();
        SetupScreenData();
    }

}
