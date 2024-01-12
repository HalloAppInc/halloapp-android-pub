package com.halloapp.util;


import android.text.format.DateFormat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TimeUtils {

    public static boolean isSameDay(long time1, long time2) {
        final Calendar calendar1 = Calendar.getInstance();
        final Calendar calendar2 = Calendar.getInstance();
        calendar1.setTimeInMillis(time1);
        calendar2.setTimeInMillis(time2);
        return calendar1.get(Calendar.DAY_OF_YEAR) == calendar2.get(Calendar.DAY_OF_YEAR) && calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR);
    }

    public static boolean isSameYear(long time1, long time2) {
        final Calendar calendar1 = Calendar.getInstance();
        final Calendar calendar2 = Calendar.getInstance();
        calendar1.setTimeInMillis(time1);
        calendar2.setTimeInMillis(time2);
        return calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR);
    }

    public static boolean isSameWeek(long time1, long time2) {
        final Calendar calendar1 = Calendar.getInstance();
        final Calendar calendar2 = Calendar.getInstance();
        calendar1.setTimeInMillis(time1);
        calendar2.setTimeInMillis(time2);
        return calendar1.get(Calendar.WEEK_OF_YEAR) == calendar2.get(Calendar.WEEK_OF_YEAR)
                && calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR);
    }

    public static String getDayMonth(long time) {
        if (time == 0) {
            return "";
        }
        Date date = new Date(time);
        String skeleton = DateFormat.getBestDateTimePattern(Locale.getDefault(), "MMM dd");
        SimpleDateFormat sdf = new SimpleDateFormat(skeleton, Locale.getDefault());
        return sdf.format(date);
    }
}
