package com.halloapp.katchup;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;

import com.google.android.material.button.MaterialButton;
import com.halloapp.R;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactLoader;
import com.halloapp.content.KatchupPost;
import com.halloapp.content.MomentManager;
import com.halloapp.content.MomentUnlockStatus;
import com.halloapp.content.Post;
import com.halloapp.katchup.avatar.KAvatarLoader;
import com.halloapp.media.MediaThumbnailLoader;
import com.halloapp.ui.BlurManager;
import com.halloapp.ui.ViewHolderWithLifecycle;
import com.halloapp.util.ViewDataLoader;

import java.util.Locale;

import eightbitlab.com.blurview.BlurView;

class KatchupPostViewHolder extends ViewHolderWithLifecycle {
    private final ImageView imageView;
    private final ImageView selfieView;
    private final View selfieContainer;
    private final TextView shareTextView;
    private final View headerView;
    private final ImageView headerAvatarView;
    private final TextView nameView;
    private final TextView lateEmojiView;
    private final TextView locationView;
    private final View unlockContainer;
    private final MaterialButton unlockButton;
    private final ImageView avatarView;

    private final BlurView blurView;
    private final View commentView;

    private Post post;
    private boolean inStack;

    private final Observer<MomentUnlockStatus> unlockedObserver;
    private boolean unlocked;

    private KatchupViewHolderParent parent;

    public abstract static class KatchupViewHolderParent {
        public abstract ContactLoader getContactLoader();
        public abstract MediaThumbnailLoader getMediaThumbnailLoader();
        public abstract KAvatarLoader getAvatarLoader();
        public abstract void startActivity(Intent intent);
    }

    public KatchupPostViewHolder(@NonNull View itemView, KatchupViewHolderParent parent) {
        super(itemView);
        this.parent = parent;

        imageView = itemView.findViewById(R.id.image);
        selfieView = itemView.findViewById(R.id.selfie_preview);
        selfieContainer = itemView.findViewById(R.id.selfie_container);
        shareTextView = itemView.findViewById(R.id.share_text);
        headerView = itemView.findViewById(R.id.moment_header);
        headerAvatarView = itemView.findViewById(R.id.header_avatar);
        nameView = itemView.findViewById(R.id.name);
        lateEmojiView = itemView.findViewById(R.id.late_emoji);
        locationView = itemView.findViewById(R.id.location);
        unlockContainer = itemView.findViewById(R.id.unlock_container);
        unlockButton = itemView.findViewById(R.id.unlock);
        avatarView = itemView.findViewById(R.id.avatar);
        commentView = itemView.findViewById(R.id.comments);

        ViewGroup blurContent = itemView.findViewById(R.id.content);
        blurView = itemView.findViewById(R.id.blur_view);
        BlurManager.getInstance().setupMomentBlur(blurView, blurContent);

        View.OnClickListener listener = v -> {
            if (!unlocked) {
                // TODO(jack): Store moment notif type in prefs so that we can open correct composer
                parent.startActivity(SelfiePostComposerActivity.startText(unlockButton.getContext(), 0, 0));
            } else {
                parent.startActivity(ViewKatchupCommentsActivity.viewPost(unlockButton.getContext(), post));
            }
        };

        commentView.setOnClickListener(listener);
        unlockButton.setOnClickListener(listener);

        Drawable lockedIcon = ContextCompat.getDrawable(unlockButton.getContext(), R.drawable.ic_eye_slash);
        unlockedObserver = unlockStatus -> {
            unlocked = unlockStatus.isUnlocked();
            unlockButton.setIcon(unlocked ? null : lockedIcon);
            handleVisibiility(unlocked, inStack);
        };
    }

    private void handleVisibiility(boolean unlocked, boolean inStack) {
        unlockContainer.setVisibility(inStack || !unlocked ? View.VISIBLE : View.GONE);
        blurView.setVisibility(inStack || !unlocked ? View.VISIBLE : View.GONE);
    }

    public void bindTo(@NonNull Post post, boolean inStack) {
        this.post = post;
        this.inStack = inStack;
        if (post.media.size() > 1) {
            parent.getMediaThumbnailLoader().load(imageView, post.media.get(1));
        }
        headerView.setVisibility(inStack ? View.GONE : View.VISIBLE);
        avatarView.setVisibility(inStack ? View.VISIBLE : View.GONE);
        handleVisibiility(unlocked, inStack);
        parent.getAvatarLoader().load(headerAvatarView, post.senderUserId);
        parent.getAvatarLoader().load(avatarView, post.senderUserId);
        parent.getContactLoader().load(shareTextView, post.senderUserId, new ViewDataLoader.Displayer<TextView, Contact>() {
            @Override
            public void showResult(@NonNull TextView view, @Nullable Contact result) {
                if (result != null) {
                    String shortName = result.getShortName(false).toLowerCase(Locale.getDefault());
                    view.setText(view.getContext().getString(R.string.post_to_see, shortName));
                    nameView.setText(shortName);
                }
            }

            @Override
            public void showLoading(@NonNull TextView view) {
                view.setText("");
            }
        });

        if (post instanceof KatchupPost) {
            String location = ((KatchupPost) post).location;
            if (location != null) {
                locationView.setText(locationView.getContext().getString(R.string.moment_location, location.toLowerCase(Locale.getDefault())));
            }

            parent.getMediaThumbnailLoader().load(selfieView, post.media.get(0));
            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) selfieContainer.getLayoutParams();
            float posX = ((KatchupPost) post).selfieX;
            float posY = ((KatchupPost) post).selfieY;
            layoutParams.horizontalBias = posX;
            layoutParams.verticalBias = posY;
            selfieContainer.setLayoutParams(layoutParams);
        }
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
}
