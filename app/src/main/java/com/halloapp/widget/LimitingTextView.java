package com.halloapp.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import com.halloapp.R;

public class LimitingTextView extends AppCompatTextView {

    private int initialLimit = 512;
    private int limit = initialLimit;
    private int step = 768;
    private CharSequence readMoreText;
    private CharSequence originalText;
    private ReadMoreListener listener;

    public LimitingTextView(Context context) {
        super(context);
        init(null, 0);
    }

    public LimitingTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public LimitingTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    private void init(AttributeSet attrs, int defStyle) {
        final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.LimitingTextView, defStyle, 0);
        readMoreText = a.getText(R.styleable.LimitingTextView_ltvReadMore);
        limit = a.getInt(R.styleable.LimitingTextView_ltvLimit, limit);
        initialLimit = limit;
        step = a.getInt(R.styleable.LimitingTextView_ltvStep, step);
        a.recycle();
    }

    public void setReadMoreListener(@Nullable ReadMoreListener listener) {
        this.listener = listener;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public void resetLimit() {
        this.limit = initialLimit;
    }

    public void setText(CharSequence text, BufferType type) {

        final CharSequence truncatedText;
        if (text != null && text.length() > limit + step / 2) {
            final SpannableStringBuilder stringBuilder = new SpannableStringBuilder(text.subSequence(0, limit));
            final SpannableString readMore = new SpannableString(readMoreText);
            readMore.setSpan(new ReadMoreSpan(), 0, readMore.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            stringBuilder.append("… ");
            stringBuilder.append(readMore);
            truncatedText = stringBuilder;
            setMovementMethod(LinkMovementMethod.getInstance());
        } else {
            truncatedText = text;
        }
        originalText = text;
        super.setText(truncatedText, type);
    }

    private void onReadMore() {
        limit += step;
        setText(originalText);
        if (listener != null) {
            listener.onReadMore(this, limit);
        }
    }

    public interface ReadMoreListener {

        void onReadMore(TextView view, int limit);
    }

    class ReadMoreSpan extends ClickableSpan {

        @Override
        public void onClick(@NonNull View widget) {
            onReadMore();
        }

        @Override
        public void updateDrawState(@NonNull TextPaint ds) {
            ds.setColor(ds.linkColor);
            ds.setUnderlineText(false);
        }
    }
}
