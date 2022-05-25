package com.qboxus.musictok.ActivitesFragment.Accounts;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import com.qboxus.musictok.SimpleClasses.AppCompatLocaleActivity;
import androidx.fragment.app.FragmentTransaction;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.qboxus.musictok.ActivitesFragment.SplashA;
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

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.paperdb.Paper;

public class LoginA extends AppCompatLocaleActivity implements View.OnClickListener {


    FirebaseAuth mAuth;
    FirebaseUser firebaseUser;
    SharedPreferences sharedPreferences;
    UserRegisterModel userRegisterModel = new UserRegisterModel();
    View topView;
    long mBackPressed;
    TextView loginTitleTxt;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Functions.setLocale(Functions.getSharedPreference(LoginA.this).getString(Variables.APP_LANGUAGE_CODE,Variables.DEFAULT_LANGUAGE_CODE)
                , this, LoginA.class,false);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        if (Build.VERSION.SDK_INT == 26) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_USER);
        }

        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_login);


        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();

        // if the user is already login trought facebook then we will logout the user automatically
        LoginManager.getInstance().logOut();

        sharedPreferences = getSharedPreferences(Variables.PREF_NAME, MODE_PRIVATE);

        findViewById(R.id.facebook_btn).setOnClickListener(this::onClick);
        findViewById(R.id.email_login_btn).setOnClickListener(this::onClick);
        findViewById(R.id.signUp).setOnClickListener(this::onClick);
        findViewById(R.id.google_btn).setOnClickListener(this::onClick);
        findViewById(R.id.goBack).setOnClickListener(this::onClick);


        topView = findViewById(R.id.top_view);

        loginTitleTxt = findViewById(R.id.login_title_txt);
        loginTitleTxt.setText(getString(R.string.you_need_a)+" " + getString(R.string.app_name) + "\n"+getString(R.string.account_to_continue));


        SpannableString ss = new SpannableString(getString(R.string.by_signing_up_you_confirm_that_you_agree_to_our_n_terms_of_use_and_have_read_and_understood_n_our_privacy_policy));
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                openPrivacyPolicy();
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
            }
        };
        ss.setSpan(clickableSpan, 99, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);


        TextView textView = (TextView) findViewById(R.id.login_terms_condition_txt);
        textView.setText(ss);
        textView.setClickable(true);
        textView.setMovementMethod(LinkMovementMethod.getInstance());


        Functions.PrintHashKey(LoginA.this);


    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.goBack:
                onBackPressed();
                break;

            case R.id.google_btn:
                signInWithGmail();
                break;

            case R.id.signUp:
                openDobFragment("signup");
                break;

            case R.id.email_login_btn:
                EmailPhoneF emailPhoneF = new EmailPhoneF("login");
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left, R.anim.in_from_left, R.anim.out_to_right);
                Bundle bundle = new Bundle();
                bundle.putSerializable("user_model", userRegisterModel);
                emailPhoneF.setArguments(bundle);
                transaction.addToBackStack(null);
                transaction.replace(R.id.login_f, emailPhoneF).commit();
                break;

            case R.id.facebook_btn:
                Loginwith_FB();
                break;
        }

    }

    public void openPrivacyPolicy() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.privacy_policy));
        startActivity(browserIntent);
    }


    // this method will be call when activity animation will complete
    @Override
    public void onEnterAnimationComplete() {
        super.onEnterAnimationComplete();

        AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(200);
        topView.startAnimation(anim);
        topView.setVisibility(View.VISIBLE);

    }


    @Override
    public void onBackPressed() {
        int count = this.getSupportFragmentManager().getBackStackEntryCount();
        if (count == 0) {
            if (mBackPressed + 2000 > System.currentTimeMillis()) {
                super.onBackPressed();
                return;
            } else {
                topView.setVisibility(View.GONE);
                finish();
                overridePendingTransition(R.anim.in_from_top, R.anim.out_from_bottom);
            }
        } else {
            super.onBackPressed();
        }
    }


    // Bottom two function are related to Fb implimentation
    private CallbackManager mCallbackManager;

    //facebook implimentation
    public void Loginwith_FB() {

        try {
            LoginManager.getInstance().logOut();
        }catch (Exception e){}
        LoginManager.getInstance()
                .logInWithReadPermissions(LoginA.this,
                        Arrays.asList("public_profile", "email"));


        mCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());
                Functions.printLog("resp_token", loginResult.getAccessToken() + "");
            }

            @Override
            public void onCancel() {
                // App code
                Functions.showToast(LoginA.this, getString(R.string.login_cancel));
            }

            @Override
            public void onError(FacebookException error) {
                Functions.printLog("resp", "" + error.toString());
                Functions.showToast(LoginA.this, getString(R.string.login_error) + error.toString());
            }

        });


    }

    private void handleFacebookAccessToken(final AccessToken token) {
        // if user is login then this method will call and
        // facebook will return us a token which will user for get the info of user
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        Functions.printLog("resp_token", token.getToken() + "");
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Functions.showLoader(LoginA.this, false, false);
                            final String id = Profile.getCurrentProfile().getId();
                            GraphRequest request = GraphRequest.newMeRequest(token, new GraphRequest.GraphJSONObjectCallback() {
                                @Override
                                public void onCompleted(JSONObject user, GraphResponse graphResponse) {

                                    Functions.cancelLoader();
                                    Functions.printLog("resp", user.toString());
                                    //after get the info of user we will pass to function which will store the info in our server

                                    String fname = "" + user.optString("first_name");
                                    String lname = "" + user.optString("last_name");
                                    String email = "" + user.optString("email");
                                    String auth_token = token.getToken();
                                    String image = "https://graph.facebook.com/"+id+"/picture?width=500";

                                    userRegisterModel = new UserRegisterModel();

                                    userRegisterModel.fname = Functions.removeSpecialChar(fname);
                                    userRegisterModel.email = email;
                                    userRegisterModel.lname = Functions.removeSpecialChar(lname);
                                    userRegisterModel.socailId = id;
                                    userRegisterModel.picture = image;
                                    userRegisterModel.socailType = "facebook";
                                    userRegisterModel.authTokon = auth_token;


                                    callApiForLogin("" + id,
                                            "facebook",
                                            auth_token);

                                }
                            });

                            // here is the request to facebook sdk for which type of info we have required
                            Bundle parameters = new Bundle();
                            parameters.putString("fields", "last_name,first_name,email");
                            request.setParameters(parameters);
                            request.executeAsync();
                        } else {
                            Functions.cancelLoader();
                            Functions.showToast(LoginA.this, getString(R.string.authentication_failed));
                        }

                    }
                });
    }


    // call the api for login
    private void callApiForLogin(String socialId, String social, final String authtoken) {

        JSONObject parameters = new JSONObject();
        try {

            parameters.put("social_id", socialId);
            parameters.put("social", "" + social);
            parameters.put("auth_token", "" + authtoken);

        } catch (Exception e) {
            e.printStackTrace();
        }

        Functions.showLoader(this, false, false);
        VolleyRequest.JsonPostRequest(this, ApiLinks.registerUser, parameters,Functions.getHeadersWithOutLogin(this), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.checkStatus(LoginA.this,resp);
                Functions.cancelLoader();
                parseLoginData(resp, authtoken);

            }
        });


    }

    public void parseLoginData(String loginData, String authtoken) {
        try {
            JSONObject jsonObject = new JSONObject(loginData);
            String code = jsonObject.optString("code");
            if (code.equals("200")) {

                JSONObject jsonObj = jsonObject.getJSONObject("msg");
                UserModel userDetailModel = DataParsing.getUserDataModel(jsonObj.optJSONObject("User"));
                Functions.storeUserLoginDataIntoDb(LoginA.this,userDetailModel);

                Functions.setUpMultipleAccount(LoginA.this);
                sendBroadcast(new Intent("newVideo"));

                Variables.reloadMyVideos = true;
                Variables.reloadMyVideosInner = true;
                Variables.reloadMyLikesInner = true;
                Variables.reloadMyNotification = true;
                topView.setVisibility(View.GONE);

                if (Paper.book(Variables.MultiAccountKey).getAllKeys().size()>1)
                {
                    Intent intent=new Intent(LoginA.this, SplashA.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    LoginA.this.startActivity(intent);
                }
                else
                {
                    Intent intent=new Intent(this, MainMenuActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }


            } else if (code.equals("201") && !jsonObject.optString("msg").contains("have been blocked")) {
                openDobFragment("social");
            } else {
                Toast.makeText(this, jsonObject.optString("msg"), Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    private void openDobFragment(String fromWhere) {
        DateOfBirthF DOBF = new DateOfBirthF();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left, R.anim.in_from_left, R.anim.out_to_right);
        Bundle bundle = new Bundle();
        bundle.putSerializable("user_model", userRegisterModel);
        bundle.putString("fromWhere", fromWhere);
        DOBF.setArguments(bundle);
        transaction.addToBackStack(null);
        transaction.replace(R.id.login_f, DOBF).commit();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mCallbackManager != null)
        {
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }


    //google Implimentation
    GoogleSignInClient mGoogleSignInClient;
    public void signInWithGmail() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.google_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        try {
            mGoogleSignInClient.signOut();
        }catch (Exception e){}

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(LoginA.this);

        if (account != null) {

            String id = ""+account.getId();
            String fname = "" + account.getGivenName();
            String lname = "" + account.getFamilyName();
            String email = ""+account.getEmail();
            String auth_tokon = ""+account.getIdToken();
            String image=""+account.getPhotoUrl();

            userRegisterModel = new UserRegisterModel();
            userRegisterModel.fname = Functions.removeSpecialChar(fname);
            userRegisterModel.email = email;
            userRegisterModel.lname = Functions.removeSpecialChar(lname);
            userRegisterModel.socailId = id;
            userRegisterModel.authTokon = auth_tokon;
            userRegisterModel.picture=image;

            userRegisterModel.socailType = "google";


            String auth_token = "" + account.getIdToken();


            Functions.printLog(Constants.tag, "signInResult:auth_token===" + auth_token);
            callApiForLogin("" + id,
                    "google",
                    auth_token);

        } else {

            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            resultCallbackForGoogle.launch(signInIntent);

        }

    }


    ActivityResultLauncher<Intent> resultCallbackForGoogle = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                        handleSignInResult(task);
                    }
                }
            });



    //Relate to google login
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null) {
                String id = ""+account.getId();
                String fname = "" + account.getGivenName();
                String lname = "" + account.getFamilyName();
                String auth_token = ""+account.getIdToken();
                String email = ""+account.getEmail();
                String image=""+account.getPhotoUrl();

                Functions.printLog(Constants.tag, "signInResult:auth_token =" + auth_token);
                // if we do not get the picture of user then we will use default profile picture


                userRegisterModel = new UserRegisterModel();

                userRegisterModel.fname = fname;
                userRegisterModel.email = email;
                userRegisterModel.lname = lname;
                userRegisterModel.socailId = id;
                userRegisterModel.socailType = "google";
                userRegisterModel.picture=image;
                userRegisterModel.authTokon = account.getIdToken();


                callApiForLogin("" + id,
                        "google",
                        auth_token);


            }
        } catch (ApiException e) {
            Functions.printLog(Constants.tag, "signInResult:failed code=" + e.getStatusCode());
        }
    }


}
