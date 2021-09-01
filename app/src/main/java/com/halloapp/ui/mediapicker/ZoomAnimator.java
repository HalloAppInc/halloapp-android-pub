package com.halloapp.ui.mediapicker;

import android.view.ViewPropertyAnimator;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ZoomAnimator extends RecyclerView.ItemAnimator {
    private static final float PROGRESS_STEP = 0.02f;

    private float progress = 0.0f;
    private boolean isManualAnimation = false;
    private final ArrayList<Item> appear = new ArrayList<>();
    private final ArrayList<Item> disappear = new ArrayList<>();
    private final ArrayList<Item> persist = new ArrayList<>();
    private final ArrayList<Item> change = new ArrayList<>();

    private static class Item {
        public RecyclerView.ViewHolder holder, older;
        public final ItemHolderInfo pre, post;
        public float deltaX, deltaY, scaleX, scaleY;
        public boolean canScale = true;

        Item(RecyclerView.ViewHolder holder, ItemHolderInfo pre, ItemHolderInfo post) {
            this.holder = holder;
            this.pre = pre;
            this.post = post;

            if (pre != null && post != null) {
                deltaX = pre.left - post.left;
                deltaY = pre.top - post.top;

                float preWidth = pre.right - pre.left;
                float preHeight = pre.top - pre.bottom;
                float postWidth = post.right - post.left;
                float postHeight = post.top - post.bottom;

                if (postHeight != 0 && postWidth != 0) {
                    scaleX = preWidth / postWidth;
                    scaleY = preHeight / postHeight;
                    canScale = true;
                } else {
                    canScale = false;
                }
            }
        }

        Item(RecyclerView.ViewHolder older, RecyclerView.ViewHolder holder, ItemHolderInfo pre, ItemHolderInfo post) {
            this(holder, pre, post);
            this.older = older;
        }
    }

    public void beginManualAnimations() {
        isManualAnimation = true;
        progress = 0;
    }

    public void endManualAnimations() {
        isManualAnimation = false;
        completeUnfinished();

        appear.clear();
        disappear.clear();
        change.clear();
        persist.clear();

        dispatchAnimationsFinished();
    }

    public boolean isManualAnimationRunning() {
        return isManualAnimation;
    }

    private void completeUnfinished() {
        progress = 1;
        updateAnimated();
    }

    private void updateAnimated() {
        for (Item item : appear) {
            ViewPropertyAnimator animator = item.holder.itemView.animate();
            animator.setDuration(getChangeDuration()).alpha(progress);

            if (item.pre != null) {
                animator.translationX(item.deltaX * (1 - progress)).translationY(item.deltaY * (1 - progress));
            }

            if (item.pre != null && item.canScale) {
                animator.scaleX(item.scaleX + (1 - item.scaleX) * progress).scaleY(item.scaleY + (1 - item.scaleY) * progress);
            }

            animator.start();
        }

        for (Item item : disappear) {
            ViewPropertyAnimator animator = item.holder.itemView.animate();
            animator.setDuration(getChangeDuration()).alpha(1 - progress);

            if (item.pre != null) {
                animator.translationX(item.deltaX * (1 - progress)).translationY(item.deltaY * (1 - progress));
            }

            if (item.pre != null && item.canScale) {
                animator.scaleX(item.scaleX + (1 - item.scaleX) * progress).scaleY(item.scaleY + (1 - item.scaleY) * progress);
            }

            animator.start();
        }

        for (Item item : persist) {
            ViewPropertyAnimator animator = item.holder.itemView.animate().setDuration(getChangeDuration());

            if (item.pre != null) {
                animator.translationX(item.deltaX * (1 - progress)).translationY(item.deltaY * (1 - progress));
            }

            if (item.pre != null && item.canScale) {
                animator.scaleX(item.scaleX + (1 - item.scaleX) * progress).scaleY(item.scaleY + (1 - item.scaleY) * progress);
            }

            animator.start();
        }

        for (Item item : change) {
            ViewPropertyAnimator animator = item.holder.itemView.animate().setDuration(getChangeDuration());
            ViewPropertyAnimator animatorOld = item.holder.itemView.animate().setDuration(getChangeDuration());

            animator.alpha(progress);
            animatorOld.alpha((1 - progress));

            if (item.pre != null) {
                animator.translationX(item.deltaX * (1 - progress)).translationY(item.deltaY * (1 - progress));
                animatorOld.translationX(-item.deltaX * (1 - progress)).translationY(-item.deltaY * (1 - progress));
            }

            if (item.pre != null && item.canScale) {
                animator.scaleX(item.scaleX + (1 - item.scaleX) * progress).scaleY(item.scaleY + (1 - item.scaleY) * progress);
            }
        }
    }

    private void update() {
        for (Item item : appear) {
            item.holder.itemView.setAlpha(progress);

            if (item.pre != null) {
                item.holder.itemView.setTranslationX(item.deltaX * (1 - progress));
                item.holder.itemView.setTranslationY(item.deltaY * (1 - progress));
            }

            if (item.pre != null && item.canScale) {
                item.holder.itemView.setScaleX(item.scaleX + (1 - item.scaleX) * progress);
                item.holder.itemView.setScaleY(item.scaleY + (1 - item.scaleY) * progress);
            }
        }

        for (Item item : disappear) {
            item.holder.itemView.setAlpha(1 - progress);

            if (item.pre != null) {
                item.holder.itemView.setTranslationX(item.deltaX * (1 - progress));
                item.holder.itemView.setTranslationY(item.deltaY * (1 - progress));
            }

            if (item.pre != null && item.canScale) {
                item.holder.itemView.setScaleX(item.scaleX + (1 - item.scaleX) * progress);
                item.holder.itemView.setScaleY(item.scaleY + (1 - item.scaleY) * progress);
            }
        }

        for (Item item : persist) {
            if (item.pre != null) {
                item.holder.itemView.setTranslationX(item.deltaX * (1 - progress));
                item.holder.itemView.setTranslationY(item.deltaY * (1 - progress));
            }

            if (item.pre != null && item.canScale) {
                item.holder.itemView.setScaleX(item.scaleX + (1 - item.scaleX) * progress);
                item.holder.itemView.setScaleY(item.scaleY + (1 - item.scaleY) * progress);
            }
        }

        for (Item item : change) {
            item.holder.itemView.setAlpha(progress);
            item.older.itemView.setAlpha(1 - progress);

            if (item.pre != null) {
                item.holder.itemView.setTranslationX(item.deltaX * (1 - progress));
                item.holder.itemView.setTranslationY(item.deltaY * (1 - progress));
                item.older.itemView.setTranslationX(-item.deltaX * (1 - progress));
                item.older.itemView.setTranslationY(-item.deltaY * (1 - progress));
            }

            if (item.pre != null && item.canScale) {
                item.holder.itemView.setScaleX(item.scaleX + (1 - item.scaleX) * progress);
                item.holder.itemView.setScaleY(item.scaleY + (1 - item.scaleY) * progress);
            }
        }
    }

    public void goForward() {
        progress += PROGRESS_STEP;
        if (progress > 1) {
            progress = 1;
        }

        update();
    }

    public void goBackward() {
        progress -= PROGRESS_STEP;
        if (progress < 0) {
            progress = 0;
        }

        update();
    }

    @Override
    public void runPendingAnimations() {
    }

    @Override
    public void endAnimation(@NonNull RecyclerView.ViewHolder item) {

    }

    @Override
    public void endAnimations() {

    }

    @Override
    public boolean isRunning() {
        return isManualAnimation;
    }

    @Override
    public boolean animateAppearance(@NonNull RecyclerView.ViewHolder viewHolder, @Nullable ItemHolderInfo preLayoutInfo, @NonNull ItemHolderInfo postLayoutInfo) {
        if (isManualAnimation) {
            Item item = new Item(viewHolder, preLayoutInfo, postLayoutInfo);

            viewHolder.itemView.setAlpha(0);

            if (item.pre != null) {
                // Restore original position
                viewHolder.itemView.setTranslationX(item.deltaX);
                viewHolder.itemView.setTranslationY(item.deltaY);

                // Restore original size
                if (item.canScale) {
                    viewHolder.itemView.setScaleX(item.scaleX);
                    viewHolder.itemView.setScaleY(item.scaleY);
                }
            }

            appear.add(item);
        }

        return false;
    }

    @Override
    public boolean animateDisappearance(@NonNull RecyclerView.ViewHolder viewHolder, @NonNull ItemHolderInfo preLayoutInfo, @Nullable ItemHolderInfo postLayoutInfo) {
        if (isManualAnimation) {
            Item item = new Item(viewHolder, preLayoutInfo, postLayoutInfo);

            viewHolder.itemView.setAlpha(1);

            if (item.pre != null) {
                // Restore original position
                viewHolder.itemView.setTranslationX(item.deltaX);
                viewHolder.itemView.setTranslationY(item.deltaY);

                // Restore original size
                if (item.canScale) {
                    viewHolder.itemView.setScaleX(item.scaleX);
                    viewHolder.itemView.setScaleY(item.scaleY);
                }
            }

            disappear.add(item);
        }

        return false;
    }

    @Override
    public boolean animatePersistence(@NonNull RecyclerView.ViewHolder viewHolder, @NonNull ItemHolderInfo preInfo, @NonNull ItemHolderInfo postInfo) {
        if (isManualAnimation) {
            Item item = new Item(viewHolder, preInfo, postInfo);

            if (item.pre != null) {
                // Restore original position
                viewHolder.itemView.setTranslationX(item.deltaX);
                viewHolder.itemView.setTranslationY(item.deltaY);

                // Restore original size
                if (item.canScale) {
                    viewHolder.itemView.setScaleX(item.scaleX);
                    viewHolder.itemView.setScaleY(item.scaleY);
                }
            }

            persist.add(item);
        }

        return false;
    }

    @Override
    public boolean animateChange(@NonNull RecyclerView.ViewHolder oldHolder, @NonNull RecyclerView.ViewHolder newHolder, @NonNull ItemHolderInfo preInfo, @NonNull ItemHolderInfo postInfo) {
        if (isManualAnimation && oldHolder == newHolder) {
            return animatePersistence(newHolder, preInfo, postInfo);
        } else if (isManualAnimation) {
            Item item = new Item(oldHolder, newHolder, preInfo, postInfo);

            newHolder.itemView.setAlpha(1);

            if (item.pre != null) {
                // Restore original position
                newHolder.itemView.setTranslationX(item.deltaX);
                newHolder.itemView.setTranslationY(item.deltaY);

                // Restore original size
                if (item.canScale) {
                    newHolder.itemView.setScaleX(item.scaleX);
                    newHolder.itemView.setScaleY(item.scaleY);
                }
            }

            change.add(item);

            return false;
        }

        return animateSelection((MediaPickerActivity.MediaItemViewHolder)oldHolder, (MediaPickerActivity.MediaItemViewHolder)newHolder, preInfo, postInfo);
    }

    public boolean animateSelection(@NonNull MediaPickerActivity.MediaItemViewHolder oldHolder, @NonNull MediaPickerActivity.MediaItemViewHolder newHolder, @NonNull ItemHolderInfo preInfo, @NonNull ItemHolderInfo postInfo) {
        if (oldHolder.animateSelection || oldHolder.animateDeselection) {
            if (oldHolder != newHolder) {
                dispatchAnimationStarted(oldHolder);
            }
            dispatchAnimationStarted(newHolder);

            final float animateScale = oldHolder.animateDeselection ? 1.1f : .9f;

            ScaleAnimation animation = new ScaleAnimation(
                    1f,
                    animateScale,
                    1f,
                    animateScale,
                    Animation.RELATIVE_TO_SELF,
                    .5f,
                    Animation.RELATIVE_TO_SELF,
                    .5f);
            animation.setDuration(70);
            animation.setRepeatCount(1);
            animation.setRepeatMode(Animation.REVERSE);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (oldHolder != newHolder) {
                        dispatchAnimationFinished(oldHolder);
                    }
                    dispatchAnimationFinished(newHolder);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });

            if (oldHolder != newHolder) {
                oldHolder.itemView.setAlpha(0);
            }
            newHolder.itemView.setAlpha(1);
            newHolder.thumbnailFrame.startAnimation(animation);

            oldHolder.animateSelection = false;
            oldHolder.animateDeselection = false;
            newHolder.animateSelection = false;
            newHolder.animateDeselection = false;
        } else {
            if (oldHolder != newHolder) {
                dispatchAnimationFinished(oldHolder);
            }
            dispatchAnimationFinished(newHolder);
        }

        return false;
    }
}
