package com.halloapp.ui;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Outline;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.text.style.TextAppearanceSpan;
import android.text.style.URLSpan;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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
import com.halloapp.ContentDraftManager;
import com.halloapp.Debug;
import com.halloapp.R;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactLoader;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.content.Chat;
import com.halloapp.content.Comment;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Media;
import com.halloapp.content.Mention;
import com.halloapp.content.Post;
import com.halloapp.groups.ChatLoader;
import com.halloapp.id.ChatId;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.media.AudioDurationLoader;
import com.halloapp.media.MediaThumbnailLoader;
import com.halloapp.props.ServerProps;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.ui.groups.ViewGroupFeedActivity;
import com.halloapp.ui.mediaedit.MediaEditActivity;
import com.halloapp.ui.mediaexplorer.MediaExplorerActivity;
import com.halloapp.ui.mediaexplorer.MediaExplorerViewModel;
import com.halloapp.ui.mediapicker.MediaPickerActivity;
import com.halloapp.ui.mentions.MentionPickerView;
import com.halloapp.ui.mentions.MentionsFormatter;
import com.halloapp.ui.mentions.TextContentLoader;
import com.halloapp.ui.posts.PostAttributionLayout;
import com.halloapp.ui.profile.ViewProfileActivity;
import com.halloapp.util.ActivityUtils;
import com.halloapp.util.Preconditions;
import com.halloapp.util.StringUtils;
import com.halloapp.util.TimeFormatter;
import com.halloapp.util.ViewDataLoader;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.ActionBarShadowOnScrollListener;
import com.halloapp.widget.ChatInputView;
import com.halloapp.widget.ItemSwipeHelper;
import com.halloapp.widget.LimitingTextView;
import com.halloapp.widget.LinearSpacingItemDecoration;
import com.halloapp.widget.MentionableEntry;
import com.halloapp.widget.RecyclerViewKeyboardScrollHelper;
import com.halloapp.widget.SnackbarHelper;
import com.halloapp.widget.SwipeListItemHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import pub.devrel.easypermissions.EasyPermissions;

public class FlatCommentsActivity extends HalloActivity implements EasyPermissions.PermissionCallbacks {

    public static final String EXTRA_POST_SENDER_USER_ID = "post_sender_user_id";
    public static final String EXTRA_POST_ID = "post_id";
    public static final String EXTRA_REPLY_USER_ID = "reply_user_id";
    public static final String EXTRA_REPLY_COMMENT_ID = "reply_comment_id";
    public static final String EXTRA_SHOW_KEYBOARD = "show_keyboard";
    public static final String EXTRA_NO_POST_LENGTH_LIMIT = "no_post_length_limit";

    private static final String KEY_REPLY_COMMENT_ID = "reply_comment_id";
    private static final String KEY_REPLY_USER_ID = "reply_user_id";

    private static final int REQUEST_CODE_PICK_MEDIA = 1;

    private static final int REQUEST_PERMISSION_CODE_RECORD_VOICE_NOTE = 1;

    private final ServerProps serverProps = ServerProps.getInstance();
    private final ContentDraftManager contentDraftManager = ContentDraftManager.getInstance();

    private final CommentsAdapter adapter = new CommentsAdapter();
    private MediaThumbnailLoader mediaThumbnailLoader;
    private ChatLoader chatLoader;
    private AvatarLoader avatarLoader;
    private ContactLoader contactLoader;
    private TextContentLoader textContentLoader;

    private FlatCommentsViewModel viewModel;

    private String replyCommentId;
    private UserId replyUserId;

    private MentionableEntry editText;
    private MentionPickerView mentionPickerView;
    private View membershipNotice;

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

    private boolean enterTransitionComplete;
    private boolean showKeyboardAfterEnter;
    private boolean scrollToComment = false;
    private int commentsFsePosition;

    private boolean isInternalGroup;

    private AudioDurationLoader audioDurationLoader;

    private ChatInputView chatInputView;

