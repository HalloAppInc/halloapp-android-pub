package com.halloapp.util;

import android.content.Context;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.halloapp.R;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Calendar;
import java.util.Locale;

public class TimeFormatter {

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
        } else if (timeDiff < 5 * DateUtils.DAY_IN_MILLIS) {
            return DateUtils.formatDateTime(context, timestamp, DateUtils.FORMAT_ABBREV_WEEKDAY | DateUtils.FORMAT_SHOW_WEEKDAY);
        } else if (TimeUtils.isSameYear(currentTime, timestamp)) {
            return DateUtils.formatDateTime(context, timestamp, DateUtils.FORMAT_NO_YEAR|DateUtils.FORMAT_ABBREV_MONTH);
        } else {
            return DateUtils.formatDateTime(context, timestamp, DateUtils.FORMAT_NUMERIC_DATE|DateUtils.FORMAT_SHOW_YEAR);
        }
    }

    public static String formatRelativeTimeForKatchup(@NonNull Context context, long timestamp) {
        final long currentTime = System.currentTimeMillis();
        final long timeDiff = currentTime - timestamp;
        if (TimeUtils.isSameDay(currentTime, timestamp)) {
            return context.getString(R.string.today);
        } else if (TimeUtils.isSameDay(currentTime - DateUtils.DAY_IN_MILLIS, timestamp)) {
            return context.getString(R.string.yesterday);
        } else if (timeDiff < 5 * DateUtils.DAY_IN_MILLIS) {
            return DateUtils.formatDateTime(context, timestamp, DateUtils.FORMAT_ABBREV_WEEKDAY | DateUtils.FORMAT_SHOW_WEEKDAY).toLowerCase(Locale.getDefault());
        } else if (TimeUtils.isSameYear(currentTime, timestamp)) {
            return DateUtils.formatDateTime(context, timestamp, DateUtils.FORMAT_NO_YEAR|DateUtils.FORMAT_ABBREV_MONTH).toLowerCase(Locale.getDefault());
        } else {
            return DateUtils.formatDateTime(context, timestamp, DateUtils.FORMAT_NUMERIC_DATE|DateUtils.FORMAT_SHOW_YEAR).toLowerCase(Locale.getDefault());
        }
    }

    public static String formatMessageTime(@NonNull Context context, long timestamp) {
        return DateUtils.formatDateTime(context, timestamp, DateUtils.FORMAT_SHOW_TIME);
    }

    public static String formatMomentDate(@NonNull Context context, long timestamp) {
        return DateUtils.formatDateTime(context, timestamp, DateUtils.FORMAT_NO_YEAR);
    }

    public static String formatRelativePostTime(@NonNull Context context, long timestamp) {
        final long currentTime = System.currentTimeMillis();
        final long timeDiff = currentTime - timestamp;
        final long seconds = timeDiff / 1000L;
        if (seconds < 60) {
            return context.getString(R.string.time_diff_now);
        }
        if (TimeUtils.isSameDay(currentTime, timestamp)) {
            return DateUtils.formatDateTime(context, timestamp, DateUtils.FORMAT_SHOW_TIME);
        } else if (timeDiff < 5 * DateUtils.DAY_IN_MILLIS) {
            return DateUtils.formatDateTime(context, timestamp, DateUtils.FORMAT_ABBREV_WEEKDAY | DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_TIME);
        } else if (TimeUtils.isSameYear(currentTime, timestamp)) {
            return DateUtils.formatDateTime(context, timestamp, DateUtils.FORMAT_NO_YEAR|DateUtils.FORMAT_ABBREV_MONTH);
        } else {
            return DateUtils.formatDateTime(context, timestamp, DateUtils.FORMAT_NUMERIC_DATE|DateUtils.FORMAT_SHOW_YEAR);
        }
    }

    public static String formatMessageInfoDay(@NonNull Context context, long timestamp) {
        final long currentTime = System.currentTimeMillis();
        final long timeDiff = currentTime - timestamp;
        if (TimeUtils.isSameDay(currentTime, timestamp)) {
            return context.getString(R.string.today);
        } else if (timeDiff < 5 * DateUtils.DAY_IN_MILLIS) {
            return DateUtils.formatDateTime(context, timestamp, DateUtils.FORMAT_ABBREV_WEEKDAY | DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_TIME);
        } else if (TimeUtils.isSameYear(currentTime, timestamp)) {
            return DateUtils.formatDateTime(context, timestamp, DateUtils.FORMAT_NO_YEAR | DateUtils.FORMAT_ABBREV_MONTH);
        } else {
            return DateUtils.formatDateTime(context, timestamp, DateUtils.FORMAT_NUMERIC_DATE | DateUtils.FORMAT_SHOW_YEAR);
        }
    }

    public static void setTimePostsFormat(@NonNull TextView textView, long timeStamp) {
        String text = formatRelativePostTime(textView.getContext(), timeStamp);
        textView.setText(text);
        textView.setContentDescription(text);
    }

    public static CharSequence formatMessageSeparatorDate(@NonNull Context context, long timestamp) {
        final long currentTime = System.currentTimeMillis();
        final long timeDiff = currentTime - timestamp;
        if (TimeUtils.isSameDay(currentTime, timestamp)) {
            return context.getString(R.string.today);
        } else if (TimeUtils.isSameDay(currentTime - DateUtils.DAY_IN_MILLIS, timestamp)) {
            return context.getString(R.string.yesterday);
        } else if (timeDiff < 180L * DateUtils.DAY_IN_MILLIS && TimeUtils.isSameYear(currentTime, timestamp)) {
            return DateUtils.formatDateTime(context, timestamp, DateUtils.FORMAT_SHOW_DATE);
        } else {
            return DateUtils.formatDateTime(context, timestamp, DateUtils.FORMAT_SHOW_DATE|DateUtils.FORMAT_SHOW_YEAR);
        }
    }

    public static CharSequence formatCallDuration(long durationMs) {
        // For 1h2m3s but I think that doesn't localize as well.
        /*long hours = 0;
        long seconds = durationMs / 1_000;
        long minutes = seconds / 60;
        seconds -= minutes * 60;
        hours = minutes / 60;
        minutes -= hours * 60;

        StringBuilder sb = new StringBuilder();
        if (hours > 0) {
            sb.append(hours);
            sb.append("h");
        }
        if (minutes > 0) {
            sb.append(minutes);
            sb.append("m");
        }
        if (seconds > 0) {
            sb.append(seconds);
            sb.append("s");
        }*/
        return DateUtils.formatElapsedTime(durationMs / 1_000);
    }

    public static CharSequence formatLastSeen(@NonNull Context context, long timestamp) {
        final long now = System.currentTimeMillis();
        if (timestamp > now) {
            timestamp = now;
        }
        if (timestamp > now - 60_000) {
            return context.getString(R.string.last_seen_less_minute);
        }

        CharSequence dateString;
        final String time = DateUtils.formatDateTime(context, timestamp, DateUtils.FORMAT_SHOW_TIME);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        if (!DateFormat.is24HourFormat(context)) {
            hour = hour % 12;
            if (hour == 0) {
                hour += 12;
            }
        }
        if (TimeUtils.isSameDay(now, timestamp)) {
            dateString = context.getResources().getQuantityString(R.plurals.today_at_time_plurals, hour, time);
        } else if (DateUtils.isToday(timestamp + DateUtils.DAY_IN_MILLIS)) {
            dateString = context.getResources().getQuantityString(R.plurals.yesterday_at_time_plurals, hour, time);
        } else if (timestamp > now - DateUtils.WEEK_IN_MILLIS) {
            String dayOfWeekShort = new SimpleDateFormat("EE", Locale.getDefault()).format(timestamp);
            dateString = context.getResources().getQuantityString(R.plurals.day_of_week_at_time_plurals, hour, dayOfWeekShort, time);
        } else {
            dateString = DateUtils.getRelativeTimeSpanString(timestamp, now, 0, DateUtils.FORMAT_ABBREV_MONTH|DateUtils.FORMAT_SHOW_YEAR);
        }

        return context.getString(R.string.last_seen, dateString);

    }

    public static CharSequence formatExpirationDuration(@NonNull Context context, int expirationSeconds) {
        int t = expirationSeconds / 60;
        if (t < 60) {
            return context.getResources().getQuantityString(R.plurals.expiry_minutes, t, t);
        }
        t /= 60;
        if (t <= 24) {
            return context.getResources().getQuantityString(R.plurals.expiry_hours, t, t);
        }
        t /= 24;
        if (t <= 30) {
            return context.getResources().getQuantityString(R.plurals.expiry_days, t, t);
        }
        t /= 30;
        return context.getResources().getQuantityString(R.plurals.expiry_months, t, t);
    }

    public static CharSequence formatLate(@NonNull Context context, int secondsLate) {
        int m = secondsLate / 60;
        if (m < 60) {
            return context.getString(R.string.late_minutes, Math.max(m, 1));
        }
        int h = m / 60;
        return context.getString(R.string.late_hours, h);
    }
}
