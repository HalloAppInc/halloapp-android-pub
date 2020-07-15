package com.halloapp.ui;

import android.content.Intent;
import android.graphics.Outline;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
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
import com.halloapp.contacts.Contact;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Media;
import com.halloapp.content.Mention;
import com.halloapp.content.Post;
import com.halloapp.media.MediaThumbnailLoader;
import com.halloapp.ui.mediapicker.MediaPickerActivity;
import com.halloapp.ui.mentions.MentionPickerView;
import com.halloapp.ui.mentions.TextContentLoader;
import com.halloapp.util.Log;
import com.halloapp.util.Preconditions;
import com.halloapp.util.Rtl;
import com.halloapp.util.StringUtils;
import com.halloapp.widget.CenterToast;
import com.halloapp.widget.ContentPhotoView;
import com.halloapp.widget.ContentPlayerView;
import com.halloapp.widget.DrawDelegateView;
import com.halloapp.widget.MediaViewPager;
import com.halloapp.widget.MentionableEntry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.relex.circleindicator.CircleIndicator;

public class ContentComposerActivity extends HalloActivity {
    public static final String EXTRA_CHAT_ID = "chat_id";
    public static final String EXTRA_REPLY_POST_ID = "reply_post_id";
    public static final String EXTRA_REPLY_POST_MEDIA_INDEX = "reply_post_media_index";

    private final Map<ContentComposerViewModel.EditMediaPair, SimpleExoPlayer> playerMap =
            new HashMap<ContentComposerViewModel.EditMediaPair, SimpleExoPlayer>();

    private ContentComposerViewModel viewModel;
    private MediaThumbnailLoader fullThumbnailLoader;
    private TextContentLoader textContentLoader;
    private MentionableEntry editText;
    private MentionPickerView mentionPickerView;
    private MediaViewPager mediaPager;
    private CircleIndicator mediaPagerIndicator;
    private MediaPagerAdapter mediaPagerAdapter;
    private DrawDelegateView drawDelegateView;
    private View replyContainer;

    private ImageButton deletePictureButton;
    private ImageButton cropPictureButton;
    private TextView mediaIndexView;
    private ImageButton addMediaButton;

    @Nullable
    private String chatId;
    private String replyPostId;
    private int replyPostMediaIndex;

    private int expectedMediaCount;

    private boolean prevEditEmpty;

    private static final int REQUEST_CODE_CROP = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("ContentComposerActivity: onCreate");

        if (Build.VERSION.SDK_INT >= 28) {
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        getWindow().getDecorView().setSystemUiVisibility(SystemUiVisibility.getDefaultSystemUiVisibility(this));
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        setContentView(R.layout.activity_content_composer);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        final Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);
        fullThumbnailLoader = new MediaThumbnailLoader(this, Math.min(Constants.MAX_IMAGE_DIMENSION, Math.max(point.x, point.y)));
        textContentLoader = new TextContentLoader(this);

        mentionPickerView = findViewById(R.id.mention_picker_view);
        editText = findViewById(R.id.entry);
        editText.setMentionPickerView(mentionPickerView);

        final View progressView = findViewById(R.id.progress);

        cropPictureButton = findViewById(R.id.crop);
        deletePictureButton = findViewById(R.id.delete);
        mediaIndexView = findViewById(R.id.media_index);
        addMediaButton = findViewById(R.id.add_media);

        cropPictureButton.setOnClickListener(v -> cropItem(getCurrentItem()));

        deletePictureButton.setOnClickListener(v -> deleteItem(getCurrentItem()));

        addMediaButton.setOnClickListener(view -> openMediaPicker());

        final ArrayList<Uri> uris;
        if (Intent.ACTION_SEND.equals(getIntent().getAction())) {
            final Uri uri = getIntent().getParcelableExtra(CropImageActivity.EXTRA_MEDIA);
            if (uri != null) {
                uris = new ArrayList<>(Collections.singleton(uri));
            } else {
                uris = null;
            }
        } else {
            uris = getIntent().getParcelableArrayListExtra(CropImageActivity.EXTRA_MEDIA);
        }

        final Bundle editStates = getIntent().getParcelableExtra(CropImageActivity.EXTRA_STATE);

