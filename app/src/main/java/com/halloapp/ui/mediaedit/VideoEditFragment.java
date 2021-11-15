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
import android.widget.FrameLayout;
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
import com.halloapp.media.ExoUtils;
import com.halloapp.media.VideoThumbnailsExtractor;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.Preconditions;
import com.halloapp.util.logs.Log;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class VideoEditFragment extends Fragment {

    private static final int THUMBNAIL_COUNT = 6;
    private static final String FORMAT_HMMSS = "%tH:%tM:%tS.%tL";
    private static final String FORMAT_MMSS = "%tM:%tS.%tL";
    private static final long PLAYBACK_REFRESH_MS = 1000 / 60;

    private float borderThickness;
    private float handleRadius;
    private float thumbnailSize;

    private View playbackIndicatorView;
    private VideoRangeView rangeView;
    private TextView durationView, trimTimesView;
    private PlayerView playerView;
    private SimpleExoPlayer player;
    private State state;
    private final Runnable resetPlayerAction = this::resetPlayer;
    private final Runnable updatePlaybackIndicatorAction = this::updatePlaybackIndicator;
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
        borderThickness = getResources().getDimension(R.dimen.video_edit_range_border);
        handleRadius = getResources().getDimension(R.dimen.video_edit_range_handle_radius);

        playbackIndicatorView = view.findViewById(R.id.playback_indicator);
        trimTimesView = view.findViewById(R.id.trim_times);
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
            ArrayList<Bitmap> thumbnails = new ArrayList<>(THUMBNAIL_COUNT);
            VideoThumbnailsExtractor.extract(requireContext(), uri, THUMBNAIL_COUNT, (int) thumbnailSize, (thumbnails::add));

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

    private @NonNull MediaSource buildMediaSource(@NonNull Uri uri) {
        DataSource.Factory factory = ExoUtils.getDefaultDataSourceFactory(requireContext());
        return new ProgressiveMediaSource.Factory(factory).createMediaSource(ExoUtils.getUriMediaItem(uri));
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
                    playerView.postDelayed(updatePlaybackIndicatorAction, PLAYBACK_REFRESH_MS);
                } else {
                    playerView.removeCallbacks(resetPlayerAction);
                    playerView.removeCallbacks(updatePlaybackIndicatorAction);
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
        rangeView.setRangeChangedListener((start, end, region) -> {
            if (!isPlayerInitialized) {
                return;
            }

            state.setFractionalStart(start);
            state.setFractionalEnd(end);

            switch (region) {
                case START:
                case NONE:
                    resetPlayer(true);
                    break;
                case END:
                    resetPlayer(false);
                    break;
            }

            updateDurationView();
            syncState();
        });
    }

    private void toggleMute() {
        state.mute = !state.mute;
        player.setVolume(state.mute ? 0 : 1);
        requireActivity().invalidateOptionsMenu();
        syncState();
    }

    private String formatGranular(long time) {
        if (TimeUnit.MILLISECONDS.toHours(time) > 0) {
            return String.format(Locale.getDefault(), FORMAT_HMMSS, time, time, time, time);
        } else {
            return String.format(Locale.getDefault(), FORMAT_MMSS, time, time, time);
        }
    }

    private String formatRange(long start, long end) {
        if (end < start) return "";
        return String.format("%s - %s", formatGranular(start), formatGranular(end));
    }

    private void updateDurationView() {
        trimTimesView.setText(formatRange(state.getStart(), state.getEnd()));
        durationView.setText(DateUtils.formatElapsedTime(state.getDuration() / 1000));
    }

    private void updatePlaybackIndicator() {
        int minPosition = (int) (borderThickness / 2 + handleRadius);
        int maxSize = rangeView.getWidth() - 2 * (int) handleRadius - (int) (borderThickness / 2);
        long position = minPosition + maxSize * player.getCurrentPosition() / player.getDuration();

        FrameLayout.LayoutParams indicatorParams = (FrameLayout.LayoutParams) playbackIndicatorView.getLayoutParams();
        indicatorParams.setMarginStart((int) position);
        playbackIndicatorView.setLayoutParams(indicatorParams);

        playerView.postDelayed(updatePlaybackIndicatorAction, PLAYBACK_REFRESH_MS);
    }

    private void resetPlayer() {
        resetPlayer(true);
    }

    private void resetPlayer(boolean toStart) {
        player.setPlayWhenReady(false);

        if (toStart) {
            player.seekTo(state.getStart());
        } else {
            player.seekTo(state.getEnd());
        }
    }

    private void syncState() {
        if (!state.equals(selected.getState())) {
            viewModel.update(selected, state);
        }
    }

    public static class State implements Parcelable {
        public final long durationUs;
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
