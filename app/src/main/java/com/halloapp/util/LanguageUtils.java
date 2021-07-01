package com.halloapp.util;

import android.content.res.Configuration;
import android.os.Build;

import com.halloapp.AppContext;

import java.util.Locale;

public class LanguageUtils {
    public static String getLocaleIdentifier() {
        Configuration configuration = AppContext.getInstance().get().getResources().getConfiguration();
        Locale locale;
        if (Build.VERSION.SDK_INT >= 24) {
            locale = configuration.getLocales().get(0);
        } else {
            locale = configuration.locale;
        }
        String language = locale.getLanguage();
        if ("en".equals(language) || "pt".equals(language) || "zh".equals(language)) {
            language += "-" + locale.getCountry();
        }
        return language;
    }
}
