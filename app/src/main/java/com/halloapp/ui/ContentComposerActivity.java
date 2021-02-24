package com.halloapp.ui;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.SharedElementCallback;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.halloapp.Constants;
import com.halloapp.R;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Media;
import com.halloapp.content.Mention;
import com.halloapp.content.Post;
import com.halloapp.id.ChatId;
import com.halloapp.id.GroupId;
import com.halloapp.media.MediaThumbnailLoader;
import com.halloapp.media.MediaUtils;
import com.halloapp.privacy.FeedPrivacy;
import com.halloapp.props.ServerProps;
import com.halloapp.ui.chat.ChatActivity;
import com.halloapp.ui.groups.ViewGroupFeedActivity;
import com.halloapp.ui.mediaedit.VideoEditActivity;
import com.halloapp.ui.mediapicker.MediaPickerActivity;
import com.halloapp.ui.mentions.MentionPickerView;
import com.halloapp.ui.mentions.TextContentLoader;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.Preconditions;
import com.halloapp.util.Rtl;
import com.halloapp.util.StringUtils;
import com.halloapp.util.ThreadUtils;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.ContentComposerScrollView;
import com.halloapp.widget.ContentPhotoView;
import com.halloapp.widget.ContentPlayerView;
import com.halloapp.widget.DrawDelegateView;
import com.halloapp.widget.MediaViewPager;
import com.halloapp.widget.MentionableEntry;
import com.halloapp.widget.SnackbarHelper;
import com.halloapp.xmpp.privacy.PrivacyList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.relex.circleindicator.CircleIndicator;

public class ContentComposerActivity extends HalloActivity {
    public static final String EXTRA_CALLED_FROM_CAMERA = "called_from_camera";
    public static final String EXTRA_CHAT_ID = "chat_id";
    public static final String EXTRA_GROUP_ID = "group_id";
    public static final String EXTRA_REPLY_POST_ID = "reply_post_id";
    public static final String EXTRA_REPLY_POST_MEDIA_INDEX = "reply_post_media_index";

    private static final int REQUEST_CODE_CROP = 1;
    private static final int REQUEST_CODE_MORE_MEDIA = 2;

    private final Map<ContentComposerViewModel.EditMediaPair, SimpleExoPlayer> playerMap = new HashMap<>();

    private ContentComposerViewModel viewModel;
    private MediaThumbnailLoader fullThumbnailLoader;
    private TextContentLoader textContentLoader;
    private ContentComposerScrollView mediaVerticalScrollView;
    private MentionableEntry editText;
    private MentionPickerView mentionPickerView;
    private MediaViewPager mediaPager;
    private CircleIndicator mediaPagerIndicator;
    private MediaPagerAdapter mediaPagerAdapter;
    private DrawDelegateView drawDelegateView;
    private Toolbar toolbar;
    private View replyContainer;

    private boolean calledFromCamera;
    private boolean calledFromPicker;

    private ImageButton addMediaButton;
    private ImageButton deletePictureButton;
    private ImageButton cropPictureButton;

    @Nullable
    private ChatId chatId;
    @Nullable
    private GroupId groupId;
    private String replyPostId;
    private int replyPostMediaIndex;

    private int expectedMediaCount;

    private boolean prevEditEmpty;
    private boolean updatedMediaProcessed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        getWindow().setExitTransition(new Fade());

