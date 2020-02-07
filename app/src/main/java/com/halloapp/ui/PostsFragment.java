package com.halloapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.LongSparseArray;
import androidx.core.util.Preconditions;
import androidx.fragment.app.Fragment;
import androidx.paging.AsyncPagedListDiffer;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.AdapterListUpdateCallback;
import androidx.recyclerview.widget.AsyncDifferConfig;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListUpdateCallback;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.halloapp.Constants;
import com.halloapp.R;
import com.halloapp.contacts.ContactLoader;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.media.MediaThumbnailLoader;
import com.halloapp.posts.Media;
import com.halloapp.posts.Post;
import com.halloapp.util.Log;
import com.halloapp.util.TimeUtils;
import com.halloapp.widget.DrawDelegateView;
import com.halloapp.widget.LimitingTextView;
import com.halloapp.widget.MediaViewPager;
import com.halloapp.widget.PostImageView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import me.relex.circleindicator.CircleIndicator;

public class PostsFragment extends Fragment {

    protected final PostsAdapter adapter = new PostsAdapter();

    private MediaThumbnailLoader mediaThumbnailLoader;
    private ContactLoader contactLoader;

    private DrawDelegateView drawDelegateView;

    private final LongSparseArray<Integer> mediaPagerPositionMap = new LongSparseArray<>();

    private long refreshTimestampsTime = Long.MAX_VALUE;
    private final Runnable refreshTimestampsRunnable = () -> {
        Log.v("HomeFragment: refreshing timestamps at " + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.US).format(new Date(System.currentTimeMillis())));
        refreshTimestampsTime = Long.MAX_VALUE;
        adapter.notifyDataSetChanged();
    };
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

    @CallSuper
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mediaThumbnailLoader = new MediaThumbnailLoader(Preconditions.checkNotNull(getContext()));
        contactLoader = new ContactLoader(Preconditions.checkNotNull(getContext()));
        ContactsDb.getInstance(Preconditions.checkNotNull(getContext())).addObserver(contactsObserver);
    }

    @CallSuper
    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaThumbnailLoader.destroy();
        contactLoader.destroy();
        ContactsDb.getInstance(Preconditions.checkNotNull(getContext())).removeObserver(contactsObserver);
        mainHandler.removeCallbacks(refreshTimestampsRunnable);
    }

    @CallSuper
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        drawDelegateView = Preconditions.checkNotNull(getActivity()).findViewById(R.id.draw_delegate);
    }

    private void scheduleTimestampRefresh(long postTimestamp) {
        long refreshTime = TimeUtils.getRefreshTime(postTimestamp);
        if (refreshTime < refreshTimestampsTime) {
            refreshTimestampsTime = System.currentTimeMillis() + refreshTime;
            Log.v("HomeFragment: will refresh timestamps at " + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.US).format(new Date(refreshTimestampsTime)));
            mainHandler.removeCallbacks(refreshTimestampsRunnable);
            mainHandler.postDelayed(refreshTimestampsRunnable, refreshTimestampsTime - System.currentTimeMillis());
        }
    }

    private class ViewHolder extends RecyclerView.ViewHolder {

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
        }

        void bindTo(final @NonNull Post post) {

            this.post = post;

            avatarView.setImageResource(R.drawable.avatar_person); // TODO (ds): load profile photo
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
                timeView.setText(TimeUtils.formatTimeDiff(timeView.getContext(), System.currentTimeMillis() - post.timestamp));
                scheduleTimestampRefresh(post.timestamp);
            }
            if (post.media.isEmpty()) {
                mediaPagerView.setVisibility(View.GONE);
                mediaPagerIndicator.setVisibility(View.GONE);
                textView.setPadding(textView.getPaddingLeft(), getResources().getDimensionPixelSize(R.dimen.text_post_padding_top),
                        textView.getPaddingRight(), getResources().getDimensionPixelSize(R.dimen.text_post_padding_bottom));
            } else {
                mediaPagerView.setVisibility(View.VISIBLE);
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
                textView.setPadding(textView.getPaddingLeft(), getResources().getDimensionPixelSize(R.dimen.media_post_padding_top),
                        textView.getPaddingRight(), getResources().getDimensionPixelSize(R.dimen.media_post_padding_bottom));
            }

            textView.setText(post.text);
            textView.setOnReadMoreListener(new LimitingTextView.OnReadMoreListener() {
                @Override
                public boolean onReadMore(TextView view, int limit) {
                    final Intent intent = new Intent(getContext(), CommentsActivity.class);
                    intent.putExtra(CommentsActivity.EXTRA_POST_SENDER_USER_ID, post.senderUserId.rawId());
                    intent.putExtra(CommentsActivity.EXTRA_POST_ID, post.postId);
                    intent.putExtra(CommentsActivity.EXTRA_SHOW_KEYBOARD, false);
                    intent.putExtra(CommentsActivity.EXTRA_NO_POST_LENGTH_LIMIT, true);
                    startActivity(intent);
                    return true;
                }
            });

            if (TextUtils.isEmpty(post.text)) {
                textView.setVisibility(View.GONE);
                postActionsSeparator.setVisibility(View.GONE);
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
                @SuppressWarnings("SuspiciousMethodCalls")
                int index = media.indexOf(((View)object).getTag());
                return index >= 0 ? index : POSITION_NONE;
            }

            @Override
            public @NonNull Object instantiateItem(@NonNull ViewGroup container, int position) {
                final View view = getLayoutInflater().inflate(R.layout.media_pager_item, container, false);
                final PostImageView imageView = view.findViewById(R.id.image);
                imageView.setDrawDelegate(drawDelegateView);
                final Media mediaItem = media.get(position);
                view.setTag(mediaItem);
                if (mediaItem.height > mediaItem.width) {
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                } else {
                    imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                }
                mediaThumbnailLoader.load(imageView, mediaItem);
                container.addView(view);
                return view;
            }

            @Override
            public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object view) {
                container.removeView((View) view);
            }

            @Override
            public void finishUpdate(@NonNull ViewGroup container) {
                container.requestLayout();
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

    protected class PostsAdapter extends RecyclerView.Adapter<ViewHolder> {

        final List<View> headers = new ArrayList<>();
        final AsyncPagedListDiffer<Post> differ;

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
            return position < headers.size() ? position + 1 : 0;
        }

        @Override
        public long getItemId(int position) {
            return position < headers.size() ? -position : Preconditions.checkNotNull(getItem(position)).rowId;
        }

        @Override
        public @NonNull ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return viewType > 0 ? new HeaderViewHolder(headers.get(viewType - 1)) : new PostViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.post_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            if (holder instanceof PostViewHolder) {
                ((PostViewHolder)holder).bindTo(Preconditions.checkNotNull(getItem(position)));
            }
        }
    }
}
