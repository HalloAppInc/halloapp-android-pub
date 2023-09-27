package com.halloapp.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.collection.LongSparseArray;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.BuildConfig;
import com.halloapp.Constants;
import com.halloapp.Debug;
import com.halloapp.R;
import com.halloapp.contacts.ContactLoader;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.contacts.FriendshipInfo;
import com.halloapp.content.ContentDb;
import com.halloapp.content.ContentItem;
import com.halloapp.content.Post;
import com.halloapp.content.Reaction;
import com.halloapp.groups.GroupLoader;
import com.halloapp.groups.MediaProgressLoader;
import com.halloapp.media.AudioDurationLoader;
import com.halloapp.media.MediaThumbnailLoader;
import com.halloapp.media.VoiceNotePlayer;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.ui.mentions.TextContentLoader;
import com.halloapp.ui.posts.ArchivedPostViewHolder;
import com.halloapp.ui.posts.FutureProofPostViewHolder;
import com.halloapp.ui.posts.IncomingPostFooterViewHolder;
import com.halloapp.ui.posts.MomentPostViewHolder;
import com.halloapp.ui.posts.OutgoingPostFooterViewHolder;
import com.halloapp.ui.posts.PostViewHolder;
import com.halloapp.ui.posts.RetractedPostViewHolder;
import com.halloapp.ui.posts.SeenByLoader;
import com.halloapp.ui.posts.VoiceNotePostViewHolder;
import com.halloapp.ui.profile.ViewProfileActivity;
import com.halloapp.util.DialogFragmentUtils;
import com.halloapp.util.Preconditions;
import com.halloapp.widget.DrawDelegateView;
import com.halloapp.widget.NestedHorizontalScrollHelper;

public class PostContentActivity extends HalloActivity {

    private static final String EXTRA_POST_ID = "post_id";
    private static final String EXTRA_POST_MEDIA_INDEX = "post_media_index";
    private static final String EXTRA_IS_ARCHIVED = "is_archived";
    private static final String EXTRA_SHARE_ID = "share_id";
    private static final String EXTRA_SHARE_KEY = "share_key";

    public static Intent open(@NonNull Context context, @NonNull String id, int replyMediaIndex) {
        final Intent intent = new Intent(context, PostContentActivity.class);
        intent.putExtra(EXTRA_POST_ID, id);
        intent.putExtra(EXTRA_POST_MEDIA_INDEX, replyMediaIndex);
        return intent;
    }

    public static Intent openArchived(@NonNull Context context, @NonNull String id) {
        Intent intent = new Intent(context, PostContentActivity.class);
        intent.putExtra(EXTRA_POST_ID, id);
        intent.putExtra(EXTRA_IS_ARCHIVED, true);
        return intent;
    }

    public static Intent openExternal(@NonNull Context context, @NonNull String shareId, @NonNull String key) {
        Intent intent = new Intent(context, PostContentActivity.class);
        intent.putExtra(EXTRA_SHARE_ID, shareId);
        intent.putExtra(EXTRA_SHARE_KEY, key);
        return intent;
    }

    private static final int SWIPE_DOWN_VELOCITY_THRESHOLD = -1000;

    private MediaThumbnailLoader mediaThumbnailLoader;
    private MediaProgressLoader mediaProgressLoader;
    private GroupLoader groupLoader;
    private ContactLoader contactLoader;
    private ReactionLoader reactionLoader;
    private AvatarLoader avatarLoader;
    private SeenByLoader seenByLoader;
    private TextContentLoader textContentLoader;
    private TimestampRefresher timestampRefresher;
    private AudioDurationLoader audioDurationLoader;
    private SystemMessageTextResolver systemMessageTextResolver;

    private String postId;
    private PostContentViewModel viewModel;

    private DrawDelegateView drawDelegateView;
    private final RecyclerView.RecycledViewPool recycledMediaViews = new RecyclerView.RecycledViewPool();

    private RecyclerView postRecyclerView;
    private SinglePostAdapter postAdapter;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final ContactsDb.Observer contactsObserver = new ContactsDb.BaseObserver() {
        @Override
        public void onContactsChanged() {
            contactLoader.resetCache();
            mainHandler.post(() -> updatePost());
        }

        @Override
        public void onFriendshipsChanged(@NonNull FriendshipInfo friendshipInfo) {
            contactLoader.resetCache();
            mainHandler.post(() -> updatePost());
        }
    };

