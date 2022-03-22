package com.halloapp.ui.posts;

import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.halloapp.Constants;
import com.halloapp.R;
import com.halloapp.util.IntentUtils;
import com.halloapp.util.logs.Log;

public class TombstonePostViewHolder extends PostViewHolder {

    private final TextView tombstoneText;

    public TombstonePostViewHolder(@NonNull View itemView, @NonNull PostViewHolderParent parent) {
        super(itemView, parent);

        tombstoneText = itemView.findViewById(R.id.tombstone_text);

        SpannableStringBuilder current = new SpannableStringBuilder(tombstoneText.getText());
        URLSpan[] spans = current.getSpans(0, current.length(), URLSpan.class);

        int linkColor = ContextCompat.getColor(tombstoneText.getContext(), R.color.color_link);

        for (URLSpan span : spans) {
            int start = current.getSpanStart(span);
            int end = current.getSpanEnd(span);
            current.removeSpan(span);

            ClickableSpan learnMoreSpan = new ClickableSpan() {
                @Override
                public void updateDrawState(@NonNull TextPaint ds) {
                    ds.setUnderlineText(false);
                    ds.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
                    ds.setColor(linkColor);
                }

                @Override
                public void onClick(@NonNull View widget) {
                    IntentUtils.openUrlInBrowser(widget, Constants.FAQ_URL);
                }
            };
            current.setSpan(learnMoreSpan, start, end, 0);
        }
        tombstoneText.setText(current);
        tombstoneText.setMovementMethod(LinkMovementMethod.getInstance());
    }

}

