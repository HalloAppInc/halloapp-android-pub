package com.halloapp.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.halloapp.media.MediaUtils;

public class DrawableUtils {

    public static Drawable getTintedDrawable(@NonNull Context context, @DrawableRes int res, @ColorRes int colorRes) {
        Drawable d = ContextCompat.getDrawable(context, res);
        if (d == null) {
            return null;
        }
        d = DrawableCompat.wrap(d);
        DrawableCompat.setTint(d, ContextCompat.getColor(context, colorRes));
        return d;
    }

    // From https://stackoverflow.com/a/24389104/11817085
    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }

        final Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), MediaUtils.getBitmapConfig(null));
        final Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }
}
