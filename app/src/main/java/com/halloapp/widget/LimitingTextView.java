package com.halloapp.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Layout;
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

    private int lineLimit = 12;
    private int lineStep = 12;
    private SpannableString readMoreText;
    private final SpannableStringBuilder truncatedText = new SpannableStringBuilder();
    private CharSequence originalText;
    private OnReadMoreListener listener;
    private int lastMeasureWidth;
    private int lastMeasureHeight;
    private boolean truncated;

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
        readMoreText.setSpan(new ReadMoreSpan(), 2, readMoreText.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
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
        if (lineLimit != Integer.MAX_VALUE && layout.getLineCount() > lineLimit + lineStep / 2 && (lastMeasureWidth != widthMeasureSpec || lastMeasureHeight != heightMeasureSpec)) {
            truncated = true;
            lastMeasureWidth = widthMeasureSpec;
            lastMeasureHeight = heightMeasureSpec;

            final int lineBottom = layout.getLineBottom(lineLimit - 1);
            final float readMoreTextSize = layout.getPaint().measureText(readMoreText.toString());
            final float lastLineRight = layout.getLineRight(lineLimit - 1);
            final int truncatePos = layout.getOffsetForHorizontal(lineLimit - 1, Math.max(lastLineRight - readMoreTextSize, 0));

            truncatedText.clear();
            truncatedText.clearSpans();
            truncatedText.append(originalText.subSequence(0, truncatePos));
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
