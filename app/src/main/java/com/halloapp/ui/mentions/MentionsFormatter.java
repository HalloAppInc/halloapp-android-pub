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
import com.halloapp.widget.DebouncedSpan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
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

    public static List<Mention> recomputeMentionIndices(@NonNull List<Mention> mentions, CharSequence before, CharSequence after) {
        if (mentions.isEmpty()) {
            return new ArrayList<>();
        }
        ArrayList<Integer> initialMentionIndices = new ArrayList<>();
        for (int i = 0; i < before.length(); i++) {
            if (before.charAt(i) == MentionsFormatter.MENTION_CHARACTER) {
                initialMentionIndices.add(i);
            }
        }
        ArrayList<Integer> newMentionIndices = new ArrayList<>();
        for (int i = 0; i < after.length(); i++) {
            if (after.charAt(i) == MentionsFormatter.MENTION_CHARACTER) {
                newMentionIndices.add(i);
            }
        }
        List<Mention> adjustedMentions = new ArrayList<>();
        HashSet<Integer> addedMentionIndices = new HashSet<>();
        for (Mention mention : mentions) {
            if (!MentionsFormatter.isValidMention(before, mention)) {
                Log.e("MentionsFormatter invalid mention!");
                continue;
            }
            int i = initialMentionIndices.indexOf(mention.index);
            if (i < 0 || i >= newMentionIndices.size()) {
                Log.e("MentionsFormatter mention index out of bounds " + i);
                continue;
            }
            int newIndex = newMentionIndices.get(i);
            if (addedMentionIndices.contains(newIndex)) {
                Log.e("MentionsFormatter duplicated mention " + i);
                continue;
            }
            Mention copy = new Mention(newIndex, mention.userId, mention.fallbackName);
            adjustedMentions.add(copy);
            addedMentionIndices.add(newIndex);
        }
        return adjustedMentions;
    }

    static class MentionSpan extends DebouncedSpan {

        private final Mention mention;
        private @Nullable final MentionClickListener listener;

        public MentionSpan(@NonNull Mention mention, @Nullable MentionClickListener mentionClickListener){
            this.mention = mention;
            this.listener = mentionClickListener;
        }

        @Override
        public void onOneClick(@NonNull View widget) {
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
