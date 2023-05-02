package com.halloapp.ui.posts;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.R;

import java.util.Locale;

public class PostAttributionLayout extends LinearLayout {

    public PostAttributionLayout(Context context) {
        super(context);

        init(getContext());
    }

    public PostAttributionLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init(getContext());
    }

    public PostAttributionLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(getContext());
    }

    public void setGroupAttributionVisible(boolean visible) {
        if (visible) {
            groupView.setVisibility(View.VISIBLE);
            arrowView.setVisibility(View.VISIBLE);
        } else {
            groupView.setVisibility(View.GONE);
            arrowView.setVisibility(View.GONE);
        }
    }

    private TextView nameView;
    private TextView groupView;

    private View arrowView;

    private int layout = 0;

    private void init(@NonNull Context context) {
        inflate(context, R.layout.post_attribution_header, this);

        nameView = findViewById(R.id.name);
        groupView = findViewById(R.id.group_name);

        arrowView = findViewById(R.id.arrow);

        setGroupAttributionVisible(false);
    }

    public @NonNull TextView getNameView() {
        return nameView;
    }

    public @NonNull TextView getGroupView() {
        return groupView;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (groupView.getVisibility() == View.GONE || nameView.getVisibility() == View.GONE) {
            layout = 0;
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int availableWidth = width - getPaddingRight() - getPaddingLeft();
        if (widthMode == MeasureSpec.UNSPECIFIED) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } else if (widthMode == MeasureSpec.AT_MOST) {
            measureChildren(widthMeasureSpec, heightMeasureSpec);

            int singleLineWidth = nameView.getMeasuredWidth() + arrowView.getMeasuredWidth() + groupView.getMeasuredWidth();
            if (singleLineWidth <= availableWidth) {
                layout = 0;
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                return;
            }
            layout = 1;
            int firstLine = nameView.getMeasuredWidth() + arrowView.getMeasuredWidth();
            int secondLine = groupView.getMeasuredWidth();

            if (firstLine < availableWidth && secondLine < availableWidth) {
                width = Math.max(firstLine, secondLine) + getPaddingLeft() + getPaddingRight();
            }

            int firstLineHeight = Math.max(nameView.getMeasuredHeight(), arrowView.getMeasuredHeight());

            setMeasuredDimension(width, firstLineHeight + groupView.getMeasuredHeight());
        } else {
            measureChildren(widthMeasureSpec, heightMeasureSpec);
            int singleLineWidth = nameView.getMeasuredWidth() + arrowView.getMeasuredWidth() + groupView.getMeasuredWidth();
            if (singleLineWidth <= availableWidth) {
                layout = 0;
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                return;
            }
            if (nameView.getMeasuredWidth() + arrowView.getMeasuredWidth() > availableWidth) {
                nameView.measure(MeasureSpec.makeMeasureSpec(availableWidth - arrowView.getMeasuredWidth(), MeasureSpec.EXACTLY), heightMeasureSpec);
            }

            layout = 1;

            int firstLineHeight = Math.max(nameView.getMeasuredHeight(), arrowView.getMeasuredHeight());
            setMeasuredDimension(width, firstLineHeight + groupView.getMeasuredHeight());
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (groupView.getVisibility() == View.GONE || nameView.getVisibility() == View.GONE) {
            super.onLayout(changed, l, t, r, b);
            return;
        }
        boolean isRtl = TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == LAYOUT_DIRECTION_RTL;
        if (isRtl) {
            int totalWidth = getMeasuredWidth();
            int right = getPaddingRight();
            int h = nameView.getBaseline();
            nameView.layout(totalWidth - right - nameView.getMeasuredWidth(), 0, totalWidth - right, nameView.getMeasuredHeight());
            right += nameView.getMeasuredWidth();
            arrowView.layout(totalWidth - right - arrowView.getMeasuredWidth(), h - arrowView.getMeasuredHeight(), totalWidth - right, h);
            if (layout == 1) {
                right = getPaddingRight();
                groupView.layout(totalWidth - right - groupView.getMeasuredWidth(), nameView.getMeasuredHeight(), totalWidth - right, nameView.getMeasuredHeight() + groupView.getMeasuredHeight());
            } else {
                right += arrowView.getMeasuredWidth();
                groupView.layout(totalWidth - right - groupView.getMeasuredWidth(), 0, totalWidth - right, groupView.getMeasuredHeight());
            }
        } else {
            int left = getPaddingLeft();
            int h = nameView.getBaseline();
            nameView.layout(left, 0, left + nameView.getMeasuredWidth(), nameView.getMeasuredHeight());
            left += nameView.getMeasuredWidth();
            arrowView.layout(left, h - arrowView.getMeasuredHeight(), left + arrowView.getMeasuredWidth(), h);
            if (layout == 1) {
                left = getPaddingLeft();
                groupView.layout(left, nameView.getMeasuredHeight(), left + groupView.getMeasuredWidth(), nameView.getMeasuredHeight() + groupView.getMeasuredHeight());
            } else {
                left += arrowView.getMeasuredWidth();
                groupView.layout(left, 0, left + groupView.getMeasuredWidth(), groupView.getMeasuredHeight());
            }
        }
    }

}
