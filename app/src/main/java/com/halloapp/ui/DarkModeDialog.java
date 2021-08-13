package com.halloapp.ui;

import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;

import com.halloapp.Preferences;
import com.halloapp.R;
import com.halloapp.util.BgWorkers;

public class DarkModeDialog extends AlertDialog.Builder {

    private final BgWorkers bgWorkers = BgWorkers.getInstance();
    private final Preferences preferences = Preferences.getInstance();

    public DarkModeDialog(@NonNull Context context) {
        super(context);

        setTitle(R.string.dark_mode_dialog_title);
        int nightModePref = AppCompatDelegate.getDefaultNightMode();
        String[] options = new String[] {
                context.getResources().getString(R.string.dark_mode),
                context.getResources().getString(R.string.light_mode),
                Build.VERSION.SDK_INT < 29 ? context.getResources().getString(R.string.default_setting) : context.getResources().getString(R.string.system_default)
        };
        int[] modes = new int[] {
                AppCompatDelegate.MODE_NIGHT_YES,
                AppCompatDelegate.MODE_NIGHT_NO,
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        };
        int startIndex = (nightModePref == AppCompatDelegate.MODE_NIGHT_YES) ? 0 : (nightModePref == AppCompatDelegate.MODE_NIGHT_NO) ? 1 : 2;
        setSingleChoiceItems(options, startIndex, (dialogInterface, i) -> {
            bgWorkers.execute(() -> {
                preferences.setNightMode(modes[i]);
            });
            dialogInterface.dismiss();
            AppCompatDelegate.setDefaultNightMode(modes[i]);
        });
    }
}
