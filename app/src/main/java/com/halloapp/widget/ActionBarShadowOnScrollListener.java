package com.halloapp.widget;

import android.view.View;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.R;
import com.halloapp.util.Preconditions;

public class ActionBarShadowOnScrollListener extends RecyclerView.OnScrollListener implements NestedScrollView.OnScrollChangeListener{

    private final float scrolledElevation;
    private final ActionBar actionBar;
    private final View toolbarView;

    public interface Host {
        View getToolbarView();
    }

    public ActionBarShadowOnScrollListener(@NonNull AppCompatActivity activity) {
        scrolledElevation = activity.getResources().getDimension(R.dimen.action_bar_elevation);
        if (activity instanceof Host) {
            toolbarView = ((Host) activity).getToolbarView();
        } else {
            toolbarView = null;
        }
        actionBar = activity.getSupportActionBar();
    }

    public void resetElevation() {
        updateElevation(0);
    }

    @CallSuper
    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        final RecyclerView.LayoutManager layoutManager = Preconditions.checkNotNull(recyclerView.getLayoutManager());
        final View childView = layoutManager.getChildAt(0);
        final boolean scrolled = childView == null || !(childView.getTop() >= recyclerView.getPaddingTop() && layoutManager.getPosition(childView) == 0);
        final float elevation = scrolled ? scrolledElevation : 0;
        updateElevation(elevation);
    }

    @Override
    public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        updateElevation(scrollY == 0 ? 0 : scrolledElevation);
    }

    private void updateElevation(float elevation) {
        if (toolbarView != null) {
            if (toolbarView.getElevation() != elevation) {
                toolbarView.setElevation(elevation);
            }
        } else {
            if (actionBar.getElevation() != elevation) {
                actionBar.setElevation(elevation);
            }
        }
    }
}
