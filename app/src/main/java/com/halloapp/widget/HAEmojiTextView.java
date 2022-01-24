package com.halloapp.widget;

import android.content.Context;
import android.text.InputFilter;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.emoji2.viewsintegration.EmojiTextViewHelper;

//TODO (clark): remove this class when we migrate to appcompat 1.4.1

/**
 * Temporary class that allows use to support emojis on older devices using emoji2 library
 */
public class HAEmojiTextView extends AppCompatTextView {

    public HAEmojiTextView(@NonNull Context context) {
        super(context);
        init();
    }

    public HAEmojiTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HAEmojiTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private EmojiTextViewHelper emojiTextViewHelper;
    private boolean initialized = false;

    private void init() {
        if (!initialized) {
            initialized = true;
            getEmojiTextViewHelper().updateTransformationMethod();
        }
    }

    @Override
    public void setFilters(@NonNull InputFilter[] filters) {
        super.setFilters(getEmojiTextViewHelper().getFilters(filters));
    }

    @Override
    public void setAllCaps(boolean allCaps) {
        super.setAllCaps(allCaps);
        getEmojiTextViewHelper().setAllCaps(allCaps);
    }

    private EmojiTextViewHelper getEmojiTextViewHelper() {
        if (emojiTextViewHelper == null) {
            emojiTextViewHelper = new EmojiTextViewHelper(this, false);
        }
        return emojiTextViewHelper;
    }
}
