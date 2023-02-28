package com.halloapp.katchup;

import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;

import com.google.android.material.button.MaterialButton;
import com.halloapp.Preferences;
import com.halloapp.R;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactLoader;
import com.halloapp.content.KatchupPost;
import com.halloapp.content.Media;
import com.halloapp.content.MomentManager;
import com.halloapp.content.MomentUnlockStatus;
import com.halloapp.content.Post;
import com.halloapp.id.UserId;
import com.halloapp.katchup.avatar.KAvatarLoader;
import com.halloapp.katchup.media.KatchupExoPlayer;
import com.halloapp.katchup.media.ExternalSelfieLoader;
import com.halloapp.katchup.ui.LateEmoji;
import com.halloapp.media.MediaThumbnailLoader;
import com.halloapp.ui.BlurManager;
import com.halloapp.ui.ViewHolderWithLifecycle;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.TimeFormatter;
import com.halloapp.util.ViewDataLoader;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.ContentPlayerView;
import com.halloapp.widget.SnackbarHelper;
import com.halloapp.xmpp.util.Observable;

import java.util.Locale;

import eightbitlab.com.blurview.BlurView;

class KatchupPostViewHolder extends ViewHolderWithLifecycle {
    private static final int LATE_THRESHOLD_MS = 120 * 1000;

    private final ImageView imageView;
    private final View selfieContainer;
    private ImageView selfiePreview;
    private ContentPlayerView selfieView;
    private KatchupExoPlayer selfiePlayer;
    private final View headerView;
    private final ImageView headerAvatarView;
    private final TextView headerTextView;
    private final View headerFollowButton;
    private final View unlockContainer;
    private final TextView unlockMainTextView;
    private final MaterialButton unlockButton;
    private final View avatarContainer;
    private final ImageView avatarView;
    private final TextView serverScoreView;

    private final BlurView blurView;
    private final CountingCommentBubble commentView;

    public Post post;
    private boolean inStack;
    private boolean isPublic;
    public boolean seenReceiptSent;

    private final Observer<MomentUnlockStatus> unlockedObserver;
    private boolean unlocked;

    private KatchupViewHolderParent parent;

    public abstract static class KatchupViewHolderParent {
        public abstract ContactLoader getContactLoader();
        public abstract MediaThumbnailLoader getMediaThumbnailLoader();
        public abstract MediaThumbnailLoader getExternalMediaThumbnailLoader();
        public abstract ExternalSelfieLoader getExternalSelfieLoader();
        public abstract KAvatarLoader getAvatarLoader();
        public abstract void startActivity(Intent intent);
        public abstract void startComposerActivity();
        public abstract Observable<Boolean> followUser(UserId userId);
        public abstract boolean wasUserFollowed(UserId userId);
    }

