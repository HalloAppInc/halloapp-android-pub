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
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.AdapterListUpdateCallback;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListUpdateCallback;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.Constants;
import com.halloapp.R;
import com.halloapp.contacts.ContactLoader;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.contacts.FriendshipInfo;
import com.halloapp.content.ContentDb;
import com.halloapp.content.ContentItem;
import com.halloapp.content.MomentPost;
import com.halloapp.content.Post;
import com.halloapp.content.Reaction;
import com.halloapp.groups.GroupLoader;
import com.halloapp.groups.MediaProgressLoader;
import com.halloapp.media.AudioDurationLoader;
import com.halloapp.media.MediaThumbnailLoader;
import com.halloapp.media.VoiceNotePlayer;
import com.halloapp.props.ServerProps;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.ui.mentions.TextContentLoader;
import com.halloapp.ui.moments.MomentsStackLayout;
import com.halloapp.ui.profile.ViewProfileActivity;
import com.halloapp.ui.posts.CollapsedPostViewHolder;
import com.halloapp.ui.posts.FutureProofPostViewHolder;
import com.halloapp.ui.posts.IncomingPostFooterViewHolder;
import com.halloapp.ui.posts.MomentEntryViewHolder;
import com.halloapp.ui.posts.MomentPostViewHolder;
import com.halloapp.ui.posts.OutgoingPostFooterViewHolder;
import com.halloapp.ui.posts.PostFooterViewHolder;
import com.halloapp.ui.posts.PostListDiffer;
import com.halloapp.ui.posts.PostViewHolder;
import com.halloapp.ui.posts.SeenByLoader;
import com.halloapp.ui.posts.SubtlePostViewHolder;
import com.halloapp.ui.posts.TombstonePostViewHolder;
import com.halloapp.ui.posts.VoiceNotePostViewHolder;
import com.halloapp.ui.posts.ZeroZonePostViewHolder;
import com.halloapp.util.DialogFragmentUtils;
import com.halloapp.util.Preconditions;
import com.halloapp.widget.DrawDelegateView;

import java.util.List;

public abstract class PostsFragment extends HalloFragment {

    protected final PostsAdapter adapter = createAdapter();
    protected ViewGroup parentViewGroup;

    private MediaThumbnailLoader mediaThumbnailLoader;
    private MediaProgressLoader mediaProgressLoader;
    private GroupLoader groupLoader;
    private ContactLoader contactLoader;
    private ReactionLoader reactionLoader;
    private AvatarLoader avatarLoader;
    private ServerProps serverProps;
    private SeenByLoader seenByLoader;
    private TextContentLoader textContentLoader;
    private TimestampRefresher timestampRefresher;
    private AudioDurationLoader audioDurationLoader;
    private SystemMessageTextResolver systemMessageTextResolver;

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

