package com.halloapp.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.Outline;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;

import com.halloapp.R;
import com.halloapp.content.Media;
import com.halloapp.content.Message;
import com.halloapp.media.MediaThumbnailLoader;
import com.halloapp.ui.MediaPagerAdapter;
import com.halloapp.util.TransitionUtils;

import java.util.ArrayList;
import java.util.List;

public class AlbumMediaGridView extends ConstraintLayout {

    private int margin;

    public interface OnMediaClickListener {
        void onMediaClick(Media media, int index);
    }

    public AlbumMediaGridView(@NonNull Context context) {
        this(context, null);
    }

    public AlbumMediaGridView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AlbumMediaGridView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private AlbumItemView album1;
    private AlbumItemView album2;
    private AlbumItemView album3;
    private AlbumItemView album4;

    private View overflowCover;
    private TextView overflowCount;

    private OnMediaClickListener onMediaClickListener;

    public ActivityOptionsCompat createActivityTransition(Activity activity) {
        List<Pair<View, String>> transitionViews = new ArrayList<>();
        addTransitionPairIfVisible(transitionViews, album1);
        addTransitionPairIfVisible(transitionViews, album2);
        addTransitionPairIfVisible(transitionViews, album3);
        addTransitionPairIfVisible(transitionViews, album4);

        TransitionUtils.transitionSystemViews(activity, transitionViews);

        return ActivityOptionsCompat.makeSceneTransitionAnimation(activity, transitionViews.toArray(new Pair[0]));
    }

    private void addTransitionPairIfVisible(@NonNull List<Pair<View, String>> list, AlbumItemView view) {
        if (view.getVisibility() == View.VISIBLE) {
            list.add(new Pair<>(view.mediaView, view.mediaView.getTransitionName()));
        }
    }

    private void init(@NonNull Context context) {
        inflate(context, R.layout.view_album_grid, this);

        album1 = findViewById(R.id.album_1);
        album2 = findViewById(R.id.album_2);
        album3 = findViewById(R.id.album_3);
        album4 = findViewById(R.id.album_4);

        overflowCount = findViewById(R.id.overflow_count);
        overflowCover = findViewById(R.id.overflow_cover);

        margin = getResources().getDimensionPixelSize(R.dimen.album_item_margin);

        album1.setOnClickListener(v -> {
            if (onMediaClickListener != null) {
                onMediaClickListener.onMediaClick(album1.media, 0);
            }
        });
        album2.setOnClickListener(v -> {
            if (onMediaClickListener != null) {
                onMediaClickListener.onMediaClick(album2.media, 1);
            }
        });
        album3.setOnClickListener(v -> {
            if (onMediaClickListener != null) {
                onMediaClickListener.onMediaClick(album3.media, 2);
            }
        });
        album4.setOnClickListener(v -> {
            if (onMediaClickListener != null) {
                onMediaClickListener.onMediaClick(album4.media, 3);
            }
        });
    }

    public void bindMedia(MediaThumbnailLoader loader, Message message) {
        List<Media> media = message.media;
        int numItems = media.size();
        album1.bind(loader, message, 0);
        album2.bind(loader, message, 1);
        album3.bind(loader, message, 2);
        album4.bind(loader, message, 3);
        switch (numItems) {
            case 1:
                layoutOneItem();
                break;
            case 2:
                layoutTwoItems();
                break;
            case 3:
                layoutThreeItems();
                break;
            default:
                layoutFourItems();
                break;
        }
        if (numItems <= 4) {
            overflowCount.setVisibility(View.GONE);
            overflowCover.setVisibility(View.GONE);
        } else {
            overflowCount.setVisibility(View.VISIBLE);
            overflowCover.setVisibility(View.VISIBLE);
            // Only subtract 3 as we dont count the obscured one as "shown"
            overflowCount.setText(getResources().getString(R.string.album_count, numItems - 3));
        }
    }