    public KatchupPostViewHolder(@NonNull View itemView, KatchupViewHolderParent parent) {
        super(itemView);
        this.parent = parent;

        imageView = itemView.findViewById(R.id.image);
        selfieContainer = itemView.findViewById(R.id.selfie_container);
        headerView = itemView.findViewById(R.id.moment_header);
        headerAvatarView = itemView.findViewById(R.id.header_avatar);
        headerTextView = itemView.findViewById(R.id.header_text);
        headerFollowButton = itemView.findViewById(R.id.follow_button);
        unlockContainer = itemView.findViewById(R.id.unlock_container);
        unlockMainTextView = itemView.findViewById(R.id.unlock_main_text);
        unlockButton = itemView.findViewById(R.id.unlock);
        avatarContainer = itemView.findViewById(R.id.avatar_container);
        avatarView = itemView.findViewById(R.id.avatar);
        commentView = itemView.findViewById(R.id.comments);
        serverScoreView = itemView.findViewById(R.id.server_score);
        selfieView = itemView.findViewById(R.id.selfie_player);
        selfiePreview = itemView.findViewById(R.id.selfie_preview);

        ViewGroup blurContent = itemView.findViewById(R.id.content);
        blurView = itemView.findViewById(R.id.blur_view);
        BlurManager.getInstance().setupMomentBlur(blurView, blurContent);

        headerFollowButton.setOnClickListener(v -> {
            UserId userIdToFollow = post.senderUserId;
            parent.followUser(userIdToFollow).onResponse(success -> {
                if (Boolean.TRUE.equals(success)) {
                    if (userIdToFollow.equals(post.senderUserId)) {
                        headerFollowButton.post(() -> headerFollowButton.setVisibility(View.GONE));
                    }
                } else {
                    SnackbarHelper.showWarning(headerFollowButton, R.string.failed_to_follow);
                }
            }).onError(e -> SnackbarHelper.showWarning(headerFollowButton, R.string.failed_to_follow));
        });

        View.OnClickListener listener = v -> {
            if (!unlocked) {
                Analytics.getInstance().tappedLockedPost();
                parent.startComposerActivity();
            } else {
                parent.startActivity(ViewKatchupCommentsActivity.viewPost(unlockButton.getContext(), post, isPublic));
            }
        };

        commentView.setOnClickListener(listener);
        unlockButton.setOnClickListener(listener);
        itemView.findViewById(R.id.card_view).setOnClickListener(listener);

        headerView.setOnClickListener(v -> parent.startActivity(ViewKatchupProfileActivity.viewProfile(headerView.getContext(), post.senderUserId)));

        Drawable lockedIcon = ContextCompat.getDrawable(unlockButton.getContext(), R.drawable.ic_eye_slash);
        unlockedObserver = unlockStatus -> {
            unlocked = unlockStatus.isUnlocked();
            unlockButton.setIcon(unlocked ? null : lockedIcon);
            commentView.setAlpha(unlocked ? 1f : 0.4f);
            if (post != null) {
                bindTo(post, inStack, isPublic);
            }
        };
    }

    private void unbindSelfie() {
        if (selfiePlayer != null) {
            selfiePlayer.destroy();
        }
    }

    private void bindSelfie(Media selfie) {
        if (selfie.file != null) {
            selfiePlayer = KatchupExoPlayer.forSelfieView(selfieView, selfie);
        } else {
            Log.e("KatchupPostViewHolder: got null file for " + selfie);
        }
    }

    public void bindTo(@NonNull Post post, boolean inStack) {
        bindTo(post, inStack, false);
    }

