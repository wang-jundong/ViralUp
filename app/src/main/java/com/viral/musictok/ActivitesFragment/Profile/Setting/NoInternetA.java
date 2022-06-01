package com.viral.musictok.ActivitesFragment.Profile.Setting;

import com.viral.musictok.SimpleClasses.AppCompatLocaleActivity;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.TextView;

import com.viral.musictok.Constants;
import com.viral.musictok.Interfaces.InternetCheckCallback;
import com.viral.musictok.R;
import com.viral.musictok.SimpleClasses.Functions;
import com.viral.musictok.SimpleClasses.Variables;

import java.util.Locale;

public class NoInternetA extends AppCompatLocaleActivity {

    TextView tvOpenSetting;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Functions.setLocale(Functions.getSharedPreference(this).getString(Variables.APP_LANGUAGE_CODE,Variables.DEFAULT_LANGUAGE_CODE)
                , this, NoInternetA.class,false);
        setContentView(R.layout.activity_no_internet);
        InitControl();
    }

    private void InitControl() {
        tvOpenSetting=findViewById(R.id.tvOpenSetting);
        tvOpenSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              try {
                  startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
              }
              catch (Exception e)
              {
                  Functions.printLog(Constants.tag,"Exception : "+e);
              }
            }
        });

        Functions.RegisterConnectivity(NoInternetA.this, new InternetCheckCallback() {
            @Override
            public void GetResponse(String requestType, String response) {
                if(response.equalsIgnoreCase("connected")) {
                    Intent intent = new Intent();
                    intent.putExtra("isShow", true);
                    setResult(RESULT_OK, intent);
                    finish();
                    overridePendingTransition(R.anim.in_from_top,R.anim.out_from_bottom);
                }
            }
        });
    }

    public void setLocale(String lang) {
        Locale myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = new Configuration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
        onConfigurationChanged(conf);
    }

}