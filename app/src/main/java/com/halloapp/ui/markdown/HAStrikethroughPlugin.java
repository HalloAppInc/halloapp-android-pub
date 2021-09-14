package com.halloapp.ui.markdown;

import android.text.style.StrikethroughSpan;

import androidx.annotation.NonNull;

import org.commonmark.ext.gfm.strikethrough.Strikethrough;
import org.commonmark.parser.Parser;

import java.util.Collections;

import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.MarkwonConfiguration;
import io.noties.markwon.MarkwonSpansFactory;
import io.noties.markwon.MarkwonVisitor;
import io.noties.markwon.RenderProps;
import io.noties.markwon.SpanFactory;

public class HAStrikethroughPlugin extends AbstractMarkwonPlugin {
    @NonNull
    public static HAStrikethroughPlugin create() {
        return new HAStrikethroughPlugin();
    }

    @Override
    public void configureParser(@NonNull Parser.Builder builder) {
        builder.extensions(Collections.singleton(HAStrikethroughExtension.create()));
    }

    @Override
    public void configureSpansFactory(@NonNull MarkwonSpansFactory.Builder builder) {
        builder.setFactory(Strikethrough.class, new SpanFactory() {
            @Override
            public Object getSpans(@NonNull MarkwonConfiguration configuration, @NonNull RenderProps props) {
                return new StrikethroughSpan();
            }
        });
    }

    @Override
    public void configureVisitor(@NonNull MarkwonVisitor.Builder builder) {
        builder.on(Strikethrough.class, new MarkwonVisitor.NodeVisitor<Strikethrough>() {
            @Override
            public void visit(@NonNull MarkwonVisitor visitor, @NonNull Strikethrough strikethrough) {
                final int length = visitor.length();
                visitor.visitChildren(strikethrough);
                visitor.setSpansForNodeOptional(strikethrough, length);
            }
        });
    }
}
