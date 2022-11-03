package com.halloapp.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.halloapp.util.Preconditions;

public class HalloBottomSheetDialog extends BottomSheetDialog {
    public HalloBottomSheetDialog(@NonNull Context context) {
        super(context);
    }

    protected HalloBottomSheetDialog(@NonNull Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    public HalloBottomSheetDialog(@NonNull Context context, @StyleRes int theme) {
        super(context, theme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (view != null) {
            BottomSheetBehavior<?> behavior = BottomSheetBehavior.from(view);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            behavior.setPeekHeight(0);
            behavior.setSkipCollapsed(true);
        }
    }
}
