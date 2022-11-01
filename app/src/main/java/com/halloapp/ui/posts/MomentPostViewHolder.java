package com.halloapp.ui.posts;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Outline;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;

import com.google.android.material.button.MaterialButton;
import com.halloapp.R;
import com.halloapp.contacts.Contact;
import com.halloapp.content.Media;
import com.halloapp.content.MomentManager;
import com.halloapp.content.MomentPost;
import com.halloapp.content.MomentUnlockStatus;
import com.halloapp.content.Post;
import com.halloapp.ui.BlurManager;
import com.halloapp.ui.MomentViewerActivity;
import com.halloapp.ui.PostSeenByActivity;
import com.halloapp.ui.ViewHolderWithLifecycle;
import com.halloapp.ui.camera.CameraActivity;
import com.halloapp.ui.profile.ViewProfileActivity;
import com.halloapp.util.ContextUtils;
import com.halloapp.util.ViewDataLoader;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.AvatarsLayout;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import eightbitlab.com.blurview.BlurView;

public class MomentPostViewHolder extends ViewHolderWithLifecycle {

    private final ImageView imageViewFirst, imageViewSecond;
    private final View imageDivider;
    private final TextView lineOne;

    private final ImageView avatarView;

    private final MaterialButton unlockButton;

    private final PostViewHolder.PostViewHolderParent parent;

    private final TextView shareTextView;
    private final TextView shareSubtitleTextView;

    private MomentPost moment;

    private MomentUnlockStatus momentUnlockStatus;

    private final BlurView blurView;

    private final SimpleDateFormat dayFormatter;

    private final Observer<MomentUnlockStatus> unlockedObserver;

    private final View unlockContainer;

    private final AvatarsLayout seenByLayout;
    private final View seenByBtn;

    private String senderName;

