package com.halloapp.widget;

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

import com.halloapp.R;
import com.halloapp.content.Media;
import com.halloapp.media.MediaThumbnailLoader;

import java.util.ArrayList;
import java.util.List;

public class AlbumMediaGridView extends ConstraintLayout {

    private int margin;

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

    private void init(@NonNull Context context) {
        inflate(context, R.layout.view_album_grid, this);

        album1 = findViewById(R.id.album_1);
        album2 = findViewById(R.id.album_2);
        album3 = findViewById(R.id.album_3);
        album4 = findViewById(R.id.album_4);

        overflowCount = findViewById(R.id.overflow_count);
        overflowCover = findViewById(R.id.overflow_cover);

        margin = getResources().getDimensionPixelSize(R.dimen.album_item_margin);
    }

    public void bindMedia(MediaThumbnailLoader loader, List<Media> media) {
        int numItems = media.size();
        album1.bind(loader, numItems > 0 ? media.get(0) : null);
        album2.bind(loader, numItems > 1 ? media.get(1) : null);
        album3.bind(loader, numItems > 2 ? media.get(2) : null);
        album4.bind(loader, numItems > 3 ? media.get(3) : null);
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
            overflowCount.setText(getResources().getString(R.string.album_count, numItems - 4));
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



    public static class AlbumItemView extends FrameLayout {

        private ImageView mediaView;
        private View videoIndicator;

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
            setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), getResources().getDimension(R.dimen.album_media_corner_radius));
                }
            });
            setClipToOutline(true);
        }

        public void bind(MediaThumbnailLoader loader, Media media) {
            if (media == null) {
                loader.cancel(mediaView);
            } else {
                loader.load(mediaView, media);
                videoIndicator.setVisibility(media.type == Media.MEDIA_TYPE_VIDEO ? View.VISIBLE : View.GONE);
            }
        }
    }
}
