package com.halloapp.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.appbar.AppBarLayout;
import com.halloapp.R;

public class CommentHeaderBehavior extends CoordinatorLayout.Behavior<View> {

    private int startXPositionImage;
    private int startYPositionImage;
    private int startToolbarHeight;

    private boolean initialised = false;

    private float amountOfToolbarToMove;
    private float amountToMoveXPosition;
    private float amountToMoveYPosition;

    private float finalToolbarHeight, finalXPosition, finalYPosition;

    public CommentHeaderBehavior(
            final Context context,
            final AttributeSet attrs) {

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CommentHeaderBehavior);
            finalXPosition = a.getDimension(R.styleable.CommentHeaderBehavior_finalXPosition, 0);
            finalYPosition = a.getDimension(R.styleable.CommentHeaderBehavior_finalYPosition, 0);
            finalToolbarHeight = a.getDimension(R.styleable.CommentHeaderBehavior_finalToolbarHeight, 0);
            a.recycle();
        }
    }

    @Override
    public boolean layoutDependsOn(
            @NonNull final CoordinatorLayout parent,
            @NonNull final View child,
            @NonNull final View dependency) {

        return dependency instanceof AppBarLayout;
    }

    @Override
    public boolean onDependentViewChanged(@NonNull final CoordinatorLayout parent, @NonNull final View child, @NonNull final View dependency) {
        initProperties(child, dependency);

        float currentToolbarHeight = Math.min(dependency.getBottom(), startToolbarHeight);

        final float amountAlreadyMoved = 1.5f * (startToolbarHeight - currentToolbarHeight);
        final float progress = Math.min(100f, 100 * amountAlreadyMoved / amountOfToolbarToMove);

        final float distanceXToSubtract = progress * amountToMoveXPosition / 100;
        final float distanceYToSubtract = progress * amountToMoveYPosition / 100;

        float newXPosition = startXPositionImage - distanceXToSubtract;
        child.setX(newXPosition);
        child.setY(startYPositionImage - distanceYToSubtract);

        return true;
    }

    private void initProperties(
            final View child,
            final View dependency) {
        startToolbarHeight = Math.max(dependency.getBottom(), startToolbarHeight);
        amountOfToolbarToMove = startToolbarHeight - finalToolbarHeight;
        if (!initialised) {
            startXPositionImage = (int) child.getX();
            startYPositionImage = (int) child.getY();
            startToolbarHeight = dependency.getHeight();
            amountToMoveXPosition = startXPositionImage - finalXPosition;
            amountToMoveYPosition = startYPositionImage - finalYPosition;
            initialised = true;
        }
    }
}
