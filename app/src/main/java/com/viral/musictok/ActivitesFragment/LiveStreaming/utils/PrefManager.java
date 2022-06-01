package com.viral.musictok.ActivitesFragment.LiveStreaming.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.viral.musictok.ActivitesFragment.LiveStreaming.Constants;


public class PrefManager {
    public static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
    }
}
