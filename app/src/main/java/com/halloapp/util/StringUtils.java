package com.halloapp.util;

import android.content.Context;
import android.graphics.Typeface;
import android.telephony.PhoneNumberUtils;
import android.text.BidiFormatter;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.text.style.URLSpan;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.emoji2.text.EmojiCompat;
import androidx.emoji2.text.EmojiSpan;

import com.halloapp.Constants;
import com.halloapp.R;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class StringUtils {

    private static final int EMOJI_LIMIT = 3;

    private static final List<Character> UNICODE_SPACE_LIST = new ArrayList<>(Arrays.asList(
            '\u00A0', // no-break space
            '\u2000', // en quad
            '\u2001', // em quad
            '\u2002', // en space
            '\u2003', // em space
            '\u2004', // three-per-em space
            '\u2005', // four-per-em space
            '\u2006', // six-per-em space
            '\u2007', // figure space
            '\u2008', // punctuation space
            '\u2009', // thin space
            '\u200A', // hair space
            '\u202F', // narrow no-break space
            '\u205F', // medium mathematical space
            '\u2800', // braille pattern blank
            '\u3000'  // ideographic space (CJK)
    ));

    public static String unicodeTrim(String s) {
        int len = s.length();
        int start = 0;

        while (start < len && isWhitespace(s.charAt(start))) {
            start++;
        }
        while (start < len && isWhitespace(s.charAt(len - 1))) {
            len--;
        }

        return ((start > 0) || (len < s.length())) ? s.substring(start, len) : s;
    }

    public static CharSequence unicodeTrim(CharSequence s) {
        int len = s.length();
        int start = 0;

        while (start < len && isWhitespace(s.charAt(start))) {
            start++;
        }
        while (start < len && isWhitespace(s.charAt(len - 1))) {
            len--;
        }

        return ((start > 0) || (len < s.length())) ? s.subSequence(start, len) : s;
    }

    private static boolean isWhitespace(char c) {
        return c <= ' ' || UNICODE_SPACE_LIST.contains(c);
    }

    public static boolean isAllWhitespace(String s) {
        for (int i=0; i<s.length(); i++) {
            if (!isWhitespace(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static String preparePostText(final String text) {
        if (TextUtils.isEmpty(text)) {
            return text;
        }
        String postText = unicodeTrim(text);
        return postText.length() < Constants.MAX_TEXT_LENGTH ? postText : postText.substring(0, Constants.MAX_TEXT_LENGTH);
    }

    public static String prepareGroupDescriptionText(final String text) {
        if (TextUtils.isEmpty(text)) {
            return text;
        }
        String postText = unicodeTrim(text);
        return postText.length() < Constants.MAX_GROUP_DESCRIPTION_LENGTH ? postText : postText.substring(0, Constants.MAX_GROUP_DESCRIPTION_LENGTH);
    }

    public static String bytesToHexString(byte[] bytes) {
        if (bytes == null) {
            return "null";
        }
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

    public static String convertColorToHexString(@ColorInt int intColor) {
        return String.format("#%06X", (0xFFFFFF & intColor));
    }

    public static Spanned replaceBoldWithMedium(@NonNull CharSequence str) {
        SpannableStringBuilder current = new SpannableStringBuilder(str);
        StyleSpan[] spans = current.getSpans(0, str.length(), StyleSpan.class);
        for (StyleSpan span : spans) {
            int start = current.getSpanStart(span);
            int end = current.getSpanEnd(span);
            current.removeSpan(span);

            TypefaceSpan typefaceSpan = new TypefaceSpan("sans-serif-medium");
            current.setSpan(typefaceSpan, start, end, 0);
        }
        return current;
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

    public static String formatMaxLineLengths(@NonNull String string, int maxLengthPerLine, int maxLines) {
        Preconditions.checkArgument(string.length() <= maxLengthPerLine * maxLines);
        String[] tokens = string.split("\\s");

        boolean tokenTooLong = false;
        List<String> lines = new ArrayList<>();
        String line = "";
        for (String token : tokens) {
            if (token.length() > maxLengthPerLine) {
                tokenTooLong = true;
                break;
            } else if (line.length() + token.length() + 1 < maxLengthPerLine) {
                line += " " + token;
            } else {
                lines.add(line);
                line = token;
            }
        }
        lines.add(line);

        if (tokenTooLong || lines.size() > maxLines) {
            lines.clear();
            line = "";
            for (int i=0; i<string.length(); i++) {
                char c = string.charAt(i);
                if (Character.isWhitespace(c) && line.length() >= maxLengthPerLine - 1) {
                    lines.add(line);
                    line = "";
                } else if (Character.isWhitespace(c) && line.length() == 0) {
                    // don't add whitespace at beginning of line
                } else {
                    line += c;
                    if (line.length() >= maxLengthPerLine) {
                        lines.add(line);
                        line = "";
                    }
                }
            }
            if (!TextUtils.isEmpty(line)) {
                lines.add(line);
            }
        }

        Preconditions.checkState(lines.size() <= maxLines);

        return TextUtils.join("\n", lines);
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

    public static int getOnlyEmojiCount(String text) {
        if (text == null) {
            return 0;
        }
        CharSequence processed = EmojiCompat.get().process(text, 0, text.length(), Integer.MAX_VALUE, EmojiCompat.REPLACE_STRATEGY_ALL);
        if (processed instanceof Spannable) {
            Spannable spannable = (Spannable) processed;
            EmojiSpan[] emojiSpans = spannable.getSpans(0, spannable.length(), EmojiSpan.class);
            int spanArea = 0;
            for (EmojiSpan span : emojiSpans) {
                int start = spannable.getSpanStart(span);
                int end = spannable.getSpanEnd(span);
                spanArea += end - start;
            }
            if (spanArea == processed.length()) {
                return emojiSpans.length;
            }
        }
        return 0;
    }
}
