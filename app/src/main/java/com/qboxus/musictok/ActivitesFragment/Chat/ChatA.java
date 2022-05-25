package com.qboxus.musictok.ActivitesFragment.Chat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import com.qboxus.musictok.SimpleClasses.AppCompatLocaleActivity;
import androidx.exifinterface.media.ExifInterface;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.devlomi.record_view.OnRecordListener;
import com.devlomi.record_view.RecordButton;
import com.devlomi.record_view.RecordView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.qboxus.musictok.ActivitesFragment.Chat.Audio.SendAudio;
import com.qboxus.musictok.ActivitesFragment.Profile.EditProfileA;
import com.qboxus.musictok.ActivitesFragment.Profile.ProfileA;
import com.qboxus.musictok.ActivitesFragment.Profile.ReportTypeA;
import com.qboxus.musictok.ActivitesFragment.Profile.SeeFullImageA;
import com.qboxus.musictok.ActivitesFragment.Profile.Setting.NoInternetA;
import com.qboxus.musictok.ActivitesFragment.WatchVideosA;
import com.qboxus.musictok.Constants;
import com.qboxus.musictok.Interfaces.FragmentCallBack;
import com.qboxus.musictok.Interfaces.InternetCheckCallback;
import com.qboxus.musictok.Models.PrivacyPolicySettingModel;
import com.qboxus.musictok.Models.PushNotificationSettingModel;
import com.qboxus.musictok.Models.UserModel;
import com.qboxus.musictok.R;
import com.volley.plus.interfaces.APICallBack;
import com.qboxus.musictok.ApiClasses.ApiLinks;
import com.volley.plus.VPackages.VolleyRequest;
import com.qboxus.musictok.SimpleClasses.DataParsing;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.PermissionUtils;
import com.qboxus.musictok.SimpleClasses.Variables;
import com.downloader.Error;
import com.downloader.OnDownloadListener;
import com.downloader.PRDownloader;
import com.giphy.sdk.core.models.Media;
import com.giphy.sdk.core.models.enums.MediaType;
import com.giphy.sdk.core.network.api.CompletionHandler;
import com.giphy.sdk.core.network.api.GPHApi;
import com.giphy.sdk.core.network.api.GPHApiClient;
import com.giphy.sdk.core.network.response.ListMediaResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class ChatA extends AppCompatLocaleActivity implements View.OnClickListener {

    DatabaseReference rootref;
    String senderId = "";
    String receiverId = "";
    String receiverName = "";
    String receiverPic = "null";
    public static String token = "null";
    public static String playingId = "none";
    public static MediaPlayer mediaPlayer;
    EditText message;
    public int audioPostion;
    public static int mediaPlayerProgress = 0;
    private DatabaseReference adduserInbox;

    private DatabaseReference mchatRefReteriving;
    private DatabaseReference sendTypingIndication;
    private DatabaseReference receiveTypingIndication;
    RecyclerView chatrecyclerview;
    TextView userName;
    private List<ChatModel> mChats = new ArrayList<>();
    ChatAdapter mAdapter;
    ProgressBar pBar;

    Query queryGetchat;
    Query myBlockStatusQuery;
    Query otherBlockStatusQuery;
    boolean isUserAlreadyBlock = false;

    SimpleDraweeView profileimage;
    public static String senderidForCheckNotification = "";
    public static String uploadingImageId = "none";
    FrameLayout chatMainView;
    public Context context;
    LinearLayout gifLayout;
    ImageButton uploadGifBtn;
    ImageView sendbtn;
    ImageButton alertBtn;

    public static String uploadingAudioId = "none";

    private RecordButton micBtn;
    File direct;
    SendAudio sendAudio;
    boolean isPrivacyfollow = false;
    RelativeLayout tabChat;


    PermissionUtils takePermissionUtils;

    String audioPermissionCheck="player";
    View selectedAudioView;
    int selectedAudioPosition;
    ChatModel selectedChatModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Functions.setLocale(Functions.getSharedPreference(ChatA.this).getString(Variables.APP_LANGUAGE_CODE,Variables.DEFAULT_LANGUAGE_CODE)
                , this, ChatA.class,false);
        setContentView(R.layout.activity_chat);

        context = ChatA.this;
        direct = new File(Functions.getAppFolder(ChatA.this));
        // intialize the database refer
        rootref = FirebaseDatabase.getInstance().getReference();
        adduserInbox = FirebaseDatabase.getInstance().getReference();
        chatMainView=findViewById(R.id.chat_F);
        message = (EditText) findViewById(R.id.msgedittext);
        tabChat = findViewById(R.id.writechatlayout);
        userName = findViewById(R.id.username);
        profileimage = findViewById(R.id.profileimage);
        pBar = findViewById(R.id.progress_bar);

        // the sender id and reciever id from the back activity in which we come from

       {

           senderId = Functions.getSharedPreference(ChatA.this).getString(Variables.U_ID, "");
           receiverId = getIntent().getStringExtra("user_id");
           receiverName = getIntent().getStringExtra("user_name");
           receiverPic = getIntent().getStringExtra("user_pic");


           if (receiverId == null && TextUtils.isEmpty(receiverId)) {
               moveBack();
           } else {

               if (!TextUtils.isEmpty(receiverName))
                   userName.setText(receiverName);


               if (receiverPic != null && receiverPic.equalsIgnoreCase("")) {
                   Uri uri = Uri.parse(receiverPic);
                   profileimage.setImageURI(uri);
               }


           }

           senderidForCheckNotification = receiverId;
           rootref.child("Users").child(receiverId).addListenerForSingleValueEvent(new ValueEventListener() {
               @Override
               public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                   if (dataSnapshot.exists())
                       token = dataSnapshot.child("token").getValue().toString();
               }

               @Override
               public void onCancelled(@NonNull DatabaseError databaseError) {

               }
           });
       }



        //set layout manager to chat recycler view and get all the privous chat of th user which spacifc user
        chatrecyclerview = (RecyclerView) findViewById(R.id.chatlist);
        final LinearLayoutManager layout = new LinearLayoutManager(context);
        layout.setStackFromEnd(true);
        chatrecyclerview.setLayoutManager(layout);
        chatrecyclerview.setHasFixedSize(false);
        ((SimpleItemAnimator) chatrecyclerview.getItemAnimator()).setSupportsChangeAnimations(false);
        mAdapter = new ChatAdapter(mChats, senderId, context, new ChatAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(ChatModel item, View view, int postion) {
                if (item.getType().equalsIgnoreCase("profileShare"))
                    openUserProfile(item);
                else
                if (item.getType().equals("image"))
                    openFullSizeImage(item);

                else if(item.getType().equals("video"))
                    openVideo(item);

                if (view.getId() == R.id.audio_bubble) {
                    selectedAudioView=view;
                    selectedAudioPosition=postion;
                    selectedChatModel=item;
                    takePermissionUtils=new PermissionUtils(ChatA.this,mPermissionStorageRecordingResult);
                    audioPermissionCheck="playing";
                    if (takePermissionUtils.isStorageRecordingPermissionGranted()) {
                        audioPlaying(selectedAudioView,selectedChatModel,selectedAudioPosition);
                    }
                    else
                    {
                        takePermissionUtils.showStorageRecordingPermissionDailog(getString(R.string.we_need_recording_permission_for_upload_sound));
                    }
                }

            }
        }, new ChatAdapter.OnLongClickListener() {
            @Override
            public void onLongclick(ChatModel item, View view) {
                if (senderId.equals(item.getSender_id()) && istodaymessage(item.getTimestamp())) {
                    if (view.getId() == R.id.msgtxt) {
                        deleteMessage(item);
                    } else if (view.getId() == R.id.chatimage) {
                        deleteMessage(item);
                    } else if (view.getId() == R.id.audio_bubble) {
                        deleteMessage(item);
                    }
                }

            }

        });


        chatrecyclerview.setAdapter(mAdapter);


        chatrecyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            boolean userScrolled;
            int scrollOutitems;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    userScrolled = true;
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                scrollOutitems = layout.findFirstCompletelyVisibleItemPosition();

                if (userScrolled && (scrollOutitems == 0 && mChats.size() > 9)) {
                    userScrolled = false;
                    rootref.child("chat").child(senderId + "-" + receiverId).orderByChild("chat_id")
                            .endAt(mChats.get(0).getChat_id()).limitToLast(20)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    ArrayList<ChatModel> arrayList = new ArrayList<>();
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                        ChatModel item = snapshot.getValue(ChatModel.class);
                                        arrayList.add(item);
                                    }
                                    for (int i = arrayList.size() - 2; i >= 0; i--) {
                                        mChats.add(0, arrayList.get(i));
                                    }

                                    mAdapter.notifyDataSetChanged();

                                    if (arrayList.size() > 8) {
                                        chatrecyclerview.scrollToPosition(arrayList.size());
                                    }

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                }
            }
        });


        gifLayout = findViewById(R.id.gif_layout);

        // this the send btn action in that mehtod we will check message field is empty or not
        // if not then we call a method and pass the message
        sendbtn = findViewById(R.id.sendbtn);
        sendbtn.setOnClickListener(this::onClick);

        findViewById(R.id.uploadimagebtn).setOnClickListener(this::onClick);


        uploadGifBtn = findViewById(R.id.upload_gif_btn);
        uploadGifBtn.setOnClickListener(this::onClick);


        findViewById(R.id.goBack).setOnClickListener(this::onClick);

        message.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    sendTypingIndicator(false);
                }
            }
        });

        micBtn = findViewById(R.id.mic_btn);
        // this is the message field event lister which tells the second user either the user is typing or not
        // most importent to show type indicator to second user
        message.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    sendTypingIndicator(true);
                    sendbtn.setVisibility(View.VISIBLE);
                    micBtn.setVisibility(View.GONE);
                } else {
                    sendbtn.setVisibility(View.GONE);
                    micBtn.setVisibility(View.VISIBLE);
                    sendTypingIndicator(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });


        sendAudio = new SendAudio(context, message, rootref, adduserInbox, senderId, receiverId, receiverName, receiverPic);

        // this the mic touch listener
        // when our touch action is Down is will start recording and when our Touch action is Up
        // it will stop the recording


        RecordView recordView = (RecordView) findViewById(R.id.record_view);
        micBtn.setRecordView(recordView);
        recordView.setSoundEnabled(true);
        recordView.setOnRecordListener(new OnRecordListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onStart() {
                findViewById(R.id.write_layout).setVisibility(View.GONE);
                findViewById(R.id.uploadimagebtn).setVisibility(View.GONE);

                takePermissionUtils=new PermissionUtils(ChatA.this,mPermissionStorageRecordingResult);
                audioPermissionCheck="recording";
                if (takePermissionUtils.isStorageRecordingPermissionGranted()) {
                    sendAudio.startRecording();
                }
                else
                {
                    takePermissionUtils.showStorageRecordingPermissionDailog(getString(R.string.we_need_recording_permission_for_upload_sound));
                }


            }

            @Override
            public void onCancel() {

                sendAudio.stopTimer();
                findViewById(R.id.write_layout).setVisibility(View.VISIBLE);
            }

            @Override
            public void onFinish(long recordTime) {
                sendAudio.stopRecording();

                findViewById(R.id.write_layout).setVisibility(View.VISIBLE);
            }

            @Override
            public void onLessThanSecond() {
                sendAudio.stopTimerWithoutRecoder();
                findViewById(R.id.write_layout).setVisibility(View.VISIBLE);
            }
        });
        recordView.setSlideToCancelText(getString(R.string.slide_to_cancel));
        micBtn.setListenForRecord(true);
        recordView.setLessThanSecondAllowed(false);


        // this method receiver the type indicator of second user to tell that his friend is typing or not
        receiveTypeIndication();


        alertBtn = findViewById(R.id.alert_btn);
        alertBtn.setOnClickListener(this::onClick);


        client = new GPHApiClient(context.getResources().getString(R.string.gif_api_key));
        getChatData();
        callApiForUserDetails();
    }

    private void audioPlaying(View view,ChatModel item,int postion) {
        RelativeLayout mainlayout = (RelativeLayout) view.getParent();
        File fullpath = new File(Functions.getAppFolder(ChatA.this) + item.chat_id + ".mp3");

        if (fullpath.exists()) {

            if (playingId.equals(item.chat_id)) {
                stopPlaying();
            } else {

                playAudio(postion, item);
            }

        } else {

            downloadAudio((ProgressBar) mainlayout.findViewById(R.id.p_bar), item);
        }
    }

    private void openUserProfile(ChatModel item) {
       try {

           JSONObject jsonObject=new JSONObject(item.getText());
           String userId=jsonObject.optString("id");
           String username=jsonObject.optString("username");
           String pic=jsonObject.optString("pic");

           Intent intent=new Intent(ChatA.this, ProfileA.class);
           intent.putExtra("user_id", userId);
           intent.putExtra("user_name", username);
           intent.putExtra("user_pic", pic);
           startActivity(intent);
           overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top);

       }
       catch (Exception e)
       {
           Log.d(Constants.tag,"Exception : "+e);
       }
    }

    private void moveBack() {
        Intent intent = new Intent();
        intent.putExtra("isShow", true);
        setResult(RESULT_OK, intent);
        finish();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.alert_btn:
                blockUserDialog();
                break;

            case R.id.goBack:
                Functions.hideSoftKeyboard(ChatA.this);
                ChatA.super.onBackPressed();
                break;

            case R.id.upload_gif_btn:
                if (gifLayout.getVisibility() == View.VISIBLE) {
                    slideDown();
                } else {
                    slideUp();
                    getGipy();
                }
                break;

            case R.id.uploadimagebtn:
            {
                takePermissionUtils=new PermissionUtils(ChatA.this,mPermissionCameraStorageResult);

                if (takePermissionUtils.isStorageCameraPermissionGranted())
                {
                    selectImage();
                }
                else
                {
                    takePermissionUtils.showStorageCameraPermissionDailog(getString(R.string.we_need_storage_permission_for_upload_media_file));
                }

            }

                break;

            case R.id.sendbtn:

                if (!TextUtils.isEmpty(message.getText().toString())) {
                    if (gifLayout.getVisibility() == View.VISIBLE) {
                        searchGif(message.getText().toString());
                    } else {
                        sendMessage(message.getText().toString());
                        message.setText(null);
                    }

                }

                break;
        }

    }






    ValueEventListener valueEventListener;
    ChildEventListener eventListener;
    ValueEventListener myInboxListener;
    ValueEventListener otherInboxListener;

    private void getChatData() {
        mChats.clear();
        mchatRefReteriving = FirebaseDatabase.getInstance().getReference();
        queryGetchat = mchatRefReteriving.child("chat").child(senderId + "-" + receiverId);

        myBlockStatusQuery = mchatRefReteriving.child("Inbox")
                .child(Functions.getSharedPreference(ChatA.this).getString(Variables.U_ID, "0"))
                .child(receiverId);

        otherBlockStatusQuery = mchatRefReteriving.child("Inbox")
                .child(receiverId)
                .child(Functions.getSharedPreference(ChatA.this).getString(Variables.U_ID, "0"));


        // this will get all the messages between two users
        eventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                try {
                    ChatModel model = dataSnapshot.getValue(ChatModel.class);
                    mChats.add(model);
                    mAdapter.notifyDataSetChanged();
                    chatrecyclerview.scrollToPosition(mChats.size() - 1);
                } catch (Exception ex) {
                    Log.e("", ex.getMessage());
                }
                changeStatus();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {


                if (dataSnapshot != null && dataSnapshot.getValue() != null) {

                    try {
                        ChatModel model = dataSnapshot.getValue(ChatModel.class);

                        for (int i = mChats.size() - 1; i >= 0; i--) {
                            if (mChats.get(i).getTimestamp().equals(dataSnapshot.child("timestamp").getValue())) {
                                mChats.remove(i);
                                mChats.add(i, model);
                                break;
                            }
                        }
                        mAdapter.notifyDataSetChanged();
                    } catch (Exception ex) {
                        Log.e("", ex.getMessage());
                    }
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Functions.printLog("", databaseError.getMessage());
            }
        };


        // this will check the two user are do chat before or not
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(senderId + "-" + receiverId)) {
                    pBar.setVisibility(View.GONE);
                    queryGetchat.removeEventListener(valueEventListener);
                } else {
                    pBar.setVisibility(View.GONE);
                    queryGetchat.removeEventListener(valueEventListener);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };


        //this will check the block status of user which is open the chat. to know either i am blocked or not
        // if i am block then the bottom Writechat layout will be invisible
        myInboxListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (isPrivacyfollow) {

                    if (dataSnapshot.exists() && dataSnapshot.child("block").getValue() != null) {
                        String block = dataSnapshot.child("block").getValue().toString();
                        if (block.equals("1")) {
                            tabChat.setVisibility(View.GONE);
                        } else {
                            tabChat.setVisibility(View.VISIBLE);
                        }
                    } else {
                        tabChat.setVisibility(View.VISIBLE);
                    }

                } else {
                    tabChat.setVisibility(View.GONE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        // this will check the block status of other user and according to them the block status dialog's option will be change
        otherInboxListener = new ValueEventListener() {
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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };


        queryGetchat.limitToLast(20).addChildEventListener(eventListener);
        mchatRefReteriving.child("chat").addValueEventListener(valueEventListener);

        myBlockStatusQuery.addValueEventListener(myInboxListener);
        otherBlockStatusQuery.addValueEventListener(otherInboxListener);
    }


    // this will add the new message in chat node and update the ChatInbox by new message by present date
    public void sendMessage(final String message) {
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
                receivermap.put("name", receiverName);
                receivermap.put("pic", receiverPic);
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

                        ChatA.sendPushNotification(ChatA.this, Functions.getSharedPreference(context).getString(Variables.U_NAME, ""), message,
                                receiverId, senderId);

                    }
                });
            }
        });
    }


    // this method will upload the image in chhat
    public void uploadImage(ByteArrayOutputStream byteArrayOutputStream) {
        byte[] data = byteArrayOutputStream.toByteArray();
        Date c = Calendar.getInstance().getTime();
        final String formattedDate = Variables.df.format(c);

        StorageReference reference = FirebaseStorage.getInstance().getReference();
        DatabaseReference dref = rootref.child("chat").child(senderId + "-" + receiverId).push();
        final String key = dref.getKey();
        uploadingImageId = key;
        final String current_user_ref = "chat" + "/" + senderId + "-" + receiverId;
        final String chat_user_ref = "chat" + "/" + receiverId + "-" + senderId;

        HashMap my_dummi_pic_map = new HashMap<>();
        my_dummi_pic_map.put("receiver_id", receiverId);
        my_dummi_pic_map.put("sender_id", senderId);
        my_dummi_pic_map.put("chat_id", key);
        my_dummi_pic_map.put("text", "");
        my_dummi_pic_map.put("type", "image");
        my_dummi_pic_map.put("pic_url", "none");
        my_dummi_pic_map.put("status", "0");
        my_dummi_pic_map.put("time", "");
        my_dummi_pic_map.put("sender_name", Functions.getSharedPreference(context).getString(Variables.U_NAME, ""));
        my_dummi_pic_map.put("timestamp", formattedDate);

        HashMap dummy_push = new HashMap<>();
        dummy_push.put(current_user_ref + "/" + key, my_dummi_pic_map);
        rootref.updateChildren(dummy_push);

        final StorageReference imagepath = reference.child("images").child(key + ".jpg");
        imagepath.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                imagepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        uploadingImageId = "none";
                        HashMap message_user_map = new HashMap<>();
                        message_user_map.put("receiver_id", receiverId);
                        message_user_map.put("sender_id", senderId);
                        message_user_map.put("chat_id", key);
                        message_user_map.put("text", "");
                        message_user_map.put("type", "image");
                        message_user_map.put("pic_url", uri.toString());
                        message_user_map.put("status", "0");
                        message_user_map.put("time", "");
                        message_user_map.put("sender_name", Functions.getSharedPreference(context).getString(Variables.U_NAME, ""));
                        message_user_map.put("timestamp", formattedDate);

                        HashMap user_map = new HashMap<>();

                        user_map.put(current_user_ref + "/" + key, message_user_map);
                        user_map.put(chat_user_ref + "/" + key, message_user_map);

                        rootref.updateChildren(user_map, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                String inbox_sender_ref = "Inbox" + "/" + senderId + "/" + receiverId;
                                String inbox_receiver_ref = "Inbox" + "/" + receiverId + "/" + senderId;

                                HashMap sendermap = new HashMap<>();
                                sendermap.put("rid", senderId);
                                sendermap.put("name", Functions.getSharedPreference(context).getString(Variables.U_NAME, ""));
                                sendermap.put("pic", Functions.getSharedPreference(context).getString(Variables.U_PIC, ""));
                                sendermap.put("msg", "Send an image...");
                                sendermap.put("status", "0");
                                sendermap.put("timestamp", -1 * System.currentTimeMillis());
                                sendermap.put("date", formattedDate);

                                HashMap receivermap = new HashMap<>();
                                receivermap.put("rid", receiverId);
                                receivermap.put("name", receiverName);
                                receivermap.put("pic", receiverPic);
                                receivermap.put("msg", "Send an image...");
                                receivermap.put("status", "1");
                                receivermap.put("timestamp", -1 * System.currentTimeMillis());
                                receivermap.put("date", formattedDate);

                                HashMap both_user_map = new HashMap<>();
                                both_user_map.put(inbox_sender_ref, receivermap);
                                both_user_map.put(inbox_receiver_ref, sendermap);

                                adduserInbox.updateChildren(both_user_map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        ChatA.sendPushNotification(ChatA.this, Functions.getSharedPreference(context).getString(Variables.U_NAME, ""), "Send an Image....",
                                                receiverId, senderId);

                                    }
                                });


                            }
                        });


                    }
                });

            }
        });
    }


    // this method will upload the image in chhat
    public void sendGif(String url) {
        Date c = Calendar.getInstance().getTime();
        final String formattedDate = Variables.df.format(c);


        DatabaseReference dref = rootref.child("chat").child(senderId + "-" + receiverId).push();
        final String key = dref.getKey();

        String current_user_ref = "chat" + "/" + senderId + "-" + receiverId;
        String chat_user_ref = "chat" + "/" + receiverId + "-" + senderId;

        HashMap message_user_map = new HashMap<>();
        message_user_map.put("receiver_id", receiverId);
        message_user_map.put("sender_id", senderId);
        message_user_map.put("chat_id", key);
        message_user_map.put("text", "");
        message_user_map.put("type", "gif");
        message_user_map.put("pic_url", url);
        message_user_map.put("status", "0");
        message_user_map.put("time", "");
        message_user_map.put("sender_name", Functions.getSharedPreference(context).getString(Variables.U_NAME, ""));
        message_user_map.put("timestamp", formattedDate);
        HashMap user_map = new HashMap<>();

        user_map.put(current_user_ref + "/" + key, message_user_map);
        user_map.put(chat_user_ref + "/" + key, message_user_map);

        rootref.updateChildren(user_map, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                String inbox_sender_ref = "Inbox" + "/" + senderId + "/" + receiverId;
                String inbox_receiver_ref = "Inbox" + "/" + receiverId + "/" + senderId;


                HashMap sendermap = new HashMap<>();
                sendermap.put("rid", senderId);
                sendermap.put("name", Functions.getSharedPreference(context).getString(Variables.U_NAME, ""));
                sendermap.put("pic", Functions.getSharedPreference(context).getString(Variables.U_PIC, ""));
                sendermap.put("msg", "Send an gif image...");
                sendermap.put("status", "0");
                sendermap.put("timestamp", -1 * System.currentTimeMillis());
                sendermap.put("date", formattedDate);

                HashMap receivermap = new HashMap<>();
                receivermap.put("rid", receiverId);
                receivermap.put("name", receiverName);
                receivermap.put("pic", receiverPic);
                receivermap.put("msg", "Send an gif image...");
                receivermap.put("status", "1");
                receivermap.put("timestamp", -1 * System.currentTimeMillis());
                receivermap.put("date", formattedDate);

                HashMap both_user_map = new HashMap<>();
                both_user_map.put(inbox_sender_ref, receivermap);
                both_user_map.put(inbox_receiver_ref, sendermap);

                adduserInbox.updateChildren(both_user_map).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        ChatA.sendPushNotification(ChatA.this, Functions.getSharedPreference(context).getString(Variables.U_NAME, ""), "Send an gif image....",
                                receiverId, senderId);

                    }
                });

            }
        });
    }


    // this method will change the status to ensure that
    // user is seen all the message or not (in both chat node and Chatinbox node)
    public void changeStatus() {
        final Date c = Calendar.getInstance().getTime();
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        final Query query1 = reference.child("chat").child(receiverId + "-" + senderId).orderByChild("status").equalTo("0");
        final Query query2 = reference.child("chat").child(senderId + "-" + receiverId).orderByChild("status").equalTo("0");

        final DatabaseReference inbox_change_status_1 = reference.child("Inbox").child(senderId + "/" + receiverId);
        final DatabaseReference inbox_change_status_2 = reference.child("Inbox").child(receiverId + "/" + senderId);

        query1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot nodeDataSnapshot : dataSnapshot.getChildren()) {
                    if (!nodeDataSnapshot.child("sender_id").getValue().equals(senderId)) {
                        String key = nodeDataSnapshot.getKey(); // this key is `K1NRz9l5PU_0CFDtgXz`
                        String path = "chat" + "/" + dataSnapshot.getKey() + "/" + key;
                        HashMap<String, Object> result = new HashMap<>();
                        result.put("status", "1");
                        result.put("time", Variables.df2.format(c));
                        reference.child(path).updateChildren(result);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        query2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot nodeDataSnapshot : dataSnapshot.getChildren()) {
                    if (!nodeDataSnapshot.child("sender_id").getValue().equals(senderId)) {
                        String key = nodeDataSnapshot.getKey(); // this key is `K1NRz9l5PU_0CFDtgXz`
                        String path = "chat" + "/" + dataSnapshot.getKey() + "/" + key;
                        HashMap<String, Object> result = new HashMap<>();
                        result.put("status", "1");
                        result.put("time", Variables.df2.format(c));
                        reference.child(path).updateChildren(result);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        inbox_change_status_1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.child("rid").getValue() != null && dataSnapshot.child("rid").getValue().equals(receiverId)) {
                        HashMap<String, Object> result = new HashMap<>();
                        result.put("status", "1");
                        inbox_change_status_1.updateChildren(result);

                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        inbox_change_status_2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.child("rid").getValue() != null && dataSnapshot.child("rid").getValue().equals(receiverId)) {
                        HashMap<String, Object> result = new HashMap<>();
                        result.put("status", "1");
                        inbox_change_status_2.updateChildren(result);

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    public void downloadAudio(final ProgressBar p_bar, ChatModel item) {
        p_bar.setVisibility(View.VISIBLE);
        PRDownloader.download(item.getPic_url(), direct.getPath(), item.getChat_id() + ".mp3")
                .build()
                .start(new OnDownloadListener() {
                    @Override
                    public void onDownloadComplete() {
                        p_bar.setVisibility(View.GONE);
                        mAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(Error error) {

                    }

                });

    }


    // this is the delete message diloge which will show after long press in chat message
    private void deleteMessage(final ChatModel chat_model) {

        final CharSequence[] options = {getString(R.string.delete_this_message), getString(R.string.cancel_)};

        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogCustom);

        builder.setTitle(null);

        builder.setItems(options, new DialogInterface.OnClickListener() {

            @Override

            public void onClick(DialogInterface dialog, int item) {

                if (options[item].equals(getString(R.string.delete_this_message))) {
                    updateMessage(chat_model);

                } else if (options[item].equals(getString(R.string.cancel_))) {

                    dialog.dismiss();

                }

            }

        });

        builder.show();

    }


    // we will update the privious message means we will tells the other user that we have seen your message
    public void updateMessage(ChatModel item) {
        final String current_user_ref = "chat" + "/" + senderId + "-" + receiverId;
        final String chat_user_ref = "chat" + "/" + receiverId + "-" + senderId;


        final HashMap message_user_map = new HashMap<>();
        message_user_map.put("receiver_id", item.getReceiver_id());
        message_user_map.put("sender_id", item.getSender_id());
        message_user_map.put("chat_id", item.getChat_id());
        message_user_map.put("text", "Delete this message");
        message_user_map.put("type", "delete");
        message_user_map.put("pic_url", "");
        message_user_map.put("status", "0");
        message_user_map.put("time", "");
        message_user_map.put("sender_name", Functions.getSharedPreference(context).getString(Variables.U_NAME, ""));
        message_user_map.put("timestamp", item.getTimestamp());

        final HashMap user_map = new HashMap<>();
        user_map.put(current_user_ref + "/" + item.getChat_id(), message_user_map);
        user_map.put(chat_user_ref + "/" + item.getChat_id(), message_user_map);

        rootref.updateChildren(user_map);

    }


    // this is the block dialog which will be show when user click on alert buttom of Top right in screen
    private void blockUserDialog() {
        final CharSequence[] options;
        if (isUserAlreadyBlock)
            options = new CharSequence[]{getString(R.string.unblock_this_user), getString(R.string.report_user), getString(R.string.cancel_)};
        else
            options = new CharSequence[]{getString(R.string.block_this_user), getString(R.string.report_user), getString(R.string.cancel_)};

        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogCustom);

        builder.setTitle(null);

        builder.setItems(options, new DialogInterface.OnClickListener() {

            @Override

            public void onClick(DialogInterface dialog, int item) {

                String text = (String) options[item];

                if (text.equals(getString(R.string.block_this_user))) {

                    blockUser();
                } else if (text.equals(getString(R.string.unblock_this_user))) {

                    unBlockUser();
                } else if (text.equalsIgnoreCase(getString(R.string.report_user))) {
                    openUserReport();
                } else if (options[item].equals(getString(R.string.cancel_))) {

                    dialog.dismiss();

                }

            }

        });

        builder.show();

    }


    public void blockUser() {
        rootref.child("Inbox")
                .child(receiverId)
                .child(Functions.getSharedPreference(ChatA.this).getString(Variables.U_ID, "0")).child("block").setValue("1");
        Functions.showToast(ChatA.this, getString(R.string.user_blocked));

    }

    public void unBlockUser() {
        rootref.child("Inbox")
                .child(receiverId)
                .child(Functions.getSharedPreference(ChatA.this).getString(Variables.U_ID, "0")).child("block").setValue("0");
        Functions.showToast(ChatA.this, getString(R.string.user_unblocked));

    }


    public void openUserReport() {
        onPause();
        Intent intent=new Intent(ChatA.this, ReportTypeA.class);
        intent.putExtra("user_id", receiverId);
        intent.putExtra("isFrom",false);
        startActivity(intent);
       overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top);
    }

    // we will delete only the today message so it is important to check the given message is the today message or not
    // if the given message is the today message then we will delete the message
    public boolean istodaymessage(String date) {
        Calendar cal = Calendar.getInstance();
        int today_day = cal.get(Calendar.DAY_OF_MONTH);
        //current date in millisecond
        long currenttime = System.currentTimeMillis();

        //database date in millisecond
        SimpleDateFormat f = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss",Locale.ENGLISH);
        long databasedate = 0;
        Date d = null;
        try {
            d = f.parse(date);
            databasedate = d.getTime();

        } catch (Exception e) {
            e.printStackTrace();
        }
        long difference = currenttime - databasedate;
        if (difference < 86400000) {
            int chatday = Functions.parseInterger(date.substring(0, 2));
            if (today_day == chatday)
                return true;
            else
                return false;
        }

        return false;
    }

    // this method will show the dialog of selete the either take a picture form camera or pick the image from gallary
    private void selectImage() {

        final CharSequence[] options = {getString(R.string.take_photo), getString(R.string.choose_from_gallery),getString(R.string.cancel_) };


        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogCustom);

        builder.setTitle(getString(R.string.add_photo_));

        builder.setItems(options, new DialogInterface.OnClickListener() {

            @Override

            public void onClick(DialogInterface dialog, int item) {

                if (options[item].equals(getString(R.string.take_photo))) {
                    openCameraIntent();
                } else if (options[item].equals(getString(R.string.choose_from_gallery))) {
                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    resultCallbackForGallery.launch(intent);
                } else if (options[item].equals(getString(R.string.cancel_))) {
                    dialog.dismiss();
                }

            }

        });

        builder.show();

    }


    ActivityResultLauncher<Intent> resultCallbackForGallery = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {

                        Uri selectedImage = result.getData().getData();
                        InputStream imageStream = null;
                        try {
                            imageStream = getContentResolver().openInputStream(selectedImage);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        final Bitmap imagebitmap = BitmapFactory.decodeStream(imageStream);

                        String path = getPath(selectedImage);
                        Matrix matrix = new Matrix();
                        ExifInterface exif = null;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                            try {
                                exif = new ExifInterface(path);
                                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
                                switch (orientation) {
                                    case ExifInterface.ORIENTATION_ROTATE_90:
                                        matrix.postRotate(90);
                                        break;
                                    case ExifInterface.ORIENTATION_ROTATE_180:
                                        matrix.postRotate(180);
                                        break;
                                    case ExifInterface.ORIENTATION_ROTATE_270:
                                        matrix.postRotate(270);
                                        break;
                                    default:
                                        break;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        Bitmap rotatedBitmap = Bitmap.createBitmap(imagebitmap, 0, 0, imagebitmap.getWidth(), imagebitmap.getHeight(), matrix, true);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
                        uploadImage(baos);

                    }
                }
            });

    ActivityResultLauncher<Intent> resultCallbackForCamera = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Matrix matrix = new Matrix();
                        try {
                            ExifInterface exif = new ExifInterface(imageFilePath);
                            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
                            switch (orientation) {
                                case ExifInterface.ORIENTATION_ROTATE_90:
                                    matrix.postRotate(90);
                                    break;
                                case ExifInterface.ORIENTATION_ROTATE_180:
                                    matrix.postRotate(180);
                                    break;
                                case ExifInterface.ORIENTATION_ROTATE_270:
                                    matrix.postRotate(270);
                                    break;
                                default:
                                    break;
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Uri selectedImage = (Uri.fromFile(new File(imageFilePath)));

                        InputStream imageStream = null;
                        try {
                            imageStream =getContentResolver().openInputStream(selectedImage);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                        final Bitmap imagebitmap = BitmapFactory.decodeStream(imageStream);
                        Bitmap rotatedBitmap = Bitmap.createBitmap(imagebitmap, 0, 0, imagebitmap.getWidth(), imagebitmap.getHeight(), matrix, true);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
                        uploadImage(baos);
                    }
                }
            });






    // below three method is related with taking the picture from camera
    private void openCameraIntent() {
        Intent pictureIntent = new Intent(
                MediaStore.ACTION_IMAGE_CAPTURE);
        if (pictureIntent.resolveActivity(getPackageManager()) != null) {
            //Create a file to store the image
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (Exception ex) {
                // Error occurred while creating the File

            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(context.getApplicationContext(), getPackageName() + ".fileprovider", photoFile);
                pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                resultCallbackForCamera.launch(pictureIntent);
            }
        }
    }

    String imageFilePath;
    private File createImageFile() throws Exception {
        String timeStamp =
                new SimpleDateFormat("yyyyMMdd_HHmmss",
                        Locale.ENGLISH).format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir =
               getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        imageFilePath = image.getAbsolutePath();
        return image;
    }


    public String getPath(Uri uri) {
        String result = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri, proj, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int column_index = cursor.getColumnIndexOrThrow(proj[0]);
                result = cursor.getString(column_index);
            }
            cursor.close();
        }
        if (result == null) {
            result = "Not found";
        }
        return result;
    }


    // send the type indicator if the user is typing message
    public void sendTypingIndicator(boolean indicate) {
        // if the type incator is present then we remove it if not then we create the typing indicator
        if (indicate) {
            final HashMap message_user_map = new HashMap<>();
            message_user_map.put("receiver_id", receiverId);
            message_user_map.put("sender_id", senderId);

            sendTypingIndication = FirebaseDatabase.getInstance().getReference().child("typing_indicator");
            sendTypingIndication.child(senderId + "-" + receiverId).setValue(message_user_map).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    sendTypingIndication.child(receiverId + "-" + senderId).setValue(message_user_map);
                }
            });
        } else {
            sendTypingIndication = FirebaseDatabase.getInstance().getReference().child("typing_indicator");

            sendTypingIndication.child(senderId + "-" + receiverId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    sendTypingIndication.child(receiverId + "-" + senderId).removeValue();

                }
            });

        }

    }


    // receive the type indication to show that your friend is typing or not
    LinearLayout mainlayout;

    public void receiveTypeIndication() {
        mainlayout = findViewById(R.id.typeindicator);

        receiveTypingIndication = FirebaseDatabase.getInstance().getReference().child("typing_indicator");
        receiveTypingIndication.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(receiverId + "-" + senderId).exists()) {
                    String receiver = String.valueOf(dataSnapshot.child(receiverId + "-" + senderId).child("sender_id").getValue());
                    if (receiver.equals(receiverId)) {
                        mainlayout.setVisibility(View.VISIBLE);
                    }
                } else {
                    mainlayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }



    public void openVideo(ChatModel item){

        Intent intent = new Intent(ChatA.this, WatchVideosA.class);
        intent.putExtra("video_id", item.video_id);
        intent.putExtra("position", 0);
        intent.putExtra("pageCount", 0);
        intent.putExtra("userId",Functions.getSharedPreference(ChatA.this).getString(Variables.U_ID,""));
        intent.putExtra("whereFrom","IdVideo");
        startActivity(intent);
    }


    // on destory delete the typing indicator
    @Override
    public void onDestroy() {
        uploadingImageId = "none";
        senderidForCheckNotification = "";
        sendTypingIndicator(false);
        queryGetchat.removeEventListener(eventListener);
        myBlockStatusQuery.removeEventListener(myInboxListener);
        otherBlockStatusQuery.removeEventListener(otherInboxListener);

        mPermissionCameraStorageResult.unregister();
        mPermissionStorageRecordingResult.unregister();

        Functions.hideSoftKeyboard(ChatA.this);
        super.onDestroy();
    }


    //this method will get the big size of image in private chat
    public void openFullSizeImage(ChatModel item) {
        Intent intent=new Intent(ChatA.this, SeeFullImageA.class);
        intent.putExtra("image_url", item.getPic_url());
        intent.putExtra("chat_id", item.getChat_id());
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

    }


    // this is related with the list of Gifs that is show in the list below
    GifAdapter gif_adapter;
    final ArrayList<String> url_list = new ArrayList<>();
    RecyclerView gips_list;
    GPHApi client;

    public void getGipy() {
        url_list.clear();
        gips_list = findViewById(R.id.gif_recylerview);
        gips_list.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        gif_adapter = new GifAdapter(context, url_list, new GifAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String item) {
                sendGif(item);
                slideDown();
            }
        });
        gips_list.setAdapter(gif_adapter);

        client.trending(MediaType.gif, null, null, null, new CompletionHandler<ListMediaResponse>() {
            @Override
            public void onComplete(ListMediaResponse result, Throwable e) {
                if (result == null) {
                    // Do what you want to do with the error
                } else {
                    if (result.getData() != null) {
                        for (Media gif : result.getData()) {

                            url_list.add(gif.getId());
                        }
                        gif_adapter.notifyDataSetChanged();

                    } else {
                        Log.e("giphy error", "No results found");
                    }
                }
            }
        });
    }


    // if we want to search the gif then this mehtod is immportaant
    public void searchGif(String search) {
        /// Gif Search
        client.search(search, MediaType.gif, null, null, null, null, new CompletionHandler<ListMediaResponse>() {
            @Override
            public void onComplete(ListMediaResponse result, Throwable e) {
                if (result == null) {
                    // Do what you want to do with the error
                } else {
                    if (result.getData() != null) {
                        url_list.clear();
                        for (Media gif : result.getData()) {
                            url_list.add(gif.getId());
                            gif_adapter.notifyDataSetChanged();
                        }
                        gips_list.smoothScrollToPosition(0);

                    } else {
                        Log.e("giphy error", "No results found");
                    }
                }
            }
        });
    }


    // slide the view from below itself to the current position
    public void slideUp() {
        message.setHint(getString(R.string.search_gift));
        uploadGifBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_gif_image));
        gifLayout.setVisibility(View.VISIBLE);
        sendbtn.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_search));
        TranslateAnimation animate = new TranslateAnimation(
                0,
                0,
                chatMainView.getHeight(),
                0);
        animate.setDuration(700);
        animate.setFillAfter(true);
        gifLayout.startAnimation(animate);
    }


    // slide the view from its current position to below itself
    public void slideDown() {
        message.setHint(getString(R.string.type_your_message_here_));
        message.setText("");
        uploadGifBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_gif_image_gray));
        sendbtn.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_send));
        TranslateAnimation animate = new TranslateAnimation(
                0,                 // fromXDelta
                0,                 // toXDelta
                0,                 // fromYDelta
                chatMainView.getHeight()); // toYDelta
        animate.setDuration(700);
        animate.setFillAfter(true);
        animate.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                gifLayout.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        gifLayout.startAnimation(animate);
    }


    // this mehtos the will add a node of notification in to database
    // then our firebase cloud function will listen node and send the notification to spacific user
    public static void sendPushNotification(Activity context,
                                            String name, String message,
                                            String receiverid, String senderid) {

        if (pushNotificationSetting_model != null && pushNotificationSetting_model.getDirectmessage().equalsIgnoreCase("1")) {

            JSONObject notimap = new JSONObject();
            try {
                notimap.put("title", name);
                notimap.put("message", message);
                notimap.put("sender_id", senderid);
                notimap.put("receiver_id", receiverid);
            } catch (Exception e) {
                e.printStackTrace();
            }

            VolleyRequest.JsonPostRequest(context, ApiLinks.sendPushNotification, notimap, Functions.getHeaders(context),null);
        }
    }


    // this will get the user data and parse the data and show the data into views
    public void callApiForUserDetails() {
        Functions.callApiForGetUserData(ChatA.this,
                receiverId,
                new APICallBack() {
                    @Override
                    public void arrayData(ArrayList arrayList) {

                    }

                    @Override
                    public void onSuccess(String responce) {
                        parseUserData(responce);
                    }

                    @Override
                    public void onFail(String responce) {

                    }
                });
    }


    public static PushNotificationSettingModel pushNotificationSetting_model;
    PrivacyPolicySettingModel privacyPolicySetting_model;

    public void parseUserData(String responce) {
        try {
            JSONObject jsonObject = new JSONObject(responce);

            String code = jsonObject.optString("code");

            if (code.equals("200")) {
                JSONObject msg = jsonObject.optJSONObject("msg");

                JSONObject push_notification_setting = msg.optJSONObject("PushNotification");
                JSONObject privacy_policy_setting = msg.optJSONObject("PrivacySetting");
                UserModel userDetailModel= DataParsing.getUserDataModel(msg.optJSONObject("User"));


                receiverName = userDetailModel.getUsername();
                userName.setText(receiverName);

                // these two method will get other datial of user like there profile pic link and username
                receiverPic = userDetailModel.getProfilePic();
                if (!receiverPic.contains(Variables.http)) {
                    receiverPic = Constants.BASE_URL + receiverPic;
                }
                if (receiverPic != null && !receiverPic.equalsIgnoreCase("")) {
                    Uri uri = Uri.parse(receiverPic);
                    profileimage.setImageURI(uri);
                }


                pushNotificationSetting_model = new PushNotificationSettingModel();
                pushNotificationSetting_model.setDirectmessage("" + push_notification_setting.optString("direct_messages"));

                privacyPolicySetting_model = new PrivacyPolicySettingModel();
                privacyPolicySetting_model.setDirect_message("" + privacy_policy_setting.optString("direct_message"));


                isPrivacyfollow = true;
                tabChat.setVisibility(View.VISIBLE);

                if (Functions.isShowContentPrivacy(context, privacyPolicySetting_model.getDirect_message(),
                        userDetailModel.getButton().toLowerCase().equalsIgnoreCase("friends"))) {
                    isPrivacyfollow = true;
                    tabChat.setVisibility(View.VISIBLE);
                } else {
                    isPrivacyfollow = false;
                    tabChat.setVisibility(View.GONE);
                }

            } else {
                Functions.showToast(ChatA.this, jsonObject.optString("msg"));

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void playAudio(int postion, ChatModel item) {

        audioPostion = postion;
        mediaPlayerProgress = 0;

        stopPlaying();

        File fullpath = new File(Functions.getAppFolder(ChatA.this) + item.chat_id + ".mp3");
        if (fullpath.exists()) {
            Uri uri = Uri.parse(fullpath.getAbsolutePath());

            mediaPlayer = MediaPlayer.create(context, uri);

            if (mediaPlayer != null) {
                mediaPlayer.start();
                countdownTimer(true);

                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        stopPlaying();
                    }
                });
                playingId = item.chat_id;
                mAdapter.notifyDataSetChanged();
            }

        }
    }

    public void stopPlaying() {
        playingId = "none";
        countdownTimer(false);
        mAdapter.notifyDataSetChanged();
        if (mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    CountDownTimer countDownTimer;

    public void countdownTimer(boolean starttimer) {

        if (countDownTimer != null)
            countDownTimer.cancel();


        if (starttimer) {
            countDownTimer = new CountDownTimer(mediaPlayer.getDuration(), 300) {
                @Override
                public void onTick(long millisUntilFinished) {

                    mediaPlayerProgress = ((mediaPlayer.getCurrentPosition() * 100) / mediaPlayer.getDuration());
                    if (mediaPlayerProgress > 95) {
                        countdownTimer(false);
                        mediaPlayerProgress = 0;
                    }
                    mAdapter.notifyItemChanged(audioPostion);
                }

                @Override
                public void onFinish() {
                    mediaPlayerProgress = 0;
                    countdownTimer(false);
                    mAdapter.notifyItemChanged(audioPostion);
                }
            };
            countDownTimer.start();


        }

    }


    private ActivityResultLauncher<String[]> mPermissionCameraStorageResult = registerForActivityResult(
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
                            blockPermissionCheck.add(Functions.getPermissionStatus(ChatA.this,key));
                        }
                    }
                    if (blockPermissionCheck.contains("blocked"))
                    {
                        Functions.showPermissionSetting(ChatA.this,getString(R.string.we_need_storage_permission_for_upload_media_file));
                    }
                    else
                    if (allPermissionClear)
                    {
                        selectImage();
                    }

                }
            });




    private ActivityResultLauncher<String[]> mPermissionStorageRecordingResult = registerForActivityResult(
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
                            blockPermissionCheck.add(Functions.getPermissionStatus(ChatA.this,key));
                        }
                    }
                    if (blockPermissionCheck.contains("blocked"))
                    {
                        Functions.showPermissionSetting(ChatA.this,getString(R.string.we_need_recording_permission_for_upload_sound));
                    }
                    else
                    if (allPermissionClear)
                    {
                        if (audioPermissionCheck.equalsIgnoreCase("playing"))
                        {
                            audioPlaying(selectedAudioView,selectedChatModel,selectedAudioPosition);
                        }
                        else
                        {
                            sendAudio.startRecording();
                        }
                    }

                }
            });



}
