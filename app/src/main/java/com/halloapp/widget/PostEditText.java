package com.halloapp.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;

import androidx.appcompat.widget.AppCompatEditText;

public class PostEditText extends AppCompatEditText {

    private PreImeListener preImeListener;

    public interface PreImeListener {
        boolean onKeyPreIme(int keyCode, KeyEvent event);
    }

    public PostEditText(Context context) {
        super(context);
    }

    public PostEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PostEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setPreImeListener(PreImeListener listener) {
        preImeListener = listener;
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (preImeListener != null && preImeListener.onKeyPreIme(keyCode, event)) {
            return true;
        }
        return super.onKeyPreIme(keyCode, event);
    }
}
