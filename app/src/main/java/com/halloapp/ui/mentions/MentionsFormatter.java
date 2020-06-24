package com.halloapp.ui.mentions;

import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import com.halloapp.content.Mention;
import com.halloapp.util.Log;

import java.util.Collections;
import java.util.List;

public class MentionsFormatter {

    private static final char MENTION_CHARACTER = '@';

    @NonNull
    public static CharSequence insertMentions(@NonNull CharSequence str, @NonNull List<Mention> mentions) {
        Collections.sort(mentions, (o1, o2) -> o1.index - o2.index);

        SpannableStringBuilder builder = new SpannableStringBuilder(str);
        for (int i = mentions.size() - 1; i >= 0; i--) {
            Mention mention = mentions.get(i);
            if (!isValidMention(str, mention)) {
                Log.i("MentionsFormatter/insertMentions invalid mention");
                continue;
            }
            builder.replace(mention.index, mention.index + 1, createSpan(mention));
        }
        return builder;
    }

    private static boolean isValidMention(@NonNull CharSequence str, @NonNull Mention mention) {
        if (mention.index < 0 || mention.index >= str.length()) {
            return false;
        }
        if (str.charAt(mention.index) != MENTION_CHARACTER) {
            return false;
        }
        return true;
    }

    private static CharSequence createSpan(@NonNull Mention mention) {
        SpannableString mentionString = new SpannableString("@" + mention.fallbackName);
        mentionString.setSpan(new MentionSpan(), 0, mentionString.length(),  Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        return mentionString;
    }

    static class MentionSpan extends ClickableSpan {

        public MentionSpan(){
        }

        @Override
        public void onClick(@NonNull View widget) {

        }

        @Override
        public void updateDrawState(@NonNull TextPaint ds) {
            ds.setUnderlineText(false);
            ds.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        }
    }
}
