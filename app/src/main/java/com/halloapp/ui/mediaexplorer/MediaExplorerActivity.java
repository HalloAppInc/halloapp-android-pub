package com.halloapp.ui.mediaexplorer;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.github.chrisbanes.photoview.PhotoView;
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
import com.halloapp.media.MediaUtils;
import com.halloapp.ui.HalloActivity;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.Preconditions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.relex.circleindicator.CircleIndicator3;

public class MediaExplorerActivity extends HalloActivity {
    public static final String EXTRA_MEDIA = "media";
    public static final String EXTRA_SELECTED = "selected";

    private float swipeDistanceThreshold;
    private float swipeVelocityThreshold;

    private ViewPager2 pager;
    private ArrayList<Model> data = new ArrayList<>();
    private MotionEvent swipeDownStart;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        swipeDistanceThreshold = getResources().getDimension(R.dimen.swipe_down_distance_threshold);
        swipeVelocityThreshold = getResources().getDimension(R.dimen.swipe_down_velocity_threshold);

        if (Build.VERSION.SDK_INT >= 28) {
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        toggleSystemUI();

        setContentView(R.layout.activity_media_explorer);
        Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        data = getIntent().getParcelableArrayListExtra(EXTRA_MEDIA);
        if (data == null || data.size() == 0) {
            finish();
            return;
        }

        pager = findViewById(R.id.media_pager);
        pager.setAdapter(new MediaExplorerAdapter(data));

        CircleIndicator3 indicator = findViewById(R.id.media_pager_indicator);
        indicator.setViewPager(pager);
        indicator.setVisibility(data.size() > 1 ? View.VISIBLE : View.GONE);

        pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                if (0 <= position && position < data.size()) {
                    handlePlaybackOnPageChange(position);
                    updatePlaybackControlsVisibility();
                }
            }
        });

        pager.setCurrentItem(getIntent().getIntExtra(EXTRA_SELECTED, 0), false);

        findViewById(R.id.main).setOnClickListener(v -> toggleSystemUI());
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        int action = event.getAction();

        if (event.getPointerCount() == 1 && action == MotionEvent.ACTION_DOWN) {
            swipeDownStart = MotionEvent.obtain(event);
        } else if (event.getPointerCount() > 1 || action == MotionEvent.ACTION_CANCEL) {
            swipeDownStart = null;
        } else if (action == MotionEvent.ACTION_UP && swipeDownStart != null) {
            if (isSwipeDown(swipeDownStart, event)) {
                onSwipeDown();
            }

            swipeDownStart = null;
        }

        return super.dispatchTouchEvent(event);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopPlayback();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayers();
    }

    private void updatePlaybackControlsVisibility() {
        boolean isShown = isSystemUIShown();

        for (Model model : data) {
            if (model.type != Media.MEDIA_TYPE_VIDEO) {
                continue;
            }

            PlayerView playerView = pager.findViewWithTag(model);

            if (playerView != null) {
                if (isShown) {
                    playerView.showController();
                } else {
                    playerView.hideController();
                }
            }
        }
    }

    private void handlePlaybackOnPageChange(int position) {
        stopPlayback();

        if (data.get(position).type == Media.MEDIA_TYPE_VIDEO) {
            PlayerView playerView = pager.findViewWithTag(data.get(position));

            if (playerView != null) {
                SimpleExoPlayer player = (SimpleExoPlayer) playerView.getPlayer();

                if (player != null && !player.isPlaying()) {
                    player.setPlayWhenReady(true);
                }
            }
        }
    }

    private void stopPlayback() {
        for (Model model : data) {
            if (model.type != Media.MEDIA_TYPE_VIDEO) {
                continue;
            }

            PlayerView playerView = pager.findViewWithTag(model);
            if (playerView != null) {
                Player player = playerView.getPlayer();

                if (player != null) {
                    player.setPlayWhenReady(false);
                }
            }
        }
    }

    private void releasePlayers() {
        for (Model model : data) {
            if (model.type != Media.MEDIA_TYPE_VIDEO) {
                continue;
            }

            PlayerView playerView = pager.findViewWithTag(model);
            if (playerView != null) {
                Player player = playerView.getPlayer();

                if (player != null) {
                    player.stop();
                    player.release();
                }
            }
        }
    }

    private boolean isSwipeDown(MotionEvent start, MotionEvent end) {
        float distanceY = end.getY() - start.getY();
        float time = end.getEventTime() - start.getEventTime();

        return time > 0 && distanceY > swipeDistanceThreshold && (distanceY * 1000 / time) > swipeVelocityThreshold;
    }

    private void onSwipeDown() {
        finish();
        overridePendingTransition(0, R.anim.slide_down);
    }

    private void toggleSystemUI() {
        int options = getWindow().getDecorView().getSystemUiVisibility();

        options ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        options ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        options ^= View.SYSTEM_UI_FLAG_FULLSCREEN;

        options |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        options |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        options |= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;

        getWindow().getDecorView().setSystemUiVisibility(options);
    }

    private boolean isSystemUIShown() {
        return (getWindow().getDecorView().getSystemUiVisibility() & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0;
    }

    public static class Model implements Parcelable {
        public Uri uri;
        public int type;

        public Model(@NonNull Uri uri, int type) {
            this.uri = uri;
            this.type = type;
        }

        private Model(Parcel in) {
            uri = in.readParcelable(Uri.class.getClassLoader());
            type = in.readInt();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int flags) {
            parcel.writeParcelable(uri, flags);
            parcel.writeInt(type);
        }

        public static final Parcelable.Creator<Model> CREATOR = new Parcelable.Creator<Model>() {
            public Model createFromParcel(Parcel in) {
                return new Model(in);
            }

            public Model[] newArray(int size) {
                return new Model[size];
            }
        };
    }

    private abstract static class DefaultHolder extends RecyclerView.ViewHolder {

        public DefaultHolder(@NonNull View itemView) {
            super(itemView);
        }

        public abstract void bindTo(@NonNull Model model, int position);
    }

    private class ImageHolder extends DefaultHolder {
        private PhotoView imageView;

        public ImageHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image);
            imageView.setReturnToMinScaleOnUp(false);
            imageView.setOnClickListener(v -> toggleSystemUI());
        }

        @Override
        public void bindTo(@NonNull Model model, int position) {
            imageView.setTag(model);

            BgWorkers.getInstance().execute(() -> {
                Bitmap bitmap;
                try {
                    bitmap = MediaUtils.decodeImage(new File(model.uri.getPath()), Constants.MAX_IMAGE_DIMENSION);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }

                imageView.post(() -> {
                    if (imageView.getTag() == model) {
                        imageView.setImageBitmap(bitmap);
                    }
                });
            });
        }
    }

    private class VideoHolder extends DefaultHolder {
        private PlayerView playerView;

        public VideoHolder(@NonNull View itemView) {
            super(itemView);
            playerView = itemView.findViewById(R.id.player);
            playerView.setControllerAutoShow(false);
            playerView.setOnClickListener(v -> toggleSystemUI());

            if (isSystemUIShown()) {
                playerView.showController();
            } else {
                playerView.hideController();
            }
        }

        @Override
        public void bindTo(@NonNull Model model, int position) {
            playerView.setTag(model);

            final DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(playerView.getContext(), Constants.USER_AGENT);
            MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(model.uri);

            SimpleExoPlayer player = new SimpleExoPlayer.Builder(playerView.getContext()).build();
            playerView.setPlayer(player);

            player.setRepeatMode(Player.REPEAT_MODE_ALL);
            player.prepare(mediaSource);
        }
    }

    private class MediaExplorerAdapter extends RecyclerView.Adapter<DefaultHolder> {
        private ArrayList<Model> models = new ArrayList<>();

        MediaExplorerAdapter(@NonNull List<Model> data) {
            models.addAll(data);
        }

        @Override
        public int getItemViewType(int position) {
            return models.get(position).type;
        }

        @NonNull
        @Override
        public DefaultHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == Media.MEDIA_TYPE_IMAGE) {
                return new ImageHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.media_explorer_image, parent, false));
            } else {
                return new VideoHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.media_explorer_video, parent, false));
            }
        }

        @Override
        public void onBindViewHolder(@NonNull DefaultHolder holder, int position) {
            holder.bindTo(models.get(position), position);
        }

        @Override
        public int getItemCount() {
            return models.size();
        }
    }
}
