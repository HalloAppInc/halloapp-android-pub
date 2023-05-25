package com.halloapp.katchup;

import android.content.Intent;
import android.graphics.Outline;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;

import com.google.android.exoplayer2.Player;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
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
import com.halloapp.katchup.ui.CountingCommentBubble;
import com.halloapp.katchup.ui.CountingLikeButton;
import com.halloapp.katchup.ui.ReactionTooltipPopupWindow;
import com.halloapp.media.MediaThumbnailLoader;
import com.halloapp.ui.BlurManager;
import com.halloapp.ui.ViewHolderWithLifecycle;
import com.halloapp.util.TimeFormatter;
import com.halloapp.util.ViewDataLoader;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.ContentPlayerView;
import com.halloapp.widget.SnackbarHelper;
import com.halloapp.xmpp.util.Observable;

import java.util.Locale;

import eightbitlab.com.blurview.BlurView;

class KatchupPostViewHolder extends ViewHolderWithLifecycle {
    private static final String ON_TIME_SUFFIX = " \uD83D\uDDA4";

    private final ImageView imageView;
    private final View selfieContainer;
    private ImageView selfiePreview;
    private ContentPlayerView selfieView;
    private KatchupExoPlayer selfiePlayer;
    private final View headerView;
    private final ImageView headerAvatarView;
    private final TextView headerUsername;
    private final TextView headerGeotag;
    private final TextView headerTimeAndPlace;
    private final TextView headerFollowButton;
    private final View unlockContainer;
    private final TextView unlockMainTextView;
    private final MaterialButton unlockButton;
    private final View avatarContainer;
    private final ImageView avatarView;
    private final TextView serverScoreView;
    private final MaterialCardView cardView;
    private final View cardContent;
    private final View entryContainer;
    private final View entryEmoticon;
    private final View entryText;

    private View uploadingProgressView;
    private View contentLoadingView;
    private View selfieLoadingView;
    private final BlurView blurView;
    private final CountingCommentBubble commentView;;
    private final CountingLikeButton likeView;

    public Post post;
    private boolean inStack;
    private boolean isPublic;
    public boolean seenReceiptSent;

    private final Observer<MomentUnlockStatus> unlockedObserver;
    private boolean unlocked;
    private boolean unlocking;

    private KatchupViewHolderParent parent;

    private final View recordVideoReaction;
    private ReactionTooltipPopupWindow reactionTooltipPopupWindow;

