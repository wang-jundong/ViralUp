package com.qboxus.musictok.ActivitesFragment;

import com.qboxus.musictok.SimpleClasses.AppCompatLocaleActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.qboxus.musictok.ActivitesFragment.Profile.Setting.NoInternetA;
import com.qboxus.musictok.Constants;
import com.qboxus.musictok.Interfaces.InternetCheckCallback;
import com.qboxus.musictok.R;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.Variables;

public class WebviewA extends AppCompatLocaleActivity implements View.OnClickListener{



    ProgressBar progressBar;
    WebView webView;
    String url = "www.google.com";
    String title;
    TextView titleTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Functions.setLocale(Functions.getSharedPreference(WebviewA.this).getString(Variables.APP_LANGUAGE_CODE,Variables.DEFAULT_LANGUAGE_CODE)
                , this, WebviewA.class,false);
        setContentView(R.layout.activity_webview);

        url = getIntent().getStringExtra("url");
        title = getIntent().getStringExtra("title");
        if (title.equals(getString(R.string.promote_video))) {
            findViewById(R.id.toolbar).setVisibility(View.GONE);
        }

        Functions.printLog(Constants.tag,url);


        findViewById(R.id.goBack).setOnClickListener(this::onClick);

        titleTxt = findViewById(R.id.title_txt);
        titleTxt.setText(title);

        webView = findViewById(R.id.webview);
        progressBar = findViewById(R.id.progress_bar);
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int progress) {
                if (progress >= 80) {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });


        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(url);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                if (url.equalsIgnoreCase("closePopup")) {
                    WebviewA.super.onBackPressed();
                }
                return false;
            }
        });

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.goBack:
                WebviewA.super.onBackPressed();
                break;
        }
    }

}
