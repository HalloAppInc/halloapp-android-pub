package com.halloapp.katchup.compose;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.graphics.Outline;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.GridLayoutManager;
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
import com.halloapp.FileStore;
import com.halloapp.R;
import com.halloapp.content.Media;
import com.halloapp.katchup.SelfiePostComposerActivity;
import com.halloapp.media.ExoUtils;
import com.halloapp.ui.mediapicker.GalleryDataSource;
import com.halloapp.ui.mediapicker.GalleryItem;
import com.halloapp.ui.mediapicker.GallerySpanSizeLookup;
import com.halloapp.ui.mediapicker.GalleryThumbnailLoader;
import com.halloapp.ui.mediapicker.MediaPickerActivity;
import com.halloapp.ui.mediapicker.MediaPickerViewModel;
import com.halloapp.util.FileUtils;
import com.halloapp.util.Preconditions;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.ActionBarShadowOnScrollListener;
import com.halloapp.widget.ContentPlayerView;
import com.halloapp.widget.GridSpacingItemDecoration;

import java.io.File;
import java.util.ArrayList;

public class GalleryComposeFragment extends ComposeFragment {

    public static GalleryComposeFragment newInstance(String prompt) {
        Bundle args = new Bundle();
        args.putString(EXTRA_PROMPT, prompt);

        GalleryComposeFragment fragment = new GalleryComposeFragment();
        fragment.setArguments(args);

        return fragment;
    }

    private static final String EXTRA_PROMPT = "prompt";

    private static final int ITEMS_PER_ROW = 3;

    private View mediaPreviewContainer;
    private View gallerySelectionContainer;

    private ImageView mediaPreviewView;

    private SelfiePostComposerActivity host;

    private SelfieComposerViewModel viewModel;
    private MediaItemsAdapter adapter = new MediaItemsAdapter();
    private GalleryThumbnailLoader thumbnailLoader;

    private File captureFile;
    private @Media.MediaType int captureType;

    private ContentPlayerView videoPlayerView;

    private SimpleExoPlayer videoPlayer;

    private TextView promptView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.katchup_fragment_gallery_compose, container, false);

        gallerySelectionContainer = root.findViewById(R.id.gallery_container);

        mediaPreviewContainer = root.findViewById(R.id.preview_container);
        mediaPreviewView = root.findViewById(R.id.media_preview);
        videoPlayerView = root.findViewById(R.id.video_player);

        promptView = root.findViewById(R.id.prompt);

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
                case SelfieComposerViewModel.ComposeState.COMPOSING_SELFIE:
                case SelfieComposerViewModel.ComposeState.TRANSITIONING:
                case SelfieComposerViewModel.ComposeState.READY_TO_SEND:
                    showPreviewView();
                    break;
            }
        });

        Bundle args = getArguments();
        if (args != null) {
            String prompt = args.getString(EXTRA_PROMPT, null);
            if (!TextUtils.isEmpty(prompt)) {
                promptView.setText(prompt);
            }
        }

        final GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), ITEMS_PER_ROW);
        final RecyclerView mediaView = root.findViewById(android.R.id.list);
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

        viewModel.getMediaList().observe(getViewLifecycleOwner(), mediaItems -> {
            adapter.setPagedList(mediaItems);
//            progressView.setVisibility(View.GONE);
//            emptyView.setVisibility(mediaItems.isEmpty() ? View.VISIBLE : View.GONE);
        });

        return root;
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

    private void handleSelection(@NonNull Uri uri) {
        File file = FileStore.getInstance().getTmpFileForUri(uri, null);
        FileUtils.uriToFile(requireContext(), uri, file);
        captureFile = file;
        captureType = Media.MEDIA_TYPE_IMAGE;
        showPreviewView();
        viewModel.onComposedMedia(Uri.fromFile(file), captureType);
    }

    private void showSelectionView() {
        gallerySelectionContainer.setVisibility(View.VISIBLE);
        mediaPreviewContainer.setVisibility(View.GONE);
        videoPlayerView.setPlayer(null);
        if (videoPlayer != null) {
            videoPlayer.stop(true);
            videoPlayer = null;
        }
    }

    private void showPreviewView() {
        gallerySelectionContainer.setVisibility(View.GONE);
        mediaPreviewContainer.setVisibility(View.VISIBLE);
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
        return Media.createFromFile(captureType, captureFile);
    }

    @Override
    public View getPreview() {
        return mediaPreviewContainer;
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
        public @NonNull
        MediaItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
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
            Uri uri = ContentUris.withAppendedId(MediaStore.Files.getContentUri(GalleryDataSource.MEDIA_VOLUME), galleryItem.id);
            handleSelection(uri);
        }
    }
}
