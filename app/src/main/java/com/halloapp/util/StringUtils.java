package com.halloapp.util;

import android.telephony.PhoneNumberUtils;
import android.text.BidiFormatter;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.halloapp.Constants;

import java.text.BreakIterator;

public class StringUtils {

    private static final int EMOJI_LIMIT = 3;

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

    public static String formatPhoneNumber(@NonNull String phone) {
        return BidiFormatter.getInstance().unicodeWrap(PhoneNumberUtils.formatNumber("+" + phone, null));
    }

    public static boolean isFewEmoji(String text) {
        if (TextUtils.isEmpty(text)) {
            return false;
        }

        BreakIterator breakIterator = BreakIterator.getCharacterInstance();
        breakIterator.setText(text);
        int count = 0;
        while (breakIterator.next() != BreakIterator.DONE) {
            count++;
            if (count > EMOJI_LIMIT) {
                return false;
            }
        }

        int codePointCount = Character.codePointCount(text, 0, text.length());
        for (int i=0; i<codePointCount; i++) {
            int codePoint = Character.codePointAt(text, i);
            int type = Character.getType(codePoint);
            if (type != Character.OTHER_SYMBOL && type != Character.SURROGATE && type != Character.FORMAT && type != Character.NON_SPACING_MARK) {
                return false;
            }
        }
        return true;
    }
}
