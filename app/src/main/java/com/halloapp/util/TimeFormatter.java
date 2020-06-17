package com.halloapp.util;

import android.content.Context;
import android.text.format.DateUtils;
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

    /**
     * Formats time relative to current time
     * If within 60 seconds: Now
     * If same day: time (e.g. 3:20pm)
     * If same week: day of week (e.g. Mon)
     * If same year: month and day (e.g. Jun 10)
     * Otherwise: numeric date (e.g. 3/20/2019)
     * @param context
     * @param timestamp
     * @return time formatted as string relative to current time
     */
    public static String formatRelativeTime(@NonNull Context context, long timestamp) {
        final long currentTime = System.currentTimeMillis();
        final long timeDiff = currentTime - timestamp;
        final long seconds = timeDiff / 1000L;
        if (seconds < 60) {
            return context.getString(R.string.time_diff_now);
        }
        if (TimeUtils.isSameDay(currentTime, timestamp)) {
            return DateUtils.formatDateTime(context, timestamp, DateUtils.FORMAT_SHOW_TIME);
        } else if (TimeUtils.isSameDay(currentTime - DateUtils.DAY_IN_MILLIS, timestamp)) {
            return context.getString(R.string.yesterday);
        } else if (TimeUtils.isSameWeek(currentTime, timestamp)) {
            return DateUtils.formatDateTime(context, timestamp, DateUtils.FORMAT_ABBREV_WEEKDAY | DateUtils.FORMAT_SHOW_WEEKDAY);
        } else if (TimeUtils.isSameYear(currentTime, timestamp)) {
            return DateUtils.formatDateTime(context, timestamp, DateUtils.FORMAT_NO_YEAR|DateUtils.FORMAT_ABBREV_MONTH);
        } else {
            return DateUtils.formatDateTime(context, timestamp, DateUtils.FORMAT_NUMERIC_DATE);
        }
    }

    public static void setTimeDiffText(@NonNull TextView textView, long timeDiff) {
        textView.setText(formatTimeDiff(textView.getContext(), timeDiff, false));
        textView.setContentDescription(formatTimeDiff(textView.getContext(), timeDiff, true));
    }

    public static CharSequence formatMessageSeparatorDate(@NonNull Context context, long timestamp) {
        final long currentTime = System.currentTimeMillis();
        if (TimeUtils.isSameDay(currentTime, timestamp)) {
            return context.getString(R.string.today);
        } else if (TimeUtils.isSameDay(currentTime - DateUtils.DAY_IN_MILLIS, timestamp)) {
            return context.getString(R.string.yesterday);
        } else if (TimeUtils.isSameYear(currentTime, timestamp)) {
            return DateUtils.formatDateTime(context, timestamp, DateUtils.FORMAT_SHOW_DATE);
        } else {
            return DateUtils.formatDateTime(context, timestamp, DateUtils.FORMAT_SHOW_DATE|DateUtils.FORMAT_SHOW_YEAR);
        }
    }

    public static CharSequence formatLastSeen(@NonNull Context context, long timestamp) {
        final long now = System.currentTimeMillis();
        if (timestamp > now) {
            timestamp = now;
        }
        if (timestamp > now - 60_000) {
            return context.getString(R.string.last_seen_less_minute);
        }
        return context.getString(R.string.last_seen, DateUtils.getRelativeTimeSpanString(timestamp));

    }
}