    private final ContentDb.Observer contentObserver = new ContentDb.DefaultObserver() {
        @Override
        public void onReactionAdded(@NonNull Reaction reaction, @NonNull ContentItem contentItem) {
            if (reaction.contentId.equals(postId)) {
                RecyclerView.ViewHolder viewHolder = postRecyclerView.findViewHolderForItemId(contentItem.rowId);
                if (viewHolder instanceof PostViewHolder) {
                    ((PostViewHolder) viewHolder).reloadReactions();
                }
            }
        }
    };

    static final int POST_TYPE_TEXT = 0x00;
    static final int POST_TYPE_MEDIA = 0x01;
    static final int POST_TYPE_RETRACTED = 0x02;
    static final int POST_TYPE_FUTURE_PROOF = 0x03;
    static final int POST_TYPE_VOICE_NOTE = 0x04;
    static final int POST_TYPE_MOMENT = 0x05;
    static final int POST_TYPE_MASK = 0xFF;

    static final int POST_DIRECTION_OUTGOING = 0x0000;
    static final int POST_DIRECTION_INCOMING = 0x0100;
    static final int POST_DIRECTION_MASK = 0xFF00;

    private final PostViewHolder.PostViewHolderParent postViewHolderParent = new PostViewHolder.PostViewHolderParent() {

        @Override
        public void showDialogFragment(@NonNull DialogFragment dialogFragment) {
            DialogFragmentUtils.showDialogFragmentOnce(dialogFragment, getSupportFragmentManager());
        }

        @Override
        public VoiceNotePlayer getVoiceNotePlayer() {
            return viewModel.getVoiceNotePlayer();
        }

        private final LongSparseArray<Integer> mediaPagerPositionMap = new LongSparseArray<>();
        private final LongSparseArray<Integer> textLimits = new LongSparseArray<>();

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
            PostContentActivity.this.startActivity(intent);
        }

        @Override
        public void startActivity(@NonNull Intent intent, @NonNull ActivityOptionsCompat options) {
            PostContentActivity.this.startActivity(intent, options.toBundle());
        }

        @Override
        public LifecycleOwner getLifecycleOwner() {
            return PostContentActivity.this;
        }

