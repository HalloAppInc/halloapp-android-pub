package com.halloapp.util;

import android.content.Context;
import android.text.format.DateUtils;

import androidx.annotation.NonNull;

import com.halloapp.R;

public class TimeUtils {

    public static String formatTimeDiff(@NonNull Context context, long timeDiff) {
        final long seconds = timeDiff / 1000L;
        if (seconds < 60) {
            return context.getString(R.string.time_diff_now);
        }
        final long minutes = seconds / 60L;
        if (minutes < 60) {
            return context.getResources().getQuantityString(R.plurals.time_diff_minutes, (int)minutes, (int)minutes);
        }
        final long hours = minutes / 60L;
        if (hours < 24) {
            return context.getResources().getQuantityString(R.plurals.time_diff_hours, (int)hours, (int)hours);
        }
        final long days = hours / 24L;
        return context.getResources().getQuantityString(R.plurals.time_diff_days, (int)days, (int)days);
    }
}
