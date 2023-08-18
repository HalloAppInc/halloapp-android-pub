package com.halloapp.ui.mediapicker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Outline;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ActionMode;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.R;
import com.halloapp.id.ChatId;
import com.halloapp.id.GroupId;
import com.halloapp.props.ServerProps;
import com.halloapp.ui.ContentComposerActivity;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.avatar.AvatarPreviewActivity;
import com.halloapp.ui.camera.CameraActivity;
import com.halloapp.ui.mediaedit.MediaEditActivity;
import com.halloapp.util.Preconditions;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.ActionBarShadowOnScrollListener;
import com.halloapp.widget.GridSpacingItemDecoration;
import com.halloapp.widget.SnackbarHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class MediaPickerActivity extends HalloActivity implements EasyPermissions.PermissionCallbacks {

    public static Intent pickAvatar(@NonNull Context context) {
        Intent intent = new Intent(context, MediaPickerActivity.class);
        intent.putExtra(EXTRA_PICKER_PURPOSE, PICKER_PURPOSE_AVATAR);
        intent.putExtra(EXTRA_SHOW_VIDEOS, false);
        intent.putExtra(EXTRA_SHOW_CAMERA, true);
        intent.putExtra(EXTRA_ALLOW_MULTIPLE, false);
        intent.putExtra(EXTRA_TITLE_ID, R.string.avatar_picker_title);
        intent.putExtra(EXTRA_CAMERA_PURPOSE, CameraActivity.PURPOSE_USER_AVATAR);
        return intent;
    }

    public static Intent pickGroupAvatar(@NonNull Context context) {
        Intent intent = new Intent(context, MediaPickerActivity.class);
        intent.putExtra(EXTRA_PICKER_PURPOSE, PICKER_PURPOSE_GROUP_AVATAR);
        intent.putExtra(EXTRA_SHOW_VIDEOS, false);
        intent.putExtra(EXTRA_SHOW_CAMERA, true);
        intent.putExtra(EXTRA_ALLOW_MULTIPLE, false);
        intent.putExtra(EXTRA_TITLE_ID, R.string.group_avatar_picker_title);
        intent.putExtra(EXTRA_CAMERA_PURPOSE, CameraActivity.PURPOSE_GROUP_AVATAR);
        return intent;
    }

    public static Intent pickForMessage(@NonNull Context context, @NonNull ChatId chatId, @Nullable String replyPostId, int replyPostMediaIndex,
                                        String textDraft) {
        Preconditions.checkNotNull(chatId);
        final Intent intent = new Intent(context, MediaPickerActivity.class);
        intent.putExtra(EXTRA_PICKER_PURPOSE, PICKER_PURPOSE_SEND);
        intent.putExtra(EXTRA_CHAT_ID, chatId);
        intent.putExtra(EXTRA_REPLY_POST_ID, replyPostId);
        intent.putExtra(EXTRA_REPLY_POST_MEDIA_INDEX, replyPostMediaIndex);
        intent.putExtra(EXTRA_SHOW_CAMERA, true);
        intent.putExtra(EXTRA_TITLE_ID, R.string.new_message);
        intent.putExtra(EXTRA_MAX_MEDIA_ITEMS, ServerProps.getInstance().getMaxChatMediaItems());
        intent.putExtra(MediaPickerActivity.EXTRA_TEXT_DRAFT, textDraft);
        return intent;
    }

    public static Intent pickForPost(@NonNull Context context, @Nullable GroupId groupId) {
        Intent intent = new Intent(context, MediaPickerActivity.class);
        intent.putExtra(EXTRA_PICKER_PURPOSE, PICKER_PURPOSE_POST);
        intent.putExtra(EXTRA_SHOW_CAMERA, true);
        intent.putExtra(EXTRA_MAX_MEDIA_ITEMS, ServerProps.getInstance().getMaxPostMediaItems());
        if (groupId != null) {
            intent.putExtra(EXTRA_GROUP_ID, groupId);
        }
        return intent;
    }

    public static Intent pickForPost(@NonNull Context context) {
        return pickForPost(context, null);
    }

    public static Intent pickFromCamera(@NonNull Context context, boolean allowVideos) {
        Intent intent = new Intent(context, MediaPickerActivity.class);
        intent.putExtra(EXTRA_PICKER_PURPOSE, PICKER_PURPOSE_RESULT);
        intent.putExtra(EXTRA_ALLOW_MULTIPLE, false);
        intent.putExtra(EXTRA_SHOW_CAMERA, false);
        intent.putExtra(EXTRA_SHOW_VIDEOS, allowVideos);

        return intent;
    }

    public static Intent pickForComment(@NonNull Context context) {
        Intent intent = new Intent(context, MediaPickerActivity.class);
        intent.putExtra(EXTRA_PICKER_PURPOSE, PICKER_PURPOSE_COMMENT);
        intent.putExtra(EXTRA_ALLOW_MULTIPLE, false);
        intent.putExtra(EXTRA_TITLE_ID, R.string.comment);
        intent.putExtra(EXTRA_NEXT_BUTTON_TEXT_ID, R.string.done);
        return intent;
    }

    public static Intent pickMoreMedia(@NonNull Context context, boolean isMessage) {
        Intent intent = new Intent(context, MediaPickerActivity.class);
        intent.putExtra(EXTRA_PICKER_PURPOSE, PICKER_PURPOSE_RESULT);
        intent.putExtra(EXTRA_SHOW_CAMERA, true);
        intent.putExtra(EXTRA_NEXT_BUTTON_TEXT_ID, R.string.done);
        int maxItems = isMessage ? ServerProps.getInstance().getMaxChatMediaItems() : ServerProps.getInstance().getMaxPostMediaItems();
        intent.putExtra(EXTRA_MAX_MEDIA_ITEMS, maxItems);
        return intent;
    }

    private static final String EXTRA_PICKER_PURPOSE = "picker_purpose";
    private static final String EXTRA_CHAT_ID = "chat_id";
    private static final String EXTRA_GROUP_ID = "group_id";
    private static final String EXTRA_REPLY_POST_ID = "reply_id";
    private static final String EXTRA_REPLY_POST_MEDIA_INDEX = "reply_post_media_index";

    private static final String EXTRA_TITLE_ID = "title_id";
    private static final String EXTRA_NEXT_BUTTON_TEXT_ID = "next_button_text_id";
    private static final String EXTRA_ALLOW_MULTIPLE = "allow_multiple";
    private static final String EXTRA_SHOW_CAMERA = "show_camera";
    private static final String EXTRA_SHOW_VIDEOS = "show_videos";
    private static final String EXTRA_CAMERA_PURPOSE = "camera_purpose";
    private static final String EXTRA_MAX_MEDIA_ITEMS = "max_media_items";

    private static final String EXTRA_TEXT_DRAFT = "text_draft";

    private static final boolean SHOW_VIDEOS_DEFAULT = true;
    private static final boolean SHOW_CAMERA_DEFAULT = false;
    private static final boolean ALLOW_MULTIPLE_DEFAULT = true;
    private static final int TITLE_ID_DEFAULT = R.string.new_post;
    private static final int CAMERA_PURPOSE_DEFAULT = CameraActivity.PURPOSE_COMPOSE;

    private static final int PICKER_PURPOSE_SEND = 1;
    private static final int PICKER_PURPOSE_AVATAR = 2;
    private static final int PICKER_PURPOSE_RESULT = 3;
    private static final int PICKER_PURPOSE_POST = 4;
    private static final int PICKER_PURPOSE_GROUP_AVATAR = 5;
    private static final int PICKER_PURPOSE_COMMENT = 6;


    private static final int REQUEST_CODE_ASK_STORAGE_PERMISSION = 1;
    private static final int REQUEST_CODE_COMPOSE_CONTENT = 2;
    private static final int REQUEST_CODE_PICK_MEDIA = 3;
    private static final int REQUEST_CODE_SET_AVATAR = 4;
    private static final int REQUEST_CODE_TAKE_PHOTO = 5;

    public static final int RESULT_SELECT_MORE = RESULT_FIRST_USER + 1;

    private MediaPickerViewModel viewModel;
    private MediaItemsAdapter adapter;
    private GalleryThumbnailLoader thumbnailLoader;
    private MediaPickerPreview preview;
    private ImageButton nextButton;
    final private List<Long> selected = new ArrayList<>();

    private ActionMode actionMode;
    private static final String KEY_SELECTED_MEDIA = "selected_media";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_picker);

        Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        setTitle(getIntent().getIntExtra(EXTRA_TITLE_ID, TITLE_ID_DEFAULT));

        final View actionsView = findViewById(R.id.actions);
        final View progressView = findViewById(R.id.progress);
        final View emptyView = findViewById(android.R.id.empty);
        nextButton = actionsView.findViewById(R.id.next);
        nextButton.setEnabled(false);

        setResult(RESULT_CANCELED);

        viewModel = buildViewModel(savedInstanceState);
        viewModel.original = getIntent().getParcelableArrayListExtra(MediaEditActivity.EXTRA_MEDIA);
        viewModel.state = getIntent().getBundleExtra(MediaEditActivity.EXTRA_STATE);

        adapter = new MediaItemsAdapter();

        final GridLayoutManager layoutManager = new GridLayoutManager(this, 1);
        layoutManager.setSpanCount(MediaPickerViewModel.SPAN_COUNT_DAY_SMALL);
        layoutManager.setSpanSizeLookup(new GallerySpanSizeLookup(viewModel));

        final RecyclerView mediaView = findViewById(android.R.id.list);
        mediaView.setLayoutManager(layoutManager);
        mediaView.addItemDecoration(new GridSpacingItemDecoration(viewModel, getResources().getDimensionPixelSize(R.dimen.media_gallery_grid_spacing)));
        mediaView.setAdapter(adapter);
        mediaView.addOnScrollListener(new ActionBarShadowOnScrollListener(this));

        thumbnailLoader = new GalleryThumbnailLoader(this, getResources().getDimensionPixelSize(R.dimen.media_gallery_grid_size));
        preview = new MediaPickerPreview(this);

        setupZoom(mediaView);

        viewModel.getMediaList().observe(this, mediaItems -> {
            adapter.setPagedList(mediaItems);
            progressView.setVisibility(View.GONE);
            emptyView.setVisibility(mediaItems.isEmpty() ? View.VISIBLE : View.GONE);
        });
        viewModel.getSelected().observe(this, selected -> {
            notifyAdapterOnSelection(selected);
            updateActionMode(selected);
            updateActions(!selected.isEmpty());
        });
        viewModel.getLayout().observe(this, layout -> {
            switch (layout) {
                case MediaPickerViewModel.LAYOUT_DAY_SMALL: {
                    layoutManager.setSpanCount(MediaPickerViewModel.SPAN_COUNT_DAY_SMALL);
                    break;
                }
                case MediaPickerViewModel.LAYOUT_DAY_LARGE: {
                    layoutManager.setSpanCount(MediaPickerViewModel.SPAN_COUNT_DAY_LARGE);
                    break;
                }
                case MediaPickerViewModel.LAYOUT_MONTH: {
                    layoutManager.setSpanCount(MediaPickerViewModel.SPAN_COUNT_MONTH);
                    break;
                }
            }

            layoutManager.removeAllViews();
            layoutManager.requestLayout();
            adapter.notifyDataSetChanged();
        });

        requestPermissions();

        nextButton.setOnClickListener((v) -> handleSelection(viewModel.getSelectedUris()));
    }

    private void notifyAdapterOnSelection(List<Long> selected) {
        HashSet<Long> set = new HashSet<>(this.selected);

        if (selected != null) {
            set.addAll(selected);
        }

        for (int i = 0; i < adapter.getItemCount(); ++i) {
            if (set.contains(adapter.getItemId(i))) {
                adapter.notifyItemChanged(i);
            }
        }

        this.selected.clear();

        if (selected != null) {
            this.selected.addAll(selected);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        thumbnailLoader.destroy();

        int pickerPurpose = getIntent().getIntExtra(EXTRA_PICKER_PURPOSE, PICKER_PURPOSE_SEND);
        if (pickerPurpose == PICKER_PURPOSE_SEND || pickerPurpose == PICKER_PURPOSE_POST) {
            if (viewModel.original != null && viewModel.original.size() > 0) {
                viewModel.clean(viewModel.original);
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
    protected void onActivityResult(final int request, final int result, @Nullable final Intent data) {
        super.onActivityResult(request, result, data);
        switch (request) {
            case REQUEST_CODE_PICK_MEDIA: {
                if (result == RESULT_OK) {
                    if (data == null) {
                        Log.e("MediaPackerActivity.onActivityResult.REQUEST_CODE_PICK_IMAGE: no data");
                        SnackbarHelper.showWarning(this, R.string.bad_image);
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
                            SnackbarHelper.showWarning(this, R.string.bad_image);
                        }
                    }
                }
                break;
            }
            case REQUEST_CODE_COMPOSE_CONTENT:
            case REQUEST_CODE_SET_AVATAR: {
                if (result == RESULT_OK) {
                    overridePendingTransition(0, 0);
                    setResult(RESULT_OK, data);
                    finish();
                } else if (result == RESULT_SELECT_MORE) {
                    viewModel.original = data.getParcelableArrayListExtra(MediaEditActivity.EXTRA_MEDIA);
                    viewModel.state = data.getBundleExtra(MediaEditActivity.EXTRA_STATE);
                    viewModel.setSelected(data.getParcelableArrayListExtra(MediaEditActivity.EXTRA_MEDIA));
                }
                break;
            }
            case REQUEST_CODE_TAKE_PHOTO: {
                if (result == RESULT_OK) {
                    setResult(RESULT_OK, data);
                    finish();
                }
            }
        }
    }

    private void requestPermissions() {
        final String[] perms = Build.VERSION.SDK_INT >= 31
                ? new String[] {Manifest.permission.READ_MEDIA_AUDIO, Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO}
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
        if (getIntent().getBooleanExtra(EXTRA_SHOW_CAMERA, SHOW_CAMERA_DEFAULT)) {
            MenuItem menuItem = menu.findItem(R.id.camera);
            menuItem.setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.other_media) {
            final Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, getIntent().getBooleanExtra(EXTRA_ALLOW_MULTIPLE, true));
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            final String[] mimeTypes = (getIntent().getBooleanExtra(EXTRA_SHOW_VIDEOS, true)) ? new String[]{"image/*", "video/*"} : new String[]{"image/*"};
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
            startActivityForResult(intent, REQUEST_CODE_PICK_MEDIA);
            return true;
        } else if (item.getItemId() == R.id.camera) {
            final Intent intent = new Intent(this, CameraActivity.class);
            ChatId chatId = getIntent().getParcelableExtra(EXTRA_CHAT_ID);
            GroupId groupId = getIntent().getParcelableExtra(EXTRA_GROUP_ID);
            intent.putExtra(CameraActivity.EXTRA_CHAT_ID, chatId);
            intent.putExtra(CameraActivity.EXTRA_GROUP_ID, groupId);
            intent.putExtra(CameraActivity.EXTRA_REPLY_POST_ID, getIntent().getStringExtra(EXTRA_REPLY_POST_ID));
            intent.putExtra(CameraActivity.EXTRA_REPLY_POST_MEDIA_INDEX, getIntent().getIntExtra(EXTRA_REPLY_POST_MEDIA_INDEX, -1));
            intent.putExtra(CameraActivity.EXTRA_PURPOSE, getIntent().getIntExtra(EXTRA_CAMERA_PURPOSE, CAMERA_PURPOSE_DEFAULT));
            startActivityForResult(intent, REQUEST_CODE_TAKE_PHOTO);
        }
        return super.onOptionsItemSelected(item);
    }

    private MediaPickerViewModel buildViewModel(Bundle savedInstanceState) {
        MediaPickerViewModelFactory factory;
        final boolean includeVideos = getIntent().getBooleanExtra(EXTRA_SHOW_VIDEOS, SHOW_VIDEOS_DEFAULT);
        if (savedInstanceState != null && savedInstanceState.getLongArray(KEY_SELECTED_MEDIA) != null) {
            factory = new MediaPickerViewModelFactory(getApplication(), includeVideos, savedInstanceState.getLongArray(KEY_SELECTED_MEDIA));
        } else if (getIntent().getParcelableArrayListExtra(MediaEditActivity.EXTRA_MEDIA) != null) {
            factory = new MediaPickerViewModelFactory(getApplication(), includeVideos, getIntent().getParcelableArrayListExtra(MediaEditActivity.EXTRA_MEDIA));
        } else {
            factory = new MediaPickerViewModelFactory(getApplication(), includeVideos);
        }

        return new ViewModelProvider(this, factory).get(MediaPickerViewModel.class);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupZoom(RecyclerView mediaView) {
        ZoomAnimator animator = new ZoomAnimator();
        mediaView.setItemAnimator(animator);

        final ScaleGestureDetector zoomDetector = new ScaleGestureDetector(this, new ZoomDetectorListener(animator, viewModel));
        mediaView.setOnTouchListener((View view, MotionEvent motionEvent) -> {
            if (motionEvent.getPointerCount() > 1) {
                return zoomDetector.onTouchEvent(motionEvent);
            }

            return false;
        });
    }

    private void startContentComposer(@NonNull ArrayList<Uri> uris) {
        final Intent intent = new Intent(this, ContentComposerActivity.class);
        ChatId chatId = getIntent().getParcelableExtra(EXTRA_CHAT_ID);
        GroupId groupId = getIntent().getParcelableExtra(EXTRA_GROUP_ID);
        String textDraft = getIntent().getStringExtra(EXTRA_TEXT_DRAFT);
        intent.putExtra(ContentComposerActivity.EXTRA_CHAT_ID, chatId);
        intent.putExtra(ContentComposerActivity.EXTRA_GROUP_ID, groupId);
        intent.putExtra(ContentComposerActivity.EXTRA_ALLOW_ADD_MEDIA, true);
        intent.putExtra(ContentComposerActivity.EXTRA_REPLY_POST_ID, getIntent().getStringExtra(EXTRA_REPLY_POST_ID));
        intent.putExtra(ContentComposerActivity.EXTRA_REPLY_POST_MEDIA_INDEX, getIntent().getIntExtra(EXTRA_REPLY_POST_MEDIA_INDEX, -1));
        intent.putExtra(ContentComposerActivity.EXTRA_NAVIGATE_TO_DESTINATION, false);
        intent.putExtra(Intent.EXTRA_TEXT, textDraft);
        prepareResults(intent, uris);
        startActivityForResult(intent, REQUEST_CODE_COMPOSE_CONTENT);
    }

    private void startAvatarPreview(@NonNull Uri uri) {
        int pickerPurpose = getIntent().getIntExtra(EXTRA_PICKER_PURPOSE, PICKER_PURPOSE_SEND);
        Intent intent = AvatarPreviewActivity.open(this, uri, pickerPurpose == PICKER_PURPOSE_GROUP_AVATAR);
        startActivityForResult(intent, REQUEST_CODE_SET_AVATAR);
    }

    private void finishWithSelected(@NonNull ArrayList<Uri> uris) {
        final Intent intent = new Intent();
        prepareResults(intent, uris);
        setResult(RESULT_OK, intent);
        finish();
    }

    private int getMaxMediaItems() {
        if (getIntent().getBooleanExtra(EXTRA_ALLOW_MULTIPLE, ALLOW_MULTIPLE_DEFAULT)) {
            return getIntent().getIntExtra(EXTRA_MAX_MEDIA_ITEMS, ServerProps.getInstance().getMaxPostMediaItems());
        }

        return 1;
    }

    private void handleSelection(@NonNull ArrayList<Uri> uris) {
        int pickerPurpose = getIntent().getIntExtra(EXTRA_PICKER_PURPOSE, PICKER_PURPOSE_SEND);
        if (pickerPurpose == PICKER_PURPOSE_SEND || pickerPurpose == PICKER_PURPOSE_POST) {
            Preconditions.checkState(uris.size() > 0);
            startContentComposer(uris);
        } else if (pickerPurpose == PICKER_PURPOSE_AVATAR || pickerPurpose == PICKER_PURPOSE_GROUP_AVATAR) {
            Preconditions.checkState(uris.size() == 1);
            startAvatarPreview(uris.get(0));
        } else if (pickerPurpose == PICKER_PURPOSE_RESULT || pickerPurpose == PICKER_PURPOSE_COMMENT) {
            Preconditions.checkState(uris.size() > 0);
            finishWithSelected(uris);
        }
    }

    private void updateActions(boolean hasSelected) {
        if (!hasSelected || !getIntent().getBooleanExtra(EXTRA_ALLOW_MULTIPLE, ALLOW_MULTIPLE_DEFAULT)) {
            nextButton.setEnabled(false);
        } else {
            nextButton.setEnabled(true);
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
                        final int size = viewModel.selectedSize();
                        viewModel.deselectAll();
                        actionMode = null;

                        if (size > 0) {
                            setResult(RESULT_CANCELED);
                            finish();
                        }
                    }
                });

                TextView tv = (TextView) getLayoutInflater().inflate(R.layout.media_action_mode_title, null);
                actionMode.setCustomView(tv);
            }

            TextView tv = (TextView) actionMode.getCustomView();
            tv.setText(String.format(Locale.getDefault(), "%d", selected.size()));

            if (selected.size() >= getMaxMediaItems()) {
                tv.setTextColor(getResources().getColor(R.color.color_accent));
            } else {
                tv.setTextColor(getResources().getColor(R.color.primary_text));
            }
        }
    }

    private void prepareResults(@NonNull Intent intent, @NonNull ArrayList<Uri> uris) {
        ArrayList<Uri> original = viewModel.original != null ? (ArrayList<Uri>) viewModel.original.clone() : null;
        Bundle state = viewModel.state != null ? (Bundle)viewModel.state.clone() : null;

        Log.i("MediaPickerActivity preparing results for " + uris.size() + " uris");
        intent.putParcelableArrayListExtra(MediaEditActivity.EXTRA_MEDIA, uris);

        if (original != null) {
            for (int index = 0; index < uris.size(); index++) {
                if (!original.contains(uris.get(index))) {
                    intent.putExtra(MediaEditActivity.EXTRA_SELECTED, index);
                    break;
                }
            }
            original.removeAll(uris);
            viewModel.clean(original);

            if (state != null) {
                for (Uri uri : original) {
                    state.remove(uri.toString());
                }

                intent.putExtra(MediaEditActivity.EXTRA_STATE, state);
            }
        }
    }

    public class MediaItemsAdapter extends RecyclerView.Adapter<MediaItemViewHolder> {
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
        public @NonNull MediaItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new MediaItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.media_gallery_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull MediaItemViewHolder holder, int position) {
            holder.bindTo(Preconditions.checkNotNull(items.get(position)));
            items.loadAround(position);
        }

        @Override
        public long getItemId(int position) {
            return Preconditions.checkNotNull(items.get(position)).id;
        }
    }

    public class MediaItemViewHolder extends RecyclerView.ViewHolder {
        final int mediaGallerySelectionRadius = getResources().getDimensionPixelSize(R.dimen.media_gallery_selection_radius);
        private final ViewOutlineProvider vop = new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), mediaGallerySelectionRadius);
            }
        };
        boolean animateSelection = false;
        boolean animateDeselection = false;

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
                thumbnailView.setOnClickListener(v12 -> onItemClicked());

                thumbnailView.setOnLongClickListener(v1 -> {
                    preview.show(galleryItem, thumbnailFrame);
                    return true;
                });

                thumbnailView.setOnTouchListener((view, motionEvent) -> {
                    if (preview.isShowing()) {
                        thumbnailView.getParent().requestDisallowInterceptTouchEvent(true);

                        if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                            preview.hide();
                        }

                        return true;
                    }

                    thumbnailView.getParent().requestDisallowInterceptTouchEvent(false);
                    return false;
                });
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

            if (!getIntent().getBooleanExtra(EXTRA_ALLOW_MULTIPLE, ALLOW_MULTIPLE_DEFAULT)) {
                selectionIndicator.setVisibility(View.GONE);
                thumbnailFrame.setPadding(0, 0, 0, 0);
                thumbnailView.setSelected(false);
            } else {
                if (viewModel.isSelected(galleryItem.id)) {
                    setupSelected(viewModel.indexOfSelected(galleryItem.id));
                } else {
                    setupDefault();
                }
            }

            thumbnailLoader.load(thumbnailView, galleryItem);
        }

        private void setupSelected(int index) {
            selectionCounter.setVisibility(View.VISIBLE);
            selectionCounter.setText(String.format(Locale.getDefault(), "%d", index + 1));
            selectionIndicator.setVisibility(View.GONE);

            thumbnailView.setSelected(true);
            thumbnailView.setOutlineProvider(vop);
            thumbnailView.setClipToOutline(true);
        }

        private void setupDefault() {
            selectionIndicator.setImageResource(R.drawable.ic_item_unselected);
            selectionCounter.setVisibility(View.GONE);
            selectionIndicator.setVisibility(View.VISIBLE);

            thumbnailView.setSelected(false);
            thumbnailView.setOutlineProvider(null);
            thumbnailView.setClipToOutline(false);
        }

        private void notifyTooManyItems() {
            int maxItems = getMaxMediaItems();
            final String message = getResources().getQuantityString(R.plurals.max_post_media_items, maxItems, maxItems);
            Log.i("MediaPickerActivity tried to select too many items");
            SnackbarHelper.showInfo(itemView, message);
        }

        private void onItemClicked() {
            if (!getIntent().getBooleanExtra(EXTRA_ALLOW_MULTIPLE, ALLOW_MULTIPLE_DEFAULT)) {
                final ArrayList<Uri> uris = new ArrayList<>(1);
                uris.add(ContentUris.withAppendedId(MediaStore.Files.getContentUri(GalleryDataSource.MEDIA_VOLUME), galleryItem.id));
                handleSelection(uris);
            } else {
                final boolean isSelected = viewModel.isSelected(galleryItem.id);

                if (!isSelected && viewModel.selectedSize() >= getMaxMediaItems()) {
                    notifyTooManyItems();
                    return;
                }

                if (isSelected) {
                    animateDeselection = true;
                    viewModel.deselect(galleryItem.id);
                } else {
                    animateSelection = true;
                    viewModel.select(galleryItem.id);
                }
            }
        }
    }
}
