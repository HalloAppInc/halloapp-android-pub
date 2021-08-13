package com.halloapp;

import android.os.Build;

public class AndroidHallOfShame {

    public static boolean deviceDoesWorkOnUIThread() {
        if ("OnePlus".equalsIgnoreCase(Build.BRAND)) {
            return "KB2005".equalsIgnoreCase(Build.MODEL);
        }
        if ("samsung".equalsIgnoreCase(Build.BRAND)) {
            return "SM-G960F".equalsIgnoreCase(Build.MODEL);
        }
        return false;
    }
}
