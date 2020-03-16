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

    public static String bytesToHexString(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte aByte : bytes) {
            hexString.append(Integer.toHexString((aByte & 0xFF) | 0x100).substring(1, 3));
        }
        return hexString.toString();
    }

    public static byte[] bytesFromHexString(String hex) {
        int len = hex.length() / 2;
        byte[] ret = new byte[len];
        for (int i=0; i<len; i++) {
            String b = hex.substring(i*2, i*2+2);
            ret[i] = (byte)Integer.parseInt(b, 16);
        }
        return ret;
    }
}
