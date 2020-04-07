package com.halloapp.util;


import java.util.Calendar;

public class TimeUtils {

    public static boolean isSameDay(long time1, long time2) {
        final Calendar calendar1 = Calendar.getInstance();
        final Calendar calendar2 = Calendar.getInstance();
        calendar1.setTimeInMillis(time1);
        calendar2.setTimeInMillis(time2);
        return calendar1.get(Calendar.DAY_OF_YEAR) == calendar2.get(Calendar.DAY_OF_YEAR) && calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR);
    }
}
