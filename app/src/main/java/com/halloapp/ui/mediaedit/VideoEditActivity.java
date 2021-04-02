package com.halloapp.ui.mediaedit;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.media.MediaMuxer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.halloapp.Constants;
import com.halloapp.FileStore;
import com.halloapp.R;
import com.halloapp.ui.CropImageActivity;
import com.halloapp.ui.HalloActivity;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.Preconditions;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.SnackbarHelper;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

public class VideoEditActivity extends HalloActivity {
    private static final int TRIMMING_BUFFER_SIZE = 2 * 1024 * 1024;

    private long videoDuration;
    private final int numberOfThumbnails = 6;
    private float thumbnailSize;
    private VideoRangeView range;
    private TextView durationView;
    private PlayerView playerView;
    private SimpleExoPlayer player;
    private State videoState;
    private final Handler handler = new Handler();
    private final Runnable resetPlayerAction = this::resetPlayer;
    private final BgWorkers bgWorkers = BgWorkers.getInstance();
    private boolean isProcessing = false;
    private boolean isPlayerInitialized = false;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        thumbnailSize = getResources().getDimension(R.dimen.video_edit_thumbnail_size);

        setContentView(R.layout.activity_video_edit);
        Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        ArrayList<Uri> uris = getIntent().getParcelableArrayListExtra(CropImageActivity.EXTRA_MEDIA);
        Bundle state = getIntent().getBundleExtra(CropImageActivity.EXTRA_STATE);
        int selected = getIntent().getIntExtra(CropImageActivity.EXTRA_SELECTED, 0);

        Uri current;
        if (uris != null) {
            current = uris.get(selected);
        } else {
            finish();
            return;
        }

        String parcelableKey = current.toString();
        videoState = state != null && state.containsKey(parcelableKey) ? state.getParcelable(parcelableKey) : new State();

        range = findViewById(R.id.trim_control_range);
        durationView = findViewById(R.id.duration);
        playerView = findViewById(R.id.player);

        setupThumbnails(current);
        setupPlayer(current);

        findViewById(R.id.reset).setOnClickListener(v -> {
            if (isProcessing || !isPlayerInitialized) {
                return;
            }

            setRange(0, 1);
            setMute(false);
            range.setRange(0, 1);
        });

