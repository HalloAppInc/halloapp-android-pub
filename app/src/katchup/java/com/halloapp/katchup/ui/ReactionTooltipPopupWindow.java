package com.halloapp.katchup.ui;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;

import com.halloapp.R;

public class ReactionTooltipPopupWindow extends PopupWindow {
    private static final int AUTO_DISMISS_DELAY_MS = 3000;

    public ReactionTooltipPopupWindow(@NonNull Context context) {
        super(context);

        View root = LayoutInflater.from(context).inflate(R.layout.popup_video_reaction_info, null, false);
        setContentView(root);

        setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        setOutsideTouchable(true);
        setFocusable(false);
    }

    public void show(@NonNull View anchor) {
        View contentView = getContentView();
        contentView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

        showAsDropDown(anchor, 0, -contentView.getMeasuredHeight() - anchor.getHeight());

        contentView.postDelayed(this::dismiss, AUTO_DISMISS_DELAY_MS);
    }
}
