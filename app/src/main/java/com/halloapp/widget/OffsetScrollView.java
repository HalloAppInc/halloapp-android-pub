package com.halloapp.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

public class OffsetScrollView extends ScrollView {

    private int offset;

    public OffsetScrollView(Context context) {
        super(context);
    }

    public OffsetScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public OffsetScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public OffsetScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setOffset(int offset){
        this.offset = offset;
    }

    @Override
    public void scrollTo(int x, int y) {
        if (y < getScrollY()) {
            super.scrollTo(x, y);
        } else {
            super.scrollTo(x, y + this.offset);
        }
    }
}
