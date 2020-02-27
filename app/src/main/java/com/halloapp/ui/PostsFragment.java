package com.halloapp.ui;

import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.CallSuper;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.LongSparseArray;
import androidx.core.util.Preconditions;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.AsyncPagedListDiffer;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.AdapterListUpdateCallback;
import androidx.recyclerview.widget.AsyncDifferConfig;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListUpdateCallback;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.halloapp.BuildConfig;
import com.halloapp.Constants;
import com.halloapp.R;
import com.halloapp.contacts.ContactLoader;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.media.MediaThumbnailLoader;
import com.halloapp.posts.Media;
import com.halloapp.posts.Post;
import com.halloapp.posts.PostsDb;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.util.Log;
import com.halloapp.util.TimeFormatter;
import com.halloapp.widget.AvatarsLayout;
import com.halloapp.widget.DrawDelegateView;
import com.halloapp.widget.LimitingTextView;
import com.halloapp.widget.MediaViewPager;
import com.halloapp.widget.PostImageView;
import com.halloapp.widget.SeenDetectorLayout;
import com.halloapp.xmpp.Connection;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import me.relex.circleindicator.CircleIndicator;

public class PostsFragment extends Fragment {

    private static final int MAX_SEEN_BY_AVATARS = 4;

    protected final PostsAdapter adapter = new PostsAdapter();

    private MediaThumbnailLoader mediaThumbnailLoader;
    private ContactLoader contactLoader;
    private AvatarLoader avatarLoader;

    private DrawDelegateView drawDelegateView;
    private final Stack<View> recycledMediaViews = new Stack<>();

    private final LongSparseArray<Integer> mediaPagerPositionMap = new LongSparseArray<>();

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final ContactsDb.Observer contactsObserver = new ContactsDb.Observer() {

        @Override
        public void onContactsChanged() {
            contactLoader.resetCache();
            mainHandler.post(adapter::notifyDataSetChanged);
        }

        @Override
        public void onContactsReset() {
        }
    };

    private TimestampRefresher timestampRefresher;