        @Override
        public MediaProgressLoader getMediaProgressLoader() {
            return mediaProgressLoader;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 28) {
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        supportRequestWindowFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        supportRequestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        // Allow showing content under the nav bar, we handle the padding ourselves
        getWindow().getDecorView().setSystemUiVisibility(SystemUiVisibility.getDefaultSystemUiVisibility(this) | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        postponeEnterTransition();

        setContentView(R.layout.activity_post_content);
        ProgressDialog postLoadProgressDialog = ProgressDialog.show(this, null, getString(R.string.loading_post));

        postRecyclerView = findViewById(R.id.post);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        postRecyclerView.setLayoutManager(linearLayoutManager);
        postAdapter = new SinglePostAdapter();
        postRecyclerView.setAdapter(postAdapter);
        NestedHorizontalScrollHelper.applyDefaultScrollRatio(postRecyclerView);

        postRecyclerView.setOnFlingListener(new RecyclerView.OnFlingListener() {
            @Override
            public boolean onFling(int velocityX, int velocityY) {
                if (velocityY < SWIPE_DOWN_VELOCITY_THRESHOLD && postRecyclerView.computeVerticalScrollOffset() == 0) {
                    onBackPressed();
                }

                return false;
            }
        });

        View content = findViewById(R.id.content);
        content.setOnClickListener(v -> onBackPressed());

        final Drawable overlay = content.getBackground();

        ViewCompat.setOnApplyWindowInsetsListener(getWindow().getDecorView(), (v, insets) -> {
            content.setPadding(0, insets.getSystemWindowInsetTop(), 0, insets.getSystemWindowInsetBottom());
            InsetDrawable insetDrawable =
                    new InsetDrawable(overlay, insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(), insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
            content.setBackground(insetDrawable);
            return ViewCompat.onApplyWindowInsets(v, insets);
        });

        postId = getIntent().getStringExtra(EXTRA_POST_ID);
        final String shareId = getIntent().getStringExtra(EXTRA_SHARE_ID);
        final String shareKey = getIntent().getStringExtra(EXTRA_SHARE_KEY);
        final boolean isArchivedPost = getIntent().getBooleanExtra(EXTRA_IS_ARCHIVED, false);

        viewModel = new ViewModelProvider(this, new PostContentViewModel.Factory(getApplication(), postId, shareId, shareKey, isArchivedPost)).get(PostContentViewModel.class);

        viewModel.isRegistered.getLiveData().observe(this, registered -> {
            findViewById(R.id.join_button).setVisibility(Boolean.TRUE.equals(registered) ? View.GONE : View.VISIBLE);
        });
        viewModel.canInteract.observe(this, canInteract -> {
            postAdapter.setCanInteract(Boolean.TRUE.equals(canInteract));
        });

        final Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);
        int dimensionLimit = Math.min(Constants.MAX_IMAGE_DIMENSION, Math.max(point.x, point.y));
        mediaProgressLoader = new MediaProgressLoader();
        mediaThumbnailLoader = postId != null ? new MediaThumbnailLoader(this, dimensionLimit) : new ExternalMediaThumbnailLoader(this, dimensionLimit);
        groupLoader = new GroupLoader();
        contactLoader = new ContactLoader(userId -> {
            startActivity(ViewProfileActivity.viewProfile(this, userId));
            return null;
        });
        reactionLoader = new ReactionLoader();
        seenByLoader = new SeenByLoader();
        avatarLoader = AvatarLoader.getInstance();
        textContentLoader = new TextContentLoader();
        audioDurationLoader = new AudioDurationLoader(this);
        ContactsDb.getInstance().addObserver(contactsObserver);
        ContentDb.getInstance().addObserver(contentObserver);
        timestampRefresher = new ViewModelProvider(this).get(TimestampRefresher.class);
        timestampRefresher.refresh.observe(this, value -> updatePost());
        systemMessageTextResolver = new SystemMessageTextResolver(contactLoader);

        drawDelegateView = Preconditions.checkNotNull(this).findViewById(R.id.draw_delegate);

        viewModel.post.getLiveData().observe(this, post -> {
            postLoadProgressDialog.dismiss();
            if (post == null) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.post_no_longer_available_dialog_title);
                builder.setMessage(R.string.post_no_longer_available_dialog_message);
                builder.setPositiveButton(R.string.ok, (dialog, which) -> finish());
                builder.show();
                return;
            }

            showPost(post);
            if (post.media.size() > 0) {
                MediaPagerAdapter.preparePagerForTransition(postRecyclerView, post.id, getIntent().getIntExtra(EXTRA_POST_MEDIA_INDEX, 0), this::startPostponedEnterTransition);
            } else {
                startPostponedEnterTransition();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        groupLoader.destroy();
        mediaThumbnailLoader.destroy();
        mediaProgressLoader.destroy();
        contactLoader.destroy();
        reactionLoader.destroy();
        seenByLoader.destroy();
        ContactsDb.getInstance().removeObserver(contactsObserver);
        ContentDb.getInstance().removeObserver(contentObserver);
    }

    @Override
    public void onBackPressed() {
        String postId = getIntent().getStringExtra(EXTRA_POST_ID);
        if (postId != null) {
            int mediaIndex = getIntent().getIntExtra(EXTRA_POST_MEDIA_INDEX, 0);
            MediaPagerAdapter.preparePagerForTransition(postRecyclerView, postId, mediaIndex, this::finishAfterTransition);
        } else {
            finish();
        }
    }

    private void showPost(@Nullable Post post) {
        postAdapter.setPost(post);
    }

    private void updatePost() {
        postAdapter.notifyDataSetChanged();
    }

    private class SinglePostAdapter extends AdapterWithLifecycle<ViewHolderWithLifecycle> {

        private Post post;
        private boolean canInteract = true;

        private boolean initialSelectionSet = false;

        public void setPost(@NonNull Post post) {
            this.post = post;
            notifyDataSetChanged();
        }

        public void setCanInteract(boolean canInteract) {
            this.canInteract = canInteract;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolderWithLifecycle onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            final View layout = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_item, parent, false);
            if (BuildConfig.DEBUG) {
                layout.findViewById(R.id.time).setOnLongClickListener(v -> {
                    Debug.askSendLogsWithId(PostContentActivity.this, PostContentActivity.this, post.id);
                    return false;
                });
            }
            @LayoutRes int contentLayoutRes;
            switch (viewType & POST_TYPE_MASK) {
                case POST_TYPE_TEXT: {
                    contentLayoutRes = R.layout.post_item_text;
                    break;
                }
                case POST_TYPE_MEDIA: {
                    contentLayoutRes = R.layout.post_item_media;
                    break;
                }
                case POST_TYPE_RETRACTED: {
                    contentLayoutRes = R.layout.post_item_retracted;
                    break;
                }
                case POST_TYPE_FUTURE_PROOF: {
                    contentLayoutRes = R.layout.post_item_future_proof;
                    break;
                }
                case POST_TYPE_VOICE_NOTE: {
                    contentLayoutRes = R.layout.post_item_voice_note;
                    break;
                }
                case POST_TYPE_MOMENT: {
                    return new MomentPostViewHolder(LayoutInflater.from(PostContentActivity.this).inflate(R.layout.post_item_moment, parent, false), postViewHolderParent);
                }
                default: {
                    throw new IllegalArgumentException();
                }
            }
            final ViewGroup content = layout.findViewById(R.id.post_content);
            LayoutInflater.from(content.getContext()).inflate(contentLayoutRes, content, true);
            if ((viewType & POST_TYPE_MASK) == POST_TYPE_RETRACTED) {
                return new RetractedPostViewHolder(layout, postViewHolderParent);
            }
            if (post.isArchived) {
                return new ArchivedPostViewHolder(layout, postViewHolderParent);
            }
            final ViewGroup footer = layout.findViewById(R.id.post_footer);
            PostViewHolder postViewHolder;
            if ((viewType & POST_TYPE_MASK) == POST_TYPE_FUTURE_PROOF) {
                postViewHolder = new FutureProofPostViewHolder(layout, postViewHolderParent);
            } else {
                if ((viewType & POST_TYPE_MASK) == POST_TYPE_VOICE_NOTE) {
                    postViewHolder = new VoiceNotePostViewHolder(layout, postViewHolderParent);
                } else {
                    postViewHolder = new PostViewHolder(layout, postViewHolderParent);
                }
                switch (viewType & POST_DIRECTION_MASK) {
                    case POST_DIRECTION_INCOMING: {
                        LayoutInflater.from(footer.getContext()).inflate(R.layout.post_footer_incoming, footer, true);
                        postViewHolder.setFooter(new IncomingPostFooterViewHolder(layout, postViewHolderParent));
                        break;
                    }
                    case POST_DIRECTION_OUTGOING: {
                        LayoutInflater.from(footer.getContext()).inflate(R.layout.post_footer_outgoing, footer, true);
                        postViewHolder.setFooter(new OutgoingPostFooterViewHolder(layout, postViewHolderParent));
                        break;
                    }
                    default: {
                        throw new IllegalArgumentException();
                    }
                }
            }
            return postViewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolderWithLifecycle viewHolder, int position) {
            if (viewHolder instanceof  PostViewHolder) {
                PostViewHolder postViewHolder = (PostViewHolder) viewHolder;
                if (post.isArchived) {
                    postViewHolder.setShowGroupName(true);
                }
                postViewHolder.setBackupName(viewModel.backupName);
                postViewHolder.bindTo(post);
                postViewHolder.setCanInteract(canInteract);
                if (!initialSelectionSet) {
                    postViewHolder.selectMedia(getIntent().getIntExtra(EXTRA_POST_MEDIA_INDEX, 0));
                    initialSelectionSet = true;
                }
            } else if (viewHolder instanceof MomentPostViewHolder) {
                MomentPostViewHolder momentPostViewHolder = (MomentPostViewHolder) viewHolder;
                momentPostViewHolder.bindTo(post);
            }
        }

        @Override
        public int getItemViewType(int position) {
            int type = Post.TYPE_USER;
            switch (post.type) {
                case Post.TYPE_FUTURE_PROOF:
                    type = POST_TYPE_FUTURE_PROOF;
                    break;
                case Post.TYPE_USER: {
                    type = post.media.isEmpty() ? POST_TYPE_TEXT : POST_TYPE_MEDIA;
                    break;
                }
                case Post.TYPE_RETRACTED_MOMENT:
                case Post.TYPE_RETRACTED: {
                    type = POST_TYPE_RETRACTED;
                    break;
                }
                case Post.TYPE_VOICE_NOTE: {
                    type = POST_TYPE_VOICE_NOTE;
                    break;
                }
                case Post.TYPE_MOMENT_PSA:
                case Post.TYPE_MOMENT: {
                    type = POST_TYPE_MOMENT;
                    break;
                }
            }
            if (post.isRetracted()) {
                type = POST_TYPE_RETRACTED;
            }
            return type |
                    (post.isOutgoing() ? POST_DIRECTION_OUTGOING : POST_DIRECTION_INCOMING);
        }

        @Override
        public int getItemCount() {
            return post == null ? 0 : 1;
        }
    }
}
