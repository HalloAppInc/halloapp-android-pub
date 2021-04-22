package com.halloapp.util;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

import androidx.annotation.MainThread;

import com.halloapp.AppContext;

public class ClipUtils {

    @MainThread
    public static void copyToClipboard(String text) {
        ThreadUtils.runWithoutStrictModeRestrictions(() -> {
            ClipboardManager clipboard = (ClipboardManager) AppContext.getInstance().get().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText(null, text);
            clipboard.setPrimaryClip(clip);
        });
    }
}
