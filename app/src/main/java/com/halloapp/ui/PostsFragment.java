package com.halloapp.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.CallSuper;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.LongSparseArray;
import androidx.core.app.ActivityOptionsCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.AsyncPagedListDiffer;
import androidx.recyclerview.widget.AdapterListUpdateCallback;
import androidx.recyclerview.widget.AsyncDifferConfig;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListUpdateCallback;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.Constants;
import com.halloapp.R;
import com.halloapp.contacts.ContactLoader;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.content.Post;
import com.halloapp.groups.ChatLoader;
import com.halloapp.media.MediaThumbnailLoader;
import com.halloapp.props.ServerProps;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.ui.mentions.TextContentLoader;
import com.halloapp.ui.posts.IncomingPostViewHolder;
import com.halloapp.ui.posts.OutgoingPostViewHolder;
import com.halloapp.ui.posts.PostViewHolder;
import com.halloapp.ui.posts.SeenByLoader;
import com.halloapp.ui.posts.SubtlePostViewHolder;
import com.halloapp.util.Preconditions;
import com.halloapp.widget.DrawDelegateView;

public class PostsFragment extends HalloFragment {

    protected final PostsAdapter adapter = new PostsAdapter();
    protected ViewGroup parentViewGroup;

    private ServerProps serverProps = ServerProps.getInstance();
    private MediaThumbnailLoader mediaThumbnailLoader;
    private ChatLoader chatLoader;
    private ContactLoader contactLoader;
    private AvatarLoader avatarLoader;
    private SeenByLoader seenByLoader;
    private TextContentLoader textContentLoader;
    private TimestampRefresher timestampRefresher;

    private DrawDelegateView drawDelegateView;
    private final RecyclerView.RecycledViewPool recycledMediaViews = new RecyclerView.RecycledViewPool();

    private final LongSparseArray<Integer> mediaPagerPositionMap = new LongSparseArray<>();
    private final LongSparseArray<Integer> textLimits = new LongSparseArray<>();

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final ContactsDb.Observer contactsObserver = new ContactsDb.BaseObserver() {
        @Override
        public void onContactsChanged() {
            contactLoader.resetCache();
            mainHandler.post(adapter::notifyDataSetChanged);
        }
    };

