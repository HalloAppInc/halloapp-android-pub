package com.halloapp.util;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.halloapp.R;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;

public class FilterUtils {
    @Nullable
    public static List<String> getFilterTokens(final @Nullable CharSequence filterText) {
        if (TextUtils.isEmpty(filterText)) {
            return null;
        }
        final List<String> filterTokens = new ArrayList<>();
        final BreakIterator boundary = BreakIterator.getWordInstance();
        final String filterTextString = filterText.toString();
        boundary.setText(filterTextString);
        int start = boundary.first();
        for (int end = boundary.next(); end != BreakIterator.DONE; start = end, end = boundary.next()) {
            if (end > start) {
                filterTokens.add(filterTextString.substring(start, end).toLowerCase());
            }
        }
        return filterTokens;
    }

    @Nullable
    public static CharSequence formatMatchingText(@NonNull Context context, @NonNull String text, @NonNull List<String> filterTokens) {
        SpannableString formattedName = null;
        final BreakIterator boundary = BreakIterator.getWordInstance();
        boundary.setText(text);
        int start = boundary.first();
        for (int end = boundary.next(); end != BreakIterator.DONE; start = end, end = boundary.next()) {
            if (end <= start) {
                continue;
            }
            final String word = text.substring(start, end).toLowerCase();
            for (String filterToken : filterTokens) {
                if (word.startsWith(filterToken)) {
                    if (formattedName == null) {
                        formattedName = new SpannableString(text);
                    }
                    @ColorInt int searchHighlightColor = ContextCompat.getColor(context, R.color.search_highlight);
                    int spanEnd = Math.min(end, start + filterToken.length());
                    formattedName.setSpan(new ForegroundColorSpan(searchHighlightColor), start, spanEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        }
        return formattedName;
    }

    public static boolean matchTokens(@NonNull String text, @NonNull List<String> filterTokens) {
        List<String> words = getFilterTokens(text);
        if (words == null) {
            return false;
        }
        boolean match = true;
        for (String filterToken : filterTokens) {
            boolean tokenMatch = false;
            for (String word : words) {
                if (word.startsWith(filterToken)) {
                    tokenMatch = true;
                    break;
                }
            }
            if (!tokenMatch) {
                match = false;
                break;
            }
        }
        return match;
    }
}