    private String postId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        setExitSharedElementCallback(new SharedElementCallback() {
            @Override
            public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                RecyclerView comments = findViewById(R.id.comments);
                String name = names.get(0);

                if (commentsFsePosition == 0) {
                    Post post = Preconditions.checkNotNull(viewModel.post.getValue());
                    RecyclerView gallery = comments.findViewWithTag(post);
                    RecyclerView.LayoutManager layoutManager = Preconditions.checkNotNull(gallery.getLayoutManager());

                    for (int i = 0; i < post.media.size(); ++i) {
                        View view = layoutManager.findViewByPosition(i);

                        if (view != null && name.equals(view.getTransitionName())) {
                            sharedElements.put(name, view);
                            return;
                        }
                    }
                } else {
                    RecyclerView.LayoutManager commentsLayoutManager = Preconditions.checkNotNull(comments.getLayoutManager());
                    View row = commentsLayoutManager.getChildAt(commentsFsePosition);

                    if (row != null) {
                        View view = row.findViewById(R.id.comment_media);

                        if (view != null && name.equals(view.getTransitionName())) {
                            sharedElements.put(name, view);
                            return;
                        }
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

        postId = Preconditions.checkNotNull(getIntent().getStringExtra(EXTRA_POST_ID));

        final View replyIndicator = findViewById(R.id.reply_indicator);
        final TextView replyIndicatorText = findViewById(R.id.reply_indicator_text);
        final View replyIndicatorCloseButton = findViewById(R.id.reply_indicator_close);

        viewModel = new ViewModelProvider(this, new FlatCommentsViewModel.Factory(getApplication(), postId)).get(FlatCommentsViewModel.class);
        viewModel.getCommentList().observe(this, comments -> {
            adapter.submitList(comments, () -> commentsView.post(() -> {
                if (!scrollToComment) {
                    return;
                }
                scrollToComment = false;
                long currentOldest = 0;
                int commentPosition = -1;
                for (int i = 0; i < comments.size(); i++) {
                    Comment comment = Preconditions.checkNotNull(comments.get(i));
                    if (comment.senderUserId.isMe() && comment.timestamp > currentOldest) {
                        commentPosition = i;
                        currentOldest = comment.timestamp;
                    }
                }
                if (commentPosition != -1) {
                    commentsView.smoothScrollToPosition(commentPosition+1);
                }
            }));
        });

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
            }
        });

        chatInputView = findViewById(R.id.chat_input);
        chatInputView.setVoiceNoteControlView(findViewById(R.id.recording_ui));
        chatInputView.setInputParent(new ChatInputView.InputParent() {
            @Override
            public void onSendText() {
                final Pair<String, List<Mention>> textWithMentions = editText.getTextWithMentions();
                final String postText = StringUtils.preparePostText(textWithMentions.first);
                if (TextUtils.isEmpty(postText) && viewModel.commentMedia.getValue() == null) {
                    Log.w("CommentsActivity: cannot send empty comment");
                    return;
                }
                viewModel.sendComment(postText, textWithMentions.second, replyCommentId, ActivityUtils.supportsWideColor(FlatCommentsActivity.this));
                editText.setText(null);
                final InputMethodManager imm = Preconditions.checkNotNull((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE));
                imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                resetReplyIndicator();
                scrollToComment = true;
            }

            @Override
            public void onSendVoiceNote() {
                viewModel.finishRecording(replyCommentId, false);
                resetReplyIndicator();
                scrollToComment = true;
            }

            @Override
            public void onSendVoiceDraft(File draft) {
                viewModel.sendVoiceNote(replyCommentId, draft);
                resetReplyIndicator();
                scrollToComment = true;
            }

            @Override
            public void onChooseMedia() {
                pickMedia();
            }

            @Override
            public void requestVoicePermissions() {
                EasyPermissions.requestPermissions(FlatCommentsActivity.this, getString(R.string.voice_note_record_audio_permission_rationale), REQUEST_PERMISSION_CODE_RECORD_VOICE_NOTE, Manifest.permission.RECORD_AUDIO);
            }
        });
        chatInputView.bindVoicePlayer(this, viewModel.getVoiceNotePlayer());
        chatInputView.bindVoiceRecorder(this, viewModel.getVoiceNoteRecorder());

        View mediaContainer = findViewById(R.id.media_container);
        ImageView imageView = findViewById(R.id.media_preview);
        imageView.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), getResources().getDimension(R.dimen.comment_media_preview_corner_radius));
            }
        });
        imageView.setClipToOutline(true);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setAdjustViewBounds(true);

        viewModel.commentMedia.observe(this, media -> {
            if (media == null) {
                mediaContainer.setVisibility(View.GONE);
            } else {
                mediaContainer.setVisibility(View.VISIBLE);
                mediaThumbnailLoader.load(imageView, media);
            }
            updateSendButton();
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
            isInternalGroup = serverProps.getIsInternalUser() &&
                    post != null &&
                    post.getParentGroup() != null;
            chatInputView.setAllowMedia(isInternalGroup);
            chatInputView.setAllowVoiceNoteRecording(serverProps.getVoiceNoteSendingEnabled() && isInternalGroup);
        });
        viewModel.loadPost(postId);

        membershipNotice = findViewById(R.id.membership_layout);
        viewModel.isMember.getLiveData().observe(this, isMember -> {
            adapter.notifyDataSetChanged();
            if (!isMember) {
                chatInputView.setVisibility(View.INVISIBLE);
                membershipNotice.setVisibility(View.VISIBLE);
                editText.setFocusableInTouchMode(false);
                editText.setFocusable(false);
                replyIndicator.setVisibility(View.GONE);
                final InputMethodManager imm = Preconditions.checkNotNull((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE));
                imm.hideSoftInputFromWindow(editText.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
                showKeyboardAfterEnter = false;
            } else {
                chatInputView.setVisibility(View.VISIBLE);
                membershipNotice.setVisibility(View.INVISIBLE);
                editText.setFocusableInTouchMode(true);
                editText.setFocusable(true);
            }
        });

        replyIndicatorCloseButton.setOnClickListener(v -> resetReplyIndicator());

        commentsView.setAdapter(adapter);
        editText = findViewById(R.id.entry_card);
        editText.setMentionPickerView(mentionPickerView);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                updateSendButton();
            }
        });

        if (getIntent().getBooleanExtra(EXTRA_NO_POST_LENGTH_LIMIT, false)) {
            textLimits.put(POST_TEXT_LIMITS_ID, Integer.MAX_VALUE);
        }

        if (getIntent().getBooleanExtra(EXTRA_SHOW_KEYBOARD, true)) {
            showKeyboard();
        }

        mediaThumbnailLoader = new MediaThumbnailLoader(this, 2 * getResources().getDimensionPixelSize(R.dimen.comment_media_list_height));
        contactLoader = new ContactLoader();
        avatarLoader = AvatarLoader.getInstance(this);
        chatLoader = new ChatLoader();
        textContentLoader = new TextContentLoader();

        timestampRefresher = new ViewModelProvider(this).get(TimestampRefresher.class);
        timestampRefresher.refresh.observe(this, value -> adapter.notifyDataSetChanged());

        audioDurationLoader = new AudioDurationLoader(this);

        ContactsDb.getInstance().addObserver(contactsObserver);

        if (savedInstanceState != null) {
            updateReplyIndicator(savedInstanceState.getString(KEY_REPLY_USER_ID), savedInstanceState.getString(KEY_REPLY_COMMENT_ID));
        } else {
            updateReplyIndicator(getIntent().getStringExtra(EXTRA_REPLY_USER_ID), getIntent().getStringExtra(EXTRA_REPLY_COMMENT_ID));
        }

        itemSwipeHelper = new ItemSwipeHelper(new SwipeListItemHelper(
                Preconditions.checkNotNull(ContextCompat.getDrawable(this, R.drawable.ic_delete_white)),
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
                final AlertDialog.Builder builder = new AlertDialog.Builder(FlatCommentsActivity.this);
                builder.setMessage(getBaseContext().getString(R.string.retract_comment_confirmation));
                builder.setCancelable(true);
                builder.setPositiveButton(R.string.yes, (dialog, which) -> ContentDb.getInstance().retractComment(comment));
                builder.setNegativeButton(R.string.no, null);
                builder.show();
            }
        });
        itemSwipeHelper.attachToRecyclerView(commentsView);
    }

    private void updateSendButton() {
        Editable e = editText.getText();
        String s = e == null ? null : e.toString();
        Media m = viewModel.commentMedia.getValue();
        boolean canSend = m != null || !TextUtils.isEmpty(s);
        chatInputView.setCanSend(canSend);
    }

    @Override
    protected void onResume() {
        super.onResume();

        File audioDraft = contentDraftManager.getCommentAudioDraft(postId);
        if (audioDraft != null) {
            chatInputView.bindAudioDraft(audioDurationLoader, audioDraft);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        File draft = chatInputView.getAudioDraft();
        contentDraftManager.setCommentAudioDraft(postId, draft);
    }

    private void pickMedia() {
        final Intent intent = MediaPickerActivity.pickForComment(this);
        startActivityForResult(intent, REQUEST_CODE_PICK_MEDIA);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //noinspection SwitchStatementWithTooFewBranches
        switch (requestCode) {
            case REQUEST_CODE_PICK_MEDIA: {
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        final ArrayList<Uri> uris = data.getParcelableArrayListExtra(MediaEditActivity.EXTRA_MEDIA);
                        if (uris == null) {
                            Log.e("CommentsActivity: Got null media");
                        } else if (uris.size() == 1) {
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

    private void showKeyboard() {
        if (!enterTransitionComplete) {
            showKeyboardAfterEnter = true;
            return;
        }
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
            RecyclerView.LayoutManager layoutManager = Preconditions.checkNotNull(gallery.getLayoutManager());
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
    public void onEnterAnimationComplete() {
        super.onEnterAnimationComplete();

        enterTransitionComplete = true;
        if (showKeyboardAfterEnter) {
            showKeyboard();
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

    private void updateReplyIndicator(@Nullable String rawUserId, @Nullable String commentId) {
        if (rawUserId == null || commentId == null) {
            return;
        }
        replyUserId = new UserId(rawUserId);
        replyCommentId = commentId;
        viewModel.loadReplyUser(replyUserId);
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

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
    }

    private class VoiceNoteViewHolder extends ViewHolder {

        Comment comment;

        private final SeekBar seekBar;
        private final ImageView controlButton;
        private final TextView seekTime;

        private boolean playing;

        private String audioPath;
        private boolean wasPlaying;

        public VoiceNoteViewHolder(@NonNull View itemView) {
            super(itemView);

            seekBar = itemView.findViewById(R.id.voice_note_seekbar);
            controlButton = itemView.findViewById(R.id.control_btn);
            seekTime = itemView.findViewById(R.id.seek_time);

            controlButton.setOnClickListener(v -> {
                if (playing) {
                    viewModel.getVoiceNotePlayer().pause();
                } else if (audioPath != null) {
                    viewModel.getVoiceNotePlayer().playFile(audioPath, seekBar.getProgress());
                }
            });
        }

        @Override
        public void markRecycled() {
            super.markRecycled();
            audioPath = null;
        }

        void bindTo(final @NonNull Comment comment, long lastSeencommentRowId, int position) {
            this.comment = comment;
            bindCommon(comment, lastSeencommentRowId, position);
            if (comment.media != null && !comment.media.isEmpty()) {
                Media media = comment.media.get(0);
                if (media.transferred == Media.TRANSFERRED_YES) {
                    if (media.file != null) {
                        String newPath = media.file.getAbsolutePath();
                        if (!newPath.equals(audioPath)) {
                            this.audioPath = media.file.getAbsolutePath();
                            audioDurationLoader.load(seekTime, media);
                            startObservingPlayback();
                        }
                    }
                    controlButton.setVisibility(View.VISIBLE);
                } else {
                    controlButton.setVisibility(View.INVISIBLE);
                }
            }
        }

        private void startObservingPlayback() {
            viewModel.getVoiceNotePlayer().getPlaybackState().observe(this, state -> {
                if (state == null || audioPath == null || !audioPath.equals(state.playingTag)) {
                    return;
                }
                playing = state.playing;
                if (playing) {
                    controlButton.setImageResource(R.drawable.ic_pause);
                    seekTime.setText(StringUtils.formatVoiceNoteDuration(seekTime.getContext(), state.seek));
                } else {
                    controlButton.setImageResource(R.drawable.ic_play_arrow);
                    seekTime.setText(StringUtils.formatVoiceNoteDuration(seekTime.getContext(), state.seekMax));
                }
                seekBar.setMax(state.seekMax);
                seekBar.setProgress(state.seek);
                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        wasPlaying = playing;
                        if (playing) {
                            viewModel.getVoiceNotePlayer().pause();
                        }
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        if (wasPlaying) {
                            viewModel.getVoiceNotePlayer().playFile(audioPath, seekBar.getProgress());
                        }
                    }
                });
            });
        }

    }

    private class FutureProofViewHolder extends ViewHolder {

        Comment comment;

        public FutureProofViewHolder(@NonNull View itemView) {
            super(itemView);

            TextView futureProofMessage = itemView.findViewById(R.id.future_proof_text);

            SpannableStringBuilder current= new SpannableStringBuilder(futureProofMessage.getText());
            URLSpan[] spans= current.getSpans(0, current.length(), URLSpan.class);

            int linkColor = ContextCompat.getColor(futureProofMessage.getContext(), R.color.color_link);

            for (URLSpan span : spans) {
                int start = current.getSpanStart(span);
                int end = current.getSpanEnd(span);
                current.removeSpan(span);

                ClickableSpan learnMoreSpan = new ClickableSpan() {
                    @Override
                    public void updateDrawState(@NonNull TextPaint ds) {
                        ds.setUnderlineText(false);
                        ds.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
                        ds.setColor(linkColor);
                    }

                    @Override
                    public void onClick(@NonNull View widget) {
                        try {
                            final Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID));
                            intent.setPackage("com.android.vending");
                            startActivity(intent);
                        } catch (ActivityNotFoundException e) {
                            Log.i("CommentsActivity Play Store Not Installed", e);
                            SnackbarHelper.showWarning(futureProofMessage,  R.string.app_expiration_no_play_store);
                        }
                    }
                };
                current.setSpan(learnMoreSpan, start, end, 0);
            }
            futureProofMessage.setText(current);
            futureProofMessage.setMovementMethod(LinkMovementMethod.getInstance());
        }

        void bindTo(final @NonNull Comment comment, long lastSeenCommentRowId, int position) {
            this.comment = comment;
            bindCommon(comment, lastSeenCommentRowId, position);

            avatarLoader.load(avatarView, comment.senderUserId);

            progressView.setVisibility(comment.transferred ? View.GONE : View.VISIBLE);
            TimeFormatter.setTimePostsFormat(timeView, comment.timestamp);
            timestampRefresher.scheduleTimestampRefresh(comment.timestamp);

            replyButton.setVisibility(View.VISIBLE);

            replyButton.setOnClickListener(v -> {
                keyboardScrollHelper.setAnchorForKeyboardChange(position);
                updateReplyIndicator(comment.senderUserId, comment.id);
                editText.requestFocus();
                final InputMethodManager imm = Preconditions.checkNotNull((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE));
                imm.showSoftInput(editText,0);
            });
        }
    }

    private class ViewHolder extends ViewHolderWithLifecycle {

        final ImageView avatarView;
        final View commentMediaContainer;
        final ImageView commentMedia;
        final ImageView commentVideoIndicator;
        final LimitingTextView commentView;
        final TextView timeView;
        final View progressView;
        final View replyButton;
        final RecyclerView mediaGallery;
        final View cardView;
        final View replyArrow;
        final View replyBubble;
        final TextView replyTextView;
        final TextView nameView;
        final TextView groupView;
        final PostAttributionLayout attributionLayout;

        Comment comment;

        ViewHolder(final @NonNull View v) {
            super(v);
            avatarView = v.findViewById(R.id.avatar);
            commentMediaContainer = v.findViewById(R.id.comment_media_container);
            commentMedia = v.findViewById(R.id.comment_media);
            commentVideoIndicator = v.findViewById(R.id.comment_video_indicator);
            commentView = v.findViewById(R.id.comment_text);
            timeView = v.findViewById(R.id.time);
            progressView = v.findViewById(R.id.progress);
            replyButton = v.findViewById(R.id.reply);
            mediaGallery = v.findViewById(R.id.media);
            cardView = v.findViewById(R.id.comment);
            replyBubble = v.findViewById(R.id.reply_bubble);
            replyArrow = v.findViewById(R.id.reply_arrow);
            replyTextView = v.findViewById(R.id.reply_text);
            nameView = itemView.findViewById(R.id.name);
            groupView = itemView.findViewById(R.id.group_name);
            attributionLayout = itemView.findViewById(R.id.post_header);

            if (mediaGallery != null) {
                final LinearLayoutManager layoutManager = new LinearLayoutManager(mediaGallery.getContext(), RecyclerView.HORIZONTAL, false);
                mediaGallery.setLayoutManager(layoutManager);
                mediaGallery.addItemDecoration(new LinearSpacingItemDecoration(layoutManager, getResources().getDimensionPixelSize(R.dimen.comment_media_list_spacing)));
            }
            if (commentMedia != null) {
                commentMedia.setOutlineProvider(new ViewOutlineProvider() {
                    @Override
                    public void getOutline(View view, Outline outline) {
                        outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), getResources().getDimension(R.dimen.comment_media_corner_radius));
                    }
                });
                commentMedia.setClipToOutline(true);
            }
        }

        void bindTo(final @NonNull Comment comment, long lastSeenCommentRowId, int position) {
            this.comment = comment;
            bindCommon(comment, lastSeenCommentRowId, position);

            if (commentMediaContainer != null) {
                if (comment.media.isEmpty()) {
                    commentMediaContainer.setVisibility(View.GONE);
                } else {
                    commentMediaContainer.setVisibility(View.VISIBLE);
                    Media media = comment.media.get(0);
                    mediaThumbnailLoader.load(commentMedia, media);
                    commentVideoIndicator.setVisibility(media.type == Media.MEDIA_TYPE_VIDEO ? View.VISIBLE : View.GONE);
                    commentMedia.setOnClickListener(v -> {
                        commentsFsePosition = position;

                        Intent intent = new Intent(commentMedia.getContext(), MediaExplorerActivity.class);
                        intent.putExtra(MediaExplorerActivity.EXTRA_MEDIA, MediaExplorerViewModel.MediaModel.fromMedia(Collections.singletonList(media)));
                        intent.putExtra(MediaExplorerActivity.EXTRA_SELECTED, 0);
                        intent.putExtra(MediaExplorerActivity.EXTRA_CONTENT_ID, comment.id);

                        if (commentMedia.getContext() instanceof Activity) {
                            final ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(FlatCommentsActivity.this, commentMedia, commentMedia.getTransitionName());
                            startActivity(intent, options.toBundle());
                        } else {
                            startActivity(intent);
                        }
                    });
                    commentMedia.setTransitionName(MediaPagerAdapter.getTransitionName(comment.id, 0));
                }
            }
        }

        void bindCommon(@NonNull Comment comment, long lastSeenCommentRowId, int position) {
            avatarLoader.load(avatarView, comment.senderUserId);

            final Integer textLimit = textLimits.get(comment.rowId);
            commentView.setLineLimit(textLimit != null ? textLimit : Constants.TEXT_POST_LINE_LIMIT);
            commentView.setLineLimitTolerance(textLimit != null ? Constants.POST_LINE_LIMIT_TOLERANCE : 0);
            commentView.setText(getCommentText(comment, false));
            commentView.setOnReadMoreListener((view, limit) -> {
                textLimits.put(comment.rowId, limit);
                return false;
            });

            boolean hasParent = comment.parentCommentId != null;
            if (replyArrow != null) {
                replyArrow.setVisibility(hasParent ? View.VISIBLE : View.GONE);
            }
            if (replyBubble != null) {
                replyBubble.setVisibility(hasParent ? View.VISIBLE : View.GONE);
            }
            if (hasParent) {
                if (comment.parentComment != null) {
                    replyTextView.setText(getCommentText(comment.parentComment, true));
                } else {
                    SpannableString s = new SpannableString(getString(R.string.reply_original_comment_not_found));
                    s.setSpan(new StyleSpan(Typeface.ITALIC), 0, s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    replyTextView.setText(s);
                }
            }

            progressView.setVisibility(comment.transferred ? View.GONE : View.VISIBLE);
            TimeFormatter.setTimePostsFormat(timeView, comment.timestamp);
            timestampRefresher.scheduleTimestampRefresh(comment.timestamp);

            replyButton.setOnClickListener(v -> {
                keyboardScrollHelper.setAnchorForKeyboardChange(position);
                updateReplyIndicator(comment.senderUserId, comment.id);
                editText.requestFocus();
                final InputMethodManager imm = Preconditions.checkNotNull((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE));
                imm.showSoftInput(editText,0);
            });

            // TODO(jack): Make this more reliable
            if (comment.isRetracted() || Boolean.FALSE.equals(viewModel.isMember.getLiveData().getValue())) {
                replyButton.setVisibility(View.GONE);
            } else {
                replyButton.setVisibility(View.VISIBLE);
            }
        }

        void bindTo(final @Nullable Post post) {

            this.comment = null;

            if (post == null) {
                return;
            }
            avatarLoader.load(avatarView, post.senderUserId);
            contactLoader.load(nameView, post.senderUserId);
            chatLoader.cancel(groupView);
            if (attributionLayout != null) {
                attributionLayout.setGroupAttributionVisible(post.getParentGroup() != null);
            }
            if (post.getParentGroup() != null) {
                chatLoader.load(groupView, new ViewDataLoader.Displayer<View, Chat>() {
                    @Override
                    public void showResult(@NonNull View view, @Nullable Chat result) {
                        if (result != null) {
                            groupView.setText(result.name);
                            if (result.rowId != -1) {
                                groupView.setOnClickListener(v -> {
                                    ChatId chatId = result.chatId;
                                    if (!(chatId instanceof GroupId)) {
                                        Log.w("Cannot open group feed for non-group " + chatId);
                                        return;
                                    }
                                    startActivity(ViewGroupFeedActivity.viewFeed(groupView.getContext(), (GroupId)chatId));
                                });
                            }
                        } else {
                            Log.e("PostViewHolder/bind failed to load chat " + post.getParentGroup());
                        }
                    }

                    @Override
                    public void showLoading(@NonNull View view) {
                        groupView.setText("");
                    }
                }, post.getParentGroup());
            }
            progressView.setVisibility(post.transferred != Post.TRANSFERRED_NO ? View.GONE : View.VISIBLE);
            TimeFormatter.setTimePostsFormat(timeView, post.timestamp);
            timestampRefresher.scheduleTimestampRefresh(post.timestamp);

            if (post.type != Post.TYPE_FUTURE_PROOF) {
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
        }

        private class PostMediaItemViewHolder extends RecyclerView.ViewHolder {

            final public ImageView previewImage;
            final public ImageView videoIcon;

            PostMediaItemViewHolder(@NonNull View itemView) {
                super(itemView);
                previewImage = itemView.findViewById(R.id.media_thumbnail);
                videoIcon = itemView.findViewById(R.id.video_icon);
            }
        }

        private class MediaAdapter extends RecyclerView.Adapter<PostMediaItemViewHolder> {

            final List<Media> media;
            final Post post;

            MediaAdapter(Post post) {
                this.post = post;
                this.media = post.media;
                Log.i("CommentsActivity.MediaAdapter: post " + post.id + " has " + media.size() + " media: " + media);
            }

            @NonNull
            @Override
            public PostMediaItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                PostMediaItemViewHolder holder = new PostMediaItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.post_media_item, parent, false));
                holder.itemView.setOutlineProvider(new ViewOutlineProvider() {
                    @Override
                    public void getOutline(View view, Outline outline) {
                        outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), getResources().getDimension(R.dimen.comment_media_list_corner_radius));
                    }
                });
                holder.itemView.setClipToOutline(true);
                return holder;
            }

            @Override
            public void onBindViewHolder(@NonNull PostMediaItemViewHolder holder, int position) {
                final ImageView imageView = holder.previewImage;
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                imageView.setAdjustViewBounds(true);
                mediaThumbnailLoader.load(imageView, media.get(position));
                imageView.setOnClickListener(v -> {
                    commentsFsePosition = 0;

                    Intent intent = new Intent(imageView.getContext(), MediaExplorerActivity.class);
                    intent.putExtra(MediaExplorerActivity.EXTRA_MEDIA, MediaExplorerViewModel.MediaModel.fromMedia(media));
                    intent.putExtra(MediaExplorerActivity.EXTRA_SELECTED, position);
                    intent.putExtra(MediaExplorerActivity.EXTRA_CONTENT_ID, post.id);
                    intent.putExtra(MediaExplorerActivity.EXTRA_ALLOW_SAVING, post.parentGroup != null);

                    if (imageView.getContext() instanceof Activity) {
                        final ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(FlatCommentsActivity.this, imageView, imageView.getTransitionName());
                        startActivity(intent, options.toBundle());
                    } else {
                        startActivity(intent);
                    }
                });
                holder.videoIcon.setVisibility(media.get(position).type == Media.MEDIA_TYPE_VIDEO ? View.VISIBLE : View.GONE);
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
    private static final int ITEM_TYPE_FUTURE_PROOF_POST = 2;
    private static final int ITEM_TYPE_COMMENT_FUTURE_PROOF = 3;
    private static final int ITEM_TYPE_VOICE_NOTE = 4;

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
                Post post = viewModel.post.getValue();
                if (post != null && post.type == Post.TYPE_FUTURE_PROOF) {
                    return ITEM_TYPE_FUTURE_PROOF_POST;
                }
                return ITEM_TYPE_POST;
            }
            final Comment comment = Preconditions.checkNotNull(getItem(position));
            switch (comment.type) {
                case Comment.TYPE_FUTURE_PROOF:
                    return ITEM_TYPE_COMMENT_FUTURE_PROOF;
                case Comment.TYPE_VOICE_NOTE:
                    return ITEM_TYPE_VOICE_NOTE;
                case Comment.TYPE_USER:
                case Comment.TYPE_RETRACTED:
                default:
                    return ITEM_TYPE_COMMENT;
            }
        }

        private @LayoutRes int getLayoutId(int viewType) {
            switch (viewType) {
                case ITEM_TYPE_FUTURE_PROOF_POST:
                    return R.layout.flat_comment_future_proof_post_item;
                case ITEM_TYPE_POST:
                    return R.layout.flat_comment_post_item;
                case ITEM_TYPE_COMMENT:
                    return R.layout.flat_comment_item;
                case ITEM_TYPE_COMMENT_FUTURE_PROOF:
                    return R.layout.flat_comment_future_proof;
                case ITEM_TYPE_VOICE_NOTE:
                    return R.layout.flat_comment_voice_note;
            }
            throw new IllegalArgumentException("unknown view type " + viewType);
        }

        @Override
        public @NonNull ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(getLayoutId(viewType), parent, false);
            if (viewType == ITEM_TYPE_COMMENT_FUTURE_PROOF) {
                return new FutureProofViewHolder(itemView);
            }
            if (viewType == ITEM_TYPE_VOICE_NOTE) {
                return new VoiceNoteViewHolder(itemView);
            }
            return new ViewHolder(itemView);
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

    private CharSequence getCommentText(@NonNull Comment comment, boolean isReply) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(createAuthorSpan(comment.senderContact, (v) -> v.getContext().startActivity(ViewProfileActivity.viewProfile(v.getContext(), comment.senderContact.userId))));
        CharSequence commentText = "";
        switch (comment.type) {
            case Comment.TYPE_FUTURE_PROOF: {
                if (isReply) {
                    commentText = getString(R.string.unsupported_content);
                }
                break;
            }
            case Comment.TYPE_RETRACTED: {
                SpannableString s = new SpannableString(getString(R.string.comment_retracted_placeholder));
                Object span;
                if (isReply) {
                    span = new StyleSpan(Typeface.ITALIC);
                } else {
                    span = new TextAppearanceSpan(this, R.style.FlatCommentTextAppearanceRetracted);
                }
                s.setSpan(span, 0, s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                commentText = s;
                break;
            }
            case Comment.TYPE_USER: {
                if (!TextUtils.isEmpty(comment.text)) {
                    commentText = MentionsFormatter.insertMentions(comment.getText(), comment.mentions, (v, mention) -> {
                        v.getContext().startActivity(ViewProfileActivity.viewProfile(v.getContext(), mention.userId));
                    });
                } else if (!comment.media.isEmpty()) {
                    switch (comment.media.get(0).type) {
                        case Media.MEDIA_TYPE_IMAGE: {
                            commentText = getString(R.string.photo);
                            break;
                        }
                        case Media.MEDIA_TYPE_VIDEO: {
                            commentText = getString(R.string.video);
                            break;
                        }
                        case Media.MEDIA_TYPE_AUDIO: {
                            commentText = getString(R.string.voice_note);
                            break;
                        }
                        default: {
                            commentText = "";
                        }
                    }
                }
                break;
            }
            case Comment.TYPE_VOICE_NOTE: {
                if (isReply) {
                    commentText = getString(R.string.voice_note);
                }
                break;
            }
            default:
                commentText = "";
        }
        if (!TextUtils.isEmpty(commentText)) {
            builder.append(": ");
            builder.append(commentText);
        }
        return builder;
    }

    private CharSequence createAuthorSpan(Contact contact, @Nullable View.OnClickListener listener) {
        CharSequence name = contact.userId.isMe() ? getString(R.string.me) : contact.getDisplayName();
        SpannableString mentionString = new SpannableString(name);
        mentionString.setSpan(new AuthorSpan(listener), 0, mentionString.length(),  Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return mentionString;
    }

    static class AuthorSpan extends ClickableSpan {

        private @Nullable
        final View.OnClickListener listener;

        public AuthorSpan(@Nullable View.OnClickListener listener){
            this.listener = listener;
        }

        @Override
        public void onClick(@NonNull View widget) {
            if (listener != null) {
                listener.onClick(widget);
            }
        }

        @Override
        public void updateDrawState(@NonNull TextPaint ds) {
            ds.setUnderlineText(false);
            ds.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        }
    }
}
