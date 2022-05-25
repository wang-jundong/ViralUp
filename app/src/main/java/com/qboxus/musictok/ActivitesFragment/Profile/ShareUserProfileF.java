package com.qboxus.musictok.ActivitesFragment.Profile;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.facebook.drawee.view.SimpleDraweeView;
import com.giphy.sdk.core.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.qboxus.musictok.ActivitesFragment.Chat.ChatA;
import com.qboxus.musictok.Adapters.FollowingShareAdapter;
import com.qboxus.musictok.Adapters.ProfileSharingAdapter;
import com.qboxus.musictok.ApiClasses.ApiLinks;
import com.qboxus.musictok.Models.UserModel;
import com.qboxus.musictok.SimpleClasses.DataParsing;
import com.volley.plus.VPackages.VolleyRequest;
import com.qboxus.musictok.Constants;
import com.qboxus.musictok.Interfaces.AdapterClickListener;
import com.volley.plus.interfaces.Callback;
import com.qboxus.musictok.Interfaces.FragmentCallBack;
import com.qboxus.musictok.Models.FollowingModel;
import com.qboxus.musictok.Models.ShareAppModel;
import com.qboxus.musictok.R;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.Variables;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;


public class ShareUserProfileF extends BottomSheetDialogFragment implements View.OnClickListener {


    View view;
    Context context;
    FragmentCallBack callback;
    RecyclerView recyclerView,recylerviewFollowing;
    String userId,userName,fullName,userPic,buttonStatus;

    boolean isDirectMessage=false,fromSetting;
    TextView bottomBtn,sendBtn;
    LinearLayout tabSendMsgToFollower,tabMessageSend;
    DatabaseReference rootref,adduserInbox;
    LinearLayout messageTab,removeFollowerTab,reportTab,blockTab;
    HorizontalScrollView tabSelfScroll;
    View viewSelfScroll;
    SimpleDraweeView ivUserPic;
    EditText edtMessage;
    RelativeLayout tabSendTo;
    ProgressBar progressBarFollowing;
    ArrayList<FollowingModel> followingList =new ArrayList<>();;
    FollowingShareAdapter followingShareAdapter;
    ArrayList<FollowingModel> selectedUserList=new ArrayList<>();
    String senderId="",receiverId="";

    public ShareUserProfileF() {
    }


    public ShareUserProfileF(String id,String name,String fullName,String pic, String buttonStatus, boolean isDirectMessage, boolean fromSetting, FragmentCallBack callback) {
        userId = id;
        this.fullName=fullName;
        userName = name;
        userPic = pic;
        this.buttonStatus=buttonStatus;
        this.fromSetting=fromSetting;
        this.callback = callback;
        this.isDirectMessage=isDirectMessage;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view= inflater.inflate(R.layout.fragment_share_user_profile_, container, false);
        context = view.getContext();
        senderId=Functions.getSharedPreference(context).getString(Variables.U_ID, "");
        rootref = FirebaseDatabase.getInstance().getReference();
        adduserInbox = FirebaseDatabase.getInstance().getReference();
        edtMessage=view.findViewById(R.id.edtMessage);
        tabSendMsgToFollower=view.findViewById(R.id.tabSendMsgToFollower);
        tabMessageSend=view.findViewById(R.id.tabMessageSend);
        tabSendTo=view.findViewById(R.id.tabSendTo);
        progressBarFollowing = view.findViewById(R.id.progressBarFollowing);
        ivUserPic=view.findViewById(R.id.ivUserPic);
        tabSelfScroll=view.findViewById(R.id.tabSelfScroll);
        viewSelfScroll=view.findViewById(R.id.viewSelfScroll);

        messageTab=view.findViewById(R.id.send_message_layout);
        messageTab.setOnClickListener(this);

        removeFollowerTab=view.findViewById(R.id.remove_follower_layout);
        removeFollowerTab.setOnClickListener(this);

        reportTab=view.findViewById(R.id.report_layout);
        reportTab.setOnClickListener(this);

        blockTab=view.findViewById(R.id.block_layout);
        blockTab.setOnClickListener(this);

        bottomBtn =view.findViewById(R.id.bottom_btn);
        bottomBtn.setOnClickListener(this);
        sendBtn=view.findViewById(R.id.sendBtn);
        sendBtn.setOnClickListener(this);




        if (Functions.getSharedPreference(context).getBoolean(Variables.IS_LOGIN,false))
        {
            if (fromSetting)
            {
                tabSelfScroll.setVisibility(View.GONE);
                viewSelfScroll.setVisibility(View.GONE);
            }
            else
            {
                tabSelfScroll.setVisibility(View.VISIBLE);
                viewSelfScroll.setVisibility(View.VISIBLE);
            }
            blockTab.setVisibility(View.VISIBLE);
            if (getFriendStatus())
            {
                removeFollowerTab.setVisibility(View.VISIBLE);
            }
            else
            {
                removeFollowerTab.setVisibility(View.GONE);
            }

            if (isDirectMessage)
            {
                messageTab.setVisibility(View.VISIBLE);
            }
            else
            {
                messageTab.setVisibility(View.GONE);
            }
        }
        else
        {
            messageTab.setVisibility(View.GONE);
            removeFollowerTab.setVisibility(View.GONE);
            blockTab.setVisibility(View.GONE);
        }

        if(Functions.getSharedPreference(context).getBoolean(Variables.IS_LOGIN,false)) {
            setFollowingAdapter();
            callApiForGetAllfollowing();
            getOwnSharedApp();
        }

        return view;
    }