        setExitSharedElementCallback(new SharedElementCallback() {
            @Override
            public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                sharedElements.put(CropImageActivity.TRANSITION_VIEW_NAME, mediaPagerAdapter.getCurrentView().findViewById(R.id.image));
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

        mediaVerticalScrollView = findViewById(R.id.media_vertical_scroll);
        final float scrolledElevation = getResources().getDimension(R.dimen.action_bar_elevation);
        mediaVerticalScrollView.setOnScrollChangeListener((ContentComposerScrollView.OnScrollChangeListener) (view, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            final float elevation = scrollY > 0 ? scrolledElevation : 0;
            if (toolbar.getElevation() != elevation) {
                toolbar.setElevation(elevation);
            }
        });
        mediaVerticalScrollView.setOnOverScrollChangeListener((view, scrollX, scrollY, clampedX, clampedY) -> {
            if (scrollY <= 0 && clampedY) {
                clearEditFocus();
            }
        });

        final Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);
        fullThumbnailLoader = new MediaThumbnailLoader(this, Math.min(Constants.MAX_IMAGE_DIMENSION, Math.max(point.x, point.y)));
        textContentLoader = new TextContentLoader(this);

        mentionPickerView = findViewById(R.id.mention_picker_view);

        final View loadingView = findViewById(R.id.loading);

        addMediaButton = findViewById(R.id.add_media);
        cropPictureButton = findViewById(R.id.crop);
        deletePictureButton = findViewById(R.id.delete);

        addMediaButton.setOnClickListener(v -> addAdditionalMedia());
        cropPictureButton.setOnClickListener(v -> cropItem(getCurrentItem()));

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
            if (getIntent().hasExtra(CropImageActivity.EXTRA_MEDIA)) {
                calledFromPicker = true;
                uris = getIntent().getParcelableArrayListExtra(CropImageActivity.EXTRA_MEDIA);
            } else {
                calledFromPicker = false;
                uris = getIntent().getParcelableArrayListExtra(Intent.EXTRA_STREAM);
            }
        }
        calledFromCamera = getIntent().getBooleanExtra(EXTRA_CALLED_FROM_CAMERA, false);

        final Bundle editStates = getIntent().getParcelableExtra(CropImageActivity.EXTRA_STATE);

