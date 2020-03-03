package com.halloapp.ui.posts;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;

import com.halloapp.R;
import com.halloapp.contacts.UserId;
import com.halloapp.posts.Comment;
import com.halloapp.posts.Post;
import com.halloapp.ui.CommentsActivity;
import com.halloapp.ui.PostDetailsActivity;
import com.halloapp.util.TimeFormatter;
import com.halloapp.widget.AvatarsLayout;

public class OutgoingPostViewHolder extends PostViewHolder {

    private static final int MAX_SEEN_BY_AVATARS = 4;

    private final View addCommentButton;
    private final View viewCommentsButton;
    private final View viewCommentsInvicator;
    private final AvatarsLayout seenIndicator;
    private final View firstCommentContent;
    private final ImageView firstCommentAvatar;
    private final TextView firstCommentName;
    private final TextView firstCommentText;
    private final TextView firstCommentTimestamp;

    public OutgoingPostViewHolder(@NonNull View itemView, @NonNull PostViewHolderParent parent) {
        super(itemView, parent);

        addCommentButton = itemView.findViewById(R.id.add_comment);
        viewCommentsButton = itemView.findViewById(R.id.view_comments);
        viewCommentsInvicator = itemView.findViewById(R.id.comments_indicator);
        seenIndicator = itemView.findViewById(R.id.seen_indicator);
        firstCommentContent = itemView.findViewById(R.id.comment_content);
        firstCommentAvatar = itemView.findViewById(R.id.comment_avatar);
        firstCommentName = itemView.findViewById(R.id.comment_name);
        firstCommentText = itemView.findViewById(R.id.comment_text);
        firstCommentTimestamp = itemView.findViewById(R.id.comment_time);

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

        itemView.findViewById(R.id.comment_reply).setOnClickListener(v -> {
            final Intent intent = new Intent(itemView.getContext(), CommentsActivity.class);
            intent.putExtra(CommentsActivity.EXTRA_POST_SENDER_USER_ID, post.senderUserId.rawId());
            intent.putExtra(CommentsActivity.EXTRA_POST_ID, post.postId);
            intent.putExtra(CommentsActivity.EXTRA_REPLY_USER_ID, post.firstComment.commentSenderUserId.rawId());
            intent.putExtra(CommentsActivity.EXTRA_REPLY_COMMENT_ID, post.firstComment.commentId);
            intent.putExtra(CommentsActivity.EXTRA_SHOW_KEYBOARD, true);
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

        viewCommentsInvicator.setVisibility(post.unseenCommentCount > 0 ? View.VISIBLE : View.GONE);

        final Comment firstComment = post.firstComment;
        if (firstComment != null) {
            firstCommentContent.setVisibility(View.VISIBLE);

            firstCommentAvatar.setImageResource(R.drawable.avatar_person); // TODO (ds): load profile photo
            parent.getAvatarLoader().loadAvatarFor(firstComment.commentSenderUserId, this, firstCommentAvatar::setImageBitmap);

            firstCommentText.setText(firstComment.text);

            if (firstComment.commentSenderUserId.isMe()) {
                firstCommentName.setText(firstCommentName.getContext().getString(R.string.me));
            } else {
                parent.getContactLoader().load(firstCommentName, firstComment.commentSenderUserId);
            }

            firstCommentTimestamp.setText(TimeFormatter.formatTimeDiff(firstCommentTimestamp.getContext(), System.currentTimeMillis() - firstComment.timestamp));
            parent.getTimestampRefresher().scheduleTimestampRefresh(firstComment.timestamp);
        } else {
            firstCommentContent.setVisibility(View.GONE);
        }
    }
}

