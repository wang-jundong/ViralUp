package com.qboxus.musictok.ActivitesFragment.Profile.InviteFriends;

import com.qboxus.musictok.SimpleClasses.AppCompatLocaleActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.qboxus.musictok.ActivitesFragment.Profile.ProfileA;
import com.qboxus.musictok.Adapters.FollowingAdapter;
import com.qboxus.musictok.Adapters.InviteFriendAdapter;
import com.qboxus.musictok.Adapters.ProfileSharingAdapter;
import com.qboxus.musictok.ApiClasses.ApiLinks;
import com.volley.plus.VPackages.VolleyRequest;
import com.qboxus.musictok.Constants;
import com.volley.plus.interfaces.APICallBack;
import com.qboxus.musictok.Interfaces.AdapterClickListener;
import com.qboxus.musictok.Models.FollowingModel;
import com.qboxus.musictok.Models.InviteFriendModel;
import com.qboxus.musictok.Models.ShareAppModel;
import com.qboxus.musictok.Models.UserModel;
import com.qboxus.musictok.R;
import com.qboxus.musictok.SimpleClasses.DataParsing;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.Variables;
import com.rilixtech.widget.countrycodepicker.CountryCodePicker;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import cat.ereza.customactivityoncrash.CustomActivityOnCrash;


public class InviteFriendsFromContactsA extends AppCompatLocaleActivity implements View.OnClickListener {


    CountryCodePicker ccp;
    EditText checkNumber;
    RelativeLayout tabInviteOnMultiple;
    TextView tvAllFriend;
    RecyclerView recyclerView;
    RecyclerView inviteRecyclerView;
    ArrayList<InviteFriendModel> datalist=new ArrayList<>();
    InviteFriendAdapter adapter;
    String fromWhere="2";


    ArrayList<FollowingModel> userList;
    FollowingAdapter usersAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Functions.setLocale(Functions.getSharedPreference(this).getString(Variables.APP_LANGUAGE_CODE,Variables.DEFAULT_LANGUAGE_CODE)
                , this, InviteFriendsFromContactsA.class,false);
        setContentView(R.layout.activity_invite_friends_from_contacts);

