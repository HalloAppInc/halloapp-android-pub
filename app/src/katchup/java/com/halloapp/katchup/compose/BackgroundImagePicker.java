package com.halloapp.katchup.compose;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Application;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.FileStore;
import com.halloapp.R;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.mediapicker.GalleryDataSource;
import com.halloapp.ui.mediapicker.GalleryItem;
import com.halloapp.ui.mediapicker.GalleryThumbnailLoader;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.FileUtils;
import com.halloapp.util.Preconditions;

import java.io.File;
import java.util.List;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class BackgroundImagePicker extends HalloActivity implements EasyPermissions.PermissionCallbacks {

    public static Intent open(@NonNull Context context) {
        Intent intent = new Intent(context, BackgroundImagePicker.class);
        return intent;
    }

    public static final String EXTRA_RESULT_FILE = "result_file";

    private static final int ITEMS_PER_ROW = 3;

    private static final int REQUEST_CODE_ASK_STORAGE_PERMISSION = 2;

    private GalleryThumbnailLoader thumbnailLoader;
    private MediaItemsAdapter adapter = new MediaItemsAdapter();
    private BackgroundImagePickerViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.BLACK);

        setContentView(R.layout.activity_background_image_picker);

        View back = findViewById(R.id.back);
        back.setOnClickListener(v -> {
            onBackPressed();
        });

        thumbnailLoader = new GalleryThumbnailLoader(this, getResources().getDimensionPixelSize(R.dimen.media_gallery_grid_size));

        final GridLayoutManager layoutManager = new GridLayoutManager(this, ITEMS_PER_ROW);
        final RecyclerView mediaView = findViewById(android.R.id.list);
        mediaView.setLayoutManager(layoutManager);
        mediaView.addItemDecoration(new RecyclerView.ItemDecoration() {
            final int spacing = getResources().getDimensionPixelSize(R.dimen.media_gallery_grid_spacing);
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                int position = parent.getChildAdapterPosition(view);
                int column = position % ITEMS_PER_ROW;
                int columnsCount = ITEMS_PER_ROW;

                outRect.bottom = spacing;
                outRect.left = column * spacing / columnsCount;
                outRect.right = spacing - (column + 1) * spacing / columnsCount;
            }
        });
        mediaView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this, new BackgroundImagePickerViewModel.Factory(getApplication())).get(BackgroundImagePickerViewModel.class);

        viewModel.getMediaList().observe(this, mediaItems -> {
            adapter.setPagedList(mediaItems);
        });

        requestStoragePermissions();
    }

    private void handleSelection(@NonNull GalleryItem galleryItem) {
        BgWorkers.getInstance().execute(() -> {
            Uri uri = ContentUris.withAppendedId(MediaStore.Files.getContentUri(GalleryDataSource.MEDIA_VOLUME), galleryItem.id);
            File file = FileStore.getInstance().getTmpFileForUri(uri, null);
            FileUtils.uriToFile(this, uri, file);
            Intent intent = new Intent();
            intent.putExtra(EXTRA_RESULT_FILE, file.getAbsolutePath());
            setResult(RESULT_OK, intent);
            finish();
        });
    }

    private void requestStoragePermissions() {
        final String[] perms = Build.VERSION.SDK_INT >= 31
                ? new String[] {Manifest.permission.READ_MEDIA_IMAGES}
                : new String[] {Manifest.permission.READ_EXTERNAL_STORAGE};
        if (!EasyPermissions.hasPermissions(this, perms)) {
            EasyPermissions.requestPermissions(this, getString(R.string.storage_permission_rationale),
                    REQUEST_CODE_ASK_STORAGE_PERMISSION, perms);
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> list) {
        //noinspection SwitchStatementWithTooFewBranches
        switch (requestCode) {
            case REQUEST_CODE_ASK_STORAGE_PERMISSION: {
                viewModel.invalidateGallery();
                break;
            }
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            //noinspection SwitchStatementWithTooFewBranches
            switch (requestCode) {
                case REQUEST_CODE_ASK_STORAGE_PERMISSION: {
                    new AppSettingsDialog.Builder(this)
                            .setRationale(getString(R.string.storage_permission_rationale_denied))
                            .build().show();
                    break;
                }
            }
        }
    }

    public class MediaItemsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int VIEW_TYPE_MEDIA = 2;

        private PagedList<GalleryItem> items;

        private final PagedList.Callback pagedListCallback = new PagedList.Callback() {
            @Override
            public void onChanged(int position, int count) {
            }

            @Override
            public void onInserted(int position, int count) {
                notifyDataSetChanged();
            }

            @Override
            public void onRemoved(int position, int count) {
            }
        };

        MediaItemsAdapter() {
            super();
            setHasStableIds(true);
        }

        public void setPagedList(PagedList<GalleryItem> items) {
            this.items = items;
            this.items.addWeakCallback(null, pagedListCallback);

            notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            return items == null ? 0 : items.size();
        }

        @Override
        public int getItemViewType(int position) {
            return VIEW_TYPE_MEDIA;
        }

        @Override
        public @NonNull
        RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new MediaItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.media_gallery_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof MediaItemViewHolder) {
                ((MediaItemViewHolder) holder).bindTo(Preconditions.checkNotNull(items.get(position)));
                items.loadAround(position);
            }
        }

        @Override
        public long getItemId(int position) {
            return Preconditions.checkNotNull(items.get(position)).id;
        }
    }

    public class MediaItemViewHolder extends RecyclerView.ViewHolder {

        final ImageView thumbnailView;
        final View thumbnailFrame;
        final ImageView selectionIndicator;
        final TextView selectionCounter;
        final TextView duration;

        GalleryItem galleryItem;

        @SuppressLint("ClickableViewAccessibility")
        MediaItemViewHolder(final @NonNull View v) {
            super(v);
            thumbnailView = v.findViewById(R.id.thumbnail);
            thumbnailFrame = v.findViewById(R.id.thumbnail_frame);
            selectionIndicator = v.findViewById(R.id.selection_indicator);
            selectionCounter = v.findViewById(R.id.selection_counter);
            duration = v.findViewById(R.id.duration);

            if (thumbnailView != null) {
                thumbnailView.setOnClickListener(tv -> onItemClicked());
            }
        }

        void bindTo(final @NonNull GalleryItem galleryItem) {
            this.galleryItem = galleryItem;
            if (galleryItem.type == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) {
                duration.setVisibility(View.VISIBLE);
                duration.setText(DateUtils.formatElapsedTime(galleryItem.duration / 1000));
                thumbnailView.setContentDescription(getString(R.string.video));
            } else {
                duration.setVisibility(View.GONE);
                thumbnailView.setContentDescription(getString(R.string.photo));
            }
            selectionIndicator.setVisibility(View.GONE);
            thumbnailFrame.setPadding(0, 0, 0, 0);
            thumbnailView.setSelected(false);
            thumbnailLoader.load(thumbnailView, galleryItem);
        }

        private void onItemClicked() {
            handleSelection(galleryItem);
        }
    }

    static class BackgroundImagePickerViewModel extends AndroidViewModel {

        private final GalleryDataSource.Factory dataSourceFactory;
        private final LiveData<PagedList<GalleryItem>> mediaList;

        public BackgroundImagePickerViewModel(@NonNull Application application) {
            super(application);

            dataSourceFactory = new GalleryDataSource.Factory(getApplication().getContentResolver(), false);
            mediaList = new LivePagedListBuilder<>(dataSourceFactory, 250).build();
        }

        public LiveData<PagedList<GalleryItem>> getMediaList() {
            return mediaList;
        }

        public void invalidateGallery() {
            dataSourceFactory.invalidateLatestDataSource();
        }

        public static class Factory implements ViewModelProvider.Factory {
            private final Application application;

            public Factory(@NonNull Application application) {
                this.application = application;
            }

            @Override
            public @NonNull <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                if (modelClass.isAssignableFrom(BackgroundImagePickerViewModel.class)) {
                    //noinspection unchecked
                    return (T) new BackgroundImagePickerViewModel(application);
                }
                throw new IllegalArgumentException("Unknown ViewModel class");
            }
        }
    }
}