    private boolean getFriendStatus() {
        if (buttonStatus.equalsIgnoreCase("following")) {
            return false;

        }
        else if (buttonStatus.equalsIgnoreCase("friends")) {
            return true;

        }
        else if (buttonStatus.equalsIgnoreCase("follow back")){
            return false;

        }
        else {
            return false;

        }
    }

    private void setFollowingAdapter() {

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

                    item.profile_pic =userDetailModel.getProfilePic();

                    item.follow_status_button = userDetailModel.getButton().toLowerCase();


                    followingList.add(item);
                    followingShareAdapter.notifyDataSetChanged();
                }

               checkFollowerListStatus();

            }
            else
            {
                checkFollowerListStatus();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void checkFollowerListStatus() {
        progressBarFollowing.setVisibility(View.GONE);
        if (followingList.size()>0)
        {
            tabSendTo.setVisibility(View.VISIBLE);

        }
        else
        {
            tabSendTo.setVisibility(View.GONE);
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
            sendBtn.setText(selectedUserList.size()+" "+view.getContext().getString(R.string.send));
            tabSendMsgToFollower.setVisibility(View.GONE);
            tabMessageSend.setVisibility(View.VISIBLE);
        }
        else {
            tabSendMsgToFollower.setVisibility(View.VISIBLE);
            tabMessageSend.setVisibility(View.GONE);
            sendBtn.setText(view.getContext().getString(R.string.send));
        }

        ivUserPic.setController(Functions.frescoImageLoad(userPic,ivUserPic,false));

    }


    // this method will share user profile
    public void profileShare() {

        Intent intent=new Intent(view.getContext(),SendDirectMsg.class);
        intent.putExtra("userId",userId);
        intent.putExtra("userName",userName);
        intent.putExtra("userPic",userPic);
        intent.putExtra("fullName",fullName);
        intent.putExtra("type","profileShare");
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
        dismiss();

    }

    ProfileSharingAdapter adapter;


    public void getOwnSharedApp() {
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
        });recyclerView.setAdapter(adapter);


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


    public void shareProfile(ShareAppModel item) {
        String profielLink = Variables.http+"://"+getString(R.string.share_profile_domain_second)+getString(R.string.share_profile_endpoint_second) + Functions.getSharedPreference(getActivity()).getString(Variables.U_ID,"");
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
                sendIntent.putExtra(Intent.EXTRA_TEXT, profielLink);
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
                sendIntent.putExtra(Intent.EXTRA_TEXT, profielLink);
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
                sendIntent.putExtra(Intent.EXTRA_TEXT, profielLink);
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
                smsIntent.putExtra("sms_body",""+profielLink);
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
                ClipData clip = ClipData.newPlainText("Copied Text", profielLink);
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
                sendIntent.putExtra(Intent.EXTRA_TEXT, profielLink);
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
                sendIntent.putExtra(Intent.EXTRA_TEXT, profielLink);
                startActivity(sendIntent);
            } catch(Exception e) {
                Log.d(Constants.tag,"Exception : "+e);
            }
        }

    }

    private void moveToDirectMsg() {
        profileShare();
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.report_layout:
            {
                dismiss();
                openUserReport();
            }
                break;
            case R.id.block_layout:
            {
                dismiss();
                openBlockUserDialog();
            }
            break;
            case R.id.send_message_layout:
            {
              dismiss();
              openChatF();
            }
            break;
            case R.id.remove_follower_layout:
            {
               dismiss();
                hitRemoveFollowerAPI();
            }
            break;
            case R.id.bottom_btn:
                dismiss();
                break;
            case R.id.sendBtn:
            {
                if(selectedUserList.size()>0)
                {
                    for (FollowingModel item:selectedUserList)
                    {
                       getActivity().runOnUiThread(new Runnable() {
                           @Override
                           public void run() {
                               sendProfileShareMsg(item);
                               if (!(TextUtils.isEmpty(edtMessage.getText().toString())))
                               {
                                   sendInboxMsg(item,edtMessage.getText().toString());
                               }
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
                else
                {
                    Toast.makeText(view.getContext(), view.getContext().getString(R.string.select_atleast_one_user), Toast.LENGTH_SHORT).show();
                }
            }
            break;

        }


    }

    private void hitRemoveFollowerAPI() {
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("user_id", Functions.getSharedPreference(context).getString(Variables.U_ID, ""));
            parameters.put("follower_id", userId);

        } catch (Exception e) {
            e.printStackTrace();
        }


        VolleyRequest.JsonPostRequest(getActivity(), ApiLinks.deleteFollower, parameters,Functions.getHeaders(getActivity()), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.checkStatus(getActivity(),resp);
                try {
                    JSONObject jsonObject = new JSONObject(resp);
                    String code = jsonObject.optString("code");
                    if (code.equals("200")) {
                        Bundle bundle=new Bundle();
                        bundle.putBoolean("isShow",true);
                        callback.onResponce(bundle);
                    }
                }catch (Exception e)
                {
                    Log.d(Constants.tag,"Exception : "+e);
                }
            }
        });

    }

    private void openChatF() {
        Intent intent=new Intent(view.getContext(),ChatA.class);
        intent.putExtra("user_id", userId);
        intent.putExtra("user_name", userName);
        intent.putExtra("user_pic", userPic);
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
    }


    boolean isUserAlreadyBlock=false;
    private void openBlockUserDialog() {
        Query otherBlockStatusQuery = FirebaseDatabase.getInstance().getReference().child("Inbox")
                .child(userId)
                .child(Functions.getSharedPreference(view.getContext()).getString(Variables.U_ID, "0"));

        otherBlockStatusQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.child("block").getValue() != null) {
                    String block = dataSnapshot.child("block").getValue().toString();
                    if (block.equals("1")) {
                        isUserAlreadyBlock = true;
                    } else {
                        isUserAlreadyBlock = false;
                    }
                } else {
                    isUserAlreadyBlock = false;
                }
                blockUserDialog();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Functions.printLog(Constants.tag,"UserNotExit "+error);
            }
        });

    }

    private void blockUserDialog() {
        final CharSequence[] options;
        if (isUserAlreadyBlock)
            options = new CharSequence[]{getString(R.string.unblock_this_user), getString(R.string.cancel_)};
        else
            options = new CharSequence[]{getString(R.string.block_this_user), getString(R.string.cancel_)};

        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogCustom);

        builder.setTitle(null);

        builder.setItems(options, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int item) {

                String text = (String) options[item];
                if (text.equals(getString(R.string.block_this_user))) {
                    rootref.child("Inbox")
                            .child(userId)
                            .child(Functions.getSharedPreference(view.getContext()).getString(Variables.U_ID, "0")).child("block").setValue("1");
                    Functions.showToast(view.getContext(), getString(R.string.user_blocked));
                } else if (text.equals(getString(R.string.unblock_this_user))) {
                    rootref.child("Inbox")
                            .child(userId)
                            .child(Functions.getSharedPreference(view.getContext()).getString(Variables.U_ID, "0")).child("block").setValue("0");
                    Functions.showToast(view.getContext(), getString(R.string.user_unblocked));
                } else if (options[item].equals(getString(R.string.cancel_))) {
                    dialog.dismiss();
                }

            }

        });

        builder.show();
    }


    public void openUserReport() {
        Intent intent=new Intent(view.getContext(), ReportTypeA.class);
        intent.putExtra("user_id", userId);
        intent.putExtra("isFrom",false);
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top);
    }


    private void sendInboxMsg(FollowingModel item,String message) {
        receiverId=item.fb_id;
        Date c = Calendar.getInstance().getTime();
        final String formattedDate = Variables.df.format(c);

        final String current_user_ref = "chat" + "/" + senderId + "-" + receiverId;
        final String chat_user_ref = "chat" + "/" + receiverId + "-" + senderId;

        DatabaseReference reference = rootref.child("chat").child(senderId + "-" + receiverId).push();
        final String pushid = reference.getKey();

        final HashMap message_user_map = new HashMap<>();
        message_user_map.put("receiver_id", receiverId);
        message_user_map.put("sender_id", senderId);
        message_user_map.put("chat_id", pushid);
        message_user_map.put("text", message);
        message_user_map.put("type", "text");
        message_user_map.put("pic_url", "");
        message_user_map.put("status", "0");
        message_user_map.put("time", "");
        message_user_map.put("sender_name", Functions.getSharedPreference(context).getString(Variables.U_NAME, ""));
        message_user_map.put("timestamp", formattedDate);

        final HashMap user_map = new HashMap<>();
        user_map.put(current_user_ref + "/" + pushid, message_user_map);
        user_map.put(chat_user_ref + "/" + pushid, message_user_map);

        rootref.updateChildren(user_map, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                //if first message then set the visibility of whoops layout gone
                String inbox_sender_ref = "Inbox" + "/" + senderId + "/" + receiverId;
                String inbox_receiver_ref = "Inbox" + "/" + receiverId + "/" + senderId;

                HashMap sendermap = new HashMap<>();
                sendermap.put("rid", senderId);
                sendermap.put("name", Functions.getSharedPreference(context).getString(Variables.U_NAME, ""));
                sendermap.put("pic", Functions.getSharedPreference(context).getString(Variables.U_PIC, ""));
                sendermap.put("msg", message);
                sendermap.put("status", "0");
                sendermap.put("timestamp", -1 * System.currentTimeMillis());
                sendermap.put("date", formattedDate);

                HashMap receivermap = new HashMap<>();
                receivermap.put("rid", receiverId);
                receivermap.put("name", item.username);
                receivermap.put("pic", item.profile_pic);
                receivermap.put("msg", message);
                receivermap.put("status", "1");
                receivermap.put("timestamp", -1 * System.currentTimeMillis());
                receivermap.put("date", formattedDate);

                HashMap both_user_map = new HashMap<>();
                both_user_map.put(inbox_sender_ref, receivermap);
                both_user_map.put(inbox_receiver_ref, sendermap);

                adduserInbox.updateChildren(both_user_map).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {


                        ChatA.sendPushNotification(getActivity(), Functions.getSharedPreference(context).getString(Variables.U_NAME, ""), message,
                                receiverId, senderId);


                    }
                });
            }
        });
    }

    private void sendProfileShareMsg(FollowingModel item) {
        receiverId=item.fb_id;
        String message="You shared a profile: "+Functions.showUsername(userName);

        Date c = Calendar.getInstance().getTime();
        final String formattedDate = Variables.df.format(c);

        final String current_user_ref = "chat" + "/" + senderId + "-" + receiverId;
        final String chat_user_ref = "chat" + "/" + receiverId + "-" + senderId;

        DatabaseReference reference = rootref.child("chat").child(senderId + "-" + receiverId).push();
        final String pushid = reference.getKey();

        JSONObject object=new JSONObject();
        try {
            object.put("id",userId);
            object.put("fullName",fullName);
            object.put("username",userName);
            object.put("pic",userPic);
        }catch (Exception e){}

        final HashMap message_user_map = new HashMap<>();
        message_user_map.put("receiver_id", receiverId);
        message_user_map.put("sender_id", senderId);
        message_user_map.put("chat_id", pushid);
        message_user_map.put("text", ""+object);
        message_user_map.put("type", "profileShare");
        message_user_map.put("pic_url", "");
        message_user_map.put("status", "0");
        message_user_map.put("time", "");
        message_user_map.put("sender_name", Functions.getSharedPreference(context).getString(Variables.U_NAME, ""));
        message_user_map.put("timestamp", formattedDate);

        final HashMap user_map = new HashMap<>();
        user_map.put(current_user_ref + "/" + pushid, message_user_map);
        user_map.put(chat_user_ref + "/" + pushid, message_user_map);

        rootref.updateChildren(user_map, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                //if first message then set the visibility of whoops layout gone
                String inbox_sender_ref = "Inbox" + "/" + senderId + "/" + receiverId;
                String inbox_receiver_ref = "Inbox" + "/" + receiverId + "/" + senderId;

                HashMap sendermap = new HashMap<>();
                sendermap.put("rid", senderId);
                sendermap.put("name", Functions.getSharedPreference(context).getString(Variables.U_NAME, ""));
                sendermap.put("pic", Functions.getSharedPreference(context).getString(Variables.U_PIC, ""));
                sendermap.put("msg", message);
                sendermap.put("status", "0");
                sendermap.put("timestamp", -1 * System.currentTimeMillis());
                sendermap.put("date", formattedDate);

                HashMap receivermap = new HashMap<>();
                receivermap.put("rid", receiverId);
                receivermap.put("name", item.username);
                receivermap.put("pic", item.profile_pic);
                receivermap.put("msg", message);
                receivermap.put("status", "1");
                receivermap.put("timestamp", -1 * System.currentTimeMillis());
                receivermap.put("date", formattedDate);

                HashMap both_user_map = new HashMap<>();
                both_user_map.put(inbox_sender_ref, receivermap);
                both_user_map.put(inbox_receiver_ref, sendermap);

                adduserInbox.updateChildren(both_user_map).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {


                        JSONObject notimap = new JSONObject();
                        try {
                            notimap.put("title", Functions.getSharedPreference(context).getString(Variables.U_NAME, ""));
                            notimap.put("message", message);
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


}
