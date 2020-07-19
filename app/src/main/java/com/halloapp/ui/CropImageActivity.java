package com.halloapp.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.transition.Fade;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.Window;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.Constants;
import com.halloapp.R;
import com.halloapp.content.Media;
import com.halloapp.media.MediaThumbnailLoader;
import com.halloapp.ui.mediapicker.MediaPickerActivity;
import com.halloapp.util.Log;
import com.halloapp.util.Preconditions;
import com.halloapp.util.ViewDataLoader;
import com.halloapp.widget.CenterToast;
import com.halloapp.widget.ClippedBitmapDrawable;
import com.halloapp.widget.LinearSpacingItemDecoration;
import com.halloapp.widget.PlaceholderDrawable;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;
import java.util.List;

public class CropImageActivity extends HalloActivity {
    public static final String EXTRA_MEDIA = "media";
    public static final String EXTRA_SELECTED = "selected";
    public static final String EXTRA_STATE = "state";

    public static final String TRANSITION_VIEW_NAME = "crop_image";

    private static final int REQUEST_CODE_MORE_MEDIA = 1;

    private CropImageView cropImageView;
    private RecyclerView mediaListView;
    private CropImageViewModel viewModel;
    private MediaListAdapter adapter;
    private MediaThumbnailLoader mediaLoader;
    private CropImageViewModel.MediaModel selected;
    private boolean transitionStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("ImageCropActivity: onCreate");

        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        getWindow().setEnterTransition(new Fade());
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
        cropImageView = findViewById(R.id.image);
        cropImageView.setTransitionName(TRANSITION_VIEW_NAME);
        cropImageView.setMinAspectRatio(1 / Constants.MAX_IMAGE_ASPECT_RATIO);

        cropImageView.setOnSetImageUriCompleteListener((view, uri, error) -> {
            if (!transitionStarted) {
                transitionStarted = true;
                startPostponedEnterTransition();
            }

            if (error != null) {
                CenterToast.show(this, R.string.failed_to_load_media);
                finish();
            } else {
                if (selected == null || selected.state == null) {
                    setInitialFrame();
                }
            }
        });

        viewModel.getSelected().observe(this, model -> {
            if (model == null) {
                return;
            }

            selected = model;

            if (selected.state != null) {
                cropImageView.clearImage();
                cropImageView.onRestoreInstanceState(selected.state);
            } else {
                cropImageView.setImageUriAsync(Uri.fromFile(selected.original.file));
            }

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

            protected boolean isLast(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                return recyclerView.getAdapter() != null && recyclerView.getAdapter().getItemCount() == viewHolder.getAdapterPosition() + 1;
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                if (start == -1) {
                    start = viewHolder.getAdapterPosition();
                }

                if (isLast(recyclerView, target)) {
                    return false;
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
                if (isLast(recyclerView, viewHolder)) {
                    return 0;
                } else {
                    return makeFlag(ItemTouchHelper.ACTION_STATE_DRAG, ItemTouchHelper.START | ItemTouchHelper.END);
                }
            }
        });

        helper.attachToRecyclerView(mediaListView);
    }

