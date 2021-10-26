package com.halloapp.ui.markdown;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.content.Mention;
import com.halloapp.ui.mentions.MentionsFormatter;
import com.halloapp.util.logs.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import io.noties.markwon.Markwon;
import io.noties.markwon.editor.MarkwonEditor;
import io.noties.markwon.editor.handler.ItalicsEditHandler;
import io.noties.markwon.inlineparser.BackticksInlineProcessor;
import io.noties.markwon.inlineparser.MarkwonInlineParser;
import io.noties.markwon.inlineparser.MarkwonInlineParserPlugin;

public class MarkdownUtils {

    public static Markwon createMarkwon(@NonNull Context context) {
        return Markwon.builder(context)
                .usePlugin(HAStrikethroughPlugin.create())
                .usePlugin(createInlineParser())
                .usePlugin(new ItalicsTypefacePlugin())
                .usePlugin(new HAExtraMarkdownRemoverPlugin())
                .build();
    }

    private static MarkwonInlineParserPlugin createInlineParser() {
        MarkwonInlineParser.FactoryBuilder builder = MarkwonInlineParser.factoryBuilderNoDefaults();
        builder.addInlineProcessor(new BackticksInlineProcessor());
        builder.addDelimiterProcessor(new BoldDelimiterProcessor());
        builder.addDelimiterProcessor(new ItalicsDelimiterProcessor());
        return MarkwonInlineParserPlugin.create(builder);
    }

    public static MarkwonEditor createMarkwonEditor(@NonNull Context context) {
        Markwon markwon = Markwon.builder(context)
                .usePlugin(HAStrikethroughPlugin.create())
                .usePlugin(createInlineParser())
                .usePlugin(new HAExtraMarkdownRemoverPlugin())
                .build();

        return MarkwonEditor.builder(markwon)
                .useEditHandler(new BoldEditHandler())
                .useEditHandler(new ItalicsEditHandler())
                .useEditHandler(HAStrikethroughEditHandler.create())
                .build();
    }

    public static CharSequence formatMarkdownWithMentions(@NonNull Context context, @Nullable String text, List<Mention> mentions) {
        return formatMarkdownWithMentions(context, text, mentions, true, null);
    }

    public static CharSequence formatMarkdown(@NonNull Context context, @Nullable String text) {
        return formatMarkdownWithMentions(context, text, null);
    }

    public static CharSequence formatMarkdownWithMentions(@NonNull Context context, @Nullable String text, @Nullable List<Mention> mentions, boolean showAt, @Nullable MentionsFormatter.MentionClickListener mentionClickListener) {
        if (text == null) {
            return null;
        }
        ArrayList<Integer> initialMentionIndices = new ArrayList<>();
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == MentionsFormatter.MENTION_CHARACTER) {
                initialMentionIndices.add(i);
            }
        }
        Markwon markwon = createMarkwon(context);
        CharSequence markdown = markwon.toMarkdown(text);
        if (mentions == null || mentions.isEmpty()) {
            return markdown;
        }
        ArrayList<Integer> newMentionIndices = new ArrayList<>();
        for (int i = 0; i < markdown.length(); i++) {
            if (markdown.charAt(i) == MentionsFormatter.MENTION_CHARACTER) {
                newMentionIndices.add(i);
            }
        }
        List<Mention> adjustedMentions = new ArrayList<>();
        HashSet<Integer> addedMentionIndices = new HashSet<>();
        for (Mention mention : mentions) {
            if (!MentionsFormatter.isValidMention(text, mention)) {
                Log.e("MarkdownUtils/formatMarkdownWithMentions invalid mention!");
                continue;
            }
            int i = initialMentionIndices.indexOf(mention.index);
            if (i < 0 || i >= newMentionIndices.size()) {
                Log.e("MarkdownUtils/formatMarkdownWithMentions mention index out of bounds " + i);
                continue;
            }
            int newIndex = newMentionIndices.get(i);
            if (addedMentionIndices.contains(newIndex)) {
                Log.e("MarkdownUtils/formatMarkdownWithMentions duplicated mention " + i);
                continue;
            }
            Mention copy = new Mention(newIndex, mention.userId, mention.fallbackName);
            adjustedMentions.add(copy);
            addedMentionIndices.add(newIndex);
        }
        return MentionsFormatter.insertMentions(markdown, adjustedMentions, showAt, mentionClickListener);
    }
}
