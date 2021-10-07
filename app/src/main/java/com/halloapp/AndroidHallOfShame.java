package com.halloapp;

import android.os.Build;

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
}
