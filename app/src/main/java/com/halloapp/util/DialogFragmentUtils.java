package com.halloapp.util;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

public class DialogFragmentUtils {

    public static void showDialogFragmentOnce(DialogFragment dialogFragment, FragmentManager fragmentManager) {
        String tag = dialogFragment.getClass().getName();
        if (fragmentManager.findFragmentByTag(tag) == null) {
            fragmentManager.beginTransaction().add(dialogFragment, tag).commit();
        }
    }

}
