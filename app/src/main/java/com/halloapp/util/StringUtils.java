package com.halloapp.util;

import android.text.TextUtils;

import com.halloapp.Constants;

public class StringUtils {

    public static String preparePostText(final String text) {
        if (TextUtils.isEmpty(text)) {
            return text;
        }
        String postText = text.trim();
        return postText.length() < Constants.MAX_TEXT_LENGTH ? postText : postText.substring(0, Constants.MAX_TEXT_LENGTH);
    }
}
