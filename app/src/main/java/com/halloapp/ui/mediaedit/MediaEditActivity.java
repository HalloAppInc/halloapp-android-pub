package com.halloapp.ui.mediaedit;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Outline;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.transition.Transition;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.BuildConfig;
import com.halloapp.R;
import com.halloapp.content.Media;
import com.halloapp.media.MediaThumbnailLoader;
import com.halloapp.media.MediaUtils;
import com.halloapp.props.ServerProps;
import com.halloapp.ui.HalloActivity;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.Preconditions;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.LinearSpacingItemDecoration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MediaEditActivity extends HalloActivity {
    public static final String EXTRA_MEDIA = "media";
    public static final String EXTRA_SELECTED = "selected";
    public static final String EXTRA_STATE = "state";

    public static final String TRANSITION_VIEW_NAME = "crop_image";

    private final BgWorkers bgWorkers = BgWorkers.getInstance();
    private MediaEditViewModel viewModel;
    private MediaPreviewAdapter previewAdapter;
    private MediaThumbnailLoader thumbnailLoader;
    private boolean isTransitionInProgress = true;
    private boolean isExitInProgress = false;

    private static final float PREVIEW_SELECTED_ALPHA = 1.0f;
    private static final float PREVIEW_DEFAULT_ALPHA = .6f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        supportRequestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        postponeEnterTransition();

        setContentView(R.layout.activity_media_edit);
        Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        viewModel = new ViewModelProvider(this).get(MediaEditViewModel.class);
        thumbnailLoader = new MediaThumbnailLoader(this, 2 * getResources().getDimensionPixelSize(R.dimen.details_media_list_height));

        setupMediaEdit();
        setupPreviewListView();
        setupButtons();

        ArrayList<Uri> uris = getIntent().getParcelableArrayListExtra(EXTRA_MEDIA);
        Bundle state = getIntent().getBundleExtra(EXTRA_STATE);
        viewModel.setMedia(uris, state, getIntent().getIntExtra(EXTRA_SELECTED, 0));
    }

    @Override
    public void onBackPressed() {
        if (isExitInProgress) {
            return;
        }
        isExitInProgress = true;

        int position = viewModel.getSelectedPosition();
        if (position < 0) {
            position = getIntent().getIntExtra(EXTRA_SELECTED, 0);
        }

        Intent intent = new Intent();
        intent.putExtra(EXTRA_SELECTED, position);
        setResult(RESULT_OK, getIntentWithResults());

        prepareTransitionView(false, this::finishAfterTransition);
    }

    private boolean canUndo() {
        if (!BuildConfig.DEBUG && !ServerProps.getInstance().getIsInternalUser() && !ServerProps.getInstance().getMediaDrawingEnabled()) {
            return false;
        }

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (fragment instanceof ImageEditFragment) {
            ImageEditFragment imageEditFragment = (ImageEditFragment) fragment;
            return imageEditFragment.canUndo();
        }

        return false;
    }

    private void undo() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (fragment instanceof ImageEditFragment) {
            ImageEditFragment imageEditFragment = (ImageEditFragment) fragment;
            imageEditFragment.undo();
        }
    }

    private void setupButtons() {
        View undoButton = findViewById(R.id.undo);
        undoButton.setVisibility(canUndo() ? View.VISIBLE : View.GONE);
        undoButton.setOnClickListener(v -> {
            if (isExitInProgress) {
                return;
            }

            undo();
        });

        findViewById(R.id.done).setOnClickListener(v -> {
            if (isExitInProgress) {
                return;
            }
            isExitInProgress = true;

            findViewById(R.id.processing).setVisibility(View.VISIBLE);

            bgWorkers.execute(() -> {
                viewModel.save(this);
                runOnUiThread(() -> {
                    findViewById(R.id.processing).setVisibility(View.GONE);
                    setResult(RESULT_OK, getIntentWithResults());
                    prepareTransitionView(false, this::finishAfterTransition);
                });
            });
        });
    }

    private void setupMediaEdit() {
        viewModel.getSelected().observe(this, model -> {
            Class<? extends Fragment> klass = model.getType() == Media.MEDIA_TYPE_IMAGE ? ImageEditFragment.class : VideoEditFragment.class;

            getSupportFragmentManager()
                    .beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(R.id.fragment_container, klass, null)
                    .commit();

            previewAdapter.notifyDataSetChanged();

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

    private void setupPreviewListView() {
        LinearLayoutManager linearManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);

        RecyclerView previewListView = findViewById(R.id.preview_list);
        previewListView.setLayoutManager(linearManager);
        previewListView.addItemDecoration(new LinearSpacingItemDecoration(linearManager, getResources().getDimensionPixelSize(R.dimen.media_crop_preview_list_spacing)));

        previewAdapter = new MediaPreviewAdapter();
        previewListView.setAdapter(previewAdapter);

        viewModel.getMedia().observe(this, models -> {
            if (models.size() > 1) {
                previewListView.setVisibility(View.VISIBLE);
                previewAdapter.onViewModelUpdate(models);
            } else {
                previewListView.setVisibility(View.GONE);
            }
        });

        setupDragNDrop(previewListView);
    }

    private void setupDragNDrop(RecyclerView previewListView) {
        new ItemTouchHelper(new ItemTouchHelper.Callback() {
            private int start = -1, end = -1;
            private float alpha;

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                if (start == -1) {
                    start = viewHolder.getAdapterPosition();
                }

                end = target.getAdapterPosition();
                previewAdapter.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());

                return true;
            }

            @Override
            public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);

                if (start != -1 && end != -1 && start != end) {
                    viewModel.move(start, end);
                }

                start = end = -1;
                ((ViewHolder) viewHolder).thumbnailView.setAlpha(alpha);
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            }

            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                return makeFlag(ItemTouchHelper.ACTION_STATE_DRAG, ItemTouchHelper.START | ItemTouchHelper.END);
            }

            @Override
            public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
                super.onSelectedChanged(viewHolder, actionState);

                if (actionState != ItemTouchHelper.ACTION_STATE_IDLE && viewHolder != null) {
                    ImageView draggedThumbnail = ((ViewHolder) viewHolder).thumbnailView;
                    alpha = draggedThumbnail.getAlpha();
                    draggedThumbnail.setAlpha(PREVIEW_SELECTED_ALPHA);
                }
            }
        }).attachToRecyclerView(previewListView);
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

    private void onMediaSelect(int position) {
        if (viewModel.getSelectedPosition() == position) {
            return;
        }

        viewModel.select(position);
    }

    private class MediaPreviewAdapter extends RecyclerView.Adapter<ViewHolder> {
        private final List<MediaEditViewModel.Model> dataset = new ArrayList<>();

        public void onViewModelUpdate(@NonNull List<MediaEditViewModel.Model> models) {
            dataset.clear();
            dataset.addAll(models);

            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.media_preview_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            MediaEditViewModel.Model model = dataset.get(position);

            holder.thumbnailView.setImageDrawable(null);
            thumbnailLoader.load(holder.thumbnailView, model.original);

            if (model.getType() == Media.MEDIA_TYPE_VIDEO) {
                holder.typeIndicator.setVisibility(View.VISIBLE);
                holder.thumbnailView.setContentDescription(getString(R.string.video));
            } else {
                holder.typeIndicator.setVisibility(View.GONE);
                holder.thumbnailView.setContentDescription(getString(R.string.photo));
            }

            if (viewModel.getSelectedPosition() == position) {
                holder.thumbnailView.setAlpha(PREVIEW_SELECTED_ALPHA);
                holder.itemView.setBackgroundResource(R.drawable.media_preview_item_selected);
            } else {
                holder.thumbnailView.setAlpha(PREVIEW_DEFAULT_ALPHA);
                holder.itemView.setBackgroundResource(R.drawable.media_preview_item_default);
            }

            holder.thumbnailView.setOnClickListener(v -> onMediaSelect(position));
        }

        @Override
        public int getItemCount() {
            return dataset.size();
        }
    }

    private class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView thumbnailView;
        final ImageView typeIndicator;

        private final float thumbnailRadius = getResources().getDimension(R.dimen.details_media_list_thumb_radius);

        ViewHolder(@NonNull View v) {
            super(v);

            typeIndicator = v.findViewById(R.id.type_indicator);
            thumbnailView = v.findViewById(R.id.thumbnail);

            if (thumbnailView != null) {
                thumbnailView.setOutlineProvider(new ViewOutlineProvider() {
                    @Override
                    public void getOutline(View view, Outline outline) {
                        outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), thumbnailRadius);
                    }
                });
                thumbnailView.setClipToOutline(true);
            }
        }
    }
}
