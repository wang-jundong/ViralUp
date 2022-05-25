package com.qboxus.musictok.ActivitesFragment.WalletAndWithdraw;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import com.qboxus.musictok.SimpleClasses.AppCompatLocaleActivity;
import androidx.core.content.ContextCompat;

import com.qboxus.musictok.ActivitesFragment.LiveStreaming.CallBack;
import com.qboxus.musictok.ActivitesFragment.Profile.Setting.AddPayoutMethodA;
import com.qboxus.musictok.ApiClasses.ApiLinks;
import com.volley.plus.VPackages.VolleyRequest;
import com.volley.plus.interfaces.Callback;
import com.qboxus.musictok.R;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.Variables;

import org.json.JSONObject;


public class WithdrawCoinsA extends AppCompatLocaleActivity {


    TextView coins_txt,coins_txt2,amount_txt,checkout_btn;

    double total_coins=0f,total_amount=0f,unit_amount=0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Functions.setLocale(Functions.getSharedPreference(WithdrawCoinsA.this).getString(Variables.APP_LANGUAGE_CODE,Variables.DEFAULT_LANGUAGE_CODE)
                , this, WithdrawCoinsA.class,false);
        setContentView(R.layout.activity_withdraw_coins);

        coins_txt=findViewById(R.id.coins_txt);
        coins_txt2=findViewById(R.id.coins_txt2);

        amount_txt=findViewById(R.id.amount_txt);

        checkout_btn=findViewById(R.id.checkout_btn);

        findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveBack();
            }
        });

        checkout_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (total_amount >= 1)
                    Call_api_cash_out();
                else
                    Toast.makeText(WithdrawCoinsA.this, getString(R.string.you_dont_have_sufficent_coins), Toast.LENGTH_SHORT).show();

            }
        });

        String wallet =""+Functions.getSharedPreference(WithdrawCoinsA.this).getString(Variables.U_WALLET, "0");
        coins_txt.setText(wallet);
        coins_txt2.setText(wallet);
        total_coins=Double.parseDouble(wallet);

        Call_api_get_coins_value();



    }

    private void moveBack() {
        Intent intent = new Intent();
        intent.putExtra("isShow", true);
        setResult(RESULT_OK, intent);
        finish();
    }


    public void Call_api_get_coins_value(){

        JSONObject params=new JSONObject();
        VolleyRequest.JsonPostRequest(this, ApiLinks.showCoinWorth, params,Functions.getHeaders(this), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.checkStatus(WithdrawCoinsA.this,resp);

                try {
                    JSONObject jsonObject=new JSONObject(resp);
                    String code=jsonObject.optString("code");
                    if(code.equals("200")){


                        JSONObject msgObj=jsonObject.optJSONObject("msg");
                        JSONObject object=msgObj.optJSONObject("CoinWorth");

                        String amount=object.optString("price");


                        unit_amount=Double.parseDouble(amount);

                        total_amount=(total_coins*unit_amount);

                        if(total_amount>=1)
                            checkout_btn.setBackgroundTintList(ContextCompat.getColorStateList(WithdrawCoinsA.this, R.color.blue));

                        amount_txt.setText("$"+total_amount);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        });
    }



    public void Call_api_cash_out(){

        JSONObject params=new JSONObject();
        try {
            params.put("user_id",Variables.sharedPreferences.getString(Variables.U_ID,""));
//            params.put("coins",""+total_coins);
            params.put("amount",""+total_amount);
//            params.put("paypal_id",payment_edit.getText().toString());

        } catch (Exception e) {
            e.printStackTrace();
        }

        Functions.showLoader(this,false,false);
        VolleyRequest.JsonPostRequest(this, ApiLinks.withdrawRequest, params,Functions.getHeaders(this), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.checkStatus(WithdrawCoinsA.this,resp);

                Functions.cancelLoader();
                try {
                    JSONObject jsonObject=new JSONObject(resp);
                    String code=jsonObject.optString("code");
                    if(code.equals("200")){
                        Toast.makeText(WithdrawCoinsA.this, getString(R.string.check_out_successfully), Toast.LENGTH_SHORT).show();
                        moveBack();
                    }
                    else
                    if(code.equals("201") && (!jsonObject.optString("msg").equalsIgnoreCase("You have already requested a payout.")))
                    {
                        Functions.showAlert(WithdrawCoinsA.this, getString(R.string.alert), getString(R.string.for_payout_you_must_need_to_add_paypal_id), new CallBack() {
                            @Override
                            public void getResponse(String requestType, String response) {

                                Intent intent=new Intent(WithdrawCoinsA.this, AddPayoutMethodA.class);
                                intent.putExtra("email","");
                                intent.putExtra("isEdit",false);
                                addPaymentMethodResult.launch(intent);
                                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);

                            }
                        });
                    }

                }
                catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }


    ActivityResultLauncher<Intent> addPaymentMethodResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        if (result.getData().getBooleanExtra("isShow",false))
                        {
                            Call_api_cash_out();
                        }
                    }
                }
            });


    @Override
    public void onBackPressed() {
        moveBack();
        super.onBackPressed();
    }
}