    public void bindTo(@NonNull Post post, boolean inStack, boolean isPublic) {
        this.post = post;
        this.commentView.setCommentCount(post.commentCount);
        this.isPublic = isPublic;
        this.inStack = inStack;
        MediaThumbnailLoader mediaThumbnailLoader = isPublic ? parent.getExternalMediaThumbnailLoader() : parent.getMediaThumbnailLoader();
        if (post.media.size() > 1) {
            mediaThumbnailLoader.load(imageView, post.media.get(1));
        }
        headerView.setVisibility(inStack ? View.GONE : View.VISIBLE);
        headerFollowButton.setVisibility(isPublic && !parent.wasUserFollowed(post.senderUserId) ? View.VISIBLE : View.GONE);
        avatarContainer.setVisibility(inStack ? View.VISIBLE : View.GONE);
        unlockContainer.setVisibility(inStack || !unlocked ? View.VISIBLE : View.GONE);
        blurView.setVisibility(inStack || !unlocked ? View.VISIBLE : View.GONE);
        parent.getAvatarLoader().load(headerAvatarView, post.senderUserId);
        parent.getAvatarLoader().load(avatarView, post.senderUserId);
        parent.getContactLoader().load(unlockMainTextView, post.senderUserId, new ViewDataLoader.Displayer<TextView, Contact>() {
            @Override
            public void showResult(@NonNull TextView view, @Nullable Contact result) {
                if (result != null) {
                    String shortName = result.username == null ? "" : result.username.toLowerCase(Locale.getDefault());
                    if (inStack && unlocked) {
                        view.setText(view.getContext().getString(R.string.new_from, shortName));
                    } else {
                        view.setText(view.getContext().getString(R.string.post_to_see, shortName));
                    }

                    SpannableStringBuilder headerText = new SpannableStringBuilder();
                    SpannableString name = new SpannableString(shortName);
                    name.setSpan(new StyleSpan(Typeface.BOLD), 0, shortName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    headerText.append(name);
                    if (post instanceof KatchupPost) {
                        headerText.append(" ");
                        long lateMs = post.timestamp - ((KatchupPost) post).notificationTimestamp;
                        if (lateMs > LATE_THRESHOLD_MS) {
                            CharSequence timeText = TimeFormatter.formatLate(headerView.getContext(), (int) (lateMs / 1000));
                            SpannableString time = new SpannableString(timeText);
                            time.setSpan(new ForegroundColorSpan(headerTextView.getResources().getColor(R.color.black_40)), 0, timeText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            headerText.append(time);
                            headerText.append(" ");
                            headerText.append(LateEmoji.getLateEmoji(post.id));
                        } else {
                            CharSequence timeText = TimeFormatter.formatMessageTime(headerTextView.getContext(), post.timestamp);
                            SpannableString time = new SpannableString(timeText);
                            time.setSpan(new ForegroundColorSpan(headerTextView.getResources().getColor(R.color.black_40)), 0, timeText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            headerText.append(time);
                        }

                        String location = ((KatchupPost) post).location;
                        if (location != null) {
                            headerText.append(" ");
                            String locText = headerTextView.getContext().getString(R.string.moment_location, location.toLowerCase(Locale.getDefault()));
                            SpannableString loc = new SpannableString(locText);
                            loc.setSpan(new ForegroundColorSpan(headerTextView.getResources().getColor(R.color.black_40)), 0, locText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            headerText.append(loc);
                        }
                    }
                    headerTextView.setText(headerText);
                }
            }

            @Override
            public void showLoading(@NonNull TextView view) {
                view.setText("");
            }
        });

        if (post instanceof KatchupPost) {
            Media selfieMedia = post.media.get(0);
            if (selfieMedia == null) {
                Log.w("Got null selfie media for " + post);
                Log.sendErrorReport("Null Selfie Media");
            } else {
                if (unlocked && !inStack) {
                    unbindSelfie();
                    if (isPublic) {
                        parent.getExternalSelfieLoader().load(selfieView, selfieMedia, new ViewDataLoader.Displayer<ContentPlayerView, Media>() {
                            @Override
                            public void showResult(@NonNull ContentPlayerView view, @Nullable Media result) {
                                if (result != null) {
                                    bindSelfie(result);
                                    selfiePreview.setVisibility(View.GONE);
                                    selfieView.setVisibility(View.VISIBLE);
                                }
                            }

                            @Override
                            public void showLoading(@NonNull ContentPlayerView view) {

                            }
                        });
                    } else {
                        bindSelfie(selfieMedia);
                        selfiePreview.setVisibility(View.GONE);
                        selfieView.setVisibility(View.VISIBLE);
                    }
                } else {
                    mediaThumbnailLoader.load(selfiePreview, selfieMedia);
                    selfiePreview.setVisibility(View.VISIBLE);
                    selfieView.setVisibility(View.GONE);
                }
            }
            serverScoreView.setText(((KatchupPost) post).serverScore);
            serverScoreView.setVisibility(Preferences.getInstance().getShowServerScore() ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void markAttach() {
        super.markAttach();
        MomentManager.getInstance().isUnlockedLiveData().observe(this, unlockedObserver);
        if (selfiePlayer != null && unlocked && !inStack) {
            selfiePlayer.play();
        }
    }

    @Override
    public void markDetach() {
        super.markDetach();
        seenReceiptSent = false;
        MomentManager.getInstance().isUnlockedLiveData().removeObserver(unlockedObserver);
        if (selfiePlayer != null) {
            selfiePlayer.pause();
        }
    }
}
