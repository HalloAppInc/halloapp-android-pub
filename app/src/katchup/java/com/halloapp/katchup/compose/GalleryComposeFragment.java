package com.halloapp.katchup.compose;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Outline;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.ControlDispatcher;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.halloapp.Constants;
import com.halloapp.FileStore;
import com.halloapp.R;
import com.halloapp.content.Media;
import com.halloapp.katchup.SelfiePostComposerActivity;
import com.halloapp.media.ExoUtils;
import com.halloapp.media.MediaThumbnailLoader;
import com.halloapp.media.MediaUtils;
import com.halloapp.ui.mediapicker.GalleryDataSource;
import com.halloapp.ui.mediapicker.GalleryItem;
import com.halloapp.ui.mediapicker.GalleryThumbnailLoader;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.FileUtils;
import com.halloapp.util.IntentUtils;
import com.halloapp.util.Preconditions;
import com.halloapp.util.RandomId;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.ContentPlayerView;
import com.halloapp.widget.CropPhotoView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GalleryComposeFragment extends ComposeFragment {

    public static GalleryComposeFragment newInstance(String prompt) {
        return newInstance(prompt, false);
    }

    public static GalleryComposeFragment newInstance(String prompt, boolean allowMultipleSelection) {
        Bundle args = new Bundle();
        args.putString(EXTRA_PROMPT, prompt);
        args.putBoolean(EXTRA_ALLOW_MULTIPLE, allowMultipleSelection);

        GalleryComposeFragment fragment = new GalleryComposeFragment();
        fragment.setArguments(args);

        return fragment;
    }

    private static final String EXTRA_PROMPT = "prompt";
    private static final String EXTRA_ALLOW_MULTIPLE = "allow_multiple";

    private static final int ITEMS_PER_ROW = 3;

    private View mediaPreviewContainer;
    private View gallerySelectionContainer;
    private TextView previewPromptTextView;

    private ImageView mediaPreviewView;
    private RecyclerView mediaView;

    private SelfiePostComposerActivity host;

    private SelfieComposerViewModel viewModel;
    private final MediaItemsAdapter adapter = new MediaItemsAdapter();
    private final SelectionAdapter selectionAdapter = new SelectionAdapter();
    private GalleryThumbnailLoader thumbnailLoader;
    private MediaThumbnailLoader mediaLoader;

    private File captureFile;
    private @Media.MediaType int captureType;
    private GalleryPopupWindow galleryPopupWindow;
    private FullMediaPopupWindow fullMediaPopupWindow;
    private File createdImage;

    private ContentPlayerView videoPlayerView;

    private SimpleExoPlayer videoPlayer;

    private String prompt;
    private boolean isMultipleSelectionAllowed = false;
    private int headerHeight;
    private TextView nextBtnView;

    int verticalOffset = 0;
    RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            verticalOffset += dy;
            showToolbarBasedOnScroll();
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.katchup_fragment_gallery_compose, container, false);

        gallerySelectionContainer = root.findViewById(R.id.gallery_container);

        mediaPreviewContainer = root.findViewById(R.id.preview_container);
        mediaPreviewView = root.findViewById(R.id.media_preview);
        videoPlayerView = root.findViewById(R.id.video_player);
        previewPromptTextView = root.findViewById(R.id.preview_prompt_text);
        nextBtnView = root.findViewById(R.id.next);

        final Point point = new Point();
        requireActivity().getWindowManager().getDefaultDisplay().getSize(point);
        mediaLoader = new MediaThumbnailLoader(requireContext(), Math.min(Constants.MAX_IMAGE_DIMENSION, Math.max(point.x, point.y)));

        final float cameraViewRadius = getResources().getDimension(R.dimen.camera_preview_border_radius);
        ViewOutlineProvider roundedOutlineProvider = new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), cameraViewRadius);
            }
        };

        mediaPreviewView.setClipToOutline(true);
        mediaPreviewView.setOutlineProvider(roundedOutlineProvider);

        thumbnailLoader = new GalleryThumbnailLoader(requireContext(), getResources().getDimensionPixelSize(R.dimen.media_gallery_grid_size));

        viewModel = new ViewModelProvider(requireActivity()).get(SelfieComposerViewModel.class);
        viewModel.getComposerState().observe(getViewLifecycleOwner(), state -> {
            switch (state) {
                case SelfieComposerViewModel.ComposeState.COMPOSING_CONTENT:
                    showSelectionView();
                    break;
                case SelfieComposerViewModel.ComposeState.CROPPING:
                    showCropView();
                    break;
                case SelfieComposerViewModel.ComposeState.COMPOSING_SELFIE:
                    showToolbarPrompt();
                    showPreviewView();
                    break;
                case SelfieComposerViewModel.ComposeState.TRANSITIONING:
                case SelfieComposerViewModel.ComposeState.READY_TO_SEND:
                    hideToolbarPrompt();
                    showPreviewView();
                    break;
            }
        });

        Bundle args = getArguments();
        if (args != null) {
            prompt = args.getString(EXTRA_PROMPT, null);
            isMultipleSelectionAllowed = args.getBoolean(EXTRA_ALLOW_MULTIPLE, false);
        }
        previewPromptTextView.setText(prompt);

        final GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), ITEMS_PER_ROW);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (position == 0) {
                    return ITEMS_PER_ROW;
                }
                return 1;
            }
        });
        mediaView = root.findViewById(android.R.id.list);
        mediaView.setLayoutManager(layoutManager);
        mediaView.addItemDecoration(new RecyclerView.ItemDecoration() {
            final int spacing = getResources().getDimensionPixelSize(R.dimen.media_gallery_grid_spacing);
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                int position = parent.getChildAdapterPosition(view) - 1;
                int column = position % ITEMS_PER_ROW;
                int columnsCount = ITEMS_PER_ROW;

                outRect.bottom = spacing;
                outRect.left = column * spacing / columnsCount;
                outRect.right = spacing - (column + 1) * spacing / columnsCount;
            }
        });
        mediaView.setAdapter(adapter);
        mediaView.setOnScrollListener(scrollListener);

        LinearLayoutManager selectionLayoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        RecyclerView selectionView = root.findViewById(R.id.selection);
        selectionView.setLayoutManager(selectionLayoutManager);
        selectionView.setAdapter(selectionAdapter);
        selectionView.setVisibility(isMultipleSelectionAllowed ? View.VISIBLE : View.GONE);

        viewModel.getMediaList().observe(getViewLifecycleOwner(), mediaItems -> {
            adapter.setPagedList(mediaItems);
//            progressView.setVisibility(View.GONE);
//            emptyView.setVisibility(mediaItems.isEmpty() ? View.VISIBLE : View.GONE);
        });

        viewModel.getGallerySelection().observe(getViewLifecycleOwner(), items -> {
            adapter.notifyDataSetChanged();
            selectionAdapter.setSelectionList(items);
            enableNextButton(viewModel.isGallerySelectionReady());
        });

        nextBtnView.setVisibility(isMultipleSelectionAllowed ? View.VISIBLE : View.GONE);
        nextBtnView.setOnClickListener(v -> onNext());
        enableNextButton(false);

        return root;
    }

    private void openPicker() {
        final Intent intent = IntentUtils.createPhotoPickerIntent(isMultipleSelectionAllowed);
        requireActivity().startActivityForResult(intent, SelfiePostComposerActivity.REQUEST_CODE_CHOOSE_PHOTO);
    }

    private void showToolbarBasedOnScroll() {
        if (verticalOffset < headerHeight) {
            hideToolbarPrompt();
        } else {
            showToolbarPrompt();
        }
    }

    private void hideToolbarPrompt() {
        ((SelfiePostComposerActivity) requireActivity()).hideToolbarPrompt();
    }

    private void showToolbarPrompt() {
        ((SelfiePostComposerActivity) requireActivity()).showToolbarPrompt();
    }

    private void enableNextButton(boolean enabled) {
        nextBtnView.setEnabled(enabled);
        nextBtnView.setAlpha(enabled ? 1f : 0.7f);
    }

    private ProgressDialog progressDialog;
    private void onNext() {
        if (!viewModel.isGallerySelectionReady()) {
            return;
        }

        List<GalleryItem> selection = viewModel.getGallerySelection().getValue();

        ArrayList<Uri> content = new ArrayList<>(selection.size());
        for (GalleryItem item : selection) {
            content.add(getUri(item));
        }

        enableNextButton(false);
        progressDialog = ProgressDialog.show(requireContext(), null, getString(R.string.loading_spinner));
        progressDialog.show();

        viewModel.onComposedDump(content);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (videoPlayer != null) {
            videoPlayer.stop(true);
            videoPlayer = null;
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof SelfiePostComposerActivity) {
            host = (SelfiePostComposerActivity) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        host = null;
    }

    private void handleSelection(@NonNull GalleryItem galleryItem) {
        if (isMultipleSelectionAllowed) {
            if (viewModel.isSelectedInGallery(galleryItem) || viewModel.canSelectMoreFromGallery()) {
                viewModel.toggleGallerySelection(galleryItem);
            }
        } else {
            viewModel.onSelectedMedia(getUri(galleryItem));
        }
    }

    private Uri getUri(@NonNull GalleryItem item) {
        return ContentUris.withAppendedId(MediaStore.Files.getContentUri(GalleryDataSource.MEDIA_VOLUME), item.id);
    }

    private void scrollToITem(@NonNull GalleryItem galleryItem) {
        mediaView.smoothScrollToPosition(adapter.items.indexOf(galleryItem));
    }

    private void showSelectionView() {
        previewPromptTextView.setVisibility(View.GONE);
        showToolbarBasedOnScroll();
        gallerySelectionContainer.setVisibility(View.VISIBLE);
        mediaPreviewContainer.setVisibility(View.GONE);
        videoPlayerView.setPlayer(null);
        if (videoPlayer != null) {
            videoPlayer.stop(true);
            videoPlayer = null;
        }
        if (galleryPopupWindow != null) {
            galleryPopupWindow.dismiss();
            galleryPopupWindow = null;
        }
    }

    private void showCropView() {
        previewPromptTextView.setVisibility(View.GONE);
        showToolbarPrompt();
        gallerySelectionContainer.setVisibility(View.VISIBLE);
        mediaPreviewContainer.setVisibility(View.GONE);
        videoPlayerView.setPlayer(null);
        if (videoPlayer != null) {
            videoPlayer.stop(true);
            videoPlayer = null;
        }
        if (galleryPopupWindow != null) {
            galleryPopupWindow.dismiss();
            galleryPopupWindow = null;
        }
        galleryPopupWindow = new GalleryPopupWindow(requireContext(), viewModel.getSelectedImage().getValue());
        galleryPopupWindow.show(mediaPreviewContainer);
    }

    private void showFullMedia(Uri uri) {
        if (fullMediaPopupWindow != null) {
            fullMediaPopupWindow.dismiss();
            fullMediaPopupWindow = null;
        }

        fullMediaPopupWindow = new FullMediaPopupWindow(requireContext(), uri);
        fullMediaPopupWindow.show(mediaPreviewContainer);
    }

    private void showPreviewView() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
            enableNextButton(true);
        }

        previewPromptTextView.setVisibility(View.VISIBLE);
        gallerySelectionContainer.setVisibility(View.GONE);
        mediaPreviewContainer.setVisibility(View.VISIBLE);

        if (isMultipleSelectionAllowed) {
            captureFile = viewModel.getImageDump();
            captureType = Media.MEDIA_TYPE_VIDEO;
            previewPromptTextView.setVisibility(View.GONE);
        }

        if (captureFile != null) {
            host.getMediaThumbnailLoader().load(mediaPreviewView, Media.createFromFile(captureType, captureFile));
            if (captureType == Media.MEDIA_TYPE_VIDEO) {
                videoPlayerView.setVisibility(View.VISIBLE);
                bindVideo();
            } else {
                videoPlayerView.setPlayer(null);
                if (videoPlayer != null) {
                    videoPlayer.stop(true);
                }
                videoPlayerView.setVisibility(View.GONE);
            }
        } else {
            host.getMediaThumbnailLoader().cancel(mediaPreviewView);
            mediaPreviewView.setImageBitmap(null);
        }
        if (galleryPopupWindow != null) {
            galleryPopupWindow.dismiss();
            galleryPopupWindow = null;
        }
        if (captureType == Media.MEDIA_TYPE_IMAGE) {
            savePreview();
        }
    }

    private void savePreview() {
        mediaPreviewContainer.post(() -> {
            mediaPreviewContainer.setDrawingCacheEnabled(true);
            final Bitmap b = Bitmap.createBitmap(mediaPreviewContainer.getDrawingCache());
            mediaPreviewContainer.setDrawingCacheEnabled(false);
            if (createdImage == null) {
                createdImage = FileStore.getInstance().getTmpFile(RandomId.create() + "." + Media.getFileExt(Media.MEDIA_TYPE_IMAGE));
            }
            BgWorkers.getInstance().execute(() -> {
                if (createdImage == null) {
                    return;
                }
                if (!createdImage.delete()) {
                    Log.e("GalleryComposeFragment/savePreview failed to delete file");
                }
                try (FileOutputStream out = new FileOutputStream(createdImage)) {
                    if (!b.compress(Bitmap.CompressFormat.JPEG, Constants.JPEG_QUALITY, out)) {
                        Log.e("GalleryComposeFragment/savePreview failed to compress");
                    }
                } catch (IOException e) {
                    Log.e("GalleryComposeFragment/savePreview failed", e);
                }
            });
        });
    }

    private void bindVideo() {
        if (captureFile != null) {
            final DataSource.Factory dataSourceFactory;
            final MediaItem exoMediaItem;
            dataSourceFactory = ExoUtils.getDefaultDataSourceFactory(videoPlayerView.getContext());
            exoMediaItem = ExoUtils.getUriMediaItem(Uri.fromFile(captureFile));

            videoPlayerView.setPauseHiddenPlayerOnScroll(true);
            videoPlayerView.setControllerAutoShow(true);
            final MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(exoMediaItem);

            if (videoPlayer != null) {
                videoPlayer.stop(true);
            }
            videoPlayer = new SimpleExoPlayer.Builder(videoPlayerView.getContext()).build();

            videoPlayerView.setPlayer(videoPlayer);
            videoPlayerView.setUseController(false);
            videoPlayerView.setVisibility(View.VISIBLE);

            videoPlayer.addListener(new Player.EventListener() {
                @Override
                public void onPlaybackStateChanged(int state) {

                }

                @Override
                public void onIsPlayingChanged(boolean isPlaying) {
                    videoPlayerView.setKeepScreenOn(isPlaying);
                }
            });

            videoPlayer.setRepeatMode(Player.REPEAT_MODE_ALL);
            videoPlayer.setMediaSource(mediaSource);
            videoPlayer.setPlayWhenReady(true);
            videoPlayer.prepare();

            PlayerControlView controlView = videoPlayerView.findViewById(R.id.exo_controller);
            controlView.setControlDispatcher(new ControlDispatcher() {
                @Override
                public boolean dispatchPrepare(Player player) {
                    return false;
                }

                @Override
                public boolean dispatchSetPlayWhenReady(@NonNull Player player, boolean playWhenReady) {
                    return false;
                }

                @Override
                public boolean dispatchSeekTo(Player player, int windowIndex, long positionMs) {
                    return false;
                }

                @Override
                public boolean dispatchPrevious(Player player) {
                    return false;
                }

                @Override
                public boolean dispatchNext(Player player) {
                    return false;
                }

                @Override
                public boolean dispatchRewind(Player player) {
                    return false;
                }

                @Override
                public boolean dispatchFastForward(Player player) {
                    return false;
                }

                @Override
                public boolean dispatchSetRepeatMode(Player player, int repeatMode) {
                    return false;
                }

                @Override
                public boolean dispatchSetShuffleModeEnabled(Player player, boolean shuffleModeEnabled) {
                    return false;
                }

                @Override
                public boolean dispatchStop(Player player, boolean reset) {
                    return false;
                }

                @Override
                public boolean dispatchSetPlaybackParameters(Player player, PlaybackParameters playbackParameters) {
                    return false;
                }

                @Override
                public boolean isRewindEnabled() {
                    return false;
                }

                @Override
                public boolean isFastForwardEnabled() {
                    return false;
                }
            });
        }
    }

    @Override
    public Media getComposedMedia() {
        return Media.createFromFile(captureType, captureType == Media.MEDIA_TYPE_IMAGE ? createdImage : captureFile);
    }

    @Override
    public View getPreview() {
        return mediaPreviewContainer;
    }

    public class MediaItemsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int VIEW_TYPE_HEADER = 1;
        private static final int VIEW_TYPE_MEDIA = 2;

        PagedList<GalleryItem> items;

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
            return items == null ? 1 : items.size() + 1;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return VIEW_TYPE_HEADER;
            }
            return VIEW_TYPE_MEDIA;
        }

        @Override
        public @NonNull
        RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_HEADER) {
                return new HeaderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.media_gallery_header, parent, false));
            }
            return new MediaItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.media_gallery_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof MediaItemViewHolder) {
                ((MediaItemViewHolder) holder).bindTo(Preconditions.checkNotNull(items.get(position - 1)));
                items.loadAround(position - 1);
            }
        }

        @Override
        public long getItemId(int position) {
            if (position == 0) {
                return -1;
            }
            return Preconditions.checkNotNull(items.get(position - 1)).id;
        }
    }

    public class SelectionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private List<GalleryItem> items;

        public void setSelectionList(List<GalleryItem> items) {
            this.items = items;
            notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            return viewModel.getMaxGallerySelection();
        }

        @Override
        public @NonNull
        RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new SelectionItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.gallery_selected_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof SelectionItemViewHolder) {
                ((SelectionItemViewHolder) holder).bindTo(position < items.size() ? items.get(position) : null);
            }
        }

        @Override
        public long getItemId(int position) {
            if (items != null && position < items.size()) {
                return Preconditions.checkNotNull(items.get(position)).id;
            }

            return -1;
        }
    }

    public class HeaderViewHolder extends RecyclerView.ViewHolder {

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);

            TextView tv = itemView.findViewById(R.id.prompt);
            tv.setText(prompt);
            itemView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    headerHeight = itemView.getHeight();
                }
            });

            View albums = itemView.findViewById(R.id.albums);
            albums.setOnClickListener(v -> openPicker());
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
                thumbnailView.setOnLongClickListener(tv -> {
                    Uri uri = ContentUris.withAppendedId(MediaStore.Files.getContentUri(GalleryDataSource.MEDIA_VOLUME), galleryItem.id);
                    showFullMedia(uri);
                    return true;
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

            if (isMultipleSelectionAllowed && viewModel.isSelectedInGallery(galleryItem)) {
                selectionIndicator.setVisibility(View.GONE);
                selectionCounter.setVisibility(View.VISIBLE);
                selectionCounter.setText(String.format(Locale.getDefault(), "%d", viewModel.galleryIndexOf(galleryItem) + 1));
            } else if (isMultipleSelectionAllowed && viewModel.canSelectMoreFromGallery()) {
                selectionIndicator.setImageResource(R.drawable.ic_item_unselected);
                selectionIndicator.setVisibility(View.VISIBLE);
                selectionCounter.setVisibility(View.GONE);
            } else {
                selectionIndicator.setVisibility(View.GONE);
                selectionCounter.setVisibility(View.GONE);
            }

            thumbnailFrame.setPadding(0, 0, 0, 0);
            thumbnailView.setSelected(false);
            thumbnailLoader.load(thumbnailView, galleryItem);
        }

        private void onItemClicked() {
            handleSelection(galleryItem);
        }
    }

    public class SelectionItemViewHolder extends RecyclerView.ViewHolder {

        final ImageView imageView;

        GalleryItem galleryItem;

        @SuppressLint("ClickableViewAccessibility")
        SelectionItemViewHolder(final @NonNull View v) {
            super(v);
            imageView = v.findViewById(R.id.image);

            if (imageView != null) {
                imageView.setOnClickListener(tv -> onItemClicked());
            }
        }

        void bindTo(final @Nullable GalleryItem galleryItem) {
            this.galleryItem = galleryItem;

            if (galleryItem == null) {
                imageView.setImageDrawable(null);
                imageView.setBackgroundColor(getResources().getColor(R.color.white_20));
            } else {
                thumbnailLoader.load(imageView, galleryItem);
            }
        }

        private void onItemClicked() {
            scrollToITem(galleryItem);
        }
    }

    private class GalleryPopupWindow extends PopupWindow {
        public GalleryPopupWindow(@NonNull Context context, @NonNull Uri uri) {
            super(LayoutInflater.from(context).inflate(R.layout.gallery_post_popup_window, null, false), ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);

            File file = FileStore.getInstance().getTmpFileForUri(uri, null);
            FileUtils.uriToFile(requireContext(), uri, file);

            final View contentView = getContentView();
            contentView.setOnClickListener(v -> dismiss());

            final CropPhotoView cropPhotoView = contentView.findViewById(R.id.image);
            mediaLoader.load(cropPhotoView, Media.createFromFile(Media.MEDIA_TYPE_IMAGE, file));
            cropPhotoView.setSinglePointerDragStartDisabled(false);
            cropPhotoView.setReturnToMinScaleOnUp(false);
            cropPhotoView.setGridEnabled(false);
            cropPhotoView.setOnCropListener(rect -> viewModel.setCropRect(rect));

            final View doneButton = contentView.findViewById(R.id.done_button);
            doneButton.setOnClickListener(v -> {
                BgWorkers.getInstance().execute(() -> {
                    File outFile = FileStore.getInstance().getTmpFile(RandomId.create());
                    try {
                        MediaUtils.cropImage(file, outFile, viewModel.cropRect, Constants.MAX_IMAGE_DIMENSION);
                        captureFile = outFile;
                        captureType = Media.MEDIA_TYPE_IMAGE;
                        v.post(() -> viewModel.onComposedMedia(Uri.fromFile(file), captureType));
                    } catch (IOException e) {
                        Log.e("failed to crop image", e);
                    }
                });
            });

            setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            setOutsideTouchable(true);
            setFocusable(false);
        }

        public void show(@NonNull View anchor) {
            View contentView = getContentView();
            contentView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            showAsDropDown(anchor);
        }
    }

    private class FullMediaPopupWindow extends PopupWindow {
        public FullMediaPopupWindow(@NonNull Context context, @NonNull Uri uri) {
            super(LayoutInflater.from(context).inflate(R.layout.gallery_full_media_popup_window, null, false), ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);

            setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            setOutsideTouchable(true);
            setFocusable(false);

            File file = FileStore.getInstance().getTmpFileForUri(uri, null);
            FileUtils.uriToFile(requireContext(), uri, file);

            View contentView = getContentView();
            contentView.setOnClickListener(v -> {
                BgWorkers.getInstance().execute(file::delete);
                dismiss();
            });

            ImageView imageView = contentView.findViewById(R.id.image);
            mediaLoader.load(imageView, Media.createFromFile(Media.MEDIA_TYPE_IMAGE, file));
        }

        public void show(@NonNull View anchor) {
            View contentView = getContentView();
            contentView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            showAsDropDown(anchor);
        }
    }
}
