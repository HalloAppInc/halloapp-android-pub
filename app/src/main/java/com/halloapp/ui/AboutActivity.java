package com.halloapp.ui;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.R;
import com.halloapp.ui.contacts.ContactHashInfoBottomSheetDialogFragment;
import com.halloapp.util.DialogFragmentUtils;

public class AboutActivity extends HalloActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_about);

        TextView footer = findViewById(R.id.about_footer);
        footer.setMovementMethod(LinkMovementMethod.getInstance());

        SpannableStringBuilder current= new SpannableStringBuilder(getString(R.string.about_footer));
        Linkify.addLinks(current, Linkify.EMAIL_ADDRESSES);

        URLSpan[] spans= current.getSpans(0, current.length(), URLSpan.class);

        for (URLSpan span : spans) {
            int start = current.getSpanStart(span);
            int end = current.getSpanEnd(span);
            current.removeSpan(span);

            ClickableSpan learnMoreSpan = new ClickableSpan() {
                @Override
                public void updateDrawState(@NonNull TextPaint ds) {
                    ds.setUnderlineText(false);
                    ds.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
                }

                @Override
                public void onClick(@NonNull View widget) {
                    span.onClick(widget);
                }
            };
            current.setSpan(learnMoreSpan, start, end, 0);
        }
        footer.setText(current);
    }
}
