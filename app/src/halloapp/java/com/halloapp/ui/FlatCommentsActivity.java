package com.halloapp.ui;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Outline;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.StyleSpan;
import android.util.Pair;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.Toolbar;
import androidx.collection.LongSparseArray;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.app.SharedElementCallback;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.AsyncPagedListDiffer;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.AdapterListUpdateCallback;
import androidx.recyclerview.widget.AsyncDifferConfig;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListUpdateCallback;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.halloapp.BuildConfig;
import com.halloapp.Constants;
import com.halloapp.ContentDraftManager;
import com.halloapp.Debug;
import com.halloapp.R;
import com.halloapp.UrlPreview;
import com.halloapp.UrlPreviewLoader;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactLoader;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.content.Comment;
import com.halloapp.content.ContentDb;
import com.halloapp.content.ContentItem;
import com.halloapp.content.Group;
import com.halloapp.content.Media;
import com.halloapp.content.Mention;
import com.halloapp.content.Post;
import com.halloapp.content.Reaction;
import com.halloapp.emoji.ReactionPopupWindow;
import com.halloapp.groups.GroupLoader;
import com.halloapp.id.ChatId;
import com.halloapp.id.UserId;
import com.halloapp.media.AudioDurationLoader;
import com.halloapp.media.MediaThumbnailLoader;
import com.halloapp.media.VoiceNotePlayer;
import com.halloapp.privacy.BlockListManager;
import com.halloapp.props.ServerProps;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.ui.groups.GroupContentDecryptStatLoader;
import com.halloapp.ui.groups.GroupParticipants;
import com.halloapp.ui.groups.ViewGroupFeedActivity;
import com.halloapp.ui.home.HomeContentDecryptStatLoader;
import com.halloapp.ui.markdown.MarkdownUtils;
import com.halloapp.ui.mediaedit.MediaEditActivity;
import com.halloapp.ui.mediaexplorer.MediaExplorerActivity;
import com.halloapp.ui.mediaexplorer.MediaExplorerViewModel;
import com.halloapp.ui.mediapicker.MediaPickerActivity;
import com.halloapp.ui.mentions.MentionPickerView;
import com.halloapp.ui.mentions.TextContentLoader;
import com.halloapp.ui.posts.PostAttributionLayout;
import com.halloapp.ui.profile.ViewProfileActivity;
import com.halloapp.util.ActivityUtils;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.ClipUtils;
import com.halloapp.util.DialogFragmentUtils;
import com.halloapp.util.IntentUtils;
import com.halloapp.util.Preconditions;
import com.halloapp.util.RandomId;
import com.halloapp.util.Result;
import com.halloapp.util.StringUtils;
import com.halloapp.util.TimeFormatter;
import com.halloapp.util.TimeUtils;
import com.halloapp.util.ViewDataLoader;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.BaseInputView;
import com.halloapp.widget.DrawDelegateView;
import com.halloapp.widget.FocusableMessageView;
import com.halloapp.widget.ItemSwipeHelper;
import com.halloapp.widget.LimitingTextView;
import com.halloapp.widget.LinearSpacingItemDecoration;
import com.halloapp.widget.LinkPreviewComposeView;
import com.halloapp.widget.MentionableEntry;
import com.halloapp.widget.MessageTextLayout;
import com.halloapp.widget.PressInterceptView;
import com.halloapp.widget.ReactionsLayout;
import com.halloapp.widget.RecyclerViewKeyboardScrollHelper;
import com.halloapp.widget.SnackbarHelper;
import com.halloapp.widget.SwipeListItemHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class FlatCommentsActivity extends HalloActivity implements EasyPermissions.PermissionCallbacks {

    public static final String EXTRA_POST_SENDER_USER_ID = "post_sender_user_id";
    public static final String EXTRA_POST_ID = "post_id";
    public static final String EXTRA_REPLY_COMMENT_ID = "reply_comment_id";
    public static final String EXTRA_SHOW_KEYBOARD = "show_keyboard";
    public static final String EXTRA_NO_POST_LENGTH_LIMIT = "no_post_length_limit";
    public static final String EXTRA_NAVIGATE_TO_COMMENT_ID = "navigate_to_comment_id";

    private static final String KEY_REPLY_COMMENT_ID = "reply_comment_id";
    private static final String KEY_COMMENT_MEDIA_URI = "comment_media_uri";

    private static final int REQUEST_CODE_PICK_MEDIA = 1;
    private static final int REQUEST_CODE_EDIT_MEDIA = 2;

    private static final int REQUEST_PERMISSION_CODE_RECORD_VOICE_NOTE = 1;

    private static final int PERSIST_DELAY_MS = 500;

    private final ServerProps serverProps = ServerProps.getInstance();
    private final ContentDraftManager contentDraftManager = ContentDraftManager.getInstance();
    private final BlockListManager blockListManager = BlockListManager.getInstance();
    private final BgWorkers bgWorkers = BgWorkers.getInstance();

    private Runnable updateDraftRunnable;

    public static Intent viewComments(@NonNull Context context, String postId, @Nullable UserId senderUserId) {
        final Intent intent = new Intent(context, FlatCommentsActivity.class);
        if (senderUserId != null) {
            intent.putExtra(FlatCommentsActivity.EXTRA_POST_SENDER_USER_ID, senderUserId.rawId());
        }
        intent.putExtra(FlatCommentsActivity.EXTRA_POST_ID, postId);
        intent.putExtra(FlatCommentsActivity.EXTRA_SHOW_KEYBOARD, false);

        return intent;
    }

    public static Intent viewComments(@NonNull Context context, String postId) {
        return viewComments(context, postId, null);
    }

    private final CommentsAdapter adapter = new CommentsAdapter();
    private final ReactionLoader reactionLoader = new ReactionLoader();
    private MediaThumbnailLoader mediaThumbnailLoader;
    private GroupLoader groupLoader;
    private AvatarLoader avatarLoader;
    private ContactLoader contactLoader;
    private UrlPreviewLoader urlPreviewLoader;
    private TextContentLoader textContentLoader;

    private FlatCommentsViewModel viewModel;

    private String replyCommentId;

    private MentionableEntry editText;
    private MentionPickerView mentionPickerView;
    private View membershipNotice;

    private ItemSwipeHelper itemSwipeHelper;
    private RecyclerViewKeyboardScrollHelper keyboardScrollHelper;

    private static final long POST_TEXT_LIMITS_ID = -1;
    private static final int POSITION_TOP = -1;
    private static final int POSITION_BOT = -2;

    private final LongSparseArray<Integer> textLimits = new LongSparseArray<>();

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private ActionMode actionMode;
    private View screenOverlay;

    private Comment selectedComment;
    private BaseCommentViewHolder selectedViewHolder;
    private RecyclerView commentsView;

    private ReactionPopupWindow reactionPopupWindow;

    private final ContactsDb.Observer contactsObserver = new ContactsDb.BaseObserver() {
        @Override
        public void onContactsChanged() {
            contactLoader.resetCache();
            mainHandler.post(adapter::notifyDataSetChanged);
        }
    };

    private final ContentDb.DefaultObserver contentObserver = new ContentDb.DefaultObserver() {
        @Override
        public void onReactionAdded(Reaction reaction, ContentItem contentItem) {
            if (contentItem instanceof Comment) {
                RecyclerView.ViewHolder viewHolder = commentsView.findViewHolderForItemId(contentItem.rowId);
                if (viewHolder instanceof BaseCommentViewHolder) {
                    ((BaseCommentViewHolder) viewHolder).reloadReactions();
                }
            }
        }
    };

    private List<UserId> blockList = null;

    private final BlockListManager.Observer blockListObserver = new BlockListManager.Observer() {
        @Override
        public void onBlockListChanged() {
            bgWorkers.execute(() -> {
                refreshBlockList();
            });
        }
    };

    private void refreshBlockList() {
        blockList = blockListManager.getBlockList();
        mainHandler.post(adapter::notifyDataSetChanged);
    }

    private TimestampRefresher timestampRefresher;

    private boolean enterTransitionComplete;
    private boolean showKeyboardAfterEnter;
    private int commentsFsePosition;

    private AudioDurationLoader audioDurationLoader;

    private BaseInputView chatInputView;

    private String postId;

    private LinearLayoutManager commentsLayoutManager;
    private long highlightedComment = -1;

    private ImageView postAvatarView;
    private TextView postNameView;
    private TextView postGroupView;

    private LinkPreviewComposeView linkPreviewComposeView;

    private PostAttributionLayout postAttributionLayout;

    private View postProgressView;
    private TextView postTimeView;

    private FrameLayout postContentContainer;
    private DrawDelegateView drawDelegateView;

    private int postType = -1;
    private Integer scrollToPos = null;

    private boolean playing;

    private String audioPath;
    private boolean wasPlaying;

    private Observer<VoiceNotePlayer.PlaybackState> playbackStateObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flat_comments);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.post_card_background));

        TextView titleView = findViewById(R.id.title);
        titleView.setText(R.string.comments);
        AppBarLayout appBarLayout = findViewById(R.id.app_bar_layout);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int offset) {
                int maxScroll = appBarLayout.getTotalScrollRange();
                float percentage = (float) Math.abs(offset) / (float) maxScroll;

                titleView.setAlpha(Math.max(0, 2 * (0.5f - percentage)));
            }
        });

        Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        setExitSharedElementCallback(new SharedElementCallback() {
            @Override
            public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                if (!names.isEmpty()) {
                    RecyclerView comments = findViewById(R.id.comments);
                    String name = names.get(0);

                    if (commentsFsePosition == 0 && postContentContainer != null) {
                        Post post = Preconditions.checkNotNull(viewModel.post.getValue());
                        RecyclerView gallery = postContentContainer.findViewById(R.id.media);
                        RecyclerView.LayoutManager layoutManager = Preconditions.checkNotNull(gallery.getLayoutManager());

                        for (int i = 0; i < post.media.size(); ++i) {
                            View view = layoutManager.findViewByPosition(i);

                            if (view != null) {
                                View mediaView = view.findViewById(R.id.media_thumbnail);

                                if (mediaView != null && name.equals(mediaView.getTransitionName())) {
                                    sharedElements.put(name, view);
                                    return;
                                }
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
                }

                super.onMapSharedElements(names, sharedElements);
            }
        });

        commentsView = findViewById(R.id.comments);
        commentsView.setItemAnimator(null);

        mentionPickerView = findViewById(R.id.mention_picker_view);

        commentsLayoutManager = new LinearLayoutManager(this);
        commentsView.setLayoutManager(commentsLayoutManager);

        keyboardScrollHelper = new RecyclerViewKeyboardScrollHelper(commentsView);

        screenOverlay = findViewById(R.id.darken_screen);
        screenOverlay.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (actionMode != null) {
                    actionMode.finish();
                }
                return true;
            }
        });

        postId = Preconditions.checkNotNull(getIntent().getStringExtra(EXTRA_POST_ID));

        final View replyIndicator = findViewById(R.id.reply_container);
        final TextView replyNameView = replyIndicator.findViewById(R.id.reply_name);
        final View replyIndicatorCloseButton = findViewById(R.id.reply_close);

        viewModel = new ViewModelProvider(this, new FlatCommentsViewModel.Factory(getApplication(), postId)).get(FlatCommentsViewModel.class);
        viewModel.setNavigationCommentId(getIntent().getStringExtra(EXTRA_NAVIGATE_TO_COMMENT_ID));
        viewModel.getCommentList().observe(this, comments -> {
            adapter.submitList(comments, () -> commentsView.post(() -> {
                if (scrollToPos == null) {
                    return;
                }
                if (scrollToPos == POSITION_BOT) {
                    int lastVisible = commentsLayoutManager.findLastCompletelyVisibleItemPosition();
                    if (lastVisible != comments.size() - 1) {
                        appBarLayout.setExpanded(false, true);
                        commentsView.scrollToPosition(comments.size() - 1);
                    }
                } else if (scrollToPos == POSITION_TOP) {
                    long currentOldest = 0;
                    Integer commentPosition = null;
                    for (int i = 0; i < comments.size(); i++) {
                        Comment comment = Preconditions.checkNotNull(comments.get(i));
                        if (comment.senderUserId.isMe() && comment.timestamp > currentOldest) {
                            commentPosition = i;
                            currentOldest = comment.timestamp;
                        }
                    }
                    if (commentPosition != null) {
                        appBarLayout.setExpanded(false, true);
                        commentsView.smoothScrollToPosition(commentPosition + 1);
                    }
                } else {
                    appBarLayout.setExpanded(false, true);
                    commentsView.scrollToPosition(scrollToPos);
                }
                scrollToPos = null;
            }));
        });
        viewModel.getNavCommentPosition().observe(this, pos -> {
            if (pos == null) {
                scrollToPos = POSITION_TOP;
                return;
            }
            if (pos < adapter.getItemCount()) {
                commentsLayoutManager.scrollToPosition(pos);
            } else {
                scrollToPos = pos;
            }
        });

        viewModel.unseenCommentCount.getLiveData().observe(this, pair -> {
            adapter.setFirstUnseenCommentId(pair.first);
            adapter.setNewCommentCount(pair.second);
            if (pair.second == 0) {
                scrollToPos = POSITION_BOT;
                adapter.notifyDataSetChanged();
            }
        });

        viewModel.getReply().observe(this, reply -> {
            if (reply == null) {
                replyIndicator.setVisibility(View.GONE);
                return;
            }
            replyIndicator.setVisibility(View.VISIBLE);
            contactLoader.load(replyNameView, reply.comment.senderUserId);
            TextView replyTextView = replyIndicator.findViewById(R.id.reply_text);
            textContentLoader.load(replyTextView, reply.comment, false);
            final ImageView replyMediaIconView = replyIndicator.findViewById(R.id.reply_media_icon);
            final ImageView replyMediaThumbView = replyIndicator.findViewById(R.id.reply_media_thumb);

            audioDurationLoader.cancel(replyTextView);
            if (0 < reply.comment.media.size()) {
                replyMediaThumbView.setVisibility(View.VISIBLE);
                replyMediaThumbView.setOutlineProvider(new ViewOutlineProvider() {
                    @Override
                    public void getOutline(View view, Outline outline) {
                        outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), getResources().getDimension(R.dimen.comment_media_list_corner_radius));
                    }
                });
                replyMediaThumbView.setClipToOutline(true);
                final Media media = reply.comment.media.get(0);
                mediaThumbnailLoader.load(replyMediaThumbView, media);
                replyMediaIconView.setVisibility(View.VISIBLE);
                switch (media.type) {
                    case Media.MEDIA_TYPE_IMAGE: {
                        replyMediaIconView.setImageResource(R.drawable.ic_camera);
                        if (TextUtils.isEmpty(reply.comment.text)) {
                            replyTextView.setText(R.string.photo);
                        }
                        break;
                    }
                    case Media.MEDIA_TYPE_VIDEO: {
                        replyMediaIconView.setImageResource(R.drawable.ic_video);
                        if (TextUtils.isEmpty(reply.comment.text)) {
                            replyTextView.setText(R.string.video);
                        }
                        break;
                    }
                    case Media.MEDIA_TYPE_AUDIO: {
                        replyMediaIconView.setImageResource(R.drawable.ic_keyboard_voice);
                        replyMediaThumbView.setVisibility(View.GONE);
                        final String voiceNote = getString(R.string.voice_note);
                        audioDurationLoader.load(replyTextView, reply.comment.media.get(0).file, new ViewDataLoader.Displayer<TextView, Long>() {
                            @Override
                            public void showResult(@NonNull TextView view, @Nullable Long result) {
                                if (result != null) {
                                    replyTextView.setText(getString(R.string.voice_note_preview, StringUtils.formatVoiceNoteDuration(FlatCommentsActivity.this, result)));
                                }
                            }

                            @Override
                            public void showLoading(@NonNull TextView view) {
                                replyTextView.setText(voiceNote);
                            }
                        });
                        break;
                    }
                    case Media.MEDIA_TYPE_UNKNOWN:
                    default: {
                        replyMediaIconView.setImageResource(R.drawable.ic_media_collection);
                        replyTextView.setText(reply.comment.text);
                        break;
                    }
                }
            } else {
                replyMediaThumbView.setVisibility(View.GONE);
                replyMediaIconView.setVisibility(View.GONE);
            }
        });

        linkPreviewComposeView = findViewById(R.id.link_preview_compose_view);

        chatInputView = findViewById(R.id.chat_input);
        chatInputView.bindEmojiKeyboardLayout(findViewById(R.id.emoji_keyboard));
        chatInputView.setVoiceNoteControlView(findViewById(R.id.recording_ui));
        chatInputView.setInputParent(new BaseInputView.InputParent() {
            @Override
            public void onSendText() {
                sendComment();
                linkPreviewComposeView.updateUrlPreview(null);
                viewModel.resetCommentMediaUri();
                editText.setText(null);
                resetReplyIndicator();
                scrollToPos = POSITION_BOT;
            }

            @Override
            public void onSendVoiceNote() {
                viewModel.finishRecording(replyCommentId, false);
                viewModel.resetCommentMediaUri();
                resetReplyIndicator();
                scrollToPos = POSITION_BOT;
            }

            @Override
            public void onSendVoiceDraft(File draft) {
                viewModel.sendVoiceNote(replyCommentId, draft);
                viewModel.resetCommentMediaUri();
                resetReplyIndicator();
                scrollToPos = POSITION_BOT;
            }

            @Override
            public void onChooseGallery() {
                pickMedia();
            }

            @Override
            public void onChooseDocument() {

            }

            @Override
            public void onChooseCamera() {

            }

            @Override
            public void onChooseContact() {

            }

            @Override
            public void requestVoicePermissions() {
                EasyPermissions.requestPermissions(FlatCommentsActivity.this, getString(R.string.voice_note_record_audio_permission_rationale), REQUEST_PERMISSION_CODE_RECORD_VOICE_NOTE, Manifest.permission.RECORD_AUDIO);
            }

            @Override
            public void onUrl(String url) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                urlPreviewLoader.load(linkPreviewComposeView, url, new ViewDataLoader.Displayer<View, UrlPreview>() {
                    @Override
                    public void showResult(@NonNull View view, @Nullable UrlPreview result) {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }
                        linkPreviewComposeView.updateUrlPreview(result);
                    }

                    @Override
                    public void showLoading(@NonNull View view) {
                        linkPreviewComposeView.setLoadingUrl(url);
                        linkPreviewComposeView.setLoading(!TextUtils.isEmpty(url));
                    }
                });
            }
        });
        chatInputView.bindVoicePlayer(this, viewModel.getVoiceNotePlayer());
        chatInputView.bindVoiceRecorder(this, viewModel.getVoiceNoteRecorder());

        postContentContainer = findViewById(R.id.post_content_placeholder);
        postAvatarView = findViewById(R.id.post_avatar);
        postTimeView = findViewById(R.id.post_time);
        postAttributionLayout = findViewById(R.id.post_header);
        postNameView = postAttributionLayout.findViewById(R.id.name);
        postGroupView = postAttributionLayout.findViewById(R.id.group_name);
        postProgressView = findViewById(R.id.post_progress);
        drawDelegateView = findViewById(R.id.draw_delegate);

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
        mediaContainer.setOnClickListener(v -> {
            ArrayList<Uri> uris = new ArrayList<>();
            uris.add(viewModel.getCommentMediaUri());
            Intent intent = new Intent(this, MediaEditActivity.class);
            intent.putExtra(MediaEditActivity.EXTRA_MEDIA, uris);
            intent.putExtra(MediaEditActivity.EXTRA_SELECTED, 0);
            intent.putExtra(MediaEditActivity.EXTRA_STATE, viewModel.getMediaEditState());
            intent.putExtra(MediaEditActivity.EXTRA_PURPOSE, MediaEditActivity.EDIT_PURPOSE_CROP);
            startActivityForResult(intent, REQUEST_CODE_EDIT_MEDIA, ActivityOptions.makeSceneTransitionAnimation(this, imageView, MediaEditActivity.TRANSITION_VIEW_NAME).toBundle());
        });

        viewModel.commentMedia.observe(this, media -> {
            if (media == null) {
                mediaContainer.setVisibility(View.GONE);
            } else if (!media.isSuccess()) {
                mediaContainer.setVisibility(View.GONE);
                SnackbarHelper.showWarning(this, R.string.failed_to_load_media);
            } else {
                mediaContainer.setVisibility(View.VISIBLE);
                mediaThumbnailLoader.remove(media.getResult().file);
                imageView.setImageDrawable(null);
                mediaThumbnailLoader.load(imageView, media.getResult());
            }
            updateSendButton();
            updateCommentDraft();
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
            chatInputView.setAllowMedia(true);
            chatInputView.setAllowVoiceNoteRecording(true);
            if (post != null) {
                bindPost(post);
            }
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
        editText.setHint(R.string.type_a_comment_hint);
        editText.setMentionPickerView(mentionPickerView);
        editText.setText(contentDraftManager.getPostCommentDraft(postId));
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
                updateCommentDraft();
            }
        });

        linkPreviewComposeView.setOnRemovePreviewClickListener(v -> {
            urlPreviewLoader.cancel(linkPreviewComposeView);
            linkPreviewComposeView.setLoading(false);
            linkPreviewComposeView.updateUrlPreview(null);
        });

        if (getIntent().getBooleanExtra(EXTRA_NO_POST_LENGTH_LIMIT, false)) {
            textLimits.put(POST_TEXT_LIMITS_ID, Integer.MAX_VALUE);
        }

        if (getIntent().getBooleanExtra(EXTRA_SHOW_KEYBOARD, true)) {
            showKeyboard();
        }

        mediaThumbnailLoader = new MediaThumbnailLoader(this, 2 * getResources().getDimensionPixelSize(R.dimen.comment_media_side_length));
        contactLoader = new ContactLoader(userId -> {
            startActivity(ViewProfileActivity.viewProfile(this, userId));
            return null;
        });
        avatarLoader = AvatarLoader.getInstance();
        groupLoader = new GroupLoader();
        textContentLoader = new TextContentLoader();
        urlPreviewLoader = new UrlPreviewLoader();

        linkPreviewComposeView.setMediaThumbnailLoader(mediaThumbnailLoader);

        timestampRefresher = new ViewModelProvider(this).get(TimestampRefresher.class);
        timestampRefresher.refresh.observe(this, value -> adapter.notifyDataSetChanged());

        audioDurationLoader = new AudioDurationLoader(this);

        ContactsDb.getInstance().addObserver(contactsObserver);
        ContentDb.getInstance().addObserver(contentObserver);

        bgWorkers.execute(() -> {
            refreshBlockList();
        });

        blockListManager.addObserver(blockListObserver);

        if (savedInstanceState != null) {
            updateReplyIndicator(savedInstanceState.getString(KEY_REPLY_COMMENT_ID));
            Uri commentMediaUri = savedInstanceState.getParcelable(KEY_COMMENT_MEDIA_URI);
            if (commentMediaUri != null) {
                viewModel.loadCommentMediaUri(commentMediaUri);
            }
        } else {
            updateReplyIndicator(getIntent().getStringExtra(EXTRA_REPLY_COMMENT_ID));
        }

        SwipeListItemHelper swipeListItemHelper = new SwipeListItemHelper(
                Preconditions.checkNotNull(ContextCompat.getDrawable(this, R.drawable.ic_swipe_reply)),
                ContextCompat.getColor(this, R.color.swipe_reply_background),
                getResources().getDimensionPixelSize(R.dimen.swipe_reply_icon_margin)) {

            @Override
            public boolean isItemViewSwipeEnabled() {
                return false; // initiate swipes manually by calling startSwipe()
            }

            @Override
            public boolean canSwipe(@NonNull RecyclerView.ViewHolder viewHolder) {
                switch (viewHolder.getItemViewType()) {
                    case ITEM_TYPE_POST:
                    case ITEM_TYPE_RETRACTED_INCOMING:
                    case ITEM_TYPE_RETRACTED_OUTGOING:
                        return false;
                }
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // workaround to reset swiped out view
                adapter.notifyItemChanged(viewHolder.getAdapterPosition());
                itemSwipeHelper.attachToRecyclerView(null);
                itemSwipeHelper.attachToRecyclerView(commentsView);

                final Comment comment = ((BaseCommentViewHolder)viewHolder).comment;
                if (comment == null || comment.isRetracted()) {
                    return;
                }

                updateReplyIndicator(comment.id);
                showKeyboard();
            }
        };
        itemSwipeHelper = new ItemSwipeHelper(swipeListItemHelper);
        itemSwipeHelper.attachToRecyclerView(commentsView);

        // Modified from ItemTouchHelper
        commentsView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            private static final int ACTIVE_POINTER_ID_NONE = -1;

            private final int slop = ViewConfiguration.get(commentsView.getContext()).getScaledTouchSlop();

            private int activePointerId;
            private float initialX;
            private float initialY;

            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent event) {
                final int action = event.getActionMasked();
                if (action == MotionEvent.ACTION_DOWN) {
                    activePointerId = event.getPointerId(0);
                    initialX = event.getX();
                    initialY = event.getY();
                } else if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
                    activePointerId = ACTIVE_POINTER_ID_NONE;
                } else if (activePointerId != ACTIVE_POINTER_ID_NONE) {
                    final int index = event.findPointerIndex(activePointerId);
                    if (index >= 0) {
                        RecyclerView.ViewHolder vh = findSwipedView(event);
                        if (vh instanceof BaseCommentViewHolder) {
                            BaseCommentViewHolder mvh = (BaseCommentViewHolder) vh;
                            Comment comment = mvh.comment;
                            swipeListItemHelper.setIconTint(GroupParticipants.getParticipantNameColor(FlatCommentsActivity.this, comment.senderContact));
                        }
                        if (vh != null) {
                            itemSwipeHelper.startSwipe(vh);
                        }
                    }
                }

                return false;
            }
            private RecyclerView.ViewHolder findSwipedView(MotionEvent motionEvent) {
                final RecyclerView.LayoutManager lm = Preconditions.checkNotNull(commentsView.getLayoutManager());
                if (activePointerId == ACTIVE_POINTER_ID_NONE) {
                    return null;
                }
                final int pointerIndex = motionEvent.findPointerIndex(activePointerId);
                final float dx = motionEvent.getX(pointerIndex) - initialX;
                final float dy = motionEvent.getY(pointerIndex) - initialY;
                final float absDx = Math.abs(dx);
                final float absDy = Math.abs(dy);

                if (absDx < slop && absDy < slop) {
                    return null;
                }
                if (absDx > absDy && lm.canScrollHorizontally()) {
                    return null;
                } else if (absDy > absDx && lm.canScrollVertically()) {
                    return null;
                }
                if (commentsView.getScrollState() != RecyclerView.SCROLL_STATE_IDLE) {
                    return null;
                }
                View child = commentsView.findChildViewUnder(initialX, initialY);
                if (child == null) {
                    return null;
                }
                return commentsView.getChildViewHolder(child);
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        if (!chatInputView.onBackPressed()) {
            super.onBackPressed();
        }
    }

    private void focusSelectedComment() {
        for (int childCount = commentsView.getChildCount(), i = 0; i < childCount; ++i) {
            final BaseCommentViewHolder holder = (BaseCommentViewHolder) commentsView.getChildViewHolder(commentsView.getChildAt(i));
            if (selectedComment.rowId == holder.comment.rowId) {
                selectedViewHolder = holder;
                holder.focusViewHolder();
            }
        }
    }

    private void unfocusSelectedComment() {
        if (selectedViewHolder != null) {
            selectedViewHolder.unfocusViewHolder();
            selectedViewHolder = null;
        }
    }

    private void sendComment() {
        final Pair<String, List<Mention>> textWithMentions = editText.getTextWithMentions();
        final String postText = textWithMentions.first;
        if (TextUtils.isEmpty(postText) && (viewModel.commentMedia.getValue() == null || !viewModel.commentMedia.getValue().isSuccess())) {
            Log.w("CommentsActivity: cannot send empty comment");
            return;
        }
        List<Mention> mentions = textWithMentions.second;
        final Comment comment = new Comment(
                0,
                postId,
                UserId.ME,
                RandomId.create(),
                replyCommentId,
                System.currentTimeMillis(),
                Comment.TRANSFERRED_NO,
                true,
                postText);
        linkPreviewComposeView.attachPreview(comment);
        urlPreviewLoader.cancel(linkPreviewComposeView, true);

        if (comment.urlPreview == null && comment.loadingUrlPreview != null) {
            urlPreviewLoader.addWaitingContentItem(comment);
        }

        for (Mention mention : mentions) {
            if (mention.index < 0 || mention.index >= postText.length()) {
                continue;
            }
            comment.mentions.add(mention);
        }
        viewModel.sendComment(comment, ActivityUtils.supportsWideColor(FlatCommentsActivity.this));
    }

    private void updateSendButton() {
        Editable e = editText.getText();
        String s = e == null ? null : e.toString();
        Result<Media> r = viewModel.commentMedia.getValue();
        boolean canSend = (r != null && r.isSuccess()) || !TextUtils.isEmpty(s);
        chatInputView.setCanSend(canSend);
    }

    private void updateCommentDraft() {
        Editable e = editText.getText();
        String s = e == null ? null : e.toString();
        boolean currentlyRecording = viewModel.checkIsRecording();
        boolean emptyText = TextUtils.isEmpty(s);

        if (!currentlyRecording) {
            if (updateDraftRunnable != null) {
                mainHandler.removeCallbacks(updateDraftRunnable);
            }
            updateDraftRunnable = () -> {
                if (emptyText) {
                    contentDraftManager.clearPostCommentDraft(postId);
                } else{
                    contentDraftManager.setPostCommentDraft(postId, s);
                }
            };
            mainHandler.postDelayed(updateDraftRunnable, PERSIST_DELAY_MS);
        }
    }

    private void bindPost(@NonNull Post post) {
        avatarLoader.load(postAvatarView, post.senderUserId);
        contactLoader.load(postNameView, post.senderUserId);
        groupLoader.cancel(postGroupView);
        if (postAttributionLayout != null) {
            postAttributionLayout.setGroupAttributionVisible(post.getParentGroup() != null);
        }
        if (post.getParentGroup() != null) {
            groupLoader.load(postGroupView, new ViewDataLoader.Displayer<View, Group>() {
                @Override
                public void showResult(@NonNull View view, @Nullable Group result) {
                    if (result != null) {
                        postGroupView.setText(result.name);
                        if (result.rowId != -1) {
                            postGroupView.setOnClickListener(v -> {
                                ChatId chatId = result.groupId;
                                startActivity(ViewGroupFeedActivity.viewFeed(postGroupView.getContext(), result.groupId));
                            });
                        }
                    } else {
                        Log.e("PostViewHolder/bind failed to load chat " + post.getParentGroup());
                    }
                }

                @Override
                public void showLoading(@NonNull View view) {
                    postGroupView.setText("");
                }
            }, post.getParentGroup());
        }
        postProgressView.setVisibility(post.transferred != Post.TRANSFERRED_NO ? View.GONE : View.VISIBLE);
        TimeFormatter.setTimePostsFormat(postTimeView, post.timestamp);
        timestampRefresher.scheduleTimestampRefresh(post.timestamp);

        if (postType != post.type) {
            postContentContainer.removeAllViews();

            @LayoutRes int layout = R.layout.flat_comment_post_item;
            switch (post.type) {
                case Post.TYPE_FUTURE_PROOF:
                    layout = R.layout.flat_comment_future_proof_post_item;
                    break;
                case Post.TYPE_VOICE_NOTE:
                    layout = R.layout.flat_comment_voice_note_post_item;
                    break;
            }
            LayoutInflater.from(postContentContainer.getContext()).inflate(layout, postContentContainer, true);
            postType = post.type;

            if (postType != Post.TYPE_FUTURE_PROOF) {
                RecyclerView postMediaGallery = postContentContainer.findViewById(R.id.media);
                if (postMediaGallery != null) {
                    final LinearLayoutManager layoutManager = new LinearLayoutManager(postMediaGallery.getContext(), RecyclerView.HORIZONTAL, false);
                    postMediaGallery.setLayoutManager(layoutManager);
                    postMediaGallery.addItemDecoration(new LinearSpacingItemDecoration(layoutManager, getResources().getDimensionPixelSize(R.dimen.comment_media_list_spacing)));
                }
                if (postType == Post.TYPE_VOICE_NOTE) {
                    SeekBar seekBar = postContentContainer.findViewById(R.id.voice_note_seekbar);
                    ImageView controlButton = postContentContainer.findViewById(R.id.control_btn);
                    TextView seekTime = postContentContainer.findViewById(R.id.seek_time);

                    controlButton.setOnClickListener(v -> {
                        if (playing) {
                            viewModel.getVoiceNotePlayer().pause();
                        } else if (audioPath != null) {
                            viewModel.getVoiceNotePlayer().playFile(audioPath, seekBar.getProgress());
                        }
                    });

                    playbackStateObserver = state -> {
                        if (state == null || audioPath == null || !audioPath.equals(state.playingTag)) {
                            return;
                        }
                        if (playing != state.playing) {
                            playing = state.playing;
                        }
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
                    };
                }
            }
        }
        if (postType == Post.TYPE_FUTURE_PROOF) {
            TextView futureProofMessage = postContentContainer.findViewById(R.id.future_proof_text);
            linkifyFutureProof(futureProofMessage);
            return;
        } else if (postType == Post.TYPE_VOICE_NOTE) {
            ImageView controlButton = postContentContainer.findViewById(R.id.control_btn);
            TextView seekTime = postContentContainer.findViewById(R.id.seek_time);
            ProgressBar loading = postContentContainer.findViewById(R.id.loading);
            if (!post.media.isEmpty()) {
                Media media = post.media.get(0);
                if (media.transferred == Media.TRANSFERRED_YES) {
                    if (media.file != null) {
                        String newPath = media.file.getAbsolutePath();
                        if (!newPath.equals(audioPath)) {
                            this.audioPath = media.file.getAbsolutePath();
                            audioDurationLoader.load(seekTime, media);
                        }
                    }
                    loading.setVisibility(View.GONE);
                    controlButton.setVisibility(View.VISIBLE);
                } else {
                    loading.setVisibility(View.VISIBLE);
                    controlButton.setVisibility(View.INVISIBLE);
                }
            }
            viewModel.getVoiceNotePlayer().getPlaybackState().observe(this, playbackStateObserver);
        }
        RecyclerView postMediaGallery = postContentContainer.findViewById(R.id.media);
        postMediaGallery.setTag(post);
        if (post.getMedia().isEmpty()) {
            postMediaGallery.setVisibility(View.GONE);
        } else {
            postMediaGallery.setVisibility(View.VISIBLE);
            postMediaGallery.setAdapter(new MediaAdapter(post));
        }

        LimitingTextView postCommentView = postContentContainer.findViewById(R.id.comment_text);

        final Integer textLimit = textLimits.get(POST_TEXT_LIMITS_ID);
        postCommentView.setLineLimit(textLimit != null ? textLimit : Constants.TEXT_POST_LINE_LIMIT);
        postCommentView.setLineLimitTolerance(textLimit != null ? Constants.POST_LINE_LIMIT_TOLERANCE : 0);
        postCommentView.setOnReadMoreListener((view, limit) -> {
            textLimits.put(POST_TEXT_LIMITS_ID, limit);
            return false;
        });

        postCommentView.setVisibility(TextUtils.isEmpty(post.text) ? View.GONE : View.VISIBLE);
        textContentLoader.load(postCommentView, post);
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
            case REQUEST_CODE_EDIT_MEDIA: {
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        final Bundle editStates = data.getParcelableExtra(MediaEditActivity.EXTRA_STATE);
                        viewModel.onSavedEdit(editStates);
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
        ContentDb.getInstance().removeObserver(contentObserver);
        blockListManager.removeObserver(blockListObserver);
        mediaThumbnailLoader.destroy();
        contactLoader.destroy();
        urlPreviewLoader.destroy();
        reactionLoader.destroy();
    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        super.onActivityReenter(resultCode, data);

        if (resultCode == RESULT_OK && data != null && data.hasExtra(MediaExplorerActivity.EXTRA_CONTENT_ID) && data.hasExtra(MediaExplorerActivity.EXTRA_SELECTED)) {
            String contentId = data.getStringExtra(MediaExplorerActivity.EXTRA_CONTENT_ID);
            int position = data.getIntExtra(MediaExplorerActivity.EXTRA_SELECTED, 0);
            Post post = viewModel.post.getValue();

            if (!post.id.equals(contentId) || postContentContainer == null) {
                return;
            }

            RecyclerView gallery = postContentContainer.findViewById(R.id.media);
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
        outState.putString(KEY_REPLY_COMMENT_ID, replyCommentId);
        outState.putParcelable(KEY_COMMENT_MEDIA_URI, viewModel.getCommentMediaUri());
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (BuildConfig.DEBUG && keyCode == KeyEvent.KEYCODE_BACK) {
            final UserId userId = new UserId(Preconditions.checkNotNull(getIntent().getStringExtra(EXTRA_POST_SENDER_USER_ID)));
            final String postId = Preconditions.checkNotNull(getIntent().getStringExtra(EXTRA_POST_ID));
            Debug.showCommentsDebugMenu(this, editText, viewModel.post.getValue().getParentGroup(), postId);
            return true;
        }
        return super.onKeyLongPress(keyCode, event);
    }

    private void scrollToOriginal(@Nullable Comment comment) {
        if (comment == null) {
            return;
        }
        int c = adapter.getItemCount();
        for (int i=0; i<c; i++) {
            if (adapter.getItemId(i) == comment.rowId) {
                highlightedComment = comment.rowId;
                commentsLayoutManager.scrollToPosition(i);
                adapter.notifyDataSetChanged();
                break;
            }
        }
    }

    private void updateReplyIndicator(@NonNull String commentId) {
        replyCommentId = commentId;
        viewModel.loadReply(commentId);
    }

    private void resetReplyIndicator() {
        replyCommentId = null;
        viewModel.loadReply(null);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (requestCode == REQUEST_PERMISSION_CODE_RECORD_VOICE_NOTE) {
            if (EasyPermissions.permissionPermanentlyDenied(FlatCommentsActivity.this, Manifest.permission.RECORD_AUDIO)) {
                new AppSettingsDialog.Builder(FlatCommentsActivity.this)
                        .setRationale(getString(R.string.voice_note_record_audio_permission_rationale_denied))
                        .build().show();
            }
        }
    }

    private class VoiceNoteViewHolder extends BaseCommentViewHolder {
        private final SeekBar seekBar;
        private final ImageView controlButton;
        private final TextView seekTime;
        private final ProgressBar loading;
        private final TextView showComment;

        private boolean playing;

        private String audioPath;
        private boolean wasPlaying;

        private final Observer<VoiceNotePlayer.PlaybackState> playbackStateObserver;

        final int topPaddingNoName;

        public VoiceNoteViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnLongClickListener(v -> {
                if (comment == null) {
                    return false;
                }
                selectedComment = comment;
                selectedViewHolder = this;
                focusSelectedComment();
                updateActionMode();
                return true;
            });
            topPaddingNoName = itemView.getResources().getDimensionPixelSize(R.dimen.comment_voice_note_top_padding_no_name);
            seekBar = itemView.findViewById(R.id.voice_note_seekbar);
            controlButton = itemView.findViewById(R.id.control_btn);
            seekTime = itemView.findViewById(R.id.seek_time);
            loading = itemView.findViewById(R.id.loading);
            showComment = itemView.findViewById(R.id.show_comment);

            controlButton.setOnClickListener(v -> {
                if (playing) {
                    viewModel.getVoiceNotePlayer().pause();
                } else if (audioPath != null) {
                    viewModel.getVoiceNotePlayer().playFile(audioPath, seekBar.getProgress());
                }
            });

            playbackStateObserver = state -> {
                if (state == null || audioPath == null || !audioPath.equals(state.playingTag)) {
                    return;
                }
                if (playing != state.playing) {
                    playing = state.playing;
                    updateVoiceNoteTint(true);
                }
                if (playing) {
                    controlButton.setImageResource(R.drawable.ic_pause);
                    seekTime.setText(StringUtils.formatVoiceNoteDuration(seekTime.getContext(), state.seek));
                    if (!comment.played) {
                        comment.played = true;
                        ContentDb.getInstance().setCommentPlayed(comment.postId, comment.id, true);
                        updateVoiceNoteTint(true);
                    }
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
            };
        }

        @Override
        public void fillView(boolean changed) {
            UserId sender = this.comment.senderUserId;
            if (blockList != null && blockList.contains(sender)) {
                controlButton.setVisibility(View.GONE);
                loading.setVisibility(View.GONE);
                seekBar.setVisibility(View.GONE);
                seekTime.setVisibility(View.GONE);
                if (showComment != null) {
                    showComment.setVisibility(View.VISIBLE);
                    showComment.setOnClickListener(v -> {
                        fillVoiceNoteUnblocked(changed);
                        nameView.setText(comment.senderContact.getDisplayName());
                        if (nameView.getVisibility() != View.GONE) {
                            blockedView.setVisibility(View.VISIBLE);
                        }
                    });
                }
            } else {
                if (showComment != null) {
                    showComment.setOnClickListener(null);
                }
                fillVoiceNoteUnblocked(changed);
            }
        }

        public void fillVoiceNoteUnblocked(boolean changed) {
            if (showComment != null) {
                showComment.setVisibility(View.GONE);
            }
            controlButton.setVisibility(View.VISIBLE);
            loading.setVisibility(View.VISIBLE);
            seekBar.setVisibility(View.VISIBLE);
            seekTime.setVisibility(View.VISIBLE);
            if (blockedView != null) {
                blockedView.setVisibility(View.GONE);
            }
            if (comment.media != null && !comment.media.isEmpty()) {
                Media media = comment.media.get(0);
                if (media.transferred == Media.TRANSFERRED_YES) {
                    if (media.file != null) {
                        String newPath = media.file.getAbsolutePath();
                        if (!newPath.equals(audioPath)) {
                            this.audioPath = media.file.getAbsolutePath();
                            audioDurationLoader.load(seekTime, media);
                        }
                    }
                    loading.setVisibility(View.GONE);
                    controlButton.setVisibility(View.VISIBLE);
                } else {
                    loading.setVisibility(View.VISIBLE);
                    controlButton.setVisibility(View.INVISIBLE);
                }
            }
            updateVoiceNoteTint(comment.played);
            if (changed) {
                if (comment.isIncoming() && (nameView == null || nameView.getVisibility() == View.GONE)) {
                    contentView.setPadding(contentView.getPaddingLeft(), topPaddingNoName, contentView.getPaddingRight(), contentView.getPaddingBottom());
                } else {
                    contentView.setPadding(contentView.getPaddingLeft(), 0, contentView.getPaddingRight(), contentView.getPaddingBottom());
                }
            }
        }

        @Override
        public void markAttach() {
            super.markAttach();
            startObservingPlayback();
        }

        @Override
        public void markDetach() {
            super.markDetach();
            stopObservingPlayback();
        }

        @Override
        public void markRecycled() {
            super.markRecycled();
            audioPath = null;
            playing = false;
            seekBar.setProgress(0);
            controlButton.setImageResource(R.drawable.ic_play_arrow);
        }


        private void startObservingPlayback() {
            viewModel.getVoiceNotePlayer().getPlaybackState().observe(this, playbackStateObserver);
        }

        private void stopObservingPlayback() {
            viewModel.getVoiceNotePlayer().getPlaybackState().removeObservers(this);
        }

        private void updateVoiceNoteTint(boolean wasPlayed) {
            @ColorInt int color;
            if (wasPlayed) {
                color = ContextCompat.getColor(controlButton.getContext(), R.color.voice_note_played);
            } else {
                color = ContextCompat.getColor(controlButton.getContext(), R.color.color_secondary);
            }
            controlButton.setImageTintList(ColorStateList.valueOf(color));
            if (playing) {
                seekBar.getThumb().clearColorFilter();
            } else {
                seekBar.getThumb().setColorFilter(color, PorterDuff.Mode.SRC_IN);
            }
            loading.getIndeterminateDrawable().setColorFilter(color, PorterDuff.Mode.SRC_IN);
        }

    }
    private boolean updateActionMode() {
        if (actionMode == null) {
            actionMode = startSupportActionMode(new ActionMode.Callback() {
                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    getMenuInflater().inflate(R.menu.comment_select, menu);

                    if (selectedViewHolder.contentView != null && ServerProps.getInstance().getCommentReactionsEnabled()) {
                        reactionPopupWindow = new ReactionPopupWindow(getBaseContext(), selectedComment, () -> {
                            reactionPopupWindow.dismiss();
                            if (actionMode != null) {
                                actionMode.finish();
                            }
                        });
                        reactionPopupWindow.show(selectedViewHolder.contentView);
                    }

                    screenOverlay.setVisibility(View.VISIBLE);

                    return true;
                }

                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    menu.findItem(R.id.delete).setVisible(selectedComment != null && selectedComment.senderUserId.isMe());
                    menu.findItem(R.id.copy).setVisible(selectedComment != null && !TextUtils.isEmpty(selectedComment.text));
                    return true;
                }

                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    if (item.getItemId() == R.id.reply) {
                        if (selectedComment != null) {
                            updateReplyIndicator(selectedComment.id);
                        }
                        if (actionMode != null) {
                            actionMode.finish();
                        }
                        return true;
                    } else if (item.getItemId() == R.id.delete) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(FlatCommentsActivity.this);
                        builder.setMessage(R.string.retract_comment_confirmation);
                        builder.setNegativeButton(R.string.no, null);
                        DialogInterface.OnClickListener listener = (dialog, which) -> {
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE: {
                                    if (selectedComment != null) {
                                        ContentDb.getInstance().retractComment(selectedComment);
                                    }
                                    if (actionMode != null) {
                                        actionMode.finish();
                                    }
                                    break;
                                }
                            }
                        };
                        builder.setPositiveButton(R.string.yes, listener);
                        builder.create().show();
                        return true;
                    } else if (item.getItemId() == R.id.copy) {
                        String text = selectedComment != null ? selectedComment.text : null;
                        ClipUtils.copyToClipboard(text);
                        if (actionMode != null) {
                            actionMode.finish();
                        }
                    }
                    return true;
                }

                @Override
                public void onDestroyActionMode(ActionMode mode) {
                    if (reactionPopupWindow != null) {
                        reactionPopupWindow.dismiss();
                    }
                    unfocusSelectedComment();
                    selectedComment = null;
                    selectedViewHolder = null;
                    adapter.notifyDataSetChanged();
                    actionMode = null;
                    screenOverlay.setVisibility(View.INVISIBLE);
                }
            });
        } else {
            actionMode.invalidate();
        }
        adapter.notifyDataSetChanged();
        return true;
    }

    private class FutureProofViewHolder extends BaseCommentViewHolder {

        public FutureProofViewHolder(@NonNull View itemView) {
            super(itemView);

            TextView futureProofMessage = itemView.findViewById(R.id.future_proof_text);
            linkifyFutureProof(futureProofMessage);

            itemView.setOnLongClickListener(v -> {
                if (comment == null) {
                    return false;
                }
                selectedComment = comment;
                selectedViewHolder = this;
                focusSelectedComment();
                updateActionMode();
                return true;
            });
        }

        @Override
        public void fillView(boolean changed) {
        }
    }

    private class TombstoneViewHolder extends BaseCommentViewHolder {

        public TombstoneViewHolder(@NonNull View itemView) {
            super(itemView);

            TextView tombstoneText = itemView.findViewById(R.id.tombstone_text);

            CharSequence text = Html.fromHtml(tombstoneText.getContext().getString(R.string.comment_tombstone_placeholder));
            text = StringUtils.replaceLink(tombstoneText.getContext(), text, "learn-more", () -> {
                IntentUtils.openOurWebsiteInBrowser(tombstoneText, Constants.WAITING_ON_MESSAGE_FAQ_SUFFIX);
            });
            tombstoneText.setText(text);
            tombstoneText.setMovementMethod(LinkMovementMethod.getInstance());

            itemView.setOnLongClickListener(v -> {
                if (comment == null) {
                    return false;
                }
                selectedComment = comment;
                selectedViewHolder = this;
                focusSelectedComment();
                updateActionMode();
                return true;
            });
        }

        @Override
        public void fillView(boolean changed) {
        }
    }

    private void linkifyFutureProof(@NonNull TextView futureProofMessage) {
        CharSequence text = Html.fromHtml(futureProofMessage.getContext().getString(R.string.comment_upgrade_placeholder));
        text = StringUtils.replaceLink(futureProofMessage.getContext(), text, "update-app", () -> {
            IntentUtils.openPlayOrMarket(futureProofMessage);
        });
        futureProofMessage.setText(text);
        futureProofMessage.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private abstract class BaseCommentViewHolder extends ViewHolderWithLifecycle implements SwipeListItemHelper.SwipeableViewHolder {
        protected final TextView timestampView;
        protected final TextView decryptStatusView;
        protected final TextView nameView;
        protected final TextView blockedView;
        private final View linkPreviewContainer;
        private final TextView linkPreviewTitle;
        private final TextView linkPreviewUrl;
        private final ImageView linkPreviewImg;
        protected final View contentView;
        private final ReactionsLayout reactionsView;

        private final FrameLayout replyContainerView;
        private final TextView replyNameView;
        private final TextView replyTextView;
        private final ImageView replyMediaThumbView;
        private final ImageView replyMediaIconView;

        private final TextView newCommentsSeparator;
        private final TextView dateSeparator;

        private final GroupContentDecryptStatLoader groupContentDecryptStatLoader;
        private final HomeContentDecryptStatLoader homeContentDecryptStatLoader;

        private final Handler mainHandler = new Handler(Looper.getMainLooper());

        protected Comment comment;

        protected int position;

        private AnimatorSet highlightAnimation;

        public View getSwipeView() {
            return contentView;
        }

        private View.OnClickListener scrollToOriginalClickListener = v -> {
            if (comment.parentComment != null) {
                scrollToOriginal(comment.parentComment);
            }
        };

        public BaseCommentViewHolder(@NonNull View itemView) {
            super(itemView);

            nameView = itemView.findViewById(R.id.name);
            blockedView = itemView.findViewById(R.id.blocked);
            timestampView = itemView.findViewById(R.id.timestamp);
            decryptStatusView = itemView.findViewById(R.id.decrypt_status);
            linkPreviewContainer = itemView.findViewById(R.id.link_preview_container);
            linkPreviewTitle = itemView.findViewById(R.id.link_title);
            linkPreviewUrl = itemView.findViewById(R.id.link_domain);
            linkPreviewImg = itemView.findViewById(R.id.link_preview_image);
            contentView = itemView.findViewById(R.id.content);
            replyContainerView = itemView.findViewById(R.id.reply_container);
            newCommentsSeparator = itemView.findViewById(R.id.new_comments);
            dateSeparator = itemView.findViewById(R.id.date);
            reactionsView = itemView.findViewById(R.id.selected_emoji);

            groupContentDecryptStatLoader = new GroupContentDecryptStatLoader();
            homeContentDecryptStatLoader = new HomeContentDecryptStatLoader();

            if (replyContainerView != null) {
                LayoutInflater.from(replyContainerView.getContext()).inflate(R.layout.message_item_reply_content, replyContainerView);

                replyNameView = replyContainerView.findViewById(R.id.reply_name);
                replyTextView = replyContainerView.findViewById(R.id.reply_text);
                replyMediaThumbView = replyContainerView.findViewById(R.id.reply_media_thumb);
                replyMediaThumbView.setOutlineProvider(new ViewOutlineProvider() {
                    @Override
                    public void getOutline(View view, Outline outline) {
                        outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), replyMediaThumbView.getContext().getResources().getDimension(R.dimen.message_bubble_reply_corner_radius));
                    }
                });
                replyMediaThumbView.setClipToOutline(true);
                replyMediaIconView = replyContainerView.findViewById(R.id.reply_media_icon);
                replyContainerView.setOnClickListener(scrollToOriginalClickListener);
            } else {
                replyNameView = null;
                replyTextView = null;
                replyMediaThumbView = null;
                replyMediaIconView = null;
            }
            if (linkPreviewContainer != null) {
                linkPreviewContainer.setOutlineProvider(new ViewOutlineProvider() {
                    @Override
                    public void getOutline(View view, Outline outline) {
                        int left = 0;
                        int top = 0;
                        int right = view.getWidth();
                        int bottom = view.getHeight();
                        float cornerRadius = itemView.getContext().getResources().getDimension(R.dimen.message_bubble_reply_corner_radius);
                        outline.setRoundRect(left, top, right, bottom, cornerRadius);

                    }
                });
                linkPreviewContainer.setOnClickListener(v -> {
                    UrlPreview preview = comment == null ? null : comment.urlPreview;
                    if (preview != null && preview.url != null) {
                        IntentUtils.openUrlInBrowser(linkPreviewContainer, preview.url);
                    }
                });
                linkPreviewContainer.setClipToOutline(true);
            }
        }

        public void bindTo(@NonNull Comment comment, @Nullable Comment prevComment, @Nullable Comment nextComment, int position, int newCommentCountSeparator) {
            boolean changed = !Objects.equals(this.comment, comment);
            this.comment = comment;
            this.position = position;

            boolean diffSender = prevComment == null || !prevComment.senderUserId.equals(comment.senderUserId);

            if (selectedComment != null && selectedComment.rowId == comment.rowId) {
                itemView.setBackgroundColor(itemView.getContext().getResources().getColor(R.color.color_secondary_40_alpha));
            } else {
                itemView.setBackgroundColor(0);
            }
            if (highlightedComment == comment.rowId) {
                if (highlightAnimation != null) {
                    highlightAnimation.cancel();
                }
                AnimatorSet set = (AnimatorSet) AnimatorInflater.loadAnimator(itemView.getContext(), R.animator.message_highlight);
                set.setTarget(itemView);
                set.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        highlightedComment = -1;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        highlightedComment = -1;
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
                highlightAnimation = set;
                set.start();
            } else if (highlightAnimation != null) {
                highlightAnimation.cancel();
            }

            if (contentView != null) {
                View contentParent = (contentView.getParent() instanceof View) ? (View) contentView.getParent() : null;
                if (contentParent != null) {
                    int paddingTop;
                    int paddingBottom;
                    final int separator = contentParent.getResources().getDimensionPixelSize(R.dimen.comment_vertical_separator);
                    final int separatorSeq = contentParent.getResources().getDimensionPixelSize(R.dimen.comment_vertical_separator_sequence);
                    if (nextComment == null) {
                        paddingBottom = separator;
                    } else {
                        if (nextComment.senderUserId.equals(comment.senderUserId)) {
                            paddingBottom = separatorSeq / 2;
                        } else {
                            paddingBottom = separator / 2;
                        }
                    }
                    if (prevComment == null) {
                        paddingTop = separator;
                    } else if (diffSender) {
                        paddingTop = separator / 2;
                    } else {
                        paddingTop = separatorSeq / 2;
                    }
                    contentParent.setPadding(
                            contentParent.getPaddingLeft(),
                            paddingTop,
                            contentParent.getPaddingRight(),
                            paddingBottom);
                }
            }
            timestampView.setText(TimeFormatter.formatMessageTime(timestampView.getContext(), comment.timestamp));
            if (decryptStatusView != null) {
                if (comment.getParentPost() != null && comment.getParentPost().getParentGroup() != null) {
                    groupContentDecryptStatLoader.loadComment(this, decryptStatusView, comment.id);
                } else {
                    homeContentDecryptStatLoader.loadComment(this, decryptStatusView, comment.id);
                }
            }

            if (reactionsView != null) {
                reactionLoader.load(reactionsView, comment.id);
                reactionsView.setOnClickListener(v -> DialogFragmentUtils.showDialogFragmentOnce(ReactionListBottomSheetDialogFragment.newInstance(comment.id), getSupportFragmentManager()));
            }

            timestampRefresher.scheduleTimestampRefresh(comment.timestamp);
            if (nameView != null) {
                if (comment.isOutgoing()) {
                    nameView.setVisibility(View.GONE);
                    blockedView.setVisibility(View.GONE);
                } else if (diffSender) {
                    UserId sender = comment.senderUserId;
                    if (blockList != null && blockList.contains(sender)) {
                        nameView.setTextColor(getResources().getColor(R.color.primary_text));
                        nameView.setText(R.string.blocked_user_hidden_name);
                    } else {
                        nameView.setTextColor(GroupParticipants.getParticipantNameColor(nameView.getContext(), comment.senderContact));
                        nameView.setText(comment.senderContact.getDisplayName());
                    }
                    if (!comment.senderUserId.isMe()) {
                        nameView.setOnClickListener(v -> {
                            v.getContext().startActivity(ViewProfileActivity.viewProfile(v.getContext(), comment.senderUserId));
                        });
                    }
                    nameView.setVisibility(View.VISIBLE);
                } else {
                    nameView.setVisibility(View.GONE);
                    blockedView.setVisibility(View.GONE);
                }
            }
            if (linkPreviewContainer != null) {
                mediaThumbnailLoader.cancel(linkPreviewImg);
                if (comment.urlPreview == null) {
                    linkPreviewContainer.setVisibility(View.GONE);
                } else {
                    linkPreviewContainer.setVisibility(View.VISIBLE);
                    linkPreviewUrl.setText(comment.urlPreview.tld);
                    linkPreviewTitle.setText(comment.urlPreview.title);
                    @ColorRes int colorRes;
                    if (comment.isOutgoing()) {
                        colorRes = R.color.message_url_preview_background_outgoing;
                    } else {
                        colorRes = R.color.message_url_preview_background_incoming;
                    }
                    linkPreviewContainer.setBackgroundColor(ContextCompat.getColor(linkPreviewContainer.getContext(), colorRes));
                    if (comment.urlPreview.imageMedia == null || (comment.urlPreview.imageMedia.transferred != Media.TRANSFERRED_YES && comment.urlPreview.imageMedia.transferred != Media.TRANSFERRED_PARTIAL_CHUNKED)) {
                        linkPreviewImg.setVisibility(View.GONE);
                    } else {
                        linkPreviewImg.setVisibility(View.VISIBLE);
                        mediaThumbnailLoader.load(linkPreviewImg, comment.urlPreview.imageMedia);
                    }
                }
            }
            if (replyContainerView != null) {
                if (comment.parentCommentId == null) {
                    replyContainerView.setVisibility(View.GONE);
                } else {
                    replyContainerView.setVisibility(View.VISIBLE);
                    replyContainerView.setBackgroundResource(R.drawable.reply_frame_background);
                    replyContainerView.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(replyContainerView.getContext(),
                            comment.isOutgoing()
                                    ? R.color.message_background_reply_outgoing
                                    : R.color.message_background_reply_incoming)));
                    Comment parentComment = comment.parentComment;
                    if (parentComment == null) {
                        mediaThumbnailLoader.cancel(replyMediaThumbView);
                        replyMediaThumbView.setVisibility(View.GONE);
                        replyMediaIconView.setVisibility(View.GONE);
                        replyNameView.setText(R.string.unknown_contact);
                        replyTextView.setText(R.string.reply_original_comment_not_found);
                        replyTextView.setTypeface(replyTextView.getTypeface(), Typeface.ITALIC);
                    } else {
                        UserId sender = comment.senderUserId;
                        if (blockList != null && blockList.contains(sender)) {
                            replyTextView.setTextColor(getResources().getColor(R.color.show_comment));
                            replyTextView.setText(R.string.blocked_user_hidden_text);
                            replyTextView.setOnClickListener(v -> {
                                fillReplyUnblocked(changed);
                                nameView.setText(comment.senderContact.getDisplayName());
                                if (nameView.getVisibility() != View.GONE) {
                                    blockedView.setVisibility(View.VISIBLE);
                                }
                            });
                        } else {
                            replyTextView.setOnClickListener(scrollToOriginalClickListener);
                            fillReplyUnblocked(changed);
                        }
                    }
                }
            }
            if (newCommentsSeparator != null) {
                if (newCommentCountSeparator > 0) {
                    newCommentsSeparator.setVisibility(View.VISIBLE);
                    newCommentsSeparator.setText(newCommentsSeparator.getContext().getResources().getQuantityString(R.plurals.new_comment_separator, newCommentCountSeparator, newCommentCountSeparator));
                } else {
                    newCommentsSeparator.setVisibility(View.GONE);
                }
            }
            if (dateSeparator != null) {
                if (prevComment == null || !TimeUtils.isSameDay(comment.timestamp, prevComment.timestamp)) {
                    dateSeparator.setVisibility(View.VISIBLE);
                    dateSeparator.setText(TimeFormatter.formatMessageSeparatorDate(dateSeparator.getContext(), comment.timestamp));
                } else {
                    dateSeparator.setVisibility(View.GONE);
                }
            }

            fillView(changed);
        }

        public void fillReplyUnblocked(boolean changed) {
            if (blockedView != null) {
                blockedView.setVisibility(View.GONE);
            }
            Comment parentComment = comment.parentComment;
            Contact senderContact = parentComment.senderContact;
            replyNameView.setTextColor(GroupParticipants.getParticipantNameColor(FlatCommentsActivity.this, senderContact, true));
            replyNameView.setText(senderContact.userId.isMe() ? getString(R.string.me) : senderContact.getDisplayName());
            replyTextView.setTypeface(replyTextView.getTypeface(), Typeface.NORMAL);
            setCommentText(replyTextView, parentComment);
            if (parentComment.media.isEmpty()) {
                mediaThumbnailLoader.cancel(replyMediaThumbView);
                replyMediaThumbView.setVisibility(View.GONE);
                replyMediaIconView.setVisibility(View.GONE);
            } else {
                mediaThumbnailLoader.load(replyMediaThumbView, parentComment.media.get(0));
                replyMediaThumbView.setVisibility(View.VISIBLE);
                replyMediaIconView.setVisibility(View.VISIBLE);
                switch (parentComment.media.get(0).type) {
                    case Media.MEDIA_TYPE_IMAGE: {
                        replyMediaIconView.setImageResource(R.drawable.ic_camera);
                        break;
                    }
                    case Media.MEDIA_TYPE_VIDEO: {
                        replyMediaIconView.setImageResource(R.drawable.ic_video);
                        break;
                    }
                    case Media.MEDIA_TYPE_AUDIO: {
                        replyMediaIconView.setImageResource(R.drawable.ic_keyboard_voice);
                        replyMediaThumbView.setVisibility(View.GONE);
                        break;
                    }
                    case Media.MEDIA_TYPE_UNKNOWN:
                    default: {
                        replyMediaIconView.setImageResource(R.drawable.ic_media_collection);
                        break;
                    }
                }
            }
        }

        public void focusViewHolder() {
            if (contentView instanceof FocusableMessageView) {
                ((FocusableMessageView) contentView).focusView(drawDelegateView);
                drawDelegateView.invalidateDelegateView(contentView);
            }
        }

        public void unfocusViewHolder() {
            if (contentView instanceof FocusableMessageView) {
                ((FocusableMessageView) contentView).unfocusView();
            }
        }

        public void reloadReactions() {
            mainHandler.post(() -> reactionLoader.load(reactionsView, comment.id));
        }

        public abstract void fillView(boolean changed);
    }

    private class RetractedCommentViewHolder extends BaseCommentViewHolder {

        public RetractedCommentViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        public void fillView(boolean changed) {

        }
    }

    private class TextCommentViewHolder extends BaseCommentViewHolder {

        private final LimitingTextView textView;
        private final MessageTextLayout messageTextLayout;
        private final View mediaContainer;
        private final View videoIndicator;
        private final ImageView commentMedia;

        public TextCommentViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnLongClickListener(v -> {
                if (comment == null) {
                    return false;
                }
                selectedComment = comment;
                selectedViewHolder = this;
                focusSelectedComment();
                updateActionMode();
                return true;
            });

            textView = itemView.findViewById(R.id.text);
            messageTextLayout = itemView.findViewById(R.id.message_text_container);
            mediaContainer = itemView.findViewById(R.id.comment_media_container);

            videoIndicator = itemView.findViewById(R.id.comment_video_indicator);
            commentMedia = itemView.findViewById(R.id.comment_media);
            commentMedia.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), getResources().getDimension(R.dimen.comment_media_corner_radius));
                }
            });
            commentMedia.setClipToOutline(true);
        }

        @Override
        public void fillView(boolean changed) {
            final Integer textLimit = textLimits.get(comment.rowId);
            textView.setLineLimit(textLimit != null ? textLimit : Constants.TEXT_POST_LINE_LIMIT);
            textView.setLineLimitTolerance(textLimit != null ? Constants.POST_LINE_LIMIT_TOLERANCE : 0);
            textView.setOnReadMoreListener((view, limit) -> {
                textLimits.put(comment.rowId, limit);
                return false;
            });
            boolean emojisOnly = StringUtils.isFewEmoji(comment.text);
            UserId sender = comment.senderUserId;
            if (blockList != null && blockList.contains(sender)) {
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.message_text_size));
                textView.setTextColor(getResources().getColor(R.color.show_comment));
                textView.setText(R.string.blocked_user_hidden_text);
                textView.setOnClickListener(v -> {
                    if (!comment.media.isEmpty()) {
                        fillMediaUnblocked(changed);
                    }
                    fillTextUnblocked(changed, emojisOnly);
                    nameView.setText(comment.senderContact.getDisplayName());
                    if (nameView.getVisibility() != View.GONE) {
                        blockedView.setVisibility(View.VISIBLE);
                    }
                });
            } else {
                textView.setOnClickListener(null);
                if (!comment.media.isEmpty()) {
                    fillMediaUnblocked(changed);
                }
                fillTextUnblocked(changed, emojisOnly);
            }
            if (comment.media.isEmpty()) {
                mediaContainer.setVisibility(View.GONE);
            } else {
                if (blockList != null && blockList.contains(sender)) {
                    mediaContainer.setVisibility(View.GONE);
                    textView.setVisibility(View.VISIBLE);
                    textView.setTextColor(getResources().getColor(R.color.show_comment));
                    textView.setText(R.string.blocked_user_hidden_text);
                } else {
                    fillMediaUnblocked(changed);
                }
            }
        }

        public void fillMediaUnblocked(boolean changed) {
            mediaContainer.setVisibility(View.VISIBLE);
            if (blockedView != null) {
                blockedView.setVisibility(View.GONE);
            }
            Media media = comment.media.get(0);
            mediaThumbnailLoader.load(commentMedia, media);
            videoIndicator.setVisibility(media.type == Media.MEDIA_TYPE_VIDEO ? View.VISIBLE : View.GONE);
            commentMedia.setOnClickListener(v -> {
                commentsFsePosition = position;

                Post parentPost = comment.getParentPost();
                boolean isInGroup = parentPost != null && parentPost.getParentGroup() != null;

                Intent intent = new Intent(commentMedia.getContext(), MediaExplorerActivity.class);
                intent.putExtra(MediaExplorerActivity.EXTRA_MEDIA, MediaExplorerViewModel.MediaModel.fromMedia(Collections.singletonList(media)));
                intent.putExtra(MediaExplorerActivity.EXTRA_SELECTED, 0);
                intent.putExtra(MediaExplorerActivity.EXTRA_CONTENT_ID, comment.id);
                intent.putExtra(MediaExplorerActivity.EXTRA_ALLOW_SAVING, isInGroup);

                if (commentMedia.getContext() instanceof Activity) {
                    final ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(FlatCommentsActivity.this, commentMedia, commentMedia.getTransitionName());
                    startActivity(intent, options.toBundle());
                } else {
                    startActivity(intent);
                }
            });
            commentMedia.setTransitionName(MediaPagerAdapter.getTransitionName(comment.id, 0));
        }

        public void fillTextUnblocked(boolean changed, boolean emojisOnly) {
            if (blockedView != null) {
                blockedView.setVisibility(View.GONE);
            }
            if (TextUtils.isEmpty(comment.text)) {
                textView.setText(comment.text);
                textView.setVisibility(View.GONE);
            } else {
                textView.setVisibility(View.VISIBLE);
                textView.setTextColor(ContextCompat.getColor(textView.getContext(), comment.isIncoming() ? R.color.message_text_incoming : R.color.message_text_outgoing));
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textView.getResources().getDimension(emojisOnly ? R.dimen.message_text_size_few_emoji : R.dimen.message_text_size));
                if (messageTextLayout != null) {
                    messageTextLayout.setForceSeparateLine(emojisOnly);
                }
            }
            textContentLoader.load(textView, comment, false);
        }
    }

    private static class PostMediaItemViewHolder extends RecyclerView.ViewHolder {

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
            this.media = post.getMedia();
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
    private static final int ITEM_TYPE_TEXT_INCOMING = 1;
    private static final int ITEM_TYPE_TEXT_OUTGOING = 2;
    private static final int ITEM_TYPE_MEDIA_INCOMING = 3;
    private static final int ITEM_TYPE_MEDIA_OUTGOING = 4;
    private static final int ITEM_TYPE_FUTURE_PROOF_POST = 5;
    private static final int ITEM_TYPE_COMMENT_FUTURE_PROOF = 6;
    private static final int ITEM_TYPE_VOICE_NOTE_INCOMING = 7;
    private static final int ITEM_TYPE_VOICE_NOTE_OUTGOING = 8;
    private static final int ITEM_TYPE_RETRACTED_OUTGOING = 9;
    private static final int ITEM_TYPE_RETRACTED_INCOMING= 10;
    private static final int ITEM_TYPE_TOMBSTONE = 11;

    private class CommentsAdapter extends AdapterWithLifecycle<ViewHolderWithLifecycle> {

        final AsyncPagedListDiffer<Comment> differ;
        private long firstUnseenCommentId;

        private int unseenCommentCount;

        CommentsAdapter() {
            setHasStableIds(true);

            AdapterListUpdateCallback adapterCallback = new AdapterListUpdateCallback(this);
            ListUpdateCallback listUpdateCallback = new ListUpdateCallback() {

                public void onInserted(int position, int count) {
                    adapterCallback.onInserted(position, count);
                }

                public void onRemoved(int position, int count) {
                    adapterCallback.onRemoved(position, count);
                }

                public void onMoved(int fromPosition, int toPosition) {
                    adapterCallback.onMoved(fromPosition, toPosition);
                }

                public void onChanged(int position, int count, @Nullable Object payload) {
                    adapterCallback.onChanged(position, count, payload);
                }
            };

            differ = new AsyncPagedListDiffer<>(listUpdateCallback, new AsyncDifferConfig.Builder<>(DIFF_CALLBACK).build());
        }

        public void setNewCommentCount(int count) {
            if (this.unseenCommentCount != count) {
                this.unseenCommentCount = count;
                notifyDataSetChanged();
            }
        }

        public void setFirstUnseenCommentId(long rowId) {
            if (firstUnseenCommentId != rowId) {
                firstUnseenCommentId = rowId;
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
            return Preconditions.checkNotNull(getItem(position)).rowId;
        }

        @Override
        public int getItemCount() {
            return differ.getItemCount();
        }

        @Nullable Comment getItem(int position) {
            return differ.getItem(position);
        }

        @Override
        public int getItemViewType(int position) {
            final Comment comment = Preconditions.checkNotNull(getItem(position));
            boolean isGroupComment = comment.getParentPost() != null && comment.getParentPost().getParentGroup() != null;
            boolean showTombstoneIfDecryptFailed = (isGroupComment && !serverProps.getUsePlaintextGroupFeed()) || (!isGroupComment && !serverProps.getUsePlaintextHomeFeed());
            if (comment.transferred == Comment.TRANSFERRED_DECRYPT_FAILED && showTombstoneIfDecryptFailed) {
                return ITEM_TYPE_TOMBSTONE;
            }
            switch (comment.type) {
                case Comment.TYPE_FUTURE_PROOF:
                    return ITEM_TYPE_COMMENT_FUTURE_PROOF;
                case Comment.TYPE_VOICE_NOTE:
                    return comment.isIncoming() ? ITEM_TYPE_VOICE_NOTE_INCOMING : ITEM_TYPE_VOICE_NOTE_OUTGOING;
                case Comment.TYPE_RETRACTED:
                    return comment.isIncoming() ? ITEM_TYPE_RETRACTED_INCOMING : ITEM_TYPE_RETRACTED_OUTGOING;
                case Comment.TYPE_USER:
                    if (!comment.media.isEmpty()) {
                        return comment.isIncoming() ? ITEM_TYPE_MEDIA_INCOMING : ITEM_TYPE_MEDIA_OUTGOING;
                    }
                default:
                    return comment.isIncoming() ? ITEM_TYPE_TEXT_INCOMING : ITEM_TYPE_TEXT_OUTGOING;
            }
        }

        private @LayoutRes int getLayoutId(int viewType) {
            switch (viewType) {
                case ITEM_TYPE_FUTURE_PROOF_POST:
                    return R.layout.flat_comment_future_proof_post_item;
                case ITEM_TYPE_POST:
                    return R.layout.flat_comment_post_item;
                case ITEM_TYPE_TEXT_OUTGOING:
                case ITEM_TYPE_MEDIA_OUTGOING:
                    return R.layout.comment_item_outgoing_text;
                case ITEM_TYPE_TEXT_INCOMING:
                case ITEM_TYPE_MEDIA_INCOMING:
                    return R.layout.comment_item_incoming_text;
                case ITEM_TYPE_COMMENT_FUTURE_PROOF:
                    return R.layout.comment_item_future_proof;
                case ITEM_TYPE_VOICE_NOTE_INCOMING:
                    return R.layout.comment_item_incoming_voice_note;
                case ITEM_TYPE_VOICE_NOTE_OUTGOING:
                    return R.layout.comment_item_outgoing_voice_note;
                case ITEM_TYPE_RETRACTED_INCOMING:
                    return R.layout.comment_item_incoming_retracted;
                case ITEM_TYPE_RETRACTED_OUTGOING:
                    return R.layout.comment_item_outgoing_retracted;
                case ITEM_TYPE_TOMBSTONE:
                    return R.layout.comment_item_tombstone;
            }
            throw new IllegalArgumentException("unknown view type " + viewType);
        }

        @Override
        public @NonNull ViewHolderWithLifecycle onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            PressInterceptView root = new PressInterceptView(parent.getContext());
            root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            View itemView = LayoutInflater.from(parent.getContext()).inflate(getLayoutId(viewType), root, true);
            switch (viewType) {
                case ITEM_TYPE_COMMENT_FUTURE_PROOF:
                    return new FutureProofViewHolder(root);
                case ITEM_TYPE_TOMBSTONE:
                    return new TombstoneViewHolder(root);
                case ITEM_TYPE_VOICE_NOTE_INCOMING:
                case ITEM_TYPE_VOICE_NOTE_OUTGOING:
                    return new VoiceNoteViewHolder(root);
                case ITEM_TYPE_TEXT_INCOMING:
                case ITEM_TYPE_TEXT_OUTGOING:
                case ITEM_TYPE_MEDIA_INCOMING:
                case ITEM_TYPE_MEDIA_OUTGOING:
                default:
                    return new TextCommentViewHolder(root);
                case ITEM_TYPE_RETRACTED_INCOMING:
                case ITEM_TYPE_RETRACTED_OUTGOING:
                    return new RetractedCommentViewHolder(root);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolderWithLifecycle holder, int position) {
            if (holder instanceof BaseCommentViewHolder) {
                Comment prevComment = null;
                if (position > 0) {
                    prevComment = getItem(position - 1);
                }
                Comment nextComment = null;
                if (position < getItemCount() - 1) {
                    nextComment = getItem(position + 1);
                }
                Comment comment = Preconditions.checkNotNull(getItem(position));
                ((BaseCommentViewHolder) holder).bindTo(comment, prevComment, nextComment, position, comment.rowId == firstUnseenCommentId ? unseenCommentCount : 0);
            }
        }
    }

    private void setCommentText(@NonNull TextView textView, @NonNull Comment comment) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        CharSequence commentText = "";
        audioDurationLoader.cancel(textView);
        boolean setCommentText = true;
        switch (comment.type) {
            case Comment.TYPE_FUTURE_PROOF: {
                commentText = getString(R.string.unsupported_content);
                break;
            }
            case Comment.TYPE_RETRACTED: {
                SpannableString s = new SpannableString(getString(R.string.comment_retracted_placeholder));
                Object span;
                span = new StyleSpan(Typeface.ITALIC);
                s.setSpan(span, 0, s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                commentText = s;
                break;
            }
            case Comment.TYPE_USER: {
                SpannableStringBuilder mediaText = new SpannableStringBuilder();
                if (!TextUtils.isEmpty(comment.text)) {
                    mediaText.append(MarkdownUtils.formatMarkdownWithMentions(this, comment.getText(), comment.mentions, false, (v, mention) -> {
                        v.getContext().startActivity(ViewProfileActivity.viewProfile(v.getContext(), mention.userId));
                    }));
                } else if (!comment.media.isEmpty()){
                    switch (comment.media.get(0).type) {
                        case Media.MEDIA_TYPE_IMAGE: {
                            mediaText.append(getString(R.string.photo));
                            break;
                        }
                        case Media.MEDIA_TYPE_VIDEO: {
                            mediaText.append(getString(R.string.video));
                            break;
                        }
                        case Media.MEDIA_TYPE_AUDIO: {
                            break;
                        }
                        default:
                            break;
                    }
                }
                commentText = mediaText;
                break;
            }
            case Comment.TYPE_VOICE_NOTE: {
                setCommentText = false;
                audioDurationLoader.load(textView, comment.media.get(0).file, new ViewDataLoader.Displayer<TextView, Long>() {
                    @Override
                    public void showResult(@NonNull TextView view, @Nullable Long result) {
                        if (result != null) {
                            builder.append(getString(R.string.voice_note_preview, StringUtils.formatVoiceNoteDuration(view.getContext(), result)));
                            view.setText(builder);
                        }
                    }

                    @Override
                    public void showLoading(@NonNull TextView view) {
                        SpannableStringBuilder loading = new SpannableStringBuilder(builder);
                        loading.append(getString(R.string.voice_note));
                        view.setText(loading);
                    }
                });
                break;
            }
            default:
                commentText = "";
        }
        builder.append(commentText);

        if (setCommentText) {
            textView.setText(builder);
        }
    }
}