        InitControl();
    }

    private void InitControl() {
        fromWhere=getIntent().getStringExtra("fromWhere");
        findViewById(R.id.ivBack).setOnClickListener(this);
        //for validation check
        ccp=new CountryCodePicker(InviteFriendsFromContactsA.this);
        checkNumber=new EditText(InviteFriendsFromContactsA.this);
        tabInviteOnMultiple=findViewById(R.id.tabInviteOnMultiple);
        tvAllFriend=findViewById(R.id.tvAllFriend);

        if (fromWhere.equalsIgnoreCase("1"))
        {
            tvAllFriend.setVisibility(View.VISIBLE);
            tabInviteOnMultiple.setVisibility(View.VISIBLE);
            getSharedApp();
            setUpContactAdapter();
            getContactList();
        }
        else
        if (fromWhere.equalsIgnoreCase("2"))
        {
            tvAllFriend.setVisibility(View.GONE);
            tabInviteOnMultiple.setVisibility(View.GONE);
            setUpContactAdapter();
            getContactList();
        }
        else
        if (fromWhere.equalsIgnoreCase("3"))
        {
            tvAllFriend.setVisibility(View.GONE);
            tabInviteOnMultiple.setVisibility(View.GONE);
            setUpFacebookContactAdapter();
            getFacebookContact();
        }



    }


    ProfileSharingAdapter inviteAdapter;
    public void getSharedApp() {
        inviteRecyclerView = (RecyclerView) findViewById(R.id.inviteRecyclerView);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(InviteFriendsFromContactsA.this,LinearLayoutManager.HORIZONTAL,false);
        inviteRecyclerView.setLayoutManager(layoutManager);
        inviteRecyclerView.setHasFixedSize(false);
        inviteAdapter = new ProfileSharingAdapter(InviteFriendsFromContactsA.this, getAppShareDataList(), new AdapterClickListener() {
            @Override
            public void onItemClick(View view, int pos, Object object) {
                ShareAppModel item= (ShareAppModel) object;
                shareProfile(item);
            }
        });
        inviteRecyclerView.setAdapter(inviteAdapter);
    }

    public void shareProfile(ShareAppModel item) {
        String profielLink = Variables.http+"://"+getString(R.string.share_profile_domain_second)+getString(R.string.share_profile_endpoint_second) + Functions.getSharedPreference(InviteFriendsFromContactsA.this).getString(Variables.U_ID,"");
        if (item.getName().equalsIgnoreCase(getString(R.string.whatsapp)))
        {

            try {
                Intent sendIntent = new Intent("android.intent.action.MAIN");
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.setType("text/plain");
                sendIntent.putExtra(Intent.EXTRA_TEXT, profielLink);
                sendIntent.setPackage("com.whatsapp");
                startActivity(sendIntent);
            } catch(Exception e) {
                Log.d(Constants.tag,"Exception : "+e);
            }
        }
        else
        if (item.getName().equalsIgnoreCase(getString(R.string.facebook)))
        {
            try {
                Intent sendIntent = new Intent("android.intent.action.MAIN");
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.setType("text/plain");
                sendIntent.putExtra(Intent.EXTRA_TEXT, profielLink);
                sendIntent.setPackage("com.facebook.katana");
                startActivity(sendIntent);
            } catch(Exception e) {
                Log.d(Constants.tag,"Exception : "+e);
            }
        }
        else
        if (item.getName().equalsIgnoreCase(getString(R.string.messenger)))
        {
            try {
                Intent sendIntent = new Intent("android.intent.action.MAIN");
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.setType("text/plain");
                sendIntent.putExtra(Intent.EXTRA_TEXT, profielLink);
                sendIntent.setPackage("com.facebook.orca");
                startActivity(sendIntent);
            } catch(Exception e) {
                Log.d(Constants.tag,"Exception : "+e);
            }
        }
        else
        if (item.getName().equalsIgnoreCase(getString(R.string.sms)))
        {
            try {
                Intent smsIntent = new Intent(Intent.ACTION_VIEW);
                smsIntent.setType("vnd.android-dir/mms-sms");
                smsIntent.putExtra("sms_body",""+profielLink);
                startActivity(smsIntent);
            } catch(Exception e) {
                Log.d(Constants.tag,"Exception : "+e);
            }
        }
        else
        if (item.getName().equalsIgnoreCase(getString(R.string.copy_link)))
        {
            try {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Copied Text", profielLink);
                clipboard.setPrimaryClip(clip);

                Toast.makeText(InviteFriendsFromContactsA.this, getString(R.string.link_copy_in_clipboard), Toast.LENGTH_SHORT).show();
            } catch(Exception e) {
                Log.d(Constants.tag,"Exception : "+e);
            }
        }
        else
        if (item.getName().equalsIgnoreCase(getString(R.string.email)))
        {
            try {
                Intent sendIntent = new Intent("android.intent.action.MAIN");
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.setType("text/plain");
                sendIntent.putExtra(Intent.EXTRA_TEXT, profielLink);
                sendIntent.setPackage("com.google.android.gm");
                startActivity(sendIntent);
            } catch(Exception e) {
                Log.d(Constants.tag,"Exception : "+e);
            }
        }
        else
        if (item.getName().equalsIgnoreCase(getString(R.string.other)))
        {
            try {
                Intent sendIntent = new Intent("android.intent.action.MAIN");
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.setType("text/plain");
                sendIntent.putExtra(Intent.EXTRA_TEXT, profielLink);
                startActivity(sendIntent);
            } catch(Exception e) {
                Log.d(Constants.tag,"Exception : "+e);
            }
        }

    }


    private ArrayList<ShareAppModel> getAppShareDataList() {
        ArrayList<ShareAppModel> dataList=new ArrayList<>();
        {
            if (Functions.appInstalledOrNot(InviteFriendsFromContactsA.this,"com.whatsapp"))
            {
                ShareAppModel item=new ShareAppModel();
                item.setName(getString(R.string.whatsapp));
                item.setIcon(R.drawable.ic_share_whatsapp);
                dataList.add(item);
            }
        }
        {
            if (Functions.appInstalledOrNot(InviteFriendsFromContactsA.this,"com.facebook.katana"))
            {
                ShareAppModel item=new ShareAppModel();
                item.setName(getString(R.string.facebook));
                item.setIcon(R.drawable.ic_share_facebook);
                dataList.add(item);
            }
        }
        {
            if (Functions.appInstalledOrNot(InviteFriendsFromContactsA.this,"com.facebook.orca"))
            {
                ShareAppModel item=new ShareAppModel();
                item.setName(getString(R.string.messenger));
                item.setIcon(R.drawable.ic_share_messenger);
                dataList.add(item);
            }
        }
        {
            ShareAppModel item=new ShareAppModel();
            item.setName(getString(R.string.sms));
            item.setIcon(R.drawable.ic_share_sms);
            dataList.add(item);
        }
        {
            ShareAppModel item=new ShareAppModel();
            item.setName(getString(R.string.copy_link));
            item.setIcon(R.drawable.ic_share_copy_link);
            dataList.add(item);
        }
        {
            if (Functions.appInstalledOrNot(InviteFriendsFromContactsA.this,"com.whatsapp"))
            {
                ShareAppModel item=new ShareAppModel();
                item.setName(getString(R.string.email));
                item.setIcon(R.drawable.ic_share_email);
                dataList.add(item);
            }
        }
        {
            ShareAppModel item=new ShareAppModel();
            item.setName(getString(R.string.other));
            item.setIcon(R.drawable.ic_share_other);
            dataList.add(item);
        }
        return dataList;
    }


    private CallbackManager mCallbackManager;
    private void getFacebookContact() {
        LoginManager.getInstance().logOut();
        mCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().logInWithReadPermissions(InviteFriendsFromContactsA.this, Arrays.asList("public_profile", "user_friends", "email"));
        LoginManager.getInstance().registerCallback(mCallbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        {
                            GraphRequest request = GraphRequest.newMyFriendsRequest(loginResult.getAccessToken(),
                                    new GraphRequest.GraphJSONArrayCallback() {
                                        @Override
                                        public void onCompleted(JSONArray objects, GraphResponse response) {
                                            try {

                                                JSONArray contactArray=new JSONArray();
                                                for (int i=0;i<objects.length();i++)
                                                {
                                                    JSONObject innerObj=objects.getJSONObject(i);
                                                    JSONObject user = new JSONObject();
                                                    user.put("fb_id", innerObj.optString("id"));
                                                    contactArray.put(user);
                                                }
                                                hitFacebookInvitationContactApi(contactArray);
                                            }
                                            catch (Exception e)
                                            {
                                                Log.d(Constants.tag,"Exception : "+e);
                                            }
                                        }
                                    });
                            request.executeAsync();
                            Bundle param = new Bundle();
                            param.putString("fields", "id,name");
                            request.setParameters(param);
                            request.executeAsync();
                        }
                    }
                    @Override
                    public void onCancel() {
                        Log.d(Constants.tag, "onCancel");
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Log.d(Constants.tag, "onError : "+exception);
                    }
                });

    }

    private void hitFacebookInvitationContactApi(JSONArray contactJSON) {
        JSONObject parameters = new JSONObject();
        try {

            parameters.put("user_id", Functions.getSharedPreference(InviteFriendsFromContactsA.this).getString(Variables.U_ID, ""));
            parameters.put("facebook_ids",contactJSON);

        } catch (Exception e) {
            e.printStackTrace();
        }


        VolleyRequest.JsonPostRequest(InviteFriendsFromContactsA.this, ApiLinks.showRegisteredContacts, parameters, Functions.getHeaders(this),resp -> {
            parseFacebookData(resp);
        });
    }


    // parse the list of all the follower list
    public void parseFacebookData(String responce) {
        try {
            JSONObject jsonObject = new JSONObject(responce);
            String code = jsonObject.optString("code");
            userList.clear();
            if (code.equals("200")) {
                JSONArray msgArray = jsonObject.getJSONArray("msg");

                for (int i = 0; i < msgArray.length(); i++) {
                    JSONObject object = msgArray.optJSONObject(i);
                    UserModel userDetailModel = DataParsing.getUserDataModel(object.optJSONObject("User"));

                    FollowingModel item = new FollowingModel();
                    item.fb_id = userDetailModel.getId();
                    item.first_name = userDetailModel.getFirstName();
                    item.last_name = userDetailModel.getLastName();
                    item.bio = userDetailModel.getBio();
                    item.username = userDetailModel.getUsername();
                    item.profile_pic = userDetailModel.getProfilePic();
                    item.isFollow=true;
                    String userStatus=userDetailModel.getButton().toLowerCase();
                    item.follow_status_button=Functions.getFollowButtonStatus(userStatus,InviteFriendsFromContactsA.this);
                    item.notificationType="normal";
                    userList.add(item);
                    usersAdapter.notifyDataSetChanged();
                }
            }

            if (userList.isEmpty()) {
                findViewById(R.id.no_data_layout).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.no_data_layout).setVisibility(View.GONE);
            }

        } catch (Exception e) {
            Log.d(Constants.tag,"Exception : "+e);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mCallbackManager != null) {
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }


    private void setUpFacebookContactAdapter() {
        userList = new ArrayList<>();
        recyclerView =findViewById(R.id.recylerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(InviteFriendsFromContactsA.this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        usersAdapter = new FollowingAdapter(InviteFriendsFromContactsA.this,true,"",userList, new FollowingAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int postion, FollowingModel item) {

                switch (view.getId()) {
                    case R.id.action_txt:
                        if (Functions.checkLoginUser(InviteFriendsFromContactsA.this)) {
                            if (!item.fb_id.equals(Functions.getSharedPreference(InviteFriendsFromContactsA.this).getString(Variables.U_ID, "")))
                                followUnFollowUser(item, postion);
                        }
                        break;
                    case R.id.mainlayout:
                        openProfile(item);
                        break;
                }

            }
        });
        recyclerView.setAdapter(usersAdapter);

    }


    public void followUnFollowUser(final FollowingModel item, final int position) {

        Functions.callApiForFollowUnFollow(InviteFriendsFromContactsA.this,
                Functions.getSharedPreference(InviteFriendsFromContactsA.this).getString(Variables.U_ID, ""),
                item.fb_id,
                new APICallBack() {
                    @Override
                    public void arrayData(ArrayList arrayList) {


                    }

                    @Override
                    public void onSuccess(String responce) {
                        try {
                            JSONObject jsonObject=new JSONObject(responce);
                            String code=jsonObject.optString("code");
                            if(code.equalsIgnoreCase("200")){
                                JSONObject msg=jsonObject.optJSONObject("msg");
                                if(msg!=null){
                                    UserModel userDetailModel = DataParsing.getUserDataModel(msg.optJSONObject("User"));
                                    if(!(TextUtils.isEmpty(userDetailModel.getId()))){
                                        FollowingModel itemUpdte=item;
                                        String userStatus=userDetailModel.getButton().toLowerCase();
                                        itemUpdte.follow_status_button=Functions.getFollowButtonStatus(userStatus,InviteFriendsFromContactsA.this);
                                        userList.set(position,itemUpdte);
                                        usersAdapter.notifyDataSetChanged();

                                    }
                                }
                            }
                        } catch (Exception e) {
                            Log.d(Constants.tag,"Exception : "+e);
                        }
                    }

                    @Override
                    public void onFail(String responce) {

                    }

                });


    }



    private void openProfile(final FollowingModel item) {
        Intent intent=new Intent(InviteFriendsFromContactsA.this, ProfileA.class);
        intent.putExtra("user_id", item.fb_id);
        intent.putExtra("user_name", item.username);
        intent.putExtra("user_pic", item.profile_pic);
        startActivity(intent);
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
    }


    private void setUpContactAdapter() {
        datalist = new ArrayList<>();
        recyclerView =findViewById(R.id.recylerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(InviteFriendsFromContactsA.this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        adapter = new InviteFriendAdapter(datalist, new AdapterClickListener() {
            @Override
            public void onItemClick(View view, int pos, Object object) {
                InviteFriendModel item= (InviteFriendModel) object;
                String profielLink = Variables.http+"://"+getString(R.string.share_profile_domain_second)+getString(R.string.share_profile_endpoint_second) + Functions.getSharedPreference(InviteFriendsFromContactsA.this).getString(Variables.U_ID,"");
                try {
                    String fullName=Functions.getSharedPreference(InviteFriendsFromContactsA.this).getString(Variables.F_NAME,"")+" "+Functions.getSharedPreference(InviteFriendsFromContactsA.this).getString(Variables.L_NAME,"");
                    String sendingMessage=getString(R.string.app_name)+" i am "+fullName+" on "+getString(R.string.app_name)+".To donwload the app and watch more videos,\\ntap: "+profielLink;

                    Intent smsIntent = new Intent(Intent.ACTION_VIEW);
                    smsIntent.setType("vnd.android-dir/mms-sms");
                    smsIntent.putExtra("address", item.getPhone());
                    smsIntent.putExtra("sms_body",""+sendingMessage);
                    startActivity(smsIntent);
                } catch(Exception e) {
                    Log.d(Constants.tag,"Exception : "+e);
                }
            }
        });
        recyclerView.setAdapter(adapter);
    }


    private static final String[] PROJECTION = new String[] {
            ContactsContract.CommonDataKinds.Phone.NUMBER,ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,ContactsContract.CommonDataKinds.Phone.PHOTO_URI
    };

    private void getContactList() {
        datalist.clear();
        adapter.notifyDataSetChanged();
        Functions.showLoader(InviteFriendsFromContactsA.this,false,false);
        new AsyncTask<Object, Object, Object>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();

            }

            @Override
            protected Object doInBackground(Object[] objects) {
                try {

                    ContentResolver cr = getContentResolver();
                    Cursor cursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, PROJECTION, null, null, null);
                    if (cursor != null) {
                        try {
                            final int number = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                            final int Name = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                            final int image = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI);

                            while (cursor.moveToNext()) {
                                if (cursor.getString(number) != null) {

                                    try {

                                        String userNumber = cursor.getString(number).replaceAll("[\\s*#.]", "");
                                        String userName = cursor.getString(Name);
                                        String userImage = cursor.getString(image);



                                        ccp.registerPhoneNumberTextView(checkNumber);
                                        checkNumber.setText(userNumber);
                                        if (ccp.isValid())
                                        {
                                            userNumber= checkNumber.getText().toString();
                                            if (userNumber.charAt(0)=='0')
                                            {
                                                userNumber=userNumber.substring(1);
                                            }
                                            if (!(userNumber.contains("+")))
                                            {
                                                userNumber=userNumber.replace(ccp.getSelectedCountryCode(),"");
                                                userNumber=ccp.getSelectedCountryCodeWithPlus()+userNumber;
                                            }
                                            userNumber=userNumber.replace(" ","");
                                            userNumber=userNumber.replace("(","");
                                            userNumber=userNumber.replace(")","");
                                            userNumber=userNumber.replace("-","");



                                            InviteFriendModel model=new InviteFriendModel();
                                            model.setName(userName);
                                            model.setPhone(userNumber);
                                            model.setPath(userImage);
                                            datalist.add(model);

                                        }



                                    }
                                    catch (Exception e)
                                    {
                                        Log.d(Constants.tag,"Exception : "+e);
                                    }


                                }
                            }
                        } finally {
                            cursor.close();
                        }
                    }

                }catch (Exception e){
                    Log.d(Constants.tag,"Exception : "+e);
                }
                finally {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                ArrayList<InviteFriendModel> tempList=new ArrayList<>();
                tempList.addAll(datalist);

                try {
                    HashMap<String, InviteFriendModel> mapData=new HashMap<>();
                    for (InviteFriendModel itemData:tempList)
                    {
                        mapData.put(itemData.getPhone(),itemData);
                    }
                    tempList.clear();
                    JSONArray contactArray=new JSONArray();
                    for (String key : mapData.keySet())
                    {
                        tempList.add(mapData.get(key));
                        JSONObject innerJSON=new JSONObject();
                        innerJSON.put("phone_number",mapData.get(key).getPhone());
                        innerJSON.put("name",mapData.get(key).getName());
                        contactArray.put(innerJSON);
                    }
                    datalist.clear();
                    datalist.addAll(tempList);
                    Functions.cancelLoader();
                    adapter.notifyDataSetChanged();

                }
                catch (Exception e)
                {
                    Log.d(Constants.tag,"Exception : "+e);
                }
            }
        }.execute();
    }



    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.ivBack:
            {
                InviteFriendsFromContactsA.super.onBackPressed();
            }
            break;
        }
    }


}