    private void layoutOneItem() {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(this);
        constraintSet.connect(R.id.album_1,ConstraintSet.START,ConstraintSet.PARENT_ID,ConstraintSet.START,margin);
        constraintSet.connect(R.id.album_1,ConstraintSet.END,ConstraintSet.PARENT_ID,ConstraintSet.END,margin);
        constraintSet.connect(R.id.album_1,ConstraintSet.TOP,ConstraintSet.PARENT_ID,ConstraintSet.TOP,margin);
        constraintSet.connect(R.id.album_1,ConstraintSet.BOTTOM,ConstraintSet.PARENT_ID,ConstraintSet.BOTTOM,margin);
        constraintSet.applyTo(this);

        album2.setVisibility(View.GONE);
        album3.setVisibility(View.GONE);
        album4.setVisibility(View.GONE);
    }

    private void layoutTwoItems() {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(this);
        constraintSet.connect(R.id.album_1,ConstraintSet.START,ConstraintSet.PARENT_ID,ConstraintSet.START, margin);
        constraintSet.connect(R.id.album_1,ConstraintSet.END,R.id.album_2,ConstraintSet.START,margin);
        constraintSet.connect(R.id.album_1,ConstraintSet.TOP,ConstraintSet.PARENT_ID,ConstraintSet.TOP,margin);
        constraintSet.connect(R.id.album_1,ConstraintSet.BOTTOM,ConstraintSet.PARENT_ID,ConstraintSet.BOTTOM,margin);

        constraintSet.connect(R.id.album_2,ConstraintSet.START,R.id.album_1,ConstraintSet.END,margin);
        constraintSet.connect(R.id.album_2,ConstraintSet.END,ConstraintSet.PARENT_ID,ConstraintSet.END, margin);
        constraintSet.connect(R.id.album_2,ConstraintSet.TOP,ConstraintSet.PARENT_ID,ConstraintSet.TOP,margin);
        constraintSet.connect(R.id.album_2,ConstraintSet.BOTTOM,ConstraintSet.PARENT_ID,ConstraintSet.BOTTOM,margin);
        constraintSet.applyTo(this);

        album2.setVisibility(View.VISIBLE);
        album3.setVisibility(View.GONE);
        album4.setVisibility(View.GONE);
    }

    private void layoutThreeItems() {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(this);
        constraintSet.connect(R.id.album_1,ConstraintSet.START,ConstraintSet.PARENT_ID,ConstraintSet.START, margin);
        constraintSet.connect(R.id.album_1,ConstraintSet.END,R.id.album_2,ConstraintSet.START,margin);
        constraintSet.connect(R.id.album_1,ConstraintSet.TOP,ConstraintSet.PARENT_ID,ConstraintSet.TOP,margin);
        constraintSet.connect(R.id.album_1,ConstraintSet.BOTTOM,R.id.album_3,ConstraintSet.TOP,margin);

        constraintSet.connect(R.id.album_2,ConstraintSet.START,R.id.album_1,ConstraintSet.END,margin);
        constraintSet.connect(R.id.album_2,ConstraintSet.END,ConstraintSet.PARENT_ID,ConstraintSet.END, margin);
        constraintSet.connect(R.id.album_2,ConstraintSet.TOP,ConstraintSet.PARENT_ID,ConstraintSet.TOP,margin);
        constraintSet.connect(R.id.album_2,ConstraintSet.BOTTOM,R.id.album_1,ConstraintSet.BOTTOM,0);

        constraintSet.connect(R.id.album_3,ConstraintSet.START,ConstraintSet.PARENT_ID,ConstraintSet.START,margin);
        constraintSet.connect(R.id.album_3,ConstraintSet.END,ConstraintSet.PARENT_ID,ConstraintSet.END, margin);
        constraintSet.connect(R.id.album_3,ConstraintSet.TOP,R.id.album_1,ConstraintSet.BOTTOM,margin);
        constraintSet.connect(R.id.album_3,ConstraintSet.BOTTOM,ConstraintSet.PARENT_ID,ConstraintSet.BOTTOM,margin);

        constraintSet.applyTo(this);

        album2.setVisibility(View.VISIBLE);
        album3.setVisibility(View.VISIBLE);
        album4.setVisibility(View.GONE);
    }

