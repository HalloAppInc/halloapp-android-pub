package com.halloapp.katchup;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.inputmethod.InputMethodManager;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ActionMode;
import androidx.camera.view.PreviewView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.emoji2.text.EmojiSpan;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.AsyncPagedListDiffer;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.AdapterListUpdateCallback;
import androidx.recyclerview.widget.AsyncDifferConfig;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListUpdateCallback;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.TransitionManager;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.halloapp.Constants;
import com.halloapp.Me;
import com.halloapp.R;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactLoader;
import com.halloapp.content.Comment;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Media;
import com.halloapp.content.Post;
import com.halloapp.emoji.EmojiKeyboardLayout;
import com.halloapp.id.UserId;
import com.halloapp.katchup.avatar.KAvatarLoader;
import com.halloapp.katchup.media.KatchupExoPlayer;
import com.halloapp.katchup.ui.Colors;
import com.halloapp.katchup.ui.TextStickerView;
import com.halloapp.katchup.ui.VideoReactionRecordControlView;
import com.halloapp.katchup.vm.CommentsViewModel;
import com.halloapp.media.ExoUtils;
import com.halloapp.media.MediaThumbnailLoader;
import com.halloapp.ui.AdapterWithLifecycle;
import com.halloapp.ui.ExternalMediaThumbnailLoader;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.ViewHolderWithLifecycle;
import com.halloapp.ui.camera.HalloCamera;
import com.halloapp.util.KeyboardUtils;
import com.halloapp.util.Preconditions;
import com.halloapp.util.StringUtils;
import com.halloapp.util.TimeFormatter;
import com.halloapp.util.ViewDataLoader;
import com.halloapp.widget.ContentPlayerView;
import com.halloapp.widget.PressInterceptView;
import com.halloapp.widget.SnackbarHelper;

import java.io.File;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;

import pub.devrel.easypermissions.EasyPermissions;

public class ViewKatchupCommentsActivity extends HalloActivity {

    public static Intent viewPost(@NonNull Context context, @NonNull Post post) {
        return viewPost(context, post, false);
    }

    public static Intent viewPost(@NonNull Context context, @NonNull Post post, boolean isPublic) {
        return viewPost(context, post.id, isPublic);
    }

    public static Intent viewPost(@NonNull Context context, @NonNull String postId, boolean isPublic) {
        Intent i = new Intent(context, ViewKatchupCommentsActivity.class);
        i.putExtra(EXTRA_POST_ID, postId);
        i.putExtra(EXTRA_IS_PUBLIC_POST, isPublic);

        return i;
    }

    private static final int REQUEST_CODE_ASK_CAMERA_AND_AUDIO_PERMISSION = 1;
    private static final String EXTRA_POST_ID = "post_id";
    private static final String EXTRA_IS_PUBLIC_POST = "is_public_post";

    private static final int MAX_RECORD_TIME_SECONDS = 15;

    private final KAvatarLoader kAvatarLoader = KAvatarLoader.getInstance();

    private MediaThumbnailLoader mediaThumbnailLoader;

    private CommentsViewModel viewModel;

    private ImageView avatarView;
    private TextView nameView;
    private ImageView postPhotoView;
    private ContentPlayerView postVideoView;

    private View selfieContainer;
    private ContentPlayerView selfieView;

    private ContactLoader contactLoader;
    private KatchupExoPlayer contentPlayer;
    private KatchupExoPlayer selfiePlayer;

    private View contentContainer;
    private View contentProtection;
    private View recordProtection;
    private EmojiKeyboardLayout emojiKeyboardLayout;

    private boolean protectionFromBottomsheet;
    private boolean protectionFromKeyboard;
    private boolean protectionFromRecording;

    private float bottomsheetSlide;

    private BottomSheetBehavior bottomSheetBehavior;

    private int selfieMargin;

    private ImageView sendButtonAvatarView;
    private View sendButtonContainer;
    private View recordVideoReaction;
    private View videoPreviewBlock;
    private View shareButton;
    private View moreButton;

    private EditText textEntry;

    private String postId;
    private boolean isMyOwnPost;
    private float selfieTranslationX;
    private float selfieTranslationY;
    private int selfieVerticalMargin;
    private int selfieHorizontalMargin;

    private boolean scrollToBottom = false;

    private LinearLayoutManager commentLayoutManager;

    private HalloCamera camera;

    private PreviewView videoPreviewView;
    private View videoPreviewContainer;
    private VideoReactionProgressView videoProgressContainer;

    private View entryDisclaimer;
    private View entryContainer;

    private View totalEntry;

    private VideoReactionRecordControlView videoReactionRecordControlView;

    private boolean canceled = false;

    private final HashSet<KatchupExoPlayer> currentPlayers = new HashSet<>();

    private View videoRecordAvatarContainer;
    private Chronometer videoDurationChronometer;
    private View videoRecordIndicator;

    private View stickerSendContainer;
    private TextStickerView textStickerPreview;
    private RecyclerView emojiStickerRv;

    private boolean keyboardOpened;
    private boolean canTextBeSticker = true;

