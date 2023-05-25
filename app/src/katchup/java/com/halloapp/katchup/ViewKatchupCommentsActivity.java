package com.halloapp.katchup;

import android.Manifest;
import android.app.Activity;
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
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ActionMode;
import androidx.camera.view.PreviewView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.emoji2.text.EmojiSpan;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.AsyncPagedListDiffer;
import androidx.paging.PagedList;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.AdapterListUpdateCallback;
import androidx.recyclerview.widget.AsyncDifferConfig;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListUpdateCallback;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.TransitionManager;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.tabs.TabLayout;
import com.halloapp.Constants;
import com.halloapp.Me;
import com.halloapp.Preferences;
import com.halloapp.R;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactLoader;
import com.halloapp.content.Comment;
import com.halloapp.content.ContentDb;
import com.halloapp.content.KatchupPost;
import com.halloapp.content.Media;
import com.halloapp.content.Post;
import com.halloapp.content.Reaction;
import com.halloapp.emoji.EmojiKeyboardLayout;
import com.halloapp.id.UserId;
import com.halloapp.katchup.avatar.KAvatarLoader;
import com.halloapp.katchup.media.ExternalSelfieLoader;
import com.halloapp.katchup.media.KatchupExoPlayer;
import com.halloapp.katchup.ui.Colors;
import com.halloapp.katchup.ui.CountingLikeButton;
import com.halloapp.katchup.ui.KatchupShareExternallyView;
import com.halloapp.katchup.ui.ReactionTooltipPopupWindow;
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
import com.halloapp.util.ScreenshotDetector;
import com.halloapp.util.StringUtils;
import com.halloapp.util.TimeFormatter;
import com.halloapp.util.ViewDataLoader;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.ContentPlayerView;
import com.halloapp.widget.PressInterceptView;
import com.halloapp.widget.ShareExternallyView;
import com.halloapp.widget.SnackbarHelper;
import com.halloapp.xmpp.Connection;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import pub.devrel.easypermissions.EasyPermissions;

public class ViewKatchupCommentsActivity extends HalloActivity {

    public static Intent viewPost(@NonNull Context context, @NonNull Post post) {
        return viewPost(context, post.id);
    }

    public static Intent viewPost(@NonNull Context context, @NonNull String postId) {
        return viewPost(context, postId, false, false);
    }

    public static Intent viewPost(@NonNull Context context, @NonNull String postId, boolean disableComments, boolean fromStack) {
        return viewPost(context, postId, null, disableComments, fromStack, false, false);
    }

    public static Intent viewPost(@NonNull Context context, @NonNull String postId, @Nullable ArrayList<String> postIdList, boolean disableComments, boolean fromStack, boolean fromArchive, boolean entryFocused) {
        return viewPost(context, postId, postIdList, disableComments, fromStack, fromArchive, entryFocused, false);
    }

    public static Intent viewPost(@NonNull Context context, @NonNull String postId, @Nullable ArrayList<String> postIdList, boolean disableComments, boolean fromStack, boolean fromArchive, boolean entryFocused, boolean commentsExpanded) {
        Intent i = new Intent(context, ViewKatchupCommentsActivity.class);
        i.putExtra(EXTRA_POST_ID, postId);
        i.putStringArrayListExtra(EXTRA_POST_ID_LIST, postIdList);
        i.putExtra(EXTRA_DISABLE_COMMENTS, disableComments);
        i.putExtra(EXTRA_FROM_STACK, fromStack);
        i.putExtra(EXTRA_FROM_ARCHIVE, fromArchive);
        i.putExtra(EXTRA_ENTRY_FOCUSED, entryFocused);
        i.putExtra(EXTRA_COMMENTS_EXPANDED, commentsExpanded);

        return i;
    }

    private static final int REQUEST_CODE_ASK_CAMERA_AND_AUDIO_PERMISSION = 1;
    private static final int REQUEST_CODE_REPORT = 2;

    private static final String EXTRA_POST_ID = "post_id";
    private static final String EXTRA_POST_ID_LIST = "post_id_list";
    private static final String EXTRA_DISABLE_COMMENTS = "disable_comments";
    private static final String EXTRA_FROM_STACK = "from_stack";
    private static final String EXTRA_FROM_ARCHIVE = "from_archive";
    private static final String EXTRA_COMMENTS_EXPANDED = "comments_expanded";

    private static final float CONTENT_ASPECT_RATIO = 0.75f;
    private static final int MAX_RECORD_TIME_SECONDS = 15;
    private static final int MAX_STICKER_LENGTH = 20;

    private static final String ON_TIME_SUFFIX = " \uD83E\uDD0D";
    private static final String EXTRA_ENTRY_FOCUSED = "entry_focused";

    private static final int TAB_COMMENTS_POSITION = 0;
    private static final int TAB_REACTIONS_POSITION = 1;

    private final KAvatarLoader kAvatarLoader = KAvatarLoader.getInstance();

    private MediaThumbnailLoader localMediaThumbnailLoader;
    private MediaThumbnailLoader externalMediaThumbnailLoader;
    private final ExternalSelfieLoader externalSelfieLoader = new ExternalSelfieLoader();
    private final GeotagLoader geotagLoader = new GeotagLoader();

    private CommentsViewModel viewModel;

    private ImageView avatarView;
    private TextView headerUsername;
    private TextView headerGeotag;
    private TextView headerTimeAndPlace;
    private TextView headerFollowButton;
    private CountingLikeButton likeButton;

    private ContactLoader contactLoader;

    private View coordinator;
    private ViewPager2 contentContainer;
    private final PostsAdapter postsAdapter = new PostsAdapter();
    private View scalableContainer;
    private View recordProtection;
    private EmojiKeyboardLayout emojiKeyboardLayout;

    private int screenWidth;

    private boolean protectionFromBottomsheet;
    private boolean protectionFromKeyboard;
    private boolean protectionFromRecording;

