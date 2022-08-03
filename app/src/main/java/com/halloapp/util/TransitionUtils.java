package com.halloapp.util;

import android.app.Activity;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;

import com.halloapp.R;

import java.util.List;

public class TransitionUtils {

    public static final String NAV_BAR_TRANSITION_NAME = "transition_view_nav_bar";
    public static final String STATUS_BAR_TRANSITION_NAME = "transition_view_status_bar";
    public static final String ACTION_BAR_TRANSITION_NAME = "transition_view_action_bar";

    public static void transitionSystemViews(@NonNull Activity activity, List<Pair<View, String>> pairs) {
        View decor = activity.getWindow().getDecorView();
        if (decor == null) {
            return;
        }
        View statusBar = decor.findViewById(android.R.id.statusBarBackground);
        View navBar = decor.findViewById(android.R.id.navigationBarBackground);
        View actionBar = activity.findViewById(R.id.toolbar);

        addTransitionView(pairs, statusBar, STATUS_BAR_TRANSITION_NAME);
        addTransitionView(pairs, navBar, NAV_BAR_TRANSITION_NAME);
        addTransitionView(pairs, actionBar, ACTION_BAR_TRANSITION_NAME);
    }

    public static void finishTransitionSystemViews(@NonNull Activity activity) {
        View decor = activity.getWindow().getDecorView();
        if (decor == null) {
            return;
        }
        View statusBar = decor.findViewById(android.R.id.statusBarBackground);
        View navBar = decor.findViewById(android.R.id.navigationBarBackground);
        View actionBar = activity.findViewById(R.id.toolbar);
        
        if (statusBar != null) {
            statusBar.setTransitionName(STATUS_BAR_TRANSITION_NAME);
        }
        if (navBar != null) {
            navBar.setTransitionName(NAV_BAR_TRANSITION_NAME);
        }
        if (actionBar != null) {
            actionBar.setTransitionName(ACTION_BAR_TRANSITION_NAME);
        }
    }

    private static void addTransitionView(@NonNull List<Pair<View, String>> pairs, @Nullable View view, @NonNull String name) {
        if (view == null) {
            return;
        }
        view.setTransitionName(name);
        pairs.add(new Pair<>(view, name));
    }
}
