package com.qboxus.musictok.ActivitesFragment.Accounts;

import com.qboxus.musictok.SimpleClasses.AppCompatLocaleActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.chaos.view.PinView;
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

public class ForgotPassA extends AppCompatLocaleActivity implements View.OnClickListener {

    ViewFlipper viewFlipper;
    ImageView goBack, goBack2, goBack3;
    EditText recoverEmail, edNewPass;
    Button btnNextEmail, confirmOtp, changePass;
    String otp, userId, userEmail;
    RelativeLayout rl1;
    TextView tv1, editNum, resendCode;
    PinView etCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Functions.setLocale(Functions.getSharedPreference(ForgotPassA.this).getString(Variables.APP_LANGUAGE_CODE,Variables.DEFAULT_LANGUAGE_CODE)
                , this, ForgotPassA.class,false);
        setContentView(R.layout.activity_forgot_pass);
        viewFlipper = findViewById(R.id.viewflliper);
        goBack = findViewById(R.id.goBack1);
        goBack2 = findViewById(R.id.goBack2);
        goBack3 = findViewById(R.id.goBack3);
        confirmOtp = findViewById(R.id.confirm_otp);
        edNewPass = findViewById(R.id.ed_new_pass);
        rl1 = findViewById(R.id.rl1_id);
        tv1 = findViewById(R.id.tv1_id);
        recoverEmail = findViewById(R.id.recover_email);
        btnNextEmail = findViewById(R.id.btn_next);
        changePass = findViewById(R.id.change_password_btn);
        changePass.setOnClickListener(this);
        confirmOtp.setOnClickListener(this);
        btnNextEmail.setOnClickListener(this);
        goBack.setOnClickListener(this);
        goBack2.setOnClickListener(this);
        goBack3.setOnClickListener(this);
        editNum = findViewById(R.id.edit_email);


        resendCode = findViewById(R.id.resend_code);
        resendCode.setOnClickListener(this);

