package com.halloapp.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

import com.halloapp.R;
import com.halloapp.util.ContextUtils;

import java.util.ArrayList;
import java.util.Iterator;

public class HACustomFab extends LinearLayout {

    private static final int SUB_ANIMATION_DELAY = 25;
    private static final int ANIMATION_DURATION = 200;
    private static final int EXTEND_ANIMATION_DURATION = 150;

    public interface OnActionSelectedListener {
        void onActionSelected(@IdRes int actionId);
        void onOverlay(boolean visible);
    }

    private ImageView logoImageView;
    private View logoContainerView;
    private View fadeGradient;

    private LinearLayout primaryFab;

    private ImageView iconView;

    private View fabOverlay;

    private TextView subMenuTitleView;
    private final ArrayList<FabWithLabelView> subFabViews = new ArrayList<>();

    private @Nullable OnActionSelectedListener onActionSelectedListener;
    private @Nullable View.OnClickListener onClickListener;

    private int fabPadding;
    private int fabSize;

    private boolean expanded = true;
    private boolean subMenuOpen = false;

    private @ColorInt int defaultSystemUiColor;

    private ValueAnimator animator;
    private ValueAnimator systemUiAnimator;

    public HACustomFab(Context context) {
        super(context);
        init();
    }

    public HACustomFab(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HACustomFab(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        final Context context = getContext();

        final TypedValue typedValue = new TypedValue();
        final TypedArray a = context.obtainStyledAttributes(typedValue.data, new int[] { R.attr.colorPrimaryDark });
        defaultSystemUiColor = a.getColor(0, 0);
        a.recycle();

        setOrientation(VERTICAL);
        setGravity(Gravity.END);
        setClipToPadding(false);
        setClipChildren(false);

        inflate(context, R.layout.view_halloapp_fab, this);

        fabPadding = context.getResources().getDimensionPixelSize(R.dimen.fab_padding);
        fabSize = context.getResources().getDimensionPixelSize(R.dimen.fab_size);

        logoImageView = findViewById(R.id.hallo);
        logoContainerView = findViewById(R.id.hallo_container);
        iconView = findViewById(R.id.icon);
        primaryFab = findViewById(R.id.fab);
        fadeGradient = findViewById(R.id.fade_gradient);

        setPadding(fabPadding, fabPadding, fabPadding, fabPadding);

        primaryFab.setOnClickListener(v -> {
            if (!subFabViews.isEmpty()) {
                transitionToOpen(!subMenuOpen);
            } else if (onClickListener != null) {
                onClickListener.onClick(v);
            }
        });
    }

    public void setOnActionSelectedListener(@Nullable OnActionSelectedListener onActionSelectedListener) {
        this.onActionSelectedListener = onActionSelectedListener;
    }

    public void setOnFabClickListener(@Nullable OnClickListener onFabClickListener) {
        this.onClickListener = onFabClickListener;
    }

    public void addSubFab(@IdRes int id, @DrawableRes int iconRes, @StringRes int stringRes) {
        FabWithLabelView fab = new FabWithLabelView(getContext());
        fab.setId(id);
        fab.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        fab.setDrawable(iconRes);
        fab.setLabel(getResources().getString(stringRes));
        fab.setVisibility(View.GONE);
        fab.getFab().setOnClickListener(v -> {
            if (onActionSelectedListener != null) {
                onActionSelectedListener.onActionSelected(id);
            }
        });
        subFabViews.add(fab);
        addView(fab, 0);
    }

    public void addTitle(@StringRes int titleRes) {
        TextView titleView = new TextView(getContext());
        titleView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        titleView.setText(titleRes);
        titleView.setTextSize(17);
        titleView.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        titleView.setTextColor(ContextCompat.getColor(getContext(), R.color.fab_label_text));
        titleView.setPadding(0, 0, 0, getResources().getDimensionPixelSize(R.dimen.sub_fab_title_bottom_padding));
        titleView.setVisibility(View.GONE);
        subMenuTitleView = titleView;
        addView(titleView, 0);
    }

    public void clearActionItems() {
        if (subMenuTitleView != null) {
            removeView(subMenuTitleView);
            subMenuTitleView = null;
        }
        Iterator<FabWithLabelView> subFabIterator = subFabViews.iterator();
        while (subFabIterator.hasNext()) {
            View subFab = subFabIterator.next();
            removeView(subFab);
            subFabIterator.remove();
        }
    }

    public boolean isOpen() {
        return subMenuOpen;
    }

    public void show() {
        setVisibility(View.VISIBLE);
    }

    public void hide() {
        setVisibility(View.GONE);
    }

    public void setMainFabIcon(@DrawableRes int iconRes, @StringRes int contentDescRes) {
        setMainFabIcon(iconRes, contentDescRes, false);
    }

    public void setMainFabIcon(@DrawableRes int iconRes, @StringRes int contentDescRes, boolean showLogo) {
        iconView.setImageResource(iconRes);
        iconView.setContentDescription(getResources().getString(contentDescRes));
        logoContainerView.setVisibility(showLogo ? View.VISIBLE : View.GONE);
        if (showLogo) {
            if (expanded) {
                ViewGroup.LayoutParams layoutParams = primaryFab.getLayoutParams();
                layoutParams.width = LayoutParams.WRAP_CONTENT;
                primaryFab.setLayoutParams(layoutParams);
            }
        } else {
            ViewGroup.LayoutParams layoutParams = primaryFab.getLayoutParams();
            layoutParams.width = fabSize;
            primaryFab.setLayoutParams(layoutParams);
            primaryFab.setPadding(fabPadding, 0, fabPadding, 0);
        }
    }

    private void transitionToOpen(boolean isOpen) {
        transitionToOpen(isOpen, true);
    }

    private void transitionToOpen(boolean isOpen, boolean animate) {
        boolean openStateChanged = subMenuOpen != isOpen;
        if (isOpen) {
            if (openStateChanged) {
                transitionToOpen();
                updateSubMenuVisibilities(true, animate);
            }
        } else {
            if (openStateChanged) {
                updateSubMenuVisibilities(false, animate);
                transitionToClosed();
            }
        }
        subMenuOpen = isOpen;
    }

    private float getCollapsePercentage() {
        final int start = primaryFab.getWidth();
        final int logoViewWidth = logoImageView.getDrawable().getIntrinsicWidth() + logoImageView.getPaddingEnd() + logoImageView.getPaddingStart();
        return (float) Math.max(0, (start - fabSize) / ((float)logoViewWidth));
    }

    public void collapse() {
        if (!expanded) {
            return;
        }
        createCollapseAnimator();
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        fadeGradient.setVisibility(View.VISIBLE);
        animator.start();
        expanded = false;
    }

    private void createCollapseAnimator() {
        float initialPercentage = getCollapsePercentage();
        setAnimator(ValueAnimator.ofFloat(0, 1));
        animator.setDuration((int)(ANIMATION_DURATION * initialPercentage));

        CollapseAnimator collapseAnimator = new CollapseAnimator();
        animator.addUpdateListener(collapseAnimator);
        animator.addListener(collapseAnimator);
    }

    private void createExtendAnimator() {
        final int logoContainerWidth = logoImageView.getDrawable().getIntrinsicWidth() + logoImageView.getPaddingEnd() + logoImageView.getPaddingStart();
        float initialPercentage = (float)((float)(primaryFab.getWidth() - fabSize) / (float)logoContainerWidth);
        setAnimator(ValueAnimator.ofFloat(initialPercentage, 1));
        animator.setDuration((int)(EXTEND_ANIMATION_DURATION * (1f - initialPercentage)));

        ExtendAnimator extendAnimator = new ExtendAnimator();
        animator.addUpdateListener(extendAnimator);
        animator.addListener(extendAnimator);
    }

    private void setAnimator(ValueAnimator animator) {
        if (this.animator != null) {
            this.animator.cancel();
        }
        this.animator = animator;
    }

    private void onShowOverlay() {
        if (onActionSelectedListener != null) {
            onActionSelectedListener.onOverlay(true);
        }
        if (systemUiAnimator != null) {
            systemUiAnimator.cancel();
        }
        Activity activity = ContextUtils.getActivity(getContext());
        if (activity != null) {
            Window window = activity.getWindow();
            @ColorInt int startColor = ContextCompat.getColor(getContext(), R.color.window_background);
            @ColorInt int color = ContextCompat.getColor(getContext(), R.color.fab_overlay_gradient_top);
            systemUiAnimator = ValueAnimator.ofArgb(startColor, color);
            systemUiAnimator.addUpdateListener(animation -> {
                int val = (Integer) animation.getAnimatedValue();
                window.setStatusBarColor(val);
                window.setNavigationBarColor(val);
            });
            systemUiAnimator.setDuration(ANIMATION_DURATION);
            systemUiAnimator.start();
        }
    }

    private void onStartHideOverlay() {
        if (systemUiAnimator != null) {
            systemUiAnimator.cancel();
        }
        Activity activity = ContextUtils.getActivity(getContext());
        if (activity != null) {
            Window window = activity.getWindow();
            @ColorInt int color = ContextCompat.getColor(getContext(), R.color.fab_overlay_gradient_top);
            systemUiAnimator = ValueAnimator.ofArgb(color, defaultSystemUiColor);
            systemUiAnimator.addUpdateListener(animation -> {
                int val = (Integer) animation.getAnimatedValue();
                window.setStatusBarColor(val);
                window.setNavigationBarColor(val);
            });
            systemUiAnimator.setDuration(ANIMATION_DURATION);
            systemUiAnimator.start();
        }
    }

    private void onHideOverlay() {
        if (onActionSelectedListener != null) {
            onActionSelectedListener.onOverlay(false);
        }
    }

    private void transitionToClosed() {
        if (expanded) {
            createExtendAnimator();
            fadeGradient.setVisibility(View.GONE);
        } else {
            createCollapseAnimator();
            animator.setDuration(ANIMATION_DURATION);
        }
        animator.addUpdateListener(animation -> {
            float val = (Float) animator.getAnimatedValue();
            iconView.setRotation((1 - val) * 45f);
            if (fabOverlay != null) {
                fabOverlay.setAlpha(1f - val);
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (fabOverlay != null) {
                    fabOverlay.setVisibility(View.GONE);
                    onHideOverlay();
                }
                iconView.setRotation(0);
            }
        });
        iconView.setColorFilter(0xFFFFFFFF);
        primaryFab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.color_primary)));
        animator.start();
        onStartHideOverlay();
    }

    private class CollapseAnimator extends AnimatorListenerAdapter implements ValueAnimator.AnimatorUpdateListener {

        private final int logoViewWidth;
        private float basePercentage;

        public CollapseAnimator() {
            logoViewWidth = logoImageView.getDrawable().getIntrinsicWidth() + logoImageView.getPaddingEnd() + logoImageView.getPaddingStart();
            this.basePercentage = 1f - getCollapsePercentage();
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            float percent = basePercentage + (1f - basePercentage) * ((Float) animator.getAnimatedValue());
            int curLogoWidth = (int)((1f - percent) * logoViewWidth);
            ViewGroup.LayoutParams layoutParams = primaryFab.getLayoutParams();
            layoutParams.width = fabSize + curLogoWidth;
            primaryFab.setLayoutParams(layoutParams);

            layoutParams = logoContainerView.getLayoutParams();
            layoutParams.width = curLogoWidth;
            logoContainerView.setLayoutParams(layoutParams);
            float alpha = 1f - percent;
            logoContainerView.setAlpha(alpha);
        }

        @Override
        public void onAnimationEnd(Animator animation, boolean isReverse) {
            logoContainerView.setVisibility(View.GONE);
            fadeGradient.setVisibility(View.GONE);
        }
    }

    private class ExtendAnimator extends AnimatorListenerAdapter implements ValueAnimator.AnimatorUpdateListener {

        private final int logoContainerWidth;

        public ExtendAnimator() {
            logoContainerWidth = logoImageView.getDrawable().getIntrinsicWidth() + logoImageView.getPaddingEnd() + logoImageView.getPaddingStart();
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            ViewGroup.LayoutParams layoutParams = primaryFab.getLayoutParams();
            layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            primaryFab.setLayoutParams(layoutParams);
            layoutParams = logoContainerView.getLayoutParams();
            layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            logoContainerView.setLayoutParams(layoutParams);
            logoContainerView.setVisibility(View.VISIBLE);
            fadeGradient.setVisibility(View.GONE);
        }

        @Override
        public void onAnimationStart(Animator animation) {
            logoContainerView.setVisibility(View.VISIBLE);
            logoContainerView.setAlpha(0f);
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            float val = (Float) animator.getAnimatedValue();
            ViewGroup.LayoutParams layoutParams = primaryFab.getLayoutParams();

            final int width = (int)  (logoContainerWidth * val);

            layoutParams.width = fabSize + width;
            primaryFab.setLayoutParams(layoutParams);

            layoutParams = logoContainerView.getLayoutParams();
            layoutParams.width = width;
            logoContainerView.setLayoutParams(layoutParams);
            logoContainerView.setAlpha(val);
        }
    }

    private void transitionToOpen() {
        final int start = primaryFab.getWidth();
        final int logoStart = logoContainerView.getWidth();
        final int end = getResources().getDimensionPixelSize(R.dimen.fab_size);
        setAnimator(ValueAnimator.ofFloat(0, 1));
        animator.setDuration(ANIMATION_DURATION);
        final boolean isExpanded = true;
        if (fabOverlay == null) {
            fabOverlay = ((View)getParent()).findViewById(R.id.fab_overlay);
            if (fabOverlay != null) {
                fabOverlay.setOnTouchListener((v, e) -> {
                    transitionToOpen(false);
                    return true;
                });
            }
        }
        if (fabOverlay != null) {
            fabOverlay.setVisibility(View.VISIBLE);
            fabOverlay.setAlpha(0f);
            onShowOverlay();
        }
        animator.addUpdateListener(animation -> {
            float val = (Float) animator.getAnimatedValue();
            if (isExpanded) {
                ViewGroup.LayoutParams layoutParams = primaryFab.getLayoutParams();
                int newWidth = (int) (val * (end - start) + start);
                layoutParams.width = newWidth;
                primaryFab.setLayoutParams(layoutParams);

                layoutParams = logoContainerView.getLayoutParams();
                layoutParams.width = (int)((1.0f - val) * logoStart);
                logoContainerView.setLayoutParams(layoutParams);
                float alpha = (float)((float)(val - end) / (float)(start - end));
                logoContainerView.setAlpha(alpha);
            }
            iconView.setRotation(val * 45f);
            if (fabOverlay != null) {
                fabOverlay.setAlpha(val);
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                logoContainerView.setVisibility(View.GONE);
            }
        });
        iconView.setColorFilter(ContextCompat.getColor(getContext(), R.color.color_primary));
        primaryFab.setBackgroundTintList(ColorStateList.valueOf(0xFFFFFFFF));

        animator.start();
    }

    public void expand() {
        if (expanded) {
            return;
        }
        createExtendAnimator();
        logoContainerView.setVisibility(View.VISIBLE);
        logoContainerView.setAlpha(0f);
        fadeGradient.setVisibility(View.VISIBLE);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        ExtendAnimator extendAnimator = new ExtendAnimator();
        animator.addUpdateListener(extendAnimator);
        animator.addListener(extendAnimator);
        animator.start();
        expanded = true;
    }

    private void updateSubMenuVisibilities(boolean visible, boolean animate) {
        int animDelay = subFabViews.size() * SUB_ANIMATION_DELAY;
        for (FabWithLabelView subFab : subFabViews) {
            if (animate) {
                if (visible) {
                    enlargeAnim(subFab.getFab(), animDelay);
                    labelEnterAnim(subFab.getLabel(), animDelay + 50);
                } else {
                    shrinkAnim(subFab.getFab(), animDelay);
                    labelExitAnim(subFab.getLabel(), animDelay);
                }
                animDelay -= SUB_ANIMATION_DELAY;
            } else {
                ViewCompat.animate(subFab.getFab()).cancel();
                ViewCompat.animate(subFab.getLabel()).cancel();
                subFab.getFab().setVisibility(visible ? View.VISIBLE : View.GONE);
                subFab.getLabel().setVisibility(visible ? View.VISIBLE : View.GONE);
            }
            subFab.setVisibility(View.VISIBLE);
            subFab.setAlpha(1);
        }
        if (subMenuTitleView != null) {
            if (animate) {
                if (visible) {
                    enlargeAnim(subMenuTitleView, animDelay + SUB_ANIMATION_DELAY);
                } else {
                    shrinkAnim(subMenuTitleView, 0);
                }
            } else {
                subMenuTitleView.setVisibility(visible ? View.VISIBLE : View.GONE);
            }
        }
    }

    public void open() {
        transitionToOpen(true);
    }

    public void close() {
        transitionToOpen(false);
    }

    public void close(boolean animate) {
        transitionToOpen(false, animate);
    }

    private static void enlargeAnim(View view, long startOffset) {
        ViewCompat.animate(view).cancel();
        view.setVisibility(View.VISIBLE);
        Animation anim = AnimationUtils.loadAnimation(view.getContext(), R.anim.sub_fab_scale_fade_translate_in);
        anim.setStartOffset(startOffset);
        view.startAnimation(anim);
    }

    public static void shrinkAnim(final View view, long startOffset) {
        ViewCompat.animate(view).cancel();
        view.setVisibility(View.VISIBLE);
        Animation anim = AnimationUtils.loadAnimation(view.getContext(), R.anim.sub_fab_scale_fade_translate_out);
        anim.setStartOffset(startOffset);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        view.startAnimation(anim);
    }

    private static void labelEnterAnim(View view, long startOffset) {
        ViewCompat.animate(view).cancel();
        view.setVisibility(View.VISIBLE);
        Animation anim = AnimationUtils.loadAnimation(view.getContext(), R.anim.sub_fab_fade_translate_x_in);
        anim.setStartOffset(startOffset);
        view.startAnimation(anim);
    }

    private static void labelExitAnim(View view, long startOffset) {
        ViewCompat.animate(view).cancel();
        view.setVisibility(View.VISIBLE);
        Animation anim = AnimationUtils.loadAnimation(view.getContext(), R.anim.sub_fab_fade_translate_x_out);
        anim.setStartOffset(startOffset);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        view.startAnimation(anim);
    }
}
