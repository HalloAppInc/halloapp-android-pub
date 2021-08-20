package com.halloapp.util;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

public class DrawableUtils {

    public static Drawable getTintedDrawable(@NonNull Context context, @DrawableRes int res, @ColorRes int colorRes) {
        Drawable d = context.getDrawable(res);
        if (d == null) {
            return null;
        }
        d = DrawableCompat.wrap(d);
        DrawableCompat.setTint(d, ContextCompat.getColor(context, colorRes));
        return d;
    }
}
