package com.halloapp.widget;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.os.BuildCompat;
import androidx.core.view.inputmethod.EditorInfoCompat;
import androidx.core.view.inputmethod.InputConnectionCompat;

public class PostEditText extends AppCompatEditText {

    private PreImeListener preImeListener;
    private MediaInputListener mediaInputListener;

    public interface PreImeListener {
        boolean onKeyPreIme(int keyCode, KeyEvent event);
    }

    public interface MediaInputListener {
        void onMediaInput(Uri uri);
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

    public void setMediaInputListener(MediaInputListener listener) {
        mediaInputListener = listener;
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (preImeListener != null && preImeListener.onKeyPreIme(keyCode, event)) {
            return true;
        }
        return super.onKeyPreIme(keyCode, event);
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo editorInfo) {
        final InputConnection inputConnection = super.onCreateInputConnection(editorInfo);
        if (mediaInputListener == null) {
            return inputConnection;
        }
        EditorInfoCompat.setContentMimeTypes(editorInfo, new String [] {"image/*"});
        final InputConnectionCompat.OnCommitContentListener callback =
                (inputContentInfo, flags, opts) -> {
                    if (BuildCompat.isAtLeastNMR1() && (flags & InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION) != 0) {
                        try {
                            inputContentInfo.requestPermission();
                        } catch (Exception e) {
                            return false;
                        }
                    }
                    if (mediaInputListener != null) {
                        mediaInputListener.onMediaInput(inputContentInfo.getContentUri());
                    }
                    return true;
                };
        return InputConnectionCompat.createWrapper(inputConnection, editorInfo, callback);
    }
}