        @Override
        public void onFriendshipsChanged(@NonNull FriendshipInfo friendshipInfo) {
            contactLoader.resetCache();
            mainHandler.post(adapter::notifyDataSetChanged);
        }
    };

    private final ContentDb.Observer contentObserver = new ContentDb.DefaultObserver() {
        @Override
        public void onReactionAdded(@NonNull Reaction reaction, @NonNull ContentItem contentItem) {
            RecyclerView recyclerView = getRecyclerView();
            if (recyclerView != null && contentItem instanceof Post) {
                RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForItemId(contentItem.rowId);
                if (viewHolder instanceof PostViewHolder) {
                    ((PostViewHolder) viewHolder).reloadReactions();
                }
            }
        }
    };

    @CallSuper
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Point point = new Point();
        requireActivity().getWindowManager().getDefaultDisplay().getSize(point);
        mediaThumbnailLoader = new MediaThumbnailLoader(requireContext(), Math.min(Constants.MAX_IMAGE_DIMENSION, Math.max(point.x, point.y)));
        mediaProgressLoader = new MediaProgressLoader();
        groupLoader = new GroupLoader();
        contactLoader = new ContactLoader(userId -> {
            startActivity(ViewProfileActivity.viewProfile(requireContext(), userId));
            return null;
        });
        reactionLoader = new ReactionLoader();
        seenByLoader = new SeenByLoader();
        avatarLoader = AvatarLoader.getInstance();
        serverProps = ServerProps.getInstance();
        audioDurationLoader = new AudioDurationLoader(requireContext());
        textContentLoader = new TextContentLoader();
        ContactsDb.getInstance().addObserver(contactsObserver);
        ContentDb.getInstance().addObserver(contentObserver);
        timestampRefresher = new ViewModelProvider(this).get(TimestampRefresher.class);
        timestampRefresher.refresh.observe(this, value -> adapter.notifyDataSetChanged());
        systemMessageTextResolver = new SystemMessageTextResolver(contactLoader);

        adapter.addMomentsHeader();
    }

    @CallSuper
    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaThumbnailLoader.destroy();
        mediaProgressLoader.destroy();
        contactLoader.destroy();
        reactionLoader.destroy();
        groupLoader.destroy();
        seenByLoader.destroy();
        ContactsDb.getInstance().removeObserver(contactsObserver);
        ContentDb.getInstance().removeObserver(contentObserver);
    }

    @CallSuper
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        drawDelegateView = requireActivity().findViewById(R.id.draw_delegate);
    }

    protected PostsAdapter createAdapter() {
        return new PostsAdapter();
    }

    protected abstract RecyclerView getRecyclerView();

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
        static final int POST_TYPE_FUTURE_PROOF = 0x04;
        static final int POST_TYPE_ZERO_ZONE_HOME = 0x05;
        static final int POST_TYPE_ZERO_ZONE_GROUP = 0x06;
        static final int POST_TYPE_VOICE_NOTE = 0x07;
        static final int POST_TYPE_COLLAPSED = 0x08;
        protected static final int POST_TYPE_INVITE_CARD = 0x09;
        static final int POST_TYPE_TOMBSTONE = 0x0a;
        static final int POST_TYPE_MOMENT = 0x0b;
        static final int POST_TYPE_MOMENT_ENTRY = 0x0c;
        static final int POST_TYPE_MASK = 0xFF;

        static final int POST_DIRECTION_OUTGOING = 0x0000;
        static final int POST_DIRECTION_INCOMING = 0x0100;
        static final int POST_DIRECTION_MASK = 0xFF00;

        private boolean showGroup = true;

        private int theme;

        private final PostListDiffer postListDiffer;
        private MomentsStackLayout momentsHeaderView;

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
            public ReactionLoader getReactionLoader() {
                return reactionLoader;
            }

            @Override
            public SystemMessageTextResolver getSystemMessageTextResolver() {
                return systemMessageTextResolver;
            }

            @Override
            public GroupLoader getGroupLoader() {
                return groupLoader;
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
            public AudioDurationLoader getAudioDurationLoader() {
                return audioDurationLoader;
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

            @Override
            public void showDialogFragment(@NonNull DialogFragment dialogFragment) {
                DialogFragmentUtils.showDialogFragmentOnce(dialogFragment, getParentFragmentManager());
            }

            @Override
            public VoiceNotePlayer getVoiceNotePlayer() {
                return PostsFragment.this.getVoiceNotePlayer();
            }

            @Override
            public MediaProgressLoader getMediaProgressLoader() {
                return mediaProgressLoader;
            }
        };

        protected PostsAdapter() {
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

            final ListUpdateCallback listUpdateCallback = createUpdateCallback();

            postListDiffer = new PostListDiffer(listUpdateCallback);
            setDiffer(postListDiffer);
        }

        protected ListUpdateCallback createUpdateCallback() {
            final AdapterListUpdateCallback adapterCallback = new AdapterListUpdateCallback(this);
            return new ListUpdateCallback() {

                public void onInserted(int position, int count) {
                    position = translateToAdapterPos(position);
                    adapterCallback.onInserted(position, count);
                }

                public void onRemoved(int position, int count) {
                    position = translateToAdapterPos(position);
                    adapterCallback.onRemoved(position, count);
                }

                public void onMoved(int fromPosition, int toPosition) {
                    fromPosition = translateToAdapterPos(fromPosition);
                    toPosition = translateToAdapterPos(toPosition);
                    adapterCallback.onMoved(fromPosition, toPosition);
                }

                public void onChanged(int position, int count, @Nullable Object payload) {
                    position = translateToAdapterPos(position);
                    adapterCallback.onChanged(position, count, payload);
                }
            };
        }

        protected int translateToAdapterPos(int position) {
            position += getHeaderCount();
            return position;
        }

        protected int getInviteCardIndex() {
            return -1;
        }

        public void applyTheme(int theme) {
            if (this.theme != theme) {
                this.theme = theme;
                notifyDataSetChanged();
            }
        }

        public void setShowGroup(boolean showGroup) {
            this.showGroup = showGroup;
        }

        @Override
        public int getViewTypeForItem(Post post) {
            if (post instanceof PostListDiffer.PostCollection) {
                return POST_TYPE_COLLAPSED;
            } else if (post instanceof PostListDiffer.ExpandedPost) {
                post = ((PostListDiffer.ExpandedPost) post).wrappedPost;
            }
            boolean isGroupPost = post.getParentGroup() != null;
            boolean showTombstoneIfDecryptFailed = (isGroupPost && !serverProps.getUsePlaintextGroupFeed()) || (!isGroupPost && !serverProps.getUsePlaintextHomeFeed());
            if (post.transferred == Post.TRANSFERRED_DECRYPT_FAILED && showTombstoneIfDecryptFailed) {
                return POST_TYPE_TOMBSTONE;
            }
            int type = Post.TYPE_USER;
            switch (post.type) {
                case Post.TYPE_SYSTEM:
                    return POST_TYPE_SYSTEM;
                case Post.TYPE_FUTURE_PROOF:
                    type = POST_TYPE_FUTURE_PROOF;
                    break;
                case Post.TYPE_USER: {
                    type = post.media.isEmpty() ? POST_TYPE_TEXT : POST_TYPE_MEDIA;
                    break;
                }
                case Post.TYPE_RETRACTED:
                    type = POST_TYPE_RETRACTED;
                    break;
                case Post.TYPE_ZERO_ZONE:
                    type = post.getParentGroup() != null ? POST_TYPE_ZERO_ZONE_GROUP : POST_TYPE_ZERO_ZONE_HOME;
                    break;
                case Post.TYPE_VOICE_NOTE:
                    type = POST_TYPE_VOICE_NOTE;
                    break;
                case Post.TYPE_MOMENT:
                case Post.TYPE_MOMENT_PSA:
                    type = POST_TYPE_MOMENT;
                    break;
                case Post.TYPE_MOMENT_ENTRY:
                    type = POST_TYPE_MOMENT_ENTRY;
                    break;
            }
            if (post.isRetracted()) {
                type = POST_TYPE_RETRACTED;
            }
            return type |
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
            if (postType == POST_TYPE_COLLAPSED) {
                return new CollapsedPostViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.centered_post_item, parent, false), postViewHolderParent);
            }
            if (postType == POST_TYPE_MOMENT) {
                return new MomentPostViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.post_item_moment, parent, false), postViewHolderParent);
            }
            if (postType == POST_TYPE_MOMENT_ENTRY) {
                return new MomentEntryViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.post_item_moment, parent, false), postViewHolderParent);
            }
            if (postType == POST_TYPE_ZERO_ZONE_HOME) {
                return new ZeroZonePostViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.post_item_zero_zone_home, parent, false), postViewHolderParent);
            } else if (postType == POST_TYPE_ZERO_ZONE_GROUP) {
                return new ZeroZonePostViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.post_item_zero_zone_group, parent, false), postViewHolderParent);
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
                case POST_TYPE_FUTURE_PROOF: {
                    contentLayoutRes = R.layout.post_item_future_proof;
                    break;
                }
                case POST_TYPE_TOMBSTONE: {
                    contentLayoutRes = R.layout.post_item_tombstone;
                    break;
                }
                case POST_TYPE_VOICE_NOTE: {
                    contentLayoutRes = R.layout.post_item_voice_note;
                    break;
                }
                default: {
                    throw new IllegalArgumentException();
                }
            }
            final ViewGroup content = layout.findViewById(R.id.post_content);
            LayoutInflater.from(content.getContext()).inflate(contentLayoutRes, content, true);
            final ViewGroup footer = layout.findViewById(R.id.post_footer);
            PostFooterViewHolder postFooterViewHolder = null;
            switch (viewType & POST_DIRECTION_MASK) {
                case POST_DIRECTION_INCOMING: {
                    LayoutInflater.from(footer.getContext()).inflate(R.layout.post_footer_incoming, footer, true);
                    postFooterViewHolder = new IncomingPostFooterViewHolder(layout, postViewHolderParent);
                    break;
                }
                case POST_DIRECTION_OUTGOING: {
                    LayoutInflater.from(footer.getContext()).inflate(R.layout.post_footer_outgoing, footer, true);
                    postFooterViewHolder = new OutgoingPostFooterViewHolder(layout, postViewHolderParent);
                    break;
                }
                default: {
                    throw new IllegalArgumentException();
                }
            }
            PostViewHolder postViewHolder;
            switch (viewType & POST_TYPE_MASK) {
                case POST_TYPE_FUTURE_PROOF:
                    postViewHolder = new FutureProofPostViewHolder(layout, postViewHolderParent);
                    break;
                case POST_TYPE_TOMBSTONE:
                    postViewHolder = new TombstonePostViewHolder(layout, postViewHolderParent);
                    break;
                case POST_TYPE_VOICE_NOTE:
                    postViewHolder = new VoiceNotePostViewHolder(layout, postViewHolderParent);
                    break;
                default:
                    postViewHolder = new PostViewHolder(layout, postViewHolderParent);
            }
            postViewHolder.setFooter(postFooterViewHolder);
            return postViewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolderWithLifecycle holder, int position) {
            if (holder instanceof PostViewHolder) {
                PostViewHolder postViewHolder = (PostViewHolder) holder;
                postViewHolder.setShowGroupName(showGroup);
                postViewHolder.setCardBackgroundColor(theme == 0 ? 0 : R.color.post_card_color_background);
                postViewHolder.bindTo(Preconditions.checkNotNull(getItem(position)));
            } else if (holder instanceof SubtlePostViewHolder) {
                SubtlePostViewHolder postViewHolder = (SubtlePostViewHolder) holder;
                postViewHolder.applyTheme(theme);
                Post post = Preconditions.checkNotNull(getItem(position));
                if (post instanceof PostListDiffer.ExpandedPost) {
                    final PostListDiffer.ExpandedPost expandedPost = (PostListDiffer.ExpandedPost) post;
                    holder.itemView.setOnClickListener(v -> {
                        postListDiffer.collapse(expandedPost.parent.getAdapterIndex());
                    });
                    post = expandedPost.wrappedPost;
                } else {
                    holder.itemView.setOnClickListener(null);
                }
                postViewHolder.bindTo(post, position == 0);
            } else if (holder instanceof ZeroZonePostViewHolder) {
                ZeroZonePostViewHolder postViewHolder = (ZeroZonePostViewHolder) holder;
                postViewHolder.bindTo(Preconditions.checkNotNull(getItem(position)), position == 0);
            } else if (holder instanceof CollapsedPostViewHolder) {
                CollapsedPostViewHolder postViewHolder = (CollapsedPostViewHolder) holder;
                postViewHolder.applyTheme(theme);
                PostListDiffer.PostCollection postCollection = (PostListDiffer.PostCollection) getItem(position);
                postViewHolder.bindTo(postCollection, position == 0);
                postViewHolder.itemView.setOnClickListener(v -> {
                    postListDiffer.expand(postCollection.parent.getAdapterIndex());
                });
            } else if (holder instanceof MomentPostViewHolder) {
                MomentPostViewHolder postViewHolder = (MomentPostViewHolder) holder;
                postViewHolder.bindTo(getItem(position));
            }
        }

        public void addMomentsHeader() {
            momentsHeaderView = (MomentsStackLayout) addHeader(R.layout.moment_stack);
            momentsHeaderView.load(postViewHolderParent);

            hideMoments();
        }

        private void hideMoments() {
            ViewGroup.LayoutParams params = momentsHeaderView.getLayoutParams();
            if (params == null) {
                params = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
            } else {
                params.height = 0;
            }

            momentsHeaderView.setLayoutParams(params);
        }

        private void showMoments() {
            ViewGroup.LayoutParams params = momentsHeaderView.getLayoutParams();
            if (params == null) {
                params = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
            } else {
                params.height = RecyclerView.LayoutParams.WRAP_CONTENT;
            }

            momentsHeaderView.setLayoutParams(params);
        }

        public void setMoments(List<MomentPost> moments) {
            if (moments != null && moments.size() > 0) {
                momentsHeaderView.bindTo(moments);
                showMoments();
            } else {
                hideMoments();
            }
        }
    }

    protected abstract VoiceNotePlayer getVoiceNotePlayer();
}
