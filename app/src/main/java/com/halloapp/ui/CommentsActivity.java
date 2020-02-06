package com.halloapp.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.collection.LongSparseArray;
import androidx.core.content.ContextCompat;
import androidx.core.util.Preconditions;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.AsyncPagedListDiffer;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.AdapterListUpdateCallback;
import androidx.recyclerview.widget.AsyncDifferConfig;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListUpdateCallback;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.BuildConfig;
import com.halloapp.Debug;
import com.halloapp.R;
import com.halloapp.contacts.ContactLoader;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.contacts.UserId;
import com.halloapp.media.MediaThumbnailLoader;
import com.halloapp.posts.Comment;
import com.halloapp.posts.Media;
import com.halloapp.posts.Post;
import com.halloapp.posts.PostsDb;
import com.halloapp.util.Log;
import com.halloapp.util.RandomId;
import com.halloapp.util.StringUtils;
import com.halloapp.util.TimeUtils;
import com.halloapp.widget.ActionBarShadowOnScrollListener;
import com.halloapp.widget.LimitingTextView;
import com.halloapp.widget.PostEditText;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CommentsActivity extends AppCompatActivity {

    public static final String EXTRA_POST_SENDER_USER_ID = "post_sender_user_id";
    public static final String EXTRA_POST_ID = "post_id";
    public static final String EXTRA_SHOW_KEYBOARD = "show_keyboard";
    public static final String EXTRA_NO_POST_LENGTH_LIMIT = "no_post_length_limit";

    private static final String KEY_REPLY_COMMENT_ID = "reply_comment_id";
    private static final String KEY_REPLY_USER_ID = "reply_user_id";

    private final CommentsAdapter adapter = new CommentsAdapter();
    private MediaThumbnailLoader mediaThumbnailLoader;
    private ContactLoader contactLoader;

    private CommentsViewModel viewModel;

    private String replyCommentId;
    private UserId replyUserId;

    private PostEditText editText;

    private static final long POST_TEXT_LIMITS_ID = -1;
    private final LongSparseArray<Integer> textLimits = new LongSparseArray<>();

    private long refreshTimestampsTime = Long.MAX_VALUE;
    private final Runnable refreshTimestampsRunnable = () -> {
        Log.v("CommentsActivity: refreshing timestamps at " + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.US).format(new Date(System.currentTimeMillis())));
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("CommentsActivity.onCreate");
        setContentView(R.layout.activity_comments);

        Preconditions.checkNotNull(getSupportActionBar()).setElevation(0);
        Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        final RecyclerView commentsView = findViewById(R.id.comments);
        commentsView.setItemAnimator(null);

        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        commentsView.setLayoutManager(layoutManager);

        commentsView.addOnScrollListener(new ActionBarShadowOnScrollListener(this));

        final UserId userId = new UserId(Preconditions.checkNotNull(getIntent().getStringExtra(EXTRA_POST_SENDER_USER_ID)));
        final String postId = Preconditions.checkNotNull(getIntent().getStringExtra(EXTRA_POST_ID));

        PostsDb.getInstance(this).setCommentsSeen(userId, postId);

        final View replyIndicator = findViewById(R.id.reply_indicator);
        final TextView replyIndicatorText = findViewById(R.id.reply_indicator_text);
        final View replyIndicatorCloseButton = findViewById(R.id.reply_indicator_close);

        viewModel = new ViewModelProvider(this, new CommentsViewModel.Factory(getApplication(), userId, postId)).get(CommentsViewModel.class);
        viewModel.commentList.observe(this, comments -> adapter.submitList(comments, () -> {
        }));

        viewModel.replyContact.observe(this, contact -> {
            if (contact == null) {
                replyIndicator.setVisibility(View.GONE);
            } else {
                replyIndicator.setVisibility(View.VISIBLE);
                if (contact.userId != null && contact.userId.isMe()) {
                    replyIndicatorText.setText(R.string.reply_to_self);
                } else {
                    replyIndicatorText.setText(getString(R.string.reply_to_contact, contact.getDisplayName()));
                }
            }
        });

        viewModel.post.observe(this, post -> adapter.notifyDataSetChanged());
        viewModel.loadPost(userId, postId);

        replyIndicatorCloseButton.setOnClickListener(v -> resetReplyIndicator());

        commentsView.setAdapter(adapter);

        editText = findViewById(R.id.entry);
        final View sendButton = findViewById(R.id.send);
        sendButton.setOnClickListener(v -> {
            final String postText = StringUtils.preparePostText(Preconditions.checkNotNull(editText.getText()).toString());
            if (TextUtils.isEmpty(postText)) {
                Log.w("CommentsActivity: cannot send empty comment");
                return;
            }
            final Comment comment = new Comment(
                    0,
                    userId,
                    postId,
                    UserId.ME,
                    RandomId.create(),
                    replyCommentId,
                    System.currentTimeMillis(),
                    false,
                    postText);
            PostsDb.getInstance(Preconditions.checkNotNull(getBaseContext())).addComment(comment);
            editText.setText(null);
            final InputMethodManager imm = Preconditions.checkNotNull((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE));
            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
            resetReplyIndicator();
        });

        if (getIntent().getBooleanExtra(EXTRA_NO_POST_LENGTH_LIMIT, false)) {
            textLimits.put(POST_TEXT_LIMITS_ID, Integer.MAX_VALUE);
        }

        if (getIntent().getBooleanExtra(EXTRA_SHOW_KEYBOARD, true)) {
            editText.requestFocus();
        }

        mediaThumbnailLoader = new MediaThumbnailLoader(this, 2 * getResources().getDimensionPixelSize(R.dimen.comment_media_list_height));
        contactLoader = new ContactLoader(this);

        ContactsDb.getInstance(this).addObserver(contactsObserver);

        if (savedInstanceState != null) {
            final String replyUser = savedInstanceState.getString(KEY_REPLY_USER_ID);
            final String replyCommentId = savedInstanceState.getString(KEY_REPLY_COMMENT_ID);
            if (replyUser != null && replyCommentId != null) {
                updateReplyIndicator(new UserId(replyUser), replyCommentId);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("CommentsActivity.onDestroy");
        ContactsDb.getInstance(this).removeObserver(contactsObserver);
        mediaThumbnailLoader.destroy();
        contactLoader.destroy();
        mainHandler.removeCallbacks(refreshTimestampsRunnable);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_REPLY_USER_ID, replyUserId == null ? null : replyUserId.rawId());
        outState.putString(KEY_REPLY_COMMENT_ID, replyCommentId);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (BuildConfig.DEBUG && keyCode == KeyEvent.KEYCODE_BACK) {
            final UserId userId = new UserId(Preconditions.checkNotNull(getIntent().getStringExtra(EXTRA_POST_SENDER_USER_ID)));
            final String postId = Preconditions.checkNotNull(getIntent().getStringExtra(EXTRA_POST_ID));
            Debug.showDebugMenu(this, editText, userId, postId);
            return true;
        }
        return super.onKeyLongPress(keyCode, event);
    }

    private void updateReplyIndicator(@NonNull UserId userId, @NonNull String commentId) {
        replyUserId = userId;
        replyCommentId = commentId;
        viewModel.loadReplyUser(userId);
    }

    private void resetReplyIndicator() {
        replyUserId = null;
        replyCommentId = null;
        viewModel.resetReplyUser();
    }

    private void scheduleTimestampRefresh(long postTimestamp) {
        long refreshTime = TimeUtils.getRefreshTime(postTimestamp);
        if (refreshTime < refreshTimestampsTime) {
            refreshTimestampsTime = refreshTime;
            Log.v("CommentsActivity: will refresh timestamps at " + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.US).format(new Date(refreshTimestampsTime)));
            mainHandler.removeCallbacks(refreshTimestampsRunnable);
            mainHandler.postDelayed(refreshTimestampsRunnable, refreshTimestampsTime - System.currentTimeMillis());
        }
    }

    private class ViewHolder extends RecyclerView.ViewHolder {

        final ImageView avatarView;
        final TextView nameView;
        final LimitingTextView commentView;
        final TextView timeView;
        final View progressView;
        final View replyButton;
        final RecyclerView mediaGallery;

        ViewHolder(final @NonNull View v) {
            super(v);
            avatarView = v.findViewById(R.id.avatar);
            nameView = v.findViewById(R.id.name);
            commentView = v.findViewById(R.id.comment_text);
            timeView = v.findViewById(R.id.time);
            progressView = v.findViewById(R.id.progress);
            replyButton = v.findViewById(R.id.reply);
            mediaGallery = v.findViewById(R.id.media);
        }

        void bindTo(final @NonNull Comment comment) {

            avatarView.setImageResource(R.drawable.avatar_person); // TODO (ds): load profile photo

            if (comment.isOutgoing()) {
                nameView.setText(nameView.getContext().getString(R.string.me));
            } else {
                contactLoader.load(nameView, comment.commentSenderUserId);
            }
            progressView.setVisibility(comment.transferred ? View.GONE : View.VISIBLE);
            timeView.setText(TimeUtils.formatTimeDiff(timeView.getContext(), System.currentTimeMillis() - comment.timestamp));
            scheduleTimestampRefresh(comment.timestamp);

            final Integer textLimit = textLimits.get(comment.rowId);
            if (textLimit != null) {
                commentView.setLimit(textLimit);
            } else {
                commentView.resetLimit();
            }
            commentView.setText(comment.text);
            commentView.setOnReadMoreListener((view, limit) -> {
                textLimits.put(comment.rowId, limit);
                return false;
            });

            replyButton.setOnClickListener(v -> {
                updateReplyIndicator(comment.commentSenderUserId, comment.commentId);
                editText.requestFocus();
                final InputMethodManager imm = Preconditions.checkNotNull((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE));
                imm.showSoftInput(editText,0);
            });
        }

        void bindTo(final @Nullable Post post) {
            if (post == null) {
                return;
            }
            avatarView.setImageResource(R.drawable.avatar_person); // TODO (ds): load profile photo

            if (post.isOutgoing()) {
                nameView.setText(nameView.getContext().getString(R.string.me));
            } else {
                contactLoader.load(nameView, post.senderUserId);
            }
            progressView.setVisibility(post.transferred ? View.GONE : View.VISIBLE);
            timeView.setText(TimeUtils.formatTimeDiff(timeView.getContext(), System.currentTimeMillis() - post.timestamp));
            scheduleTimestampRefresh(post.timestamp);

            if (post.media.isEmpty()) {
                mediaGallery.setVisibility(View.GONE);
            } else {
                mediaGallery.setVisibility(View.VISIBLE);
                final LinearLayoutManager layoutManager = new LinearLayoutManager(mediaGallery.getContext(), RecyclerView.HORIZONTAL, false);
                mediaGallery.setLayoutManager(layoutManager);
                if (mediaGallery.getItemDecorationCount() == 0) {
                    final DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mediaGallery.getContext(), layoutManager.getOrientation());
                    dividerItemDecoration.setDrawable(Preconditions.checkNotNull(ContextCompat.getDrawable(mediaGallery.getContext(), R.drawable.comment_media_list_spacing)));
                    mediaGallery.addItemDecoration(dividerItemDecoration);
                }
                mediaGallery.setAdapter(new CommentsAdapter(post.media));
            }

            final Integer textLimit = textLimits.get(POST_TEXT_LIMITS_ID);
            if (textLimit != null) {
                commentView.setLimit(textLimit);
            } else {
                commentView.resetLimit();
            }
            commentView.setOnReadMoreListener((view, limit) -> {
                textLimits.put(POST_TEXT_LIMITS_ID, limit);
                return false;
            });

            commentView.setVisibility(TextUtils.isEmpty(post.text) ? View.GONE : View.VISIBLE);
            commentView.setText(post.text);
        }

        private class PostMediaItemViewHolder extends RecyclerView.ViewHolder {

            PostMediaItemViewHolder(@NonNull View itemView) {
                super(itemView);
            }
        }

        private class CommentsAdapter extends RecyclerView.Adapter<PostMediaItemViewHolder> {

            final List<Media> media;

            CommentsAdapter(List<Media> media) {
                this.media = media;
            }

            @NonNull
            @Override
            public PostMediaItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                final ImageView imageView = new ImageView(parent.getContext());
                imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
                return new PostMediaItemViewHolder(imageView);
            }

            @Override
            public void onBindViewHolder(@NonNull PostMediaItemViewHolder holder, int position) {
                final ImageView imageView = (ImageView)holder.itemView;
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                imageView.setAdjustViewBounds(true);
                mediaThumbnailLoader.load(imageView, media.get(position));
            }

            @Override
            public int getItemCount() {
                return media.size();
            }
        }
    }

    private static final DiffUtil.ItemCallback<Comment> DIFF_CALLBACK = new DiffUtil.ItemCallback<Comment>() {

        @Override
        public boolean areItemsTheSame(Comment oldItem, Comment newItem) {
            // The ID property identifies when items are the same.
            return oldItem.rowId == newItem.rowId;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Comment oldItem, @NonNull Comment newItem) {
            return oldItem.equals(newItem);
        }
    };

    private static final int ITEM_TYPE_POST = 0;
    private static final int ITEM_TYPE_COMMENT = 1;
    private static final int ITEM_TYPE_REPLY = 2;

    private class CommentsAdapter extends RecyclerView.Adapter<ViewHolder> {

        AsyncPagedListDiffer<Comment> differ;

        CommentsAdapter() {
            setHasStableIds(true);

            AdapterListUpdateCallback adapterCallback = new AdapterListUpdateCallback(this);
            ListUpdateCallback listUpdateCallback = new ListUpdateCallback() {

                public void onInserted(int position, int count) {
                    adapterCallback.onInserted(position + 1, count);
                }

                public void onRemoved(int position, int count) {
                    adapterCallback.onRemoved(position + 1, count);
                }

                public void onMoved(int fromPosition, int toPosition) {
                    adapterCallback.onMoved(fromPosition + 1, toPosition + 1);
                }

                public void onChanged(int position, int count, @Nullable Object payload) {
                    adapterCallback.onChanged(position + 1, count, payload);
                }
            };

            differ = new AsyncPagedListDiffer<>(listUpdateCallback, new AsyncDifferConfig.Builder<>(DIFF_CALLBACK).build());
        }

        void submitList(@Nullable PagedList<Comment> pagedList) {
            differ.submitList(pagedList);
        }

        void submitList(@Nullable PagedList<Comment> pagedList, @Nullable final Runnable commitCallback) {
            differ.submitList(pagedList, commitCallback);
        }

        @Nullable
        public PagedList<Comment> getCurrentList() {
            return differ.getCurrentList();
        }

        @Override
        public long getItemId(int position) {
            return position == 0 ? -1 : Preconditions.checkNotNull(getItem(position)).rowId;
        }

        @Override
        public int getItemCount() {
            return 1 + differ.getItemCount();
        }

        @Nullable Comment getItem(int position) {
            return position == 0 ? null : differ.getItem(position - 1);
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return ITEM_TYPE_POST;
            }
            final Comment comment = Preconditions.checkNotNull(getItem(position));
            return comment.parentCommentId == null ? ITEM_TYPE_COMMENT : ITEM_TYPE_REPLY;
        }

        private @LayoutRes int getLayoutId(int viewType) {
            switch (viewType) {
                case ITEM_TYPE_POST:
                    return R.layout.comment_post_item;
                case ITEM_TYPE_COMMENT:
                    return R.layout.comment_item;
                case ITEM_TYPE_REPLY:
                    return R.layout.comment_reply_item;
            }
            throw new IllegalArgumentException("unknown view type " + viewType);
        }

        @Override
        public @NonNull ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(getLayoutId(viewType), parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            if (position == 0) {
                holder.bindTo(viewModel.post.getValue());
            } else {
                holder.bindTo(Preconditions.checkNotNull(getItem(position)));
            }
        }
    }
}
