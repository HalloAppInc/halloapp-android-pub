package com.halloapp.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.transition.Fade;
import android.transition.Transition;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.Constants;
import com.halloapp.R;
import com.halloapp.content.Media;
import com.halloapp.media.MediaThumbnailLoader;
import com.halloapp.ui.mediaedit.EditImageView;
import com.halloapp.ui.mediaedit.ImageCropper;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.Preconditions;
import com.halloapp.util.ViewDataLoader;
import com.halloapp.widget.ClippedBitmapDrawable;
import com.halloapp.widget.LinearSpacingItemDecoration;
import com.halloapp.widget.PlaceholderDrawable;

import java.util.ArrayList;
import java.util.List;

public class CropImageActivity extends HalloActivity {
    public static final String EXTRA_MEDIA = "media";
    public static final String EXTRA_SELECTED = "selected";
    public static final String EXTRA_STATE = "state";

    public static final String TRANSITION_VIEW_NAME = "crop_image";

    private static final int REQUEST_CODE_MORE_MEDIA = 1;

    private EditImageView editImageView;
    private ImageView transitionView;
    private RecyclerView mediaListView;
    private CropImageViewModel viewModel;
    private MediaListAdapter adapter;
    private MediaThumbnailLoader mediaLoader;
    private CropImageViewModel.MediaModel selected;
    private boolean transitionStarted = false;
    private final BgWorkers bgWorkers = BgWorkers.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Fade fade = new Fade();
        fade.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {
            }

