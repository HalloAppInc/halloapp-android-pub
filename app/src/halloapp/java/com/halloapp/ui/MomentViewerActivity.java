package com.halloapp.ui;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.transition.Transition;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityOptionsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.transition.TransitionManager;

import com.halloapp.Constants;
import com.halloapp.R;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactLoader;
import com.halloapp.content.Media;
import com.halloapp.content.MomentPost;
import com.halloapp.content.Post;
import com.halloapp.emoji.EmojiKeyboardLayout;
import com.halloapp.media.MediaThumbnailLoader;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.ui.home.HomeContentDecryptStatLoader;
import com.halloapp.ui.mediapicker.MediaPickerActivity;
import com.halloapp.ui.posts.SeenByLoader;
import com.halloapp.ui.profile.ViewProfileActivity;
import com.halloapp.util.DialogFragmentUtils;
import com.halloapp.util.KeyboardUtils;
import com.halloapp.util.Preconditions;
import com.halloapp.util.ScreenshotDetector;
import com.halloapp.util.TimeFormatter;
import com.halloapp.util.VibrationUtils;
import com.halloapp.util.ViewDataLoader;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.AvatarsLayout;
import com.halloapp.widget.BaseInputView;
import com.halloapp.widget.ChatInputView;
import com.halloapp.widget.PlaceholderDrawable;
import com.halloapp.widget.ShareExternallyView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class MomentViewerActivity extends HalloActivity implements EasyPermissions.PermissionCallbacks {

    public static final String MOMENT_TRANSITION_NAME = "moment-transition-image";
    private static final String EXTRA_MOMENT_POST_ID = "moment_post_id";
    private static final String EXTRA_USING_SHARED_TRANSITION = "using_shared_transition";

    private static final int CHECK_FADE_ANIM_DELAY = 1000;
    private static final int CHECK_FADE_ANIM_DURATION = 300;

    private static final int FLING_DISMISS_THRESHOLD = 1000;

    private static final int REQUEST_PERMISSIONS_RECORD_VOICE_NOTE = 1;
    private static final int REQUEST_CODE_COMPOSE = 2;

    public static Intent viewMoment(@NonNull Context context, @NonNull String postId) {
        Intent i = new Intent(context, MomentViewerActivity.class);
        i.putExtra(EXTRA_MOMENT_POST_ID, postId);

        return i;
    }

    public static void viewMomentWithTransition(@NonNull Activity activity, @NonNull String postId, @NonNull View photoView) {
        photoView.setTransitionName(MOMENT_TRANSITION_NAME);
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, photoView, photoView.getTransitionName());
        Intent i = viewMoment(activity, postId);
        i.putExtra(EXTRA_USING_SHARED_TRANSITION, true);
        activity.startActivity(i, options.toBundle());
    }

    private MomentViewerViewModel viewModel;

    private SeenByLoader seenByLoader;
    private AvatarLoader avatarLoader;
    private ContactLoader contactLoader;
    private MediaThumbnailLoader fullThumbnailLoader;

    private LinearLayout uploadingContainer;

    private View uploadingCover;
    private View uploadingDone;
    private View uploadingProgress;

    private ImageView avatar;
    private TextView name;
    private TextView time;
    private View moreOptions;
    private AvatarsLayout avatarsLayout;

    private ShareExternallyView shareExternallyView;
    private TextView shareExternallyTitle;

    private MomentCardHolder bottomCardHolder;
    private MomentCardHolder topCardHolder;

    private BaseInputView chatInputView;

    private EmojiKeyboardLayout emojiKeyboardLayout;
    private GestureDetector flingDetector;

    private float flingXVelocity;
    private float flingYVelocity;

    private int swipeVelocityThreshold;
    private int swipeExitStartThreshold;
    private float swipeExitTransDistance;
    private float mediaRadius;

    private ScreenshotDetector screenshotDetector;
    private HandlerThread screenshotHandlerThread;

    private MotionEvent swipeExitStart;
    private boolean isSwipeExitInProgress = false;
    private boolean isExiting = false;
    private boolean usingSharedTransition = false;
    private boolean hasUnlockingMoment = false;

    private ImageView transitionView;

    private boolean shouldNotifyScreenshot = false;

    private final SimpleDateFormat dayFormatter = new SimpleDateFormat("EEEE", Locale.getDefault());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        supportRequestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS);

        setContentView(R.layout.activity_moment_viewer);
        Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        setTitle("");

        String postId = getIntent().getStringExtra(EXTRA_MOMENT_POST_ID);
        usingSharedTransition = getIntent().getBooleanExtra(EXTRA_USING_SHARED_TRANSITION, false);
        transitionView = findViewById(R.id.transition_view);
        transitionView.setTransitionName(MOMENT_TRANSITION_NAME);

        if (usingSharedTransition) {
            postponeEnterTransition();
        }

        if (postId == null) {
            Log.e("MomentViewerActivity/onCreate null post id");
            finish();
            return;
        }

        swipeExitStartThreshold = getResources().getDimensionPixelSize(R.dimen.swipe_exit_start_threshold);
        swipeExitTransDistance = getResources().getDimension(R.dimen.swipe_exit_transition_distance);

        final Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);
        fullThumbnailLoader = new MediaThumbnailLoader(this, Math.min(Constants.MAX_IMAGE_DIMENSION, point.x));
        contactLoader = new ContactLoader(userId -> {
            startActivity(ViewProfileActivity.viewProfile(this, userId));
            return null;
        });
        seenByLoader = new SeenByLoader();
        avatarLoader = AvatarLoader.getInstance();

        screenshotHandlerThread = new HandlerThread("ScreenshotHandlerThread");
        screenshotHandlerThread.start();
        screenshotDetector = new ScreenshotDetector(this, new Handler(screenshotHandlerThread.getLooper()));
        screenshotDetector.setListener(this::onScreenshot);
        screenshotDetector.start();

        shareExternallyTitle = findViewById(R.id.share_externally_title);
        shareExternallyView = findViewById(R.id.share_externally);
        shareExternallyView.setListener(new ShareExternallyView.ShareListener() {
            @Override
            public void onOpenShare() {
                DialogFragmentUtils.showDialogFragmentOnce(ExternalSharingBottomSheetDialogFragment.shareDirectly(viewModel.getCurrent().getValue().id, null), getSupportFragmentManager());
            }

            @Override
            public void onShareTo(ShareExternallyView.ShareTarget target) {
                DialogFragmentUtils.showDialogFragmentOnce(ExternalSharingBottomSheetDialogFragment.shareDirectly(viewModel.getCurrent().getValue().id, target.getPackageName()), getSupportFragmentManager());
            }
        });
        viewModel = new ViewModelProvider(this, new MomentViewerViewModel.Factory(getApplication(), postId)).get(MomentViewerViewModel.class);

        bottomCardHolder = new MomentCardHolder(findViewById(R.id.first_card));
        topCardHolder = new MomentCardHolder(findViewById(R.id.second_card));

        avatar = findViewById(R.id.avatar);
        name = findViewById(R.id.name);
        time = findViewById(R.id.time);
        moreOptions = findViewById(R.id.more_options);

        Resources r = getResources();
        float density = r.getDisplayMetrics().density;
        swipeVelocityThreshold = (int)(FLING_DISMISS_THRESHOLD * density);

        mediaRadius = r.getDimension(R.dimen.moment_media_corner_radius);

        uploadingCover = findViewById(R.id.uploading_cover);
        uploadingDone = findViewById(R.id.uploaded_check);
        uploadingProgress = findViewById(R.id.uploaded_progress);

        TextView decryptStatus = findViewById(R.id.decrypt_status);
        if (decryptStatus != null) {
            new HomeContentDecryptStatLoader().loadPost(this, decryptStatus, postId);
        }

        avatarsLayout = findViewById(R.id.seen_indicator);
        avatarsLayout.setAvatarLoader(avatarLoader);
        avatarsLayout.setAvatarCount(3);
        avatarsLayout.setOnClickListener(v -> {
            final Intent intent = new Intent(MomentViewerActivity.this, PostSeenByActivity.class);
            Post post = viewModel.getCurrent().getValue();
            if (post != null) {
                intent.putExtra(PostSeenByActivity.EXTRA_POST_ID, post.id);
                startActivity(intent);
            } else {
                Log.i("MomentViewerActivity/seenOnClick null post");
            }
        });

        chatInputView = findViewById(R.id.text_entry);
        emojiKeyboardLayout = findViewById(R.id.emoji_keyboard);
        chatInputView.bindEmojiKeyboardLayout(emojiKeyboardLayout);

        chatInputView.setVoiceNoteControlView(findViewById(R.id.recording_ui));
        chatInputView.bindVoicePlayer(this, viewModel.getVoiceNotePlayer());
        chatInputView.bindVoiceRecorder(this, viewModel.getVoiceNoteRecorder());

        chatInputView.setAllowMedia(true);
        chatInputView.setAllowVoiceNoteRecording(true);
        chatInputView.setInputParent(new ChatInputView.InputParent() {
            @Override
            public void onSendText() {
                viewModel.sendMessage(chatInputView.getTextDraft());
                onMessageSent();
            }

            @Override
            public void onSendVoiceNote() {
                viewModel.finishRecording(false);
                onMessageSent();
            }

            @Override
            public void onSendVoiceDraft(File draft) {
                viewModel.sendVoiceNote(draft);
                onMessageSent();
            }

            @Override
            public void onChooseGallery() {
                Post moment = viewModel.getCurrent().getValue();
                if (moment != null) {
                    final Intent intent = MediaPickerActivity.pickForMessage(MomentViewerActivity.this, moment.senderUserId, moment.id, 0, chatInputView.getTextDraft());
                    startActivityForResult(intent, REQUEST_CODE_COMPOSE);
                }
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
                EasyPermissions.requestPermissions(MomentViewerActivity.this, getString(R.string.voice_note_record_audio_permission_rationale), REQUEST_PERMISSIONS_RECORD_VOICE_NOTE, Manifest.permission.RECORD_AUDIO);
            }

            @Override
            public void onUrl(String url) {

            }
        });
        uploadingContainer = findViewById(R.id.uploading_container);
        chatInputView.getTextEntry().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                updateSendButton(s);
            }
        });
        ImageView uploadingMomentImageFirst = findViewById(R.id.uploading_moment_image_first);
        ImageView uploadingMomentImageSecond = findViewById(R.id.uploading_moment_image_second);
        View uploadingMomentImageDivider = findViewById(R.id.uploading_moment_image_divider);

        float mediaRadius = getResources().getDimension(R.dimen.moment_media_corner_radius);
        View uploadingMomentImageContainer = findViewById(R.id.uploading_moment_image_container);
        uploadingMomentImageContainer.setClipToOutline(true);
        uploadingMomentImageContainer.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), mediaRadius);
            }
        });
        transitionView.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), mediaRadius);
            }
        });
        transitionView.setClipToOutline(true);

        viewModel.unlockingMoment.getLiveData().observe(this, unlockingMoment -> {
            updateViewUnlockState();

            if (unlockingMoment == null) {
                if (hasUnlockingMoment) {
                    finish();
                }

                return;
            }

            hasUnlockingMoment = true;

            List<Media> media = unlockingMoment.getMedia();
            if (!media.isEmpty()) {
                fullThumbnailLoader.load(uploadingMomentImageFirst, media.get(0));

                if (media.size() > 1) {
                    uploadingMomentImageSecond.setVisibility(View.VISIBLE);
                    uploadingMomentImageDivider.setVisibility(View.VISIBLE);

                    fullThumbnailLoader.load(uploadingMomentImageSecond, media.get(1));
                } else {
                    uploadingMomentImageSecond.setVisibility(View.GONE);
                    uploadingMomentImageDivider.setVisibility(View.GONE);
                }
            } else {
                Log.e("MomentViewerActivity/unlocking moment has no media id=" + unlockingMoment.id);
            }
        });

        viewModel.getCurrent().observe(this, moment -> {
            if (moment == null) {
                Log.w("MomentViewerActivity no moments to show yet");
                return;
            }

            topCardHolder.bindTo(moment);
            onMomentChanged(moment);

            if (viewModel.isInitializing() && usingSharedTransition) {
                viewModel.setInitializing(false);
                transitionView.setVisibility(View.VISIBLE);

                topCardHolder.imageViewContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        if (isImageViewReady(topCardHolder.imageViewFirst) && isImageViewReady(topCardHolder.imageViewSecond)) {
                            topCardHolder.imageViewContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            preparePostponedEnterTransition();
                        }
                    }
                });
            }
        });

        viewModel.getNext().observe(this, moment -> {
            if (moment != null) {
                bottomCardHolder.bindTo(moment);
            }
        });

        flingDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                onFlingWithVelocity(velocityX, velocityY);
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        shouldNotifyScreenshot = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        shouldNotifyScreenshot = false;
    }

    private boolean isImageViewReady(ImageView imageView) {
        return imageView.getVisibility() != View.VISIBLE || (imageView.getDrawable() != null && !(imageView.getDrawable() instanceof PlaceholderDrawable));
    }

    private void preparePostponedEnterTransition() {
        Bitmap snapshot = Bitmap.createBitmap(topCardHolder.imageViewContainer.getWidth(), topCardHolder.imageViewContainer.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(snapshot);

        topCardHolder.imageViewContainer.draw(canvas);
        transitionView.setImageBitmap(snapshot);

        int[] locationOrigin = new int[2];
        topCardHolder.imageViewContainer.getLocationInWindow(locationOrigin);

        int[] locationTransition = new int[2];
        transitionView.getLocationInWindow(locationTransition);

        transitionView.setTranslationX(locationOrigin[0] - locationTransition[0]);
        transitionView.setTranslationY(locationOrigin[1] - locationTransition[1]);

        getWindow().getSharedElementEnterTransition().addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {}

            @Override
            public void onTransitionEnd(Transition transition) {
                transitionView.post(() -> transitionView.setVisibility(View.GONE));
            }

            @Override
            public void onTransitionCancel(Transition transition) {}

            @Override
            public void onTransitionPause(Transition transition) {}

            @Override
            public void onTransitionResume(Transition transition) {}
        });

        transitionView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (isImageViewReady(transitionView)) {
                    transitionView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    startPostponedEnterTransition();
                }
            }
        });
    }

    private void onScreenshot() {
        if (shouldNotifyScreenshot) {
            viewModel.onScreenshotted();
        }
    }

    private void onMessageSent() {
        chatInputView.clearTextDraft();
        Toast.makeText(this, R.string.private_reply_sent, Toast.LENGTH_SHORT).show();
        KeyboardUtils.hideSoftKeyboard(chatInputView.getTextEntry());
        VibrationUtils.quickVibration(this);
    }

    private void updateSendButton(Editable s) {
        boolean emptyText = s == null || TextUtils.isEmpty(s.toString());
        chatInputView.setCanSend(!emptyText);
    }

    private void onFlingWithVelocity(float velX, float velY) {
        if (Math.abs(velY) >= swipeVelocityThreshold || Math.abs(velX) >= swipeVelocityThreshold) {
            isExiting = true;
            flingXVelocity = velX;
            flingYVelocity = velY;

            flingCard();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_COMPOSE: {
                if (resultCode == RESULT_OK) {
                    onMessageSent();
                }
                break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (emojiKeyboardLayout.isEmojiKeyboardOpen()) {
            emojiKeyboardLayout.hideEmojiKeyboard();
            return;
        }
        super.onBackPressed();
    }

    private void onMomentChanged(Post moment) {
        if (moment == null) {
            return;
        }

        setUserDetails(moment);

        time.setText(TimeFormatter.formatMessageTime(time.getContext(), moment.timestamp));

        if (moment.isIncoming()) {
            chatInputView.setVisibility(View.VISIBLE);
            avatarsLayout.setVisibility(View.GONE);
            moreOptions.setVisibility(View.GONE);
            moreOptions.setOnClickListener(null);
            shareExternallyView.setVisibility(View.GONE);
            shareExternallyTitle.setVisibility(View.GONE);
            if (moment.isAllMediaTransferred()) {
                viewModel.setLoaded();
            }
        } else {
            chatInputView.setVisibility(View.GONE);
            avatarsLayout.setVisibility(View.VISIBLE);
            moreOptions.setVisibility(View.VISIBLE);
            shareExternallyView.setVisibility(View.VISIBLE);
            shareExternallyTitle.setVisibility(View.VISIBLE);
            avatarsLayout.setAvatarCount(Math.min(moment.seenByCount, 3));
            seenByLoader.load(avatarsLayout, moment.id);
            viewModel.setLoaded();

            moreOptions.setOnClickListener(v -> {
                PostOptionsBottomSheetDialogFragment dialogFragment = PostOptionsBottomSheetDialogFragment.newInstance(moment.id, moment.isArchived);
                DialogFragmentUtils.showDialogFragmentOnce(dialogFragment, getSupportFragmentManager());
            });
        }

        updateViewUnlockState();
    }

    private void setUserDetails(@NonNull Post post) {
        avatarLoader.cancel(avatar);
        contactLoader.cancel(name);

        avatarLoader.load(avatar, post.senderUserId, false);

        if (post.isIncoming()) {
            contactLoader.load(name, post.senderUserId, new ViewDataLoader.Displayer<TextView, Contact>() {
                @Override
                public void showResult(@NonNull TextView view, @Nullable Contact result) {
                    if (result != null) {
                        boolean showTilde = TextUtils.isEmpty(post.psaTag);
                        name.setText(result.getDisplayName(showTilde));
                        chatInputView.getTextEntry().setHint(getString(R.string.reply_to_contact, result.getDisplayName(showTilde)));
                    }
                }

                @Override
                public void showLoading(@NonNull TextView view) {
                    name.setText("");
                }
            });
        } else {
            name.setText(R.string.me);
        }
    }

    private void updateViewUnlockState() {
        Post post = viewModel.getCurrent().getValue();
        Post unlockingPost = viewModel.unlockingMoment.getLiveData().getValue();

        boolean unlocked = false;
        if (post != null && post.isOutgoing()) {
            unlocked = true;
        } else if (unlockingPost != null && unlockingPost.transferred == Post.TRANSFERRED_YES && post != null && post.isAllMediaTransferred()) {
            unlocked = true;
        }
        if (unlocked) {
            topCardHolder.showCover(false);
            if (bottomCardHolder != null) {
                bottomCardHolder.showCover(false);
            }

            viewModel.setUncovered();
            uploadingProgress.setVisibility(View.GONE);
            uploadingDone.setVisibility(View.VISIBLE);
            if (uploadingContainer.getVisibility() == View.VISIBLE && uploadingCover.getVisibility() == View.VISIBLE) {
                fadeOutUploadingCover();
            } else if (uploadingContainer.getVisibility() == View.VISIBLE) {
                fadeOutUploadingContainer();
            }
        } else {
            if (unlockingPost != null) {
                if (uploadingContainer.isLaidOut()) {
                    TransitionManager.beginDelayedTransition(uploadingContainer);
                }
                uploadingContainer.setVisibility(View.VISIBLE);
            }

            topCardHolder.showCover(true);
            if (bottomCardHolder != null) {
                bottomCardHolder.showCover(true);
            }
        }
    }

    private void fadeOutUploadingCover() {
        Animation fade = new AlphaAnimation(1.0f, 0.0f);
        fade.setDuration(CHECK_FADE_ANIM_DURATION);
        fade.setStartOffset(CHECK_FADE_ANIM_DELAY);
        fade.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                uploadingCover.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        uploadingCover.startAnimation(fade);
    }

    private void fadeOutUploadingContainer() {
        Animation fade = new AlphaAnimation(1.0f, 0.0f);
        fade.setDuration(CHECK_FADE_ANIM_DURATION);
        fade.setStartOffset(CHECK_FADE_ANIM_DELAY);
        fade.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                uploadingContainer.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        uploadingContainer.startAnimation(fade);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (Boolean.TRUE.equals(viewModel.getVoiceNoteRecorder().isRecording().getValue())) {
            return super.dispatchTouchEvent(event);
        }
        return onTouchEventForSwipeExit(event) || super.dispatchTouchEvent(event);
    }

    private final Rect externallyRect = new Rect();

    private boolean onTouchEventForSwipeExit(MotionEvent event) {
        if (flingDetector != null) {
            flingDetector.onTouchEvent(event);
        }
        int action = event.getAction();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (event.getPointerCount() == 1) {
                    boolean shareExternallyVisible = shareExternallyView.getVisibility() == View.VISIBLE;
                    if (shareExternallyVisible) {
                        shareExternallyView.getGlobalVisibleRect(externallyRect);
                    }
                    if (shareExternallyVisible && externallyRect.contains((int)event.getX(), (int)event.getY())) {
                        swipeExitStart = null;
                    } else if (emojiKeyboardLayout.isEmojiKeyboardOpen()) {
                        if (event.getY() < emojiKeyboardLayout.getY()) {
                            swipeExitStart = MotionEvent.obtain(event);
                        } else {
                            swipeExitStart = null;
                        }
                    } else {
                        swipeExitStart = MotionEvent.obtain(event);
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (swipeExitStart != null && event.getPointerCount() > 1) {
                    cancelSwipeExit();
                } else if (isSwipeExitInProgress) {
                    onSwipeExitMove(event);
                } else if (swipeExitStart != null) {
                    float distanceX = Math.abs(event.getX() - swipeExitStart.getX());
                    float distanceY = Math.abs(event.getY() - swipeExitStart.getY());

                    if (distanceY > swipeExitStartThreshold || distanceX > swipeExitStartThreshold) {
                        isSwipeExitInProgress = true;
                        onSwipeExitMove(event);
                    }
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                cancelSwipeExit();
                break;
            case MotionEvent.ACTION_UP:
                if (!(isSwipeExitInProgress && isExiting)) {
                    cancelSwipeExit();
                }

                swipeExitStart = null;
                isSwipeExitInProgress = false;
                isExiting = false;
                break;
        }

        return isSwipeExitInProgress;
    }

    private void cancelSwipeExit() {
        if (swipeExitStart != null && isSwipeExitInProgress) {
            View card = topCardHolder.cardView;
            AnimatorSet set = new AnimatorSet();
            set.play(ObjectAnimator.ofFloat(card, "translationX", card.getTranslationX(), 0f))
                    .with(ObjectAnimator.ofFloat(card, "translationY", card.getTranslationY(), 0f))
                    .with(ObjectAnimator.ofFloat(card, "scaleX", card.getScaleX(), 1.0f))
                    .with(ObjectAnimator.ofFloat(card, "scaleY", card.getScaleY(), 1.0f));
            set.setDuration(300);
            set.start();
        }
    }

    private void flingCard() {
        if (swipeExitStart != null && isSwipeExitInProgress) {
            View card = topCardHolder.cardView;
            int durationMs = 300;
            float destX = card.getTranslationX() + ((durationMs / 1000f) * flingXVelocity);
            float destY = card.getTranslationX() + ((durationMs / 1000f) * flingYVelocity);
            AnimatorSet set = new AnimatorSet();
            set.play(ObjectAnimator.ofFloat(card, "translationX", card.getTranslationX(), destX))
                    .with(ObjectAnimator.ofFloat(card, "translationY", card.getTranslationY(), destY));
            set.setDuration(durationMs);
            set.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    nextCard();
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    nextCard();
                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            set.start();
        }

        swipeExitStart = null;
        isSwipeExitInProgress = false;
    }

    private void onSwipeExitMove(MotionEvent event) {
        if (swipeExitStart != null && isSwipeExitInProgress) {
            final float swipeExitScale = 0.95f;

            float distanceX = event.getX() - swipeExitStart.getX();
            float distanceY = event.getY() - swipeExitStart.getY();
            float progress = Math.min((distanceX * distanceX + distanceY * distanceY ) / (swipeExitTransDistance * swipeExitTransDistance), 1.0f);
            float scale = 1 - progress + swipeExitScale * progress;

            View card = topCardHolder.cardView;
            card.setTranslationX(distanceX);
            card.setTranslationY(distanceY);
            card.setScaleX(scale);
            card.setScaleY(scale);
        }
    }

    private void nextCard() {
        if (viewModel.getMomentCount() > 1) {
            topCardHolder.cardView.setTranslationX(0);
            topCardHolder.cardView.setTranslationY(0);
            topCardHolder.cardView.setScaleX(1);
            topCardHolder.cardView.setScaleY(1);
            topCardHolder.cardView.setVisibility(View.INVISIBLE);

            MomentCardHolder tmp = topCardHolder;
            topCardHolder = bottomCardHolder;
            bottomCardHolder = tmp;

            topCardHolder.cardView.bringToFront();

            TransitionManager.beginDelayedTransition(topCardHolder.cardView);
            topCardHolder.cardView.setVisibility(View.VISIBLE);

            viewModel.moveToNext();
        } else {
            finish();
            overridePendingTransition(0, android.R.anim.fade_out);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        contactLoader.destroy();
        seenByLoader.destroy();
        screenshotDetector.stop();
        screenshotHandlerThread.quit();
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (requestCode == REQUEST_PERMISSIONS_RECORD_VOICE_NOTE) {
            if (EasyPermissions.permissionPermanentlyDenied(MomentViewerActivity.this, Manifest.permission.RECORD_AUDIO)) {
                new AppSettingsDialog.Builder(MomentViewerActivity.this)
                        .setRationale(getString(R.string.voice_note_record_audio_permission_rationale_denied))
                        .build().show();
            }
        }
    }

    private class MomentCardHolder {
        public final CardView cardView;

        private final ImageView imageViewFirst;
        private final ImageView imageViewSecond;
        private final View imageDivider;
        private final View imageViewContainer;
        private final View downloadProgressView;
        private final TextView lineOneView;
        private final View coverView;

        private MomentPost moment;

        MomentCardHolder(CardView cardView) {
            this.cardView = cardView;

            imageViewFirst = cardView.findViewById(R.id.image_first);
            imageViewSecond = cardView.findViewById(R.id.image_second);
            imageDivider = cardView.findViewById(R.id.image_divider);
            imageViewContainer = cardView.findViewById(R.id.image_container);
            downloadProgressView = cardView.findViewById(R.id.download_progress);
            lineOneView = cardView.findViewById(R.id.line_one);
            coverView = cardView.findViewById(R.id.moment_cover);

            imageViewContainer.setClipToOutline(true);
            imageViewContainer.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), mediaRadius);
                }
            });
        }

        void bindTo(@NonNull Post post) {
            if (!shouldUpdate(post)) {
                return;
            }
            moment = (MomentPost) post;

            if (post.isOutgoing() || post.isAllMediaTransferred()) {
                downloadProgressView.setVisibility(View.GONE);

                imageViewFirst.setVisibility(View.VISIBLE);
                fullThumbnailLoader.load(imageViewFirst, post.getMedia().get(0));

                if (post.getMedia().size() > 1) {
                    imageDivider.setVisibility(View.VISIBLE);
                    imageViewSecond.setVisibility(View.VISIBLE);
                    fullThumbnailLoader.load(imageViewSecond, post.getMedia().get(1));
                } else {
                    imageDivider.setVisibility(View.GONE);
                    imageViewSecond.setVisibility(View.GONE);
                }
            } else {
                imageDivider.setVisibility(View.GONE);
                imageViewFirst.setVisibility(View.GONE);
                imageViewSecond.setVisibility(View.GONE);
                downloadProgressView.setVisibility(View.VISIBLE);
            }

            if (!TextUtils.isEmpty(moment.location)) {
                lineOneView.setText(moment.location);
            } else {
                lineOneView.setText(dayFormatter.format(new Date(post.timestamp)));
            }
        }

        void showCover(boolean show) {
            coverView.setVisibility(show ? View.VISIBLE : View.GONE);
        }

        private boolean shouldUpdate(@NonNull Post post) {
            return moment == null ||
                   !moment.id.equals(post.id) ||
                   moment.seenByCount != post.seenByCount ||
                   moment.isAllMediaTransferred() != post.isAllMediaTransferred();
        }
    }
}
