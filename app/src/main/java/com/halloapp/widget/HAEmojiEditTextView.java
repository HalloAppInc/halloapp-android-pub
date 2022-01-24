package com.halloapp.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.emoji2.viewsintegration.EmojiEditTextHelper;

//TODO (clark): remove this class when we migrate to appcompat 1.4.1

/**
 * Temporary class that allows use to support emojis on older devices using emoji2 library
 */
public class HAEmojiEditTextView extends AppCompatEditText {

    public HAEmojiEditTextView(@NonNull Context context) {
        super(context);
        init();
    }

    public HAEmojiEditTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HAEmojiEditTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private EmojiEditTextHelper emojiEditTextHelper;
    private boolean initialized = false;

    private void init() {
        if (!initialized) {
            initialized = true;
            super.setKeyListener(getEmojiEditTextHelper().getKeyListener(getKeyListener()));
        }
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        InputConnection inputConnection = super.onCreateInputConnection(outAttrs);
        return getEmojiEditTextHelper().onCreateInputConnection(inputConnection, outAttrs);
    }

    @Override
    public void setKeyListener(android.text.method.KeyListener keyListener) {
        super.setKeyListener(getEmojiEditTextHelper().getKeyListener(keyListener));
    }

    private EmojiEditTextHelper getEmojiEditTextHelper() {
        if (emojiEditTextHelper == null) {
            emojiEditTextHelper = new EmojiEditTextHelper(this, false);
        }
        return emojiEditTextHelper;
    }
}
