package com.halloapp.ui.moments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.constraintlayout.motion.widget.TransitionAdapter;
import androidx.core.view.GestureDetectorCompat;

import com.halloapp.R;
import com.halloapp.content.Post;
import com.halloapp.ui.posts.MomentPostViewHolder;
import com.halloapp.ui.posts.PostViewHolder;

import java.util.ArrayList;
import java.util.List;

public class MomentsStackLayout extends MotionLayout {
    private boolean isSideScrolling;

    // Prevents interference from child click events and parent scroll events
    private final GestureDetectorCompat detector = new GestureDetectorCompat(getContext(), new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (Math.abs(e1.getX() - e2.getX()) > Math.abs(e1.getY() - e2.getY())) {
                isSideScrolling = true;

                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
            }

            return false;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (isInteractionEnabled()) {
                setInteractionEnabled(false);
                transitionToEnd(() -> {
                    setInteractionEnabled(true);
                });
            }

            return super.onFling(e1, e2, velocityX, velocityY);
        }
    });

    private final MotionLayout.TransitionListener transitionListener = new TransitionAdapter() {
        @Override
        public void onTransitionCompleted(MotionLayout layout, int currentId) {
            super.onTransitionCompleted(layout, currentId);

            if (currentId == R.id.momentLeftEnd || currentId == R.id.momentRightEnd) {
                moments.add(moments.remove(0));

                layout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        if (layout.getProgress() > 0) {
                            return;
                        }

                        layout.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                        if (moments.size() > 1) {
                            bind(1);
                        }

                        if (moments.size() > 2) {
                            bind(2);
                        }
                    }
                });

                bind(0);
                layout.setProgress(0);
            }
        }
    };

    private final ArrayList<MomentPostViewHolder> holders = new ArrayList<>();
    private final List<Post> moments = new ArrayList<>();

    public MomentsStackLayout(@NonNull Context context) {
        super(context);
    }

    public MomentsStackLayout(@NonNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MomentsStackLayout(@NonNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        detector.onTouchEvent(event);
        return isSideScrolling || super.onInterceptTouchEvent(event);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        detector.onTouchEvent(event);

        if (event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP) {
            isSideScrolling = false;
            getParent().requestDisallowInterceptTouchEvent(false);
        }

        return super.onTouchEvent(event);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        for (MomentPostViewHolder holder : holders) {
            holder.markDetach();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        for (MomentPostViewHolder holder : holders) {
            holder.markAttach();
        }
    }

    public void load(PostViewHolder.PostViewHolderParent parent) {
        holders.add(new MomentPostViewHolder(findViewById(R.id.top), parent));
        holders.add(new MomentPostViewHolder(findViewById(R.id.middle), parent));
        holders.add(new MomentPostViewHolder(findViewById(R.id.bottom), parent));
    }

    public void bindTo(@NonNull List<Post> moments) {
        if (moments.size() > 1) {
            enableTransitions();
        } else {
            disableTransitions();
        }

        if (moments.size() == 0) {
            this.moments.clear();
            return;
        }

        List<Post> result = prepare(moments);

        if (this.moments.size() == 0 || this.moments.get(0) != result.get(0)) {
            holders.get(0).bindTo(result.get(0));
            holders.get(0).markAttach();
        }

        if (result.size() > 1 && (this.moments.size() < 2 || this.moments.get(1) != result.get(1))) {
            holders.get(1).bindTo(result.get(1));
            holders.get(1).markAttach();
        }

        if (result.size() > 2 && (this.moments.size() < 3 || this.moments.get(2) != result.get(2))) {
            holders.get(2).bindTo(result.get(2));
            holders.get(2).markAttach();
        }

        setInteractionEnabled(moments.size() > 1);

        this.moments.clear();
        this.moments.addAll(result);

        if (moments.size() == 1) {
            transitionToState(R.id.momentSingle);
            transitionToEnd();
        }
    }

    private void bind(int position) {
        MomentPostViewHolder viewHolder = holders.get(position);
        viewHolder.markDetach();
        viewHolder.bindTo(moments.get(position));
        viewHolder.markAttach();
    }

    private void enableTransitions() {
        setTransitionListener(transitionListener);
    }

    private void disableTransitions() {
        removeTransitionListener(transitionListener);
    }

    private List<Post> prepare(@NonNull List<Post> moments) {
        // keep the moments from the three top displayed cards on top on update for consistency
        ArrayList<Post> result = new ArrayList<>(moments.size());

        if (this.moments.size() > 0 && moments.contains(this.moments.get(0))) {
            result.add(this.moments.get(0));
        }

        if (this.moments.size() > 1 && moments.contains(this.moments.get(1))) {
            result.add(this.moments.get(1));
        }

        if (this.moments.size() > 2 && moments.contains(this.moments.get(2))) {
            result.add(this.moments.get(2));
        }

        for (Post m: moments) {
            if (!result.contains(m)) {
                result.add(m);
            }
        }

        return result;
    }
}