        etCode = findViewById(R.id.et_code);
        recoverEmail.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int count) {
                // check the email validations
                String txtName = recoverEmail.getText().toString();
                if (txtName.length() > 0) {
                    btnNextEmail.setEnabled(true);
                    btnNextEmail.setClickable(true);
                } else {
                    btnNextEmail.setEnabled(false);
                    btnNextEmail.setClickable(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        etCode.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int count) {
                // check the code validation
                String txtName = etCode.getText().toString();
                if (txtName.length() == 4) {
                    confirmOtp.setEnabled(true);
                    confirmOtp.setClickable(true);
                } else {
                    confirmOtp.setEnabled(false);
                    confirmOtp.setClickable(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        edNewPass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int count) {
                // check the password validation
                String txtName = edNewPass.getText().toString();
                if (txtName.length() > 0) {
                    changePass.setEnabled(true);
                    changePass.setClickable(true);
                } else {
                    changePass.setEnabled(false);
                    changePass.setClickable(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }


    // this will start the count down of one minute
    public void oneMinuteTimer() {
        rl1.setVisibility(View.VISIBLE);
        etCode.setText("");
        new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long l) {
                tv1.setText(getString(R.string.resend_code)+" 00:" + l / 1000);
            }

            @Override
            public void onFinish() {
                rl1.setVisibility(View.GONE);
                resendCode.setVisibility(View.VISIBLE);
            }

        }.start();

    }

    @Override
    public void onClick(View v) {
        if (v == goBack) {
            onBackPressed();
        } else if (v == goBack2) {
            viewFlipper.showPrevious();
        } else if (v == goBack3) {
            viewFlipper.showPrevious();
        } else if (v == btnNextEmail) {
            if (validateEmail()) {
                Functions.showLoader(this, false, false);
                checkEmail(recoverEmail.getText().toString());
                userEmail = recoverEmail.getText().toString();
            }
        } else if (v == confirmOtp) {
            otp = etCode.getText().toString();
            if (otp != null && otp.length() == 4) {
                Functions.showLoader(this, false, false);
                checkOtp(otp, userEmail);
            } else {
                Functions.showToast(this, getString(R.string.code_is_invalid));
            }
        } else if (v == changePass) {
            if (validateNewPassword()) {
                Functions.showLoader(this, false, false);
                forgotChangePassword(edNewPass.getText().toString(), userEmail);
            }
        } else if (v == resendCode) {
            checkOtp(otp, userEmail);
            oneMinuteTimer();
        }
    }

    // this method will call the api of change password
    private void forgotChangePassword(String newpass, String email) {
        JSONObject params = new JSONObject();
        try {
            params.put("email", email);
            params.put("password", newpass);
        } catch (Exception e) {
            Functions.cancelLoader();
            e.printStackTrace();
        }


        VolleyRequest.JsonPostRequest(this, ApiLinks.changePasswordForgot, params,Functions.getHeadersWithOutLogin(this), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.cancelLoader();
                try {
                    JSONObject response = new JSONObject(resp);
                    String code = response.optString("code");
                    if (code.equals("200")) {
                        Functions.cancelLoader();
                        startActivity(new Intent(ForgotPassA.this, LoginA.class));
                        finish();
                    } else {
                        String msg_txt = response.getString("msg");
                        Functions.showToast(ForgotPassA.this, msg_txt);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }


    // check the password verification code
    private void checkOtp(String otp, String email) {
        JSONObject params = new JSONObject();
        try {
            params.put("email", email);
            params.put("code", otp);
        } catch (Exception e) {
            Functions.cancelLoader();
            e.printStackTrace();
        }

        VolleyRequest.JsonPostRequest(this, ApiLinks.verifyforgotPasswordCode, params,Functions.getHeadersWithOutLogin(this), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.cancelLoader();
                try {
                    JSONObject response = new JSONObject(resp);
                    String code = response.optString("code");

                    if (code.equals("200")) {
                        JSONObject json = new JSONObject(response.toString());
                        JSONObject msgObj = json.getJSONObject("msg");
                        JSONObject jsonObj = new JSONObject(msgObj.toString());
                        UserModel userDetailModel= DataParsing.getUserDataModel(jsonObj.optJSONObject("User"));
                        userId = userDetailModel.getId();
                        viewFlipper.showNext();
                        Functions.cancelLoader();
                    } else {
                        String msg_txt = response.getString("msg");
                        Toast.makeText(ForgotPassA.this, "" + msg_txt, Toast.LENGTH_SHORT).show();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void checkEmail(final String email) {
        JSONObject params = new JSONObject();
        try {
            params.put("email", email);
        } catch (Exception e) {
            Functions.cancelLoader();
            e.printStackTrace();
        }

        VolleyRequest.JsonPostRequest(this, ApiLinks.forgotPassword, params,Functions.getHeadersWithOutLogin(this), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.checkStatus(ForgotPassA.this,resp);
                Functions.cancelLoader();

                try {
                    JSONObject response = new JSONObject(resp);
                    String code = response.optString("code");

                    if (code.equals("200")) {
                        Functions.cancelLoader();
                        viewFlipper.showNext();
                        editNum.setText(getString(R.string.your_code_was_emailed_to)+" " + recoverEmail.getText().toString());
                        oneMinuteTimer();
                    } else {
                        String msg_txt = response.getString("msg");
                        Toast.makeText(ForgotPassA.this, "" + msg_txt, Toast.LENGTH_SHORT).show();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }


    // check the email validations
    public boolean validateEmail() {
        String email = recoverEmail.getText().toString().trim();
        if (email.isEmpty()) {
            Functions.showToast(this, getString(R.string.please_enter_valid_email));
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Functions.showToast(this, getString(R.string.please_enter_valid_email));
            return false;
        } else {

            return true;
        }
    }

    @Override
    public void onBackPressed() {
        if (viewFlipper.getDisplayedChild() == 0) {
            finish();
        } else if (viewFlipper.getDisplayedChild() == 1) {
            viewFlipper.showPrevious();
        } else
            viewFlipper.showPrevious();
    }


    // check the new password validations
    public boolean validateNewPassword() {
        String newpass = edNewPass.getText().toString();
        if (newpass.isEmpty()) {
            Functions.showToast(this, getString(R.string.please_enter_valid_password));
            return false;
        }
        if (newpass.length() <= 5 || newpass.length() >= 12) {
            Functions.showToast(this, getString(R.string.valid_password_length));
            return false;
        } else {

            return true;
        }
    }
}