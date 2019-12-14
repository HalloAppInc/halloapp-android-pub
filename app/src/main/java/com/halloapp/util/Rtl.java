package com.halloapp.util;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;

public class Rtl {

    public static boolean isRtl(@NonNull Context context) {
        return context.getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
    }
}
