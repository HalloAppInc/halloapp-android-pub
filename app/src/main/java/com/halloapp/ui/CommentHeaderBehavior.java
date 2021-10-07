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

    private int startXPos;
    private int startYPos;
    private int startToolbarHeight;

    private boolean initialized = false;

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

        float currentToolbarHeight = startToolbarHeight + dependency.getY(); // current expanded height of toolbar
        currentToolbarHeight = Math.max(currentToolbarHeight, finalToolbarHeight);

        final float amountAlreadyMoved = startToolbarHeight - currentToolbarHeight;
        final float progress = 100 * amountAlreadyMoved / amountOfToolbarToMove; // how much % of expand we reached

        final float distanceXToSubtract = progress * amountToMoveXPosition / 100;
        final float distanceYToSubtract = progress * amountToMoveYPosition / 100;
        float newXPosition = startXPos - distanceXToSubtract;
        child.setX(newXPosition);
        child.setY(startYPos - distanceYToSubtract);

        return true;
    }

    private void initProperties(
            final View child,
            final View dependency) {

        if (!initialized) {
            startXPos = (int) child.getX();
            startYPos = (int) child.getY();
            startToolbarHeight = dependency.getHeight();
            amountOfToolbarToMove = startToolbarHeight - finalToolbarHeight;
            amountToMoveXPosition = startXPos - finalXPosition;
            amountToMoveYPosition = startYPos - finalYPosition;
            initialized = true;
        }
    }
}
