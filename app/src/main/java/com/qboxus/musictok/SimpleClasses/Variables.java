package com.qboxus.musictok.SimpleClasses;

import android.content.SharedPreferences;
import android.os.Environment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by qboxus on 2/15/2019.
 */

public class Variables {


    public Variables() {
    }

    public static final String DEVICE = "android";
    public static final String SelectedAudio_AAC = "SelectedAudio.aac";


    public static final String APP_HIDED_FOLDER = "HidedTicTic/";
    public static final String DRAFT_APP_FOLDER = "Draft/";

    public static final String onlineUser = "OnlineUsers";

    public static String is_puchase = "is_puchase";
    public static String output_frontcamera = APP_HIDED_FOLDER + "output_frontcamera.mp4";
    public static String outputfile = APP_HIDED_FOLDER + "output.mp4";
    public static String outputfile2 = APP_HIDED_FOLDER + "output2.mp4";
    public static String output_filter_file = APP_HIDED_FOLDER + "output-filtered.mp4";
    public static String gallery_trimed_video = APP_HIDED_FOLDER + "gallery_trimed_video.mp4";
    public static String gallery_resize_video = APP_HIDED_FOLDER + "gallery_resize_video.mp4";


    public static SharedPreferences sharedPreferences;
    public static final String PREF_NAME = "pref_name";
    public static final String U_ID = "u_id";
    public static final String U_WALLET = "u_wallet";
    public static final String U_PAYOUT_ID = "u_payout_id";
    public static final String U_NAME = "u_name";
    public static final String U_PHONE_NO = "Phone_No";
    public static final String U_EMAIL = "User_Email";
    public static final String U_SOCIAL_ID = "Social_Id";
    public static final String IS_VERIFIED = "is_verified";
    public static final String IS_VERIFICATION_APPLY = "is_verification_apply";
    public static final String U_PIC = "u_pic";
    public static final String F_NAME = "f_name";
    public static final String L_NAME = "l_name";
    public static final String GENDER = "u_gender";
    public static final String U_BIO = "U_bio";
    public static final String U_LINK = "U_link";
    public static final String IS_LOGIN = "is_login";
    public static final String DEVICE_TOKEN = "device_token";
    public static final String DEVICE_IP = "device_ip";
    public static final String AUTH_TOKEN = "api_token";
    public static final String DEVICE_ID = "device_id";
    public static final String UPLOADING_VIDEO_THUMB = "uploading_video_thumb";
    public static final String APP_LANGUAGE = "app_language";
    public static final String APP_LANGUAGE_CODE = "app_language_code";
    public static final String DEFAULT_LANGUAGE_CODE = "en";
    public static final String DEFAULT_LANGUAGE = "English";


    public static final String IsExtended = "IsExtended";
    //Paper DB collection
    public static final String MultiAccountKey = "Accounts";
    public static final String PrivacySetting = "Setting";
    public static final String PrivacySettingModel = "PrivacySettingModel";
    public static final String PushSettingModel = "PushSettingModel";
    public static final String PromoAds = "Promo";
    public static final String PromoAdsModel = "ads";


    public static String selectedSoundId = "null";
    public static boolean reloadMyVideos = false;
    public static boolean reloadMyVideosInner = false;
    public static boolean reloadMyLikesInner = false;
    public static boolean reloadMyNotification = false;


    public static final String GIF_FIRSTPART = "https://media.giphy.com/media/";
    public static final String GIF_SECONDPART = "/100w.gif";


    public static final String http = "http";

    public static final SimpleDateFormat df =
            new SimpleDateFormat("dd-MM-yyyy HH:mm:ssZZ", Locale.ENGLISH);

    public static final SimpleDateFormat df2 =
            new SimpleDateFormat("dd-MM-yyyy HH:mmZZ", Locale.ENGLISH);


}
