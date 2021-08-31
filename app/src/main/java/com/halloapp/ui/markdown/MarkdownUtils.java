package com.halloapp.ui.markdown;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.content.Mention;
import com.halloapp.ui.mentions.MentionsFormatter;

import java.util.ArrayList;
import java.util.List;

import io.noties.markwon.Markwon;
import io.noties.markwon.editor.MarkwonEditor;
import io.noties.markwon.editor.handler.EmphasisEditHandler;
import io.noties.markwon.editor.handler.StrongEmphasisEditHandler;
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin;
import io.noties.markwon.inlineparser.MarkwonInlineParserPlugin;

public class MarkdownUtils {

    public static Markwon createMarkwon(@NonNull Context context) {
        return Markwon.builder(context)
                .usePlugin(StrikethroughPlugin.create())
                .usePlugin(MarkwonInlineParserPlugin.create())
                .usePlugin(new HAExtraMarkdownRemoverPlugin())
                .usePlugin(new ItalicsTypefacePlugin())
                .build();
    }

    public static MarkwonEditor createMarkwonEditor(@NonNull Context context) {
        Markwon markwon = Markwon.builder(context)
                .usePlugin(StrikethroughPlugin.create())
                .usePlugin(MarkwonInlineParserPlugin.create())
                .usePlugin(new HAExtraMarkdownRemoverPlugin())
                .build();

        return MarkwonEditor.builder(markwon)
                .useEditHandler(new EmphasisEditHandler())
                .useEditHandler(new StrongEmphasisEditHandler())
                .useEditHandler(StrikethroughEditHandler.create())
                .build();
    }

    public static CharSequence formatMarkdownWithMentions(@NonNull Context context, String text, List<Mention> mentions) {
        return formatMarkdownWithMentions(context, text, mentions, null);
    }

    public static CharSequence formatMarkdownWithMentions(@NonNull Context context, String text, List<Mention> mentions, @Nullable MentionsFormatter.MentionClickListener mentionClickListener) {
        ArrayList<Integer> initialMentionIndices = new ArrayList<>();
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '@') {
                initialMentionIndices.add(i);
            }
        }
        Markwon markwon = createMarkwon(context);
        CharSequence markdown = markwon.toMarkdown(text);
        ArrayList<Integer> newMentionIndices = new ArrayList<>();
        for (int i = 0; i < markdown.length(); i++) {
            if (markdown.charAt(i) == '@') {
                newMentionIndices.add(i);
            }
        }
        List<Mention> adjustedMentions = new ArrayList<>();
        for (Mention mention : mentions) {
            int i = initialMentionIndices.indexOf(mention.index);
            Mention copy = new Mention(newMentionIndices.get(i), mention.userId, mention.fallbackName);
            adjustedMentions.add(copy);
        }
        return MentionsFormatter.insertMentions(markdown, adjustedMentions, mentionClickListener);
    }
}
