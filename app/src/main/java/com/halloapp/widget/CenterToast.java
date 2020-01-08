package com.halloapp.widget;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

public class CenterToast {

    public static void show(@NonNull Context context, @StringRes int id) {
        final Toast toast = Toast.makeText(context.getApplicationContext(), id, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}
