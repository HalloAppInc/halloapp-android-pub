package com.halloapp.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.halloapp.R;

public class BlueToast {
    private static void makeBlue(@NonNull Context context, @NonNull Toast toast) {
        View tv = toast.getView();
        tv.getBackground().setColorFilter(context.getResources().getColor(R.color.color_secondary), PorterDuff.Mode.SRC_IN);

        TextView text = tv.findViewById(android.R.id.message);
        text.setTextColor(Color.WHITE);
    }

    public static void showCenter(@NonNull Context context, @StringRes int id) {
        final Toast toast = Toast.makeText(context.getApplicationContext(), id, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        makeBlue(context, toast);
        toast.show();
    }

    public static void showCenter(@NonNull Context context, @NonNull CharSequence text) {
        final Toast toast = Toast.makeText(context.getApplicationContext(), text, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        makeBlue(context, toast);
        toast.show();
    }

    public static void show(@NonNull Context context, @StringRes int id) {
        final Toast toast = Toast.makeText(context.getApplicationContext(), id, Toast.LENGTH_SHORT);
        makeBlue(context, toast);
        toast.show();
    }

    public static void show(@NonNull Context context, @NonNull CharSequence text) {
        final Toast toast = Toast.makeText(context.getApplicationContext(), text, Toast.LENGTH_SHORT);
        makeBlue(context, toast);
        toast.show();
    }
}