    @CallSuper
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Point point = new Point();
        Preconditions.checkNotNull(getActivity()).getWindowManager().getDefaultDisplay().getSize(point);
        mediaThumbnailLoader = new MediaThumbnailLoader(Preconditions.checkNotNull(getContext()), Math.min(Constants.MAX_IMAGE_DIMENSION, Math.max(point.x, point.y)));
        contactLoader = new ContactLoader(Preconditions.checkNotNull(getContext()));
        avatarLoader = AvatarLoader.getInstance(Connection.getInstance());
        ContactsDb.getInstance(Preconditions.checkNotNull(getContext())).addObserver(contactsObserver);
        timestampRefresher = new ViewModelProvider(this).get(TimestampRefresher.class);
        timestampRefresher.refresh.observe(this, value -> adapter.notifyDataSetChanged());
    }

    @CallSuper
    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaThumbnailLoader.destroy();
        contactLoader.destroy();
        ContactsDb.getInstance(Preconditions.checkNotNull(getContext())).removeObserver(contactsObserver);
    }

    @CallSuper
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        drawDelegateView = Preconditions.checkNotNull(getActivity()).findViewById(R.id.draw_delegate);
    }

    private class ViewHolder extends ViewHolderWithLifecycle {

        ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    private class HeaderViewHolder extends ViewHolder {

        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    private class PostViewHolder extends ViewHolder {

        final ImageView avatarView;
        final TextView nameView;
        final TextView timeView;
        final View progressView;
        final MediaViewPager mediaPagerView;
        final CircleIndicator mediaPagerIndicator;
        final LimitingTextView textView;
        final View commentButton;
        final View messageButton;
        final View commentsIndicator;
        final View postActionsSeparator;
        final PostMediaPagerAdapter mediaPagerAdapter;
        final SeenDetectorLayout postContentLayout;
        final AvatarsLayout seenIndicator;
        final View seenIndicatorSpace;

        Post post;

        PostViewHolder(final @NonNull View v) {
            super(v);

            avatarView = v.findViewById(R.id.avatar);
            nameView = v.findViewById(R.id.name);
            timeView = v.findViewById(R.id.time);
            progressView = v.findViewById(R.id.progress);
            mediaPagerView = v.findViewById(R.id.media_pager);
            mediaPagerIndicator = v.findViewById(R.id.media_pager_indicator);
            textView = v.findViewById(R.id.text);
            commentButton = v.findViewById(R.id.comment);
            messageButton = v.findViewById(R.id.message);
            commentsIndicator = v.findViewById(R.id.comments_indicator);
            postActionsSeparator = v.findViewById(R.id.post_actions_separator);
            postContentLayout = v.findViewById(R.id.post_content);
            seenIndicator = v.findViewById(R.id.seen_indicator);
            seenIndicatorSpace = v.findViewById(R.id.seen_indicator_space);

            if (mediaPagerView != null) {
                mediaPagerAdapter = new PostMediaPagerAdapter();
                mediaPagerView.setAdapter(mediaPagerAdapter);
                mediaPagerView.setPageMargin(Preconditions.checkNotNull(getContext()).getResources().getDimensionPixelSize(R.dimen.media_pager_margin));

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
                            mediaPagerPositionMap.remove(post.rowId);
                        } else {
                            mediaPagerPositionMap.put(post.rowId, position);
                        }
                    }

                    @Override
                    public void onPageScrollStateChanged(int state) {
                    }
                });
            } else {
                mediaPagerAdapter = null;
            }

            postContentLayout.setOnSeenListener(() -> {
                if (post.seen == Post.POST_SEEN_NO && post.isIncoming()) {
                    post.seen = Post.POST_SEEN_YES_PENDING;
                    PostsDb.getInstance(Preconditions.checkNotNull(getContext())).setIncomingPostSeen(post.senderUserId, post.postId);
                }
            });

            seenIndicator.setOnClickListener(v1 -> {
                final Intent intent = new Intent(getContext(), PostDetailsActivity.class);
                intent.putExtra(PostDetailsActivity.EXTRA_POST_ID, post.postId);
                startActivity(intent);
            });

            textView.setOnReadMoreListener((view, limit) -> {
                final Intent intent = new Intent(getContext(), CommentsActivity.class);
                intent.putExtra(CommentsActivity.EXTRA_POST_SENDER_USER_ID, post.senderUserId.rawId());
                intent.putExtra(CommentsActivity.EXTRA_POST_ID, post.postId);
                intent.putExtra(CommentsActivity.EXTRA_SHOW_KEYBOARD, false);
                intent.putExtra(CommentsActivity.EXTRA_NO_POST_LENGTH_LIMIT, true);
                startActivity(intent);
                return true;
            });
        }

        void bindTo(final @NonNull Post post) {

            this.post = post;

            avatarView.setImageResource(R.drawable.avatar_person); // TODO (ds): load profile photo
            avatarLoader.loadAvatarFor(post.senderUserId, this, avatarView::setImageBitmap);
            if (post.isOutgoing()) {
                nameView.setText(nameView.getContext().getString(R.string.me));
            } else {
                contactLoader.load(nameView, post.senderUserId);
            }
            if (!post.transferred) {
                progressView.setVisibility(View.VISIBLE);
                timeView.setVisibility(View.GONE);
            } else {
                progressView.setVisibility(View.GONE);
                timeView.setVisibility(View.VISIBLE);
                timeView.setText(TimeFormatter.formatTimeDiff(timeView.getContext(), System.currentTimeMillis() - post.timestamp));
                timestampRefresher.scheduleTimestampRefresh(post.timestamp);
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
                final Integer selPos = mediaPagerPositionMap.get(post.rowId);
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

            if (post.unseenCommentCount > 0) {
                commentsIndicator.setVisibility(View.VISIBLE);
                commentsIndicator.setBackgroundResource(R.drawable.new_comments_indicator);
            } else if (post.commentCount > 0) {
                commentsIndicator.setVisibility(View.VISIBLE);
                commentsIndicator.setBackgroundResource(R.drawable.old_comments_indicator);
            } else {
                commentsIndicator.setVisibility(View.GONE);
            }

            if (post.seenByCount > 0) {
                seenIndicator.setVisibility(View.VISIBLE);
                seenIndicatorSpace.setVisibility(View.GONE);
                seenIndicator.setContentDescription(getResources().getQuantityString(R.plurals.seen_by, post.seenByCount, post.seenByCount));
                seenIndicator.setAvatarCount(Math.min(post.seenByCount, MAX_SEEN_BY_AVATARS));
                if (seenIndicator.getChildCount() == MAX_SEEN_BY_AVATARS) {
                    seenIndicator.getChildAt(0).setAlpha(.5f);
                }
            } else {
                seenIndicator.setVisibility(View.GONE);
                seenIndicatorSpace.setVisibility(TextUtils.isEmpty(post.text) ? View.GONE : View.VISIBLE);
            }

            commentButton.setOnClickListener(v -> {
                final Intent intent = new Intent(getContext(), CommentsActivity.class);
                intent.putExtra(CommentsActivity.EXTRA_POST_SENDER_USER_ID, post.senderUserId.rawId());
                intent.putExtra(CommentsActivity.EXTRA_POST_ID, post.postId);
                intent.putExtra(CommentsActivity.EXTRA_SHOW_KEYBOARD, post.commentCount == 0);
                startActivity(intent);
            });
            messageButton.setOnClickListener(v -> {
                // TODO (ds): start message activity
            });
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
                if (recycledMediaViews.empty()) {
                    view = getLayoutInflater().inflate(R.layout.post_feed_media_pager_item, container, false);
                    if (BuildConfig.DEBUG_MEDIA) {
                        final TextView mediaInfoView = new TextView(container.getContext());
                        mediaInfoView.setTextColor(0xffffffff);
                        mediaInfoView.setShadowLayer(1, 1, 1, 0xff000000);
                        int padding = getResources().getDimensionPixelSize(R.dimen.media_gallery_grid_spacing);
                        mediaInfoView.setPadding(2 * padding, padding, 2 * padding, padding);
                        mediaInfoView.setId(R.id.comment);
                        ((ViewGroup)view).addView(mediaInfoView, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM));
                    }
                } else {
                    view = recycledMediaViews.pop();
                }
                final Media mediaItem = media.get(position);
                view.setTag(mediaItem.id);
                final PostImageView imageView = view.findViewById(R.id.image);
                imageView.setSinglePointerDragStartDisabled(true);
                imageView.setDrawDelegate(drawDelegateView);
                mediaThumbnailLoader.load(imageView, mediaItem);
                final View playButton = view.findViewById(R.id.play);
                if (mediaItem.type == Media.MEDIA_TYPE_VIDEO) {
                    playButton.setVisibility(View.VISIBLE);
                    if (mediaItem.file != null) {
                        playButton.setOnClickListener(v -> {
                            final Intent intent = new Intent(getContext(), VideoPlaybackActivity.class);
                            intent.setData(Uri.fromFile(mediaItem.file));
                            startActivity(intent);
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
                recycledMediaViews.push(view);
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
                            mediaThumbnailLoader.load(imageView, mediaItem);
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

    private static final DiffUtil.ItemCallback<Post> DIFF_CALLBACK = new DiffUtil.ItemCallback<Post>() {

        @Override
        public boolean areItemsTheSame(Post oldItem, Post newItem) {
            // The ID property identifies when items are the same.
            return oldItem.rowId == newItem.rowId;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Post oldItem, @NonNull Post newItem) {
            return oldItem.equals(newItem);
        }
    };

    protected class PostsAdapter extends AdapterWithLifecycle<ViewHolder> {

        final List<View> headers = new ArrayList<>();
        final AsyncPagedListDiffer<Post> differ;

        static final int POST_TYPE_TEXT = 0;
        static final int POST_TYPE_MEDIA = 1;

        PostsAdapter() {
            setHasStableIds(true);

            AdapterListUpdateCallback adapterCallback = new AdapterListUpdateCallback(this);
            ListUpdateCallback listUpdateCallback = new ListUpdateCallback() {

                public void onInserted(int position, int count) {
                    adapterCallback.onInserted(position + headers.size(), count);
                }

                public void onRemoved(int position, int count) {
                    adapterCallback.onRemoved(position + headers.size(), count);
                }

                public void onMoved(int fromPosition, int toPosition) {
                    adapterCallback.onMoved(fromPosition + headers.size(), toPosition + 1);
                }

                public void onChanged(int position, int count, @Nullable Object payload) {
                    adapterCallback.onChanged(position + headers.size(), count, payload);
                }
            };

            differ = new AsyncPagedListDiffer<>(listUpdateCallback, new AsyncDifferConfig.Builder<>(DIFF_CALLBACK).build());
        }

        public void addHeader(@NonNull View view) {
            headers.add(view);
        }

        public void submitList(@Nullable PagedList<Post> pagedList) {
            differ.submitList(pagedList);
        }

        public void submitList(@Nullable PagedList<Post> pagedList, @Nullable final Runnable commitCallback) {
            differ.submitList(pagedList, commitCallback);
        }

        public @Nullable PagedList<Post> getCurrentList() {
            return differ.getCurrentList();
        }

        public @Nullable Post getItem(int position) {
            return position < headers.size() ? null : differ.getItem(position - headers.size());
        }

        @Override
        public int getItemCount() {
            return headers.size() + differ.getItemCount();
        }

        @Override
        public int getItemViewType(int position) {
            // negative view types are headers
            return position < headers.size() ? -position - 1 : (Preconditions.checkNotNull(getItem(position)).media.isEmpty() ? POST_TYPE_TEXT : POST_TYPE_MEDIA);
        }

        @Override
        public long getItemId(int position) {
            return position < headers.size() ? -position : Preconditions.checkNotNull(getItem(position)).rowId;
        }

        @Override
        public @NonNull ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType < 0) {
                return new HeaderViewHolder(headers.get(-viewType - 1));
            } else {
                View layout = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_item, parent, false);
                @LayoutRes int contentLayoutRes;
                switch (viewType) {
                    case POST_TYPE_TEXT: {
                        contentLayoutRes = R.layout.post_item_text;
                        break;
                    }
                    case POST_TYPE_MEDIA: {
                        contentLayoutRes = R.layout.post_item_media;
                        break;
                    }
                    default: {
                        throw new IllegalArgumentException();
                    }
                }
                final ViewGroup content = layout.findViewById(R.id.post_content);
                LayoutInflater.from(content.getContext()).inflate(contentLayoutRes, content, true);
                return new PostViewHolder(layout);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            if (holder instanceof PostViewHolder) {
                ((PostViewHolder)holder).bindTo(Preconditions.checkNotNull(getItem(position)));
            }
        }
    }
}