        findViewById(R.id.done).setOnClickListener(v -> {
            if (isProcessing || !isPlayerInitialized) {
                return;
            }
            isProcessing = true;

            playerView.hideController();
            findViewById(R.id.processing).setVisibility(View.VISIBLE);

            bgWorkers.execute(() -> {
                try {
                    trim(current);
                } catch (IOException e) {
                    Log.e("VideoEditActivity: unable to trim video " + current);
                    SnackbarHelper.showWarning(this, R.string.video_edit_unable_trim);
                    return;
                }

                runOnUiThread(this::prepareAndFinish);
            });
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        player.setPlayWhenReady(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        player.stop();
        player.release();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.video_edit, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (!isProcessing) {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.mute);
        item.setIcon(videoState.mute ? R.drawable.ic_volume_mute : R.drawable.ic_volume_up);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.mute) {
            setMute(!videoState.mute);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupThumbnails(Uri uri) {
        LinearLayout trimControlThumbnails = findViewById(R.id.trim_control_thumbnails);
        LinearLayout.LayoutParams thumbParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
        thumbParams.weight = 1;

        bgWorkers.execute(() -> {
            ArrayList<Bitmap> thumbnails = generateThumbnails(uri);

            runOnUiThread(() -> {
                for (Bitmap thumb : thumbnails) {
                    ImageView imageView = new ImageView(this);
                    imageView.setLayoutParams(thumbParams);
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    imageView.setImageBitmap(thumb);

                    trimControlThumbnails.addView(imageView);
                }
            });
        });
    }

    @WorkerThread
    private ArrayList<Bitmap> generateThumbnails(Uri uri) {
        ArrayList<Bitmap> thumbnails = new ArrayList<>();

        try(MediaMetadataRetriever retriever = new MediaMetadataRetriever()) {
            retriever.setDataSource(this, uri);

            long durationMicroSeconds = 1000 * Long.parseLong(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
            long thumbnailInterval = durationMicroSeconds / numberOfThumbnails;

            float width = Float.parseFloat(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
            float height = Float.parseFloat(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
            int rotation = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION));

            float ratio = Math.max(thumbnailSize / width, thumbnailSize / height);
            int scaledWidth = (int) (rotation == 0 || rotation == 180 ? ratio * width : ratio * height);
            int scaledHeight = (int) (rotation == 0 || rotation == 180 ? ratio * height : ratio * width);

            for (int i = 0; i < numberOfThumbnails; ++i) {
                Bitmap extractedImage = retriever.getFrameAtTime(i * thumbnailInterval, 0);
                Bitmap scaledImage = Bitmap.createScaledBitmap(extractedImage, scaledWidth, scaledHeight, false);
                extractedImage.recycle();
                thumbnails.add(scaledImage);
            }
        }

        return thumbnails;
    }

    private void setupPlayer(Uri uri) {
        player = new SimpleExoPlayer.Builder(playerView.getContext()).build();
        playerView.setPlayer(player);

        DataSource.Factory factory = new DefaultDataSourceFactory(playerView.getContext(), Constants.USER_AGENT);
        MediaItem mediaItem = MediaItem.fromUri(uri);
        MediaSource source = new ProgressiveMediaSource.Factory(factory).createMediaSource(mediaItem);
        player.setMediaSource(source);
        player.prepare();

        player.addListener(new Player.EventListener() {
            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                // Prevent the player from playing the video beyond its trimmed end.
                if (isPlaying) {
                    long resetDelay = getEndTime() - player.getCurrentPosition();
                    handler.postDelayed(resetPlayerAction, resetDelay);
                } else {
                    handler.removeCallbacks(resetPlayerAction);
                }
                playerView.setKeepScreenOn(isPlaying);
            }

            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == Player.STATE_READY && !isPlayerInitialized) {
                    isPlayerInitialized = true;
                    videoDuration = player.getDuration();

                    player.setVolume(videoState.mute ? 0 : 1);

                    updateDurationView();
                    resetPlayer();
                    setupVideoRange();
                }
            }
        });
    }

    private void setupVideoRange() {
        range.setRange(videoState.start, videoState.end);
        range.setRangeChangedListener(this::setRange);
    }

    private long getDurationInSeconds() {
        return (long) (videoDuration * (videoState.end - videoState.start) / 1000);
    }

    private long getStartTime() {
        return (long) (videoDuration * videoState.start);
    }

    private long getEndTime() {
        return (long) (videoDuration * videoState.end);
    }

    private void setRange(float start, float end) {
        videoState.start = start;
        videoState.end = end;

        updateDurationView();
        resetPlayer();
    }

    private void setMute(boolean mute) {
        videoState.mute = mute;
        invalidateOptionsMenu();

        player.setVolume(mute ? 0 : 1);
    }

    private void updateDurationView() {
        durationView.setText(DateUtils.formatElapsedTime(getDurationInSeconds()));
    }

    private void resetPlayer() {
        player.setPlayWhenReady(false);
        player.seekTo(getStartTime());
    }

    @WorkerThread
    private void trim(Uri uri) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(TRIMMING_BUFFER_SIZE);
        File edit = FileStore.getInstance().getTmpFileForUri(uri, "edit");
        int rotationDegrees = 0;

        MediaMuxer muxer = new MediaMuxer(edit.getAbsolutePath(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

        MediaExtractor extractor = new MediaExtractor();
        extractor.setDataSource(this, uri, null);

        HashMap<Integer, Integer> indexes = new HashMap<>();
        for (int i = 0; i < extractor.getTrackCount(); ++i) {
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);

            if (videoState.mute && mime != null && mime.startsWith("audio/")) {
                continue;
            }

            if (Build.VERSION.SDK_INT >= 23 && format.containsKey(MediaFormat.KEY_ROTATION)) {
                rotationDegrees = format.getInteger(MediaFormat.KEY_ROTATION);
            }

            indexes.put(i, muxer.addTrack(format));
        }

        long startTimeUs = getStartTime() * 1000;
        long endTimeUs = getEndTime() * 1000;

        muxer.setOrientationHint(rotationDegrees);

        muxer.start();
        for (int i = 0; i < extractor.getTrackCount(); ++i) {
            if (!indexes.containsKey(i)) {
                continue;
            }

            int index = indexes.get(i);

            extractor.selectTrack(i);
            extractor.seekTo(startTimeUs, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);

            while (true) {
                MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();

                try {
                    info.size = extractor.readSampleData(buffer, 0);
                    info.presentationTimeUs = extractor.getSampleTime();
                    info.flags = extractor.getSampleFlags();
                } catch (IllegalArgumentException e) {
                    Log.e("VideoEditActivity: unable to extract video, probably buffer too small");
                    edit.delete();
                    throw new IOException();
                }

                if (info.size < 0 || (endTimeUs > 0 && info.presentationTimeUs > endTimeUs)) {
                    break;
                }

                muxer.writeSampleData(index, buffer, info);
                extractor.advance();
            }

            extractor.unselectTrack(i);
        }
        muxer.stop();

        extractor.release();
        muxer.release();
    }

    private void prepareAndFinish() {
        Intent intent = new Intent();

        ArrayList<Uri> uris = getIntent().getParcelableArrayListExtra(CropImageActivity.EXTRA_MEDIA);
        Bundle state = getIntent().getBundleExtra(CropImageActivity.EXTRA_STATE);
        int selected = getIntent().getIntExtra(CropImageActivity.EXTRA_SELECTED, 0);

        if (uris != null) {
            Uri current = uris.get(selected);

            if (state == null) {
                state = new Bundle();
            }

            state.putParcelable(current.toString(), videoState);
        }

        intent.putParcelableArrayListExtra(CropImageActivity.EXTRA_MEDIA, uris);
        intent.putExtra(CropImageActivity.EXTRA_STATE, state);
        intent.putExtra(CropImageActivity.EXTRA_SELECTED, selected);

        setResult(RESULT_OK, intent);
        finish();
    }

    public static class State implements Parcelable {
        public float start;
        public float end;
        public boolean mute;

        @Override
        public int describeContents() {
            return 0;
        }

        public State() {
            start = 0;
            end = 1;
            mute = false;
        }

        private State(Parcel in) {
            start = in.readFloat();
            end = in.readFloat();
            mute = in.readInt() == 1;
        }

        @Override
        public void writeToParcel(Parcel parcel, int flags) {
            parcel.writeFloat(start);
            parcel.writeFloat(end);
            parcel.writeInt(mute ? 1 : 0);
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
