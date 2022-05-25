package com.qboxus.musictok.ActivitesFragment.Accounts;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.chaos.view.PinView;
import com.qboxus.musictok.ActivitesFragment.SplashA;
import com.qboxus.musictok.Models.UserModel;
import com.qboxus.musictok.Models.UserRegisterModel;
import com.qboxus.musictok.MainMenu.MainMenuActivity;
import com.qboxus.musictok.R;
import com.qboxus.musictok.ApiClasses.ApiLinks;
import com.volley.plus.VPackages.VolleyRequest;
import com.volley.plus.interfaces.Callback;
import com.qboxus.musictok.SimpleClasses.DataParsing;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.Variables;

import org.json.JSONObject;

import io.paperdb.Paper;

public class PhoneOtpF extends Fragment implements View.OnClickListener {
    View view;

    TextView tv1, resendCode, editNum;
    ImageView back;
    RelativeLayout rl1;
    SharedPreferences sharedPreferences;
    String phoneNum;
    UserRegisterModel userRegisterModel;
    Button sendOtpBtn;
    PinView etCode;

    public PhoneOtpF() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_otp, container, false);

        Bundle bundle = getArguments();
        phoneNum = bundle.getString("phone_number");
        userRegisterModel = (UserRegisterModel) bundle.getSerializable("user_data");
        sharedPreferences = Functions.getSharedPreference(getContext());

        initViews();
        addClicklistner();
        oneMinuteTimer();

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
        editNum = (TextView) view.findViewById(R.id.edit_num_id);
        editNum.setText(phoneNum);

        back = view.findViewById(R.id.goBack);
        rl1 = view.findViewById(R.id.rl1_id);
        sendOtpBtn = view.findViewById(R.id.send_otp_btn);
        etCode = view.findViewById(R.id.et_code);

    }

    // initlize all the click lister
    private void addClicklistner() {
        back.setOnClickListener(this);
        resendCode.setOnClickListener(this);
        editNum.setOnClickListener(this);
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
    private void callApiCodeVerification() {
        JSONObject parameters = new JSONObject();
        try {

            parameters.put("phone", phoneNum);
            parameters.put("verify", "1");
            parameters.put("code", etCode.getText().toString());
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        Functions.showLoader(getActivity(), false, false);
        VolleyRequest.JsonPostRequest(getActivity(), ApiLinks.verifyPhoneNo, parameters,Functions.getHeadersWithOutLogin(getActivity()), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.checkStatus(getActivity(),resp);
                Functions.cancelLoader();
                parseOptData(resp);

            }
        });
    }

    // this method will parse the api responce
    public void parseOptData(String loginData) {
        try {
            JSONObject jsonObject = new JSONObject(loginData);
            String code = jsonObject.optString("code");
            if (code.equals("200")) {
                callApiPhoneRegisteration();
            } else {
                Toast.makeText(getContext(), jsonObject.optString("msg"), Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // call api for phone register code
    private void callApiPhoneRegisteration() {
        JSONObject parameters = new JSONObject();
        try {

            parameters.put("phone", phoneNum);
        } catch (
                Exception e) {
            e.printStackTrace();
        }

        Functions.showLoader(getActivity(), false, false);
        VolleyRequest.JsonPostRequest(getActivity(), ApiLinks.registerUser, parameters,Functions.getHeadersWithOutLogin(getActivity()), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.checkStatus(getActivity(),resp);
                Functions.cancelLoader();
                parseLoginData(resp);

            }
        });
    }


    private void parseLoginData(String loginData) {
        try {
            JSONObject jsonObject = new JSONObject(loginData);
            String code = jsonObject.optString("code");
            if (code.equals("200")) {
                JSONObject jsonObj = jsonObject.getJSONObject("msg");
                UserModel userDetailModel = DataParsing.getUserDataModel(jsonObj.optJSONObject("User"));
                Functions.storeUserLoginDataIntoDb(view.getContext(),userDetailModel);

                Functions.setUpMultipleAccount(view.getContext());
                Variables.reloadMyVideos = true;
                Variables.reloadMyVideosInner = true;
                Variables.reloadMyLikesInner = true;
                Variables.reloadMyNotification = true;

                if (Paper.book(Variables.MultiAccountKey).getAllKeys().size()>1)
                {
                    Intent intent=new Intent(view.getContext(), SplashA.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    view.getContext().startActivity(intent);
                }
                else
                {
                    Intent intent=new Intent(view.getContext(), MainMenuActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    view.getContext().startActivity(intent);
                }

            } else if (code.equals("201") && !jsonObject.optString("msg").contains("have been blocked")) {

                if (userRegisterModel.dateOfBirth == null) {
                    Functions.showAlert(getActivity(), "", view.getContext().getString(R.string.incorrect_login_details), view.getContext().getString(R.string.signup),view.getContext().getString(R.string.cancel_) , new Callback() {
                        @Override
                        public void onResponce(java.lang.String resp) {
                            if (resp.equalsIgnoreCase("yes")) {
                                openDobFragment();
                            }
                        }
                    });
                } else {
                    openUsernameF();
                }

            } else {
                Toast.makeText(getContext(), jsonObject.optString("msg"), Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void openUsernameF() {
        CreateUsernameF signUp_fragment = new CreateUsernameF("fromPhone");
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left, R.anim.in_from_left, R.anim.out_to_right);
        Bundle bundle = new Bundle();
        userRegisterModel.phoneNo = phoneNum;
        bundle.putSerializable("user_model", userRegisterModel);
        signUp_fragment.setArguments(bundle);
        transaction.addToBackStack(null);
        transaction.replace(R.id.dob_fragment, signUp_fragment).commit();

    }

    private void openDobFragment() {
        DateOfBirthF DOBF = new DateOfBirthF();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left, R.anim.in_from_left, R.anim.out_to_right);
        Bundle bundle = new Bundle();
        userRegisterModel.phoneNo = phoneNum;
        bundle.putSerializable("user_model", userRegisterModel);
        bundle.putString("fromWhere", "fromPhone");
        DOBF.setArguments(bundle);
        transaction.addToBackStack(null);
        transaction.replace(R.id.sign_up_fragment, DOBF).commit();
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
                oneMinuteTimer();
                break;


            case R.id.edit_num_id:
                getActivity().onBackPressed();
                break;

            case R.id.send_otp_btn:
                callApiCodeVerification();
                break;

        }
    }

}