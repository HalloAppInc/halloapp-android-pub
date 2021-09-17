package com.halloapp.ui.markdown;

import androidx.annotation.NonNull;

import org.commonmark.parser.Parser;

import java.util.HashSet;

import io.noties.markwon.AbstractMarkwonPlugin;

/**
 * Markwon plugin that strips out many of the unused features of
 * common-mark (e.g. html, linkification, headers)
 */
public class HAExtraMarkdownRemoverPlugin extends AbstractMarkwonPlugin {

    @Override
    public void configureParser(@NonNull Parser.Builder builder) {
        builder.enabledBlockTypes(new HashSet<>());
    }
}
