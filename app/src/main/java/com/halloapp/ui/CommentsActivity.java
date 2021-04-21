package com.halloapp.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Outline;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.collection.LongSparseArray;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.app.SharedElementCallback;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.AsyncPagedListDiffer;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.AdapterListUpdateCallback;
import androidx.recyclerview.widget.AsyncDifferConfig;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListUpdateCallback;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.BuildConfig;
import com.halloapp.Constants;
import com.halloapp.Debug;
import com.halloapp.R;
import com.halloapp.contacts.ContactLoader;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.content.Comment;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Media;
import com.halloapp.content.Mention;
import com.halloapp.content.Post;
import com.halloapp.id.UserId;
import com.halloapp.media.MediaThumbnailLoader;
import com.halloapp.props.ServerProps;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.ui.mediaexplorer.MediaExplorerActivity;
import com.halloapp.ui.mediaexplorer.MediaExplorerViewModel;
import com.halloapp.ui.mediapicker.MediaPickerActivity;
import com.halloapp.ui.mentions.MentionPickerView;
import com.halloapp.ui.mentions.TextContentLoader;
import com.halloapp.util.ActivityUtils;
import com.halloapp.util.Preconditions;
import com.halloapp.util.RandomId;
import com.halloapp.util.StringUtils;
import com.halloapp.util.TimeFormatter;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.ActionBarShadowOnScrollListener;
import com.halloapp.widget.ItemSwipeHelper;
import com.halloapp.widget.LimitingTextView;
import com.halloapp.widget.LinearSpacingItemDecoration;
import com.halloapp.widget.MentionableEntry;
import com.halloapp.widget.RecyclerViewKeyboardScrollHelper;
import com.halloapp.widget.SwipeListItemHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CommentsActivity extends HalloActivity {

    public static final String EXTRA_POST_SENDER_USER_ID = "post_sender_user_id";
    public static final String EXTRA_POST_ID = "post_id";
    public static final String EXTRA_REPLY_USER_ID = "reply_user_id";
    public static final String EXTRA_REPLY_COMMENT_ID = "reply_comment_id";
    public static final String EXTRA_SHOW_KEYBOARD = "show_keyboard";
    public static final String EXTRA_NO_POST_LENGTH_LIMIT = "no_post_length_limit";

    private static final String KEY_REPLY_COMMENT_ID = "reply_comment_id";
    private static final String KEY_REPLY_USER_ID = "reply_user_id";

    private static final int REQUEST_CODE_PICK_MEDIA = 1;

    private final CommentsAdapter adapter = new CommentsAdapter();
    private MediaThumbnailLoader mediaThumbnailLoader;
    private AvatarLoader avatarLoader;
    private ContactLoader contactLoader;
    private TextContentLoader textContentLoader;

    private CommentsViewModel viewModel;

    private String replyCommentId;
    private UserId replyUserId;

    private MentionableEntry editText;
    private MentionPickerView mentionPickerView;
    private ImageView sendButton;

    private ItemSwipeHelper itemSwipeHelper;
    private RecyclerViewKeyboardScrollHelper keyboardScrollHelper;

    private static final long POST_TEXT_LIMITS_ID = -1;
    private final LongSparseArray<Integer> textLimits = new LongSparseArray<>();

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final ContactsDb.Observer contactsObserver = new ContactsDb.BaseObserver() {
        @Override
        public void onContactsChanged() {
            contactLoader.resetCache();
            mainHandler.post(adapter::notifyDataSetChanged);
        }
    };

    private TimestampRefresher timestampRefresher;

    private boolean showKeyboardOnResume;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        setExitSharedElementCallback(new SharedElementCallback() {
            @Override
            public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                Post post = viewModel.post.getValue();
                RecyclerView gallery = findViewById(R.id.comments).findViewWithTag(post);
                RecyclerView.LayoutManager layoutManager = gallery.getLayoutManager();

                String name = names.get(0);
                for (int i = 0; i < post.media.size(); ++i) {
                    View view = layoutManager.findViewByPosition(i);

                    if (view != null && name.equals(view.getTransitionName())) {
                        sharedElements.put(name, view);
                        return;
                    }
                }

                super.onMapSharedElements(names, sharedElements);
            }
        });

        final RecyclerView commentsView = findViewById(R.id.comments);
        commentsView.setItemAnimator(null);

        mentionPickerView = findViewById(R.id.mention_picker_view);

        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        commentsView.setLayoutManager(layoutManager);

        commentsView.addOnScrollListener(new ActionBarShadowOnScrollListener(this));

        keyboardScrollHelper = new RecyclerViewKeyboardScrollHelper(commentsView);

        final String postId = Preconditions.checkNotNull(getIntent().getStringExtra(EXTRA_POST_ID));

        final View replyIndicator = findViewById(R.id.reply_indicator);
        final TextView replyIndicatorText = findViewById(R.id.reply_indicator_text);
        final View replyIndicatorCloseButton = findViewById(R.id.reply_indicator_close);

        viewModel = new ViewModelProvider(this, new CommentsViewModel.Factory(getApplication(), postId)).get(CommentsViewModel.class);
        viewModel.commentList.observe(this, comments -> adapter.submitList(comments, () -> {
        }));

        viewModel.lastSeenCommentRowId.getLiveData().observe(this, rowId -> adapter.setLastSeenCommentRowId(rowId == null ? -1 : rowId));

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
                if (editText != null && TextUtils.isEmpty(editText.getText()) && !contact.userId.isMe()) {
                    editText.appendMention(contact);
                }
            }
        });

        View mediaContainer = findViewById(R.id.media_container);
        ImageView imageView = findViewById(R.id.media_preview);
        viewModel.commentMedia.observe(this, media -> {
            if (media == null) {
                mediaContainer.setVisibility(View.GONE);
            } else {
                mediaContainer.setVisibility(View.VISIBLE);
                mediaThumbnailLoader.load(imageView, media);
            }
            updateSendButtonColor();
        });
        View removeMedia = findViewById(R.id.remove);
        removeMedia.setOnClickListener(v -> viewModel.resetCommentMediaUri());

        viewModel.postDeleted.observe(this, deleted -> {
            if (Boolean.TRUE.equals(deleted)) {
                finish();
            }
        });

        viewModel.mentionableContacts.getLiveData().observe(this, contacts -> {
            mentionPickerView.setMentionableContacts(contacts);
        });

        viewModel.post.observe(this, post -> {
            adapter.notifyDataSetChanged();
            viewModel.mentionableContacts.invalidate();
        });
        viewModel.loadPost(postId);

        replyIndicatorCloseButton.setOnClickListener(v -> resetReplyIndicator());

        commentsView.setAdapter(adapter);
        editText = findViewById(R.id.entry_card);
        editText.setMentionPickerView(mentionPickerView);
        sendButton = findViewById(R.id.send);
        sendButton.setOnClickListener(v -> {
            final Pair<String, List<Mention>> textWithMentions = editText.getTextWithMentions();
            final String postText = StringUtils.preparePostText(textWithMentions.first);
            if (TextUtils.isEmpty(postText) && viewModel.commentMedia.getValue() == null) {
                Log.w("CommentsActivity: cannot send empty comment");
                return;
            }
            viewModel.sendComment(postText, textWithMentions.second, replyCommentId, ActivityUtils.supportsWideColor(this));
            editText.setText(null);
            final InputMethodManager imm = Preconditions.checkNotNull((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE));
            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
            resetReplyIndicator();
        });

        final View media = findViewById(R.id.media);
        media.setOnClickListener(v -> pickMedia());
        media.setVisibility(ServerProps.getInstance().getIsInternalUser() ? View.VISIBLE : View.GONE);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                updateSendButtonColor();
            }
        });

        if (getIntent().getBooleanExtra(EXTRA_NO_POST_LENGTH_LIMIT, false)) {
            textLimits.put(POST_TEXT_LIMITS_ID, Integer.MAX_VALUE);
        }

        if (getIntent().getBooleanExtra(EXTRA_SHOW_KEYBOARD, true)) {
            showKeyboardOnResume = true;
        }

        mediaThumbnailLoader = new MediaThumbnailLoader(this, 2 * getResources().getDimensionPixelSize(R.dimen.comment_media_list_height));
        contactLoader = new ContactLoader();
        avatarLoader = AvatarLoader.getInstance();
        textContentLoader = new TextContentLoader(this);

        timestampRefresher = new ViewModelProvider(this).get(TimestampRefresher.class);
        timestampRefresher.refresh.observe(this, value -> adapter.notifyDataSetChanged());

        ContactsDb.getInstance().addObserver(contactsObserver);

        final String replyUser;
        final String replyCommentId;
        if (savedInstanceState != null) {
            replyUser = savedInstanceState.getString(KEY_REPLY_USER_ID);
            replyCommentId = savedInstanceState.getString(KEY_REPLY_COMMENT_ID);
        } else {
            replyUser = getIntent().getStringExtra(EXTRA_REPLY_USER_ID);
            replyCommentId = getIntent().getStringExtra(EXTRA_REPLY_COMMENT_ID);
        }
        if (replyUser != null && replyCommentId != null) {
            updateReplyIndicator(new UserId(replyUser), replyCommentId);
        }

        itemSwipeHelper = new ItemSwipeHelper(new SwipeListItemHelper(
                Preconditions.checkNotNull(getDrawable(R.drawable.ic_delete_white)),
                ContextCompat.getColor(this, R.color.swipe_delete_background),
                getResources().getDimensionPixelSize(R.dimen.swipe_delete_icon_margin)) {

            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                if (viewHolder.getItemViewType() == ITEM_TYPE_POST) {
                    return makeMovementFlags(0);
                }
                return super.getMovementFlags(recyclerView, viewHolder);
            }

            @Override
            public boolean canSwipe(@NonNull RecyclerView.ViewHolder viewHolder) {
                final Comment comment = ((ViewHolder)viewHolder).comment;
                return comment != null && comment.senderUserId.isMe() && !comment.isRetracted();
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // workaround to reset swiped out view
                adapter.notifyItemChanged(viewHolder.getAdapterPosition());
                itemSwipeHelper.attachToRecyclerView(null);
                itemSwipeHelper.attachToRecyclerView(commentsView);

                final Comment comment = ((ViewHolder)viewHolder).comment;
                if (comment == null || !comment.senderUserId.isMe() || comment.isRetracted()) {
                    return;
                }
                final AlertDialog.Builder builder = new AlertDialog.Builder(CommentsActivity.this);
                builder.setMessage(getBaseContext().getString(R.string.retract_comment_confirmation));
                builder.setCancelable(true);
                builder.setPositiveButton(R.string.yes, (dialog, which) -> ContentDb.getInstance().retractComment(comment));
                builder.setNegativeButton(R.string.no, null);
                builder.show();
            }
        });
        itemSwipeHelper.attachToRecyclerView(commentsView);
    }

    private void updateSendButtonColor() {
        Editable e = editText.getText();
        String s = e == null ? null : e.toString();
        Media m = viewModel.commentMedia.getValue();
        if (m != null || !TextUtils.isEmpty(s)) {
            sendButton.setColorFilter(ContextCompat.getColor(CommentsActivity.this, R.color.color_secondary));
        } else {
            sendButton.clearColorFilter();
        }
    }

    private void pickMedia() {
        final Intent intent = new Intent(this, MediaPickerActivity.class);
        intent.putExtra(MediaPickerActivity.EXTRA_PICKER_PURPOSE, MediaPickerActivity.PICKER_PURPOSE_COMMENT);
        startActivityForResult(intent, REQUEST_CODE_PICK_MEDIA);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_PICK_MEDIA: {
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        final ArrayList<Uri> uris = data.getParcelableArrayListExtra(CropImageActivity.EXTRA_MEDIA);
                        if (uris.size() == 1) {
                            viewModel.loadCommentMediaUri(uris.get(0));
                        } else {
                            Log.w("CommentsActivity: Invalid comment media count " + uris.size());
                        }
                    }
                }
                break;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (showKeyboardOnResume) {
            editText.postDelayed(this::showKeyboard, Constants.KEYBOARD_SHOW_DELAY);

            showKeyboardOnResume = false;
        }
    }

    private void showKeyboard() {
        editText.requestFocus();
        final InputMethodManager imm = Preconditions.checkNotNull((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE));
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ContactsDb.getInstance().removeObserver(contactsObserver);
        mediaThumbnailLoader.destroy();
        contactLoader.destroy();
    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        super.onActivityReenter(resultCode, data);

        if (resultCode == RESULT_OK && data.hasExtra(MediaExplorerActivity.EXTRA_CONTENT_ID) && data.hasExtra(MediaExplorerActivity.EXTRA_SELECTED)) {
            String contentId = data.getStringExtra(MediaExplorerActivity.EXTRA_CONTENT_ID);
            int position = data.getIntExtra(MediaExplorerActivity.EXTRA_SELECTED, 0);
            Post post = viewModel.post.getValue();

            if (!post.id.equals(contentId)) {
                return;
            }

            RecyclerView gallery = findViewById(R.id.comments).findViewWithTag(post);
            RecyclerView.LayoutManager layoutManager = gallery.getLayoutManager();
            View view = layoutManager.findViewByPosition(position);

            if (view == null || layoutManager.isViewPartiallyVisible(view, false, true)) {
                postponeEnterTransition();

                gallery.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        gallery.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        startPostponedEnterTransition();
                    }
                });

                layoutManager.scrollToPosition(position);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_REPLY_USER_ID, replyUserId == null ? null : replyUserId.rawId());
        outState.putString(KEY_REPLY_COMMENT_ID, replyCommentId);
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
        viewModel.resetCommentMediaUri();
    }

    private class ViewHolder extends ViewHolderWithLifecycle {

        final ImageView avatarView;
        final TextView nameView;
        final ImageView commentMedia;
        final LimitingTextView commentView;
        final TextView timeView;
        final View progressView;
        final View replyButton;
        final View retractButton;
        final RecyclerView mediaGallery;
        final CardView cardView;

        Comment comment;

        ViewHolder(final @NonNull View v) {
            super(v);
            avatarView = v.findViewById(R.id.avatar);
            nameView = v.findViewById(R.id.name);
            commentMedia = v.findViewById(R.id.comment_media);
            commentView = v.findViewById(R.id.comment_text);
            timeView = v.findViewById(R.id.time);
            progressView = v.findViewById(R.id.progress);
            replyButton = v.findViewById(R.id.reply);
            retractButton = v.findViewById(R.id.retract);
            mediaGallery = v.findViewById(R.id.media);
            cardView = v.findViewById(R.id.comment);
            if (mediaGallery != null) {
                final LinearLayoutManager layoutManager = new LinearLayoutManager(mediaGallery.getContext(), RecyclerView.HORIZONTAL, false);
                mediaGallery.setLayoutManager(layoutManager);
                mediaGallery.addItemDecoration(new LinearSpacingItemDecoration(layoutManager, getResources().getDimensionPixelSize(R.dimen.comment_media_list_spacing)));
            }
        }

        void bindTo(final @NonNull Comment comment, long lastSeenCommentRowId, int position) {

            this.comment = comment;

            avatarLoader.load(avatarView, comment.senderUserId);
            contactLoader.load(nameView, comment.senderUserId);

            progressView.setVisibility(comment.transferred ? View.GONE : View.VISIBLE);
            TimeFormatter.setTimePostsFormat(timeView, comment.timestamp);
            timestampRefresher.scheduleTimestampRefresh(comment.timestamp);

            final Integer textLimit = textLimits.get(comment.rowId);
            commentView.setLineLimit(textLimit != null ? textLimit : Constants.TEXT_POST_LINE_LIMIT);
            commentView.setLineLimitTolerance(textLimit != null ? Constants.POST_LINE_LIMIT_TOLERANCE : 0);
            if (comment.isRetracted()) {
                commentView.setText(getString(R.string.comment_retracted_placeholder));
                commentView.setTextAppearance(commentView.getContext(), R.style.CommentTextAppearanceRetracted);
                replyButton.setVisibility(View.GONE);
                retractButton.setVisibility(View.GONE);
            } else {
                textContentLoader.load(commentView, comment);

                commentView.setVisibility(TextUtils.isEmpty(comment.text) ? View.GONE : View.VISIBLE);

                if (StringUtils.isFewEmoji(comment.text)) {
                    commentView.setTextAppearance(commentView.getContext(), R.style.CommentTextAppearanceFewEmoji);
                } else {
                    commentView.setTextAppearance(commentView.getContext(), R.style.CommentTextAppearanceNormal);
                }

                replyButton.setVisibility(View.VISIBLE);
                retractButton.setVisibility(comment.canBeRetracted() ? View.VISIBLE : View.GONE);
            }
            commentView.setOnReadMoreListener((view, limit) -> {
                textLimits.put(comment.rowId, limit);
                return false;
            });

            if (comment.media.isEmpty()) {
                commentMedia.setVisibility(View.GONE);
            } else {
                commentMedia.setVisibility(View.VISIBLE);
                mediaThumbnailLoader.load(commentMedia, comment.media.get(0));
            }

            cardView.setCardBackgroundColor(ContextCompat.getColor(cardView.getContext(), comment.rowId <= lastSeenCommentRowId ? R.color.seen_comment_background : R.color.card_background));

            replyButton.setOnClickListener(v -> {
                keyboardScrollHelper.setAnchorForKeyboardChange(position);
                updateReplyIndicator(comment.senderUserId, comment.id);
                editText.requestFocus();
                final InputMethodManager imm = Preconditions.checkNotNull((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE));
                imm.showSoftInput(editText,0);
            });

            retractButton.setOnClickListener(v -> {
                final Context context = itemView.getContext();
                final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage(context.getString(R.string.retract_comment_confirmation));
                builder.setCancelable(true);
                builder.setPositiveButton(R.string.yes, (dialog, which) -> ContentDb.getInstance().retractComment(comment));
                builder.setNegativeButton(R.string.no, null);
                builder.show();
            });
        }

        void bindTo(final @Nullable Post post) {

            this.comment = null;

            if (post == null) {
                return;
            }
            avatarLoader.load(avatarView, post.senderUserId);
            contactLoader.load(nameView, post.senderUserId);

            progressView.setVisibility(post.transferred != Post.TRANSFERRED_NO ? View.GONE : View.VISIBLE);
            TimeFormatter.setTimePostsFormat(timeView, post.timestamp);
            timestampRefresher.scheduleTimestampRefresh(post.timestamp);

            mediaGallery.setTag(post);
            if (post.media.isEmpty()) {
                mediaGallery.setVisibility(View.GONE);
            } else {
                mediaGallery.setVisibility(View.VISIBLE);
                mediaGallery.setAdapter(new MediaAdapter(post));
            }

            final Integer textLimit = textLimits.get(POST_TEXT_LIMITS_ID);
            commentView.setLineLimit(textLimit != null ? textLimit : Constants.TEXT_POST_LINE_LIMIT);
            commentView.setLineLimitTolerance(textLimit != null ? Constants.POST_LINE_LIMIT_TOLERANCE : 0);
            commentView.setOnReadMoreListener((view, limit) -> {
                textLimits.put(POST_TEXT_LIMITS_ID, limit);
                return false;
            });

            commentView.setVisibility(TextUtils.isEmpty(post.text) ? View.GONE : View.VISIBLE);
            textContentLoader.load(commentView, post);
        }

        private class PostMediaItemViewHolder extends RecyclerView.ViewHolder {

            PostMediaItemViewHolder(@NonNull View itemView) {
                super(itemView);
            }
        }

        private class MediaAdapter extends RecyclerView.Adapter<PostMediaItemViewHolder> {

            final List<Media> media;
            final Post post;

            MediaAdapter(Post post) {
                this.post = post;
                this.media = post.media;
            }

            @NonNull
            @Override
            public PostMediaItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                final ImageView imageView = new ImageView(parent.getContext());
                imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
                imageView.setOutlineProvider(new ViewOutlineProvider() {
                    @Override
                    public void getOutline(View view, Outline outline) {
                        outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), getResources().getDimension(R.dimen.comment_media_list_corner_radius));
                    }
                });
                imageView.setClipToOutline(true);
                return new PostMediaItemViewHolder(imageView);
            }

            @Override
            public void onBindViewHolder(@NonNull PostMediaItemViewHolder holder, int position) {
                final ImageView imageView = (ImageView)holder.itemView;
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                imageView.setAdjustViewBounds(true);
                mediaThumbnailLoader.load(imageView, media.get(position));
                imageView.setOnClickListener(v -> {
                    Intent intent = new Intent(imageView.getContext(), MediaExplorerActivity.class);
                    intent.putExtra(MediaExplorerActivity.EXTRA_MEDIA, MediaExplorerViewModel.MediaModel.fromMedia(media));
                    intent.putExtra(MediaExplorerActivity.EXTRA_SELECTED, position);
                    intent.putExtra(MediaExplorerActivity.EXTRA_CONTENT_ID, post.id);

                    if (imageView.getContext() instanceof Activity) {
                        final ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(CommentsActivity.this, imageView, imageView.getTransitionName());
                        startActivity(intent, options.toBundle());
                    } else {
                        startActivity(intent);
                    }
                });

                imageView.setTransitionName(MediaPagerAdapter.getTransitionName(post.id, position));
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

    private class CommentsAdapter extends AdapterWithLifecycle<ViewHolder> {

        final AsyncPagedListDiffer<Comment> differ;
        private long lastSeenCommentRowId;

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

        public void setLastSeenCommentRowId(long rowId) {
            if (lastSeenCommentRowId != rowId) {
                lastSeenCommentRowId = rowId;
                notifyDataSetChanged();
            }
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
                holder.bindTo(Preconditions.checkNotNull(getItem(position)), lastSeenCommentRowId, position);
            }
        }
    }
}
