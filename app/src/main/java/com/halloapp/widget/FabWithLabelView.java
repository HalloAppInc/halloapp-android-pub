package com.halloapp.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.halloapp.R;

public class FabWithLabelView extends LinearLayout {

    private TextView labelView;
    private FloatingActionButton fabView;

    public FabWithLabelView(Context context) {
        super(context);

        init();
    }

    public FabWithLabelView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public FabWithLabelView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        super.setOnClickListener(l);
        fabView.setOnClickListener(l);
    }

    private void init() {
        inflate(getContext(), R.layout.view_fab_with_label, this);

        setClipChildren(false);
        setClipToPadding(false);
        fabView = findViewById(R.id.fab);
        labelView = findViewById(R.id.label);

        final int topPadding = getResources().getDimensionPixelSize(R.dimen.sub_fab_top_padding);
        final int botPadding = getResources().getDimensionPixelSize(R.dimen.sub_fab_bottom_padding);
        setPadding(0, topPadding, 0, botPadding);
    }

    public TextView getLabel() {
        return labelView;
    }

    public FloatingActionButton getFab() {
        return fabView;
    }

    public void setDrawable(@DrawableRes int res) {
        fabView.setImageResource(res);
    }

    public void setLabel(CharSequence text) {
        labelView.setText(text);
    }
}
