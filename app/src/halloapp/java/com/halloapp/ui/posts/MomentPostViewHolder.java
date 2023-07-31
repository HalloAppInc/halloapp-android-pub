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
import com.halloapp.content.MomentPost;
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

    private MomentPost moment;

    private final BlurView blurView;

    private final SimpleDateFormat dayFormatter;


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

            Activity activity = ContextUtils.getActivity(v.getContext());
            if (activity != null) {
                MomentViewerActivity.viewMomentWithTransition(activity, moment.id, imageContainer);
            } else {
                v.getContext().startActivity(MomentViewerActivity.viewMoment(v.getContext(), moment.id));
            }
        });
        unlockButton.setOnClickListener(v -> {
            if (moment != null) {
                unlockContainer.setVisibility(View.GONE);
                blurView.setVisibility(View.GONE);

                Activity activity = ContextUtils.getActivity(v.getContext());
                if (activity != null) {
                    MomentViewerActivity.viewMomentWithTransition(activity, moment.id, imageContainer);
                } else {
                    v.getContext().startActivity(MomentViewerActivity.viewMoment(v.getContext(), moment.id));
                }
            }
        });
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
            boolean seen = moment.seen == Post.SEEN_YES_PENDING || moment.seen == Post.SEEN_YES;

            unlockContainer.setVisibility(seen ? View.GONE : View.VISIBLE);
            blurView.setVisibility(seen ? View.GONE : View.VISIBLE);
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
