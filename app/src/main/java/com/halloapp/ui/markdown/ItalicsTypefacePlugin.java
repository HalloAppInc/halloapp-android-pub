package com.halloapp.ui.markdown;

import android.graphics.Typeface;

import androidx.annotation.NonNull;

import org.commonmark.node.Emphasis;

import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.MarkwonSpansFactory;
import io.noties.markwon.core.spans.CustomTypefaceSpan;

/**
 * Workaround that uses type face for italics, otherwise Android tends to clip italics
 */
public class ItalicsTypefacePlugin extends AbstractMarkwonPlugin {
    @Override
    public void configureSpansFactory(@NonNull MarkwonSpansFactory.Builder builder) {
        builder.setFactory(
                Emphasis.class,
                (configuration, props) -> new ItalicTypefaceSpan());
    }

    public static class ItalicTypefaceSpan extends CustomTypefaceSpan {
        public ItalicTypefaceSpan() {
            super(Typeface.create(Typeface.DEFAULT, Typeface.ITALIC));
        }
    }
}
