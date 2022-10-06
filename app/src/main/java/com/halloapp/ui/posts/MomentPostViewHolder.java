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
import com.halloapp.ui.PostOptionsBottomSheetDialogFragment;
import com.halloapp.ui.PostSeenByActivity;
import com.halloapp.ui.ViewHolderWithLifecycle;
import com.halloapp.ui.camera.CameraActivity;
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

    private final LinearLayout myMomentHeader;
    private final ImageView myAvatar;
    private final PostAttributionLayout postHeader;

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
        avatarView = itemView.findViewById(R.id.avatar);

        unlockButton = itemView.findViewById(R.id.unlock);
        shareTextView = itemView.findViewById(R.id.share_text);
        shareSubtitleTextView = itemView.findViewById(R.id.share_subtitle_text);

        LinearLayout blurContent = itemView.findViewById(R.id.blur_content);
        blurView = itemView.findViewById(R.id.blurView);
        BlurManager.getInstance().setupMomentBlur(blurView, blurContent);

        myMomentHeader = itemView.findViewById(R.id.my_moment_header);
        myAvatar = myMomentHeader.findViewById(R.id.my_avatar);
        postHeader = myMomentHeader.findViewById(R.id.post_header);
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

        View moreOptions = itemView.findViewById(R.id.more_options);
        moreOptions.setOnClickListener(v -> {
            parent.showDialogFragment(PostOptionsBottomSheetDialogFragment.newInstance(moment.id, moment.isArchived));
        });

        Drawable lockedIcon = unlockButton.getResources().getDrawable(R.drawable.ic_eye_slash);
        unlockedObserver = unlockStatus -> {
            this.momentUnlockStatus = unlockStatus;
            boolean unlocked = unlockStatus.isUnlocked();
            unlockButton.setIcon(unlocked ? null : lockedIcon);
            shareSubtitleTextView.setVisibility(unlocked ? View.GONE : View.VISIBLE);
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

        imageContainer.setTransitionName(MomentViewerActivity.MOMENT_TRANSITION_NAME);
        imageContainer.setOnClickListener(v -> {
            if (moment == null || moment.isIncoming()) {
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
                    v.getContext().startActivity(MomentViewerActivity.viewMoment(v.getContext(), moment.id));
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
        parent.getMediaThumbnailLoader().load(imageViewFirst, moment.media.get(0));

        if (moment.media.size() > 1) {
            imageViewSecond.setVisibility(View.VISIBLE);
            parent.getMediaThumbnailLoader().load(imageViewSecond, moment.media.get(1));

            Media selfie = moment.getSelfie();
            if (selfie != null) {
                parent.getMediaThumbnailLoader().load(avatarView, selfie);
            }
        } else {
            imageViewSecond.setVisibility(View.GONE);
            parent.getAvatarLoader().load(avatarView, moment.senderUserId);
        }

        if (moment.isIncoming()) {
            unlockContainer.setVisibility(View.VISIBLE);
            parent.getContactLoader().cancel(postHeader.getNameView());
            blurView.setVisibility(View.VISIBLE);
            myMomentHeader.setVisibility(View.GONE);
            seenByLayout.setVisibility(View.GONE);
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
        } else {
            unlockContainer.setVisibility(View.GONE);
            parent.getAvatarLoader().load(myAvatar, moment.senderUserId);
            parent.getContactLoader().load(postHeader.getNameView(), moment.senderUserId);
            seenByLayout.setVisibility(View.VISIBLE);
            myMomentHeader.setVisibility(View.VISIBLE);
            parent.getContactLoader().cancel(shareTextView);
            shareTextView.setText(R.string.instant_post_you);
            shareSubtitleTextView.setVisibility(View.GONE);
            blurView.setVisibility(View.GONE);
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

        if (!TextUtils.isEmpty(moment.location)) {
            lineOne.setText(moment.location);
        } else {
            lineOne.setText(dayFormatter.format(new Date(moment.timestamp)));
        }

    }
}