    private float bottomsheetSlide;

    private View bottomSheetView;
    private BottomSheetBehavior bottomSheetBehavior;

    private int selfieVerticalMargin;
    private int selfieHorizontalMargin;

    private View sendButtonContainer;
    private View recordVideoReaction;
    private View videoPreviewBlock;
    private View shareButton;
    private View moreButton;

    private EditText textEntry;

    private boolean isMyOwnPost;

    private boolean scrollToBottom = false;

    private LinearLayoutManager commentLayoutManager;
    private LinearLayoutManager reactionLayoutManager;

    private HalloCamera camera;

    private PreviewView videoPreviewView;
    private View videoPreviewContainer;
    private VideoReactionProgressView videoProgressContainer;

    private View entryContainer;

    private View totalEntry;

    private VideoReactionRecordControlView videoReactionRecordControlView;

    private boolean canceled = false;

    private final HashSet<KatchupExoPlayer> postsPlayers = new HashSet<>();
    private final HashSet<KatchupExoPlayer> commentsPlayers = new HashSet<>();

    private Chronometer videoDurationChronometer;
    private View videoRecordIndicator;

    private View stickerSendContainer;
    private TextStickerView textStickerPreview;
    private RecyclerView emojiStickerRv;

    private boolean keyboardOpened;
    private boolean canTextBeSticker = true;

    private TabLayout interactionTabLayout;
    private RecyclerView commentsRv;
    private RecyclerView reactionsRv;

    private ShareBannerPopupWindow shareBannerPopupWindow;
    private ReactionTooltipPopupWindow reactionTooltipPopupWindow;
    private GeotagPopupWindow geotagPopupWindow;