    private void layoutFourItems() {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(this);
        constraintSet.connect(R.id.album_1,ConstraintSet.START,ConstraintSet.PARENT_ID,ConstraintSet.START, margin);
        constraintSet.connect(R.id.album_1,ConstraintSet.END,R.id.album_2,ConstraintSet.START,margin);
        constraintSet.connect(R.id.album_1,ConstraintSet.TOP,ConstraintSet.PARENT_ID,ConstraintSet.TOP,margin);
        constraintSet.connect(R.id.album_1,ConstraintSet.BOTTOM,R.id.album_3,ConstraintSet.TOP,margin);

        constraintSet.connect(R.id.album_2,ConstraintSet.START,R.id.album_1,ConstraintSet.END,margin);
        constraintSet.connect(R.id.album_2,ConstraintSet.END,ConstraintSet.PARENT_ID,ConstraintSet.END, margin);
        constraintSet.connect(R.id.album_2,ConstraintSet.TOP,ConstraintSet.PARENT_ID,ConstraintSet.TOP,margin);
        constraintSet.connect(R.id.album_2,ConstraintSet.BOTTOM,R.id.album_4,ConstraintSet.TOP,margin);

        constraintSet.connect(R.id.album_3,ConstraintSet.START,ConstraintSet.PARENT_ID,ConstraintSet.START, margin);
        constraintSet.connect(R.id.album_3,ConstraintSet.END,R.id.album_4,ConstraintSet.START,margin);
        constraintSet.connect(R.id.album_3,ConstraintSet.TOP, R.id.album_1,ConstraintSet.BOTTOM,margin);
        constraintSet.connect(R.id.album_3,ConstraintSet.BOTTOM,ConstraintSet.PARENT_ID,ConstraintSet.BOTTOM,margin);

        constraintSet.connect(R.id.album_4,ConstraintSet.START,R.id.album_3,ConstraintSet.END,margin);
        constraintSet.connect(R.id.album_4,ConstraintSet.END,ConstraintSet.PARENT_ID,ConstraintSet.END, margin);
        constraintSet.connect(R.id.album_4,ConstraintSet.TOP, R.id.album_2,ConstraintSet.BOTTOM,margin);
        constraintSet.connect(R.id.album_4,ConstraintSet.BOTTOM,ConstraintSet.PARENT_ID,ConstraintSet.BOTTOM,margin);

        constraintSet.applyTo(this);

        album2.setVisibility(View.VISIBLE);
        album3.setVisibility(View.VISIBLE);
        album4.setVisibility(View.VISIBLE);
    }

    public void setOnMediaClickerListener(@Nullable OnMediaClickListener listener) {
        this.onMediaClickListener = listener;
    }

    public static class AlbumItemView extends FrameLayout {

        private ImageView mediaView;
        private View videoIndicator;
        private View loadingView;

        private Media media;

        public AlbumItemView(@NonNull Context context) {
            this(context, null);
        }

        public AlbumItemView(@NonNull Context context, @Nullable AttributeSet attrs) {
            this(context, attrs, 0);
        }

        public AlbumItemView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            init(context);
        }

        private void init(@NonNull Context context) {
            inflate(context, R.layout.view_album_item, this);

            mediaView = findViewById(R.id.media);
            videoIndicator = findViewById(R.id.video_indicator);
            loadingView = findViewById(R.id.loading);
            setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), getResources().getDimension(R.dimen.album_media_corner_radius));
                }
            });
            setClipToOutline(true);
        }

        public void bind(MediaThumbnailLoader loader, Message message, int mediaIndex) {
            this.media = mediaIndex < message.media.size() ? message.media.get(mediaIndex) : null;
            if (media == null) {
                loader.cancel(mediaView);
                loadingView.setVisibility(View.VISIBLE);
            } else {
                loader.load(mediaView, media);
                if (media.transferred != Media.TRANSFERRED_YES && media.transferred != Media.TRANSFERRED_PARTIAL_CHUNKED) {
                    loadingView.setVisibility(View.VISIBLE);
                    mediaView.setVisibility(View.GONE);
                } else {
                    loadingView.setVisibility(View.GONE);
                    mediaView.setVisibility(View.VISIBLE);
                }
                videoIndicator.setVisibility(media.type == Media.MEDIA_TYPE_VIDEO ? View.VISIBLE : View.GONE);
                mediaView.setTransitionName(MediaPagerAdapter.getTransitionName(message.id, mediaIndex));
            }
        }
    }
}
