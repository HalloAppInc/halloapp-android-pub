package com.halloapp.ui.posts;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;

import androidx.appcompat.view.ContextThemeWrapper;

import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.halloapp.Constants;
import com.halloapp.Me;
import com.halloapp.R;
import com.halloapp.content.Media;
import com.halloapp.content.Post;
import com.halloapp.media.AudioDurationLoader;
import com.halloapp.media.MediaThumbnailLoader;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.ui.mentions.TextContentLoader;
import com.halloapp.util.BitmapUtils;
import com.halloapp.util.TimeFormatter;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.AspectRatioFrameLayout;
import com.halloapp.widget.LimitingTextView;
import com.halloapp.widget.PostLinkPreviewView;
import com.halloapp.widget.PostScreenshotPhotoView;

import java.util.ArrayList;
import java.util.List;

import me.relex.circleindicator.CircleIndicator3;

public class PostScreenshotGenerator {

    private final TextContentLoader textContentLoader;
    private final AudioDurationLoader audioDurationLoader;
    private final MediaThumbnailLoader mediaThumbnailLoader;

    private final int indicatorSize;
    private final int indicatorMargin;

    private final ContextThemeWrapper wrappedContext;

    private PostScreenshotGenerator(@NonNull Context context) {
        textContentLoader = new TextContentLoader();
        audioDurationLoader = new AudioDurationLoader(context);
        mediaThumbnailLoader = new MediaThumbnailLoader(context, Constants.MAX_IMAGE_DIMENSION);

        textContentLoader.forceSyncLoad();
        audioDurationLoader.forceSyncLoad();
        mediaThumbnailLoader.forceSyncLoad();

        wrappedContext = new ContextThemeWrapper(context, R.style.AppTheme);
        Configuration configuration = new Configuration(context.getResources().getConfiguration());
        configuration.uiMode &= ~Configuration.UI_MODE_NIGHT_MASK;
        configuration.uiMode |= Configuration.UI_MODE_NIGHT_NO;
        configuration.densityDpi = 320;
        configuration.fontScale = 1f;
        wrappedContext.applyOverrideConfiguration(configuration);
        DisplayMetrics displayMetrics = wrappedContext.getResources().getDisplayMetrics();
        displayMetrics.densityDpi = 320;

        indicatorSize = wrappedContext.getResources().getDimensionPixelSize(R.dimen.media_indicator_size);
        indicatorMargin = wrappedContext.getResources().getDimensionPixelSize(R.dimen.media_indicator_margin);
    }

    private void destroy() {
        textContentLoader.destroy();
        audioDurationLoader.destroy();
        mediaThumbnailLoader.destroy();
    }

    public static Bitmap generateScreenshot(@NonNull Context context, @NonNull Post post) {
        PostScreenshotGenerator generator = new PostScreenshotGenerator(context);
        Bitmap bitmap = generator.generateScreenshotForPost(post);
        generator.destroy();
        return bitmap;
    }

    public static Pair<Bitmap,Bitmap> generateScreenshotWithBackground(@NonNull Context context, @NonNull Post post) {
        PostScreenshotGenerator generator = new PostScreenshotGenerator(context);
        Pair<Bitmap,Bitmap> bitmaps = generator.generateScreenshotForInstagramStories(post);
        generator.destroy();
        return bitmaps;
    }

