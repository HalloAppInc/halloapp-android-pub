package com.halloapp.ui.mentions;

import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.content.Mention;
import com.halloapp.util.logs.Log;

import java.util.Collections;
import java.util.List;

public class MentionsFormatter {

    public interface MentionClickListener {
        void onMentionClick(View v, Mention mention);
    }

    public static final char MENTION_CHARACTER = '@';

    @NonNull
    public static CharSequence insertMentions(@NonNull CharSequence str, @NonNull List<Mention> mentions, boolean showAt, @Nullable MentionClickListener mentionClickListener) {
        Collections.sort(mentions, (o1, o2) -> o1.index - o2.index);

        SpannableStringBuilder builder = new SpannableStringBuilder(str);
        for (int i = mentions.size() - 1; i >= 0; i--) {
            Mention mention = mentions.get(i);
            if (!isValidMention(str, mention)) {
                Log.i("MentionsFormatter/insertMentions invalid mention");
                continue;
            }
            builder.replace(mention.index, mention.index + 1, createSpan(mention, showAt, mentionClickListener));
        }
        return builder;
    }

    public static boolean isValidMention(@NonNull CharSequence str, @NonNull Mention mention) {
        if (mention.index < 0 || mention.index >= str.length()) {
            return false;
        }
        return str.charAt(mention.index) == MENTION_CHARACTER;
    }

    private static CharSequence createSpan(@NonNull Mention mention, boolean showAt, @Nullable MentionClickListener mentionClickListener) {
        SpannableString mentionString = new SpannableString(showAt ? "@" + mention.fallbackName : mention.fallbackName);
        mentionString.setSpan(new MentionSpan(mention, mentionClickListener), 0, mentionString.length(),  Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return mentionString;
    }

    static class MentionSpan extends ClickableSpan {

        private final Mention mention;
        private @Nullable final MentionClickListener listener;

        public MentionSpan(@NonNull Mention mention, @Nullable MentionClickListener mentionClickListener){
            this.mention = mention;
            this.listener = mentionClickListener;
        }

        @Override
        public void onClick(@NonNull View widget) {
            if (listener != null) {
                listener.onMentionClick(widget, mention);
            }
        }

        @Override
        public void updateDrawState(@NonNull TextPaint ds) {
            ds.setUnderlineText(false);
            ds.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        }
    }
}
