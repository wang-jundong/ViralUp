package com.qboxus.musictok.ActivitesFragment.Accounts;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chaos.view.PinView;
import com.qboxus.musictok.ApiClasses.ApiLinks;
import com.volley.plus.VPackages.VolleyRequest;
import com.volley.plus.interfaces.Callback;
import com.qboxus.musictok.Models.UserRegisterModel;
import com.qboxus.musictok.R;
import com.qboxus.musictok.SimpleClasses.Functions;

import org.json.JSONObject;

public class VerifySignupEmailF extends Fragment implements View.OnClickListener{

    View view;

    TextView tv1, resendCode, edtEmail;
    ImageView back;
    RelativeLayout rl1;
    SharedPreferences sharedPreferences;
    UserRegisterModel userRegisterModel;
    Button sendOtpBtn;
    PinView etCode;

    public VerifySignupEmailF() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view= inflater.inflate(R.layout.fragment_verify_signup_email, container, false);
        Bundle bundle = getArguments();
        userRegisterModel = (UserRegisterModel) bundle.getSerializable("user_model");
        sharedPreferences = Functions.getSharedPreference(getContext());

        initViews();
        addClicklistner();

        callApiCodeVerification(false);


        etCode.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int count) {
                // this will check th opt code validation
                String txtName = etCode.getText().toString();
                if (txtName.length() == 4) {
                    sendOtpBtn.setEnabled(true);
                    sendOtpBtn.setClickable(true);
                } else {
                    sendOtpBtn.setEnabled(false);
                    sendOtpBtn.setClickable(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        return view;
    }


    private void initViews() {
        tv1 = (TextView) view.findViewById(R.id.tv1_id);
        resendCode = (TextView) view.findViewById(R.id.resend_code);
        edtEmail = (TextView) view.findViewById(R.id.edtEmail);
        edtEmail.setText(userRegisterModel.email);

        back = view.findViewById(R.id.goBack);
        rl1 = view.findViewById(R.id.rl1_id);
        sendOtpBtn = view.findViewById(R.id.send_otp_btn);
        etCode = view.findViewById(R.id.et_code);

    }

    // initlize all the click lister
    private void addClicklistner() {
        back.setOnClickListener(this);
        resendCode.setOnClickListener(this);
        edtEmail.setOnClickListener(this);
        sendOtpBtn.setOnClickListener(this);
    }

    // run the one minute countdown timer
    private void oneMinuteTimer() {
        rl1.setVisibility(View.VISIBLE);

        new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long l) {
                tv1.setText(view.getContext().getString(R.string.resend_code)+" 00:" + l / 1000);
            }

            @Override
            public void onFinish() {
                rl1.setVisibility(View.GONE);
                resendCode.setVisibility(View.VISIBLE);
            }

        }.start();

    }

    // this method will call the api for code varification
    private void callApiCodeVerification(boolean isVerify) {
        JSONObject parameters = new JSONObject();
        try {

            if(isVerify)
            {
                parameters.put("email", userRegisterModel.email);
                parameters.put("code", etCode.getText().toString());
            }
            else
            {
                parameters.put("email", userRegisterModel.email);
            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }

        Functions.showLoader(getActivity(), false, false);
        VolleyRequest.JsonPostRequest(getActivity(), ApiLinks.verifyRegisterEmailCode, parameters,Functions.getHeadersWithOutLogin(getActivity()), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.checkStatus(getActivity(),resp);
                Functions.cancelLoader();
                parseOptData(resp,isVerify);
            }
        });
    }

    // this method will parse the api responce
    public void parseOptData(String loginData,boolean isVerify) {
        try {
            JSONObject jsonObject = new JSONObject(loginData);
            String code = jsonObject.optString("code");
            if (code.equals("200")) {
                if (isVerify)
                {
                    openCreatePasswordF();
                }
                else
                {
                    oneMinuteTimer();
                }

            } else {
                Toast.makeText(getContext(), jsonObject.optString("msg"), Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void openCreatePasswordF() {
        CreatePasswordF create_password_f = new CreatePasswordF("fromEmail");
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left, R.anim.in_from_left, R.anim.out_to_right);
        Bundle bundle = new Bundle();
        bundle.putSerializable("user_model", userRegisterModel);
        create_password_f.setArguments(bundle);
        transaction.addToBackStack(null);
        transaction.replace(R.id.email_verify_container, create_password_f).commit();

    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.goBack:
                getActivity().onBackPressed();
                break;

            case R.id.resend_code:
                resendCode.setVisibility(View.GONE);
                etCode.setText("");
                callApiCodeVerification(false);
                break;


            case R.id.edit_num_id:
                getActivity().onBackPressed();
                break;

            case R.id.send_otp_btn:
                callApiCodeVerification(true);
                break;

        }
    }

}