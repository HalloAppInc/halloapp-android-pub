package com.halloapp.emoji;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.content.ContextCompat;

import com.halloapp.Preferences;
import com.halloapp.R;
import com.halloapp.util.KeyboardUtils;
import com.halloapp.util.ViewUtils;
import com.halloapp.util.logs.Log;

import java.lang.reflect.Field;

public class EmojiKeyboardLayout extends FrameLayout {

    private final Preferences preferences = Preferences.getInstance();

    private final Rect tempRect = new Rect();

    private int verticalInset;

    private int statusBarHeight;

    private int defaultKeyboardHeight;
    private int minSoftkeyboardHeight;

    private boolean softKeyboardOpen;
    private boolean emojiKeyboardOpen;

    private EmojiPickerView emojiPickerView;

    private ImageView keyboardToggle;

    private Runnable onSoftKeyboardHidden;

    private EditText input;

    public EmojiKeyboardLayout(@NonNull Context context) {
        this(context, null);
    }

    public EmojiKeyboardLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EmojiKeyboardLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(@NonNull Context context) {
        setBackgroundColor(ContextCompat.getColor(context, R.color.emoji_picker_background));
        minSoftkeyboardHeight = context.getResources().getDimensionPixelSize(R.dimen.min_softkeyboard_height);
        defaultKeyboardHeight = context.getResources().getDimensionPixelSize(R.dimen.default_emoji_keyboard_height);

        statusBarHeight = ViewUtils.getStatusBarHeight(context);
        verticalInset = computeVerticalInsetReflect();

        emojiPickerView = new EmojiPickerView(context);
        addView(emojiPickerView, ViewGroup.LayoutParams.MATCH_PARENT, getKeyboardHeight());
        emojiPickerView.setVisibility(View.GONE);
    }

    public void showEmojiKeyboard() {
        if (softKeyboardOpen) {
            keyboardToggle.setImageResource(R.drawable.ic_keyboard);
            ViewGroup.LayoutParams params = emojiPickerView.getLayoutParams();
            params.height = getKeyboardHeight();
            emojiPickerView.setLayoutParams(params);
            KeyboardUtils.hideSoftKeyboard(input);
            onSoftKeyboardHidden = () -> {
                emojiPickerView.setVisibility(View.VISIBLE);
                emojiKeyboardOpen = true;
            };
        } else {
            showEmojiKeyboardInternal();
        }
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

    public boolean isEmojiKeyboardOpen() {
        return emojiKeyboardOpen;
    }

    private void showEmojiKeyboardInternal() {
        keyboardToggle.setImageResource(R.drawable.ic_keyboard);
        emojiPickerView.setVisibility(View.VISIBLE);
        ViewGroup.LayoutParams params = emojiPickerView.getLayoutParams();
        params.height = getKeyboardHeight();
        emojiPickerView.setLayoutParams(params);
        emojiKeyboardOpen = true;
    }

    public void hideEmojiKeyboard() {
        keyboardToggle.setImageResource(R.drawable.ic_emoji_smilies);
        emojiPickerView.setVisibility(View.GONE);
        emojiKeyboardOpen = false;
    }

    private void showSoftKeyboard() {
        KeyboardUtils.showSoftKeyboard(input);
    }


    public void bind(ImageView toggle, EditText editText) {
        this.input = editText;
        emojiPickerView.bindEditText(editText);
        this.keyboardToggle = toggle;
        toggle.setOnClickListener(v -> {
            if (emojiKeyboardOpen) {
                showSoftKeyboard();
            } else {
                showEmojiKeyboard();
            }
        });
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        computeVerticalInset();
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

    private void onKeyboardOpened() {
        softKeyboardOpen = true;
        if (emojiKeyboardOpen) {
            hideEmojiKeyboard();
        }
    }

    private void onKeyboardClosed() {
        softKeyboardOpen = false;
        if (onSoftKeyboardHidden != null) {
            onSoftKeyboardHidden.run();
            onSoftKeyboardHidden = null;
        }
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

    public boolean isLandscape() {
        return getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        checkKeyboard();
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
