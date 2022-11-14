package com.halloapp.ui.moments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GestureDetectorCompat;
import androidx.transition.TransitionManager;

import com.halloapp.R;
import com.halloapp.content.MomentPost;
import com.halloapp.ui.posts.MomentPostViewHolder;
import com.halloapp.ui.posts.PostViewHolder;

import java.util.ArrayList;
import java.util.List;

public class MomentsStackLayout extends ConstraintLayout {
    private boolean isSideScrolling;

    private static final int ANIM_DURATION_MS = 200;
    private static final float CARD_TILT_ANGLE_DEG = -3;
    private static final float CARD_FINAL_ANGLE_DEG = -18;
    private static final float CARD_FINAL_Y = -90;
    private static final float PROGRESS_CANCEL_THRESHOLD = 0.5f;
    private static final float VELOCITY_CANCEL_THRESHOLD = 200;

    private float velocityCancelThreshold;
    private float cardFinalY;

    // Prevents interference from child click events and parent scroll events
    private final GestureDetectorCompat detector = new GestureDetectorCompat(getContext(), new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (moments.size() < 2) {
                return false;
            }

            if (Math.abs(e1.getX() - e2.getX()) > Math.abs(e1.getY() - e2.getY())) {
                isSideScrolling = true;

                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
            }

            if (isSideScrolling) {
                moveBy(-distanceX);
            }

            return isSideScrolling;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (moments.size() < 2) {
                return false;
            }

            if (isSideScrolling) {
                isSideScrolling = false;
                finishMove(velocityX);

                return true;
            }


            return false;
        }
    });

    private final ArrayList<MomentPostViewHolder> holders = new ArrayList<>();
    private final List<MomentPost> moments = new ArrayList<>();
    private boolean cardAnimationInProgress = false;

    public MomentsStackLayout(@NonNull Context context) {
        super(context);
        init();
    }

    public MomentsStackLayout(@NonNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MomentsStackLayout(@NonNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        float density = getResources().getDisplayMetrics().density;

        velocityCancelThreshold = VELOCITY_CANCEL_THRESHOLD * density;
        cardFinalY = CARD_FINAL_Y * density;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (cardAnimationInProgress) {
            return false;
        }

        detector.onTouchEvent(event);
        return isSideScrolling || super.onInterceptTouchEvent(event);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (cardAnimationInProgress) {
            return false;
        }

        detector.onTouchEvent(event);

        if (event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP) {
            if (isSideScrolling) {
                isSideScrolling = false;
                finishMove(0);
            }

            getParent().requestDisallowInterceptTouchEvent(false);
        }

        return true;
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

        holders.get(1).itemView.setRotation(CARD_TILT_ANGLE_DEG);
    }

    public void bindTo(@NonNull List<MomentPost> moments) {
        if (moments.size() == 0) {
            this.moments.clear();
            return;
        }

        if (this.moments.size() == 0 || this.moments.get(0) != moments.get(0)) {
            holders.get(0).bindTo(moments.get(0));
            holders.get(0).markAttach();
        }

        if (moments.size() > 1 && (this.moments.size() < 2 || this.moments.get(1) != moments.get(1))) {
            holders.get(1).bindTo(moments.get(1));
            holders.get(1).markAttach();
        }

        if (moments.size() > 2 && (this.moments.size() < 3 || this.moments.get(2) != moments.get(2))) {
            holders.get(2).bindTo(moments.get(2));
            holders.get(2).markAttach();
        }

        if (moments.size() == 1) {
            TransitionManager.beginDelayedTransition(this);
            holders.get(1).itemView.setVisibility(View.INVISIBLE);
            holders.get(2).itemView.setVisibility(View.INVISIBLE);
        } else if (this.moments.size() > 1) {
            TransitionManager.beginDelayedTransition(this);
            holders.get(1).itemView.setVisibility(View.VISIBLE);
            holders.get(2).itemView.setVisibility(View.VISIBLE);
        }

        this.moments.clear();
        this.moments.addAll(moments);
    }

    private void bind(int position) {
        MomentPostViewHolder viewHolder = holders.get(position);
        viewHolder.markDetach();
        viewHolder.bindTo(moments.get(position));
        viewHolder.markAttach();
    }

    private void moveBy(float distance) {
        View top = holders.get(0).itemView;
        View last = holders.get(2).itemView;

        float translation = top.getTranslationX() + distance;
        float progress = Math.abs(translation / top.getWidth());
        float direction = translation > 0 ? 1 : -1;

        top.setTranslationX(translation);
        top.setTranslationY(cardFinalY * progress);
        top.setRotation(last.getRotation() + CARD_FINAL_ANGLE_DEG * progress * direction);
    }

    private void finishMove(float velocity) {
        cardAnimationInProgress = true;

        View top = holders.get(0).itemView;
        float progress = Math.abs(top.getTranslationX() / top.getWidth());

        if (progress > PROGRESS_CANCEL_THRESHOLD || Math.abs(velocity) > velocityCancelThreshold) {
            flingCard();
        } else {
            cancelCard();
        }
    }

    private void flingCard() {
        View top = holders.get(0).itemView;

        float direction = (top.getTranslationX() > 0 ? 1 : -1);

        AnimatorSet set = new AnimatorSet();
        set.play(ObjectAnimator.ofFloat(top, "rotation", top.getRotation(), CARD_FINAL_ANGLE_DEG * direction))
                .with(ObjectAnimator.ofFloat(top, "translationX", top.getTranslationX(), top.getWidth() * direction))
                .with(ObjectAnimator.ofFloat(top, "translationY", top.getTranslationY(), cardFinalY));
        set.setDuration(ANIM_DURATION_MS);

        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                sendCardToBack();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                sendCardToBack();
            }
        });

        set.start();
    }

    private void sendCardToBack() {
        View top = holders.get(0).itemView;
        View next = holders.get(1).itemView;

        removeView(top);
        addView(top, 0);

        AnimatorSet set = new AnimatorSet();
        set.play(ObjectAnimator.ofFloat(top, "rotation", top.getRotation(), next.getRotation()))
                .with(ObjectAnimator.ofFloat(top, "translationX", top.getTranslationX(), 0))
                .with(ObjectAnimator.ofFloat(top, "translationY", top.getTranslationY(), 0));
        set.setDuration(ANIM_DURATION_MS);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                nextCard();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                nextCard();
            }
        });

        set.start();
    }

    private void cancelCard() {
        View top = holders.get(0).itemView;
        View next = holders.get(1).itemView;

        float topAngle = next.getRotation() != CARD_TILT_ANGLE_DEG ? CARD_TILT_ANGLE_DEG : 0;

        AnimatorSet set = new AnimatorSet();
        set.play(ObjectAnimator.ofFloat(top, "rotation", top.getRotation(), topAngle))
                .with(ObjectAnimator.ofFloat(top, "translationX", top.getTranslationX(), 0f))
                .with(ObjectAnimator.ofFloat(top, "translationY", top.getTranslationY(), 0f));
        set.setDuration(ANIM_DURATION_MS);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                cardAnimationInProgress = false;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                cardAnimationInProgress = false;
            }
        });
        set.start();
    }

    private void nextCard() {
        if (moments.size() < 2) {
            return;
        }

        moments.add(moments.remove(0));
        holders.add(holders.remove(0));

        if (moments.size() > 2) {
            bind(2);
        } else {
            bind(1);
        }

        cardAnimationInProgress = false;
    }
}