    private RecyclerView commentsRv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_view_comments);

        commentsRv = findViewById(R.id.comments_rv);

        CommentsAdapter adapter = new CommentsAdapter();
        commentLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        commentsRv.setLayoutManager(commentLayoutManager);
        commentsRv.setAdapter(adapter);

        selfieMargin = getResources().getDimensionPixelSize(R.dimen.selfie_margin);

        contactLoader = new ContactLoader();

        totalEntry = findViewById(R.id.entry);
        textStickerPreview = findViewById(R.id.sticker_preview);
        emojiStickerRv = findViewById(R.id.emoji_preview);
        stickerSendContainer = findViewById(R.id.sticker_send_container);
        videoRecordAvatarContainer = findViewById(R.id.video_reaction_avatar_container);
        videoDurationChronometer = findViewById(R.id.recording_time);
        videoRecordIndicator = findViewById(R.id.recording_indicator);
        entryContainer = findViewById(R.id.entry_container);
        entryDisclaimer = findViewById(R.id.entry_disclaimer);
        videoPreviewBlock = findViewById(R.id.preview_block);
        videoReactionRecordControlView = findViewById(R.id.reaction_control_view);
        videoProgressContainer = findViewById(R.id.video_reaction_progress);
        videoPreviewContainer = findViewById(R.id.video_preview_container);
        videoPreviewView = findViewById(R.id.video_preview);
        recordVideoReaction = findViewById(R.id.video_reaction_record_button);
        sendButtonContainer = findViewById(R.id.send_comment_button);
        sendButtonAvatarView = findViewById(R.id.send_avatar);
        avatarView = findViewById(R.id.avatar);
        nameView = findViewById(R.id.name_text_view);
        selfieContainer = findViewById(R.id.selfie_container);
        postVideoView = findViewById(R.id.content_video);
        postPhotoView = findViewById(R.id.content_photo);
        contentProtection = findViewById(R.id.content_protection);
        recordProtection = findViewById(R.id.record_protection);
        selfieView = findViewById(R.id.selfie_player);
        emojiKeyboardLayout = findViewById(R.id.emoji_keyboard);
        textEntry = findViewById(R.id.entry_card);
        ImageView kbToggle = findViewById(R.id.kb_toggle);
        shareButton = findViewById(R.id.share_button);
        moreButton = findViewById(R.id.more_options);
        emojiKeyboardLayout.bind(kbToggle, textEntry);
        emojiKeyboardLayout.addListener(new EmojiKeyboardLayout.Listener() {
            @Override
            public void onKeyboardOpened() {
                protectionFromKeyboard = true;
                updateContentProtection();
                keyboardOpened = true;
                emojiKeyboardLayout.post(ViewKatchupCommentsActivity.this::updateStickerSendPreview);
            }

            @Override
            public void onKeyboardClosed() {
                protectionFromKeyboard = false;
                updateContentProtection();
                keyboardOpened = false;
                emojiKeyboardLayout.post(ViewKatchupCommentsActivity.this::updateStickerSendPreview);
            }
        });
        contentContainer = findViewById(R.id.content_container);
        final float radius = getResources().getDimension(R.dimen.post_card_radius);
        ViewOutlineProvider roundedOutlineProvider = new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), radius);
            }
        };
        contentContainer.setClipToOutline(true);
        contentContainer.setOutlineProvider(roundedOutlineProvider);

        boolean isPublic = getIntent().getBooleanExtra(EXTRA_IS_PUBLIC_POST, false);

        final Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);
        mediaThumbnailLoader = isPublic
                ? new ExternalMediaThumbnailLoader(this, Math.min(Constants.MAX_IMAGE_DIMENSION, Math.max(point.x, point.y)))
                : new MediaThumbnailLoader(this, Math.min(Constants.MAX_IMAGE_DIMENSION, Math.max(point.x, point.y)));

        viewModel = new ViewModelProvider(this, new CommentsViewModel.CommentsViewModelFactory(getIntent().getStringExtra(EXTRA_POST_ID), isPublic)).get(CommentsViewModel.class);
        viewModel.getPost().observe(this, this::bindPost);

        bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottom_sheet));
        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    protectionFromBottomsheet = false;
                } else {
                    if (!protectionFromBottomsheet) {
                        protectionFromBottomsheet = true;
                    }
                }
                updateContentProtection();
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                bottomsheetSlide = slideOffset;
                updateContentProtection();
            }
        });
        kAvatarLoader.load(sendButtonAvatarView, UserId.ME);
        textEntry.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                CharSequence trimmed = StringUtils.unicodeTrim(s);
                if (TextUtils.isEmpty(trimmed)) {
                    recordVideoReaction.setVisibility(View.VISIBLE);
                    sendButtonContainer.setVisibility(View.GONE);
                    setCanTextBeSticker(true);
                    randomizeTextStickerColor();
                    emojiStickerRv.setVisibility(View.VISIBLE);
                    textStickerPreview.setVisibility(View.GONE);
                    updateStickerSendPreview();
                } else {
                    sendButtonContainer.setVisibility(View.VISIBLE);
                    recordVideoReaction.setVisibility(View.INVISIBLE);
                    emojiStickerRv.setVisibility(View.GONE);
                    textStickerPreview.setVisibility(View.VISIBLE);
                    if (s.getSpans(0, s.length(), EmojiSpan.class).length > 0 || trimmed.length() >= 10) {
                        setCanTextBeSticker(false);
                    } else {
                        setCanTextBeSticker(true);
                        textStickerPreview.setText(trimmed.toString().toUpperCase(Locale.getDefault()));
                    }
                    updateStickerSendPreview();
                }
            }
        });

        shareButton.setOnClickListener(v -> {
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(getString(R.string.share_moment_progress));
            progressDialog.show();
            viewModel.shareExternallyWithPreview(this).observe(this, intent -> {
                startActivity(intent);
                progressDialog.cancel();
            });
        });

        moreButton.setOnClickListener(v -> {
            PopupMenu menu = new PopupMenu(this, v);
            menu.inflate(R.menu.katchup_post);

            menu.getMenu().findItem(R.id.report).setVisible(!isMyOwnPost);
            menu.getMenu().findItem(R.id.delete).setVisible(isMyOwnPost);
            menu.getMenu().findItem(R.id.save).setVisible(isMyOwnPost);

            menu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.report) {
                    Post post = viewModel.getPost().getValue();
                    startActivity(ReportActivity.open(this, post.senderUserId, post.id));
                } else if (item.getItemId() == R.id.delete) {
                    Post post = viewModel.getPost().getValue();
                    Analytics.getInstance().deletedPost();
                    ContentDb.getInstance().retractPost(post);
                    finish();
                } else if (item.getItemId() == R.id.save) {
                    viewModel.saveToGallery(this).observe(this, success -> {
                        if (Boolean.TRUE.equals(success)) {
                            SnackbarHelper.showInfo(this, R.string.media_saved_to_gallery);
                        } else {
                            SnackbarHelper.showWarning(this, R.string.media_save_to_gallery_failed);
                        }
                    });
                }

                return false;
            });

            menu.show();
        });

        sendButtonContainer.setOnClickListener(v -> {
            if (canTextBeSticker) {
                sendSticker();
            } else {
                viewModel.sendComment(textEntry.getText().toString());
                onSendComment();
            }
        });
        videoReactionRecordControlView.setRecordingListener(new VideoReactionRecordControlView.RecordingListener() {
            @Override
            public void onCancel() {
                canceled = true;
                onStopRecording();
            }

            @Override
            public void onSend() {
                canceled = false;
                onStopRecording();
            }
        });
        recordVideoReaction.setOnTouchListener((v, event) -> {
            final int action = event.getActionMasked();
            if (action == MotionEvent.ACTION_DOWN) {
                if (!EasyPermissions.hasPermissions(this, Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA)) {
                    requestCameraAndAudioPermission();
                    return false;
                }
                canceled = false;
                videoReactionRecordControlView.setVisibility(View.VISIBLE);
                camera.bindCameraUseCases();
                videoPreviewContainer.setVisibility(View.VISIBLE);
                videoProgressContainer.setVisibility(View.VISIBLE);
                entryContainer.setVisibility(View.INVISIBLE);
                entryDisclaimer.setVisibility(View.INVISIBLE);
                protectionFromRecording = true;
                videoRecordAvatarContainer.setVisibility(View.INVISIBLE);
                updateContentProtection();
            }
            videoReactionRecordControlView.onTouch(event);
            return true;
        });

        viewModel.getCommentList().observe(this, list -> {
            adapter.submitList(list, () -> {
                if (!scrollToBottom) {
                    return;
                }
                int lastVisible = commentLayoutManager.findLastCompletelyVisibleItemPosition();
                if (lastVisible != list.size() - 1) {
                    commentsRv.scrollToPosition(list.size() - 1);
                }
                scrollToBottom = false;
            });
        });
        initializeCamera();
        videoPreviewView.getPreviewStreamState().observe(this, state -> {
            if (state == PreviewView.StreamState.STREAMING) {
                videoPreviewBlock.setVisibility(View.GONE);
                startRecordingReaction();
            } else {
                videoPreviewBlock.setVisibility(View.VISIBLE);
            }
        });
        videoDurationChronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                long dT = SystemClock.elapsedRealtime() - chronometer.getBase();
                if (dT / 1_000 >= MAX_RECORD_TIME_SECONDS) {
                    canceled = false;
                    onStopRecording();
                }
            }
        });

        emojiStickerRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        emojiStickerRv.setAdapter(new EmojiStickerAdapter());

        textStickerPreview.setOnClickListener(v -> sendSticker());

        viewModel.getSelectedComment().observe(this, selectedComment -> {
            if (selectedComment != null) {
                updateActionMode();
            } else if (actionMode != null) {
                actionMode.finish();
            }
        });

        makeSelfieDraggable();
        moveSelfieToCorner();

        randomizeTextStickerColor();
    }

    @Override
    public void onResume() {
        super.onResume();
        Analytics.getInstance().openScreen("comments");
    }

    private void sendSticker() {
        viewModel.sendTextSticker(textStickerPreview.getText().toString(), textStickerPreview.getCurrentTextColor());
        onSendComment(true);
    }

    private ActionMode actionMode;

    private boolean updateActionMode() {
        if (actionMode == null) {
            actionMode = startSupportActionMode(new ActionMode.Callback() {

                private int statusBarColor;
                private int previousVisibility;

                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    getMenuInflater().inflate(R.menu.menu_comments, menu);

                    statusBarColor = getWindow().getStatusBarColor();
                    getWindow().setStatusBarColor(getResources().getColor(R.color.black_80));
                    previousVisibility = getWindow().getDecorView().getSystemUiVisibility();
                    if (Build.VERSION.SDK_INT >= 23) {
                        getWindow().getDecorView().setSystemUiVisibility(previousVisibility & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                    }

                    return true;
                }

                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    Comment comment = viewModel.getSelectedComment().getValue();
                    if (comment == null) {
                        return false;
                    }
                    MenuItem deleteItem = menu.findItem(R.id.delete);
                    MenuItem viewProfile = menu.findItem(R.id.view_profile);
                    MenuItem blockItem = menu.findItem(R.id.block);

                    Post post = viewModel.getPost().getValue();
                    boolean amCommentSender = comment.senderUserId.isMe();
                    boolean amPostSender = post != null && post.senderUserId.isMe();

                    deleteItem.setVisible(amCommentSender || amPostSender);
                    viewProfile.setVisible(!amCommentSender);
                    blockItem.setVisible(!amCommentSender);
                    return true;
                }

                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    if (item.getItemId() == R.id.delete) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(ViewKatchupCommentsActivity.this);
                        builder.setMessage(getResources().getQuantityString(R.plurals.delete_messages_confirmation, 1, 1));
                        builder.setNegativeButton(R.string.cancel, null);
                        DialogInterface.OnClickListener listener = (dialog, which) -> {
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE: {
                                    handleMessageRetract();
                                    break;
                                }
                            }
                        };
                        builder.setPositiveButton(R.string.delete, listener);
                        builder.create().show();
                        return true;
                    } else if (item.getItemId() == R.id.view_profile) {
                        Comment comment = viewModel.getSelectedComment().getValue();
                        if (comment != null) {
                            startActivity(ViewKatchupProfileActivity.viewProfile(ViewKatchupCommentsActivity.this, comment.senderUserId));
                        }
                        if (actionMode != null) {
                            actionMode.finish();
                        }
                    }
                    return true;
                }

                @Override
                public void onDestroyActionMode(ActionMode mode) {
                    viewModel.selectComment(null);
                    actionMode = null;

                    getWindow().setStatusBarColor(statusBarColor);
                    getWindow().getDecorView().setSystemUiVisibility(previousVisibility);
                }
            });
        } else {
            Comment comment = viewModel.getSelectedComment().getValue();
            if (comment == null) {
                actionMode.finish();
            } else {
                actionMode.invalidate();
            }
        }
        return true;
    }

    private void makeSelfieDraggable() {
        selfieVerticalMargin = getResources().getDimensionPixelSize(R.dimen.compose_selfie_vertical_margin);
        selfieHorizontalMargin = getResources().getDimensionPixelSize(R.dimen.compose_selfie_horizontal_margin);
        selfieContainer.setOnTouchListener(new View.OnTouchListener() {

            private float startX;
            private float startY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    startX = event.getRawX();
                    startY = event.getRawY();
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    selfieContainer.setTranslationX(selfieTranslationX + (event.getRawX() - startX));
                    selfieContainer.setTranslationY(selfieTranslationY + (event.getRawY() - startY));
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    selfieTranslationX = selfieContainer.getTranslationX();
                    selfieTranslationY = selfieContainer.getTranslationY();
                    forceWithinBounds();
                }
                return false;
            }

            private void forceWithinBounds() {
                float posX = selfieContainer.getX();
                float posY = selfieContainer.getY();

                int width = selfieContainer.getWidth();
                int height = selfieContainer.getHeight();

                if (selfieContainer.getX() + width > contentContainer.getRight() - selfieHorizontalMargin) {
                    selfieTranslationX = (contentContainer.getRight() - selfieHorizontalMargin) - selfieContainer.getRight();
                }
                if (posX < selfieHorizontalMargin) {
                    selfieTranslationX = selfieHorizontalMargin - selfieContainer.getLeft();
                }

                if (posY < contentContainer.getY() + selfieVerticalMargin) {
                    selfieTranslationY = (contentContainer.getTop() + selfieVerticalMargin) - selfieContainer.getTop();
                }
                if (posY + height > contentContainer.getBottom() - selfieVerticalMargin) {
                    selfieTranslationY = (contentContainer.getBottom() - selfieVerticalMargin) - selfieContainer.getBottom();
                }

                selfieContainer.setTranslationX(selfieTranslationX);
                selfieContainer.setTranslationY(selfieTranslationY);
            }
        });
    }

    private void handleMessageRetract() {
        ProgressDialog createDeleteMessageDialog = ProgressDialog.show(ViewKatchupCommentsActivity.this, null, getResources().getQuantityString(R.plurals.delete_messages_progress, 1, 1));
        Comment selectedMessage = viewModel.getSelectedComment().getValue();
        if (selectedMessage != null) {
            viewModel.retractComment(selectedMessage, createDeleteMessageDialog);
        }
        if (actionMode != null) {
            actionMode.finish();
        }
    }

    private void setCanTextBeSticker(boolean canBeSticker) {
        if (this.canTextBeSticker == canBeSticker) {
            return;
        }
        this.canTextBeSticker = canBeSticker;
    }

    private void randomizeTextStickerColor() {
        textStickerPreview.setTextColor(ContextCompat.getColor(this, Colors.getRandomStickerColor()));
    }

    private void onSendComment() {
        onSendComment(false);
    }

    private void onSendComment(boolean hideKeyboard) {
        textEntry.setText("");
        KeyboardUtils.hideSoftKeyboard(textEntry);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        scrollToBottom = true;
    }

    private void updateStickerSendPreview() {
        if (keyboardOpened && canTextBeSticker) {
            stickerSendContainer.setVisibility(View.VISIBLE);
        } else {
            stickerSendContainer.setVisibility(View.GONE);
        }
    }


    private class EmojiStickerViewHolder extends RecyclerView.ViewHolder {

        private TextView emojiTv;
        private String emoji;

        public EmojiStickerViewHolder(@NonNull View itemView) {
            super(itemView);

            emojiTv = itemView.findViewById(R.id.emoji);
            itemView.setOnClickListener(v -> {
                viewModel.sendComment(emoji);
                onSendComment(true);
            });
        }

        public void bind(String emoji) {
            this.emoji = emoji;
            emojiTv.setText(emoji);
        }
    }

    private class EmojiStickerAdapter extends RecyclerView.Adapter<EmojiStickerViewHolder> {

        private String[] emojis = new String[] {"\uD83E\uDD70", "\uD83D\uDE06", "\uD83E\uDEE0", "\uD83E\uDD72", "\uD83D\uDCAF", "\uD83E\uDD7A"};

        @NonNull
        @Override
        public EmojiStickerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new EmojiStickerViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_emoji_sticker, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull EmojiStickerViewHolder holder, int position) {
            holder.bind(emojis[position]);
        }

        @Override
        public int getItemCount() {
            return emojis.length;
        }
    }

    private void onStopRecording() {
        if (camera.isRecordingVideo()) {
            camera.stopRecordingVideo();
            videoPreviewContainer.setVisibility(View.GONE);
            videoProgressContainer.setVisibility(View.GONE);
            videoReactionRecordControlView.setVisibility(View.GONE);
            camera.unbind();
            entryContainer.setVisibility(View.VISIBLE);
            entryDisclaimer.setVisibility(View.VISIBLE);
            protectionFromRecording = false;
            updateContentProtection();
            videoRecordAvatarContainer.setVisibility(View.VISIBLE);
            videoRecordIndicator.setVisibility(View.GONE);
            videoDurationChronometer.setVisibility(View.GONE);
            videoDurationChronometer.stop();
            videoProgressContainer.stopProgress();
        }
    }

    private void startRecordingReaction() {
        camera.startRecordingVideo();
        videoDurationChronometer.setBase(SystemClock.elapsedRealtime());
        videoDurationChronometer.start();
        videoRecordIndicator.setVisibility(View.VISIBLE);
        videoDurationChronometer.setVisibility(View.VISIBLE);
        videoProgressContainer.startProgress(MAX_RECORD_TIME_SECONDS);
    }

    private void initializeCamera() {
        camera = new HalloCamera(this, videoPreviewView, true, false, Surface.ROTATION_0, new HalloCamera.DefaultListener() {
            @Override
            public void onCaptureSuccess(File file, int type) {
                runOnUiThread(() -> {
                    viewModel.onVideoReaction(file, canceled).observe(ViewKatchupCommentsActivity.this, sent -> {
                        onSendComment();
                    });
                });
            }

            @Override
            public void onCameraPermissionsMissing() {
                runOnUiThread(ViewKatchupCommentsActivity.this::requestCameraAndAudioPermission);
            }
        });
    }

    private void requestCameraAndAudioPermission() {
        final String[] permissions = { Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO };
        if (!EasyPermissions.hasPermissions(this, permissions)) {
            EasyPermissions.requestPermissions(this, getString(R.string.camera_record_audio_permission_rationale),
                    REQUEST_CODE_ASK_CAMERA_AND_AUDIO_PERMISSION, permissions);
        }
    }

    @Override
    public void onBackPressed() {
        if (emojiKeyboardLayout.isEmojiKeyboardOpen()) {
            emojiKeyboardLayout.hideEmojiKeyboard();
            return;
        }
        if (bottomSheetBehavior.getState() < BottomSheetBehavior.STATE_COLLAPSED) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            commentLayoutManager.scrollToPosition(0);
            return;
        }
        super.onBackPressed();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (getCurrentFocus() != null) {
            int[] location = new int[2];
            totalEntry.getLocationOnScreen(location);
            if (ev.getX() < location[0] || ev.getX() > location[0] + totalEntry.getWidth() || ev.getY() < location[1] || ev.getY() > location[1] + totalEntry.getHeight()) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    private void updateContentPlayingForProtection() {
        if (protectionFromBottomsheet || protectionFromKeyboard || protectionFromRecording) {
            if (contentPlayer != null) {
                contentPlayer.pause();
            }
            if (selfiePlayer != null) {
                selfiePlayer.pause();
            }
        } else {
            if (contentPlayer != null) {
                contentPlayer.play();
            }
            if (selfiePlayer != null) {
                selfiePlayer.play();
            }
        }
        if (protectionFromRecording) {
            for (KatchupExoPlayer player : currentPlayers) {
                SimpleExoPlayer p = player.getPlayer();
                p.setPlayWhenReady(false);
            }
        } else {
            for (KatchupExoPlayer player : currentPlayers) {
                SimpleExoPlayer p = player.getPlayer();
                p.setPlayWhenReady(true);
            }
        }
    }

    private void updateProtectionOverlays() {
        if (protectionFromRecording) {
            contentProtection.setVisibility(View.INVISIBLE);
            recordProtection.setVisibility(View.VISIBLE);
        } else if (protectionFromBottomsheet || protectionFromKeyboard) {
            if (!protectionFromKeyboard) {
                contentProtection.setAlpha(bottomsheetSlide);
            } else {
                contentProtection.setAlpha(1f);
            }
            contentProtection.setVisibility(View.VISIBLE);
            recordProtection.setVisibility(View.INVISIBLE);
        } else {
            contentProtection.setVisibility(View.INVISIBLE);
            recordProtection.setVisibility(View.INVISIBLE);
        }
    }

    private void updateContentProtection() {
        updateProtectionOverlays();
        updateContentPlayingForProtection();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (selfiePlayer != null) {
            selfiePlayer.destroy();
            selfiePlayer = null;
        }
        if (contentPlayer != null) {
            contentPlayer.destroy();
            contentPlayer = null;
        }
        contactLoader.destroy();
        currentPlayers.clear();
    }

    private void bindPost(Post post) {
        this.isMyOwnPost = post.senderUserId.isMe();
        if (post.senderUserId.isMe()) {
            shareButton.setVisibility(View.VISIBLE);
        } else {
            shareButton.setVisibility(View.GONE);
        }
        if (post.media.size() > 1) {
            Media content = post.media.get(1);
            if (content.type == Media.MEDIA_TYPE_VIDEO) {
                bindContentVideo(content);
            } else {
                bindContentPhoto(content);
            }
        }
        Media selfie = post.media.get(0);
        bindSelfie(selfie);
        kAvatarLoader.load(avatarView, post.senderUserId);
        if (post.senderUserId.isMe()) {
            nameView.setText(Me.getInstance().getUsername());
        } else {
            contactLoader.load(nameView, post.senderUserId, new ViewDataLoader.Displayer<TextView, Contact>() {
                @Override
                public void showResult(@NonNull TextView view, @Nullable Contact result) {
                    if (result != null) {
                        String shortName = result.username == null ? "" : result.username.toLowerCase(Locale.getDefault());
                        nameView.setText(shortName);
                    }
                }

                @Override
                public void showLoading(@NonNull TextView view) {
                    view.setText("");
                }
            });
        }
    }

    private void moveSelfieToCorner() {
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) selfieContainer.getLayoutParams();
        layoutParams.bottomMargin = layoutParams.topMargin = selfieVerticalMargin;
        layoutParams.leftMargin = layoutParams.rightMargin = selfieHorizontalMargin;
        layoutParams.height = ConstraintLayout.LayoutParams.WRAP_CONTENT;

        layoutParams.topToTop = layoutParams.endToEnd = contentContainer.getId();
        layoutParams.bottomToBottom = layoutParams.startToStart = ConstraintLayout.LayoutParams.UNSET;
        layoutParams.matchConstraintPercentWidth = 0.4f;
        layoutParams.constrainedWidth = true;

        selfieTranslationY = 0;
        selfieTranslationX = 0;
        selfieContainer.setTranslationY(0);
        selfieContainer.setTranslationX(0);
        selfieContainer.setLayoutParams(layoutParams);
    }

    private void bindSelfie(Media selfie) {
        if (selfiePlayer != null) {
            selfiePlayer.destroy();
        }
        selfiePlayer = KatchupExoPlayer.forSelfieView(selfieView, selfie);
    }

    private void bindContentVideo(Media content) {
        if (contentPlayer != null) {
            contentPlayer.destroy();
        }
        postPhotoView.setVisibility(View.GONE);
        postVideoView.setVisibility(View.VISIBLE);
        contentPlayer = KatchupExoPlayer.fromPlayerView(postVideoView);
        contentPlayer.getPlayer().addListener(new Player.EventListener() {
            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                postVideoView.setKeepScreenOn(isPlaying);
            }
        });
        postVideoView.setPlayer(contentPlayer.getPlayer());

        final DataSource.Factory dataSourceFactory;
        final MediaItem exoMediaItem;
        dataSourceFactory = ExoUtils.getDefaultDataSourceFactory(postVideoView.getContext());
        exoMediaItem = ExoUtils.getUriMediaItem(Uri.fromFile(content.file));

        postVideoView.setControllerAutoShow(true);
        final MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(exoMediaItem);

        postVideoView.setUseController(false);

        SimpleExoPlayer player = contentPlayer.getPlayer();
        player.setRepeatMode(Player.REPEAT_MODE_ALL);
        player.setMediaSource(mediaSource);
        player.setPlayWhenReady(true);
        player.prepare();
    }

    private void bindContentPhoto(Media content) {
        if (contentPlayer != null) {
            contentPlayer.destroy();
            contentPlayer = null;
        }
        postVideoView.setVisibility(View.GONE);
        postPhotoView.setVisibility(View.VISIBLE);
        mediaThumbnailLoader.load(postPhotoView, content);
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

    private class CommentViewHolder extends ViewHolderWithLifecycle {

        private Comment comment;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);

            itemView.setOnLongClickListener(v -> {
                viewModel.selectComment(comment);
                return false;
            });
        }

        @Override
        public void markAttach() {
            super.markAttach();

            viewModel.getSelectedComment().observe(this, selectedComment -> {
                if (comment != null && comment.equals(selectedComment)) {
                    ((FrameLayout) itemView).setForeground(new ColorDrawable(0x4dfed3d3));
                } else {
                    ((FrameLayout) itemView).setForeground(null);
                }
            });
        }

        @Override
        public void markDetach() {
            super.markDetach();
        }

        public void bind(Comment comment) {
            this.comment = comment;

        }
    }

    private class VideoReactionViewHolder extends CommentViewHolder {

        private KatchupExoPlayer player;
        private ContentPlayerView contentPlayerView;
        private View videoContainerView;

        private Media media;
        private TextView durationView;

        private View durationContainer;
        private View muteIcon;

        public VideoReactionViewHolder(@NonNull View itemView) {
            super(itemView);

            contentPlayerView = itemView.findViewById(R.id.video_player);
            durationView = itemView.findViewById(R.id.video_duration);
            videoContainerView = itemView.findViewById(R.id.video_container);
            durationContainer = itemView.findViewById(R.id.duration_container);
            muteIcon = itemView.findViewById(R.id.mute_icon);

            videoContainerView.setOnClickListener(v -> {
                if (media != null && !media.equals(viewModel.getPlayingVideoReaction().getValue())) {
                    viewModel.setPlayingVideoReaction(media);
                } else {
                    viewModel.setPlayingVideoReaction(null);
                }
            });
        }

        private void togglePlaying(boolean playing) {
            TransitionManager.beginDelayedTransition((ViewGroup) itemView);
            ConstraintLayout.LayoutParams containerLp = (ConstraintLayout.LayoutParams) videoContainerView.getLayoutParams();
            ConstraintLayout.LayoutParams durationlp = (ConstraintLayout.LayoutParams) durationContainer.getLayoutParams();
            if (playing) {
                containerLp.width = videoContainerView.getContext().getResources().getDimensionPixelSize(R.dimen.reaction_expand_width);

                containerLp.topToTop = ConstraintLayout.LayoutParams.UNSET;
                containerLp.topToBottom = durationView.getId();

                durationlp.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
                muteIcon.setVisibility(View.GONE);
            } else {
                ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) videoContainerView.getLayoutParams();
                lp.width = videoContainerView.getContext().getResources().getDimensionPixelSize(R.dimen.reaction_width);

                durationlp.topToTop = videoContainerView.getId();

                containerLp.topToBottom = ConstraintLayout.LayoutParams.UNSET;
                containerLp.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
                muteIcon.setVisibility(View.VISIBLE);
            }

            durationContainer.setLayoutParams(durationlp);
            videoContainerView.setLayoutParams(containerLp);
        }

        @Override
        public void markAttach() {
            super.markAttach();
            if (player != null) {
                player.destroy();
                player = null;
            }
            player = KatchupExoPlayer.forVideoReaction(contentPlayerView, media);
            player.getPlayer().addListener(new Player.EventListener() {
                @Override
                public void onTimelineChanged(Timeline timeline, int reason) {
                    durationView.setText(TimeFormatter.formatCallDuration(player.getPlayer().getDuration()));
                }
            });
            player.observeLifecycle(ViewKatchupCommentsActivity.this);
            currentPlayers.add(player);
            durationView.setText(TimeFormatter.formatCallDuration(player.getPlayer().getDuration() * 1000));

            viewModel.getPlayingVideoReaction().observe(this, playingReaction -> {
                if (media != null && media.equals(playingReaction)) {
                    player.getPlayer().setVolume(1f);
                    togglePlaying(true);
                } else {
                    player.getPlayer().setVolume(0f);
                    togglePlaying(false);
                    if (playingReaction == null) {
                        player.play();
                    } else {
                        player.pause();
                    }
                }
            });
        }

        @Override
        public void markDetach() {
            super.markDetach();
            if (player != null) {
                player.destroy();
                currentPlayers.remove(player);
                player = null;
                if (media != null && media.equals(viewModel.getPlayingVideoReaction().getValue())) {
                    viewModel.setPlayingVideoReaction(null);
                }
            }
        }

        public void bind(Comment comment) {
            super.bind(comment);
            this.media = comment.media.get(0);
        }
    }

    private class TextCommentViewHolder extends CommentViewHolder {

        private View textContainer;
        private TextView emojiOnlyView;
        private TextView textView;
        private ImageView avatarView;

        public TextCommentViewHolder(@NonNull View itemView) {
            super(itemView);

            textView = itemView.findViewById(R.id.text);
            avatarView = itemView.findViewById(R.id.avatar);
            textContainer = itemView.findViewById(R.id.text_container);
            emojiOnlyView = itemView.findViewById(R.id.emoji_only_text);
        }

        @Override
        public void bind(Comment comment) {
            super.bind(comment);
            int emojiOnlyCount = StringUtils.getOnlyEmojiCount(comment.text);
            if (emojiOnlyCount >= 1 && emojiOnlyCount <= 4) {
                textContainer.setVisibility(View.GONE);
                emojiOnlyView.setVisibility(View.VISIBLE);
                if (emojiOnlyCount == 4) {
                    emojiOnlyView.setTextSize(0.5f * 77);
                } else if (emojiOnlyCount == 3) {
                    emojiOnlyView.setTextSize(0.65f * 77);
                } else if (emojiOnlyCount == 2) {
                    emojiOnlyView.setTextSize(0.75f * 77);
                } else {
                    emojiOnlyView.setTextSize(77);
                }
                emojiOnlyView.setText(comment.text);
            } else {
                textContainer.setVisibility(View.VISIBLE);
                emojiOnlyView.setVisibility(View.GONE);
                textView.setText(comment.text);
                textView.setTextColor(ContextCompat.getColor(textView.getContext(), Colors.getCommentColor(comment.senderContact.getColorIndex())));
            }
            kAvatarLoader.load(avatarView, comment.senderUserId);
            avatarView.setOnClickListener(v -> startActivity(ViewKatchupProfileActivity.viewProfile(avatarView.getContext(), comment.senderUserId)));
        }

    }

    private class TextStickerCommentViewHolder extends CommentViewHolder {

        private TextView textView;
        private ImageView avatarView;

        private String commentText;

        public TextStickerCommentViewHolder(@NonNull View itemView) {
            super(itemView);

            textView = itemView.findViewById(R.id.text);
            avatarView = itemView.findViewById(R.id.avatar);
        }

        @Override
        public void bind(Comment comment) {
            super.bind(comment);
            kAvatarLoader.load(avatarView, comment.senderUserId);
            avatarView.setOnClickListener(v -> startActivity(ViewKatchupProfileActivity.viewProfile(avatarView.getContext(), comment.senderUserId)));
            if (Objects.equals(commentText, comment.text)) {
                return;
            }
            @ColorInt int color;
            if (comment.text != null && comment.text.length() > 7) {
                try {
                    color = Color.parseColor(comment.text.substring(0, 7));
                } catch (Exception e) {
                    color = Colors.getDefaultStickerColor();
                }
                textView.setTextColor(color);
                textView.setText(comment.text.substring(7));
            } else {
                textView.setText(comment.text);
            }
            commentText = comment.text;
        }

    }

    private static final int ITEM_TYPE_TEXT = 1;
    private static final int ITEM_TYPE_TEXT_STICKER = 2;
    private static final int ITEM_TYPE_VIDEO_REACTION = 3;

    private static final int FLAG_RIGHT_SIDE = 1 << 31;

    private class CommentsAdapter extends AdapterWithLifecycle<ViewHolderWithLifecycle> {

        final AsyncPagedListDiffer<Comment> differ;
        private long firstUnseenCommentId;

        private int unseenCommentCount;

        CommentsAdapter() {
            setHasStableIds(false);

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
            int viewType = ITEM_TYPE_TEXT;
            switch (comment.type) {
                case Comment.TYPE_USER:
                    viewType = ITEM_TYPE_TEXT;
                    break;
                case Comment.TYPE_VIDEO_REACTION:
                    viewType = ITEM_TYPE_VIDEO_REACTION;
                    break;
                case Comment.TYPE_STICKER:
                    viewType = ITEM_TYPE_TEXT_STICKER;
                    break;
            }
            viewType = viewType | (((position % 2) == 1) ? FLAG_RIGHT_SIDE : 0);

            return viewType;
        }

        @Override
        public @NonNull
        ViewHolderWithLifecycle onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            PressInterceptView root = new PressInterceptView(parent.getContext());
            root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            boolean rightSide = (viewType & FLAG_RIGHT_SIDE) == FLAG_RIGHT_SIDE;
            switch (viewType & ~FLAG_RIGHT_SIDE) {
                case ITEM_TYPE_TEXT:
                    return new TextCommentViewHolder(layoutInflater.inflate(rightSide ? R.layout.katchup_comment_item_text_right : R.layout.katchup_comment_item_text_left, root, true));
                case ITEM_TYPE_TEXT_STICKER:
                    return new TextStickerCommentViewHolder(layoutInflater.inflate(rightSide ? R.layout.katchup_comment_item_sticker_right : R.layout.katchup_comment_item_sticker_left, root, true));
                case ITEM_TYPE_VIDEO_REACTION:
                    return new VideoReactionViewHolder(layoutInflater.inflate(rightSide ? R.layout.katchup_comment_item_video_reaction_right : R.layout.katchup_comment_item_video_reaction_left, root, true));
            }
            return new CommentViewHolder(null);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolderWithLifecycle holder, int position) {
            if (holder instanceof CommentViewHolder) {
                ((CommentViewHolder) holder).bind(getItem(position));
            }

        }
    }
}
