package com.qboxus.musictok.ActivitesFragment.Profile.Setting;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.qboxus.musictok.ApiClasses.ApiLinks;
import com.qboxus.musictok.SimpleClasses.AppCompatLocaleActivity;
import com.volley.plus.VPackages.VolleyRequest;
import com.volley.plus.interfaces.Callback;
import com.qboxus.musictok.Models.UserModel;
import com.qboxus.musictok.R;
import com.qboxus.musictok.SimpleClasses.DataParsing;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.Variables;

import org.json.JSONObject;

public class AddPayoutMethodA extends AppCompatLocaleActivity implements View.OnClickListener {

    EditText etEmail;
    Button btnAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Functions.setLocale(Functions.getSharedPreference(AddPayoutMethodA.this).getString(Variables.APP_LANGUAGE_CODE,Variables.DEFAULT_LANGUAGE_CODE), this, AddPayoutMethodA.class,false);
        setContentView(R.layout.activity_add_payout_method);
        InitControl();
        ActionContorl();
    }

    private void ActionContorl() {
        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int count) {
                // check the email validation during user typing
                String txtName = etEmail.getText().toString();
                if (txtName.length() > 0) {
                    if (Functions.isValidEmail(etEmail.getText().toString()))
                    {
                        btnAdd.setEnabled(true);
                        btnAdd.setClickable(true);
                    }
                    else
                    {
                        btnAdd.setEnabled(false);
                        btnAdd.setClickable(false);
                    }
                } else {
                    btnAdd.setEnabled(false);
                    btnAdd.setClickable(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void InitControl() {
        etEmail=findViewById(R.id.et_email);
        btnAdd=findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(this);

        findViewById(R.id.goBack1).setOnClickListener(this);
        SetupScreenData();
    }

    private void SetupScreenData() {
        if (getIntent().getBooleanExtra("isEdit",false))
        {
            etEmail.setText(getIntent().getStringExtra("email"));
            btnAdd.setText(getString(R.string.update_payout));
        }
        else
        {
            btnAdd.setText(getString(R.string.add_payout));
        }

    }


    private void CallApiAddPayment() {
        JSONObject sendobj = new JSONObject();
        try {
            sendobj.put("user_id", Functions.getSharedPreference(AddPayoutMethodA.this).getString(Variables.U_ID, "0"));
            sendobj.put("email", etEmail.getText().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        Functions.showLoader(AddPayoutMethodA.this,false,false);
        VolleyRequest.JsonPostRequest(AddPayoutMethodA.this, ApiLinks.addPayout, sendobj, Functions.getHeaders(this),new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.checkStatus(AddPayoutMethodA.this,resp);
                Functions.cancelLoader();
                if (resp!=null){

                    try {
                        JSONObject respobj = new JSONObject(resp);

                        if (respobj.optString("code").equals("200")){
                            JSONObject msgObj=respobj.getJSONObject("msg");
                            UserModel userDetailModel= DataParsing.getUserDataModel(msgObj.optJSONObject("User"));
                            SharedPreferences.Editor editor = Functions.getSharedPreference(AddPayoutMethodA.this).edit();
                            editor.putString(Variables.U_PAYOUT_ID, userDetailModel.getPaypal());
                            editor.commit();
                            Intent intent = new Intent();
                            intent.putExtra("isShow",true);
                            setResult(RESULT_OK, intent);
                            finish();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
        });
    }


    @Override
    public void onClick(View view) {
        switch (view.getId())
        {

            case R.id.goBack1:
                AddPayoutMethodA.super.onBackPressed();
                break;

            case R.id.btnAdd:
            {
                if (TextUtils.isEmpty(etEmail.getText().toString()))
                {
                    etEmail.setError(getString(R.string.email_cant_empty));
                    etEmail.setFocusable(true);
                    return;
                }
                if (!(Functions.isValidEmail(etEmail.getText().toString())))
                {
                    etEmail.setError(getString(R.string.enter_valid_email));
                    etEmail.setFocusable(true);
                    return;
                }
                CallApiAddPayment();


            }
            break;
        }
    }


}