package com.halloapp.katchup;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.halloapp.Constants;
import com.halloapp.R;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactLoader;
import com.halloapp.content.Comment;
import com.halloapp.content.KatchupPost;
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
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.ViewHolderWithLifecycle;
import com.halloapp.ui.camera.HalloCamera;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.KeyboardUtils;
import com.halloapp.util.Preconditions;
import com.halloapp.util.StringUtils;
import com.halloapp.util.ViewDataLoader;
import com.halloapp.widget.ContentPlayerView;
import com.halloapp.widget.PressInterceptView;

import java.io.File;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;

import pub.devrel.easypermissions.EasyPermissions;

public class ViewKatchupCommentsActivity extends HalloActivity {

    public static Intent viewPost(@NonNull Context context, @NonNull Post post) {
        Intent i = new Intent(context, ViewKatchupCommentsActivity.class);
        i.putExtra(EXTRA_POST_ID, post.id);

        return i;
    }

    private static final int REQUEST_CODE_ASK_CAMERA_AND_AUDIO_PERMISSION = 1;
    private static final String EXTRA_POST_ID = "post_id";

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

    private EditText textEntry;

    private String postId;

    private boolean scrollToBottom = false;

    private LinearLayoutManager commentLayoutManager;

    private HalloCamera camera;

    private PreviewView videoPreviewView;
    private View videoPreviewContainer;

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

        final Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);
        mediaThumbnailLoader = new MediaThumbnailLoader(this, Math.min(Constants.MAX_IMAGE_DIMENSION, Math.max(point.x, point.y)));

        viewModel = new ViewModelProvider(this, new CommentsViewModel.CommentsViewModelFactory(getIntent().getStringExtra(EXTRA_POST_ID))).get(CommentsViewModel.class);
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
                        textStickerPreview.setText(trimmed.toString());
                    }
                    updateStickerSendPreview();
                }
            }
        });

        sendButtonContainer.setOnClickListener(v -> {
            viewModel.sendComment(textEntry.getText().toString());
            onSendComment();
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

        textStickerPreview.setOnClickListener(v -> {
            viewModel.sendTextSticker(textStickerPreview.getText().toString(), textStickerPreview.getCurrentTextColor());
            onSendComment(true);
        });

        randomizeTextStickerColor();
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
        }
    }

    private void startRecordingReaction() {
        camera.startRecordingVideo();
        videoDurationChronometer.setBase(SystemClock.elapsedRealtime());
        videoDurationChronometer.start();
        videoRecordIndicator.setVisibility(View.VISIBLE);
        videoDurationChronometer.setVisibility(View.VISIBLE);
    }

    private void initializeCamera() {
        camera = new HalloCamera(this, videoPreviewView, true, false, Surface.ROTATION_0, new HalloCamera.DefaultListener() {
            @Override
            public void onCaptureSuccess(File file, int type) {
                viewModel.onVideoReaction(file, canceled);
                onSendComment();
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
        if (post instanceof KatchupPost) {
            KatchupPost kp = (KatchupPost) post;
            updateSelfiePosition(kp.selfieX, kp.selfieY);
        } else {
            updateSelfiePosition(0, 1f);
        }
        kAvatarLoader.load(avatarView, post.senderUserId);
        contactLoader.load(nameView, post.senderUserId, new ViewDataLoader.Displayer<TextView, Contact>() {
            @Override
            public void showResult(@NonNull TextView view, @Nullable Contact result) {
                if (result != null) {
                    String shortName = result.getShortName(false).toLowerCase(Locale.getDefault());
                    nameView.setText(shortName);
                }
            }

            @Override
            public void showLoading(@NonNull TextView view) {
                view.setText("");
            }
        });
    }

    private void updateSelfiePosition(float x, float y) {
        contentContainer.post(() -> {
            int w = contentContainer.getWidth() - selfieContainer.getWidth() - (2 * selfieMargin);
            int h = contentContainer.getHeight() - selfieContainer.getHeight() - (2 * selfieMargin);

            selfieContainer.setTranslationX(w * x + selfieMargin);
            selfieContainer.setTranslationY(h * y + selfieMargin);
        });
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

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        public void bind(Comment comment) {}
    }

    private class VideoReactionViewHolder extends CommentViewHolder {

        private KatchupExoPlayer player;
        private ContentPlayerView contentPlayerView;

        private Media media;

        public VideoReactionViewHolder(@NonNull View itemView) {
            super(itemView);

            contentPlayerView = itemView.findViewById(R.id.video_player);
        }

        @Override
        public void markAttach() {
            super.markAttach();
            if (player != null) {
                player.destroy();
                player = null;
            }
            player = KatchupExoPlayer.forVideoReaction(contentPlayerView, media);
            player.observeLifecycle(ViewKatchupCommentsActivity.this);
            currentPlayers.add(player);
        }

        @Override
        public void markDetach() {
            super.markDetach();
            if (player != null) {
                player.destroy();
                currentPlayers.remove(player);
                player = null;
            }
        }

        public void bind(Comment comment) {
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
            kAvatarLoader.load(avatarView, comment.senderUserId);
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
                    return new TextCommentViewHolder(layoutInflater.inflate(rightSide ? R.layout.katchup_comment_item_text_right : R.layout.katchup_comment_item_text_left, parent, false));
                case ITEM_TYPE_TEXT_STICKER:
                    return new TextStickerCommentViewHolder(layoutInflater.inflate(rightSide ? R.layout.katchup_comment_item_sticker_right : R.layout.katchup_comment_item_sticker_left, parent, false));
                case ITEM_TYPE_VIDEO_REACTION:
                    return new VideoReactionViewHolder(layoutInflater.inflate(rightSide ? R.layout.katchup_comment_item_video_reaction_right : R.layout.katchup_comment_item_video_reaction_left, parent, false));
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
