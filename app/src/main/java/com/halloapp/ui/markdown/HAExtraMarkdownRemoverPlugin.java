package com.halloapp.ui.markdown;

import androidx.annotation.NonNull;

import org.commonmark.parser.Parser;

import java.util.HashSet;

import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.inlineparser.BangInlineProcessor;
import io.noties.markwon.inlineparser.HtmlInlineProcessor;
import io.noties.markwon.inlineparser.MarkwonInlineParserPlugin;
import io.noties.markwon.inlineparser.OpenBracketInlineProcessor;

/**
 * Markwon plugin that strips out many of the unused features of
 * common-mark (e.g. html, linkification, headers)
 */
public class HAExtraMarkdownRemoverPlugin extends AbstractMarkwonPlugin {
    @Override
    public void configure(@NonNull Registry registry) {
        registry.require(MarkwonInlineParserPlugin.class, plugin -> {
            plugin.factoryBuilder()
                    .excludeInlineProcessor(HtmlInlineProcessor.class)
                    .excludeInlineProcessor(BangInlineProcessor.class)
                    // Uncomment to disable inline code blocks
                    //.excludeInlineProcessor(BackticksInlineProcessor.class)
                    .excludeInlineProcessor(OpenBracketInlineProcessor.class);
        });
    }

    @Override
    public void configureParser(@NonNull Parser.Builder builder) {
        builder.enabledBlockTypes(new HashSet<>());
    }
}
