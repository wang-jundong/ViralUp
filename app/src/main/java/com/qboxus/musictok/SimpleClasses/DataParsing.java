package com.qboxus.musictok.SimpleClasses;

import android.text.TextUtils;
import android.util.Log;

import com.qboxus.musictok.Constants;
import com.qboxus.musictok.Models.UserModel;

import org.json.JSONObject;

public class DataParsing {


    public static UserModel getUserDataModel(JSONObject user)
    {

        UserModel model=new UserModel();
        try {

            model.setId(user.optString("id"));
            model.setFirstName(user.optString("first_name"));
            model.setLastName(user.optString("last_name"));
            model.setGender(user.optString("gender"));
            model.setBio(user.optString("bio"));
            model.setWebsite(user.optString("website"));
            model.setDob(user.optString("dob"));
            model.setSocial_id(user.optString("social_id"));
            model.setEmail(user.optString("email"));
            model.setPhone(user.optString("phone"));
            model.setPassword(user.optString("password"));
            if (TextUtils.isEmpty(user.optString("profile_pic_small")))
            {
                model.setProfilePic(user.optString("profile_pic"));
            }
            else
            {
                model.setProfilePic(user.optString("profile_pic_small"));
            }
            model.setRole(user.optString("role"));
            model.setUsername(user.optString("username"));
            model.setSocialType(user.optString("social"));
            model.setDeviceToken(user.optString("device_token"));
            model.setToken(user.optString("token"));
            model.setActive(user.optString("active"));
            model.setLat(user.optDouble("lat",0));
            model.setLng(user.optDouble("long",0));
            model.setOnline(user.optString("online"));
            model.setVerified(user.optString("verified"));
            model.setApplyVerification(user.optString("verification_applied"));
            model.setAuthToken(user.optString("auth_token"));
            model.setVersion(user.optString("version"));
            model.setDevice(user.optString("device"));
            model.setIp(user.optString("ip"));
            model.setCity(user.optString("city"));
            model.setCountry(user.optString("country"));
            model.setCityId(user.optString("city_id"));
            model.setStateId(user.optString("state_id"));
            model.setCountryId(user.optString("country_id"));
            model.setWallet(user.optLong("wallet",0));
            model.setPaypal(user.optString("paypal"));
            model.setResetWalletDatetime(user.optString("reset_wallet_datetime"));
            model.setFbId(user.optString("fb_id"));
            model.setCreated(user.optString("created"));
            model.setFollowersCount(user.optString("followers_count","0"));
            model.setFollowingCount(user.optString("following_count","0"));
            model.setLikesCount(user.optString("likes_count","0"));
            model.setVideoCount(user.optString("video_count","0"));
            model.setNotification(user.optString("notification","1"));
            model.setButton(user.optString("button"));
            model.setBlock(user.optString("block"));
            //you are only blocked by yourself
            try {
                JSONObject blockObj=user.getJSONObject("BlockUser");
                model.setBlockByUser(blockObj.optString("user_id","0"));
            }catch (Exception e){
                model.setBlockByUser(user.optString("id"));
            }

        }
        catch (Exception e)
        {
            Log.d(Constants.tag,"Exception : "+e);
        }

        return model;
    }
}