            @Override
            public void onTransitionEnd(Transition transition) {
                // NOTE(stefan): trans.setVisibility(View.GONE) doesn't seem to work here, instead make view zero size
                ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)transitionView.getLayoutParams();
                params.topMargin = 0;
                params.leftMargin = 0;
                params.bottomMargin = editImageView.getHeight();
                params.rightMargin = editImageView.getWidth();
                transitionView.setLayoutParams(params);
            }

            @Override
            public void onTransitionCancel(Transition transition) {
            }

            @Override
            public void onTransitionPause(Transition transition) {
            }

            @Override
            public void onTransitionResume(Transition transition) {
            }
        });

        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        getWindow().setEnterTransition(fade);
        postponeEnterTransition();

        setContentView(R.layout.activity_image_crop);
        Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        viewModel = new ViewModelProvider(this).get(CropImageViewModel.class);
        mediaLoader = new MediaThumbnailLoader(this, 2 * getResources().getDimensionPixelSize(R.dimen.details_media_list_height));

        setupEditView();
        setupMediaListView();
        setupButtons();

        ArrayList<Uri> uris = getIntent().getParcelableArrayListExtra(EXTRA_MEDIA);
        Bundle state = getIntent().getBundleExtra(EXTRA_STATE);

        viewModel.loadMediaData(uris, state, getIntent().getIntExtra(EXTRA_SELECTED, 0));

        viewModel.getMediaData().observe(this, this::finishWhenNoImages);
    }

    private void setupEditView() {
        transitionView = findViewById(R.id.transition);
        transitionView.setTransitionName(TRANSITION_VIEW_NAME);

        editImageView = findViewById(R.id.image);

        viewModel.getSelected().observe(this, model -> {
            if (model == null) {
                return;
            }

            selected = model;

            editImageView.setAsyncImageFile(selected.original.file, selected.getMedia().file, (EditImageView.State) selected.getState(), (originalBitmap, croppedBitmap) -> {
                if (!transitionStarted) {
                    prepareTransitionView(originalBitmap, croppedBitmap);
                    transitionView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            transitionView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            transitionStarted = true;
                            startPostponedEnterTransition();
                        }
                    });
                }
            });

            adapter.notifyDataSetChanged();
        });
    }

    private void setupMediaListView() {
        final LinearLayoutManager linearManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);

        mediaListView = findViewById(R.id.media_list);
        mediaListView.setLayoutManager(linearManager);
        mediaListView.addItemDecoration(new LinearSpacingItemDecoration(linearManager, getResources().getDimensionPixelSize(R.dimen.media_crop_preview_list_spacing)));

        adapter = new MediaListAdapter();
        mediaListView.setAdapter(adapter);

        viewModel.getMediaData().observe(this, adapter::onViewModelUpdate);

        setupMediaListDragNDrop();
    }

    private void setupMediaListDragNDrop() {
        // Drag & drop all views except the last one which contains an add button
        final ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
            private int start = -1, end = -1;

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                if (start == -1) {
                    start = viewHolder.getAdapterPosition();
                }

                end = target.getAdapterPosition();
                adapter.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());

                return true;
            }

            @Override
            public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);

                if (start != -1 && end != -1) {
                    viewModel.onMoved(start, end);
                }

                start = end = -1;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            }

            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                return makeFlag(ItemTouchHelper.ACTION_STATE_DRAG, ItemTouchHelper.START | ItemTouchHelper.END);
            }
        });

        helper.attachToRecyclerView(mediaListView);
    }

    private void setupButtons() {
        findViewById(R.id.reset).setOnClickListener(v -> editImageView.reset());
        findViewById(R.id.done).setOnClickListener(v -> saveAndExitWithTransition());
    }

    private void prepareTransitionView(Bitmap originalBitmap, Bitmap croppedBitmap) {
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)transitionView.getLayoutParams();
        EditImageView.State state = (EditImageView.State) selected.getState();

        if (state == null) {
            params.topMargin = 0;
            params.leftMargin = 0;
            params.bottomMargin = 0;
            params.rightMargin = 0;
            transitionView.setImageDrawable(null);
            transitionView.setImageBitmap(originalBitmap);
        } else {
            params.topMargin = (int)state.cropRect.top;
            params.leftMargin = (int)state.cropRect.left;
            params.bottomMargin = (int)(editImageView.getHeight() - state.cropRect.bottom);
            params.rightMargin = (int)(editImageView.getWidth() - state.cropRect.right);
            transitionView.setImageDrawable(null);
            transitionView.setImageBitmap(croppedBitmap);
        }

        transitionView.setLayoutParams(params);
    }

    private void saveAndExitWithTransition() {
        final EditImageView.State state = editImageView.getState();

        ImageCropper.crop(this, selected.original.file, selected.tmp.file, state, (originalBitmap, croppedBitmap) -> {
            selected.tmpState = state;

            bgWorkers.execute(() -> {
                for (CropImageViewModel.MediaModel model : viewModel.getMediaData().getValue()) {
                    model.save();
                    model.clear();
                }

                runOnUiThread(() -> {
                    prepareTransitionView(originalBitmap, croppedBitmap);
                    transitionView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            transitionView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            final Intent intent = new Intent();
                            prepareResults(intent);
                            setResult(RESULT_OK, intent);
                            finishAfterTransition();
                        }
                    });
                });
            });
        });
    }

    private void prepareResults(@NonNull Intent intent) {
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
    }

    @Override
    public void onBackPressed() {
        bgWorkers.execute(() -> {
            for (CropImageViewModel.MediaModel model : viewModel.getMediaData().getValue()) {
                model.clear();
            }

            runOnUiThread(this::finish);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.image_crop_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.rotate) {
            editImageView.rotate();
            return true;
        } else if (id == R.id.flip) {
            editImageView.flip();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_MORE_MEDIA) {
            ArrayList<Uri> uris = new ArrayList<>();
            Bundle state = null;

            if (data != null) {
                uris = data.getParcelableArrayListExtra(EXTRA_MEDIA);
                state = data.getBundleExtra(EXTRA_STATE);
            }

            viewModel.loadMediaData(uris, state);
        }
    }

    private void finishWhenNoImages(List<CropImageViewModel.MediaModel> models) {
        boolean hasImages = false;
        for (CropImageViewModel.MediaModel m : models) {
            if (m.original.type == Media.MEDIA_TYPE_IMAGE) {
                hasImages = true;
                break;
            }
        }

        if (!hasImages) {
            final Intent intent = new Intent();
            prepareResults(intent);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    private void onMediaSelect(@NonNull Media media, int position) {
        if (media.type == Media.MEDIA_TYPE_VIDEO || viewModel.getSelectedPosition() == position) {
            return;
        }

        if (selected != null) {
            final EditImageView.State state = editImageView.getState();

            ImageCropper.crop(this, selected.original.file, selected.tmp.file, state, (originalBitmap, croppedBitmap) -> {
                selected.tmpState = state;
                mediaLoader.remove(selected.tmp.file);
                viewModel.update(selected);
                viewModel.select(position);
            });
        } else {
            viewModel.select(position);
        }
    }

    private class MediaListAdapter extends RecyclerView.Adapter<MediaListAdapter.ViewHolder> {
        private final int backgroundColorDefault = ContextCompat.getColor(getBaseContext(), R.color.white_20);
        private final List<CropImageViewModel.MediaModel> dataset = new ArrayList<>();

        MediaListAdapter() {
            super();
        }

        void onViewModelUpdate(@NonNull List<CropImageViewModel.MediaModel> models) {
            dataset.clear();
            dataset.addAll(models);

            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public MediaListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new MediaListAdapter.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.media_preview_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull MediaListAdapter.ViewHolder holder, int position) {
            final CropImageViewModel.MediaModel model = dataset.get(position);
            final Media media = model.getMedia();

            holder.thumbnailView.setImageDrawable(null);
            mediaLoader.load(holder.thumbnailView, media, new Displayer(media));

            if (media.type == Media.MEDIA_TYPE_VIDEO) {
                holder.typeIndicator.setImageResource(R.drawable.ic_video);
                holder.typeIndicator.setVisibility(View.VISIBLE);
                holder.thumbnailView.setContentDescription(getString(R.string.video));
            } else {
                holder.typeIndicator.setVisibility(View.GONE);
                holder.thumbnailView.setContentDescription(getString(R.string.photo));
            }

            if (viewModel.getSelectedPosition() == position) {
                holder.itemView.setBackgroundColor(Color.WHITE);
                holder.thumbnailView.setAlpha(1.0f);
            } else {
                holder.itemView.setBackgroundColor(backgroundColorDefault);
                holder.thumbnailView.setAlpha(.6f);
            }

            holder.thumbnailView.setOnClickListener(v -> onMediaSelect(media, position));
        }

        @Override
        public int getItemCount() {
            return dataset.size();
        }

        private class ViewHolder extends RecyclerView.ViewHolder {
            final ImageView thumbnailView;
            final ImageView typeIndicator;

            ViewHolder(@NonNull View v) {
                super(v);

                v.setOutlineProvider(new ViewOutlineProvider() {
                    @Override
                    public void getOutline(View view, Outline outline) {
                        outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), getResources().getDimension(R.dimen.details_media_list_corner_radius));
                    }
                });
                v.setClipToOutline(true);

                typeIndicator = v.findViewById(R.id.type_indicator);
                thumbnailView = v.findViewById(R.id.thumbnail);
                if (thumbnailView != null) {
                    thumbnailView.setOutlineProvider(new ViewOutlineProvider() {
                        @Override
                        public void getOutline(View view, Outline outline) {
                            outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), getResources().getDimension(R.dimen.details_media_list_thumb_radius));
                        }
                    });
                    thumbnailView.setClipToOutline(true);
                }
            }
        }

        private class Displayer implements ViewDataLoader.Displayer<ImageView, Bitmap> {
            final private Media media;

            Displayer(Media media) {
                this.media = media;
            }

            @Override
            public void showResult(@NonNull ImageView view, @Nullable Bitmap bitmap) {
                if (bitmap != null) {
                    if (bitmap.getHeight() > Constants.MAX_IMAGE_ASPECT_RATIO * bitmap.getWidth()) {
                        final int padding = (int) ((bitmap.getHeight() - Constants.MAX_IMAGE_ASPECT_RATIO * bitmap.getWidth()) / 2);
                        final Rect bitmapRect = new Rect(0, padding, bitmap.getWidth(), bitmap.getHeight() - padding);
                        view.setImageDrawable(new ClippedBitmapDrawable(bitmap, bitmapRect));
                    } else {
                        view.setImageBitmap(bitmap);
                    }
                } else {
                    showLoading(view);
                }
            }

            @Override
            public void showLoading(@NonNull ImageView view) {
                if (media.width != 0 && media.height != 0) {
                    view.setImageDrawable(new PlaceholderDrawable(media.width, media.height, ContextCompat.getColor(getBaseContext(), R.color.media_placeholder)));
                } else {
                    view.setImageDrawable(null);
                }
            }
        }
    }
}