        if (uris != null) {
            progressView.setVisibility(View.VISIBLE);
            if (uris.size() > Constants.MAX_POST_MEDIA_ITEMS) {
                CenterToast.show(this, getResources().getQuantityString(R.plurals.max_post_media_items, Constants.MAX_POST_MEDIA_ITEMS, Constants.MAX_POST_MEDIA_ITEMS));
                uris.subList(Constants.MAX_POST_MEDIA_ITEMS, uris.size()).clear();
            }
            editText.setHint(R.string.write_description);
            addMediaButton.setVisibility(View.VISIBLE);
        } else {
            progressView.setVisibility(View.GONE);
            editText.setMinimumHeight(
                    getResources().getDimensionPixelSize(R.dimen.type_post_edit_minimum_hight));
            editText.requestFocus();
            editText.setHint(R.string.type_a_post_hint);
            editText.setPreImeListener((keyCode, event) -> {
                if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                    finish();
                    return true;
                }
                return false;
            });
        }

        if (savedInstanceState == null) {
            chatId = getIntent().getStringExtra(EXTRA_CHAT_ID);
            replyPostId = getIntent().getStringExtra(EXTRA_REPLY_POST_ID);
            replyPostMediaIndex = getIntent().getIntExtra(EXTRA_REPLY_POST_MEDIA_INDEX, -1);
        } else {
            chatId = savedInstanceState.getString(EXTRA_CHAT_ID);
            replyPostId = savedInstanceState.getString(EXTRA_REPLY_POST_ID);
            replyPostMediaIndex = savedInstanceState.getInt(EXTRA_REPLY_POST_MEDIA_INDEX, -1);
        }
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
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        mediaPager = findViewById(R.id.media_pager);
        mediaPager.setPageMargin(getResources().getDimensionPixelSize(R.dimen.media_pager_margin));
        mediaPager.setVisibility(View.GONE);
        mediaPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
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

        drawDelegateView = findViewById(R.id.draw_delegate);

        expectedMediaCount = (uris != null) ? uris.size() : 0;
        viewModel = new ViewModelProvider(this,
                new ContentComposerViewModel.Factory(getApplication(), chatId, uris, editStates, replyPostId, replyPostMediaIndex)).get(ContentComposerViewModel.class);
        viewModel.editMedia.observe(this, media -> {
            progressView.setVisibility(View.GONE);
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
                // NOTE(Vasil): The following line violates strict thread file loading policy.
                CenterToast.show(getBaseContext(), R.string.failed_to_load_media);
            }
            invalidateOptionsMenu();
            updateMediaButtons();
            updateAspectRatioForMedia(media);
        });
        viewModel.mentionableContacts.getLiveData().observe(this, new Observer<List<Contact>>() {
            @Override
            public void onChanged(List<Contact> contacts) {
                mentionPickerView.setMentionableContacts(contacts);
            }
        });
        viewModel.contentItem.observe(this, contentItem -> {
            if (contentItem != null) {
                contentItem.addToStorage(ContentDb.getInstance(getBaseContext()));
                setResult(RESULT_OK);
                viewModel.cleanTmpFiles();
                finish();

                if (Intent.ACTION_SEND.equals(getIntent().getAction()) ||
                        Intent.ACTION_SEND_MULTIPLE.equals(getIntent().getAction())) {
                    final Intent intent = new Intent(getBaseContext(), MainActivity.class);
                    intent.putExtra(MainActivity.EXTRA_NAV_TARGET, MainActivity.NAV_TARGET_FEED);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }
        });
        if (viewModel.chatName != null) {
            setTitle("");
            viewModel.chatName.getLiveData().observe(this, name -> {
                this.setTitle(name);
                if (replyPostId != null) {
                    final TextView replyNameView = findViewById(R.id.reply_name);
                    replyNameView.setText(name);
                }
            });
        } else {
            setTitle(R.string.new_post);
        }

        replyContainer = findViewById(R.id.reply_container);
        if (viewModel.replyPost != null) {
            viewModel.replyPost.getLiveData().observe(this, this::updatePostReply);
        } else {
            replyContainer.setVisibility(View.GONE);
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
        prepareResult();
        super.onBackPressed();
    }

    private void openMediaPicker() {
        prepareResult();
        finish();
    }

    private void prepareResult() {
        final Intent intent = new Intent();
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
            outState.putString(EXTRA_CHAT_ID, chatId);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("ContentComposerActivity: onDestroy");
        fullThumbnailLoader.destroy();
    }
    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT > 23) {
            Log.d("ContentComposerActivity onStart");
            initializeAllVideoPlayers();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Util.SDK_INT <= 23) {
            Log.d("ContentComposerActivity onResume");
            initializeAllVideoPlayers();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            Log.d("ContentComposerActivity onPause");
            releaseAllVideoPlayers();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            Log.d("ContentComposerActivity onStop");
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

            final Intent intent = new Intent(this, CropImageActivity.class);
            intent.putExtra(CropImageActivity.EXTRA_MEDIA, uris);
            intent.putExtra(CropImageActivity.EXTRA_SELECTED, currentItem);
            intent.putExtra(CropImageActivity.EXTRA_STATE, state);

            startActivityForResult(intent, REQUEST_CODE_CROP);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.share: {
                final Pair<String, List<Mention>> textAndMentions = editText.getTextWithMentions();
                final String postText = StringUtils.preparePostText(textAndMentions.first);
                if (TextUtils.isEmpty(postText) && viewModel.getEditMedia() == null) {
                    Log.w("ContentComposerActivity: cannot send empty content");
                } else {
                    viewModel.prepareContent(
                            getIntent().getStringExtra(EXTRA_CHAT_ID), postText.trim(), textAndMentions.second);
                }
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    @Override
    public void onActivityResult(final int request, final int result, final Intent data) {
        super.onActivityResult(request, result, data);
        //noinspection SwitchStatementWithTooFewBranches
        switch (request) {
            case REQUEST_CODE_CROP: {
                if (result == RESULT_OK) {
                    onCropped(data);
                }
                break;
            }
        }
    }

    private void onCropped(@NonNull final Intent data) {
        final ArrayList<Uri> uris = data.getParcelableArrayListExtra(CropImageActivity.EXTRA_MEDIA);
        final int currentItem = data.getIntExtra(CropImageActivity.EXTRA_SELECTED, getCurrentItem());
        final Bundle editStates = data.getParcelableExtra(CropImageActivity.EXTRA_STATE);

        if (uris != null) {
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
            final View progressView = findViewById(R.id.progress);
            progressView.setVisibility(View.VISIBLE);
            mediaPager.setVisibility(View.GONE);
            expectedMediaCount = (uris != null) ? uris.size() : 0;
            viewModel.loadUris(uris, editStates);
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
        mediaPagerAdapter.notifyDataSetChanged();
        if (!mediaPairList.isEmpty()) {
            setCurrentItem(currentItem > mediaPairList.size() ? mediaPairList.size() - 1 : currentItem, true);
        } else {
            editText.setHint(R.string.type_a_post_hint);
            editText.requestFocus();
            final InputMethodManager imm = Preconditions.checkNotNull(
                    (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE));
            imm.showSoftInput(editText,0);
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

    private void updateMediaButtons() {
        final List<ContentComposerViewModel.EditMediaPair> mediaPairList = viewModel.getEditMedia();
        final int currentItem = getCurrentItem();
        if (mediaPairList == null || mediaPairList.size() <= 1) {
            mediaIndexView.setVisibility(View.GONE);
        } else {
            mediaIndexView.setText(String.format("%d / %d", currentItem + 1, mediaPairList.size()));
            mediaIndexView.setVisibility(View.VISIBLE);
        }
        if (mediaPairList != null && !mediaPairList.isEmpty()) {
            final Media mediaItem = mediaPairList.get(currentItem).getRelevantMedia();
            addMediaButton.setVisibility(View.VISIBLE);
            deletePictureButton.setVisibility(View.VISIBLE);
            if (mediaItem != null && mediaItem.type == Media.MEDIA_TYPE_IMAGE) {
                cropPictureButton.setVisibility(View.VISIBLE);
            } else {
                cropPictureButton.setVisibility(View.GONE);
            }
        } else {
            addMediaButton.setVisibility(View.GONE);
            cropPictureButton.setVisibility(View.GONE);
            deletePictureButton.setVisibility(View.GONE);
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
                            initializeVideoPlayer(mediaPair, contentPlayerView, index == currentPosition);
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
                    new DefaultDataSourceFactory(getApplicationContext(), "hallo");
            final MediaSource mediaSource =
                    new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.fromFile(mediaPair.original.file));

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

        }
        if (player != null && shouldAutoPlay != player.getPlayWhenReady()) {
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
                    initializeVideoPlayer(mediaPair, contentPlayerView, activePosition == currentPosition);
                }
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