    private Bitmap generateScreenshotForPost(@NonNull Post post) {
        int width = wrappedContext.getResources().getDimensionPixelSize(R.dimen.post_screenshot_only_width);

        View v = buildViewForPost(post, R.layout.view_post_screenshot);
        v.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY), View.MeasureSpec.UNSPECIFIED);
        v.layout(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());

        v.setDrawingCacheEnabled(true);
        v.buildDrawingCache();

        return v.getDrawingCache();
    }

    private Pair<Bitmap, Bitmap> generateScreenshotForInstagramStories(@NonNull Post post) {
        int width = wrappedContext.getResources().getDimensionPixelSize(R.dimen.post_screenshot_stories_width);
        int height = wrappedContext.getResources().getDimensionPixelSize(R.dimen.post_screenshot_stories_height);

        ConstraintLayout v = (ConstraintLayout) buildViewForPost(post, R.layout.view_external_share_preview);
        View footer = v.findViewById(R.id.share_footer);
        View cardView = v.findViewById(R.id.card_view);
        v.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY));
        v.layout(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());
        int maxMarginBottom = wrappedContext.getResources().getDimensionPixelSize(R.dimen.post_screenshot_footer_max_margin_bottom);

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) footer.getLayoutParams();
        params.bottomMargin = Math.min(height - footer.getBottom(), maxMarginBottom);
        params.topToBottom = -1;
        footer.setLayoutParams(params);

        v.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY));
        v.layout(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());
        cardView.setDrawingCacheEnabled(true);
        cardView.buildDrawingCache();
        Bitmap postCard = cardView.getDrawingCache();
        v.removeView(cardView);

        v.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY));
        v.layout(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());

        v.setDrawingCacheEnabled(true);
        v.buildDrawingCache();
        return new Pair<>(v.getDrawingCache(), postCard);
    }

    private View buildViewForPost(@NonNull Post post, @LayoutRes int layoutRes) {
        View root = LayoutInflater.from(wrappedContext).inflate(layoutRes, null, false);

        PostAttributionLayout header = root.findViewById(R.id.post_header);
        header.getNameView().setText(Me.getInstance().getName());

        TextView timeView = root.findViewById(R.id.time);
        timeView.setText(TimeFormatter.formatMessageTime(wrappedContext, post.timestamp));

        ImageView avatarView = root.findViewById(R.id.avatar);
        avatarView.setImageBitmap(BitmapUtils.cropCircle(AvatarLoader.getInstance().getAvatar(wrappedContext, post.senderUserId)));

        switch (post.type) {
            case Post.TYPE_USER:
                if (post.media.isEmpty()) {
                    return buildTextPostView(wrappedContext, root, post);
                } else {
                    return buildMediaPostView(wrappedContext, root, post);
                }
            case Post.TYPE_VOICE_NOTE:
                return buildVoicePostView(wrappedContext, root, post);
        }
        Log.e("PostScreenshotGenerator invalid post to screenshot");
        return null;
    }

    private View buildTextPostView(@NonNull Context context, @NonNull View root, @NonNull Post post) {
        ViewGroup content = root.findViewById(R.id.post_content);
        LayoutInflater.from(context).inflate(R.layout.post_item_text, content);

        LimitingTextView textView = content.findViewById(R.id.text);
        PostLinkPreviewView linkPreviewView = content.findViewById(R.id.link_preview);

        linkPreviewView.setMediaThumbnailLoader(mediaThumbnailLoader);

        List<Media> postMedia = post.getMedia();

        textView.setLineLimit(postMedia.isEmpty() ? Constants.TEXT_POST_LINE_LIMIT : Constants.MEDIA_POST_LINE_LIMIT);
        textView.setLineLimitTolerance(0);
        if (post.text != null) {
            textContentLoader.load(textView, post);
        } else {
            textView.setText("");
        }

        textView.setVisibility(View.VISIBLE);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textView.getContext().getResources().getDimension(
                (post.text.length() < 180 && postMedia.isEmpty()) ? R.dimen.post_text_size_large : R.dimen.post_text_size));

        linkPreviewView.updateUrlPreview(post.urlPreview);

        return root;
    }

    private void replaceIndicatorView(@NonNull CircleIndicator3 circleIndicatorView, @NonNull ViewGroup parent, int numItems) {
        LinearLayoutCompat replacement = new LinearLayoutCompat(circleIndicatorView.getContext());
        replacement.setId(circleIndicatorView.getId());
        replacement.setGravity(Gravity.CENTER_VERTICAL);
        replacement.setLayoutParams(circleIndicatorView.getLayoutParams());
        replacement.setOrientation(LinearLayoutCompat.HORIZONTAL);

        for (int i = 0; i < numItems; i++) {
            View indicator = new View(replacement.getContext());
            final LinearLayout.LayoutParams params = new LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.WRAP_CONTENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT);
            params.width = indicatorSize;
            params.height = indicatorSize;
            params.leftMargin = indicatorMargin;
            params.rightMargin = indicatorMargin;
            indicator.setBackgroundResource(R.drawable.pager_item_indicator_selected);
            if (i == 0) {
                indicator.setAlpha(0.7f);
                indicator.setScaleY(1.2f);
                indicator.setScaleX(1.2f);
            } else {
                indicator.setAlpha(0.2f);
                indicator.setScaleY(1f);
                indicator.setScaleX(1f);
            }
            replacement.addView(indicator, params);
        }

        final int index = parent.indexOfChild(circleIndicatorView);
        parent.removeView(circleIndicatorView);
        parent.addView(replacement, index);
    }

    private View buildVoicePostView(@NonNull Context context, @NonNull View root, @NonNull Post post) {
        ViewGroup content = root.findViewById(R.id.post_content);
        LayoutInflater.from(context).inflate(R.layout.post_item_voice_note, content);
        ViewPager2 mediaPagerView = content.findViewById(R.id.media_pager);
        TextView durationView = content.findViewById(R.id.seek_time);
        AppCompatSeekBar seekBar = content.findViewById(R.id.voice_note_seekbar);
        CircleIndicator3 mediaPagerIndicator = content.findViewById(R.id.media_pager_indicator);

        @ColorInt int color = ContextCompat.getColor(wrappedContext, R.color.color_secondary);
        seekBar.setThumb(ContextCompat.getDrawable(wrappedContext, R.drawable.screenshot_thumb));
        seekBar.getThumb().setColorFilter(color, PorterDuff.Mode.SRC_IN);
        List<Media> postMedia = post.getMedia();
        if (post.media.size() > 1)  {
            mediaPagerView.setVisibility(View.VISIBLE);
        } else {
            mediaPagerIndicator.setVisibility(View.GONE);
            mediaPagerView.setVisibility(View.GONE);
        }
        if (!postMedia.isEmpty()) {
            MediaPagerAdapter mediaPagerAdapter = new MediaPagerAdapter(context.getResources().getDimensionPixelSize(R.dimen.post_screenshot_max_media_height));
            mediaPagerView.setOffscreenPageLimit(1);
            mediaPagerView.setAdapter(mediaPagerAdapter);
            mediaPagerAdapter.setMedia(postMedia);
            mediaPagerAdapter.setContentId(post.id);
            final int defaultMediaInset = mediaPagerView.getResources().getDimensionPixelSize(R.dimen.media_pager_child_padding);
            if (postMedia.size() > 1) {
                replaceIndicatorView(mediaPagerIndicator, content, postMedia.size());
                mediaPagerAdapter.setMediaInset(defaultMediaInset, defaultMediaInset, defaultMediaInset, defaultMediaInset);
            } else {
                mediaPagerAdapter.setMediaInset(defaultMediaInset, defaultMediaInset, defaultMediaInset, 0);
                mediaPagerIndicator.setVisibility(View.GONE);
            }
            mediaPagerView.setCurrentItem(0, false);
            mediaPagerView.setNestedScrollingEnabled(false);
        }

        if (!post.media.isEmpty()) {
            Media media = post.media.get(0);
            if (media.file != null) {
                audioDurationLoader.load(durationView, media);
            }
        }

        return root;
    }

    private View buildMediaPostView(@NonNull Context context, @NonNull View root, @NonNull Post post) {
        ViewGroup content = root.findViewById(R.id.post_content);
        LayoutInflater.from(context).inflate(R.layout.post_item_media, content);

        ViewPager2 mediaPagerView = content.findViewById(R.id.media_pager);
        LimitingTextView textView = content.findViewById(R.id.text);
        CircleIndicator3 mediaPagerIndicator = content.findViewById(R.id.media_pager_indicator);


        List<Media> postMedia = post.getMedia();
        if (!postMedia.isEmpty()) {
            MediaPagerAdapter mediaPagerAdapter = new MediaPagerAdapter(context.getResources().getDimensionPixelSize(R.dimen.post_screenshot_max_media_height));
            mediaPagerView.setOffscreenPageLimit(1);
            mediaPagerView.setAdapter(mediaPagerAdapter);
            mediaPagerAdapter.setMedia(postMedia);
            mediaPagerAdapter.setContentId(post.id);
            final int defaultMediaInset = mediaPagerView.getResources().getDimensionPixelSize(R.dimen.media_pager_child_padding);
            if (postMedia.size() > 1) {
                replaceIndicatorView(mediaPagerIndicator, content, postMedia.size());
                mediaPagerAdapter.setMediaInset(defaultMediaInset, defaultMediaInset, defaultMediaInset, defaultMediaInset);
            } else {
                mediaPagerAdapter.setMediaInset(defaultMediaInset, defaultMediaInset, defaultMediaInset, 0);
                mediaPagerIndicator.setVisibility(View.GONE);
            }
            mediaPagerView.setCurrentItem(0, false);
            mediaPagerView.setNestedScrollingEnabled(false);
        }
        final boolean noCaption = TextUtils.isEmpty(post.text);

        textView.setLineLimit(postMedia.isEmpty() ? Constants.TEXT_POST_LINE_LIMIT : Constants.MEDIA_POST_LINE_LIMIT);
        textView.setLineLimitTolerance(0);
        if (post.text != null) {
            textContentLoader.load(textView, post);
        } else {
            textView.setText("");
        }
        if (noCaption) {
            textView.setVisibility(View.GONE);
        } else {
            textView.setVisibility(View.VISIBLE);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textView.getContext().getResources().getDimension(
                    (post.text.length() < 180 && postMedia.isEmpty()) ? R.dimen.post_text_size_large : R.dimen.post_text_size));
        }
        return root;
    }

    private class MediaPagerAdapter extends RecyclerView.Adapter<MediaViewHolder> {
        private ArrayList<Media> media;
        private String contentId;

        private boolean overrideMediaPadding = false;
        private int mediaInsetLeft;
        private int mediaInsetRight;
        private int mediaInsetBottom;
        private int mediaInsetTop;

        private float fixedAspectRatio;
        private final int maxHeight;

        public MediaPagerAdapter(int maxHeight) {
            this.maxHeight = maxHeight;
        }

        public void setMedia(@NonNull List<Media> media) {
            Log.d("MediaPagerAdapter.setMedia");
            if (this.media == null || !this.media.equals(media)) {
                this.media = new ArrayList<>(media);
                fixedAspectRatio = Media.getMaxAspectRatio(media);

                notifyDataSetChanged();
            }
        }

        public void setMediaInset(int leftInsetPx, int topInsetPx, int rightInsetPx, int bottomInsetPx) {
            overrideMediaPadding = true;
            this.mediaInsetLeft = leftInsetPx;
            this.mediaInsetRight = rightInsetPx;
            this.mediaInsetBottom = bottomInsetPx;
            this.mediaInsetTop = topInsetPx;
            notifyDataSetChanged();
        }

        public void setContentId(@NonNull String contentId) {
            this.contentId = contentId;
        }

        public String getContentId() {
            return contentId;
        }

        @NonNull
        @Override
        public MediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Log.d("MediaPagerAdapter.onCreateViewHolder");
            return new MediaViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.screenshot_media_pager_item, parent, false));
        }

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public void onBindViewHolder(@NonNull MediaViewHolder holder, int position) {
            Log.d("MediaPagerAdapter.onBindViewHolder");
            holder.imageView.setTransitionName("");
            holder.imageView.setVisibility(View.GONE);

            if (overrideMediaPadding) {
                holder.itemView.setPadding(mediaInsetLeft, mediaInsetTop, mediaInsetRight, mediaInsetBottom);
            }
            final Media mediaItem = media.get(position);
            holder.mediaItem = mediaItem;
            holder.container.setTag(mediaItem);
            holder.container.setAspectRatio(fixedAspectRatio);

            holder.container.setMaxHeight((int) (maxHeight));

            holder.imageView.setOnClickListener(null);
            holder.imageView.setVisibility(View.VISIBLE);

            mediaThumbnailLoader.load(holder.imageView, mediaItem);

            if (mediaItem.type == Media.MEDIA_TYPE_VIDEO) {
                holder.playButton.setVisibility(View.VISIBLE);
            } else {
                holder.playButton.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return media == null ? 0 : media.size();
        }
    }

    private static class MediaViewHolder extends RecyclerView.ViewHolder {
        final PostScreenshotPhotoView imageView;
        final View playButton;
        final AspectRatioFrameLayout container;
        Media mediaItem;

        public MediaViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.container);
            imageView = itemView.findViewById(R.id.image);
            playButton = itemView.findViewById(R.id.play);

            imageView.setCornerRadius(itemView.getContext().getResources().getDimension(R.dimen.post_media_radius));
            imageView.setMaxAspectRatio(0);
        }
    }

}
