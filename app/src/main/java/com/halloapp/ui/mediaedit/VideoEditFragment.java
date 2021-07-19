package com.halloapp.ui.mediaedit;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.DateUtils;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.halloapp.Constants;
import com.halloapp.R;
import com.halloapp.content.Media;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.Preconditions;
import com.halloapp.util.logs.Log;

import java.util.ArrayList;

public class VideoEditFragment extends Fragment {

    private static final int THUMBNAIL_COUNT = 6;

    private float thumbnailSize;
    private VideoRangeView rangeView;
    private TextView durationView;
    private PlayerView playerView;
    private SimpleExoPlayer player;
    private State state;
    private final Runnable resetPlayerAction = this::resetPlayer;
    private boolean isPlayerInitialized = false;
    private MediaEditViewModel viewModel;
    private MediaEditViewModel.Model selected;
    private final BgWorkers bgWorkers = BgWorkers.getInstance();

    public VideoEditFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_video_edit, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(MediaEditViewModel.class);
        selected = viewModel.getSelected().getValue();

        if (selected == null || selected.getType() != Media.MEDIA_TYPE_VIDEO) {
            return;
        }

        thumbnailSize = getResources().getDimension(R.dimen.video_edit_thumbnail_size);

        rangeView = view.findViewById(R.id.trim_control_range);
        durationView = view.findViewById(R.id.duration);
        playerView = view.findViewById(R.id.player);

        setupThumbnails(view, selected.uri);
        setupVideoRange();
        setupPlayer(selected.uri);

        viewModel.getMedia().observe(getViewLifecycleOwner(), models -> {
            if (isPlayerInitialized && !state.equals(selected.getState())) {
                setState((State) selected.getState());
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();

        player.setPlayWhenReady(false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        player.stop();
        player.release();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.video_edit, menu);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        MenuItem muteMenuItem = menu.findItem(R.id.mute);
        muteMenuItem.setVisible(isPlayerInitialized);
        muteMenuItem.setIcon(state != null && state.mute ? R.drawable.ic_volume_mute : R.drawable.ic_volume_up);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.mute) {
            toggleMute();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupThumbnails(@NonNull View root, @NonNull Uri uri) {
        bgWorkers.execute(() -> {
            ArrayList<Bitmap> thumbnails = generateThumbnails(uri);

            root.post(() -> {
                LinearLayout thumbnailsView = root.findViewById(R.id.trim_control_thumbnails);
                LinearLayout.LayoutParams imageViewLayoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
                imageViewLayoutParams.weight = 1;

                for (Bitmap thumb : thumbnails) {
                    ImageView imageView = new ImageView(root.getContext());
                    imageView.setLayoutParams(imageViewLayoutParams);
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    imageView.setImageBitmap(thumb);

                    thumbnailsView.addView(imageView);
                }
            });
        });
    }

    @NonNull
    @WorkerThread
    private ArrayList<Bitmap> generateThumbnails(@NonNull Uri uri) {
        ArrayList<Bitmap> thumbnails = new ArrayList<>();

        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(requireContext(), uri);

            long durationNs = Long.parseLong(Preconditions.checkNotNull(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)));
            float width = Float.parseFloat(Preconditions.checkNotNull(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)));
            float height = Float.parseFloat(Preconditions.checkNotNull(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)));
            int rotation = Integer.parseInt(Preconditions.checkNotNull(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)));

            long thumbnailIntervalUs = 1000 * durationNs / THUMBNAIL_COUNT;

            float ratio = Math.max(thumbnailSize / width, thumbnailSize / height);
            int scaledWidth = (int) (rotation == 0 || rotation == 180 ? ratio * width : ratio * height);
            int scaledHeight = (int) (rotation == 0 || rotation == 180 ? ratio * height : ratio * width);

            for (int i = 0; i < THUMBNAIL_COUNT; ++i) {
                Bitmap extractedImage = retriever.getFrameAtTime(i * thumbnailIntervalUs, 0);
                if (extractedImage == null) {
                    continue;
                }

                Bitmap scaledImage = Bitmap.createScaledBitmap(extractedImage, scaledWidth, scaledHeight, false);
                extractedImage.recycle();
                thumbnails.add(scaledImage);
            }

