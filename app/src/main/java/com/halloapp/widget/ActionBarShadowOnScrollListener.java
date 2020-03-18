package com.halloapp.widget;

import android.view.View;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.R;
import com.halloapp.util.Preconditions;

public class ActionBarShadowOnScrollListener extends RecyclerView.OnScrollListener {

    private final float scrolledElevation;
    private final ActionBar actionBar;

    public ActionBarShadowOnScrollListener(@NonNull AppCompatActivity activity) {
        scrolledElevation = activity.getResources().getDimension(R.dimen.action_bar_elevation);
        actionBar = activity.getSupportActionBar();
    }

    @CallSuper
    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        final RecyclerView.LayoutManager layoutManager = Preconditions.checkNotNull(recyclerView.getLayoutManager());
        final View childView = layoutManager.getChildAt(0);
        final boolean scrolled = childView == null || !(childView.getTop() == 0 && layoutManager.getPosition(childView) == 0);
        final float elevation = scrolled ? scrolledElevation : 0;
        if (actionBar.getElevation() != elevation) {
            actionBar.setElevation(elevation);
        }
    }
}
