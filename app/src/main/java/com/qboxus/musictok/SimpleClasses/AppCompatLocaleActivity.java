package com.qboxus.musictok.SimpleClasses;

import android.content.Context;
import android.os.Build;

import androidx.appcompat.app.AppCompatActivity;

import com.qboxus.musictok.SimpleClasses.AppCompatLocaleActivity;

import com.qboxus.musictok.R;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class AppCompatLocaleActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {

        String[] languageArray=newBase.getResources().getStringArray(R.array.app_language_code);

        List<String> languageCode = Arrays.asList(languageArray);

        String language = Functions.getSharedPreference(newBase).getString(Variables.APP_LANGUAGE_CODE, Variables.DEFAULT_LANGUAGE_CODE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && languageCode.contains(language)) {
            Locale newLocale = new Locale(language);
            super.attachBaseContext(ContextWrapper.wrap(newBase, newLocale));
        } else {
            super.attachBaseContext(newBase);
        }

    }
}
