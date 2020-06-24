package com.halloapp.ui.posts;

import android.content.res.ColorStateList;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.halloapp.Constants;
import com.halloapp.R;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Media;
import com.halloapp.content.Post;
import com.halloapp.ui.ContentViewHolderParent;
import com.halloapp.ui.MediaPagerAdapter;
import com.halloapp.ui.ViewHolderWithLifecycle;
import com.halloapp.util.Rtl;
import com.halloapp.util.TimeFormatter;
import com.halloapp.widget.LimitingTextView;
import com.halloapp.widget.MediaViewPager;
import com.halloapp.widget.SeenDetectorLayout;

import me.relex.circleindicator.CircleIndicator;

public class PostViewHolder extends ViewHolderWithLifecycle {

    private final ImageView avatarView;
    private final TextView nameView;
    private final TextView timeView;
    private final ImageView statusView;
    private final View progressView;
    private final MediaViewPager mediaPagerView;
    private final CircleIndicator mediaPagerIndicator;
    private final LimitingTextView textView;
    protected final MediaPagerAdapter mediaPagerAdapter;
    private final View footer;
    final View footerSpacing;

    final PostViewHolderParent parent;
    Post post;

    public abstract static class PostViewHolderParent implements MediaPagerAdapter.MediaPagerAdapterParent, ContentViewHolderParent {
    }

    PostViewHolder(@NonNull View itemView, @NonNull PostViewHolderParent parent) {
        super(itemView);

        this.parent = parent;

        avatarView = itemView.findViewById(R.id.avatar);
        nameView = itemView.findViewById(R.id.name);
        timeView = itemView.findViewById(R.id.time);
        statusView = itemView.findViewById(R.id.status);
        progressView = itemView.findViewById(R.id.progress);
        mediaPagerView = itemView.findViewById(R.id.media_pager);
        mediaPagerIndicator = itemView.findViewById(R.id.media_pager_indicator);
        textView = itemView.findViewById(R.id.text);
        footerSpacing = itemView.findViewById(R.id.footer_spacing);
        footer = itemView.findViewById(R.id.post_footer);

        if (mediaPagerView != null) {
            mediaPagerAdapter = new MediaPagerAdapter(parent, itemView.getContext().getResources().getDimension(R.dimen.post_media_radius));
            mediaPagerView.setAdapter(mediaPagerAdapter);
            mediaPagerView.setPageMargin(itemView.getContext().getResources().getDimensionPixelSize(R.dimen.media_pager_margin));

            mediaPagerView.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                @Override
                public void onPageSelected(int position) {
                    if (post == null) {
                        return;
                    }
                    if (position == 0) {
                        parent.getMediaPagerPositionMap().remove(post.rowId);
                    } else {
                        parent.getMediaPagerPositionMap().put(post.rowId, position);
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }
            });
        } else {
            mediaPagerAdapter = null;
        }

        final SeenDetectorLayout postContentLayout = itemView.findViewById(R.id.post_content);
        postContentLayout.setOnSeenListener(() -> {
            if (post.seen == Post.SEEN_NO && post.isIncoming()) {
                post.seen = Post.SEEN_YES_PENDING;
                ContentDb.getInstance(itemView.getContext()).setIncomingPostSeen(post.senderUserId, post.id);
            }
        });

        textView.setOnReadMoreListener((view, limit) -> {
            parent.getTextLimits().put(post.rowId, limit);
            return false;
        });
    }

    @CallSuper
    public void bindTo(@NonNull Post post) {

        this.post = post;

        parent.getAvatarLoader().load(avatarView, post.senderUserId);
        if (post.isOutgoing()) {
            nameView.setText(nameView.getContext().getString(R.string.me));
        } else {
            parent.getContactLoader().load(nameView, post.senderUserId);
        }
        if (post.transferred == Post.TRANSFERRED_NO) {
            if (post.isTransferFailed()) {
                progressView.setVisibility(View.GONE);
                statusView.setVisibility(View.VISIBLE);
                statusView.setImageResource(R.drawable.ic_error);
                statusView.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(statusView.getContext(), R.color.design_default_color_error)));
            } else {
                progressView.setVisibility(View.VISIBLE);
                statusView.setVisibility(View.GONE);
            }
        } else {
            progressView.setVisibility(View.GONE);
            statusView.setVisibility(View.GONE);
        }
        TimeFormatter.setTimeDiffText(timeView, System.currentTimeMillis() - post.timestamp);
        parent.getTimestampRefresher().scheduleTimestampRefresh(post.timestamp);

        final boolean noCaption = TextUtils.isEmpty(post.text);

        if (!post.media.isEmpty()) {
            mediaPagerView.setMaxAspectRatio(Math.min(Constants.MAX_IMAGE_ASPECT_RATIO, Media.getMaxAspectRatio(post.media)));
            mediaPagerAdapter.setContentId(post.id);
            mediaPagerAdapter.setMedia(post.media);
            if (post.media.size() > 1) {
                mediaPagerIndicator.setVisibility(View.VISIBLE);
                mediaPagerIndicator.setViewPager(mediaPagerView);
            } else {
                mediaPagerIndicator.setVisibility(View.GONE);
            }
            final Integer selPos = parent.getMediaPagerPositionMap().get(post.rowId);
            mediaPagerView.setCurrentItem(selPos == null ? (Rtl.isRtl(mediaPagerView.getContext()) ? post.media.size() - 1 : 0) : selPos, false);
        }
        final Integer textLimit = parent.getTextLimits().get(post.rowId);
        textView.setLineLimit(textLimit != null ? textLimit :
                (post.media.isEmpty() ? Constants.TEXT_POST_LINE_LIMIT : Constants.MEDIA_POST_LINE_LIMIT));
        textView.setLineLimitTolerance(textLimit != null ? Constants.POST_LINE_LIMIT_TOLERANCE : 0);
        if (post.text != null) {
            parent.getTextContentLoader().load(textView, post);
        } else {
            textView.setText("");
        }

        if (noCaption) {
            textView.setVisibility(View.GONE);
        } else {
            textView.setVisibility(View.VISIBLE);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textView.getContext().getResources().getDimension(
                    (post.text.length() < 180 && post.media.isEmpty()) ? R.dimen.post_text_size_large : R.dimen.post_text_size));
        }

        if (post.isRetracted()) {
            footer.setVisibility(View.GONE);
        } else {
            footer.setVisibility(View.VISIBLE);
        }
    }

    public void selectMedia(int index) {
        if (mediaPagerView != null) {
            mediaPagerView.setCurrentItem(index, false);
        }
    }
}

