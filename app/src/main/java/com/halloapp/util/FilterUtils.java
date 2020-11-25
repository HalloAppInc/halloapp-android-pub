package com.halloapp.util;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Filter;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.halloapp.R;
import com.halloapp.contacts.Contact;

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
        final String filterTextString = Preconditions.checkNotNull(filterText).toString();
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

    public static abstract class ItemFilter<Item> extends Filter {

        private final List<Item> items;

        public ItemFilter(@NonNull List<Item> items) {
            this.items = items;
        }

        @Override
        protected FilterResults performFiltering(@Nullable CharSequence prefix) {
            final FilterResults results = new FilterResults();
            final List<String> filterTokens = FilterUtils.getFilterTokens(prefix);
            if (filterTokens == null) {
                results.values = items;
                results.count = items.size();
            } else {
                final ArrayList<Item> filteredItems = new ArrayList<>();
                for (Item item : items) {
                    final String s = itemToString(item);
                    if (matchTokens(s, filterTokens)) {
                        filteredItems.add(item);
                    }
                }
                results.values = filteredItems;
                results.count = filteredItems.size();
            }
            return results;
        }

        protected abstract String itemToString(Item item);
    }
}
