package com.qboxus.musictok.ActivitesFragment;


import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.qboxus.musictok.ActivitesFragment.Profile.SendDirectMsg;
import com.qboxus.musictok.Adapters.FollowingShareAdapter;
import com.qboxus.musictok.Adapters.ProfileSharingAdapter;
import com.qboxus.musictok.Models.UserModel;
import com.qboxus.musictok.SimpleClasses.DataParsing;
import com.volley.plus.VPackages.VolleyRequest;
import com.qboxus.musictok.Constants;
import com.qboxus.musictok.Interfaces.AdapterClickListener;
import com.volley.plus.interfaces.Callback;
import com.qboxus.musictok.Models.FollowingModel;
import com.qboxus.musictok.Models.HomeModel;
import com.qboxus.musictok.ApiClasses.ApiLinks;
import com.qboxus.musictok.Models.ShareAppModel;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.PermissionUtils;
import com.qboxus.musictok.SimpleClasses.Variables;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.qboxus.musictok.R;
import com.qboxus.musictok.Interfaces.FragmentCallBack;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */

public class VideoActionF extends BottomSheetDialogFragment implements View.OnClickListener {

    View view;
    Context context;
    RecyclerView recyclerView, recylerviewFollowing;

    FragmentCallBack fragmentCallback;

    String videoId, userId,userName,userPic,fullName;
    ProgressBar progressBar;
    HomeModel item;
    String senderId="",receiverId="";
    ArrayList<FollowingModel> selectedUserList=new ArrayList<>();
    TextView bottomBtn;


    PermissionUtils takePermissionUtils;

    public VideoActionF() {
    }

    @SuppressLint("ValidFragment")
    public VideoActionF(String id, FragmentCallBack fragmentCallback) {
        videoId = id;
        this.fragmentCallback = fragmentCallback;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        view = inflater.inflate(R.layout.fragment_video_action, container, false);
        context = getContext();
        senderId=Functions.getSharedPreference(view.getContext()).getString(Variables.U_ID, "");

        Bundle bundle = getArguments();
        if (bundle != null) {
            item = (HomeModel) bundle.getSerializable("data");
            videoId = bundle.getString("videoId");
            userId = bundle.getString("userId");
            userName = bundle.getString("userName");
            userPic = bundle.getString("userPic");
            fullName = bundle.getString("fullName");
        }

        progressBar = view.findViewById(R.id.progress_bar);

        view.findViewById(R.id.copy_layout).setOnClickListener(this);
        view.findViewById(R.id.delete_layout).setOnClickListener(this);
        view.findViewById(R.id.privacy_setting_layout).setOnClickListener(this);

        if (userId != null && userId.equals(Functions.getSharedPreference(context).getString(Variables.U_ID, ""))) {
            view.findViewById(R.id.delete_layout).setVisibility(View.VISIBLE);
            view.findViewById(R.id.privacy_setting_layout).setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.delete_layout).setVisibility(View.GONE);
            view.findViewById(R.id.privacy_setting_layout).setVisibility(View.GONE);
        }

        if (isShowVideoDownloadPrivacy(item)) {

            view.findViewById(R.id.save_video_layout).setVisibility(View.VISIBLE);
            view.findViewById(R.id.save_video_layout).setOnClickListener(this::onClick);
        } else
            view.findViewById(R.id.save_video_layout).setVisibility(View.GONE);


        if (Constants.IS_DEMO_APP) {
            progressBar.setVisibility(View.GONE);
            view.findViewById(R.id.copy_layout).setVisibility(View.GONE);
        } else {

            getSharedApp();

        }




        if (Functions.getSharedPreference(context).getBoolean(Variables.IsExtended,false) && (item.allow_duet != null && item.allow_duet.equalsIgnoreCase("1"))
                && Functions.isShowContentPrivacy(context, item.apply_privacy_model.getDuet(), item.follow_status_button.equalsIgnoreCase("friends"))) {
            view.findViewById(R.id.duet_layout).setVisibility(View.VISIBLE);
            view.findViewById(R.id.duet_layout).setOnClickListener(this::onClick);
        } else {
            view.findViewById(R.id.duet_layout).setVisibility(View.GONE);
        }


