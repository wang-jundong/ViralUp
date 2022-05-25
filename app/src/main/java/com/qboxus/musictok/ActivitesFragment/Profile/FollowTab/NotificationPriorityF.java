package com.qboxus.musictok.ActivitesFragment.Profile.FollowTab;

import android.os.Bundle;

import androidx.core.content.ContextCompat;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.qboxus.musictok.ApiClasses.ApiLinks;
import com.volley.plus.VPackages.VolleyRequest;
import com.qboxus.musictok.Constants;
import com.volley.plus.interfaces.APICallBack;
import com.volley.plus.interfaces.Callback;
import com.qboxus.musictok.Interfaces.FragmentCallBack;
import com.qboxus.musictok.Models.UserModel;
import com.qboxus.musictok.R;
import com.qboxus.musictok.SimpleClasses.DataParsing;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.Variables;

import org.json.JSONObject;

import java.util.ArrayList;


public class NotificationPriorityF extends BottomSheetDialogFragment implements View.OnClickListener {

    private View view;
    FragmentCallBack callback;
    ImageView ivClose;
    RelativeLayout tabLiveNotification,tabMuteNotification;
    String notificationType,userName,userId;
    ImageView ivLiveTick,ivMuteTick;
    TextView tvTitle,tvMessage,tvFollowBtn;
    boolean isFriend;
    RelativeLayout tabshowNotification,tabshowProfile;

    public NotificationPriorityF() {
    }

    public NotificationPriorityF(String notificationType,boolean isFriend,String userName,String userId, FragmentCallBack callback) {
        this.callback = callback;
        this.notificationType=notificationType;
        this.isFriend=isFriend;
        this.userName=userName;
        this.userId=userId;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view= inflater.inflate(R.layout.fragment_notification_priority, container, false);
        return init();
    }

    private View init() {
        ivClose=view.findViewById(R.id.ivClose);
        ivClose.setOnClickListener(this);


        tabshowNotification=view.findViewById(R.id.tabshowNotification);
        tabshowProfile=view.findViewById(R.id.tabshowProfile);

        ivLiveTick=view.findViewById(R.id.iv_live_tick);
        ivMuteTick=view.findViewById(R.id.ivMuteNick);


        tabLiveNotification=view.findViewById(R.id.tabLiveNotification);
        tabLiveNotification.setOnClickListener(this);

        tabMuteNotification=view.findViewById(R.id.tabMuteNotification);
        tabMuteNotification.setOnClickListener(this);

        tvTitle=view.findViewById(R.id.tvTitle);
        tvMessage=view.findViewById(R.id.tvMessage);

        tvFollowBtn=view.findViewById(R.id.tvFollowBtn);
        tvFollowBtn.setOnClickListener(this);



        if (isFriend)
        {
            tvTitle.setText(view.getContext().getString(R.string.live_notification_settings));
            tabshowNotification.animate().alpha(1).setDuration(400).start();
            tabshowProfile.animate().alpha(0).setDuration(400).start();
            tabSlection(notificationType);
        }
        else
        {
            tvTitle.setText(userName);
            tvMessage.setText(view.getContext().getString(R.string.follow)+" "+userName+
                    " "+view.getContext().getString(R.string.to_set_up_live_notifications));
            tabshowNotification.animate().alpha(0).setDuration(400).start();
            tabshowProfile.animate().alpha(1).setDuration(400).start();
            tabSlection(notificationType);
        }

        return view;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.ivClose:
                dismiss();
                break;
            case R.id.tabLiveNotification:
                selectNotificationType("1");
                break;
            case R.id.tabMuteNotification:
                selectNotificationType("0");
                break;
            case R.id.tvFollowBtn:
                followUnFollowUser(userId);
                break;
        }
    }


    public void followUnFollowUser(String fb_id) {

        Functions.callApiForFollowUnFollow(getActivity(),
                Functions.getSharedPreference(view.getContext()).getString(Variables.U_ID, ""),
                fb_id,
                new APICallBack() {
                    @Override
                    public void arrayData(ArrayList arrayList) {


                    }

                    @Override
                    public void onSuccess(String responce) {
                        Log.d(Constants.tag,"Response : "+responce);


                        try {
                            JSONObject jsonObject=new JSONObject(responce);
                            String code=jsonObject.optString("code");
                            if(code.equalsIgnoreCase("200")){
                                JSONObject msg=jsonObject.optJSONObject("msg");
                                if(msg!=null){
                                    UserModel userDetailModel= DataParsing.getUserDataModel(msg.optJSONObject("User"));
                                    if(!(TextUtils.isEmpty(userDetailModel.getId()))){
                                        String userStatus=userDetailModel.getButton().toLowerCase();

                                        if (Functions.isNotificaitonShow(userStatus))
                                        {
                                            Bundle bundle=new Bundle();
                                            bundle.putBoolean("isShow",false);
                                            bundle.putString("type",notificationType);
                                            callback.onResponce(bundle);

                                            tabshowNotification.animate().alpha(1).setDuration(400).start();
                                            tabshowProfile.animate().alpha(0).setDuration(400).start();
                                        }

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




    private void selectNotificationType(String type) {

        JSONObject parameters = new JSONObject();
        try {
            parameters.put("sender_id", Functions.getSharedPreference(view.getContext()).getString(Variables.U_ID, ""));
            parameters.put("receiver_id", userId);
            parameters.put("notification", type);
        } catch (Exception e) {
            e.printStackTrace();
        }
        VolleyRequest.JsonPostRequest(getActivity(), ApiLinks.followerNotification, parameters,Functions.getHeaders(getActivity()), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.checkStatus(getActivity(),resp);
              if (resp!=null)
              {
                  try {
                      JSONObject jsonObject = new JSONObject(resp);
                      String code = jsonObject.optString("code");
                      if (code.equals("200")) {

                          tabSlection(type);
                          Bundle bundle=new Bundle();
                          bundle.putBoolean("isShow",true);
                          bundle.putString("type",type);
                          callback.onResponce(bundle);
                          dismiss();

                      }
                  }
                  catch (Exception e)
                  {
                      Log.d(Constants.tag,"Exception : "+e);
                  }
              }
            }
        });




    }

    private void tabSlection(String type) {
        if (type.equalsIgnoreCase("1"))
        {
            ivLiveTick.setImageDrawable(ContextCompat.getDrawable(view.getContext(),R.drawable.ic_circle_selection));
            ivMuteTick.setImageDrawable(ContextCompat.getDrawable(view.getContext(),R.drawable.ic_circle_stroke));
        }
        else
        if (type.equalsIgnoreCase("0"))
        {
            ivLiveTick.setImageDrawable(ContextCompat.getDrawable(view.getContext(),R.drawable.ic_circle_stroke));
            ivMuteTick.setImageDrawable(ContextCompat.getDrawable(view.getContext(),R.drawable.ic_circle_selection));
        }

    }


}