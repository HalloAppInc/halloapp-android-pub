package com.halloapp.ui.posts;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;

import com.halloapp.Constants;
import com.halloapp.R;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Media;
import com.halloapp.content.Post;
import com.halloapp.ui.CommentsActivity;
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
    private final View progressView;
    private final MediaViewPager mediaPagerView;
    private final CircleIndicator mediaPagerIndicator;
    private final LimitingTextView textView;
    private final View postActionsSeparator;
    private final MediaPagerAdapter mediaPagerAdapter;
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
        progressView = itemView.findViewById(R.id.progress);
        mediaPagerView = itemView.findViewById(R.id.media_pager);
        mediaPagerIndicator = itemView.findViewById(R.id.media_pager_indicator);
        textView = itemView.findViewById(R.id.text);
        postActionsSeparator = itemView.findViewById(R.id.post_actions_separator);
        footerSpacing = itemView.findViewById(R.id.footer_spacing);
        footer = itemView.findViewById(R.id.post_footer);

        if (mediaPagerView != null) {
            mediaPagerAdapter = new MediaPagerAdapter(parent);
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
            final Intent intent = new Intent(itemView.getContext(), CommentsActivity.class);
            intent.putExtra(CommentsActivity.EXTRA_POST_SENDER_USER_ID, post.senderUserId.rawId());
            intent.putExtra(CommentsActivity.EXTRA_POST_ID, post.id);
            intent.putExtra(CommentsActivity.EXTRA_SHOW_KEYBOARD, false);
            intent.putExtra(CommentsActivity.EXTRA_NO_POST_LENGTH_LIMIT, true);
            parent.startActivity(intent);
            return true;
        });
    }

    @CallSuper
    public void bindTo(@NonNull Post post) {

        this.post = post;

        avatarView.setImageResource(R.drawable.avatar_person); // TODO (ds): load profile photo
        parent.getAvatarLoader().load(avatarView, post.senderUserId);
        if (post.isOutgoing()) {
            nameView.setText(nameView.getContext().getString(R.string.me));
        } else {
            parent.getContactLoader().load(nameView, post.senderUserId);
        }
        if (post.transferred == Post.TRANSFERRED_NO) {
            progressView.setVisibility(View.VISIBLE);
            timeView.setVisibility(View.GONE);
        } else {
            progressView.setVisibility(View.GONE);
            timeView.setVisibility(View.VISIBLE);
            TimeFormatter.setTimeDiffText(timeView, System.currentTimeMillis() - post.timestamp);
            parent.getTimestampRefresher().scheduleTimestampRefresh(post.timestamp);
        }
        if (post.media.isEmpty()) {
            textView.setLineLimit(Constants.TEXT_POST_LINE_LIMIT);
        } else {
            mediaPagerView.setMaxAspectRatio(Math.min(Constants.MAX_IMAGE_ASPECT_RATIO, Media.getMaxAspectRatio(post.media)));
            mediaPagerAdapter.setMedia(post.media);
            if (post.media.size() > 1) {
                mediaPagerIndicator.setVisibility(View.VISIBLE);
                mediaPagerIndicator.setViewPager(mediaPagerView);
            } else {
                mediaPagerIndicator.setVisibility(View.GONE);
            }
            final Integer selPos = parent.getMediaPagerPositionMap().get(post.rowId);
            mediaPagerView.setCurrentItem(selPos == null ? (Rtl.isRtl(mediaPagerView.getContext()) ? post.media.size() - 1 : 0) : selPos, false);
            textView.setLineLimit(Constants.MEDIA_POST_LINE_LIMIT);
        }
        textView.setLineStep(0);
        textView.setText(post.text);

        if (TextUtils.isEmpty(post.text)) {
            textView.setVisibility(View.GONE);
            postActionsSeparator.setVisibility(post.seenByCount == 0 ? View.GONE : View.VISIBLE);
        } else {
            textView.setVisibility(View.VISIBLE);
            postActionsSeparator.setVisibility(View.VISIBLE);
        }

        if (post.isRetracted()) {
            footer.setVisibility(View.GONE);
            postActionsSeparator.setVisibility(View.GONE);
        } else {
            footer.setVisibility(View.VISIBLE);
        }
    }
}

