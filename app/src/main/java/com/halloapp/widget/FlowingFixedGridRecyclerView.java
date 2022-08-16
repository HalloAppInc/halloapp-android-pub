package com.halloapp.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.R;

public class FlowingFixedGridRecyclerView extends RecyclerView {

    private GridLayoutManager layoutManager;

    private float columnWidth;
    private int columnCount = 3;

    public FlowingFixedGridRecyclerView(@NonNull Context context) {
        super(context);
        init(context, null, 0);
    }

    public FlowingFixedGridRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public FlowingFixedGridRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.FlowingFixedGridRecyclerView, defStyle, 0);
        columnCount = a.getInt(R.styleable.FlowingFixedGridRecyclerView_ffgrvInitColCount, columnCount);
        columnWidth = a.getDimension(R.styleable.FlowingFixedGridRecyclerView_ffgrvItemWidth, 1f);
        a.recycle();
        layoutManager = new GridLayoutManager(context, columnCount);

        setLayoutManager(layoutManager);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int width = r - l;
        if (width > 0) {
            int columns = Math.max(1, (int) (width / columnWidth));

            if (columns != columnCount) {
                layoutManager.setSpanCount(columns);
                columnCount = columns;
            }
        }
        super.onLayout(changed, l, t, r, b);
    }
}
