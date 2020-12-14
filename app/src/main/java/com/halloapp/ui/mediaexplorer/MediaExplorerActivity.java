package com.halloapp.ui.mediaexplorer;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.transition.Transition;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.SharedElementCallback;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.MarginPageTransformer;
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
import com.halloapp.ui.MediaPagerAdapter;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.Preconditions;
import com.halloapp.util.logs.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import me.relex.circleindicator.CircleIndicator3;

public class MediaExplorerActivity extends HalloActivity {
    public static final String EXTRA_MEDIA = "media";
    public static final String EXTRA_SELECTED = "selected";
    public static final String EXTRA_CONTENT_ID = "content-id";

    private int swipeExitStartThreshold;
    private int swipeExitFinishThreshold;
    private float swipeExitTransDistance;

    private ViewPager2 pager;
    private CircleIndicator3 indicator;
    private ArrayList<Model> data = new ArrayList<>();
    private MotionEvent swipeExitStart;
    private boolean isSwipeExitInProgress = false;
    private String contentId;
    private ImageView transitionImage;

    final private Transition.TransitionListener transitionListener = new Transition.TransitionListener() {
        @Override
        public void onTransitionStart(Transition transition) {}

        @Override
        public void onTransitionEnd(Transition transition) {
            transition.removeListener(this);

            transitionImage.post(() -> {
                pager.setAlpha(1f);
                transitionImage.setAlpha(0f);
                transitionImage.setImageBitmap(null);
                transitionImage.setLeft(0);
                transitionImage.setTop(0);
                transitionImage.setBottom(0);
                transitionImage.setRight(0);

                ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)transitionImage.getLayoutParams();
                params.width = 0;
                params.height = 0;
                params.topMargin = 0;
                params.leftMargin = 0;
                transitionImage.setLayoutParams(params);
            });
        }

        @Override
        public void onTransitionCancel(Transition transition) { }
        @Override
        public void onTransitionPause(Transition transition) { }
        @Override
        public void onTransitionResume(Transition transition) { }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        postponeEnterTransition();

        swipeExitStartThreshold = getResources().getDimensionPixelSize(R.dimen.swipe_exit_start_threshold);
        swipeExitFinishThreshold = getResources().getDimensionPixelSize(R.dimen.swipe_exit_finish_threshold);
        swipeExitTransDistance = getResources().getDimension(R.dimen.swipe_exit_transition_distance);

