package com.halloapp.ui;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;

import com.halloapp.R;

public class SystemUiVisibility {

    public static int getDefaultSystemUiVisibility(@NonNull Context context) {
        boolean useLightSystemBars = context.getResources().getBoolean(R.bool.light_system_bars);
        if (Build.VERSION.SDK_INT >= 23) {
            TypedValue typedValue = new TypedValue();
            Resources.Theme theme = context.getTheme();
            if (theme != null) {
                context.getTheme().resolveAttribute(R.attr.useLightSystemBars, typedValue, true);
                useLightSystemBars = typedValue.data != 0;
            }
        }
        return
                (Build.VERSION.SDK_INT >= 23 && useLightSystemBars ? View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR : 0) |
                (Build.VERSION.SDK_INT >= 26 && useLightSystemBars ? View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR : 0) |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
    }

    public static int getFullScreenSystemUiVisibility(@NonNull Context context) {
        return SystemUiVisibility.getDefaultSystemUiVisibility(context)
                // Hide the nav bar and status bar
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
    }

    public static int getDefaultSystemUiVisibilityDark(@NonNull Context context) {
        return
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
    }

    public static int getFullScreenSystemUiVisibilityDark(@NonNull Context context) {
        return SystemUiVisibility.getDefaultSystemUiVisibilityDark(context)
                // Hide the nav bar and status bar
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
    }
}
