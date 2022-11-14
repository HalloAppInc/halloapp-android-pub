package com.halloapp.ui.posts;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
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

import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;

import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.halloapp.Constants;
import com.halloapp.R;
import com.halloapp.content.Media;
import com.halloapp.content.Post;
import com.halloapp.media.AudioDurationLoader;
import com.halloapp.media.MediaThumbnailLoader;
import com.halloapp.ui.mentions.TextContentLoader;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.AspectRatioFrameLayout;
import com.halloapp.widget.LimitingTextView;
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
        configuration.densityDpi = 480;
        configuration.fontScale = 1f;
        wrappedContext.applyOverrideConfiguration(configuration);
        DisplayMetrics displayMetrics = wrappedContext.getResources().getDisplayMetrics();
        displayMetrics.densityDpi = 480;

        indicatorSize = wrappedContext.getResources().getDimensionPixelSize(R.dimen.media_indicator_size);
        indicatorMargin = wrappedContext.getResources().getDimensionPixelSize(R.dimen.media_indicator_margin);
    }

    private void destroy() {
        textContentLoader.destroy();
        audioDurationLoader.destroy();
        mediaThumbnailLoader.destroy();
    }

    public static Pair<Bitmap,Bitmap> generateScreenshotWithBackgroundSplit(@NonNull Context context, @NonNull Post post, int previewIndex) {
        PostScreenshotGenerator generator = new PostScreenshotGenerator(context);
        Pair<Bitmap,Bitmap> bitmaps = generator.generateScreenshotForInstagramStories(post, previewIndex);
        generator.destroy();
        return bitmaps;
    }

    public static Bitmap generateScreenshotWithBackgroundCombined(@NonNull Context context, @NonNull Post post, int previewIndex) {
        PostScreenshotGenerator generator = new PostScreenshotGenerator(context);
        Bitmap bitmap = generator.generateScreenshotWithBackground(post, previewIndex);
        generator.destroy();
        return bitmap;
    }

    private void setupShareFooterText(View footer) {
        TextView footerText = footer.findViewById(R.id.share_footer_text);
        SpannableStringBuilder sb = new SpannableStringBuilder();
        String halloapp = "HalloApp";
        SpannableString halloappSpan = new SpannableString(halloapp);
        halloappSpan.setSpan(new ForegroundColorSpan(Color.WHITE), 0, halloappSpan.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        sb.append(wrappedContext.getString(R.string.external_share_posted_from));
        sb.append(" ");
        sb.append(halloappSpan);
        sb.append("\n");
        sb.append("halloapp.com");
        footerText.setText(sb);
    }

    private Bitmap generateScreenshotWithBackground(@NonNull Post post, int previewIndex) {
        int width = wrappedContext.getResources().getDimensionPixelSize(R.dimen.post_screenshot_stories_width);
        int height = wrappedContext.getResources().getDimensionPixelSize(R.dimen.post_screenshot_stories_height);

        ConstraintLayout v = (ConstraintLayout) buildViewForPost(post, previewIndex, R.layout.view_external_share_preview);
        if (v == null) {
            return null;
        }

        v.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY));
        v.layout(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());

        v.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY));
        v.layout(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());

        v.setDrawingCacheEnabled(true);
        v.buildDrawingCache();
        return v.getDrawingCache();
    }

    private void configurePreviewForInstagram(@NonNull ConstraintLayout root, @NonNull Post post) {
        View postContent = root.findViewById(R.id.post_content);
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) postContent.getLayoutParams();
        int extraMargin = wrappedContext.getResources().getDimensionPixelSize(R.dimen.post_instagram_extra_margin);

        layoutParams.bottomMargin = extraMargin;
        layoutParams.leftMargin = extraMargin;
        layoutParams.rightMargin = extraMargin;
        layoutParams.topMargin = extraMargin;

        postContent.setLayoutParams(layoutParams);

        if (post.type == Post.TYPE_MOMENT) {
            ImageView imageFirst = root.findViewById(R.id.image_first);

            int radius = wrappedContext.getResources().getDimensionPixelSize(R.dimen.moment_screenshot_corner_size);
            Bitmap b = ((BitmapDrawable) imageFirst.getDrawable()).getBitmap();
            imageFirst.setImageBitmap(combineMomentsBitmap(b, null, b.getHeight(), radius));
        }
    }

    private Pair<Bitmap, Bitmap> generateScreenshotForInstagramStories(@NonNull Post post, int previewIndex) {
        int width = wrappedContext.getResources().getDimensionPixelSize(R.dimen.post_screenshot_stories_width);
        int height = wrappedContext.getResources().getDimensionPixelSize(R.dimen.post_screenshot_stories_height);

        ConstraintLayout v = (ConstraintLayout) buildViewForPost(post, R.layout.view_external_share_preview, previewIndex);
        configurePreviewForInstagram(v, post);
        TextView footerText = v.findViewById(R.id.share_footer_text);
        footerText.setText("");

        v.setBackgroundColor(0);

        View cardView = v.findViewById(R.id.post_content);
        v.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY));
        v.layout(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());

        v.setDrawingCacheEnabled(true);
        v.buildDrawingCache();

        Bitmap postCard = Bitmap.createBitmap(v.getDrawingCache());
        v.removeView(cardView);
        v.setBackgroundColor(ContextCompat.getColor(wrappedContext, R.color.external_share_image_bg));
        setupShareFooterText(v.findViewById(R.id.share_footer));

        v.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY));
        v.layout(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());

        v.buildDrawingCache();
        return new Pair<>(v.getDrawingCache(), postCard);
    }

    public static Bitmap combineMomentsBitmap(@NonNull Bitmap one, @Nullable Bitmap two, int size, int cornerRadius) {
        Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Rect r1;
        Rect r2;

        if (two != null) {
            int halfWidth = size / 2;

            r1 = centerRect(one, halfWidth, size);
            r2 = centerRect(two, halfWidth, size);
            r2.left += halfWidth;
            r2.right += halfWidth;
        } else {
            r1 = centerRect(one, size, size);
            r2 = null;
        }

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, output.getWidth(), output.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(one, null, r1, paint);
        if (two != null) {
            canvas.drawBitmap(two, null, r2, paint);
        }
        return output;
    }

    private static Rect centerRect(Bitmap b, int w, int h) {
        int newWidth = b.getWidth();
        int newHeight = b.getHeight();
        if (newWidth - w > newHeight - h) {
            newWidth = (newWidth * h) / newHeight;
            newHeight = h;
        } else {
            newHeight = (newHeight * w) / newWidth;
            newWidth = w;
        }

        Rect r = new Rect();
        r.left = (w - newWidth) / 2;
        r.right = r.left + newWidth;
        r.top = (h - newHeight) / 2;
        r.bottom = r.top + newHeight;

        return r;
    }

    public static Bitmap getRoundedCornerBitmap(Bitmap input, int w, int h, int roundPx, boolean squareTL, boolean squareTR, boolean squareBL, boolean squareBR) {
        Bitmap output = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, w, h);
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        if (squareTL ){
            canvas.drawRect(0, 0, w/2f, h/2f, paint);
        }
        if (squareTR ){
            canvas.drawRect(w/2f, 0, w, h/2f, paint);
        }
        if (squareBL ){
            canvas.drawRect(0, h/2f, w/2f, h, paint);
        }
        if (squareBR ){
            canvas.drawRect(w/2f, h/2f, w, h, paint);
        }

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(input, 0,0, paint);

        return output;
    }

    private View buildViewForPost(@NonNull Post post, int previewIndex, @LayoutRes int layoutRes) {
        View root = LayoutInflater.from(wrappedContext).inflate(layoutRes, null, false);
        View footer = root.findViewById(R.id.share_footer);
        setupShareFooterText(footer);
        switch (post.type) {
            case Post.TYPE_USER:
                if (post.media.isEmpty()) {
                    return buildTextPostView(wrappedContext, root, post);
                } else {
                    return buildMediaPostView(wrappedContext, root, post, previewIndex);
                }
            case Post.TYPE_MOMENT: {
                return buildMomentPostView(wrappedContext, root, post);
            }
            case Post.TYPE_VOICE_NOTE:
                if ( post.media.size() > 1) {
                    return buildMediaPostView(wrappedContext, root, post, previewIndex);
                }
                return null;
        }
        Log.e("PostScreenshotGenerator invalid post to screenshot");
        return null;
    }

    private View buildMomentPostView(@NonNull Context context, @NonNull View root, @NonNull Post post) {
        ViewGroup content = root.findViewById(R.id.post_content);
        LayoutInflater.from(context).inflate(R.layout.post_screenshot_moment, content);

        ImageView imageViewFirst = content.findViewById(R.id.image_first);
        ImageView imageViewSecond = content.findViewById(R.id.image_second);
        View imageDivider = content.findViewById(R.id.image_divider);

        int size = wrappedContext.getResources().getDimensionPixelSize(R.dimen.moment_screenshot_size);

        mediaThumbnailLoader.load(imageViewFirst, post.media.get(0));

        if (post.media.size() > 1) {
            imageDivider.setVisibility(View.VISIBLE);
            imageViewSecond.setVisibility(View.VISIBLE);
            mediaThumbnailLoader.load(imageViewSecond, post.media.get(1));

            BitmapDrawable leftDrawable = (BitmapDrawable) imageViewFirst.getDrawable();
            BitmapDrawable rightDrawable = (BitmapDrawable) imageViewSecond.getDrawable();

            imageViewFirst.setImageBitmap(combineMomentsBitmap(leftDrawable.getBitmap(), rightDrawable.getBitmap(), size, 0));
        } else {
            imageDivider.setVisibility(View.GONE);
        }
        imageViewSecond.setVisibility(View.GONE);

        return root;
    }

    private View buildTextPostView(@NonNull Context context, @NonNull View root, @NonNull Post post) {
        ViewGroup content = root.findViewById(R.id.post_content);
        LayoutInflater.from(context).inflate(R.layout.post_screenshot_text, content);

        LimitingTextView textView = content.findViewById(R.id.text);

        List<Media> postMedia = post.getMedia();

        textView.setLineLimit(postMedia.isEmpty() ? Constants.TEXT_POST_LINE_LIMIT : Constants.MEDIA_POST_LINE_LIMIT);
        textView.setLineLimitTolerance(0);
        if (post.text != null) {
            textContentLoader.load(textView, post);
        } else {
            textView.setText("");
        }

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

    private View buildMediaPostView(@NonNull Context context, @NonNull View root, @NonNull Post post, int previewIndex) {
        ViewGroup content = root.findViewById(R.id.post_content);
        LayoutInflater.from(context).inflate(R.layout.post_screenshot_media, content);

        LimitingTextView textView = content.findViewById(R.id.text);

        AspectRatioFrameLayout container = content.findViewById(R.id.container);
        PostScreenshotPhotoView imageView = content.findViewById(R.id.image);

        List<Media> postMedia = post.getMedia();
        Media mediaItem;
        if (previewIndex < postMedia.size()) {
            mediaItem = postMedia.get(previewIndex);
        } else {
            mediaItem = postMedia.get(0);
        }
        if (mediaItem.type != Media.MEDIA_TYPE_IMAGE) {
            Log.e("PostScreenshotGenerator/buildMediaPostView non supported media type " + mediaItem.type);
            return null;
        }
        float maxAspectRatio = 0;
        if (mediaItem.width != 0) {
            float ratio = 1f * mediaItem.height / mediaItem.width;
            if (ratio > maxAspectRatio) {
                maxAspectRatio = ratio;
            }
        }
        container.setAspectRatio(maxAspectRatio);
        imageView.setMaxAspectRatio(maxAspectRatio);
        container.setMaxHeight(context.getResources().getDimensionPixelSize(R.dimen.post_screenshot_max_media_height));

        mediaThumbnailLoader.load(imageView, mediaItem);
        final boolean noCaption = TextUtils.isEmpty(post.text);

        textView.setLineLimit(5);
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
