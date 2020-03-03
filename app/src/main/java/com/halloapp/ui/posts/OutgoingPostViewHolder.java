package com.halloapp.ui.posts;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;

import com.halloapp.R;
import com.halloapp.contacts.UserId;
import com.halloapp.posts.Post;
import com.halloapp.ui.CommentsActivity;
import com.halloapp.ui.PostDetailsActivity;
import com.halloapp.widget.AvatarsLayout;

public class OutgoingPostViewHolder extends PostViewHolder {

    private static final int MAX_SEEN_BY_AVATARS = 4;

    private final View addCommentButton;
    private final View viewCommentsButton;
    private final AvatarsLayout seenIndicator;

    public OutgoingPostViewHolder(@NonNull View itemView, @NonNull PostViewHolderParent parent) {
        super(itemView, parent);

        addCommentButton = itemView.findViewById(R.id.add_comment);
        viewCommentsButton = itemView.findViewById(R.id.view_comments);
        seenIndicator = itemView.findViewById(R.id.seen_indicator);

        final View.OnClickListener commentsClickListener = v -> {
            final Intent intent = new Intent(itemView.getContext(), CommentsActivity.class);
            intent.putExtra(CommentsActivity.EXTRA_POST_SENDER_USER_ID, post.senderUserId.rawId());
            intent.putExtra(CommentsActivity.EXTRA_POST_ID, post.postId);
            intent.putExtra(CommentsActivity.EXTRA_SHOW_KEYBOARD, post.commentCount == 0);
            parent.startActivity(intent);
        };
        addCommentButton.setOnClickListener(commentsClickListener);
        viewCommentsButton.setOnClickListener(commentsClickListener);

        seenIndicator.setOnClickListener(v1 -> {
            final Intent intent = new Intent(itemView.getContext(), PostDetailsActivity.class);
            intent.putExtra(PostDetailsActivity.EXTRA_POST_ID, post.postId);
            parent.startActivity(intent);
        });

        final ImageView myAvatarView = itemView.findViewById(R.id.my_avatar);
        myAvatarView.setImageResource(R.drawable.avatar_person); // TODO (ds): load profile photo
        parent.getAvatarLoader().loadAvatarFor(UserId.ME, this, myAvatarView::setImageBitmap);
    }

    @CallSuper
    public void bindTo(@NonNull Post post) {
        super.bindTo(post);

        if (post.commentCount == 0) {
            addCommentButton.setVisibility(View.VISIBLE);
            viewCommentsButton.setVisibility(View.GONE);
        } else {
            addCommentButton.setVisibility(View.GONE);
            viewCommentsButton.setVisibility(View.VISIBLE);
        }

        if (post.seenByCount > 0) {
            seenIndicator.setVisibility(View.VISIBLE);
            footerSpacing.setVisibility(View.GONE);
            seenIndicator.setContentDescription(itemView.getContext().getResources().getQuantityString(R.plurals.seen_by, post.seenByCount, post.seenByCount));
            seenIndicator.setAvatarCount(Math.min(post.seenByCount, MAX_SEEN_BY_AVATARS));
            if (seenIndicator.getChildCount() == MAX_SEEN_BY_AVATARS) {
                seenIndicator.getChildAt(0).setAlpha(.5f);
            }
        } else {
            seenIndicator.setVisibility(View.GONE);
            footerSpacing.setVisibility(TextUtils.isEmpty(post.text) ? View.GONE : View.VISIBLE);
        }
    }
}

