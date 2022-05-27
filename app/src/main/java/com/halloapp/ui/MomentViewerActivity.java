package com.halloapp.ui;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityOptionsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.transition.TransitionManager;

import com.halloapp.Constants;
import com.halloapp.R;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactLoader;
import com.halloapp.content.Post;
import com.halloapp.emoji.EmojiKeyboardLayout;
import com.halloapp.media.MediaThumbnailLoader;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.ui.posts.SeenByLoader;
import com.halloapp.util.KeyboardUtils;
import com.halloapp.util.Preconditions;
import com.halloapp.util.TimeFormatter;
import com.halloapp.util.VibrationUtils;
import com.halloapp.util.ViewDataLoader;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.AvatarsLayout;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MomentViewerActivity extends HalloActivity {

    public static final String MOMENT_TRANSITION_NAME = "moment-transition-image";
    private static final String EXTRA_MOMENT_POST_ID = "moment_post_id";
    private static final String EXTRA_USING_SHARED_TRANSITION = "using_shared_transition";

    private static final int CHECK_FADE_ANIM_DELAY = 1000;
    private static final int CHECK_FADE_ANIM_DURATION = 300;

    private static final int FLING_DISMISS_THRESHOLD = 1000;

    public static Intent viewMoment(@NonNull Context context, @NonNull String postId) {
        Intent i = new Intent(context, MomentViewerActivity.class);
        i.putExtra(EXTRA_MOMENT_POST_ID, postId);

        return i;
    }

    public static void viewMomentWithTransition(@NonNull Activity activity, @NonNull String postId, @NonNull ImageView photoView) {
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

    private View cover;
    private ViewGroup content;
    private LinearLayout uploadingContainer;

    private View uploadingCover;
    private View uploadingDone;
    private View uploadingProgress;

    private View card;

    private EmojiKeyboardLayout emojiKeyboardLayout;
    private GestureDetector flingDetector;


    private float flingXVelocity;
    private float flingYVelocity;

    private int swipeVelocityThreshold;
    private int swipeExitStartThreshold;
    private float swipeExitTransDistance;

    private MotionEvent swipeExitStart;
    private boolean isSwipeExitInProgress = false;
    private boolean isExiting = false;
    private boolean usingSharedTransition = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_moment_viewer);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        setTitle("");

        String postId = getIntent().getStringExtra(EXTRA_MOMENT_POST_ID);
        usingSharedTransition = getIntent().getBooleanExtra(EXTRA_USING_SHARED_TRANSITION, false);

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
        contactLoader = new ContactLoader();
        seenByLoader = new SeenByLoader();
        avatarLoader = AvatarLoader.getInstance();

        viewModel = new ViewModelProvider(this, new MomentViewerViewModel.Factory(postId)).get(MomentViewerViewModel.class);

        SimpleDateFormat dayFormatter = new SimpleDateFormat("EEEE", Locale.getDefault());
        ImageView imageView = findViewById(R.id.image);
        imageView.setTransitionName(MOMENT_TRANSITION_NAME);
        final ImageView avatar = findViewById(R.id.avatar);
        final TextView name = findViewById(R.id.name);
        final TextView time = findViewById(R.id.time);
        card = findViewById(R.id.card_view);
        TextView lineOne = findViewById(R.id.line_one);
        TextView lineTwo = findViewById(R.id.line_two);
        content = findViewById(R.id.content);

        Resources r = getResources();
        float density = r.getDisplayMetrics().density;
        swipeVelocityThreshold = (int)(FLING_DISMISS_THRESHOLD * density);

        uploadingCover = findViewById(R.id.uploading_cover);
        uploadingDone = findViewById(R.id.uploaded_check);
        uploadingProgress = findViewById(R.id.uploaded_progress);

        AvatarsLayout avatarsLayout = findViewById(R.id.seen_indicator);
        avatarsLayout.setAvatarLoader(avatarLoader);
        avatarsLayout.setAvatarCount(3);
        avatarsLayout.setOnClickListener(v -> {
            final Intent intent = new Intent(MomentViewerActivity.this, PostSeenByActivity.class);
            Post post = viewModel.post.getLiveData().getValue();
            if (post != null) {
                intent.putExtra(PostSeenByActivity.EXTRA_POST_ID, post.id);
                startActivity(intent);
            } else {
                Log.i("MomentViewerActivity/seenOnClick null post");
            }
        });
        EditText textEntry = findViewById(R.id.entry);
        View textEntryContainer = findViewById(R.id.text_entry);
        View sendBtn = findViewById(R.id.bottom_composer_send);

        ImageView emojiBtn = findViewById(R.id.kb_toggle);
        emojiKeyboardLayout = findViewById(R.id.emoji_keyboard);
        emojiKeyboardLayout.bind(emojiBtn, textEntry);
        sendBtn.setEnabled(false);
        uploadingContainer = findViewById(R.id.uploading_container);
        textEntry.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (TextUtils.isEmpty(s)) {
                    sendBtn.setEnabled(false);
                } else {
                    sendBtn.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        ImageView uploadingMomentImageView = findViewById(R.id.uploading_moment_image);
        sendBtn.setOnClickListener(v -> {
            String text = textEntry.getText().toString();
            if (TextUtils.isEmpty(text)) {
                Log.i("MomentViewerActivity/sendMessage no text");
                return;
            }
            viewModel.sendMessage(textEntry.getText().toString());
            textEntry.setText("");
            Toast.makeText(sendBtn.getContext(), R.string.private_reply_sent, Toast.LENGTH_SHORT).show();
            KeyboardUtils.hideSoftKeyboard(textEntry);
            VibrationUtils.quickVibration(MomentViewerActivity.this);
        });
        cover = findViewById(R.id.moment_cover);
        viewModel.unlockingMoment.getLiveData().observe(this, unlockingMoment -> {
            updateViewUnlockState();
            if (unlockingMoment == null) {
                fullThumbnailLoader.cancel(imageView);
                return;
            }
            fullThumbnailLoader.load(uploadingMomentImageView, unlockingMoment.getMedia().get(0));
        });
        viewModel.post.getLiveData().observe(this, post -> {
            updateViewUnlockState();
            if (post != null) {
                fullThumbnailLoader.load(imageView, post.getMedia().get(0));
                avatarLoader.load(avatar, post.senderUserId, false);
                if (post.isIncoming()) {
                    contactLoader.load(name, post.senderUserId, new ViewDataLoader.Displayer<TextView, Contact>() {
                        @Override
                        public void showResult(@NonNull TextView view, @Nullable Contact result) {
                            if (result != null) {
                                name.setText(result.getDisplayName());
                                textEntry.setHint(getString(R.string.reply_to_contact, result.getDisplayName()));
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
                time.setText(TimeFormatter.formatMessageTime(time.getContext(), post.timestamp));
                lineTwo.setText(TimeFormatter.formatMessageTime(lineOne.getContext(), post.timestamp));
                lineOne.setText(dayFormatter.format(new Date(post.timestamp)));
                viewModel.setLoaded();
                if (post.isIncoming()) {
                    textEntryContainer.setVisibility(View.VISIBLE);
                    avatarsLayout.setVisibility(View.INVISIBLE);
                } else {
                    textEntryContainer.setVisibility(View.INVISIBLE);
                    avatarsLayout.setVisibility(View.VISIBLE);
                    avatarsLayout.setAvatarCount(Math.min(post.seenByCount, 3));
                    seenByLoader.load(avatarsLayout, postId);
                }
            } else {
                fullThumbnailLoader.cancel(imageView);
                avatarLoader.cancel(avatar);
                contactLoader.cancel(name);
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

    private void onFlingWithVelocity(float velX, float velY) {
        if (Math.abs(velY) >= swipeVelocityThreshold) {
            isExiting = true;
            flingXVelocity = velX;
            flingYVelocity = velY;
            if (!usingSharedTransition) {
                flingCard();
            } else {
                super.onBackPressed();
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

    private void updateViewUnlockState() {
        Post post = viewModel.post.getLiveData().getValue();
        Post unlockingPost = viewModel.unlockingMoment.getLiveData().getValue();

        boolean unlocked = false;
        if (post != null && post.isOutgoing()) {
            unlocked = true;
        } else if (unlockingPost != null && unlockingPost.transferred == Post.TRANSFERRED_YES) {
            unlocked = true;
        }
        if (unlocked) {
            cover.setVisibility(View.GONE);
            viewModel.setUncovered();
            uploadingProgress.setVisibility(View.GONE);
            uploadingDone.setVisibility(View.VISIBLE);
            if (uploadingContainer.getVisibility() == View.VISIBLE && uploadingCover.getVisibility() == View.VISIBLE) {
                fadeOutUploadingCover();
            }
        } else {
            if (unlockingPost != null) {
                if (uploadingContainer.isLaidOut()) {
                    TransitionManager.beginDelayedTransition(uploadingContainer);
                }
                uploadingContainer.setVisibility(View.VISIBLE);
            }
            cover.setVisibility(View.VISIBLE);
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

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        return onTouchEventForSwipeExit(event) || super.dispatchTouchEvent(event);
    }

    private boolean onTouchEventForSwipeExit(MotionEvent event) {
        if (flingDetector != null) {
            flingDetector.onTouchEvent(event);
        }
        int action = event.getAction();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (event.getPointerCount() == 1) {
                    swipeExitStart = MotionEvent.obtain(event);
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

                    if (distanceY > swipeExitStartThreshold && distanceY > distanceX) {
                        isSwipeExitInProgress = true;
                        onSwipeExitMove(event);
                    }
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                cancelSwipeExit();
                break;
            case MotionEvent.ACTION_UP:
                if (swipeExitStart != null) {
                    if (!(isSwipeExitInProgress && isExiting)) {
                        cancelSwipeExit();
                    }
                }
                break;
        }

        return isSwipeExitInProgress;
    }

    private void cancelSwipeExit() {
        if (swipeExitStart != null && isSwipeExitInProgress) {
            View main = findViewById(R.id.main);
            AnimatorSet set = new AnimatorSet();
            set.play(ObjectAnimator.ofFloat(main, "alpha", main.getAlpha(), 1.0f))
                    .with(ObjectAnimator.ofFloat(card, "translationX", card.getTranslationX(), 0f))
                    .with(ObjectAnimator.ofFloat(card, "translationY", card.getTranslationY(), 0f))
                    .with(ObjectAnimator.ofFloat(card, "scaleX", card.getScaleX(), 1.0f))
                    .with(ObjectAnimator.ofFloat(card, "scaleY", card.getScaleY(), 1.0f));
            set.setDuration(300);
            set.start();
        }

        swipeExitStart = null;
        isSwipeExitInProgress = false;
    }

    private void flingCard() {
        if (swipeExitStart != null && isSwipeExitInProgress) {
            View main = findViewById(R.id.main);
            int durationMs = 300;
            float destX = card.getTranslationX() + ((durationMs / 1000f) * flingXVelocity);
            float destY = card.getTranslationX() + ((durationMs / 1000f) * flingYVelocity);
            AnimatorSet set = new AnimatorSet();
            set.play(ObjectAnimator.ofFloat(main, "alpha", main.getAlpha(), 0.0f))
                    .with(ObjectAnimator.ofFloat(card, "translationX", card.getTranslationX(), destX))
                    .with(ObjectAnimator.ofFloat(card, "translationY", card.getTranslationY(), destY));
            set.setDuration(durationMs);
            set.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    finish();
                    overridePendingTransition(0, android.R.anim.fade_out);
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    finish();
                    overridePendingTransition(0, android.R.anim.fade_out);
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
            final float swipeExitAlpha = 0.3f;

            float distanceX = event.getX() - swipeExitStart.getX();
            float distanceY = event.getY() - swipeExitStart.getY();
            float progress = Math.min((distanceX * distanceX + distanceY * distanceY ) / (swipeExitTransDistance * swipeExitTransDistance), 1.0f);
            float scale = 1 - progress + swipeExitScale * progress;
            int alpha = (int)((255) * (1 - progress + swipeExitAlpha * progress));

            View view = card;
            view.setTranslationX(distanceX);
            view.setTranslationY(distanceY);
            view.setScaleX(scale);
            view.setScaleY(scale);

            View main = findViewById(R.id.main);
            main.setAlpha(alpha / 255f);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        contactLoader.destroy();
        seenByLoader.destroy();
    }
}