        if (Build.VERSION.SDK_INT >= 28) {
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        toggleSystemUI();

        setContentView(R.layout.activity_media_explorer);
        Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Preconditions.checkNotNull(getSupportActionBar()).setHomeAsUpIndicator(R.drawable.ic_arrow_left_stroke);

        data = getIntent().getParcelableArrayListExtra(EXTRA_MEDIA);
        if (data == null || data.size() == 0) {
            finish();
            return;
        }

        contentId = getIntent().getStringExtra(EXTRA_CONTENT_ID);

        pager = findViewById(R.id.media_pager);
        pager.setAdapter(new MediaExplorerAdapter(data));
        pager.setPageTransformer(new MarginPageTransformer(getResources().getDimensionPixelSize(R.dimen.explorer_pager_margin)));

        indicator = findViewById(R.id.media_pager_indicator);
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

        transitionImage = findViewById(R.id.transition_image);
        transitionImage.setTransitionName(MediaPagerAdapter.getTransitionName(contentId, pager.getCurrentItem()));
        getWindow().getSharedElementEnterTransition().addListener(transitionListener);

        pager.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                pager.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                final Model model = data.get(pager.getCurrentItem());
                BgWorkers.getInstance().execute(() -> {
                    Bitmap bitmap;
                    try {
                        bitmap = MediaUtils.decode(new File(model.uri.getPath()), model.type, Constants.MAX_IMAGE_DIMENSION);
                    } catch (IOException e) {
                        Log.e("MediaExplorerActivity: missing shared element enter transition media", e);
                        return;
                    }

                    transitionImage.post(() -> {
                        transitionImage.setImageBitmap(bitmap);
                        pager.setAlpha(0f);
                        transitionImage.setAlpha(1f);
                        startPostponedEnterTransition();
                    });
                });
            }
        });

        setEnterSharedElementCallback(new SharedElementCallback() {
            @Override
            public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                sharedElements.put(transitionImage.getTransitionName(), transitionImage);
            }

            @Override
            public void onSharedElementEnd(List<String> sharedElementNames, List<View> sharedElements, List<View> sharedElementSnapshots) {
                if (sharedElementSnapshots.size() < 1 || sharedElementSnapshots.get(0) == null) {
                    return;
                }

                Rect snapshotFrame = getFrame(sharedElementSnapshots.get(0));
                Rect containerFrame = getFrame(findViewById(R.id.main));

                float scale = Math.min((float)containerFrame.width() / (float)snapshotFrame.width(), (float)containerFrame.height() / (float)snapshotFrame.height());
                int width = (int)((float)snapshotFrame.width() * scale);
                int height = (int)((float)snapshotFrame.height() * scale);
                int centerX = containerFrame.centerX();
                int centerY = containerFrame.centerY();

                Model model = data.get(pager.getCurrentItem());
                View mediaView = pager.findViewWithTag(model);
                if (mediaView != null) {
                    centerX += mediaView.getTranslationX();
                    centerY += mediaView.getTranslationY();
                    width *= mediaView.getScaleX();
                    height *= mediaView.getScaleY();
                }

                // Layout is executed only after the transition, where as setLeft, ..., have immediate
                // effect but are not kept after transition
                View view = sharedElements.get(0);
                view.setLeft(centerX - width / 2);
                view.setTop(centerY - height / 2);
                view.setBottom(centerY + height / 2);
                view.setRight(centerX + width / 2);

                // Layout is executed only after the transition, but the state is kept
                ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)view.getLayoutParams();
                params.width = width;
                params.height = height;
                params.topMargin = centerY - height / 2;
                params.leftMargin = centerX - width / 2;
                view.setLayoutParams(params);
            }

            @NonNull
            private Rect getFrame(@NonNull View view) {
                int[] location = new int[2];
                view.getLocationInWindow(location);

                return new Rect(location[0], location[1], location[0] + view.getWidth(), location[1] + view.getHeight());
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        return onTouchEventForSwipeExit(event) || super.dispatchTouchEvent(event);
    }

    private boolean onTouchEventForSwipeExit(MotionEvent event) {
        int action = event.getAction();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (event.getPointerCount() == 1 && shouldAllowSwipeExit()) {
                    swipeExitStart = MotionEvent.obtain(event);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (swipeExitStart != null && event.getPointerCount() > 1) {
                    cancelSwipeExit();
                } else if (isSwipeExitInProgress) {
                    onSwipeExitMove(event);
                } else if (swipeExitStart != null) {
                    float distanceX = event.getX() - swipeExitStart.getX();
                    float distanceY = event.getY() - swipeExitStart.getY();

                    if (distanceY > swipeExitStartThreshold && distanceY > Math.abs(distanceX)) {
                        isSwipeExitInProgress = true;
                        onSwipeExitMove(event);
                    }
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                cancelSwipeExit();
                break;
            case MotionEvent.ACTION_UP:
                if (swipeExitStart != null) {
                    float distanceX = event.getX() - swipeExitStart.getX();
                    float distanceY = event.getY() - swipeExitStart.getY();

                    if (isSwipeExitInProgress && distanceX * distanceX + distanceY * distanceY > swipeExitFinishThreshold * swipeExitFinishThreshold) {
                        finishSwipeExit();
                    } else {
                        cancelSwipeExit();
                    }
                }
                break;
        }

        return isSwipeExitInProgress;
    }

    private boolean shouldAllowSwipeExit() {
        Model model = data.get(pager.getCurrentItem());

        if (model.type == Media.MEDIA_TYPE_IMAGE) {
            PhotoView view = pager.findViewWithTag(model);
            return Math.abs(view.getScale() - 1.0) < 0.2;
        }

        return true;
    }

    private void cancelSwipeExit() {
        if (swipeExitStart != null && isSwipeExitInProgress) {
            Model model = data.get(pager.getCurrentItem());
            View view = pager.findViewWithTag(model);

            View main = findViewById(R.id.main);
            main.setBackgroundColor(Color.rgb(0, 0, 0));

            AnimatorSet set = new AnimatorSet();
            set.play(ObjectAnimator.ofFloat(main, "alpha", main.getAlpha(), 1.0f))
                .with(ObjectAnimator.ofFloat(view, "translationX", view.getTranslationX(), 0f))
                .with(ObjectAnimator.ofFloat(view, "translationY", view.getTranslationY(), 0f))
                .with(ObjectAnimator.ofFloat(view, "scaleX", view.getScaleX(), 1.0f))
                .with(ObjectAnimator.ofFloat(view, "scaleY", view.getScaleY(), 1.0f));
            set.setDuration(300);
            set.start();
        }

        swipeExitStart = null;
        isSwipeExitInProgress = false;
    }

    private void finishSwipeExit() {
         onBackPressed();
    }

    private void onSwipeExitMove(MotionEvent event) {
        if (swipeExitStart != null && isSwipeExitInProgress) {
            final float swipeExitScale = 0.8f;
            final float swipeExitAlpha = 0.3f;

            float distanceX = event.getX() - swipeExitStart.getX();
            float distanceY = event.getY() - swipeExitStart.getY();
            float progress = Math.min((distanceX * distanceX + distanceY * distanceY ) / (swipeExitTransDistance * swipeExitTransDistance), 1.0f);
            float scale = 1 - progress + swipeExitScale * progress;
            int alpha = (int)(255 * (1 - progress + swipeExitAlpha * progress));

            Model model = data.get(pager.getCurrentItem());
            View view = pager.findViewWithTag(model);
            view.setTranslationX(distanceX);
            view.setTranslationY(distanceY);
            view.setScaleX(scale);
            view.setScaleY(scale);

            View main = findViewById(R.id.main);
            main.setBackgroundColor(Color.argb(alpha, 0, 0, 0));
        }
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

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_CONTENT_ID, contentId);
        intent.putExtra(EXTRA_SELECTED, pager.getCurrentItem());

        setResult(RESULT_OK, intent);

        Model model = data.get(pager.getCurrentItem());
        transitionImage.setTransitionName(MediaPagerAdapter.getTransitionName(contentId, pager.getCurrentItem()));

        BgWorkers.getInstance().execute(() -> {
            Bitmap bitmap;
            try {
                bitmap = MediaUtils.decode(new File(model.uri.getPath()), model.type, Constants.MAX_IMAGE_DIMENSION);
            } catch (IOException e) {
                Log.e("MediaExplorerActivity: missing shared element return transition media", e);
                return;
            }

            transitionImage.post(() -> {
                transitionImage.setImageBitmap(bitmap);
                transitionImage.setAlpha(1f);
                finishAfterTransition();
            });
        });
    }

    private void toggleSystemUI() {
        if (data.size() > 1) {
            indicator.animate().alpha(isSystemUIShown() ? 0.0f : 1.0f).start();
        }

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
        private final PhotoView imageView;

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
                    Log.e("MediaExplorerActivity: unable to bind image", e);
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
        private final PlayerView playerView;

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
        private final ArrayList<Model> models = new ArrayList<>();

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
