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

public class FabExpandOnScrollListener extends RecyclerView.OnScrollListener implements NestedScrollView.OnScrollChangeListener{
    private final HACustomFab fabView;

    public interface Host {
        HACustomFab getFab();
    }

    public FabExpandOnScrollListener(@NonNull AppCompatActivity activity) {
        if (activity instanceof Host) {
            fabView = ((Host) activity).getFab();
        } else {
            fabView = null;
        }
    }

    @CallSuper
    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        if (fabView != null) {
            final RecyclerView.LayoutManager layoutManager = Preconditions.checkNotNull(recyclerView.getLayoutManager());
            final View childView = layoutManager.getChildAt(0);
            final boolean scrolled = childView == null || !(childView.getTop() == 0 && layoutManager.getPosition(childView) == 0);
            if (scrolled) {
                fabView.collapse();
            } else {
                fabView.expand();
            }
        }
    }

    @Override
    public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        if (fabView != null) {
            if (scrollY > 200) {
                fabView.collapse();
            }
        }
    }

}
