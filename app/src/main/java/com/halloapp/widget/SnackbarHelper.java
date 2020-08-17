package com.halloapp.widget;

import android.app.Activity;
import android.view.View;

import androidx.annotation.ColorRes;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.halloapp.R;
import com.halloapp.util.Preconditions;

public class SnackbarHelper {

    public static Snackbar makeBase(View view, CharSequence message, @BaseTransientBottomBar.Duration int duration, @ColorRes int backgroundColor) {
        Snackbar snack = Snackbar.make(view, message, duration);
        snack.setTextColor(ContextCompat.getColor(view.getContext(), R.color.white_87));
        snack.setBackgroundTint(ContextCompat.getColor(view.getContext(), backgroundColor));
        return snack;
    }

    public static Snackbar showInfo(View view, CharSequence message) {
        Snackbar snack = makeBase(view, message, Snackbar.LENGTH_LONG, R.color.color_secondary);
        snack.show();
        return snack;
    }

    public static Snackbar showInfo(View view, @StringRes int res) {
        return showInfo(view, view.getResources().getString(res));
    }

    public static Snackbar showInfo(Activity activity, CharSequence message) {
        return showInfo(getView(activity), message);
    }

    public static Snackbar showInfo(Activity activity, @StringRes int res) {
        return showInfo(getView(activity), res);
    }

    public static Snackbar showWarning(View view, CharSequence message) {
        Snackbar snack = makeBase(view, message, Snackbar.LENGTH_LONG, R.color.color_primary);
        snack.show();
        return snack;
    }

    public static Snackbar showWarning(View view, @StringRes int res) {
        return showWarning(view, view.getResources().getString(res));
    }

    public static Snackbar showWarning(Activity activity, CharSequence message) {
        return showWarning(getView(activity), message);
    }

    public static Snackbar showWarning(Activity activity, @StringRes int res) {
        return showWarning(getView(activity), res);
    }

    public static Snackbar showIndefinitely(View view, CharSequence message) {
        Snackbar snack = makeBase(view, message, Snackbar.LENGTH_INDEFINITE, R.color.color_secondary);
        snack.show();
        return snack;
    }

    public static Snackbar showIndefinitely(View view, @StringRes int res) {
        return showIndefinitely(view, view.getResources().getString(res));
    }

    private static View getView(Activity activity) {
        return Preconditions.checkNotNull(activity.findViewById(android.R.id.content));
    }
}
