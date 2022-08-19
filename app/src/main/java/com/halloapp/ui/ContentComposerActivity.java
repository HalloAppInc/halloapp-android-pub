package com.halloapp.ui;

import android.Manifest;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;
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
import com.google.firebase.analytics.FirebaseAnalytics;
import com.halloapp.Constants;
import com.halloapp.FileStore;
import com.halloapp.Preferences;
import com.halloapp.R;
import com.halloapp.UrlPreview;
import com.halloapp.UrlPreviewLoader;
import com.halloapp.contacts.ContactLoader;
import com.halloapp.content.ContentDb;
import com.halloapp.content.ContentItem;
import com.halloapp.content.Media;
import com.halloapp.content.Mention;
import com.halloapp.id.ChatId;
import com.halloapp.id.GroupId;
import com.halloapp.media.AudioDurationLoader;
import com.halloapp.media.ExoUtils;
import com.halloapp.media.MediaThumbnailLoader;
import com.halloapp.media.MediaUtils;
import com.halloapp.privacy.FeedPrivacy;
import com.halloapp.props.ServerProps;
import com.halloapp.ui.chat.ChatActivity;
import com.halloapp.ui.chat.ReplyPreviewContainer;
import com.halloapp.ui.contacts.FavoritesNuxBottomSheetDialogFragment;
import com.halloapp.ui.groups.ViewGroupFeedActivity;
import com.halloapp.ui.mediaedit.MediaEditActivity;
import com.halloapp.ui.mediapicker.MediaPickerActivity;
import com.halloapp.ui.mentions.MentionPickerView;
import com.halloapp.ui.mentions.TextContentLoader;
import com.halloapp.ui.share.ShareDestination;
import com.halloapp.ui.share.ShareDestinationListView;
import com.halloapp.util.ActivityUtils;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.DialogFragmentUtils;
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
import com.halloapp.widget.VoiceVisualizerView;
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
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class ContentComposerActivity extends HalloActivity implements EasyPermissions.PermissionCallbacks {
    public static final String EXTRA_CALLED_FROM_CAMERA = "called_from_camera";
    public static final String EXTRA_ALLOW_ADD_MEDIA = "allow_add_media";
    public static final String EXTRA_CHAT_ID = "chat_id";
    public static final String EXTRA_GROUP_ID = "group_id";
    public static final String EXTRA_REPLY_POST_ID = "reply_post_id";
    public static final String EXTRA_REPLY_POST_MEDIA_INDEX = "reply_post_media_index";
    public static final String EXTRA_VOICE_NOTE_POST = "voice_note_post";
    public static final String EXTRA_DESTINATIONS = "destinations";
    public static final String EXTRA_NAVIGATE_TO_DESTINATION = "navigate_to_destination";
    public static final String EXTRA_FIRST_TIME_POST_ONBOARDING = "first_time_post_onboarding";
    public static final String EXTRA_FIRST_TIME_POST_SKIP_CONTACT_SELECT = "skip_contact_select";

    private static final int REQUEST_CODE_CROP = 1;
    private static final int REQUEST_CODE_MORE_MEDIA = 2;
    private static final int REQUEST_CODE_CHANGE_PRIVACY = 3;
    private static final int REQUEST_CODE_VOICE_PERMISSIONS = 4;
    private static final int REQUEST_CODE_CHOOSE_PRIVACY = 5;

    private static final int EXO_PLAYER_BUFFER_MS = 25000;

    public static Intent newTextPost(@NonNull Context context) {
        Intent i = new Intent(context, ContentComposerActivity.class);
        i.putExtra(EXTRA_ALLOW_ADD_MEDIA, true);
        return i;
    }

    public static Intent firstTimePostOnboarding(@NonNull Context context, int numContacts) {
        Intent i = new Intent(context, ContentComposerActivity.class);
        i.putExtra(EXTRA_ALLOW_ADD_MEDIA, true);
        i.putExtra(EXTRA_FIRST_TIME_POST_ONBOARDING, true);
        i.putExtra(EXTRA_FIRST_TIME_POST_SKIP_CONTACT_SELECT, numContacts == 0);
        return i;
    }

    public static Intent newAudioPost(@NonNull Context context) {
        Intent i = new Intent(context, ContentComposerActivity.class);
        i.putExtra(EXTRA_ALLOW_ADD_MEDIA, true);
        i.putExtra(EXTRA_VOICE_NOTE_POST, true);
        return i;
    }

    public static Intent newSharePost(@NonNull Context context, List<ShareDestination> destinations) {
        Intent i = new Intent(context, ContentComposerActivity.class);
        i.putExtra(EXTRA_ALLOW_ADD_MEDIA, false);
        i.putExtra(EXTRA_DESTINATIONS, new ArrayList<>(destinations));
        return i;
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        // dont need to do anything here
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (requestCode == REQUEST_CODE_VOICE_PERMISSIONS) {
            if (EasyPermissions.permissionPermanentlyDenied(this, Manifest.permission.RECORD_AUDIO)) {
                new AppSettingsDialog.Builder(this)
                        .setRationale(getString(R.string.voice_post_record_audio_permission_rationale_denied))
                        .build().show();
            }
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef ({ComposeMode.TEXT, ComposeMode.AUDIO, ComposeMode.MEDIA})
    public @interface ComposeMode {
        int TEXT = 1;
        int AUDIO = 2;
        int MEDIA = 3;
    }

    private final Map<ContentComposerViewModel.EditMediaPair, SimpleExoPlayer> playerMap = new HashMap<>();

    private FirebaseAnalytics firebaseAnalytics;

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

    private ReplyPreviewContainer replyPreviewContainer;

    private View audioComposer;

    private PostEntryView postEntryView;
    private View bottomSendButton;
    private View textEntryCard;
    private View mediaContainer;

    private View textAddMedia;
    private View voiceAddMedia;

    private TextView privacyDestination;
    private ImageView privacyIcon;
    private View changePrivacyBtn;

    private ContactLoader contactLoader;
    private UrlPreviewLoader urlPreviewLoader;
    private AudioDurationLoader audioDurationLoader;
    private PostLinkPreviewView postLinkPreviewView;
    private MediaThumbnailLoader mediaThumbnailLoader;

    private boolean allowAddMedia;
    private boolean calledFromCamera;
    private boolean calledFromPicker;

    private ImageButton addMediaButton;
    private ImageButton deletePictureButton;
    private ImageButton cropPictureButton;
    private View addMoreText;

    private View root;

    private VoicePostComposerView voicePostComposerView;
    private VoicePostRecorderControlView voiceNoteRecorderControlView;

    @Nullable
    private ChatId chatId;
    @Nullable
    private GroupId groupId;
    private String replyPostId;
    private int replyPostMediaIndex;

    private int expectedMediaCount;

    private boolean navigateToDestination;
    private boolean prevEditEmpty;
    private boolean updatedMediaProcessed = false;
    private int currentItemToSet = -1;

    private boolean allowVoiceNotes;

    private @ComposeMode int composeMode;

    private int minSoftKeyboardHeight;

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

        firebaseAnalytics = FirebaseAnalytics.getInstance(this);

        View toolbarContainer = findViewById(R.id.toolbar_container);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Preconditions.checkNotNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        final float scrolledElevation = getResources().getDimension(R.dimen.action_bar_elevation);
        mediaVerticalScrollView = findViewById(R.id.media_vertical_scroll);
        mediaVerticalScrollView.setOnScrollChangeListener((ContentComposerScrollView.OnScrollChangeListener) (view, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            final float elevation = scrollY > 0 ? scrolledElevation : 0;
            if (toolbarContainer.getElevation() != elevation) {
                toolbarContainer.setElevation(elevation);
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

        VoiceVisualizerView visualizer = findViewById(R.id.bottom_visualizer);
        voiceNoteRecorderControlView = findViewById(R.id.recording_ui);
        voiceNoteRecorderControlView.setVoiceVisualizerView(visualizer);
        audioComposer = findViewById(R.id.voice_composer);
        postEntryView = findViewById(R.id.post_entry);
        postEntryView.setInputParent(new PostEntryView.InputParent() {
            @Override
            public void onSendVoiceNote() {
                viewModel.finishRecording();
                postEntryView.bindAudioDraft(audioDurationLoader, viewModel.getVoiceDraft());
            }

            @Override
            public void onDeleteVoiceDraft() {
                final AlertDialog.Builder builder = new AlertDialog.Builder(ContentComposerActivity.this);
                builder.setMessage(getResources().getString(R.string.post_audio_draft_discard_confirmation));
                builder.setCancelable(true);
                builder.setPositiveButton(R.string.action_discard, (dialog, which) -> {
                    viewModel.deleteDraft();
                    postEntryView.bindAudioDraft(audioDurationLoader, null);
                });
                builder.setNegativeButton(R.string.cancel, null);
                builder.show();
            }

            @Override
            public void requestVoicePermissions() {
                requestVoicePostPermissions();
            }

            @Override
            public void onUrl(String url) {

            }
        });
        root = findViewById(R.id.root);
        minSoftKeyboardHeight = getResources().getDimensionPixelSize(R.dimen.min_softkeyboard_height);
        voiceNoteRecorderControlView.setRecordingTimeView(postEntryView.getRecordingTimeView());
        bottomSendButton = findViewById(R.id.bottom_composer_send);
        bottomSendButton.setOnClickListener(new DebouncedClickListener() {
            @Override
            public void onOneClick(@NonNull View view) {
                onSendButtonClick();
            }
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

        ShareDestinationListView destinationListView = findViewById(R.id.destinationList);

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
        navigateToDestination = getIntent().getBooleanExtra(EXTRA_NAVIGATE_TO_DESTINATION, true);
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
        final Bundle editStates = getIntent().getParcelableExtra(MediaEditActivity.EXTRA_STATE);
        textPostEntry = findViewById(R.id.entry_card);
        ArrayList<ShareDestination> destinations;
        if (savedInstanceState == null) {
            chatId = getIntent().getParcelableExtra(EXTRA_CHAT_ID);
            groupId = getIntent().getParcelableExtra(EXTRA_GROUP_ID);
            destinations = getIntent().getParcelableArrayListExtra(EXTRA_DESTINATIONS);
            replyPostId = getIntent().getStringExtra(EXTRA_REPLY_POST_ID);
            replyPostMediaIndex = getIntent().getIntExtra(EXTRA_REPLY_POST_MEDIA_INDEX, -1);
        } else {
            chatId = savedInstanceState.getParcelable(EXTRA_CHAT_ID);
            groupId = savedInstanceState.getParcelable(EXTRA_GROUP_ID);
            destinations = savedInstanceState.getParcelableArrayList(EXTRA_DESTINATIONS);
            replyPostId = savedInstanceState.getString(EXTRA_REPLY_POST_ID);
            replyPostMediaIndex = savedInstanceState.getInt(EXTRA_REPLY_POST_MEDIA_INDEX, -1);
        }

        viewModel = new ViewModelProvider(this,
                new ContentComposerViewModel.Factory(getApplication(), chatId, groupId, uris, editStates, destinations, replyPostId, replyPostMediaIndex)).get(ContentComposerViewModel.class);
        if (uris != null) {
            Log.i("ContentComposerActivity received " + uris.size() + " uris");
            loadingView.setVisibility(View.VISIBLE);

            int maxItems = chatId == null ? ServerProps.getInstance().getMaxPostMediaItems() : ServerProps.getInstance().getMaxChatMediaItems();

            if (uris.size() > maxItems) {
                SnackbarHelper.showInfo(mediaVerticalScrollView, getResources().getQuantityString(R.plurals.max_post_media_items, maxItems, maxItems));
                uris.subList(maxItems, uris.size()).clear();
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
            }
        }

        mediaThumbnailLoader = new MediaThumbnailLoader(this, 2 * getResources().getDimensionPixelSize(R.dimen.comment_media_list_height));
        urlPreviewLoader = new UrlPreviewLoader();
        postLinkPreviewView = findViewById(R.id.link_preview);
        postLinkPreviewView.setMediaThumbnailLoader(mediaThumbnailLoader);
        contactLoader = new ContactLoader();

        final TextView shareBtn = findViewById(R.id.share_btn);
        shareBtn.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                float radius = getResources().getDimension(R.dimen.share_destination_next_radius);
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), radius);
            }
        });
        shareBtn.setClipToOutline(true);
        if (isFirstTimeOnboardingPost()) {
            shareBtn.setText(R.string.skip);
            shareBtn.setOnClickListener(new DebouncedClickListener() {
                @Override
                public void onOneClick(@NonNull View view) {
                    Preferences.getInstance().applyCompletedFirstPostOnboarding(true);
                    final Intent intent = new Intent(getBaseContext(), MainActivity.class);
                    intent.putExtra(MainActivity.EXTRA_NAV_TARGET, MainActivity.NAV_TARGET_FEED);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
            });
            shareBtn.setVisibility(View.VISIBLE);
        } else {
            if (destinations != null && destinations.size() > 0) {
                shareBtn.setOnClickListener(new DebouncedClickListener() {
                    @Override
                    public void onOneClick(@NonNull View view) {
                        onSendButtonClick();
                    }
                });
                shareBtn.setVisibility(View.VISIBLE);
                bottomSendButton.setVisibility(View.GONE);
                final int horizontalPadding = getResources().getDimensionPixelSize(R.dimen.voice_post_share_end_padding);
                visualizer.setPadding(horizontalPadding, visualizer.getPaddingTop(), horizontalPadding, visualizer.getPaddingBottom());
            } else {
                viewModel.favoritesNuxShown.getLiveData().observe(this, shown -> {
                    if (!shown) {
                        DialogFragmentUtils.showDialogFragmentOnce(FavoritesNuxBottomSheetDialogFragment.newInstance(), getSupportFragmentManager());
                    }
                });
            }
        }

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

        if (isFirstTimeOnboardingPost()) {
            initialText = getString(R.string.first_time_post_content);
            ImageView textSendIcon = findViewById(R.id.text_only_send_icon);
            ImageView bottomSendIcon = findViewById(R.id.bottom_send_icon);
            textSendIcon.setImageResource(R.drawable.ic_media_gallery_next);
            bottomSendIcon.setImageResource(R.drawable.ic_media_gallery_next);
        }

        View textOnlySend = findViewById(R.id.text_only_send);
        textOnlySend.setEnabled(!TextUtils.isEmpty(initialText));
        textOnlySend.setOnClickListener(new DebouncedClickListener() {
            @Override
            public void onOneClick(@NonNull View view) {
                onSendButtonClick();
            }
        });
        textOnlySend.setVisibility(destinations != null && destinations.size() > 0 ? View.GONE : View.VISIBLE);

        textPostEntry.setVisibility(View.VISIBLE);
        bottomEditText.setVisibility(View.VISIBLE);

        allowVoiceNotes = shouldAllowVoiceNotes();
        postEntryView.setAllowVoiceNoteRecording(allowVoiceNotes && TextUtils.isEmpty(initialText));

        textAddMedia.setOnClickListener(v -> {
            addAdditionalMedia();
        });

        voiceAddMedia.setOnClickListener(v -> {
            addAdditionalMedia();
        });

        final boolean isMediaPost = uris != null;
        bottomEditText.setOnFocusChangeListener((view, hasFocus) -> {
            updateMediaButtons();
            mediaVerticalScrollView.setShouldScrollToBottom(hasFocus);
        });

        textPostEntry.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                boolean isEmpty = TextUtils.isEmpty(charSequence);
                textOnlySend.setEnabled(!isEmpty);

                if (composeMode == ComposeMode.TEXT && destinations != null && destinations.size() > 0) {
                    shareBtn.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
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
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (allowVoiceNotes) {
                    postEntryView.setAllowVoiceNoteRecording(TextUtils.isEmpty(charSequence));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        if (viewModel.getVoiceDraft() != null) {
            voicePostComposerView.bindAudioDraft(audioDurationLoader, viewModel.getVoiceDraft());
            postEntryView.bindAudioDraft(audioDurationLoader, viewModel.getVoiceDraft());
        }

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
            setCurrentItem(Math.max(currentItemToSet, 0), false);
            currentItemToSet = -1;
        });
        viewModel.mentionableContacts.getLiveData().observe(this, contacts -> mentionPickerView.setMentionableContacts(contacts));
        viewModel.contentItems.observe(this, contentItems -> {
            if (contentItems == null || contentItems.size() == 0) {
                return;
            }

            for (ContentItem item : contentItems) {
                if (!item.hasMedia()) {
                    postLinkPreviewView.attachPreview(item);
                    urlPreviewLoader.cancel(postLinkPreviewView, true);
                }

                if (item.urlPreview != null) {
                    BgWorkers.getInstance().execute(() -> {
                        if (item.urlPreview.imageMedia != null) {
                            final File imagePreview = FileStore.getInstance().getMediaFile(RandomId.create() + "." + Media.getFileExt(Media.MEDIA_TYPE_IMAGE));
                            try {
                                MediaUtils.transcodeImage(item.urlPreview.imageMedia.file, imagePreview, null, Constants.MAX_IMAGE_DIMENSION, Constants.JPEG_QUALITY, false);
                                item.urlPreview.imageMedia.file = imagePreview;
                            } catch (IOException e) {
                                Log.e("failed to transcode url preview image", e);
                                item.urlPreview.imageMedia = null;
                            }
                        }
                        item.addToStorage(ContentDb.getInstance());
                    });
                } else {
                    if (item.loadingUrlPreview != null) {
                        urlPreviewLoader.addWaitingContentItem(item);
                    }
                    item.addToStorage(ContentDb.getInstance());
                }
            }


            firebaseAnalytics.logEvent("post_sent", null);

            if (isFirstTimeOnboardingPost()) {
                final Intent intent = new Intent(getBaseContext(), MainActivity.class);
                intent.putExtra(MainActivity.EXTRA_NAV_TARGET, MainActivity.NAV_TARGET_FEED);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else {
                setResult(RESULT_OK);
                finish();
                if (navigateToDestination) {
                    boolean isMultiSharedToFeed = false;
                    if (viewModel.destinationList.getValue() != null && viewModel.destinationList.getValue().size() > 0) {
                        for (ShareDestination dest : viewModel.destinationList.getValue()) {
                            if (dest.type == ShareDestination.TYPE_MY_CONTACTS || dest.type == ShareDestination.TYPE_FAVORITES) {
                                isMultiSharedToFeed = true;
                                groupId = null;
                                chatId = null;
                                break;
                            } else if (dest.type == ShareDestination.TYPE_GROUP) {
                                groupId = (GroupId) dest.id;
                            } else if (dest.type == ShareDestination.TYPE_CONTACT) {
                                chatId = dest.id;
                            }
                        }
                    }

                    if (chatId != null) {
                        final Intent intent = ChatActivity.open(this, chatId);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    } else if (groupId != null) {
                        final Intent intent = ViewGroupFeedActivity.viewFeed(this, groupId, true);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                    } else if (isMultiSharedToFeed || calledFromCamera ||
                            Intent.ACTION_SEND.equals(getIntent().getAction()) ||
                            Intent.ACTION_SEND_MULTIPLE.equals(getIntent().getAction())) {
                        final Intent intent = new Intent(getBaseContext(), MainActivity.class);
                        intent.putExtra(MainActivity.EXTRA_NAV_TARGET, MainActivity.NAV_TARGET_FEED);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                }
            }
        });

        changePrivacyBtn = findViewById(R.id.change_privacy);
        privacyDestination = findViewById(R.id.privacy_destination);
        privacyIcon = findViewById(R.id.privacy_icon);
        final TextView titleView = toolbar.findViewById(R.id.toolbar_title);
        if (isFirstTimeOnboardingPost()) {
            titleView.setText(R.string.my_first_post_title);
        } else if (chatId != null) {
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


        if (chatId == null && destinations == null && !isFirstTimeOnboardingPost()) {
            changePrivacyBtn.setVisibility(View.VISIBLE);
            changePrivacyBtn.setOnClickListener(v -> {
                startActivityForResult(SharePrivacyActivity.openPostPrivacy(this, viewModel.getPrivacyList(), groupId), REQUEST_CODE_CHANGE_PRIVACY);
            });
        }

        if (destinations != null && destinations.size() > 0) {
            findViewById(R.id.destination_list_container).setVisibility(View.VISIBLE);
            destinationListView.setOnRemoveListener(viewModel::onDestinationRemoved);
            viewModel.destinationList.observe(this, destinationList -> {
                destinationListView.submitList(destinationList);
                if (destinationList.isEmpty()) {
                    Intent newList = new Intent();
                    newList.putParcelableArrayListExtra(EXTRA_DESTINATIONS, new ArrayList<>(destinationList));
                    setResult(RESULT_CANCELED, newList);
                    finish();
                } else {
                    boolean shouldAllowVoice = shouldAllowVoiceNotes();
                    if (allowVoiceNotes != shouldAllowVoice) {
                        allowVoiceNotes = shouldAllowVoice;
                        postEntryView.setAllowVoiceNoteRecording(allowVoiceNotes && TextUtils.isEmpty(bottomEditText.getText()));
                    }
                }
            });
            textAddMedia.setVisibility(View.GONE);
        }

        replyContainer = findViewById(R.id.reply_container);
        replyPreviewContainer = new ReplyPreviewContainer(replyContainer);
        replyPreviewContainer.init(contactLoader, textContentLoader, audioDurationLoader, mediaThumbnailLoader);
        replyPreviewContainer.setOnDismissListener(() -> {
            replyPostId = null;
            replyPostMediaIndex = -1;
            replyContainer.setVisibility(View.GONE);
        });
        if (viewModel.replyPost != null) {
            viewModel.replyPost.getLiveData().observe(this, post -> {
                replyPreviewContainer.bindPost(post, replyPostMediaIndex);
            });
        } else {
            replyPreviewContainer.hide();
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
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }
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
                    final AlertDialog.Builder builder = new AlertDialog.Builder(ContentComposerActivity.this);
                    builder.setMessage(getResources().getString(R.string.post_audio_draft_discard_confirmation));
                    builder.setCancelable(true);
                    builder.setPositiveButton(R.string.action_discard, (dialog, which) -> {
                        viewModel.deleteDraft();
                        voicePostComposerView.bindAudioDraft(audioDurationLoader, null);
                    });
                    builder.setNegativeButton(R.string.cancel, null);
                    builder.show();
                }

                @Override
                public void requestVoicePermissions() {
                    requestVoicePostPermissions();
                }
            }, viewModel.getVoiceNotePlayer(), viewModel.getVoiceNoteRecorder());

            postEntryView.setVoiceNoteControlView(voiceNoteRecorderControlView);
            postEntryView.bindVoicePlayer(this, viewModel.getVoiceNotePlayer());
            postEntryView.bindVoiceRecorder(this, viewModel.getVoiceNoteRecorder());
        }

        bottomEditText.setText(initialText);
        textPostEntry.setText(initialText);

        if (composeMode == ComposeMode.TEXT || replyPostId != null || (!isMediaPost && !voiceNotePost)) {
            Editable text = textPostEntry.getText();
            textPostEntry.requestFocus();
            textPostEntry.setSelection(text != null ? text.length() : 0);
        }
    }

    private void onSendButtonClick() {
        if (isFirstTimeOnboardingPost()) {
            Preferences.getInstance().applyCompletedFirstPostOnboarding(true);
            if (getIntent().getBooleanExtra(EXTRA_FIRST_TIME_POST_SKIP_CONTACT_SELECT, false)) {
                sharePost();
            } else {
                startActivityForResult(SharePrivacyActivity.selectFirstPostDestination(this, viewModel.getPrivacyList(), groupId), REQUEST_CODE_CHOOSE_PRIVACY);
            }
        } else {
            sharePost();
        }
    }

    private void requestVoicePostPermissions() {
        EasyPermissions.requestPermissions(this, getString(R.string.voice_post_record_audio_permission_rationale), REQUEST_CODE_VOICE_PERMISSIONS, Manifest.permission.RECORD_AUDIO);
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

    private boolean shouldAllowVoiceNotes() {
        boolean hasChatDestination = false;
        List<ShareDestination> destList = viewModel.destinationList.getValue();
        if (destList != null) {
            for (ShareDestination dest : destList) {
                if (dest.type == ShareDestination.TYPE_CONTACT) {
                    hasChatDestination = true;
                    break;
                }
            }
        }
        return !hasChatDestination && chatId == null;
    }

    private void showMixedMediaCompose() {
        textEntryCard.setVisibility(View.GONE);
        audioComposer.setVisibility(View.GONE);
        bottomSendButton.setVisibility(viewModel.hasDestinations() ? View.GONE : View.VISIBLE);
        postEntryView.setVisibility(View.VISIBLE);
        mediaContainer.setVisibility(View.VISIBLE);
        textPostEntry.setMentionPickerView(null);
        bottomEditText.setMentionPickerView(mentionPickerView);
    }

    private void showTextOnlyCompose() {
        textEntryCard.setVisibility(View.VISIBLE);
        audioComposer.setVisibility(View.GONE);
        bottomSendButton.setVisibility(View.INVISIBLE);
        postEntryView.setVisibility(View.INVISIBLE);
        mediaContainer.setVisibility(View.INVISIBLE);
        bottomEditText.setMentionPickerView(null);
        textPostEntry.setMentionPickerView(mentionPickerView);
    }

    private void showAudioOnlyCompose() {
        textEntryCard.setVisibility(View.GONE);
        audioComposer.setVisibility(View.VISIBLE);
        bottomSendButton.setVisibility(View.INVISIBLE);
        postEntryView.setVisibility(View.INVISIBLE);
        mediaContainer.setVisibility(View.INVISIBLE);
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

    private int getMediaMaxHeight() {
        int height = mediaContainer.getHeight() - getResources().getDimensionPixelSize(R.dimen.content_composer_min_card_margin);
        if (root.getPaddingBottom() >= minSoftKeyboardHeight) {
            height += root.getPaddingBottom();
        }
        return height;
    }

    private void updateAspectRatioForMedia(List<ContentComposerViewModel.EditMediaPair> mediaPairList) {
        int maxHeight = getMediaMaxHeight();

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
            privacyIcon.setVisibility(View.GONE);
            changePrivacyBtn.setBackgroundTintList(AppCompatResources.getColorStateList(this, R.color.secondary_button_color_selector));
        } else {
            viewModel.getFeedPrivacy().observe(this, this::updatePostSubtitle);
            privacyIcon.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        if (viewModel.getVoiceDraft() != null) {
            deleteAudioConfirm();
            return;
        }
        if (composeMode == ComposeMode.MEDIA && !calledFromCamera) {
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
        if (calledFromPicker) {
            openMediaPicker();
        } else {
            Intent i = MediaPickerActivity.pickForPost(this, groupId);
            putExtraMediaDataInIntent(i);
            startActivity(i);
            finish();
        }
    }

    private void deleteAudioConfirm() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getResources().getString(R.string.post_audio_draft_discard_confirmation));
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.action_discard, (dialog, which) -> {
            if (composeMode == ComposeMode.MEDIA && !calledFromCamera) {
                finishToMediaPicker();
            } else {
                finish();
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
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
        }
        if (feedPrivacy == null || PrivacyList.Type.ALL.equals(feedPrivacy.activeList) || PrivacyList.Type.EXCEPT.equals(feedPrivacy.activeList)) {
            privacyDestination.setText(R.string.setting_feed_all);
            privacyIcon.setImageResource(R.drawable.ic_privacy_my_contacts);
            changePrivacyBtn.setBackgroundTintList(AppCompatResources.getColorStateList(this, R.color.secondary_button_color_selector));
        } else if (PrivacyList.Type.ONLY.equals(feedPrivacy.activeList)) {
            privacyDestination.setText(R.string.contact_favorites);
            privacyIcon.setImageResource(R.drawable.ic_privacy_favorites);
            changePrivacyBtn.setBackgroundTintList(AppCompatResources.getColorStateList(this, R.color.favorites_button_color_selector));
        } else {
            Log.e("ContentComposerActivity: updatePostSubtitle received unexpected activeList - " + feedPrivacy.activeList);
            privacyDestination.setText("");
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
        List<ShareDestination> viewModelDestinations = viewModel.destinationList.getValue();
        if (viewModelDestinations != null) {
            outState.putParcelableArrayList(EXTRA_DESTINATIONS, new ArrayList<>(viewModelDestinations));
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
        contactLoader.destroy();
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
        if (resultCode == RESULT_OK && data != null && data.hasExtra(MediaEditActivity.EXTRA_SELECTED)) {
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

    private boolean isFirstTimeOnboardingPost() {
        return getIntent().getBooleanExtra(EXTRA_FIRST_TIME_POST_ONBOARDING, false);
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

                    String privacyType = data.getStringExtra(SharePrivacyActivity.RESULT_PRIVACY_TYPE);
                    if (newId == null) {
                        if (PrivacyList.Type.ONLY.equals(privacyType)) {
                            viewModel.setPrivacyList(privacyType);
                        } else {
                            viewModel.setPrivacyList(PrivacyList.Type.ALL);
                        }
                    }
                    updateDestination(newId);
                }
                break;
            case REQUEST_CODE_CHOOSE_PRIVACY:
                if (result == RESULT_OK && data != null) {
                    GroupId newId = data.getParcelableExtra(SharePrivacyActivity.RESULT_GROUP_ID);
                    String privacyType = data.getStringExtra(SharePrivacyActivity.RESULT_PRIVACY_TYPE);
                    if (newId == null) {
                        if (PrivacyList.Type.ONLY.equals(privacyType)) {
                            viewModel.setPrivacyList(privacyType);
                        } else {
                            viewModel.setPrivacyList(PrivacyList.Type.ALL);
                        }
                    }
                    updateDestination(newId);
                    sharePost();
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