    private void setupButtons() {
        findViewById(R.id.reset).setOnClickListener(v -> {
            cropImageView.resetCropRect();
            setInitialFrame();
        });
        findViewById(R.id.done).setOnClickListener(v -> {
            cropImageView.setOnCropImageCompleteListener((view, result) -> {
                selected.state = cropImageView.onSaveInstanceState();
                mediaLoader.remove(selected.edit.file);
                viewModel.update(selected);

                final Intent intent = new Intent();
                prepareResults(intent);
                setResult(RESULT_OK, intent);

                cropImageView.setShowCropOverlay(false);
                finishAfterTransition();
            });

            saveCroppedImageAsync();
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

    private void setInitialFrame() {
        Rect rect = cropImageView.getWholeImageRect();
        final int w, h;

        if (cropImageView.getRotatedDegrees() % 180 == 0) {
            w = rect.width();
            h = rect.height();
        } else {
            w = rect.height();
            h = rect.width();
        }

        final Rect cropRect;
        if (h > Constants.MAX_IMAGE_ASPECT_RATIO * w) {
            int padding = (int) ((h - Constants.MAX_IMAGE_ASPECT_RATIO * w) / 2);
            cropRect = new Rect(0, padding, w, h - padding);
        } else {
            cropRect = new Rect(0, 0, w, h);
        }

        cropImageView.setCropRect(cropRect);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("ImageCropActivity: onDestroy");
    }

    @Override
    public void onBackPressed() {
        cropImageView.setOnCropImageCompleteListener((view, result) -> {
            selected.state = cropImageView.onSaveInstanceState();
            mediaLoader.remove(selected.edit.file);
            viewModel.update(selected);

            final Intent intent = new Intent();
            prepareResults(intent);
            setResult(RESULT_OK, intent);

            cropImageView.setShowCropOverlay(false);
            finishAfterTransition();
        });

        saveCroppedImageAsync();
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.image_crop_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.rotate: {
                cropImageView.rotateImage(270);
                return true;
            }
            case R.id.flip: {
                cropImageView.flipImageHorizontally();
                return true;
            }
            case R.id.delete: {
                if (selected != null) {
                    viewModel.delete(selected);
                }
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_MORE_MEDIA) {
            ArrayList<Uri> uris = data.getParcelableArrayListExtra(EXTRA_MEDIA);
            Bundle state = data.getBundleExtra(EXTRA_STATE);

            viewModel.loadMediaData(uris, state);
        }
    }

    private void saveCroppedImageAsync() {
        cropImageView.saveCroppedImageAsync(
            Uri.fromFile(selected.edit.file),
            Bitmap.CompressFormat.JPEG,
            Constants.JPEG_QUALITY,
            Constants.MAX_IMAGE_DIMENSION,
            Constants.MAX_IMAGE_DIMENSION,
            CropImageView.RequestSizeOptions.RESIZE_INSIDE
        );
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

    private void selectMoreImages() {
        cropImageView.setOnCropImageCompleteListener((view, result) -> {
            selected.state = cropImageView.onSaveInstanceState();
            mediaLoader.remove(selected.edit.file);
            viewModel.update(selected);

            final Intent intent = new Intent(this, MediaPickerActivity.class);
            intent.putExtra(MediaPickerActivity.EXTRA_PICKER_PURPOSE, MediaPickerActivity.PICKER_PURPOSE_RESULT);

            prepareResults(intent);
            startActivityForResult(intent, REQUEST_CODE_MORE_MEDIA);
        });

        saveCroppedImageAsync();
    }

    private void onMediaSelect(@NonNull Media media, int position) {
        if (media.type == Media.MEDIA_TYPE_VIDEO || viewModel.getSelectedPosition() == position) {
            return;
        }

        if (selected != null) {
            selected.state = cropImageView.onSaveInstanceState();

            cropImageView.setOnCropImageCompleteListener((view, result) -> {
                mediaLoader.remove(selected.edit.file);
                viewModel.update(selected);
                viewModel.select(position);
            });

            saveCroppedImageAsync();
        } else {
            viewModel.select(position);
        }
    }

    private class MediaListAdapter extends RecyclerView.Adapter<MediaListAdapter.ViewHolder> {
        private static final int VIEW_TYPE_IMAGE = 1;
        private static final int VIEW_TYPE_BUTTON = 2;

        private final int WHITE_20 = ContextCompat.getColor(getBaseContext(), R.color.white_20);
        private final List<CropImageViewModel.MediaModel> dataset = new ArrayList<>();

        MediaListAdapter() {
            super();
        }

        void onViewModelUpdate(@NonNull List<CropImageViewModel.MediaModel> models) {
            dataset.clear();
            dataset.addAll(models);

            notifyDataSetChanged();
        }

        @Override
        public int getItemViewType(int position) {
            return dataset.size() == position ? VIEW_TYPE_BUTTON : VIEW_TYPE_IMAGE;
        }

        @NonNull
        @Override
        public MediaListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_IMAGE) {
                return new MediaListAdapter.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.media_preview_item, parent, false));
            } else {
                return new MediaListAdapter.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.cropper_add_button, parent, false));
            }
        }

        @Override
        public void onBindViewHolder(@NonNull MediaListAdapter.ViewHolder holder, int position) {
            if (position == dataset.size()) {
                holder.button.setOnClickListener(v -> selectMoreImages());
            } else {
                final CropImageViewModel.MediaModel model = dataset.get(position);
                final Media media = model.state == null ? model.original : model.edit;

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
                } else {
                    holder.itemView.setBackgroundColor(WHITE_20);
                }

                holder.thumbnailView.setOnClickListener(v -> onMediaSelect(media, position));
            }
        }

        @Override
        public int getItemCount() {
            return dataset.size() + 1;
        }

        private class ViewHolder extends RecyclerView.ViewHolder {
            final ImageView button;
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
                            outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), getResources().getDimension(R.dimen.details_media_list_corner_radius));
                        }
                    });
                    thumbnailView.setClipToOutline(true);
                }

                button = v.findViewById(R.id.button);
            }
        }

        private class Displayer implements ViewDataLoader.Displayer<ImageView, Bitmap> {
            private Media media;

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
