package com.halloapp.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;

import com.halloapp.R;

public class LimitingTextView extends AppCompatTextView {

    private int lineLimit = 12;
    private int lineStep = 12;
    private int lineLimitTolerance = 0; // don't cut off the text when "text lines count" <= lineLimit + lineLimitTolerance; this prevents the situation when only 1-2 lines are hidden by "read more"
    private SpannableString readMoreText;
    private final SpannableStringBuilder truncatedText = new SpannableStringBuilder();
    private CharSequence originalText;
    private OnReadMoreListener listener;
    private int lastMeasureWidth;
    private int lastMeasureHeight;
    private boolean truncated;
    private Typeface typeface;
    private TextPaint mediumPaint;

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
        lineLimit = a.getInt(R.styleable.LimitingTextView_ltvLimit, lineLimit);
        lineStep = a.getInt(R.styleable.LimitingTextView_ltvStep, lineStep);
        readMoreText = new SpannableString("… " + a.getText(R.styleable.LimitingTextView_ltvReadMore));
        readMoreText.setSpan(new ReadMoreSpan(ContextCompat.getColor(getContext(), R.color.read_more_link)), 2, readMoreText.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        a.recycle();

        setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (truncated && (lastMeasureWidth != widthMeasureSpec || lastMeasureHeight != heightMeasureSpec)) {
            super.setText(originalText, BufferType.NORMAL);
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        final Layout layout = getLayout();
        if (lineLimit != Integer.MAX_VALUE && layout.getLineCount() > lineLimit + lineLimitTolerance && (lastMeasureWidth != widthMeasureSpec || lastMeasureHeight != heightMeasureSpec)) {
            truncated = true;
            lastMeasureWidth = widthMeasureSpec;
            lastMeasureHeight = heightMeasureSpec;
            if (typeface == null) {
                typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL);
            }
            if (mediumPaint == null) {
                mediumPaint = new TextPaint();
            }
            mediumPaint.set(layout.getPaint());
            mediumPaint.setTypeface(typeface);

            final int lineBottom = layout.getLineBottom(lineLimit - 1);
            final float readMoreTextSize = mediumPaint.measureText(readMoreText.toString());
            final float lastLineRight = layout.getLineRight(lineLimit - 1);
            int truncatePos = layout.getOffsetForHorizontal(lineLimit - 1, Math.max(lastLineRight - readMoreTextSize, 0));
            truncatedText.clear();
            truncatedText.clearSpans();
            truncatedText.append(originalText);
            Linkify.addLinks(truncatedText, Linkify.WEB_URLS);
            URLSpan[] spans = truncatedText.getSpans(truncatePos, truncatePos, URLSpan.class);
            for (URLSpan span : spans) {
                int spanEnd = truncatedText.getSpanEnd(span);
                int spanStart = truncatedText.getSpanStart(span);
                if (spanEnd > truncatePos && spanStart < truncatePos) {
                    truncatePos = spanStart;
                }
            }
            truncatedText.delete(truncatePos, truncatedText.length());
            truncatedText.append(readMoreText);

            super.setText(truncatedText, BufferType.NORMAL);
            setMeasuredDimension(getMeasuredWidth(), lineBottom + getPaddingTop() + getPaddingBottom());
        }
    }

    public void setOnReadMoreListener(@Nullable OnReadMoreListener listener) {
        this.listener = listener;
    }

    /*
     * call this before setting text
     * */
    public void setLineStep(int lineStep) {
        this.lineStep = lineStep;
    }

    /*
     * call this before setting text
     * */
    public void setLineLimit(int lineLimit) {
        this.lineLimit = lineLimit;
    }

    /*
     * call this before setting text
     * */
    public void setLineLimitTolerance(int lineLimitTolerance) {
        this.lineLimitTolerance = lineLimitTolerance;
    }

    public void setText(CharSequence text, BufferType type) {
        originalText = text;
        truncated = false;
        lastMeasureWidth = 0;
        lastMeasureHeight = 0;
        super.setText(text, type);
    }

    private void onReadMore() {
        if (listener != null && listener.onReadMore(this, lineLimit + lineStep)) {
            return;
        }
        lineLimit += lineStep;
        setText(originalText);
    }

    public interface OnReadMoreListener {

        boolean onReadMore(TextView view, int limit);
    }

    class ReadMoreSpan extends ClickableSpan {

        private @ColorInt int spanColor;

        public ReadMoreSpan(@ColorInt int color) {
            this.spanColor = color;
        }

        @Override
        public void onClick(@NonNull View widget) {
            onReadMore();
        }

        @Override
        public void updateDrawState(@NonNull TextPaint ds) {
            ds.setColor(spanColor);
            ds.setUnderlineText(false);
            ds.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        }
    }
}
