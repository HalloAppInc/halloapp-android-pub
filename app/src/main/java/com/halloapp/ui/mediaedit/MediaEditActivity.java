package com.halloapp.ui.mediaedit;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.transition.Transition;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.halloapp.R;
import com.halloapp.content.Media;
import com.halloapp.media.MediaUtils;
import com.halloapp.ui.HalloActivity;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.Preconditions;
import com.halloapp.util.logs.Log;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;

public class MediaEditActivity extends HalloActivity {
    public static final String EXTRA_MEDIA = "media";
    public static final String EXTRA_SELECTED = "selected";
    public static final String EXTRA_STATE = "state";
    public static final String EXTRA_PURPOSE = "purpose";

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({EDIT_PURPOSE_CROP, EDIT_PURPOSE_ANNOTATE, EDIT_PURPOSE_DRAW})
    public @interface EditPurpose {}
    public static final int EDIT_PURPOSE_CROP = 1;
    public static final int EDIT_PURPOSE_ANNOTATE = 2;
    public static final int EDIT_PURPOSE_DRAW = 3;

    public static final String TRANSITION_VIEW_NAME = "crop_image";

    private final BgWorkers bgWorkers = BgWorkers.getInstance();
    private MediaEditViewModel viewModel;
    private boolean isTransitionInProgress = true;
    private boolean isExitInProgress = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().getIntExtra(EXTRA_PURPOSE, EDIT_PURPOSE_CROP) == EDIT_PURPOSE_ANNOTATE) {
            // If this flag is not set, the soft keyboard does not appear initially in annotation mode.
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }

        supportRequestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        postponeEnterTransition();

        setContentView(R.layout.activity_media_edit);
        Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        viewModel = new ViewModelProvider(this).get(MediaEditViewModel.class);

        setupMediaEdit();
        setupButtons();

        ArrayList<Uri> uris = getIntent().getParcelableArrayListExtra(EXTRA_MEDIA);
        Bundle state = getIntent().getBundleExtra(EXTRA_STATE);
        viewModel.setMedia(uris, state, getIntent().getIntExtra(EXTRA_SELECTED, 0));
    }

    private void potentiallyFinishAnnotating() {
        final ImageEditFragment imageFragment = (ImageEditFragment) getSupportFragmentManager().findFragmentByTag(ImageEditFragment.class.getName());
        if (imageFragment != null) {
            imageFragment.stopAnnotating();
        }
    }

    private void triggerFinish(boolean saveChanges) {
        if (isExitInProgress) {
            return;
        }
        isExitInProgress = true;

        if (saveChanges) {
            findViewById(R.id.processing).setVisibility(View.VISIBLE);
            bgWorkers.execute(() -> {
                viewModel.save(this);
                runOnUiThread(() -> {
                    findViewById(R.id.processing).setVisibility(View.GONE);
                    setResult(RESULT_OK, getIntentWithResults());
                    prepareTransitionView(false, this::finishAfterTransition);
                });
            });
        } else {
            setResult(RESULT_OK, getIntentWithResults());
            prepareTransitionView(false, this::finishAfterTransition);
        }
    }

    @Override
    public void onBackPressed() {
        potentiallyFinishAnnotating();
        if (!viewModel.hasChanges()) {
            triggerFinish(false);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.alert_discard_media_edit_message);
            builder.setPositiveButton(R.string.action_discard, (dialog, which) -> triggerFinish(false));
            builder.setNegativeButton(R.string.cancel, null);
            builder.create().show();
        }
    }

    private void setupButtons() {
        findViewById(R.id.done).setOnClickListener(v -> {
            potentiallyFinishAnnotating();
            triggerFinish(true);
        });
    }

    private void setupMediaEdit() {
        viewModel.getSelected().observe(this, model -> {
            Class<? extends Fragment> klass = model.getType() == Media.MEDIA_TYPE_IMAGE ? ImageEditFragment.class : VideoEditFragment.class;

            getSupportFragmentManager()
                    .beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(R.id.fragment_container, klass, null, klass.getName())
                    .commit();

            setupButtons();

            if (isTransitionInProgress) {
                isTransitionInProgress = false;
                prepareTransitionView(true, this::startPostponedEnterTransition);
            }
        });

        viewModel.getMedia().observe(this, models -> {
            // on button state change, update buttons
            setupButtons();
        });
    }

    private Intent getIntentWithResults() {
        Intent intent = new Intent();

        ArrayList<Uri> uris = viewModel.getUris();
        if (uris == null) {
            uris = getIntent().getParcelableArrayListExtra(EXTRA_MEDIA);
        }

        Bundle state = viewModel.getState();
        if (state == null) {
            state = getIntent().getBundleExtra(EXTRA_STATE);
        }

        int position = viewModel.getSelectedPosition();
        if (position < 0) {
            position = getIntent().getIntExtra(EXTRA_SELECTED, 0);
        }

        intent.putParcelableArrayListExtra(EXTRA_MEDIA, uris);
        intent.putExtra(EXTRA_STATE, state);
        intent.putExtra(EXTRA_SELECTED, position);

        return intent;
    }

    private void prepareTransitionView(boolean enter, @NonNull Runnable action) {
        MediaEditViewModel.Model model = viewModel.getSelected().getValue();
        if (model == null) {
            return;
        }

        bgWorkers.execute(() -> {
            final Bitmap snapshot;
            final Bitmap original;
            try {
                snapshot = MediaUtils.decode(this, model.getMedia().file, model.getType());
                original = MediaUtils.decode(this, model.original.file, model.getType());
            } catch (IOException e) {
                Log.e("MediaEditActivity: unable to get snapshot", e);
                runOnUiThread(action);
                return;
            }

            if (snapshot == null) {
                runOnUiThread(action);
                return;
            }

            runOnUiThread(() -> {
                ImageView transitionView = findViewById(R.id.transition);
                transitionView.setVisibility(View.VISIBLE);
                transitionView.setTransitionName(TRANSITION_VIEW_NAME);

                if (enter) {
                    getWindow().getSharedElementEnterTransition().addListener(new Transition.TransitionListener() {
                        @Override
                        public void onTransitionStart(Transition transition) {}

                        @Override
                        public void onTransitionEnd(Transition transition) {
                            transitionView.post(() -> transitionView.setVisibility(View.GONE));
                        }

                        @Override
                        public void onTransitionCancel(Transition transition) {}

                        @Override
                        public void onTransitionPause(Transition transition) {}

                        @Override
                        public void onTransitionResume(Transition transition) {}
                    });
                }

                ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) transitionView.getLayoutParams();

                Parcelable state = model.getState();
                if (original != null && model.getType() == Media.MEDIA_TYPE_IMAGE && (state instanceof EditImageView.State)) {
                    View mediaView = findViewById(R.id.image);
                    EditImageView.State imageState = (EditImageView.State) state;
                    RectF cropRect = cropRectInView(imageState, original.getWidth(), original.getHeight(), mediaView.getWidth(), mediaView.getHeight());

                    params.leftMargin = (int) cropRect.left;
                    params.topMargin = (int) cropRect.top;
                    params.rightMargin = mediaView.getWidth() - (int) cropRect.right;
                    params.bottomMargin = mediaView.getHeight() - (int) cropRect.bottom;
                }

                transitionView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        transitionView.getViewTreeObserver().removeOnPreDrawListener(this);
                        action.run();
                        return true;
                    }
                });

                transitionView.setImageBitmap(snapshot);
                transitionView.setLayoutParams(params);
            });
        });
    }

    private RectF cropRectInView(EditImageView.State state, float imageWidth, float imageHeight, float viewWidth, float viewHeight) {
        float baseScale = Math.min(viewWidth / imageWidth,  viewHeight / imageHeight);
        float viewCenterX = viewWidth / 2;
        float viewCenterY = viewHeight / 2;

        float offsetX = state.offsetX * baseScale * state.scale;
        float offsetY = state.offsetY * baseScale * state.scale;

        float cropWidth = state.cropWidth * baseScale;
        float cropHeight = state.cropHeight * baseScale;
        float cropX = state.cropOffsetX * baseScale + viewCenterX + offsetX - cropWidth / 2;
        float cropY = state.cropOffsetY * baseScale + viewCenterY + offsetY - cropHeight / 2;

        return new RectF(cropX, cropY, cropX + cropWidth, cropY + cropHeight);
    }
}
