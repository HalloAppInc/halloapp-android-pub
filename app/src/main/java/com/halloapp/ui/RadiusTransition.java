package com.halloapp.ui;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.transition.Transition;
import android.transition.TransitionValues;
import android.util.FloatProperty;
import android.util.Property;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

public class RadiusTransition extends Transition {

    private static final String PROP_RADIUS = "radius";

    final boolean toCircle;

    private RadiusTransition(boolean fromCircle) {
        this.toCircle = fromCircle;
    }

    public static RadiusTransition toCircle() {
        return new RadiusTransition(false);
    }

    public static RadiusTransition toSquare() {
        return new RadiusTransition(true);
    }

    @Override
    public Animator createAnimator(ViewGroup sceneRoot, TransitionValues startValues, TransitionValues endValues) {
        if (startValues == null || endValues == null) {
            return null;
        }

        ImageView endImageView = (ImageView) endValues.view;
        Float start = (Float) startValues.values.get(PROP_RADIUS);
        Float end = (Float) endValues.values.get(PROP_RADIUS);

        ObjectAnimator objectAnimator = ObjectAnimator
                .ofFloat(endImageView, createRadiusProperty(), start, end);
        objectAnimator.setInterpolator(super.getInterpolator());
        return objectAnimator;
    }

    private static Property<ImageView, Float> createRadiusProperty() {
        if (Build.VERSION.SDK_INT >= 24) {
            return new FloatProperty<ImageView>(PROP_RADIUS) {
                @Override
                public void setValue(ImageView object, float value) {
                    convertDrawableIfNeeded(object);
                    Drawable drawable = object.getDrawable();
                    if (drawable instanceof RoundedBitmapDrawable) {
                        ((RoundedBitmapDrawable) drawable).setCornerRadius(value);
                    }
                }

                @Override
                public Float get(ImageView object) {
                    convertDrawableIfNeeded(object);
                    Drawable drawable = object.getDrawable();
                    if (drawable instanceof RoundedBitmapDrawable) {
                        return ((RoundedBitmapDrawable) drawable).getCornerRadius();
                    }
                    return 0.0f;
                }
            };
        } else {
            return new Property<ImageView, Float>(Float.class, PROP_RADIUS) {
                @Override
                public void set(ImageView object, Float value) {
                    convertDrawableIfNeeded(object);
                    Drawable drawable = object.getDrawable();
                    if (drawable instanceof RoundedBitmapDrawable) {
                        ((RoundedBitmapDrawable) drawable).setCornerRadius(value);
                    }
                }

                @Override
                public Float get(ImageView object) {
                    convertDrawableIfNeeded(object);
                    Drawable drawable = object.getDrawable();
                    if (drawable instanceof RoundedBitmapDrawable) {
                        return ((RoundedBitmapDrawable) drawable).getCornerRadius();
                    }
                    return 0.0f;
                }
            };
        }
    }

    private static void convertDrawableIfNeeded(ImageView view) {
        Drawable drawable = view.getDrawable();
        if (drawable instanceof RoundedBitmapDrawable) {
            return;
        }
        if (drawable instanceof BitmapDrawable) {
            view.setImageDrawable(RoundedBitmapDrawableFactory.create(view.getResources(), ((BitmapDrawable) drawable).getBitmap()));
        }
    }

    @Override
    public void captureStartValues(TransitionValues transitionValues) {
        if (transitionValues.view instanceof ImageView) {
            float startRadius;
            if (toCircle) {
                startRadius = Math.min(transitionValues.view.getWidth(), transitionValues.view.getHeight()) / 2.0f;
            } else {
                startRadius = 0;
            }
            transitionValues.values.put(PROP_RADIUS, startRadius);
        }
    }

    @Override
    public void captureEndValues(TransitionValues transitionValues) {
        if (transitionValues.view instanceof ImageView) {
            float endRadius;
            if (!toCircle) {
                endRadius = Math.min(transitionValues.view.getWidth(), transitionValues.view.getHeight()) / 2.0f;
            } else {
                endRadius = 0;
            }
            transitionValues.values.put(PROP_RADIUS, endRadius);
        }
    }
}
