package com.halloapp.ui;

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
import androidx.collection.LongSparseArray;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.ViewCompat;
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
import com.halloapp.content.Post;
import com.halloapp.groups.ChatLoader;
import com.halloapp.media.MediaThumbnailLoader;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.ui.mentions.TextContentLoader;
import com.halloapp.ui.posts.IncomingPostViewHolder;
import com.halloapp.ui.posts.OutgoingPostViewHolder;
import com.halloapp.ui.posts.PostViewHolder;
import com.halloapp.ui.posts.RetractedPostViewHolder;
import com.halloapp.ui.posts.SeenByLoader;
import com.halloapp.util.Preconditions;
import com.halloapp.widget.DrawDelegateView;
import com.halloapp.widget.NestedHorizontalScrollHelper;

public class PostContentActivity extends HalloActivity {

    public static final String EXTRA_POST_SENDER_USER_ID = "post_sender_user_id";
    public static final String EXTRA_POST_ID = "post_id";
    public static final String EXTRA_POST_MEDIA_INDEX = "post_media_index";

    private MediaThumbnailLoader mediaThumbnailLoader;
    private ChatLoader chatLoader;
    private ContactLoader contactLoader;
    private AvatarLoader avatarLoader;
    private SeenByLoader seenByLoader;
    private TextContentLoader textContentLoader;
    private TimestampRefresher timestampRefresher;

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
    };

    static final int POST_TYPE_TEXT = 0x00;
    static final int POST_TYPE_MEDIA = 0x01;
    static final int POST_TYPE_RETRACTED = 0x02;
    static final int POST_TYPE_MASK = 0xFF;

    static final int POST_DIRECTION_OUTGOING = 0x0000;
    static final int POST_DIRECTION_INCOMING = 0x0100;
    static final int POST_DIRECTION_MASK = 0xFF00;

    private final PostViewHolder.PostViewHolderParent postViewHolderParent = new PostViewHolder.PostViewHolderParent() {

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
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 28) {
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        // Allow showing content under the nav bar, we handle the padding ourselves
        getWindow().getDecorView().setSystemUiVisibility(SystemUiVisibility.getDefaultSystemUiVisibility(this) | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        setContentView(R.layout.activity_post_content);

        postRecyclerView = findViewById(R.id.post);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        postRecyclerView.setLayoutManager(linearLayoutManager);
        postAdapter = new SinglePostAdapter();
        postRecyclerView.setAdapter(postAdapter);
        NestedHorizontalScrollHelper.applyDefaultScrollRatio(postRecyclerView);

        View content = findViewById(R.id.content);
        content.setOnClickListener(v -> finishAfterTransition());

        final Drawable overlay = content.getBackground();

        ViewCompat.setOnApplyWindowInsetsListener(getWindow().getDecorView(), (v, insets) -> {
            content.setPadding(0, insets.getSystemWindowInsetTop(), 0, insets.getSystemWindowInsetBottom());
            InsetDrawable insetDrawable =
                    new InsetDrawable(overlay, insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(), insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
            content.setBackground(insetDrawable);
            return ViewCompat.onApplyWindowInsets(v, insets);
        });

        final String postId = Preconditions.checkNotNull(getIntent().getStringExtra(EXTRA_POST_ID));

        final PostContentViewModel viewModel = new ViewModelProvider(this, new PostContentViewModel.Factory(getApplication(), postId)).get(PostContentViewModel.class);

        final Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);
        mediaThumbnailLoader = new MediaThumbnailLoader(this, Math.min(Constants.MAX_IMAGE_DIMENSION, Math.max(point.x, point.y)));
        chatLoader = new ChatLoader();
        contactLoader = new ContactLoader();
        seenByLoader = new SeenByLoader(this);
        avatarLoader = AvatarLoader.getInstance();
        textContentLoader = new TextContentLoader(this);
        ContactsDb.getInstance().addObserver(contactsObserver);
        timestampRefresher = new ViewModelProvider(this).get(TimestampRefresher.class);
        timestampRefresher.refresh.observe(this, value -> updatePost());

        drawDelegateView = Preconditions.checkNotNull(this).findViewById(R.id.draw_delegate);

        viewModel.post.getLiveData().observe(this, this::showPost);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        chatLoader.destroy();
        mediaThumbnailLoader.destroy();
        contactLoader.destroy();
        seenByLoader.destroy();
        ContactsDb.getInstance().removeObserver(contactsObserver);
    }

    private void showPost(@Nullable Post post) {
        postAdapter.setPost(post);
    }

    private void updatePost() {
        postAdapter.notifyDataSetChanged();
    }

    private class SinglePostAdapter extends RecyclerView.Adapter<PostViewHolder> {

        private Post post;

        private boolean initialSelectionSet = false;

        public void setPost(@NonNull Post post) {
            this.post = post;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            final View layout = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_item, parent, false);
            if (BuildConfig.DEBUG) {
                layout.findViewById(R.id.time).setOnLongClickListener(v -> {
                    Debug.askSendLogsWithId(PostContentActivity.this, post.id);
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
                default: {
                    throw new IllegalArgumentException();
                }
            }
            final ViewGroup content = layout.findViewById(R.id.post_content);
            LayoutInflater.from(content.getContext()).inflate(contentLayoutRes, content, true);
            if ((viewType & POST_TYPE_MASK) == POST_TYPE_RETRACTED) {
                return new RetractedPostViewHolder(layout, postViewHolderParent);
            }
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
        public void onBindViewHolder(@NonNull PostViewHolder postViewHolder, int position) {
            postViewHolder.bindTo(post);
            if (!initialSelectionSet) {
                postViewHolder.selectMedia(getIntent().getIntExtra(EXTRA_POST_MEDIA_INDEX, 0));
                initialSelectionSet = true;
            }
        }

        @Override
        public int getItemViewType(int position) {
            return (post.isRetracted() ? POST_TYPE_RETRACTED : (post.media.isEmpty() ? POST_TYPE_TEXT : POST_TYPE_MEDIA)) |
                    (post.isOutgoing() ? POST_DIRECTION_OUTGOING : POST_DIRECTION_INCOMING);
        }

        @Override
        public int getItemCount() {
            return post == null ? 0 : 1;
        }
    }
}
