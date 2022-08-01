package com.halloapp.ui.mediaexplorer;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.transition.Transition;
import android.util.Size;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.Keep;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.app.SharedElementCallback;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.PagedList;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.halloapp.Constants;
import com.halloapp.R;
import com.halloapp.content.Media;
import com.halloapp.content.Message;
import com.halloapp.id.ChatId;
import com.halloapp.media.ChunkedMediaParameters;
import com.halloapp.media.ChunkedMediaParametersException;
import com.halloapp.media.ExoUtils;
import com.halloapp.media.MediaUtils;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.MediaPagerAdapter;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.Preconditions;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.SnackbarHelper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import me.relex.circleindicator.CircleIndicator3;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class MediaExplorerActivity extends HalloActivity implements EasyPermissions.PermissionCallbacks {
    public static final String EXTRA_MEDIA = "media";
    public static final String EXTRA_SELECTED = "selected";
    public static final String EXTRA_CONTENT_ID = "content-id";
    public static final String EXTRA_CHAT_ID = "chat-id";
    public static final String EXTRA_INITIAL_TIME = "initial-time";
    public static final String EXTRA_ALLOW_SAVING = "allow_saving";

    private static final int REQUEST_EXTERNAL_STORAGE_PERMISSIONS = 1;

    private int swipeExitStartThreshold;
    private int swipeExitFinishThreshold;
    private float swipeExitTransDistance;
    private float animatedCornerRadius;

    private MediaExplorerViewModel viewModel;
    private ViewPager2 pager;
    private final MediaExplorerAdapter adapter = new MediaExplorerAdapter();
    private CircleIndicator3 indicator;
    private MotionEvent swipeExitStart;
    private boolean isSwipeExitInProgress = false;
    private boolean isExiting = false;

    private boolean allowSaving;

    private final HashSet<PlayerView> playerViews = new HashSet<>();
    private final BgWorkers bgWorkers = BgWorkers.getInstance();

    public static Intent openMessageMedia(@NonNull Context context, Message message, int selectedIndex) {
        Intent intent = new Intent(context, MediaExplorerActivity.class);
        intent.putExtra(MediaExplorerActivity.EXTRA_MEDIA, MediaExplorerViewModel.MediaModel.fromMedia(message.media));
        intent.putExtra(MediaExplorerActivity.EXTRA_SELECTED, selectedIndex);
        intent.putExtra(MediaExplorerActivity.EXTRA_CONTENT_ID, message.id);
        intent.putExtra(MediaExplorerActivity.EXTRA_CHAT_ID, message.chatId);
        intent.putExtra(MediaExplorerActivity.EXTRA_ALLOW_SAVING, true);

        return intent;
    }

    final private Transition.TransitionListener transitionListener = new Transition.TransitionListener() {
        @Override
        public void onTransitionStart(Transition transition) {}

        @Override
        public void onTransitionEnd(Transition transition) {
            pager.post(() -> {
                viewModel.setInitializationInProgress(false);
                handlePlaybackOnPageChange(true);
                updatePlaybackControlsVisibility();
            });
        }

        @Override
        public void onTransitionCancel(Transition transition) { }
        @Override
        public void onTransitionPause(Transition transition) { }
        @Override
        public void onTransitionResume(Transition transition) { }
    };

    final private SharedElementCallback sharedElementCallback = new SharedElementCallback() {
        @Override
        public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
            MediaExplorerViewModel.MediaModel model = getCurrentItem();
            View mediaView = pager.findViewWithTag(model);

            if (mediaView != null) {
                sharedElements.put(mediaView.getTransitionName(), mediaView);
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) { // TODO: Fix crash
        super.onCreate(savedInstanceState);

        supportRequestWindowFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        supportRequestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        setEnterSharedElementCallback(sharedElementCallback);
        postponeEnterTransition();

        swipeExitStartThreshold = getResources().getDimensionPixelSize(R.dimen.swipe_exit_start_threshold);
        swipeExitFinishThreshold = getResources().getDimensionPixelSize(R.dimen.swipe_exit_finish_threshold);
        swipeExitTransDistance = getResources().getDimension(R.dimen.swipe_exit_transition_distance);
        animatedCornerRadius = getResources().getDimension(R.dimen.post_media_radius);

        if (Build.VERSION.SDK_INT >= 28) {
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }

        setContentView(R.layout.activity_media_explorer);
        Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Preconditions.checkNotNull(getSupportActionBar()).setHomeAsUpIndicator(R.drawable.ic_arrow_left_round);

        pager = findViewById(R.id.media_pager);
        pager.setAdapter(adapter);
        pager.setPageTransformer(new MarginPageTransformer(getResources().getDimensionPixelSize(R.dimen.explorer_pager_margin)));
        pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                viewModel.setPosition(position);

                handlePlaybackOnPageChange(false);
                updatePlaybackControlsVisibility();
                invalidateOptionsMenu();
            }
        });

        float indicatorRadius = getResources().getDimension(R.dimen.explorer_indicator_radius);
        indicator = findViewById(R.id.media_pager_indicator);
        indicator.setClipToOutline(true);
        indicator.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), indicatorRadius);
            }
        });

        findViewById(R.id.main).setOnClickListener(v -> toggleSystemUI());

        ArrayList<MediaExplorerViewModel.MediaModel> media = getIntent().getParcelableArrayListExtra(EXTRA_MEDIA);
        if (media == null || media.size() == 0) {
            finish();
            return;
        }

        allowSaving = getIntent().getBooleanExtra(EXTRA_ALLOW_SAVING, false);

        setupViewModel(getIntent().getParcelableExtra(EXTRA_CHAT_ID), media, getIntent().getIntExtra(EXTRA_SELECTED, 0));
    }

    private void setupViewModel(@Nullable ChatId chatId, @NonNull List<MediaExplorerViewModel.MediaModel> media, int selected) {
        MediaExplorerViewModel.Factory factory = new MediaExplorerViewModel.Factory(getApplication(), chatId, media, selected);
        viewModel = new ViewModelProvider(this, factory).get(MediaExplorerViewModel.class);

        viewModel.getMedia().observe(this, list -> {
            adapter.submitList(list, () -> {
                if (viewModel.isInitializationInProgress()) {
                    finishInitialization(chatId, media, selected);
                }
            });
        });
    }

    private void finishInitialization(@Nullable ChatId chatId, @NonNull List<MediaExplorerViewModel.MediaModel> media, int selected) {
        if (media.size() > 1 && chatId == null) {
            indicator.setVisibility(View.VISIBLE);
            indicator.setViewPager(pager);
        } else {
            indicator.setVisibility(View.GONE);
        }

        toggleSystemUI();

        if (chatId == null) {
            viewModel.setPosition(selected);
            pager.setCurrentItem(viewModel.getPosition(), false);
            finishEnterTransitionWhenReady();
        } else {
            bgWorkers.execute(() -> {
                int position = viewModel.getPositionInChat(media.get(selected).rowId);
                viewModel.setPosition(position);
                pager.post(() -> {
                    pager.setCurrentItem(position, false);
                    finishEnterTransitionWhenReady();
                });
            });
        }
    }

    @Nullable
    private Size computePlayerViewFinalSize(@NonNull PlayerView playerView) {
            if (playerView.getPlayer() instanceof SimpleExoPlayer) {
                SimpleExoPlayer player = (SimpleExoPlayer) playerView.getPlayer();
                Format format = player.getVideoFormat();

                if (format == null) {
                    Log.d("MediaExplorerActivity.computePlayerViewFinalSize: missing video format");
                    return null;
                }

                float fw, fh;
                if (format.rotationDegrees == 90 || format.rotationDegrees == 270) {
                    fw = format.height;
                    fh = format.width;
                } else {
                    fw = format.width;
                    fh = format.height;
                }

                float vw = playerView.getWidth();
                float vh = playerView.getHeight();
                float scale = Math.min(vw / fw, vh / fh);

                return new Size((int) (fw * scale), (int) (fh * scale));
            }

            Log.d("MediaExplorerActivity.computePlayerViewFinalSize: null player");
            return null;
    }

    @Nullable
    private Size computeImageViewFinalSize(@NonNull ImageView imageView) {
        Drawable drawable = imageView.getDrawable();

        if (drawable == null) {
            Log.d("MediaExplorerActivity.computeImageViewFinalSize: missing drawable");
            return null;
        }

        float dw = drawable.getIntrinsicWidth();
        float dh = drawable.getIntrinsicHeight();
        float vw = imageView.getWidth();
        float vh = imageView.getHeight();
        float scale = Math.min(vw / dw, vh / dh);

        return new Size((int) (dw * scale), (int) (dh * scale));
    }

    private void prepareViewForEnterTransition(@NonNull View view) {
        Size size;
        if (view instanceof PlayerView) {
            PlayerView playerView = (PlayerView) view;
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);

            size = computePlayerViewFinalSize(playerView);
        } else {
            ImageView imageView = (ImageView) view;
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

            size = computeImageViewFinalSize(imageView);
        }

        if (size != null) {
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            layoutParams.width = size.getWidth();
            layoutParams.height = size.getHeight();
        }

        AnimatedOutlineProvider outlineProvider = new AnimatedOutlineProvider(animatedCornerRadius);
        view.setOutlineProvider(outlineProvider);
        view.setClipToOutline(true);

        outlineProvider.animate(0, () -> {
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;

            view.setClipToOutline(false);

            if (view instanceof PlayerView) {
                PlayerView playerView = (PlayerView) view;
                playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
            } else {
                ImageView imageView = (ImageView) view;
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            }

            view.requestLayout();
        });
    }

    @MainThread
    private void finishEnterTransitionWhenReady() {
        pager.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                MediaExplorerViewModel.MediaModel model = getCurrentItem();
                View view = pager.findViewWithTag(model);

                if (view == null) {
                    return;
                }

                pager.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                String contentId = getIntent().getStringExtra(EXTRA_CONTENT_ID);
                int selected = getIntent().getIntExtra(EXTRA_SELECTED, 0);

                view.setTransitionName(MediaPagerAdapter.getTransitionName(contentId, selected));
                getWindow().getSharedElementEnterTransition().addListener(transitionListener);

                if (view instanceof PlayerView) {
                    PlayerView playerView = (PlayerView) view;
                    playerView.hideController();

                    SimpleExoPlayer player = (SimpleExoPlayer) playerView.getPlayer();
                    if (player != null) {
                        player.prepare();
                        player.addListener(new Player.EventListener() {
                            @Override
                            public void onPlaybackStateChanged(int state) {
                                if (state == Player.STATE_READY) {
                                    player.removeListener(this);

                                    prepareViewForEnterTransition(view);

                                    startPostponedEnterTransition();
                                }
                            }
                        });
                    } else {
                        startPostponedEnterTransition();
                    }
                } else {
                    ImageView imageView = (ImageView) view;

                    imageView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            if (imageView.getDrawable() == null) {
                                return;
                            }

                            imageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                            prepareViewForEnterTransition(view);

                            startPostponedEnterTransition();
                        }
                    });
                }
            }
        });
    }

    private MediaExplorerViewModel.MediaModel getCurrentItem() {
        final PagedList<MediaExplorerViewModel.MediaModel> pagedList = adapter.getCurrentList();
        int position = viewModel.getPosition();
        return pagedList == null || position >= pagedList.size() ? null : pagedList.get(position);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        return onTouchEventForSwipeExit(event) || super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!allowSaving) {
            return false;
        }
        getMenuInflater().inflate(R.menu.media_explorer, menu);
        return changeMenuVisibility(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (!allowSaving) {
            return false;
        }
        return changeMenuVisibility(menu);
    }

    private boolean changeMenuVisibility(@NonNull Menu menu) {
        final MenuItem saveToGalleryItem = menu.findItem(R.id.save_to_gallery);
        final MediaExplorerViewModel.MediaModel currentItem = getCurrentItem();
        if (saveToGalleryItem != null && currentItem != null) {
            saveToGalleryItem.setVisible(currentItem.canBeSavedToGallery());
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.save_to_gallery) {
            if (Build.VERSION.SDK_INT < 29) {
                if (!EasyPermissions.hasPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    EasyPermissions.requestPermissions(this, getString(R.string.save_to_gallery_storage_permission_rationale), REQUEST_EXTERNAL_STORAGE_PERMISSIONS, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    return true;
                }
            }
            saveCurrentItemToGallery();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
                    float distanceX = Math.abs(event.getX() - swipeExitStart.getX());
                    float distanceY = Math.abs(event.getY() - swipeExitStart.getY());

                    if (distanceY > swipeExitStartThreshold && distanceY > distanceX) {
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
        MediaExplorerViewModel.MediaModel model = getCurrentItem();

        if (model != null && model.type == Media.MEDIA_TYPE_IMAGE) {
            PhotoView view = pager.findViewWithTag(model);
            return Math.abs(view.getScale() - 1.0) < 0.2;
        }

        return true;
    }

    private void cancelSwipeExit() {
        if (swipeExitStart != null && isSwipeExitInProgress) {
            MediaExplorerViewModel.MediaModel model = getCurrentItem();
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

            MediaExplorerViewModel.MediaModel model = getCurrentItem();
            View view = pager.findViewWithTag(model);
            view.setTranslationX(distanceX);
            view.setTranslationY(distanceY);
            view.setScaleX(scale);
            view.setScaleY(scale);

            View main = findViewById(R.id.main);
            main.setBackgroundColor(Color.argb(alpha, 0, 0, 0));

            if (model != null && model.type == Media.MEDIA_TYPE_VIDEO) {
                ((PlayerView)view).hideController();
            }
        }
    }

    private void saveCurrentItemToGallery() {
        MediaExplorerViewModel.MediaModel current = getCurrentItem();
        viewModel.savePostToGallery(current).observe(this, success -> {
            if (success == null) {
                return;
            }
            if (success) {
                SnackbarHelper.showInfo(getWindow().getDecorView(), R.string.media_saved_to_gallery);
            } else {
                SnackbarHelper.showWarning(getWindow().getDecorView(), R.string.media_save_to_gallery_failed);
            }
        });
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

        for (PlayerView playerView : playerViews) {
            if (playerView != null) {
                if (isShown) {
                    playerView.showController();
                } else {
                    playerView.hideController();
                }
            }
        }
    }

    private void handlePlaybackOnPageChange(boolean seekToInitialTime) {
        if (viewModel.isInitializationInProgress()) {
            return;
        }

        stopPlayback();

        MediaExplorerViewModel.MediaModel model = getCurrentItem();
        if (model != null && model.type == Media.MEDIA_TYPE_VIDEO) {
            PlayerView playerView = pager.findViewWithTag(model);

            if (playerView != null) {
                SimpleExoPlayer player = (SimpleExoPlayer) playerView.getPlayer();

                if (player != null && !player.isPlaying()) {
                    if (seekToInitialTime) {
                        player.seekTo(getIntent().getLongExtra(EXTRA_INITIAL_TIME, 0));
                    }

                    player.setPlayWhenReady(true);
                }
            }
        }
    }

    private void stopPlayback() {
        for (PlayerView playerView : playerViews) {
            if (playerView != null) {
                Player player = playerView.getPlayer();

                if (player != null) {
                    player.setPlayWhenReady(false);
                }
            }
        }
    }

    private void releasePlayers() {
        for (PlayerView playerView : playerViews) {
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
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        if (requestCode == REQUEST_EXTERNAL_STORAGE_PERMISSIONS) {
            saveCurrentItemToGallery();
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (requestCode == REQUEST_EXTERNAL_STORAGE_PERMISSIONS) {
            if (EasyPermissions.permissionPermanentlyDenied(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                new AppSettingsDialog.Builder(this)
                        .setRationale(getString(R.string.save_to_gallery_storage_permission_rationale_denied))
                        .build().show();
            }
        }
    }


    @Override
    public void onBackPressed() {
        if (isExiting) {
            return;
        }
        isExiting = true;

        if (!isSystemUIShown()) {
            int options = getWindow().getDecorView().getSystemUiVisibility();
            options ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            getWindow().getDecorView().setSystemUiVisibility(options);
        }

        MediaExplorerViewModel.MediaModel model = getCurrentItem();

        bgWorkers.execute(() -> {
            String contentId;
            int selected;
            if (viewModel.isChat() && model != null) {
                contentId = viewModel.getContentIdInChat(model.rowId);
                selected = viewModel.getPositionInMessage(model.rowId);
            } else {
                contentId = getIntent().getStringExtra(EXTRA_CONTENT_ID);
                selected = viewModel.getPosition();
            }

            View view = pager.findViewWithTag(model);

            pager.post(() -> {
                if (view != null) {
                    if (model != null && model.type == Media.MEDIA_TYPE_VIDEO) {
                        ((PlayerView)view).hideController();
                    }

                    view.setTransitionName(MediaPagerAdapter.getTransitionName(contentId, selected));

                    AnimatedOutlineProvider outlineProvider = new AnimatedOutlineProvider(0);
                    view.setOutlineProvider(outlineProvider);
                    view.setClipToOutline(true);
                    outlineProvider.animate(animatedCornerRadius, null);
                }

                Intent intent = new Intent();
                intent.putExtra(EXTRA_CONTENT_ID, contentId);
                intent.putExtra(EXTRA_SELECTED, selected);
                setResult(RESULT_OK, intent);

                finishAfterTransition();
            });
        });
    }

    private void toggleSystemUI() {
        if (indicator.getVisibility() == View.VISIBLE) {
            indicator.animate().alpha(isSystemUIShown() ? 0.0f : 1.0f).start();
        }

        int options = getWindow().getDecorView().getSystemUiVisibility();

        options ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        options ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        options ^= View.SYSTEM_UI_FLAG_FULLSCREEN;

        options |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        options |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;

        getWindow().getDecorView().setSystemUiVisibility(options);
    }

    private boolean isSystemUIShown() {
        return (getWindow().getDecorView().getSystemUiVisibility() & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0;
    }

    private abstract static class DefaultHolder extends RecyclerView.ViewHolder {

        public DefaultHolder(@NonNull View itemView) {
            super(itemView);
        }

        public abstract void bindTo(@NonNull MediaExplorerViewModel.MediaModel model, int position);
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
        public void bindTo(@NonNull MediaExplorerViewModel.MediaModel model, int position) {
            imageView.setTag(model);

            bgWorkers.execute(() -> {
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

            GestureDetector doubleTapGestureDetector = new GestureDetector(playerView.getContext(), new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    Player player = playerView.getPlayer();
                    if (player != null) {
                        player.setPlayWhenReady(!player.isPlaying());
                    }

                    return true;
                }
            });

            playerView.setOnTouchListener((v, event) -> doubleTapGestureDetector.onTouchEvent(event));

            if (isSystemUIShown()) {
                playerView.showController();
            } else {
                playerView.hideController();
            }

            playerViews.add(playerView);
        }

        @Override
        public void bindTo(@NonNull MediaExplorerViewModel.MediaModel model, int position) {
            playerView.setTag(model);

            final DataSource.Factory dataSourceFactory;
            final MediaItem mediaItem;
            Log.d("MediaExplorerActivity.bindTo model = " + model);
            if (model.isStreamingVideo()) {
                final ChunkedMediaParameters chunkedParameters;
                try {
                    chunkedParameters = ChunkedMediaParameters.computeFromBlobSize(model.blobSize, model.chunkSize);
                } catch (ChunkedMediaParametersException e) {
                    Log.e("MediaExplorerActivity.bindTo", e);
                    return;
                }
                if (!Objects.equals(model.uri.getScheme(), "file")) {
                    Log.e("MediaExplorerActivity.bindTo attempt to stream video with no local cache file");
                    return;
                }
                dataSourceFactory = ExoUtils.getChunkedMediaDataSourceFactory(model.rowId, model.url, chunkedParameters, new File(model.uri.getPath()));
                mediaItem = ExoUtils.getChunkedMediaItem(model.rowId, model.url);
            } else {
                dataSourceFactory = ExoUtils.getDefaultDataSourceFactory(playerView.getContext());
                mediaItem = ExoUtils.getUriMediaItem(model.uri);
            }
            final MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem);

            if (playerView.getPlayer() != null) {
                Player player = playerView.getPlayer();
                playerView.setPlayer(null);

                player.stop();
                player.release();
            }

            SimpleExoPlayer player = new SimpleExoPlayer.Builder(playerView.getContext()).build();
            player.addListener(new Player.EventListener() {
                @Override
                public void onIsPlayingChanged(boolean isPlaying) {
                    playerView.setKeepScreenOn(isPlaying);
                }
            });
            playerView.setPlayer(player);

            player.setRepeatMode(Player.REPEAT_MODE_ALL);
            player.setMediaSource(mediaSource);
            player.prepare();
        }
    }

    private static class UnknownHolder extends DefaultHolder {

        public UnknownHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        public void bindTo(@NonNull MediaExplorerViewModel.MediaModel model, int position) {
        }
    }

    private class MediaExplorerAdapter extends PagedListAdapter<MediaExplorerViewModel.MediaModel, DefaultHolder> {

        MediaExplorerAdapter() {
            super(new DiffUtil.ItemCallback<MediaExplorerViewModel.MediaModel>() {
                @Override
                public boolean areItemsTheSame(@NonNull MediaExplorerViewModel.MediaModel oldItem, @NonNull MediaExplorerViewModel.MediaModel newItem) {
                    return oldItem.rowId == newItem.rowId && oldItem.uri.equals(newItem.uri);
                }

                @Override
                public boolean areContentsTheSame(@NonNull MediaExplorerViewModel.MediaModel oldItem, @NonNull MediaExplorerViewModel.MediaModel newItem) {
                    return oldItem.uri.equals(newItem.uri);
                }
            });
        }

        @Override
        public int getItemViewType(int position) {
            MediaExplorerViewModel.MediaModel model = getItem(position);
            return model != null ? model.type : Media.MEDIA_TYPE_UNKNOWN;
        }

        @NonNull
        @Override
        public DefaultHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            switch (viewType) {
                case Media.MEDIA_TYPE_IMAGE:
                    return new ImageHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.media_explorer_image, parent, false));
                case Media.MEDIA_TYPE_VIDEO:
                    return new VideoHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.media_explorer_video, parent, false));
                default:
                    View view = new View(parent.getContext());
                    view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    return new UnknownHolder(view);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull DefaultHolder holder, int position) {
            MediaExplorerViewModel.MediaModel model = getItem(position);

            if (model != null) {
                holder.bindTo(model, position);
            }
        }
    }

    private static class AnimatedOutlineProvider extends ViewOutlineProvider {

        private static final int ANIMATION_DURATION_MS = 300;

        private float cornerRadius;

        public AnimatedOutlineProvider(float cornerRadius) {
            this.cornerRadius = cornerRadius;
        }

        @Keep
        public void setCornerRadius(float cornerRadius) {
            this.cornerRadius = cornerRadius;
        }

        @Keep
        public float getCornerRadius() {
            return cornerRadius;
        }

        public void animate(float finalCornerRadius, @Nullable Runnable onComplete) {
            ObjectAnimator animator = ObjectAnimator.ofFloat(this, "cornerRadius", finalCornerRadius);
            animator.setDuration(ANIMATION_DURATION_MS);
            animator.start();

            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (onComplete != null) {
                        onComplete.run();
                    }
                }
            });
        }

        @Override
        public void getOutline(View view, Outline outline) {
            outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), cornerRadius);
        }
    }
}
