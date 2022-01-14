package com.halloapp.ui;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.SharedElementCallback;
import android.content.Context;
import android.content.Intent;
import android.graphics.Outline;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.transition.Fade;
import android.util.Pair;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.util.Util;
import com.halloapp.Constants;
import com.halloapp.FileStore;
import com.halloapp.R;
import com.halloapp.UrlPreview;
import com.halloapp.UrlPreviewLoader;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Media;
import com.halloapp.content.Mention;
import com.halloapp.content.Post;
import com.halloapp.id.ChatId;
import com.halloapp.id.GroupId;
import com.halloapp.media.AudioDurationLoader;
import com.halloapp.media.ExoUtils;
import com.halloapp.media.MediaThumbnailLoader;
import com.halloapp.media.MediaUtils;
import com.halloapp.privacy.FeedPrivacy;
import com.halloapp.props.ServerProps;
import com.halloapp.ui.chat.ChatActivity;
import com.halloapp.ui.groups.ViewGroupFeedActivity;
import com.halloapp.ui.mediaedit.MediaEditActivity;
import com.halloapp.ui.mediapicker.MediaPickerActivity;
import com.halloapp.ui.mentions.MentionPickerView;
import com.halloapp.ui.mentions.TextContentLoader;
import com.halloapp.util.ActivityUtils;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.Preconditions;
import com.halloapp.util.RandomId;
import com.halloapp.util.Rtl;
import com.halloapp.util.ThreadUtils;
import com.halloapp.util.ViewDataLoader;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.ContentComposerScrollView;
import com.halloapp.widget.ContentPhotoView;
import com.halloapp.widget.ContentPlayerView;
import com.halloapp.widget.DrawDelegateView;
import com.halloapp.widget.MediaViewPager;
import com.halloapp.widget.MentionableEntry;
import com.halloapp.widget.PostEntryView;
import com.halloapp.widget.PostLinkPreviewView;
import com.halloapp.widget.SnackbarHelper;
import com.halloapp.widget.VoicePostRecorderControlView;
import com.halloapp.xmpp.privacy.PrivacyList;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.relex.circleindicator.CircleIndicator;

public class ContentComposerActivity extends HalloActivity {
    public static final String EXTRA_CALLED_FROM_CAMERA = "called_from_camera";
    public static final String EXTRA_ALLOW_ADD_MEDIA = "allow_add_media";
    public static final String EXTRA_CHAT_ID = "chat_id";
    public static final String EXTRA_GROUP_ID = "group_id";
    public static final String EXTRA_REPLY_POST_ID = "reply_post_id";
    public static final String EXTRA_REPLY_POST_MEDIA_INDEX = "reply_post_media_index";
    public static final String EXTRA_VOICE_NOTE_POST = "voice_note_post";

    private static final int REQUEST_CODE_CROP = 1;
    private static final int REQUEST_CODE_MORE_MEDIA = 2;
    private static final int REQUEST_CODE_CHANGE_PRIVACY = 3;

    private static final int EXO_PLAYER_BUFFER_MS = 25000;

    public static Intent newTextPost(@NonNull Context context) {
        Intent i = new Intent(context, ContentComposerActivity.class);
        i.putExtra(EXTRA_ALLOW_ADD_MEDIA, true);
        return i;
    }

    public static Intent newAudioPost(@NonNull Context context) {
        Intent i = new Intent(context, ContentComposerActivity.class);
        i.putExtra(EXTRA_ALLOW_ADD_MEDIA, true);
        i.putExtra(EXTRA_VOICE_NOTE_POST, true);
        return i;
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef ({ComposeMode.TEXT, ComposeMode.AUDIO, ComposeMode.MEDIA})
    public @interface ComposeMode {
        int TEXT = 1;
        int AUDIO = 2;
        int MEDIA = 3;
    }

    private final Map<ContentComposerViewModel.EditMediaPair, SimpleExoPlayer> playerMap = new HashMap<>();

    private ContentComposerViewModel viewModel;
    private MediaThumbnailLoader fullThumbnailLoader;
    private TextContentLoader textContentLoader;
    private ContentComposerScrollView mediaVerticalScrollView;
    private MentionableEntry textPostEntry;
    private MentionableEntry bottomEditText;
    private MentionPickerView mentionPickerView;
    private MediaViewPager mediaPager;
    private CircleIndicator mediaPagerIndicator;
    private MediaPagerAdapter mediaPagerAdapter;
    private DrawDelegateView drawDelegateView;
    private Toolbar toolbar;
    private View replyContainer;

    private View audioComposer;

    private PostEntryView postEntryView;
    private View bottomSendButton;
    private View textEntryCard;
    private View mediaContainer;

    private View textAddMedia;
    private View voiceAddMedia;

    private TextView privacyDestination;

    private PostLinkPreviewView postLinkPreviewView;
    private UrlPreviewLoader urlPreviewLoader;
    private AudioDurationLoader audioDurationLoader;
    private MediaThumbnailLoader mediaThumbnailLoader;

    private boolean allowAddMedia;
    private boolean calledFromCamera;
    private boolean calledFromPicker;

    private ImageButton addMediaButton;
    private ImageButton deletePictureButton;
    private ImageButton cropPictureButton;
    private View addMoreText;

    private VoicePostComposerView voicePostComposerView;
    private VoicePostRecorderControlView voiceNoteRecorderControlView;

    @Nullable
    private ChatId chatId;
    @Nullable
    private GroupId groupId;
    private String replyPostId;
    private int replyPostMediaIndex;

    private int expectedMediaCount;

    private boolean prevEditEmpty;
    private boolean updatedMediaProcessed = false;
    private int currentItemToSet = -1;

    private boolean allowVoiceNotes;

    private @ComposeMode int composeMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        supportRequestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        getWindow().setExitTransition(new Fade());

        setExitSharedElementCallback(new SharedElementCallback() {
            @Override
            public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                final View currentView = mediaPagerAdapter.getCurrentView();
                if (currentView != null) {
                    ContentPhotoView imageView = currentView.findViewById(R.id.image);
                    ContentPlayerView contentPlayerView = currentView.findViewById(R.id.video);

                    if (imageView.getVisibility() == View.VISIBLE) {
                        sharedElements.put(MediaEditActivity.TRANSITION_VIEW_NAME, imageView);
                    } else {
                        sharedElements.put(MediaEditActivity.TRANSITION_VIEW_NAME, contentPlayerView);
                    }

                }
            }
        });

