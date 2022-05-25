package com.qboxus.musictok.ActivitesFragment;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import com.qboxus.musictok.SimpleClasses.AppCompatLocaleActivity;

import com.qboxus.musictok.Constants;
import com.qboxus.musictok.R;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.volley.plus.VPackages.VolleyRequest;
import com.volley.plus.interfaces.Callback;

import org.json.JSONObject;

import java.util.HashMap;
import cat.ereza.customactivityoncrash.CustomActivityOnCrash;
import cat.ereza.customactivityoncrash.config.CaocConfig;

public class CustomErrorActivity extends AppCompatLocaleActivity implements View.OnClickListener {

    String pacakgeName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_error);
         pacakgeName = getApplicationContext().getPackageName();

        Button restartButton = findViewById(R.id.restart_button);

        final CaocConfig config = CustomActivityOnCrash.getConfigFromIntent(getIntent());

        if (config == null) {
            finish();
            return;
        }

        if (config.isShowRestartButton() && config.getRestartActivityClass() != null) {
            restartButton.setText(R.string.restart_app);
            restartButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(CustomErrorActivity.this, SplashA.class));
                    finish();
                }
            });
        }
        else {
            restartButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CustomActivityOnCrash.closeApplication(CustomErrorActivity.this, config);
                }
            });
        }


        findViewById(R.id.detail_button).setOnClickListener(this::onClick);

        showSendReport();
    }


    @Override
    public void onClick(View v) {

        switch (v.getId()){

            case R.id.detail_button:
                showAlert();
                break;
        }
    }


    public void showAlert(){
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.customactivityoncrash_error_activity_error_details_title)
                .setMessage(CustomActivityOnCrash.getAllErrorDetailsFromIntent(CustomErrorActivity.this, getIntent()))
                .setPositiveButton(R.string.customactivityoncrash_error_activity_error_details_close, null)
                .setNeutralButton(R.string.customactivityoncrash_error_activity_error_details_copy,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                copyErrorToClipboard();
                            }
                        })
                .show();
    }


    private void copyErrorToClipboard() {
        String errorInformation = CustomActivityOnCrash.getAllErrorDetailsFromIntent(CustomErrorActivity.this, getIntent());
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        //Are there any devices without clipboard...?
        if (clipboard != null) {
            ClipData clip = ClipData.newPlainText(getString(R.string.customactivityoncrash_error_activity_error_details_clipboard_label), errorInformation);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(CustomErrorActivity.this, R.string.customactivityoncrash_error_activity_error_details_copied, Toast.LENGTH_SHORT).show();
        }
    }




    public void showSendReport(){
        Button sendReposrt = findViewById(R.id.send_reposrt);

        if (pacakgeName.contains("qboxus")) {
            sendReposrt.setVisibility(View.VISIBLE);
            sendReposrt.setOnClickListener(this::onClick);
        } else {
            sendReposrt.setVisibility(View.GONE);
        }
    }

}