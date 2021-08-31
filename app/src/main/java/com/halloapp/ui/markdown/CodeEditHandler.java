package com.halloapp.ui.markdown;

import android.text.Editable;
import android.text.Spanned;

import androidx.annotation.NonNull;

import io.noties.markwon.Markwon;
import io.noties.markwon.core.MarkwonTheme;
import io.noties.markwon.core.spans.CodeSpan;
import io.noties.markwon.editor.AbstractEditHandler;
import io.noties.markwon.editor.MarkwonEditorUtils;
import io.noties.markwon.editor.PersistedSpans;

public class CodeEditHandler extends AbstractEditHandler<CodeSpan> {

    private MarkwonTheme theme;

    @NonNull
    public static CodeEditHandler create() {
        return new CodeEditHandler();
    }

    @Override
    public void configurePersistedSpans(@NonNull PersistedSpans.Builder builder) {
        builder.persistSpan(CodeSpan.class, () -> new CodeSpan(theme));
    }

    @Override
    public void handleMarkdownSpan(@NonNull PersistedSpans persistedSpans, @NonNull Editable editable, @NonNull String input, @NonNull CodeSpan span, int spanStart, int spanTextLength) {
        // inline spans can delimit other inline spans,
        //  for example: `**_~~hey~~_**`, this is why we must additionally find delimiter used
        //  and its actual start/end positions
        final MarkwonEditorUtils.Match match =
                MarkwonEditorUtils.findDelimited(input, spanStart, "```");
        if (match != null) {
            editable.setSpan(persistedSpans.get(CodeSpan.class), match.start(), match.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    @NonNull
    @Override
    public Class<CodeSpan> markdownSpanType() {
        return CodeSpan.class;
    }

    @Override
    public void init(@NonNull Markwon markwon) {
        theme = markwon.configuration().theme();
    }
}