        if (Build.VERSION.SDK_INT >= 28) {
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        getWindow().getDecorView().setSystemUiVisibility(SystemUiVisibility.getDefaultSystemUiVisibility(this));
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        setContentView(R.layout.activity_content_composer);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Preconditions.checkNotNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        final float scrolledElevation = getResources().getDimension(R.dimen.action_bar_elevation);
        mediaVerticalScrollView = findViewById(R.id.media_vertical_scroll);
        mediaVerticalScrollView.setOnScrollChangeListener((ContentComposerScrollView.OnScrollChangeListener) (view, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            final float elevation = scrollY > 0 ? scrolledElevation : 0;
            if (toolbar.getElevation() != elevation) {
                toolbar.setElevation(elevation);
            }
        });
        mediaVerticalScrollView.setOnOverScrollChangeListener((view, scrollX, scrollY, clampedX, clampedY) -> {
            if (mediaPager.getVisibility() == View.VISIBLE && scrollY <= 0 && clampedY) {
                clearEditFocus();
            }
        });

        final Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);
        fullThumbnailLoader = new MediaThumbnailLoader(this, Math.min(Constants.MAX_IMAGE_DIMENSION, Math.max(point.x, point.y)));
        textContentLoader = new TextContentLoader();
        audioDurationLoader = new AudioDurationLoader(this);

        voiceNoteRecorderControlView = findViewById(R.id.recording_ui);
        voiceNoteRecorderControlView.setVoiceVisualizerView(findViewById(R.id.bottom_visualizer));
        audioComposer = findViewById(R.id.voice_composer);
        postEntryView = findViewById(R.id.post_entry);
        postEntryView.setInputParent(new PostEntryView.InputParent() {
            @Override
            public void onSendVoiceNote() {
                viewModel.finishRecording();
                postEntryView.bindAudioDraft(audioDurationLoader, viewModel.getVoiceDraft());
            }

            @Override
            public void requestVoicePermissions() {

            }

            @Override
            public void onUrl(String url) {

            }
        });
        voiceNoteRecorderControlView.setRecordingTimeView(postEntryView.getRecordingTimeView());
        bottomSendButton = findViewById(R.id.bottom_composer_send);
        bottomSendButton.setOnClickListener(v -> {
            sharePost();
        });
        View textOnlySend = findViewById(R.id.text_only_send);
        textOnlySend.setEnabled(false);
        textOnlySend.setOnClickListener(v -> {
            sharePost();
        });

        mentionPickerView = findViewById(R.id.mention_picker_view);

        final View loadingView = findViewById(R.id.media_loading);

        addMediaButton = findViewById(R.id.add_media);
        addMoreText = findViewById(R.id.add_more_text);
        cropPictureButton = findViewById(R.id.crop);
        deletePictureButton = findViewById(R.id.delete);

        addMediaButton.setOnClickListener(v -> addAdditionalMedia());
        addMoreText.setOnClickListener(v -> addAdditionalMedia());
        cropPictureButton.setOnClickListener(v -> cropItem(getCurrentItem()));

        voicePostComposerView = findViewById(R.id.voice_composer_view);
        textEntryCard = findViewById(R.id.text_entry_card);
        mediaContainer = findViewById(R.id.media_container);

        voiceAddMedia = findViewById(R.id.voice_add_media);
        textAddMedia = findViewById(R.id.text_add_media);

