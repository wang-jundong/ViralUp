package com.qboxus.musictok.ActivitesFragment.Profile.Setting;

import com.qboxus.musictok.SimpleClasses.AppCompatLocaleActivity;
import androidx.core.content.ContextCompat;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.qboxus.musictok.ApiClasses.ApiLinks;
import com.volley.plus.VPackages.VolleyRequest;
import com.volley.plus.interfaces.Callback;
import com.qboxus.musictok.R;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.Variables;

import org.json.JSONArray;
import org.json.JSONObject;

public class ChangePasswordA extends AppCompatLocaleActivity implements View.OnClickListener{

    RelativeLayout llOldPass, llNewPass, llConfirmPass;
    EditText etOldpass, etNewpass, etConfirmpass;
    private ImageView ivOldHide, ivNewHide, ivConfirmHide;
    private Boolean oldCheck = true, newCheck = true, confirmCheck = true;
    Button changePass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Functions.setLocale(Functions.getSharedPreference(ChangePasswordA.this).getString(Variables.APP_LANGUAGE_CODE,Variables.DEFAULT_LANGUAGE_CODE)
                , this, ChangePasswordA.class,false);
        setContentView(R.layout.activity_change_password);

        findViewById(R.id.goBack).setOnClickListener(this);
        findViewById(R.id.change_password_btn).setOnClickListener(this);

        ivOldHide = findViewById(R.id.iv_old_hide);
        ivNewHide =findViewById(R.id.iv_new_hide);
        ivConfirmHide = findViewById(R.id.iv_confirm_hide);

        llOldPass = findViewById(R.id.ll_old_hide);
        llOldPass.setOnClickListener(this);
        llNewPass = findViewById(R.id.ll_new_hide);
        llNewPass.setOnClickListener(this);
        llConfirmPass = findViewById(R.id.ll_confirm_hide);
        llConfirmPass.setOnClickListener(this);


        etOldpass = findViewById(R.id.old_password_et);
        etNewpass = findViewById(R.id.new_password_et);
        etConfirmpass = findViewById(R.id.re_password_et);
        changePass = findViewById(R.id.change_password_btn);


        etOldpass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        etNewpass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        etConfirmpass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        etConfirmpass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int count) {
                String txtName = etConfirmpass.getText().toString();
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

    // this will check the validations like none of the field can be the empty
    public boolean checkValidation() {

        String o_password = etOldpass.getText().toString();
        String n_password = etNewpass.getText().toString();
        String v_password = etConfirmpass.getText().toString();

        if (o_password.isEmpty()) {
            etOldpass.setError(getString(R.string.enter_valid_old_password));
            etOldpass.setFocusable(true);
            return false;
        }

        if (o_password.length() <= 5 || o_password.length() >= 12) {
            etOldpass.setError(getString(R.string.valid_password_length));
            etOldpass.setFocusable(true);
            return false;
        }


        if (TextUtils.isEmpty(n_password) || n_password.length() < 1) {
            etNewpass.setError(getString(R.string.enter_valid_new_password));
            etNewpass.setFocusable(true);
            return false;
        }

        if (n_password.length() <= 5 || n_password.length() >= 12) {
            etNewpass.setError(getString(R.string.valid_password_length));
            etNewpass.setFocusable(true);
            return false;
        }


        if (n_password.equalsIgnoreCase(o_password)) {
            etNewpass.setError(getString(R.string.your_password_must_be_different_from_old));
            etNewpass.setFocusable(true);
            return false;
        }


        if (v_password.isEmpty()) {
            etConfirmpass.setError(getString(R.string.enter_valid_verify_password));
            etConfirmpass.setFocusable(true);
            return false;
        }
        if (!v_password.equals(n_password)) {
            etConfirmpass.setError(getString(R.string.password_not_match));
            etConfirmpass.setFocusable(true);
            return false;
        }

        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.goBack:
                ChangePasswordA.super.onBackPressed();
                break;


            case R.id.change_password_btn:
                if (checkValidation()) {
                    callApiForChangePass();
                }
                break;
            case R.id.ll_old_hide:
            {
                if (oldCheck){
                    etOldpass.setTransformationMethod(null);
                    ivOldHide.setImageDrawable(ContextCompat.getDrawable(ChangePasswordA.this,R.drawable.ic_show));
                    oldCheck = false;
                    etOldpass.setSelection(etOldpass.length());
                }else {
                    etOldpass.setTransformationMethod(new PasswordTransformationMethod());
                    ivOldHide.setImageDrawable(ContextCompat.getDrawable(ChangePasswordA.this,R.drawable.ic_hide));
                    oldCheck = true;
                    etOldpass.setSelection(etOldpass.length());
                }
            }
            break;
            case R.id.ll_new_hide:
            {
                if (newCheck){
                    etNewpass.setTransformationMethod(null);
                    ivNewHide.setImageDrawable(ContextCompat.getDrawable(ChangePasswordA.this,R.drawable.ic_show));
                    newCheck = false;
                    etNewpass.setSelection(etNewpass.length());
                }else {
                    etNewpass.setTransformationMethod(new PasswordTransformationMethod());
                    ivNewHide.setImageDrawable(ContextCompat.getDrawable(ChangePasswordA.this,R.drawable.ic_hide));
                    newCheck = true;
                    etNewpass.setSelection(etNewpass.length());
                }
            }
            break;
            case R.id.ll_confirm_hide:
            {
                if (confirmCheck){
                    etConfirmpass.setTransformationMethod(null);
                    ivConfirmHide.setImageDrawable(ContextCompat.getDrawable(ChangePasswordA.this,R.drawable.ic_show));
                    confirmCheck = false;
                    etConfirmpass.setSelection(etConfirmpass.length());
                }else {
                    etConfirmpass.setTransformationMethod(new PasswordTransformationMethod());
                    ivConfirmHide.setImageDrawable(ContextCompat.getDrawable(ChangePasswordA.this,R.drawable.ic_hide));
                    confirmCheck = true;
                    etConfirmpass.setSelection(etConfirmpass.length());
                }
            }
            break;

        }
    }

    private void callApiForChangePass() {
        Functions.showLoader(ChangePasswordA.this, false, false);

        JSONObject parameters = new JSONObject();
        try {
            parameters.put("user_id", Functions.getSharedPreference(ChangePasswordA.this).getString(Variables.U_ID, "0"));
            parameters.put("old_password", etOldpass.getText().toString());
            parameters.put("new_password", etNewpass.getText().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        VolleyRequest.JsonPostRequest(ChangePasswordA.this, ApiLinks.changePassword, parameters,Functions.getHeaders(this), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.checkStatus(ChangePasswordA.this,resp);
                Functions.cancelLoader();
                try {
                    JSONObject response = new JSONObject(resp);
                    String code = response.optString("code");
                    JSONArray msg = response.optJSONArray("msg");
                    if (code.equals("200")) {
                        Toast.makeText(ChangePasswordA.this, getString(R.string.password_change_sucessfully), Toast.LENGTH_SHORT).show();
                        ChangePasswordA.super.onBackPressed();
                    } else {
                        String msg_txt = response.getString("msg");
                        Functions.showToast(ChangePasswordA.this, msg_txt);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }
}