    // This is a workaround for the content bleeding out past the edges of the card view. It is a known open issue:
    // https://github.com/material-components/material-components-android/issues/881
    // None of the workarounds suggested there or this stack overflow post work for our situation:
    // https://stackoverflow.com/questions/35369691/how-to-add-colored-border-on-cardview
    // Instead we use the following outline provider to make sure the content does not reach out to where
    // the anti-aliasing will be performed for the border.
    private final ViewOutlineProvider insetRoundedOutlineProvider = new ViewOutlineProvider() {
        private static final int OUTLINE_INSET_DP = 1;
        @Override
        public void getOutline(View view, Outline outline) {
            int inset = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, OUTLINE_INSET_DP, view.getResources().getDisplayMetrics());
            float radius = view.getResources().getDimension(R.dimen.post_card_radius);
            outline.setRoundRect(inset, inset, view.getWidth() - inset, view.getHeight() - inset, radius);
        }
    };

    public abstract static class KatchupViewHolderParent {
        public abstract ContactLoader getContactLoader();
        public abstract MediaThumbnailLoader getMediaThumbnailLoader();
        public abstract MediaThumbnailLoader getExternalMediaThumbnailLoader();
        public abstract ExternalSelfieLoader getExternalSelfieLoader();
        public abstract KAvatarLoader getAvatarLoader();
        public abstract GeotagLoader getGeotagLoader();
        public abstract void startActivity(Intent intent);
        public abstract void startComposerActivity();
        public abstract Observable<Boolean> followUser(UserId userId);
        public abstract boolean wasUserFollowed(UserId userId);
        public abstract boolean videoReactionTouchCallback(int screenYPosition, View view, MotionEvent motionEvent, Post post, boolean isPublic);
        public abstract void toggleLike(@NonNull Post post, boolean isPublic);
    }

    public KatchupPostViewHolder(@NonNull View itemView, KatchupViewHolderParent parent) {
        super(itemView);
        this.parent = parent;

        imageView = itemView.findViewById(R.id.image);
        selfieContainer = itemView.findViewById(R.id.selfie_container);
        headerView = itemView.findViewById(R.id.moment_header);
        headerAvatarView = itemView.findViewById(R.id.header_avatar);
        headerUsername = itemView.findViewById(R.id.header_username);
        headerGeotag = itemView.findViewById(R.id.header_geotag);
        headerTimeAndPlace = itemView.findViewById(R.id.header_time_and_place);
        headerFollowButton = itemView.findViewById(R.id.follow_button);
        unlockContainer = itemView.findViewById(R.id.unlock_container);
        unlockMainTextView = itemView.findViewById(R.id.unlock_main_text);
        unlockButton = itemView.findViewById(R.id.unlock);
        avatarContainer = itemView.findViewById(R.id.avatar_container);
        avatarView = itemView.findViewById(R.id.avatar);
        likeView = itemView.findViewById(R.id.likes);
        commentView = itemView.findViewById(R.id.comments);
        serverScoreView = itemView.findViewById(R.id.server_score);
        selfieView = itemView.findViewById(R.id.selfie_player);
        selfiePreview = itemView.findViewById(R.id.selfie_preview);
        cardView = itemView.findViewById(R.id.card_view);
        cardContent = itemView.findViewById(R.id.card_content);
        uploadingProgressView = itemView.findViewById(R.id.uploading_progress);
        contentLoadingView = itemView.findViewById(R.id.content_loading);
        selfieLoadingView = itemView.findViewById(R.id.selfie_loading);
        entryContainer = itemView.findViewById(R.id.entry_container);
        entryEmoticon = itemView.findViewById(R.id.kb_toggle);
        entryText = itemView.findViewById(R.id.entry_card);

        recordVideoReaction = itemView.findViewById(R.id.video_reaction_record_button);

        ViewGroup blurContent = itemView.findViewById(R.id.content);
        blurView = itemView.findViewById(R.id.blur_view);
        BlurManager.getInstance().setupMomentBlur(blurView, blurContent);

        headerGeotag.setOnClickListener(v -> {
            new GeotagPopupWindow(headerGeotag.getContext(), false, headerUsername.getText().toString(), headerGeotag.getText().toString(), null).show(headerGeotag);
        });

        headerFollowButton.setText(" · " + headerFollowButton.getContext().getString(R.string.follow_profile));
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
            if (!unlocked && !unlocking) {
                Analytics.getInstance().tappedLockedPost();
                parent.startComposerActivity();
            } else if (!unlocking) {
                parent.startActivity(ViewKatchupCommentsActivity.viewPost(unlockButton.getContext(), post.id, null, false, inStack, false, v.equals(entryEmoticon) || v.equals(entryText), v.equals(commentView)));
            }
        };

        likeView.setOnClickListener(v -> {
            if (!unlocked && !unlocking) {
                Analytics.getInstance().tappedLockedPost();
                parent.startComposerActivity();
            } else if (!unlocking) {
                parent.toggleLike(post, isPublic);
            }
        });

        commentView.setOnClickListener(listener);
        unlockButton.setOnClickListener(listener);
        cardView.setOnClickListener(listener);
        entryEmoticon.setOnClickListener(listener);
        entryText.setOnClickListener(listener);

        cardContent.setClipToOutline(true);
        cardContent.setOutlineProvider(insetRoundedOutlineProvider);

        headerView.setOnClickListener(v -> parent.startActivity(ViewKatchupProfileActivity.viewProfile(headerView.getContext(), post.senderUserId)));

        unlockedObserver = unlockStatus -> {
            unlocked = unlockStatus.isUnlocked();
            unlocking = unlockStatus.isUnlocking();
            unlockButton.setText(unlocked ? R.string.card_btn_view_katchup : R.string.card_btn_post_katchup);
            uploadingProgressView.setVisibility(unlocking ? View.VISIBLE : View.GONE);
            likeView.setAlpha(unlocked ? 1f : 0.4f);
            commentView.setAlpha(unlocked ? 1f : 0.4f);
            if (post != null) {
                bindTo(post, inStack, isPublic);
            }
        };

        final GestureDetector recordVideoReactionLockedDetector = new GestureDetector(recordVideoReaction.getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(@NonNull MotionEvent e) {
                Analytics.getInstance().tappedLockedPost();
                parent.startComposerActivity();
                return true;
            }
        });
        recordVideoReaction.setOnTouchListener((view, event) -> {
            if (!unlocked && !unlocking) {
                recordVideoReactionLockedDetector.onTouchEvent(event);
                return true;
            } else if (!unlocking) {
                final int screenLocation[] = new int[2];
                recordVideoReaction.getLocationOnScreen(screenLocation);
                return parent.videoReactionTouchCallback(screenLocation[1], view, event, post, isPublic);
            } else {
                return false;
            }
        });
    }

    private void unbindSelfie() {
        if (selfiePlayer != null) {
            selfiePlayer.destroy();
        }
    }

    private void bindSelfie(Media selfie) {
        if (selfie.file != null) {
            showSelfieLoading();
            selfiePlayer = KatchupExoPlayer.forSelfieView(selfieView, selfie);
            selfiePlayer.getPlayer().addListener(new Player.EventListener() {
                @Override
                public void onPlaybackStateChanged(int state) {
                    if (state == Player.STATE_READY) {
                        hideSelfieLoading();
                    }
                }
            });
        } else {
            Log.e("KatchupPostViewHolder: got null file for " + selfie);
        }
    }

    public void bindTo(@NonNull Post post, boolean inStack) {
        bindTo(post, inStack, false);
    }

    public void bindTo(@NonNull Post post, boolean inStack, boolean isPublic) {
        this.post = post;
        this.likeView.setInteractionCount(post.reactionCount);
        this.likeView.setIsLiked(post.reactedByMe);
        this.commentView.setInteractionCount(post.commentCount);
        this.isPublic = isPublic;
        this.inStack = inStack;
        MediaThumbnailLoader mediaThumbnailLoader = isPublic ? parent.getExternalMediaThumbnailLoader() : parent.getMediaThumbnailLoader();
        if (post.media.size() > 1) {
            contentLoadingView.setVisibility(View.VISIBLE);
            mediaThumbnailLoader.load(imageView, post.media.get(1), () -> contentLoadingView.setVisibility(View.GONE));
        } else {
            contentLoadingView.setVisibility(View.GONE);
        }
        cardContent.setOutlineProvider(inStack ? insetRoundedOutlineProvider : null);
        cardView.setStrokeWidth(!inStack ? 0 : (int) cardView.getResources().getDimension(R.dimen.post_stroke_width));
        headerView.setVisibility(inStack ? View.GONE : View.VISIBLE);
        headerFollowButton.setVisibility(isPublic && !parent.wasUserFollowed(post.senderUserId) ? View.VISIBLE : View.GONE);
        avatarContainer.setVisibility(inStack ? View.VISIBLE : View.GONE);
        unlockContainer.setVisibility((inStack || !unlocked) && !unlocking ? View.VISIBLE : View.GONE);
        blurView.setVisibility(inStack || !unlocked ? View.VISIBLE : View.GONE);
        parent.getAvatarLoader().load(headerAvatarView, post.senderUserId);
        parent.getAvatarLoader().load(avatarView, post.senderUserId);
        parent.getGeotagLoader().load(headerGeotag, post.senderUserId);
        parent.getContactLoader().load(unlockMainTextView, post.senderUserId, new ViewDataLoader.Displayer<TextView, Contact>() {
            @Override
            public void showResult(@NonNull TextView view, @Nullable Contact result) {
                if (result != null) {
                    String username = result.username == null ? "" : result.username.toLowerCase(Locale.getDefault());
                    if (inStack && unlocked) {
                        view.setText(view.getContext().getString(R.string.new_from, username));
                    } else {
                        view.setText(view.getContext().getString(R.string.post_to_see, username));
                    }

                    headerUsername.setText(username);
                }
            }

            @Override
            public void showLoading(@NonNull TextView view) {
                view.setText("");
            }
        });

        final CharSequence timeText = TimeFormatter.formatMessageTime(headerTimeAndPlace.getContext(), post.timestamp).toLowerCase(Locale.getDefault());
        String location = ((KatchupPost)post).location;
        if (location != null) {
            headerTimeAndPlace.setText(timeText + " · " + location.toLowerCase(Locale.getDefault()));
        } else {
            headerTimeAndPlace.setText(timeText);
        }

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
                                    entryContainer.setVisibility(View.VISIBLE);
                                    recordVideoReaction.setVisibility(View.VISIBLE);
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
                        entryContainer.setVisibility(View.VISIBLE);
                        recordVideoReaction.setVisibility(View.VISIBLE);
                    }
                } else {
                    showSelfieLoading();
                    mediaThumbnailLoader.load(selfiePreview, selfieMedia, this::hideSelfieLoading);
                    selfiePreview.setVisibility(View.VISIBLE);
                    selfieView.setVisibility(View.GONE);
                    entryContainer.setVisibility(View.GONE);
                    recordVideoReaction.setVisibility(View.GONE);
                }
            }
            serverScoreView.setText(((KatchupPost) post).serverScore);
            serverScoreView.setVisibility(Preferences.getInstance().getShowServerScore() ? View.VISIBLE : View.GONE);
        }
    }

    private void showSelfieLoading() {
        if (selfieLoadingView != null) {
            selfieLoadingView.setVisibility(View.VISIBLE);
        }
    }

    private void hideSelfieLoading() {
        if (selfieLoadingView != null) {
            selfieLoadingView.setVisibility(View.GONE);
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

    public void showReactionTooltip() {
        if (reactionTooltipPopupWindow != null) {
            reactionTooltipPopupWindow.dismiss();
        }

        reactionTooltipPopupWindow = new ReactionTooltipPopupWindow(recordVideoReaction.getContext());
        reactionTooltipPopupWindow.show(recordVideoReaction);
    }
}
