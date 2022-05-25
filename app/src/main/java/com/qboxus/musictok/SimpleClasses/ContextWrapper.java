package com.qboxus.musictok.SimpleClasses;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;

import java.util.Locale;

public class ContextWrapper extends android.content.ContextWrapper {

    public ContextWrapper(Context base) {
        super(base);
    }

    public static ContextWrapper wrap(Context context, Locale newLocale) {
        Resources res = context.getResources();
        Configuration configuration = res.getConfiguration();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocale(newLocale);

            LocaleList localeList = null;
            localeList = new LocaleList(newLocale);

//            LocaleList.setDefault(localeList);
            configuration.setLocales(localeList);

            context = context.createConfigurationContext(configuration);

        }

        return new ContextWrapper(context);
    }
}