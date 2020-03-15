package com.halloapp.util;

import android.content.Context;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.halloapp.R;

public class TimeFormatter {

    public static String formatTimeDiff(@NonNull Context context, long timeDiff, boolean longFormat) {
        final long seconds = timeDiff / 1000L;
        if (seconds < 60) {
            return context.getString(R.string.time_diff_now);
        }
        final long minutes = seconds / 60L;
        if (minutes < 60) {
            return context.getResources().getQuantityString(
                    longFormat ? R.plurals.time_diff_minutes_long : R.plurals.time_diff_minutes, (int)minutes, (int)minutes);
        }
        final long hours = minutes / 60L;
        if (hours < 24) {
            return context.getResources().getQuantityString(
                    longFormat ? R.plurals.time_diff_hours_long : R.plurals.time_diff_hours, (int)hours, (int)hours);
        }
        final long days = hours / 24L;
        return context.getResources().getQuantityString(
                longFormat ? R.plurals.time_diff_days_long : R.plurals.time_diff_days, (int)days, (int)days);
    }

    public static void setTimeDiffText(@NonNull TextView textView, long timeDiff) {
        textView.setText(formatTimeDiff(textView.getContext(), timeDiff, false));
        textView.setContentDescription(formatTimeDiff(textView.getContext(), timeDiff, true));
    }
}
