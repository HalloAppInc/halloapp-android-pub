package com.halloapp.ui.posts;

import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.halloapp.BuildConfig;
import com.halloapp.Constants;
import com.halloapp.R;
import com.halloapp.posts.Media;
import com.halloapp.posts.Post;
import com.halloapp.posts.PostsDb;
import com.halloapp.ui.CommentsActivity;
import com.halloapp.ui.PostDetailsActivity;
import com.halloapp.ui.VideoPlaybackActivity;
import com.halloapp.ui.ViewHolderWithLifecycle;
import com.halloapp.util.TimeFormatter;
import com.halloapp.widget.AvatarsLayout;
import com.halloapp.widget.LimitingTextView;
import com.halloapp.widget.MediaViewPager;
import com.halloapp.widget.PostImageView;
import com.halloapp.widget.SeenDetectorLayout;

import java.util.List;

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
    private final PostMediaPagerAdapter mediaPagerAdapter;

    private final PostViewHolderParent parent;
    Post post;

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

        if (mediaPagerView != null) {
            mediaPagerAdapter = new PostMediaPagerAdapter();
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
            if (post.seen == Post.POST_SEEN_NO && post.isIncoming()) {
                post.seen = Post.POST_SEEN_YES_PENDING;
                PostsDb.getInstance(itemView.getContext()).setIncomingPostSeen(post.senderUserId, post.postId);
            }
        });

        textView.setOnReadMoreListener((view, limit) -> {
            final Intent intent = new Intent(itemView.getContext(), CommentsActivity.class);
            intent.putExtra(CommentsActivity.EXTRA_POST_SENDER_USER_ID, post.senderUserId.rawId());
            intent.putExtra(CommentsActivity.EXTRA_POST_ID, post.postId);
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
        parent.getAvatarLoader().loadAvatarFor(post.senderUserId, this, avatarView::setImageBitmap);
        if (post.isOutgoing()) {
            nameView.setText(nameView.getContext().getString(R.string.me));
        } else {
            parent.getContactLoader().load(nameView, post.senderUserId);
        }
        if (!post.transferred) {
            progressView.setVisibility(View.VISIBLE);
            timeView.setVisibility(View.GONE);
        } else {
            progressView.setVisibility(View.GONE);
            timeView.setVisibility(View.VISIBLE);
            timeView.setText(TimeFormatter.formatTimeDiff(timeView.getContext(), System.currentTimeMillis() - post.timestamp));
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
            mediaPagerView.setCurrentItem(selPos == null ? 0 : selPos, false);
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
    }

    private class PostMediaPagerAdapter extends PagerAdapter {

        List<Media> media;

        PostMediaPagerAdapter() {
        }

        void setMedia(@NonNull List<Media> media) {
            this.media = media;
            notifyDataSetChanged();
        }

        public int getItemPosition(@NonNull Object object) {
            int index = 0;
            final Object tag = ((View)object).getTag();
            for (Media mediaItem : media) {
                if (mediaItem.id.equals(tag)) {
                    return index;
                }
                index++;
            }
            return POSITION_NONE;
        }

        @Override
        public @NonNull Object instantiateItem(@NonNull ViewGroup container, int position) {
            final View view;
            if (parent.getRecycledMediaViews().empty()) {
                view = LayoutInflater.from(container.getContext()).inflate(R.layout.post_feed_media_pager_item, container, false);
                if (BuildConfig.DEBUG_MEDIA) {
                    final TextView mediaInfoView = new TextView(container.getContext());
                    mediaInfoView.setTextColor(0xffffffff);
                    mediaInfoView.setShadowLayer(1, 1, 1, 0xff000000);
                    int padding = container.getContext().getResources().getDimensionPixelSize(R.dimen.media_gallery_grid_spacing);
                    mediaInfoView.setPadding(2 * padding, padding, 2 * padding, padding);
                    mediaInfoView.setId(R.id.comment);
                    ((ViewGroup)view).addView(mediaInfoView, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM));
                }
            } else {
                view = parent.getRecycledMediaViews().pop();
            }
            final Media mediaItem = media.get(position);
            view.setTag(mediaItem.id);
            final PostImageView imageView = view.findViewById(R.id.image);
            imageView.setSinglePointerDragStartDisabled(true);
            imageView.setDrawDelegate(parent.getDrawDelegateView());
            parent.getMediaThumbnailLoader().load(imageView, mediaItem);
            final View playButton = view.findViewById(R.id.play);
            if (mediaItem.type == Media.MEDIA_TYPE_VIDEO) {
                playButton.setVisibility(View.VISIBLE);
                if (mediaItem.file != null) {
                    playButton.setOnClickListener(v -> {
                        final Intent intent = new Intent(itemView.getContext(), VideoPlaybackActivity.class);
                        intent.setData(Uri.fromFile(mediaItem.file));
                        parent.startActivity(intent);
                    });
                } else {
                    playButton.setOnClickListener(null);
                }
            } else {
                playButton.setVisibility(View.GONE);
            }

            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            final View view = (View) object;
            final PostImageView imageView = view.findViewById(R.id.image);
            imageView.setImageDrawable(null);
            container.removeView(view);
            parent.getRecycledMediaViews().push(view);
        }

        @Override
        public void finishUpdate(@NonNull ViewGroup container) {
            final int childCount = container.getChildCount();
            for (int i = 0; i < childCount; i++) {
                final View child = container.getChildAt(i);
                final Object tag = child.getTag();
                final PostImageView imageView = child.findViewById(R.id.image);
                for (Media mediaItem : media) {
                    if (mediaItem.id.equals(tag)) {
                        parent.getMediaThumbnailLoader().load(imageView, mediaItem);
                        break;
                    }
                }
            }
        }

        @Override
        public int getCount() {
            return media == null ? 0 : media.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }
    }
}

