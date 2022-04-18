package com.halloapp.ui.posts;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.halloapp.R;
import com.halloapp.content.Post;
import com.halloapp.props.ServerProps;
import com.halloapp.ui.ExternalSharingBottomSheetDialogFragment;
import com.halloapp.ui.FlatCommentsActivity;
import com.halloapp.ui.PostSeenByActivity;
import com.halloapp.widget.AvatarsLayout;

public class OutgoingPostFooterViewHolder extends PostFooterViewHolder {

    private static final int MAX_SEEN_BY_AVATARS = 3;

    private final View postActionsSeparator;
    private final View commentButton;
    private final View commentsIndicator;
    private final AvatarsLayout seenIndicator;
    private final View seenButton;
    private final View shareButton;

    public OutgoingPostFooterViewHolder(@NonNull View itemView, @NonNull PostViewHolder.PostViewHolderParent parent) {
        super(itemView, parent);

        postActionsSeparator = itemView.findViewById(R.id.post_actions_separator);
        commentButton = itemView.findViewById(R.id.comment);
        commentsIndicator = itemView.findViewById(R.id.comments_indicator);
        seenIndicator = itemView.findViewById(R.id.seen_indicator);
        seenButton = itemView.findViewById(R.id.seen_button);
        shareButton = itemView.findViewById(R.id.share_button);

        seenIndicator.setAvatarLoader(parent.getAvatarLoader());

        commentButton.setOnClickListener(view -> {
            final Intent intent = FlatCommentsActivity.viewComments(itemView.getContext(), post.id, post.senderUserId);
            intent.putExtra(FlatCommentsActivity.EXTRA_SHOW_KEYBOARD, post.commentCount == 0);
            parent.startActivity(intent);
        });

        final View.OnClickListener seenClickListener = v -> {
            final Intent intent = new Intent(itemView.getContext(), PostSeenByActivity.class);
            intent.putExtra(PostSeenByActivity.EXTRA_POST_ID, post.id);
            parent.startActivity(intent);
        };
        seenIndicator.setOnClickListener(seenClickListener);
        seenButton.setOnClickListener(seenClickListener);
        shareButton.setOnClickListener(v -> parent.showDialogFragment(ExternalSharingBottomSheetDialogFragment.newInstance(post.id)));
    }

    @Override
    public void setCanInteract(boolean canInteract) {
        commentButton.setAlpha(canInteract ? 1f : DISABLED_OPACITY);
        commentButton.setEnabled(canInteract);
        seenButton.setAlpha(canInteract ? 1f : DISABLED_OPACITY);
        seenButton.setEnabled(canInteract);
        seenIndicator.setAlpha(canInteract ? 1f : DISABLED_OPACITY);
        seenIndicator.setEnabled(canInteract);
        shareButton.setAlpha(canInteract ? 1f : DISABLED_OPACITY);
        shareButton.setEnabled(canInteract);
    }

    public void bindTo(@NonNull Post post) {
        super.bindTo(post);

        shareButton.setVisibility(ServerProps.getInstance().getExternalSharing() ? View.VISIBLE : View.GONE);
        if (post.seenByCount > 0) {
            seenIndicator.setVisibility(View.VISIBLE);
            seenButton.setVisibility(View.GONE);
            seenIndicator.setContentDescription(itemView.getContext().getResources().getQuantityString(R.plurals.seen_by_people, post.seenByCount, post.seenByCount));
            seenIndicator.setAvatarCount(Math.min(post.seenByCount, MAX_SEEN_BY_AVATARS));
            if (post.seenByCount > MAX_SEEN_BY_AVATARS) {
                final ImageView imageView0 = (ImageView)seenIndicator.getChildAt(0);
                imageView0.setImageTintMode(PorterDuff.Mode.OVERLAY);
                imageView0.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(imageView0.getContext(), R.color.dim_facepile_avatar_0)));
                final ImageView imageView1 = (ImageView)seenIndicator.getChildAt(1);
                imageView1.setImageTintMode(PorterDuff.Mode.OVERLAY);
                imageView1.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(imageView0.getContext(), R.color.dim_facepile_avatar_1)));
            } else {
                if (seenIndicator.getChildCount() > 0) {
                    final ImageView imageView = (ImageView)seenIndicator.getChildAt(0);
                    imageView.setImageTintList(null);
                }
                if (seenIndicator.getChildCount() > 1) {
                    final ImageView imageView = (ImageView)seenIndicator.getChildAt(1);
                    imageView.setImageTintList(null);
                }
            }
            parent.getSeenByLoader().load(seenIndicator, post.id);
        } else {
            seenIndicator.setVisibility(View.GONE);
            seenButton.setVisibility(View.VISIBLE);
        }
        if (TextUtils.isEmpty(post.text) || post.urlPreview != null) {
            postActionsSeparator.setVisibility(View.GONE);
            footerSpacing.setVisibility(View.GONE);
        } else {
            postActionsSeparator.setVisibility(View.VISIBLE);
            footerSpacing.setVisibility(View.VISIBLE);
        }

        if (post.unseenCommentCount > 0) {
            commentsIndicator.setVisibility(View.VISIBLE);
            commentsIndicator.setBackgroundResource(R.drawable.new_comments_indicator);
        } else if (post.commentCount > 0) {
            commentsIndicator.setVisibility(View.VISIBLE);
            commentsIndicator.setBackgroundResource(R.drawable.old_comments_indicator);
        } else {
            commentsIndicator.setVisibility(View.INVISIBLE);
        }
    }
}