    @CallSuper
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Point point = new Point();
        requireActivity().getWindowManager().getDefaultDisplay().getSize(point);
        mediaThumbnailLoader = new MediaThumbnailLoader(requireContext(), Math.min(Constants.MAX_IMAGE_DIMENSION, Math.max(point.x, point.y)));
        chatLoader = new ChatLoader();
        contactLoader = new ContactLoader();
        seenByLoader = new SeenByLoader(requireContext());
        avatarLoader = AvatarLoader.getInstance();
        textContentLoader = new TextContentLoader(requireContext());
        ContactsDb.getInstance().addObserver(contactsObserver);
        timestampRefresher = new ViewModelProvider(this).get(TimestampRefresher.class);
        timestampRefresher.refresh.observe(this, value -> adapter.notifyDataSetChanged());
    }

    @CallSuper
    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaThumbnailLoader.destroy();
        contactLoader.destroy();
        chatLoader.destroy();
        seenByLoader.destroy();
        ContactsDb.getInstance().removeObserver(contactsObserver);
    }

    @CallSuper
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        drawDelegateView = requireActivity().findViewById(R.id.draw_delegate);
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

    protected boolean shouldOpenProfileOnNamePress() {
        return true;
    }

    protected class PostsAdapter extends HeaderFooterAdapter<Post> {

        static final int POST_TYPE_TEXT = 0x00;
        static final int POST_TYPE_MEDIA = 0x01;
        static final int POST_TYPE_RETRACTED = 0x02;
        static final int POST_TYPE_SYSTEM = 0x03;
        static final int POST_TYPE_MASK = 0xFF;

        static final int POST_DIRECTION_OUTGOING = 0x0000;
        static final int POST_DIRECTION_INCOMING = 0x0100;
        static final int POST_DIRECTION_MASK = 0xFF00;

        private boolean showGroup = true;

        private final PostViewHolder.PostViewHolderParent postViewHolderParent = new PostViewHolder.PostViewHolderParent() {

            @Override
            public AvatarLoader getAvatarLoader() {
                return avatarLoader;
            }

            @Override
            public ContactLoader getContactLoader() {
                return contactLoader;
            }

            @Override
            public ChatLoader getChatLoader() {
                return chatLoader;
            }

            @Override
            public SeenByLoader getSeenByLoader() {
                return seenByLoader;
            }

            @Override
            public TextContentLoader getTextContentLoader() {
                return textContentLoader;
            }

            @Override
            public DrawDelegateView getDrawDelegateView() {
                return drawDelegateView;
            }

            @Override
            public MediaThumbnailLoader getMediaThumbnailLoader() {
                return mediaThumbnailLoader;
            }

            @Override
            public LongSparseArray<Integer> getMediaPagerPositionMap() {
                return mediaPagerPositionMap;
            }

            @Override
            public LongSparseArray<Integer> getTextLimits() {
                return textLimits;
            }

            @Override
            public RecyclerView.RecycledViewPool getMediaViewPool() {
                return recycledMediaViews;
            }

            @Override
            public TimestampRefresher getTimestampRefresher() {
                return timestampRefresher;
            }

            @Override
            public void startActivity(@NonNull Intent intent) {
                PostsFragment.this.startActivity(intent);
            }

            @Override
            public void startActivity(@NonNull Intent intent, @NonNull ActivityOptionsCompat options) {
                PostsFragment.this.startActivity(intent, options.toBundle());
            }

            @Override
            public LifecycleOwner getLifecycleOwner() {
                return PostsFragment.this;
            }

            @Override
            public boolean shouldOpenProfileOnNamePress() {
                return PostsFragment.this.shouldOpenProfileOnNamePress();
            }
        };

        PostsAdapter() {
            super(new HeaderFooterAdapter.HeaderFooterAdapterParent() {
                @NonNull
                @Override
                public Context getContext() {
                    return requireContext();
                }

                @NonNull
                @Override
                public ViewGroup getParentViewGroup() {
                    return parentViewGroup;
                }
            });

            setHasStableIds(true);

            final AdapterListUpdateCallback adapterCallback = new AdapterListUpdateCallback(this);
            final ListUpdateCallback listUpdateCallback = new ListUpdateCallback() {

                public void onInserted(int position, int count) {
                    adapterCallback.onInserted(position + getHeaderCount(), count);
                }

                public void onRemoved(int position, int count) {
                    adapterCallback.onRemoved(position + getHeaderCount(), count);
                }

                public void onMoved(int fromPosition, int toPosition) {
                    adapterCallback.onMoved(fromPosition + getHeaderCount(), toPosition + 1);
                }

                public void onChanged(int position, int count, @Nullable Object payload) {
                    adapterCallback.onChanged(position + getHeaderCount(), count, payload);
                }
            };

            setDiffer(new AsyncPagedListDiffer<>(listUpdateCallback, new AsyncDifferConfig.Builder<>(DIFF_CALLBACK).build()));
        }

        public void setShowGroup(boolean showGroup) {
            this.showGroup = showGroup;
        }

        @Override
        public int getViewTypeForItem(Post post) {
            if (post.type == Post.TYPE_SYSTEM) {
                return POST_TYPE_SYSTEM;
            }
            return (post.isRetracted() ? POST_TYPE_RETRACTED : (post.media.isEmpty() ? POST_TYPE_TEXT : POST_TYPE_MEDIA)) |
                    (post.isOutgoing() ? POST_DIRECTION_OUTGOING : POST_DIRECTION_INCOMING);
        }

        @Override
        public long getIdForItem(Post post) {
            return post.rowId;
        }

        @NonNull
        @Override
        public ViewHolderWithLifecycle createViewHolderForViewType(@NonNull ViewGroup parent, int viewType) {
            int postType = viewType & POST_TYPE_MASK;
            if (postType == POST_TYPE_RETRACTED || postType == POST_TYPE_SYSTEM) {
                return new SubtlePostViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.centered_post_item, parent, false), postViewHolderParent);
            }
            View layout = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_item, parent, false);
            @LayoutRes int contentLayoutRes;
            switch (postType) {
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
            final ViewGroup footer = layout.findViewById(R.id.post_footer);
            switch (viewType & POST_DIRECTION_MASK) {
                case POST_DIRECTION_INCOMING: {
                    LayoutInflater.from(footer.getContext()).inflate(R.layout.post_footer_incoming, footer, true);
                    return new IncomingPostViewHolder(layout, postViewHolderParent);
                }
                case POST_DIRECTION_OUTGOING: {
                    LayoutInflater.from(footer.getContext()).inflate(R.layout.post_footer_outgoing, footer, true);
                    return new OutgoingPostViewHolder(layout, postViewHolderParent);
                }
                default: {
                    throw new IllegalArgumentException();
                }
            }
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolderWithLifecycle holder, int position) {
            if (holder instanceof PostViewHolder) {
                ((PostViewHolder)holder).setShowGroupName(showGroup);
                ((PostViewHolder)holder).bindTo(Preconditions.checkNotNull(getItem(position)));
            } else if (holder instanceof SubtlePostViewHolder) {
                ((SubtlePostViewHolder) holder).bindTo(Preconditions.checkNotNull(getItem(position)), position == 0);
            }
        }
    }
}
