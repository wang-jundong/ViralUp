package com.viral.musictok.ActivitesFragment.NewCoin;

import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.viral.musictok.R;
import com.viral.musictok.SimpleClasses.AppCompatLocaleActivity;
import com.viral.musictok.SimpleClasses.Functions;
import com.viral.musictok.SimpleClasses.Variables;

public class CoinBalanceA extends AppCompatLocaleActivity implements View.OnClickListener {

    private TextView claim_profile_btn, market_profile_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Functions.setLocale(Functions.getSharedPreference(CoinBalanceA.this).getString(Variables.APP_LANGUAGE_CODE, Variables.DEFAULT_LANGUAGE_CODE)
                , this, CoinBalanceA.class, false);
        setContentView(R.layout.activity_balance);

        init();

    }

    private void init() {
        findViewById(R.id.back_btn).setOnClickListener(v -> finish());
        claim_profile_btn = findViewById(R.id.claim_profile_btn);
        claim_profile_btn.setOnClickListener(this);

        market_profile_btn = findViewById(R.id.market_profile_btn);
        market_profile_btn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.claim_profile_btn:
                showClaimSuccess();
                break;

            case R.id.market_profile_btn:
                showClaimActivate();
                break;
        }
    }

    private void showClaimSuccess() {
        final Dialog dialog = new Dialog(CoinBalanceA.this);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_claim_success);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        final TextView tvDone;
        tvDone = dialog.findViewById(R.id.tvDone);
        tvDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void showClaimActivate() {
        final Dialog dialog = new Dialog(CoinBalanceA.this);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_claim_activate);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        final TextView tvDone;
        tvDone = dialog.findViewById(R.id.tvDone);
        tvDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

}