    private ScreenshotDetector screenshotDetector;
    private HandlerThread screenshotHandlerThread;
    private boolean shouldNotifyScreenshot = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_view_comments);

        interactionTabLayout = findViewById(R.id.interaction_tabs);
        commentsRv = findViewById(R.id.comments_rv);
        reactionsRv = findViewById(R.id.reactions_rv);

        interactionTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                final boolean commentsSelected = tab.getPosition() == TAB_COMMENTS_POSITION;
                commentsRv.setVisibility(commentsSelected ? View.VISIBLE : View.GONE);
                reactionsRv.setVisibility(commentsSelected ? View.GONE : View.VISIBLE);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        final CommentsAdapter commentsAdapter = new CommentsAdapter();
        commentLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        commentsRv.setLayoutManager(commentLayoutManager);
        commentsRv.setAdapter(commentsAdapter);

        final ReactionsAdapter reactionsAdapter = new ReactionsAdapter();
        reactionLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        reactionsRv.setLayoutManager(reactionLayoutManager);
        reactionsRv.setAdapter(reactionsAdapter);

        selfieVerticalMargin = getResources().getDimensionPixelSize(R.dimen.compose_selfie_vertical_margin);
        selfieHorizontalMargin = getResources().getDimensionPixelSize(R.dimen.compose_selfie_horizontal_margin);

        contactLoader = new ContactLoader(userId -> null);

        screenshotHandlerThread = new HandlerThread("ScreenshotHandlerThread");
        screenshotHandlerThread.start();
        screenshotDetector = new ScreenshotDetector(this, new Handler(screenshotHandlerThread.getLooper()));
        screenshotDetector.setListener(this::onScreenshot);
        screenshotDetector.start();

        totalEntry = findViewById(R.id.entry);
        textStickerPreview = findViewById(R.id.sticker_preview);
        emojiStickerRv = findViewById(R.id.emoji_preview);
        stickerSendContainer = findViewById(R.id.sticker_send_container);
        videoDurationChronometer = findViewById(R.id.recording_time);
        videoRecordIndicator = findViewById(R.id.recording_indicator);
        entryContainer = findViewById(R.id.entry_container);
        videoPreviewBlock = findViewById(R.id.preview_block);
        videoReactionRecordControlView = findViewById(R.id.reaction_control_view);
        videoProgressContainer = findViewById(R.id.video_reaction_progress);
        videoPreviewContainer = findViewById(R.id.video_preview_container);
        videoPreviewView = findViewById(R.id.video_preview);
        recordVideoReaction = findViewById(R.id.video_reaction_record_button);
        sendButtonContainer = findViewById(R.id.send_comment_button);
        avatarView = findViewById(R.id.avatar);
        headerUsername = findViewById(R.id.header_username);
        headerGeotag = findViewById(R.id.header_geotag);
        headerTimeAndPlace = findViewById(R.id.header_time_and_place);
        headerFollowButton = findViewById(R.id.follow_button);
        likeButton = findViewById(R.id.likes);
        recordProtection = findViewById(R.id.record_protection);
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
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
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

        coordinator = findViewById(R.id.coordinator_view);
        coordinator.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                final int coordinatorWidth = coordinator.getWidth();
                final int coordinatorHeight = coordinator.getHeight();

                if (coordinatorWidth > 0 && coordinatorHeight > 0) {
                    coordinator.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                    final int bottomSheetMinPeekHeight = getResources().getDimensionPixelSize(R.dimen.view_katchup_bottom_sheet_min_peek_height);
                    final int preferredConstraintHeight = (int) (coordinatorWidth / CONTENT_ASPECT_RATIO);
                    final int bottomSheetPeekHeight = Math.max(bottomSheetMinPeekHeight, coordinatorHeight - preferredConstraintHeight);

                    bottomSheetBehavior.setPeekHeight(bottomSheetPeekHeight);
                    rescaleContent(bottomSheetView.getTop());
                }
            }
        });

        final DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels;

        scalableContainer = findViewById(R.id.scalable_container);
        final CoordinatorLayout.LayoutParams params = new CoordinatorLayout.LayoutParams(scalableContainer.getLayoutParams());
        params.height = (int) (screenWidth / CONTENT_ASPECT_RATIO);
        scalableContainer.setLayoutParams(params);

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
        contentContainer.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                Post post = Objects.requireNonNull(postsAdapter.getCurrentList()).get(position);
                if (post != null) {
                    viewModel.setPost(post);
                }
            }
        });
        contentContainer.setAdapter(postsAdapter);

        boolean fromStack = getIntent().getBooleanExtra(EXTRA_FROM_STACK, false);
        boolean fromArchive = getIntent().getBooleanExtra(EXTRA_FROM_ARCHIVE, false);

        if (getIntent().getBooleanExtra(EXTRA_ENTRY_FOCUSED, false)) {
            KeyboardUtils.showSoftKeyboard(textEntry);
        }

        final Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);
        localMediaThumbnailLoader = new MediaThumbnailLoader(this, Math.min(Constants.MAX_IMAGE_DIMENSION, Math.max(point.x, point.y)));
        externalMediaThumbnailLoader = new ExternalMediaThumbnailLoader(this, Math.min(Constants.MAX_IMAGE_DIMENSION, Math.max(point.x, point.y)));

        String originalPostId = getIntent().getStringExtra(EXTRA_POST_ID);
        ArrayList<String> postIdList = getIntent().getStringArrayListExtra(EXTRA_POST_ID_LIST);
        viewModel = new ViewModelProvider(this, new CommentsViewModel.CommentsViewModelFactory(getApplication(), originalPostId, postIdList, fromStack, fromArchive)).get(CommentsViewModel.class);
        viewModel.getPost().observe(this, post -> bindPost(post));
        viewModel.getPostIsSelfReacted().observe(this, liked -> likeButton.setIsLiked(liked));

        bottomSheetView = findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetView);
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
                rescaleContent(bottomSheet.getTop());
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                bottomsheetSlide = slideOffset;
                updateContentProtection();
                rescaleContent(bottomSheet.getTop());
            }
        });

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
                    if (s.getSpans(0, s.length(), EmojiSpan.class).length > 0 || trimmed.length() > MAX_STICKER_LENGTH) {
                        setCanTextBeSticker(false);
                    } else {
                        setCanTextBeSticker(true);
                        String stickerText = StringUtils.formatMaxLineLengths(trimmed.toString().toUpperCase(Locale.getDefault()), Constants.STICKER_MAX_CHARS_PER_LINE, Constants.STICKER_MAX_LINES);
                        textStickerPreview.setText(stickerText);
                    }
                    updateStickerSendPreview();
                }
            }
        });

        shareButton.setOnClickListener(v -> {
            Post post = viewModel.getPost().getValue();
            if (post == null) {
                return;
            }

            if (shareBannerPopupWindow != null) {
                shareBannerPopupWindow.dismiss();
            }

            shareBannerPopupWindow = new ShareBannerPopupWindow(this, post, intent -> {
                if (intent != null) {
                    startActivity(intent);
                } else {
                    SnackbarHelper.showWarning(shareButton, R.string.external_share_failed);
                }
            });

            shareBannerPopupWindow.show(contentContainer);
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
                    Intent intent = ReportActivity.open(this, post.senderUserId, post.id);
                    startActivityForResult(intent, REQUEST_CODE_REPORT);
                } else if (item.getItemId() == R.id.delete) {
                    KatchupPost post = (KatchupPost) viewModel.getPost().getValue();
                    ContentDb.getInstance().retractPost(post);
                    Analytics.getInstance().deletedPost(post.contentType, post.notificationId);
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
                viewModel.sendComment(textEntry.getText().toString().trim());
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
            public void onDone() {
                if (!canceled) {
                    canceled = true;
                    onStopRecording();
                }
                hideRecordingUI();
            }

            @Override
            public void onSend() {
                canceled = false;
                onStopRecording();
                hideRecordingUI();
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
                protectionFromRecording = true;
                updateContentProtection();
            }
            videoReactionRecordControlView.onTouch(event);
            return true;
        });

        boolean disableComments = getIntent().getBooleanExtra(EXTRA_DISABLE_COMMENTS, false);

        viewModel.getCommentList().observe(this, list -> {
            final TabLayout.Tab commentsTab = interactionTabLayout.getTabAt(TAB_COMMENTS_POSITION);
            if (commentsTab != null) {
                commentsTab.setText(Integer.toString(list.size()));
            }
            updateTabVisibility(list, viewModel.getReactionList().getValue());
            final PagedList<Reaction> reactions = viewModel.getReactionList().getValue();
            final boolean expandComments = getIntent().getBooleanExtra(EXTRA_COMMENTS_EXPANDED, false);
            if (expandComments && list.size() > 0 && !fromStack && !fromArchive && !disableComments) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
            commentsAdapter.submitList(list, () -> {
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
        viewModel.getReactionList().observe(this, list -> {
            final TabLayout.Tab commentsTab = interactionTabLayout.getTabAt(TAB_REACTIONS_POSITION);
            if (commentsTab != null) {
                commentsTab.setText(Integer.toString(list.size()));
            }
            updateTabVisibility(viewModel.getCommentList().getValue(), list);
            reactionsAdapter.submitList(list, () -> {
                if (!scrollToBottom) {
                    return;
                }
                int lastVisible = reactionLayoutManager.findLastCompletelyVisibleItemPosition();
                if (lastVisible != list.size() - 1) {
                    reactionsRv.scrollToPosition(list.size() - 1);
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
        videoDurationChronometer.setOnChronometerTickListener(chronometer -> {
            long dT = SystemClock.elapsedRealtime() - chronometer.getBase();
            if (dT / 1_000 >= Constants.MAX_REACTION_RECORD_TIME_SECONDS) {
                canceled = false;
                onStopRecording();
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

        viewModel.getPostList().observe(this, list -> {
            postsAdapter.submitList(list, () -> {
                // the data source is expected to load the initial post
                for (int i = 0; i < list.size(); i++) {
                    Post post = list.get(i);

                    if (post != null && post.id.equals(originalPostId)) {
                        contentContainer.setCurrentItem(i, false);
                        viewModel.setPost(post);
                        break;
                    }
                }
            });
        });

        randomizeTextStickerColor();

        final View prev = findViewById(R.id.prev);
        prev.setOnClickListener(v -> finish());
    }

    @Override
    public void onResume() {
        super.onResume();
        Analytics.getInstance().openScreen("comments");
        resumePostsPlayback();
        shouldNotifyScreenshot = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        pausePostsPlayback();
        shouldNotifyScreenshot = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        contactLoader.destroy();
        postsPlayers.clear();
        commentsPlayers.clear();
        screenshotDetector.stop();
        screenshotHandlerThread.quit();
    }

    private void onScreenshot() {
        if (shouldNotifyScreenshot) {
            viewModel.onScreenshotted();
        }
    }

    private void sendSticker() {
        viewModel.sendTextSticker(textStickerPreview.getText().toString(), textStickerPreview.getCurrentTextColor());
        onSendComment(true);
    }

    private void updateTabVisibility(@Nullable List<Comment> comments, @Nullable List<Reaction> reactions) {
        boolean hideTab = (comments == null || comments.isEmpty()) && (reactions == null || reactions.isEmpty());
        hideTab |= getIntent().getBooleanExtra(EXTRA_DISABLE_COMMENTS, false);
        interactionTabLayout.setVisibility(hideTab ? View.GONE : View.VISIBLE);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_REPORT && resultCode == Activity.RESULT_OK) {
            finish();
        }
    }

    private void rescaleContent(int height) {
        final float scale = Math.min(1f, height * CONTENT_ASPECT_RATIO / screenWidth);
        final float offsetY = (1 - scale) * screenWidth / CONTENT_ASPECT_RATIO / 2;
        scalableContainer.setScaleX(scale);
        scalableContainer.setScaleY(scale);
        scalableContainer.setTranslationY(-offsetY);
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

    private void showRecordingUI() {
        videoReactionRecordControlView.setVisibility(View.VISIBLE);
    }

    private void hideRecordingUI() {
        videoReactionRecordControlView.setVisibility(View.GONE);
    }

    private void onStopRecording() {
        if (!camera.isRecordingVideo() && !canceled) {
            if (reactionTooltipPopupWindow != null) {
                reactionTooltipPopupWindow.dismiss();
            }

            reactionTooltipPopupWindow = new ReactionTooltipPopupWindow(this);
            reactionTooltipPopupWindow.show(recordVideoReaction);
        }

        camera.stopRecordingVideo();
        videoPreviewContainer.setVisibility(View.GONE);
        videoProgressContainer.setVisibility(View.GONE);
        camera.unbind();
        entryContainer.setVisibility(View.VISIBLE);
        protectionFromRecording = false;
        updateContentProtection();
        videoRecordIndicator.setVisibility(View.GONE);
        videoDurationChronometer.setVisibility(View.GONE);
        videoDurationChronometer.stop();
        videoProgressContainer.stopProgress();
    }

    private void startRecordingReaction() {
        camera.startRecordingVideo();
        videoDurationChronometer.setBase(SystemClock.elapsedRealtime());
        videoDurationChronometer.start();
        videoRecordIndicator.setVisibility(View.VISIBLE);
        videoDurationChronometer.setVisibility(View.VISIBLE);
        videoProgressContainer.startProgress(Constants.MAX_REACTION_RECORD_TIME_SECONDS);
    }

    private void initializeCamera() {
        camera = new HalloCamera(this, videoPreviewView, true, false, Surface.ROTATION_0, new HalloCamera.DefaultListener() {
            @Override
            public void onCaptureSuccess(File file, int type) {
                runOnUiThread(() -> {
                    viewModel.onVideoReaction(file, canceled).observe(ViewKatchupCommentsActivity.this, sent -> {
                        if (sent) {
                            onSendComment();
                        }
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
            stickerSendContainer.getLocationOnScreen(location);
            if (ev.getX() < location[0] || ev.getX() > location[0] + totalEntry.getWidth() || ev.getY() < location[1] || ev.getY() > location[1] + stickerSendContainer.getHeight() + totalEntry.getHeight()) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    private void updateContentPlayingForProtection() {
        if (protectionFromBottomsheet || protectionFromKeyboard || protectionFromRecording) {
            pausePostsPlayback();
            contentContainer.setUserInputEnabled(false);
        } else {
            resumePostsPlayback();
            contentContainer.setUserInputEnabled(true);
        }
        if (protectionFromRecording) {
            for (KatchupExoPlayer player : commentsPlayers) {
                SimpleExoPlayer p = player.getPlayer();
                p.setPlayWhenReady(false);
            }
        } else {
            for (KatchupExoPlayer player : commentsPlayers) {
                SimpleExoPlayer p = player.getPlayer();
                p.setPlayWhenReady(true);
            }
        }
    }

    private void updateContentProtection() {
        recordProtection.setVisibility(protectionFromRecording ? View.VISIBLE : View.INVISIBLE);
        updateContentPlayingForProtection();
    }

    private void bindPost(Post post) {
        if (post == null) {
            finish();
            return;
        }
        this.isMyOwnPost = post.senderUserId.isMe();
        if (post.senderUserId.isMe()) {
            shareButton.setVisibility(View.VISIBLE);
        } else {
            shareButton.setVisibility(View.GONE);
        }

        boolean disableComments = getIntent().getBooleanExtra(EXTRA_DISABLE_COMMENTS, false);

        if (disableComments || post.isExpired()) {
            commentsRv.setVisibility(View.GONE);
            totalEntry.setVisibility(View.GONE);
            likeButton.setVisibility(View.GONE);
        }

        likeButton.setOnClickListener(v -> viewModel.toggleLike());

        final CharSequence timeText = TimeFormatter.formatMessageTime(headerTimeAndPlace.getContext(), post.timestamp).toLowerCase(Locale.getDefault());
        String location = ((KatchupPost)post).location;
        if (location != null) {
            headerTimeAndPlace.setText(timeText + " · " + location.toLowerCase(Locale.getDefault()));
        } else {
            headerTimeAndPlace.setText(timeText);
        }

        viewModel.getFollowable().observe(this, followable -> {
            headerFollowButton.setVisibility(Boolean.TRUE.equals(followable) ? View.VISIBLE : View.GONE);
        });
        headerFollowButton.setText(" · " + headerFollowButton.getContext().getString(R.string.follow_profile));
        headerFollowButton.setOnClickListener(v -> {
            UserId userIdToFollow = post.senderUserId;
            Connection.getInstance().requestFollowUser(userIdToFollow).onResponse(success -> {
                if (Boolean.TRUE.equals(success)) {
                    if (userIdToFollow.equals(post.senderUserId)) {
                        headerFollowButton.post(() -> headerFollowButton.setVisibility(View.GONE));
                    }
                } else {
                    SnackbarHelper.showWarning(headerFollowButton, R.string.failed_to_follow);
                }
            }).onError(e -> SnackbarHelper.showWarning(headerFollowButton, R.string.failed_to_follow));
        });

        headerGeotag.setOnClickListener(v -> {
            Runnable removeRunnable = () -> {
                Connection.getInstance().removeGeotag(headerGeotag.getText().toString()).onResponse(res -> {
                    if (res.success) {
                        Log.i("ViewKatchupCommentsActivity successfully removed geotag");
                        Preferences.getInstance().setGeotag(null);
                        Analytics.getInstance().updateGeotag();
                        geotagLoader.load(headerGeotag, post.senderUserId);
                    } else {
                        Log.w("ViewKatchupCommentsActivity failed to remove geotag");
                        SnackbarHelper.showWarning(this, R.string.failed_remove_geotag);
                    }
                    headerGeotag.post(geotagPopupWindow::dismiss);
                }).onError(e -> {
                    Log.e("ViewKatchupCommentsActivity failed to remove geotag", e);
                    SnackbarHelper.showWarning(this, R.string.failed_remove_geotag);
                    headerGeotag.post(geotagPopupWindow::dismiss);
                });
            };
            geotagPopupWindow = new GeotagPopupWindow(this, post.senderUserId.isMe(), headerUsername.getText().toString(), headerGeotag.getText().toString(), removeRunnable);
            geotagPopupWindow.show(headerGeotag);
        });

        geotagLoader.load(headerGeotag, post.senderUserId);
        kAvatarLoader.load(avatarView, post.senderUserId);
        avatarView.setOnClickListener(v -> startActivity(ViewKatchupProfileActivity.viewProfile(this, post.senderUserId)));
        headerUsername.setOnClickListener(v -> startActivity(ViewKatchupProfileActivity.viewProfile(this, post.senderUserId)));
        if (post.senderUserId.isMe()) {
            headerUsername.setText(Me.getInstance().getUsername());
        } else {
            if (viewModel.isPublic()) {
                PublicContentCache.getInstance().subscribeToPost(post);
            }
            contactLoader.load(headerUsername, post.senderUserId, new ViewDataLoader.Displayer<TextView, Contact>() {
                @Override
                public void showResult(@NonNull TextView view, @Nullable Contact result) {
                    if (result != null) {
                        view.setText(result.username == null ? "" : result.username.toLowerCase(Locale.getDefault()));
                    }
                }

                @Override
                public void showLoading(@NonNull TextView view) {
                    view.setText("");
                }
            });
        }
    }

    private void resumePostsPlayback() {
        Post post = viewModel.getPost().getValue();
        if (post == null) {
            return;
        }

        if (post.media.size() > 0) {
            ContentPlayerView playerView = contentContainer.findViewWithTag(post.media.get(0));
            if (playerView != null) {
                playerView.play();
            }
        }

        if (post.media.size() > 1) {
            ContentPlayerView playerView = contentContainer.findViewWithTag(post.media.get(1));
            if (playerView != null) {
                playerView.play();
            }
        }
    }

    private void pausePostsPlayback() {
        Post post = viewModel.getPost().getValue();
        if (post == null) {
            return;
        }

        if (post.media.size() > 0) {
            ContentPlayerView playerView = contentContainer.findViewWithTag(post.media.get(0));
            if (playerView != null) {
                playerView.pause();
            }
        }

        if (post.media.size() > 1) {
            ContentPlayerView playerView = contentContainer.findViewWithTag(post.media.get(1));
            if (playerView != null) {
                playerView.pause();
            }
        }
    }

    private class PostViewHolder extends RecyclerView.ViewHolder {

        private final View selfieContainer;
        private final ContentPlayerView selfieView;
        private final LoadingView selfieLoadingView;
        private final ImageView postPhotoView;
        private final ContentPlayerView postVideoView;
        private final LoadingView contentLoadingView;
        private KatchupExoPlayer contentPlayer;
        private KatchupExoPlayer selfiePlayer;

        private float selfieTranslationX;
        private float selfieTranslationY;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);

            selfieContainer = itemView.findViewById(R.id.selfie_container);
            selfieView = itemView.findViewById(R.id.selfie_player);
            selfieLoadingView = itemView.findViewById(R.id.selfie_loading);
            postVideoView = itemView.findViewById(R.id.content_video);
            postPhotoView = itemView.findViewById(R.id.content_photo);
            contentLoadingView = itemView.findViewById(R.id.content_loading);

            makeSelfieDraggable();
        }

        public void bind(@NonNull Post post) {
            clear();
            moveSelfieToCorner();

            if (post.media.size() > 1) {
                Media content = post.media.get(1);
                if (content.type == Media.MEDIA_TYPE_VIDEO) {
                    bindContentVideo(content);
                } else {
                    bindContentPhoto(post, content);
                }
            }

            Media selfie = post.media.get(0);
            if (selfie.file == null) {
                selfieLoadingView.setVisibility(View.VISIBLE);
                externalSelfieLoader.load(selfieView, selfie, new ViewDataLoader.Displayer<ContentPlayerView, Media>() {
                    @Override
                    public void showResult(@NonNull ContentPlayerView view, @Nullable Media result) {
                        if (result != null) {
                            bindSelfie(result);
                        } else {
                            selfieLoadingView.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void showLoading(@NonNull ContentPlayerView view) {
                    }
                });
            } else {
                bindSelfie(selfie);
            }
        }

        public void clear() {
            if (selfiePlayer != null) {
                postsPlayers.remove(selfiePlayer);
                selfiePlayer.destroy();
                selfiePlayer = null;
                selfieView.setPlayer(null);
                selfieView.setTag(null);
            }

            if (contentPlayer != null) {
                postsPlayers.remove(contentPlayer);
                contentPlayer.destroy();
                contentPlayer = null;
                postVideoView.setPlayer(null);
                postPhotoView.setTag(null);
            }
        }

        private void bindSelfie(Media selfie) {
            if (selfiePlayer != null) {
                postsPlayers.remove(selfiePlayer);
                selfiePlayer.destroy();
                selfieView.setPlayer(null);
            }
            selfiePlayer = KatchupExoPlayer.forSelfieView(selfieView, selfie);
            selfiePlayer.getPlayer().addListener(new Player.EventListener() {
                @Override
                public void onPlaybackStateChanged(int state) {
                    if (state == Player.STATE_READY) {
                        selfieLoadingView.setVisibility(View.GONE);
                    }
                }
            });
            postsPlayers.add(selfiePlayer);
            selfieView.setTag(selfie);
        }

        private void bindContentVideo(Media content) {
            if (contentPlayer != null) {
                postsPlayers.remove(contentPlayer);
                contentPlayer.destroy();
                postVideoView.setPlayer(null);
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
            postVideoView.setTag(content);
            postsPlayers.add(contentPlayer);

            final DataSource.Factory dataSourceFactory;
            final MediaItem exoMediaItem;
            dataSourceFactory = ExoUtils.getDefaultDataSourceFactory(postVideoView.getContext());
            exoMediaItem = ExoUtils.getUriMediaItem(Uri.fromFile(content.file));

            postVideoView.setControllerAutoShow(true);
            final MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(exoMediaItem);

            postVideoView.setUseController(false);

            contentLoadingView.setVisibility(View.VISIBLE);
            SimpleExoPlayer player = contentPlayer.getPlayer();
            player.addListener(new Player.EventListener() {
                @Override
                public void onPlaybackStateChanged(int state) {
                    if (state == Player.STATE_READY) {
                        contentLoadingView.setVisibility(View.GONE);
                    }
                }
            });
            player.setRepeatMode(Player.REPEAT_MODE_ALL);
            player.setMediaSource(mediaSource);
            player.setPlayWhenReady(false);
            player.prepare();
        }

        private void bindContentPhoto(Post post, Media content) {
            if (contentPlayer != null) {
                contentPlayer.destroy();
                contentPlayer = null;
            }
            postVideoView.setVisibility(View.GONE);
            postPhotoView.setVisibility(View.VISIBLE);
            contentLoadingView.setVisibility(View.VISIBLE);

            if (viewModel.isPublic(post.id)) {
                externalMediaThumbnailLoader.load(postPhotoView, content, () -> contentLoadingView.setVisibility(View.GONE));
            } else {
                localMediaThumbnailLoader.load(postPhotoView, content, () -> contentLoadingView.setVisibility(View.GONE));
            }
        }

        private void moveSelfieToCorner() {
            selfieTranslationY = 0;
            selfieTranslationX = 0;
            selfieContainer.setTranslationY(0);
            selfieContainer.setTranslationX(0);
        }

        private void makeSelfieDraggable() {
            selfieContainer.setOnTouchListener(new View.OnTouchListener() {

                private float startX;
                private float startY;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        startX = event.getRawX();
                        startY = event.getRawY();
                        contentContainer.setUserInputEnabled(false);
                        return true;
                    } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                        final float containerScale = scalableContainer.getScaleX();
                        selfieContainer.setTranslationX(selfieTranslationX + (event.getRawX() - startX) / containerScale);
                        selfieContainer.setTranslationY(selfieTranslationY + (event.getRawY() - startY) / containerScale);
                        return true;
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        selfieTranslationX = selfieContainer.getTranslationX();
                        selfieTranslationY = selfieContainer.getTranslationY();
                        forceWithinBounds();
                        contentContainer.setUserInputEnabled(true);
                    }
                    return false;
                }

                private void forceWithinBounds() {
                    final float posX = selfieContainer.getX();
                    final float posY = selfieContainer.getY();
                    final int scaledHorizontalMargin = selfieHorizontalMargin;
                    final int scaledVerticalMargin = selfieVerticalMargin;

                    if (posX < scaledHorizontalMargin) {
                        selfieTranslationX = scaledHorizontalMargin - selfieContainer.getLeft();
                    }
                    if (posX + selfieContainer.getWidth() > contentContainer.getWidth() - scaledHorizontalMargin) {
                        selfieTranslationX = contentContainer.getWidth() - scaledHorizontalMargin - selfieContainer.getRight();
                    }

                    if (posY < scaledVerticalMargin) {
                        selfieTranslationY = scaledVerticalMargin - selfieContainer.getTop();
                    }
                    if (posY + selfieContainer.getHeight() > contentContainer.getHeight() - scaledVerticalMargin) {
                        selfieTranslationY = contentContainer.getHeight() - scaledVerticalMargin - selfieContainer.getBottom();
                    }

                    selfieContainer.setTranslationX(selfieTranslationX);
                    selfieContainer.setTranslationY(selfieTranslationY);
                }
            });
        }

        public void pause() {
            if (selfiePlayer != null) {
                selfiePlayer.pause();
            }

            if (contentPlayer != null) {
                contentPlayer.pause();
            }
        }

        public void play() {
            if (selfiePlayer != null) {
                selfiePlayer.play();
            }

            if (contentPlayer != null) {
                contentPlayer.play();
            }
        }
    }

    private class PostsAdapter extends PagedListAdapter<Post, PostViewHolder> {

        PostsAdapter() {
            super(new DiffUtil.ItemCallback<Post>() {
                @Override
                public boolean areItemsTheSame(@NonNull Post oldItem, @NonNull Post newItem) {
                    // The ID property identifies when items are the same.
                    return oldItem.rowId == newItem.rowId;
                }

                @Override
                public boolean areContentsTheSame(@NonNull Post oldItem, @NonNull Post newItem) {
                    return oldItem.equals(newItem);
                }
            });
        }

        @Override
        public long getItemId(int position) {
            return Objects.requireNonNull(getItem(position)).rowId;
        }

        @NonNull
        @Override
        public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new PostViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.katchup_comment_post_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
            Post post = getItem(position);

            if (post != null) {
                holder.bind(post);
            }
        }

        @Override
        public void onViewRecycled(@NonNull PostViewHolder holder) {
            super.onViewRecycled(holder);
            holder.clear();
        }

        @Override
        public void onViewDetachedFromWindow(@NonNull PostViewHolder holder) {
            super.onViewDetachedFromWindow(holder);
            holder.pause();
        }

        @Override
        public void onViewAttachedToWindow(@NonNull PostViewHolder holder) {
            super.onViewAttachedToWindow(holder);
            holder.play();
        }
    }

    private static final DiffUtil.ItemCallback<Comment> DIFF_COMMENT_CALLBACK = new DiffUtil.ItemCallback<Comment>() {

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

    private static final DiffUtil.ItemCallback<Reaction> DIFF_REACTION_CALLBACK = new DiffUtil.ItemCallback<Reaction>() {
        @Override
        public boolean areItemsTheSame(Reaction oldItem, Reaction newItem) {
            return oldItem.reactionId.equals(newItem.reactionId);
        }

        @Override
        public boolean areContentsTheSame(@NonNull Reaction oldItem, @NonNull Reaction newItem) {
            return oldItem.equals(newItem) && oldItem.isFollowingSender == newItem.isFollowingSender && oldItem.isFollowerSender == newItem.isFollowerSender;
        }
    };

    private class ReactionViewHolder extends ViewHolderWithLifecycle {
        private Reaction reaction;

        private final ImageView avatarView;
        private final TextView nameView;
        private final TextView usernameView;
        private final TextView mutuals;
        private final TextView followButton;

        public ReactionViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarView = itemView.findViewById(R.id.avatar);
            nameView = itemView.findViewById(R.id.name);
            usernameView = itemView.findViewById(R.id.username);
            mutuals = itemView.findViewById(R.id.mutuals);
            followButton = itemView.findViewById(R.id.follow_button);

            final View reactionContainer = itemView.findViewById(R.id.reaction_container);
            reactionContainer.setOnClickListener(v -> {
                startActivity(ViewKatchupProfileActivity.viewProfile(ViewKatchupCommentsActivity.this, reaction.senderUserId));
            });
            followButton.setOnClickListener(v -> {
                viewModel.followUser(reaction.senderUserId).observe(ViewKatchupCommentsActivity.this, success -> {
                    if (!Boolean.TRUE.equals(success)) {
                        SnackbarHelper.showWarning(followButton, R.string.failed_to_follow);
                    }
                });
            });
        }

        public void bind(Reaction reaction) {
            // TODO(vasil): No batch request for BasicUserProfile/UserProfile exists, and RelationshipList might not contain data for the relevant users.
            //  Create a specialized loader to efficiently fetch BasicUserProfile to display the mutuals count.
            this.reaction = reaction;
            if (reaction.senderUserId.isMe()) {
                nameView.setText(Me.getInstance().getName());
                usernameView.setText(Me.getInstance().getUsername());
                followButton.setVisibility(View.GONE);
            } else if (reaction.senderContact != null) {
                nameView.setText(reaction.senderContact.halloName);
                usernameView.setText(reaction.senderContact.username);
                if (reaction.isFollowingSender) {
                    followButton.setVisibility(View.GONE);
                } else {
                    followButton.setVisibility(View.VISIBLE);
                    followButton.setText(getString(reaction.isFollowerSender ? R.string.follow_back_profile : R.string.follow_profile));
                }
            }
            kAvatarLoader.load(avatarView, reaction.senderUserId);
        }
    }

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
        private View loadingView;

        private Media media;
        private TextView durationView;

        private View durationContainer;
        private View muteIcon;

        public VideoReactionViewHolder(@NonNull View itemView) {
            super(itemView);

            contentPlayerView = itemView.findViewById(R.id.video_player);
            durationView = itemView.findViewById(R.id.video_duration);
            videoContainerView = itemView.findViewById(R.id.video_container);
            loadingView = itemView.findViewById(R.id.video_loading);
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
            bindReaction(media);

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
                commentsPlayers.remove(player);
                player = null;
                if (media != null && media.equals(viewModel.getPlayingVideoReaction().getValue())) {
                    viewModel.setPlayingVideoReaction(null);
                }
            }
        }

        private void showLoading() {
            if (loadingView != null) {
                loadingView.setVisibility(View.VISIBLE);
            }
        }

        private void hideLoading() {
            if (loadingView != null) {
                loadingView.setVisibility(View.GONE);
            }
        }

        public void bind(Comment comment) {
            super.bind(comment);
            this.media = comment.media.get(0);

            if (media.file == null) {
                showLoading();
                externalSelfieLoader.load(contentPlayerView, media, new ViewDataLoader.Displayer<ContentPlayerView, Media>() {
                    @Override
                    public void showResult(@NonNull ContentPlayerView view, @Nullable Media result) {
                        if (result != null) {
                            bindReaction(result);
                        }
                    }

                    @Override
                    public void showLoading(@NonNull ContentPlayerView view) {
                    }
                });
            } else {
                bindReaction(media);
            }
        }

        private void bindReaction(@NonNull Media media) {
            showLoading();
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
            commentsPlayers.add(player);
            durationView.setText(TimeFormatter.formatCallDuration(player.getPlayer().getDuration() * 1000));
            player.getPlayer().addListener(new Player.EventListener() {
                @Override
                public void onPlaybackStateChanged(int state) {
                    if (state == Player.STATE_READY) {
                        hideLoading();
                    }
                }
            });
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
                String text = comment.text.substring(7);
                text = text.length() > Constants.STICKER_MAX_CHARS_PER_LINE * Constants.STICKER_MAX_LINES
                        ? text
                        : StringUtils.formatMaxLineLengths(text, Constants.STICKER_MAX_CHARS_PER_LINE, Constants.STICKER_MAX_LINES);
                textView.setText(text);
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

            differ = new AsyncPagedListDiffer<>(listUpdateCallback, new AsyncDifferConfig.Builder<>(DIFF_COMMENT_CALLBACK).build());
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

    private class ReactionsAdapter extends RecyclerView.Adapter<ReactionViewHolder> {
        final AsyncPagedListDiffer<Reaction> differ;

        ReactionsAdapter() {
            setHasStableIds(false);

            final AdapterListUpdateCallback adapterCallback = new AdapterListUpdateCallback(this);
            final ListUpdateCallback listUpdateCallback = new ListUpdateCallback() {

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

            differ = new AsyncPagedListDiffer<>(listUpdateCallback, new AsyncDifferConfig.Builder<>(DIFF_REACTION_CALLBACK).build());
        }

        void submitList(@Nullable PagedList<Reaction> pagedList, @Nullable final Runnable commitCallback) {
            differ.submitList(pagedList, commitCallback);
        }

        @NonNull
        @Override
        public ReactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            final PressInterceptView root = new PressInterceptView(parent.getContext());
            final LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            return new ReactionViewHolder(layoutInflater.inflate(R.layout.reaction_comment, root, true));
        }

        @Override
        public void onBindViewHolder(@NonNull ReactionViewHolder holder, int position) {
            Reaction reaction = getItem(position);
            if (reaction != null) {
                holder.bind(reaction);
            } else {
                Log.e("ReactionsAdapter.onBindViewHolder no reaction for position = " + position);
            }
        }

        @Override
        public int getItemCount() {
            return differ.getItemCount();
        }

        @Nullable Reaction getItem(int position) {
            return differ.getItem(position);
        }
    }

    interface ShareBannerPopupWindowCallback {
        void onCompletion(Intent intent);
    }

    class ShareBannerPopupWindow extends PopupWindow {

        private static final int ANIMATION_DURATION_MS = 300;
        private static final int AUTO_HIDE_DELAY_MS = 5000;

        private final View container;

        private final ShareBannerPopupWindowCallback callback;

        public ShareBannerPopupWindow(@NonNull Context context, @NonNull Post post, @NonNull ShareBannerPopupWindowCallback callback) {
            super(context);

            this.callback = callback;

            setWidth(ViewGroup.LayoutParams.MATCH_PARENT);

            View root = LayoutInflater.from(context).inflate(R.layout.share_banner_comments, null, false);
            setContentView(root);

            KatchupShareExternallyView shareExternallyView = root.findViewById(R.id.list);
            shareExternallyView.setupVerticalLayout();
            shareExternallyView.setListener(new ShareExternallyView.ShareListener() {
                @Override
                public void onOpenShare() {
                    share(root, null, post);
                }

                @Override
                public void onShareTo(ShareExternallyView.ShareTarget target) {
                    share(root, target.getPackageName(), post);
                }
            });

            container = root.findViewById(R.id.container);

            setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            setOutsideTouchable(true);
            setFocusable(false);
        }

        public void show(@NonNull View anchor) {
            View contentView = getContentView();
            contentView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

            showAsDropDown(anchor, 0, (-contentView.getMeasuredHeight() - anchor.getHeight()) / 2);

            int animationStartX = contentView.getMeasuredWidth() + contentView.getPaddingEnd();

            showAnimated(animationStartX, () ->
                container.postDelayed(() -> hideAnimated(animationStartX, this::dismiss), AUTO_HIDE_DELAY_MS)
            );
        }

        private void showAnimated(int animationStartX, Runnable completion) {
            container.setTranslationX(animationStartX);

            container.animate()
                    .setDuration(ANIMATION_DURATION_MS)
                    .translationX(0)
                    .start();

            container.postDelayed(completion, ANIMATION_DURATION_MS);
        }

        private void hideAnimated(int animationStartX, Runnable completion) {
            container.animate()
                    .setDuration(ANIMATION_DURATION_MS)
                    .translationX(animationStartX)
                    .start();

            container.postDelayed(completion, ANIMATION_DURATION_MS);
        }

        private void share(@NonNull View view, @Nullable String targetPackage, @NonNull Post post) {
            Context context = view.getContext();
            ProgressDialog progressDialog = ProgressDialog.show(context, null, getString(R.string.share_moment_progress));

            boolean isCenterCrop = Constants.PACKAGE_INSTAGRAM.equals(targetPackage) || Constants.PACKAGE_SNAPCHAT.equals(targetPackage);

            ShareIntentHelper.shareExternallyWithPreview(context, targetPackage, post, true, isCenterCrop).observe(ViewKatchupCommentsActivity.this, intent -> {
                progressDialog.dismiss();
                dismiss();
                callback.onCompletion(intent);
            });
        }
    }

}