        if (uris != null) {
            Log.i("ContentComposerActivity received " + uris.size() + " uris");
            loadingView.setVisibility(View.VISIBLE);
            if (uris.size() > Constants.MAX_POST_MEDIA_ITEMS) {
                SnackbarHelper.showInfo(this, getResources().getQuantityString(R.plurals.max_post_media_items, Constants.MAX_POST_MEDIA_ITEMS, Constants.MAX_POST_MEDIA_ITEMS));
                uris.subList(Constants.MAX_POST_MEDIA_ITEMS, uris.size()).clear();
            }
            editText = findViewById(R.id.entry_bottom);
            editText.setPreImeListener((keyCode, event) -> {
                if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                    clearEditFocus();
                    return true;
                }
                return false;
            });
        } else {
            Log.i("ContentComposerActivity no uri list provided");
            loadingView.setVisibility(View.GONE);
            editText = findViewById(R.id.entry_card);
            editText.requestFocus();
            editText.setPreImeListener((keyCode, event) -> {
                if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                    finish();
                    return true;
                }
                return false;
            });
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

        editText.setVisibility(View.VISIBLE);
        editText.setMentionPickerView(mentionPickerView);
        editText.setText(getIntent().getStringExtra(Intent.EXTRA_TEXT));

        final boolean isMediaPost = uris != null;
        final int minHeightUnfocused = getResources().getDimensionPixelSize(R.dimen.entry_bottom_unfocused_min_height);
        final int minHeightFocused = getResources().getDimensionPixelSize(R.dimen.entry_bottom_focused_min_height);
        editText.setOnFocusChangeListener((view, hasFocus) -> {
            updateMediaButtons();
            if (isMediaPost) {
                final int minHeight = hasFocus ? minHeightFocused : minHeightUnfocused;
                editText.setMinHeight(minHeight);
                editText.setMinimumHeight(minHeight);
            }
            mediaVerticalScrollView.setShouldScrollToBottom(hasFocus);
        });

        if (replyPostId != null) {
            editText.requestFocus();
        }

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                prevEditEmpty = TextUtils.isEmpty(charSequence);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (prevEditEmpty != TextUtils.isEmpty(charSequence)) {
                    invalidateOptionsMenu();
                }

                final boolean useLargeText = (charSequence.length() < 180 && mediaPager.getVisibility() == View.GONE);
                editText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(
                        useLargeText ? R.dimen.composer_text_size_large : R.dimen.composer_text_size));
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
            }
            mediaPagerAdapter.setMediaPairList(media);
            if (media.size() <= 1) {
                mediaPagerIndicator.setVisibility(View.GONE);
            } else {
                mediaPagerIndicator.setVisibility(View.VISIBLE);
                mediaPagerIndicator.setViewPager(mediaPager);
            }
            if (media.size() != expectedMediaCount) {
                SnackbarHelper.showWarning(this, R.string.failed_to_load_media);
            }
            invalidateOptionsMenu();
            updateMediaButtons();
            updateAspectRatioForMedia(media);
        });
        viewModel.mentionableContacts.getLiveData().observe(this, contacts -> mentionPickerView.setMentionableContacts(contacts));
        viewModel.contentItem.observe(this, contentItem -> {
            if (contentItem != null) {
                contentItem.addToStorage(ContentDb.getInstance());
                setResult(RESULT_OK);
                finish();
                if (chatId != null) {
                    final Intent intent = new Intent(this, ChatActivity.class);
                    intent.putExtra(ChatActivity.EXTRA_CHAT_ID, chatId);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } else if (groupId != null) {
                    final Intent intent = ViewGroupFeedActivity.viewFeed(this, groupId);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
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
        final TextView titleView = toolbar.findViewById(R.id.toolbar_title);
        if (viewModel.shareTargetName != null) {
            if (groupId != null) {
                titleView.setText(R.string.new_post);
                viewModel.shareTargetName.getLiveData().observe(this, this::updatePostSubtitle);
            } else {
                titleView.setText(R.string.new_message);
                viewModel.shareTargetName.getLiveData().observe(this, name -> {
                    updateMessageSubtitle(name);
                    if (replyPostId != null) {
                        final TextView replyNameView = findViewById(R.id.reply_name);
                        replyNameView.setText(name);
                    }
                });
            }
        } else {
            titleView.setText(R.string.new_post);
            viewModel.getFeedPrivacy().observe(this, this::updatePostSubtitle);
        }

        replyContainer = findViewById(R.id.reply_container);
        if (viewModel.replyPost != null) {
            viewModel.replyPost.getLiveData().observe(this, this::updatePostReply);
        } else {
            replyContainer.setVisibility(View.GONE);
        }
    }

    private void updatePostSubtitle(final FeedPrivacy feedPrivacy) {
        final TextView subtitleView = toolbar.findViewById(R.id.toolbar_subtitle);
        if (feedPrivacy == null) {
            Log.e("ContentComposerActivity: updatePostSubtitle received null FeedPrivacy");
            subtitleView.setText("");
        } else if (PrivacyList.Type.ALL.equals(feedPrivacy.activeList)) {
            subtitleView.setText(R.string.composer_sharing_all_summary);
        } else if (PrivacyList.Type.EXCEPT.equals(feedPrivacy.activeList)) {
            subtitleView.setText(R.string.composer_sharing_except_summary);
        } else if (PrivacyList.Type.ONLY.equals(feedPrivacy.activeList)) {
            final int onlySize = feedPrivacy.onlyList.size();
            subtitleView.setText(getResources().getQuantityString(R.plurals.composer_sharing_only_summary, onlySize, onlySize));
        } else {
            Log.e("ContentComposerActivity: updatePostSubtitle received unexpected activeList - " + feedPrivacy.activeList);
            subtitleView.setText("");
        }
    }

    private void updatePostSubtitle(final String name) {
        final TextView subtitleView = toolbar.findViewById(R.id.toolbar_subtitle);
        if (name == null) {
            Log.e("ContentComposerActivity: updateMessageSubtitle received null name");
            subtitleView.setText("");
        } else {
            subtitleView.setText(getString(R.string.composer_sharing_post, name));
        }
    }

    private void updateMessageSubtitle(final String name) {
        final TextView subtitleView = toolbar.findViewById(R.id.toolbar_subtitle);
        if (name == null) {
            Log.e("ContentComposerActivity: updateMessageSubtitle received null name");
            subtitleView.setText("");
        } else {
            subtitleView.setText(getString(R.string.composer_sharing_message, name));
        }
    }

    private void updateAspectRatioForMedia(List<ContentComposerViewModel.EditMediaPair> mediaPairList) {
        if (chatId == null) {
            mediaPager.setMaxAspectRatio(
                    Math.min(Constants.MAX_IMAGE_ASPECT_RATIO, ContentComposerViewModel.EditMediaPair.getMaxAspectRatio(mediaPairList)));
        } else {
            mediaPager.setMaxAspectRatio(ContentComposerViewModel.EditMediaPair.getMaxAspectRatio(mediaPairList));
        }
    }

    @Override
    public void onBackPressed() {
        if (calledFromPicker) {
            viewModel.doNotDeleteTempFiles();
            prepareResult();
            super.onBackPressed();
        } else {
            super.onBackPressed();
        }
    }

    private void openMediaPicker() {
        if (calledFromPicker) {
            viewModel.doNotDeleteTempFiles();
            prepareResult();
        }
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

        intent.putParcelableArrayListExtra(CropImageActivity.EXTRA_MEDIA, uris);
        intent.putExtra(CropImageActivity.EXTRA_STATE, editStates);
    }

    private void prepareResult() {
        final Intent intent = new Intent();
        putExtraMediaDataInIntent(intent);
        setResult(MediaPickerActivity.RESULT_SELECT_MORE, intent);
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

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.content_composer_menu, menu);
        return true;
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        final MenuItem shareMenuItem = menu.findItem(R.id.share);
        final List<ContentComposerViewModel.EditMediaPair> mediaPairList = viewModel.editMedia.getValue();
        shareMenuItem.setVisible((mediaPairList != null && !mediaPairList.isEmpty()) || !TextUtils.isEmpty(editText.getText()));
        if (chatId != null) {
            shareMenuItem.setTitle(R.string.send);
        } else {
            shareMenuItem.setTitle(R.string.share);
        }
        return true;
    }

    private void addAdditionalMedia() {
        final Intent intent = new Intent(this, MediaPickerActivity.class);
        intent.putExtra(MediaPickerActivity.EXTRA_PICKER_PURPOSE, MediaPickerActivity.PICKER_PURPOSE_RESULT);
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

            final Intent intent;
            if (mediaPairList.get(currentItem).getRelevantMedia().type == Media.MEDIA_TYPE_IMAGE) {
                intent = new Intent(this, CropImageActivity.class);
            } else {
                intent = new Intent(this, VideoEditActivity.class);
            }

            intent.putExtra(CropImageActivity.EXTRA_MEDIA, uris);
            intent.putExtra(CropImageActivity.EXTRA_SELECTED, currentItem);
            intent.putExtra(CropImageActivity.EXTRA_STATE, state);

            if (chatId != null) {
                intent.putExtra(CropImageActivity.EXTRA_MAX_ASPECT_RATIO, 0f);
            }

            updatedMediaProcessed = false;
            ThreadUtils.runWithoutStrictModeRestrictions(() -> {
                startActivityForResult(intent, REQUEST_CODE_CROP, ActivityOptions.makeSceneTransitionAnimation(this, mediaPager, CropImageActivity.TRANSITION_VIEW_NAME).toBundle());
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
            openMediaPicker();
            return;
        }
        if (mediaPairList.size() <= 1) {
            mediaPagerIndicator.setVisibility(View.GONE);
        } else {
            mediaPagerIndicator.setVisibility(View.VISIBLE);
            mediaPagerIndicator.setViewPager(mediaPager);
        }
        updateAspectRatioForMedia(mediaPairList);
        invalidateOptionsMenu();
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
                    String message = getResources().getString(R.string.max_video_length, maxVideoLength);
                    SnackbarHelper.showWarning(this, message);

                    fail.run();
                });
            } else {
                runOnUiThread(success);
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.share) {
            final Pair<String, List<Mention>> textAndMentions = editText.getTextWithMentions();
            final String postText = StringUtils.preparePostText(textAndMentions.first);
            if (TextUtils.isEmpty(postText) && viewModel.getEditMedia() == null) {
                Log.w("ContentComposerActivity: cannot send empty content");
            } else {
                item.setEnabled(false);

                verifyVideosDurationWithinLimit(
                    () -> item.setEnabled(true),
                    () -> viewModel.prepareContent(chatId, groupId, postText.trim(), textAndMentions.second)
                );
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        super.onActivityReenter(resultCode, data);

        // Do it here instead of onActivityResult because onActivityResult is called only after
        // the animated transition ends.
        if (resultCode == RESULT_OK && data.hasExtra(CropImageActivity.EXTRA_MEDIA)) {
            postponeEnterTransition();
            onDataUpdated(data);
            updatedMediaProcessed = true;

            mediaPager.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View view, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    mediaPager.removeOnLayoutChangeListener(this);
                    startPostponedEnterTransition();
                }
            });
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
        }
    }

    private void onDataUpdated(@NonNull final Intent data) {
        final ArrayList<Uri> uris = data.getParcelableArrayListExtra(CropImageActivity.EXTRA_MEDIA);
        final int currentItem = data.getIntExtra(CropImageActivity.EXTRA_SELECTED, getCurrentItem());
        final Bundle editStates = data.getParcelableExtra(CropImageActivity.EXTRA_STATE);

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
            final View loadingView = findViewById(R.id.loading);
            loadingView.setVisibility(View.VISIBLE);
            mediaPager.setVisibility(View.GONE);
            expectedMediaCount = uris.size();
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
        final int currentItem = getCurrentItem();
        final boolean editIsFocused = editText != null && editText.isFocused();
        if (mediaPairList != null && !mediaPairList.isEmpty() && !editIsFocused) {
            addMediaButton.setVisibility(calledFromCamera ? View.GONE : View.VISIBLE);
            final Media mediaItem = mediaPairList.get(currentItem).getRelevantMedia();
            deletePictureButton.setVisibility(View.VISIBLE);
            cropPictureButton.setVisibility(View.VISIBLE);
        } else {
            addMediaButton.setVisibility(View.GONE);
            cropPictureButton.setVisibility(View.GONE);
            deletePictureButton.setVisibility(View.GONE);
        }
    }

    private void clearEditFocus() {
        if (editText.hasFocus()) {
            editText.clearFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
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

    private void initializeVideoPlayer(@NonNull ContentComposerViewModel.EditMediaPair mediaPair,
                                       @NonNull ContentPlayerView contentPlayerView,
                                       boolean shouldAutoPlay) {
        SimpleExoPlayer player = playerMap.get(mediaPair);
        if (player == null) {
            final DataSource.Factory dataSourceFactory =
                    new DefaultDataSourceFactory(getApplicationContext(), Constants.USER_AGENT);
            final MediaSource mediaSource =
                    new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.fromFile(mediaPair.getRelevantMedia().file));

            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.CONTENT_TYPE_MOVIE)
                    .build();

            player = new SimpleExoPlayer.Builder(getApplicationContext()).build();
            playerMap.put(mediaPair, player);
            player.setRepeatMode(Player.REPEAT_MODE_ALL);
            player.setAudioAttributes(audioAttributes, true);
            contentPlayerView.setPlayer(player);
            player.prepare(mediaSource, false, false);
            Log.d(String.format("ContentComposerActivity: initializeVideoPlayer %s", mediaPair.uri));
        } else {
            DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(getApplicationContext(), Constants.USER_AGENT);
            MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.fromFile(mediaPair.getRelevantMedia().file));
            player.prepare(mediaSource, false, false);
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
            if (chatId != null) {
                imageView.setMaxAspectRatio(0);
                contentPlayerView.setMaxAspectRatio(0);
            }
            final int currentPosition = Rtl.isRtl(container.getContext()) ? mediaPairList.size() - 1 - position : position;
            final ContentComposerViewModel.EditMediaPair mediaPair = mediaPairList.get(currentPosition);
            final Media mediaItem = mediaPair.getRelevantMedia();

            view.setTag(mediaPair);

            if (mediaItem.type == Media.MEDIA_TYPE_VIDEO) {
                imageView.setVisibility(View.GONE);
                contentPlayerView.setVisibility(View.VISIBLE);
                if (mediaItem.height > Constants.MAX_IMAGE_ASPECT_RATIO * mediaItem.width) {
                    contentPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
                } else {
                    contentPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
                }
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
                if (mediaItem.type == Media.MEDIA_TYPE_IMAGE) {
                    if (chatId == null && mediaItem.height > Constants.MAX_IMAGE_ASPECT_RATIO * mediaItem.width) {
                        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    } else {
                        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    }
                } else {
                    imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                }
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