        view.findViewById(R.id.add_favourite_layout).setOnClickListener(this::onClick);
        view.findViewById(R.id.not_intrested_layout).setOnClickListener(this::onClick);
        view.findViewById(R.id.report_layout).setOnClickListener(this::onClick);
        if (userId != null && userId.equals(Functions.getSharedPreference(context).getString(Variables.U_ID, ""))) {
            view.findViewById(R.id.not_intrested_layout).setVisibility(View.GONE);
            view.findViewById(R.id.report_layout).setVisibility(View.GONE);
        }



        bottomBtn =view.findViewById(R.id.bottom_btn);
        bottomBtn.setOnClickListener(this::onClick);

        if(Functions.getSharedPreference(context).getBoolean(Variables.IS_LOGIN,false)) {
            setFollowingAdapter();
            callApiForGetAllfollowing();
        }


        return view;
    }

    private boolean isShowVideoDownloadPrivacy(HomeModel home_item) {
        if (home_item.apply_privacy_model.getVideos_download() != null && home_item.apply_privacy_model.getVideos_download().equalsIgnoreCase("0"))
            return false;
        else
            return true;
    }






    ArrayList<FollowingModel> followingList;
    FollowingShareAdapter followingShareAdapter;
    public void setFollowingAdapter(){
        followingList=new ArrayList<>();
        recylerviewFollowing =view.findViewById(R.id.recylerview_following);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false);
        recylerviewFollowing.setLayoutManager(layoutManager);
        recylerviewFollowing.setHasFixedSize(false);

        followingShareAdapter=new FollowingShareAdapter(context, followingList, new AdapterClickListener() {
            @Override
            public void onItemClick(View view, int pos, Object object) {
                clickedUsers(pos);
            }
        });
        recylerviewFollowing.setAdapter(followingShareAdapter);
    }

    private void callApiForGetAllfollowing() {

        JSONObject parameters = new JSONObject();
        try {
            parameters.put("user_id", Functions.getSharedPreference(context).getString(Variables.U_ID, ""));

        } catch (Exception e) {
            e.printStackTrace();
        }


        VolleyRequest.JsonPostRequest(getActivity(), ApiLinks.showFollowing, parameters,Functions.getHeaders(getActivity()), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.checkStatus(getActivity(),resp);
                parseFollowingData(resp);
            }
        });


    }

    public void parseFollowingData(String responce) {

        try {
            JSONObject jsonObject = new JSONObject(responce);
            String code = jsonObject.optString("code");
            if (code.equals("200")) {
                JSONArray msgArray = jsonObject.getJSONArray("msg");
                for (int i = 0; i < msgArray.length(); i++) {

                    JSONObject object = msgArray.optJSONObject(i);
                    UserModel userDetailModel= DataParsing.getUserDataModel(object.optJSONObject("FollowingList"));

                    FollowingModel item = new FollowingModel();
                    item.fb_id = userDetailModel.getId();
                    item.bio = userDetailModel.getBio();
                    item.username = userDetailModel.getUsername();
                    item.profile_pic = userDetailModel.getProfilePic();
                    item.follow_status_button = userDetailModel.getButton().toLowerCase();


                    followingList.add(item);
                }

                followingShareAdapter.notifyDataSetChanged();
                view.findViewById(R.id.sendTo_txt).setVisibility(View.VISIBLE);
                view.findViewById(R.id.recylerview_following).setVisibility(View.VISIBLE);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public void clickedUsers(int postion){
        FollowingModel itemUpdate= followingList.get(postion);
        selectedUserList=new ArrayList<>();
        if (itemUpdate.is_select)
        {
            itemUpdate.is_select=false;
            followingList.set(postion,itemUpdate);
        }
        else
        {
            itemUpdate.is_select=true;
            followingList.set(postion,itemUpdate);
        }
        followingShareAdapter.notifyDataSetChanged();

        for (int i = 0; i< followingList.size(); i++){

            if (followingList.get(i).is_select)
            {
                selectedUserList.add(followingList.get(i));
            }
        }
        if(selectedUserList.size()>0){
            bottomBtn.setText(selectedUserList.size()+" "+view.getContext().getString(R.string.send));
            bottomBtn.setBackground(ContextCompat.getDrawable(view.getContext(),R.color.colorPrimary));
        }
        else {
            bottomBtn.setBackground(ContextCompat.getDrawable(view.getContext(),R.color.white));
            bottomBtn.setText(view.getContext().getString(R.string.cancel_));
        }


    }


    public void sendvideo(FollowingModel followerItem) {

        DatabaseReference rootref= FirebaseDatabase.getInstance().getReference();
        String senderId=Functions.getSharedPreference(view.getContext()).getString(Variables.U_ID,"0");

        Date c = Calendar.getInstance().getTime();
        final String formattedDate = Variables.df.format(c);


        DatabaseReference dref = rootref.child("chat").child(senderId + "-" + followerItem.fb_id).push();
        final String key = dref.getKey();

        String current_user_ref = "chat" + "/" + senderId + "-" + followerItem.fb_id;
        String chat_user_ref = "chat" + "/" + followerItem.fb_id + "-" + senderId;

        HashMap message_user_map = new HashMap<>();
        message_user_map.put("receiver_id", followerItem.fb_id);
        message_user_map.put("sender_id", senderId);
        message_user_map.put("chat_id", key);
        message_user_map.put("text", "");
        message_user_map.put("type", "video");
        message_user_map.put("pic_url", item.thum);
        message_user_map.put("video_id", item.video_id);
        message_user_map.put("status", "0");
        message_user_map.put("time", "");
        message_user_map.put("sender_name", Functions.getSharedPreference(view.getContext()).getString(Variables.U_NAME, ""));
        message_user_map.put("timestamp", formattedDate);
        HashMap user_map = new HashMap<>();

        user_map.put(current_user_ref + "/" + key, message_user_map);
        user_map.put(chat_user_ref + "/" + key, message_user_map);

        rootref.updateChildren(user_map, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                String inbox_sender_ref = "Inbox" + "/" + senderId + "/" + followerItem.fb_id;
                String inbox_receiver_ref = "Inbox" + "/" + followerItem.fb_id + "/" + senderId;


                HashMap sendermap = new HashMap<>();
                sendermap.put("rid", senderId);
                sendermap.put("name", Functions.getSharedPreference(view.getContext()).getString(Variables.U_NAME, ""));
                sendermap.put("pic", Functions.getSharedPreference(view.getContext()).getString(Variables.U_PIC, ""));
                sendermap.put("msg", "Send an video...");
                sendermap.put("status", "0");
                sendermap.put("timestamp", -1 * System.currentTimeMillis());
                sendermap.put("date", formattedDate);

                HashMap receivermap = new HashMap<>();
                receivermap.put("rid", followerItem.fb_id);
                receivermap.put("name", followerItem.username);
                receivermap.put("pic", followerItem.profile_pic);
                receivermap.put("msg", "Send an video...");
                receivermap.put("status", "1");
                receivermap.put("timestamp", -1 * System.currentTimeMillis());
                receivermap.put("date", formattedDate);

                HashMap both_user_map = new HashMap<>();
                both_user_map.put(inbox_sender_ref, receivermap);
                both_user_map.put(inbox_receiver_ref, sendermap);

                rootref.updateChildren(both_user_map).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        JSONObject notimap = new JSONObject();
                        try {
                            notimap.put("title", Functions.getSharedPreference(view.getContext()).getString(Variables.U_NAME, ""));
                            notimap.put("message", "You have a new message");
                            notimap.put("sender_id", senderId);
                            JSONArray receiverArray=new JSONArray();
                            receiverArray.put(new JSONObject().put("receiver_id",receiverId));
                            notimap.put("receivers", receiverArray);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        VolleyRequest.JsonPostRequest(getActivity(), ApiLinks.sendPushNotification, notimap,Functions.getHeaders(getActivity()), new Callback() {
                            @Override
                            public void onResponce(String resp) {
                Functions.checkStatus(getActivity(),resp);

                            }
                        });
                    }
                });

            }
        });
    }


    // show the list of apps that can be used for share the video link to its friends
    ProfileSharingAdapter adapter;

    public void getSharedApp() {
        recyclerView = (RecyclerView) view.findViewById(R.id.recylerview);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(false);

        adapter = new ProfileSharingAdapter(context, getAppShareDataList(), new AdapterClickListener() {
            @Override
            public void onItemClick(View view, int pos, Object object) {
                ShareAppModel item= (ShareAppModel) object;

                shareProfile(item);
            }
        });

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                recyclerView.setAdapter(adapter);
                progressBar.setVisibility(View.GONE);
            }
        });
    }


    public void shareProfile(ShareAppModel item) {
        String videoLink = Constants.BASE_URL + "?" + Functions.getRandomString(3) + videoId + Functions.getRandomString(3);
        if (item.getName().equalsIgnoreCase(view.getContext().getString(R.string.messenge)))
        {
            moveToDirectMsg();
        }
        else
        if (item.getName().equalsIgnoreCase(view.getContext().getString(R.string.whatsapp)))
        {

            try {
                Intent sendIntent = new Intent("android.intent.action.MAIN");
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.setType("text/plain");
                sendIntent.putExtra(Intent.EXTRA_TEXT, videoLink);
                sendIntent.setPackage("com.whatsapp");
                startActivity(sendIntent);
            } catch(Exception e) {
                Log.d(Constants.tag,"Exception : "+e);
            }
        }
        else
        if (item.getName().equalsIgnoreCase(view.getContext().getString(R.string.facebook)))
        {
            try {
                Intent sendIntent = new Intent("android.intent.action.MAIN");
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.setType("text/plain");
                sendIntent.putExtra(Intent.EXTRA_TEXT, videoLink);
                sendIntent.setPackage("com.facebook.katana");
                startActivity(sendIntent);
            } catch(Exception e) {
                Log.d(Constants.tag,"Exception : "+e);
            }
        }
        else
        if (item.getName().equalsIgnoreCase(view.getContext().getString(R.string.messenger)))
        {
            try {
                Intent sendIntent = new Intent("android.intent.action.MAIN");
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.setType("text/plain");
                sendIntent.putExtra(Intent.EXTRA_TEXT, videoLink);
                sendIntent.setPackage("com.facebook.orca");
                startActivity(sendIntent);
            } catch(Exception e) {
                Log.d(Constants.tag,"Exception : "+e);
            }
        }
        else
        if (item.getName().equalsIgnoreCase(view.getContext().getString(R.string.sms)))
        {
            try {
                Intent smsIntent = new Intent(Intent.ACTION_VIEW);
                smsIntent.setType("vnd.android-dir/mms-sms");
                smsIntent.putExtra("sms_body",""+videoLink);
                startActivity(smsIntent);
            } catch(Exception e) {
                Log.d(Constants.tag,"Exception : "+e);
            }
        }
        else
        if (item.getName().equalsIgnoreCase(view.getContext().getString(R.string.copy_link)))
        {
            try {
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Copied Text", videoLink);
                clipboard.setPrimaryClip(clip);

                Toast.makeText(context, context.getString(R.string.link_copy_in_clipboard), Toast.LENGTH_SHORT).show();
            } catch(Exception e) {
                Log.d(Constants.tag,"Exception : "+e);
            }
        }
        else
        if (item.getName().equalsIgnoreCase(view.getContext().getString(R.string.email)))
        {
            try {
                Intent sendIntent = new Intent("android.intent.action.MAIN");
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.setType("text/plain");
                sendIntent.putExtra(Intent.EXTRA_TEXT, videoLink);
                sendIntent.setPackage("com.google.android.gm");
                startActivity(sendIntent);
            } catch(Exception e) {
                Log.d(Constants.tag,"Exception : "+e);
            }
        }
        else
        if (item.getName().equalsIgnoreCase(view.getContext().getString(R.string.other)))
        {
            try {
                Intent sendIntent = new Intent("android.intent.action.MAIN");
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.setType("text/plain");
                sendIntent.putExtra(Intent.EXTRA_TEXT, videoLink);
                startActivity(sendIntent);
            } catch(Exception e) {
                Log.d(Constants.tag,"Exception : "+e);
            }
        }

    }




    private void moveToDirectMsg() {
        videoShare();
    }

    private void videoShare() {
        Intent intent=new Intent(view.getContext(), SendDirectMsg.class);
        intent.putExtra("userId",userId);
        intent.putExtra("userName",userName);
        intent.putExtra("userPic",userPic);
        intent.putExtra("fullName",fullName);
        intent.putExtra("thum",item.thum);
        intent.putExtra("videoId",item.video_id);
        intent.putExtra("type","videoShare");
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
        dismiss();
    }


    private ArrayList<ShareAppModel> getAppShareDataList() {
        ArrayList<ShareAppModel> dataList=new ArrayList<>();
        {
            ShareAppModel item=new ShareAppModel();
            item.setName(getString(R.string.messenge));
            item.setIcon(R.drawable.ic_share_message);
            dataList.add(item);
        }
        {
            if (Functions.appInstalledOrNot(view.getContext(),"com.whatsapp"))
            {
                ShareAppModel item=new ShareAppModel();
                item.setName(getString(R.string.whatsapp));
                item.setIcon(R.drawable.ic_share_whatsapp);
                dataList.add(item);
            }
        }
        {
            if (Functions.appInstalledOrNot(view.getContext(),"com.facebook.katana"))
            {
                ShareAppModel item=new ShareAppModel();
                item.setName(getString(R.string.facebook));
                item.setIcon(R.drawable.ic_share_facebook);
                dataList.add(item);
            }
        }
        {
            if (Functions.appInstalledOrNot(view.getContext(),"com.facebook.orca"))
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
            if (Functions.appInstalledOrNot(view.getContext(),"com.whatsapp"))
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







    private ActivityResultLauncher<String[]> mPermissionResult = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onActivityResult(Map<String, Boolean> result) {

                    boolean allPermissionClear=true;
                    List<String> blockPermissionCheck=new ArrayList<>();
                    for (String key : result.keySet())
                    {
                        if (!(result.get(key)))
                        {
                            allPermissionClear=false;
                            blockPermissionCheck.add(Functions.getPermissionStatus(getActivity(),key));
                        }
                    }
                    if (blockPermissionCheck.contains("blocked"))
                    {
                        Functions.showPermissionSetting(view.getContext(),getString(R.string.we_need_storage_permission_for_save_video));
                    }
                    else
                    if (allPermissionClear)
                    {
                        saveVideoAction();
                    }

                }
            });











    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.save_video_layout:
                takePermissionUtils=new PermissionUtils(getActivity(),mPermissionResult);
                if (takePermissionUtils.isStoragePermissionGranted()) {
                    saveVideoAction();
                }
                else
                {
                    takePermissionUtils.showStoragePermissionDailog(view.getContext().getString(R.string.we_need_storage_permission_for_save_video));
                }

                break;


            case R.id.duet_layout:
                if (Functions.checkLoginUser(getActivity()))
                {
                    takePermissionUtils=new PermissionUtils(getActivity(),mPermissionStorageCameraRecordingResult);
                    if (takePermissionUtils.isStorageCameraRecordingPermissionGranted())
                    {
                        openDuetAction();
                    }
                    else
                    {
                        takePermissionUtils.showStorageCameraRecordingPermissionDailog(context.getString(R.string.we_need_storage_camera_recording_permission_for_make_new_duet_video));
                    }

                }
                break;

            case R.id.copy_layout:
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Copied Text", Constants.BASE_URL + "?" + Functions.getRandomString(3) + videoId + Functions.getRandomString(3));
                clipboard.setPrimaryClip(clip);

                Toast.makeText(context, context.getString(R.string.link_copy_in_clipboard), Toast.LENGTH_SHORT).show();
                break;

            case R.id.delete_layout:
                if (Constants.IS_DEMO_APP) {
                    Toast.makeText(context, context.getString(R.string.delete_function_not_available_in_demo), Toast.LENGTH_SHORT).show();
                } else {
                    Bundle bundle = new Bundle();
                    bundle.putString("action", "delete");
                    dismiss();

                    if (fragmentCallback != null)
                        fragmentCallback.onResponce(bundle);
                }
                break;


            case R.id.privacy_setting_layout:
                if (Constants.IS_DEMO_APP) {
                    Toast.makeText(context, context.getString(R.string.privacy_function_not_available_in_demo), Toast.LENGTH_SHORT).show();
                } else {
                    Bundle bundle = new Bundle();
                    bundle.putString("action", "privacy");
                    dismiss();

                    if (fragmentCallback != null)
                        fragmentCallback.onResponce(bundle);
                }
                break;

            case R.id.add_favourite_layout:
                Bundle bundle = new Bundle();
                bundle.putString("action", "favourite");
                dismiss();

                if (fragmentCallback != null)
                    fragmentCallback.onResponce(bundle);

                break;

            case R.id.not_intrested_layout:
                Bundle not_interested_bundle = new Bundle();
                not_interested_bundle.putString("action", "not_intrested");
                dismiss();

                if (fragmentCallback != null)
                    fragmentCallback.onResponce(not_interested_bundle);
                break;

            case R.id.report_layout:
                Bundle report_bundle = new Bundle();
                report_bundle.putString("action", "report");
                dismiss();

                if (fragmentCallback != null)
                    fragmentCallback.onResponce(report_bundle);
                break;


            case R.id.bottom_btn:
                if(selectedUserList.size()>0){
                    for (FollowingModel item:selectedUserList)
                    {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                sendvideo(item);
                            }
                        });
                    }
                    Functions.showLoader(view.getContext(),false,false);
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Functions.cancelLoader();
                                    Toast.makeText(view.getContext(), view.getContext().getString(R.string.profile_share_successfully_completed), Toast.LENGTH_SHORT).show();
                                    dismiss();
                                }
                            });
                        }
                    },1500);
                }
                else {
                    dismiss();
                }

                break;

        }


    }

    private void openDuetAction() {
        Bundle duet_bundle = new Bundle();
        duet_bundle.putString("action", "duet");
        dismiss();

        if (fragmentCallback != null)
            fragmentCallback.onResponce(duet_bundle);
    }

    private void saveVideoAction() {
        Bundle bundle = new Bundle();
        bundle.putString("action", "save");
        dismiss();

        if (fragmentCallback != null)
            fragmentCallback.onResponce(bundle);
    }


    private ActivityResultLauncher<String[]> mPermissionStorageCameraRecordingResult = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onActivityResult(Map<String, Boolean> result) {

                    boolean allPermissionClear=true;
                    List<String> blockPermissionCheck=new ArrayList<>();
                    for (String key : result.keySet())
                    {
                        if (!(result.get(key)))
                        {
                            allPermissionClear=false;
                            blockPermissionCheck.add(Functions.getPermissionStatus(getActivity(),key));
                        }
                    }
                    if (blockPermissionCheck.contains("blocked"))
                    {
                        Functions.showPermissionSetting(context,context.getString(R.string.we_need_storage_camera_recording_permission_for_make_new_duet_video));
                    }
                    else
                    if (allPermissionClear)
                    {
                        openDuetAction();
                    }

                }
            });


    @Override
    public void onDetach() {
        super.onDetach();
        mPermissionResult.unregister();
        mPermissionStorageCameraRecordingResult.unregister();
    }
}
