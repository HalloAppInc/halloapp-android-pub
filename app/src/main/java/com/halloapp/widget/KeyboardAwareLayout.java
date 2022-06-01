package com.halloapp.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.WindowInsets;
import android.widget.EditText;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.Preferences;
import com.halloapp.R;
import com.halloapp.util.ViewUtils;

import java.lang.reflect.Field;

public abstract class KeyboardAwareLayout extends FrameLayout {

    private final Preferences preferences = Preferences.getInstance();

    private final Rect tempRect = new Rect();

    private int verticalInset;

    private int statusBarHeight;

    private int defaultKeyboardHeight;
    private int minSoftkeyboardHeight;

    private boolean softKeyboardOpen;

    public KeyboardAwareLayout(@NonNull Context context) {
        this(context, null);
    }

    public KeyboardAwareLayout(@NonNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KeyboardAwareLayout(@NonNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(@NonNull Context context) {
        minSoftkeyboardHeight = context.getResources().getDimensionPixelSize(R.dimen.min_softkeyboard_height);
        defaultKeyboardHeight = context.getResources().getDimensionPixelSize(R.dimen.default_emoji_keyboard_height);

        statusBarHeight = ViewUtils.getStatusBarHeight(context);
        verticalInset = computeVerticalInsetReflect();
    }

    private void computeVerticalInset() {
        if (Build.VERSION.SDK_INT >= 23 && verticalInset == 0 && getRootWindowInsets() != null) {
            WindowInsets insets = getRootWindowInsets();
            int navInset;
            if (Build.VERSION.SDK_INT < 30) {
                navInset = insets.getStableInsetBottom();
            } else {
                navInset = insets.getInsetsIgnoringVisibility(WindowInsets.Type.navigationBars()).bottom;
            }
            if (navInset != 0 && verticalInset == 0) {
                verticalInset = navInset;
            }
        }
    }

    private int computeVerticalInsetReflect() {
        Field attachInfoField;
        try {
            attachInfoField = View.class.getDeclaredField("mAttachInfo");
            attachInfoField.setAccessible(true);
            Object attachInfo = attachInfoField.get(this);
            if (attachInfo != null) {
                Field stableInsetsField = attachInfo.getClass().getDeclaredField("mStableInsets");
                stableInsetsField.setAccessible(true);
                Rect insets = (Rect) stableInsetsField.get(attachInfo);
                if (insets != null) {
                    return insets.bottom;
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
        }
        // Fall back to status bar height as an ideally non-zero value
        return statusBarHeight;
    }

    public int getKeyboardHeight() {
        if (isLandscape()) {
            return preferences.getKeyboardHeightLandscape(defaultKeyboardHeight);
        } else {
            return preferences.getKeyboardHeightPortrait(defaultKeyboardHeight);
        }
    }

    private void updateSavedKeyboardHeight(int newHeight) {
        if (isLandscape()) {
            preferences.setKeyboardHeightLandscape(newHeight);
        } else {
            preferences.setKeyboardHeightPortrait(newHeight);
        }
    }

    public boolean isLandscape() {
        return getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    private void onKeyboardOpened() {
        softKeyboardOpen = true;
        onOpenKeyboard();
    }

    private void onKeyboardClosed() {
        softKeyboardOpen = false;
        onCloseKeyboard();
    }

    public boolean isSoftKeyboardOpen() {
        return softKeyboardOpen;
    }

    public boolean isKeyboardOpen() {
        return isSoftKeyboardOpen();
    }

    protected abstract void onCloseKeyboard();
    protected abstract void onOpenKeyboard();

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        computeVerticalInset();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        checkKeyboard();
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void checkKeyboard() {
        getWindowVisibleDisplayFrame(tempRect);

        int keyboardHeight = getRootView().getHeight() - verticalInset - tempRect.bottom;

        if (keyboardHeight > minSoftkeyboardHeight) {
            if (getKeyboardHeight() != keyboardHeight) {
                updateSavedKeyboardHeight(keyboardHeight);
            }
            if (!softKeyboardOpen) {
                onKeyboardOpened();
            }
        } else if (softKeyboardOpen) {
            onKeyboardClosed();
        }
    }

}
