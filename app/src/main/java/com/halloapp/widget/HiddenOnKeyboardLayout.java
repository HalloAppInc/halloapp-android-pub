package com.halloapp.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Layout that hides itself when the keyboard opens, and shows itself when the keyboard closes.
 */
public class HiddenOnKeyboardLayout extends KeyboardAwareLayout {
    public HiddenOnKeyboardLayout(@NonNull Context context) {
        super(context);
    }

    public HiddenOnKeyboardLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public HiddenOnKeyboardLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onCloseKeyboard() {
        setChildrenVisibility(View.VISIBLE);
    }

    private void setChildrenVisibility(int visibility) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            child.setVisibility(visibility);
        }
    }

    @Override
    protected void onOpenKeyboard() {
        setChildrenVisibility(View.GONE);
    }
}
