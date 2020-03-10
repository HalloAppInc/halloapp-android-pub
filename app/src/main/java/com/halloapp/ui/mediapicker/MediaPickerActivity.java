package com.halloapp.ui.mediapicker;

import android.Manifest;
import android.content.ClipData;
import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.core.util.Preconditions;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.Constants;
import com.halloapp.R;
import com.halloapp.ui.PostComposerActivity;
import com.halloapp.ui.avatar.AvatarPreviewActivity;
import com.halloapp.util.Log;
import com.halloapp.widget.ActionBarShadowOnScrollListener;
import com.halloapp.widget.CenterToast;
import com.halloapp.widget.GridSpacingItemDecoration;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class MediaPickerActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    public static final String EXTRA_PICKER_PURPOSE = "picker_purpose";

    public static final int PICKER_PURPOSE_POST = 1;
    public static final int PICKER_PURPOSE_AVATAR = 2;

    private static final int REQUEST_CODE_ASK_STORAGE_PERMISSION = 1;
    private static final int REQUEST_CODE_COMPOSE_POST = 2;
    private static final int REQUEST_CODE_PICK_MEDIA = 3;
    private static final int REQUEST_CODE_SET_AVATAR = 4;

    private MediaPickerViewModel viewModel;
    private MediaItemsAdapter adapter;
    private GalleryThumbnailLoader thumbnailLoader;

    private final Set<Long> selectedItems = new LinkedHashSet<>();
    private ActionMode actionMode;
    private int pickerPurpose = PICKER_PURPOSE_POST;

    private static final String KEY_SELECTED_MEDIA = "selected_media";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_picker);

        Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        pickerPurpose = getIntent().getIntExtra(EXTRA_PICKER_PURPOSE, PICKER_PURPOSE_POST);

        final RecyclerView mediaView = findViewById(android.R.id.list);
        final View progressView = findViewById(R.id.progress);
        final View emptyView = findViewById(android.R.id.empty);

        mediaView.addOnScrollListener(new ActionBarShadowOnScrollListener(this));

        final GridLayoutManager layoutManager = new GridLayoutManager(this, 1);
        mediaView.setLayoutManager(layoutManager);
        mediaView.getViewTreeObserver().addOnGlobalLayoutListener(() ->
                layoutManager.setSpanCount((mediaView.getWidth() - mediaView.getPaddingLeft() - mediaView.getPaddingRight()) /
                        getResources().getDimensionPixelSize(R.dimen.media_gallery_grid_size)));

        mediaView.addItemDecoration(new GridSpacingItemDecoration(layoutManager, getResources().getDimensionPixelSize(R.dimen.media_gallery_grid_spacing)));

        adapter = new MediaItemsAdapter();
        mediaView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(MediaPickerViewModel.class);
        viewModel.mediaList.observe(this, mediaItems -> {
            adapter.submitList(mediaItems);
            progressView.setVisibility(View.GONE);
            emptyView.setVisibility(mediaItems.isEmpty() ? View.VISIBLE : View.GONE);
        });

        thumbnailLoader = new GalleryThumbnailLoader(this, getResources().getDimensionPixelSize(R.dimen.media_gallery_grid_size));

        if (savedInstanceState != null) {
            final long [] selectedItemsArray = savedInstanceState.getLongArray(KEY_SELECTED_MEDIA);
            if (selectedItemsArray != null) {
                for (long item : selectedItemsArray) {
                    selectedItems.add(item);
                }
                updateActionMode();
            }
        }

        final String[] perms = {Manifest.permission.READ_EXTERNAL_STORAGE};
        if (!EasyPermissions.hasPermissions(this, perms)) {
            EasyPermissions.requestPermissions(this, getString(R.string.storage_permission_rationale),
                    REQUEST_CODE_ASK_STORAGE_PERMISSION, perms);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        thumbnailLoader.destroy();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (!selectedItems.isEmpty()) {
            final long [] selectedItemsArray = new long [selectedItems.size()];
            int index = 0;
            for (Long item : selectedItems) {
                selectedItemsArray[index] = item;
                index++;
            }
            outState.putLongArray(KEY_SELECTED_MEDIA, selectedItemsArray);
        }
    }

    @Override
    public void onActivityResult(final int request, final int result, final Intent data) {
        super.onActivityResult(request, result, data);
        switch (request) {
            case REQUEST_CODE_PICK_MEDIA: {
                if (result == RESULT_OK) {
                    if (data == null) {
                        Log.e("MediaPackerActivity.onActivityResult.REQUEST_CODE_PICK_IMAGE: no data");
                        CenterToast.show(this, R.string.bad_image);
                    } else {
                        final ArrayList<Uri> uris = new ArrayList<>();
                        if (uris.isEmpty()) {
                            final ClipData clipData = data.getClipData();
                            if (clipData != null) {
                                for (int i = 0; i < clipData.getItemCount(); i++) {
                                    uris.add(clipData.getItemAt(i).getUri());
                                }
                            } else {
                                final Uri uri = data.getData();
                                if (uri != null) {
                                    uris.add(uri);
                                }
                            }
                        }
                        if (!uris.isEmpty()) {
                            handleSelection(uris);
                        } else {
                            Log.e("MediaPackerActivity.onActivityResult.REQUEST_CODE_PICK_MEDIA: no uri");
                            CenterToast.show(this, R.string.bad_image);
                        }
                    }
                }
                break;
            }
            case REQUEST_CODE_COMPOSE_POST:
            case REQUEST_CODE_SET_AVATAR: {
                if (result == RESULT_OK) {
                    overridePendingTransition(0, 0);
                    setResult(RESULT_OK);
                    finish();
                }
                break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> list) {
        //noinspection SwitchStatementWithTooFewBranches
        switch (requestCode) {
            case REQUEST_CODE_ASK_STORAGE_PERMISSION: {
                viewModel.invalidate();
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

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.media_picker, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //noinspection SwitchStatementWithTooFewBranches
        switch (item.getItemId()) {
            case R.id.other_media: {
                final Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                final String[] mimeTypes = {"image/*", "video/*"};
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
                startActivityForResult(intent, REQUEST_CODE_PICK_MEDIA);
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    private void startPostComposer(@NonNull ArrayList<Uri> uris) {
        final Intent intent = new Intent(this, PostComposerActivity.class);
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        startActivityForResult(intent, REQUEST_CODE_COMPOSE_POST);
    }

    private void startAvatarPreview(@NonNull ArrayList<Uri> uris) {
        final Intent intent = new Intent(this, AvatarPreviewActivity.class);
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        startActivityForResult(intent, REQUEST_CODE_SET_AVATAR);
    }

    private void handleSelection(@NonNull ArrayList<Uri> uris) {
        if (pickerPurpose == PICKER_PURPOSE_POST) {
            startPostComposer(uris);
        } else if (pickerPurpose == PICKER_PURPOSE_AVATAR) {
            startAvatarPreview(uris);
        }
    }

    private void onItemClicked(@NonNull GalleryItem galleryItem, View view) {
        if (selectedItems.isEmpty()) {
            final ArrayList<Uri> uris = new ArrayList<>(1);
            uris.add(ContentUris.withAppendedId(MediaStore.Files.getContentUri(GalleryDataSource.MEDIA_VOLUME), galleryItem.id));
            handleSelection(uris);
        } else {
            handleMultiSelection(galleryItem, view);
        }
    }

    private void onItemLongClicked(@NonNull GalleryItem galleryItem, View view) {
        handleMultiSelection(galleryItem, view);
    }

    private void handleMultiSelection(@NonNull GalleryItem galleryItem, View view) {
        final float animateScale;
        if (!selectedItems.remove(galleryItem.id)) {
            if (selectedItems.size() >= Constants.MAX_POST_MEDIA_ITEMS) {
                CenterToast.show(this, getResources().getQuantityString(R.plurals.max_post_media_items, Constants.MAX_POST_MEDIA_ITEMS, Constants.MAX_POST_MEDIA_ITEMS));
                return;
            }
            selectedItems.add(galleryItem.id);
            animateScale = .9f;
        } else {
            animateScale = 1.1f;
        }
        final ScaleAnimation animation = new ScaleAnimation(1f, animateScale, 1f, animateScale,
                Animation.RELATIVE_TO_SELF, .5f, Animation.RELATIVE_TO_SELF, .5f);
        animation.setDuration(70);
        animation.setRepeatCount(1);
        animation.setRepeatMode(Animation.REVERSE);
        view.startAnimation(animation);

        adapter.notifyDataSetChanged();
        updateActionMode();
    }

    private void onItemsSelected() {
        final ArrayList<Uri> uris = new ArrayList<>(selectedItems.size());
        for (Long item : selectedItems) {
            uris.add(ContentUris.withAppendedId(MediaStore.Files.getContentUri(GalleryDataSource.MEDIA_VOLUME), item));
        }
        handleSelection(uris);
    }

    private void updateActionMode() {
        if (selectedItems.isEmpty()) {
            if (actionMode != null) {
                actionMode.finish();
                actionMode = null;
            }
        } else {
            if (actionMode == null) {
                actionMode = startSupportActionMode(new ActionMode.Callback() {
                    @Override
                    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                        getMenuInflater().inflate(R.menu.media_picker_action_mode, menu);
                        return true;
                    }

                    @Override
                    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                        return true;
                    }

                    @Override
                    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                        if (item.getItemId() == R.id.select) {
                            onItemsSelected();
                        }
                        return true;
                    }

                    @Override
                    public void onDestroyActionMode(ActionMode mode) {
                        selectedItems.clear();
                        adapter.notifyDataSetChanged();
                        actionMode = null;
                    }
                });
            }
            if (actionMode != null) {
                actionMode.setTitle(String.format(Locale.getDefault(), "%d", selectedItems.size()));
            }
        }
    }

    private static final DiffUtil.ItemCallback<GalleryItem> DIFF_CALLBACK = new DiffUtil.ItemCallback<GalleryItem>() {

        @Override
        public boolean areItemsTheSame(GalleryItem oldItem, GalleryItem newItem) {
            return oldItem.id == newItem.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull GalleryItem oldItem, @NonNull GalleryItem newItem) {
            return oldItem.equals(newItem);
        }
    };

    private class MediaItemsAdapter extends PagedListAdapter<GalleryItem, MediaItemViewHolder> {

        MediaItemsAdapter() {
            super(DIFF_CALLBACK);
            setHasStableIds(true);
        }

        public long getItemId(int position) {
            return Preconditions.checkNotNull(getItem(position)).id;
        }

        @Override
        public @NonNull MediaItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new MediaItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.media_gallery_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull MediaItemViewHolder holder, int position) {
            holder.bindTo(Preconditions.checkNotNull(getItem(position)));
        }
    }

    private class MediaItemViewHolder extends RecyclerView.ViewHolder {

        final ImageView thumbnailView;
        final View thumbnailFrame;
        final ImageView selectionIndicator;
        final ImageView typeIndicator;

        GalleryItem galleryItem;

        MediaItemViewHolder(final @NonNull View v) {
            super(v);
            thumbnailView = v.findViewById(R.id.thumbnail);
            thumbnailFrame = v.findViewById(R.id.thumbnail_frame);
            selectionIndicator = v.findViewById(R.id.selection_indicator);
            typeIndicator = v.findViewById(R.id.type_indicator);

            thumbnailView.setOnClickListener(v12 -> onItemClicked(galleryItem, thumbnailFrame));
            thumbnailView.setOnLongClickListener(v1 -> {
                onItemLongClicked(galleryItem, thumbnailFrame);
                return true;
            });
        }

        void bindTo(final @NonNull GalleryItem galleryItem) {
            this.galleryItem = galleryItem;
            if (galleryItem.type == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) {
                typeIndicator.setImageResource(R.drawable.ic_video);
                typeIndicator.setVisibility(View.VISIBLE);
            } else {
                typeIndicator.setVisibility(View.GONE);
            }
            if (selectedItems.isEmpty()) {
                selectionIndicator.setVisibility(View.GONE);
                thumbnailFrame.setPadding(0, 0, 0, 0);
            } else {
                selectionIndicator.setVisibility(View.VISIBLE);
                if (selectedItems.contains(galleryItem.id)) {
                    selectionIndicator.setImageResource(R.drawable.ic_item_selected);
                    int mediaGallerySelectionPadding = getResources().getDimensionPixelSize(R.dimen.media_gallery_selection_padding);
                    thumbnailFrame.setPadding(mediaGallerySelectionPadding, mediaGallerySelectionPadding, mediaGallerySelectionPadding, mediaGallerySelectionPadding);
                } else {
                    selectionIndicator.setImageResource(R.drawable.ic_item_unselected);
                    thumbnailFrame.setPadding(0, 0, 0, 0);
                }
            }

            thumbnailLoader.load(thumbnailView, galleryItem);
        }
    }
}
