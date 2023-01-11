package com.halloapp.katchup;

import android.text.InputFilter;
import android.text.Spanned;

import java.util.Locale;

public class UsernameInputFilter implements InputFilter {
    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        // Prevent non alpha char from being set at the start
        if (dstart == 0 && start < end && !Character.isAlphabetic(source.charAt(start))) {
            return "";
        }

        final String lowercase = String.valueOf(source).toLowerCase(Locale.getDefault());
        final StringBuilder builder = new StringBuilder();
        for (int i = start; i < end; i++) {
            char c = lowercase.charAt(i);
            if (Character.isLetterOrDigit(c) || c == '_' || c == '.') {
                builder.append(c);
            }
        }

        final String filtered = builder.toString();
        return filtered.contentEquals(source) ? null : filtered;
    }
}