            retriever.release();
        } catch (NullPointerException e) {
            Log.w("VideoEditFragment: generateThumbnails video missing keys ", e);
        } catch (IllegalArgumentException e) {
            Log.e("VideoEditFragment.generateThumbnails", e);
        }

        return thumbnails;
    }

    private @NonNull MediaSource buildMediaSource(@NonNull Uri uri) {
        DataSource.Factory factory = new DefaultDataSourceFactory(requireContext(), Constants.USER_AGENT);
        return new ProgressiveMediaSource.Factory(factory).createMediaSource(MediaItem.fromUri(uri));
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupPlayer(@NonNull Uri uri) {
        player = new SimpleExoPlayer.Builder(requireContext()).build();
        player.setMediaSource(buildMediaSource(uri));
        player.addListener(new Player.EventListener() {
            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                // Prevent the player from playing the video beyond its trimmed end.
                if (isPlaying) {
                    long resetDelay = state.getEnd() - player.getCurrentPosition();
                    playerView.postDelayed(resetPlayerAction, resetDelay);
                } else {
                    playerView.removeCallbacks(resetPlayerAction);
                }
                playerView.setKeepScreenOn(isPlaying);
            }

            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == Player.STATE_READY && !isPlayerInitialized) {
                    isPlayerInitialized = true;
                    setState((State) selected.getState());
                }
            }
        });
        player.prepare();

        GestureDetector tapDetector = new GestureDetector(requireContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                player.setPlayWhenReady(!player.isPlaying());
                return true;
            }
        });
        playerView.setOnTouchListener((view, event) -> tapDetector.onTouchEvent(event));

        playerView.setPlayer(player);
    }

    private void setState(@Nullable State newState) {
        if (!isPlayerInitialized) {
            return;
        }

        if (newState == null) {
            state = new State(player.getDuration() * 1000);
        } else {
            state = new State(newState);
        }

        rangeView.setRange(state.getFractionalStart(), state.getFractionalEnd());
        player.setVolume(state.mute ? 0 : 1);

        updateDurationView();
        resetPlayer();
        requireActivity().invalidateOptionsMenu();
    }

    private void setupVideoRange() {
        rangeView.setRangeChangedListener((start, end) -> {
            if (!isPlayerInitialized) {
                return;
            }

            state.setFractionalStart(start);
            state.setFractionalEnd(end);

            updateDurationView();
            resetPlayer();
            syncState();
        });
    }

    private void toggleMute() {
        state.mute = !state.mute;
        player.setVolume(state.mute ? 0 : 1);
        requireActivity().invalidateOptionsMenu();
        syncState();
    }

    private void updateDurationView() {
        durationView.setText(DateUtils.formatElapsedTime(state.getDuration() / 1000));
    }

    private void resetPlayer() {
        player.setPlayWhenReady(false);
        player.seekTo(state.getStart());
    }

    private void syncState() {
        if (!state.equals(selected.getState())) {
            viewModel.update(selected, state);
        }
    }

    public static class State implements Parcelable {
        public long durationUs;
        public long startUs;
        public long endUs;
        public boolean mute;

        @Override
        public int describeContents() {
            return 0;
        }

        private State(long durationUS) {
            this.endUs = this.durationUs = durationUS;
        }

        private State(State state) {
            durationUs = state.durationUs;
            startUs = state.startUs;
            endUs = state.endUs;
            mute = state.mute;
        }

        public long getStart() {
            return startUs / 1000;
        }

        public long getEnd() {
            return endUs / 1000;
        }

        public long getDuration() {
            return (endUs - startUs) / 1000;
        }

        public float getFractionalStart() {
            return 1f * startUs / durationUs;
        }

        public float getFractionalEnd() {
            return 1f * endUs / durationUs;
        }

        public void setFractionalStart(float start) {
            startUs = (long) (start * durationUs);
        }

        public void setFractionalEnd(float end) {
            endUs = (long) (end * durationUs);
        }

        private State(Parcel in) {
            durationUs = in.readLong();
            startUs = in.readLong();
            endUs = in.readLong();
            mute = in.readInt() == 1;
        }

        @Override
        public void writeToParcel(Parcel parcel, int flags) {
            parcel.writeLong(durationUs);
            parcel.writeLong(startUs);
            parcel.writeLong(endUs);
            parcel.writeInt(mute ? 1 : 0);
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (super.equals(obj)) {
                return true;
            }

            if (!(obj instanceof State)) {
                return false;
            }

            State state = (State) obj;

            return state.startUs == startUs && state.endUs == endUs && state.mute == mute;
        }

        public static final Parcelable.Creator<State> CREATOR = new Parcelable.Creator<State>() {
            public State createFromParcel(Parcel in) {
                return new State(in);
            }

            public State[] newArray(int size) {
                return new State[size];
            }
        };
    }
}