    public MomentPostViewHolder(@NonNull View itemView, @NonNull PostViewHolder.PostViewHolderParent parent) {
        super(itemView);

        this.parent = parent;

        dayFormatter = new SimpleDateFormat("EEEE", Locale.getDefault());

        lineOne = itemView.findViewById(R.id.line_one);

        imageViewFirst = itemView.findViewById(R.id.image_first);
        imageViewSecond = itemView.findViewById(R.id.image_second);
        imageDivider = itemView.findViewById(R.id.image_divider);
        avatarView = itemView.findViewById(R.id.avatar);

        unlockButton = itemView.findViewById(R.id.unlock);
        shareTextView = itemView.findViewById(R.id.share_text);
        shareSubtitleTextView = itemView.findViewById(R.id.share_subtitle_text);

        LinearLayout blurContent = itemView.findViewById(R.id.blur_content);
        blurView = itemView.findViewById(R.id.blurView);
        BlurManager.getInstance().setupMomentBlur(blurView, blurContent);

        seenByBtn = itemView.findViewById(R.id.seen_button);
        seenByLayout = itemView.findViewById(R.id.seen_indicator);
        seenByLayout.setAvatarLoader(parent.getAvatarLoader());
        View.OnClickListener seenByClickListener = v -> {
            final Intent intent = new Intent(v.getContext(), PostSeenByActivity.class);
            if (moment != null) {
                intent.putExtra(PostSeenByActivity.EXTRA_POST_ID, moment.id);
                parent.startActivity(intent);
            } else {
                Log.i("MomentPostViewHolder/seenOnClick null post");
            }
        };
        seenByBtn.setOnClickListener(seenByClickListener);
        seenByLayout.setOnClickListener(seenByClickListener);

        unlockContainer = itemView.findViewById(R.id.unlock_container);

        Drawable lockedIcon = unlockButton.getResources().getDrawable(R.drawable.ic_eye_slash);
        unlockedObserver = unlockStatus -> {
            this.momentUnlockStatus = unlockStatus;
            boolean unlocked = unlockStatus.isUnlocked();
            boolean seen = moment != null && (moment.seen == Post.SEEN_YES_PENDING || moment.seen == Post.SEEN_YES);
            unlockButton.setIcon(unlocked ? null : lockedIcon);
            shareSubtitleTextView.setVisibility(unlocked ? View.GONE : View.VISIBLE);
            unlockContainer.setVisibility(unlocked && seen ? View.GONE : View.VISIBLE);
            blurView.setVisibility(unlocked && seen ? View.GONE : View.VISIBLE);
        };

        float mediaRadius = itemView.getResources().getDimension(R.dimen.moment_media_corner_radius);
        View imageContainer = itemView.findViewById(R.id.image_container);
        imageContainer.setClipToOutline(true);
        imageContainer.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), mediaRadius);
            }
        });

        imageContainer.setOnClickListener(v -> {
            if (moment == null) {
                return;
            }

            boolean unlocked = momentUnlockStatus != null && momentUnlockStatus.isUnlocked();
            boolean seen = moment.seen == Post.SEEN_YES_PENDING || moment.seen == Post.SEEN_YES;

            if (moment.isIncoming() && (!unlocked || !seen)) {
                return;
            }

            Activity activity = ContextUtils.getActivity(v.getContext());
            if (activity != null) {
                MomentViewerActivity.viewMomentWithTransition(activity, moment.id, imageContainer);
            } else {
                v.getContext().startActivity(MomentViewerActivity.viewMoment(v.getContext(), moment.id));
            }
        });
        unlockButton.setOnClickListener(v -> {
            if (moment != null) {
                if (momentUnlockStatus.unlockingMomentId != null) {
                    unlockContainer.setVisibility(View.GONE);
                    blurView.setVisibility(View.GONE);

                    Activity activity = ContextUtils.getActivity(v.getContext());
                    if (activity != null) {
                        MomentViewerActivity.viewMomentWithTransition(activity, moment.id, imageContainer);
                    } else {
                        v.getContext().startActivity(MomentViewerActivity.viewMoment(v.getContext(), moment.id));
                    }
                } else {
                    Intent i = new Intent(v.getContext(), CameraActivity.class);
                    i.putExtra(CameraActivity.EXTRA_PURPOSE, CameraActivity.PURPOSE_MOMENT);
                    i.putExtra(CameraActivity.EXTRA_TARGET_MOMENT, moment.id);
                    i.putExtra(CameraActivity.EXTRA_TARGET_MOMENT_SENDER_NAME, senderName);
                    i.putExtra(CameraActivity.EXTRA_TARGET_MOMENT_USER_ID, moment.senderUserId);
                    v.getContext().startActivity(i);
                }
            }
        });
    }

    @Override
    public void markAttach() {
        super.markAttach();
        MomentManager.getInstance().isUnlockedLiveData().observe(this, unlockedObserver);
    }

    @Override
    public void markDetach() {
        super.markDetach();
        MomentManager.getInstance().isUnlockedLiveData().removeObserver(unlockedObserver);
    }

    public void bindTo(Post post) {
        moment = (MomentPost) post;
        avatarView.setOnClickListener(null);

        if (moment.isOutgoing() || moment.isAllMediaTransferred()) {
            imageViewFirst.setVisibility(View.VISIBLE);
            parent.getMediaThumbnailLoader().load(imageViewFirst, moment.media.get(0));

            if (moment.media.size() > 1) {
                imageDivider.setVisibility(View.VISIBLE);
                imageViewSecond.setVisibility(View.VISIBLE);
                parent.getMediaThumbnailLoader().load(imageViewSecond, moment.media.get(1));

                Media selfie = moment.getSelfie();
                if (selfie != null) {
                    parent.getMediaThumbnailLoader().load(avatarView, selfie);
                    avatarView.setOnClickListener(v -> v.getContext().startActivity(ViewProfileActivity.viewProfile(v.getContext(), moment.senderUserId)));
                } else {
                    parent.getAvatarLoader().load(avatarView, moment.senderUserId);
                }
            } else {
                imageDivider.setVisibility(View.GONE);
                imageViewSecond.setVisibility(View.GONE);
                parent.getAvatarLoader().load(avatarView, moment.senderUserId);
            }
        } else {
            imageDivider.setVisibility(View.GONE);
            imageViewFirst.setVisibility(View.GONE);
            imageViewSecond.setVisibility(View.GONE);
            parent.getAvatarLoader().load(avatarView, moment.senderUserId);
        }

        if (moment.isIncoming()) {
            boolean unlocked = momentUnlockStatus != null && momentUnlockStatus.isUnlocked();
            boolean seen = moment.seen == Post.SEEN_YES_PENDING || moment.seen == Post.SEEN_YES;

            unlockContainer.setVisibility(unlocked && seen ? View.GONE : View.VISIBLE);
            blurView.setVisibility(unlocked && seen ? View.GONE : View.VISIBLE);
            lineOne.setVisibility(View.VISIBLE);
            seenByLayout.setVisibility(View.GONE);
            seenByBtn.setVisibility(View.GONE);
            parent.getContactLoader().load(shareTextView, moment.senderUserId, new ViewDataLoader.Displayer<TextView, Contact>() {
                @Override
                public void showResult(@NonNull TextView view, @Nullable Contact result) {
                    if (result != null) {
                        boolean showTilde = TextUtils.isEmpty(moment.psaTag);
                        senderName = result.getShortName(showTilde);
                        String name = result.getDisplayName(showTilde);
                        view.setText(view.getContext().getString(R.string.instant_post_from, name));
                        shareSubtitleTextView.setText(view.getContext().getString(R.string.unlock_all_moment_subtitle));
                    }
                }

                @Override
                public void showLoading(@NonNull TextView view) {
                    senderName = null;
                    view.setText("");
                }
            });

            if (!TextUtils.isEmpty(moment.location)) {
                lineOne.setText(moment.location);
            } else {
                lineOne.setText(dayFormatter.format(new Date(moment.timestamp)));
            }
        } else {
            unlockContainer.setVisibility(View.GONE);
            parent.getContactLoader().cancel(shareTextView);
            shareTextView.setText(R.string.instant_post_you);
            shareSubtitleTextView.setVisibility(View.GONE);
            blurView.setVisibility(View.GONE);
            lineOne.setVisibility(View.GONE);
            if (moment.seenByCount > 0) {
                seenByLayout.setVisibility(View.VISIBLE);
                seenByBtn.setVisibility(View.GONE);
                seenByLayout.setAvatarCount(Math.min(moment.seenByCount, 3));
                parent.getSeenByLoader().load(seenByLayout, moment.id);
            } else {
                seenByLayout.setVisibility(View.GONE);
                seenByBtn.setVisibility(View.VISIBLE);
            }
        }
    }
}
