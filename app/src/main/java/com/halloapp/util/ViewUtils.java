package com.halloapp.util;

import android.content.Context;
import android.graphics.Outline;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.Window;

import androidx.annotation.DimenRes;
import androidx.annotation.NonNull;

import com.halloapp.R;

public class ViewUtils {
    public static void setViewAndChildrenEnabled(View view, boolean enabled) {
        view.setEnabled(enabled);
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                setViewAndChildrenEnabled(child, enabled);
            }
        }
    }

    public static int getStatusBarHeight(Context context) {
        Rect rectangle = new Rect();
        Window window = ContextUtils.getActivity(context).getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
        return rectangle.top;
    }

    public static void clipRoundedRect(@NonNull View view, @DimenRes int radiusRes) {
        view.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                float radius= view.getResources().getDimension(radiusRes);
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), radius);
            }
        });
        view.setClipToOutline(true);
    }
}
