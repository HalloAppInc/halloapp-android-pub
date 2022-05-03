package com.halloapp.ui;

import android.graphics.Outline;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.halloapp.AppContext;
import com.halloapp.R;

import eightbitlab.com.blurview.BlurAlgorithm;
import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.BlurViewFacade;
import eightbitlab.com.blurview.RenderEffectBlur;
import eightbitlab.com.blurview.RenderEffectPrecision;
import eightbitlab.com.blurview.RenderScriptBlur;

public class BlurManager {

    private static BlurManager instance;

    public static BlurManager getInstance() {
        if (instance == null) {
            synchronized (BlurManager.class) {
                if (instance == null) {
                    instance = new BlurManager(AppContext.getInstance());
                }
            }
        }
        return instance;
    }

    private final AppContext appContext;

    private RenderScriptBlur renderScriptBlur;

    private BlurManager(@NonNull AppContext appContext) {
        this.appContext = appContext;
    }

    public void init() {
        getRenderScriptBlur();
    }

    public void setupMomentBlur(@NonNull BlurView view, @NonNull ViewGroup content) {
        view.setupWith(content)
                .setBlurAlgorithm(getBlur(view))
                .setBlurRadius(20f)
                .setFrameClearDrawable(new ColorDrawable(ContextCompat.getColor(view.getContext(), R.color.card_background)))
                .setBlurAutoUpdate(true);
        view.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                float radius = view.getResources().getDimension(R.dimen.post_media_radius);
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), radius);
            }
        });
        view.setClipToOutline(true);
    }

    public BlurAlgorithm getBlur(@NonNull BlurView blurView) {
        return getRenderScriptBlur();
    }

    @NonNull
    private RenderScriptBlur getRenderScriptBlur() {
        if (renderScriptBlur == null) {
            renderScriptBlur = new RenderScriptBlur(appContext.get());
        }
        return renderScriptBlur;
    }
}
