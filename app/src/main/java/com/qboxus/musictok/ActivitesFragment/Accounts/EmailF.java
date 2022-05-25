package com.qboxus.musictok.ActivitesFragment.Accounts;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.klinker.android.link_builder.Link;
import com.klinker.android.link_builder.LinkBuilder;
import com.klinker.android.link_builder.TouchableMovementMethod;
import com.qboxus.musictok.ActivitesFragment.SplashA;
import com.qboxus.musictok.ActivitesFragment.WebviewA;
import com.qboxus.musictok.Constants;
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

import java.util.ArrayList;
import java.util.List;

import io.paperdb.Paper;

public class EmailF extends Fragment implements View.OnClickListener {

    View view;
    EditText emailEdit, passwordEdt;
    ImageView ivHide;
    private Boolean passwordCheck = true;
    RelativeLayout tabPassword,ll_hide;
    TextView  forgotPassBtn;
    Button nextBtn;
    SharedPreferences sharedPreferences;
    String fromWhere;
    UserRegisterModel userRegisterModel;

    TextView loginTermsConditionTxt;
    CheckBox chBox;
    RelativeLayout tabTermsCondition;

    List<Link> links = new ArrayList<>();

    public EmailF(UserRegisterModel userRegisterModel, String fromWhere) {
        this.userRegisterModel = userRegisterModel;
        this.fromWhere = fromWhere;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_email_reg, container, false);
        tabPassword=view.findViewById(R.id.tabPassword);
        emailEdit = view.findViewById(R.id.email_edit);
        passwordEdt = view.findViewById(R.id.password_et);
        passwordEdt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        ll_hide=view.findViewById(R.id.ll_hide);
        ll_hide.setOnClickListener(this);
        ivHide =view.findViewById(R.id.iv_hide);
        forgotPassBtn = view.findViewById(R.id.forgot_pass_btn);
        nextBtn = view.findViewById(R.id.btn_next);
        loginTermsConditionTxt = view.findViewById(R.id.login_terms_condition_txt);
        chBox = view.findViewById(R.id.chBox);
        tabTermsCondition = view.findViewById(R.id.tabTermsCondition);
        chBox.setOnClickListener(this);

        sharedPreferences = Functions.getSharedPreference(view.getContext());
        if (fromWhere != null && fromWhere != null) {
            if (fromWhere.equals("login")) {
                tabTermsCondition.setVisibility(View.GONE);
                forgotPassBtn.setVisibility(View.VISIBLE);
                tabPassword.setVisibility(View.VISIBLE);
                nextBtn.setText(view.getContext().getString(R.string.login));
            } else {
                if (userRegisterModel != null) {
                    emailEdit.setText(userRegisterModel.email);
                    nextBtn.setEnabled(true);
                    nextBtn.setClickable(true);
                }
            }
        }


        emailEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int count) {
                // check the email validation during user typing
                if (loginTermsConditionTxt.getCurrentTextColor()==ContextCompat.getColor(view.getContext(),R.color.redcolor))
                {
                    loginTermsConditionTxt.setError(null);
                    loginTermsConditionTxt.setTextColor(ContextCompat.getColor(view.getContext(),R.color.darkgray));
                }
                String txtName = emailEdit.getText().toString();
                if (txtName.length() > 0) {
                    nextBtn.setEnabled(true);
                    nextBtn.setClickable(true);
                } else {
                    nextBtn.setEnabled(false);
                    nextBtn.setClickable(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        nextBtn.setOnClickListener(this::onClick);
        forgotPassBtn.setOnClickListener(this::onClick);
        SetupScreenData();

        return view;
    }


    private void SetupScreenData() {

        Link link = new Link(view.getContext().getString(R.string.terms_of_use));
        link.setTextColor(getResources().getColor(R.color.black));
        link.setTextColorOfHighlightedLink(getResources().getColor(R.color.colorPrimary));
        link.setUnderlined(true);
        link.setBold(false);
        link.setHighlightAlpha(.20f);
        link.setOnClickListener(new Link.OnClickListener() {
            @Override
            public void onClick(String clickedText) {
                openWebUrl(view.getContext().getString(R.string.terms_of_use),Constants.terms_conditions);
            }
        });

        Link link2 = new Link(view.getContext().getString(R.string.privacy_policy));
        link2.setTextColor(getResources().getColor(R.color.black));
        link2.setTextColorOfHighlightedLink(getResources().getColor(R.color.colorPrimary));
        link2.setUnderlined(true);
        link2.setBold(false);
        link2.setHighlightAlpha(.20f);
        link2.setOnClickListener(new Link.OnClickListener() {
            @Override
            public void onClick(String clickedText) {
                openWebUrl(view.getContext().getString(R.string.privacy_policy),Constants.privacy_policy);
            }
        });
        links.add(link);
        links.add(link2);
        CharSequence sequence = LinkBuilder.from(view.getContext(), loginTermsConditionTxt.getText().toString())
                .addLinks(links)
                .build();
        loginTermsConditionTxt.setText(sequence);
        loginTermsConditionTxt.setMovementMethod(TouchableMovementMethod.getInstance());
    }



    public void openWebUrl(String title, String url) {
        Intent intent=new Intent(view.getContext(), WebviewA.class);
        intent.putExtra("url", url);
        intent.putExtra("title", title);
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.chBox:
            {
                if (loginTermsConditionTxt.getCurrentTextColor()== ContextCompat.getColor(view.getContext(),R.color.redcolor))
                {
                    loginTermsConditionTxt.setError(null);
                    loginTermsConditionTxt.setTextColor(ContextCompat.getColor(view.getContext(),R.color.darkgray));
                }
            }
            break;
            case R.id.ll_hide:
            {
                if (passwordCheck){
                    passwordEdt.setTransformationMethod(null);
                    ivHide.setImageDrawable(ContextCompat.getDrawable(view.getContext(),R.drawable.ic_show));
                    passwordCheck = false;
                    passwordEdt.setSelection(passwordEdt.length());
                }else {
                    passwordEdt.setTransformationMethod(new PasswordTransformationMethod());
                    ivHide.setImageDrawable(ContextCompat.getDrawable(view.getContext(),R.drawable.ic_hide));
                    passwordCheck = true;
                    passwordEdt.setSelection(passwordEdt.length());
                }
            }
            break;
            case R.id.btn_next:
                Functions.printLog(Constants.tag,"next button click");
                // check the email validation and then call the api for login or open the password screen depend of login or signup
                if (checkValidation()) {
                    if (fromWhere.equals("login")) {
                        Functions.printLog(Constants.tag,"next button Login");
                        callApiForLogin();
                    } else {
                        if (chBox.isChecked())
                        {
                            VerifySignupEmailF fragment = new VerifySignupEmailF();
                            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                            transaction.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left, R.anim.in_from_left, R.anim.out_to_right);
                            Bundle bundle = new Bundle();
                            userRegisterModel.email = emailEdit.getText().toString();
                            bundle.putSerializable("user_model", userRegisterModel);
                            fragment.setArguments(bundle);
                            transaction.addToBackStack(null);
                            transaction.replace(R.id.sign_up_fragment, fragment).commit();
                        }
                        else
                        {
                            loginTermsConditionTxt.setError(view.getContext().getString(R.string.please_confirm_terms_and_condition));
                            loginTermsConditionTxt.setTextColor(ContextCompat.getColor(view.getContext(),R.color.redcolor));
                        }

                    }
                }
                break;
            case R.id.forgot_pass_btn:
                startActivity(new Intent(getActivity(), ForgotPassA.class));
                break;
        }

    }

    private void callApiForLogin() {
        JSONObject parameters = new JSONObject();
        try {

            parameters.put("email", emailEdit.getText().toString());
            parameters.put("password", "" + passwordEdt.getText().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        Functions.showLoader(getActivity(), false, false);
        VolleyRequest.JsonPostRequest(getActivity(), ApiLinks.login, parameters,Functions.getHeadersWithOutLogin(getActivity()), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.checkStatus(getActivity(),resp);
                Functions.cancelLoader();
                parseLoginData(resp);

            }
        });

    }

    public void parseLoginData(String loginData) {
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

            } else {
                Toast.makeText(getActivity(), jsonObject.optString("msg"), Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // this will check the validations like none of the field can be the empty
    public boolean checkValidation() {

        final String st_email = emailEdit.getText().toString();
        final String password = passwordEdt.getText().toString();

        Functions.printLog(Constants.tag,"next button st_email");

        if (TextUtils.isEmpty(st_email)) {
            emailEdit.setError(view.getContext().getString(R.string.please_enter_email));
            emailEdit.setFocusable(true);
            return false;
        }

        else if(!Functions.isValidEmail(st_email)){
            emailEdit.setError(view.getContext().getString(R.string.please_enter_valid_email));
            emailEdit.setFocusable(true);
            return false;
        }

        if (fromWhere.equals("login")) {
            if (TextUtils.isEmpty(password)) {
                passwordEdt.setError(view.getContext().getString(R.string.please_enter_password));
                passwordEdt.setFocusable(true);
                return false;
            }
        }

        return true;
    }


}