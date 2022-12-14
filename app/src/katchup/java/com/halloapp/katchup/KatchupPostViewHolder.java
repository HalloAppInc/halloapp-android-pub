package com.halloapp.katchup;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.halloapp.R;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactLoader;
import com.halloapp.content.KatchupPost;
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

    private final BlurView blurView;
    private final View commentView;

    private Post post;

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
        commentView = itemView.findViewById(R.id.comments);

        LinearLayout blurContent = itemView.findViewById(R.id.image_container);
        blurView = itemView.findViewById(R.id.blur_view);
        BlurManager.getInstance().setupMomentBlur(blurView, blurContent);

        commentView.setOnClickListener(v -> {
            parent.startActivity(ViewKatchupCommentsActivity.viewPost(commentView.getContext(), post));
        });
    }

    public void bindTo(@NonNull Post post, boolean inStack) {
        this.post = post;
        if (post.media.size() > 1) {
            parent.getMediaThumbnailLoader().load(imageView, post.media.get(1));
        }
        headerView.setVisibility(inStack ? View.GONE : View.VISIBLE);
        parent.getAvatarLoader().load(headerAvatarView, post.senderUserId);
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
}
