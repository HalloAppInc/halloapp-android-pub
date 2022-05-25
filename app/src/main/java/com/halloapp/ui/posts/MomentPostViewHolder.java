package com.halloapp.ui.posts;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import androidx.lifecycle.Observer;

import com.halloapp.R;
import com.halloapp.contacts.Contact;
import com.halloapp.content.ContentDb;
import com.halloapp.content.MomentManager;
import com.halloapp.content.Post;
import com.halloapp.ui.BlurManager;
import com.halloapp.ui.MomentViewerActivity;
import com.halloapp.ui.PostOptionsBottomSheetDialogFragment;
import com.halloapp.ui.PostSeenByActivity;
import com.halloapp.ui.ViewHolderWithLifecycle;
import com.halloapp.ui.camera.CameraActivity;
import com.halloapp.util.ContextUtils;
import com.halloapp.util.TimeFormatter;
import com.halloapp.util.ViewDataLoader;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.AvatarsLayout;
import com.halloapp.widget.ContentPhotoView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import eightbitlab.com.blurview.BlurView;

public class MomentPostViewHolder extends ViewHolderWithLifecycle {

    private ContentPhotoView imageView;
    private TextView lineOne;
    private TextView lineTwo;

    private ImageView avatarView;

    private Button unlockButton;

    private PostViewHolder.PostViewHolderParent parent;

    private TextView shareTextView;

    private Post post;

    private boolean unlocked;

    private BlurView blurView;

    private final SimpleDateFormat dayFormatter;

    private final Observer<Boolean> unlockedObserver;

    private View unlockContainer;

    private LinearLayout myMomentHeader;
    private ImageView myAvatar;
    private PostAttributionLayout postHeader;

    private AvatarsLayout seenByLayout;

    private String senderName;

    public MomentPostViewHolder(@NonNull View itemView, @NonNull PostViewHolder.PostViewHolderParent parent) {
        super(itemView);

        this.parent = parent;

        dayFormatter = new SimpleDateFormat("EEEE", Locale.getDefault());

        lineOne = itemView.findViewById(R.id.line_one);
        lineTwo = itemView.findViewById(R.id.line_two);

        imageView = itemView.findViewById(R.id.image);
        imageView.setDrawDelegate(parent.getDrawDelegateView());
        avatarView = itemView.findViewById(R.id.avatar);

        unlockButton = itemView.findViewById(R.id.unlock);
        shareTextView = itemView.findViewById(R.id.share_text);

        LinearLayout blurContent = itemView.findViewById(R.id.blur_content);
        blurView = itemView.findViewById(R.id.blurView);
        BlurManager.getInstance().setupMomentBlur(blurView, blurContent);

        myMomentHeader = itemView.findViewById(R.id.my_moment_header);
        myAvatar = myMomentHeader.findViewById(R.id.my_avatar);
        postHeader = myMomentHeader.findViewById(R.id.post_header);
        seenByLayout = itemView.findViewById(R.id.seen_indicator);
        seenByLayout.setAvatarLoader(parent.getAvatarLoader());
        seenByLayout.setOnClickListener(v -> {
            final Intent intent = new Intent(v.getContext(), PostSeenByActivity.class);
            if (post != null) {
                intent.putExtra(PostSeenByActivity.EXTRA_POST_ID, post.id);
                parent.startActivity(intent);
            } else {
                Log.i("MomentPostViewHolder/seenOnClick null post");
            }
        });

        unlockContainer = itemView.findViewById(R.id.unlock_container);

        View moreOptions = itemView.findViewById(R.id.more_options);
        moreOptions.setOnClickListener(v -> {
            parent.showDialogFragment(PostOptionsBottomSheetDialogFragment.newInstance(post.id, post.isArchived));
        });

        unlockedObserver = isUnlocked -> {
            unlocked = isUnlocked;
            unlockButton.setText(isUnlocked ? R.string.view_action : R.string.unlock_action);
        };

        imageView.setTransitionName(MomentViewerActivity.MOMENT_TRANSITION_NAME);
        imageView.setOnClickListener(v -> {
            if (post == null || post.isIncoming()) {
                return;
            }
            Activity activity = ContextUtils.getActivity(v.getContext());
            if (activity != null) {
                MomentViewerActivity.viewMomentWithTransition(activity, post.id, imageView);
            } else {
                v.getContext().startActivity(MomentViewerActivity.viewMoment(v.getContext(), post.id));
            }
        });
        unlockButton.setOnClickListener(v -> {
            if (post != null) {
                if (unlocked) {
                    ContentDb.getInstance().hideMomentOnView(post);
                    v.getContext().startActivity(MomentViewerActivity.viewMoment(v.getContext(), post.id));
                } else {
                    Intent i = new Intent(v.getContext(), CameraActivity.class);
                    i.putExtra(CameraActivity.EXTRA_PURPOSE, CameraActivity.PURPOSE_MOMENT);
                    i.putExtra(CameraActivity.EXTRA_TARGET_MOMENT, post.id);
                    i.putExtra(CameraActivity.EXTRA_TARGET_MOMENT_SENDER_NAME, senderName);
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
        this.post = post;
        parent.getMediaThumbnailLoader().load(imageView, post.media.get(0));
        parent.getAvatarLoader().load(avatarView, post.senderUserId);
        if (post.isIncoming()) {
            unlockContainer.setVisibility(View.VISIBLE);
            parent.getContactLoader().cancel(postHeader.getNameView());
            blurView.setVisibility(View.VISIBLE);
            myMomentHeader.setVisibility(View.GONE);
            seenByLayout.setVisibility(View.GONE);
            imageView.setZoomable(false);
            parent.getContactLoader().load(shareTextView, post.senderUserId, new ViewDataLoader.Displayer<TextView, Contact>() {
                @Override
                public void showResult(@NonNull TextView view, @Nullable Contact result) {
                    if (result != null) {
                        senderName = result.getShortName();
                        view.setText(view.getContext().getString(R.string.instant_post_from, result.getDisplayName()));
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
            parent.getAvatarLoader().load(myAvatar, post.senderUserId);
            parent.getContactLoader().load(postHeader.getNameView(), post.senderUserId);
            seenByLayout.setVisibility(View.VISIBLE);
            imageView.setZoomable(true);
            myMomentHeader.setVisibility(View.VISIBLE);
            parent.getContactLoader().cancel(shareTextView);
            shareTextView.setText(R.string.instant_post_you);
            blurView.setVisibility(View.GONE);
            seenByLayout.setAvatarCount(Math.min(post.seenByCount, 3));
            parent.getSeenByLoader().load(seenByLayout, post.id);
        }
        lineTwo.setText(TimeFormatter.formatMessageTime(lineOne.getContext(), post.timestamp));
        lineOne.setText(dayFormatter.format(new Date(post.timestamp)));
    }
}
