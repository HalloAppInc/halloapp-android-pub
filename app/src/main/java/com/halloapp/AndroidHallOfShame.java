package com.halloapp;

import android.os.Build;

import java.util.Locale;

public class AndroidHallOfShame {

    public static boolean deviceDoesWorkOnUIThread() {
        if ("OnePlus".equalsIgnoreCase(Build.BRAND)) {
            return "KB2005".equalsIgnoreCase(Build.MODEL);
        }
        if ("samsung".equalsIgnoreCase(Build.BRAND)) {
            if ("SM-G960F".equalsIgnoreCase(Build.MODEL) ||
                    "SM-G991U".equalsIgnoreCase(Build.MODEL)) {
                return true;
            }
        }
        return false;
    }

    public static boolean deviceDoesNotSupport4To3Encoding() {
        return "Huawei".equalsIgnoreCase(Build.BRAND) && "STK-LX3".equalsIgnoreCase(Build.MODEL);
    }

    public static boolean isTelecomBrokenDevice() {
        String device = Build.MODEL + " " + Build.PRODUCT + " " + Build.MANUFACTURER;
        return device.toLowerCase(Locale.US).contains("oneplus");
    }
}
