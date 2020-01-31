package com.halloapp.util;

import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.PluralsRes;
import androidx.annotation.StringRes;
import androidx.core.util.Preconditions;

import com.halloapp.R;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class ListFormatter {

    public static String format(@NonNull Context context, Collection<?> items) {
        if (Build.VERSION.SDK_INT >= 26) {
            return android.icu.text.ListFormatter.getInstance().format(items);
        } else {
            return formatLegacy(context, items);
        }
    }

    private static String formatLegacy(@NonNull Context context, Collection<?> items) {
        final Iterator<?> it = items.iterator();
        final int count = items.size();
        switch (count) {
            case 0:
                return "";
            case 1:
                return it.next().toString();
            case 2:
                return context.getResources().getString(R.string.list_of_two, it.next(), it.next());
        }
        StringBuilder stringBuilder = new StringBuilder(context.getResources().getString(R.string.list_of_many_start, it.next()));
        for (int i = 2; i < count; i++) {
            stringBuilder.append(context.getResources().getString(R.string.list_of_many_middle, it.next()));
        }
        stringBuilder.append(context.getResources().getString(R.string.list_of_many_end, it.next()));
        return stringBuilder.toString();
    }

    public static String format(@NonNull Context context, @StringRes int id1, @StringRes int id2, @StringRes int id3, @PluralsRes int id4, @NonNull List<?> items, Object... formatArgs) {
        Preconditions.checkArgument(!items.isEmpty());
        switch (items.size()) {
            case 1:
                return context.getString(id1, makeArgs(items, formatArgs));
            case 2:
                return context.getString(id2, makeArgs(items, formatArgs));
            case 3:
                return context.getString(id3, makeArgs(items, formatArgs));
            default: {
                final Object [] args = new Object[3 + formatArgs.length];
                args[0] = items.get(0);
                args[1] = items.get(1);
                args[2] = items.size() - 2;
                System.arraycopy(formatArgs, 0, args, 3, formatArgs.length);
                return context.getResources().getQuantityString(id4, items.size() - 2, args);
            }
        }
    }

    private static Object [] makeArgs(@NonNull List<?> items, Object... formatArgs) {
        Object [] args = new Object[items.size() + formatArgs.length];
        for (int i = 0; i < items.size(); i++) {
            args[i] = items.get(i);
        }
        for (int i = 0; i < formatArgs.length; i++) {
            args[items.size() + i] = formatArgs[i];
        }
        return args;
    }
}
