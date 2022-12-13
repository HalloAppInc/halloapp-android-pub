package com.halloapp.emoji;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.halloapp.R;
import com.halloapp.util.KeyboardUtils;
import com.halloapp.widget.KeyboardAwareLayout;

import java.util.HashSet;
import java.util.Set;

public class EmojiKeyboardLayout extends KeyboardAwareLayout {

    private boolean emojiKeyboardOpen;

    private EmojiPickerView emojiPickerView;

    private ImageView keyboardToggle;

    private Runnable onSoftKeyboardHidden;

    private EditText input;

    public interface Listener {
        void onKeyboardOpened();
        void onKeyboardClosed();
    }

    private final Set<Listener> listeners = new HashSet<>();

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

        if (isInEditMode()) {
            return;
        }
        emojiPickerView = new EmojiPickerView(context);
        addView(emojiPickerView, ViewGroup.LayoutParams.MATCH_PARENT, getKeyboardHeight());
        emojiPickerView.setVisibility(View.GONE);
    }

    public void showEmojiKeyboard() {
        if (isSoftKeyboardOpen()) {
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
            notifyKeyboardOpened();
        }
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    @Override
    public boolean isKeyboardOpen() {
        return isSoftKeyboardOpen() || isEmojiKeyboardOpen();
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

    private void hideEmojiKeyboardInternal() {
        keyboardToggle.setImageResource(R.drawable.ic_emoji_keyboard);
        emojiPickerView.setVisibility(View.GONE);
        emojiKeyboardOpen = false;
    }

    public void hideEmojiKeyboard() {
        if (emojiKeyboardOpen) {
            hideEmojiKeyboardInternal();
            notifyKeyboardClosed();
        }
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
    protected void onCloseKeyboard() {
        if (onSoftKeyboardHidden != null) {
            onSoftKeyboardHidden.run();
            onSoftKeyboardHidden = null;
        } else {
            notifyKeyboardClosed();
        }
    }

    @Override
    protected void onOpenKeyboard() {
        if (emojiKeyboardOpen) {
            hideEmojiKeyboardInternal();
        } else {
            notifyKeyboardOpened();
        }
    }

    private void notifyKeyboardOpened() {
        for (Listener listener : listeners) {
            listener.onKeyboardOpened();
        }
    }

    private void notifyKeyboardClosed() {
        for (Listener listener : listeners) {
            listener.onKeyboardClosed();
        }
    }
}
