package com.halloapp.util;

import android.content.Context;
import android.graphics.Typeface;
import android.telephony.PhoneNumberUtils;
import android.text.BidiFormatter;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.CharacterStyle;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.view.View;

import androidx.annotation.NonNull;

import com.halloapp.Constants;
import com.halloapp.R;

import java.text.BreakIterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class StringUtils {

    private static final int EMOJI_LIMIT = 3;

    public static String preparePostText(final String text) {
        if (TextUtils.isEmpty(text)) {
            return text;
        }
        String postText = text.trim();
        return postText.length() < Constants.MAX_TEXT_LENGTH ? postText : postText.substring(0, Constants.MAX_TEXT_LENGTH);
    }

    public static String prepareGroupDescriptionText(final String text) {
        if (TextUtils.isEmpty(text)) {
            return text;
        }
        String postText = text.trim();
        return postText.length() < Constants.MAX_GROUP_DESCRIPTION_LENGTH ? postText : postText.substring(0, Constants.MAX_GROUP_DESCRIPTION_LENGTH);
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

    public static Spanned replaceLink(@NonNull Context context, @NonNull CharSequence str, String url, Runnable onClick) {
        SpannableStringBuilder current = new SpannableStringBuilder(str);
        URLSpan[] spans = current.getSpans(0, str.length(), URLSpan.class);
        for (URLSpan span : spans) {
            if (!url.equals(span.getURL())) {
                continue;
            }
            int start = current.getSpanStart(span);
            int end = current.getSpanEnd(span);
            current.removeSpan(span);

            ClickableSpan learnMoreSpan = new ClickableSpan() {
                @Override
                public void updateDrawState(@NonNull TextPaint ds) {
                    ds.setUnderlineText(false);
                    ds.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
                    ds.setColor(context.getResources().getColor(R.color.color_secondary));
                }

                @Override
                public void onClick(@NonNull View widget) {
                    onClick.run();
                }
            };
            current.setSpan(learnMoreSpan, start, end, 0);
        }
        return current;
    }

    public static String formatVoiceNoteDuration(Context context, long millis) {
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        long minutes =  TimeUnit.MILLISECONDS.toMinutes(millis) -
                TimeUnit.HOURS.toMinutes(hours);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) -
                TimeUnit.MINUTES.toSeconds(minutes);
        if (hours > 0) {
            return String.format(context.getResources().getConfiguration().locale, "%d:%02d:%02d",
                    hours, minutes, seconds);
        }
        return String.format(context.getResources().getConfiguration().locale, "%d:%02d", minutes, seconds);
    }

    public static String formatPhoneNumber(@NonNull String phone) {
        return BidiFormatter.getInstance().unicodeWrap(PhoneNumberUtils.formatNumber("+" + phone, null));
    }

    public static String formatCommaSeparatedList(@NonNull List<String> stringList) {
        if (stringList.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(stringList.get(0));
        for (int i = 1; i < stringList.size(); i++) {
            sb.append(", ");
            sb.append(stringList.get(i));
        }
        return sb.toString();
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
