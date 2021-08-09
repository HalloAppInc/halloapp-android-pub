package com.halloapp;

import android.os.Build;

public class AndroidHallOfShame {

    public static boolean deviceDoesWorkOnUIThread() {
        if ("OnePlus".equalsIgnoreCase(Build.BRAND)) {
            return "KB2005".equalsIgnoreCase(Build.MODEL);
        }
        return false;
    }

    public static boolean brokenHEAACEncoder() {
        if ("samsung".equalsIgnoreCase(Build.BRAND)) {
            return "SM-G930U".equalsIgnoreCase(Build.MODEL);
        }
        return false;
    }
}
