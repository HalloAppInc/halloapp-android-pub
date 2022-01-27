package com.halloapp.util;

import android.content.Context;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class KeyboardUtils {
    public static void showSoftKeyboard(EditText input) {
        InputMethodManager imm = (InputMethodManager)
                input.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        input.requestFocus();
        imm.showSoftInput(input, 0);
    }

    public static void hideSoftKeyboard(EditText input) {
        InputMethodManager imm = (InputMethodManager)
                input.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
    }
}
