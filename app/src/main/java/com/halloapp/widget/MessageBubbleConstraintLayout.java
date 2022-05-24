package com.halloapp.widget;

import android.content.Context;
import android.graphics.Outline;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.halloapp.R;

public class MessageBubbleConstraintLayout extends ConstraintLayout {

    public MessageBubbleConstraintLayout(@NonNull Context context) {
        super(context);

        init(context);
    }

    public MessageBubbleConstraintLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init(context);
    }

    public MessageBubbleConstraintLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context);
    }

    private void init(@NonNull Context context) {
        final int radius = context.getResources().getDimensionPixelSize(R.dimen.message_bubble_corner_radius);
        final int elevation = context.getResources().getDimensionPixelSize(R.dimen.message_bubble_elevation);
        setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), radius);
            }
        });
        setClipToOutline(true);
        setElevation(elevation);
    }
}
