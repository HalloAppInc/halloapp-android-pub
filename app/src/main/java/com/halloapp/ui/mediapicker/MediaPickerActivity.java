package com.halloapp.ui.mediapicker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ContentUris;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ActionMode;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.PagedList;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.Constants;
import com.halloapp.R;
import com.halloapp.media.MediaUtils;
import com.halloapp.ui.ContentComposerActivity;
import com.halloapp.ui.CropImageActivity;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.avatar.AvatarPreviewActivity;
import com.halloapp.util.Log;
import com.halloapp.util.Preconditions;
import com.halloapp.widget.ActionBarShadowOnScrollListener;
import com.halloapp.widget.BlueToast;
import com.halloapp.widget.CenterToast;
import com.halloapp.widget.GridSpacingItemDecoration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class MediaPickerActivity extends HalloActivity implements EasyPermissions.PermissionCallbacks {

    public static final String EXTRA_PICKER_PURPOSE = "picker_purpose";
    public static final String EXTRA_CHAT_ID = "chat_id";
    public static final String EXTRA_REPLY_POST_ID = "reply_id";
    public static final String EXTRA_REPLY_POST_MEDIA_INDEX = "reply_post_media_index";

    public static final int PICKER_PURPOSE_SEND = 1;
    public static final int PICKER_PURPOSE_AVATAR = 2;
    public static final int PICKER_PURPOSE_RESULT = 3;

    private static final int REQUEST_CODE_ASK_STORAGE_PERMISSION = 1;
    private static final int REQUEST_CODE_COMPOSE_CONTENT = 2;
    private static final int REQUEST_CODE_PICK_MEDIA = 3;
    private static final int REQUEST_CODE_SET_AVATAR = 4;
    private static final int REQUEST_CODE_TAKE_PHOTO = 5;

    private static final int RESULT_SELECT_MORE = RESULT_FIRST_USER + 1;

    private MediaPickerViewModel viewModel;
    private MediaItemsAdapter adapter;
    private GalleryThumbnailLoader thumbnailLoader;
    private MediaPickerPreview preview;

    private ActionMode actionMode;
    private int pickerPurpose = PICKER_PURPOSE_SEND;

    private static final String KEY_SELECTED_MEDIA = "selected_media";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_picker);

        Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        pickerPurpose = getIntent().getIntExtra(EXTRA_PICKER_PURPOSE, PICKER_PURPOSE_SEND);

        if (pickerPurpose == PICKER_PURPOSE_AVATAR) {
            setTitle(R.string.avatar_picker_title);
        }

        final RecyclerView mediaView = findViewById(android.R.id.list);
        final View progressView = findViewById(R.id.progress);
        final View emptyView = findViewById(android.R.id.empty);

        mediaView.addOnScrollListener(new ActionBarShadowOnScrollListener(this));

        final GridLayoutManager layoutManager = new GridLayoutManager(this, 1);
        layoutManager.setSpanCount(MediaItemsAdapter.SPAN_COUNT_DAY_SMALL);
        layoutManager.setSpanSizeLookup(new GallerySpanSizeLookup(mediaView));

        mediaView.setLayoutManager(layoutManager);
        mediaView.addItemDecoration(new GridSpacingItemDecoration(getResources().getDimensionPixelSize(R.dimen.media_gallery_grid_spacing)));

        adapter = new MediaItemsAdapter();
        mediaView.setAdapter(adapter);

        MediaPickerViewModelFactory factory;
        final boolean includeVideos = pickerPurpose != PICKER_PURPOSE_AVATAR;
        if (savedInstanceState != null && savedInstanceState.getLongArray(KEY_SELECTED_MEDIA) != null) {
            factory = new MediaPickerViewModelFactory(getApplication(), includeVideos, savedInstanceState.getLongArray(KEY_SELECTED_MEDIA));
        } else if (getIntent().getParcelableArrayListExtra(CropImageActivity.EXTRA_MEDIA) != null) {
            factory = new MediaPickerViewModelFactory(getApplication(), includeVideos, getIntent().getParcelableArrayListExtra(CropImageActivity.EXTRA_MEDIA));
        } else {
            factory = new MediaPickerViewModelFactory(getApplication(), includeVideos);
        }

        viewModel = new ViewModelProvider(this, factory).get(MediaPickerViewModel.class);
        viewModel.mediaList.observe(this, mediaItems -> {
            adapter.submitList(mediaItems);
            progressView.setVisibility(View.GONE);
            emptyView.setVisibility(mediaItems.isEmpty() ? View.VISIBLE : View.GONE);
        });
        viewModel.getSelected().observe(this, selected -> {
            adapter.notifyDataSetChanged();
            updateActionMode(selected);
        });

        thumbnailLoader = new GalleryThumbnailLoader(this, getResources().getDimensionPixelSize(R.dimen.media_gallery_grid_size));
        preview = new MediaPickerPreview(this);

        setupZoom(mediaView);
        requestPermissions();
    }

    public void onDestroy() {
        super.onDestroy();
        thumbnailLoader.destroy();

        if (pickerPurpose == PICKER_PURPOSE_SEND) {
            ArrayList<Uri> original = getIntent().getParcelableArrayListExtra(CropImageActivity.EXTRA_MEDIA);
            if (original != null && original.size() > 0) {
                viewModel.clean(original);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        long[] selected = viewModel.getSelectedArray();
        if (selected != null && selected.length > 0) {
            outState.putLongArray(KEY_SELECTED_MEDIA, selected);
        }
    }

    @Override
    public void onActivityResult(final int request, final int result, final Intent data) {
        super.onActivityResult(request, result, data);
        switch (request) {
            case REQUEST_CODE_TAKE_PHOTO: {
                if (result == RESULT_OK) {
                    final ArrayList<Uri> uris = new ArrayList<>();
                    uris.add(MediaUtils.getImageCaptureUri(this));
                    handleSelection(uris);
                }
                break;
            }
            case REQUEST_CODE_PICK_MEDIA: {
                if (result == RESULT_OK) {
                    if (data == null) {
                        Log.e("MediaPackerActivity.onActivityResult.REQUEST_CODE_PICK_IMAGE: no data");
                        CenterToast.show(this, R.string.bad_image);
                    } else {
                        final ArrayList<Uri> uris = new ArrayList<>();
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
            case REQUEST_CODE_COMPOSE_CONTENT:
            case REQUEST_CODE_SET_AVATAR: {
                if (result == RESULT_OK) {
                    overridePendingTransition(0, 0);
                    setResult(RESULT_OK);
                    finish();
                } else if (result == RESULT_SELECT_MORE) {
                    viewModel.setSelected(getIntent().getParcelableArrayListExtra(CropImageActivity.EXTRA_MEDIA));
                }
                break;
            }
        }
    }

    private void requestPermissions() {
        final String[] perms = {Manifest.permission.READ_EXTERNAL_STORAGE};
        if (!EasyPermissions.hasPermissions(this, perms)) {
            EasyPermissions.requestPermissions(this, getString(R.string.storage_permission_rationale),
                    REQUEST_CODE_ASK_STORAGE_PERMISSION, perms);
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
        if (pickerPurpose == PICKER_PURPOSE_AVATAR) {
            MenuItem menuItem = menu.findItem(R.id.camera);
            menuItem.setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
            case R.id.camera: {
                final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, MediaUtils.getImageCaptureUri(this));
                startActivityForResult(intent, REQUEST_CODE_TAKE_PHOTO);
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupZoom(RecyclerView mediaView) {
        mediaView.setItemAnimator(new ZoomAnimator());

        final ScaleGestureDetector zoomDetector = new ScaleGestureDetector(this, new ZoomDetectorListener(mediaView));
        mediaView.setOnTouchListener((View view, MotionEvent motionEvent) -> {
            if (motionEvent.getPointerCount() > 1) {
                return zoomDetector.onTouchEvent(motionEvent);
            }

            return false;
        });
    }

    private void startContentComposer(@NonNull ArrayList<Uri> uris) {
        final Intent intent = new Intent(this, ContentComposerActivity.class);
        intent.putExtra(ContentComposerActivity.EXTRA_CHAT_ID, getIntent().getStringExtra(EXTRA_CHAT_ID));
        intent.putExtra(ContentComposerActivity.EXTRA_REPLY_POST_ID, getIntent().getStringExtra(EXTRA_REPLY_POST_ID));
        intent.putExtra(ContentComposerActivity.EXTRA_REPLY_POST_MEDIA_INDEX, getIntent().getIntExtra(EXTRA_REPLY_POST_MEDIA_INDEX, -1));

        prepareResults(intent, uris);
        startActivityForResult(intent, REQUEST_CODE_COMPOSE_CONTENT);
    }

    private void startAvatarPreview(@NonNull Uri uri) {
        final Intent intent = new Intent(this, AvatarPreviewActivity.class);
        intent.setData(uri);
        startActivityForResult(intent, REQUEST_CODE_SET_AVATAR);
    }

    private void finishWithSelected(@NonNull ArrayList<Uri> uris) {
        final Intent intent = new Intent();
        prepareResults(intent, uris);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void handleSelection(@NonNull ArrayList<Uri> uris) {
        if (pickerPurpose == PICKER_PURPOSE_SEND) {
            Preconditions.checkState(uris.size() > 0);
            startContentComposer(uris);
        } else if (pickerPurpose == PICKER_PURPOSE_AVATAR) {
            Preconditions.checkState(uris.size() == 1);
            startAvatarPreview(uris.get(0));
        } else if (pickerPurpose == PICKER_PURPOSE_RESULT) {
            Preconditions.checkState(uris.size() > 0);
            finishWithSelected(uris);
        }
    }

    private void onItemClicked(@NonNull GalleryItem galleryItem, View view) {
        if (pickerPurpose == PICKER_PURPOSE_AVATAR) {
            final ArrayList<Uri> uris = new ArrayList<>(1);
            uris.add(ContentUris.withAppendedId(MediaStore.Files.getContentUri(GalleryDataSource.MEDIA_VOLUME), galleryItem.id));
            handleSelection(uris);
        } else {
            handleMultiSelection(galleryItem, view);
        }
    }

    private void handleMultiSelection(@NonNull GalleryItem galleryItem, View view) {
        final float animateScale;
        if (viewModel.isSelected(galleryItem.id)) {
            animateScale = 1.1f;
        } else {
            animateScale = .9f;

            if (viewModel.selectedSize() >= Constants.MAX_POST_MEDIA_ITEMS) {
                BlueToast.show(this, getResources().getQuantityString(R.plurals.max_post_media_items, Constants.MAX_POST_MEDIA_ITEMS, Constants.MAX_POST_MEDIA_ITEMS));
                return;
            }
        }

        final ScaleAnimation animation = new ScaleAnimation(1f, animateScale, 1f, animateScale,
                Animation.RELATIVE_TO_SELF, .5f, Animation.RELATIVE_TO_SELF, .5f);
        animation.setDuration(70);
        animation.setRepeatCount(1);
        animation.setRepeatMode(Animation.REVERSE);
        view.startAnimation(animation);

        if (viewModel.isSelected(galleryItem.id)) {
            viewModel.deselect(galleryItem.id);
        } else {
            viewModel.select(galleryItem.id);
        }
    }

    private void updateActionMode(List<Long> selected) {
        if (selected.isEmpty()) {
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

                        if (pickerPurpose == PICKER_PURPOSE_RESULT) {
                            MenuItem menuItem = menu.findItem(R.id.select);
                            menuItem.setTitle(R.string.done);
                        }

                        return true;
                    }

                    @Override
                    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                        return true;
                    }

                    @Override
                    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                        if (item.getItemId() == R.id.select) {
                            handleSelection(viewModel.getSelectedUris());
                        }
                        return true;
                    }

                    @Override
                    public void onDestroyActionMode(ActionMode mode) {
                        viewModel.deselectAll();
                        adapter.notifyDataSetChanged();
                        actionMode = null;
                    }
                });

                TextView tv = (TextView) getLayoutInflater().inflate(R.layout.media_action_mode_title, null);
                actionMode.setCustomView(tv);
            }

            TextView tv = (TextView) actionMode.getCustomView();
            tv.setText(String.format(Locale.getDefault(), "%d", selected.size()));

            if (selected.size() >= Constants.MAX_POST_MEDIA_ITEMS) {
                tv.setTextColor(getResources().getColor(R.color.color_accent));
            } else {
                tv.setTextColor(Color.BLACK);
            }
        }
    }

    private void prepareResults(@NonNull Intent intent, @NonNull ArrayList<Uri> uris) {
        ArrayList<Uri> original = getIntent().getParcelableArrayListExtra(CropImageActivity.EXTRA_MEDIA);
        Bundle state = getIntent().getParcelableExtra(CropImageActivity.EXTRA_STATE);

        intent.putParcelableArrayListExtra(CropImageActivity.EXTRA_MEDIA, uris);

        if (original != null) {
            original.removeAll(uris);
            viewModel.clean(original);

            if (state != null) {
                for (Uri uri : original) {
                    state.remove(uri.toString());
                }

                intent.putExtra(CropImageActivity.EXTRA_STATE, state);
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

    public class MediaItemsAdapter extends PagedListAdapter<GalleryItem, MediaItemViewHolder> {
        public final static int LAYOUT_DAY_LARGE = 1;
        public final static int LAYOUT_DAY_SMALL = 2;
        public final static int LAYOUT_MONTH = 3;

        public final static int SPAN_COUNT_DAY_LARGE = 6;
        public final static int SPAN_COUNT_DAY_SMALL = 4;
        public final static int SPAN_COUNT_MONTH = 5;

        public final static int BLOCK_SIZE_DAY_LARGE = 5;

        public final static int TYPE_HEADER = 1;
        public final static int TYPE_ITEM = 2;

        private final SimpleDateFormat dayFormat = new SimpleDateFormat("EEE, MMM dd", Locale.getDefault());
        private final SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM", Locale.getDefault());

        private  class Pointer {
            public int type;
            public int position;

            Pointer(int type, int position) {
                this.type = type;
                this.position = position;
            }
        }

        private int gridLayout = LAYOUT_DAY_SMALL;
        private ArrayList<String> headers = new ArrayList<>();
        private ArrayList<Pointer> pointers = new ArrayList<>();

        MediaItemsAdapter() {
            super(DIFF_CALLBACK);
            setHasStableIds(true);
        }

        @Nullable
        @Override
        protected GalleryItem getItem(int position) {
            if (pointers.get(position).type == TYPE_ITEM) {
                return getCurrentList().get(pointers.get(position).position);
            } else {
                return null;
            }
        }

        public long getItemId(int position) {
            if (pointers.get(position).type == TYPE_ITEM) {
                return Preconditions.checkNotNull(getItem(position)).id;
            } else {
                // The minus is to avoid accidental collision with item ids
                return -headers.get(pointers.get(position).position).hashCode();
            }
        }

        public int getGridLayout() {
            return gridLayout;
        }

        public void setGridLayout(int layout) {
            gridLayout = layout;
            buildHeaders(getCurrentList());
        }

        @Override
        public int getItemCount() {
            return pointers.size();
        }

        @Override
        public int getItemViewType(int position) {
            return pointers.get(position).type;
        }

        private boolean notSameMonth(GalleryItem l, GalleryItem r) {
            return l.year != r.year || l.month != r.month;
        }

        private boolean notSameDay(GalleryItem l, GalleryItem r) {
            return l.year != r.year || l.month != r.month || l.day != r.day;
        }

        private boolean shouldAddHeader(int position, GalleryItem current, GalleryItem prev) {
            return position == 0 ||
                    (gridLayout == LAYOUT_MONTH && notSameMonth(current, prev)) ||
                    (gridLayout == LAYOUT_DAY_SMALL && notSameDay(current, prev)) ||
                    (gridLayout == LAYOUT_DAY_LARGE && notSameDay(current, prev));
        }

        public void buildHeaders(@Nullable PagedList<GalleryItem> pagedList) {
            headers.clear();
            pointers.clear();

            if (pagedList != null) {
                for (int i = 0; i < pagedList.getLoadedCount(); ++i) {
                    GalleryItem item = pagedList.get(i);

                    if (shouldAddHeader(i, item, i == 0 ? null : pagedList.get(i - 1))) {
                        pointers.add(new Pointer(TYPE_HEADER, headers.size()));

                        if (gridLayout == LAYOUT_DAY_LARGE || gridLayout == LAYOUT_DAY_SMALL) {
                            headers.add(dayFormat.format(new Date(item.date)));
                        } else {
                            headers.add(monthFormat.format(new Date(item.date)));
                        }
                    }

                    pointers.add(new Pointer(TYPE_ITEM, i));
                }
            }
        }

        @Override
        public void submitList(@Nullable PagedList<GalleryItem> pagedList) {
            buildHeaders(pagedList);
            super.submitList(pagedList);
        }

        @Override
        public @NonNull MediaItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == TYPE_ITEM) {
                return new MediaItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.media_gallery_item, parent, false));
            } else {
                return new MediaItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.media_header, parent, false));
            }
        }

        @Override
        public void onBindViewHolder(@NonNull MediaItemViewHolder holder, int position) {
            if (getItemViewType(position) == TYPE_HEADER) {
                holder.bindTo(headers.get(pointers.get(position).position));
            } else {
                holder.bindTo(Preconditions.checkNotNull(getItem(position)));
            }
        }
    }

    private class MediaItemViewHolder extends RecyclerView.ViewHolder {

        final TextView titleView;
        final ImageView thumbnailView;
        final View thumbnailFrame;
        final ImageView selectionIndicator;
        final TextView selectionCounter;
        final ImageView typeIndicator;

        GalleryItem galleryItem;

        @SuppressLint("ClickableViewAccessibility")
        MediaItemViewHolder(final @NonNull View v) {
            super(v);
            thumbnailView = v.findViewById(R.id.thumbnail);
            thumbnailFrame = v.findViewById(R.id.thumbnail_frame);
            selectionIndicator = v.findViewById(R.id.selection_indicator);
            selectionCounter = v.findViewById(R.id.selection_counter);
            typeIndicator = v.findViewById(R.id.type_indicator);
            titleView = v.findViewById(R.id.title);

            if (thumbnailView != null) {
                thumbnailView.setOnClickListener(v12 -> onItemClicked(galleryItem, thumbnailFrame));

                thumbnailView.setOnLongClickListener(v1 -> {
                    preview.show(galleryItem, thumbnailFrame);
                    return true;
                });

                thumbnailView.setOnTouchListener((view, motionEvent) -> {
                    if (preview.isShowing() && motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        preview.hide();
                        return true;
                    }

                    return false;
                });
            }
        }

        void bindTo(final @NonNull String text) {
            titleView.setText(text);
        }

        void bindTo(final @NonNull GalleryItem galleryItem) {
            this.galleryItem = galleryItem;
            if (galleryItem.type == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) {
                typeIndicator.setImageResource(R.drawable.ic_video);
                typeIndicator.setVisibility(View.VISIBLE);
                thumbnailView.setContentDescription(getString(R.string.video));
            } else {
                typeIndicator.setVisibility(View.GONE);
                thumbnailView.setContentDescription(getString(R.string.photo));
            }

            if (pickerPurpose == PICKER_PURPOSE_AVATAR) {
                selectionCounter.setVisibility(View.GONE);
                selectionIndicator.setVisibility(View.GONE);
                thumbnailFrame.setPadding(0, 0, 0, 0);
                thumbnailView.setSelected(false);
            } else {
                if (viewModel.isSelected(galleryItem.id)) {
                    int index = viewModel.indexOfSelected(galleryItem.id);
                    selectionCounter.setVisibility(View.VISIBLE);
                    selectionCounter.setText(String.format(Locale.getDefault(), "%d", index + 1));
                    selectionIndicator.setVisibility(View.GONE);

                    int mediaGallerySelectionPadding = getResources().getDimensionPixelSize(R.dimen.media_gallery_selection_padding);
                    thumbnailFrame.setPadding(mediaGallerySelectionPadding, mediaGallerySelectionPadding, mediaGallerySelectionPadding, mediaGallerySelectionPadding);
                    thumbnailView.setSelected(true);
                } else {
                    selectionCounter.setVisibility(View.GONE);
                    selectionIndicator.setVisibility(View.VISIBLE);
                    selectionIndicator.setImageResource(R.drawable.ic_item_unselected);
                    thumbnailFrame.setPadding(0, 0, 0, 0);
                    thumbnailView.setSelected(false);
                }
            }

            thumbnailLoader.load(thumbnailView, galleryItem);
        }
    }
}