        deletePictureButton.setOnClickListener(v -> deleteItem(getCurrentItem()));
        final ArrayList<Uri> uris;
        if (Intent.ACTION_SEND.equals(getIntent().getAction())) {
            calledFromPicker = false;
            final Uri uri = getIntent().getParcelableExtra(Intent.EXTRA_STREAM);
            if (uri != null) {
                uris = new ArrayList<>(Collections.singleton(uri));
            } else {
                uris = null;
            }
        } else {
            if (getIntent().hasExtra(MediaEditActivity.EXTRA_MEDIA)) {
                calledFromPicker = true;
                uris = getIntent().getParcelableArrayListExtra(MediaEditActivity.EXTRA_MEDIA);
            } else {
                calledFromPicker = false;
                uris = getIntent().getParcelableArrayListExtra(Intent.EXTRA_STREAM);
            }
        }
        calledFromCamera = getIntent().getBooleanExtra(EXTRA_CALLED_FROM_CAMERA, false);
        allowAddMedia = getIntent().getBooleanExtra(EXTRA_ALLOW_ADD_MEDIA, false);
        boolean voiceNotePost = getIntent().getBooleanExtra(EXTRA_VOICE_NOTE_POST, false);
        bottomEditText = findViewById(R.id.entry_bottom);
        bottomEditText.setPreImeListener((keyCode, event) -> {
            if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                clearEditFocus();
                return true;
            }
            return false;
        });
        textPostEntry = findViewById(R.id.entry_card);
        final Bundle editStates = getIntent().getParcelableExtra(MediaEditActivity.EXTRA_STATE);
        if (uris != null) {
            Log.i("ContentComposerActivity received " + uris.size() + " uris");
            loadingView.setVisibility(View.VISIBLE);
            if (uris.size() > Constants.MAX_POST_MEDIA_ITEMS) {
                SnackbarHelper.showInfo(mediaVerticalScrollView, getResources().getQuantityString(R.plurals.max_post_media_items, Constants.MAX_POST_MEDIA_ITEMS, Constants.MAX_POST_MEDIA_ITEMS));
                uris.subList(Constants.MAX_POST_MEDIA_ITEMS, uris.size()).clear();
            }
            showMixedMediaCompose();
            composeMode = ComposeMode.MEDIA;
        } else {
            Log.i("ContentComposerActivity no uri list provided");
            if (voiceNotePost) {
                showAudioOnlyCompose();
                composeMode = ComposeMode.AUDIO;
            } else {
                composeMode = ComposeMode.TEXT;
                showTextOnlyCompose();
                textPostEntry.requestFocus();
            }
        }

        if (savedInstanceState == null) {
            chatId = getIntent().getParcelableExtra(EXTRA_CHAT_ID);
            groupId = getIntent().getParcelableExtra(EXTRA_GROUP_ID);
            replyPostId = getIntent().getStringExtra(EXTRA_REPLY_POST_ID);
            replyPostMediaIndex = getIntent().getIntExtra(EXTRA_REPLY_POST_MEDIA_INDEX, -1);
        } else {
            chatId = savedInstanceState.getParcelable(EXTRA_CHAT_ID);
            groupId = savedInstanceState.getParcelable(EXTRA_GROUP_ID);
            replyPostId = savedInstanceState.getString(EXTRA_REPLY_POST_ID);
            replyPostMediaIndex = savedInstanceState.getInt(EXTRA_REPLY_POST_MEDIA_INDEX, -1);
        }

        viewModel = new ViewModelProvider(this,
                new ContentComposerViewModel.Factory(getApplication(), chatId, groupId, uris, editStates, replyPostId, replyPostMediaIndex)).get(ContentComposerViewModel.class);

        mediaThumbnailLoader = new MediaThumbnailLoader(this, 2 * getResources().getDimensionPixelSize(R.dimen.comment_media_list_height));
        urlPreviewLoader = new UrlPreviewLoader();
        postLinkPreviewView = findViewById(R.id.link_preview);
        postLinkPreviewView.setMediaThumbnailLoader(mediaThumbnailLoader);

        mediaPager = findViewById(R.id.media_pager);
        mediaPager.setPageMargin(getResources().getDimensionPixelSize(R.dimen.media_pager_margin));
        mediaPager.setVisibility(View.GONE);
        mediaPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                clearEditFocus();
                updateMediaButtons();
                final int currentPosition = Rtl.isRtl(mediaPager.getContext()) ? mediaPagerAdapter.getCount() - 1 - position : position;
                refreshVideoPlayers(currentPosition);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        mediaPagerIndicator = findViewById(R.id.media_pager_indicator);
        mediaPagerAdapter = new MediaPagerAdapter();
        mediaPager.setAdapter(mediaPagerAdapter);

        String initialText = getIntent().getStringExtra(Intent.EXTRA_TEXT);

        textPostEntry.setVisibility(View.VISIBLE);
        textPostEntry.setMentionPickerView(mentionPickerView);
        textPostEntry.setText(initialText);

        bottomEditText.setVisibility(View.VISIBLE);
        bottomEditText.setMentionPickerView(mentionPickerView);
        bottomEditText.setText(initialText);

        allowVoiceNotes = chatId == null && ServerProps.getInstance().getVoicePostsEnabled();
        postEntryView.setAllowVoiceNoteRecording(allowVoiceNotes && TextUtils.isEmpty(initialText));

        textAddMedia.setOnClickListener(v -> {
            addAdditionalMedia();
        });

        voiceAddMedia.setOnClickListener(v -> {
            addAdditionalMedia();
        });

        final boolean isMediaPost = uris != null;
        final int minHeightUnfocused = getResources().getDimensionPixelSize(R.dimen.entry_bottom_unfocused_min_height);
        final int minHeightFocused = getResources().getDimensionPixelSize(R.dimen.entry_bottom_focused_min_height);
        bottomEditText.setOnFocusChangeListener((view, hasFocus) -> {
            updateMediaButtons();
            mediaVerticalScrollView.setShouldScrollToBottom(hasFocus);
        });

        if (replyPostId != null || (!isMediaPost && !voiceNotePost)) {
            textPostEntry.requestFocus();
        }

        textPostEntry.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                prevEditEmpty = TextUtils.isEmpty(charSequence);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                boolean isEmpty = TextUtils.isEmpty(charSequence);
                if (prevEditEmpty != isEmpty) {
                    textOnlySend.setEnabled(!isEmpty);
                }

                final boolean useLargeText = (charSequence.length() < 180 && mediaPager.getVisibility() == View.GONE);
                textPostEntry.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(
                        useLargeText ? R.dimen.composer_text_size_large : R.dimen.composer_text_size));
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        bottomEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                prevEditEmpty = TextUtils.isEmpty(charSequence);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                boolean isEmpty = TextUtils.isEmpty(charSequence);
                if (allowVoiceNotes && prevEditEmpty != isEmpty) {
                    postEntryView.setAllowVoiceNoteRecording(isEmpty);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        drawDelegateView = findViewById(R.id.draw_delegate);

        expectedMediaCount = (uris != null) ? uris.size() : 0;

        viewModel.loadingItem.observe(this, editItem -> setProgressPreview(editItem, true));
        viewModel.editMedia.observe(this, media -> {
            loadingView.setVisibility(View.GONE);
            setProgressPreview(viewModel.loadingItem.getValue(), false);

            if (!media.isEmpty()) {
                mediaPager.setVisibility(View.VISIBLE);
                mediaPager.setOffscreenPageLimit(media.size());
                updateComposeMode(ComposeMode.MEDIA);
            }
            mediaPagerAdapter.setMediaPairList(media);
            if (media.size() <= 1) {
                mediaPagerIndicator.setVisibility(View.GONE);
                addMoreText.setVisibility(allowAddMedia ? View.VISIBLE : View.GONE);
            } else {
                mediaPagerIndicator.setVisibility(View.VISIBLE);
                addMoreText.setVisibility(View.GONE);
                mediaPagerIndicator.setViewPager(mediaPager);
            }
            if (media.size() != expectedMediaCount) {
                SnackbarHelper.showWarning(mediaVerticalScrollView, R.string.failed_to_load_media);
            }
            updateMediaButtons();
            updateAspectRatioForMedia(media);
            if (chatId != null) {
                mediaVerticalScrollView.postScrollToBottom();
            }
            if (currentItemToSet != -1) {
                setCurrentItem(currentItemToSet, false);
                currentItemToSet = -1;
            }
        });
        viewModel.mentionableContacts.getLiveData().observe(this, contacts -> mentionPickerView.setMentionableContacts(contacts));
        viewModel.contentItem.observe(this, contentItem -> {
            if (contentItem != null) {
                if (!contentItem.hasMedia() && contentItem instanceof Post) {
                    postLinkPreviewView.attachPreview(contentItem);
                    urlPreviewLoader.cancel(postLinkPreviewView, true);
                }
                if (contentItem.urlPreview != null) {
                    BgWorkers.getInstance().execute(() -> {
                        if (contentItem.urlPreview.imageMedia != null) {
                            final File imagePreview = FileStore.getInstance().getMediaFile(RandomId.create() + "." + Media.getFileExt(Media.MEDIA_TYPE_IMAGE));
                            try {
                                MediaUtils.transcodeImage(contentItem.urlPreview.imageMedia.file, imagePreview, null, Constants.MAX_IMAGE_DIMENSION, Constants.JPEG_QUALITY, false);
                                contentItem.urlPreview.imageMedia.file = imagePreview;
                            } catch (IOException e) {
                                Log.e("failed to transcode url preview image", e);
                                contentItem.urlPreview.imageMedia = null;
                            }
                        }
                        contentItem.addToStorage(ContentDb.getInstance());
                    });
                } else {
                    if (contentItem.loadingUrlPreview != null) {
                        urlPreviewLoader.addWaitingContentItem(contentItem);
                    }
                    contentItem.addToStorage(ContentDb.getInstance());
                }
                setResult(RESULT_OK);
                finish();
                if (chatId != null) {
                    final Intent intent = ChatActivity.open(this, chatId);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } else if (groupId != null) {
                    final Intent intent = ViewGroupFeedActivity.viewFeed(this, groupId, true);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                } else if (calledFromCamera ||
                        Intent.ACTION_SEND.equals(getIntent().getAction()) ||
                        Intent.ACTION_SEND_MULTIPLE.equals(getIntent().getAction())) {
                    final Intent intent = new Intent(getBaseContext(), MainActivity.class);
                    intent.putExtra(MainActivity.EXTRA_NAV_TARGET, MainActivity.NAV_TARGET_FEED);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }
        });
        privacyDestination = findViewById(R.id.privacy_destination);
        final TextView titleView = toolbar.findViewById(R.id.toolbar_title);
        if (chatId != null) {
            titleView.setText(R.string.new_message);
            viewModel.shareTargetName.getLiveData().observe(this, name -> {
                updateMessageSubtitle(name);
                if (replyPostId != null) {
                    final TextView replyNameView = findViewById(R.id.reply_name);
                    replyNameView.setText(name);
                }
            });
        } else {
            viewModel.shareTargetName.getLiveData().observe(this, name -> {
                if (groupId != null) {
                    privacyDestination.setText(name);
                }
            });
            updateDestination(groupId);
        }

        if (chatId == null) {
            View changePrivacy = findViewById(R.id.change_privacy);
            changePrivacy.setVisibility(View.VISIBLE);
            changePrivacy.setOnClickListener(v -> {
                startActivityForResult(SharePrivacyActivity.openPostPrivacy(this, groupId), REQUEST_CODE_CHANGE_PRIVACY);
            });
        }

        replyContainer = findViewById(R.id.reply_container);
        if (viewModel.replyPost != null) {
            viewModel.replyPost.getLiveData().observe(this, this::updatePostReply);
        } else {
            replyContainer.setVisibility(View.GONE);
        }

        if (chatId == null) {
            textPostEntry.addTextChangedListener(new UrlPreviewTextWatcher(url -> {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                if (mediaPager.getVisibility() == View.VISIBLE) {
                    urlPreviewLoader.cancel(postLinkPreviewView);
                    return;
                }
                urlPreviewLoader.load(postLinkPreviewView, url, new ViewDataLoader.Displayer<View, UrlPreview>() {
                    @Override
                    public void showResult(@NonNull View view, @Nullable UrlPreview result) {
                        postLinkPreviewView.updateUrlPreview(result);
                    }

                    @Override
                    public void showLoading(@NonNull View view) {
                        postLinkPreviewView.setLoadingUrl(url);
                        postLinkPreviewView.setLoading(!TextUtils.isEmpty(url));
                    }
                });
            }));
            postLinkPreviewView.setOnRemovePreviewClickListener(v -> {
                urlPreviewLoader.cancel(postLinkPreviewView);
                postLinkPreviewView.setLoading(false);
                postLinkPreviewView.updateUrlPreview(null);
            });

            voicePostComposerView.bindHost(this, new VoicePostComposerView.Host() {
                @Override
                public void onStartRecording() {
                    viewModel.getVoiceNoteRecorder().record();
                }

                @Override
                public void onStopRecording() {
                    viewModel.finishRecording();
                    voicePostComposerView.bindAudioDraft(audioDurationLoader, viewModel.getVoiceDraft());
                }

                @Override
                public void onSend() {
                    final boolean supportsWideColor = ActivityUtils.supportsWideColor(ContentComposerActivity.this);
                    verifyVideosDurationWithinLimit(
                            () -> {},
                            () -> viewModel.prepareContent(chatId, groupId, null, null, supportsWideColor));
                }

                @Override
                public void onAttachMedia() {

                }

                @Override
                public void onDeleteRecording() {
                    viewModel.deleteDraft();
                    voicePostComposerView.bindAudioDraft(audioDurationLoader, null);
                }
            }, viewModel.getVoiceNotePlayer(), viewModel.getVoiceNoteRecorder());

            postEntryView.setVoiceNoteControlView(voiceNoteRecorderControlView);
            postEntryView.bindVoicePlayer(this, viewModel.getVoiceNotePlayer());
            postEntryView.bindVoiceRecorder(this, viewModel.getVoiceNoteRecorder());
        }
    }

    private void updateComposeMode(@ComposeMode int newComposeMode) {
        if (newComposeMode == composeMode) {
            return;
        }
        if (composeMode == ComposeMode.TEXT) {
            bottomEditText.setText(textPostEntry.getText());
        } else if (composeMode == ComposeMode.MEDIA) {
            textPostEntry.setText(bottomEditText.getText());
        }

        switch (newComposeMode) {
            case ComposeMode.MEDIA:
                postEntryView.bindAudioDraft(audioDurationLoader, viewModel.getVoiceDraft());
                showMixedMediaCompose();
                break;
            case ComposeMode.AUDIO:
                showAudioOnlyCompose();
                voicePostComposerView.bindAudioDraft(audioDurationLoader, viewModel.getVoiceDraft());
                break;
            case ComposeMode.TEXT:
                showTextOnlyCompose();
                break;
        }
        composeMode = newComposeMode;
    }

    private void showMixedMediaCompose() {
        textEntryCard.setVisibility(View.GONE);
        audioComposer.setVisibility(View.GONE);
        bottomSendButton.setVisibility(View.VISIBLE);
        postEntryView.setVisibility(View.VISIBLE);
        mediaContainer.setVisibility(View.VISIBLE);
    }

    private void showTextOnlyCompose() {
        textEntryCard.setVisibility(View.VISIBLE);
        audioComposer.setVisibility(View.GONE);
        bottomSendButton.setVisibility(View.GONE);
        postEntryView.setVisibility(View.GONE);
        mediaContainer.setVisibility(View.GONE);
    }

    private void showAudioOnlyCompose() {
        textEntryCard.setVisibility(View.GONE);
        audioComposer.setVisibility(View.VISIBLE);
        bottomSendButton.setVisibility(View.GONE);
        postEntryView.setVisibility(View.GONE);
        mediaContainer.setVisibility(View.GONE);
        voiceNoteRecorderControlView.setVisibility(View.GONE);
    }

    private void updateMessageSubtitle(final String name) {
        final TextView subtitleView = toolbar.findViewById(R.id.toolbar_subtitle);
        subtitleView.setVisibility(View.VISIBLE);
        if (name == null) {
            Log.e("ContentComposerActivity: updateMessageSubtitle received null name");
            subtitleView.setText("");
        } else {
            subtitleView.setText(getString(R.string.composer_sharing_message, name));
        }
    }

    private void updateAspectRatioForMedia(List<ContentComposerViewModel.EditMediaPair> mediaPairList) {
        int maxHeight = findViewById(R.id.media_container).getHeight() - getResources().getDimensionPixelSize(R.dimen.content_composer_min_card_margin);

        if (mediaPairList.size() > 1) {
            maxHeight -= mediaPagerIndicator.getHeight();
        }

        mediaPager.setSizeLimits(ContentComposerViewModel.EditMediaPair.getMaxAspectRatio(mediaPairList), maxHeight);
    }

    private void updateDestination(@Nullable GroupId newFeedTarget) {
        groupId = newFeedTarget;
        viewModel.setDestinationFeed(newFeedTarget);
        if (newFeedTarget != null) {
            viewModel.getFeedPrivacy().removeObservers(this);
        } else {
            viewModel.getFeedPrivacy().observe(this, this::updatePostSubtitle);
        }
    }

    @Override
    public void onBackPressed() {
        if (calledFromPicker) {
            viewModel.doNotDeleteTempFiles();
            prepareResult();
        } else if (composeMode == ComposeMode.MEDIA) {
            finishToMediaPicker();
        }
        super.onBackPressed();
    }

    private void openMediaPicker() {
        if (calledFromPicker) {
            viewModel.doNotDeleteTempFiles();
            prepareResult();
        }
        finish();
    }

    private void finishToMediaPicker() {
        Intent i = MediaPickerActivity.pickForPost(this, groupId);
        putExtraMediaDataInIntent(i);
        startActivity(i);
        finish();
    }

    private void putExtraMediaDataInIntent(@NonNull final Intent intent) {
        final ArrayList<Uri> uris = new ArrayList<>();
        final Bundle editStates = new Bundle();

        final List<ContentComposerViewModel.EditMediaPair> mediaPairList = viewModel.editMedia.getValue();
        if (mediaPairList != null) {
            for (ContentComposerViewModel.EditMediaPair mediaPair : mediaPairList) {
                uris.add(mediaPair.uri);
                editStates.putParcelable(mediaPair.uri.toString(), mediaPair.state);
            }
        }

        intent.putParcelableArrayListExtra(MediaEditActivity.EXTRA_MEDIA, uris);
        intent.putExtra(MediaEditActivity.EXTRA_STATE, editStates);
    }

    private void prepareResult() {
        final Intent intent = new Intent();
        putExtraMediaDataInIntent(intent);
        setResult(MediaPickerActivity.RESULT_SELECT_MORE, intent);
    }

    public void updatePostSubtitle(FeedPrivacy feedPrivacy) {
        if (feedPrivacy == null) {
            Log.e("ContentComposerActivity: updatePostSubtitle received null FeedPrivacy");
            privacyDestination.setText(R.string.home);
        } else if (PrivacyList.Type.ALL.equals(feedPrivacy.activeList)) {
            privacyDestination.setText(R.string.setting_feed_all);
        } else if (PrivacyList.Type.EXCEPT.equals(feedPrivacy.activeList)) {
            privacyDestination.setText(R.string.setting_feed_except);
        } else if (PrivacyList.Type.ONLY.equals(feedPrivacy.activeList)) {
            privacyDestination.setText(R.string.setting_feed_only);
        } else {
            Log.e("ContentComposerActivity: updatePostSubtitle received unexpected activeList - " + feedPrivacy.activeList);
            privacyDestination.setText("");
        }
    }

    private void updatePostReply(@Nullable Post post) {
        if (post == null) {
            replyContainer.setVisibility(View.GONE);
        } else {
            replyContainer.setVisibility(View.VISIBLE);
            final TextView replyTextView = findViewById(R.id.reply_text);
            textContentLoader.load(replyTextView, post);
            final ImageView replyMediaIconView = findViewById(R.id.reply_media_icon);
            final ImageView replyMediaThumbView = findViewById(R.id.reply_media_thumb);
            if (replyPostMediaIndex >= 0 && replyPostMediaIndex < post.media.size()) {
                replyMediaThumbView.setVisibility(View.VISIBLE);
                replyMediaThumbView.setOutlineProvider(new ViewOutlineProvider() {
                    @Override
                    public void getOutline(View view, Outline outline) {
                        outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), getResources().getDimension(R.dimen.comment_media_list_corner_radius));
                    }
                });
                replyMediaThumbView.setClipToOutline(true);
                final Media media = post.media.get(replyPostMediaIndex);
                fullThumbnailLoader.load(replyMediaThumbView, media);
                replyMediaIconView.setVisibility(View.VISIBLE);
                switch (media.type) {
                    case Media.MEDIA_TYPE_IMAGE: {
                        replyMediaIconView.setImageResource(R.drawable.ic_camera);
                        if (TextUtils.isEmpty(post.text)) {
                            replyTextView.setText(R.string.photo);
                        }
                        break;
                    }
                    case Media.MEDIA_TYPE_VIDEO: {
                        replyMediaIconView.setImageResource(R.drawable.ic_video);
                        if (TextUtils.isEmpty(post.text)) {
                            replyTextView.setText(R.string.video);
                        }
                        break;
                    }
                    case Media.MEDIA_TYPE_UNKNOWN:
                    default: {
                        replyMediaIconView.setImageResource(R.drawable.ic_media_collection);
                        break;
                    }
                }
            } else {
                replyMediaThumbView.setVisibility(View.GONE);
                replyMediaIconView.setVisibility(View.GONE);
            }
            findViewById(R.id.reply_close).setOnClickListener(v -> {
                replyPostId = null;
                replyPostMediaIndex = -1;
                replyContainer.setVisibility(View.GONE);
            });
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (replyPostId != null) {
            outState.putString(EXTRA_REPLY_POST_ID, replyPostId);
            outState.putInt(EXTRA_REPLY_POST_MEDIA_INDEX, replyPostMediaIndex);
        }
        if (chatId != null) {
            outState.putParcelable(EXTRA_CHAT_ID, chatId);
        }
        if (groupId != null) {
            outState.putParcelable(EXTRA_GROUP_ID, groupId);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        fullThumbnailLoader.destroy();
        urlPreviewLoader.destroy();
        mediaThumbnailLoader.destroy();
        textContentLoader.destroy();
        audioDurationLoader.destroy();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT > 23) {
            Log.d("ContentComposerActivity: init all video players onStart");
            initializeAllVideoPlayers();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Util.SDK_INT <= 23) {
            Log.d("ContentComposerActivity: init all video players onResume");
            initializeAllVideoPlayers();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            Log.d("ContentComposerActivity: release all video players onPause");
            releaseAllVideoPlayers();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            Log.d("ContentComposerActivity: release all video players onStop");
            releaseAllVideoPlayers();
        }
    }

    private void addAdditionalMedia() {
        final Intent intent = MediaPickerActivity.pickMoreMedia(this);
        putExtraMediaDataInIntent(intent);
        updatedMediaProcessed = false;
        startActivityForResult(intent, REQUEST_CODE_MORE_MEDIA);
    }

    public void cropItem(final int currentItem) {
        final List<ContentComposerViewModel.EditMediaPair> mediaPairList = viewModel.editMedia.getValue();
        if (mediaPairList != null && mediaPairList.size() > currentItem) {
            final Bundle state = new Bundle();
            final ArrayList<Uri> uris = new ArrayList<>(mediaPairList.size());

            for (final ContentComposerViewModel.EditMediaPair mediaPair : mediaPairList) {
                uris.add(mediaPair.uri);
                if (mediaPair.state != null) {
                    state.putParcelable(mediaPair.uri.toString(), mediaPair.state);
                }
            }

            Intent intent = new Intent(this, MediaEditActivity.class);
            intent.putExtra(MediaEditActivity.EXTRA_MEDIA, uris);
            intent.putExtra(MediaEditActivity.EXTRA_SELECTED, currentItem);
            intent.putExtra(MediaEditActivity.EXTRA_STATE, state);

            updatedMediaProcessed = false;
            ThreadUtils.runWithoutStrictModeRestrictions(() -> {
                startActivityForResult(intent, REQUEST_CODE_CROP, ActivityOptions.makeSceneTransitionAnimation(this, mediaPager, MediaEditActivity.TRANSITION_VIEW_NAME).toBundle());
            });
        }
    }

    private void deleteItem(final int currentItem) {
        final List<ContentComposerViewModel.EditMediaPair> mediaPairList = viewModel.editMedia.getValue();
        if (mediaPairList == null || currentItem < 0 || mediaPairList.size() <= currentItem) {
            return;
        }

        final ContentComposerViewModel.EditMediaPair mediaPair = mediaPairList.get(currentItem);
        if (mediaPair.original.type == Media.MEDIA_TYPE_VIDEO) {
            final View mediaView = mediaPager.findViewWithTag(mediaPair);
            final ContentPlayerView contentPlayerView = (mediaView != null) ? mediaView.findViewById(R.id.video) : null;
            releaseVideoPlayer(mediaPair, contentPlayerView);
        }

        fullThumbnailLoader.remove(mediaPair.original.file);
        if (mediaPair.edit != null) {
            fullThumbnailLoader.remove(mediaPair.edit.file);
        }
        viewModel.deleteMediaItem(currentItem);
        mediaPagerAdapter.setMediaPairList(mediaPairList);
        if (!mediaPairList.isEmpty()) {
            setCurrentItem(currentItem > mediaPairList.size() ? mediaPairList.size() - 1 : currentItem, true);
        } else {
            if (viewModel.getVoiceDraft() != null) {
                updateComposeMode(ComposeMode.AUDIO);
            } else if (TextUtils.isEmpty(bottomEditText.getText())) {
                openMediaPicker();
                return;
            } else {
                updateComposeMode(ComposeMode.TEXT);
            }
            return;
        }
        if (mediaPairList.size() <= 1) {
            mediaPagerIndicator.setVisibility(View.GONE);
            addMoreText.setVisibility(View.VISIBLE);
        } else {
            mediaPagerIndicator.setVisibility(View.VISIBLE);
            mediaPagerIndicator.setViewPager(mediaPager);
            addMoreText.setVisibility(View.GONE);
        }
        updateAspectRatioForMedia(mediaPairList);
        updateMediaButtons();
        refreshVideoPlayers(getCurrentItem());
    }

    private void verifyVideosDurationWithinLimit(Runnable fail, Runnable success) {
        long maxVideoLength;
        if (chatId != null) {
            maxVideoLength = ServerProps.getInstance().getMaxChatVideoDuration();
        } else {
            maxVideoLength = ServerProps.getInstance().getMaxFeedVideoDuration();
        }

        List<ContentComposerViewModel.EditMediaPair> media = viewModel.getEditMedia();

        BgWorkers.getInstance().execute(() -> {
            boolean videoTooLong = false;

            if (media != null) {
                for (ContentComposerViewModel.EditMediaPair pair : media) {
                    Media m = pair.getRelevantMedia();
                    if (m.type == Media.MEDIA_TYPE_VIDEO && MediaUtils.getVideoDuration(m.file) > maxVideoLength * 1000) {
                        Log.w("Media " + m + " too long; canceling");
                        videoTooLong = true;
                        break;
                    }
                }
            }

            if (videoTooLong) {
                runOnUiThread(() -> {
                    String message = getResources().getQuantityString(R.plurals.max_video_length, (int) maxVideoLength, maxVideoLength);
                    SnackbarHelper.showWarning(mediaVerticalScrollView, message);

                    fail.run();
                });
            } else {
                runOnUiThread(success);
            }
        });

    }

    private void sharePost() {
        if (Boolean.TRUE.equals(viewModel.getVoiceNoteRecorder().isLocked().getValue())) {
            viewModel.finishRecording();
        }
        final Pair<String, List<Mention>> textAndMentions;
        if (composeMode == ComposeMode.TEXT) {
            textAndMentions = textPostEntry.getTextWithMentions();
        } else {
            textAndMentions = bottomEditText.getTextWithMentions();
        }
        final String postText = textAndMentions.first;
        if (TextUtils.isEmpty(postText) && viewModel.getEditMedia() == null) {
            Log.w("ContentComposerActivity: cannot send empty content");
        } else {
            postEntryView.setCanSend(false);
            final boolean supportsWideColor = ActivityUtils.supportsWideColor(this);
            verifyVideosDurationWithinLimit(
                    () -> postEntryView.setCanSend(true),
                    () -> viewModel.prepareContent(chatId, groupId, postText.trim(), textAndMentions.second, supportsWideColor)
            );
        }
    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        super.onActivityReenter(resultCode, data);

        // Do it here instead of onActivityResult because onActivityResult is called only after
        // the animated transition ends.
        if (resultCode == RESULT_OK && data.hasExtra(MediaEditActivity.EXTRA_SELECTED)) {
            if (data.hasExtra(MediaEditActivity.EXTRA_MEDIA)) {
                postponeEnterTransition();

                mediaPager.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        if (mediaPager.getVisibility() != View.VISIBLE) {
                            return true;
                        }

                        mediaPager.getViewTreeObserver().removeOnPreDrawListener(this);
                        startPostponedEnterTransition();

                        return true;
                    }
                });

                onDataUpdated(data);
            } else {
                int selected = data.getIntExtra(MediaEditActivity.EXTRA_SELECTED, getCurrentItem());
                List<ContentComposerViewModel.EditMediaPair> items = viewModel.editMedia.getValue();

                if (items != null && getCurrentItem() != selected && 0 <= selected && selected < items.size()) {
                    postponeEnterTransition();
                    setCurrentItem(selected, false);
                    startPostponedEnterTransition();
                }
            }

            updatedMediaProcessed = true;
        }
    }

    @Override
    public void onActivityResult(final int request, final int result, final Intent data) {
        super.onActivityResult(request, result, data);
        switch (request) {
            case REQUEST_CODE_CROP:
            case REQUEST_CODE_MORE_MEDIA:
                if (result == RESULT_OK && !updatedMediaProcessed) {
                    onDataUpdated(data);
                    updatedMediaProcessed = true;
                }
                break;
            case REQUEST_CODE_CHANGE_PRIVACY:
                if (result == RESULT_OK && data != null) {
                    GroupId newId = data.getParcelableExtra(SharePrivacyActivity.RESULT_GROUP_ID);
                    updateDestination(newId);
                }
                break;
        }
    }

    private void onDataUpdated(@NonNull final Intent data) {
        final ArrayList<Uri> uris = data.getParcelableArrayListExtra(MediaEditActivity.EXTRA_MEDIA);
        final int currentItem = data.getIntExtra(MediaEditActivity.EXTRA_SELECTED, getCurrentItem());
        final Bundle editStates = data.getParcelableExtra(MediaEditActivity.EXTRA_STATE);

        if (uris != null) {
            if (uris.size() == 0) {
                openMediaPicker();
                return;
            }
            // Clean old data
            final List<ContentComposerViewModel.EditMediaPair> mediaPairList = viewModel.editMedia.getValue();
            if (mediaPairList != null && !mediaPairList.isEmpty()) {
                releaseAllVideoPlayers();
                for (final ContentComposerViewModel.EditMediaPair mediaPair : mediaPairList) {
                    fullThumbnailLoader.remove(mediaPair.getRelevantMedia().file);
                }
                if (0 <= currentItem && currentItem < mediaPairList.size()) {
                    setCurrentItem(currentItem, false);
                }
                mediaPairList.clear();
            }

            // Load new data
            final View loadingView = findViewById(R.id.media_loading);
            loadingView.setVisibility(View.VISIBLE);
            mediaPager.setVisibility(View.GONE);
            expectedMediaCount = uris.size();
            currentItemToSet = currentItem;
            viewModel.loadUris(uris, editStates);
        }
    }

    private void setProgressPreview(@Nullable ContentComposerViewModel.EditMediaPair editMediaPair, boolean showPreview) {
        final Media mediaItem = editMediaPair != null ? editMediaPair.getRelevantMedia() : null;
        final ContentPhotoView previewView = findViewById(R.id.preview);
        final View progressView = findViewById(R.id.progress);

        if (showPreview && mediaItem != null) {
            previewView.setVisibility(View.VISIBLE);
            progressView.setVisibility(View.GONE);
            fullThumbnailLoader.load(previewView, mediaItem);
        } else {
            previewView.setVisibility(View.GONE);
            progressView.setVisibility(View.VISIBLE);
            if (mediaItem != null && mediaItem.type == Media.MEDIA_TYPE_VIDEO) {
                fullThumbnailLoader.remove(mediaItem.file);
            }
        }
    }

    private void updateMediaButtons() {
        final List<ContentComposerViewModel.EditMediaPair> mediaPairList = viewModel.getEditMedia();
        final boolean editIsFocused = textPostEntry != null && textPostEntry.isFocused();
        if (mediaPairList != null && !mediaPairList.isEmpty() && !editIsFocused) {
            addMediaButton.setVisibility(allowAddMedia ? View.VISIBLE : View.GONE);
            deletePictureButton.setVisibility(View.VISIBLE);
            cropPictureButton.setVisibility(View.VISIBLE);
        } else {
            addMediaButton.setVisibility(View.GONE);
            cropPictureButton.setVisibility(View.GONE);
            deletePictureButton.setVisibility(View.GONE);
        }
    }

    private void clearEditFocus() {
        if (bottomEditText.hasFocus()) {
            bottomEditText.clearFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(bottomEditText.getWindowToken(), 0);
        }
    }

    private void refreshVideoPlayers(int currentPosition) {
        final List<ContentComposerViewModel.EditMediaPair> mediaPairList = viewModel.getEditMedia();
        if (mediaPairList != null && !mediaPairList.isEmpty()) {
            int index = 0;
            for (ContentComposerViewModel.EditMediaPair mediaPair : mediaPairList) {
                if (mediaPair.original.type == Media.MEDIA_TYPE_VIDEO) {
                    final View mediaView = mediaPager.findViewWithTag(mediaPair);
                    if (mediaView != null) {
                        final ContentPlayerView contentPlayerView = mediaView.findViewById(R.id.video);
                        if (shouldPlayerBeActive(index, currentPosition)) {
                            initializeVideoPlayer(mediaPair, contentPlayerView, false);
                        } else {
                            releaseVideoPlayer(mediaPair, contentPlayerView);
                        }
                    }
                }
                index++;
            }
        }
    }

    private boolean shouldPlayerBeActive(int index, int activeIndex) {
        return Math.abs(index - activeIndex) <= 1;
    }

    private void initializeAllVideoPlayers() {
        refreshVideoPlayers(getCurrentItem());
    }

    private void initializeVideoPlayer(@NonNull final ContentComposerViewModel.EditMediaPair mediaPair,
                                       @NonNull final ContentPlayerView contentPlayerView,
                                       boolean shouldAutoPlay) {
        SimpleExoPlayer player = playerMap.get(mediaPair);

        if (player == null) {
            Log.d(String.format("ContentComposerActivity: initializeVideoPlayer %s", mediaPair.uri));
            final DataSource.Factory dataSourceFactory = ExoUtils.getDefaultDataSourceFactory(getApplicationContext());
            final MediaItem mediaItem = ExoUtils.getUriMediaItem(Uri.fromFile(mediaPair.getRelevantMedia().file));
            final MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem);

            final AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.CONTENT_TYPE_MOVIE)
                    .build();

            DefaultLoadControl.Builder builder = new DefaultLoadControl.Builder();
            builder.setBufferDurationsMs(EXO_PLAYER_BUFFER_MS, EXO_PLAYER_BUFFER_MS, DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_MS, DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS);
            player = new SimpleExoPlayer.Builder(getApplicationContext()).setLoadControl(builder.build()).build();
            player.addListener(new Player.EventListener() {
                @Override
                public void onIsPlayingChanged(boolean isPlaying) {
                    contentPlayerView.setKeepScreenOn(isPlaying);
                }
            });
            playerMap.put(mediaPair, player);
            player.setRepeatMode(Player.REPEAT_MODE_ALL);
            player.setAudioAttributes(audioAttributes, true);
            contentPlayerView.setPlayer(player);
            player.setMediaSource(mediaSource, false);
            player.prepare();
        }

        if (shouldAutoPlay != player.getPlayWhenReady()) {
            player.setPlayWhenReady(shouldAutoPlay);
        }
    }

    private void releaseAllVideoPlayers() {
        final List<ContentComposerViewModel.EditMediaPair> mediaPairList = viewModel.getEditMedia();
        if (mediaPairList != null && !mediaPairList.isEmpty()) {
            for (ContentComposerViewModel.EditMediaPair mediaPair : mediaPairList) {
                if (mediaPair.original.type == Media.MEDIA_TYPE_VIDEO) {
                    final View mediaView = mediaPager.findViewWithTag(mediaPair);
                    final ContentPlayerView contentPlayerView = (mediaView != null) ? mediaView.findViewById(R.id.video) : null;
                    releaseVideoPlayer(mediaPair, contentPlayerView);
                }
            }
        }
    }

    private void releaseVideoPlayer(@NonNull ContentComposerViewModel.EditMediaPair mediaPair, @Nullable ContentPlayerView playerView) {
        if (playerView != null) {
            playerView.setPlayer(null);
        }
        final SimpleExoPlayer player = playerMap.get(mediaPair);
        if (player != null) {
            player.stop();
            player.release();
            playerMap.remove(mediaPair);
            Log.d(String.format("ContentComposerActivity: releaseVideoPlayer %s", mediaPair.uri));
        }
    }

    private class MediaPagerAdapter extends PagerAdapter {
        final List<ContentComposerViewModel.EditMediaPair> mediaPairList = new ArrayList<>();
        private View currentView;

        MediaPagerAdapter() {
        }

        void setMediaPairList(@NonNull List<ContentComposerViewModel.EditMediaPair> mediaPairList) {
            this.mediaPairList.clear();
            this.mediaPairList.addAll(mediaPairList);
            notifyDataSetChanged();
        }

        public int getItemPosition(@NonNull Object object) {
            int index = 0;
            final Object tag = ((View) object).getTag();
            for (ContentComposerViewModel.EditMediaPair mediaPair : mediaPairList) {
                if (mediaPair.equals(tag)) {
                    return Rtl.isRtl(mediaPager.getContext()) ? mediaPairList.size() - 1 - index : index;
                }
                index++;
            }
            return POSITION_NONE;
        }

        @Nullable
        public View getCurrentView() {
            return currentView;
        }

        @Override
        public void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            super.setPrimaryItem(container, position, object);
            currentView = (View)object;
        }

        @Override
        public @NonNull Object instantiateItem(@NonNull ViewGroup container, int position) {
            final View view = getLayoutInflater().inflate(R.layout.content_composer_media_pager_item, container, false);
            final ContentPhotoView imageView = view.findViewById(R.id.image);
            final ContentPlayerView contentPlayerView = view.findViewById(R.id.video);
            imageView.setMaxAspectRatio(0);
            contentPlayerView.setMaxAspectRatio(0);

            final int currentPosition = Rtl.isRtl(container.getContext()) ? mediaPairList.size() - 1 - position : position;
            final ContentComposerViewModel.EditMediaPair mediaPair = mediaPairList.get(currentPosition);
            final Media mediaItem = mediaPair.getRelevantMedia();

            view.setTag(mediaPair);

            if (mediaItem.type == Media.MEDIA_TYPE_VIDEO) {
                imageView.setVisibility(View.GONE);
                contentPlayerView.setVisibility(View.VISIBLE);
                contentPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
                if (mediaItem.width > 0) {
                    contentPlayerView.setAspectRatio(1f * mediaItem.height / mediaItem.width);
                }

                final int activePosition = Rtl.isRtl(container.getContext()) ? mediaPairList.size() - 1 - getCurrentItem() : getCurrentItem();
                if (shouldPlayerBeActive(activePosition, currentPosition)) {
                    initializeVideoPlayer(mediaPair, contentPlayerView, false);
                }
                contentPlayerView.setOnClickListener(v -> clearEditFocus());
                GestureDetector doubleTapDetector = new GestureDetector(contentPlayerView.getContext(), new GestureDetector.SimpleOnGestureListener() {

                    @Override
                    public boolean onSingleTapConfirmed(MotionEvent e) {
                        if (contentPlayerView.isPlaying()) {
                            contentPlayerView.pause();
                        } else {
                            contentPlayerView.play();
                        }
                        return true;
                    }
                });
                contentPlayerView.setOnTouchListener((v, event) -> doubleTapDetector.onTouchEvent(event));
            } else {
                fullThumbnailLoader.load(imageView, mediaItem);
                imageView.setDrawDelegate(drawDelegateView);
                imageView.setOnClickListener(v -> clearEditFocus());
            }

            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object view) {
            container.removeView((View) view);
        }

        @Override
        public int getCount() {
            return mediaPairList.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }
    }

    void setCurrentItem(int position, boolean smoothScroll) {
        mediaPager.setCurrentItem(Rtl.isRtl(mediaPager.getContext()) ? mediaPagerAdapter.getCount() - 1 - position : position, smoothScroll);
    }

    int getCurrentItem() {
        if (mediaPagerAdapter.getCount() == 0) {
            return 0;
        }
        return Rtl.isRtl(mediaPager.getContext()) ? mediaPagerAdapter.getCount() - 1 - mediaPager.getCurrentItem() : mediaPager.getCurrentItem();
    }

}
